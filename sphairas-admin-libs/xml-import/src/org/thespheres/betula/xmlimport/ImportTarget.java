/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport;

import java.io.IOException;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.SchemeProvider;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.model.Product;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;

/**
 *
 * @author boris.heithecker
 */
public interface ImportTarget {

    public Product getProduct();

    public ProviderInfo getProviderInfo();

    public NamingResolver getNamingResolver();

    public SchemeProvider getTermSchemeProvider();

    public WebServiceProvider getWebServiceProvider();

    public TargetDocumentProperties[] createTargetDocuments(ImportTargetsItem lesson);

    default public String getSourceTargetLinksConfigFile(Term term) {
        return null;
    }

    default public String getSourceTargetLinksWebDavUrl(Term term) throws IOException {
        return null;
    }

    public String getAuthority();

}
