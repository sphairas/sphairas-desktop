/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.thespheres.betula.database.DBAdminTaskResult;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "submitTaskResponse", propOrder = {
    "_return"
})
public class SubmitResponse {

    @XmlElement(name = "return")
    protected DBAdminTaskResult _return;

    public DBAdminTaskResult getReturn() {
        return _return;
    }

    public void setReturn(DBAdminTaskResult value) {
        this._return = value;
    }
}
