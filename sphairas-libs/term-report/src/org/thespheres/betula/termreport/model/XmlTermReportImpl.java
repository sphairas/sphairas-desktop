/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import org.thespheres.betula.termreport.XmlAssessmentProviderDataProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openide.loaders.DataObject;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.termreport.XmlAssessmentProviderData;
import org.thespheres.betula.termreport.xml.XmlTermReport;
import org.thespheres.betula.termreport.xml.XmlNote;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
public class XmlTermReportImpl extends TermReport {

    private final DataObject environment;
    final ProviderChildren children = new ProviderChildren();
    private final Map<StudentId, List<Note>> notes;
    private final XmlTermReport xmlReport;

    public XmlTermReportImpl(XmlTermReport xmlReport, DataObject data) {
        this.xmlReport = xmlReport;
        this.environment = data;
        Arrays.stream(xmlReport.getReferences())
                .sorted()
                .map(pd -> pd.createAssessmentProvider(environment.getLookup()))
                .forEach(this::addAssessmentProvider);
        notes = Arrays.stream(xmlReport.getNotes())
                .filter(XmlNote.class::isInstance)
                .map(XmlNote.class::cast)
                .collect(Collectors.groupingBy(XmlNote::getStudent,
                        Collectors.mapping(Note.class::cast, Collectors.toList())));
        children.update();
    }

    public XmlTermReport getXmlTermReport() {
        final XmlAssessmentProviderData[] data = assessments.stream()
                .map(XmlAssessmentProviderDataProvider.class::cast)
                .map(XmlAssessmentProviderDataProvider::getXmlAssessmentProviderData)
                .toArray(XmlAssessmentProviderData[]::new);
        xmlReport.setReferences(data);
        XmlNote[] xmlNotes = notes.entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                .map(n -> new XmlNote(e.getKey(), n.getText(), n.getTimestamp())))
                .toArray(XmlNote[]::new);
        xmlReport.setNotes(xmlNotes);
        return xmlReport;
    }

    DataObject getEnvironment() {
        return environment;
    }

    @Override
    protected Node createNodeDelegate() {
        return new XmlTermReportImplNode(this);
    }

    @Override
    public Map<StudentId, List<Note>> getNotes() {
        return notes;
    }

    @Override
    public boolean addNote(StudentId s, String text) {
        final String v = StringUtils.trimToNull(text);
        boolean ret = false;
        if (v != null) {
            boolean contained = notes.getOrDefault(s, (List<Note>) Collections.EMPTY_LIST)
                    .stream()
                    .anyMatch(n -> n.getText().equals(v));
            if (!contained) {
                final XmlNote n = new XmlNote(null, v, Timestamp.now());
                ret = notes.computeIfAbsent(s, st -> new ArrayList<>())
                        .add(n);
                fireNoteAdded(n);
            }
        }
        return ret;
    }

    @Override
    public void removeNotes(StudentId il) {
        final List<Note> rm = getNotes().remove(il);
        if (rm != null) {
            rm.forEach(this::fireNoteRemoved);
        }
    }

    public void addAssessmentProvider(AssessmentProvider p) throws IllegalArgumentException {
        final String proposed = p.getId();
        synchronized (assessments) {
            boolean exists = assessments.stream()
                    .map(AssessmentProvider::getId)
                    .anyMatch(proposed::equals);
            if (exists) {
                throw new IllegalArgumentException(p.getId() + " exists.");
            }
            assessments.add(p);
        }
        fireAssessmentAdded(p);
    }

    public void removeAssessmentProvider(AssessmentProvider p) throws IllegalArgumentException {
        final String id = p.getId();
        synchronized (assessments) {
            boolean exists = assessments.stream()
                    .map(AssessmentProvider::getId)
                    .anyMatch(id::equals);
            if (!exists) {
                throw new IllegalArgumentException(p.getId() + " exists.");
            }
            assessments.remove(p);
        }
        fireAssessmentRemoved(p);
    }

    @Override
    protected void fireAssessmentAdded(AssessmentProvider p) {
        Mutex.EVENT.writeAccess(children::update);
        super.fireAssessmentAdded(p);
    }

    @Override
    protected void fireAssessmentRemoved(AssessmentProvider p) {
        Mutex.EVENT.writeAccess(children::update);
        super.fireAssessmentRemoved(p);
    }

    final class ProviderChildren extends Index.KeysChildren<AssessmentProvider> {

        private ProviderChildren() {
            super(assessments);
        }

        @Override
        protected Node[] createNodes(AssessmentProvider key) {
            return new Node[]{key.getNodeDelegate()};
        }

        @Override
        protected void reorder(int[] perm) {
//            AssessmentProvider[] old = assessments.stream().toArray(AssessmentProvider[]::new);
            super.reorder(perm); //To change body of generated methods, choose Tools | Templates.
//            AssessmentProvider[] reordered = assessments.stream().toArray(AssessmentProvider[]::new);
            final CollectionChangeEvent pce = new CollectionChangeEvent(XmlTermReportImpl.this, TermReport.PROP_ASSESSMENTS, null, CollectionChangeEvent.Type.REORDER);
            eventBus.post(pce);
        }
    }
}
