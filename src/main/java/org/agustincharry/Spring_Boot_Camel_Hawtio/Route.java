package org.agustincharry.Spring_Boot_Camel_Hawtio;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Route extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        //from("timer:aa?period=500")
        from("direct:aa")
                .log("Hola Mundo------1");


        from("timer:aa?period=500")
                .log("Hola Mundo-----2");
    }
}
