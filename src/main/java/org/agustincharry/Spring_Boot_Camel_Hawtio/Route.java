package org.agustincharry.Spring_Boot_Camel_Hawtio;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.bouncycastle.util.test.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Route extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("jetty:http://0.0.0.0:9080/queue_distributor")
                .convertBodyTo(String.class)
                .unmarshal().json(JsonLibrary.Jackson, TestMessage.class)
                .setProperty("QUEUE_NUMBER").simple("${body.queueNumber}")
                .setProperty("SEND_TO_ALL").simple("${body.sendToAll}")
                .setBody().simple("${body.message}")
                .choice()
                .when().simple("${exchangeProperty.QUEUE_NUMBER} > 0")
                .log("My Queue Number is #${exchangeProperty.QUEUE_NUMBER}")
                .toD("ibmmq:queue:DEV.QUEUE.${exchangeProperty.QUEUE_NUMBER}?exchangePattern=InOnly")
                .endChoice()
                .end()
                .choice()
                .when().simple("${exchangeProperty.QUEUE_NUMBER} > 1")
                // only send to Queue #2 and Queue #3 Explicitly
                .to(ExchangePattern.InOnly, "ibmmq:queue:DEV.QUEUE.2")
                .to(ExchangePattern.InOnly, "ibmmq:queue:DEV.QUEUE.3")
                .endChoice()
                .end()
                .choice()
                .when().simple("${exchangeProperty.SEND_TO_ALL}")
                .to(ExchangePattern.InOnly,"ibmmq:queue:DEV.QUEUE.1")
                .to(ExchangePattern.InOnly,"ibmmq:queue:DEV.QUEUE.2")
                .to(ExchangePattern.InOnly,"ibmmq:queue:DEV.QUEUE.3")
                .endChoice()
                .end();

        from("ibmmq:queue:DEV.QUEUE.1")
                .log("Nuevo mensaje recibido Q1: \"${body}\"");
        from("ibmmq:queue:DEV.QUEUE.2")
                .log("Nuevo mensaje recibido Q2: \"${body}\"");
        from("ibmmq:queue:DEV.QUEUE.3")
                .log("Nuevo mensaje recibido Q3: \"${body}\"");

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
