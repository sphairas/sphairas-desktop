/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.Identity;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public interface NamingResolver {

    default public String resolveDisplayName(UnitId unit, Term term) throws IllegalAuthorityException {
        Result res = resolveDisplayNameResult(unit);
        return res.getResolvedName(term);
    }

    default public String resolveDisplayName(Identity id) throws IllegalAuthorityException {
        return resolveDisplayNameResult(id).getResolvedName();
    }

    public Result resolveDisplayNameResult(Identity id) throws IllegalAuthorityException;
    
    default public Map<String, String> properties() {
        return Collections.EMPTY_MAP;
    }

    public static NamingResolver find(final String url) {
//            return Lookup.getDefault().lookupAll(NamingResolver.Provider.class).stream()
//                    .map(NamingResolver.Provider.class::cast)
//                    .filter(wp -> wp.getInfo().getURL().equals(url))
//                    .collect(CollectionUtil.requireSingleton())
//                    .orElseThrow(() -> new NoProviderException(NamingResolver.Provider.class, url));

        final NamingResolver found = Lookup.getDefault().lookupAll(NamingResolver.Provider.class).stream()
                .map(wp -> wp.findNamingResolver(url))
                .filter(Objects::nonNull)
                .collect(CollectionUtil.requireSingleOrNull());
        if (found != null) {
            return found;
        }
        return Lookups.forPath("ProviderServices/NamingResolver").lookupAll(NamingResolver.Provider.class).stream()
                .map(wp -> wp.findNamingResolver(url))
                .filter(Objects::nonNull)
                .collect(CollectionUtil.requireSingleton())
                .orElseThrow(() -> new NoProviderException(NamingResolver.class, url));
    }

    public ProviderInfo getInfo();

    public interface Provider {

        public NamingResolver findNamingResolver(String provider);

    }

    public static abstract class Result {

        public static final String ELEMENT_DEFAULT_NAME = "default.name";
        public static final String HINT_STATIC_NAME = "static.name";
        public static final String HINT_UNRESOLVED = "unresolved";
        
        protected final Map<String, String> elements = new HashMap<>();
        protected final ArrayList<String> hints = new ArrayList<>();
        protected final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

        protected Result(Map<String, String> elements) {
            this.elements.putAll(elements);
        }

        public String getResolvedElement(String elementName) {
            return elements.get(elementName);
        }

        public abstract String getResolvedName(Object... params);

        public void addResolverHint(String hint) {
            hints.add(hint);
        }

        public boolean hasResolverHint(String hint) {
            return hints.contains(hint);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pSupport.removePropertyChangeListener(listener);
        }

    }
}
