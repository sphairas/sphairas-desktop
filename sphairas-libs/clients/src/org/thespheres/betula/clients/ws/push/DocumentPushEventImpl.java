/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.ws.push;

import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.ws.push.DocumentPushEvent;

/**
 *
 * @author boris.heithecker
 */
class DocumentPushEventImpl implements DocumentPushEvent {

    private final DocumentId document;
    private final Timestamp time;

    DocumentPushEventImpl(DocumentId document, Timestamp time) {
        this.document = document;
        this.time = time;
    }

    @Override
    public DocumentId getEventItem() {
        return document;
    }

    @Override
    public Timestamp getTimestamp() {
        return time;
    }
}
