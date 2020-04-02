package org.thespheres.betula.services.dav;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"lockscope", "locktype", "depth", "owner", "timeout", "locktoken"})
@XmlRootElement(name = "activelock")
public class ActiveLock {

    @XmlElement(required = true)
    protected LockScope lockscope;
    @XmlElement(required = true)
    protected LockType locktype;
    @XmlElement(required = true)
    protected String depth;
    protected Owner owner;
    protected String timeout;
    @XmlElement(required = true)
    protected LockToken locktoken;

    public LockScope getLockscope() {
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

    public String getDepth() {
        return depth;
    }

    public void setDepth(String value) {
        this.depth = value;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner value) {
        this.owner = value;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String value) {
        this.timeout = value;
    }

    public LockToken getLockToken() {
        return locktoken;
    }

    public void setLockToken(LockToken value) {
        this.locktoken = value;
    }

}
