/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.curriculum.Section;

/**
 *
 * @author boris.heithecker
 */
public class SectionNode extends AbstractNode {

    SectionNode(final Section section) {
        super(Children.LEAF, Lookups.singleton(section));
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/text/curriculum-section-context/Actions").stream()
                .toArray(Action[]::new);
    }
}
