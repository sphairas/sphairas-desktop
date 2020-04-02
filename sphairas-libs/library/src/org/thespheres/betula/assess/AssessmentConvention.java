/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.util.Arrays;
import java.util.Iterator;
import org.thespheres.betula.Convention;

/**
 *
 * @author boris.heithecker
 */
public interface AssessmentConvention extends Iterable<Grade>, Convention<Grade> {

    public Grade[] getAllGrades();

    public default Grade[] getAllGradesReverseOrder() {
        final Grade[] arr = getAllGrades();
        final Grade[] ret = new Grade[arr.length];
        int to = 0;
        for (int from = arr.length - 1; from >= 0; from--) {
            ret[to++] = arr[from];
        }
        return ret;
    }

    @Override
    public default Iterator<Grade> iterator() {
        return Arrays.stream(getAllGrades()).iterator();
    }

    public Grade parseGrade(String text) throws GradeParsingException;

    public interface OfBiasable extends AssessmentConvention {

        public Grade[] getAllGradesUnbiased();

        public default Grade getFloorUnbiased() {
            final Grade[] arr = getAllGradesUnbiased();
            return arr.length > 0 ? arr[0] : null;
        }

        public default Grade getCeilingUnbiased() {
            final Grade[] arr = getAllGradesUnbiased();
            return arr.length > 0 ? arr[arr.length - 1] : null;
        }

    }

}
