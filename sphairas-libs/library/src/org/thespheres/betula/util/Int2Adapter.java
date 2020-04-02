/*
 * Int2Adapter.java
 *
 * Created on 1. November 2007, 09:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.thespheres.betula.util;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Boris Heithecker
 */
@XmlTransient
public class Int2Adapter extends XmlAdapter<Integer, Int2> {

    @Override
    public Int2 unmarshal(Integer v) throws Exception {
        return Int2.fromInternalValue(v);
    }

    @Override
    public Integer marshal(Int2 v) throws Exception {
        return v != null ? v.getInternalValue() : null;
    }
    
}
