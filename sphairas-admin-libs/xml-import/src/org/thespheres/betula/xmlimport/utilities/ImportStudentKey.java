/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.util.LocalDateAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "import-student-key")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ImportStudentKey implements Comparable<ImportStudentKey> {

    private static final Comparator<ImportStudentKey> COMARATOR = Comparator.comparing(o -> o.name + o.dateOfBirth, Collator.getInstance(Locale.GERMANY));
    @XmlValue
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String name;
    @XmlAttribute(name = "date-of-birth")
    private String dateOfBirth;
    @XmlAttribute(name = "date-of-birth-parsed")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate parsed;
    @XmlTransient
    private StudentId studentId;

    //JAXB only
    public ImportStudentKey() {
    }

    public ImportStudentKey(String name, String sourceDateOfBirth, LocalDate parsedDateOfBirth) {
        if (StringUtils.isBlank(name) || sourceDateOfBirth == null || parsedDateOfBirth == null) {
            throw new IllegalArgumentException("Either name, or sourceDateOfBirth or parsedDateOfBirth is null or empty");
        }
        this.name = name;
        this.dateOfBirth = sourceDateOfBirth;
        this.parsed = parsedDateOfBirth;
    }

    //DateOfBirth intentionally left blank/unknown
    public ImportStudentKey(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Name is null or empty");
        }
        this.name = name;
        this.dateOfBirth = null;
        this.parsed = null;
    }

    public StudentId getStudentId() {
        return studentId;
    }

    public void setStudentId(StudentId studentId) {
        this.studentId = studentId;
    }

    public String getSourceName() {
        return name;
    }

    public String getSourceDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDate getDateOfBirth() {
        return parsed;
    }

    @Override
    public int compareTo(ImportStudentKey o) {
        return COMARATOR.compare(this, o);
    }

    boolean compareDateOfBirth(final VCardStudent vcs) {
        return getDateOfBirth() == null ? true : getDateOfBirth().equals(vcs.getDateOfBirth());
    }

    @Override
    public String toString() {
        return name + " (" + (dateOfBirth == null ? "unknown" : dateOfBirth) + ")";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
        return 37 * hash + Objects.hashCode(this.dateOfBirth);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImportStudentKey other = (ImportStudentKey) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.dateOfBirth, other.dateOfBirth);
    }

}
