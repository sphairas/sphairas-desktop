/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import java.io.Serializable;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 */
public class UnitDocumentEvent extends AbstractDocumentEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    private final UnitId unit;

    public UnitDocumentEvent(DocumentId source, UnitId unit, DocumentEventType type, Signee signee) {
        super(source, type, signee);
        this.unit = unit;
    }

    public UnitId getUnitId() {
        return unit;
    }
}
