/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import java.util.Arrays;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentDefaults;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.GradeAdapter;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "target-import-settings", namespace = "http://www.thespheres.org/xsd/betula/target-import-settings.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTargetImportSettings implements DocumentDefaults<Grade, TargetDocument> {

    @XmlJavaTypeAdapter(GradeAdapter.class)
    @XmlElement(name = "default")
    private Grade defaultGrade;
    @XmlJavaTypeAdapter(GradeAdapter.class)
    @XmlElement(name = "default-no-signee")
    private Grade noSignee;
    @XmlElementWrapper(name = "target-defaults")
    @XmlElement(name = "target")
    private TargetDefault[] targetDefaults;
    @XmlElementWrapper(name = "target-selectors")
    @XmlElementRef
    private XmlTargetSelector[] targetSelectors;

    @Override
    public Grade getDefaultValue(DocumentId id, TargetDocument document) {
        if ((document != null && document.getSignees().get("entitled.signee") == null) && noSignee != null) {
            return noSignee;
        }
        final String target = document == null ? null : document.getTargetType();
        Grade ret = null;
        if (target != null && targetDefaults != null) {
            ret = Arrays.stream(targetDefaults)
                    .filter(td -> td.target.equalsIgnoreCase(target))
                    .map(td -> td.defaultGrade)
                    .filter(Objects::nonNull)
                    .collect(CollectionUtil.singleOrNull());
        }
        if (ret == null) {
            ret = defaultGrade;
        }
        return ret;
    }

    public TargetDefault[] getTargetDefaults(final ImportTargetsItem selector) {
        if (targetDefaults != null) {
            return Arrays.stream(targetDefaults)
                    .filter(td -> (selector == null || td.selector == null) || Arrays.stream(td.selector).allMatch(s -> s.applies(selector)))
                    .toArray(TargetDefault[]::new);
        }
        return new TargetDefault[0];
    }

    public Grade getDefaultGrade() {
        return defaultGrade;
    }

    public void setDefaultGrade(Grade defaultGrade) {
        this.defaultGrade = defaultGrade;
    }

    public Grade getNoSignee() {
        return noSignee;
    }

    public void setNoSignee(Grade noSignee) {
        this.noSignee = noSignee;
    }

    public TargetDefault[] getTargetDefaults() {
        return targetDefaults;
    }

    public void setTargetDefaults(TargetDefault[] targetDefaults) {
        this.targetDefaults = targetDefaults;
    }

    public XmlTargetSelector[] getTargetSelectors() {
        return targetSelectors;
    }

    public void setTargetSelectors(XmlTargetSelector[] targetSelectors) {
        this.targetSelectors = targetSelectors;
    }

    @XmlType(namespace = "http://www.thespheres.org/xsd/betula/target-import-settings.xsd")//Import, see packageInfo, default namespace is different
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TargetDefault {

        @XmlAttribute(name = "target-type", required = true)
        private String target;
        @XmlAttribute(name = "target-selector")
        @XmlList
        @XmlIDREF
        private XmlTargetSelector[] selector;
        @XmlJavaTypeAdapter(GradeAdapter.class)
        @XmlElement(name = "default")
        private Grade defaultGrade;
        @XmlElement(name = "preferred-convention")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String preferredConvention;

        public TargetDefault() {
        }

        public TargetDefault(String target) {
            this.target = target;
        }

        public String getTargetType() {
            return target;
        }

        public Grade getDefaultGrade() {
            return defaultGrade;
        }

        public String getPreferredConvention() {
            return preferredConvention;
        }

        public void setPreferredConvention(String preferredConvention) {
            this.preferredConvention = preferredConvention;
        }

        public XmlTargetSelector[] getSelector() {
            return selector;
        }

        public void setSelector(XmlTargetSelector[] selector) {
            this.selector = selector;
        }

    }
}
