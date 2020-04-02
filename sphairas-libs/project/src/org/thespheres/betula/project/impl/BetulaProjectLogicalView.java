package org.thespheres.betula.project.impl;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import javax.swing.Action;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.thespheres.betula.project.BetulaProject;
import org.thespheres.betula.services.AppPropertyNames;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.ProviderRegistry;

public class BetulaProjectLogicalView implements LogicalViewProvider {

    private final BetulaProject project;

    public BetulaProjectLogicalView(BetulaProject project) {
        this.project = project;
    }

    @Override
    public org.openide.nodes.Node createLogicalView() {

        final Children ch = NodeFactorySupport.createCompositeChildren(project, "Projects/org-thespheres-betula-project-local/Nodes");
        final FileObject fo = project.getProjectDirectory();
        final DataFolder dataobj = DataFolder.findFolder(fo);
        final Node node = dataobj.getNodeDelegate();
        return new BetulaProjectNode(node, ch, project);
    }

    @Override
    public Node findPath(Node root, Object target) {
        //leave unimplemented for now
        return null;
    }

    static boolean multipleOpenProviders() {
        return Arrays.stream(OpenProjects.getDefault().getOpenProjects())
                .map(p -> p.getLookup().lookup(LocalProperties.class))
                .filter(Objects::nonNull)
                .map(p -> p.getProperty(AppPropertyNames.LP_PROVIDER))
                .distinct()
                .count() > 1;
    }

    private static final class BetulaProjectNode extends FilterNode implements PropertyChangeListener {

        final BetulaProject project;
        private final ProjectInformation info;

        @SuppressWarnings("LeakingThisInConstructor")
        private BetulaProjectNode(Node node, org.openide.nodes.Children children, BetulaProject project) {
            super(node, children,
                    //The projects system wants the project in the Node's lookup.
                    //NewAction and friends want the original Node's lookup.
                    //Make a merge of both
                    new ProxyLookup(new Lookup[]{Lookups.singleton(project),
                node.getLookup()
            }));
            this.project = project;
            this.info = ProjectUtils.getInformation(project);//strong reference required!
            info.addPropertyChangeListener(this);
            this.setValue(PROP_NAME, node);
        }

        @Override
        public Action[] getActions(boolean arg0) {
            Action[] nodeActions = new Action[8];
            nodeActions[0] = CommonProjectActions.newFileAction();
            nodeActions[1] = CommonProjectActions.copyProjectAction();
            nodeActions[2] = CommonProjectActions.deleteProjectAction();
            nodeActions[5] = CommonProjectActions.setAsMainProjectAction();
            nodeActions[6] = CommonProjectActions.closeProjectAction();
            nodeActions[7] = CommonProjectActions.customizeProjectAction();
            return nodeActions;
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.icon2Image(ProjectUtils.getInformation(project).getIcon());
        }

        @Override
        public Image getOpenedIcon(int type) {
            return ImageUtilities.icon2Image(ProjectUtils.getInformation(project).getIcon());
        }

        @Override
        public String getDisplayName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }

        @Override
        public String getHtmlDisplayName() {
            final String displayName = getDisplayName();
            StringJoiner sj = new StringJoiner(" ", "<html>", "</html>");
            if (multipleOpenProviders()) {
                final String providerName = findProviderDisplayName();
                if (providerName != null) {
                    sj.add("<font color='AAAAAA'><i>" + providerName + "</i></font>");
                }
            }
            sj.add(displayName);
            return sj.toString();
        }

        private String findProviderDisplayName() throws NoProviderException {
            final String provider = project.getLookup().lookup(LocalProperties.class)
                    .getProperty(AppPropertyNames.LP_PROVIDER);
            try {
                return ProviderRegistry.getDefault().get(provider).getDisplayName();
            } catch (NoProviderException npe) {
                return provider;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            fireDisplayNameChange((String) evt.getOldValue(), (String) evt.getNewValue());
        }

    }

}
