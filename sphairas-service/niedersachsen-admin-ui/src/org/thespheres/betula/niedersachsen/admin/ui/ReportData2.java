/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.ValueElement;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.model.ReportDocument;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.document.util.AbstractReportDocument;
import org.thespheres.betula.niedersachsen.ASVAssessmentConvention;
import org.thespheres.betula.niedersachsen.Abschluesse;
import org.thespheres.betula.niedersachsen.Ersatzeintrag;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.RemoteReportsModel2Impl;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.ReportData2Edit;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar.ZeugnisCalendarLookup2;
import org.thespheres.betula.niedersachsen.zeugnis.ZeugnisArt;
import org.thespheres.betula.niedersachsen.xml.NdsZeugnisAngaben;
import org.thespheres.betula.niedersachsen.xml.NdsZeugnisAngaben.FreieBemerkung;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportConstants;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
public abstract class ReportData2 extends AbstractReportDocument implements ReportDocument<Subject> {

    public static final String PROP_ARBEITSVERHALTEN = "arbeitsverhalten";
    public static final String PROP_SOZIALVERHALTEN = "sozialverhalten";
    public static final String PROP_FEHLTAGE = "fehltage";
    public static final String PROP_UNENTSCHULDIGT = "unentschuldigt";
    public static final String PROP_ZEUGNISTYP = "zeugnistyp";
    public static final String PROP_REPORT_NOTES = "report.notes";
    private Integer fehltage;
    private Integer unentschuldigt;
    private Grade arbeitsverhalten;
    private Grade sozialverhalten;
    private final DocumentId document;
    private final TermId term;
    private final List<TextNote> textNotes = new ArrayList<>();
    private final List<ReportNote> reportNotes = new ArrayList<>();
    private boolean updateNotes = true;
    private final Comparator<ReportNote> rncomp = Comparator.comparing(ReportNote::getPosition);
    private Object[] fArgs;
    private Marker zgnType;
    private final RemoteReportsModel2 history;
    private final StudentId student;
    protected final Set<String> propsInvalid = new HashSet<>();
    private long time;
    private final Set<Marker> runtimeMarkerSet = Collections.synchronizedSet(new HashSet<>());
    private NodeDel node;

    protected ReportData2(DocumentId identity, TermId term, StudentId student, RemoteReportsModel2 rrl) {
        super(identity, term, null, null);
        this.history = rrl;
        this.document = identity;
        this.term = term;
        this.student = student;
    }

    @Override
    public String getDisplayLabel() {
        return getDocumentId().getId();
    }

