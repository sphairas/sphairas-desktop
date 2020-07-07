/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.ui.swingx.treetable.NbSwingXTreeTableElement;
import org.thespheres.betula.ui.util.UIUtilities;

/**
 *
 * @author boris.heithecker
 */
@MultiViewElement.Registration(mimeType = "application/nds-report-notes",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS,
        displayName = "Struktur",
        preferredID = "EditBemerkungenPropertiesComponentSet",
        position = 1000)
public class EditBemerkungenSetComponent extends NbSwingXTreeTableElement implements MultiViewElement, Serializable {

    private final EditBemerkungenSetModel children = new EditBemerkungenSetModel();
    private final Listener listener = new Listener();
    private EditBemerkungenEnv env;

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public EditBemerkungenSetComponent() {
        this.addNodeDelegateToActivatedNodes = true;
        final Action save = new SaveAction();
        toolbar.add(save);
        final Action add = new AddCategoryAction();
        toolbar.add(add);
        setDropTarget(true);
    }

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public EditBemerkungenSetComponent(Lookup context) throws IOException {
        this();
        env = context.lookup(EditBemerkungenEnv.class);
        if (env == null) {
            throw new IOException();
        }
        initializeComponent();
    }

    @Override
    protected Node getNodeDelegate() {
        return env.getNodeDelegate();
    }

    @Override
    protected void initializeComponent() throws IOException {
        super.initializeComponent();
        env.RP.post(() -> children.setEnv(env));
        setModel(children);
        env.addPropertyChangeListener(listener);
        setActivatedNodes(new Node[]{env.getNodeDelegate()});
        updateName(getNodeDelegate());
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        EventQueue.invokeLater(() -> WindowManager.getDefault().findTopComponent("EditBemerkungenPaletteTopComponent").open());
        ReportContextListener.getDefault().addChangeListener(children);
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        WindowManager.getDefault().findTopComponent("EditBemerkungenPaletteTopComponent").close();
        ReportContextListener.getDefault().removeChangeListener(children);
    }

    @Override
    protected void activatedNodes(final List<Node> sel) {
        //MoveUpAction, MoveDownAction accept only one activated node
        addNodeDelegateToActivatedNodes = sel == null || sel.isEmpty();
        super.activatedNodes(sel);
    }

    @Override
    protected void updateName(final Node n) {
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

    final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent pce) {
            updateName(getNodeDelegate());
        }

    }

    @Messages({"AddCategoryAction.newCategory.displayName=Neue Kategorie"})
    final class AddCategoryAction extends AbstractAction {

        public AddCategoryAction() {
            final Icon icon = ImageUtilities.loadImageIcon("org/thespheres/betula/niedersachsen/admin/ui/resources/folder--plus.png", true);
            putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final String cat = NbBundle.getMessage(AddCategoryAction.class, "AddCategoryAction.newCategory.displayName");
            children.addCategory(cat, true);
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
                env.saveTemplate();
            }
        }

    }

}
