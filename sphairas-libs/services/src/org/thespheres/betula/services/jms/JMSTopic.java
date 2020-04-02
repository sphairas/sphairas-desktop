/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

/**
 *
 * @author boris.heithecker
 */
public enum JMSTopic {

    DOCUMENTS_TOPIC("jms/documents-topic", "documentsTopic"),
    TICKETS_TOPIC("jms/tickets-topic", "ticketsTopic"),
    MESSAGES_TOPIC("jms/messages-topic", "messagesTopic"),
    APP_RESOURCES_TOPIC("jms/app-resources-topic", "appResourcesTopic");
    
    private final String jmsResource;
    private final String topicName;

    JMSTopic(final String resource, final String name) {
        this.jmsResource = resource;
        this.topicName = name;
    }

    public String getJmsResource() {
        return jmsResource;
    }

    public String getTopicName() {
        return topicName;
    }

}
