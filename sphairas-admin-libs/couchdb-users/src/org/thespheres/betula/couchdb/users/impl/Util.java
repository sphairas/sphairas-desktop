/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb.users.impl;

import java.util.HashMap;
import javax.swing.Action;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author boris.heithecker
 */
public class Util {

    private static final HashMap<String, RequestProcessor> RP = new HashMap<>();
    private static InputOutput io;

    public static RequestProcessor RP(final String host) {
        return RP.computeIfAbsent(host, (String k) -> {
            return new RequestProcessor(host, 1);
        });
    }

    @NbBundle.Messages({"Util.ioTab.title=CouchDB"})
    public static InputOutput getIO() {
        if (io == null) {
//            io = IOProvider.getDefault().getIO(NbBundle.getMessage(ImportUtil.class, "ImportUtil.ioTab.title"), false);
            Action[] ac = new Action[0]; //{new SendAction()};
            io = IOProvider.getDefault().getIO(NbBundle.getMessage(Util.class, "Util.ioTab.title"), ac);
        }
        return io;
    }
}
