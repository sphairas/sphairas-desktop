/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.ReportDocument;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.document.util.MarkerAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
public abstract class Condition {

    @XmlElementWrapper(name = "report-document-distinguishing-markers")
    @XmlElement(name = "marker")
    @XmlJavaTypeAdapter(value = MarkerAdapter.XmlMarkerAdapter.class)
    protected Marker[] reportDistinguishingMarkers;

    public Condition() {
    }

    protected abstract boolean evaluate(final Set<Subject> filtered, ReportDocument report, final PolicyRun props, final Policy policy);

    public Marker[] getReportDistinguishingMarkers() {
        return reportDistinguishingMarkers;
    }

}
