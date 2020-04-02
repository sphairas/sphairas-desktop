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
 * @param <P>
 */
public interface IComponent<P extends ComponentProperty> {

    public static final String BEGIN = "BEGIN";
    public static final String END = "END";

    public String getName();

    public P getAnyProperty(String name);

    public Optional<String> getAnyPropertyValue(String name);

    public List<P> getProperties();

    public Set<String> getPropertyNames();

    public List<P> getProperties(String name);

    public void validate() throws InvalidComponentException;

    //Meine eigene
    @Override
    public String toString();

}
