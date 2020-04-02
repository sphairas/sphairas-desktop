/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import java.awt.Component;
import java.awt.Cursor;
import org.thespheres.betula.xmlimport.utilities.SourceTargetLinksAccess;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Objects;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.WizardDescriptor;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.sibank.SiBankAssoziation;
import org.thespheres.betula.sibank.SiBankAssoziationenCollection;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.ui.util.JAXBUtil;

/**
 *
 * @author boris.heithecker
 */
public class SiBankSourceTargetAccess extends SourceTargetLinksAccess<SiBankAssoziationenCollection, SiBankImportTarget> implements PropertyChangeListener {

    private final SiBankImportData wizard;
    private final RequestProcessor.Task[] LOAD_TASK = new RequestProcessor.Task[]{null};
    private SiBankImportTarget lastCfg;
    private final WizardDescriptor.Iterator<?> iterator;

    public SiBankSourceTargetAccess(final SiBankImportData wiz, final WizardDescriptor.Iterator<?> iterator) {
        super(SiBankAssoziationenCollection.class, "SiBankPlus-Assoziationen");
        this.iterator = iterator;
        wizard = wiz;
        init();
    }

    @Override
    protected JAXBContext getJAXB(SiBankImportTarget config) throws IOException {
        if (ctx2 == null) {
            final Class[] tl = JAXBUtil.lookupJAXBTypes("SiBankSourceTargetAccess", SiBankAssoziationenCollection.class, SiBankAssoziation.class); //, config.getImportierterKursImplementationClass());
            try {
                ctx2 = JAXBContext.newInstance(tl);
            } catch (JAXBException ex) {
                throw new IOException(ex);
            }
        }
        return ctx2;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AbstractFileImportAction.IMPORT_TARGET)) {
            init();
        }
    }

    private void init() {
        synchronized (LOAD_TASK) {
            final SiBankImportTarget cfg = (SiBankImportTarget) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
            if (cfg != null && !Objects.equals(cfg, lastCfg)) {
                lastCfg = cfg;
                if (LOAD_TASK[0] != null) {
                    LOAD_TASK[0].cancel();
                }
                LOAD_TASK[0] = RP.post(() -> run(cfg));
            }
        }
    }

    void waitLoadingFinished() {
        synchronized (LOAD_TASK) {
            if (LOAD_TASK[0] != null) {
                final Component cmp = iterator.current() != null ? iterator.current().getComponent() : null;
                if (SwingUtilities.isEventDispatchThread() && cmp != null) {
                    final Cursor c = cmp.getCursor();
                    cmp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    LOAD_TASK[0].waitFinished();
                    cmp.setCursor(c);
                } else {
                    LOAD_TASK[0].waitFinished();
                }
            }
        }
    }

    @Override
    protected SiBankAssoziationenCollection loadSourceTargetLinks(SiBankImportTarget config) throws IOException {
        SiBankAssoziationenCollection res = super.loadSourceTargetLinks(config);
        if (res != null) {
            config.checkAssoziationen(res);
        }
        wizard.putProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS, res);
        return res;
    }

    @Override
    protected boolean overrideSourceTargetLinks(SiBankAssoziationenCollection assoziationen, SiBankImportTarget config) {
        return !assoziationen.getItems().isEmpty();
    }

}
