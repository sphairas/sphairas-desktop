/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.config;

import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.util.Lookup;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.RemoteSignee;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.ui.util.AbstractListConfigPanel;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class HeadTeacherConfigurationPanel extends AbstractListConfigPanel<RemoteSignee, UnitId> implements StringValue {

    protected NamingResolver currentNamingResolver;
    protected Signees currentSignees;
    protected final String docIdName;
    protected final String remoteSigneeProperty;

    @SuppressWarnings({"LeakingThisInConstructor"})
    public HeadTeacherConfigurationPanel(final JXComboBox component, final String docIdName, final String property) {
        super(component);
        this.docIdName = docIdName;
        remoteSigneeProperty = property;
        final DefaultListRenderer r = new DefaultListRenderer(this);
        component.setRenderer(r);
    }

    @Subscribe
    public void onRemoteSigneePropertyChange(final PropertyChangeEvent evt) {
        if (evt.getSource() instanceof RemoteSignee && remoteSigneeProperty.equals(evt.getPropertyName())) {
            final Signee signee = ((RemoteSignee) evt.getSource()).getSignee();
            EventQueue.invokeLater(() -> updateSelectionIfCurrent(signee));
        }
    }

    private void updateSelectionIfCurrent(Signee sig) {
        if (current != null && current.getSignee().equals(sig)) {
            comboBox.removeActionListener(this);
            model.setSelectedItem(getCurrentValue());
            comboBox.addActionListener(this);
        }
    }

    private void updateItems(UnitId[] items) {
        if (items != null) {
            Arrays.stream(items)
                    .forEach(model::addElement);
        }
    }

    @Override
    protected UnitId getCurrentValue() {
        if (current != null) {
            return current.getClientProperty(remoteSigneeProperty, UnitId.class);
        }
        return null;
    }

    @Override
    protected void updateValue(final UnitId pu) {
        if (current != null) {
            final HeadTeachers kl = HeadTeachers.find(docIdName, current.getSignees());
            if (kl != null) {
                kl.post(current.getSignee(), pu);
            }
        }
    }

    @Override
    protected void onContextChange(final Lookup context) {
        final RemoteSignee rs = context.lookup(RemoteSignee.class);
        if (Objects.equals(rs, current)) {
            return;
        }
        if (current != null) {
            current.getEventBus().unregister(this);
        }
        current = rs;
        final Signees beforeSignees = currentSignees;
        currentSignees = current == null ? null : current.getSignees();
        if (current != null) {
            current.getEventBus().register(this);
        }
        if (!Objects.equals(currentSignees, beforeSignees)) {
//            if (beforeSignees != null) {
//                final Klassenlehrer kl = Klassenlehrer.find(beforeSignees);
//                kl.removeChangeListener(this);
//            }
            model.removeAllElements();
            model.addElement(null);
            final UnitId[] pus;
            if (currentSignees != null) {
                currentNamingResolver = NamingResolver.find(currentSignees.getProviderUrl());
                final HeadTeachers kl = HeadTeachers.find(docIdName, currentSignees);
                if (kl != null) {
                    try {
                        //                kl.addChangeListener(this);
                        pus = kl.getUnits();
                    } catch (IOException ex) {
                        currentSignees = null;
                        PlatformUtil.getCodeNameBaseLogger(HeadTeacherConfigurationPanel.class).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                        return;
                    }
                } else {
                    pus = null;
                }
            } else {
                currentNamingResolver = null;
                pus = null;
            }
            if (pus != null) {
                final NamingResolver nr = currentNamingResolver;
                final Collator collator = Collator.getInstance(Locale.getDefault());
                if (nr != null) {
                    Arrays.sort(pus, Comparator.comparing(u -> {
                        try {
                            return nr.resolveDisplayName(u);
                        } catch (IllegalAuthorityException ex) {
                            return u.getId();
                        }
                    }, collator));
                }
                updateItems(pus);
            }
        }
    }

    @Override
    public String getString(Object value) {
        if (value instanceof UnitId) {
            UnitId uid = (UnitId) value;
            if (currentNamingResolver != null) {
                try {
                    return currentNamingResolver.resolveDisplayName(uid);
                } catch (IllegalAuthorityException ex) {
                }
            }
            return uid.getId();
        }
        return "---";
    }

}
