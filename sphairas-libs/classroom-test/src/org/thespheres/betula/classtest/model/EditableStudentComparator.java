/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thespheres.betula.classtest.model;

import org.thespheres.betula.util.StudentComparator;
import java.util.Comparator;

/**
 *
 * @author boris.heithecker
 */
class EditableStudentComparator implements Comparator<EditableStudent> {

    private final StudentComparator studcomp;

    public EditableStudentComparator() {
        studcomp = new StudentComparator();
    }

    @Override
    public int compare(EditableStudent o1, EditableStudent o2) {
        return studcomp.compare(o1.getStudent(), o2.getStudent());
    }

}
