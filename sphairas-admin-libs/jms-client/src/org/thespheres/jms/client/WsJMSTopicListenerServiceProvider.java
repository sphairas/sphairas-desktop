/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.jms.client;

import java.util.ArrayList;
import java.util.Arrays;
import org.thespheres.betula.services.jms.JMSTopic;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import org.openide.util.Exceptions;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.client.jms.JMSTopicListenerServiceProvider;

/**
 *
 * @author boris.heithecker
 */
public abstract class WsJMSTopicListenerServiceProvider implements JMSTopicListenerServiceProvider {

    protected final Map<String, List<WsJMSTopicListenerService>> services = new HashMap<>();

    protected WsJMSTopicListenerServiceProvider() {
    }

    protected void addService(final String provider, final String addressList, final String certName, final JMSTopic topic) {
//        final String topic = jndiNameToTopicName(jndiName);
        final WsJMSTopicListenerService ns = new WsJMSTopicListenerService(provider, addressList, certName, topic.getJmsResource(), topic.getTopicName(), this);
        ns.initialize();
        services.computeIfAbsent(provider, p -> new ArrayList<>()).add(ns);
    }

    protected void addDefaultServices(final String provider, final String addressList, final String certName) {
        Arrays.stream(JMSTopic.values()).forEach(t -> addService(provider, addressList, certName, t));
    }

    @Override
    public List<JMSTopicListenerService> getListenerServices(final String provider) {
        return services.getOrDefault(provider, Collections.EMPTY_LIST);
    }

//    protected String jndiNameToTopicName(final String jndi) {
//        if (null != jndi) {
//            switch (jndi) {
//                case JMSTopic.DOCUMENTS_TOPIC:
//                    return "documentsTopic";
//                case JMSTopic.TICKETS_TOPIC:
//                    return "ticketsTopic";
//                case JMSTopic.MESSAGES_TOPIC:
//                    return "messagesTopic";
//                default:
//                    return null;
//            }
//        }
//        return null;
//    }
    protected void onInitialisationException(final JMSException ex, final WsJMSTopicListenerService service) {
        Exceptions.printStackTrace(ex);
    }

}
