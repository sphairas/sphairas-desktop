/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.time.LocalDateTime;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author boris.heithecker
 */
public final class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    @Override
    public final LocalDateTime unmarshal(String v) throws Exception {
        return LocalDateTime.parse(v);
    }

    @Override
    public final String marshal(LocalDateTime v) throws Exception {
        return v == null ? null : v.toString();
    }

}
