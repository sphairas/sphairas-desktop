/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ical;

import org.openide.util.Lookup;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.ical.ICalendar;
import org.thespheres.ical.builder.CalendarResourceType;

/**
 *
 * @author boris.heithecker
 */
public interface CalendarResourceProvider {

    public CalendarResourceType getType();

    public ICalendar getCalendar();

    public ProviderInfo getProviderInfo();

    public static CalendarResourceProvider find(final String url) throws NoProviderException {
        return Lookup.getDefault().lookupAll(CalendarResourceProvider.class).stream()
                .map(CalendarResourceProvider.class::cast)
                .filter(wp -> wp.getProviderInfo().getURL().equals(url))
                .collect(CollectionUtil.requireSingleton())
                .orElseThrow(() -> new NoProviderException(SchemeProvider.class, url));
    }
}
