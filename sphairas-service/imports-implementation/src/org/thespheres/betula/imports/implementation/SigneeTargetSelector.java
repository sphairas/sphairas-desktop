/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.ui.util.JAXBUtil;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.XmlTargetSelector;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistrations({
    @JAXBUtil.JAXBRegistration(target = "XmlTargetImportSettings")})
@XmlRootElement(name = "signee-target-selector") //, namespace = "http://www.thespheres.org/xsd/betula/target-settings-defaults.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class SigneeTargetSelector extends XmlTargetSelector {

    @XmlAttribute(name = "require-signee")
    private boolean requireSignee = true;

    public SigneeTargetSelector() {
        super();
    }

    public SigneeTargetSelector(String id) {
        super(id);
    }

    public boolean isRequireSignee() {
        return requireSignee;
    }

    public void setRequireSignee(boolean requireSignee) {
        this.requireSignee = requireSignee;
    }

    @Override
    public boolean applies(ImportTargetsItem item) {
        final boolean signeeSet = !Signee.isNull(item.getSignee());
        return requireSignee ? signeeSet : true;
    }

}
