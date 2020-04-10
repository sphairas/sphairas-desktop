/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.config;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.thespheres.betula.adminconfig.ConfigurationRegistration;
import org.thespheres.betula.niedersachsen.xml.NdsZeugnisSchulvorlage;
import org.thespheres.betula.niedersachsen.zeugnis.NdsReportBuilderFactory;

/**
 *
 * @author boris
 */
public class Resources {

    static JAXBContext JAXB_VORLAGE;

    private Resources() {
    }

    private synchronized static JAXBContext getJAXB_Vorlage() {
        if (JAXB_VORLAGE == null) {
            try {
                JAXB_VORLAGE = JAXBContext.newInstance(NdsZeugnisSchulvorlage.class);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return JAXB_VORLAGE;
    }

    @ConfigurationRegistration(resource = "schulvorlage.xml")
    public static NdsReportBuilderFactory createNdsReportBuilderFactory(final InputStream is) throws IOException {
        final NdsZeugnisSchulvorlage vorlage;
        try {
            vorlage = (NdsZeugnisSchulvorlage) getJAXB_Vorlage().createUnmarshaller().unmarshal(is);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
        return new NdsReportBuilderFactory(vorlage);
    }
}
