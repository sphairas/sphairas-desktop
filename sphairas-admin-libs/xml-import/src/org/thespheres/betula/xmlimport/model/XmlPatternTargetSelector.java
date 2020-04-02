/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "pattern-target-selector", namespace = "http://www.thespheres.org/xsd/betula/target-import-settings.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlPatternTargetSelector extends XmlTargetSelector {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name = "pattern", required = true)
    private String source;
    @XmlTransient
    private Pattern pattern;

    public XmlPatternTargetSelector() {
        super();
    }

    public XmlPatternTargetSelector(String name) {
        super(name);
    }

    @Override
    public boolean applies(ImportTargetsItem item) {
        final DocumentId targetDocumentIdBase = item.getTargetDocumentIdBase();
        return getPatternCompiled().matcher(targetDocumentIdBase.getId()).matches();
    }

    private Pattern getPatternCompiled() {
        if (pattern == null) {
            pattern = Pattern.compile(source);
        }
        return pattern;
    }

    public String getPattern() {
        return source;
    }

    public void setPattern(String source) {
        this.source = source;
    }

}
