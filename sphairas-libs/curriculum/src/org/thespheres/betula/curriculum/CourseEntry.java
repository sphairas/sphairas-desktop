/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum;

import java.util.List;
import org.thespheres.betula.document.model.MultiSubject;

/**
 *
 * @author boris.heithecker
 */
public interface CourseEntry {

    public String getId();

    public String getName();

    public int getPosition();

    public void setName(String name);

    public void setPosition(int pos);

    public CourseGroup getGroup();

    public MultiSubject getSubject();

    public void setSubject(MultiSubject subject);

    public List<CourseDetail> getDetails();

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();

}
