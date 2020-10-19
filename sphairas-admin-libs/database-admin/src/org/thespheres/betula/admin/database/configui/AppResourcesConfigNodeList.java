/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.configui;

import java.util.Map;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.awt.Actions;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.adminconfig.ConfigNodeTopComponentNodeList;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"AppResourcesConfigNodeList.node.displayName=Konfiguration ({0})"})
public class AppResourcesConfigNodeList extends ConfigNodeTopComponentNodeList {

    static final int DELAY = 2000;
    public static final String NAME = "app-resources";
    public static final String APPRESOURCES_CONFIG_NODE_POSITION_KEY = "AppResourcesConfigNodeList.position";
    private final Node node;

    private AppResourcesConfigNodeList(final String provider) {
        super(provider, NbPreferences.forModule(AppResourcesConfigNodeList.class).getInt(APPRESOURCES_CONFIG_NODE_POSITION_KEY, 50000));
        node = new AppResourcesConfigNode();
    }

    @Override
    public Node getSingleNode() {
        return node;
    }

    class AppResourcesConfigNode extends AbstractNode {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        AppResourcesConfigNode(final AppResourcesFileChildren ch, final AppResourcesConfigOpenSupport os) {
            super(Children.create(ch, true), Lookups.fixed(ch, ch.dir, os, os.getEnv()));
            setName(NAME + ":" + provider);
            setIconBaseWithExtension("org/thespheres/betula/admin/database/resources/gear.png");
        }

        AppResourcesConfigNode() {
            this(AppResourcesFileChildren.createRoot(provider),  AppResourcesConfigOpenSupport.find(getProvider()));
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(AppResourcesConfigNodeList.class, "AppResourcesConfigNodeList.node.displayName", getProviderInfo().getDisplayName());
        }

        @Override
        public Action getPreferredAction() {
            return OpenAction.get(OpenAction.class);
        }

        @Override
        public Action[] getActions(boolean context) {
            final Action ua = Actions.forID("Tools", "org.thespheres.betula.admin.database.configui.UploadAction");
            return new Action[]{ua};
        }
    }

    @ConfigNodeTopComponentNodeList.Factory.Registration
    public static class AppResourcesConfigNodeListFactory extends ConfigNodeTopComponentNodeList.Factory<AppResourcesConfigNodeList> {

        public AppResourcesConfigNodeListFactory() {
            super(NAME);
        }

        @Override
        public AppResourcesConfigNodeList create(final String provider, final Map props) {
            return new AppResourcesConfigNodeList(provider);
        }

    }
}
