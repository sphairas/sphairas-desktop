/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.NumberEditorExt;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.nodes.Node;
import org.thespheres.betula.tag.State;

/**
 *
 * @author boris.heithecker
 */
public abstract class NumberAssessmentProvider extends AssessmentProvider<Number> {

    public static final String PROP_PROVIDER_REFERENCES = "number-assessment-provider-references";

    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
    private final StringValue numberStringValue = o -> o instanceof Number ? nf.format(((Number) o).doubleValue()) : "---";

    protected NumberAssessmentProvider(String id, State initial) {
        super(id, initial);
        nf.setMaximumFractionDigits(2);

    }

    @Override
    protected TableColumnConfiguration createTableColumnConfiguration() {
        class NumberTableColumnConfiguration extends TableColumnConfiguration {

            private final NumberEditorExt nEditor;

            public NumberTableColumnConfiguration() {
                super(NumberAssessmentProvider.this);
                nEditor = new NumberEditorExt(nf);
            }

            @Override
            public void configureTableColumn(TableModel m, TableColumnExt columnExt) {
                super.configureTableColumn(m, columnExt);
                columnExt.setCellRenderer(new DefaultTableRenderer(numberStringValue));
                if (isEditable()) {
                    columnExt.setCellEditor(nEditor);
                }
            }

            @Override
            public String getString(Object value) {
                if (value instanceof Number) {
                    return numberStringValue.getString(value);
                }
                return super.getString(value);
            }

        }
        return new NumberTableColumnConfiguration();
    }

    public abstract List<? extends ProviderReference> getProviderReferences();

    public interface ProviderReference {

        public static final String PROP_WEIGHT = "weight";

        public Node getNodeDelegate();

        public AssessmentProvider getReferenced();

        public double getWeight();

        public void setWeight(double weight);

        public void addPropertyChangeListener(PropertyChangeListener listener);

        public void removePropertyChangeListener(PropertyChangeListener listener);
    }
}
