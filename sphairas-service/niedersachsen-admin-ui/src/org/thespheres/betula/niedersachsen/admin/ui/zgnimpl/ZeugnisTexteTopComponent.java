/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.MouseUtils;
import org.openide.awt.UndoRedo;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.util.ButtonEditor;
import org.thespheres.betula.util.CollectionUtil;

@ConvertAsProperties(dtd = "-//org.thespheres.betula.niedersachsen.admin.ui.zgnimpl//ZeugnisTexte//EN",
        autostore = false)
@MultiViewElement.Registration(mimeType = ZeugnisBemerkungenEnv.MIME,
        persistenceType = TopComponent.PERSISTENCE_ALWAYS,
        displayName = "#ZeugnisTexteTopComponent.name",
        preferredID = "ZeugnisTexteTopComponent",
        position = 2000)
@Messages({"ZeugnisTexteTopComponent.name=Zeugnistexte",
    "ZeugnisTexteTopComponent=Zeugnistexte - {0} {1} {2})"})
public final class ZeugnisTexteTopComponent extends TopComponent implements MultiViewElement {

    private final ColFactory columnFactory = new ColFactory();
    private final Lookup.Result<RemoteStudent> lkpRes;
    private final Listener listener = new Listener();
    private ZeugnisAngabenModel currentZeugnisAngabenModel;
    private final ZeugnisTexteModel model = new ZeugnisTexteModel();
    private final PopupAdapter popupListener;
    protected final UndoRedo.Manager undoRedo = new UndoRedo.Manager();
    private final JButton saveButton;
    private final JButton addButton;
    private final JToolBar toolbar = new JToolBar();
    private MultiViewElementCallback callback;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public ZeugnisTexteTopComponent() {
        toolbar.setFloatable(false);
        popupListener = new PopupAdapter();
        lkpRes = Utilities.actionsGlobalContext().lookupResult(RemoteStudent.class);
        initComponents();
        table.addMouseListener(popupListener);
        table.setModel(model);
        final ImageIcon saveImage = ImageUtilities.loadImageIcon("org/thespheres/betula/niedersachsen/admin/ui/resources/disk.png", true);
        saveButton = new JButton(saveImage);
        saveButton.addActionListener(e -> model.saveTemplate());
        toolbar.add(saveButton);
        final ImageIcon addImage = ImageUtilities.loadImageIcon("org/thespheres/betula/niedersachsen/admin/ui/resources/plus-button.png", true);
        addButton = new JButton(addImage);
        addButton.addActionListener(e -> model.createTemplate());
        toolbar.add(addButton);
        updateActions();
        model.pSupport.addPropertyChangeListener(listener);
        setName(NbBundle.getMessage(ZeugnisTexteTopComponent.class, "ZeugnisTexteTopComponent.name"));
    }

    private void updateActions() {
        saveButton.setEnabled(model.isTemplateValid());
        addButton.setEnabled(!model.hasTemplate());
    }

    private synchronized void onChange() {

        Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                .flatMap(n -> n.getLookup().lookupAll(ZeugnisAngabenModel.class).stream())
                .findAny()
                .ifPresent(this::setCurrentZeugnisTexteModel);

        setStudent();

    }

    private void setCurrentZeugnisTexteModel(final ZeugnisAngabenModel m) {
        if (m != null) {
            final UnitId uid = m.getUnitOpenSupport().getUnitId();
            if (!Objects.equals(uid, getCurrentUnitId())) {
                currentZeugnisAngabenModel = m;
            }
        } else {
            currentZeugnisAngabenModel = null;
            model.setCurrentStudent(null);
            updateName();
        }
    }

    private void setStudent() {
        final RemoteStudent rs = lkpRes.allInstances().stream().collect(CollectionUtil.singleOrNull());
        if (currentZeugnisAngabenModel != null) {
            if (rs != null && model.getCurrentStudent().map(s -> !s.getRemoteStudent().equals(rs)).orElse(true)) {
                final ReportData2 sd = currentZeugnisAngabenModel.findReport(rs.getStudentId());
                model.setCurrentStudent(sd);
            }
        } else {
            model.setCurrentStudent(null);
        }
        updateName();
    }

    private UnitId getCurrentUnitId() {
        return currentZeugnisAngabenModel != null ? currentZeugnisAngabenModel.getUnitOpenSupport().getUnitId() : null;
    }

