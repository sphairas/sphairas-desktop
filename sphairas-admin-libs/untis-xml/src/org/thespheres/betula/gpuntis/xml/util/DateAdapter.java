/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.xml.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author boris.heithecker
 */
public class DateAdapter extends XmlAdapter<String, LocalDate> {

    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public LocalDate unmarshal(String v) throws Exception {
        return v == null ? null : LocalDate.parse(v, DATE);
    }

    @Override
    public String marshal(LocalDate v) throws Exception {
        return v == null ? null : DATE.format(v);
    }

}
