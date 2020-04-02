/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Document.SigneeInfo;
import org.thespheres.betula.document.Document.Validity;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.ReportDocument;
import org.thespheres.betula.document.model.Subject;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractReportDocument implements ReportDocument<Subject> {

    public static final String PROP_MARKERS = "markers";
    private final DocumentId report;
    protected final Map<Subject, Grade> map;
    private final TermId term;
    protected final Set<Marker> markers = new HashSet<>();
    private final LocalDate reportDate;
    protected final Map<String, Object> properties = new HashMap<>();
    protected final AbstractCreationInfo creationInfo = new AbstractCreationInfo();
    protected final AbstractDocumentValidity validity = new AbstractDocumentValidity();

    protected AbstractReportDocument(DocumentId report, TermId term, Map<Subject, Grade> map, LocalDate reportDate) {
        this.report = report;
        this.term = term;
        this.map = map;
        this.reportDate = reportDate;
    }

    @Override
    public DocumentId getDocumentId() {
        return report;
    }

    @Override
    public Grade select(Subject subject) {
        synchronized (map) {
            return map.get(subject);
        }
    }

    @Override
    public Set<Subject> getSubjects() {
        synchronized (map) {
            return map.keySet().stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public LocalDate getReportDate() {
        return reportDate;
    }

    @Override
    public TermId getTerm() {
        return term;
    }

    @Override
    public Marker[] markers() {
        synchronized (markers) {
            return markers.stream()
                    .toArray(Marker[]::new);
        }
    }

    @Override
    public Map<String, SigneeInfo> getSigneeInfos() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public <C> C getProperty(String name, Class<C> type, C defaultValue) throws IOException {
        Object value;
        synchronized (properties) {
            value = properties.get(name);
        }
        if (value == null) {
            return defaultValue;
        } else if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        } else {
            return null;
        }
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public Validity getDocumentValidity() {
        return validity;
    }

    @Override
    public SigneeInfo getCreationInfo() {
        return creationInfo;
    }

}
