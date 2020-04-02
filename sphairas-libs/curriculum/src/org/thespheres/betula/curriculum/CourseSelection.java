/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum;

import java.util.Optional;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author boris.heithecker
 */
public interface CourseSelection {

    public CourseEntry getCourse();

    public CourseSelectionValue getCourseSelectionValue();

    public void setCourseSelectionValue(CourseSelectionValue value);

    public String getNote();

    public void setNote(String note);

    public <P extends ClientProperty> Optional<P> getClientProperty(String key, Class<P> clz);

    public <P extends ClientProperty> void setClientProperty(String key, P value) throws UnsupportedPropertyTypeException;

    @XmlRootElement(name = "client-property")
    public static class ClientProperty {
    }

}
