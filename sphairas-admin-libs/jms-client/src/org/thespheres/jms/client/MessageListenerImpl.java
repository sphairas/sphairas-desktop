/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.jms.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.jms.JMSEvent;

/**
 *
 * @author boris.heithecker
 */
class MessageListenerImpl implements MessageListener {

    private final JMSTopicListenerService serviceimpl;

    MessageListenerImpl(JMSTopicListenerService serviceimpl) {
        this.serviceimpl = serviceimpl;
    }

    @Override
    public void onMessage(Message msg) {
        JMSEvent evt = null;
        try {
            if (msg.isBodyAssignableTo(JMSEvent.class)) {
                evt = msg.getBody(JMSEvent.class);
            }
        } catch (JMSException ex) {
            Logger.getLogger(MessageListenerImpl.class.getName()).log(Level.WARNING, null, ex);
        }
        if (evt != null) {
            final JMSEvent event = evt;
            serviceimpl.RP.post(() -> serviceimpl.run(event), 0, 3);
        }
    }

}
