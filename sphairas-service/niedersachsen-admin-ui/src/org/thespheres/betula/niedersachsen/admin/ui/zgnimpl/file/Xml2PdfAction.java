/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
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

@ActionID(category = "Betula",
        id = "org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.file.Xml2PdfAction")
@ActionRegistration(
        displayName = "#Xml2PdfAction.displayName",
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/text/zeugnis-sekundarstufe-niedersachsen+xml/Actions", position = 1225)})
@Messages("Xml2PdfAction.displayName=pdf-Datei erstellen")
public final class Xml2PdfAction extends AbstractXml2PdfAction implements ActionListener {

    private final List<ZgnSekINdsDataObject> context;

    public Xml2PdfAction(List<ZgnSekINdsDataObject> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {

        for (final ZgnSekINdsDataObject report : context) {
            final String provider = report.getLookup().lookup(NdsZeugnisFormular.class).getProvider();
            if (provider == null) {
                notifyNoProvider(report);
                continue;
            }
            final FileObject source = report.getPrimaryFile();
            final Formatter f = Formatter.getDefault(); //getFormatter(p);
            final String name = FileUtil.findFreeFileName(source.getParent(), source.getName(), "pdf") + ".pdf";
            try {
                final FileObject result = FileUtil.createData(source.getParent(), name);
                processOne(provider, source, result, f);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
