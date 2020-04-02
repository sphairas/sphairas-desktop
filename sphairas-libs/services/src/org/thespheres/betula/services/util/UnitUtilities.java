/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;

/**
 *
 * @author boris.heithecker
 */
public class UnitUtilities {

    public static List<UnitId> extractUnitList(Envelope node) {
//        List<UnitId> ret = new ArrayList<>();
//        node.getChildren().stream()
//                .filter((s) -> (s instanceof Entry))
//                .map((s) -> ((Entry) s).getIdentity())
//                .filter((i) -> (i instanceof UnitId))
//                .forEach((i) -> {
//                    ret.add((UnitId) i);
//                });
//        return ret;
        return node.getChildren().stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .map(Entry::getIdentity)
                .filter(UnitId.class::isInstance)
                .map(UnitId.class::cast)
                .distinct()
                .collect(Collectors.toList());
    }

    public static String queryEncodeUnitId(UnitId unit) {
        return new StringBuilder()
                .append("unit.id=").append(encode(unit.getId()))
                .append("&unit.authority=").append(encode(unit.getAuthority()))
                .toString();
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException();
        }
    }
}
