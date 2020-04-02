/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.units.RemoteSignee;
import org.thespheres.betula.admin.units.RemoteSignee.DocumentInfo;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.SigneesTopComponentModel;
import org.thespheres.betula.admin.units.TargetsSelectionElementEnv2;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.ui.util.MultiContextAction;
import org.thespheres.betula.ui.util.MultiContextSensitiveAction;
import org.thespheres.betula.util.CollectionUtil;

@ActionID(category = "Betula",
        id = "org.thespheres.betula.admin.units.ui.TargetsSelectionOpenAction")
@ActionRegistration(displayName = "#CTL_TargetsSelectionOpenAction",
        lazy = false,
        surviveFocusChange = true)
@ActionReference(path = "Loaders/application/betula-signee-target-document/Actions", position = 2200)
public final class TargetsSelectionOpenAction extends MultiContextAction {

    public TargetsSelectionOpenAction() {
        super(SigneesTopComponentModel.class);
        multiTypes.add(RemoteSignee.DocumentInfo.class);
        putValue("iconBase", "org/thespheres/betula/admin/units/resources/document-list.png");
    }
    
    public TargetsSelectionOpenAction(final List<RemoteTargetAssessmentDocument> ctx) {
        
    }

    @Override
    protected MultiContextSensitiveAction createMultiContextSensitiveAction() {
        return new ActionRunner();
    }

    static String getName() {
        return NbBundle.getMessage(TargetsSelectionOpenAction.class, "CTL_TargetsSelectionOpenAction");
    }

    public static void actionPerformed(final Set<DocumentId> l, final String provider) {
        final TargetsSelectionElementEnv2 env = new TargetsSelectionElementEnv2(provider);
        final LocalProperties lm = LocalProperties.find(provider);
        final DocumentsModel dm = new DocumentsModel();
        dm.initialize(lm.getProperties());
        final String sfx = lm.getProperty(DocumentsModel.PROP_DOCUMENT_SUFFIXES, "");
        final String[] sfxArr = sfx != null ? sfx.split(",") : new String[0];
        final Comparator<DocumentId> comp = Comparator.comparing(d -> dm.getSuffix(d), Comparator.comparing(s -> {
            for (int i = 0; i < sfxArr.length; i++) {
                if (s != null && s.equals(sfxArr[i])) {
                    return i;
                }
            }
            return Integer.MAX_VALUE;
        }));

        l.stream()
                .sorted(comp)
                .forEach(env::addTarget);
        env.findCloneableOpenSupport().open();
    }

    static class ActionRunner extends MultiContextSensitiveAction {

        @Override
        protected String getName(final boolean enabled) {
            return TargetsSelectionOpenAction.getName();
        }

        @Override
        protected void updateEnabled(boolean enabled, Lookup instances) {
            super.updateEnabled(enabled, instances);
            if (instances.lookupAll(SigneesTopComponentModel.class).stream()
                    .count() != 1l) {
                setEnabled(false);
            }
        }

        @Override
        public void actionPerformed(final ActionEvent e, final Lookup context) {
            final SigneesTopComponentModel m = context.lookupAll(SigneesTopComponentModel.class).stream()
                    .map(SigneesTopComponentModel.class::cast)
                    .collect(CollectionUtil.requireSingleOrNull());
            final Set<DocumentId> docs = context.lookupAll(RemoteSignee.DocumentInfo.class).stream()
                    .map(RemoteSignee.DocumentInfo.class::cast)
                    .map(DocumentInfo::getDocument)
                    .collect(Collectors.toSet());
            TargetsSelectionOpenAction.actionPerformed(docs, m.getProvider());
        }

    }
}
