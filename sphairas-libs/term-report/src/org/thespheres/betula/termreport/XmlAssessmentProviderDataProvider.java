/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

/**
 *
 * @author boris.heithecker
 * @param <P>
 */
public interface XmlAssessmentProviderDataProvider<P extends AssessmentProvider> {

    public <D extends XmlAssessmentProviderData<P>> D getXmlAssessmentProviderData();
}
