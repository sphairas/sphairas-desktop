/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbPreferences;

/**
 *
 * @author boris
 */
public class JMSEvents {

    public static final String KEY_LOGGING_ENABLED = "jms-logging-enabled";
    static final String LOGGER_NAME = "jms-event-logger";
    private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

    private JMSEvents() {
    }

    private static Level level() {
        final boolean enabled = NbPreferences.forModule(JMSEvents.class).getBoolean(KEY_LOGGING_ENABLED, false);
        return enabled ? Level.INFO : Level.FINE;
    }

    public static void log(final JMSEvent event) {
        final Level level = level();
        LOGGER.log(level, event.toString());
    }
}
