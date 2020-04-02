/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.model;

import org.thespheres.betula.classtest.Assessable.Problem;

/**
 *
 * @author boris.heithecker
 * @param <P>
 */
public interface ClassroomTestOutline<P extends Problem> {

    public P createProblem(EditableClassroomTest<?, ?, ?> test, EditableProblem<?> parent);
}
