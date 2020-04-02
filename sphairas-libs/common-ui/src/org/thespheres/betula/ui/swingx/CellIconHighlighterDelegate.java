/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import java.awt.Component;
import java.awt.Image;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.openide.util.ImageUtilities;

/**
 *
 * @author boris.heithecker
 */
public class CellIconHighlighterDelegate extends AbstractHighlighter {

    private final String iconBase;

    public CellIconHighlighterDelegate(String iconBase) {
        this.iconBase = iconBase;
    }

    public CellIconHighlighterDelegate(String iconBase, HighlightPredicate predicate) {
        super(predicate);
        this.iconBase = iconBase;
    }

    public Image getIcon() {
        return ImageUtilities.loadImage(iconBase, true);
    }

    @Override
    protected final Component doHighlight(Component component, ComponentAdapter adapter) {
        throw new UnsupportedOperationException("Must not be called. IconHighlighterDelegate is only a delegate.");
    }

}
