/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import java.awt.Component;
import java.awt.Cursor;
import org.thespheres.betula.xmlimport.utilities.SourceTargetLinksAccess;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.WizardDescriptor;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.gpuntis.UntisImportConfiguration;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;

/**
 *
 * @author boris.heithecker
 */
public class UntisSourceTargetAccess extends SourceTargetLinksAccess<UntisSourceTargetLinks, UntisImportConfiguration> implements PropertyChangeListener {

    private final UntisImportData wizard;
    private final RequestProcessor.Task[] LOAD_TASK = new RequestProcessor.Task[]{null};
    private final WizardDescriptor.Iterator<?> iterator;

    public UntisSourceTargetAccess(final UntisImportData wiz, final WizardDescriptor.Iterator<?> iterator) {
        super(UntisSourceTargetLinks.class, "Untis-Assoziationen");
        wizard = wiz;
        this.iterator = iterator;
        init();
    }

    @Override
    protected JAXBContext getJAXB(UntisImportConfiguration config) throws IOException {
        if (ctx2 == null) {
            final Class[] tl = JAXBUtil.lookupJAXBTypes("UntisSourceTargetAccess", UntisSourceTargetLinks.class, UntisTargetLink.class);
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

    private synchronized void init() {
        final UntisImportConfiguration cfg = (UntisImportConfiguration) wizard.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        if (cfg != null) {
            if (LOAD_TASK[0] != null) {
                LOAD_TASK[0].cancel();
            }
            LOAD_TASK[0] = RP.post(() -> run(cfg));
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
    protected UntisSourceTargetLinks loadSourceTargetLinks(UntisImportConfiguration config) throws IOException {
        UntisSourceTargetLinks res = super.loadSourceTargetLinks(config);
        wizard.putProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS, res);
        return res;
    }

    @Override
    protected boolean overrideSourceTargetLinks(UntisSourceTargetLinks assoziationen, UntisImportConfiguration config) {
        return !assoziationen.isEmpty();
    }
}
