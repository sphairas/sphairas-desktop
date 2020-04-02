/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.modules.OnStart;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.thespheres.betula.Unit;

/**
 *
 * @author boris.heithecker
 */
public class UIUtilities {

    static boolean TABNAMES_HTML = Boolean.parseBoolean(System.getProperty("nb.tabnames.html", "true"));

//    static {
//        DataNode.setShowFileExtensions(false);
//    }
    private UIUtilities() {
    }

    public static String annotateName(String label, boolean html, boolean modified, boolean readOnly) {
        Parameters.notNull("original", label);
        if (html && TABNAMES_HTML) {
            if (label.startsWith("<html>")) {
                label = label.substring(6);
            }
            if (modified) {
                label = "<b>" + label + "</b>";
            }
            if (readOnly) {
                label = "<i>" + label + "</i>";
            }
            return "<html>" + label;
        } else {
            if (html && !label.startsWith("<html>")) {
                label = "<html>" + label;
            }
            int version = modified ? (readOnly ? 2 : 1) : (readOnly ? 0 : 3);
            try {
                return NbBundle.getMessage(DataObject.class, "LAB_EditorName", version, label);
            } catch (IllegalArgumentException iae) {
                Logger.getLogger(UIUtilities.class.getCanonicalName()).log(Level.WARNING, "Label formatting failed.", iae);
                return label;
            }
        }
    }

    public static String findDisplayName(DataObject dob) {
        final Node n = dob.getNodeDelegate();
        final Unit unit = dob.getLookup().lookup(Unit.class);
        final String ret = n.getDisplayName();
        if (unit != null) {
            return unit.getDisplayName() + ": " + ret;
        } else {
            return ret;
        }
    }

    @OnStart
    public final static class InitDataNode implements Runnable {

        @Override
        public void run() {
            DataNode.setShowFileExtensions(false);
        }

    }
}
