/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.util;

import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculum.xml.XmlCurriculum;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
public abstract class CurriculumTableColumn extends PluggableTableColumn<Curriculum, CourseEntry> {

    protected XmlCurriculum curriculum;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    protected CurriculumTableColumn(String id, int position, boolean editable, int width) {
        super(id, position, editable, width);

    }

    public static abstract class Factory extends PluggableTableColumn.Factory<CurriculumTableColumn> {
    }
}
