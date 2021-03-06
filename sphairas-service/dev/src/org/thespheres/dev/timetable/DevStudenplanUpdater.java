/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.dev.timetable;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.logging.Level;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openide.util.Exceptions;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.niedersachsen.Faecher;
import org.thespheres.betula.services.calendar.LessonData;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.calendar.LessonTimeData;
import org.thespheres.betula.services.scheme.spi.LessonId;
import org.thespheres.betula.services.scheme.spi.PeriodId;
import org.thespheres.betula.services.ui.util.dav.URLs;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
public class DevStudenplanUpdater {

    final String provider;
    final Signee signee;
    final UnitId unit;
    final Marker[] fach;
    final LessonId lid; // = new LessonId(imp.getTargetDocumentIdBase().getAuthority(), imp.getTargetDocumentIdBase().getId());
    final DocumentId calendar = null;

    public DevStudenplanUpdater(String provider, Signee signee, UnitId unit, Marker[] fach, LessonId lid) {
        this.provider = provider;
        this.signee = signee;
        this.unit = unit;
        this.fach = fach;
        this.lid = lid;
    }

    public static void update() throws IOException {
        final String provider = System.getProperty("dev.provider");
        LocalProperties lp = LocalProperties.find(provider);
        ConfigurableImportTarget cit = ConfigurableImportTarget.find(provider);
        String sigSuffix = cit.getDefaultSigneeSuffix();
        Marker subject = MarkerFactory.find(Faecher.CONVENTION_NAME, "geschichte", null);
        String authority = lp.getProperty("authority");//AppProperties.provider(lp);

        Signee sig = new Signee("iserv.nutzer", sigSuffix, true);
        UnitId unit = new UnitId(authority, "geschichte-2015-b");

        LessonId lid = new LessonId(unit.getAuthority(), unit.getId());

        final LocalTime start = LocalTime.of(8, 0);
        final LocalTime end = LocalTime.of(8, 45);

        final LocalTime start2 = LocalTime.of(9, 45);
        final LocalTime end2 = LocalTime.of(10, 30);

        final PeriodId period = new PeriodId(provider, 1, PeriodId.Version.UNSPECIFIED);
        final PeriodId period2 = new PeriodId(provider, 3, PeriodId.Version.UNSPECIFIED);

        final LessonTimeData timeDi = new LessonTimeData(start, end, DayOfWeek.TUESDAY, period);
        timeDi.setSince(LocalDate.of(2020, Month.AUGUST, 25));
        timeDi.setUntil(LocalDate.of(2020, Month.SEPTEMBER, 25));

        final LessonTimeData timeDo = new LessonTimeData(start2, end2, DayOfWeek.THURSDAY, period2);
        timeDo.setSince(LocalDate.of(2020, Month.AUGUST, 27));
        timeDo.setUntil(LocalDate.of(2020, Month.SEPTEMBER, 27));

        DevStudenplanUpdater su = new DevStudenplanUpdater(provider, sig, unit, new Marker[]{subject}, lid);
        
        su.doUpdate(new LessonTimeData[]{timeDi, timeDo});
    }

    void doUpdate(final LessonTimeData[] times) throws IOException {
        //
        final LessonData ld = new LessonData(lid, LessonData.METHOD_PUBLISH, new UnitId[]{unit}, signee, fach, times);

        final WebProvider wp = WebProvider.find(provider, WebProvider.class);
        final String base = URLs.adminBase(LocalProperties.find(provider));
        final String url = base + "calendar/resource";

        final Client client = ClientBuilder.newBuilder()
                .sslContext(((WebProvider.SSL) wp).getSSLContext())
                .build();

        final WebTarget target = client.target(url);

        final Response resp;
        try {
            resp = target.path("lessons")
                    .path("lesson")
                    //                    .queryParam("calendar-authority", authority)
                    //                    .queryParam("calendar-id", id)
                    //                    .queryParam("calendar-version", DocumentId.Version.LATEST.getVersion())
                    .request()
                    .put(Entity.entity(ld, MediaType.APPLICATION_XML), Response.class);
        } catch (final Exception e) {
            Exceptions.attachSeverity(e, Level.INFO);
            Exceptions.printStackTrace(e);
            return;
        }
        final Response.StatusType statusInfo = resp.getStatusInfo();
        if (statusInfo.getStatusCode() != Response.Status.OK.getStatusCode()) {
            throw new IOException(statusInfo.getReasonPhrase());
        }
    }

}
