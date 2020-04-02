/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.impl;

import javax.swing.JComponent;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-thespheres-betula-project-local")
@Messages({"DefaultCustomizer.defaultCategory.displayName=Allgemein"})
public class DefaultCustomizer implements ProjectCustomizer.CompositeCategoryProvider {

    @Override
    public ProjectCustomizer.Category createCategory(Lookup context) {
        String displayName = NbBundle.getMessage(DefaultCustomizer.class, "DefaultCustomizer.defaultCategory.displayName");
        return ProjectCustomizer.Category.create("default", displayName, null);
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        return new DefaultCustomizerPanel(category, context);
    }

}
