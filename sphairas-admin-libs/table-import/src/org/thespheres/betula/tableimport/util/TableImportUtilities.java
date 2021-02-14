/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.util;

import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.xmlimport.model.XmlUnitItem;

/**
 *
 * @author boris.heithecker@gmx.net
 */
public class TableImportUtilities {

    private TableImportUtilities() {
    }

    public static Marker[] parseSourceMarkers(final XmlUnitItem source, final String useDefaultConvention) {
        if (source.getSourcesMarkers() == null || source.getSourcesMarkers().length == 0) {
            return new Marker[0];
        }
        return Arrays.stream(source.getSourcesMarkers())
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .map(s -> {
                    if (!s.contains("#") && useDefaultConvention != null) {
                        return useDefaultConvention + "#" + s;
                    }
                    return s;
                })
                .map(MarkerFactory::resolveAbstract)
                .toArray(Marker[]::new);
    }
}
