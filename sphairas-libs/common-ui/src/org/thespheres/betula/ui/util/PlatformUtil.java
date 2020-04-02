/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.util.logging.Logger;
import javax.swing.ToolTipManager;
import org.openide.modules.Modules;
import org.openide.windows.OnShowing;

/**
 *
 * @author boris.heithecker
 */
public class PlatformUtil {

    public static Logger getCodeNameBaseLogger(Class<?> clz) {
        return Logger.getLogger(Modules.getDefault().ownerOf(clz).getCodeNameBase());
    }

    @OnShowing
    public static class SetToolTipTime implements Runnable {

        @Override
        public void run() {
            ToolTipManager.sharedInstance().setDismissDelay(12000);
        }

    }

}
