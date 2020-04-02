/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.navigatorui;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.Actions;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.admin.units.RemoteUnitsModel;

/**
 *
 * @author boris.heithecker
 */
@ActionReferences({
    @ActionReference(path = "Loaders/application/betula-remote-students/Actions", id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"), position = 10),
    @ActionReference(path = "Loaders/application/betula-remote-students/Actions", id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"), position = 1000000, separatorBefore = 990000)
})
public class RemoteStudentNode extends AbstractNode {

    private final RemoteStudent student;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public RemoteStudentNode(RemoteStudent student, RemoteUnitsModel model) {
        super(Children.LEAF, Lookups.fixed(student, model, model.getUnitOpenSupport()));
        this.student = student;
        setIconBaseWithExtension("org/thespheres/betula/admin/units/resources/user-medium.png");
        setDisplayName(student.getDirectoryName());
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
//        return NodeTransfer.createPaste(this);DefaultEditorKit
        return NodeTransfer.transferable(this, NodeTransfer.CLIPBOARD_COPY);
    }

    @Override
    public Action getPreferredAction() {
        return Actions.forID("Window", "org.thespheres.betula.ui.actions.ConfigPanelVisible");
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/application/betula-remote-students/Actions").stream()
                .toArray(Action[]::new);
    }

    @Override
    protected Sheet createSheet() {
        Sheet result = super.createSheet();
        //Sheet.Set  set = result.createPropertiesSet();
        Sheet.Set ps = Sheet.createPropertiesSet();
        ps.setDisplayName("Schülerdaten");
        ps.put(new PropertySupport.ReadOnly("pu", String.class, "Klasse", "Die Klasse des Schülers") {

            @Override
            public Object getValue() throws IllegalAccessException, InvocationTargetException {
                return student.getGender();
            }
        });

        result.put(ps);
        return result;

    }

}
