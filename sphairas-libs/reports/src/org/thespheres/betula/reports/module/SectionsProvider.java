/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.module;

import org.thespheres.betula.reports.ReportsSectionsProvider;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.thespheres.betula.reports.model.Report;
import org.thespheres.betula.reports.model.Report.ReportCollection;

/**
 *
 * @author boris.heithecker
 */
class SectionsProvider extends ReportsSectionsProvider<DefaultEditableReport, DefaultEditableReportCollection> {

    static JAXBContext jaxb;
    private final Lookup context;

    static {
        try {
            jaxb = JAXBContext.newInstance(Report.class, ReportCollection.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private SectionsProvider(GuardedEditorSupport editor) {
        super(editor, false);
        context = ((DataEditorSupport) editor).getDataObject().getLookup();
    }

    @Override
    public Lookup getContext() {
        return context;
    }

    @Override
    public char[] writeSections(List<GuardedSection> sections, char[] content) {
        assert context != null && editor.getDocument() != null;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Result readSections(char[] content) {
        assert context != null && editor.getDocument() != null;
        CharArrayReader car = new CharArrayReader(content);
        ReportCollection coll;
        try {
            coll = (ReportCollection) jaxb.createUnmarshaller().unmarshal(car);
        } catch (JAXBException ex) {
            Logger.getLogger(SectionsProvider.class.getCanonicalName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            return new Result(content, Collections.EMPTY_LIST);
        }
        try {
            return readSectionsFromModel(content, coll);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected DefaultEditableReportCollection createEditableReportCollection() {
        return new DefaultEditableReportCollection(editor.getDocument(), getContext());
    }

    @Override
    protected DefaultEditableReport addReport(DefaultEditableReportCollection collection, Report r) {
        return collection.addReport(r);
    }

    @Override
    protected void setSection(DefaultEditableReport editableReport, InteriorSection section) {
        editableReport.setSection(section);
    }

    @MimeRegistration(mimeType = "text/betula-reports+xml", service = GuardedSectionsFactory.class)
    public static class Factory extends GuardedSectionsFactory {

        @Override
        public GuardedSectionsProvider create(GuardedEditorSupport editor) {
            return new SectionsProvider(editor);
        }
    }

}
