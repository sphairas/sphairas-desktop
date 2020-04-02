/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.target;

import org.thespheres.betula.ui.util.AbstractListConfigPanel;
import java.util.Arrays;
import java.util.Optional;
import org.jdesktop.swingx.JXComboBox;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.services.CommonTargetProperties;
import org.thespheres.betula.ui.ConfigurationPanelComponent;
import org.thespheres.betula.ui.ConfigurationPanelComponentProvider;
import org.thespheres.betula.util.XmlTargetAssessment;

/**
 *
 * @author boris.heithecker
 */
public class JournalAssessmentConfigPanel extends AbstractListConfigPanel<XmlTargetAssessment, AssessmentConvention> {

    @SuppressWarnings({"LeakingThisInConstructor"})
    protected JournalAssessmentConfigPanel(JXComboBox component) {
        super(component);
    }

    @Override
    protected AssessmentConvention getCurrentValue() {
        if (current != null && current.getPreferredConvention() != null) {
            return GradeFactory.findConvention(current.getPreferredConvention());
        }
        return null;
    }

    @Override
    protected void onContextChange(Lookup context) {
        current = context.lookup(XmlTargetAssessment.class);
        model.removeAllElements();
        model.addElement(null);
        Optional.ofNullable(context.lookup(DataObject.class))
                .map(dob -> FileOwnerQuery.getOwner(dob.getPrimaryFile()))
                .map(prj -> prj.getLookup().lookup(CommonTargetProperties.class))
                .map(ctp -> ctp.getAssessmentConventions())
                .ifPresent(arr -> Arrays.stream(arr)
                        .forEach(model::addElement));
    }

    @Override
    protected void updateValue(AssessmentConvention ac) {
        String name = ac != null ? ((AssessmentConvention) ac).getName() : null;
        current.setPreferredConvention(name);
    }

    @MimeRegistration(mimeType = "text/betula-journal-file+xml", position = 5000, service = ConfigurationPanelComponentProvider.class)
    public static class Registration implements ConfigurationPanelComponentProvider {

        @Override
        public ConfigurationPanelComponent createConfigurationPanelComponent() {
            final JXComboBox cb = AbstractListConfigPanel.createAssesmentConventionComboBox();
            return new JournalAssessmentConfigPanel(cb);
        }

    }

}
