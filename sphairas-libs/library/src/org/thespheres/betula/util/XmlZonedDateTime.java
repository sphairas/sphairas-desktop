/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlZonedDateTime {

    @XmlAttribute(name = "zone-id")
    private String zoneId;
    @XmlElement(name = "date-time")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime dateTime;

    public XmlZonedDateTime() {
    }

    public XmlZonedDateTime(ZonedDateTime zdt) {
        this.zoneId =  zdt.getZone().getId();
        this.dateTime = zdt.toLocalDateTime();
    }

    public String getZoneId() {
        return zoneId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public ZonedDateTime getZonedDateTime() {
        return (dateTime != null && zoneId != null) ? ZonedDateTime.of(dateTime, ZoneId.of(zoneId)) : null;
    }

    public static class ZonedDateTimeAdapter extends XmlAdapter<XmlZonedDateTime, ZonedDateTime> {

        @Override
        public ZonedDateTime unmarshal(XmlZonedDateTime v) throws Exception {
            return v.getZonedDateTime();
        }

        @Override
        public XmlZonedDateTime marshal(ZonedDateTime v) throws Exception {
            return v == null ? null : new XmlZonedDateTime(v);
        }

    }
}
