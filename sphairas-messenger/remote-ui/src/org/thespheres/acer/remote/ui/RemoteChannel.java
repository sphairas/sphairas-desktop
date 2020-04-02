/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui;

import com.google.common.eventbus.Subscribe;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import org.openide.awt.Actions;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Mutex;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import org.thespheres.acer.remote.ui.remoteunits.CreateStudentsChannelAction;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.util.Util;

/**
 *
 * @author boris.heithecker
 */
public class RemoteChannel extends ChildFactory<AbstractMessage> {

    private ChannelNode node;
    private final RemoteMessagesModel model;
    private final String channel;
    private final String type;
    private List<AbstractMessage> list;
    private boolean invalidated;
    private final String displayName;

    @SuppressWarnings("LeakingThisInConstructor")
    RemoteChannel(String name, String type, String display, RemoteMessagesModel model) {
        this.model = model;
        this.displayName = display;
        this.channel = name;
        this.type = type;
        model.registerEventListener(this);
    }

    public Node getNodeDelegate() {
        if (node == null) {
            node = new ChannelNode();
        }
        return node;
    }

    public String getDisplayName() {
        return displayName;
    }

    public RemoteMessagesModel getRemoteMessagesModel() {
        return model;
    }

    private List<AbstractMessage> list() {
        synchronized (this) {
            if (list == null || invalidated) {
                list = model.getRemoteMessagesSorted(channel);
                invalidated = false;
            }
        }
        return list;
    }

    private void invalidate() {
        synchronized (this) {
            invalidated = true;
        }
    }

    @Override
    protected boolean createKeys(List<AbstractMessage> toPopulate) {
        toPopulate.addAll(list());
        return true;
    }

    @Override
    protected Node createNodeForKey(AbstractMessage rm) {
        return rm.getNodeDelegate();
    }

    public String getChannelName() {
        return channel;
    }

    public String getChannelType() {
        return type;
    }

    public AbstractMessage createDraftMessage() {
        DraftMessage ret = new DraftMessage(this);
        model.addDraftMessage(channel, ret);
        return ret;
    }

    public void remove() {
        model.removeChannel(this);
    }

    @Subscribe
    void onModelChange(final ChangeEvent evt) {
        refresh(false);
    }

    @Subscribe
    public void propertyChange(final PropertyChangeEvent evt) {
        if (null != evt.getPropertyName()) {
            switch (evt.getPropertyName()) {
                case RemoteMessagesModel.PROP_MESSAGE:
                    AbstractMessage published = (AbstractMessage) evt.getNewValue();
                    AbstractMessage deleted = (AbstractMessage) evt.getOldValue();
                    if (published != null && published.getChannelName().equals(getChannelName())) {
                        Mutex.EVENT.writeAccess(() -> fireNodeInserted(published));
                    } else if (deleted != null) {
                        Mutex.EVENT.writeAccess(() -> fireNodeRemoved(deleted));
                    }
                    break;
            }
        }
    }

    private void fireNodeRemoved(AbstractMessage deleted) {
        int index = list().indexOf(deleted);
        invalidate();
        refresh(true);
        //Do not delete:: NbSwingXTreeTableElement
//        TreeModelSupport tms = model.getTreeModelSupport();
//        if (index != -1 && tms != null) {
//            Object m = Visualizer.findVisualizer(deleted.getNodeDelegate());
//            Object c = Visualizer.findVisualizer(getNodeDelegate());
//            if (m != null && c != null) {
//                TreePath tp = new TreePath(new Object[]{model.getRootNode(), c});
//                tms.fireChildRemoved(tp, index, m);
//            }
//        }
    }

    private void fireNodeInserted(AbstractMessage published) {
        invalidate();
        refresh(true);
        int index = list().indexOf(published);
        //Do not delete:: NbSwingXTreeTableElement
//        TreeModelSupport tms = model.getTreeModelSupport();
//        if (index != -1 && tms != null) {
//            Object m = Visualizer.findVisualizer(published.getNodeDelegate());
//            Object c = Visualizer.findVisualizer(getNodeDelegate());
//            if (m != null && c != null) {
//                TreePath tp = new TreePath(new Object[]{model.getRootNode(), c});
//                tms.fireChildAdded(tp, index, m);
//                model.expandPath(tp);
//            }
//        }
    }

    private class ChannelNode extends AbstractNode {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private ChannelNode() {
            super(Children.create(RemoteChannel.this, true), Lookups.singleton(RemoteChannel.this));
            setName(RemoteChannel.this.getChannelName());
            setDisplayName(RemoteChannel.this.getDisplayName());
            setIconBaseWithExtension("org/thespheres/acer/remote/ui/resources/category.png");
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                Actions.forID("Edit", "org.openide.actions.PasteAction"),
                Actions.forID("Betula", "org.thespheres.acer.remote.ui.action.PublishMessageAction"),
                Actions.forID("Betula", "org.thespheres.acer.remote.ui.action.RemoveChannelAction")};
        }//

        @Override
        protected void createPasteTypes(Transferable t, List<PasteType> s) {
            super.createPasteTypes(t, s); //To change body of generated methods, choose Tools | Templates.
            final Node n = NodeTransfer.node(t, NodeTransfer.CLIPBOARD_COPY);
            if (n != null) {
                PasteType pt = createStudentPasteType(n);
                s.add(pt);
            }
        }

        @Override
        public PasteType getDropType(final Transferable t, int action, int index) {
            final Node n = NodeTransfer.node(t, DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
            if (n != null) {
                return createStudentPasteType(n);
            }
            return null;
        }

        private PasteType createStudentPasteType(Node n) {
            final RemoteStudent rs = n.getLookup().lookup(RemoteStudent.class);
            if (getChannelType().equals("org.thespheres.acer.entities.messages.StudentsChannel") && rs != null && !this.equals(n.getParentNode())) {
                return new PasteType() {
                    @Override
                    public Transferable paste() throws IOException {
                        StudentId[] stud = new StudentId[]{rs.getStudentId()};
                        Util.RP(RemoteChannel.this.model.getProviderInfo().getURL()).post(() -> {
                            CreateStudentsChannelAction.updateStudentsChannel(RemoteChannel.this.model.getProviderInfo(), stud, RemoteChannel.this.channel, null);
                        });
//                        if ((action & DnDConstants.ACTION_MOVE) != 0) {
//                            dropNode.getParentNode().getChildren().remove(new Node[]{dropNode});
//                        }
                        return null;
                    }
                };
            }
            return null;
        }

    }
}
