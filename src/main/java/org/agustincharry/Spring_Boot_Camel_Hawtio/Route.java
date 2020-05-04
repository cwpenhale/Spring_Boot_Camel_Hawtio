package org.agustincharry.Spring_Boot_Camel_Hawtio;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Route extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:aa")
                .log("Hola Mundo------------------>>1")
                .to("direct:bb");

        from("direct:bb")
                .log("Hola Mundo------------------>>2");
    }
}
