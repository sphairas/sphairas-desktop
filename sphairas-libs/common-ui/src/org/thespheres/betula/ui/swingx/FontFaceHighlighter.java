/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import java.awt.Component;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

/**
 *
 * @author boris.heithecker
 */
public class FontFaceHighlighter extends FontHighlighter {

    private final int style;

    public FontFaceHighlighter(int style) {
        this.style = style;
    }

    public FontFaceHighlighter(int style, HighlightPredicate predicate) {
        super(predicate);
        this.style = style;
    }

    @Override
    protected boolean canHighlight(Component component, ComponentAdapter adapter) {
        setFont(adapter.getComponent().getFont().deriveFont(style));
        return super.canHighlight(component, adapter);
    }
}
