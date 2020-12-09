/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import org.thespheres.betula.gpuntis.impl.StudenplanUpdater;
import org.thespheres.betula.gpuntis.impl.UntisXmlDataObject;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.MissingResourceException;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.filechooser.FileFilter;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.thespheres.betula.gpuntis.ImportUntisUtil;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.Untis;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.gpuntis.UntisImportSigneeItem;
import org.thespheres.betula.xmlimport.utilities.SigneeUpdater;
import org.thespheres.betula.gpuntis.impl.UntisSourceUserOverrides;
import org.thespheres.betula.gpuntis.impl.UntisDefaultUpdaterFilter;
import org.thespheres.betula.gpuntis.impl.UntisSourceTargetAccess;
import org.thespheres.betula.gpuntis.impl.UntisSourceTargetLinks;
import org.thespheres.betula.gpuntis.impl.UploadXml;
import org.thespheres.betula.gpuntis.xml.Document;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.MimeFileFilter;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;

@Messages({"ImportAction.dialog.title=Untis-Import"})
public class ImportAction extends AbstractFileImportAction<UntisImportData, Document, UntisImportConfiguration, ImportedLesson> {

    public static final String LESSON = "lesson";
    public static final String SIGNEE = "signee";
    public static final String UNTIS_SOURCE_TARGET_ACCESS = "untis-source-target-access";
    private final String type;
    private final static FileFilter FILE_FILTER = new MimeFileFilter(UntisXmlDataObject.MIME,
            NbBundle.getMessage(ImportAction.class, "ImportLessonAction.FileChooser.FileDescription"));

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.gpuntis.ui.ImportAction.lessonImport")
    @ActionRegistration(
            displayName = "#ImportAction.lessonImport.displayName"
    )
    @ActionReference(path = "Menu/import-export", position = 2000)
    @Messages({"ImportAction.lessonImport.displayName=Untis (Unterricht)"})
    public static ImportAction lessonImport() {
        return new ImportAction(ImportAction.LESSON);
    }

    @ActionID(category = "Betula",
            id = "org.thespheres.betula.gpuntis.ui.ImportAction.signeeImport")
    @ActionRegistration(
            displayName = "#ImportAction.signeeImport.displayName")
    @ActionReference(path = "Menu/import-export", position = 2200)
    @Messages({"ImportAction.signeeImport.displayName=Untis (Lehrer)"})
    public static ImportAction signeeImport() {
        return new ImportAction(ImportAction.SIGNEE);
    }
    private DataObject data;

    private ImportAction(String type) {
        super(NbBundle.getMessage(ImportAction.class, "ImportAction.dialog.title"));
        this.type = type;
    }

    @Override
    protected Product getProduct() {
        return Untis.getProduct();
    }

    @Override
    protected WizardDescriptor.Iterator<UntisImportData> createIterator(Document xml, UntisImportData d) {
        return new ImportActionWizardIterator(type, d);
    }

    @Override
    protected UntisImportData createSettings(Document xml) {
        return new UntisImportData();
    }

    @Override
    protected String findLastImportTargetUrl() {
        return NbPreferences.forModule(ImportAction.class).get(SAVED_IMPORT_TARGET_PROVIDER, null);
    }

    @Override
    protected void onConfigurationSelectionChange(UntisImportConfiguration newConfig, UntisImportData wiz, WizardDescriptor wd) {
        if (newConfig != null) {
            String title = dialogTitle + " - " + newConfig.getProviderInfo().getDisplayName();
            wd.setTitle(title);
            final Term term = findImportTerm(wiz.getUntisDocument(), newConfig);
            wiz.putProperty(TERM, term);
        }
    }

//    @Override
    static Term findImportTerm(Document xml, UntisImportConfiguration config) {
        TermSchedule schedule = config.getTermSchemeProvider().getScheme(TermSchedule.DEFAULT_SCHEME, TermSchedule.class);
        return ImportUntisUtil.findDocumentTerm(xml.getGeneral(), schedule);
    }

