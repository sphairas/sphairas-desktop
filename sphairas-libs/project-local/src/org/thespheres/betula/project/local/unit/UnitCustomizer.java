package org.thespheres.betula.project.local.unit;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import javax.swing.JComponent;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.services.LocalFileProperties;

/**
 *
 * @author boris.heithecker
 */
@Messages({"UnitCustomizer.displayName=Gruppe"})
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-thespheres-betula-project-local", position = 10000)
public class UnitCustomizer implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String REGULAR_CALENDAR = "regular";

    @Override
    public Category createCategory(Lookup context) {
        LocalFileProperties prop = context.lookup(LocalFileProperties.class);
        if (prop.getProperty("providerURL") == null) {
            final String dn = NbBundle.getMessage(UnitCustomizer.class, "UnitCustomizer.displayName");
            Category ret = ProjectCustomizer.Category.create("UnitCustomizer", dn, null);
            return ret;
        }
        return null;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        LocalUnit unit = context.lookup(LocalUnit.class);
        StudentsPanelModel m = new StudentsPanelModel(unit);
        category.setOkButtonListener(m);
        return new StudentsPanel(m);
    }

}
