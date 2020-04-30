/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import org.openide.WizardDescriptor;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.utilities.AbstractUpdater;

/**
 *
 * @author boris.heithecker
 * @param <Data>
 * @param <T>
 * @param <C>
 * @param <I>
 */
public abstract class AbstractFileImportAction<Data extends AbstractImportWizardSettings, T, C extends ImportTarget, I extends ImportItem> extends AbstractImportAction<Data, C, I> {

    public static final String DATA = "data";

    private T xml;

    protected AbstractFileImportAction(final String dialogTitle) {
        super(dialogTitle);
    }

    protected abstract T openFile() throws IOException;

    @Override
    protected final Data createSettingsAndIterator() {
        throw new UnsupportedOperationException("Never called.");
    }

    protected abstract WizardDescriptor.Iterator<Data> createIterator(T xml, Data settings);

    protected abstract Data createSettings(T xml);

    @Override
    public void actionPerformed(ActionEvent e) {
        initialize();
        initFold();
        try {
            xml = openFile();
        } catch (IOException ex) {
            Logger.getLogger(AbstractFileImportAction.class.getCanonicalName()).warning(ex.getLocalizedMessage());
            return;
        }
        if (xml == null) {
            return;
        }
        final Data d = createSettings(xml);
        if (d == null) {
            return;
        }
        if ((iterator = createIterator(xml, d)) == null) {
            return;
        }
        d.putProperty(AbstractFileImportAction.DATA, xml);
        showWizard(d);
    }

    @Override
    protected final void onWizardFinishOK(C config, Set<?> selected, Data wiz) {
        onWizardFinishOK(config, selected, xml, wiz);
    }

    protected void onWizardFinishOK(C config, Set<?> selected, T xml, Data wiz) {
        super.onWizardFinishOK(config, selected, wiz);
    }

    @Override
    protected final void onUpdateFinished(C config, Set<?> selected, Data wiz, AbstractUpdater<?> updater) {
        onUpdateFinished(config, selected, xml, wiz, updater);
    }

    protected void onUpdateFinished(C config, Set<?> selected, T xml, Data wiz, AbstractUpdater<?> updater) {
    }

}
