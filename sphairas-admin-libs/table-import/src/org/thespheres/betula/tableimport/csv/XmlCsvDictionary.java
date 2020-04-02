/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.csv;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "xml-csv-dictionary") //, namespace = "http://www.thespheres.org/xsd/betula/csv-import.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCsvDictionary {

    @XmlAttribute(name = "display-name")
    private String name;
    @XmlElement(name = "entry")
    private Entry[] entries;

    public XmlCsvDictionary() {
    }

    public XmlCsvDictionary(String name, Entry[] entries) {
        this.name = name;
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public Entry[] getEntries() {
        return entries;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Entry {

        @XmlAttribute(name = "assignable-key", required = true)
        private String assignedKey;
        @XmlAttribute(name = "label", required = true)
        private String label;
        @XmlAttribute(name = "grouping-key")
        private Boolean isGroupingKey;
        @XmlValue
        private String value;

        public Entry() {
        }

        public Entry(String assignedKey, String value) {
            this.assignedKey = assignedKey;
            this.value = value;
        }

        public Entry(String assignedKey, String value, String label) {
            this.assignedKey = assignedKey;
            this.value = value;
            this.label = label;
        }

        public String getAssignedKey() {
            return assignedKey;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public boolean isIsGroupingKey() {
            return isGroupingKey != null && isGroupingKey;
        }

        public void setIsGroupingKey(boolean isGroupingKey) {
            this.isGroupingKey = isGroupingKey;
        }

    }
}
