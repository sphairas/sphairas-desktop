/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.impl;

import java.io.File;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
//        String sys = System.getProperty("java.home");
//        String sys2 = System.getProperty("derby.home");
        ProjectChooser.setProjectsFolder(new File(System.getProperty("user.home")));
    }
}
