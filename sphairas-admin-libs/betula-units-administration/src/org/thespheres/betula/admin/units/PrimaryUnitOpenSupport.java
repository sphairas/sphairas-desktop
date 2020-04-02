/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.RemoteUnitsModel.INITIALISATION;
import org.thespheres.betula.admin.units.impl.DocumentsModelImpl;
import org.thespheres.betula.admin.units.ui.PrimaryUnitNode;
import org.thespheres.betula.admin.units.util.OpenSupportProperties;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.util.Units;

/**
 *
 * @author boris.heithecker
 */
public final class PrimaryUnitOpenSupport extends AbstractUnitOpenSupport {

    private Object model;
    private Object namingResolverResult;
    private boolean modif = false;

    @SuppressWarnings("LeakingThisInConstructor")
    private PrimaryUnitOpenSupport(FileObject projectDir, UnitId id) {
        super(new Env(projectDir, id), new Props(projectDir, id));
    }

    public FileObject getProjectDirectory() {
        return ((Props) properties).getProjectDir();
    }

    public UnitId getUnitId() {
        return ((Env) env).unit;
    }

    private boolean isModified() {
        return modif;
    }

    private void setModified(boolean modif) {
        this.modif = modif;
    }

    @Override
    protected Lookup createLookup(Lookup base) {
        return LookupProviderSupport.createCompositeLookup(base, "Loaders/application/betula-unit-data/Lookup");
    }

    @Override
    protected Node createNodeDelegate() {
        return new PrimaryUnitNode(this);
    }

    @Override
    public Properties getLoadingProperties() {
        final Properties ret = super.getLoadingProperties();
        final boolean linked = NbPreferences.forModule(RemoteUnitsModel.class).getBoolean("always-load-linked", false);
        if (linked) {
            ret.put("linked", linked);
        }
        return ret;
    }

    @Override
    public RemoteUnitsModel getRemoteUnitsModel() throws IOException {
        return getRemoteUnitsModel(INITIALISATION.MAXIMUM);
    }

    public RemoteUnitsModel getRemoteUnitsModel(String prioritySuffix, TermId prioTerm) throws IOException {
        return getRemoteUnitsModel(INITIALISATION.PRIORITY, prioritySuffix, prioTerm);
    }

    @Override
    public synchronized RemoteUnitsModel getRemoteUnitsModel(final INITIALISATION stage) throws IOException {
        return getRemoteUnitsModel(stage, null, null);
    }

    public synchronized RemoteUnitsModel getRemoteUnitsModel(final INITIALISATION stage, final String prioritySuffix, TermId prioTerm) throws IOException {
        if (model == null) {
            try {
                model = createRemoteUnitsModel(new UnitId[]{getUnitId()});
            } catch (IOException ex) {
                model = ex;
            }
        }
        if (model instanceof IOException) {
            throw (IOException) model;
        }
        final RemoteUnitsModel ret = (RemoteUnitsModel) model;
        ret.initialize(stage, prioritySuffix, prioTerm);
        return ret;
    }

    public NamingResolver.Result findNamingResolverResult() throws IOException {
        if (namingResolverResult == null) {
            NamingResolver nr;
            try {
                nr = findNamingResolver();
                namingResolverResult = nr.resolveDisplayNameResult(getUnitId());
            } catch (IOException ex) {
                namingResolverResult = ex;
            } catch (IllegalAuthorityException ex) {
                namingResolverResult = new IOException(ex);
            }
            if (namingResolverResult == null) {
                namingResolverResult = new IOException("No naming resolver result found for project \"" + properties.getName() + "\".");
            }
        }
        if (namingResolverResult instanceof IOException) {
            throw (IOException) namingResolverResult;
        }
        return (NamingResolver.Result) namingResolverResult;
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        return MultiViews.createCloneableMultiView(MIME, (Env) env);
    }

    private static class Props extends OpenSupportProperties {

        private Object projectProperties;
        private final FileObject projectDir;
        private final UnitId puosUnitId;

        private Props(final FileObject dir, final UnitId id) {
            super(dir.getPath());
            this.projectDir = dir;
            this.puosUnitId = id;
        }

        public FileObject getProjectDir() {
            return projectDir;
        }

