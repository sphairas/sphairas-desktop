/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.time.ZonedDateTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentEntry;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "betula-unit-document", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(name = "unitEntryType", namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class UnitEntry extends DocumentEntry {

    public static final String PROP_COMMON_UNIT_NAME = "common-unit-name";
    @XmlElement(name = "unit-entry-unit", type = UnitId.class)
    private UnitId unit;

    public UnitEntry() {
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private UnitEntry(Action action, DocumentId id, GenericXmlDocument doc) {
        super(action, id);
        setValue(doc);
    }

    public UnitEntry(final DocumentId id, final UnitId unit, final Action action, final boolean fragment) {
        this(action, id, new GenericXmlDocument(fragment));
        this.unit = unit;
    }

    public UnitId getUnit() {
        return unit;
    }

    @Override
    public GenericXmlDocument getValue() {
        return (GenericXmlDocument) super.getValue();
    }

    public void setDocumentValidity(ZonedDateTime expiry) {
        getValue().setDocumentValidity(expiry);
    }

    public String getPreferredTermSchedule() {
        return getValue().getContentString(TargetAssessment.PROP_PREFERRED_TERMSCHEDULE_PROVIDER);
    }

    public void setPreferredTermSchedule(String url) {
        getValue().setContent(TargetAssessment.PROP_PREFERRED_TERMSCHEDULE_PROVIDER, url);
    }

    public String getCommonUnitName() {
        return getValue().getContentString(PROP_COMMON_UNIT_NAME);
    }

    public void setCommonUnitName(String name) {
        getValue().setContent(PROP_COMMON_UNIT_NAME, name);
    }
}
