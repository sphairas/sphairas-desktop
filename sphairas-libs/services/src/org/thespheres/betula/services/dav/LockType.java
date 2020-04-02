package org.thespheres.betula.services.dav;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"write"})
@XmlRootElement(name = "locktype")
public class LockType {

    @XmlElement(required = true)
    protected Write write;

    public LockType() {
    }

    public static LockType createWrite() {
        final LockType ret = new LockType();
        ret.write = new Write();
        return ret;
    }

}
