package org.agustincharry.Spring_Boot_Camel_Hawtio;

import java.io.Serializable;

public class XmlParseRequest implements Serializable {
    private String xsdUrl;
    private String xmlUrl;

    public XmlParseRequest() {
    }

    public String getXsdUrl() {
        return xsdUrl;
    }

    public void setXsdUrl(String xsdUrl) {
        this.xsdUrl = xsdUrl;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }

    public void setXmlUrl(String xmlUrl) {
        this.xmlUrl = xmlUrl;
    }
}
