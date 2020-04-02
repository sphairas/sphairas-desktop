/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import org.openide.nodes.Node;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.tag.State;

/**
 *
 * @author boris.heithecker
 */
public abstract class TargetAssessmentProvider extends AssessmentProvider<Grade> implements TargetAssessment<Grade, TargetAssessment.Listener<Grade>> {

    protected TargetAssessmentProvider(String id, State initial) {
        super(id, initial);
    }

    @Override
    protected TableColumnConfiguration createTableColumnConfiguration() {
        return new TargetTableColumnConfiguration(this);
    }

    @Override
    protected Node createNodeDelegate() {
        return new AssessmentProviderNode(this);
    }

}
