package org.thespheres.betula.niedersachsen.admin.ui.validate;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.validation.impl.ZensurensprungResult;

/**
 *
 * @author boris.heithecker
 */
@Messages({"ZensurensprungResultImpl.message=Notensprung bei {0} in {1}: Vorzensur {2}, aktuell {3}."})
public class ZensurensprungResultImpl extends ZensurensprungResult<RemoteStudent, RemoteTargetAssessmentDocument> {

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    ZensurensprungResultImpl(RemoteStudent student, TermId term, RemoteTargetAssessmentDocument d, Grade before, Grade current) {
        super(student, term, before, current, d.getDocumentId(), d);
        final String msg = NbBundle.getMessage(ZensurensprungResultImpl.class, "ZensurensprungResultImpl.message", student.getFullName(), d.getName().getDisplayName(null), before.getShortLabel(), current.getShortLabel());
        setMessage(msg);
    }
}
