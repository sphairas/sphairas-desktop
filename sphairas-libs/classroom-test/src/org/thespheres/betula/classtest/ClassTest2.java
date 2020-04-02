/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest;

import java.util.Map;
import org.thespheres.betula.RecordId;
import org.thespheres.betula.StudentId;

/**
 *
 * @author boris.heithecker
 * @param <P>
 * @param <C>
 */
public interface ClassTest2<P extends Assessable.Problem, C extends StudentScores> {

    public RecordId getRecord();

    public Map<String, P> getProblems();

    public Map<StudentId, C> getStudentScores();
}
