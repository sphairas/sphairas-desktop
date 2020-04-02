/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports.sibank;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerParsingException;
import org.thespheres.betula.sibank.SiBankImportStudentItem;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.ui2.SiBankUpdateStudentsTableModel;
import org.thespheres.betula.xmlimport.Constants;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns.DefaultMarkerColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn.Factory.Registration;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"StudentSGLColumn.columnName.sgl=Schulzweig"})
class StudentSGLColumn extends DefaultMarkerColumn<SiBankImportStudentItem, SiBankImportTarget, SiBankImportData<SiBankImportStudentItem>, SiBankUpdateStudentsTableModel> implements HighlightPredicate {

    private MarkerConvention[] cc;

    private StudentSGLColumn() {
        super("sgl", 250, true, 100, true);
    }

    @Override
    public void initialize(SiBankImportTarget configuration, SiBankImportData wizard) {
        super.initialize(configuration, wizard);
        cc = Arrays.stream(configuration.getStudentCareerConventions())
                .toArray(MarkerConvention[]::new);
    }

    @Override
    protected MarkerConvention[] getMarkerConventions(SiBankImportTarget configuration) {
        return configuration.getStudentCareerConventions();
    }

    @Override
    public Marker getColumnValue(SiBankImportStudentItem il) {
        Marker sgl = (Marker) il.getClientProperty(Constants.PROP_STUDENT_CAREER);
        if (sgl == null) {
            sgl = setSGL(il);
        }
        assert sgl != null;
        return sgl;
    }

    @Override
    public boolean setColumnValue(SiBankImportStudentItem il, Object value) {
        final Marker sgl = (Marker) value;
        try {
            il.setClientProperty(Constants.PROP_STUDENT_CAREER, sgl == null ? Marker.NULL : sgl);
        } catch (PropertyVetoException ex) {
        }
        return false;
    }

    private Marker setSGL(SiBankImportStudentItem il) {
        final String sgltext = il.getSourceStudentCareer();
        Marker sgl = null;
        if (sgltext != null) {
            for (MarkerConvention mc : cc) {
                try {
                    sgl = mc.parseMarker(sgltext);
                    break;
                } catch (MarkerParsingException ex) {
                }
            }
        }
        if (sgl == null) {
            sgl = Marker.NULL;
        }
        try {
            il.setClientProperty(Constants.PROP_STUDENT_CAREER, sgl);
        } catch (PropertyVetoException ex) {
        }
        return sgl;
    }

    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        final Object v = adapter.getValue();
        return Marker.NULL.equals(v);
    }

    @Override
    public void configureTableColumn(SiBankUpdateStudentsTableModel model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        col.addHighlighter(new BorderHighlighter(this, BorderFactory.createLineBorder(Color.RED, 2)));
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(StudentSGLColumn.class, "StudentSGLColumn.columnName.sgl");
    }

    @Registration(component = "SiBankUpdateStudentsVisualPanel")
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new StudentSGLColumn();
        }

    }
}
