/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.Component;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.openide.windows.TopComponent;
import org.thespheres.betula.journal.model.JournalEditor;

/**
 *
 * @author boris.heithecker
 */
public class CalendarFontHighlighter extends AbstractHighlighter {

    private JournalEditor editor;
    private final int style;

    protected CalendarFontHighlighter(int style) {
        super();
        this.style = style;
//        setFont(Font.getFont("Arial"));
    }

    protected JournalEditor getEditor(ComponentAdapter ca) {
        if (editor == null) {
            TopComponent tc = findTC(ca.getComponent());
            if (tc != null) {
                JournalEditor ed = tc.getLookup().lookup(JournalEditor.class);
                if (ed != null) {
                    this.editor = ed;
                    notifyEditor(ed);
                }
            }
        }
        return editor;
    }

    protected void notifyEditor(JournalEditor ed) {
    }

    private TopComponent findTC(Component comp) {
        //von ExplorerManger.find.....
        for (;;) {
            if (comp == null) {
                return null;
            }

            if (comp instanceof TopComponent) {
                return (TopComponent) comp;
            }
            comp = comp.getParent();
        }
    }

    @Override
    protected Component doHighlight(Component comp, ComponentAdapter ca) {
        comp.setFont(comp.getFont().deriveFont(style));
        return comp;
    }
}
