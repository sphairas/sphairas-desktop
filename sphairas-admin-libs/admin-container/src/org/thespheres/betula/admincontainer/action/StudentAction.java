/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.util.HashMap;
import java.util.Map;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 */
class StudentAction {

    private final Map<DocumentId, Grade[]> updates = new HashMap<>();
    private final StudentId student;
    private final ImportTargetsItem item;

    StudentAction(StudentId student, final ImportTargetsItem iti) {
        this.item = iti;
        this.student = student;
    }

    StudentId getStudent() {
        return student;
    }

    void addUpdate(DocumentId d, Term term, Grade delete, Grade update) {
        final Timestamp gradeTime = new Timestamp(term.getBegin());
        item.submit(student, term.getScheduledItemId(), update, gradeTime);
        updates.put(d, new Grade[]{delete, update});
    }

}
