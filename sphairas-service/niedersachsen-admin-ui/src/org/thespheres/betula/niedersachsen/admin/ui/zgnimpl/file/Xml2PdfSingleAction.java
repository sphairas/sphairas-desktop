/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.MissingResourceException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.listprint.Formatter;
import org.thespheres.betula.niedersachsen.NdsZeugnisFormular;
import org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadZeugnisse;
import static org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file.AbstractXml2PdfAction.notifyNoProvider;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file.Xml2PdfSingleAction")
@ActionRegistration(
        displayName = "#Xml2PdfSingleAction.displayName",
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen-mappe+xml/Actions", position = 1230)})
@Messages("Xml2PdfSingleAction.displayName=pdf-Datei erstellen")
public final class Xml2PdfSingleAction extends AbstractXml2PdfAction implements ActionListener {

    private final List<ZgnSekINdsMappeDataObject> context;

    public Xml2PdfSingleAction(List<ZgnSekINdsMappeDataObject> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {

        for (final ZgnSekINdsMappeDataObject report : context) {
            final String provider = report.getLookup().lookup(NdsZeugnisFormular.ZeugnisMappe.class).getProvider();
            if (provider == null) {
                notifyNoProvider(report);
                continue;
            }
            final FileObject source = report.getPrimaryFile();
            final Path p = Paths.get(System.getProperty("user.home"));//User dialog for resources, or create inline data base64
            final URI base = p.toUri();
            final Formatter f = Formatter.create(base);
            String name;
            try {
                name = DownloadZeugnisse.createFilename(source.getNameExt(), "pdf");
            } catch (MissingResourceException | ParseException ex) {
                name = FileUtil.findFreeFileName(source.getParent(), source.getName(), "pdf");
                name = name + ".pdf";
            }
            try {
                final FileObject result = FileUtil.createData(source.getParent(), name);
                processOne(provider, source, result, f);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
