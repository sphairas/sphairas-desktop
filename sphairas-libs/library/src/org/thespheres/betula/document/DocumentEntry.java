/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.Identity;

/**
 *
 * @author boris.heithecker
 * @param <D>
 */
@XmlRootElement(name = "betula-document")
@XmlType(name = "documentEntryType")
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentEntry<D extends Document> extends Entry<DocumentId, D> {

    public DocumentEntry() {
    }

    public DocumentEntry(Action action, DocumentId id) {
        super(action, id);
    }

    @Override
    public DocumentId getIdentity() {
        return (DocumentId) super.getIdentity();
    }

    @Override
    public void setIdentity(DocumentId identity) {
        super.setIdentity(identity);
    }

    protected <I extends Identity> Entry<I, ?> findEntry(I identity) {
        return findEntry(identity, this);
    }

    protected <I extends Identity> Entry<I, ?> findEntry(I identity, Template<?> parent) {
        for (Template t : parent.getChildren()) {
            if (t instanceof Entry) {
                Entry entry = (Entry) t;
                if (entry.getIdentity() != null && entry.getIdentity().equals(identity)) {
                    return entry;
                }
            }
        }
        return null;
    }
}
