/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.betula.termreport.AssessmentProvider;
import org.thespheres.betula.termreport.TermReport;
import org.thespheres.betula.util.Utilities;

/**
 *
 * @author boris.heithecker
 */
public class TermReportUtilities {

    public static String findId(TermReport context) {
        final Set<String> existing = context.getProviders().stream().map(AssessmentProvider::getId).collect(Collectors.toSet());
        int c = existing.size();
        String id;
        while (existing.contains(id = Utilities.createId(c++))) {
        }
        return id;
    }
    
}
