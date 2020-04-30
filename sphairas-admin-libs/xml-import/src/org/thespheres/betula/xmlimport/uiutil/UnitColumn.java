/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.text.ParseException;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.WeakListeners;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.util.Units;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <T>
 * @param <W>
 * @param <M>
 */
public class UnitColumn<I extends ImportTargetsItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultColumns<I, T, W, M> {

    public static final String ID = "unit";
    private final JXComboBox unitsBox;
    private final UnitFormatter unitFormatter = new UnitFormatter();
    private final ExistsHL existsHighlighter = new ExistsHL();

    public UnitColumn(String product) {
        super(ID, 300, true, 170, product);
        unitsBox = new JXComboBox();
        unitsBox.setEditable(true);
    }

    public UnitColumn(String product, boolean editable) {
        super(ID, 300, editable, 170, product);
        unitsBox = new JXComboBox();
        unitsBox.setEditable(true);
    }

    @Override
    public void initialize(T configuration, W wiz) {
        final UnitId[] units = Units.get(configuration.getWebServiceProvider().getInfo().getURL())
                .map(Units::getUnits)
                .map(u -> u.stream().toArray(UnitId[]::new))
                .orElseThrow(IllegalStateException::new);
        DefaultComboBoxModel cbm = new DefaultComboBoxModel(units);
        unitsBox.setModel(cbm);
        unitFormatter.initialize(configuration);
    }

    @Override
    protected void initialize(I il) {
        super.initialize(il);
        il.addVetoableChangeListener(WeakListeners.vetoableChange(existsHighlighter, il));
        existsHighlighter.update();
    }

    @Override
    public Object getColumnValue(I il) {
        return il.getUnitId();
    }

    @Override
    public boolean setColumnValue(I il, Object value) {
        il.setUnitId((UnitId) value);
        return true;
    }

    @Override
    public void configureTableColumn(M model, TableColumnExt col) {
        final JFormattedTextField tfield = new JFormattedTextField(unitFormatter);
        tfield.setBorder(BorderFactory.createEmptyBorder());

        class CellEditor extends DefaultCellEditor {

            private CellEditor() {
                super(tfield);
                tfield.removeActionListener(delegate);
                delegate = new DefaultCellEditor.EditorDelegate() {
                    @Override
                    public void setValue(Object value) {
                        tfield.setValue(value);
                    }

                    @Override
                    public Object getCellEditorValue() {
                        return tfield.getValue();
                    }
                };
                tfield.addActionListener(delegate);
            }
        }

        class UserUnitIdHighlighter extends FontHighlighter implements HighlightPredicate {

            @SuppressWarnings({"LeakingThisInConstructor",
                "OverridableMethodCallInConstructor"})
            public UserUnitIdHighlighter(ImportTableModel model) {
                setHighlightPredicate(this);
            }

            @Override
            protected boolean canHighlight(Component component, ComponentAdapter adapter) {
                if (getFont() == null) {
                    setFont(adapter.getComponent().getFont().deriveFont(Font.BOLD));
                }
                return super.canHighlight(component, adapter); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                M m = (M) ((JTable) adapter.getComponent()).getModel();
                int r = adapter.convertRowIndexToModel(adapter.row);
                if (r < m.selected.size()) {
                    I kurs = m.selected.get(r);
                    return !kurs.isUnitIdGenerated();
                }
                return false;
            }
        }

        col.setCellEditor(new CellEditor());
        col.setCellRenderer(new DefaultTableRenderer(o -> o instanceof UnitId ? ((UnitId) o).getId() : null));
        col.addHighlighter(existsHighlighter);
        col.addHighlighter(new UserUnitIdHighlighter((ImportTableModel) model));
    }

    private class ExistsHL extends ColorHighlighter implements HighlightPredicate, VetoableChangeListener {

        @SuppressWarnings({"LeakingThisInConstructor",
            "OverridableMethodCallInConstructor"})
        private ExistsHL() {
            super(null, Color.BLUE);
            setHighlightPredicate(this);
        }

        @Override
        public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
            final M m = (M) ((JTable) adapter.getComponent()).getModel();
            int r = adapter.convertRowIndexToModel(adapter.row);
            if (r < m.selected.size()) {
                I kurs = m.selected.get(r);
                return kurs.existsUnitInSystem();
            }
            return false;
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (ImportTargetsItem.PROP_UNITID.equals(evt.getPropertyName())) {
                update();
            }
        }

        void update() {
            EventQueue.invokeLater(this::fireStateChanged);
        }
    }

    private static class UnitFormatter extends IdFormatter<UnitId> {

        @Override
        public UnitId stringToValue(String text) throws ParseException {
            if (config != null && checkUid(text)) {
                final String uid = StringUtils.trimToNull(text);
                return uid != null ? new UnitId(config.getAuthority(), uid) : null;
            } else if (NULL_LABEL.equals(StringUtils.trimToEmpty(text))) {
                return null;
            }
            throw new ParseException(text, 0);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            return value != null ? ((UnitId) value).getId() : NULL_LABEL;
        }

    }
}
