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
public interface GroupingGrades {

    public String getDisplayName();

    public String getName();

    public GradeGroup findGroup(Grade g);

    public static interface GradeGroup {

        public Grade[] grades();

        public String getDisplayLabel();
    }
}
