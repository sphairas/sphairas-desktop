/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb.users.ui;

import java.io.IOException;
import java.nio.file.Path;
import org.thespheres.betula.couchdb.users.impl.GenDBTask;
import org.thespheres.betula.couchdb.users.impl.Util;
import org.thespheres.betula.listprint.XSLFOException;
import org.thespheres.betula.listprint.Formatter;

/**
 *
 * @author boris.heithecker
 */
class PrintTask implements Runnable {

    private final PDFFac genTask;

    PrintTask(GenDBTask t, Path save) {
        genTask = new PDFFac(t, save);
    }

    @Override
    public void run() {
        try {
            Formatter.getDefault().transform(genTask.createRoot(), genTask.getOutputStream(null), "application/pdf");
        } catch (IOException | XSLFOException ex) {
            ex.printStackTrace(Util.getIO().getErr());
        }
    }

}
