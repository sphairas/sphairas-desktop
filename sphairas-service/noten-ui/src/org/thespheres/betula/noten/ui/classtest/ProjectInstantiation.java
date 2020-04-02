/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.ui.classtest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.project.BetulaProjectInstantiation;
import org.thespheres.betula.project.BetulaProjectType;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = BetulaProjectInstantiation.class)
public class ProjectInstantiation implements BetulaProjectInstantiation {

    @Override
    public void instatiate(Path project, Path sphairas, Properties prop, BetulaProjectType... type) throws IOException {
        final boolean create = NbPreferences.forModule(BetulaProjectInstantiation.class).getBoolean("create.default.target.folders", true);
        if (create && Arrays.stream(type).anyMatch(t -> t.equals(BetulaProjectType.LOCAL) || t.equals(BetulaProjectType.PROVIDER))) {
            final String folder = NbBundle.getMessage(NotenClasstestWizardIterator.class, NotenClasstestWizardIterator.PRESELECTED_TARGET_FOLDER);
            Files.createDirectories(project.resolve(folder));
        }
    }

}
