/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.JXTable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages(value = {"MultiMarkerColumnPanel.step.name=Auswahl"})
public class MultiMarkerColumnPanel extends JPanel {

    private final JScrollPane scrollPanel;
    private final JXTable table;
    final MultiMarkerColumnTableModel model;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public MultiMarkerColumnPanel(final Marker[] all, final Marker[] selected, final String displayContextName) {
        super();
        model = new MultiMarkerColumnTableModel(all, selected, displayContextName);
        scrollPanel = new JScrollPane();
        table = new JXTable();
        setLayout(new BorderLayout());
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);
        add(scrollPanel, BorderLayout.CENTER);
        table.setColumnFactory(model.createColumnFactory());
        table.setModel(model);
        model.initialize(null, Lookup.EMPTY);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MultiMarkerColumnPanel.class, "MultiMarkerColumnPanel.step.name");
    }

    public Marker[] getSelection() {
        return this.model.list.stream()
                .filter(s -> s.isSelected())
                .map(s -> s.getMarker())
                .toArray(Marker[]::new);
    }

}
