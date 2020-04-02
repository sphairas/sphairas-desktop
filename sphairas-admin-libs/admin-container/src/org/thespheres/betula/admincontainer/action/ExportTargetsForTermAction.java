/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.io.File;
import org.thespheres.betula.admin.units.AbstractRemoteTargetsAction;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.Identity;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.FileChooserBuilderWithHint;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.ContainerBuilder;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.admin.container.action.ExportTargetsForTermAction")
@ActionRegistration(
        displayName = "#ExportTargetsForTermAction.action.name",
        lazy = false,
        surviveFocusChange = true,
        asynchronous = true)
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-target-assessment-document/Actions", position = 19000)})
@NbBundle.Messages({"ExportTargetsForTermAction.FileChooser.Title=Ordner",
    "ExportTargetsForTermAction.action.name=Zensurenlisten exportieren (Container-xml)",
    "ExportTargetsForTermAction.fileHint.multiple={0} Zensurenlisten",
    "ExportTargetsForTermAction.action.failure.title=Fehler beim Exportieren der Zensurenlisten",
    "ExportTargetsForTermAction.action.overwriteFile.title=Bestätigen",
    "ExportTargetsForTermAction.action.overwriteFile.text=Existierende Datei {0} überschreiben?",
    "ExportTargetsForTermAction.action.success.text={0} geschrieben"})
public final class ExportTargetsForTermAction extends AbstractRemoteTargetsAction {

    static final UnitId NULLUNIT = new UnitId("null", "unknown");
    private static JAXBContext jaxb;

    public ExportTargetsForTermAction() {
    }

    private ExportTargetsForTermAction(Lookup context) {
        super(context, false);
        updateEnabled();
    }

