/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.Curriculum;
import org.thespheres.betula.curriculum.impl.CourseEntryChildren.EditBemerkungenSetRootNode;
import org.thespheres.betula.curriculum.impl.CurriculumTableColumn.SumColumn;
import org.thespheres.betula.ui.swingx.treetable.NbPluggableSwingXTreeTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"CurriculumTableModel.hierarchicalColumnHeader=Fach"})
class CurriculumTableModel extends NbPluggableSwingXTreeTableModel<Curriculum, CourseEntry> implements ChangeListener {

    private final CourseEntryChildren children;
//    private CurriculumDataObject env;

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
        "LeakingThisInConstructor"})
    private CurriculumTableModel(final CourseEntryChildren ch) {
        super(CurriculumTableModel.class.getName(), new EditBemerkungenSetRootNode(ch), createColumns());
        this.children = ch;
//        children.setModel(this);
    }

    CurriculumTableModel() {
        this(new CourseEntryChildren(null, null));
    }

    static Set<PluggableTableColumn<Curriculum, CourseEntry>> createColumns() {
        final HashSet<PluggableTableColumn<Curriculum, CourseEntry>> ret = new HashSet<>();
        ret.add(new SectionsColumn());
        ret.add(new SumColumn());
        return ret;
    }

    @Override
    protected Object getHierarchicalColumnHeader() {
        return NbBundle.getMessage(CurriculumTableModel.class, "CurriculumTableModel.hierarchicalColumnHeader");
    }

    @Override
    protected int getHierarchicalColumnWidth() {
        return 140;
    }

    CourseEntryChildren getChildren() {
        return children;
    }

    void setEnv(final CurriculumDataObject value) {
        final Curriculum curr = value.getLookup().lookup(Curriculum.class);
//        env = value;
        children.setCurriculum(curr, value);
        this.initialize(curr, value.getLookup());
    }

//    void setModified() {
//        if (env != null) {
//            env.setModified(true);
//        }
//    }
    @Override
    protected CourseEntry getItemAt(final Object node) {
        final Node n = Visualizer.findNode(node);
        return n.getLookup().lookup(CourseEntry.class);
    }

//    @Override
//    public boolean isCellEditable(Object node, int column) {
//        final Node n = Visualizer.findNode(node);
//        final Course m = n.getLookup().lookup(Course.class);
//        final CourseGroup el = n.getLookup().lookup(CourseGroup.class);
//        if (el != null && m != null) {
////            return !Marker.isNull(m.getMarker()) && column != 0 && column != 4;
//        } else if (el != null) {
////            return column != 0;
//        }
//        return false;
//    }
//
//    @Override
//    public Object getValueAt(Object node, int column) {
//        final Node n = Visualizer.findNode(node);
//        final Course m = n.getLookup().lookup(Course.class);
//        final CourseGroup el = n.getLookup().lookup(CourseGroup.class);
//        if (el != null && m != null) {
//            return getValue(m, column);
//        } else if (el != null) {
//            return getValue(el, column);
//        }
//        return null;
//    }
//    @Override
//    public void setValueAt(Object value, Object node, int column) {
//        final Node n = Visualizer.findNode(node);
//        final Course2 m = n.getLookup().lookup(Course2.class);
//        final CourseGroup el = n.getLookup().lookup(CourseGroup.class);
//        if (el != null && m != null) {
//            setMarkerValue(m, column, value);
//        } else if (el != null) {
//            setMarkerValue(el, column, value);
//        }
//    }
    @Override
    public void stateChanged(ChangeEvent e) {
        children.update();
    }
}
