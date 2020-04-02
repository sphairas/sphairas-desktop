/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.imports.implementation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.XmlTargetImportSettings;
import org.thespheres.betula.xmlimport.model.XmlTargetSelector;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "target-processor-hints-settings", namespace = "http://www.thespheres.org/xsd/betula/target-settings-defaults.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTargetProcessorHintsSettings {

    @XmlElementWrapper(name = "hints")
    @XmlElement(name = "hint")
    private Hint[] hints;
    @XmlElementWrapper(name = "target-selectors")
    @XmlElementRef
    private XmlTargetSelector[] targetSelectors;

    public Map<String, String> getTargetDefaults(final XmlTargetImportSettings.TargetDefault targetDefault, final ImportTargetsItem item) {
        final Map<String, String> ret = new HashMap<>();
        if (hints != null) {
            Arrays.stream(hints)
                    .filter(td -> td.target == null || targetDefault.getTargetType().equals(td.target))
                    .filter(td -> (item == null || td.selector == null) || td.selector.applies(item))
                    .forEach(td -> ret.put(td.key, td.value));
        }
        return ret;
    }

    public Hint[] getHints() {
        return hints;
    }

    public void setHints(Hint[] hints) {
        this.hints = hints;
    }

    public XmlTargetSelector[] getTargetSelectors() {
        return targetSelectors;
    }

    public void setTargetSelectors(XmlTargetSelector[] targetSelectors) {
        this.targetSelectors = targetSelectors;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Hint {

        @XmlAttribute(name = "key", required = true)
        private String key;
        @XmlAttribute(name = "value", required = true)
        private String value;
        @XmlAttribute(name = "target-type")
        private String target;
        @XmlAttribute(name = "target-selector")
        @XmlIDREF
        private XmlTargetSelector selector;

        public Hint() {
        }

        public Hint(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public XmlTargetSelector getSelector() {
            return selector;
        }

        public void setSelector(XmlTargetSelector selector) {
            this.selector = selector;
        }

    }

}
