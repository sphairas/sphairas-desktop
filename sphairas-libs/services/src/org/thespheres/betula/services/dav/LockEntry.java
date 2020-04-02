package org.thespheres.betula.services.dav;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"lockscope", "locktype"})
@XmlRootElement(name = "lockentry")
public class LockEntry {

    @XmlElement(required = true)
    protected LockScope lockscope;
    @XmlElement(required = true)
    protected LockType  locktype;

    public LockScope getLockScope() {
        return lockscope;
    }

    public void setLockScope(LockScope value) {
        this.lockscope = value;
    }

    public LockType getLockType() {
        return locktype;
    }

    public void setLockType(LockType value) {
        this.locktype = value;
    }

}
