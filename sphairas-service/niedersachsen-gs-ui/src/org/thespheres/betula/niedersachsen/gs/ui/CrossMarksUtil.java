/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.gs.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.thespheres.betula.niedersachsen.NdsCommonConstants;
import org.thespheres.betula.niedersachsen.gs.*;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ui.util.dav.URLs;

/**
 *
 * @author boris.heithecker
 */
public class CrossMarksUtil {

    private static JAXBContext jaxb;

    public static CrossmarkSettings load(final String provider) throws IOException {
        final String url = URLs.adminResourcesDavBase(LocalProperties.find(provider)) + NdsCommonConstants.ANKREUZZEUGNISSE_FILE;
        return HttpUtilities.get(WebProvider.find(provider, WebProvider.class), URI.create(url), (lm, is) -> unmarshal(is), null, true);
    }

    public static CrossmarkSettings load() throws IOException {
        //load from Appresources
        return null;
    }

    private static JAXBContext getJAXB() {
        synchronized (CrossMarksUtil.class) {
            if (jaxb == null) {
                try {
                    jaxb = JAXBContext.newInstance(CrossmarkSettings.class);
                } catch (JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return jaxb;
    }

    private static CrossmarkSettings unmarshal(final InputStream is) throws IOException {
        try {
            return (CrossmarkSettings) getJAXB().createUnmarshaller().unmarshal(is);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }

}
