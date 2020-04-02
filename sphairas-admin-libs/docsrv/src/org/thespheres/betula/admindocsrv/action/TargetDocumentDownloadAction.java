/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admindocsrv.action;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admindocsrv.AbstractDownloadAction;
import org.thespheres.betula.admindocsrv.Encode;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.JavaUtilities;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.FileChooserBuilderWithHint;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@Messages({"TargetDocumentDownloadAction.FileChooser.Title=Ordner",
    "TargetDocumentDownloadAction.error.title=Fehler",
    "TargetDocumentDownloadAction.action.name=Zensurenliste für {0} erstellen ({1})",
    "TargetDocumentDownloadAction.action.disabledName.pdf=Zensurenlisten erstellen (pdf)",
    "TargetDocumentDownloadAction.action.disabledName.csv=Zensurenlisten erstellen (csv)",
    "TargetDocumentDownloadAction.download.filename={0} Zensurenliste ({1,date,d.M.yy HH'h'mm}).{2}",
    "TargetDocumentDownloadAction.download.filename.multipleTargets=Zensurenlisten.zip",
    "TargetDocumentDownloadAction.action.overwriteFile.title=Bestätigen",
    "TargetDocumentDownloadAction.action.overwriteFile.text=Existierende Datei {0} überschreiben?",
    "TargetDocumentDownloadAction.action.success={0} geschrieben"})
class TargetDocumentDownloadAction extends AbstractDownloadAction<RemoteTargetAssessmentDocument> {

    protected final RequestProcessor RP = new RequestProcessor(TargetDocumentDownloadAction.class);

    @SuppressWarnings("OverridableMethodCallInConstructor")
    protected TargetDocumentDownloadAction(String mime, String extension) {
        super(mime, extension);
        putValue(Action.NAME, getDisabledName());
        setEnabled(false);
    }

    protected TargetDocumentDownloadAction(Lookup context, String mime, String extension) {
        super(context, RemoteTargetAssessmentDocument.class, mime, extension, true, false);
        updateEnabled();
        updateName();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new TargetDocumentDownloadAction(actionContext, mime, extension);
    }

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.admindocsrv.action.TargetDocumentDownloadAction.pdfAction")
    @ActionRegistration(displayName = "#TargetDocumentDownloadAction.action.disabledName.pdf",
            lazy = false)
    @ActionReferences({
        //        @ActionReference(path = "Menu/units-administration", position = 1400),
        @ActionReference(path = "Loaders/application/betula-remote-target-assessment-document/Actions", position = 51000, separatorBefore = 50000)
    })
    public static Action pdfAction() {
        return new TargetDocumentDownloadAction("application/pdf", "pdf");
    }

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.admindocsrv.action.TargetDocumentDownloadAction.csvAction")
    @ActionRegistration(displayName = "#TargetDocumentDownloadAction.action.disabledName.csv",
            lazy = false)
    @ActionReferences({
        //        @ActionReference(path = "Menu/units-administration", position = 1400),
        @ActionReference(path = "Loaders/application/betula-remote-target-assessment-document/Actions", position = 52000, separatorBefore = 50000)
    })
    public static Action csvAction() {
        return new TargetDocumentDownloadAction("text/csv", "csv");
    }

    @Override
    public String getName() {
        String name;
        try {
            term = findCommonTerm();
            name = NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.action.name", new Object[]{term.getDisplayName(), extension});
        } catch (IOException ex) {
            setEnabled(false);
            name = getDisabledName();
        }
        return name;
    }

    protected String getDisabledName() {
        if (extension == null) {
            return null;
        }
        return NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.action.disabledName." + extension);
    }

    @Override
    protected void actionPerformed(List<RemoteTargetAssessmentDocument> context) {
        final Term selectedTerm = term;
        actionPerformed(context, selectedTerm);
    }

