package org.agustincharry.Spring_Boot_Camel_Hawtio;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RecipientListTest {

    @JsonProperty("Queues")
    private List<Integer> queues;
    @JsonProperty("Message")
    private String message;
    @JsonProperty("SendToAll")
    private Boolean sendToAll;

    public RecipientListTest() {
    }

    public List<Integer> getQueues() {
        return queues;
    }

    public void setQueues(List<Integer> queues) {
        this.queues = queues;
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
