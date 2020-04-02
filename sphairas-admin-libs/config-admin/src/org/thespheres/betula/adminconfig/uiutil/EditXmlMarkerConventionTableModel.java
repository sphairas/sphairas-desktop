/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig.uiutil;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.table.ColumnFactory;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.ui.swingx.treetable.NbSwingXTreeTableModel;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition;
import org.thespheres.betula.xmldefinitions.XmlMarkerConventionDefinition.XmlMarkerSubsetDefinition;
import org.thespheres.betula.xmldefinitions.XmlMarkerDefinition;

/**
 *
 * @author boris.heithecker
 */
class EditXmlMarkerConventionTableModel extends NbSwingXTreeTableModel implements ChangeListener {

    private final EditXmlMarkerConventionRootChildren children;
    private XmlMarkerConventionDefinition env;

    EditXmlMarkerConventionTableModel() {
        this(new EditXmlMarkerConventionRootChildren());
    }

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
        "LeakingThisInConstructor"})
    private EditXmlMarkerConventionTableModel(EditXmlMarkerConventionRootChildren root) {
        super(root.getRoot());
        this.children = root;
        children.setModel(this);
    }

    @Override
    public ColumnFactory createColumnFactory() {
        return new EditXmlMarkerConventionColumnFactory();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    EditXmlMarkerConventionRootChildren getChildren() {
        return children;
    }

    void setEnv(XmlMarkerConventionDefinition value) {
        env = value;
        children.setTemplate(env);
    }

    void setModified() {
        if (env != null) {
//            env.setModified("set");
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        final Node n = Visualizer.findNode(node);
        final XmlMarkerDefinition m = n.getLookup().lookup(XmlMarkerDefinition.class);
        final XmlMarkerSubsetDefinition el = n.getLookup().lookup(XmlMarkerSubsetDefinition.class);
        if (el != null && m != null) {
            return !Marker.isNull(m) && column != 0;
        } else if (el != null) {
            return column != 0;
        }
        return false;
    }

    @Override
    public Object getValueAt(Object node, int column) {
        final Node n = Visualizer.findNode(node);
        final XmlMarkerDefinition m = n.getLookup().lookup(XmlMarkerDefinition.class);
        final XmlMarkerSubsetDefinition el = n.getLookup().lookup(XmlMarkerSubsetDefinition.class);
        if (el != null && m != null) {
            return getValue(m, column);
        } else if (el != null) {
            return getValue(el, column);
        }
        return null;
    }

    private Object getValue(XmlMarkerSubsetDefinition el, int column) {
        switch (column) {
            case 0:
                return null;
            case 1:
                return el.getCategory();
            case 2:
                return null;
        }
        return null;
    }

    private Object getValue(XmlMarkerDefinition m, int column) {
        switch (column) {
            case 0:
                return null;
            case 1:
                return m.getLongLabel();
            case 2:
                return m.getShortLabel();
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        final Node n = Visualizer.findNode(node);
        final XmlMarkerDefinition m = n.getLookup().lookup(XmlMarkerDefinition.class);
        final XmlMarkerSubsetDefinition el = n.getLookup().lookup(XmlMarkerSubsetDefinition.class);
        if (el != null && m != null) {
//            setMarkerValue(m, column, value);
        } else if (el != null) {
            setMarkerValue(el, column, value);
        }
    }

    private void setMarkerValue(XmlMarkerSubsetDefinition el, int column, Object value) {
//        final Iterator<Tag> it = el.getDisplayHint().iterator();
//        switch (column) {
//            case 1:
//                final boolean h = (boolean) value;
//                el.setHidden(!h);
//                break;
//            case 2:
//                final Tag t = (Tag) value;
//                while (it.hasNext()) {
//                    if (it.next().getConvention().equals("de.halbjahre")) {
//                        it.remove();
//                    }
//                }
//                if (t != null) {
//                    el.getDisplayHint().add(t);
//                }
//                break;
//        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        children.update(); //refresh();
    }
}
