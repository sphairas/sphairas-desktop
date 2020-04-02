/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl.calendar;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.icalendar.ComponentNode;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.Terms;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.ical.CalendarComponent;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
class ZeugnisKonferenzNode2 extends ComponentNode {

    private final ZeugnisCalendarLookup2 context;
    private Optional<String> summary;

    ZeugnisKonferenzNode2(final CalendarComponent event, final ZeugnisCalendarLookup2 calendarLookup) {
        super(event);
        this.context = calendarLookup;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        context.remove(event.getUID());
    }

    @Override
    protected Optional<String> getSummary() {
        if (summary == null) {
            summary = findSummary();
        }
        return summary;
    }

    private Optional<String> findSummary() {
        final UnitId unit = IComponentUtilities.parseUnitId(event);
        final TermId tid = IComponentUtilities.parseTermId(event);
        final Set<String> cat = IComponentUtilities.parseCategories(event);
        final List<DocumentId> report = IComponentUtilities.parseDocumentIds(event);
        final StringJoiner sj = new StringJoiner(" ");
        event.getAnyPropertyValue(CalendarComponentProperty.SUMMARY).ifPresent(sj::add);
        if (unit != null) {
            try {
                final NamingResolver.Result res = context.support.findNamingResolver()
                        .resolveDisplayNameResult(unit);
                final String name;
                if (tid != null) {
                    final Term term = Terms.forTermId(tid);
                    name = res.getResolvedName(term);
                } else {
                    name = res.getResolvedName();
                }
                sj.add(name);
            } catch (IOException | IllegalAuthorityException ex) {
            }
        }
        if (cat.contains("abschlusszeugnisse")) {
            sj.add("Abschlusszeugnisse");
        }
        if (!report.isEmpty()) {
            final String rr = report.stream()
                    .map(DocumentId::getId)
                    .collect(Collectors.joining(","));
            sj.add(rr);
        }
        return Optional.of(sj.toString());
    }

}
