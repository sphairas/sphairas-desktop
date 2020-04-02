/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.web;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.util.PropertyMapAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "web-ui-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlWebUIConfiguration implements WebUIConfiguration {

    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "logo-resource")
    private String logoResource;
    @XmlElement(name = "login-provider-display-label")
    private String loginProviderDisplayLabel;
    @XmlElement(name = "primary-unit-listed-target-types")
    @XmlList
    private String[] primaryUnitListedTargetTypes;
    @XmlElement(name = "commit-target-types")
    @XmlList
    private String[] commitTargetTypes;
    @XmlElement(name = "default-commit-target-type")
    private String defaultCommitTargetType;
    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertyMapAdapter.class)
    private final Map<String, String> stringProperties = new HashMap<>();

    public XmlWebUIConfiguration() {
    }

    public XmlWebUIConfiguration(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLogoResource() {
        return logoResource;
    }

    public void setLogoResource(String logoResource) {
        this.logoResource = logoResource;
    }

    @Override
    public String[] getCommitTargetTypes() {
//        return new String[]{"quartalsnoten", NdsCommonConstants.SUFFIX_ZEUGNISNOTEN, NdsCommonConstants.SUFFIX_AV, NdsCommonConstants.SUFFIX_SV};
        return commitTargetTypes;
    }

    public void setEditableTargetTypes(String[] editableTargetTypes) {
        this.commitTargetTypes = editableTargetTypes;
    }

    @Override
    public String[] getPrimaryUnitListedTargetTypes() {
//        return new String[]{NdsCommonConstants.SUFFIX_ZEUGNISNOTEN, NdsCommonConstants.SUFFIX_AV, NdsCommonConstants.SUFFIX_SV, "vorzensuren"};
        return primaryUnitListedTargetTypes;
    }

    public void setPrimaryUnitListedTargetTypes(String[] listedTargetTypes) {
        this.primaryUnitListedTargetTypes = listedTargetTypes;
    }

    @Override
    public String getDefaultCommitTargetType() {
//        return NdsCommonConstants.SUFFIX_ZEUGNISNOTEN;
        return defaultCommitTargetType;
    }

    public void setDefaultEditingTargetType(String defaultTargetType) {
        this.defaultCommitTargetType = defaultTargetType;
    }

    @Override
    public String getLoginProviderDisplayLabel() {
        return loginProviderDisplayLabel;
    }

    public void setLoginProviderDisplayLabel(String loginProviderDisplayLabel) {
        this.loginProviderDisplayLabel = loginProviderDisplayLabel;
    }

    @Override
    public String getProperty(final String prop) {
        return stringProperties.get(prop);
    }
    
    public void setProperty(final String key, final String value) {
        stringProperties.put(key, value);
    }

}
