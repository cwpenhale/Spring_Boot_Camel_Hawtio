package org.agustincharry.Spring_Boot_Camel_Hawtio;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.apache.camel.Endpoint;
import org.apache.camel.component.jms.JmsComponent;

import javax.jms.JMSException;
import java.util.Map;

public class MQComponent extends JmsComponent {

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {

        MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
        try {
            connectionFactory.setHostName("localhost");
            connectionFactory.setPort(1414);
            connectionFactory.setQueueManager("MQ1");
            connectionFactory.setChannel("DEV.APP.SVRCONN");
            connectionFactory.setTransportType(1);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        super.setConnectionFactory(connectionFactory);
        return super.createEndpoint(uri, remaining, parameters);
    }

}
