/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris.heithecker
 */
public class LSchB {

    public static final String AUTHORITY = "mk.niedersachsen.de";
    public static final ProviderInfo PROVIDER_INFO = new Info();

    private final static class Info implements ProviderInfo {

        @Override
        public String getURL() {
            return "mk.niedersachsen.de";
        }

        @Override
        public String getDisplayName() {
            return "Niedersachsen";
        }

    }
}