    //nb.native.filechooser && tool-options-darstellung-laf-maximize use of native look and feel
    @Messages({"ImportLessonAction.FileChooser.Title=Importiere Untis",
        "ImportLessonAction.FileChooser.FileDescription=Untis xml-Daten"})
    @Override
    protected Document openFile() throws IOException {
        File home = new File(System.getProperty("user.home"));
        String title = NbBundle.getMessage(ImportAction.class, "ImportLessonAction.FileChooser.Title");
        FileChooserBuilder fcb = new FileChooserBuilder(ImportAction.class);
        fcb.setTitle(title).setDefaultWorkingDirectory(home).setFileHiding(true).setFileFilter(FILE_FILTER);
        File open = fcb.showOpenDialog();
        if (open == null || !open.exists()) {
            return null;
        }
        FileObject fo = FileUtil.toFileObject(open);
        DataObject dob;
        try {
            dob = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            throw new IOException(ex);
        }
        Document xml = dob.getLookup().lookup(Document.class);
        if (xml == null) {
            throw new IOException();
        }
        return xml;
    }

    @Override
    protected void beforeWizardShow(UntisImportData wiz) {
        if (ImportAction.LESSON.equals(type)) {
            final UntisSourceTargetAccess l = new UntisSourceTargetAccess(wiz, iterator);
            wiz.addPropertyChangeListener(l);
            wiz.putProperty(UNTIS_SOURCE_TARGET_ACCESS, l);
            final UntisSourceUserOverrides userOverrides = new UntisSourceUserOverrides(wiz);
            wiz.putProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES, userOverrides);
        }
    }

    @Override
    protected void onWizardFinishOK(UntisImportConfiguration config, Set<?> selected, Document xml, UntisImportData wiz) {
        if (wiz.isUploadUntisDocument()) {
            ImportAction.this.uploadUntisDocument(config, xml);
        }
        super.onWizardFinishOK(config, selected, xml, wiz);
        NbPreferences.forModule(ImportAction.class).put(SAVED_IMPORT_TARGET_PROVIDER, config.getProviderInfo().getURL());
//        final RemoteLookup remote = config.getRemoteLookup();
        if (ImportAction.LESSON.equals(type)) {
            final StudenplanUpdater sup = new StudenplanUpdater(config, (Set<ImportedLesson>) selected);
            config.getWebServiceProvider().getDefaultRequestProcessor().post(sup);
//            remote.getRequestProcessor().post(sup);//TODO: use own RP  --> may last long until processed if app opened recently
        }
    }
    
    @Override
    protected void onUpdateFinished(UntisImportConfiguration config, Set<?> selected, Document xml, UntisImportData wiz, AbstractUpdater<?> updater) {
        final UntisSourceTargetAccess acc = (UntisSourceTargetAccess) wiz.getProperty(UNTIS_SOURCE_TARGET_ACCESS);
        if (acc != null) {
            UntisSourceTargetLinks links = (UntisSourceTargetLinks) wiz.getProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS);
            if (links != null) {
                acc.saveSourceTargetLinks(links, config);
            }
        }
    }

    @Messages({"ImportAction.UploadXml.error.title=Fehler beim Upload",
        "ImportAction.UploadXml.error.message=Beim Upload der Units-Datei ist ein Fehler aufgetreten: (Type: {0}, Message: {1}.",
        "ImportAction.UploadXml.succes=Untis-Datei nach {0} hochgeladen."})
    private void uploadUntisDocument(UntisImportConfiguration config, Document xml) throws MissingResourceException {
        try {
            UploadXml.upload(config, xml);
            final String msg = NbBundle.getMessage(ImportAction.class, "ImportAction.UploadXml.succes", config.getUntisXmlDocumentResource());
            ImportUtil.getIO().getOut().println(msg);
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(ImportAction.class).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
            final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
            final String title = NbBundle.getMessage(ImportAction.class, "ImportAction.UploadXml.error.title");
            final String message = NbBundle.getMessage(ImportAction.class, "ImportAction.UploadXml.error.message");
            NotificationDisplayer.getDefault()
                    .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
        }
    }

    @Override
    protected AbstractUpdater<?> createUpdater(Set<?> selected, UntisImportConfiguration config, Term term, UntisImportData wiz) {
        if (ImportAction.LESSON.equals(type)) {
            final ImportedLesson[] iti = selected.stream()
                    .map(ImportedLesson.class::cast)
                    .toArray(ImportedLesson[]::new);
            return new TargetItemsUpdater<>(iti, config.getWebServiceProvider(), term, Collections.singletonList(new UntisDefaultUpdaterFilter()));
        } else if (ImportAction.SIGNEE.equals(type)) {
            final UntisImportSigneeItem[] items = selected.stream()
                    .map(UntisImportSigneeItem.class::cast)
                    .toArray(UntisImportSigneeItem[]::new);
            return new SigneeUpdater<>(config, items, UntisImportSigneeItem::doUpdate);
        }
        return null;
    }
}
