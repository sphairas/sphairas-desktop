/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import java.io.Serializable;
import org.thespheres.betula.Identity;

/**
 *
 * @author boris.heithecker
 * @param <S>
 */
public abstract class AbstractJMSEvent<S extends Identity> implements Serializable, JMSEvent {

    protected final S source;
    protected String topic;
    private final String propagationId;

    protected AbstractJMSEvent(S source) {
        this(source, null);
    }

    protected AbstractJMSEvent(S source, String propagationId) {
        this.source = source;
        this.propagationId = propagationId;
    }

    public S getSource() {
        return source;
    }

    public String getTopic() {
        return topic;
    }

    public String getPropagationId() {
        return propagationId;
    }

}
