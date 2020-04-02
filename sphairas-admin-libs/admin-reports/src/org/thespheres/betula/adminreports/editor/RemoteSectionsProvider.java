/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.editor;

import org.thespheres.betula.adminreports.impl.RemoteEditableReportCollection;
import org.thespheres.betula.adminreports.impl.RemoteEditableReport;
import org.thespheres.betula.adminreports.impl.RemoteReportsModel;
import org.thespheres.betula.adminreports.impl.RemoteReportsSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.thespheres.betula.reports.ReportsSectionsProvider;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.util.Lookup;
import org.thespheres.betula.reports.model.Report;
import org.thespheres.betula.reports.model.Report.ReportCollection;

/**
 *
 * @author boris.heithecker
 */
public class RemoteSectionsProvider extends ReportsSectionsProvider<RemoteEditableReport, RemoteEditableReportCollection> implements GuardedSectionsProvider {

    private final Lookup context;

    private RemoteSectionsProvider(GuardedEditorSupport editor) {
        super(editor, false);
        context = ((RemoteReportsSupport) editor).getRemoteReportsModel().getLookup();
    }

    @Override
    public Lookup getContext() {
        return context;
    }

    @Override
    public char[] writeSections(List<GuardedSection> sections, char[] content) {
//        assert context != null && editor.getDocument() != null;
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Result readSections(char[] content) {
        assert context != null && editor.getDocument() != null;
        final ReportCollection coll = context.lookup(RemoteReportsModel.class).getReportsCollection();
        if (coll == null) {
            return new Result(content, Collections.EMPTY_LIST);
        }
        try {
            return readSectionsFromModel(content, coll);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected RemoteEditableReportCollection createEditableReportCollection() {
        final RemoteEditableReportCollection ret = getContext().lookup(RemoteReportsModel.class).createEditableReportCollection(editor.getDocument());
        final RemoteReportsSupport rrs = (RemoteReportsSupport) editor;
        class Listener implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(Observable.PROP_OPENED_PANES)) {
                    rrs.removePropertyChangeListener(this);
                    ret.notifyEditorReady();
                }
            }

        }
//        org.netbeans.api.editor.settings.FontColorSettings fcs = MimeLookup.getLookup(RemoteReportsDescriptorFileDataObject.FILE_MIME).lookup(FontColorSettings.class); // RemoteReportsSupport.EDITOR_MIME;
//        org.netbeans.api.editor.settings.AttributeSet tokenFontColors = fcs.getFontColors(FontColorNames.GUARDED_COLORING);
////        editor.getDocument().
//        Object property = editor.getDocument().getProperty(FontColorNames.GUARDED_COLORING);

        rrs.addPropertyChangeListener(new Listener());
        return ret;
    }

    @Override
    protected RemoteEditableReport addReport(RemoteEditableReportCollection collection, Report r) throws IOException {
        return collection.addReport(r);
    }

    @Override
    protected void setSection(RemoteEditableReport editableReport, InteriorSection section) {
        editableReport.setSection(section);
    }

    @MimeRegistration(mimeType = "text/betula-remote-reports", service = GuardedSectionsFactory.class)
    public static class Factory extends GuardedSectionsFactory {

        @Override
        public GuardedSectionsProvider create(GuardedEditorSupport editor) {
            return new RemoteSectionsProvider(editor);
        }
    }

}
