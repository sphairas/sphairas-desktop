/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import java.awt.Component;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.Painter;

/**
 *
 * @author boris.heithecker
 */
public class CellIconHighlighter extends PainterHighlighter implements HighlightPredicate, ChangeListener {

    private final List<CellIconHighlighterDelegate> delegates = new ArrayList<>();
    private final ComponentPainter painter = new ComponentPainter();

    @SuppressWarnings({"LeakingThisInConstructor", 
        "OverridableMethodCallInConstructor"})
    public CellIconHighlighter() {
        setHighlightPredicate(this);
        setPainter(painter);
    }

    public void addIconHighlighterDelegate(CellIconHighlighterDelegate add) {
        delegates.add(0, add);
        add.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireStateChanged();
    }

    @Override
    public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
        return delegates.stream().map(CellIconHighlighterDelegate::getHighlightPredicate).anyMatch((p) -> p.isHighlighted(renderer, adapter));
    }

    @Override
    protected Component doHighlight(Component component, ComponentAdapter adapter) {
        Component ret = super.doHighlight(component, adapter);
        painter.setCurrentAdapter(adapter);
        return ret;
    }

    private final class ComponentPainter implements Painter<Component> {

        private ComponentAdapter current;

        private void setCurrentAdapter(ComponentAdapter adapter) {
            current = adapter;
        }

        @Override
        public void paint(Graphics2D g, Component object, int width, int height) {
            if (current != null) {
                int shift = 1;
                for (CellIconHighlighterDelegate d : delegates) {
                    if (d.getHighlightPredicate().isHighlighted(object, current)) {
                        g.drawImage(d.getIcon(), width - shift++ * 17, 1, 16, 16, null);
                    }
                }
            }
        }
    }

}
