/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2.impl;

import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.filechooser.FileFilter;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.sibank.DatenExportXml;
import org.thespheres.betula.sibank.SiBankImportStudentItem;
import org.thespheres.betula.sibank.SiBankKursItem;
import org.thespheres.betula.sibank.SiBankAssoziationenCollection;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankKlasseItem;
import org.thespheres.betula.sibank.SiBankPlus;
import org.thespheres.betula.sibank.impl.DatenExportDataObject;
import org.thespheres.betula.sibank.impl.KlassenUpdater;
import org.thespheres.betula.sibank.impl.SiBankSourceTargetAccess;
import org.thespheres.betula.sibank.impl.SiBankSourceOverrides;
import org.thespheres.betula.sibank.ui2.impl.KursauswahlVisualPanel2.KursauswahlPanel2;
import org.thespheres.betula.sibank.ui2.impl.SiBankCreateDocumentsVisualPanel.SiBankCreateDocumentsPanel;
import org.thespheres.betula.sibank.ui2.impl.SiBankUpdateKlasseVisualPanel.SiBankUpdateKlassePanel;
import org.thespheres.betula.sibank.ui2.impl.SiBankUpdateStudentsVisualPanel.SiBankUpdateStudentsPanel;
import org.thespheres.betula.sibank.ui2.impl.SibankImportConfigVisualPanel2.SibankImportConfigPanel2;
import org.thespheres.betula.ui.util.MimeFileFilter;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportWizard;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.sibank.ui2.ImportUnitsAction")
@ActionRegistration(
        displayName = "#ImportUnitsAction.displayName")
@ActionReference(path = "Menu/import-export", position = 5000)
@Messages({"ImportUnitsAction.displayName=SiBank Plus",
    "ImportUnitsAction.dialog.title=SiBankPlus-Import",
    "ImportUnitsAction.message.fileType=Import aus dem SiBankPlus-Bereich \"{0}\""})
public class ImportUnitsAction extends AbstractFileImportAction<SiBankImportData, DatenExportXml, SiBankImportTarget, ImportTargetsItem> {

    public static final String SIBANK_SOURCE_TARGET_ACCESS = "sibank-source-target-access";
    public static final String SIBANK_IMPORT_ACTION_TYPE = "sibank-import-action-type";
    private final static FileFilter FILTER = new MimeFileFilter(DatenExportDataObject.MIME,
            NbBundle.getMessage(ImportUnitsAction.class, "ImportUnitsAction.FileChooser.FileDescription"));

    public ImportUnitsAction() {
        super(NbBundle.getMessage(ImportUnitsAction.class, "ImportUnitsAction.dialog.title"));
    }

    @Override
    protected Product getProduct() {
        return SiBankPlus.getProduct();
    }

    @Override
    protected WizardDescriptor.Iterator<SiBankImportData> createIterator(DatenExportXml file, SiBankImportData d) {
        return new ImportActionWizardIterator(file);
    }

    @Override
    protected SiBankImportData createSettings(DatenExportXml file) {
        return new SiBankImportData();
    }

    @Override
    protected String findLastImportTargetUrl() {
        return NbPreferences.forModule(ImportUnitsAction.class).get(SAVED_IMPORT_TARGET_PROVIDER, null);
    }

    @Override
    protected void onConfigurationSelectionChange(SiBankImportTarget newConfig, SiBankImportData wiz, WizardDescriptor wd) {
        if (newConfig != null) {
            String title = dialogTitle + " - " + newConfig.getProviderInfo().getDisplayName();
            wd.setTitle(title);
        }
    }

    @Messages({"ImportUnitsAction.FileChooser.Title=Importiere SiBank",
        "ImportUnitsAction.FileChooser.FileDescription=SiBank xml-Daten"})
    @Override
    protected DatenExportXml openFile() throws IOException {
        File home = new File(System.getProperty("user.home"));
        String title = NbBundle.getMessage(ImportUnitsAction.class, "ImportUnitsAction.FileChooser.Title");
        FileChooserBuilder fcb = new FileChooserBuilder(ImportUnitsAction.class);
        fcb.setTitle(title).setDefaultWorkingDirectory(home).setFileHiding(true).setFileFilter(FILTER);
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
        DatenExportXml xml = dob.getLookup().lookup(DatenExportXml.class);
        if (xml == null) {
            throw new IOException();
        }
        return xml;
    }

