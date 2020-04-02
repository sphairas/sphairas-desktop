/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.vcard;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.List;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.VCard;
import org.thespheres.ical.builder.VCardBuilder;

/**
 *
 * @author boris.heithecker
 */
public class VCardAdapter extends XmlAdapter<String, VCard> {

    @Override
    public VCard unmarshal(String v) throws Exception {
        final String content = v.trim();
        List<VCard> l = null;
        try {
            l = VCardBuilder.parseCards(new StringReader(content));
        } catch (ParseException | IOException | InvalidComponentException ex) {
            throw new UnmarshalException(ex);
        }
        if (l.size() == 1) {
            return l.get(0);
        } else if (l.isEmpty()) {
            final String msg = "Empty vCard list extracted from: " + content;
            throw new UnmarshalException(msg);
        } else {
            final String msg = "Non unique vCard list extracted from: " + content;
            throw new UnmarshalException(msg);
        }
    }

    @Override
    public String marshal(VCard v) throws Exception {
        return v != null ? v.toString() : null;
    }

}
