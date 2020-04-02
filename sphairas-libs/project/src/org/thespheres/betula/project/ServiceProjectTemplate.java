/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project;

import java.io.IOException;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.thespheres.betula.project.ServiceProjectTemplate.Selection;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;

/**
 *
 * @author boris.heithecker
 * @param <E>
 */
public abstract class ServiceProjectTemplate<E extends Selection> {

    protected final String provider;

    protected ServiceProjectTemplate(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public ProviderInfo getProviderInfo() {
        return ProviderRegistry.getDefault().get(provider);
    }

    public abstract List<E> createList() throws IOException;

    public static interface Selection extends Comparable<Selection> {

        public String getDisplayName();

        public abstract Properties createProjectProperties(final Properties defaults);

        @Override
        default public int compareTo(Selection o) {
            return Collator.getInstance(Locale.getDefault())
                    .compare(getDisplayName(), o.getDisplayName());
        }
    }

    public static interface Provider {

        public <S extends ServiceProjectTemplate> S findTemplate(String provider, Class<S> type);

        public <S extends ServiceProjectTemplate> List<S> findTemplates(Class<S> type);
    }
}
