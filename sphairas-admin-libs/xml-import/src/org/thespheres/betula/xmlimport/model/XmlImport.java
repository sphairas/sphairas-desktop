/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso({XmlUnitItem.class, XmlTargetItem.class, XmlTargetEntryItem.class, XmlStudentItem.class, XmlSigneeItem.class})
@XmlRootElement(name = "xml-import") //, namespace = "http://www.thespheres.org/xsd/betula/xml-import.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlImport {

    @XmlElementWrapper(name = "products")
    @XmlElement(name = "product")
    private final List<Product> products = new ArrayList<>();
    @XmlElementWrapper(name = "items")
    @XmlElementRef
    protected final List<XmlItem> items = new ArrayList<>();

    public XmlImport() {
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<XmlItem> getItems() {
        return items;
    }

    @XmlRootElement(name = "xml-imports")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XmlImports extends XmlImport {

        @XmlElement(name = "xml-import")
        private final List<XmlImport> imports = new ArrayList<>();

        public List<XmlImport> getXmlImports() {
            return imports;
        }

    }
}
