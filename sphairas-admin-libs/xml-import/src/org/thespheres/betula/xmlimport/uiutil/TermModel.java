/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.WizardDescriptor;
import org.thespheres.betula.TermId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.xmlimport.ImportTarget;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <S>
 */
public class TermModel<I extends ImportTarget, S extends AbstractImportWizardSettings> extends DefaultComboBoxModel<Term> implements PropertyChangeListener, StringValue {

    protected final ImportProviderComboBoxModel<I> providerModel;
    protected I currentConfig;

    public TermModel(ImportProviderComboBoxModel<I> providerModel) {
        this.providerModel = providerModel;
    }

    public synchronized void init(S wiz) {
        I config = (I) wiz.getProperty(AbstractFileImportAction.IMPORT_TARGET);
        Term term = (Term) wiz.getProperty(AbstractFileImportAction.TERM);
        if (config == null) {
            config = providerModel.findTarget();
        }
        if (config != null) {
            initializeModel(config, term);
            currentConfig = config;
        }
    }

    public void initializeModel(I config, Term set) {
        TermSchedule ts = config.getTermSchemeProvider().getScheme(TermSchedule.DEFAULT_SCHEME, TermSchedule.class);
        Term ct = ts.getCurrentTerm();
        if (!Objects.equals(currentConfig, config)) {
            TermId ctid = ct.getScheduledItemId();
            int id = ct.getScheduledItemId().getId();
            for (int i = id - 4; i++ <= id + 4;) {
                Term add;
                if (i == 0) {
                    add = ct;
                } else {
                    TermId tid = new TermId(ctid.getAuthority(), i);
                    try {
                        add = ts.resolve(tid);
                    } catch (TermNotFoundException | IllegalAuthorityException ex) {
                        PlatformUtil.getCodeNameBaseLogger(TermModel.class).log(LogLevel.INFO_WARNING, ex.getMessage(), ex);
                        continue;
                    }
                }
                addElement(add);
            }
            currentConfig = config;
        }
        if (set == null) {
            set = ct;
        }
        setSelectedItem(set);
    }

    @Override
    public void setSelectedItem(Object anObject) {
        super.setSelectedItem(anObject);
    }

    @Override
    public String getString(Object o) {
        return o instanceof Term ? ((Term) o).getDisplayName() : " ";
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof WizardDescriptor && evt.getPropertyName().equals(AbstractFileImportAction.IMPORT_TARGET)) {
            S wiz = (S) evt.getSource();
            init(wiz);
        }
    }
}
