/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.gs.ui.imports;

import java.beans.PropertyVetoException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.action.XmlCsvImportSettings;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"ImportCrossMarksColumn.columnName.kursart=Ankreuz"})
public class ImportCrossMarksColumn extends DefaultColumns.DefaultCheckBoxColumn<ImportTargetsItem, ConfigurableImportTarget, XmlCsvImportSettings<ImportTargetsItem>, ImportTableModel<ImportTargetsItem, XmlCsvImportSettings<ImportTargetsItem>>> {

    public static final String KEY = "import-crossmarks";

    public ImportCrossMarksColumn() {
        super(KEY, 220);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ImportCrossMarksColumn.class, "ImportCrossMarksColumn.columnName.kursart");
    }

    @Override
    public Boolean getColumnValue(ImportTargetsItem il) {
        return (Boolean) il.getClientProperty(KEY);
    }

    @Override
    public boolean setColumnValue(ImportTargetsItem il, Object value) {
        try {
            il.setClientProperty(KEY, (Boolean) value);
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    @ImportTableColumn.Factory.Registration(component = "XmlTargetDataDocumentsTableModel")
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new ImportCrossMarksColumn();
        }

    }
}
