/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import java.io.Serializable;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;

public class AbstractDocumentEvent extends AbstractJMSEvent<DocumentId> implements Serializable {

    public enum DocumentEventType {

        ADD, REMOVE, CHANGE
    }
    private final Signee signee;
    private final DocumentEventType type;

    public AbstractDocumentEvent(DocumentId source, DocumentEventType type, Signee signee, String propagationId) {
        super(source, propagationId);
        this.signee = signee;
        this.type = type;
    }

    public AbstractDocumentEvent(DocumentId source, DocumentEventType type, Signee signee) {
        super(source);
        this.signee = signee;
        this.type = type;
    }

    public DocumentEventType getType() {
        return type;
    }

    public Signee getSignee() {
        return signee;
    }

}
