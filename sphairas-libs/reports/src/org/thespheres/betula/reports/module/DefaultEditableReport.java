/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.module;

import org.thespheres.betula.reports.model.*;
import java.util.StringJoiner;
import org.netbeans.api.editor.guards.InteriorSection;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.Students;
import org.thespheres.betula.services.NamingResolver;

/**
 *
 * @author boris.heithecker
 */
@Messages({"EditableReport.createHeaderMessage.label=Bericht:"})
class DefaultEditableReport extends EditableReport {

    protected final DefaultEditableReportCollection collection;

    DefaultEditableReport(DefaultEditableReportCollection collection, Report report) {
        super(report);
        this.collection = collection;
    }

    @Override
    public EditableReportCollection getCollection() {
        return collection;
    }

    @Override
    public String getMessage() {
        StringJoiner sj = new StringJoiner(" "); //(" ", "", ReportsSectionsProvider.NB_ENDOFLINE);
        sj.add(NbBundle.getMessage(DefaultEditableReport.class, "EditableReport.createHeaderMessage.label"));
        StudentId sid = report.getStudent();
        if (sid != null) {
            Students stud = collection.getContext().lookup(Students.class);//student name
            if (stud != null) {
                Student s = stud.find(sid);
                String n = s != null ? s.getFullName() : Long.toString(sid.getId());
                sj.add(n);
            }
        }
        NamingResolver nr;//documentid
        return sj.toString();
    }

    void setSection(InteriorSection ss) {
        this.guarded = ss;
    }

}