    @Override
    protected AbstractRemoteTargetsAction createAbstractRemoteTargetsAction(Lookup context) {
        return new ExportTargetsForTermAction(context);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ExportTargetsForTermAction.class, "ExportTargetsForTermAction.action.name");
    }

    @Override
    public void actionPerformed(final List<RemoteTargetAssessmentDocument> l, Optional<AbstractUnitOpenSupport> opt) {
        if (!opt.isPresent()) {
            return;
        }

        final AbstractUnitOpenSupport support = opt.get();
        final UnitId unit = support instanceof PrimaryUnitOpenSupport ? ((PrimaryUnitOpenSupport) support).getUnitId() : null;

        final DocumentsModel model;
        final NamingResolver nr;
        try {
            model = support.findDocumentsModel();
            nr = support.findNamingResolver();
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(ExportTargetsForTermAction.class).log(LogLevel.INFO_WARNING, ex.getLocalizedMessage(), ex);
            return;
        }

        final Map<DocumentId, List<RemoteTargetAssessmentDocument>> mapped = l.stream()
                .collect(Collectors.groupingBy(t -> model.convert(t.getDocumentId())));
        Identity id = null;
        String baseName;
        if (mapped.size() == 1) {
            id = mapped.keySet().iterator().next();
        } else if (unit != null) {
            id = unit;
        }
        if (id != null) {
            try {
                baseName = nr.resolveDisplayNameResult(id).getResolvedName(term);
            } catch (IllegalAuthorityException ex) {
                baseName = id.getId().toString();
            }
            baseName = baseName.replaceAll("/", "-");
        } else {
            baseName = NbBundle.getMessage(ExportTargetsForTermAction.class, "ExportTargetsForTermAction.fileHint.multiple", l.size());
        }
        final String fName = baseName + ".xml";

        final File f = showDialog(fName);
        if (f == null || f.isDirectory()) {
            return;
        }
        final Path file = f.toPath();
        if (Files.exists(file)) {
            final String title = NbBundle.getMessage(ExportTargetsForTermAction.class, "ExportTargetsForTermAction.action.overwriteFile.title");
            final String text = NbBundle.getMessage(ExportTargetsForTermAction.class, "ExportTargetsForTermAction.action.overwriteFile.text", file.toString());
            final DialogDescriptor dd = new DialogDescriptor(text, title);
            final Object res = DialogDisplayer.getDefault().notify(dd);
            if (!res.equals(DialogDescriptor.OK_OPTION)) {
                return;
            }
        }
        try {
            //
//        FoldHandle fold;
//        try {
//            fold = AbstractFileImportAction.messageActionStart(getName());
//        } catch (IOException ex) {
//            Logger.getLogger(ExportTargetsForTermAction.class.getCanonicalName()).severe(ex.getLocalizedMessage());
//            return;
//        }
            processSelection(support, l, unit, file);
//        fold.silentFinish();
            final String msg = NbBundle.getMessage(ExportTargetsForTermAction.class, "ExportTargetsForTermAction.action.success.text", file.toString());
            StatusDisplayer.getDefault().setStatusText(msg);
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(ExportTargetsForTermAction.class).log(LogLevel.INFO_WARNING, ex.getLocalizedMessage(), ex);
        }
    }

    private File showDialog(final String hint) {
        final File home = new File(System.getProperty("user.home"));
        final String title = NbBundle.getMessage(ExportTargetsForTermAction.class, "ExportTargetsForTermAction.FileChooser.Title");
        final FileChooserBuilderWithHint fcb = new FileChooserBuilderWithHint(ExportTargetsForTermAction.class, hint);
        fcb.setTitle(title).setDefaultWorkingDirectory(home).setFileHiding(true);
        return fcb.showSaveDialog();
    }

    private static void processSelection(final AbstractUnitOpenSupport uos, final List<RemoteTargetAssessmentDocument> l, final UnitId unit, final Path folder) throws IOException {
        final WebServiceProvider wsp = uos.findWebServiceProvider();
        final Set<DocumentId> docs = l.stream()
                .map(RemoteTargetAssessmentDocument::getDocumentId)
                .collect(Collectors.toSet());
        wsp.getDefaultRequestProcessor().post(() -> {

            try {
                fetchAndWrite(unit, docs, wsp, folder);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(ExportTargetsForTermAction.class).log(LogLevel.INFO_WARNING, ex.getMessage());
                final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
                final String title = NbBundle.getMessage(ExportTargetsForTermAction.class, "ExportTargetsForTermAction.action.failure.title");
                final String detail = ex.getMessage();
                NotificationDisplayer.getDefault()
                        .notify(title, ic, detail != null ? detail : "", null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
            }

        });
    }

    private static void fetchAndWrite(final UnitId unit, final Set<DocumentId> doc, final WebServiceProvider service, final Path out) throws IOException {
        final ContainerBuilder builder = new ContainerBuilder();
        final String[] path = Paths.UNITS_TARGETS_PATH;
        final Action action = Action.REQUEST_COMPLETION;
        final boolean fragment = true;

        final Template root = new Entry(null, unit == null ? NULLUNIT : unit);
        doc.stream()
                .map(d -> new TargetAssessmentEntry<>(d, action, fragment))
                .forEach(root.getChildren()::add);
        builder.add(root, path);

        final Container response;
        try {
            response = service.createServicePort().solicit(builder.getContainer());
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        }
        final Marshaller marshaller;
        try {
            marshaller = getJAXB().createMarshaller();
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
        try {
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
        } catch (PropertyException ex) {
            throw new IOException(ex);
        }
        try (OutputStream os = Files.newOutputStream(out)) {
            marshaller.marshal(response, os);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }

//        final List<Envelope> l = DocumentUtilities.findEnvelope(response, path);
//        final List<TargetAssessmentEntry<TermId>> ret = l.stream()
//                .filter(Entry.class::isInstance)
//                .map(Entry.class::cast)
//                .filter(e -> e.getIdentity() instanceof UnitId)
//                .filter(e -> ((UnitId) e.getIdentity()).equals(unit))
//                .flatMap(e -> e.getChildren().stream())
//                .filter(TargetAssessmentEntry.class::isInstance)
//                .map(t -> (TargetAssessmentEntry<TermId>) t)
//                .filter(t -> doc.contains(t.getIdentity()))
//                .collect(Collectors.toList());
    }

    private static JAXBContext getJAXB() {
        if (jaxb == null) {
            try {
                jaxb = JAXBContext.newInstance(Container.class);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return jaxb;
    }
}
