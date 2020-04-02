/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme;

import org.thespheres.betula.TermId;
import org.thespheres.betula.assess.GradeReference;
import org.thespheres.betula.services.IllegalAuthorityException;

/**
 *
 * @author boris.heithecker
 */
public interface PrecedingTermGradeReference extends GradeReference {

    public TermId findPrecedingTermId(TermId original) throws IllegalAuthorityException;
}
