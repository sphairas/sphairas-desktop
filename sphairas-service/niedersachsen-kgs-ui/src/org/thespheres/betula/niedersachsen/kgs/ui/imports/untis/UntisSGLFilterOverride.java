/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports.untis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.kgs.ui.imports.SGLFilterOverride;
import org.thespheres.betula.ui.util.JAXBUtil;

/**
 *
 * @author boris.heithecker
 */
@JAXBUtil.JAXBRegistration(target = "UntisSourceTargetAccess")
@XmlRootElement(name = "sglfilter")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class UntisSGLFilterOverride extends SGLFilterOverride {

    @XmlAttribute(name = "negate")
    private Boolean negate = null;

    public UntisSGLFilterOverride() {
    }

    UntisSGLFilterOverride(final UntisSGLFilter f) {
        super(f.getFilterMarkers());
        this.negate = f.isNegate();
    }

    Marker getMarker() {
        final Marker[] arr = getFilters();
        return arr != null && arr.length > 0 && arr[0] != null ? arr[0] : null;
    }

    public boolean isNegate() {
        return negate != null ? negate : false;
    }

    public void setNegate(boolean negate) {
        this.negate = negate ? Boolean.TRUE : null;
    }

}
