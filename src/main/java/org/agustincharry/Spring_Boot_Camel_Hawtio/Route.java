package org.agustincharry.Spring_Boot_Camel_Hawtio;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Route extends RouteBuilder {

    @Autowired
    CamelContext context;

    @SuppressWarnings("unchecked")
    @Override
    public void configure() throws Exception {

        from("jetty:http://0.0.0.0:9080/queue_distributor")
            .convertBodyTo(String.class)
            .unmarshal().json(JsonLibrary.Jackson, XmlParseRequest.class)
            .to("direct:streamAndValidate");

        from("direct:streamAndValidate")
            .setProperty("XSD_LOCATION").simple("${body.xsdUrl}")
            .setHeader("XML_LOCATION").simple("${body.xmlUrl}")
            .setBody().constant(null)
            .log("DOWNLOADING ${headers.XML_LOCATION}")
            .toD("http4://${headers.XML_LOCATION}?copyHeaders=false&bridgeEndpoint=true").streamCaching()
                .unmarshal().gzip()
                .split()
                    .tokenizeXML("ProteinEntry")
                    .streaming().parallelProcessing()
                    .aggregationStrategy((oldExchange, newExchange) -> {
                        List<Boolean> validity = null;
                        if(oldExchange == null){
                            validity = new ArrayList<>();
                        } else {
                            validity = oldExchange.getIn().getBody(List.class);
                        }
                        validity.add(newExchange.getIn().getHeader("XSD_VALID", Boolean.class));
                        newExchange.getIn().setBody(validity);
                        return newExchange;
                    })
                    .doTry()
                        .setHeader("XSD_VALID").constant(true)
                        .toD("validator:${exchangeProperty.XSD_LOCATION}")
                    .doCatch(org.apache.camel.ValidationException.class)
                        .setHeader("XSD_VALID").constant(false)
                    .endDoTry()
                    .end()
                .end()
                .log("Done streaming, calculating score")
                .process(exchange -> {
                    List<Boolean> validity = exchange.getIn().getBody(List.class);
                    exchange.getIn().setHeader("TOTAL", validity.stream().parallel().count());
                    exchange.getIn().setHeader("VALID", validity.stream().parallel().filter(Boolean::booleanValue).count());
                    exchange.getIn().setHeader("INVALID", validity.stream().parallel().filter(b -> !b).count());
                })
                .setBody().simple("Parsed ${headers.TOTAL} elements!\n${headers.INVALID} of ${headers.TOTAL} were invalid\n${headers.VALID} of ${headers.TOTAL} were valid");

    }
}
