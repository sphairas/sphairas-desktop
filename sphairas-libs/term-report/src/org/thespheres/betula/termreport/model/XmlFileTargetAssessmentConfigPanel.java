/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport.model;

import org.thespheres.betula.ui.util.AbstractListConfigPanel;
import java.util.Arrays;
import java.util.Optional;
import org.jdesktop.swingx.JXComboBox;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.GradeFactory;
import org.thespheres.betula.services.CommonTargetProperties;
import org.thespheres.betula.ui.ConfigurationPanelComponent;
import org.thespheres.betula.ui.ConfigurationPanelContentTypeRegistration;
import org.thespheres.betula.ui.ConfigurationPanelComponentProvider;

/**
 *
 * @author boris.heithecker
 */
public class XmlFileTargetAssessmentConfigPanel extends AbstractListConfigPanel<XmlTargetAssessmentProvider, AssessmentConvention> {

    @SuppressWarnings({"LeakingThisInConstructor"})
    protected XmlFileTargetAssessmentConfigPanel(JXComboBox component) {
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
        current = context.lookup(XmlTargetAssessmentProvider.class);
        model.removeAllElements();
        model.addElement(null);
        if (current != null) {
            Optional.ofNullable(current.getEnvironment().getContextLookup().lookup(DataObject.class))
                    .map(dob -> FileOwnerQuery.getOwner(dob.getPrimaryFile()))
                    .map(prj -> prj.getLookup().lookup(CommonTargetProperties.class))
                    .map(CommonTargetProperties::getAssessmentConventions)
                    .ifPresent(arr -> Arrays.stream(arr).forEach(model::addElement));
        }
    }

    @Override
    protected void updateValue(AssessmentConvention ac) {
        String name = ac != null ? ((AssessmentConvention) ac).getName() : null;
        current.setPreferredConvention(name);
    }

    /**
     *
     * @author boris.heithecker
     */
    @ConfigurationPanelContentTypeRegistration(contentType = "XmlTargetAssessmentProvider")
    public static class Registration implements ConfigurationPanelComponentProvider {

        @Override
        public ConfigurationPanelComponent createConfigurationPanelComponent() {
            final JXComboBox cb = AbstractListConfigPanel.createAssesmentConventionComboBox();
            return new XmlFileTargetAssessmentConfigPanel(cb);
        }

    }

}
