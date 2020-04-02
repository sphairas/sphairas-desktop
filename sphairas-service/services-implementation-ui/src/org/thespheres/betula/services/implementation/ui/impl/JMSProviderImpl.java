/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.client.jms.JMSTopicListenerService;
import org.thespheres.betula.services.client.jms.JMSTopicListenerServiceProvider;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = JMSTopicListenerServiceProvider.class, position = 2000)
public class JMSProviderImpl implements JMSTopicListenerServiceProvider {

    @Override
    public List<JMSTopicListenerService> getListenerServices(String provider) {
        return Optional.ofNullable(SyncedProviderInstance.getInstances().get(provider))
                .map(p -> p.getJMSListenerServices())
                .orElse(Collections.EMPTY_LIST);
    }

}
