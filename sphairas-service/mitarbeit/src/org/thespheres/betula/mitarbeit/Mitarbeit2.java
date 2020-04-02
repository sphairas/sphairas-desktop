/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.mitarbeit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.assess.AbstractAssessmentConvention;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;

/**
 *
 * @author boris.heithecker
 */
@Messages({"mitarbeit2.displayName=Mitarbeit & Anwesenheit",
    "mitarbeit2.displayName.plus-plus=herausragend",
    "mitarbeit2.displayName.plus=überdurchschnittlich",
    "mitarbeit2.displayName.x-plus=durchschnittlich bemüht",
    "mitarbeit2.displayName.x=durchschnittlich",
    "mitarbeit2.displayName.minus=unterdurchschnittlich",
    "mitarbeit2.displayName.minus-minus=unbeteiligt",
    "mitarbeit2.displayName.f=abwesend",
    "mitarbeit2.displayName.e=abwesend",
    "mitarbeit2.displayName.entfall=entfällt",
    "mitarbeit2.displayName.undefined=Eintrag ausstehend"})
@ServiceProvider(service = AssessmentConvention.class)
public class Mitarbeit2 extends AbstractAssessmentConvention implements AssessmentConvention.OfBiasable {

    public static final String NAME = "mitarbeit2";
    public static final String[] GRADES = {"plus-plus", "plus", "x-plus", "x", "minus", "minus-minus", "f", "e", "undefined", "entfall"};
    public static final String[] SHORT_LABELS = {"plus-plus:++",
        "plus:+",
        "x-plus:*+",
        "x:*",
        "minus:-",
        "minus-minus:--",
        "f:f",
        "e:e",
        "undefined:?",
        "entfall:---"};
    private Map<String, MAGrade> map;

    public Mitarbeit2() {
        super(NAME, GRADES);
    }

    @Override
    protected synchronized Grade getGrade(String id) {
        if (map == null) {
            map = new HashMap<>();
            for (String g : GRADES) {
                Map<String, String> sl = Arrays.stream(SHORT_LABELS)
                        .map(s -> s.split(":"))
                        .collect(Collectors.toMap(pp -> pp[0], pp -> pp[1]));
                map.put(g, new MAGrade(g, sl.get(g)));
            }
        }
        return map.get(id);
    }

    @Override
    public Grade getCeilingUnbiased() {
        return getGrade("plus-plus");
    }

    @Override
    public Grade getFloorUnbiased() {
        return getGrade("minus-minus");
    }

    @Override
    public Grade[] getAllGradesUnbiased() {
        return getAllGrades();
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        String trimText = text.trim();
        for (Grade g : getAllGrades()) {
            if (g.getLongLabel().equalsIgnoreCase(trimText)
                    || g.getShortLabel().equalsIgnoreCase(trimText)) {
                return g;
            }
        }
        throw new GradeParsingException(NAME, text);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(Mitarbeit2.class, getName() + ".displayName");
    }

    private final class MAGrade extends AbstractGrade {

        private final String label;

        public MAGrade(String gradeId, String label) {
            super(NAME, gradeId);
            this.label = label;
        }

        @Override
        public String getShortLabel() {
            return label;
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            return NbBundle.getMessage(Mitarbeit2.class, getName() + ".displayName." + getId());
        }

    }
}
