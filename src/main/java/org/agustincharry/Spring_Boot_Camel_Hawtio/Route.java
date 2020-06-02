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

    /**
    * Populate a <setHeader> and <setProperty> command, and then output the resulting message to a log file by ending the route with:
    *     log:myLog?level=INFO&amp;showHeaders=true&amp;showProperties=true
    * Watch the Log4j log file and note that the headers and properties correspond to the values you’ve set
    */

    @Override
    public void configure() throws Exception {
        from("timer://theTimer?fixedRate=true&period=1000")
            // Your Properties and Headers here
            .log("log:myLog?level=INFO&amp;showHeaders=true&amp;showProperties=true");
    }

}
