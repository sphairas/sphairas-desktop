/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ticketui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbCollections;

/**
 *
 * @author boris.heithecker
 */
class SigneeType {

    private final String entitlement;
    private final String displayName;
    private boolean selected = true;
    private boolean selectable = true;

    private SigneeType(Action ac, String toolbar) {
        entitlement = (String) ac.getValue("org.thespheres.betula.admincontainer.action.SigneeAction.entitlement");
        displayName = ac.getValue(Action.NAME) != null ? (String) ac.getValue(Action.NAME) : toolbar;
    }

    static List<SigneeType> create() throws IOException {
        final FileObject folder = FileUtil.getConfigFile("TargetSigneesTopComponent/Toolbars");
        final List<SigneeType> ret = new ArrayList<>();
        if (folder == null) {
            return ret;
        }
        for (final FileObject ef : folder.getChildren()) {
            if (ef.isFolder()) {
                final String toolbar = (String) ef.getAttribute("displayName");
                for (FileObject f : NbCollections.iterable(ef.getChildren(true))) {
                    DataObject dob;
                    try {
                        dob = DataObject.find(f);
                    } catch (DataObjectNotFoundException donf) {
                        continue;
                    }
                    final InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
                    if (ic == null) {
                        continue;
                    }
                    try {
                        if (Action.class.isAssignableFrom(ic.instanceClass())) {
                            final Action ac = (Action) ic.instanceCreate();
                            if (ac.getValue("org.thespheres.betula.admincontainer.action.SigneeAction.entitlement") != null) {
                                final SigneeType type = new SigneeType(ac, toolbar);
                                final Object v = ac.getValue("org.thespheres.betula.admin.units.ticketui.fixed-selection");
                                if (v instanceof String) {
                                    if ("unselected".equals((String) v)) {
                                        type.setSelected(false);
                                        type.selectable = false;
                                    } else if ("selected".equals((String) v)) {
                                        type.setSelected(true);
                                        type.selectable = false;
                                    }
                                }
                                ret.add(type);

                            }
                        }
                    } catch (ClassNotFoundException ex) {
                    }
                }
            }

        }
        return ret;
    }

    public String getEntitlement() {
        return entitlement;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
