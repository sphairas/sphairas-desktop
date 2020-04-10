/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.adminconfig.Configuration;
import org.thespheres.betula.adminconfig.Configurations;
import org.thespheres.betula.listprint.Formatter;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportBuilderFactory;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractXml2PdfAction {

    public static final String NDS_REPORT_XSLFO_DUMP_FILE = "nds-report-xslfo-dump-file";
    protected static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static final Map<String, Object> TEMPLATES = new HashMap<>();

    protected AbstractXml2PdfAction() {
    }

    public static synchronized Templates getTemplate(final String provider) throws IOException {
        final Object get = TEMPLATES.computeIfAbsent(provider, p -> {
            final Configurations cfgs = Configurations.find(p);
            if (cfgs != null) {
                final Configuration<NdsReportBuilderFactory> cfg;
                try {
                    cfg = cfgs.readConfiguration("schulvorlage.xml", NdsReportBuilderFactory.class);
                } catch (IOException ex) {
                    return ex.getMessage();
                }
                if (cfg != null) {
                    final NdsReportBuilderFactory nrbf = cfg.get();
                    final String xslFoFile = nrbf.getSchulvorlage().getXslFoFile();
                    if (xslFoFile != null) {
                        final WebProvider wp = WebProvider.find(p, WebProvider.class);
                        final String davBase = URLs.adminResourcesDavBase(LocalProperties.find(p));
                        final URI uri = URI.create(davBase + xslFoFile);
                        try {
                            return HttpUtilities.get(wp, uri, (lm, i) -> read(i), null, false);
                        } catch (IOException ex) {
                            return ex.getMessage();
                        }
                    }
                }
            }
            return "No template available.";
        });
        if (get instanceof String) {
            throw new IOException((String) get);
        }
        return (Templates) get;
    }

    private static Templates read(final InputStream is) throws IOException {
        try {
            return TRANSFORMER_FACTORY.newTemplates(new StreamSource(is));
        } catch (TransformerConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    protected void processOne(final String provider, final FileObject source, final FileObject result, final Formatter f) throws IOException {
        final String dumpFile = NbPreferences.forModule(AbstractXml2PdfAction.class).get(NDS_REPORT_XSLFO_DUMP_FILE, "zeugnis");
        try (final InputStream is = source.getInputStream(); final OutputStream os = result.getOutputStream()) {
            f.transform(new StreamSource(is), getTemplate(provider), os, "application/pdf", dumpFile);
        }
    }

    @Messages({"AbstractXml2PdfAction.error.noProvider.title=Fehler bei der pdf-Erstellung",
        "AbstractXml2PdfAction.error.noProvider.message={0} kann nicht gedruckt werden, weil kein Mandant gesetzt wurde."})
    protected static void notifyNoProvider(final DataObject dob) {
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(RemoteUnitsModel.class, "AbstractXml2PdfAction.error.noProvider.title");
        final String message = NbBundle.getMessage(RemoteUnitsModel.class, "AbstractXml2PdfAction.error.noProvider.message", dob.getName());
        PlatformUtil.getCodeNameBaseLogger(AbstractXml2PdfAction.class).log(LogLevel.INFO_WARNING, message);
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }
}
