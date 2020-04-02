package org.thespheres.betula.project.impl;

import java.io.IOException;
import java.util.MissingResourceException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.project.BetulaProject;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;

@ServiceProvider(service = ProjectFactory.class)
public class BetulaProjectFactory implements ProjectFactory2 {

    public static final String PROJECT_FILE = "tsproject";
    public static final String PROJECT_ENDING = "properties";

    @Override
    public boolean isProject(FileObject dir) {
        if (dir.getAttribute("betula.project.extension") != null) {
            return false;
        }
        FileObject pfo = propertiesFile(dir);
        return pfo != null && pfo.isValid();
    }

    protected FileObject propertiesFile(FileObject dir) {
        FileObject pfo = dir.getFileObject(BetulaProject.SPHAIRAS_PROJECT + "/" + BetulaProject.PROJECT_PROPERTIES_FILENAME);
        //LEGACY
        if (pfo == null) {
            pfo = dir.getFileObject(PROJECT_FILE, PROJECT_ENDING);
        }
        return pfo;
    }

    @Override
    public Project loadProject(final FileObject dir, final ProjectState state) throws IOException {
        if (isProject(dir)) {
            final FileObject pfo = propertiesFile(dir);
            final DataObject dob = DataObject.find(pfo);
            if (pfo != null && dob != null && dob.isValid()) {
                return createProject(dir, pfo, dob, state);
            }
        }
        return null;
    }

    @Messages({"BetulaProjectFactory.NoProviderException.logMessage=Will not load project {0}, because no WebServiceProvider {1} could be found.",
        "BetulaProjectFactory.NoProviderException.userMessage=Kursmappe {0} wird nicht geladen, weil der Service-Provider {1} nicht gefunden wurde."})
    protected Project createProject(final FileObject dir, final FileObject pfo, final DataObject dob, final ProjectState state) throws IOException, MissingResourceException {
        final LocalFileProperties prop = new LocalFilePropertiesImpl(dir.getName(), pfo);
        final String prov = prop.getProperty("providerURL");
        if (prov != null) {
            try {
                WebProvider.find(prov, WebServiceProvider.class);
                //TODO: log, status line
            } catch (NoProviderException npex) {
                final String logMessage = NbBundle.getMessage(BetulaProjectFactory.class, "BetulaProjectFactory.NoProviderException.logMessage", dir.getPath(), prov);
                PlatformUtil.getCodeNameBaseLogger(BetulaProjectFactory.class).log(LogLevel.INFO_WARNING, logMessage);
                final String userMessage = NbBundle.getMessage(BetulaProjectFactory.class, "BetulaProjectFactory.NoProviderException.userMessage", dir.getName(), prov);
                StatusDisplayer.getDefault().setStatusText(userMessage, StatusDisplayer.IMPORTANCE_ANNOTATION);
                return null;
            }
        }
        return new BetulaProject(dob, dir, prop, state);
    }

    @Override
    public void saveProject(final Project project) throws IOException, ClassCastException {
        FileObject projectRoot = project.getProjectDirectory();
        if (projectRoot.getFileObject(PROJECT_FILE, PROJECT_ENDING) == null) {
            throw new IOException("Project dir " + projectRoot.getPath()
                    + " deleted,"
                    + " cannot save project");
        }
        //Force creation of the texts dir if it was deleted:
//        ((DemoProject) project).getTextFolder(true);
    }

    @Override
    public Result isProject2(FileObject d) {
        if (isProject(d)) {
            return new Result(ImageUtilities.loadImageIcon(BetulaProject.ICONBASE, true));
        } else {
            return null;
        }
    }
}
