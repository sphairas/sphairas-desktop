/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.docsrv;

import java.awt.Cursor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.admindocsrv.AbstractDownloadAction;
import org.thespheres.betula.admindocsrv.DownloadTargetFolders;
import org.thespheres.betula.admindocsrv.Encode;
import org.thespheres.betula.niedersachsen.NdsTerms;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.services.ui.ConfigurationException;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages({"DownloadStudentZeugnis.action.name=Zeugnis für {0} erstellen ({1})",
    "DownloadStudentZeugnis.action.disabledName=Zeugnis erstellen",
    "DownloadStudentZeugnis.filename={0} Zeugnis {1}-{2} ({3,date,dd.MM.yy HH'h'mm}).{4}"})
public class DownloadStudentZeugnis extends AbstractDownloadAction<RemoteStudent> {

    protected final RequestProcessor RP = new RequestProcessor(DownloadStudentZeugnis.class);
    private Optional<? extends RemoteUnitsModel> unitsModel;
    private final boolean useBeforeTerm;

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadStudentZeugnis.pdfAction")
    @ActionRegistration(
            displayName = "#DownloadStudentZeugnis.action.disabledName",
            lazy = false)
    @ActionReferences({
        @ActionReference(path = "Loaders/application/betula-remote-students/Actions", position = 500)
//        @ActionReference(path = "Menu/units-administration", position = 1100),
//        @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 1100, separatorBefore = 1000) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
    })
    public static Action pdfAction() {
        return new DownloadStudentZeugnis(false, "application/pdf", "pdf");
    }

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadStudentZeugnis.xmlAction")
    @ActionRegistration(
            displayName = "#DownloadStudentZeugnis.action.disabledName",
            lazy = false)
    @ActionReferences({
        @ActionReference(path = "Loaders/application/betula-remote-students/Actions", position = 510)
//        @ActionReference(path = "Menu/units-administration", position = 1100),
//        @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 1100, separatorBefore = 1000) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
    })
    public static Action xmlAction() {
        return new DownloadStudentZeugnis(false, "text/xml", "xml");
    }

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.niedersachsen.admin.ui.docsrv.DownloadStudentZeugnis.pdfActionBeforeTerm")
    @ActionRegistration(
            displayName = "#DownloadStudentZeugnis.action.disabledName",
            lazy = false)
    @ActionReferences({
        @ActionReference(path = "Loaders/application/betula-remote-students/Actions", position = 550)
//        @ActionReference(path = "Menu/units-administration", position = 1100),
//        @ActionReference(path = "Loaders/application/betula-unit-data/ZeugnisSubActions", position = 1100, separatorBefore = 1000) //    @ActionReference(path = "Editors/application/xml-dtd/Popup", position = 4000)      
    })
    public static Action pdfActionBeforeTerm() {
        return new DownloadStudentZeugnis(true, "application/pdf", "pdf");
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    private DownloadStudentZeugnis(final boolean useBeforeTerm, final String mime, final String ext) {
        super(mime, ext);
        putValue(Action.NAME, NbBundle.getMessage(DownloadStudentZeugnis.class, "DownloadStudentZeugnis.action.disabledName"));
        setEnabled(false);
        this.useBeforeTerm = useBeforeTerm;
        onContextChange(null);
        updateName();
    }

    private DownloadStudentZeugnis(Lookup context, String mime, String extension, boolean useBeforeTerm) {
        super(context, RemoteStudent.class, mime, extension, true, false);
        this.useBeforeTerm = useBeforeTerm;
        updateEnabled();
        updateName();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DownloadStudentZeugnis(actionContext, mime, extension, useBeforeTerm);
    }

    @Override
    protected void onContextChange(List<RemoteStudent> all) {
        unitsModel = Utilities.actionsGlobalContext().lookupAll(RemoteUnitsModel.class).stream()
                .collect(CollectionUtil.singleton());
        if (!unitsModel.isPresent()) {
            setEnabled(false);
        }
    }

    @Override
    public String getName() {
        String name;
        try {
            if (this.useBeforeTerm) {
                term = findBeforeTerm();
            } else {
                term = findCommonTerm();
            }
            name = NbBundle.getMessage(DownloadStudentZeugnis.class, "DownloadStudentZeugnis.action.name", term.getDisplayName(), extension);
        } catch (IOException ex) {
            setEnabled(false);
            name = NbBundle.getMessage(DownloadStudentZeugnis.class, "DownloadStudentZeugnis.action.disabledName");
        }
        return name;
    }

    @Override
    protected Term findCommonTerm() throws IOException {
        final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        term = null;
        final PrimaryUnitOpenSupport puos = unitsModel == null ? null : unitsModel.map(RemoteUnitsModel::getUnitOpenSupport)
                .filter(aos -> aos instanceof PrimaryUnitOpenSupport)
                .map(PrimaryUnitOpenSupport.class::cast)
                .orElse(null);
        if (puos != null) {
            final TermSchedule ts = puos.findTermSchedule();
            Term t = wd.isNow() ? ts.getCurrentTerm() : ts.getTerm(wd.getCurrentWorkingDate());
            if (term == null) {
                term = t;
            } else if (!term.equals(t)) {
                throw new IOException("Unequal terms.");
            }
        }
        if (term == null) {
            throw new IOException("No term.");
        }
        return term;
    }

    private Term findBeforeTerm() throws IOException {
        term = null;
        final PrimaryUnitOpenSupport puos = unitsModel.map(rm -> rm.getUnitOpenSupport())
                .filter(aos -> aos instanceof PrimaryUnitOpenSupport)
                .map(PrimaryUnitOpenSupport.class::cast)
                .orElse(null);
        final Term t = puos != null ? DownloadZeugnisse.findBeforeTermFor(puos) : null;
        if (term == null) {
            term = t;
        } else if (t != null && !term.equals(t)) {
            throw new IOException("Unequal terms.");
        }
        if (term == null) {
            throw new IOException("No term.");
        }
        return term;
    }

    @Override
    protected void actionPerformed(final List<RemoteStudent> list) {
        final TopComponent ac = TopComponent.getRegistry().getActivated();
        final Cursor before = ac.getCursor();
        ac.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        unitsModel.map(rm -> rm.getUnitOpenSupport())
                .filter(aos -> aos instanceof PrimaryUnitOpenSupport)
                .map(PrimaryUnitOpenSupport.class::cast)
                .ifPresent(puos -> {
                    final Term selTerm = term; //Do not reference instance field, may change after invocation!
                    RP.post(() -> {
                        list.forEach(rs -> {
                            try {
                                downLoad(puos, rs, selTerm);
                            } catch (IOException ex) {
                                PrimaryUnitDownloadAction.notifyError(ex);
                            }
                        });
                    }).addTaskListener(t -> ac.setCursor(before));
                });
    }

    //Term: do not use instance field, may
    private void downLoad(PrimaryUnitOpenSupport context, RemoteStudent rs, Term selectedTerm) throws IOException {
        final String href;
        try {
            href = URLs.reports(context.findBetulaProjectProperties());
        } catch (ConfigurationException cfex) {
            throw new IOException(cfex);
        }

//        final UnitId unit = context.getUnitId();
//        NamingResolver nr = context.findNamingResolver();
//        NamingResolver.Result rdn;
//        try {
//            rdn = nr.resolveDisplayNameResult(context.getUnitId());
//        } catch (IllegalAuthorityException ex) {
//            throw new IOException(ex);
//        }
//        rdn.addResolverHint("klasse.ohne.schuljahresangabe");
//        String klasse = rdn.getResolvedName(term);
        final String sj = Integer.toString((Integer) selectedTerm.getParameter(NdsTerms.JAHR));
        final int hj = (Integer) selectedTerm.getParameter(NdsTerms.HALBJAHR);
        final String file = NbBundle.getMessage(DownloadStudentZeugnis.class, "DownloadStudentZeugnis.filename", rs.getDirectoryName(), sj, hj, new Date(), extension);
        final String fe = URLEncoder.encode(file, "utf-8");

        String uri = href
                + fe
                //                + "?unit.id=" + Encode.getUnitIdEncoded(unit)
                //                + "&unit.authority=" + Encode.getUnitAuthorityEncoded(unit)
                + "?term.authority=" + Encode.getTermAuthorityEncoded(selectedTerm)
                + "&term.id=" + Encode.getTermIdEncoded(selectedTerm)
                + "&student.authority=" + Encode.getStudentAuthorityEncoded(rs.getStudentId()) //.replaceAll("\\.", "_")
                + "&student.id=" + Encode.getStudentIdEncoded(rs.getStudentId())
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

        WebServiceProvider wsp = context.findWebServiceProvider();
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

}
