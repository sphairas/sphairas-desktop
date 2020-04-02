/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx.treetable;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.TreePath;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.Mutex;

/**
 *
 * @author boris.heithecker
 */
class NbSwingXTreeTableRestore implements Runnable {
    
    List<Node.Handle[]> expanded;
    private final NbSwingXTreeTableElement restore;

    NbSwingXTreeTableRestore(final NbSwingXTreeTableElement restore) {
        this.restore = restore;
    }

    void restoreView() {
        Mutex.EVENT.writeAccess(this);
    }

    @Override
    public void run() {
        if (expanded != null) {
            expanded.stream().forEach((org.openide.nodes.Node.Handle[] arr) -> {
                try {
                    int s2 = arr.length;
                    if (s2 > 0) {
                        Node.Handle h0 = arr[0];
                        Object rt = Visualizer.findVisualizer(h0.getNode());
                        TreePath tp = new TreePath(rt);
                        for (int i = 1; i < s2; i++) {
                            Node.Handle nh = (Node.Handle) arr[i];
                            Object bn = Visualizer.findVisualizer(nh.getNode());
                            tp = tp.pathByAddingChild(bn);
                        }
                        restore.treeTable.expandPath(tp);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(NbSwingXTreeTableElement.class.getName()).log(Level.WARNING, "Could not restore TreePath", ex.getLocalizedMessage());
                }
            });
        }
    }
    
}
