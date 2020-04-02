/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.thespheres.betula.ui.swingx.AbstractTableElement;

/**
 *
 * @author boris.heithecker
 */
@MultiViewElement.Registration(mimeType = "application/nds-report-notes", persistenceType = TopComponent.PERSISTENCE_ALWAYS, displayName = "Texte", preferredID = "EditBemerkungenPropertiesComponentTexts", position = 5000)
public class EditBemerkungenPropertiesComponent extends AbstractTableElement implements Serializable {

    private final javax.swing.JScrollPane scrollPane;
    private EditBemerkungenEnv env;
    private final EditBemerkungenPropertiesComponentTableModel model;
    private final InstanceContent ic = new InstanceContent();
    private final Lookup lookup = new AbstractLookup(ic);
    private final ConfigurePropertyPanel configPanel;

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public EditBemerkungenPropertiesComponent() {
        associateLookup(lookup);
        configPanel = new ConfigurePropertyPanel(this);
        scrollPane = new javax.swing.JScrollPane();
        model = new EditBemerkungenPropertiesComponentTableModel();
        final Action save = new SaveAction();
        toolbar.add(save);
        final Action add = new AddPropertyAction();
        toolbar.add(add);
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        table.setColumnFactory(new EditBemerkungenPropertiesComponentColumnFactory(this));
        table.setModel(model);
        add(configPanel, BorderLayout.SOUTH);
        scrollPane.setViewportView(table);
    }

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public EditBemerkungenPropertiesComponent(Lookup context) throws IOException {
        this();
        env = context.lookup(EditBemerkungenEnv.class);
        if (env == null) {
            throw new IOException();
        }
        initializeComponent();
    }

    private void initializeComponent() throws IOException {
        if (!EventQueue.isDispatchThread()) {
            throw new IOException("Not in EWT");
        }
        configPanel.setActions();
        ic.add(env);
        env.RP.post(() -> model.initialize(env.getProperties(), env));
        updateName();
    }

    @Override
    protected Node getNodeForRow(int rowIndex) {
        return model.nodeForRow(rowIndex);
    }

    @Override
    protected void activatedNodes(List<Node> selected) {
        if (env != null) {
            setActivatedNodes(selected.toArray(new Node[selected.size()]));
        }
    }

    @Override
    protected void updateName() {
        if (callback != null && env != null) {
            Mutex.EVENT.writeAccess(() -> {
                final TopComponent tc = callback.getTopComponent();
                final Node n = env.getNodeDelegate();
                tc.setDisplayName(n.getDisplayName());
                tc.setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            });
        }
    }

    @Override
    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal(oi);
        final String p = oi.readUTF();
        try {
            env = EditBemerkungenEnv.find(p);
            initializeComponent();
        } catch (IOException npex) {
            throw new IOException(npex);
        }
    }

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
        super.writeExternal(oo);
        if (env != null) {
            oo.writeUTF(env.getProvider());
        }
    }

    final class AddPropertyAction extends AbstractAction {

        public AddPropertyAction() {
            final Icon icon = ImageUtilities.loadImageIcon("org/thespheres/betula/niedersachsen/admin/ui/resources/application--plus.png", true);
            putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (env != null) {
                model.addProperty();
            }
        }

    }

    final class SaveAction extends AbstractAction {

        public SaveAction() {
            final Icon icon = ImageUtilities.loadImageIcon("org/thespheres/betula/niedersachsen/admin/ui/resources/disk.png", true);
            putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (env != null) {
                model.updateForSave();
                env.saveProperties().addTaskListener(t -> model.runAfterSave(t));
            }
        }

    }
}
