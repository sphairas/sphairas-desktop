/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.awt.Color;
import org.thespheres.betula.ui.util.WideJXComboBox;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;
import javax.swing.DefaultCellEditor;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.Unit;
import org.thespheres.betula.admin.units.RemoteGradeEntry;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocumentName;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.ui.GradeComboBoxModel;

/**
 *
 * @author boris.heithecker
 */
public class TargetsForStudentsColumnFactory extends ColumnFactory {

//    private final StringValue gradeStringValue = v -> v != null ? ((Optional<RemoteGradeEntry>) v).map(RemoteGradeEntry::getGrade).map(Grade::getShortLabel).orElse("") : "null";
    private final StringValue studentStringValue = (Object value) -> {
        if (value instanceof RemoteStudent) {
            final RemoteStudent rs = (RemoteStudent) value;
            return rs.getDirectoryName();
        }
        return value != null ? value.toString() : "null";
    };
    private final StringValue puStringValue = (Object value) -> {
        if (value instanceof Unit) {
            final Unit unit = (Unit) value;
            return unit.getDisplayName();
        }
        return "---";
    };
    boolean addPrimaryUnitColumn;
    Supplier<Term> term;
    private final HashMap<String, JXComboBox> gBoxes = new HashMap<>();
    private final JXTable table;

    TargetsForStudentsColumnFactory(JXTable table) {
        this.table = table;
    }

    @Override
    public void configureColumnWidths(JXTable table, TableColumnExt col) {
        super.configureColumnWidths(table, col);
        int index = col.getModelIndex();
        switch (index) {
            case 0:
                col.setPreferredWidth(200);
                break;
            case 1:
                if (addPrimaryUnitColumn) {
                    col.setPreferredWidth(40);
                }
            default:
                col.setPreferredWidth(60);
                break;
        }
    }

    @Override
    public void configureTableColumn(TableModel model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        int index = col.getModelIndex();
        if (index == 0) {
            configureNamesColumn(model, col);
        } else if (index == 1 && addPrimaryUnitColumn) {
            configurePrimaryUnitColumn(model, col);
        } else if (index != model.getColumnCount() - 1) {
            configureTargetColumn(model, col);
        } else {
            col.setHeaderValue("");
        }
    }

    @NbBundle.Messages({"TargetsforStudentsElement.columnHeader.names=Name"})
    private void configureNamesColumn(TableModel model, TableColumnExt col) {
        final StringJoiner sj = new StringJoiner("<br>", "<html>", "</html>");
        final String header = NbBundle.getMessage(TargetsForStudentsColumnFactory.class, "TargetsforStudentsElement.columnHeader.names");
        sj.add(header).add(" ");
        col.setHeaderValue(sj.toString());
        col.setCellRenderer(new DefaultTableRenderer(studentStringValue));
    }

    @NbBundle.Messages({"TargetsforStudentsElement.columnHeader.primaryUnit=Klasse"})
    private void configurePrimaryUnitColumn(TableModel model, TableColumnExt col) {
        final StringJoiner sj = new StringJoiner("<br>", "<html>", "</html>");
        final String header = NbBundle.getMessage(TargetsForStudentsColumnFactory.class, "TargetsforStudentsElement.columnHeader.primaryUnit");
        sj.add(header).add(" ");
        col.setHeaderValue(sj.toString());
        col.setCellRenderer(new DefaultTableRenderer(puStringValue));
    }

    private void configureTargetColumn(TableModel m, final TableColumnExt col) {
        final TargetsElementModel model = (TargetsElementModel) m;
        final RemoteTargetAssessmentDocument rd = model.getRemoteTargetAssessmentDocumentAtColumnIndex(col.getModelIndex());
        col.setIdentifier(rd.getDocumentId());
        final RemoteTargetAssessmentDocumentName name = rd.getName();
        col.setHeaderValue(term == null ? name.getColumnLabel() : name.getDisplayName(term.get()));
        col.setToolTipText(name.getToolTipText());
        class PCL implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                switch (evt.getPropertyName()) {
                    case RemoteTargetAssessmentDocumentName.PROP_DISPLAYNAME:
                        col.setHeaderValue(term == null ? name.getColumnLabel() : name.getDisplayName(term.get()));
                        break;
                    case TargetsElementModel.TABLE_PROP_CURRENT_TERMID:
                        col.setHeaderValue(term == null ? name.getColumnLabel() : name.getDisplayName(term.get()));
                        break;
                }
            }

        }
        final PCL pcl = new PCL();
        rd.getName().addPropertyChangeListener(pcl); //WeakListeners.propertyChange(pcl, null));
//        col.putClientProperty("column-name-pcl", pcl);//this should be the only strong reference
        table.addPropertyChangeListener(pcl);
        final HiddenColumnAdapter hca = new HiddenColumnAdapter(col);
        col.putClientProperty(HiddenColumnAdapter.class.getName(), hca);
        if (model instanceof TargetsForStudentsModel) {
            final TargetsForStudentsModel tfst = (TargetsForStudentsModel) model;
            tfst.hidden.registerColumn(col, rd.getTargetType());
        }
        final String pConv = rd.getPreferredConvention();
        if (pConv != null) {
            JXComboBox gradesBox = gBoxes.get(pConv);
            if (gradesBox == null) {
                //TODO: user LocalFileProperties custom entry
                final LocalFileProperties find = LocalFileProperties.find(rd.getProvider());
                //Find them in Niedersachsen extra.assessment.conventions
                final GradeComboBoxModel gcbm = new GradeComboBoxModel(new String[]{pConv, "niedersachsen.ersatzeintrag", "niedersachsen.teilnahme", "niedersachsen.uebertrag", "niedersachsen.avsvvorschlag", "ndschoice1"}, false);
                gcbm.setUseLongLabel(false);
                gradesBox = new WideJXComboBox(gcbm);
                gradesBox.setEditable(false);
                gradesBox.setRenderer(new DefaultListRenderer(gcbm));
                gcbm.initialize(gradesBox);
                gBoxes.put(pConv, gradesBox);
            }
            col.setCellEditor(new DefaultCellEditor(gradesBox));
        }
        col.setCellRenderer(new DefaultTableRenderer(new GradeStringValue(rd)));
//        col.addHighlighter(pendingHighlighter);
        final Highlighter nullHL = new ColorHighlighter((r, adapter) -> !Optional.ofNullable(adapter.getValue()).filter(Optional.class::isInstance).flatMap(Optional.class::cast).isPresent(), Color.LIGHT_GRAY, null, Color.DARK_GRAY, null);
        final Highlighter invalHL = new ColorHighlighter((r, adapter) -> Optional.ofNullable(adapter.getValue()).flatMap(ad -> (Optional<RemoteGradeEntry>) ad).map(RemoteGradeEntry::isUnconfirmed).orElse(false), Color.PINK, null, Color.MAGENTA, null);
        col.addHighlighter(nullHL);
        col.addHighlighter(invalHL);
        col.addHighlighter(new AlignmentHighlighter(SwingConstants.CENTER));
    }

}
