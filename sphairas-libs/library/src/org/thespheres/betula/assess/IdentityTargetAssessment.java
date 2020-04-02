/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.util.Set;
import org.thespheres.betula.Identity;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 * @param <G>
 * @param <I>
 * @param <L>
 */
public interface IdentityTargetAssessment<G, I extends Identity, L extends IdentityTargetAssessment.Listener<G, I>> extends TargetAssessment<G, L> {

    public G select(StudentId student, I gradeId);

    public Timestamp timestamp(StudentId student, I gradeId);

    public void submit(StudentId student, I gradeId, G grade, Timestamp timestamp);

    public Set<I> identities();

    @Override
    default public void submit(StudentId student, G grade, Timestamp timestamp) {
        submit(student, null, grade, timestamp);
    }

    @Override
    default public G select(StudentId student) {
        return select(student, null);
    }

    @Override
    default public Timestamp timestamp(StudentId student) {
        return timestamp(student, null);
    }

    public interface Listener<G, I> extends TargetAssessment.Listener<G> {

        public void valueForStudentChanged(Object source, StudentId student, I gradeId, G old, G newGrade, Timestamp timestamp);

        @Override
        public default void valueForStudentChanged(Object source, StudentId s, G old, G newGrade, Timestamp timestamp) {
        }

        default public void studentRemoved(StudentId source, Identity gradeId) {
        } //TODO: ???? wofür????
    }
}
