/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.impl;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;

/**
 *
 * @author boris.heithecker
 */
class MessagesTreeView extends BeanTreeView {

    private final ExplorerManager manager;

    MessagesTreeView(ExplorerManager manager) {
        this.manager = manager;
    }

    public void scrollToNode(final Node n) {
        // has to be delayed to be sure that events for Visualizers
        // were processed and TreeNodes are already in hierarchy
        SwingUtilities.invokeLater(() -> {
            TreeNode tn = Visualizer.findVisualizer(n);
            if (tn == null) {
                return;
            }
            TreeModel model = tree.getModel();
            if (!(model instanceof DefaultTreeModel)) {
                return;
            }
            TreePath path = new TreePath(((DefaultTreeModel) model).getPathToRoot(tn));
            Rectangle r = tree.getPathBounds(path);
            if (r != null) {
                tree.scrollRectToVisible(r);
            }
        });
    }

    public List<String[]> getExpandedPaths() {

        final List<String[]> result = new ArrayList<>();

        TreeNode root = Visualizer.findVisualizer(manager.getRootContext());

        for (Enumeration expanded = tree.getExpandedDescendants(new TreePath(root)); expanded != null && expanded.hasMoreElements();) {
            final TreePath p = (TreePath) expanded.nextElement();
            final Node n = Visualizer.findNode(p.getLastPathComponent());
            final String[] path = NodeOp.createPath(n, manager.getRootContext());
            result.add(path);
        }

        return result;

    }

    public void expandNodes(final List<String[]> paths) {
        for (final String[] sp : paths) {
            Node n;
            try {
                n = NodeOp.findPath(manager.getRootContext(), sp);
            } catch (NodeNotFoundException e) {
                n = e.getClosestNode();
            }
            if (n == null) {
                continue;
            }
            final Node leafNode = n;
            EventQueue.invokeLater(() -> {
                TreeNode tns[] = new TreeNode[sp.length + 1];
                Node n1 = leafNode;
                for (int i = sp.length; i >= 0; i--) {
                    if (n1 == null) {
                        return;
                    }
                    tns[i] = Visualizer.findVisualizer(n1);
                    n1 = n1.getParentNode();
                }
                showPath(new TreePath(tns));
            });
        }
    }

}
