package org.thespheres.betula.services.dav;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "prop")
public class DAVProp implements Prop {

    @XmlElement(name = "creationdate")
    protected CreationDate creationDate;
    @XmlElement(name = "displayname")
    protected DisplayName displayName;
    @XmlElement(name = "getcontentlanguage")
    protected GetContentLanguage getContentLanguage;
    @XmlElement(name = "getcontentlength")
    protected GetContentLength getContentLength;
    @XmlElement(name = "getcontenttype")
    protected GetContentType getContentType;
    @XmlElement(name = "getetag")
    protected GetETag getETag;
    @XmlElement(name = "getlastmodified")
    protected GetLastModified getLastModified;
    @XmlElement(name = "lockdiscovery")
    protected LockDiscovery lockDiscovery;
    @XmlElement(name = "resourcetype")
    protected ResourceType resourceType;
    @XmlElement(name = "supportedlock")
    protected SupportedLock supportedLock;
    @XmlAnyElement
    protected List<Element> any;

    public CreationDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(CreationDate value) {
        this.creationDate = value;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public void setDisplayName(DisplayName value) {
        this.displayName = value;
    }

    public GetContentLanguage getGetContentlanguage() {
        return getContentLanguage;
    }

    public void setGetContentlanguage(GetContentLanguage value) {
        this.getContentLanguage = value;
    }

    public GetContentLength getGetContentLength() {
        return getContentLength;
    }

    public void setGetContentLength(GetContentLength value) {
        this.getContentLength = value;
    }

    public GetContentType getGetContentType() {
        return getContentType;
    }

    public void setGetContentType(GetContentType value) {
        this.getContentType = value;
    }

    public GetETag getGetETag() {
        return getETag;
    }

    public void setGetETag(GetETag value) {
        this.getETag = value;
    }

    public GetLastModified getGetLastModified() {
        return getLastModified;
    }

    public void setGetLastModified(GetLastModified value) {
        this.getLastModified = value;
    }

    public LockDiscovery getLockDiscovery() {
        return lockDiscovery;
    }

    public void setLockDiscovery(LockDiscovery value) {
        this.lockDiscovery = value;
    }

    public ResourceType getResourcetype() {
        return resourceType;
    }

    public void setResourcetype(ResourceType value) {
        this.resourceType = value;
    }

    public SupportedLock getSupportedLock() {
        return supportedLock;
    }

    public void setSupportedLock(SupportedLock value) {
        this.supportedLock = value;
    }

    public List<Element> getOtherElements() {
        if (any == null) {
            any = new ArrayList<>();
        }
        return this.any;
    }

}
