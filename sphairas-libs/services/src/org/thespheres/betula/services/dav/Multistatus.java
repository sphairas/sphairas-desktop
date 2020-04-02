package org.thespheres.betula.services.dav;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"response", "responseDescription"})
@XmlRootElement(name = "multistatus")
public class Multistatus {

    @XmlElement(name = "response", required = true)
    protected List<Response> response;
    @XmlElement(name = "responsedescription")
    protected String responseDescription;

    public List<Response> getResponses() {
        if (response == null) {
            response = new ArrayList<>();
        }
        return this.response;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public void setResponseDescription(String value) {
        this.responseDescription = value;
    }

}
