/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.thespheres.betula.Identity;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
//Must be final: no entry in Envelope, must must to "entry" element!
@XmlAccessorType(XmlAccessType.FIELD)
public final class ContentValueEntry<I extends Identity> extends Entry<I, Content> {

    public ContentValueEntry() {
        super();
    }

    private ContentValueEntry(I id, Action action, String value) {
        super(action, id, new XmlContentValue(value));
    }

    public static <C extends Identity> ContentValueEntry<C> create(C id, Class<C> type, Action action, String value) {
        if (!(DocumentId.class == type
                || StudentId.class == type
                || UnitId.class == type
                || RecordId.class == type
                || TermId.class == type
                || Signee.class == type)) {
            throw new IllegalArgumentException();
        }
        return new ContentValueEntry(id, action, value);
    }

    public String getStringValue() {
        return super.getValue().getContentString(XmlContentValue.VALUE);
    }

}
