/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.xml.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author boris.heithecker
 */
public class TimeSecondAdapter extends XmlAdapter<String, LocalTime> {
    
    static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HHmmss");

    @Override
    public LocalTime unmarshal(String v) throws Exception {
        return v == null ? null : LocalTime.parse(v, HMS);
    }

    @Override
    public String marshal(LocalTime v) throws Exception {
        return v == null ? null : HMS.format(v);
    }
    
}
