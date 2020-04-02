/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.gs.impl;

import java.io.IOException;
import java.io.InputStream;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.services.util.DelegatingAssessmentConvention;
import org.thespheres.betula.services.util.XmlAssessmentConventionSupport;
import org.thespheres.betula.xmldefinitions.XmlAssessmentConventionDefintion;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = AssessmentConvention.class)
public class AnkreuzCnv1 extends DelegatingAssessmentConvention {

    public AnkreuzCnv1() throws IOException {
        try (final InputStream is = AnkreuzCnv1.class.getResourceAsStream("NdsAnkreuz1.xml")) {
            final XmlAssessmentConventionDefintion def = XmlAssessmentConventionSupport.load(is);
            setDelegate(def);
        }
    }

}
