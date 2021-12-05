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

@Messages({"DownloadListen.action.name=Listen für {0} erstellen ({1})",
    "DownloadListen.action.disabledName.pdf=Listen erstellen (pdf)",
    "DownloadListen.action.disabledName.csv/zip=Listen erstellen (csv/zip)",
//    "DownloadListen.download.allezgn.filename={0} Zensuren {1}-{2} ({3,date,dd.MM.yy HH'h'mm}).{4}",
    "DownloadListen.download.allezgn.filename={0} Zensuren {1}-{2}.{4}",
    "DownloadListen.missingHref.exception=Download Listen kann nicht ausgeführt werden, weil in der Konfiguration {0} der Schlüssel \"zgnsrvUrl\" fehlt."})
public final class DownloadListen extends PrimaryUnitDownloadAction {

    private String encoding = "utf-8";
    private final String archiveExt;

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadListen.listenPdfAction")
    @ActionRegistration(displayName = "#DownloadListen.action.disabledName.pdf",
            lazy = false)
    @ActionReferences({
        //        @ActionReference(path = "Menu/units-administration", position = 1200),
        @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 12000, separatorBefore = 10000) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
    })
    public static Action listenPdfAction() {
        return new DownloadListen(Utilities.actionsGlobalContext(), "application/pdf", "pdf", null);
    }

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadListen.listenCsvAction")
    @ActionRegistration(displayName = "#DownloadListen.action.disabledName.csv/zip",
            lazy = false)
    @ActionReferences({
        @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 12100, separatorBefore = 10000) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
    })
    public static Action listenCsvAction() {
        return new DownloadListen(Utilities.actionsGlobalContext(), "text/plain", "csv/zip", "zip");
    }

    private DownloadListen(Lookup context, String mime, String extension, String archiveExt) {
        super(context, mime, extension);
        this.archiveExt = archiveExt;
        updateName();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DownloadListen(actionContext, mime, extension, archiveExt);
    }

    @Override
    public String getName() {
        String name;
        try {
            term = findCommonTerm();
            name = NbBundle.getMessage(DownloadListen.class, "DownloadListen.action.name", new Object[]{term.getDisplayName(), extension});
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
        return NbBundle.getMessage(DownloadListen.class, "DownloadListen.action.disabledName." + extension);
    }

    @Override
    protected void downLoad(PrimaryUnitOpenSupport context, Term selectedTerm) throws IOException {
        String href;
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
        final String ext = archiveExt != null ? archiveExt : extension;
        String file = NbBundle.getMessage(DownloadListen.class, "DownloadListen.download.allezgn.filename", name.replace("/", "_"), jahr, hj, new Date(), ext);
        String fe = URLEncoder.encode(file, "utf-8");

        String uri = href
                + fe
                + "?unit.id=" + Encode.getUnitIdEncoded(unit)
                + "&unit.authority=" + Encode.getUnitAuthorityEncoded(unit)
                + "&term.authority=" + Encode.getTermAuthorityEncoded(selectedTerm)
                + "&term.id=" + Encode.getTermIdEncoded(selectedTerm)
                + "&document=betula.primaryUnit.allLists"
                + "&mime=" + mime;

        if (encoding != null) {
            uri += "&encoding=" + encoding;
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
