/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ticketui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.Action;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.util.Utilities;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.util.CollectionUtil;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.thespheres.betula.admin.units.ticketui//Tickets2//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "TicketsComponent2",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.thespheres.betula.admin.units.ticketui.TicketsComponent2")
@ActionReference(path = "Menu/Window/betula-beans-services-windows", position = 2001)
@TopComponent.OpenActionRegistration(
        displayName = "#TicketsComponent2.openAction.displayName",
        preferredID = "TicketsComponent2")
@Messages({
    "TicketsComponent2.openAction.displayName=Berechtigungsfenster",
    "TicketsComponent2.displayName=Berechtigungen",
    "TicketsComponent2.hint=Das ist das Berechtigungs-Fenster."})
public final class TicketsComponent2 extends TopComponent implements LookupListener, PropertyChangeListener {

    private final TicketsComponentTableModel2 model;
    private final Lookup.Result<AbstractUnitOpenSupport> lkpResult;
    private RemoteTicketModel2 remoteModel;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public TicketsComponent2() {
        this.model = TicketsComponentTableModel2.create();
        initComponents();
        table.setColumnFactory(model.createColumnFactory());
        table.setModel(model);
        Action createTicketAction = Actions.forID("Betula", "org.thespheres.betula.admin.units.ticketui.AddUnitTicketAction");
        Actions.connect(newTicketButton, createTicketAction);
        setName(NbBundle.getMessage(TicketsComponent2.class, "TicketsComponent2.displayName"));
        setToolTipText(NbBundle.getMessage(TicketsComponent2.class, "TicketsComponent2.hint"));
        lkpResult = Utilities.actionsGlobalContext().lookupResult(AbstractUnitOpenSupport.class);
    }

    @Override
    public void componentOpened() {
        lkpResult.addLookupListener(this);
        TopComponent.getRegistry().addPropertyChangeListener(this);
        resultChanged(null);
    }

    @Override
    public void componentClosed() {
        lkpResult.removeLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        updateModel();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case TopComponent.Registry.PROP_TC_CLOSED:
                updateModel();
                break;
        }
    }

    private void updateModel() {
        final AbstractUnitOpenSupport selection = lkpResult.allInstances().stream()
                .map(AbstractUnitOpenSupport.class::cast)
                .collect(CollectionUtil.singleOrNull());
        try {
            final String url = selection != null ? selection.findWebServiceProvider().getInfo().getURL() : null;
            final RemoteTicketModel2 m2 = url != null ? RemoteTicketModel2.get(url) : null;
            if (m2 != null) { // && (remoteModel == null || !remoteModel.getProvider().equals(url))) {
                if (remoteModel == null || !remoteModel.getProvider().equals(url)) {
                    if (remoteModel != null) {
                        remoteModel.removeChangeListener(model);
                    }
                    remoteModel = m2;
                    if (remoteModel != null) {
                        remoteModel.addChangeListener(model);
                    }
                }
                model.initialize(remoteModel, selection.getLookup());
            }
        } catch (IOException ex) {
            PlatformUtil.getCodeNameBaseLogger(TicketsComponent2.class).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        table = new org.jdesktop.swingx.JXTable();
        toolbar = new javax.swing.JToolBar();
        newTicketButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        table.setColumnFactory(model.createColumnFactory());
        table.setHorizontalScrollEnabled(true);
        scrollPane.setViewportView(table);

        add(scrollPane, java.awt.BorderLayout.CENTER);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        newTicketButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/thespheres/betula/admin/units/resources/calendar--plus.png"))); // NOI18N
        toolbar.add(newTicketButton);

        add(toolbar, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton newTicketButton;
    private javax.swing.JScrollPane scrollPane;
    private org.jdesktop.swingx.JXTable table;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
