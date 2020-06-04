package com.agustincharry.Spring_Boot_Camel_Hawtio;

import java.io.File;

import org.apache.camel.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
        locations = "org.apache.camel.spring.javaconfig.patterns.FilterTest$ContextConfig",
        loader = JavaConfigContextLoader.class)
public class SpringUnitTest {

    @Autowired
    protected CamelContext camelContext;

    @Produce(uri = "file:/Users/cpenhale/file-in")
    protected ProducerTemplate fileIn;

    @Consume(uri = "file:/Users/cpenhale/file-out")
    protected ProducerTemplate fileOut;

    @Test
    @DirtiesContext
    public void testMocksAreValid() throws Exception {
        fileIn.sendBodyAndHeader("file:/Users/cpenhale/file-in",
                "Did I pass??",
                Exchange.FILE_NAME,
                "test.txt");
        Thread.sleep(2000);
        File target = new File("/Users/cpenhale/file-out/test.txt");
        Assert.assertTrue("File moved by the Camel File Endpoint", target.exists());
        String content = camelContext.getTypeConverter().convertTo(String.class, target);
        Assert.assertEquals ("Did I pass??", content);
    }
}
