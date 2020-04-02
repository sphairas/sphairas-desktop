/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports.sibank;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.kgs.ui.imports.SGLFilterOverride;
import org.thespheres.betula.ui.util.JAXBUtil;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistration(target = "SiBankSourceTargetAccess")
@XmlRootElement(name = "sglfilter")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class SiBankSGLFilterOverride extends SGLFilterOverride {

    public SiBankSGLFilterOverride() {
    }

    public SiBankSGLFilterOverride(final Marker[] filter) {
        super(filter);
    }

}
