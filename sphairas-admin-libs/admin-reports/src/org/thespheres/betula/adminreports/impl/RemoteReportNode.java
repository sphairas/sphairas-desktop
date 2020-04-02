/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.io.IOException;
import java.util.StringJoiner;
import javax.swing.Action;
import org.apache.commons.lang3.StringUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Students;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.Terms;
import org.thespheres.betula.services.scheme.spi.Term;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"RemoteReportNode.tooltip.term={0}/{1}"})
class RemoteReportNode extends AbstractNode {

    private final static String ICON = "org/thespheres/betula/adminreports/resources/report.png";
    private final RemoteEditableReport report;
    private final RemoteReportsModel model;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    RemoteReportNode(RemoteEditableReport key, RemoteReportsModel rModel) {
        super(Children.LEAF, Lookups.fixed(key, rModel));
        this.report = key;
        this.model = rModel;
        setIconBaseWithExtension(ICON);
        updateName();
    }

    private void updateName() {
        final StringJoiner sj = new StringJoiner(" - ");
        final StudentId sid = report.getStudent();
        final Students stud = report.getCollection().getContext().lookup(Students.class);
        if (stud != null) {
            final Student s = stud.find(sid);
            final String n = s != null ? s.getDirectoryName() : Long.toString(sid.getId());
            sj.add(n);
        }
        NamingResolver resolver = null;
        try {
            resolver = report.getCollection().getContext().lookup(RemoteReportsSupport.class).findNamingResolver();
        } catch (IOException ex) {
        }
        Term term = null;
        final TermId tid = report.getTerm();
        if (tid != null) {
            term = Terms.forTermId(tid);
        }
        DocumentId did = report.getDocument();
        final Marker section = report.findSectionMarker();
        if (!Marker.isNull(section)) {
            final String tr = StringUtils.substring(section.getLongLabel(), 0, 10);
            sj.add(tr);
        } else if (did != null) {
            String docdn = null;
            if (resolver != null) {
                try {
                    docdn = resolver.resolveDisplayNameResult(did).getResolvedName(term);
                } catch (IllegalAuthorityException ex) {
                }
            }
            if (docdn == null) {
                docdn = did.getId();
            }
            sj.add(docdn);
        }
        if (term != null) {
            final Integer jahr = (int) term.getParameter("jahr");
            final Integer hj = (int) term.getParameter("halbjahr");
            if (jahr != null && hj != null) {
                final String dn = NbBundle.getMessage(RemoteReportNode.class, "RemoteReportNode.tooltip.term", jahr.toString(), hj.toString());
                sj.add(dn);
            } else {
                sj.add(term.getDisplayName());
            }
        }
        setDisplayName(sj.toString());
        setShortDescription(report.getMessage());
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Editors/text/betula-remote-reports/Popup").stream()
                .toArray(Action[]::new);
    }

}
