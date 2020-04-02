/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.jms;

import org.thespheres.betula.StudentId;

/**
 *
 * @author boris.heithecker
 */
public class StudentEvent extends AbstractJMSEvent<StudentId> {

    public enum StudentEventType {

        ADD, REMOVE, CHANGE
    }
    private final StudentEventType type;

    public StudentEvent(final StudentId source, final StudentEventType type) {
        super(source);
        this.type = type;
    }

    public StudentEventType getType() {
        return type;
    }

}