    public void initializeData(final NdsZeugnisAngaben angaben) {
        final ValueElement<Grade> av = angaben.getArbeitsverhalten();
        if (av != null) {
            final Grade avGrade = isActionAnnul(av) ? null : av.getValue();
            synchronized (propsInvalid) {
                propsInvalid.remove(PROP_ARBEITSVERHALTEN);
            }
            if (!Objects.equals(avGrade, arbeitsverhalten)) {
                setArbeitsverhaltenImpl(avGrade);
            }
        }
        final ValueElement<Grade> sv = angaben.getSozialverhalten();
        if (sv != null) {
            final Grade svGrade = isActionAnnul(sv) ? null : sv.getValue();
            synchronized (propsInvalid) {
                propsInvalid.remove(PROP_SOZIALVERHALTEN);
            }
            if (!Objects.equals(svGrade, sozialverhalten)) {
                setSozialverhaltenImpl(svGrade);
            }
        }
        final ValueElement<Integer> ft = angaben.getFehltage();
        if (ft != null) {
            final Integer ftValue = isActionAnnul(ft) ? null : ft.getValue();
            synchronized (propsInvalid) {
                propsInvalid.remove(PROP_FEHLTAGE);
            }
            if (!Objects.equals(ftValue, fehltage)) {
                setFehltageImpl(ftValue);
            }
        }
        final ValueElement<Integer> ue = angaben.getUnentschuldigt();
        if (ue != null) {
            final Integer ueValue = isActionAnnul(ue) ? null : ue.getValue();
            synchronized (propsInvalid) {
                propsInvalid.remove(PROP_UNENTSCHULDIGT);
            }
            if (!Objects.equals(ueValue, unentschuldigt)) {
                setUnentschuldigtImpl(ueValue);
            }
        }
        final ValueElement<Marker>[] m = angaben.getMarkerElements();
        if (m != null) {
            synchronized (reportNotes) {
                Arrays.stream(m).forEach(me -> {
                    final Action ac = me.getAction();
                    final Marker ma = me.getValue();
                    if (ma != null) {
                        if (ac != null && ac.equals(Action.ANNUL)) {
                            markers.remove(ma);
                        } else {
                            markers.add(ma);
                        }
                        updateNotes = true;
                    }
                });
            }
            final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MARKERS, null, markers());
            history.getEventBus().post(evt);
            final PropertyChangeEvent evt2 = new PropertyChangeEvent(this, PROP_REPORT_NOTES, null, reportNotes);
            history.getEventBus().post(evt2);
        }
        final NdsZeugnisAngaben.Text[] t = angaben.getText();
        if (t != null) {
            synchronized (reportNotes) {
                Arrays.stream(t)
                        .forEach(b -> {
                            final Action ac = b.getAction();
                            if (ac != null && ac.equals(Action.ANNUL)) {
                                setTextNote(b.getKey(), null);
                            } else {
                                setTextNote(b.getKey(), b.getValue());
                            }
                        });
            }
            final PropertyChangeEvent evt2 = new PropertyChangeEvent(this, PROP_REPORT_NOTES, null, reportNotes);
            history.getEventBus().post(evt2);
        }
        final FreieBemerkung[] c = angaben.getCustom();
        if (c != null) {
            synchronized (reportNotes) {
                Arrays.stream(c)
                        .forEach(b -> {
                            final Action ac = b.getAction();
                            if (ac != null && ac.equals(Action.ANNUL)) {
                                putFreieBemerkung(b.getPosition(), null);
                            } else {
                                putFreieBemerkung(b.getPosition(), b.getValue());
                            }
                        });
            }
            final PropertyChangeEvent evt2 = new PropertyChangeEvent(this, PROP_REPORT_NOTES, null, reportNotes);
            history.getEventBus().post(evt2);
        }
        //Init FormatArgs, otherwise blocks EDT
        getFormatArgs();
        synchronized (propsInvalid) {
            time = 0l;
        }
    }

    static boolean isActionAnnul(final ValueElement<?> av) {
        return av.getAction() != null && av.getAction().equals(Action.ANNUL);
    }

    @Override
    public synchronized DocumentId getDocumentId() {
        return document;
    }

    public StudentId getStudent() {
        return student;
    }

    public RemoteStudent getRemoteStudent() {
        return history.getRemoteStudents().find(getStudent());
    }

    @Override
    public TermId getTerm() {
        return term;
    }

    public RemoteReportsModel2 getHistory() {
        return history;
    }

    public Integer getFehltage() {
        return fehltage;
    }

    public Integer getUnentschuldigt() {
        return unentschuldigt;
    }

    public Grade getArbeitsverhalten() {
        return arbeitsverhalten;
    }

    public Grade getSozialverhalten() {
        return sozialverhalten;
    }

    public void setArbeitsverhalten(final Grade g) throws IOException {
        if (g != null && !(g.getConvention().equals(ASVAssessmentConvention.AV_NAME) || g.getConvention().equals(Ersatzeintrag.NAME))) {
            throw new IllegalArgumentException();
        }
        final Grade old = getArbeitsverhalten();
        if (Objects.equals(g, old)) {
            return;
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        update.setArbeitsverhalten(new ValueElement<>(g, g == null ? Action.ANNUL : Action.FILE));
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        undo.setArbeitsverhalten(new ValueElement<>(old, old == null ? Action.ANNUL : Action.FILE));
        final ReportData2Edit<Grade> edit = new ReportData2Edit<>(update, g, undo, old, this, this::setArbeitsverhaltenImpl);
        edit.post();
        history.getUndoSupport().postEdit(edit);
        synchronized (propsInvalid) {
            time = System.currentTimeMillis();
            propsInvalid.add(PROP_ARBEITSVERHALTEN);
        }
    }

    public void setSozialverhalten(final Grade g) throws IOException {
        if (g != null && !(g.getConvention().equals(ASVAssessmentConvention.SV_NAME) || g.getConvention().equals(Ersatzeintrag.NAME))) {
            throw new IllegalArgumentException();
        }
        final Grade old = getSozialverhalten();
        if (Objects.equals(g, old)) {
            return;
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        update.setSozialverhalten(new ValueElement<>(g, g == null ? Action.ANNUL : Action.FILE));
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        undo.setSozialverhalten(new ValueElement<>(old, old == null ? Action.ANNUL : Action.FILE));
        final ReportData2Edit<Grade> edit = new ReportData2Edit<>(update, g, undo, old, this, this::setSozialverhaltenImpl);
        edit.post();
        history.getUndoSupport().postEdit(edit);
        synchronized (propsInvalid) {
            time = System.currentTimeMillis();
            propsInvalid.add(PROP_SOZIALVERHALTEN);
        }
    }

    public void setFehltage(final Integer v) throws IOException {
        if (v != null && (v < 0 || v > 365)) {
            throw new IllegalArgumentException();
        }
        final Integer old = getFehltage();
        if (Objects.equals(v, old)) {
            return;
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        update.setFehltage(new ValueElement<>(v, v == null ? Action.ANNUL : Action.FILE));
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        undo.setFehltage(new ValueElement<>(old, old == null ? Action.ANNUL : Action.FILE));
        final ReportData2Edit<Integer> edit = new ReportData2Edit<>(update, v, undo, old, this, this::setFehltageImpl);
        edit.post();
        history.getUndoSupport().postEdit(edit);
        synchronized (propsInvalid) {
            time = System.currentTimeMillis();
            propsInvalid.add(PROP_FEHLTAGE);
        }
    }

    public void setUnentschuldigt(final Integer v) throws IOException {
        if (v != null && (v < 0 || v > 365)) {
            throw new IllegalArgumentException();
        }
        final Integer old = getUnentschuldigt();
        if (Objects.equals(v, old)) {
            return;
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        update.setUnentschuldigt(new ValueElement<>(v, v == null ? Action.ANNUL : Action.FILE));
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        undo.setUnentschuldigt(new ValueElement<>(old, old == null ? Action.ANNUL : Action.FILE));
        final ReportData2Edit<Integer> edit = new ReportData2Edit<>(update, v, undo, old, this, this::setUnentschuldigtImpl);
        edit.post();
        history.getUndoSupport().postEdit(edit);
        synchronized (propsInvalid) {
            time = System.currentTimeMillis();
            propsInvalid.add(PROP_UNENTSCHULDIGT);
        }
    }

    private void setArbeitsverhaltenImpl(Grade grade) {
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_ARBEITSVERHALTEN, arbeitsverhalten, grade);
        arbeitsverhalten = grade;
        history.getEventBus().post(evt);
    }

    private void setSozialverhaltenImpl(Grade grade) {
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_SOZIALVERHALTEN, sozialverhalten, grade);
        sozialverhalten = grade;
        history.getEventBus().post(evt);
    }

    private void setFehltageImpl(Integer value) {
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_FEHLTAGE, fehltage, value);
        fehltage = value;
        history.getEventBus().post(evt);
    }

    private void setUnentschuldigtImpl(Integer value) {
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_UNENTSCHULDIGT, unentschuldigt, value);
        unentschuldigt = value;
        history.getEventBus().post(evt);
    }

    @Messages({"ReportData2.getZeugnisTyp.multipleMarkers=Mehr als ein Zeugnis-Typ gesetzt."})
    public Marker getZeugnisTyp() {
        synchronized (markers) {
            if (zgnType == null) {
                final Marker[] arr;
                arr = markers.stream()
                        .filter(m -> m.getConvention().equals(ZeugnisArt.CONVENTION_NAME) || m.getConvention().equals(Abschluesse.CONVENTION_NAME))
                        .toArray(Marker[]::new);
                if (arr.length == 1) {
                    zgnType = arr[0];
                } else if (arr.length > 1) {
                    zgnType = arr[0];
                    final Exception illex = new IllegalStateException("More than one ZeugnisTyp.");
                    final String msg = NbBundle.getMessage(ReportData2.class, "ReportData2.getZeugnisTyp.multipleMarkers");
                    RemoteReportsModel2Impl.notifyError(illex, msg);
                }
            }
        }
        return zgnType;
    }

    public void setZeugnisType(final Marker m) throws IOException {
        if (m != null && !(m.getConvention().equals(ZeugnisArt.CONVENTION_NAME) || m.getConvention().equals(Abschluesse.CONVENTION_NAME))) {
            throw new IllegalArgumentException();
        }
        final Marker old = getZeugnisTyp();
        if (Objects.equals(old, m)) {
            return;
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        final ValueElement[] ve = new ValueElement[old == null ? 1 : 2];
        ve[0] = new ValueElement<>(m, m == null ? Action.ANNUL : Action.FILE);
        if (old != null) {
            ve[1] = new ValueElement<>(old, Action.ANNUL);
        }
        update.setMarkerElements(ve);
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        final ValueElement[] veundo = new ValueElement[m == null ? 1 : 2];
        veundo[0] = new ValueElement<>(old, old == null ? Action.ANNUL : Action.FILE);
        if (m != null) {
            veundo[1] = new ValueElement<>(m, Action.ANNUL);
        }
        undo.setMarkerElements(veundo);
        final ReportData2Edit<Marker> edit = new ReportData2Edit<>(update, m, undo, old, this, ma -> setZeugnisTypeImpl(ma, old));
        edit.post();
        history.getUndoSupport().postEdit(edit);
        synchronized (propsInvalid) {
            time = System.currentTimeMillis();
            propsInvalid.add(PROP_ZEUGNISTYP);
        }
    }

    private void setZeugnisTypeImpl(final Marker m, final Marker old) {
        synchronized (markers) {
            if (old != null) {
                markers.remove(old);
            }
            if (m != null) {
                markers.add(m);
            }
            zgnType = null;
        }
    }

    public void addMarker(final Marker m) throws IOException {
        if (m == null) {
            throw new IllegalArgumentException();
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        update.setMarkerElements(new ValueElement[]{new ValueElement<>(m, Action.FILE)});
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        undo.setMarkerElements(new ValueElement[]{new ValueElement<>(m, Action.ANNUL)});
        final ReportData2Edit<Marker> edit = new ReportData2Edit<>(update, m, undo, null, this, ma -> setMarkerImpl(m, ma));
        edit.post();
        history.getUndoSupport().postEdit(edit);
    }

    public void removeMarker(final Marker m) throws IOException {
        if (m == null) {
            throw new IllegalArgumentException();
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        update.setMarkerElements(new ValueElement[]{new ValueElement<>(m, Action.ANNUL)});
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        undo.setMarkerElements(new ValueElement[]{new ValueElement<>(m, Action.FILE)});
        final ReportData2Edit<Marker> edit = new ReportData2Edit<>(update, null, undo, m, this, ma -> setMarkerImpl(ma, m));
        edit.post();
        history.getUndoSupport().postEdit(edit);
    }

    private void setMarkerImpl(final Marker m, final Marker set) {
        synchronized (markers) {
            if (set != null) {
                markers.add(set);
            } else {
                markers.remove(m);
            }
        }
    }

    public void putTextNote(final String key, final String text, final String before) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(key)) {
            throw new IllegalArgumentException();
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        update.setText(new NdsZeugnisAngaben.Text[]{new NdsZeugnisAngaben.Text(key, text, text == null ? Action.ANNUL : Action.FILE)});
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        undo.setText(new NdsZeugnisAngaben.Text[]{new NdsZeugnisAngaben.Text(key, before, before == null ? Action.ANNUL : Action.FILE)});
        final ReportData2Edit<String> edit = new ReportData2Edit<>(update, text, undo, null, this, t -> setTextNote(key, t));
        edit.post();
        history.getUndoSupport().postEdit(edit);
    }

    private void setTextNote(final String key, final String text) {
        synchronized (textNotes) {
            if (text != null) {
                final Iterator<TextNote> it = textNotes.iterator();
                while (it.hasNext()) {
                    if (it.next().getKey().equals(key)) {
                        it.remove();
                    }
                }
                final TextNote rn = new TextNote(key, text);
                textNotes.add(rn);
            } else {
                final Iterator<TextNote> it = textNotes.iterator();
                while (it.hasNext()) {
                    if (it.next().getKey().equals(key)) {
                        it.remove();
                    }
                }
            }
        }
    }

    public void addFreieBemerkung(final int position, final String text) throws IOException {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException();
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        update.setCustom(new NdsZeugnisAngaben.FreieBemerkung[]{new NdsZeugnisAngaben.FreieBemerkung(text, position, Action.FILE)});
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        undo.setCustom(new NdsZeugnisAngaben.FreieBemerkung[]{new NdsZeugnisAngaben.FreieBemerkung(null, position, Action.ANNUL)});
        final ReportData2Edit<String> edit = new ReportData2Edit<>(update, text, undo, null, this, t -> putFreieBemerkung(position, t));
        edit.post();
        history.getUndoSupport().postEdit(edit);
    }

    public void removeFreieBemerkung(final int position, final String before) throws IOException {
        if (StringUtils.isBlank(before)) {
            throw new IllegalArgumentException();
        }
        final NdsZeugnisAngaben update = new NdsZeugnisAngaben();
        update.setCustom(new NdsZeugnisAngaben.FreieBemerkung[]{new NdsZeugnisAngaben.FreieBemerkung(null, position, Action.ANNUL)});
        final NdsZeugnisAngaben undo = new NdsZeugnisAngaben();
        undo.setCustom(new NdsZeugnisAngaben.FreieBemerkung[]{new NdsZeugnisAngaben.FreieBemerkung(before, position, Action.FILE)});
        final ReportData2Edit<String> edit = new ReportData2Edit<>(update, null, undo, before, this, t -> putFreieBemerkung(position, t));
        edit.post();
        history.getUndoSupport().postEdit(edit);
    }

    private void putFreieBemerkung(final int position, final String text) {
        synchronized (reportNotes) {
            if (text != null) {
                final Iterator<ReportNote> it = reportNotes.iterator();
                while (it.hasNext()) {
                    if (it.next().getPosition() == position) {
                        it.remove();
                    }
                }
                final ReportNote<String> rn = new ReportNote<>(position, text);
                reportNotes.add(rn);
            } else {
                final Iterator<ReportNote> it = reportNotes.iterator();
                while (it.hasNext()) {
                    if (it.next().getPosition() == position) {
                        it.remove();
                    }
                }
            }
            updateNotes = true;
        }
    }

    @Override
    public Grade select(Subject subject) {
        final RemoteUnitsModel rum;
        final DocumentsModel dm;
        try {
            rum = history.support.getRemoteUnitsModel();
            dm = history.support.findDocumentsModel();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        String suffix = dm.getModelPrimarySuffix();
        for (RemoteTargetAssessmentDocument rtad : rum.getTargets()) {
            if (!rtad.getTargetType().equalsIgnoreCase(suffix)) {
                continue;
            }
            final Subject found = history.support.findSubject(rtad);
            if (Objects.equals(found, subject)) {
                final Grade g = rtad.select(student, term);
                if (g != null) {//If null continue search, eg. if dh1 == null, dh3 != null
                    return g;
                }
            }
        }
        return null;
    }

    @Override
    public Set<Subject> getSubjects() {
        final RemoteUnitsModel rum;
        final DocumentsModel dm;
        try {
            rum = history.support.getRemoteUnitsModel();
            dm = history.support.findDocumentsModel();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        final String suffix = dm.getModelPrimarySuffix();
        return rum.getTargets().stream()
                .filter(rtad -> rtad.getTargetType().equalsIgnoreCase(suffix))
                .filter(rtad -> rtad.select(student, term) != null)
                .map(rtad -> history.support.findSubject(rtad))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<Marker> getRuntimeMarkerSet() {
        return runtimeMarkerSet;
    }

    public void addRuntimeMarker(final Marker m) {
        final boolean c = runtimeMarkerSet.add(m);
        if (c) {
            final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MARKERS, null, markers());
            history.eventBus.post(evt);
        }
    }

    public void removeRuntimeMarker(final Marker m) {
        final boolean c = runtimeMarkerSet.remove(m);
        if (c) {
            final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROP_MARKERS, null, markers());
            history.eventBus.post(evt);
        }
    }

    @Override
    public Marker[] markers() {
        synchronized (markers) {
            return Stream.concat(markers.stream(), runtimeMarkerSet.stream()).toArray(Marker[]::new);
        }
    }

    private Set<String> conventions() {
        final LocalProperties lfp = history.getLocalFileProperties();
        final Set<String> ret = new HashSet<>();
        final String cnv = lfp.getProperty("report.notes.conventions");
        if (cnv != null) {
            Arrays.stream(cnv.split(","))
                    .map(String::trim)
                    .forEach(ret::add);
        }
        final String customcnv = lfp.getProperty("custom.report.notes.conventions");
        if (customcnv != null) {
            Arrays.stream(customcnv.split(","))
                    .map(String::trim)
                    .forEach(ret::add);
        }
        return ret;
    }

    public List<TextNote> getTextNotes() {
        return textNotes;
    }

    public List<ReportNote> getBemerkungen() {
        synchronized (reportNotes) {
            if (updateNotes) {
                final Set<String> cnv = conventions();
                final Iterator<ReportNote> it = reportNotes.iterator();
                while (it.hasNext()) {
                    final Object v;
                    if ((v = it.next().getValue()) instanceof Marker && cnv.contains(((Marker) v).getConvention())) {
                        it.remove();
                    }
                }
                markers.stream()
                        .filter(m -> cnv.contains(m.getConvention()))
                        .map(m -> new ReportNote(history.positionOf(m), m))
                        .forEach(reportNotes::add);
                Collections.sort(reportNotes, rncomp);
                updateNotes = false;
            }
            return reportNotes;
        }
    }

    public Object[] getFormatArgs() {
        if (fArgs == null) {
            final RemoteStudent remoteStudent = getRemoteStudent();
            final Term t;
            String nSJ = "?";
            String nStufe = "?";
            try {
                t = NdsTerms.fromId(this.term);
                int jahr = (int) t.getParameter(NdsTerms.JAHR);
                nSJ = Integer.toString(jahr) + "/" + Integer.toString(++jahr).substring(2);
                NamingResolver.Result r = history.support.findNamingResolverResult();
                r.addResolverHint("naming.only.level");
                nStufe = r.getResolvedName(NdsTerms.getTerm(jahr, 1));
            } catch (IllegalAuthorityException | NumberFormatException | IOException ex) {
            }
            final Long g = "F".equals(remoteStudent.getGender()) ? 1l : 0l;
            final String gen = NdsReportConstants.getGenitiv(remoteStudent.getGivenNames());
            final String possesiv = NdsReportConstants.getPossessivPronomen(remoteStudent.getGender());
            final String possesivGen = NdsReportConstants.getPossessivPronomenGenitiv(remoteStudent.getGender());
            final ZeugnisCalendarLookup2 calendar = history.support.getLookup().lookup(ZeugnisCalendarLookup2.class);
            Date zkDate = null;
            try {
                zkDate = calendar.findZeugnisDate(history.support.getUnitId(), term);
            } catch (IllegalStateException e) {
            }
            if (zkDate == null) {
                Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "No zeugniskonferenz date set for unit {0} in term {1}", new Object[]{history.support.getUnitId(), this.term});
                zkDate = new Date();
            }
            fArgs = new Object[]{remoteStudent.getGivenNames(), gen, possesiv, zkDate, nStufe, nSJ, g, possesivGen};
        }
        return fArgs;
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public Validity getDocumentValidity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SigneeInfo getCreationInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return 79 * hash + Objects.hashCode(this.document);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReportData2 other = (ReportData2) obj;
        return Objects.equals(this.document, other.document);
    }

    public synchronized Node getNodeDelegate() {
        if (node == null) {
            node = new NodeDel();
        }
        return node;
    }

    private class NodeDel extends AbstractNode {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private NodeDel() {
            super(Children.LEAF, Lookups.fixed(ReportData2.this, ReportData2.this.getRemoteStudent(), ReportData2.this.getHistory()));
            setName(getRemoteStudent().getDirectoryName());
            setDisplayName(getRemoteStudent().getDirectoryName());
            setIconBaseWithExtension("org/thespheres/betula/niedersachsen/admin/ui/resources/blue-document-hf-select.png");
        }

        @Override
        public javax.swing.Action[] getActions(boolean context) {
            return Utilities.actionsForPath("Loaders/application/betula-unit-context/Actions").stream()
                    .map(javax.swing.Action.class::cast)
                    .toArray(javax.swing.Action[]::new);
        }

    }

    public static class TextNote {

        protected String key;
        protected String value;

        protected TextNote(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

    }

    public static class ReportNote<V> {

        private final int position;
        private final V value;

        ReportNote(int position, V value) {
            this.position = position;
            this.value = value;
        }

        public int getPosition() {
            return position;
        }

        public V getValue() {
            return value;
        }

    }
}
