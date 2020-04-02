/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.web.ContextCredentials;

/**
 *
 * @author boris.heithecker
 */
public class WebUtil {

    private WebUtil() {
    }

    public static void resetProvider(WebProvider wsp, Exception ex) {
        if (wsp instanceof ContextCredentials.Provider) {
            ((ContextCredentials.Provider) wsp).getContextCredentials().onFailure(null, ex);
        }
    }
}
