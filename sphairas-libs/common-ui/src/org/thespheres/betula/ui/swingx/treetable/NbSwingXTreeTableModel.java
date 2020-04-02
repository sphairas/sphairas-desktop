/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx.treetable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.tree.TreeModelSupport;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.openide.explorer.view.NodeTreeModel;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 *
 * @author boris.heithecker
 */
public abstract class NbSwingXTreeTableModel extends AbstractTreeTableModel implements PropertyChangeListener {

//    private final NListener listener = new NListener();
    private final TreeListener treeListener = new TreeListener();
//    private final RemoteMessagesModel remote;
    private final NodeTreeModel nodeTreeModel;
    
    @SuppressWarnings("LeakingThisInConstructor")
    protected NbSwingXTreeTableModel(final Node root) { //(remoteModel)
        super(Visualizer.findVisualizer(root)); //new Root(remoteModel)
//        remote = remoteModel;
//remote = null;
//        remote.addPropertyChangeListener(this);
//        addListener(root);
        nodeTreeModel = new NodeTreeModel(root);
        nodeTreeModel.addTreeModelListener(treeListener);
    }

//    private void addListener(Node n) {
////        final NodeListener wl = WeakListeners.create(NodeListener.class, listener, n);
////        n.addNodeListener(wl);
////        for (final Node c : n.getChildren().getNodes()) {
////            addListener(c);
////        }
//    }
    public Node getRootNode() {
        return Visualizer.findNode(root);
    }
    
    public abstract ColumnFactory createColumnFactory();
    
    public TreeNode[] getPathToRoot(Node n) {
        return getPathToRoot(n, 0);
    }
    
    TreePath getTreePath(Node node) {
        return new TreePath(getPathToRoot(node));
    }
    
    protected TreeNode[] getPathToRoot(Node n, int depth) {
        TreeNode[] retNodes;
        if (n == null) {
            if (depth == 0) {
                return null;
            } else {
                retNodes = new TreeNode[depth];
            }
        } else {
            depth++;
            if (n == root) {
                retNodes = new TreeNode[depth];
            } else {
                retNodes = getPathToRoot(n.getParentNode(), depth);
            }
            retNodes[retNodes.length - depth] = Visualizer.findVisualizer(n);
        }
        return retNodes;
    }
    
    @Override
    public Object getChild(Object parent, int index) {
        final TreeNode tn = (TreeNode) parent;
        return tn.getChildAt(index);
//        return Children.MUTEX.readAccess(() -> {
//            final Node n = Visualizer.findNode(parent);
//            final Node ch = n.getChildren().getNodeAt(index);
//            return Visualizer.findVisualizer(ch);
//        });
    }
    
    @Override
    public int getChildCount(Object parent) {
        final TreeNode tn = (TreeNode) parent;
        return tn.getChildCount();
//        return Children.MUTEX.readAccess(() -> {
//            final Node n = Visualizer.findNode(parent);
//            return n.getChildren().getNodesCount();
//        });
    }
    
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        int i = -1;
        if (parent != null && child != null) {
            final TreeNode pn = (TreeNode) parent;
            final TreeNode cn = (TreeNode) child;
            i = pn.getIndex(cn);
//            final Node n = Visualizer.findNode(child);
//            while (++i < pn.getChildCount()) {
//                final Node c = Visualizer.findNode(pn.getChildAt(i));
//                if (System.identityHashCode(c) == System.identityHashCode(n)) {
//                    return i;
//                }
//            }

        }
        if (i == -1) {
            System.out.println("lhsdf");
        }
        return i;
//        return Children.MUTEX.readAccess(() -> {
//            int i = -1;
//            if (parent != null && child != null) {
//                Node n = Visualizer.findNode(parent);
//                Node c = Visualizer.findNode(child);
//                Children ch = n.getChildren();
//                while (++i < ch.getNodesCount()) {
//                    if (ch.getNodeAt(i).getName().equals(c.getName())) {
//                        break;
//                    }
//                }
//            }
//            return i;
//        });
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        if (RemoteMessagesModel.PROP_INIT.equals(evt.getPropertyName())) {
//            Mutex.EVENT.writeAccess(() -> modelSupport.fireTreeStructureChanged(new TreePath(root)));
//        }
    }
    
