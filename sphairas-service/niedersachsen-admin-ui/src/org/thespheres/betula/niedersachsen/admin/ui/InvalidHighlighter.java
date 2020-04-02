/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.util.Set;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;

/**
 *
 * @author boris.heithecker
 */
class InvalidHighlighter extends ColorHighlighter implements HighlightPredicate {

    private final String property;
    private final AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model;
    private boolean init;
    private RemoteReportsModel2 history;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    InvalidHighlighter(AbstractPluggableTableModel<RemoteReportsModel2, ReportData2, ?, ?> model, String prop) {
        super(Color.PINK, null, Color.MAGENTA, null);
        this.property = prop;
        this.model = model;
        setHighlightPredicate(this);
    }

    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        if (!init && model.getItemsModel() != null) {
            history = model.getItemsModel();
            history.getEventBus().register(this);
            init = true;
        }
        final int r = adapter.convertRowIndexToModel(adapter.row);
        if (history != null && model.getRows().size() > r) {
            final ReportData2 rd = model.getRows().get(r);
            final Set<String> pi = rd.propsInvalid;
            boolean ret;
            synchronized (pi) {
                ret = pi.contains(property);
            }
            return ret;
        }
        return false;
    }

    @Subscribe
    public void onPropertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(property)) {
            EventQueue.invokeLater(() -> fireStateChanged());
        }
    }
}
