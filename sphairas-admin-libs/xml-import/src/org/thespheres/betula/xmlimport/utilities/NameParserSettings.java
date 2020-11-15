/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class NameParserSettings {

    public static final String STRIP_NUM_LEADING = "parser.strip.num.leading";
    public static final String STRIP_NUM_TRAILING = "parser.strip.num.trailing";
    final Map<String, String> properties = new HashMap<>();

    NameParserSettings(final Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    public String prepareSourceUnitName(final String source) {
        String ret = source;
        final String psl = properties.get(STRIP_NUM_LEADING);
        if (psl != null) {
            try {
                final int remove = Integer.parseInt(psl);
                if (remove > 0) {
                    ret = ret.substring(remove);
                }
            } catch (NumberFormatException npe) {
                throw new IllegalArgumentException("Cannot parse " + STRIP_NUM_LEADING);
            }
        }
        final String psr = properties.get(STRIP_NUM_TRAILING);
        if (psr != null) {
            try {
                final int remove = Integer.parseInt(psr);
                if (remove > 0) {
                    ret = ret.substring(0, remove);
                }
            } catch (NumberFormatException npe) {
                throw new IllegalArgumentException("Cannot parse " + STRIP_NUM_TRAILING);
            }
        }
        return ret;
    }

}
