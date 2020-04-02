/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.database.DBAdminTask;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "submitTask", propOrder = {
    "task"
})
public class Submit {

    protected DBAdminTask task;

    public DBAdminTask getTask() {
        return task;
    }

    public void setTask(DBAdminTask value) {
        this.task = value;
    }
}
