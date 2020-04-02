/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractAssessmentConvention implements AssessmentConvention {

    private final String[] gradeIDs;
    private final String name;
    private Grade[] grades;

    protected AbstractAssessmentConvention(String name, String[] gradeIDs) {
        this.gradeIDs = gradeIDs;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Grade[] getAllGrades() {
        if (grades == null) {
            grades = new Grade[gradeIDs.length];
            for (int i = 0; i < gradeIDs.length; i++) {
                grades[i] = getGrade(gradeIDs[i]);
            }
        }
        return grades;
    }

    @Override
    public Grade find(String id) {
        for (String g : gradeIDs) {
            if (g.equals(id)) {
                return getGrade(id);
            }
        }
        return null;
    }

    protected abstract Grade getGrade(String id);
}
