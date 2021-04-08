package org.thespheres.betula.niedersachsen.ferien;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.MissingResourceException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.scheme.spi.Scheme;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.ExemptDatesScheme;

/*
 *@author boris.heithecker
 */
@ServiceProvider(service = SchemeProvider.class)
public class NiedersachsenHolidayProvider implements SchemeProvider {

    private final SimpleExScheme2 schedule;
    public static final ProviderInfo PROVIDER_INFO = new Info();

    @Messages({"NiedersachsenHolidayProvider.init.error=Could not create service provider NiedersachsenHolidayProvider."})
    public NiedersachsenHolidayProvider() throws IllegalStateException {
        try {
            final URL url = NiedersachsenHolidayProvider.class.getResource("ferien_niedersachsen.ics");
            this.schedule = new SimpleExScheme2("niedersachsen.ferien", url);
        } catch (IOException | MissingResourceException ex) {
            final String msg = NbBundle.getMessage(NiedersachsenHolidayProvider.class, "NiedersachsenHolidayProvider.init.error");
            throw new IllegalStateException(msg, ex);
        }
    }

    @Override
    public ProviderInfo getInfo() {
        return PROVIDER_INFO;
    }

    @Override
    public <G extends Scheme> G[] getAllSchemes(Class<G> type) {
        G ret = getScheme(ExemptDatesScheme.HOLIDAYS, type);
        G[] arr = (G[]) Array.newInstance(type, ret == null ? 0 : 1);
        if (ret != null) {
            arr[0] = ret;
        }
        return arr;
    }

    @Override
    public <G extends Scheme> G getScheme(String id, Class<G> type) {
        if (id != null && ExemptDatesScheme.HOLIDAYS.equals(id) && type.isAssignableFrom(ExemptDatesScheme.class)) {
            try {
                return type.cast(schedule);
            } catch (ClassCastException ex) {
            }
        }
        return null;
    }

    private final static class Info implements ProviderInfo {

        @Override
        public String getURL() {
            return "x.mk.niedersachsen.de";
        }

        @Override
        public String getDisplayName() {
            return "Niedersachsen";
        }

    }
}
