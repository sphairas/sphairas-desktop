/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.io.IOException;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.actions.OpenAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteUnitsModel;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.WorkingDate;

/**
 *
 * @author boris.heithecker
 */
public class PrimaryUnitNode extends AbstractNode implements ChangeListener { //, LookupListener {

    private final PrimaryUnitOpenSupport support;
    private NamingResolver.Result namingResult;
//    private static final RequestProcessor PROC = new RequestProcessor(PrimaryUnitNode.class.getCanonicalName(), 4, true);

    public PrimaryUnitNode(PrimaryUnitOpenSupport ur) {
        super(Children.LEAF, Lookups.singleton(ur));
        super.setName(ur.getUnitId().toString());
        setIconBaseWithExtension("org/thespheres/betula/admin/units/resources/table.png");
        this.support = ur;
//        PROC.post(this::initialize);
        initialize();
    }

    private void initialize() {
        try {
            namingResult = support.findNamingResolverResult();
            final WorkingDate wd = Lookup.getDefault().lookup(WorkingDate.class);
            wd.addChangeListener(this);
        } catch (IOException ex) {
        }
        stateChanged(null);
        try {
            support.getRemoteUnitsModel(RemoteUnitsModel.INITIALISATION.STUDENTS);
        } catch (IOException ex) {
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        String dName = null;
        if (namingResult != null) {
            dName = namingResult.getResolvedName();
        }
        if (dName == null) {
            dName = support.getUnitId().getId();
        }
        setDisplayName(dName);
    }

    @Override
    public Action getPreferredAction() {
        return OpenAction.get(OpenAction.class);
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/application/betula-unit-data/Actions").stream()
                .toArray(Action[]::new);
    }

}
