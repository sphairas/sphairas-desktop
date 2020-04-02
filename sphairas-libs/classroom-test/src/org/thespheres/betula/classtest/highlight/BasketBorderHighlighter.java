/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.highlight;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;
import org.thespheres.betula.classtest.model.EditableBasket;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.classtest.table2.ClasstestTableModel2;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
class BasketBorderHighlighter extends BorderHighlighter implements HighlightPredicate, LookupListener {

    private final Border border;
    private final TopComponent tc;
    private final Lookup.Result<ClassroomTestEditor2> result;
    private ClassroomTestEditor2 editor;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private BasketBorderHighlighter(TopComponent component) {
        border = new MatteBorder(0, 2, 0, 2, Color.BLACK); //new LineBorder(Color.BLACK, 2);
        this.setBorder(border);
        this.setHighlightPredicate(this);
        this.tc = component;
        this.result = tc.getLookup().lookupResult(ClassroomTestEditor2.class);
        this.result.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public synchronized void resultChanged(LookupEvent ev) {
        if (editor != null) {
            editor.getEditableClassroomTest().getEventBus().unregister(this);
        }
        editor = result.allInstances().stream()
                .collect(CollectionUtil.singleOrNull());
        if (editor != null) {
            editor.getEditableClassroomTest().getEventBus().register(this);
        }
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        if (editor != null) {
            ClasstestTableModel2 m = (ClasstestTableModel2) ((JXTable) ca.getComponent()).getModel();
            final int column = ca.convertColumnIndexToModel(ca.column);
            EditableProblem ep = m.findEditableProblem(column);
            if (ep != null) {
                return ep.isBasket();
            }
        }
        return false;
    }

    @Subscribe
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof EditableBasket && EditableBasket.PROP_REFERENCES.equals(evt.getPropertyName())) {
            fireStateChanged();
        }
    }

    @MimeRegistration(mimeType = "text/betula-classtest-file+xml", service = HighlighterInstanceFactory.class)
    public static class Factory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new BasketBorderHighlighter(tc);
        }
    }
}
