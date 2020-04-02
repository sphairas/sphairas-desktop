/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.document.MarkerConvention;

/**
 *
 * @author boris.heithecker
 */
public interface CommonTargetProperties {
//TODO: create abstract implementation, delegate to parent, read/write to properties

    public AssessmentConvention[] getAssessmentConventions();

    //AG, WPK usw. was zu getrenntem Bereich im 
    public MarkerConvention[] getRealmMarkerConventions();

    public MarkerConvention[] getSubjectMarkerConventions();

}
