/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.xml;

import java.io.IOException;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public interface XmlBeforeSaveCallback { //extends Comparable<XmlBeforeSaveCallback> {

    public void run(Lookup context, org.w3c.dom.Document document) throws IOException;

    default int position() {
        return 1000;
    }
//
//    @Override
//    default int compareTo(XmlBeforeSaveCallback o) {
//        return position() - o.position();
//    }

}
