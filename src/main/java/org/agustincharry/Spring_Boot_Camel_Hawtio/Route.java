package org.agustincharry.Spring_Boot_Camel_Hawtio;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Route extends RouteBuilder {

    @Autowired
    CamelContext context;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
            .process(exchange -> {
                Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                exchange.setProperty("EXCHANGE_EXCEPTION_MESSAGE", cause.getMessage());
            })
            .to("ibmmq:queue:errorQueue")
            .log(LoggingLevel.ERROR, "${exchangeProperty.EXCHANGE_EXCEPTION_MESSAGE} was logged");

        from("file:/Users/cpenhale/file-in")
            .to("file:/Users/cpenhale/file-out");

        from("jetty:http://0.0.0.0:9080/queue_distributor")
            .convertBodyTo(String.class)
            .unmarshal().json(JsonLibrary.Jackson, TestMessage.class)
            .setProperty("QUEUE_NUMBER").simple("${body.queueNumber}")
            .setProperty("SEND_TO_ALL").simple("${body.sendToAll}")
            .setBody().simple("${body.message}")
            .multicast()
                .shareUnitOfWork()
                .streaming()
                .parallelProcessing()
                    .to("direct:one")
                    .to("direct:two")
                    .to("direct:three")
                .end();

        from("direct:one")
            .filter().simple("${exchangeProperty.QUEUE_NUMBER} > 0")
                .log("My Queue Number is #${exchangeProperty.QUEUE_NUMBER}")
                .toD("ibmmq:queue:DEV.QUEUE.${exchangeProperty.QUEUE_NUMBER}?exchangePattern=InOnly");

        from("direct:two")
            .filter().simple("${exchangeProperty.QUEUE_NUMBER} > 1")
                // only send to Queue #2 and Queue #3 Explicitly
                .to(ExchangePattern.InOnly, "ibmmq:queue:DEV.QUEUE.2")
                .to(ExchangePattern.InOnly, "ibmmq:queue:DEV.QUEUE.3");

        from("direct:three")
            .filter().simple("${exchangeProperty.SEND_TO_ALL}")
                .to(ExchangePattern.InOnly,"ibmmq:queue:DEV.QUEUE.1")
                .to(ExchangePattern.InOnly,"ibmmq:queue:DEV.QUEUE.2")
                .to(ExchangePattern.InOnly,"ibmmq:queue:DEV.QUEUE.3");

        from("ibmmq:queue:DEV.QUEUE.1")
            .routeId("Route One")
                .log("Nuevo mensaje recibido Q1: \"${body}\"");

        from("ibmmq:queue:DEV.QUEUE.2")
            .routeId("Route Two")
            .process(exchange -> {
                if(Math.random() > 0.5)
                    throw new Exception("Bad Error");
            })
            .process(new ExceptionThrower())
            .log("Nuevo mensaje recibido Q2: \"${body}\"");

        from("ibmmq:queue:DEV.QUEUE.3")
            .routeId("Route Three")
                .log("Nuevo mensaje recibido test test  Q3: \"${body}\"");

        from("direct:validar")
                .log("Validando mensaje...")
                .to("validator:file:src/main/resources/schema.xsd")
                .log("Validado con éxito!!")
                .errorHandler(deadLetterChannel("direct:error"));

        from("direct:transformar")
                .log("Transformando mensaje...")
                .to("xslt:file:src/main/resources/transformation.xsl")
                .log("Transformado con éxito!!");

        from("direct:enviar")
                .log("Enviando mensaje desde Q1 a Q2")
                .to("ibmmq:queue:Q2")
                .log("Mensaje enviado con éxito!!");

        from("direct:error")
                .log("Ocurrió un error validando el mensaje!");

    }
}
