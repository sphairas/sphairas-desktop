package org.thespheres.betula.services.ui.util.dav;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thespheres.betula.services.LocalProperties;

public class URLsTest {

    private static class Properties implements LocalProperties {
        final Map<String, String> values = new HashMap<>();

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public Map<String, String> getProperties() {
            return values;
        }

        @Override
        public String getProperty(String name) {
            return values.get(name);
        }
    }

    @Test
    public void selectsVersionedDefaults() {
        Properties p = new Properties();
        p.values.put(URLs.HOST, "taurus");
        p.values.put(URLs.ADMINURLS, "true");
        assertEquals("https://taurus:8181/web/resource/provider/name", URLs.providerName(p));
        assertEquals("https://taurus:8181/web/resource/provider/version", URLs.serverVersion(p));
        assertFalse(URLs.serverVersionAtLeast(p, "2"));
        for (String version : new String[]{"1", "2", "2.0"}) {
            p.values.put(URLs.DETECTED_SERVER_VERSION, version);
            assertEquals("https://taurus:8181/admins/", URLs.adminBase(p));
            assertEquals("https://taurus:8181/admins/calendar/", URLs.adminCalendarBase(p));
            assertEquals("https://taurus:8181/admins/web/dav/", URLs.adminResourcesDavBase(p));
            assertEquals("https://taurus:8181/admins/calendar/students", URLs.students(p));
            assertEquals("https://taurus:8181/admins/web/zgnsrv/", URLs.reports(p));
            assertFalse(URLs.serverVersionAtLeast(p, "2.1"));
        }
        for (String version : new String[]{"2.1", "2.1.1", "2.10", "3.0"}) {
            p.values.put(URLs.DETECTED_SERVER_VERSION, version);
            assertEquals("https://taurus:8181/", URLs.adminBase(p));
            assertEquals("https://taurus:8181/service/api/dav/", URLs.adminResourcesDavBase(p));
            assertEquals("https://taurus:8181/service/api/calendar/", URLs.adminCalendarBase(p));
            assertEquals("https://taurus:8181/service/api/calendar/students", URLs.students(p));
            assertEquals("https://taurus:8181/web/zgnsrv/", URLs.reports(p));
            assertTrue(URLs.serverVersionAtLeast(p, "2"));
        }
        p.values.put(URLs.ADMINURLS, "false");
        assertEquals("https://taurus/service/api/calendar/students", URLs.students(p));
        assertEquals("https://taurus/web/zgnsrv/", URLs.reports(p));
    }

    @Test
    public void explicitUrlsTakePrecedence() {
        Properties p = new Properties();
        p.values.put(URLs.HOST, "taurus");
        p.values.put(URLs.DETECTED_SERVER_VERSION, "2.1");
        p.values.put(URLs.SERVER_VERSION, "https://custom/version");
        p.values.put(URLs.PROVIDER_NAME_INFO, "https://custom/name");
        p.values.put(URLs.ADMIN_BASE, "https://custom/");
        p.values.put(URLs.RESOURCES_DAV_BASE, "https://custom/dav/");
        p.values.put(URLs.CALENDAR_BASE, "https://custom/calendar/");
        p.values.put(URLs.STUDENTS, "https://custom/students");
        p.values.put(URLs.REPORTS, "https://custom/reports/");
        assertEquals("https://custom/version", URLs.serverVersion(p));
        assertEquals("https://custom/name", URLs.providerName(p));
        assertEquals("https://custom/", URLs.adminBase(p));
        assertEquals("https://custom/dav/", URLs.adminResourcesDavBase(p));
        assertEquals("https://custom/calendar/", URLs.adminCalendarBase(p));
        assertEquals("https://custom/students", URLs.students(p));
        assertEquals("https://custom/reports/", URLs.reports(p));
    }
}
