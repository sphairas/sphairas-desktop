/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.docsrv;

import org.thespheres.betula.admindocsrv.Encode;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admindocsrv.DownloadTargetFolders;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.services.ws.WebServiceProvider;

@Messages({"DownloadDetails.action.name=Detail-Listen für {0} erstellen ({1})",
    "DownloadDetails.action.disabledName.pdf=Detail-Listen erstellen (pdf)",
    //    "DownloadDetails.action.disabledName.xml=Listen erstellen (xml)",
    //    "DownloadDetails.download.allezgn.filename={0} Detail-Listen {1}-{2} ({3,date,dd.MM.yy HH'h'mm}).{4}",
    "DownloadDetails.download.allezgn.filename={0} Detail-Listen {1}-{2}.{4}",
    "DownloadDetails.missingHref.exception=Download Detail-Listen kann nicht ausgeführt werden, weil in der Konfiguration {0} der Schlüssel \"zgnsrvUrl\" fehlt."})
public final class DownloadDetails extends PrimaryUnitDownloadAction {

    @ActionID(
            category = "Betula",
            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadDetails.detailsPdfAction")
    @ActionRegistration(
            displayName = "#DownloadDetails.action.disabledName.pdf",
            lazy = false)
    @ActionReferences({
        //        @ActionReference(path = "Menu/units-administration", position = 1400),
        @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 13000) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
    })
    public static Action detailsPdfAction() {
        return new DownloadDetails(Utilities.actionsGlobalContext(), "application/pdf", "pdf");
    }

//    @ActionID(
//            category = "Betula",
//            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadDetails.xmlAction")
//    @ActionRegistration(
//            displayName = "#DownloadDetails.action.disabledName.xml",
//            lazy = false)
//    @ActionReferences({
//        @ActionReference(path = "Menu/units-administration", position = 1300),
//        @ActionReference(path = "Loaders/application/betula-unit-data/Actions", position = 11000, separatorBefore = 10600) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
//    })
//    public static Action xmlAction() {
//        return new DownloadDetails("text/xml", "xml");
//    }
//    private DownloadDetails(String mime, String extension) {
//        super(mime, extension);
//    }
    private DownloadDetails(Lookup context, String mime, String extension) {
        super(context, mime, extension);
        updateName();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DownloadDetails(actionContext, mime, extension);
    }

    @Override
    public String getName() {
        String name;
        try {
            term = findCommonTerm();
            name = NbBundle.getMessage(DownloadDetails.class, "DownloadDetails.action.name", new Object[]{term.getDisplayName(), extension});
        } catch (IOException ex) {
            setEnabled(false);
            name = getDisabledName();
        }
        return name;
    }

    @Override
    protected String getDisabledName() {
        if (extension == null) {
            return null;
        }
        return NbBundle.getMessage(DownloadDetails.class, "DownloadDetails.action.disabledName." + extension);
    }

    @Override
    protected void downLoad(PrimaryUnitOpenSupport context, Term selectedTerm) throws IOException {
        final String href;
        try {
            href = URLs.reports(context.findBetulaProjectProperties());
        } catch (ConfigurationException cfex) {
            throw new IOException(cfex);
        }
        UnitId unit = context.getUnitId();
        NamingResolver nr = context.findNamingResolver();
        NamingResolver.Result rdn;
        try {
            rdn = nr.resolveDisplayNameResult(context.getUnitId());
        } catch (IllegalAuthorityException ex) {
            throw new IOException(ex);
        }
        rdn.addResolverHint("klasse.ohne.schuljahresangabe");
        String name = rdn.getResolvedName(selectedTerm);
        String jahr = Integer.toString((Integer) selectedTerm.getParameter(NdsTerms.JAHR));
        int hj = (Integer) selectedTerm.getParameter(NdsTerms.HALBJAHR);
        String file = NbBundle.getMessage(DownloadDetails.class, "DownloadDetails.download.allezgn.filename", name.replace("/", "_"), jahr, hj, new Date(), extension);
        String fe = URLEncoder.encode(file, "utf-8");

        String uri = href
                + fe
                + "?unit.id=" + Encode.getUnitIdEncoded(unit)
                + "&unit.authority=" + Encode.getUnitAuthorityEncoded(unit)
                + "&term.authority=" + Encode.getTermAuthorityEncoded(selectedTerm)
                + "&term.id=" + Encode.getTermIdEncoded(selectedTerm)
                + "&document=betula.primaryUnit.details"
                + "&mime=" + mime;
        if (false) {
            uri = uri + "&format.details.lists.preterms.count=" + Integer.toString(2);
        }

//        FileObject files = FileUtil.createFolder(context.getProjectDirectory(), "Dateien");
//        File tmp = new File(FileUtil.toFile(files), file);
        final Path tmp = Files.createTempFile(file, null);
        tmp.toFile().deleteOnExit();

        WebServiceProvider wsp = context.findWebServiceProvider();
        try {
            doDownload(uri, tmp, wsp);
        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        }

        DownloadTargetFolders.getDefault().copy(tmp, file, context.getProjectDirectory().getPath());
        Files.delete(tmp);

//        files.refresh();
//
//        try {
//            DataObject tmpdo = DataObject.find(FileUtil.toFileObject(tmp));
//            OpenSupport support = tmpdo.getLookup().lookup(OpenSupport.class);
//            if (support != null) {
//                support.open();
//            }
//        } catch (DataObjectNotFoundException ex) {
//            throw new IOException(ex);
//        }
    }

}
