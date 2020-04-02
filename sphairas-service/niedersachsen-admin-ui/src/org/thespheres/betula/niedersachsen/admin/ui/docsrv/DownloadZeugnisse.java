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
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.MissingResourceException;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admindocsrv.DownloadTargetFolders;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.dav.URLs;

@Messages({"DownloadZeugnisse.action.name=Zeugnisse für {0} erstellen ({1})",
    "DownloadZeugnisse.action.disabledName.pdf=Zeugnisse erstellen (pdf)",
    "DownloadZeugnisse.action.disabledName.xml=Zeugnisse erstellen (xml)",
    //    "DownloadZeugnisse.download.allezgn.filename={0} Zeugnisse {1}-{2} ({3,date,dd.MM.yy HH'h'mm}).{4}",
    "DownloadZeugnisse.download.allezgn.filename={0} Zeugnisse {1}-{2}.{4}",
    "DownloadZeugnisse.missingHref.exception=Download Zeugnisse kann nicht ausgeführt werden, weil in der Konfiguration {0} der Schlüssel \"zgnsrvUrl\" fehlt."})
public class DownloadZeugnisse extends PrimaryUnitDownloadAction {

    @ActionID(
            category = "Betula",
            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadZeugnisse.pdfAction")
    @ActionRegistration(
            displayName = "#DownloadZeugnisse.action.disabledName.pdf",
            lazy = false)
    @ActionReferences({
        @ActionReference(path = "Menu/units-administration", position = 1000),
        @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 100) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
    })
    public static Action pdfAction() {
        return new DownloadZeugnisse(Utilities.actionsGlobalContext(), "application/pdf", "pdf");
    }

//    @ActionID(
//            category = "Betula",
//            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadZeugnisse.pdfActionBeforeTerm")
//    @ActionRegistration(
//            displayName = "#DownloadZeugnisse.action.disabledName.pdf",
//            lazy = false)
//    @ActionReferences({
//        //        @ActionReference(path = "Menu/units-administration", position = 1000),
//        @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 200) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
//    })
//    public static Action pdfActionBeforeTerm() {
//        return new DownloadZeugnisse(Utilities.actionsGlobalContext(), "application/pdf", "pdf", true);
//    }
    @ActionID(
            category = "Betula",
            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadZeugnisse.xmlAction")
    @ActionRegistration(
            displayName = "#DownloadZeugnisse.action.disabledName.xml",
            lazy = false)
    @ActionReferences({
        //        @ActionReference(path = "Menu/units-administration", position = 1100),
        @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 1100) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
    })
    public static Action xmlAction() {
        return new DownloadZeugnisse(Utilities.actionsGlobalContext(), "text/xml", "xml");
    }

    protected DownloadZeugnisse(Lookup context, String mime, String extension) {
        super(context, mime, extension);
        updateName();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DownloadZeugnisse(actionContext, mime, extension);
    }

//    private Term findBeforeTerm() throws IOException {
//        term = null;
//        for (PrimaryUnitOpenSupport puos : context.lookupAll(PrimaryUnitOpenSupport.class)) {
//            Term t = findBeforeTermFor(puos);
//            if (term == null) {
//                term = t;
//            } else if (!term.equals(t)) {
//                throw new IOException("Unequal terms.");
//            }
//        }
//        if (term == null) {
//            throw new IOException("No term.");
//        }
//        return term;
//    }
    static Term findBeforeTermFor(PrimaryUnitOpenSupport puos) throws IOException {
        final TermSchedule ts = puos.findTermSchedule();
        final TermId tid = ts.getCurrentTerm().getScheduledItemId();
        final int nid = tid.getId() - 1;
        if (nid < 1) {
            throw new IOException("No before term resolvable.");
        }
        final Term t;
        try {
            t = ts.resolve(new TermId(tid.getAuthority(), nid));
        } catch (TermNotFoundException | IllegalAuthorityException ex) {
            throw new IOException(ex);
        }
        return t;
    }

    @Override
    public String getName() {
        String name;
        try {
            term = findCommonTerm();
            name = NbBundle.getMessage(DownloadZeugnisse.class, "DownloadZeugnisse.action.name", new Object[]{term.getDisplayName(), extension});
        } catch (IOException ex) {
            setEnabled(false);
            name = getDisabledName();
        }
        return name;
    }

    @Override
    protected String getDisabledName() throws MissingResourceException {
        if (extension == null) {
            return null;
        }
        return NbBundle.getMessage(DownloadZeugnisse.class, "DownloadZeugnisse.action.disabledName." + extension);
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
        String klasse = rdn.getResolvedName(selectedTerm);
        String sj = Integer.toString((Integer) selectedTerm.getParameter(NdsTerms.JAHR));
        int hj = (Integer) selectedTerm.getParameter(NdsTerms.HALBJAHR);
        String file = getFilename(klasse, sj, hj, extension);
        String fe = URLEncoder.encode(file, "utf-8");

        String uri = href
                + fe
                + "?unit.id=" + Encode.getUnitIdEncoded(unit)
                + "&unit.authority=" + Encode.getUnitAuthorityEncoded(unit)
                + "&term.authority=" + Encode.getTermAuthorityEncoded(selectedTerm)
                + "&term.id=" + Encode.getTermIdEncoded(selectedTerm)
                + "&mime=" + mime;

        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        if (!wd.isNow()) {
            final LocalDate ld = LocalDate.from(wd.getCurrentWorkingDate().toInstant().atZone(ZoneId.systemDefault()));
            uri = uri + "&students.set.date=" + ld.toString();
        }

//        FileObject files = FileUtil.createFolder(context.getProjectDirectory(), "Dateien");
//        File tmp = new File(FileUtil.toFile(files), file);
        final Path tmp = Files.createTempFile(file, null);
        tmp.toFile().deleteOnExit();

        final WebServiceProvider wsp = context.findWebServiceProvider();
        try {
            doDownload(uri, tmp, wsp);
//            URLDisplayer.getDefault().showURLExternal(new URL(uri));
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

    static String getFilename(String klasse, String sj, int hj, String extension) throws MissingResourceException {
        return NbBundle.getMessage(DownloadListen.class, "DownloadZeugnisse.download.allezgn.filename", klasse, sj, hj, new Date(), extension);
    }

    public static String createFilename(String original, String extension) throws MissingResourceException, ParseException {
        final String template = NbBundle.getMessage(DownloadListen.class, "DownloadZeugnisse.download.allezgn.filename");
        final Object[] parsed = new MessageFormat(template).parse(original);
        try {
            String klasse = (String) parsed[0];
            String sj = (String) parsed[1];
            String hjtext = (String) parsed[2];
            int hj = Integer.parseInt(hjtext);
            return getFilename(klasse, sj, hj, extension);
        } catch (IndexOutOfBoundsException | ClassCastException ex) {
            ParseException pex = new ParseException(original, 0);
            throw pex;
        }
    }
}
