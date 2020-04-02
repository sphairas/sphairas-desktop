package org.thespheres.betula.gpuntis.xml.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author boris.heithecker
 */
public class TimeAdapter extends XmlAdapter<String, LocalTime> {

    static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HHmm");
    
    @Override
    public LocalTime unmarshal(String v) throws Exception {
        return v == null ? null : LocalTime.parse(v, HM);
    }

    @Override
    public String marshal(LocalTime v) throws Exception {
        return v == null ? null :  HM.format(v);
    }
}
