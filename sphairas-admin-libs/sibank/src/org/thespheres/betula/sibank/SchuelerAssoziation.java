/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.xmlimport.model.XmlStudentItem;
import org.thespheres.betula.xmlimport.utilities.ImportStudentKey;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "sibank-student-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class SchuelerAssoziation extends XmlStudentItem {

    @XmlElement(name = "sibank-student-key")
    private ImportStudentKey key;

    public SchuelerAssoziation() {
    }

    public SchuelerAssoziation(ImportStudentKey key, StudentId student) {
        this.key = key;
        this.student = student;
    }

    public ImportStudentKey getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return 37 * hash + Objects.hashCode(this.key);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchuelerAssoziation other = (SchuelerAssoziation) obj;
        return Objects.equals(this.key, other.key);
    }

}
