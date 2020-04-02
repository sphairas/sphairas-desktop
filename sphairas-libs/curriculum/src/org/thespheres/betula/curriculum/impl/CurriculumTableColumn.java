/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
@Messages({"CurriculumTableColumn.displayName.sum=Summe"})
public abstract class CurriculumTableColumn extends PluggableTableColumn<Curriculum, CourseEntry> {

    protected CurriculumTableColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);
    }

    @Override
    public String getDisplayName() {
        final String c = columnId();
        return NbBundle.getMessage(CurriculumTableColumn.class, "CurriculumTableColumn.displayName." + c);
    }

    static class SumColumn extends CurriculumTableColumn {

        SumColumn() {
            super("sum", 1000, false, 100);
        }

        @Override
        public Object getColumnValue(CourseEntry il) {
            return "---";
        }

    }

}
