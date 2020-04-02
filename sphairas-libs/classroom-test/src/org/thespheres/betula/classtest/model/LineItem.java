/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class LineItem {

    public enum LineType {
        STUDENTS(2),
        PROBLEM_MAX(0),
        PROBLEM_WEIGHT(1),
        PROBLEM_MEAN(-1);
        private static final LineType MAX;
        private final int index;

        static {
            MAX = Arrays.stream(LineType.values())
                    .max(Comparator.comparingInt(LineType::getIndex))
                    .get();
        }

        private LineType(int index) {
            this.index = index;
        }

        private int getIndex() {
            return index;
        }

    }
    final LineType type;
    final EditableStudent student;

    public LineItem(EditableStudent student) {
        this.student = student;
        this.type = LineType.STUDENTS;
    }

    public LineItem(LineType type) {
        this.student = null;
        this.type = type;
    }

    public Optional<EditableStudent> getStudent() {
        return Optional.ofNullable(student);
    }

    public LineType getLineType() {
        return type;
    }

    public static int rowCount(int size) {
        return size + LineItem.LineType.values().length - 1;
    }

    public static int toModelIndex(int row, int studentsSize) {
        if (row < 0 || studentsSize < 0) {
            throw new IllegalArgumentException("Row and studentsSize must be greater or equal null.");
        }
        final int m = LineType.MAX.getIndex();
        if (row < m || row > studentsSize + m) {
            return -1;
        } else {
            return row - m;
        }
    }

    public static LineType toLineType(int row, int studentsSize) {
        if (row < 0 || studentsSize < 0) {
            throw new IllegalArgumentException("Row and studentsSize must be greater or equal null.");
        }
        final int m = LineType.MAX.getIndex();
        if (row < m) {
            return LineItem.findForIndex(row);
        } else if (row >= studentsSize + m) {
            int i = studentsSize - row + 1;
            return LineItem.findForIndex(i);
        }
        return LineType.STUDENTS;
    }

    public static int toRowIndex(LineType type, int studentIndex, int studentsSize) {
        if (type.equals(LineType.STUDENTS)) {
            return LineType.STUDENTS.getIndex() + studentIndex;
        }
        int i = type.getIndex();
        if (i < 0) {
            return studentsSize + LineType.values().length -1 + i;
        }
        return i;
    }

    static LineType findForIndex(int i) {
        return Arrays.stream(LineType.values()).filter(lt -> lt.getIndex() == i).collect(CollectionUtil.requireSingleOrNull());
    }
}
