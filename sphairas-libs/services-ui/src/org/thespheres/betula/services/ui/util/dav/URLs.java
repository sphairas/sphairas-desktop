/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util.dav;

import org.thespheres.betula.services.ui.ConfigurationException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.LocalProperties;

/**
 *
 * @author boris.heithecker
 */
public class URLs {

    public static final String HOST = "host";
    public static final String ADMINURLS = "admin.urls";
    public static final String RESOURCES_DAV_BASE = "resources.dav.base";
    public static final String ADMIN_BASE = "admin.base.url";
    public static final String CALENDAR_BASE = "calendarBaseURL";
    public static final String STUDENTS = "studentsUrl";
    public static final String REPORTS = "zgnsrvUrl";

    @NbBundle.Messages("provider.name.info=https://{0}:8181/admins/web/resource/provider/name")
    public static String providerName(final LocalProperties prop) throws ConfigurationException {
        final String host = prop.getProperty(HOST);
        if (host == null) {
            throw new ConfigurationException(prop, HOST);
        }
        return NbBundle.getMessage(URLs.class, "provider.name.info", host);
    }

    @Messages("admin.base.url=https://{0}:8181/admins/")
    public static String adminBase(final LocalProperties prop) throws ConfigurationException {
        String davBase = prop.getProperty(ADMIN_BASE);
        if (davBase == null) {
            final String host = prop.getProperty(HOST);
            if (host != null) {
                davBase = NbBundle.getMessage(URLs.class, "admin.base.url", host);
            }
        }
        if (davBase == null) {
            throw new ConfigurationException(prop, ADMIN_BASE, HOST);
        }
        return davBase;
    }

    @Messages("resources.dav.base.url=https://{0}:8181/admins/web/dav/")
    public static String adminResourcesDavBase(final LocalProperties prop) throws ConfigurationException {
        String davBase = prop.getProperty(RESOURCES_DAV_BASE);
        if (davBase == null) {
            final String host = prop.getProperty(HOST);
            if (host != null) {
                davBase = NbBundle.getMessage(URLs.class, "resources.dav.base.url", host);
            }
        }
        if (davBase == null) {
            throw new ConfigurationException(prop, RESOURCES_DAV_BASE, HOST);
        }
        return davBase;
    }

    @Messages("calendar.base.url=https://{0}:8181/admins/calendar/")
    public static String adminCalendarBase(final LocalProperties prop) throws ConfigurationException {
        String davBase = prop.getProperty(CALENDAR_BASE);
        if (davBase == null) {
            final String host = prop.getProperty(HOST);
            if (host != null) {
                davBase = NbBundle.getMessage(URLs.class, "calendar.base.url", host);
            }
        }
        if (davBase == null) {
            throw new ConfigurationException(prop, CALENDAR_BASE, HOST);
        }
        return davBase;
    }

    @Messages({"students.url=https://{0}/calendar/students",
        "admin.students.url=https://{0}:8181/admins/calendar/students"})
    public static String students(final LocalProperties prop) throws ConfigurationException {
        String p = prop.getProperty(STUDENTS);
        if (p == null) {
            final String host = prop.getProperty(HOST, prop.getProperty(HOST));
            if (host != null) {
                final boolean admin = Boolean.valueOf(prop.getProperty(ADMINURLS));
                p = admin ? NbBundle.getMessage(URLs.class, "admin.students.url", host)
                        : NbBundle.getMessage(URLs.class, "students.url", host);
            }
        }
        if (p == null) {
            throw new ConfigurationException(prop, STUDENTS, HOST);
        }
        return p;
    }

    @Messages({"reports.url=https://{0}/web/zgnsrv/",
        "admin.reports.url=https://{0}:8181/admins/web/zgnsrv/"})
    public static String reports(final LocalProperties prop) throws ConfigurationException {
        String p = prop.getProperty(REPORTS);
        if (p == null) {
            final String host = prop.getProperty(HOST, prop.getProperty(HOST));
            if (host != null) {
                final boolean admin = Boolean.valueOf(prop.getProperty(ADMINURLS));
                p = admin ? NbBundle.getMessage(URLs.class, "admin.reports.url", host)
                        : NbBundle.getMessage(URLs.class, "reports.url", host);
            }
        }
        if (p == null) {
            throw new ConfigurationException(prop, REPORTS, HOST);
        }
        return p;
    }
}
