/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.printing;

import java.util.List;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.BorderCollapseType;
import org.plutext.jaxb.xslfo.PageBreakAfterType;
import org.plutext.jaxb.xslfo.TableColumn;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.printing.ClassroomDistributionTableBuilder.AssessorListTableItem;
import org.thespheres.betula.listprint.builder.TableBuilder;
import org.thespheres.betula.listprint.builder.TableItem;

/**
 *
 * @author boris.heithecker
 */
class ClassroomDistributionTableBuilder extends TableBuilder<AssessorListTableItem> {

    ClassroomDistributionTableBuilder(EditableClassroomTest test, AssessmentConvention acc, AssessmentContext assess) {
        super(new AssessorListTableItem(assess, acc, test));
        table.setBorderCollapse(BorderCollapseType.COLLAPSE);
        table.setPageBreakBefore(PageBreakAfterType.RIGHT);
    }

    @Override
    protected TableColumn createTableColumn(int index) {
        TableColumn ret = super.createTableColumn(index);
        final List<String> cw = ret.getColumnWidth();
        if (index == 0) {
            cw.set(0, "17mm");
        } else {
            cw.set(0, "8mm");
        }
        return ret;
    }
//    @Override
//    protected Block createTableCellBlock(int column, int row) {
//        Block b = super.createTableCellBlock(column, row);
//        if (column > data.getColumnCount() - 3) {
//            b.setTextAlign(TextAlignType.START);
//        }
//        return b;
//    }

    @Override
    protected Block createTableHeaderCellBlock(int column) {
        Block ret = super.createTableHeaderCellBlock(column);
        String content = (String) ret.getContent().get(0);
        content = content.replaceAll("\\s", "\u00A0");
        ret.getContent().set(0, content);
        return ret;
    }

    static class AssessorListTableItem implements TableItem {

        private final AssessmentContext<StudentId, ? extends Comparable> context;
        private final EditableClassroomTest etest;
        private final Grade[] grades;

        private AssessorListTableItem(AssessmentContext assess, AssessmentConvention convention, EditableClassroomTest test) {
            this.context = assess;
            this.etest = test;
            this.grades = convention instanceof AssessmentConvention.OfBiasable
                    ? ((AssessmentConvention.OfBiasable) convention).getAllGradesUnbiased()
                    : convention.getAllGrades();
        }

        @Override
        public int getRowCount() {
            return 2;
        }

        @Override
        public int getColumnCount() {
            return grades.length + 1;
        }

        @Override
        public String getColumnName(int ci) {
            return ci == 0 ? "" : grades[ci - 1].getLongLabel();
        }

        @Override
        public Object getValueAt(int rowIndex, int ci) {
            if (ci == 0) {
                switch (rowIndex) {
                    case 0:
                        return "Ab Pkt.";
                    case 1:
                        return "Anzahl";
                }
            } else {
                Grade g = grades[ci - 1];
                switch (rowIndex) {
                    case 0:
                        return context.getAllocator().getFloor(g);
                    case 1:
                        return etest.getGrades().get(g);
                }
            }
            throw new IndexOutOfBoundsException();
        }

    }

}
