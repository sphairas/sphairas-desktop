/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.GradeTermTargetAssessment.Listener;

/**
 *
 * @author boris.heithecker
 */
public interface GradeTermTargetAssessment extends IdentityTargetAssessment<Grade, TermId, Listener> {

    public interface Listener extends IdentityTargetAssessment.Listener<Grade, TermId> {
    }
}
