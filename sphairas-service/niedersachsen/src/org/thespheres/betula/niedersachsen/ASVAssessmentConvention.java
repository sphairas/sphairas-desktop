/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.assess.AbstractAssessmentConvention;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeParsingException;

/**
 *
 * @author boris.heithecker
 */
public abstract class ASVAssessmentConvention extends AbstractAssessmentConvention implements AssessmentConvention.OfBiasable {

    public static final String SV_NAME = "niedersachsen.sozialverhalten";
    public static final String AV_NAME = "niedersachsen.arbeitsverhalten";
    public static final String[] GRADES = {"a", "b", "c", "d", "e"};
    private final Map<String, Grade> map = new HashMap<>();

    protected ASVAssessmentConvention(String name) {
        super(name, GRADES);
    }

    @Override
    protected Grade getGrade(String id) {
        if (map.isEmpty()) {
            for (String g : GRADES) {
                map.put(g, new Kopfnote(getName(), g));
            }
        }
        return map.get(id);
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
        throw new GradeParsingException(getName(), text);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ASVAssessmentConvention.class, getName() + ".displayName");
    }

    @ServiceProvider(service = AssessmentConvention.class)
    public static class Arbeitsverhalten extends ASVAssessmentConvention {

        public Arbeitsverhalten() {
            super(AV_NAME);
        }

    }

    @ServiceProvider(service = AssessmentConvention.class)
    public static class Sozialverhalten extends ASVAssessmentConvention {

        public Sozialverhalten() {
            super(SV_NAME);
        }

    }
}
