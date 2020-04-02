/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import org.thespheres.betula.Tag;

/**
 *
 * @author boris.heithecker
 */
public interface Marker extends Tag {

    public static final Marker NULL = new NullMarker();

    public static boolean isNull(Marker m) {
        return m == null || m.equals(NULL) || "null".equals(m.getId());
    }

    public String getSubset();
}
