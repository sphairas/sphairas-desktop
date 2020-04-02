/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum;

import java.util.Optional;
import java.util.Set;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public interface Section {

    public int getBase();

    public int getSequence();

    public Set<CourseSelection> getSelection();

    default public Optional<CourseSelection> getSelection(final CourseEntry course) {
        return getSelection().stream()
                .filter(s -> course != null && s.getCourse() != null && course.getId().equals(s.getCourse().getId()))
                .collect(CollectionUtil.singleton());
    }
}
