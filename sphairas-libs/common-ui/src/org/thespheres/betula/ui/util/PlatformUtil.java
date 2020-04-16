/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ToolTipManager;
import org.openide.modules.Modules;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.OnShowing;

/**
 *
 * @author boris.heithecker
 */
public class PlatformUtil {

    public static final String KEY_PERFORMANCE_LOGGING_ENABLED = "performance-logging-enabled";
    static final String LOGGER_NAME = "performance-logger";
    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

    private PlatformUtil() {
    }

    public static Logger getCodeNameBaseLogger(Class<?> clz) {
        return Logger.getLogger(Modules.getDefault().ownerOf(clz).getCodeNameBase());
    }

    @Messages({"PlatformUtil.logPerformance=Time for {0}: {1}"})
    public static void logPerformance(final String resource, final long millis) {
        final boolean enabled = NbPreferences.forModule(PlatformUtil.class).getBoolean(KEY_PERFORMANCE_LOGGING_ENABLED, false);
        final Level level = enabled ? Level.INFO : Level.FINE;
        LOGGER.log(level, NbBundle.getMessage(PlatformUtil.class, "PlatformUtil.logPerformance", resource, Long.toString(millis)));
    }

    @OnShowing
    public static class SetToolTipTime implements Runnable {

        @Override
        public void run() {
            ToolTipManager.sharedInstance().setDismissDelay(12000);
        }

    }

}
