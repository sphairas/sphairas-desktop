/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import com.google.common.eventbus.Subscribe;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.openide.windows.TopComponent;
import org.thespheres.betula.journal.model.EditableRecord;
import org.thespheres.betula.journal.model.JournalEditor;

/**
 *
 * @author boris.heithecker
 */
public class CalendarColorHighlighter extends ColorHighlighter  {

    private JournalEditor editor;
    private final String[] properties;

    protected CalendarColorHighlighter(String[] properties) {
        this.properties = properties;
    }

    protected JournalEditor getEditor(ComponentAdapter ca) {
        if (editor == null) {
            TopComponent tc = findTC(ca.getComponent());
            if (tc != null) {
                JournalEditor ed = tc.getLookup().lookup(JournalEditor.class);
                if (ed != null) {
                    this.editor = ed;
                    editor.getEditableJournal().getEventBus().register(this);
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

    @Subscribe
    public void onPropertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof EditableRecord) {
            for (String prop : properties) {
                if (prop.equals(evt.getPropertyName())) {
                    fireStateChanged();
                }
            }
        }
    }

}
