/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.Identity;
import org.thespheres.betula.UserId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@XmlRootElement(name = "user-assessment-document", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(name = "userTargetAssessmentEntryType", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserTargetAssessmentEntry<I extends Identity> extends AbstractTargetAssessmentEntry<I, UserId> {

    public UserTargetAssessmentEntry() {
    }

    public UserTargetAssessmentEntry(DocumentId id, Action action, boolean fragment) {
        this(action, id, new GenericXmlDocument(fragment));
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private UserTargetAssessmentEntry(Action action, DocumentId id, GenericXmlDocument doc) {
        super(action, id);
        setValue(doc);
    }

    @Override
    public Set<UserId> students() {
        return getChildren().stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .flatMap(e -> e.getChildren().stream())
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .map(Entry::getIdentity)
                .filter(UserId.class::isInstance)
                .map(UserId.class::cast)
                .collect(Collectors.toSet());
    }

    @Override
    protected void studentChanged(UserId s, Identity id, Grade o, Grade n, Timestamp ts) {
    }

    @Override
    protected void studentRemoved(UserId s, Identity id) {
    }

}
