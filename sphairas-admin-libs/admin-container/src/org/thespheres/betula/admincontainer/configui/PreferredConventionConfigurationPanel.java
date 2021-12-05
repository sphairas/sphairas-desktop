/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.configui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.ConfigurationPanelComponent;
import org.thespheres.betula.ui.ConfigurationPanelComponentProvider;
import org.thespheres.betula.ui.ConfigurationPanelContentTypeRegistration;
import org.thespheres.betula.ui.util.AbstractListConfigPanel;
import org.thespheres.betula.xmlimport.ImportUtil;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class PreferredConventionConfigurationPanel extends AbstractListConfigPanel<RemoteTargetAssessmentDocument, AssessmentConvention> implements StringValue, PropertyChangeListener {

    private AbstractUnitOpenSupport currentSupport;

    @SuppressWarnings({"LeakingThisInConstructor"})
    public PreferredConventionConfigurationPanel(final JXComboBox component) {
        super(component);
        final DefaultListRenderer r = new DefaultListRenderer(this);
        component.setRenderer(r);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getSource() instanceof RemoteTargetAssessmentDocument && RemoteTargetAssessmentDocument.PROP_PREFERRED_CONVENTIUON.equals(evt.getPropertyName())) {
            final RemoteTargetAssessmentDocument rtad = ((RemoteTargetAssessmentDocument) evt.getSource());
            EventQueue.invokeLater(() -> updateSelectionIfCurrent(rtad));
        }
    }

    private void updateSelectionIfCurrent(final RemoteTargetAssessmentDocument rtad) {
        if (current != null && Objects.equals(current, rtad)) {
            comboBox.removeActionListener(this);
            model.setSelectedItem(getCurrentValue());
            comboBox.addActionListener(this);
        }
    }

    private void updateItems(final AssessmentConvention[] items) {
        if (items != null) {
            Arrays.stream(items)
                    .forEach(model::addElement);
        }
    }

    @Override
    protected AssessmentConvention getCurrentValue() {
        if (current != null) {
            return Optional.ofNullable(current.getPreferredConvention())
                    .map(GradeFactory::findConvention)
                    .orElse(null);
        }
        return null;
    }

    @Override
    protected void updateValue(final AssessmentConvention pu) {
        if (current != null && currentSupport != null) {
            processSelection(currentSupport, List.of(current), pu);
        }
    }

    private void processSelection(final AbstractUnitOpenSupport uos, final List<RemoteTargetAssessmentDocument> l, final AssessmentConvention pu) {
        try {
            final DocumentsModel m = uos.findDocumentsModel();
            final Map<DocumentId, List<RemoteTargetAssessmentDocument>> mapped = l.stream()
                    .collect(Collectors.groupingBy(t -> m.convert(t.getDocumentId())));
            //group selected targets
            final WebServiceProvider wsp = uos.findWebServiceProvider();

            final List<EditRemoteTargetPreferredConventionImportTargetsItem> items = mapped.entrySet().stream()
                    .flatMap(entry -> EditRemoteTargetPreferredConventionImportTargetsItem.rtadToTargetDoc(entry.getKey(), entry.getValue(), m).stream())
                    .collect(Collectors.toList());

            final EditRemoteTargetPreferredConventionEdit edit = new EditRemoteTargetPreferredConventionEdit(uos, items, wsp, pu);

//            class ImportActionWizardIterator extends AbstractFileImportWizard<EditRemoteTargetMarkersEdit> {
//
//                @Override
//                protected List<WizardDescriptor.Panel<EditRemoteTargetMarkersEdit>> createPanels() {
//                    return Collections.singletonList(new EditRemoteTargetMarkersVisualPanel.EditRemoteTargetMarkersPanel());
//                }
//
//            }
//            final WizardDescriptor wd = new WizardDescriptor(new ImportActionWizardIterator(), edit);
//            wd.setTitle(actionName);
//            if (DialogDisplayer.getDefault().notify(wd) == WizardDescriptor.FINISH_OPTION) {
//                edit.runAction();
//            }
            edit.runAction();
        } catch (IOException ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
        }
    }

    @Override
    protected void onContextChange(final Lookup context) {
        final Collection<? extends RemoteTargetAssessmentDocument> tads = context.lookupAll(RemoteTargetAssessmentDocument.class);
        final RemoteTargetAssessmentDocument tad = tads.size() == 1 ? tads.iterator().next() : null;
        if (Objects.equals(tad, current)) {
            return;
        }
        if (current != null) {
            current.removePropertyChangeListener(this);
        }
        final String beforeProvider = current != null ? current.getProvider() : null;
        current = tad;
        currentSupport = context.lookup(AbstractUnitOpenSupport.class);
        if (current != null) {
            current.addPropertyChangeListener(this);
        }
        if (!Objects.equals(beforeProvider, current != null ? current.getProvider() : null)) {
            model.removeAllElements();
            model.addElement(null);
            if (current != null) {
                final LocalFileProperties lfp = LocalFileProperties.find(current.getProvider());
                final AssessmentConvention[] acc = lfp.getAssessmentConventions();
                updateItems(acc);
            }
        }
    }

    @Override
    public String getString(Object value) {
        if (value instanceof AssessmentConvention) {
            final AssessmentConvention ac = (AssessmentConvention) value;
            return ac.getDisplayName();
        }
        return "---";
    }

    @NbBundle.Messages({"PreferredConventionConfigurationRegistration.comboBox.name=Bevorzugte Bewertungskonvention"})
    @ConfigurationPanelContentTypeRegistration(contentType = "AbstractTargetAssessmentDocument", position = 5000)
    public static class PreferredConventionConfigurationRegistration implements ConfigurationPanelComponentProvider {

        @Override
        public ConfigurationPanelComponent createConfigurationPanelComponent() {
            final JXComboBox cb = new JXComboBox();
            final String n = NbBundle.getMessage(PreferredConventionConfigurationRegistration.class, "PreferredConventionConfigurationRegistration.comboBox.name");
            cb.setName(n);
            return new PreferredConventionConfigurationPanel(cb);
        }

    }
}
