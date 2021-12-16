/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import java.util.Collections;
import java.util.Map;
import org.thespheres.betula.Identity;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ProviderRegistry;
import org.thespheres.betula.services.NamingResolver;

/**
 *
 * @author boris.heithecker
 */
public abstract class NdsNamingResolverProvider implements NamingResolver.Provider {

    final NdsNaming naming;
    final Resolver resolver;

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected NdsNamingResolverProvider(final String prov) {
        this(prov, null, 5, 11, false);
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected NdsNamingResolverProvider(final String prov, final String firstElement, int base, int baseAbi, boolean permitAlternativeSubjects) {
        this.naming = NdsNaming.create(prov, firstElement, base, baseAbi, permitAlternativeSubjects);
        this.resolver = new Resolver(prov);
    }

    public static NdsNamingResolverProvider create(final Map<String, ?> args) {
        final String provider = (String) args.get("provider");
        final String fe = (String) args.get("first-element");
        final String firstElement = (fe == null || fe.trim().isEmpty()) ? null : fe.trim();
        final Integer b = (Integer) args.get("base-level");
        final int base = (b != null && b > 0) ? b : 5;
        final Integer ba = (Integer) args.get("base-level-abitur");
        final Boolean altSub = (Boolean) args.get("permit-alternative-subjects");
        final int baseAbi = (ba != null && ba > 0) ? ba : 11;

        class ProviderImpl extends NdsNamingResolverProvider {

            ProviderImpl() {
                super(provider, firstElement, base, baseAbi, true);//= altSub != null ? altSub : false;
            }

        }
        return new ProviderImpl();
    }

    @Override
    public NamingResolver findNamingResolver(String provider) {
        return provider.equals(resolver.provider) ? resolver : null;
    }

    private class Resolver implements NamingResolver {

        private final String provider;

        private Resolver(String provider) {
            this.provider = provider;
        }

        @Override
        public NamingResolver.Result resolveDisplayNameResult(Identity id) throws IllegalAuthorityException {
            return naming.resolve(id);
        }

        @Override
        public ProviderInfo getInfo() {
            return ProviderRegistry.getDefault().get(provider);
        }

        @Override
        public Map<String, String> properties() {
            return Collections.unmodifiableMap(naming.properties());
        }
    }

}
