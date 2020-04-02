/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.thespheres.betula.document.Document;
import org.thespheres.betula.document.util.GenericXmlDocument.XmlSigneeInfo;
import org.thespheres.betula.document.util.SigneeMapAdapter.XmlSigneeMap;

/**
 *
 * @author boris.heithecker
 */
public class SigneeMapAdapter extends XmlAdapter<XmlSigneeMap, Map<String, Document.SigneeInfo>> {

    @Override
    public Map<String, Document.SigneeInfo> unmarshal(XmlSigneeMap v) throws Exception {
        Map<String, Document.SigneeInfo> ret = new HashMap<>();
        for (XmlSigneeInfo i : v.signeeInfo) {
            ret.put(i.getEntitlement(), i);
        }
        return ret;
    }

    @Override
    public XmlSigneeMap marshal(Map<String, Document.SigneeInfo> v) throws Exception {
        final XmlSigneeInfo[] ret = v == null ? null : v.entrySet().stream().map(e -> {
            if (e.getValue() instanceof XmlSigneeInfo) {
                return e.getValue();
            } else {
                return XmlSigneeInfo.create(e.getKey(), e.getValue());
            }
        }).toArray(XmlSigneeInfo[]::new);
        return ret == null ? null : new XmlSigneeMap(ret);
    }

    public static final class XmlSigneeMap {

        @XmlElement(name = "signee-info")
        private XmlSigneeInfo[] signeeInfo;

        public XmlSigneeMap() {
        }

        private XmlSigneeMap(XmlSigneeInfo[] ret) {
            this.signeeInfo = ret;
        }
    }
}
