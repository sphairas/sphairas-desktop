/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.ws.push;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.atmosphere.wasync.Function;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.ws.push.PushNotificationService;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
class PushNotificationServiceImpl implements PushNotificationService, Function<String> {

    final EventBus bus = new EventBus();
    private final RequestProcessor RP = new RequestProcessor("PushNotificationServiceImpl", 8);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void registerSubscriber(Object subscriber) {
        bus.register(subscriber);
    }

    @Override
    public void unregisterSubscriber(Object subscriber) {
        bus.unregister(subscriber);
    }

    @Override  //Wenns nicht funktiniert --> Atmosphere remove Transport.WebSocket
    public void on(String event) {
        final String message = StringUtils.trimToNull(event);
        if (message != null) {
            try {
                final JsonNode tree = mapper.readTree(message);
                final DocumentId did = JsonUtil.extractDocumentId(tree);
                final Timestamp time = JsonUtil.extractTimestamp(tree);
                final DocumentPushEventImpl push = new DocumentPushEventImpl(did, time);
                RP.post(() -> bus.post(push));
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(PushNotificationServiceImpl.class).log(Level.FINE, ex.getMessage(), ex);
            } catch(Exception other) {
                Exceptions.printStackTrace(other);
            }
        }
    }

}
