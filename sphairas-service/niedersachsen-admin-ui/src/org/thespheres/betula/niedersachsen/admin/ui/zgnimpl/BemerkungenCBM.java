/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultComboBoxModel;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.Element;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.MarkerItem;

/**
 *
 * @author boris.heithecker
 */
@Messages("BemerkungenCBM.noStudentComboItemLabel=---")
class BemerkungenCBM extends DefaultComboBoxModel implements StringValue {

    private TermReportNoteSetTemplate template;
    private ReportData2 student;

    BemerkungenCBM() {
    }

    void setTemplate(TermReportNoteSetTemplate t) {
        template = t;
        init();
    }

    void setStudent(ReportData2 r) {
        this.student = r;
    }

    void initialize(final JXComboBox box) {
        box.setHighlighters(createHighlighters(box));
        box.setUseHighlightersForCurrentValue(false);
    }

    protected void init() {
        removeAllElements();
        if (template != null) {
            for (final Element e : template.getElements()) {
                addElement(e);
                e.getMarkers().stream()
                        .filter(m -> !Marker.isNull(m.getMarker()))
                        .forEach(this::addElement);
            }
        }
        fireContentsChanged(this, 0, getSize() - 1);
    }

    private Highlighter[] createHighlighters(JXComboBox box) {
        return new Highlighter[]{new FontHighlighter(new ElementHighlighter(), box.getFont().deriveFont(Font.BOLD))};
    }

    @Override
    public String getString(Object o) {
        if (o instanceof Element) {
            return ((Element) o).getElementDisplayName();
        } else if (o instanceof MarkerItem) {
            final MarkerItem m = (MarkerItem) o;
            String s;
            final Marker ma;
            if (student != null && (ma = m.getMarker()) != null) {
                s = ma.getLongLabel(student.getFormatArgs());
            } else {
                s = NbBundle.getMessage(BemerkungenCBM.class, "BemerkungenCBM.noStudentComboItemLabel");
            }
            return " " + s;
        } else if (o == null) {
            return "";
        }
        return new IllegalArgumentException("ComboBox item must be of type Element or Marker.").toString();
    }

    private class ElementHighlighter implements HighlightPredicate {

        @Override
        public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
            int r = ca.convertColumnIndexToModel(ca.row);
            final Object el = BemerkungenCBM.this.getElementAt(r);
            return el instanceof Element;
        }
    }
}
