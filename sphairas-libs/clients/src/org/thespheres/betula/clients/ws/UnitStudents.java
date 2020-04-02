/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.ws;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Students;
import org.thespheres.ical.builder.VCardBuilder;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.services.ui.util.dav.MultiStatusSupport;
import org.thespheres.betula.services.dav.AddressData;
import org.thespheres.betula.services.dav.CardDavProp;
import org.thespheres.betula.services.dav.Multistatus;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.StudentComparator;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.VCard;

/**
 *
 * @author boris.heithecker
 */
class UnitStudents extends Students<VCardStudentExt> {

    private final SortedSet<VCardStudentExt> students;
    private final WebServiceProvider provider;
    private final String url;

    UnitStudents(WebServiceProvider prov, String url) {
        students = new TreeSet<>(new StudentComparator());
        this.provider = prov;
        this.url = url;
    }

    @Override
    public Set<VCardStudentExt> getStudents() {
        return students;
    }

    void addStudent(StudentId studentId) {
        VCardStudentExt ret = new VCardStudentExt(studentId);
        students.add(ret);
    }

    void initialize() throws IOException {
        final Multistatus ms = MultiStatusSupport.fetchMultistatus(provider, url);
        final List<String> l = ms.getResponses().stream()
                .flatMap(r -> r.getPropstat().stream())
                .filter(ps -> ps.getStatusCode() == HttpStatus.SC_OK)
                .map(ps -> ps.getProp())
                .filter(CardDavProp.class::isInstance)
                .map(CardDavProp.class::cast)
                .map(CardDavProp::getAddressData)
                .map(AddressData::getValue)
                .collect(Collectors.toList());
        for (String vc : l) {
            try {
                final List<VCard> c = VCardBuilder.parseCards(new StringReader(vc));
                for (VCard card : c) {
                    StudentId id = VCardStudent.extractStudentId(card);
                    VCardStudent vs = find(id);
                    if (vs != null) {
                        vs.setVCard(card);
                    }
                }
            } catch (ParseException | InvalidComponentException ex) {
                throw new IOException(ex);
            }

        }
    }

}
