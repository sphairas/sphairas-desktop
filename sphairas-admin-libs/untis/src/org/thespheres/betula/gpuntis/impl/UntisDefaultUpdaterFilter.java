/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import java.util.List;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 *
 * @author boris.heithecker
 */
public class UntisDefaultUpdaterFilter implements UpdaterFilter<ImportedLesson, TargetDocumentProperties> {

    @Override
    public boolean accept(ImportedLesson iti) {
        return iti.isValid();
    }

    @Override
    public boolean accept(ImportedLesson iti, UnitId u, StudentId stud) {
        final List<UpdaterFilter> f = iti.getFilters();
        return f.isEmpty() ? true : f.stream().allMatch(sbf -> sbf.accept(iti, u, stud));
    }

    @Override
    public boolean accept(ImportedLesson iti, TargetDocumentProperties td, StudentId stud) {
        final List<UpdaterFilter> f = iti.getFilters();
        return f.isEmpty() ? true : f.stream().allMatch(sbf -> sbf.accept(iti, td, stud));
    }
}
