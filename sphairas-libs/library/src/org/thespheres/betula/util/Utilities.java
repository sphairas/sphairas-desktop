/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

/**
 *
 * @author boris.heithecker
 */
public class Utilities {

    public static String createId(int index) {
        String result = "";
        for (; index >= 0; index = index / 26 - 1) {
            result = (char) ((char) (index % 26) + 'A') + result;
        }
        return result;
    }

    private Utilities() {
    }
}
