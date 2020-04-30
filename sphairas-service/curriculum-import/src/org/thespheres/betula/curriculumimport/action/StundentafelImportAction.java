package org.thespheres.betula.curriculumimport.action;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.thespheres.betula.curriculumimport.StundentafelImportTargetsItem;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.filechooser.FileFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.util.MimeFileFilter;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportAction;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction")
@ActionRegistration(
        displayName = "#StundentafelImportAction.displayName")
@ActionReferences({
    @ActionReference(path = "Menu/import-export", position = 55000),
    @ActionReference(path = "Loaders/text/remote-unit-descriptor+xml/Actions", position = 6000, separatorBefore = 5000),
    @ActionReference(path = "Loaders/application/betula-unit-data/Actions", position = 4200, separatorBefore = 4000, separatorAfter = 5000)})
@Messages({"StundentafelImportAction.displayName=Import aus einer Stundentafel",
    "StundentafelImportAction.dialog.title=Studentafel-Import"})
public class StundentafelImportAction extends AbstractImportAction<StundentafelImportSettings, ConfigurableImportTarget, StundentafelImportTargetsItem> {

    public static final String CURRICULUM = "curriculum";
    public static final String SELECTED_UNITS = "selected.units";
    public static final String ALLOW_SELECT_UNITS = "allow.select.units";
    private final static FileFilter FILE_FILTER = new MimeFileFilter("text/curriculum-file+xml",
            NbBundle.getMessage(StundentafelImportAction.class, "StundentafelImportAction.FileChooser.FileDescription"));
    private final List<PrimaryUnitOpenSupport> context;

    public StundentafelImportAction(final List<PrimaryUnitOpenSupport> context) {
        super(NbBundle.getMessage(StundentafelImportAction.class, "StundentafelImportAction.dialog.title"));
        this.context = context;
    }

    @Override
    protected Product getProduct() {
        return Product.NO;
    }

    @Override
    protected StundentafelImportSettings createSettingsAndIterator() {
        final StundentafelImportSettings settings;
        if (context != null) {
            settings = ContextStundentafelImportSettings.createSettings(context);
        } else {
            settings = GeneralStundentafelImportSettings.createSettings();
        }
        this.iterator = settings.createIterator();
        return settings;
    }

    @Override
    protected void beforeWizardShow(final StundentafelImportSettings wiz) {
        final CurriculumSourceOverrides userOverrides = new CurriculumSourceOverrides(wiz);
        wiz.putProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES, userOverrides);
    }

    @Messages({"StundentafelImportAction.FileChooser.Title=Importiere aus Stundentafel",
        "StundentafelImportAction.FileChooser.FileDescription=xml-Dateien"})
    static DataObject openFile() throws IOException {
//        final FileObject projectDir = context != null ? findCommonImportProjectDir(context) : null;
//        final File home = projectDir != null ? FileUtil.toFile(projectDir) : new File(System.getProperty("user.home"));
        final String title = NbBundle.getMessage(StundentafelImportAction.class, "StundentafelImportAction.FileChooser.Title");
        final File home = new File(System.getProperty("user.home"));
        final FileChooserBuilder fcb = new FileChooserBuilder(StundentafelImportAction.class);
        fcb.setTitle(title)
                .setDefaultWorkingDirectory(home)
                .setFileHiding(true)
                .setFileFilter(FILE_FILTER);
        final File open = fcb.showOpenDialog();
        if (open == null || !open.exists()) {
            return null;
        }
        final FileObject fo = FileUtil.toFileObject(open);
        if (fo == null) {
            throw new IOException();
        }
        return DataObject.find(fo);
    }

    @Override
    protected AbstractUpdater<ImportItem> createUpdater(Set<?> selected, ConfigurableImportTarget config, Term term, StundentafelImportSettings wiz) {
        final StundentafelImportTargetsItem[] iti = selected.stream()
                .map(StundentafelImportTargetsItem.class::cast)
                .toArray(StundentafelImportTargetsItem[]::new);
        return new TargetItemsUpdater(iti, config.getWebServiceProvider(), term, Collections.singletonList(new StundentafelImportTargetsItem.Filter()));
    }

    @Override
    protected void onUpdateFinished(final ConfigurableImportTarget config, final Set<?> selected, final StundentafelImportSettings wiz, final AbstractUpdater<?> updater) {
        wiz.saveAssociations();
    }

    @Override
    protected void onWizardExit(final StundentafelImportSettings d) {
        d.releaseLock();
    }

}
