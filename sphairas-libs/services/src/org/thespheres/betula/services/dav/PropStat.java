package org.thespheres.betula.services.dav;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"prop", "status", "error", "responsedescription"})
@XmlRootElement(name = "propstat")
public class PropStat {

    private static final String PROPSTAT_START = "HTTP/1.1 ";
//    @XmlElement(required = true)
    @XmlElements({
        @XmlElement(name = "prop", namespace = "DAV:", type = DAVProp.class),
        @XmlElement(name = "prop", namespace = "urn:ietf:params:xml:ns:carddav", type = CardDavProp.class)})
    protected Prop prop;
    @XmlElement(required = true)
    protected String status;
    protected Error error;
    protected String responsedescription;

    public Prop getProp() {
        return prop;
    }

    public void setProp(Prop value) {
        this.prop = value;
    }

    public String getStatus() {
        return status;
    }

    public int getStatusCode() {
        if (status == null || !status.startsWith(PROPSTAT_START)) {
            return -1;
        }
        return Integer.parseInt(status.substring(PROPSTAT_START.length(), PROPSTAT_START.length() + 3));
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error value) {
        this.error = value;
    }

    public String getResponseDescription() {
        return responsedescription;
    }

    public void setResponseDescription(String value) {
        this.responsedescription = value;
    }

}
