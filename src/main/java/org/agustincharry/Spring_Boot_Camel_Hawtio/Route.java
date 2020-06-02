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
        from("log:myLog?level=INFO&amp;showHeaders=true&amp;showProperties=true")
            .log("Log message...");
    }
}
