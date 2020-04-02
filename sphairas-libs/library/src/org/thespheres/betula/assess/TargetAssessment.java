/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.awt.datatransfer.DataFlavor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.TargetAssessment.Listener;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 * @param <G>
 * @param <L>
 */
public interface TargetAssessment<G, L extends Listener> {

    public static final String PROP_PREFERRED_CONVENTION = "preferred-convention";
    public static final String PROP_SUBJECT_NAME = "subject-name";
    public static final String PROP_TARGETTYPE = "target-type";
    public static final String PROP_PREFERRED_TERMSCHEDULE_PROVIDER = "preferred-term-schedule";
    public final DataFlavor FLAVOUR = new DataFlavor(TargetAssessment.class, "targetassessmentTA");

    public void submit(StudentId student, G grade, Timestamp timestamp);

    public G select(StudentId student);

    public Timestamp timestamp(StudentId student);

    public Set<StudentId> students();

    public String getPreferredConvention();

    public void addListener(L listener);

    public void removeListener(L listener);

    public interface Listener<G> extends PropertyChangeListener {

        public void valueForStudentChanged(Object source, StudentId student, G old, G newGrade, Timestamp timestamp);

        default public void studentRemoved(StudentId source) {//TODO: ???? wofür????
        }

        @Override
        default public void propertyChange(PropertyChangeEvent evt) {
        }

    }
}
