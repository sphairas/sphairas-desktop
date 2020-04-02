/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.xml;

import org.thespheres.betula.admin.units.configui.UnitSelector;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.UnitId;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "remote-unit-descriptor")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteUnitDescriptor implements UnitSelector {

    @XmlElement(name = "pattern")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String patternText;
    @XmlTransient
    private Pattern pattern;
    @XmlElement(name = "display-name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String displayName;

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean matches(final UnitId unit) {
        return getPattern().matcher(unit.getId()).matches();
    }

    private Pattern getPattern() {
        if (pattern == null) {
            pattern = Pattern.compile(patternText.trim());
        }
        return pattern;
    }

}
