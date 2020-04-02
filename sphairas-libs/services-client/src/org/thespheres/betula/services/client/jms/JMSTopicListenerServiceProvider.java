/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.client.jms;

import java.util.List;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public interface JMSTopicListenerServiceProvider {

    public List<JMSTopicListenerService> getListenerServices(String provider);

    public default JMSTopicListenerService getListenerService(final String provider, final String topic) {
        if (null != topic) {
            return getListenerServices(provider).stream()
                    .filter(s -> s.getTopicJNDIName().equals(topic))
                    .collect(CollectionUtil.singleOrNull());
        }
        return null;
    }
}
