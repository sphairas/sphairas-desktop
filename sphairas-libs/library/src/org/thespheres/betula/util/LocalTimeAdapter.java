/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.time.LocalTime;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author boris.heithecker
 */
public final class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

    @Override
    public final LocalTime unmarshal(final String v) throws Exception {
        return LocalTime.parse(v);
    }

    @Override
    public final String marshal(final LocalTime v) throws Exception {
        return v == null ? null : v.toString();
    }

}
