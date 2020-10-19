/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.database;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "db-admin-task-result")
//@XmlType(propOrder = {"path", "files", "messages", "version"})
@XmlAccessorType(XmlAccessType.FIELD)
public class DBAdminTaskResult {

    @XmlAttribute(name = "success", required = true)
    private boolean success;
    @XmlValue
    private String message = "";

    public DBAdminTaskResult() {
    }

    public DBAdminTaskResult(final boolean success, final String message) {
        this.success = success;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

}
