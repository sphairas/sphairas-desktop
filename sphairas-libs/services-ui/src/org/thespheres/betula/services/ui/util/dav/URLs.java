/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util.dav;

import org.thespheres.betula.services.ui.ConfigurationException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.modules.SpecificationVersion;
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
    public static final String PROVIDER_NAME_INFO = "provider.name.info";
    public static final String SERVER_VERSION = "server.version";
    public static final String DETECTED_SERVER_VERSION = "server.detected.version";

    public static boolean serverVersionAtLeast(final LocalProperties prop, final String version) {
        return serverVersionAtLeast(prop.getProperty(DETECTED_SERVER_VERSION, "1"), version);
    }

    public static boolean serverVersionAtLeast(final String actual, final String version) {
        return new SpecificationVersion(actual).compareTo(new SpecificationVersion(version)) >= 0;
    }

    @NbBundle.Messages({"provider.name.info=https://{0}:8181/web/resource/provider/name",
        "provider.name.info.legacy=https://{0}:8181/admins/web/resource/provider/name"})
    public static String providerName(final LocalProperties prop) throws ConfigurationException {
        final String pni = prop.getProperty(PROVIDER_NAME_INFO);
        if (pni == null) {
            final String host = prop.getProperty(HOST);
            if (host == null) {
                throw new ConfigurationException(prop, HOST);
            }
            return NbBundle.getMessage(URLs.class, "provider.name.info", host);
        }
        return pni;
    }

    @NbBundle.Messages({"server.version=https://{0}:8181/web/resource/provider/version",
        "server.version.legacy=https://{0}:8181/admins/web/resource/provider/version"})
    public static String serverVersion(final LocalProperties prop) throws ConfigurationException {
        final String sVersion = prop.getProperty(SERVER_VERSION);
        if (sVersion == null) {
            final String host = prop.getProperty(HOST);
            if (host == null) {
                throw new ConfigurationException(prop, HOST);
            }
            return NbBundle.getMessage(URLs.class, "server.version", host);
        }
        return sVersion;
    }

    @Messages({"admin.base.url=https://{0}:8181/",
        "admin.base.url.legacy=https://{0}:8181/admins/"})
    public static String adminBase(final LocalProperties prop) throws ConfigurationException {
        String davBase = prop.getProperty(ADMIN_BASE);
        if (davBase == null) {
            final String host = prop.getProperty(HOST);
            if (host != null) {
                davBase = NbBundle.getMessage(URLs.class,
                        serverVersionAtLeast(prop, "2.1") ? "admin.base.url" : "admin.base.url.legacy", host);
            }
        }
        if (davBase == null) {
            throw new ConfigurationException(prop, ADMIN_BASE, HOST);
        }
        return davBase;
    }

    @Messages({"resources.dav.base.url=https://{0}:8181/service/api/dav/",
        "resources.dav.base.url.legacy=https://{0}:8181/admins/web/dav/"})
    public static String adminResourcesDavBase(final LocalProperties prop) throws ConfigurationException {
        String davBase = prop.getProperty(RESOURCES_DAV_BASE);
        if (davBase == null) {
            final String host = prop.getProperty(HOST);
            if (host != null) {
                davBase = NbBundle.getMessage(URLs.class,
                        serverVersionAtLeast(prop, "2.1") ? "resources.dav.base.url" : "resources.dav.base.url.legacy", host);
            }
        }
        if (davBase == null) {
            throw new ConfigurationException(prop, RESOURCES_DAV_BASE, HOST);
        }
        return davBase;
    }

    @Messages({"calendar.base.url=https://{0}:8181/service/api/calendar/",
        "calendar.base.url.legacy=https://{0}:8181/admins/calendar/"})
    public static String adminCalendarBase(final LocalProperties prop) throws ConfigurationException {
        String davBase = prop.getProperty(CALENDAR_BASE);
        if (davBase == null) {
            final String host = prop.getProperty(HOST);
            if (host != null) {
                davBase = NbBundle.getMessage(URLs.class,
                        serverVersionAtLeast(prop, "2.1") ? "calendar.base.url" : "calendar.base.url.legacy", host);
            }
        }
        if (davBase == null) {
            throw new ConfigurationException(prop, CALENDAR_BASE, HOST);
        }
        return davBase;
    }

    @Messages({"students.url=https://{0}/service/api/calendar/students",
        "admin.students.url=https://{0}:8181/service/api/calendar/students",
        "students.url.legacy=https://{0}/calendar/students",
        "admin.students.url.legacy=https://{0}:8181/admins/calendar/students"})
    public static String students(final LocalProperties prop) throws ConfigurationException {
        String p = prop.getProperty(STUDENTS);
        if (p == null) {
            final String host = prop.getProperty(HOST, prop.getProperty(HOST));
            if (host != null) {
                final boolean admin = Boolean.valueOf(prop.getProperty(ADMINURLS));
                final String key = admin ? "admin.students.url" : "students.url";
                p = NbBundle.getMessage(URLs.class, serverVersionAtLeast(prop, "2.1") ? key : key + ".legacy", host);
            }
        }
        if (p == null) {
            throw new ConfigurationException(prop, STUDENTS, HOST);
        }
        return p;
    }

    @Messages({"reports.url=https://{0}/web/zgnsrv/",
        "admin.reports.url=https://{0}:8181/web/zgnsrv/",
        "admin.reports.url.legacy=https://{0}:8181/admins/web/zgnsrv/"})
    public static String reports(final LocalProperties prop) throws ConfigurationException {
        String p = prop.getProperty(REPORTS);
        if (p == null) {
            final String host = prop.getProperty(HOST, prop.getProperty(HOST));
            if (host != null) {
                final boolean admin = Boolean.valueOf(prop.getProperty(ADMINURLS));
                p = admin ? NbBundle.getMessage(URLs.class, serverVersionAtLeast(prop, "2.1")
                        ? "admin.reports.url" : "admin.reports.url.legacy", host)
                        : NbBundle.getMessage(URLs.class, "reports.url", host);
            }
        }
        if (p == null) {
            throw new ConfigurationException(prop, REPORTS, HOST);
        }
        return p;
    }
}
