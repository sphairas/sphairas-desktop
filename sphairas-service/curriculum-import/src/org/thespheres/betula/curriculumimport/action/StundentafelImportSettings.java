/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.action;

import org.thespheres.betula.curriculumimport.xml.CurriculumAssoziationenCollection;
import java.io.IOException;
import java.util.Objects;
import javax.swing.Icon;
import org.thespheres.betula.curriculumimport.StundentafelImportTargetsItem;
import org.openide.WizardDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.services.ui.util.WriteLockCapability;
import org.thespheres.betula.services.util.NbUtilities;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.DefaultImportWizardSettings;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
public class StundentafelImportSettings extends DefaultImportWizardSettings<ConfigurableImportTarget, StundentafelImportTargetsItem> {

    private Curriculum curriculum;
    private DataObject file;
    private WriteLockCapability.WriteLock currentLock;

    protected StundentafelImportSettings() {
    }

    public Curriculum getCurriculum() {
        return curriculum;
    }

    void setCurriculum(final DataObject file) {
        if (!Objects.equals(this.file, file)) {
            releaseLock();
            final Curriculum before = this.curriculum;
            final Curriculum ret;
            try {
                ret = NbUtilities.waitForLookup(file.getLookup(), Curriculum.class, 5000);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(GeneralStundentafelImportSettings.class).warning(ex.getLocalizedMessage());
                this.curriculum = null;
                return;
            }
            if (ret == null) {
                this.curriculum = null;
                return;
            }
            this.file = file;
            this.curriculum = ret;
            loadAssociations();
            this.pSupport.firePropertyChange(StundentafelImportAction.CURRICULUM, before, this.curriculum);
        }
    }

    @NbBundle.Messages({
        "StundentafelImportSettings.noWriteLock.title=Gesperrt",
        "StundentafelImportSettings.notUpdated.message=Die Datei {0} ist gesperrt und wurde nicht aktualisiert."})
    protected void notifyNoWriteLock() {
        final Icon ic = ImageUtilities.loadImageIcon("org/thespheres/betula/ui/resources/exclamation-red-frame.png", true);
        final String title = NbBundle.getMessage(StundentafelImportSettings.class, "StundentafelImportSettings.noWriteLock.title");
        final String fn = file.getNodeDelegate().getDisplayName();
        final String message = NbBundle.getMessage(StundentafelImportSettings.class, "StundentafelImportSettings.notUpdated.message", fn);
        NotificationDisplayer.getDefault()
                .notify(title, ic, message, null, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING);
    }

    @NbBundle.Messages({
        "StundentafelImportSettings.noWriteLock.message=Die Datei {0} ist gesperrt und kann nicht aktualisiert werden."})
    void loadAssociations() {
        final WriteLockCapability wlc = getDataObject().getLookup().lookup(WriteLockCapability.class);
        final WriteLockCapability.WriteLock l = wlc.writeLock();
        if (l == null) {
            final String fn = file.getNodeDelegate().getDisplayName();
            final String message = NbBundle.getMessage(StundentafelImportSettings.class, "StundentafelImportSettings.noWriteLock.message", fn);
            putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, message);
        } else {
            currentLock = l;
        }
        final CurriculumAssoziationenCollection ret;
        try {
            ret = NbUtilities.waitForLookup(getDataObject().getLookup(), CurriculumAssoziationenCollection.class, 5000);
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(GeneralStundentafelImportSettings.class).warning(ex.getLocalizedMessage());
            return;
        }
        putProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS, ret);//initializes
    }

    void releaseLock() {
        if (currentLock != null) {
            currentLock.releaseLock();
            currentLock = null;
        }
    }

    void saveAssociations() {
        if (currentLock == null) {
            notifyNoWriteLock();
        }
        putProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS, null);//uninitializes, removes templates
        try {
            getDataObject().setModified(true);
            final SaveCookie sc = getDataObject().getLookup().lookup(SaveCookie.class);
            if (sc != null) {
                sc.save();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            getDataObject().setModified(false);
            releaseLock();
        }
    }

    public DataObject getDataObject() {
        return file;
    }

    WizardDescriptor.Iterator<StundentafelImportSettings> createIterator() {
        return new ContextStundentafelImportSettings.XmlDataImportActionWizardIterator();
    }

}
