/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.action;

import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import java.io.IOException;
import java.util.Map;
import org.openide.nodes.Node;
import org.openide.util.NbPreferences;
import org.thespheres.betula.adminconfig.ConfigNodeTopComponentNodeList;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"StudentsDBConfigNodeList.node.displayName=Schülerinnen/Schüler ({0})"})
public class StudentsDBConfigNodeList extends ConfigNodeTopComponentNodeList {

    public static final String NAME = "students-database";
    public static final String BEMERKUNGEN_CONFIG_NODE_POSITION_KEY = "StudentsDBConfigNodeList.position";
    private final Node node;

    StudentsDBConfigNodeList(final String provider) throws IOException {
        super(provider, NbPreferences.forModule(StudentsDBConfigNodeList.class).getInt(BEMERKUNGEN_CONFIG_NODE_POSITION_KEY, 10000));
        node = new StudentsDBNode();
    }

    @Override
    public Node getSingleNode() {
        return node;
    }

    class StudentsDBNode extends AbstractNode {

        @SuppressWarnings({"OverridableMethodCallInConstructor"})
        StudentsDBNode() throws IOException {
            super(Children.LEAF, Lookups.singleton(StudentsDBOpenSupport.find(getProvider())));
            final String dn = NbBundle.getMessage(StudentsDBConfigNodeList.class, "StudentsDBConfigNodeList.node.displayName", getProviderInfo().getDisplayName());
            setName(dn);
            setIconBaseWithExtension("org/thespheres/betula/admin/database/resources/users.png");
        }

        @Override
        public Action getPreferredAction() {
            return OpenAction.get(OpenAction.class);
        }

    }

    @ConfigNodeTopComponentNodeList.Factory.Registration
    public static class CurriculumConfigNodeFactory extends ConfigNodeTopComponentNodeList.Factory<StudentsDBConfigNodeList> {

        public CurriculumConfigNodeFactory() {
            super(NAME);
        }

        @Override
        public StudentsDBConfigNodeList create(final String provider, final Map props) {
            try {
                return new StudentsDBConfigNodeList(provider);
            } catch (IOException ex) {
                PlatformUtil.getCodeNameBaseLogger(StudentsDBConfigNodeList.class).log(LogLevel.INFO_WARNING, ex.getLocalizedMessage(), ex);
                return null;
            }
        }

    }
}
