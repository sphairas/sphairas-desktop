/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.plaf.ColorUIResource;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.TableCellContext;

/**
 *
 * @author boris.heithecker
 */
public class TextAreaProvider extends ComponentProvider<JScrollPane> {

    final Map<Integer, Point> positionsMap = new HashMap<>();

    @Override
    protected void format(CellContext context) {
//        rendererComponent.setIcon(getValueAsIcon(context));
//        rendererComponent.setMargin(new Insets(2, 16, 2, 16));
        ((FreezableViewport) rendererComponent.getViewport()).formatting = true;
        getTextAreaRendererComponent().setText(getValueAsString(context));
        getTextAreaRendererComponent().setBackground(rendererComponent.getBackground());
        if (context.isSelected()) {
        }
        Point p = positionsMap.get(context.getRow());
        if (p != null) {
            ((FreezableViewport) rendererComponent.getViewport()).setViewPosition(p);
        }
        ((FreezableViewport) rendererComponent.getViewport()).formatting = false;
    }

    @Override
    protected void configureState(CellContext context) {
//        rendererComponent.setHorizontalAlignment(getHorizontalAlignment());
        TableCellContext tcc = (TableCellContext) context;
//        rendererComponent.setMargin(new Insets(2, 16, 2, 16)); 
    }

    private JTextArea getTextAreaRendererComponent() {
        return (JTextArea) rendererComponent.getViewport().getView();
    }

    @Override
    protected JScrollPane createRendererComponent() {
        JScrollPane pane = new JScrollPane() {

            @Override
            protected JViewport createViewport() {
                return new FreezableViewport();
            }

            @Override
            public void setBackground(Color bg) {
                super.setBackground(bg);
                if (getViewport().getView() != null) {
//                    if (-1445896  == bg.getRGB()) {  //blaus -1445896   //grün-7360912 bg instanceof ColorUIResource && 
                    getViewport().getView().setBackground(bg);
//                    } else {
//                        getViewport().getView().setBackground(bg);
//                    }
                }
            }

            @Override
            public void setForeground(Color fg) {
                super.setForeground(fg);
                if (getViewport().getView() != null) {
                    getViewport().getView().setForeground(fg);
                }
            }

        };
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JTextArea component = new JTextArea();
        component.setLineWrap(true);
        component.setWrapStyleWord(true);
//        pane.setOpaque(false);
//        component.setOpaque(false);
//        pane.getViewport().setOpaque(false);
//        component.setOpaque(true);
//        component.setMargin(new Insets(2, 16, 2, 16));
        pane.setWheelScrollingEnabled(false);
        pane.setViewportView(component);
        pane.setViewportBorder(BorderFactory.createEmptyBorder());

        return pane;
    }

    private class FreezableViewport extends JViewport {

        private boolean formatting;

        @Override
        public void setViewPosition(Point p) {
            if (formatting) {
                super.setViewPosition(p);
            }
        }

    }
}
