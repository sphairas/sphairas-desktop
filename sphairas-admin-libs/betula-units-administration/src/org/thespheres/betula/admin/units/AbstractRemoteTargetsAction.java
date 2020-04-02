/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.swing.Action;
import org.openide.util.Lookup;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.WorkingDate;
import org.thespheres.betula.ui.util.SurvivingResult;
import org.thespheres.betula.ui.util.WorkingDateSensitiveAction;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractRemoteTargetsAction extends WorkingDateSensitiveAction<RemoteTargetAssessmentDocument> {

    private Lookup.Result<AbstractUnitOpenSupport> result;
    protected Term term;

    protected AbstractRemoteTargetsAction() {
    }

    @SuppressWarnings(value = {"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    protected AbstractRemoteTargetsAction(Lookup context, boolean surviveFocusChange) {
        super(context, RemoteTargetAssessmentDocument.class, true, surviveFocusChange);
        result = new SurvivingResult(context.lookupResult(AbstractUnitOpenSupport.class), surviveFocusChange);
    }

    @Override
    public Action createContextAwareInstance(final Lookup actionContext) {
        final Lookup ctx = actionContext.lookupAll(TargetAssessmentSelectionProvider.class).stream()
                .map(TargetAssessmentSelectionProvider.class::cast)
                .map(TargetAssessmentSelectionProvider::getLookup)
                .collect(CollectionUtil.singleton())
                .orElse(actionContext);
        return createAbstractRemoteTargetsAction(ctx);
    }

    protected abstract AbstractRemoteTargetsAction createAbstractRemoteTargetsAction(Lookup context);

    protected Term findCommonTerm() throws IOException {
        WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
        term = null;
        for (final AbstractUnitOpenSupport puos : context.lookupAll(AbstractUnitOpenSupport.class)) {
            final TermSchedule ts = puos.findTermSchedule();
            final Term t = wd.isNow() ? ts.getCurrentTerm() : ts.getTerm(wd.getCurrentWorkingDate());
            if (term == null) {
                term = t;
            } else if (!term.equals(t)) {
                throw new IOException("Unequal terms.");
            }
        }
        if (term == null) {
            throw new IOException("No term.");
        }
        return term;
    }

    private synchronized Optional<AbstractUnitOpenSupport> support() {
        return result.allInstances().stream()
                .map(AbstractUnitOpenSupport.class::cast)
                .collect(CollectionUtil.singleton());
    }

    @Override
    protected final void actionPerformed(List<RemoteTargetAssessmentDocument> context) {
        actionPerformed(context, support());
    }

    protected abstract void actionPerformed(List<RemoteTargetAssessmentDocument> context, Optional<AbstractUnitOpenSupport> support);

    @Override
    protected final void onContextChange(List<RemoteTargetAssessmentDocument> all) {
        onContextChange(all, support());
    }

    protected void onContextChange(List<RemoteTargetAssessmentDocument> all, Optional<AbstractUnitOpenSupport> support) {
    }

}
