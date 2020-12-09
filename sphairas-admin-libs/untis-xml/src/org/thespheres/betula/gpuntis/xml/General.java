package org.thespheres.betula.gpuntis.xml;

import org.thespheres.betula.gpuntis.xml.util.DateAdapter;
import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
    protected String termname;

    public String getSchoolName() {
        return schoolname;
    }

    public Integer getSchoolNumber() {
        return schoolnumber;
    }

    public String getSchoolType() {
        return schooltype;
    }

    public LocalDate getSchoolYearBeginDate() {
        return schoolyearbegindate;
    }

    public LocalDate getSchoolYearEndDate() {
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

    public LocalDate getTermBeginDate() {
        return termbegindate;
    }

    public LocalDate getTermEndDate() {
        return termenddate;
    }

    public String getTermName() {
        return termname;
    }

}
