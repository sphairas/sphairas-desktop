package org.thespheres.betula.services.dav;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"content"})
@XmlRootElement(name = "getcontentlength")
public class GetContentLength {

    @XmlMixed
    protected List<String> content;

    public GetContentLength() {
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public GetContentLength(String value) {
        setValue(value);
    }

    private List<String> getContent() {
        if (content == null) {
            content = new ArrayList<>();
        }
        return this.content;
    }

    public void setValue(String value) {
        List<String> l = getContent();
        l.clear();
        if (value != null) {
            l.add(value);
        }
    }

    public String getValue() {
        List<String> l = getContent();
        return l.isEmpty() ? null : l.get(0);
    }

}
