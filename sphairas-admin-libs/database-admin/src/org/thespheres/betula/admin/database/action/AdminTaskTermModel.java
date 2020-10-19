/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import java.util.Optional;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.util.Lookup;
import org.thespheres.betula.TermId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermNotFoundException;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <S>
 */
public class AdminTaskTermModel extends DefaultComboBoxModel<Term> implements StringValue {

    public void initializeModel(final ProviderInfo config, final Term set) {
        final TermSchedule currentTermSchedule;
        if (config != null) {
            final LocalProperties props = LocalProperties.find(config.getURL());
            currentTermSchedule = Optional.of(props)
                    .map(lp -> lp.getProperty("termSchedule.providerURL"))
                    .map(tsprop -> Lookup.getDefault().lookupAll(SchemeProvider.class).stream()
                    .filter(p -> p.getInfo().getURL().equals(tsprop))
                    .collect(CollectionUtil.singleOrNull()))
                    .map(p -> p.getScheme(TermSchedule.DEFAULT_SCHEME, TermSchedule.class))
                    .orElse(null);
        } else {
            currentTermSchedule = null;
        }
        this.removeAllElements();
        this.addElement(null);
        if (currentTermSchedule != null) {
            final Term ct = currentTermSchedule.getCurrentTerm();
            for (int i = -10; i < 3; i++) {
                if (i == 0) {
                    this.addElement(ct);
                } else {
                    final TermId otid = new TermId(ct.getScheduledItemId().getAuthority(), ct.getScheduledItemId().getId() + i);
                    try {
                        final Term oterm = currentTermSchedule.resolve(otid);
                        this.addElement(oterm);
                    } catch (TermNotFoundException | IllegalAuthorityException ex) {
                        PlatformUtil.getCodeNameBaseLogger(DeleteTaskVisualPanel.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    }
                }
            }
            if (set == null) {
                setSelectedItem(ct);
            } else {
                setSelectedItem(set);
            }
        }

    }

    @Override
    public String getString(final Object o) {
        return o instanceof Term ? ((Term) o).getDisplayName() : " ";
    }

}
