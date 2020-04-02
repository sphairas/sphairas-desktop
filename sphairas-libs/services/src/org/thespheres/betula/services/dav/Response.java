package org.thespheres.betula.services.dav;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "", propOrder = {"href", "status", "propstat", "error", "responsedescription"})
//@XmlRootElement(name = "response")
public class Response {

    @XmlElement(required = true)
    protected List<String> href;
    protected String status;
    protected List<PropStat> propstat;
    protected Error error;
    protected String responsedescription;

    public Response() {
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Response(String href) {
        getHref().add(href);
    }

    public List<String> getHref() {
        if (href == null) {
            href = new ArrayList<>();
        }
        return this.href;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public List<PropStat> getPropstat() {
        if (propstat == null) {
            propstat = new ArrayList<>();
        }
        return this.propstat;
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
