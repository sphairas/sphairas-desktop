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
import org.thespheres.betula.curriculum.CourseEntry;

/**
 *
 * @author boris.heithecker
 */
class CourseNode extends AbstractNode {

    CourseNode(final CourseEntry course) {
        super(Children.LEAF, Lookups.singleton(course));
    }

    @Override
    public Action[] getActions(boolean context) {
        return Utilities.actionsForPath("Loaders/text/curriculum-course-context/Actions").stream()
                .toArray(Action[]::new);
    }

}
