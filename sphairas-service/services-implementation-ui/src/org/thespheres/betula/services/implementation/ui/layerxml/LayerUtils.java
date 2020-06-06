/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.layerxml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.thespheres.betula.services.implementation.ui.build.LayerUpdater;

/**
 *
 * @author boris
 */
public class LayerUtils {

    private static JAXBContext JAXB;

    public static JAXBContext getJAXB() {
        synchronized (LayerUpdater.class) {
            if (JAXB == null) {
                try {
                    JAXB = JAXBContext.newInstance(LayerFileSystemImpl.class);
                } catch (JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return JAXB;
        }
    }

}
