package org.thespheres.betula.services.dav;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"exclusive", "shared"})
@XmlRootElement(name = "lockscope")
public class LockScope {

    protected Exclusive exclusive;
    protected Shared shared;

    public LockScope() {
    }

    public boolean isExclusive() {
        return exclusive != null && shared == null;
    }

    public static LockScope createExclusive() {
        final LockScope ret = new LockScope();
        ret.exclusive = new Exclusive();
        return ret;
    }

    public static LockScope createShared() {
        final LockScope ret = new LockScope();
        ret.shared = new Shared();
        return ret;
    }
}
