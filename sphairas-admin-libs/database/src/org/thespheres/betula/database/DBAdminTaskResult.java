/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.database;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "db-admin-task")
//@XmlType(propOrder = {"path", "files", "messages", "version"})
@XmlAccessorType(XmlAccessType.FIELD)
public class DBAdminTaskResult {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name = "return-message")
    private String message;

    public DBAdminTaskResult() {
    }

    public DBAdminTaskResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
