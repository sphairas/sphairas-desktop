/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.admin.units.ui.TargetsSelectionElement;
import org.thespheres.betula.admin.units.util.OpenSupportProperties;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.services.LocalProperties;

/**
 *
 * @author boris.heithecker
 */
public class TargetsSelectionElementEnv2 extends AbstractUnitOpenSupport.AbstractEnv implements Serializable, Lookup.Provider {

    private final String provider;
    private final List<DocumentId> targets = new ArrayList<>();
    private transient ChangeSupport cSupport;
    static Map<TargetsSelectionElementEnv2, TargetsSelectionOpenSupport> MAP = new HashMap<>();
    private transient Lookup lkp;

    public TargetsSelectionElementEnv2(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public List<DocumentId> getTargets() {
        return Collections.unmodifiableList(targets);
    }

    public void addTarget(final DocumentId target) {
        targets.add(target);
        getCSupport().fireChange();
    }

    public void addChangeListener(ChangeListener listener) {
        getCSupport().addChangeListener(listener);
    }

    private ChangeSupport getCSupport() {
        if (cSupport == null) {
            cSupport = new ChangeSupport(this);

        }
        return cSupport;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void markModified() throws IOException {
    }

    @Override
    public void unmarkModified() {
    }

    @Override
    public TargetsSelectionOpenSupport findCloneableOpenSupport() {
        return MAP.computeIfAbsent(this, k -> new TargetsSelectionOpenSupport(this));
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.singleton(this);
        }
        return lkp;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.provider);
        return 61 * hash + Objects.hashCode(this.targets);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TargetsSelectionElementEnv2 other = (TargetsSelectionElementEnv2) obj;
        if (!Objects.equals(this.provider, other.provider)) {
            return false;
        }
        return Objects.equals(this.targets, other.targets);
    }

    public static class TargetsSelectionOpenSupport extends AbstractUnitOpenSupport {

        private RemoteUnitsModel[] rum = new RemoteUnitsModel[]{null};

        TargetsSelectionOpenSupport(final TargetsSelectionElementEnv2 env) {
            super(env, new SelectionProps(env));
        }

        @Override
        protected CloneableTopComponent createCloneableTopComponent() {
            return MultiViews.createCloneableMultiView(TargetsSelectionElement.MIME, (TargetsSelectionElementEnv2) env);
        }

        @Override
        public RemoteUnitsModel getRemoteUnitsModel() throws IOException {
            return getRemoteUnitsModel(null);
        }

        @Override
        public RemoteUnitsModel getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION stage) throws IOException {
            if (rum[0] == null) {
                rum[0] = new RemoteUnitsModel(this, new UnitId[0], findProviderUrl()) {
                    @Override
                    protected DocumentId[] fetchTargetAssessmentDocuments(StudentId[] arr) throws IOException {
                        throw new UnsupportedOperationException("Not to be called.");
                    }

                    @Override
                    protected void doInit(String prioSuffix, RemoteUnitsModel.INITIALISATION stage, RemoteUnitsModel.Reload rl) throws IOException, InterruptedException {
                    }

                    @Override
                    protected void loadDocs(TermId term) throws IOException, InterruptedException {
                    }

                    @Override
                    protected long loadStudentsAndMarkers(UnitId unit, Map<UnitId, Set<StudentId>> students, Map<UnitId, Marker[]> markers) throws IOException {
                        return 0l;
                    }

                    @Override
                    protected RemoteStudent createRemoteStudent(StudentId sid) {
                        throw new UnsupportedOperationException("Not to be called.");
                    }

                    @Override
                    protected boolean isRemoteException(Exception ex) {
                        throw new UnsupportedOperationException("Not to be called.");
                    }

                };
            }
            return rum[0];
        }

        @Override
        protected Node createNodeDelegate() {
            return new AbstractNode(Children.LEAF) {
            };
        }

    }

    static class SelectionProps extends OpenSupportProperties {

        private final String provider;

        SelectionProps(final TargetsSelectionElementEnv2 env) {
            super(findName(env));
            this.provider = env.getProvider();
        }

        private static String findName(final TargetsSelectionElementEnv2 env) {
            return env.getTargets().stream()
                    .map(d -> d.toString())
                    .collect(Collectors.joining(" "));
        }

        @Override
        public LocalProperties findBetulaProjectProperties() throws IOException {
            return LocalProperties.find(provider);
        }

    }
}
