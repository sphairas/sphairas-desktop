/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import java.awt.Component;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.openide.windows.TopComponent;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractHighlighterFactory implements HighlighterInstanceFactory {

    public static final String PROP_TABLE_MODEL_NAME = "table-model-name";
    private final String tableModelName;

    protected AbstractHighlighterFactory(String tableModelName) {
        this.tableModelName = tableModelName;
    }

    @Override
    public final Highlighter createHighlighter(JXTable table, TopComponent tc) {
        Object name = table.getClientProperty(PROP_TABLE_MODEL_NAME);
        if (tableModelName == null || (name instanceof String && ((String) name).equals(tableModelName))) {
            return doCreateHighlighter(table, tc);
        } else {
            return createNeverHighlighter();
        }
    }

    protected Highlighter createNeverHighlighter() {
        return new AbstractHighlighter(HighlightPredicate.NEVER) {
            @Override
            protected Component doHighlight(Component component, ComponentAdapter adapter) {
                throw new UnsupportedOperationException("Never called.");
            }
        };
    }

    public String getTableModelName() {
        return tableModelName;
    }

    protected abstract Highlighter doCreateHighlighter(JXTable table, TopComponent tc);
}
