/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.betula.Identity;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Document;
import org.thespheres.betula.document.DocumentEntry;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;

/**
 *
 * @author boris.heithecker@gmx.net
 * @param <I>
 * @param <S>
 */
public abstract class BaseTargetAssessmentEntry<I extends Identity, S extends Identity> extends DocumentEntry<GenericXmlDocument> implements Serializable {

    protected static final long serialVersionUID = 1L;

    protected BaseTargetAssessmentEntry() {
    }

    protected BaseTargetAssessmentEntry(final Action action, final DocumentId id) {
        super(action, id);
    }

    public abstract Set<S> students();

    public Set<I> identities() {
        return getChildren().stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .map(e -> {
                    try {
                        return (I) e.getIdentity();
                    } catch (ClassCastException cce) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public ZonedDateTime getDocumentValidity() {
        final Document.Validity dv = getValue().getDocumentValidity();
        return dv == null ? null : dv.getExpirationDate();
    }

    public void setDocumentValidity(ZonedDateTime deleteDate) {
        getValue().setDocumentValidity(deleteDate);
    }

    public String getTargetType() {
        return getValue().getContentString(TargetAssessment.PROP_TARGETTYPE);
    }

    public void setTargetType(String type) {
        getValue().setContent(TargetAssessment.PROP_TARGETTYPE, type);
    }

    public String getSubjectAlternativeName() {
        return getValue().getContentString(TargetAssessment.PROP_SUBJECT_NAME);
    }

    public void setSubjectAlternativeName(String name) {
        getValue().setContent(TargetAssessment.PROP_SUBJECT_NAME, name);
    }

}