    @Override
    protected void beforeWizardShow(SiBankImportData wiz) {
        final DatenExportXml xml = (DatenExportXml) wiz.getProperty(AbstractFileImportAction.DATA);
        final DatenExportXml.File file = xml.guessFile();
        String msg = NbBundle.getMessage(ImportUnitsAction.class, "ImportUnitsAction.message.fileType", file.getDisplayName());
        ImportUtil.getIO().getOut().println(msg);
        wiz.putProperty(SIBANK_IMPORT_ACTION_TYPE, file);
        if (!DatenExportXml.File.SCHUELER.equals(file)) {
            final SiBankSourceTargetAccess l = new SiBankSourceTargetAccess(wiz, iterator);
            wiz.addPropertyChangeListener(l);
            wiz.putProperty(SIBANK_SOURCE_TARGET_ACCESS, l);
         final   SiBankSourceOverrides userOverrides = new SiBankSourceOverrides(wiz);
            wiz.putProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES, userOverrides);
        }
    }

    @Override
    protected void onWizardFinishOK(SiBankImportTarget config, Set<?> selected, DatenExportXml xml, SiBankImportData wiz) {
        super.onWizardFinishOK(config, selected, xml, wiz);
        NbPreferences.forModule(ImportUnitsAction.class).put(SAVED_IMPORT_TARGET_PROVIDER, config.getProviderInfo().getURL());
//        RemoteLookup remote = config.getRemoteLookup();
//        StudenplanUpdater sup = new StudenplanUpdater(remote, selected);
//        remote.getRequestProcessor().post(sup);
//        SiBankSourceTargetAccess acc = (SiBankSourceTargetAccess) wiz.getProperty(SIBANK_SOURCE_TARGET_ACCESS);
//        if (acc != null) {
//            SiBankAssoziationenCollection links = (SiBankAssoziationenCollection) wiz.getProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS);
//            if (links != null) {
//                acc.saveSourceTargetLinks(links, config);
//            }
//        }
    }

    @Override
    protected void onUpdateFinished(SiBankImportTarget config, Set<?> selected, DatenExportXml xml, SiBankImportData wiz, AbstractUpdater<?> updater) {
        final SiBankSourceTargetAccess acc = (SiBankSourceTargetAccess) wiz.getProperty(SIBANK_SOURCE_TARGET_ACCESS);
        if (acc != null) {
            final SiBankAssoziationenCollection links = (SiBankAssoziationenCollection) wiz.getProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS);
            if (links != null) {
                acc.saveSourceTargetLinks(links, config);
            }
        }
    }

    @Override
    protected TargetItemsUpdater createUpdater(Set<?> selected, SiBankImportTarget config, Term term, SiBankImportData wiz) {
        final DatenExportXml.File file = (DatenExportXml.File) wiz.getProperty(SIBANK_IMPORT_ACTION_TYPE);
        if (DatenExportXml.File.SCHUELER.equals(file)) {
            final SiBankKlasseItem[] iti = selected.stream()
                    .map(SiBankKlasseItem.class::cast)
                    .toArray(SiBankKlasseItem[]::new);
            return new KlassenUpdater(iti, config.getWebServiceProvider(), term, Collections.singletonList(new DefaultUpdaterFilter()), config);
        }
        final SiBankKursItem[] iti = selected.stream()
                .map(SiBankKursItem.class::cast)
                .toArray(SiBankKursItem[]::new);
        return new TargetItemsUpdater<>(iti, config.getWebServiceProvider(), term, Collections.singletonList(new DefaultUpdaterFilter()));
    }

    static class ImportActionWizardIterator extends AbstractFileImportWizard<SiBankImportData> {

        private final DatenExportXml.File file;

        private ImportActionWizardIterator(DatenExportXml f) {
            file = f.guessFile();
        }

        @Override
        protected ArrayList<WizardDescriptor.Panel<SiBankImportData>> createPanels() {
            final ArrayList<WizardDescriptor.Panel<SiBankImportData>> ret = new ArrayList<>();
            ret.add(new SibankImportConfigPanel2());
            ret.add(new KursauswahlPanel2());
            if (DatenExportXml.File.SCHUELER.equals(file)) {
                ret.add(new SiBankUpdateKlassePanel());
                ret.add(new SiBankUpdateStudentsPanel());
            } else {
                ret.add(new SiBankCreateDocumentsPanel());
            }
            return ret;
        }

    }

    static class DefaultUpdaterFilter implements UpdaterFilter<ImportTargetsItem, TargetDocumentProperties> {

        @Override
        public boolean accept(ImportTargetsItem iti) {
            return iti.isValid();
        }

        @Override
        public boolean accept(ImportTargetsItem iti, UnitId u, StudentId stud) {
            if (iti instanceof SiBankKlasseItem) {
                final SiBankImportStudentItem isi = ((SiBankKlasseItem) iti).getStudents().get(stud);
                return isi != null ? (isi.isValid() && isi.isSelected()) : false;
            } else if (iti instanceof SiBankKursItem) {
                final List<UpdaterFilter> f = iti.getFilters();
                return f.isEmpty() ? true : f.stream().allMatch(sbf -> sbf.accept(iti, u, stud));
            }
            return true;
        }

        @Override
        public boolean accept(ImportTargetsItem iti, TargetDocumentProperties td, StudentId stud) {
            if (iti instanceof SiBankKursItem) {
                final List<UpdaterFilter> f = ((SiBankKursItem) iti).getFilters();
                return f.isEmpty() ? true : f.stream().allMatch(sbf -> sbf.accept(iti, td, stud));
            }
            return true;
        }

    }
}
