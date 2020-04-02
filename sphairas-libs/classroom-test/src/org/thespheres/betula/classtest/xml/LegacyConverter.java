package org.thespheres.betula.classtest.xml;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.thespheres.betula.util.Int2;

/**
 *
 * @author boris.heithecker
 */
@XmlTransient
public class LegacyConverter extends XmlAdapter<String, Double> {

    @Override
    public Double unmarshal(String v) throws Exception {
        if (!v.contains(".")) {
            return Int2.fromInternalValue(Integer.valueOf(v)).doubleValue();
        } else {
            return Double.valueOf(v);
        }
    }

    @Override
    public String marshal(Double v) throws Exception {
        return Double.toString(v);
    }

}
