package io.quarkus.resteasy.reactive.jaxb.deployment.test.sse;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Message {
    @XmlElement
    public String name;

    public Message(String name) {
        this.name = name;
    }

    // for JAXB
    public Message() {
    }
}
