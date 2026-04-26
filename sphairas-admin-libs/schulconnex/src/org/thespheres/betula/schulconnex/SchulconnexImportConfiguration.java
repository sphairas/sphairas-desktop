/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex;

import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetDocument;
import org.thespheres.betula.document.model.DocumentDefaults;
import org.thespheres.betula.services.CommonTargetProperties;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetFactory;

/**
 * Configuration interface for a Schulconnex import target. Implementations are
 * provided via {@link Factory} registered in the global
 * {@link org.openide.util.Lookup}.
 *
 * @author boris.heithecker
 */
public interface SchulconnexImportConfiguration extends ImportTarget, CommonTargetProperties, DocumentDefaults<Grade, TargetDocument> {

    public static final String SCHULCONNEX_CLIENTSECRET = "schulconnex.client.secret";
    public static final String SCHULCONNEX_CLIENTID = "schulconnex.client.id";
    public static final String SCHULCONNEX_API = "schulconnex.api.endpoint";
    public static final String SCHULCONNEX_TOKEN_ENDPOINT = "schulconnex.token.endpoint";
    public static final String SCHULCONNEX_PERMIT_ALTSUBJECTNAME = "schulconnex.permit.altsubjectnames";
    
    /**
     * Returns the Schulconnex API client ID from the configuration file
     * (schulconnex.properties under key "schulconnex.client.id").
     *
     * @return the client ID, or null if not configured
     */
    String getSchulconnexClientId();

    /**
     * Returns the Schulconnex API client secret from the Java system property
     * "schulconnex.client.secret" (typically passed as a boot parameter).
     *
     * @return the client secret, or null if not configured
     */
    String getSchulconnexClientSecret();

    /**
     * Returns the Schulconnex API endpoint URL from the configuration file
     * (schulconnex.properties under key "schulconnex.api.endpoint").
     *
     * @return the API endpoint URL, or null if not configured
     */
    String getSchulconnexApiEndpoint();

    /**
     * Returns the Schulconnex token endpoint URL from the configuration file
     * (schulconnex.properties under key "schulconnex.token.endpoint").
     *
     * @return the token endpoint URL, or null if not configured
     */
    String getSchulconnexTokenEndpoint();

    public boolean permitAltSubjectNames();

    public String getDefaultSigneeSuffix();

    public static abstract class Factory extends ImportTargetFactory<SchulconnexImportConfiguration> {

        protected Factory() {
            super(Schulconnex.getProduct(), SchulconnexImportConfiguration.class);
        }
    }
}
