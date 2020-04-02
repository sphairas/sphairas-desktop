/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeReference;
import org.thespheres.betula.assess.TargetDocument;

/**
 *
 * @author boris.heithecker
 */
public interface AssessmentReference {

    public Grade resolveReference(GradeReference proxy, StudentId student, TermId term, TargetDocument target);
    
}
