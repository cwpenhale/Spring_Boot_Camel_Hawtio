package org.agustincharry.Spring_Boot_Camel_Hawtio;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class AggregationBean implements AggregationStrategy {
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if(oldExchange == null)
            return newExchange;

        oldExchange.getIn().getBody(String.class);
        //CONCAT LOGIC
        return newExchange;
    }
}
