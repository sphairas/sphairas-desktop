/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.admin.units.AbstractTargetAssessmentDocument.TargetAssessmentDocumentCreationException;
import org.thespheres.betula.reports.model.EditableReportCollection;
import org.thespheres.betula.reports.model.Report;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 */
public class RemoteEditableReportCollection extends EditableReportCollection<RemoteEditableReport> {

    final ArrayList<RemoteEditableReport> reports = new ArrayList<>();
    private final Map<String, ModListener> listeners = new HashMap<>();
    final RequestProcessor RP = new RequestProcessor(RemoteEditableReportCollection.class.getName(), 1);

    protected RemoteEditableReportCollection(StyledDocument document, Lookup context) {
        super(context, document);
    }

    @Override
    public List<RemoteEditableReport> getReports() {
        return Collections.unmodifiableList(reports);
    }

    public RemoteEditableReport addReport(Report r) throws IOException {
        final String provider = getRemoteReportsModel().getDescriptor().getProvider();
        final RemoteEditableReport ret;
        try {
            ret = new RemoteEditableReport(provider, this, r);
        } catch (TargetAssessmentDocumentCreationException ex) {
            throw new IOException(ex);
        }
        reports.add(ret);
        final ModListener ml = listeners.computeIfAbsent(r.getId(), id -> new ModListener());
        ret.addPropertyChangeListener(ml);
        fireReportsChanged(ret, CollectionChangeEvent.Type.ADD);
        return ret;
    }

    public void removeReport(final RemoteEditableReport rem) {
        final String id = rem.getReport().getId();
        if (reports.remove(rem)) {
            final ModListener ml = listeners.remove(id);
            if (ml != null) {
                rem.removePropertyChangeListener(ml);
            }
            Mutex.EVENT.writeAccess(() -> {
                try {
                    rem.removeFromDocument();
                    fireReportsChanged(rem, CollectionChangeEvent.Type.REMOVE);
                } catch (BadLocationException ex) {
                    PlatformUtil.getCodeNameBaseLogger(RemoteEditableReportCollection.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            });
        }
    }

    private void updateRemoteReportsModelModified() {
        final boolean am;
        synchronized (reports) {
            am = reports.stream()
                    .anyMatch(RemoteEditableReport::isModified);
        }
        getRemoteReportsModel().setModified(am);
    }

    public void notifyEditorReady() {
        final ArrayList<RemoteEditableReport> l;
        synchronized (reports) {
            l = new ArrayList(reports);
        }
        RP.post(() -> l.forEach(RemoteEditableReport::notifyEditorReady));
    }

    RemoteReportsModel getRemoteReportsModel() {
        return getContext().lookup(RemoteReportsModel.class);
    }

    void submitCollection() throws IOException {
        for (final RemoteEditableReport rer : reports) {
            if (rer.isModified()) {
                rer.saveReport();
            }
        }
    }

    class ModListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (RemoteEditableReport.PROP_MODIFIED.equals(evt.getPropertyName())) {
                RP.post(RemoteEditableReportCollection.this::updateRemoteReportsModelModified);
            }
        }
    }
}
