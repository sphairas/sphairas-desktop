/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.util.Map;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.thespheres.betula.adminconfig.ConfigNodeTopComponentNodeList;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"BemerkungenConfigNodeTopComponentNode.node.displayName=Zeugnisbemerkungen"})
class BemerkungenConfigNodeTopComponentNode extends ConfigNodeTopComponentNodeList {

    public static final String NAME = "niedersachsen-web-zeugnisbemerkungen";
    public static final String BEMERKUNGEN_CONFIG_NODE_POSITION_KEY = "BemerkungenConfigNodeTopComponentNode.position";
    private EditBemerkungenNode node;

    BemerkungenConfigNodeTopComponentNode(final String provider) {
        super(provider, NbPreferences.forModule(BemerkungenConfigNodeTopComponentNode.class).getInt(BEMERKUNGEN_CONFIG_NODE_POSITION_KEY, 5000));
    }

    @Override
    public Node getSingleNode() {
        if (node == null) {
            node = new EditBemerkungenNode();
        }
        return node;
    }

    class EditBemerkungenNode extends AbstractNode {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        EditBemerkungenNode() {
            super(Children.LEAF);
            final String dn = NbBundle.getMessage(BemerkungenConfigNodeTopComponentNode.class, "BemerkungenConfigNodeTopComponentNode.node.displayName");
            setName(dn);
            setIconBaseWithExtension("org/thespheres/betula/niedersachsen/admin/ui/resources/blue-document-sticky-note.png");
        }

        @Override
        public Action getPreferredAction() {
            return new EditBemerkungenAction(provider);
        }

    }

    @ConfigNodeTopComponentNodeList.Factory.Registration
    public static class BemerkungenConfigNodeTCNodeFactory extends ConfigNodeTopComponentNodeList.Factory<BemerkungenConfigNodeTopComponentNode> {

        private BemerkungenConfigNodeTCNodeFactory() {
            super(NAME);
        }

        @Override
        public BemerkungenConfigNodeTopComponentNode create(String provider, Map<String, Object> props) {
            return new BemerkungenConfigNodeTopComponentNode(provider);
        }

    }

}
