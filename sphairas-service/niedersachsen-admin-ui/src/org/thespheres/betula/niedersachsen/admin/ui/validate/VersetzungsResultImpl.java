/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import java.util.List;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.niedersachsen.admin.ui.ReportData2;
import org.thespheres.betula.validation.impl.PolicyLegalHint;
import org.thespheres.betula.validation.impl.VersetzungsResult;

/**
 *
 * @author boris.heithecker
 */
public class VersetzungsResultImpl extends VersetzungsResult<RemoteStudent, ReportData2> {

    public VersetzungsResultImpl(RemoteStudent student, ReportData2 report, List<PolicyLegalHint> hints) {
        super(student, report, hints);
    }

}
