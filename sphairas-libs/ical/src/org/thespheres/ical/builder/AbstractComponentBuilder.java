/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.builder;

import org.thespheres.ical.ComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractComponentBuilder {

    public static final String NEWLINE = "\r\n"; //String str = "......\r\n";
    private final String name;

    protected AbstractComponentBuilder(String name) {
        this.name = name;
    }

    public abstract AbstractComponentBuilder addProperty(String name, String value, Parameter... parameter) throws InvalidComponentException;

    public String getName() {
        return name;
    }

    public AbstractComponentBuilder getParent() {
        return null;
    }

    public AbstractComponentBuilder mergeProperty(ComponentProperty other) throws InvalidComponentException {
        return addProperty(other.getName(), other.getValue(), other.getParameters().stream().toArray(Parameter[]::new));
    }

}
