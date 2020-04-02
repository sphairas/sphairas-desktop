/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

import java.util.Collections;
import java.util.Set;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 * @param <S>
 */
public abstract class Students<S extends Student> {

    public static final Students EMPTY = new Students() {
        @Override
        public Set<Student> getStudents() {
            return Collections.EMPTY_SET;
        }
    };

    public S find(StudentId id) {
        return getStudents().stream()
                .filter(s -> s.getStudentId().equals(id))
                .collect(CollectionUtil.singleOrNull());
    }

    public abstract Set<S> getStudents();
}
