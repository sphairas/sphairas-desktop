/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author boris.heithecker
 */
public interface ComponentProperty {

    public String getName();

    public String getValue();

    public Optional<String> getAnyParameter(String name);

    public List<Parameter> getParameters();

    public Set<String> getParameterNames();

    public List<Parameter> getParameters(String name);

    @Override
    public String toString();
}
