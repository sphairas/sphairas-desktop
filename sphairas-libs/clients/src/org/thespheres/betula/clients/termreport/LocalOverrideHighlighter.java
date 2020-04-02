/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.termreport;

import java.awt.Component;
import java.awt.Font;
import java.lang.ref.WeakReference;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.windows.TopComponent;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.termreport.TableColumnConfiguration;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;

/**
 *
 * @author boris.heithecker
 */
class LocalOverrideHighlighter extends FontHighlighter implements HighlightPredicate {

    private final WeakReference<JXTable> table;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    private LocalOverrideHighlighter(JXTable cmp) {
        super(cmp.getFont().deriveFont(Font.BOLD));
        this.table = new WeakReference(cmp);
        setHighlightPredicate(this);
    }

    @Override
    public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
        final JXTable t = table.get();
        if (t != null) {
            final Object ap = t.getColumnExt(ca.column).getClientProperty(TableColumnConfiguration.PROP_ASSESSMENTPROVIDER);
            if (ap instanceof XmlRemoteTargetAssessmentProvider) {
                XmlRemoteTargetAssessmentProvider rap = (XmlRemoteTargetAssessmentProvider) ap;
                Object sv = ca.getValue(0);
                if (sv instanceof StudentId) {
                    return rap.isLocalOverridesRemotes((StudentId) sv);
                }
            }
        }
        return false;
    }

    @MimeRegistration(mimeType = "text/term-report-file+xml", service = HighlighterInstanceFactory.class)
    public static class LocalOverrideHighlighterFactory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new LocalOverrideHighlighter(table);
        }
    }
}
