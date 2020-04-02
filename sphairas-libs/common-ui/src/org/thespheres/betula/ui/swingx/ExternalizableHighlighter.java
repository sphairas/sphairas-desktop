/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import java.io.Externalizable;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.openide.windows.TopComponent;

/**
 *
 * @author boris.heithecker
 */
public interface ExternalizableHighlighter extends Highlighter, Externalizable {

    public String id();

    default public void restore(JXTable table, TopComponent tc) {
    }

    public interface ExternalizableHighlighterInstanceFactory extends HighlighterInstanceFactory {

        public String id();

    }
}
