/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.Mutex;

/**
 *
 * @author boris.heithecker
 */
class HiddenColumnAdapter implements PropertyChangeListener {

    private final TableColumnExt column;
    private final static Set<String> PROPERTIES_HIDING = new HashSet<>();

    @SuppressWarnings("LeakingThisInConstructor")
    HiddenColumnAdapter(TableColumnExt column) {
        this.column = column;
        this.column.addPropertyChangeListener(this);
    }

    static void registerHidingProperty(String propName) {
        if (StringUtils.isBlank(propName)) {
            return;
        }
        synchronized (PROPERTIES_HIDING) {
            PROPERTIES_HIDING.add(propName);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (StringUtils.isBlank(name)) {
            return;
        }
        final boolean respect;
        synchronized (PROPERTIES_HIDING) {
            respect = PROPERTIES_HIDING.contains(name);
        }
        if (respect) {
            updateVisibility();
        }
    }

    private void updateVisibility() {
        final Set<String> props;
        synchronized (PROPERTIES_HIDING) {
            props = new HashSet<>(PROPERTIES_HIDING);
        }
        final boolean visible = !props.stream()
                .map(p -> column.getClientProperty(p))
                .filter(Boolean.class::isInstance)
                .anyMatch(p -> (Boolean) p);
        Mutex.EVENT.writeAccess(() -> column.setVisible(visible));
    }

}
