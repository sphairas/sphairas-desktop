/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.util;

import java.awt.Component;
import javax.swing.JTable;

/**
 *
 * @author boris.heithecker
 */
public interface EditableCourseSelectionValue {

    public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column);

    public Object getCellEditorValue();
    
}
