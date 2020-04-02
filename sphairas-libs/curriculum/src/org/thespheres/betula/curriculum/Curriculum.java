/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum;

import java.util.ArrayList;
import java.util.List;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public interface Curriculum {

    public General getGeneral();

    public List<CourseEntry> getEntries();

    public List<Section> getSections();

    default public List<CourseEntry> allCourses() {
        final List<CourseEntry> ret = new ArrayList<>();
        collect(getEntries(), ret);
        return ret;
    }

    default public Section findSection(final int base, final int sequence) {
        return getSections().stream()
                .filter(s -> s.getBase() == base && s.getSequence() == sequence)
                .collect(CollectionUtil.singleOrNull());
    }

    static void collect(final List<CourseEntry> from, final List<CourseEntry> ret) {
        from.forEach(e -> {
            if (e instanceof CourseGroup) {
                collect(((CourseGroup) e).getChildren(), ret);
            } else if (e instanceof CourseEntry) {
                ret.add((CourseEntry) e);
            }
        });
    }
}
