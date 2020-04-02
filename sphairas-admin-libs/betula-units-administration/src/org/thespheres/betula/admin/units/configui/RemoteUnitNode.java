/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.configui;

import java.util.StringJoiner;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.Unit;

/**
 *
 * @author boris.heithecker
 */
//@ActionReferences({
//    @ActionReference(path = "Loaders/application/betula-remote-unit-context/Actions", id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"), position = 500)
//})
public class RemoteUnitNode extends AbstractNode {

    private final Unit unit;

    public RemoteUnitNode(Unit unit) {
        super(Children.LEAF, Lookups.singleton(unit));
        super.setName(unit.getUnitId().toString());
        super.setDisplayName(unit.getUnitId().getId());
        super.setIconBaseWithExtension("org/thespheres/betula/admin/units/resources/table-medium.png");
        this.unit = unit;
    }

    @Override
    public String getHtmlDisplayName() {
        StringJoiner sj = new StringJoiner(" ", "<html>", "</html>");
        sj.add("<font color='0000FF'>" + unit.getDisplayName() + "</font>");
        sj.add("<font color='AAAAAA'><i>" + unit.getUnitId().getId() + "</i></font>");
        return sj.toString();
    }

    @Override
    public Action getPreferredAction() {
        return null;
        //open topcomponent in output mode and show all participants
//        return OpenAction.get(OpenAction.class);
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/application/betula-remote-unit-context/Actions").stream()
                .map(Action.class::cast)
                .toArray(Action[]::new);
    }

//    @Override
//    public boolean canDestroy() {
//        return true;
//    }
//
//    @Override
//    public void destroy() throws IOException {
//        unit.destroy();
//    }
}
