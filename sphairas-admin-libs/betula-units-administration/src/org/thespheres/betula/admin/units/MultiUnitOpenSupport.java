/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import org.thespheres.betula.admin.units.xml.RemoteUnitDescriptor;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.configui.UnitSelector;
import org.thespheres.betula.admin.units.project.RemoteUnitDescriptorDataObject;
import org.thespheres.betula.admin.units.util.OpenSupportProperties;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.util.Units;

/**
 *
 * @author boris.heithecker
 */
public final class MultiUnitOpenSupport extends AbstractUnitOpenSupport {

    private Object model;
    private final UnitSelector descriptor;
    private final boolean modifIfNonFile = false;

    private MultiUnitOpenSupport(final AbstractEnv env, final UnitSelector descriptor, final OpenSupportProperties props) {
        super(env, props);
        this.descriptor = descriptor;
    }

    public static MultiUnitOpenSupport create(final RemoteUnitDescriptorDataObject dob, final RemoteUnitDescriptor desc) {
        if (dob.isValid()) {
            return new MultiUnitOpenSupport(new MultiEnv(dob), desc, new MultiUnitProperties(dob));
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Properties getLoadingProperties() {
        final Properties ret = super.getLoadingProperties();
        ret.put("linked", "true");
        return ret;
    }

    @Override
    protected Node createNodeDelegate() {
        if (env instanceof MultiEnv) {
            return ((MultiEnv) env).dataObj.getNodeDelegate();
        }
        throw new IllegalStateException();
    }

    @Override
    public RemoteUnitsModel getRemoteUnitsModel() throws IOException {
        return MultiUnitOpenSupport.this.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.MAXIMUM);
    }

    public RemoteUnitsModel getRemoteUnitsModel(String prioritySuffix, TermId prioTerm) throws IOException {
        return getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.PRIORITY, prioritySuffix, prioTerm);
    }

    @Override
    public synchronized RemoteUnitsModel getRemoteUnitsModel(final RemoteUnitsModel.INITIALISATION stage) throws IOException {
        return getRemoteUnitsModel(stage, null, null);
    }

    public synchronized RemoteUnitsModel getRemoteUnitsModel(final RemoteUnitsModel.INITIALISATION stage, String prioritySuffix, TermId prioTerm) throws IOException {
        if (model == null) {
            try {
                final Set<UnitId> all = Units.get(findWebServiceProvider().getInfo().getURL())
                        .map(Units::getUnits)
                        .orElse(Collections.EMPTY_SET);//RemoteUnitsLists.getList(findWebServiceProvider(), false);
                final UnitId[] units = all.stream()
                        .filter(descriptor::matches)
                        .toArray(UnitId[]::new);
                model = createRemoteUnitsModel(units);
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

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        return MultiViews.createCloneableMultiView(MIME, (AbstractEnv) env);
    }

    public String getDisplayName() {
        return descriptor.getDisplayName();
    }

    public static class MultiUnitProperties extends OpenSupportProperties {

        private Object projectProperties;
        private final DataObject file;

        public MultiUnitProperties(final DataObject d) {
            super("null");
            this.file = d;
        }

        @Override
        public String getName() {
            return project()
                    .map(p -> p.getProjectDirectory().getPath())
                    .orElse(file.getPrimaryFile().getPath());
        }

        private Optional<Project> project() {
            final Project p = FileOwnerQuery.getOwner(file.getPrimaryFile());
            return Optional.ofNullable(p);
        }

        @Override
        public LocalProperties findBetulaProjectProperties() throws IOException {
            if (projectProperties == null) {
                final Project p = project().orElse(null);
                if (p == null) {
//                    final RemoteUnitDescriptor rud = file.getLookup().lookup(RemoteUnitDescriptor.class);
//                    rud.getProvider(); find LocalFileProperties;
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

    }

    public static final class MultiEnv extends AbstractEnv {

        private static final long serialVersionUID = 1L;
        private transient Lookup lookup;
        private final DataObject dataObj;

        MultiEnv(DataObject file) {
            this.dataObj = file;
        }

        @Override
        public boolean isValid() {
            return dataObj != null && dataObj.isValid();
        }

        @Override
        public Lookup getLookup() {
            if (lookup == null) {
                lookup = Lookups.singleton(this);
            }
            return lookup;
        }

        @Override
        public MultiUnitOpenSupport findCloneableOpenSupport() {
            if (isValid()) {
                return dataObj.getLookup().lookup(MultiUnitOpenSupport.class);
            }
            return null;
        }

        @Override
        public boolean isModified() {
            return isValid() && dataObj.isModified();
        }

        @Override
        public void markModified() throws IOException {
            if (isValid()) {
                dataObj.setModified(true);
            }
        }

        @Override
        public void unmarkModified() {
            if (isValid()) {
                dataObj.setModified(false);
            }
        }

    }
//
//    public static final class FixedEnv extends AbstractEnv implements UnitSelector {
//
//        private static final long serialVersionUID = 1L;
//        private final static Map<FixedEnv, MultiUnitOpenSupport> OBJ = new HashMap<>();
//        private final String provider;
//        private final UnitId[] units;
//        private transient Lookup lookup;
//        private String name;
//
//        public FixedEnv(String provider, UnitId[] units) {
//            this.provider = provider;
//            this.units = units;
//        }
//
//        @Override
//        public boolean isValid() {
//            return true;
//        }
//
//        @Override
//        public Lookup getLookup() {
//            if (lookup == null) {
//                lookup = Lookups.singleton(this);
//            }
//            return lookup;
//        }
//
//        @Override
//        public MultiUnitOpenSupport findCloneableOpenSupport() {
//            synchronized (OBJ) {
//                return OBJ.computeIfAbsent(this, k -> new MultiUnitOpenSupport(this));
//            }
//        }
//
//        @Override
//        public boolean isModified() {
//            return findCloneableOpenSupport().isModified();
//        }
//
//        @Override
//        public void markModified() throws IOException {
//            findCloneableOpenSupport().setModified(true);
//        }
//
//        @Override
//        public void unmarkModified() {
//            findCloneableOpenSupport().setModified(false);
//        }
//
//        @Override
//        public String getDisplayName() {
//            if (name == null) {
//                final NamingResolver nr = NamingResolver.find(provider);
//                name = Arrays.stream(units)
//                        .map(u -> {
//                            try {
//                                return nr.resolveDisplayName(u);
//                            } catch (IllegalAuthorityException ex) {
//                                throw new IllegalStateException(ex);
//                            }
//                        })
//                        .collect(Collectors.joining(" "));
//            }
//            return name;
//        }
//
//        @Override
//        public boolean matches(UnitId unit) {
//            return Arrays.stream(units).anyMatch(unit::equals);
//        }
//
//        @Override
//        public int hashCode() {
//            int hash = 7;
//            hash = 61 * hash + Objects.hashCode(this.provider);
//            return 61 * hash + Arrays.deepHashCode(this.units);
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (this == obj) {
//                return true;
//            }
//            if (obj == null) {
//                return false;
//            }
//            if (getClass() != obj.getClass()) {
//                return false;
//            }
//            final FixedEnv other = (FixedEnv) obj;
//            if (!Objects.equals(this.provider, other.provider)) {
//                return false;
//            }
//            return Arrays.deepEquals(this.units, other.units);
//        }
//
//    }
}
