/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;
import org.openide.util.Lookup;
import org.thespheres.betula.reports.model.EditableReport;
import org.thespheres.betula.reports.model.EditableReportCollection;
import org.thespheres.betula.reports.model.Report;

/**
 *
 * @author boris.heithecker
 * @param <R>
 * @param <C>
 */
public abstract class ReportsSectionsProvider<R extends EditableReport, C extends EditableReportCollection<R>> extends AbstractGuardedSectionsProvider implements GuardedSectionsProvider {

    public static final String NB_ENDOFLINE = "\n";
    protected final GuardedEditorSupport editor;

    protected ReportsSectionsProvider(GuardedEditorSupport editor, boolean useReadersWritersOnSet) {
        super(editor, useReadersWritersOnSet);
        this.editor = editor;
    }

    protected abstract Lookup getContext();

    protected Result readSectionsFromModel(char[] content, Report.ReportCollection coll) throws IOException {
        StringBuilder sb = new StringBuilder();
//        CharBuffer cb = CharBuffer.allocate(content.length);
        List<GuardedSection> sections = new ArrayList<>();
        int pos = 0;
        C ercoll = createEditableReportCollection();
        Report[] arr = coll.getReports().stream()
                .toArray(Report[]::new);
        for (int i = 0; i < arr.length; i++) {
            Report r = arr[i];
            R er = addReport(ercoll, r);
            String msg = er.getMessage();
//            cb.append(msg).append(r.getUserText()).append(NEWLINE);
            String userText = NB_ENDOFLINE + er.getUserText();
            String footer = "\u0020" + (i != arr.length - 1 ? NB_ENDOFLINE : "");
            sb.append(msg).append(userText).append(footer);
            try {
                int headerStart = pos;
                int headerEnd = pos + msg.length();
                int footerStart = headerEnd + userText.length();
                int footerEnd = footerStart + footer.length();
                InteriorSection ss = createInteriorSection(r.getId(), headerStart, headerEnd, footerStart, footerEnd);
                setSection(er, ss);
                sections.add(ss);
                pos = footerEnd;
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        }
        editor.getDocument().putProperty(EditableReportCollection.class.getCanonicalName(), ercoll);
        return new Result(sb.toString().toCharArray(), sections);
    }

    protected abstract C createEditableReportCollection();

    protected abstract R addReport(C ercoll, Report r) throws IOException;

    protected abstract void setSection(R editableReport, InteriorSection section);
}
