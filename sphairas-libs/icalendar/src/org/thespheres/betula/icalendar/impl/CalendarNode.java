/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar.impl;

import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author boris.heithecker
 */
@Messages({"CalendarNode.displayName=Termine"})
class CalendarNode extends AbstractNode {

    private boolean alert;

    CalendarNode(String name, Lookup lookup) {
        this(name, lookup, new ComponentChildren(lookup));
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    private CalendarNode(String name, Lookup lookup, ComponentChildren children) {
        super(Children.create(children, true), lookup);
        setName(name);
        setIconBaseWithExtension("org/thespheres/betula/icalendar/resources/calendar.png");
        setDisplayName(NbBundle.getMessage(CalendarNode.class, "CalendarNode.displayName"));
    }

    @Override
    public String getHtmlDisplayName() {
        if (alert) {
            return "<html><font color=\"RED\">" + getDisplayName() + "</font></html>";
        }
        return super.getHtmlDisplayName();
    }

    @Override
    public Action[] getActions(boolean context) {
        return super.getActions(context);
    }

    @NodeFactory.Registration(projectType = "org-thespheres-betula-project-local", position = 10000)
    public static class Factory implements NodeFactory {

        @Override
        public NodeList<?> createNodes(Project p) {
            ProjectInformation pi = ProjectUtils.getInformation(p);
            return NodeFactorySupport.fixedNodeList(new CalendarNode(pi.getName(), p.getLookup()));
        }

    }
}
