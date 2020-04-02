/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.XmlTargetSelector;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistrations({
    @JAXBUtil.JAXBRegistration(target = "XmlTargetImportSettings"),
    @JAXBUtil.JAXBRegistration(target = "XmlTargetProcessorHintsSettings")})
@XmlRootElement(name = "assessment-convention-selector") //, namespace = "http://www.thespheres.org/xsd/betula/target-settings-defaults.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class AssessmentConventionTargetSelector extends XmlTargetSelector {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name = "assessment-convention")
    private String convention;

    public AssessmentConventionTargetSelector() {
        super();
    }

    public AssessmentConventionTargetSelector(String id) {
        super(id);
    }

    @Override
    public boolean applies(final ImportTargetsItem item) {
        final String pc = item.getPreferredConvention();
        return Objects.equals(convention, pc);
    }

    public String getConvention() {
        return convention;
    }

    public void setConvention(String convention) {
        this.convention = convention;
    }

}
