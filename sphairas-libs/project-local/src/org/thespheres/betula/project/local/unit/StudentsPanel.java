/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.unit;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.awt.MouseUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.thespheres.betula.Student;
import org.thespheres.betula.project.local.unit.StudentsPanelColumnFactory.ModifiedHighlighter;

/**
 *
 * @author boris.heithecker
 */
class StudentsPanel extends javax.swing.JPanel {
    
    private final StudentsPanelColumnFactory colFactory = new StudentsPanelColumnFactory();
    private final StudentsPanelModel model;
    private final PopupAdapter popupListener;
    private final PasteStudents pasteStudents;
    private final DeleteSelection deleteSelection;
    private final AddAction addAction;
    
    StudentsPanel(StudentsPanelModel model) {
        pasteStudents = new PasteStudents();
        deleteSelection = new DeleteSelection();
        addAction = new AddAction();
        popupListener = new PopupAdapter();
        this.model = model;
        initComponents();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addHighlighter(new ModifiedHighlighter());
        table.addMouseListener(popupListener);
        toolbar.add(addAction);
        table.getActionMap().put("delete", deleteSelection);
        table.getActionMap().put("paste", pasteStudents);
        table.getActionMap().put("paste-from-clipboard", pasteStudents);
    }
    
    protected JPopupMenu createPopup() { //int modelCol, int modelRow, Point p, MouseEvent e) {
        Action[] actions = new Action[]{addAction, SystemAction.get(PasteAction.class), SystemAction.get(DeleteAction.class)};
        return Utilities.actionsToPopup(actions, table);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scroll = new javax.swing.JScrollPane();
        table = new org.jdesktop.swingx.JXTable();
        toolbar = new javax.swing.JToolBar();

        setLayout(new java.awt.BorderLayout());

        table.setModel(model);
        table.setColumnFactory(colFactory);
        scroll.setViewportView(table);

        add(scroll, java.awt.BorderLayout.CENTER);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        add(toolbar, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scroll;
    private org.jdesktop.swingx.JXTable table;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {
        
        @Override
        protected void showPopup(MouseEvent e) {
            int selRow = table.rowAtPoint(e.getPoint());
            
            if (selRow != -1) {
                if (!table.getSelectionModel().isSelectedIndex(selRow)) {
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().setSelectionInterval(selRow, selRow);
                }
            } else {
                table.getSelectionModel().clearSelection();
            }
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), table);
//            int column = table.convertColumnIndexToModel(table.columnAtPoint(p));
//            final int rap = table.rowAtPoint(p);
            JPopupMenu pop = null;
//            if (rap != -1) {
//            int row = table.convertRowIndexToModel(rap);
            pop = StudentsPanel.this.createPopup(); //(column, row, p, e);
//            }
            if (pop != null) {
                showPopup(p.x, p.y, pop);
                e.consume();
            }
        }
        
        private void showPopup(int xpos, int ypos, final JPopupMenu popup) {
            if ((popup != null) && (popup.getSubElements().length > 0)) {
                final PopupMenuListener p = new PopupMenuListener() {
                    
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    }
                    
                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        popup.removePopupMenuListener(this);
                        table.requestFocus();
                    }
                    
                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                };
                popup.addPopupMenuListener(p);
                popup.show(table, xpos, ypos);
            }
        }
    }
    
    @Messages({"AddAction.displayName=Neue(r) Schüler(in)"})
    private class AddAction extends AbstractAction {
        
        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private AddAction() {
            super(NbBundle.getMessage(AddAction.class, "AddAction.displayName"));
            final Icon icon = ImageUtilities.loadImageIcon("org/thespheres/betula/project/local/unit/plus-button.png", true);
            putValue(Action.SMALL_ICON, icon);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Student s = LocalStudents.getInstance().newStudent("Müller, Lisa");
            model.add(s);
        }
        
    }
    
    private class DeleteSelection extends AbstractAction {
        
        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private DeleteSelection() {
            super("delete-item");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            int sel = table.getSelectedRow();
            if (sel != -1) {
                int ri = table.convertRowIndexToModel(sel);
                model.removeItemAt(ri);
            }
        }
        
    }
    
    private class PasteStudents extends AbstractAction {
        
        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        private PasteStudents() {
            super("paste-students");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            List<Student> cp = ClipboardUtil.getContent();
            model.addAll(cp);
        }
        
    }
}
