/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.docsrv;

import org.thespheres.betula.admindocsrv.Archives;
import java.awt.Cursor;
import org.thespheres.betula.admindocsrv.Encode;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.stream.Collectors;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admindocsrv.DownloadTargetFolders;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.util.CollectionUtil;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadArchive")
@ActionRegistration(lazy = false,
        displayName = "#DownloadArchive.action.name")
@ActionReferences({
    @ActionReference(path = "Menu/units-administration", position = 49000),
    @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 49000) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
})
@Messages({"DownloadArchive.action.name=Archiv für {0} erstellen",
    "DownloadArchive.action.disabledName.zip=Archiv erstellen",
    "DownloadArchive.download.filename={0} {1} {2}-{3}.{4}",
    "DownloadArchive.download.filename.default={0} {1}-{2}.{3}"})
public class DownloadArchive extends PrimaryUnitDownloadAction {

    public DownloadArchive() {
        super(Utilities.actionsGlobalContext(), "application/zip", "zip");
        updateName();
    }

    protected DownloadArchive(Lookup context, String mime, String extension) {
        super(context, mime, extension);
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DownloadArchive(actionContext, mime, extension);
    }

    @Override
    void actionPerformed(final List<PrimaryUnitOpenSupport> list, final Term selectedTerm) {
        final TopComponent ac = TopComponent.getRegistry().getActivated();
        final Cursor before = ac.getCursor();
        ac.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        RP.post(() -> {
            try {
                downLoad(list, selectedTerm);
            } catch (IOException ex) {
                PrimaryUnitDownloadAction.notifyError(ex);
            }
        }).addTaskListener(t -> ac.setCursor(before));
    }

    @Override
    public String getName() {
        String name;
        try {
            term = findCommonTerm();
            name = NbBundle.getMessage(DownloadArchive.class, "DownloadArchive.action.name", term.getDisplayName());
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
        return NbBundle.getMessage(DownloadArchive.class, "DownloadArchive.action.disabledName." + extension);
    }

    @Override
    protected void downLoad(PrimaryUnitOpenSupport context, Term selectedTerm) throws IOException {
        throw new UnsupportedOperationException("Must not be called.");
    }

    protected void downLoad(final List<PrimaryUnitOpenSupport> context, final Term selectedTerm) throws IOException {

        final List<Archives.ArchiveFile> items = new ArrayList<>();
        for (final PrimaryUnitOpenSupport puos : context) {
            final Archives.ArchiveFile pdf = oneFile("application/pdf", "pdf", selectedTerm, "reports", puos);
            items.add(pdf);
            final Archives.ArchiveFile xml = oneFile("text/xml", "xml", selectedTerm, "reports", puos);
            items.add(xml);
            final Archives.ArchiveFile list = oneFile("application/pdf", "pdf", selectedTerm, "lists", puos);
            items.add(list);
        }

        //zip to new tmp
        final Path tmp = Files.createTempFile("archive", null);
        tmp.toFile().deleteOnExit();
        final Archives ar = new Archives();
        ar.createArchive(tmp, items);
        //copy
        final String uniquePrj = context.stream()
                .map(PrimaryUnitOpenSupport::getProjectDirectory)
                .map(FileObject::getPath)
                .distinct()
                .collect(CollectionUtil.singleOrNull());
        final String puosNames = context.stream()
                .map(puos -> puos.getNodeDelegate().getDisplayName())
                .sorted(Collator.getInstance(Locale.getDefault()))
                .collect(Collectors.joining(" "));
        if (puosNames.length() > 50) {

        }
        final String sj = Integer.toString((Integer) selectedTerm.getParameter(NdsTerms.JAHR));
        final int hj = (Integer) selectedTerm.getParameter(NdsTerms.HALBJAHR);
        final String name = getFilename(puosNames, sj, hj, null, ar.getArchiveFileExtension());
        DownloadTargetFolders.getDefault().copy(tmp, name, uniquePrj);
        //delete all tmp
        Files.delete(tmp);
        for (final Archives.ArchiveFile i : items) {
            Files.delete(i.getTmp());
        }
    }


    Archives.ArchiveFile oneFile(final String mimeType, final String ext, final Term selectedTerm, final String type, final PrimaryUnitOpenSupport puos) throws IOException {
        final String href;
        try {
            href = URLs.reports(puos.findBetulaProjectProperties());
        } catch (ConfigurationException cfex) {
            throw new IOException(cfex);
        }
        final UnitId unit = puos.getUnitId();
        final NamingResolver nr = puos.findNamingResolver();
        final NamingResolver.Result rdn;
        try {
            rdn = nr.resolveDisplayNameResult(puos.getUnitId());
        } catch (IllegalAuthorityException ex) {
            throw new IOException(ex);
        }
        rdn.addResolverHint("klasse.ohne.schuljahresangabe");
        final String klasse = rdn.getResolvedName(selectedTerm);
        final String sj = Integer.toString((Integer) selectedTerm.getParameter(NdsTerms.JAHR));
        final int hj = (Integer) selectedTerm.getParameter(NdsTerms.HALBJAHR);
        final String name = getFilename(klasse, sj, hj, type, ext);

        String uri = href
                + "?unit.id=" + Encode.getUnitIdEncoded(unit)
                + "&unit.authority=" + Encode.getUnitAuthorityEncoded(unit)
                + "&term.authority=" + Encode.getTermAuthorityEncoded(selectedTerm)
                + "&term.id=" + Encode.getTermIdEncoded(selectedTerm)
                + "&mime=" + mimeType;
        if ("lists".equals(type)) {
            uri += "&document=betula.primaryUnit.allLists";
        }
//        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
//        if (!wd.isNow()) {
//            final LocalDate ld = LocalDate.from(wd.getCurrentWorkingLocalDate());
//            uri = uri + "&students.set.date=" + ld.toString();
//        }
        final Path tmp = Files.createTempFile(name, null);
        final File tmpFile = tmp.toFile();
        tmpFile.deleteOnExit();
        WebServiceProvider wsp = puos.findWebServiceProvider();
        try {
            doDownload(uri, tmp, wsp);
            return new Archives.ArchiveFile(name, tmp);
        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        }
    }

    static String getFilename(final String klasse, final String sj, final int hj, final String type, final String extension) throws MissingResourceException {
        if (type != null) {
            final String docName = NbBundle.getMessage(DownloadArchive.class, "download.docName." + type);
            return NbBundle.getMessage(DownloadArchive.class, "DownloadArchive.download.filename", klasse, docName, sj, hj, extension);
        }
        return NbBundle.getMessage(DownloadArchive.class, "DownloadArchive.download.filename.default", klasse, sj, hj, extension);
    }

}
