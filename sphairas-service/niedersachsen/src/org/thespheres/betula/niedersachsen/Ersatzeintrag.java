/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
@ServiceProvider(service = AssessmentConvention.class)
public class Ersatzeintrag extends AbstractAssessmentConvention {

    public static final String NAME = "niedersachsen.ersatzeintrag";
    public static final String DISPLAYNAME = "Ersatzeintrag";
    public static final String[] ID = {"e", "nb", "b", "ne", "pending"};
    public static final String[] LONG_LABEL = {"e:Entfällt", "nb:Nicht beurteilbar", "b:Befreit", "ne:Nicht erteilt", "pending:Ausstehend"};
    public static final Map<String, String> MAP = new HashMap<>();

    public Ersatzeintrag() {
        super(NAME, ID);
    }

    @Override
    protected Grade getGrade(String id) {
        return new Ersatzgrade(id);
    }

    @Override
    public Grade parseGrade(String text) throws GradeParsingException {
        String trimText = text.trim();
        for (Grade g : getAllGrades()) {
            if (g.getLongLabel().equalsIgnoreCase(trimText) || g.getShortLabel().equalsIgnoreCase(trimText)) {
                return g;
            }
        }
        throw new GradeParsingException(NAME, text);
    }

    @Override
    public String getDisplayName() {
        return DISPLAYNAME;
    }

    private final class Ersatzgrade extends AbstractGrade {

        public Ersatzgrade(String gradeId) {
            super(NAME, gradeId);
        }

        @Override
        public String getShortLabel() {
            String id = getId();
            switch (id) { //"tg", "e", "nb", "b", "ne", "pending"
                case "tg":
                    return "tg.";
                case "e":
                    return "---";
                case "nb":
                    return "n.b.";
                case "b":
                    return "befreit";
                case "ne":
                    return "n.e.";
                case "pending":
                    return "?";
            }
            if ("pending".equals(getId())) {
                return "---";
            }
            return super.getShortLabel();
        }

        @Override
        public String getLongLabel(Object... formattingArgs) {
            synchronized (MAP) {
                if (MAP.isEmpty()) {
                    Arrays.stream(LONG_LABEL).map(s -> s.split(":")).forEach(pp -> {
                        MAP.put(pp[0], pp[1]);
                    });
                }
                return MAP.get(getId());
            }
        }

    }
}
