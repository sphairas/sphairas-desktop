/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.classtest.model.ClassroomTestEditor2;

@ActionID(category = "Betula", id = "org.thespheres.betula.classtest.actions.UpdateStudentsAction")
@ActionRegistration(displayName = "#UpdateStudentsAction.displayName")
@ActionReferences({
    @ActionReference(path = "Loaders/text/betula-classtest-file+xml/Actions", position = 3500)
})
@Messages("UpdateStudentsAction.displayName=Gruppe aktualisieren")
public class UpdateStudentsAction implements ActionListener {

    protected ClassroomTestEditor2 context;

    public UpdateStudentsAction(ClassroomTestEditor2 context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.updateStudents();
    }

}
