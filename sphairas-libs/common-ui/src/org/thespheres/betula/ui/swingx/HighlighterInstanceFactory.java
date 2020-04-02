/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.windows.TopComponent;

/**
 *
 * @author boris.heithecker
 */
@MimeLocation(subfolderName = "Highlighter")
public interface HighlighterInstanceFactory {

    public Highlighter createHighlighter(JXTable table, TopComponent tc);

}
