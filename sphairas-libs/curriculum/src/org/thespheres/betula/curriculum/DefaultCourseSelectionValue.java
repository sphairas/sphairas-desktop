/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.Lookup;
import org.thespheres.betula.Convention;
import org.thespheres.betula.curriculum.util.CurriculumTableActions;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.util.MarkerAdapter.XmlMarkerAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefaultCourseSelectionValue extends CourseSelectionValue {

    @XmlAttribute(name = "num-lessons")
    private Integer numLessons;
    @XmlElement(name = "alternative-entry")
    @XmlJavaTypeAdapter(XmlMarkerAdapter.class)
    private Marker option;

    public Integer getNumLessons() {
        return numLessons;
    }

    public void setNumLessons(Integer num) {
        numLessons = num;
        option = null;
    }

    public Marker getOption() {
        return option;
    }

    public void setOption(Marker alternative) {
        option = alternative;
        numLessons = null;
    }

    @Override
    public String toString(final Lookup context) {
        if (numLessons != null && option == null && numLessons > 0) {
            return Integer.toString(numLessons);
        } else if (option != null && numLessons == null) {
            return option.getLongLabel();
        }
        return "---";
    }

    public Convention[] getOptionsConventions(final Lookup context) {
        final CurriculumTableActions ac = context.lookup(CurriculumTableActions.class);
        if(ac != null) {
            ac.getLocalProperties();
            ac.getDataObject();
//            cur.getGeneral()
        }
        return new MarkerConvention[]{MarkerFactory.findConvention("kgs.schulzweige")};
    }

}
