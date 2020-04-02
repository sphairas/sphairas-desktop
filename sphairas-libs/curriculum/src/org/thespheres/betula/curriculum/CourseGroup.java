/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum;

import java.util.List;
import java.util.Set;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public interface CourseGroup extends CourseEntry {

    public List<CourseEntry> getChildren();

    public <C extends CourseEntry> List<C> getChildren(Class<C> type);

    public <C extends CourseEntry> C addChild(String id, Class<C> type);

    public Set<Marker> getDefinition();

    public String getDescription();

    public void setDescription(String description);
}
