/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author boris.heithecker
 */
public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

    private final static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HHmm");

    @Override
    public LocalTime unmarshal(String v) throws Exception {
        return LocalTime.parse(v, DTF);
    }

    @Override
    public String marshal(LocalTime v) throws Exception {
        return v == null ? null : v.format(DTF);
    }

}
