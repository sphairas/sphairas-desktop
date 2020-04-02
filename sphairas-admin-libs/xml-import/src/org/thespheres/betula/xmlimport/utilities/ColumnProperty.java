/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.thespheres.betula.xmlimport.utilities.ColumnProperty.Empty;

@XmlSeeAlso(Empty.class)
public abstract class ColumnProperty {

    public abstract String getColumnId();

    @XmlRootElement(name = "empty")
    @XmlAccessorType(value = XmlAccessType.FIELD)
    static final class Empty extends ColumnProperty {

        Empty() {
            throw new UnsupportedOperationException("Do not use this class. It exists only to satisfy a default implementation for jaxb.");
        }

        @Override
        public String getColumnId() {
            return "empty";
        }

    }

}
