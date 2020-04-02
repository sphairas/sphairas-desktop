/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.impl;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 *
 * @author boris.heithecker
 */
public class Util {

    public static String[] idsFromBundle(final ResourceBundle bundle, final Pattern idpattern, final int subStringIndex) {
        return bundle.keySet().stream()
                .filter(key -> idpattern == null || idpattern.matcher(key).matches())
                .map(s -> s.substring(subStringIndex))
                .toArray(String[]::new);
    }

}
