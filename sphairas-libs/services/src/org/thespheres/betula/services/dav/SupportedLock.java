package org.thespheres.betula.services.dav;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"lockentry"})
@XmlRootElement(name = "supportedlock")
public class SupportedLock {

    protected List<LockEntry> lockentry;

    public List<LockEntry> getLockEntry() {
        if (lockentry == null) {
            lockentry = new ArrayList<>();
        }
        return this.lockentry;
    }

}
