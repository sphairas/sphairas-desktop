/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.time.LocalDate;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author boris.heithecker
 */
public final class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public final LocalDate unmarshal(String v) throws Exception {
        return LocalDate.parse(v);
    }

    @Override
    public final String marshal(LocalDate v) throws Exception {
        return v == null ? null : v.toString();
    }

}
