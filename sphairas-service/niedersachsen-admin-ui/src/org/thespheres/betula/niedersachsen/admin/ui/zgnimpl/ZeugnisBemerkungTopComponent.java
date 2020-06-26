/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableModel;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.MouseUtils;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2.ReportNote;
import org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.CreateCustomNoteVisualPanel.CreateCustomNoteWizardPanel;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.MarkerItem;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.util.ButtonEditor;
import org.thespheres.betula.util.CollectionUtil;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.thespheres.betula.niedersachsen.admin.ui.zgnimpl//ZeugnisBemerkung//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "ZeugnisBemerkungTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.ZeugnisBemerkungTopComponent")
@ActionReference(path = "Menu/Window/betula-beans-services-windows", position = 1000)
@TopComponent.OpenActionRegistration(displayName = "#ZeugnisBemerkungTopComponent.action",
        preferredID = "ZeugnisBemerkungTopComponent")
@Messages({"ZeugnisBemerkungTopComponent.action=Zeugnisbemerkungen",
    "ZeugnisBemerkungTopComponent.name=Zeugnisbemerkungen",
    "ZeugnisBemerkungTopComponent=Zeugnisbemerkungen - {0} {1} {2})"})
public final class ZeugnisBemerkungTopComponent extends TopComponent {

    private final ColFactory columnFactory = new ColFactory();
    final BemerkungenCBM cbm;
    private final Lookup.Result<RemoteStudent> lkpRes;
    private final Listener listener = new Listener();
    private ZeugnisAngabenModel currentZeungisSettingsModel;
    private final ZeugnisBemerkungModel model = new ZeugnisBemerkungModel(this);
    private final MarkerListener markerListener = new MarkerListener();
    private final CustomNoteButtonListener cnbl = new CustomNoteButtonListener();
    TermReportNoteSetTemplate template;
    private final PopupAdapter popupListener;
    private final DefaultAction defaultAction;
    protected final UndoRedo.Manager undoRedo = new UndoRedo.Manager();

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    public ZeugnisBemerkungTopComponent() {
        popupListener = new PopupAdapter();
        defaultAction = new DefaultAction();
        cbm = new BemerkungenCBM();
        lkpRes = Utilities.actionsGlobalContext().lookupResult(RemoteStudent.class);
        initComponents();
        table.addMouseListener(popupListener);
        table.addMouseListener(defaultAction);
        addMouseListener(defaultAction);
        table.setModel(model);
        setName(NbBundle.getMessage(ZeugnisBemerkungTopComponent.class, "ZeugnisBemerkungTopComponent.name"));
    }

    private synchronized void onChange() {
        markerComboBox.removeActionListener(markerListener);
        customNoteButton.removeActionListener(cnbl);

        Arrays.stream(TopComponent.getRegistry().getActivatedNodes())
                .flatMap(n -> n.getLookup().lookupAll(ZeugnisAngabenModel.class).stream())
                .findAny()
                .ifPresent(this::setCurrentZeugnisSettingModel);

        setStudent();

        markerComboBox.addActionListener(markerListener);
        customNoteButton.addActionListener(cnbl);
    }

    private void setCurrentZeugnisSettingModel(final ZeugnisAngabenModel m) {
        if (m != null) {
            final UnitId uid = m.getUnitOpenSupport().getUnitId();
            if (!Objects.equals(uid, getCurrentUnitId())) {
                template = m.getItemsModel().getTermReportNoteSetTemplate();
                cbm.setTemplate(template);
                currentZeungisSettingsModel = m;
            }
        } else {
            currentZeungisSettingsModel = null;
            model.setCurrentStudent(null);
            updateName();
        }
    }

    private void setStudent() {
        final RemoteStudent rs = lkpRes.allInstances().stream().collect(CollectionUtil.singleOrNull());
        if (currentZeungisSettingsModel != null) {
            if (rs != null && model.getCurrentStudent().map(s -> !s.getRemoteStudent().equals(rs)).orElse(true)) {
                final ReportData2 sd = currentZeungisSettingsModel.findReport(rs.getStudentId());
                model.setCurrentStudent(sd);
            }
        } else {
            model.setCurrentStudent(null);
        }
        updateName();
    }

    private UnitId getCurrentUnitId() {
        return currentZeungisSettingsModel != null ? currentZeungisSettingsModel.getUnitOpenSupport().getUnitId() : null;
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
        topPanel = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(2, 0), new java.awt.Dimension(0, 0));
        markerComboBox = new JXComboBox();
        customNoteButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        table.setColumnFactory(columnFactory);
        table.setHorizontalScrollEnabled(true);
        scrollPanel.setViewportView(table);

        add(scrollPanel, java.awt.BorderLayout.CENTER);

        topPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 2, 2));
        topPanel.add(filler1);

        markerComboBox.setModel(cbm);
        markerComboBox.setMaximumSize(new java.awt.Dimension(720, 32767));
        markerComboBox.setPreferredSize(new java.awt.Dimension(720, 25));
        markerComboBox.setRenderer(new DefaultListRenderer(cbm));
        cbm.initialize((JXComboBox) markerComboBox);
        topPanel.add(markerComboBox);

        org.openide.awt.Mnemonics.setLocalizedText(customNoteButton, org.openide.util.NbBundle.getMessage(ZeugnisBemerkungTopComponent.class, "ZeugnisBemerkungTopComponent.customNoteButton.text")); // NOI18N
        topPanel.add(customNoteButton);

        add(topPanel, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton customNoteButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JComboBox markerComboBox;
    private javax.swing.JScrollPane scrollPanel;
    private org.jdesktop.swingx.JXTable table;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    protected void componentHidden() {
        lkpRes.removeLookupListener(listener);
        TopComponent.getRegistry().removePropertyChangeListener(listener);
        onChange();
    }

    @Override
    protected void componentShowing() {
        onChange();
        lkpRes.addLookupListener(listener);
        TopComponent.getRegistry().addPropertyChangeListener(listener);
    }

    public void updateName() {
        String n = null;
        if (currentZeungisSettingsModel != null && model.getCurrentStudent().isPresent()) {
            final ReportData2 sd = model.getCurrentStudent().get();
            final Term term;
            try {
                term = currentZeungisSettingsModel.getCurrentTerm();
                final String unit = currentZeungisSettingsModel.getUnitOpenSupport().findNamingResolver().resolveDisplayName(currentZeungisSettingsModel.getUnitOpenSupport().getUnitId(), currentZeungisSettingsModel.getCurrentTerm());
                n = NbBundle.getMessage(ZeugnisBemerkungTopComponent.class, "ZeugnisBemerkungTopComponent", sd.getRemoteStudent().getFullName(), unit, term.getDisplayName());
            } catch (IOException | IllegalAuthorityException ex) {
            }
        }
        if (n == null) {
            n = NbBundle.getMessage(ZeugnisBemerkungTopComponent.class, "ZeugnisBemerkungTopComponent.name");
        }
        final String name = n;
        Mutex.EVENT.writeAccess(() -> {
            setDisplayName(name);
            setHtmlDisplayName(name);
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

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private class DefaultAction extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            int selRow = table.rowAtPoint(e.getPoint());

            if ((selRow != -1) && SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
                // Default action.
                if (!table.getSelectionModel().isSelectedIndex(selRow)) {
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().setSelectionInterval(selRow, selRow);
                }
                int rowIndex = table.convertRowIndexToModel(selRow);
                final ReportData2 d = model.getCurrentStudent().get();
                model.getCurrentReportNoteAt(rowIndex)
                        .filter(rd -> rd.getValue() instanceof String)
                        .ifPresent(rn -> {
                            cnbl.showDialog(rn);
                        });
            }

        }
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
                pop = ZeugnisBemerkungTopComponent.this.createPopup(column, row, p, e);
            }
            if (pop != null) {
                ZeugnisBemerkungTopComponent.this.showPopup(p.x, p.y, pop);
                e.consume();
            }
        }

    }

    private final class MarkerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            final MarkerItem m = (MarkerItem) cbm.getSelectedItem();
            model.addMarker(m.getMarker());
        }

    }

    private final class CustomNoteButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            showDialog(null);
        }

        void showDialog(ReportNote<String> n) {
            model.getCurrentStudent().ifPresent(s -> {
                final WizardDescriptor wiz = CreateCustomNoteVisualPanel.createCustomNoteDialog2(n, s);
                if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
                    final String nText = (String) wiz.getProperty(CreateCustomNoteWizardPanel.PROP_NOTETEXT);
                    final Number pos = (Number) wiz.getProperty(CreateCustomNoteWizardPanel.PROP_POSITION);
                    if (!StringUtils.isEmpty(nText) && pos != null && pos.intValue() >= 0 && pos.intValue() <= Integer.MAX_VALUE) {
                        model.addNote(nText, pos.intValue());
                    }
                }
            });
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
                            setCurrentZeugnisSettingModel(null);
                        }
                    }
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
                    columnExt.setPreferredWidth(200);
                    break;
                case 2:
                    columnExt.setPreferredWidth(50);
                    break;
            }
        }

        @NbBundle.Messages({"ZeugnisBemerkungTopComponent.columnHeader.text=Bemerkung",
            "ZeugnisBemerkungTopComponent.columnHeader.position=Einordnung"
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
                    columnExt.setHeaderValue(NbBundle.getMessage(ZeugnisBemerkungTopComponent.class, "ZeugnisBemerkungTopComponent.columnHeader.text"));
                    break;
                case 2:
                    columnExt.setHeaderValue(NbBundle.getMessage(ZeugnisBemerkungTopComponent.class, "ZeugnisBemerkungTopComponent.columnHeader.position"));
                    break;
                default:
                    break;
            }
        }
    }
}
