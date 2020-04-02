/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.thespheres.betula.Convention;
import org.thespheres.betula.Tag;
import org.thespheres.betula.ui.AbstractConventionComboBox;

/**
 *
 * @author boris.heithecker
 */
class ConventionComboBoxModelExt extends AbstractConventionComboBox<Convention, Tag> {

    private Convention[] current;

    ConventionComboBoxModelExt() {
        super(null, true, true);
    }

    void setConventions(final Convention[] cvns) {
        if (Arrays.equals(cvns, current)) {
            return;
        }
        update(cvns == null ? Collections.EMPTY_LIST : Arrays.asList(cvns), cvns == null || cvns.length == 0 ? null : cvns[0].getName());
        current = cvns;
    }

    private void update(final List<Convention> l, final String precon) {
        removeAllElements();
        if (addNull) {
            addElement(null);
        }
        l.stream()
                .peek(ac -> {
                    if (precon == null || !ac.getName().equals(precon)) {
                        addElement(ac);
                    }
                })
                .forEachOrdered(ac -> {
                    allTags(ac).stream()
                            .forEach(this::addElement);
                });
        highlightPrefCon = l.size() > 1;
        preferredConvention = precon;
        fireContentsChanged(this, 0, getSize() - 1);
    }

    @Override
    protected List<Tag> allTags(Convention convention) {
        if (convention instanceof Iterable) {
            Iterable<?> it = (Iterable<?>) convention;
            return StreamSupport.stream(it.spliterator(), false)
                    .filter(Tag.class::isInstance)
                    .map(Tag.class::cast)
                    .collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    protected List<Convention> allConventions() {
        return Collections.EMPTY_LIST;
    }

}
