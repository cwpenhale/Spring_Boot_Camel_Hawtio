package org.agustincharry.Spring_Boot_Camel_Hawtio;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class Route extends RouteBuilder {

    @Autowired
    CamelContext context;

    @Override
    public void configure() throws Exception {

//        onException(Exception.class)
//            .process(exchange -> {
//                Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
//                exchange.setProperty("EXCHANGE_EXCEPTION_MESSAGE", cause.getMessage());
//            })
//            .to("ibmmq:queue:errorQueue")
//            .log(LoggingLevel.ERROR, "${exchangeProperty.EXCHANGE_EXCEPTION_MESSAGE} was logged");

        /*
        Conor I can send two or three requests and wait for the response of those requests
        to make deliver a fourth response with the results of the initial requests?
        I say this because I see that the flow is very sequential.
         */

        from("file:/Users/cpenhale/file-in")
            .setHeader("startTime").constant(Instant.now().toEpochMilli())
            .split().tokenize("\n")
                .aggregationStrategy(new AggregationStrategy() {
                    @Override
                    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                        if(oldExchange == null){
                            List<Integer> lengths = newExchange.getIn().getBody(List.class);
                            Double average = lengths.stream().mapToInt(i -> i).average().getAsDouble();
                            List<Double> averages = new ArrayList<>();
                            averages.add(average);
                            newExchange.getIn().setBody(averages);
                            return newExchange;
                        }

                        List<Integer> lengths = newExchange.getIn().getBody(List.class);
                        Double average = lengths.stream().mapToInt(i -> i).average().getAsDouble();
                        List<Double> averages = oldExchange.getIn().getBody(List.class);
                        averages.add(average);
                        newExchange.getIn().setBody(averages);
                        return newExchange;
                    }
                })
                .log("Line of Moby Dick: ${exchangeProperty.CamelSplitIndex}")
                .split().tokenize(" ")
                    .aggregationStrategy(new AggregationStrategy() {
                        @Override
                        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                            if(oldExchange == null){
                                List<Integer> lengths = new ArrayList<>();
                                lengths.add(newExchange.getIn().getBody(String.class).length());
                                newExchange.getIn().setBody(lengths);
                                return newExchange;
                            }

                            List<Integer> lengths = oldExchange.getIn().getBody(List.class);
                            lengths.add(newExchange.getIn().getBody(String.class).length());
                            newExchange.getIn().setBody(lengths);
                            return newExchange;
                        }
                    })
                    .log("Word being processed: ${exchangeProperty.CamelSplitIndex}")
                .end()
            .end()
            .process(exchange -> {
                List<Double> averages = exchange.getIn().getBody(List.class);
                Double average = averages.stream().mapToDouble(i -> i).average().getAsDouble();
                exchange.getIn().setBody(average);
            })
            .log("THE AVERAGE WORD LENGTH OF MOBY DICK IS: ${body}")
            .setHeader("endTime").constant(Instant.now().toEpochMilli())
            .process(exchange -> {
                Long startTime = exchange.getIn().getHeader("startTime", Long.class);
                Long endTime = exchange.getIn().getHeader("endTime", Long.class);
                exchange.getIn().setHeader("elapsedTime", endTime - startTime);
            })
            .log("ELAPSED TIME: ${header.elapsedTime}");

        from("jetty:http://0.0.0.0:9080/queue_distributor")
            .convertBodyTo(String.class)
            .unmarshal().json(JsonLibrary.Jackson, RecipientListTest.class)
//            .setProperty("QUEUE_NUMBER").simple("${body.queueNumber}")
//            .setProperty("SEND_TO_ALL").simple("${body.sendToAll}")
            .process(exchange -> {
                RecipientListTest rlt = exchange.getIn().getBody(RecipientListTest.class);
                List<Integer> queuesPrimitive = rlt.getQueues();
                List<String> queues =  new ArrayList<>();
                String format = "direct:queue.%s";
                for(Integer queue : queuesPrimitive){
                    queues.add(String.format(format, queue));
                }
                exchange.getIn().setHeader("RecipientListHeader", queues);
            })
            .log("Headers for RL: ${headers.RecipientListHeader}")
            .recipientList().header("RecipientListHeader")
                .parallelProcessing()
                .streaming()
                .shareUnitOfWork();

        from("direct:switchTest")
            .process(exchange -> {
                String toTest = exchange.getIn().getBody(String.class);
                switch(toTest){
                    case "ONE":
                        exchange.getIn().setHeader("FINAL_HEADER", "ONE");
                        break;
                    case "TWO":
                        exchange.getIn().setHeader("FINAL_HEADER", "TWO");
                        break;
                    default:
                        exchange.getIn().setHeader("FINAL_HEADER", "TWO");
                        break;
                }
            })
        .recipientList().header("FINAL_HEADER");

        from("direct:queue.1")
            .log("My Queue Number is ONE")
            .to("ibmmq:queue:DEV.QUEUE.1?exchangePattern=InOnly");

        from("direct:queue.2")
            // only send to Queue #2 and Queue #3 Explicitly
            .to(ExchangePattern.InOnly, "ibmmq:queue:DEV.QUEUE.2");

        from("direct:queue.3")
            .to(ExchangePattern.InOnly,"ibmmq:queue:DEV.QUEUE.3");

        from("ibmmq:queue:DEV.QUEUE.1")
            .routeId("Route One")
                .log("Nuevo mensaje recibido Q1: \"${body}\"");

        from("ibmmq:queue:DEV.QUEUE.2")
            .routeId("Route Two")
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
