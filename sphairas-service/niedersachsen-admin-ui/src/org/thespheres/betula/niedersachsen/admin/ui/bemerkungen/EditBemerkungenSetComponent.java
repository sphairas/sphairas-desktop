/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.thespheres.betula.ui.swingx.treetable.NbSwingXTreeTableElement;

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
    private EditBemerkungenEnv env;
//    private final InstanceContent ic = new InstanceContent();
//    private final Lookup lookup = new AbstractLookup(ic);

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public EditBemerkungenSetComponent() {
        final Action save = new SaveAction();
        toolbar.add(save);
        setDropTarget(true);
//        this.treeTable.setRootVisible(true);
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
        setName(env.getNodeDelegate().getDisplayName());
        associateLookup(env.getNodeDelegate().getLookup());
//        ic.add(env);
//        this.treeTable.expandAll();
//        this.treeTable.setRootVisible(false);
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        WindowManager.getDefault().findTopComponent("EditBemerkungenPaletteTopComponent").open();
        ReportContextListener.getDefault().addChangeListener(children);
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        WindowManager.getDefault().findTopComponent("EditBemerkungenPaletteTopComponent").close();
        ReportContextListener.getDefault().removeChangeListener(children);
    }

//    @Override
//    public void componentActivated() {
//        super.componentActivated();
//        final TopComponent tc = (EditBemerkungenPaletteTopComponent) WindowManager.getDefault().findTopComponent("EditBemerkungenPaletteTopComponent");
//        if (tc instanceof EditBemerkungenPaletteTopComponent) {
//            ((EditBemerkungenPaletteTopComponent) tc).initialize();
//        }
//    }

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
