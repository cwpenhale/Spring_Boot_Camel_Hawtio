package org.agustincharry.Spring_Boot_Camel_Hawtio;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestMessage {

    @JsonProperty("QueueNumber")
    private Integer queueNumber;
    @JsonProperty("Message")
    private String message;
    @JsonProperty("SendToAll")
    private Boolean sendToAll;

    public TestMessage() {
    }

    public Integer getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(Integer queueNumber) {
        this.queueNumber = queueNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSendToAll() {
        return sendToAll;
    }

    public void setSendToAll(Boolean sendToAll) {
        this.sendToAll = sendToAll;
    }
}
