/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.beans;

import org.thespheres.acer.MessageId;
import org.thespheres.betula.services.jms.AbstractJMSEvent;

/**
 *
 * @author boris.heithecker
 */
public class MessageEvent extends AbstractJMSEvent<MessageId> {

    public enum MessageEventType {

        PUBLISH, DELETE
    }
    protected final MessageEventType type;
    protected String channel;//If channel is null, it is not disclosed. User must no even know about the existence of channel they are not concerned with.

    public MessageEvent(MessageId source, MessageEventType type) {
        super(source);
        this.type = type;
    }

    public MessageEventType getType() {
        return type;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
    
}
