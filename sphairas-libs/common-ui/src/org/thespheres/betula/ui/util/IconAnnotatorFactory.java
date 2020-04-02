/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
@MimeLocation(subfolderName = "IconAnnotator")
public interface IconAnnotatorFactory {

    public IconAnnotator createIconAnnotator(Lookup context);
}
