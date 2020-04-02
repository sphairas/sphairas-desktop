package org.thespheres.betula.project.local.timetbl;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.URI;
import javax.swing.JComponent;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.project.BetulaProject;
import org.thespheres.betula.services.LocalFileProperties;

/**
 *
 * @author boris.heithecker
 */
@Messages({"TimetableCustomizer.displayName=Stundenplan"})
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-thespheres-betula-project-local", position = 20000)
public class TimetableCustomizer implements ProjectCustomizer.CompositeCategoryProvider {

    @Override
    public Category createCategory(Lookup context) {
        LocalFileProperties prop = context.lookup(LocalFileProperties.class);
        if (prop.getProperty("providerURL") == null) {
            final String dn = NbBundle.getMessage(TimetableCustomizer.class, "TimetableCustomizer.displayName");
            Category ret = ProjectCustomizer.Category.create("TimetableCustomizer", dn, null);
            return ret;
        }
        return null;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        final BetulaProject betula = context.lookup(BetulaProject.class);
        final URI config = betula.getConfigurationsPath();
        final Unit unit = context.lookup(Unit.class);
        final UnitId u = unit == null ? null : unit.getUnitId();
        return new TimetablePanel(config, u, category);
    }
}
