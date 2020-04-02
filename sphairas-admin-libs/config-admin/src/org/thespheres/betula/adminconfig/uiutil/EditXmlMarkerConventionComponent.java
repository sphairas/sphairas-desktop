/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig.uiutil;

import java.io.IOException;
import org.openide.util.Lookup;
import org.thespheres.betula.ui.swingx.treetable.NbSwingXTreeTableElement;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition;

/**
 *
 * @author boris.heithecker
 */
public abstract class EditXmlMarkerConventionComponent extends NbSwingXTreeTableElement {

    private EditXmlMarkerConventionTableModel children;
    private XmlMarkerConventionDefinition env;

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public EditXmlMarkerConventionComponent() {
        this.children = new EditXmlMarkerConventionTableModel();
//        final Action save = new SaveAction();
//        toolbar.add(save);
        setDropTarget(true);
//        this.treeTable.setRootVisible(true);
    }

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor"})
    public EditXmlMarkerConventionComponent(Lookup context) throws IOException {
        this();
        env = context.lookup(XmlMarkerConventionDefinition.class);
        if (env == null) {
            throw new IOException();
        }
        initializeComponent();
    }

//    @Override
//    protected Node getNodeDelegate() {
//        return env.getNodeDelegate();
//    }
//
    @Override
    protected void initializeComponent() throws IOException {
        super.initializeComponent();
        children.setEnv(env);
        setModel(children);
        setName(env.getDisplayName());
//        associateLookup(env.getNodeDelegate().getLookup());
    }

}
