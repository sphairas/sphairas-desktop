package org.thespheres.betula.gpuntis.xml;

import org.thespheres.betula.gpuntis.xml.util.DateAdapter;
import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class General {

    protected String schoolname;
    @XmlElement(name = "schoolnumber")
    protected Integer schoolnumber;
    protected String schooltype;
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlElement(name = "schoolyearbegindate")
    protected LocalDate schoolyearbegindate;
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlElement(name = "schoolyearenddate")
    protected LocalDate schoolyearenddate;
    protected String header1;
    protected String header2;
    protected String footer;
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlElement(name = "termbegindate")
    protected LocalDate termbegindate;
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlElement(name = "termenddate")
    protected LocalDate termenddate;

    public String getSchoolname() {
        return schoolname;
    }

    public Integer getSchoolnumber() {
        return schoolnumber;
    }

    public String getSchooltype() {
        return schooltype;
    }

    public LocalDate getSchoolyearbegindate() {
        return schoolyearbegindate;
    }

    public LocalDate getSchoolyearenddate() {
        return schoolyearenddate;
    }

    public String getHeader1() {
        return header1;
    }

    public String getHeader2() {
        return header2;
    }

    public String getFooter() {
        return footer;
    }

    public LocalDate getTermbegindate() {
        return termbegindate;
    }

    public LocalDate getTermenddate() {
        return termenddate;
    }

}
