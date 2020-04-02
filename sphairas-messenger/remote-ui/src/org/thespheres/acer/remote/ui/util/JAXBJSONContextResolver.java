/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.util;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;

/**
 *
 * @author boris.heithecker
 */
@Provider
public class JAXBJSONContextResolver implements ContextResolver<MoxyJsonConfig> {

    private final MoxyJsonConfig config;

    public JAXBJSONContextResolver() {
        config = new MoxyJsonConfig()
                //                .setAttributePrefix("")
                .setValueWrapper("value")
                //               .setNamespaceSeparator(0)               
                //                .setNamespacePrefixMapper(namespacePrefixMapper)
                .property(JAXBContextProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
    }

    @Override
    public MoxyJsonConfig getContext(Class<?> objectType) {
        return config;
    }
}
