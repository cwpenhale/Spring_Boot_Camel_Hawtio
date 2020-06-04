package org.agustincharry.Spring_Boot_Camel_Hawtio;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ExceptionThrower implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        if(Math.random() > 0.5)
            throw new Exception("Bad Error");
    }
}
