/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.io.Externalizable;
import java.util.List;
import java.util.stream.Collectors;
import org.thespheres.ical.CardComponentProperty;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
public class VCardImpl extends ComponentImpl<ComponentPropertyImpl> implements Externalizable, VCard {

    private static final long serialVersionUID = 1L;

    public VCardImpl() {
        super(VCard.VCARD);
    }

    @Override
    public ComponentPropertyImpl createProperty(String name, String value) {
        ComponentPropertyImpl ret = new ComponentPropertyImpl(name, value);
        properties.add(ret);
        return ret;
    }

    @Override
    public List<CardComponentProperty> getProperties(String name) {
        return properties.stream().filter((p) -> name.equals(p.getName())).map(i -> (CardComponentProperty) i).collect(Collectors.toList());
    }

    @Override
    public List<CardComponentProperty> getProperties() {
        return properties.stream().map(i -> (CardComponentProperty) i).collect(Collectors.toList());
    }

    @Override
    public String getFN() throws IllegalArgumentException {
        return getAnyPropertyValue(VCard.FN).orElseThrow(() -> {
            return new IllegalArgumentException("FN property must be set.");
        });
    }

    @Override
    public void validate() throws InvalidComponentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
