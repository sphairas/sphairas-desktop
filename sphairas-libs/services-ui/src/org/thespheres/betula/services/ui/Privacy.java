/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/**
 *
 * @author boris.heithecker
 */
public final class Privacy {

    public static final String IS_PRIVACY_NOTICE_KEY = "user.privacy.okay";
    private static Privacy instance;
    private Preferences node;

    private Privacy() {
    }

    private static Privacy instance() {
        synchronized (Privacy.class) {
            if (instance == null) {
                instance = new Privacy();//new InstallKeyStores()
            }
        }
        return instance;
    }

    public static void okay() throws IllegalStateException {
        final String sl = getSystemLoggedInUser();
        Logger.getLogger(Privacy.class.getName()).log(Level.INFO, sl);
        if (!instance().privacy()) {
            throw new IllegalStateException();
        }
    }

    private boolean privacy() {
        return node().get(IS_PRIVACY_NOTICE_KEY, "").equals("yes");
    }

    private Preferences node() {
        if (node == null) {
            node = NbPreferences.forModule(Privacy.class);
        }
        return node;
    }

    static String getSystemLoggedInUser() throws IllegalStateException {
        ClassLoader sysCl = Lookup.getDefault().lookup(ClassLoader.class);
        try {
            String n;
            if (Utilities.isWindows()) {
                Class<?> clz = Class.forName("com.sun.security.auth.module.NTSystem", true, sysCl);
                Method m = clz.getDeclaredMethod("getName");
                n = (String) m.invoke(clz.newInstance());
            } else {
                Class<?> clz = Class.forName("com.sun.security.auth.module.UnixSystem", true, sysCl);
                Method m = clz.getDeclaredMethod("getUsername");
                n = (String) m.invoke(clz.newInstance());
            }
            if (StringUtils.trimToNull(n) == null) {
                throw new IllegalStateException();
            }
            return n;
        } catch (IllegalArgumentException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | ClassCastException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
