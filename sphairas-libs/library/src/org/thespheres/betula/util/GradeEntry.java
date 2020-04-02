/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 */
public interface GradeEntry {

    public Grade getGrade();

    public Timestamp getTimestamp();
    
}
