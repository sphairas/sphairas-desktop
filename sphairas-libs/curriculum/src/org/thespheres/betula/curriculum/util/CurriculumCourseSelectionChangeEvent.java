/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.util;

import java.util.EventObject;
import org.thespheres.betula.curriculum.CourseSelectionValue;

/**
 *
 * @author boris.heithecker
 */
public class CurriculumCourseSelectionChangeEvent extends EventObject {

    private final String name;
    private final Object oldValue;
    private final Object newValue;

    public CurriculumCourseSelectionChangeEvent(CourseSelectionValue source, String propertyName, Object oldValue, Object newValue) {
        super(source);
        this.name = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public CourseSelectionValue getCourseSelectionValue() {
        return (CourseSelectionValue) getSource();
    }
}
