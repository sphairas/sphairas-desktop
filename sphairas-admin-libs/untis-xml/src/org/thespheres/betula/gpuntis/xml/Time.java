//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.21 at 08:09:46 PM CEST 
//
package org.thespheres.betula.gpuntis.xml;

import org.thespheres.betula.gpuntis.xml.util.TimeAdapter;
import java.time.LocalTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.gpuntis.xml.Room.RoomRef;
import org.thespheres.betula.gpuntis.xml.Timeperiod.TimeperiodRef;
import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Time {

    @XmlElement(name = "assigned_timeperiod")
    protected TimeperiodRef timeperiods;
    @XmlElement(name = "assigned_day")
    protected int day;
    @XmlElement(name = "assigned_period")
    protected int period;
    @XmlJavaTypeAdapter(TimeAdapter.class)
    @XmlElement(name = "assigned_starttime")
    protected LocalTime starttime;
    @XmlJavaTypeAdapter(TimeAdapter.class)
    @XmlElement(name = "assigned_endtime")
    protected LocalTime endtime;
    @XmlAnyElement
    protected Element[] elements;
    @XmlElement(name = "assigned_room")
    protected RoomRef assignedRoom;

    public Room getAssignedRoom() {
        return assignedRoom != null ? assignedRoom.get() : null;
    }

    public int getDay() {
        return day;
    }

    public int getPeriod() {
        return period;
    }

    public LocalTime getStarttime() {
        return starttime;
    }

    public LocalTime getEndtime() {
        return endtime;
    }

}
