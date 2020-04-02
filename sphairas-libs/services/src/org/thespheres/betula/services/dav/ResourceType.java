package org.thespheres.betula.services.dav;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"collection", "any"})
@XmlRootElement(name = "resourcetype")
public class ResourceType {

    protected Collection collection;
    @XmlAnyElement
    protected List<Element> any;

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection value) {
        this.collection = value;
    }

    public List<Element> getOther() {
        if (any == null) {
            any = new ArrayList<>();
        }
        return this.any;
    }

}
