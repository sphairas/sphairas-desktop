/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.impl;

import java.lang.reflect.Array;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.niedersachsen.LSchB;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.scheme.spi.Scheme;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;

/*
 *@author boris.heithecker
 */
@ServiceProvider(service = SchemeProvider.class)
public class TermScheduleProviderImpl implements SchemeProvider {

    SimpleTermSchedule scheme = new SimpleTermSchedule("niedersachsen");

    @Override
    public ProviderInfo getInfo() {
        return LSchB.PROVIDER_INFO;
    }

    @Override
    public <G extends Scheme> G[] getAllSchemes(Class<G> type) {
        G ret = getScheme(null, type);
        G[] arr = (G[]) Array.newInstance(type, ret == null ? 0 : 1);
        if (ret != null) {
            arr[0] = ret;
        }
        return arr;
    }

    @Override
    public <G extends Scheme> G getScheme(String id, Class<G> type) {
        if (id == null || id.isEmpty()) {
            id = Scheme.DEFAULT_SCHEME;
        }
        if (Scheme.DEFAULT_SCHEME.equals(id)) {
            try {
                return type.cast(scheme);
            } catch (ClassCastException ex) {
            }
        }
        return null;
    }
}
