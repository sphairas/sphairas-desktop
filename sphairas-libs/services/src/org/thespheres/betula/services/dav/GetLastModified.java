package org.thespheres.betula.services.dav;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"content"})
@XmlRootElement(name = "getlastmodified")
public class GetLastModified {
    
    private static final DateTimeFormatter RFC_1123_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");
    private static final DateTimeFormatter RFC_1036_DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, dd-MMM-yy HH:mm:ss zzz");
    private static final DateTimeFormatter ANSIC_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy");
    @XmlMixed
    protected List<String> content;
    
    public GetLastModified() {
    }
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public GetLastModified(String value) {
        setValue(value);
    }
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public GetLastModified(LocalDateTime value) {
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
    
    public void setValue(LocalDateTime value) {
        ZonedDateTime zdt = ZonedDateTime.of(value, ZoneId.of("GMT"));
        setValue(RFC_1123_DATE_FORMAT.format(zdt));
    }
    
    public String getValue() {
        List<String> l = getContent();
        return l.isEmpty() ? null : l.get(0);
    }
    
    public LocalDateTime getValueAsZonedDateTime() {
        return parseDate(getValue()).withZoneSameInstant(ZoneId.of("GMT")).toLocalDateTime();
    }
    
    public static ZonedDateTime parseDate(String date) {

        // http://www.squid-cache.org/mail-archive/squid-users/200307/0122.html
        // Some IE browsers send If-Modified-Since header with a length extension such as: Thu, 01 Sep 2011 00:48:38 GMT; length=347987
        int index = date.indexOf(';');
        if (index != -1) {
            date = date.substring(0, index).trim();
        }
        
        index = date.indexOf(',');
        try {
            switch (index) {
                case -1:
                    return ZonedDateTime.parse(date, ANSIC_DATE_FORMAT);
                case 3:
                    return ZonedDateTime.parse(date, RFC_1123_DATE_FORMAT);
                default:
                    return ZonedDateTime.parse(date, RFC_1036_DATE_FORMAT);
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
}
