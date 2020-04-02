/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.Lookup;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;

/**
 *
 * @author boris.heithecker
 */
public class MarkerComboBoxModel extends AbstractConventionComboBox<MarkerConvention, Marker> {

    public MarkerComboBoxModel() {
        super(null, true, true);
    }

    public MarkerComboBoxModel(String preferredConvention) {
        super(new String[]{preferredConvention}, false, true);
    }

    public MarkerComboBoxModel(String preferredConvention, boolean addNull) {
        super(new String[]{preferredConvention}, false, addNull);
    }

    public MarkerComboBoxModel(String[] conventions) {
        super(conventions, false, true);
    }

    public MarkerComboBoxModel(String[] conventions, boolean addNull) {
        super(conventions, false, addNull);
    }

    @Override
    protected List<MarkerConvention> allConventions() {
        return Lookup.getDefault().lookupAll(MarkerConvention.class).parallelStream()
                .map(MarkerConvention.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    protected List<Marker> allTags(MarkerConvention convention) {
        return Arrays.stream(convention.getAllMarkers())
                .map(Marker.class::cast)
                .collect(Collectors.toList());
    }

}
