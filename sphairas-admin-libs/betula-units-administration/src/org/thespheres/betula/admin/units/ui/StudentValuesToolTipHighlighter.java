/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.awt.Component;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.ToolTipHighlighter;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteGradeEntry;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.model.MultiSubject;

/**
 *
 * @author boris.heithecker
 */
@Messages({"StudentValuesToolTipHighlighter.timestamp=Geändert: {0,date,d.M.yy HH:mm}",
    "StudentValuesToolTipHighlighter.unconfirmed=Unbestätigt",
    "StudentValuesToolTipHighlighter.entry=Eintrag: {0}",
    "StudentValuesToolTipHighlighter.subject=Fächer: {0}",
    "StudentValuesToolTipHighlighter.signee=Unterzeichner: {0}"})
class StudentValuesToolTipHighlighter extends ToolTipHighlighter {

    private TargetsElementModel model;

    StudentValuesToolTipHighlighter(HighlightPredicate predicate) {
        super(predicate, null);
    }

    void setModel(TargetsElementModel m) {
        model = m;
        fireStateChanged();
    }

    @Override
    protected Component doHighlight(Component component, ComponentAdapter adapter) {
        if (adapter.getValue() == null) {
            return component;
        }
        final StringJoiner sj = new StringJoiner("<br>", "<html>", "</html>");
        int col = adapter.convertColumnIndexToModel(adapter.column);
        if (model != null && col > 0 && col < model.getColumnCount() - 1) {
            final RemoteTargetAssessmentDocument rtad = model.getRemoteTargetAssessmentDocumentAtColumnIndex(col);
            if (rtad != null) {
                addTargetData(sj, rtad, adapter);
            } else {
                sj.add(adapter.getString());
            }
        } else if (col == 0) {
            addStudentData(adapter, sj);
        }
        ((JComponent) component).setToolTipText(sj.toString());
        return component;
    }

    private void addStudentData(ComponentAdapter adapter, final StringJoiner sj) {
        try {
            final RemoteStudent rs = (RemoteStudent) adapter.getValue();
            sj.add(adapter.getString());
            final AbstractUnitOpenSupport uos = rs.getClientProperty(AbstractUnitOpenSupport.class.getCanonicalName(), AbstractUnitOpenSupport.class);
            if (uos != null) {
                sj.add(uos.getNodeDelegate().getDisplayName());
            }
            sj.add(Long.toString(rs.getStudentId().getId()));
        } catch (ClassCastException e) {
        }
    }

    private void addTargetData(final StringJoiner sj, final RemoteTargetAssessmentDocument rtad, final ComponentAdapter adapter) {
        //Target name / Fach
        sj.add(rtad.getName().getDisplayName(model.getCurrentIndentity())).add(adapter.getString(0));
        try {
            final Optional<RemoteGradeEntry> opt = (Optional<RemoteGradeEntry>) adapter.getValue();
            opt.ifPresent(gv -> {
                final Grade grade = gv.getGrade();
                String lbl = grade.getShortLabel();
                final String c = grade.getConvention();
                if ("niedersachsen.ersatzeintrag".equals(c) || "niedersachsen.teilnahme".equals(c)) {
                    lbl += " (" + grade.getLongLabel() + ")";
                }
                final String msg = NbBundle.getMessage(StudentValuesToolTipHighlighter.class, "StudentValuesToolTipHighlighter.entry", lbl);
                sj.add(msg);
                if (!gv.isUnconfirmed()) {
                    final Date d = gv.getTime();
                    final String message = NbBundle.getMessage(StudentValuesToolTipHighlighter.class, "StudentValuesToolTipHighlighter.timestamp", d);
                    sj.add(message);
                } else {
                    final String message = NbBundle.getMessage(StudentValuesToolTipHighlighter.class, "StudentValuesToolTipHighlighter.unconfirmed");
                    sj.add(message);
                }
            });
        } catch (ClassCastException e) {
        }
        //Subject & realm
        final MultiSubject ms = rtad.getMultiSubject();
        if (ms != null) {
            String sub = ms.getSubjectMarkerSet().stream()
                    .map(m -> m.getLongLabel())
                    .collect(Collectors.joining(","));
            if (ms.getRealmMarker() != null) {
                sub += " [" + ms.getRealmMarker().getLongLabel() + "]";
            }
            final String message = NbBundle.getMessage(StudentValuesToolTipHighlighter.class, "StudentValuesToolTipHighlighter.subject", sub);
            sj.add(message);
        }
        //Signee
        final Signee signee = rtad.getSignee("entitled.signee");
        if (!Signee.isNull(signee)) {
            final String signeeName = Optional.ofNullable(Utilities.actionsGlobalContext().lookup(AbstractUnitOpenSupport.class))
                    .flatMap(AbstractUnitOpenSupport::getSignees)
                    .filter(si -> si.getSigneeSet().contains(signee))
                    .map(si -> si.getSignee(signee))
                    .orElse(signee.getId());
            final String message = NbBundle.getMessage(StudentValuesToolTipHighlighter.class, "StudentValuesToolTipHighlighter.signee", signeeName);
            sj.add(message);
        }
    }

}
