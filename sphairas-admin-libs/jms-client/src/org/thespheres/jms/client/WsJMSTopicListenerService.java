/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.jms.client;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;

/**
 *
 * @author boris.heithecker
 */
public class WsJMSTopicListenerService extends JMSTopicListenerService {

    final static InheritableThreadLocal<String> CERT_NAME = new InheritableThreadLocal<>();
    final static InheritableThreadLocal<Boolean> SSL = new InheritableThreadLocal<>();
    private final String topicName;
    private final String provider;
    protected MessageListenerImpl listener;
    private final WsJMSTopicListenerServiceProvider parent;
    private final String addressList;
    private final String certificateName;

    WsJMSTopicListenerService(String provider, String addressList, String certName, String jndi, String topicName, WsJMSTopicListenerServiceProvider parent) {
        super(jndi);
        this.provider = provider;
        this.parent = parent;
        this.topicName = topicName;
        this.addressList = addressList;
        this.certificateName = certName;
    }

    @Override
    public ProviderInfo getInfo() {
        return ProviderRegistry.getDefault().get(provider);
    }

    public String getAddressList() {
        return addressList;
    }

    @Override
    protected void initListener() {//mqws://xxxxxxxx.xxxxxx.xxxxxxx.net: 7681/wsjms
        try {
            CERT_NAME.set(certificateName);
            SSL.set(addressList.startsWith("mqwss"));
            final com.sun.messaging.TopicConnectionFactory qFactory = new com.sun.messaging.TopicConnectionFactory();
            qFactory.setProperty(com.sun.messaging.ConnectionConfiguration.imqAddressList, addressList);
//                        qFactory.setProperty(com.sun.messaging.ConnectionConfiguration.imqDefaultUsername, addressList);
//                                 qFactory.setProperty(com.sun.messaging.ConnectionConfiguration.imqDefaultPassword, addressList);
            qFactory.setProperty(com.sun.messaging.ConnectionConfiguration.imqAddressList, addressList);
            final TopicConnection connection = qFactory.createTopicConnection();
            final TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            final Topic queue = session.createTopic(topicName);
            final MessageConsumer mc = session.createSubscriber(queue);
            final MessageListenerImpl l = new MessageListenerImpl(this);
            mc.setMessageListener(l);
            listener = l;
            initialized = true;
            connection.start();
//            
//            TextMessage tm = session.createTextMessage();
//            tm.setText("Hallo!");
//            session.createPublisher(queue).publish(tm);
        } catch (JMSException ex) {
            listener = null;
            parent.onInitialisationException(ex, this);
        } finally {
            CERT_NAME.set(null);
        }
    }

}
