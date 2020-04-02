/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.print;

import java.io.IOException;
import java.io.OutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.plutext.jaxb.xslfo.Root;
import org.thespheres.betula.listprint.PDFFactory;
import org.thespheres.betula.listprint.XSLFOException;
import org.thespheres.betula.listprint.builder.RootBuilder;
import org.thespheres.betula.listprint.builder.TableBuilder;
import org.thespheres.betula.termreport.module.TermReportSupport;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
public class PDFPrintProvider implements PDFFactory {

    private final TermReportSupport env;

    public PDFPrintProvider(TermReportSupport env) {
        this.env = env;
    }

    @Override
    public Root createRoot() throws XSLFOException {
        final TermReportTableBuilder tb = new TermReportTableBuilder(env.getModel(), env.getLookup());
        final String displayName = UIUtilities.findDisplayName(env.getDataObject());
        final RootBuilder rb = new RootBuilder(displayName, tb.build());
        final String path = env.getDataObject().getPrimaryFile().getPath();
        tb.addFootnotes(rb);
        TableBuilder.addVersion(rb, path);
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
