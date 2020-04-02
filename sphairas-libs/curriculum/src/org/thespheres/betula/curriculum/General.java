/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum;

import java.time.LocalDate;

/**
 *
 * @author boris.heithecker
 */
public interface General {

    public String getPreferredAssessmenConvention();
    
    public String[] getPreferredSubjectConventions(boolean realm);

    public LocalDate getValidFrom();

    public LocalDate getValidUntil();

    public String getDisplayName();

}