    /**
     * This method is called from within the constructor to loadTerm the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPanel = new javax.swing.JScrollPane();
        table = new org.jdesktop.swingx.JXTable();

        setLayout(new java.awt.BorderLayout());

        table.setColumnFactory(columnFactory);
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);

        add(scrollPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPanel;
    private org.jdesktop.swingx.JXTable table;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentHidden() {
        lkpRes.removeLookupListener(listener);
        TopComponent.getRegistry().removePropertyChangeListener(listener);
        onChange();
    }

    @Override
    public void componentShowing() {
        onChange();
        lkpRes.addLookupListener(listener);
        TopComponent.getRegistry().addPropertyChangeListener(listener);
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public void setMultiViewCallback(final MultiViewElementCallback cb) {
        this.callback = cb;
        updateName();
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    public void updateName() {
        String n = null;
        if (currentZeugnisAngabenModel != null && model.getCurrentStudent().isPresent()) {
            final ReportData2 sd = model.getCurrentStudent().get();
            final Term term;
            try {
                term = currentZeugnisAngabenModel.getCurrentTerm();
                final String unit = currentZeugnisAngabenModel.getUnitOpenSupport().findNamingResolver().resolveDisplayName(currentZeugnisAngabenModel.getUnitOpenSupport().getUnitId(), currentZeugnisAngabenModel.getCurrentTerm());
                n = NbBundle.getMessage(ZeugnisTexteTopComponent.class, "ZeugnisTexteTopComponent", sd.getRemoteStudent().getFullName(), unit, term.getDisplayName());
            } catch (IOException | IllegalAuthorityException ex) {
            }
        }
        if (n == null) {
            n = NbBundle.getMessage(ZeugnisTexteTopComponent.class, "ZeugnisTexteTopComponent.name");
        }
        final String name = n;
        Mutex.EVENT.writeAccess(() -> {
            if (callback != null) {
                callback.getTopComponent().setDisplayName(name);
                callback.getTopComponent().setHtmlDisplayName(name);
            }
        });
    }

    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
//                    final Node ern = ej.getEditableRecords().get(modelCol - 1).getNodeDelegate();
//            final Node epn = ej.getEditableParticipants().get(modelRow).getNodeDelegate();
//            final Lookup context = new ProxyLookup(ern.getLookup(), epn.getLookup());
//            Action[] ac = Stream.concat(Utilities.actionsForPath("Loaders/text/betula-journal-record-context/Actions").stream(),
//                    Utilities.actionsForPath("Loaders/text/betula-journal-participant-context/Actions").stream())
//                    .toArray(Action[]::new);
//            return Utilities.actionsToPopup(ac, context);
        return null;
    }

    private void showPopup(int x, int y, final JPopupMenu popup) {
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
            popup.show(table, x, y);
        }
    }

    void writeProperties(final java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        final String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

        @Override
        protected void showPopup(MouseEvent e) {
            int sel = table.rowAtPoint(e.getPoint());
            if (sel != -1) {
                if (!table.getSelectionModel().isSelectedIndex(sel)) {
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().setSelectionInterval(sel, sel);
                }
            } else {
                table.getSelectionModel().clearSelection();
            }
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), table);
            int column = table.convertColumnIndexToModel(table.columnAtPoint(p));
            final int rap = table.rowAtPoint(p);
            JPopupMenu pop = null;
            if (rap != -1) {
                int row = table.convertRowIndexToModel(rap);
                pop = ZeugnisTexteTopComponent.this.createPopup(column, row, p, e);
            }
            if (pop != null) {
                ZeugnisTexteTopComponent.this.showPopup(p.x, p.y, pop);
                e.consume();
            }
        }

    }

    private final class Listener implements PropertyChangeListener, LookupListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case TopComponent.Registry.PROP_ACTIVATED_NODES:
                    onChange();
                    break;
                case TopComponent.Registry.PROP_TC_CLOSED:
                    TopComponent closed = (TopComponent) evt.getNewValue();
                    PrimaryUnitOpenSupport uos = null;
                    if (closed != null && (uos = closed.getLookup().lookup(PrimaryUnitOpenSupport.class)) != null) {
                        if (Objects.equals(getCurrentUnitId(), uos.getUnitId())) {
                            setCurrentZeugnisTexteModel(null);
                        }
                    }
                    break;
                case ZeugnisTexteModel.PROP_TEMPLATE:
                    updateActions();
                    break;
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            setStudent();
        }

    }

    private final class ColFactory extends ColumnFactory {

        private final ButtonEditor button = new ButtonEditor();

        @Override
        public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
            int col = columnExt.getModelIndex();
            switch (col) {
                case 0:
                    ButtonEditor.configureTableColumn(columnExt);
                    break;
                case 1:
                    columnExt.setPreferredWidth(100);
                    break;
                case 2:
                    columnExt.setPreferredWidth(400);
                    break;
            }
        }

        @NbBundle.Messages({"ZeugnisTexteTopComponent.columnHeader.key=Name/Schlüssel",
            "ZeugnisTexteTopComponent.columnHeader.value=Text"
        })
        @Override
        public void configureTableColumn(TableModel model, TableColumnExt columnExt) {
            super.configureTableColumn(model, columnExt); //To change body of generated methods, choose Tools | Templates.
            switch (columnExt.getModelIndex()) {
                case 0:
                    columnExt.setHeaderValue("");
                    columnExt.setCellRenderer(button.createRenderer());
                    columnExt.setCellEditor(button);
                    break;
                case 1:
                    columnExt.setHeaderValue(NbBundle.getMessage(ZeugnisTexteTopComponent.class, "ZeugnisTexteTopComponent.columnHeader.key"));
                    break;
                case 2:
                    columnExt.setHeaderValue(NbBundle.getMessage(ZeugnisTexteTopComponent.class, "ZeugnisTexteTopComponent.columnHeader.value"));
                    break;
//                default:
//                    break;
            }
        }
    }
}
