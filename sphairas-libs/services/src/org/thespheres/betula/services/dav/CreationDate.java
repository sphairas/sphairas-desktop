package org.thespheres.betula.services.dav;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"content"})
@XmlRootElement(name = "creationdate")
public class CreationDate {

    private static final DateTimeFormatter XML_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    @XmlMixed
    protected List<String> content;

    public CreationDate() {
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public CreationDate(String date) {
        setValue(date);
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public CreationDate(ZonedDateTime date) {
        setValue(date);
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

    public void setValue(ZonedDateTime value) {
        setValue(XML_DATE_FORMAT.format(value));
    }

    public String getValue() {
        List<String> l = getContent();
        return l.isEmpty() ? null : l.get(0);
    }

    public ZonedDateTime getValueAsZonedDate() {
        return ZonedDateTime.parse(getValue(), XML_DATE_FORMAT);
    }

}
