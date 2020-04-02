/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.JXTable;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.thespheres.betula.admin.units.ui.TargetsForStudentsModel;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
public abstract class MoveStudentsToTargetDropSupport extends DropTargetAdapter {

    protected MoveStudentsToTargetDropSupport() {
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        RemoteStudent[] studs = extractStudents(dtde.getTransferable());
        if (studs.length != 0) {
            dtde.acceptDrag(NodeTransfer.DND_COPY);
        } else {
            dtde.rejectDrag();
        }
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        RemoteStudent[] studs = extractStudents(dtde.getTransferable());
        if (studs.length != 0) {
            Component cmp = dtde.getDropTargetContext().getComponent();
            if (cmp instanceof JXTable && ((JXTable) cmp).getModel() instanceof TargetsForStudentsModel) {
                JXTable tbl = (JXTable) cmp;
                TargetsForStudentsModel currentModel = (TargetsForStudentsModel) tbl.getModel();
                Point p = SwingUtilities.convertPoint(tbl, dtde.getLocation(), tbl);  //letzerer OutlineView.this
                int column = tbl.convertColumnIndexToModel(tbl.columnAtPoint(p));
                int row = tbl.convertRowIndexToModel(tbl.rowAtPoint(p));
                if (column > 0) { //isPopupAllowed()
                    RemoteTargetAssessmentDocument rtad = currentModel.getRemoteTargetAssessmentDocumentAtColumnIndex(column);
                    final Term currentTerm = currentModel.getCurrentIndentity();
//                    TermId term = currentTerm != null ? currentTerm.getScheduledItemId() : null;
                    String type = currentModel.getCurrentTargetType();
                    AbstractUnitOpenSupport support = currentModel.getSupport();
                    RemoteStudent rs = currentModel.getStudentAt(row);
                    //TODO: warning if studs is not contained in primary unit
                    TargetAssessmentSelectionProvider sp = new TargetAssessmentSelectionProvider(rtad, rs, support, currentTerm, type);
                    if (currentTerm != null) {
                        sp.ic.add(currentTerm);
                    }
                    if (rtad != null && currentTerm != null && type != null && support != null) {
                        dtde.dropComplete(moveStudentsToTarget(sp, studs));
                        return;
                    }
                }
            }
        }
        dtde.rejectDrop();
    }

    public abstract boolean moveStudentsToTarget(TargetAssessmentSelectionProvider sp, RemoteStudent[] studs);

    private RemoteStudent[] extractStudents(Transferable trans) {
        Node[] ns = NodeTransfer.nodes(trans, NodeTransfer.DND_COPY);
        RemoteStudent[] studs = Arrays.stream(ns).flatMap(n -> n.getLookup().lookupAll(RemoteStudent.class).stream()).distinct().toArray(RemoteStudent[]::new);
        return studs;
    }

}
