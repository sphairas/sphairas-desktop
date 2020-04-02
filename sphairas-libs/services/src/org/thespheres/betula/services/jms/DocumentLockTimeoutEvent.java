/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import java.io.Serializable;
import org.thespheres.betula.document.DocumentId;

public class DocumentLockTimeoutEvent extends AbstractJMSEvent<DocumentId> implements Serializable {

    public DocumentLockTimeoutEvent(DocumentId source) {
        super(source);
    }

}
