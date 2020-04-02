package org.thespheres.betula.services.dav;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"activelock"})
@XmlRootElement(name = "lockdiscovery")
public class LockDiscovery {
    @XmlElement(name = "activelock")
    protected List<ActiveLock> activelock;

    public List<ActiveLock> getActiveLock() {
        if (activelock == null) {
            activelock = new ArrayList<>();
        }
        return this.activelock;
    }

}
