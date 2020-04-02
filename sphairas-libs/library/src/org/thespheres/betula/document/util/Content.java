/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.util.Set;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public interface Content {

    public String getContentString(String key);

    public Set<Marker> getMarkerSet();

}
