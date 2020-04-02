/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.platform;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.Places;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author boris.heithecker
 */
//@OnStart
public class StartUpDiagnostics implements Runnable {

    private org.netbeans.modules.sampler.Sampler sampler;

    @Override
    public void run() {
        final boolean run = NbPreferences.forModule(StartUpDiagnostics.class).getBoolean("sphairas.startup.dump.samples", false);
        if (run) {
            sampler = org.netbeans.modules.sampler.Sampler.createSampler("startup");
            //in debug mode sampling is disabled, works only in run mode!
            if (sampler != null) {
                sampler.start();
                logger().log(Level.INFO, "Started startup sampler.");
                new RequestProcessor().schedule(this::stop, 60, TimeUnit.SECONDS);
            }
        }
    }

    private void stop() {
        logger().log(Level.INFO, "Stopping startup sampler.");
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final DataOutputStream dos = new DataOutputStream(out);
            sampler.stopAndWriteTo(dos);
            sampler = null;
            logger().log(Level.INFO, "Stopped startup sampler. Samples size is {0} bytes.", out.size());
            final Path get = Places.getUserDirectory().toPath().resolve("var/log/startup.npss");
            Files.write(get, out.toByteArray());
        } catch (IOException ex) {
            final String msg = "An error ocurred writing startup samples.";
            logger().log(Level.WARNING, msg, ex);
        }
    }

    private static Logger logger() {
        return Logger.getLogger(StartUpDiagnostics.class.getName());
    }
}