        @Override
        public LocalProperties findBetulaProjectProperties() throws IOException {
            if (projectProperties == null) {
                Project p = null;
                try {
                    p = ProjectManager.getDefault().findProject(getProjectDir());
                } catch (IllegalArgumentException iaex) {
                    projectProperties = new IOException(iaex);
                }
                if (p == null) {
                    projectProperties = new IOException("Project \"" + getName() + "\" not found.");
                } else {
                    final LocalFileProperties bpp = p.getLookup().lookup(LocalFileProperties.class);
                    if (bpp == null) {
                        projectProperties = new IOException("Project \"" + getName() + "\" is obviously not a Betula project.");
                    } else {
                        projectProperties = bpp;
                    }
                }
            }
            if (projectProperties instanceof IOException) {
                throw (IOException) projectProperties;
            }
            return (LocalFileProperties) projectProperties;
        }

        @Override
        protected DocumentsModel createDocumentsModel() throws IOException {
            try {
                final Units u = getUnits().get();
                DocumentsModel ret = new DocumentsModelImpl(u, puosUnitId);
                ret.initialize(findBetulaProjectProperties().getProperties());
                return ret;
            } catch (NoSuchElementException e) {
                throw new IOException(e);
            }
        }

    }

    public static class Registry {

        private final static Registry INSTANCE = new Registry();
        private final static HashSet<PrimaryUnitOpenSupport> OBJ = new HashSet<>();
        private final RequestProcessor RP = new RequestProcessor(Registry.class.getCanonicalName());
        private final ChangeSupport cSupport = new ChangeSupport(this);

        private Registry() {
        }

        public static Registry get() {
            return INSTANCE;
        }

        public PrimaryUnitOpenSupport find(final FileObject projectDir, final UnitId id) {
            synchronized (INSTANCE) {
                for (final PrimaryUnitOpenSupport ur : OBJ) {
                    final FileObject dir = ((Props) ur.properties).getProjectDir();
                    if (ur.getUnitId().equals(id) && dir.equals(projectDir)) {
                        return ur;
                    }
                }
                PrimaryUnitOpenSupport ur = new PrimaryUnitOpenSupport(projectDir, id);
                OBJ.add(ur);
                RP.post(cSupport::fireChange);
                return ur;
            }
        }

        public Set<PrimaryUnitOpenSupport> getRegistered() {
            synchronized (INSTANCE) {
                return new HashSet<>(OBJ);
            }
        }

        public void addChangeListener(ChangeListener listener) {
            cSupport.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            cSupport.removeChangeListener(listener);
        }

    }

    public static final class Env extends AbstractEnv {

        private static final long serialVersionUID = 1L;
        private final UnitId unit;
        private transient Lookup lookup;
        protected final FileObject projectDir;
        private transient PCL listener;

        @SuppressWarnings("LeakingThisInConstructor")
        Env(FileObject projectDir, UnitId unit) {
            this.projectDir = projectDir;
            this.unit = unit;
        }

        @Override
        protected PropertyChangeSupport getPropertyChangeSupport() {
            PropertyChangeSupport ret = super.getPropertyChangeSupport();
            if (listener == null) {
                synchronized (this) {
                    listener = new PCL();
                    try {
                        DataFolder.find(projectDir).addPropertyChangeListener(listener);
                    } catch (DataObjectNotFoundException ex) {
                    }
                }
            }
            return ret;
        }

        @Override
        public boolean isValid() {
            return projectDir.isValid()
                    && ProjectManager.getDefault().isProject(projectDir);
        }

        @Override
        public boolean isModified() {
            return findCloneableOpenSupport().isModified();
        }

        @Override
        public void markModified() throws IOException {
            findCloneableOpenSupport().setModified(true);
        }

        @Override
        public void unmarkModified() {
            findCloneableOpenSupport().setModified(false);
        }

        @Override
        public Lookup getLookup() {
            if (lookup == null) {
                lookup = Lookups.singleton(this);
            }
            return lookup;
        }

        @Override
        public PrimaryUnitOpenSupport findCloneableOpenSupport() {
            return PrimaryUnitOpenSupport.Registry.get().find(this.projectDir, this.unit);
        }

        private class PCL implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                    boolean before = (boolean) evt.getOldValue();
                    getPropertyChangeSupport().firePropertyChange(PROP_VALID, before, isValid());
                } else if (DataFolder.PROP_CHILDREN.equals(evt.getPropertyName())) {
                    getPropertyChangeSupport().firePropertyChange(PROP_VALID, null, isValid());
                }
            }
        }

    }
}
