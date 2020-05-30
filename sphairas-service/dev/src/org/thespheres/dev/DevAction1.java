/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.dev;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.dev.timetable.DevStudenplanUpdater;

@ActionID(
        category = "Dev",
        id = "org.thespheres.dev.DevAction1"
)
@ActionRegistration(
        displayName = "#CTL_DevAction1"
)
@ActionReference(path = "Menu/DEV", position = 3333)
@Messages("CTL_DevAction1=Dev Action 1")
public final class DevAction1 implements ActionListener {

    public static final Logger LOGGER = Logger.getLogger("DEV");

    static {
        System.setProperty(LOGGER.getName() + ".level", "100");
        try {
            LogManager.getLogManager().readConfiguration();
        } catch (IOException | SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            DevStudenplanUpdater.update();
//        try {
////            WebServiceProvider service = WebProvider.find("demo/1", WebServiceProvider.class);
////            BetulaWebService p = service.createServicePort();
////            p.solicit(null);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
