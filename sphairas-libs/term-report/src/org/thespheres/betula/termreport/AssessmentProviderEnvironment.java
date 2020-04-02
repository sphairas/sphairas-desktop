/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public interface AssessmentProviderEnvironment {

    public Lookup getContextLookup();

    public AssessmentProvider getProvider();

    public TermReport getTermReport();

}
