/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.beans;

import java.io.Serializable;
import org.thespheres.betula.services.jms.JMSEvent;

/**
 *
 * @author boris.heithecker
 */
public class ChannelEvent implements Serializable, JMSEvent {

    public enum ChannelEventType {

        CREATED, REMOVE
    }
    protected final ChannelEventType type;
    protected final String channel;

    public ChannelEvent(String source, ChannelEventType type) {
        this.channel = source;
        this.type = type;
    }

    public String getChannel() {
        return channel;
    }

    public ChannelEventType getType() {
        return type;
    }

}
