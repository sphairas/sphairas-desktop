/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.util.logging.Level;

/**
 *
 * @author boris.heithecker
 */
public class LogLevel extends Level {

    public static final Level INFO_WARNING = new LogLevel("INFO_WARNING", 850);

    private LogLevel(String name, int value) {
        super(name, value);
    }

}
