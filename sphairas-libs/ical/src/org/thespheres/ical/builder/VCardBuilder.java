/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.builder;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.VCard;
import org.thespheres.ical.impl.CardParserImpl;
import org.thespheres.ical.impl.ComponentPropertyImpl;
import org.thespheres.ical.impl.FormatException;
import org.thespheres.ical.impl.VCardImpl;

/**
 *
 * @author boris.heithecker
 */
public class VCardBuilder extends AbstractComponentBuilder {

    private final VCardImpl vCard = new VCardImpl();

    public VCardBuilder() {
        super(VCard.VCARD);
    }

    public static List<VCard> parseCards(Reader source) throws ParseException, IOException, InvalidComponentException {
        List<VCardBuilder> builders = parseCardsToBuilder(source);
        return builders.stream()
                .map(VCardBuilder::toVCard)
                .collect(Collectors.toList());
    }

    public static List<VCardBuilder> parseCardsToBuilder(Reader source) throws ParseException, IOException, InvalidComponentException {
//        InputStreamReader r = new InputStreamReader(source.openStream());
        CardParserImpl parser = new CardParserImpl();
        try {
            return parser.parse(new LineNumberReader(source));
        } catch (FormatException ex) {
            throw new ParseException(ex.getLine(), ex.getOffset());
        }
    }

    @Override
    public VCardBuilder addProperty(String name, String value, Parameter... parameter) throws InvalidComponentException {
        if (checkValue(name, value)) {
            ComponentPropertyImpl ret = vCard.createProperty(name, value);
            for (Parameter p : parameter) {
                if (p != null && checkValue(p.getName(), p.getValue())) {
                    ret.addParameter(p.getName(), p.getValue());
                }
            }
        }
        return this;
    }

    public VCard toVCard() {
        return vCard;
    }

    private boolean checkValue(String name, String value) throws InvalidComponentException {
        if (name == null || name.isEmpty()) {
            throw new InvalidComponentException("Name must not be null or empty");
        }
        return value != null && !value.isEmpty();
    }

}
