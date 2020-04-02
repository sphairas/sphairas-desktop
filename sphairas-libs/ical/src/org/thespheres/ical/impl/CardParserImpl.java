/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import org.thespheres.ical.builder.AbstractComponentBuilder;
import org.thespheres.ical.builder.VCardBuilder;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
public class CardParserImpl extends ParserImpl<VCardBuilder> {

    private final ArrayList<VCardBuilder> items = new ArrayList<>();

    @Override
    public List<VCardBuilder> parse(LineNumberReader text) throws FormatException, IOException, InvalidComponentException {
        parseImpl(text);
        return items;
    }

    @Override
    protected AbstractComponentBuilder addComponent(AbstractComponentBuilder current, String componentName, State state) throws FormatException {
        if (componentName == null || !componentName.equals(VCard.VCARD)) {
            throw new FormatException("      ", state.line, state.index);
        }
        return new VCardBuilder();
    }

    @Override
    protected AbstractComponentBuilder endComponent(AbstractComponentBuilder current, String componentName, State state) throws FormatException {
        // finish the current component
        if (componentName.equals(VCard.VCARD)) {
            items.add((VCardBuilder) current);
        }
        return current.getParent();
    }
}
