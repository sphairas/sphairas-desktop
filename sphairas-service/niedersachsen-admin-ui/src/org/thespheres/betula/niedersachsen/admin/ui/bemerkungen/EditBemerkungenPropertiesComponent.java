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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.thespheres.betula.ui.swingx.AbstractTableElement;
import org.thespheres.betula.ui.swingx.treetable.NbSwingXTreeTableElement;
import org.thespheres.betula.ui.util.PlatformUtil;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
@MultiViewElement.Registration(mimeType = "application/nds-report-notes", persistenceType = TopComponent.PERSISTENCE_ALWAYS, displayName = "Texte", preferredID = "EditBemerkungenPropertiesComponentTexts", position = 5000)
public class EditBemerkungenPropertiesComponent extends AbstractTableElement implements Serializable {

    private final javax.swing.JScrollPane scrollPane;
    private EditBemerkungenEnv env;
    private final EditBemerkungenPropertiesComponentTableModel model;
    private final ConfigurePropertyPanel configPanel;
    private final Listener listener = new Listener();
    private final DeleteDelegate deleteDelegate = new DeleteDelegate();

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public EditBemerkungenPropertiesComponent() {
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
        this.getActionMap().put("delete", deleteDelegate);
        configPanel.setActions();
        env.RP.post(() -> model.initialize(env.getProperties(), env));
        env.addPropertyChangeListener(listener);
        updateName();
    }

    @Override
    protected Node getNodeForRow(int rowIndex) {
        return model.nodeForRow(rowIndex);
    }

    @Override
    protected void activatedNodes(final List<Node> selected) {
        if (env != null) {
            selected.add(env.getNodeDelegate());
            deleteDelegate.updateEnabled(selected);
            setActivatedNodes(selected.toArray(new Node[selected.size()]));
        }
    }

    @Override
    protected void updateName() {
        final Node n = env.getNodeDelegate();
        if (callback != null && n != null) {
            Mutex.EVENT.writeAccess(() -> {
                final TopComponent tc = callback.getTopComponent();
                final String name = n.getDisplayName();
                final boolean modif = env != null && env.isModified();
                final boolean readOnly = false;
                tc.setDisplayName(UIUtilities.annotateName(name, false, modif, readOnly));
                tc.setHtmlDisplayName(UIUtilities.annotateName(name, true, modif, readOnly));
                tc.setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            });
        }
    }

    @Override
    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
//        final List<Action> al = (List<Action>) Utilities.actionsForPath("Loaders/application/betula-unit-context/Actions");
//        final Action[] a = al.toArray(new Action[al.size()]);
        final Action del = Actions.forID("Edit", "org.openide.actions.DeleteAction");
        final Action[] a = new Action[]{del};
        return Utilities.actionsToPopup(a, this);
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

    private class DeleteDelegate extends AbstractAction {

        private void updateEnabled(final List<Node> sel) {
            final boolean ena = sel.stream()
                    .anyMatch(Node::canDestroy);
            setEnabled(ena);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            selectionListener.currentSelection().stream()
                    .filter(Node::canDestroy)
                    .forEach(n -> {
                        try {
                            n.destroy();
                        } catch (IOException ex) {
                            PlatformUtil.getCodeNameBaseLogger(NbSwingXTreeTableElement.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                        }
                    });
        }

    }

    final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent pce) {
            updateName();
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
