/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.util.List;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <D>
 */
public interface UpdaterFilter<I extends ImportTargetsItem, D extends TargetDocumentProperties> {

    default public boolean accept(I iti) {
        return true;
    }

    default public boolean accept(I iti, D td) {
        return true;
    }

    default public boolean accept(I iti, D td, StudentId stud) {
        final List<UpdaterFilter> f = iti.getFilters();
        return f.isEmpty() ? true : f.stream()
                .filter(sbf -> sbf != this)
                .allMatch(sbf -> sbf.accept(iti, td, stud));
    }

    default public boolean accept(I iti, UnitId u, StudentId stud) {
        final List<UpdaterFilter> f = iti.getFilters();
        return f.isEmpty() ? true : f.stream()
                .filter(sbf -> sbf != this)
                .allMatch(sbf -> sbf.accept(iti, u, stud));
    }

    default public boolean accept(I iti, D td, final StudentId student, final TermId term, final ImportTargetsItem.GradeEntry entry) {
        return true;
    }
}
