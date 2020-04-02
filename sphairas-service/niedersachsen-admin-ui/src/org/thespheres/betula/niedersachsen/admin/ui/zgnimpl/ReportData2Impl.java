/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.zgnimpl;

import java.util.Set;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.niedersachsen.admin.ui.RemoteReportsModel2;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;

/**
 *
 * @author boris.heithecker
 */
class ReportData2Impl extends ReportData2 {

    ReportData2Impl(DocumentId identity, TermId term, StudentId student, RemoteReportsModel2 rrl) {
        super(identity, term, student, rrl);
    }

    Set<String> getPropsInvalid() {
        return propsInvalid;
    }

}
