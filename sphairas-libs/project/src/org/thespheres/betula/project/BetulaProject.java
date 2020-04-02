package org.thespheres.betula.project;

//import org.thespheres.betula.project.actions.AddToDoActionProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.thespheres.betula.services.LocalFileProperties;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.project.impl.BetulaProjectCustomizer;
import org.thespheres.betula.project.impl.BetulaProjectLogicalView;
//import org.thespheres.betula.project.actions.SyncActionProvider;
//import org.thespheres.betula.project.sync.BackupManager;

public class BetulaProject implements Project {

    public static final String PROJECT_PROPERTIES_FILENAME = "project.properties";
    public static final String SPHAIRAS_PROJECT = ".sphairas-project";
    public static final String BACKUP_PATH = ".backup";
    public static final String ICONBASE = "org/thespheres/betula/project/local/resources/betulaproject16.png";
    private final FileObject projectDir;
    LogicalViewProvider logicalView = new BetulaProjectLogicalView(this);
    private final ProjectState state;
    private Lookup lkp;
    final LocalFileProperties properties;
    private final Listener listener;

    public BetulaProject(DataObject propsFile, FileObject projectDir, LocalFileProperties prop, ProjectState state) {
        this.projectDir = projectDir;
        this.state = state;
        this.properties = prop;
        this.listener = new Listener(propsFile);
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    @Override
    public synchronized Lookup getLookup() {

        if (lkp == null) {
//            TermSchedule ts = null;
//            ClassSchedule tts = null;
//            for (SchemeProvider p : Lookup.getDefault().lookupAll(SchemeProvider.class)) {
//                if (p.getScheme(Scheme.DEFAULT_SCHEME, TermSchedule.class) != null) {
//                    ts = p.getScheme(Scheme.DEFAULT_SCHEME, TermSchedule.class);
//                }
//                if (p.getScheme(Scheme.DEFAULT_SCHEME, ClassSchedule.class) != null) {
//                    tts = p.getScheme(Scheme.DEFAULT_SCHEME, ClassSchedule.class);
//                }
//            }
            Lookup initial = Lookups.fixed(new Object[]{
                this, //project spec requires a project be in its own lookup
                state, //allow outside code to mark the project as needing saving
                new ActionProviderImpl(), //Provides standard actions like Build and Clean
                new DemoDeleteOperation(),
                new DemoCopyOperation(this),
                new BetulaProjectInformation(this), //Project information implementation
                logicalView, //Logical view of project implementation
                properties,
                //                        new SimpleTimetableScheme(),
                //                ts,
                //                tts,
                new BetulaProjectCustomizer(this)
            });
            lkp = LookupProviderSupport.createCompositeLookup(initial, "Projects/org-thespheres-betula-project-local/Lookup");
        }
        return lkp;
    }

    public URI getConfigurationsPath() {
        FileObject ret = projectDir.getFileObject(SPHAIRAS_PROJECT + "/");
        ret = ret != null ? ret : projectDir;
        return ret.toURI();
    }

    public Path getBackupDir() throws IOException {
        Path ret = Paths.get(getConfigurationsPath()).resolve("backup");
        if (!Files.isDirectory(ret)) {
            Files.createDirectory(ret);
        }
        return ret;
    }

    private final class Listener implements PropertyChangeListener {

        private final DataObject data;

        private Listener(DataObject propsFile) {
            this.data = propsFile;
            this.data.addPropertyChangeListener(WeakListeners.propertyChange(this, null));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                if (!data.isValid()) {
                    OpenProjects.getDefault().close(Stream.of(BetulaProject.this).toArray(Project[]::new));
                }
            }
        }

    }

    private final class ActionProviderImpl implements ActionProvider {

        private final String[] supported = new String[]{
            ActionProvider.COMMAND_DELETE,
            ActionProvider.COMMAND_COPY,
            "close",
//            SyncActionProvider.COMMAND_BACKUP,
//            AddToDoActionProvider.COMMAND_ADD_TODO
        };

        @Override
        public String[] getSupportedActions() {
            return supported;
        }

        @Override
        public void invokeAction(String string, Lookup lookup) throws IllegalArgumentException {
            if (string.equalsIgnoreCase(ActionProvider.COMMAND_DELETE)) {
                DefaultProjectOperations.performDefaultDeleteOperation(BetulaProject.this);
            } else if (string.equalsIgnoreCase(ActionProvider.COMMAND_COPY)) {
                DefaultProjectOperations.performDefaultCopyOperation(BetulaProject.this);
            } 
//            else if (string.equalsIgnoreCase(SyncActionProvider.COMMAND_BACKUP)) {
//                getLookup().lookup(SyncActionProvider.class).performBackupAction(BetulaProject.this, lookup);
//            } else if (string.equalsIgnoreCase(AddToDoActionProvider.COMMAND_ADD_TODO)) {
//                getLookup().lookup(AddToDoActionProvider.class).performAddToDoAction(BetulaProject.this);
//            }
        }

        @Override
        public boolean isActionEnabled(final String command, Lookup lookup) throws IllegalArgumentException {
            switch (command) {
                case ActionProvider.COMMAND_DELETE:
                    return true;
                case ActionProvider.COMMAND_COPY:
                    return true;
//                case AddToDoActionProvider.COMMAND_ADD_TODO: {
//                    AddToDoActionProvider prov = getLookup().lookup(AddToDoActionProvider.class);
//                    return prov != null && prov.isEnabledToDoAction(BetulaProject.this);
//                }
//                case SyncActionProvider.COMMAND_BACKUP: {
//                    SyncActionProvider prov = getLookup().lookup(SyncActionProvider.class);
//                    return prov != null && prov.isEnabledBackupAction(BetulaProject.this, lookup);
//                }
                case "close":
                    return !Boolean.valueOf((String) properties.getProperty("disable.close.project", "false"));
                default:
                    break;
            }
            return false;
        }
    }

    private final class DemoDeleteOperation implements DeleteOperationImplementation {

        @Override
        public void notifyDeleting() throws IOException {
        }

        @Override
        public void notifyDeleted() throws IOException {
        }

        @Override
        public List<FileObject> getMetadataFiles() {
            List<FileObject> dataFiles = new ArrayList<>();
            return dataFiles;
        }

        @Override
        public List<FileObject> getDataFiles() {
            List<FileObject> dataFiles = new ArrayList<>();
            return dataFiles;
        }
    }

    private final class DemoCopyOperation implements CopyOperationImplementation {

        private final BetulaProject project;
        private final FileObject projectDir;

        public DemoCopyOperation(BetulaProject project) {
            this.project = project;
            this.projectDir = project.getProjectDirectory();
        }

        @Override
        public List<FileObject> getMetadataFiles() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public List<FileObject> getDataFiles() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void notifyCopying() throws IOException {
        }

        @Override
        public void notifyCopied(Project arg0, File arg1, String arg2) throws IOException {
        }
    }
}