    TreeModelSupport getTreeModelSupport() {
        return modelSupport;
    }

//    private class NListener implements NodeListener {
//        
//        @Override
//        public void childrenAdded(NodeMemberEvent ev) {
//            handleAddRemove(ev, true);
//        }
//        
//        @Override
//        public void childrenRemoved(NodeMemberEvent ev) {
//            handleAddRemove(ev, false);
//        }
//        
//        private void handleAddRemove(final NodeMemberEvent ev, final boolean add) {
//            final int size = ev.getDelta().length;
//            assert size == ev.getDeltaIndices().length;
//            for (int i = 0; i < size; i++) {
//                final Node n = ev.getDelta()[i];
//                if (add) {
//                    addListener(n);
//                }
//                final int index = ev.getDeltaIndices()[i];
//                final TreePath tp = getTreePath(n.getParentNode());
//                final TreeNode v = Visualizer.findVisualizer(n);
////                try {
////                    Thread.sleep(500);
////                } catch (InterruptedException ex) {
////                    Exceptions.printStackTrace(ex);
////                }
//                EventQueue.invokeLater(() -> {
//                    if (add) {
//                        getTreeModelSupport().fireChildAdded(tp, index, v);
//                    } else {
//                        getTreeModelSupport().fireChildRemoved(tp, index, v);
//                    }
//                });
//            }
//        }
//        
//        @Override
//        public void childrenReordered(NodeReorderEvent ev) {
//            final int size = ev.getPermutationSize();
//            class P {
//                
//                final TreeNode child;
//                final int newIndex;
//                
//                P(TreeNode child, int newIndex) {
//                    this.child = child;
//                    this.newIndex = newIndex;
//                }
//                
//            }
//            final TreeNode tpn = Visualizer.findVisualizer(ev.getNode());
//            final List<P> diff = new ArrayList<>();
//            for (int i = 0; i < size; i++) {
//                if (i != ev.newIndexOf(i)) {
//                    final TreeNode tcn = tpn.getChildAt(i);
//                    diff.add(new P(tcn, i));
//                }
//            }
//            final TreeNode[] children = diff.stream()
//                    .sorted(Comparator.comparingInt(p -> p.newIndex))
//                    .map(p -> p.child)
//                    .toArray(TreeNode[]::new);
//            final int[] indices = diff.stream()
//                    .sorted(Comparator.comparingInt(p -> p.newIndex))
//                    .mapToInt(p -> p.newIndex)
//                    .toArray();
//            final TreePath tp = getTreePath(ev.getNode());
//            Mutex.EVENT.writeAccess(() -> {
//                getTreeModelSupport().fireChildrenChanged(tp, indices, children);
//            });
//        }
//        
//        @Override
//        public void nodeDestroyed(NodeEvent ev) {
////            throw new UnsupportedOperationException();
//        }
//        
//        @Override
//        public void propertyChange(PropertyChangeEvent evt) {
//        }
//        
//    }
    private class TreeListener implements TreeModelListener {
        
        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            getTreeModelSupport().fireChildrenChanged(e.getTreePath(), e.getChildIndices(), e.getChildren());
        }
        
        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            getTreeModelSupport().fireChildrenAdded(e.getTreePath(), e.getChildIndices(), e.getChildren());
        }
        
        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            getTreeModelSupport().fireChildrenRemoved(e.getTreePath(), e.getChildIndices(), e.getChildren());
        }
        
        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            getTreeModelSupport().fireTreeStructureChanged(e.getTreePath());
        }
        
    }
    
//    private static class Root extends AbstractNode {
//        
//        private final RootHandle handle;
//        
//        @SuppressWarnings({"OverridableMethodCallInConstructor"})
//        private Root() {   //(RemoteMessagesModel remote)
//            super(Children.LEAF);
////            super(Children.create(new ChannelChildren(remote), true));
////            ProviderInfo pi = remote.getProviderInfo();
////            setName("root-" + pi.getURL());
//            this.handle = new RootHandle(null); //pi);
//        }
//        
//        @Override
//        public Node.Handle getHandle() {
//            return handle;
//        }
//        
//        static class RootHandle implements Node.Handle {
//            
//            private static final long serialVersionUID = 1L;
//            private final ProviderInfo provider;
//            
//            public RootHandle(ProviderInfo provider) {
//                this.provider = provider;
//            }
//            
//            @Override
//            public Node getNode() throws IOException {
////                RemoteMessagesModel rm = RemoteMessagesModel.find(provider);
//                return Visualizer.findNode(null); //rm.getRootNode());
//            }
//            
//        }
//    }
    
}
