/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.thespheres.betula.classtest.model.EditableStudent;

/**
 *
 * @author boris.heithecker
 */
//@ActionID(category = "Betula", id = "org.thespheres.betula.classtest.actions.CopyStudentAndScoresAction")
//@ActionRegistration(displayName = "#CopyStudentAndScoresAction.displayName")
//@ActionReferences({
//    @ActionReference(path = "Loaders/application/betula-classroomtest-student-context/Actions", position = 3000)})
//@NbBundle.Messages("CopyStudentAndScoresAction.displayName=Schüler/Schülerin kopieren")
public class CopyStudentAndScoresAction implements ActionListener {

    private final EditableStudent student;

    public CopyStudentAndScoresAction(EditableStudent student) {
        this.student = student;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
    }

}
