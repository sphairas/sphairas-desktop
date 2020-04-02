/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.StringJoiner;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.Root;
import org.plutext.jaxb.xslfo.TextAlignType;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.journal.model.EditableJournal;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.journal.table.JournalTableSupport;
import org.thespheres.betula.listprint.PDFFactory;
import org.thespheres.betula.listprint.XSLFOException;
import org.thespheres.betula.listprint.builder.RootBuilder;
import org.thespheres.betula.listprint.builder.TableBuilder;
import org.thespheres.betula.listprint.builder.Util;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
public class PDFPrintProvider implements PDFFactory {

    private final JournalTableSupport env;

    public PDFPrintProvider(JournalTableSupport env) {
        this.env = env;
    }

    @Override
    public Root createRoot() throws XSLFOException {
        TableBuilder tb = new JournalTableBuilder(env.getModel());
        EntriesTableItem eti = new EntriesTableItem(env.getLookup().lookup(EditableJournal.class), env.getLookup().lookup(JournalEditor.class));
        TableBuilder tb2 = new EntriesTableBuilder(eti);
        String displayName = UIUtilities.findDisplayName(env.getDataObject());
        RootBuilder rb = new JournalRootBuilder(displayName, tb.build());
        rb.addFlow(tb2.build());
        AssessmentConvention ac = GradeFactory.findConvention("mitarbeit2");
        StringJoiner sj = new StringJoiner(", ", "Legende: ", "");
        Arrays.stream(ac.getAllGrades())
                .map(g -> g.getShortLabel() + ": " + g.getLongLabel())
                .forEach(sj::add);
        Block b = Util.createBlock("2mm", "2mm", "0.0cm", "7pt", "#000000", TextAlignType.START);
        b.getContent().add(sj.toString());
        rb.addFlow(b);
        return rb.build();
    }

    @Override
    public OutputStream getOutputStream(String mimeType) throws IOException {
        final FileObject pf = env.getDataObject().getPrimaryFile();
        FileObject folder = pf.getParent();
        String n = FileUtil.findFreeFileName(folder, pf.getName(), "pdf");
        return folder.createData(n, "pdf").getOutputStream();
    }

}
