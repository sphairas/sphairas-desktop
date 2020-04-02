/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;
import org.thespheres.betula.listprint.Formatter;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportBuilderFactory;
import org.thespheres.betula.niedersachsen.zeugnis.ZeugnisData;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractXml2PdfAction {

    public static final String NDS_REPORT_XSLFO_DUMP_FILE = "nds-report-xslfo-dump-file";
    protected static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static Object template;

    protected AbstractXml2PdfAction() {
    }

    public static synchronized Templates getTemplate() throws IOException {
        if (template == null) {
            final InputStream is = ZeugnisData.class.getResourceAsStream(NdsReportBuilderFactory.getXslLocation());
            try {
                template = TRANSFORMER_FACTORY.newTemplates(new StreamSource(is));
            } catch (TransformerConfigurationException ex) {
                template = new IOException(ex);
            }
        }
        if (template instanceof IOException) {
            throw (IOException) template;
        }
        return (Templates) template;
    }

    protected void processOne(final FileObject source, final FileObject result, final Formatter f) throws IOException {
        final String dumpFile = NbPreferences.forModule(AbstractXml2PdfAction.class).get(NDS_REPORT_XSLFO_DUMP_FILE, "zeugnis");
        try (final InputStream is = source.getInputStream(); final OutputStream os = result.getOutputStream()) {
            f.transform(new StreamSource(is), getTemplate(), os, "application/pdf", dumpFile);
        }
    }

}
