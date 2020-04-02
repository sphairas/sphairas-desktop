/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.xml;

import org.thespheres.betula.termreport.XmlAssessmentProviderData;
import javax.xml.bind.annotation.*;
import org.thespheres.betula.termreport.*;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "term-report", namespace = "http://www.thespheres.org/xsd/betula/term-report.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTermReport {

    //Referenzen auf die Dateien und Datenbank Targets    
    @XmlElementWrapper(name = "references")
    @XmlElementRef
    private XmlAssessmentProviderData[] references;
    @XmlElementWrapper(name = "notes")
    @XmlElementRef
    private TermReport.Note[] annot;

    public XmlAssessmentProviderData[] getReferences() {
        return references != null ? references : new XmlAssessmentProviderData[0];
    }

    public void setReferences(XmlAssessmentProviderData[] references) {
        this.references = references != null && references.length != 0 ? references : null;
        if (this.references != null) {
            int i = 0;
            for (XmlAssessmentProviderData d : this.references) {
                d.setPosition(i++);
            }
        }
    }

    public TermReport.Note[] getNotes() {
        return annot != null ? annot : new TermReport.Note[0];
    }

    public void setNotes(XmlNote[] annot) {
        this.annot = annot != null && annot.length != 0 ? annot : null;
    }

}
