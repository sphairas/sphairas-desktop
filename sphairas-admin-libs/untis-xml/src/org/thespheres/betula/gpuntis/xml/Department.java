//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.21 at 08:09:46 PM CEST 
//
package org.thespheres.betula.gpuntis.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Department {

    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    protected String longname;
    protected String foreignkey;

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the value of the longname property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getLongname() {
        return longname;
    }

    /**
     * Gets the value of the foreignkey property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getForeignkey() {
        return foreignkey;
    }

    /**
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DepartmentRef {

        @XmlAttribute(name = "id", required = true)
        @XmlIDREF
        protected Department department;

        /**
         * Gets the value of the id property.
         *
         * @return possible object is {@link Object }
         *
         */
        public Department get() {
            return department;
        }

    }
}