    void actionPerformed(final List<RemoteTargetAssessmentDocument> list, final Term t) {
        final TopComponent ac = TopComponent.getRegistry().getActivated();
        ActionEnvironment env = ActionEnvironment.create(context);
        if (env == null) {
            final String msg = "Trying to create an ActionEnvironment from RemoteTargetAssessmentDocument list action context.";
            PlatformUtil.getCodeNameBaseLogger(ActionEnvironment.class).log(Level.FINE, msg);
            env = ActionEnvironment.create(list);
        }
        if (env == null) {
            final String msg = "Failed to create an ActionEnvironment.";
            PlatformUtil.getCodeNameBaseLogger(ActionEnvironment.class).log(Level.FINE, msg);
            return;
        }
        //        
        final RemoteTargetAssessmentDocument single = list.size() == 1 ? list.get(0) : null;
        final String hint;
        if (single == null) {
            hint = NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.download.filename.multipleTargets");
        } else {
            final String name = single.getName().getDisplayName(term);
            hint = NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.download.filename", name.replace("/", "_"), new Date(), extension);
        }
        //
        File f = showFileDialog(hint);
        if (f == null || f.isDirectory()) {
            return;
        } else if (single == null && !f.getName().endsWith(".zip")) {
            f = new File(f.getParentFile(), f.getName() + ".zip");
        } else if (single != null && !f.getName().endsWith("." + extension)) {
            f = new File(f.getParentFile(), f.getName() + "." + extension);
        }
        final Path zipFileOrXmlFile = f.toPath();
        if (Files.exists(zipFileOrXmlFile)) {
            final String title = NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.action.overwriteFile.title");
            final String text = NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.action.overwriteFile.text", zipFileOrXmlFile.toString());
            final DialogDescriptor dd = new DialogDescriptor(text, title);
            final Object res = DialogDisplayer.getDefault().notify(dd);
            if (!res.equals(DialogDescriptor.OK_OPTION)) {
                return;
            }
        }
        final LocalProperties lfp = env.getProperties();
        final WebServiceProvider wsp = env.getService();
        RP.post(() -> {
            final Cursor before = ac.getCursor();
            ac.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                if (list.size() != 1) {
                    final Path tempFolder = Files.createTempDirectory(TargetDocumentDownloadAction.class.getSimpleName());
                    tempFolder.toFile().deleteOnExit();
                    for (final RemoteTargetAssessmentDocument rtad : list) {
                        downLoad(rtad, lfp, t, wsp, tempFolder);
                    }
                    JavaUtilities.zip(tempFolder, zipFileOrXmlFile);
                    tempFolder.toFile().delete();
                } else {
                    downLoad(single, lfp, t, wsp, zipFileOrXmlFile);
                }
                final String msg = NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.action.success", zipFileOrXmlFile.toString());
                StatusDisplayer.getDefault().setStatusText(msg);
            } catch (IOException ex) {
                notify(ex);
            } finally {
                ac.setCursor(before);
            }
        });
    }

    void notify(IOException ex) throws MissingResourceException {
        PlatformUtil.getCodeNameBaseLogger(TargetDocumentDownloadAction.class).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.error.title");
        final String detail = ex.getMessage();
        NotificationDisplayer.getDefault()
                .notify(title, ic, detail != null ? detail : "", null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    private File showFileDialog(final String hint) {
        File home = new File(System.getProperty("user.home"));
        String title = NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.FileChooser.Title");
        FileChooserBuilderWithHint fcb = new FileChooserBuilderWithHint(TargetDocumentDownloadAction.class, hint);
        fcb.setTitle(title).setDefaultWorkingDirectory(home).setFileHiding(true);
        return fcb.showSaveDialog();
    }

    protected void downLoad(RemoteTargetAssessmentDocument context, LocalProperties properties, Term tid, WebServiceProvider service, Path path) throws IOException {
        String href = properties.getProperty("zgnsrvUrl");
        if (href == null) {
            final String msg = NbBundle.getMessage(AbstractDownloadAction.class, "AbstractDownloadAction.missingHref.exception", properties.getName());
            throw new IOException(msg);
        }
        final String name = context.getName().getDisplayName(term);
        final String fName = NbBundle.getMessage(TargetDocumentDownloadAction.class, "TargetDocumentDownloadAction.download.filename", name.replace("/", "_"), new Date(), extension);
        final String fe = URLEncoder.encode(fName, "utf-8");
        final DocumentId document = context.getDocumentId();

        String uri = href
                + fe
                + "?document.id=" + Encode.getDocumentIdEncoded(document)
                + "&document.authority=" + Encode.getDocumentAuthorityEncoded(document)
                + "&term.authority=" + Encode.getTermAuthorityEncoded(tid)
                + "&term.id=" + Encode.getTermIdEncoded(tid)
                + "&document=betula.target"
                + "&mime=" + mime;

//        FileObject files = FileUtil.createFolder(dir, "Dateien");
        final Path tmp;
        if (Files.isDirectory(path)) {
            tmp = path.resolve(fName);
        } else {
            tmp = path;
        }
        try {
            doDownload(uri, tmp, service);
        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        }

//        files.refresh();
    }
}
