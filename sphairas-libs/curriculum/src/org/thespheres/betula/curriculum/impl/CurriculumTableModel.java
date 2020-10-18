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
import org.thespheres.betula.curriculum.impl.CourseEntryChildren.CourseEntriesRootNode;
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
        super(CurriculumTableModel.class.getName(), new CourseEntriesRootNode(ch), createColumns());
        this.children = ch;
//        children.setModel(this);
    }

    CurriculumTableModel() {
        this(new CourseEntryChildren(null));
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
        children.setCurriculum(curr, value);
        this.initialize(curr, value.getLookup());
    }

    @Override
    protected CourseEntry getItemAt(final Object node) {
        final Node n = Visualizer.findNode(node);
        return n.getLookup().lookup(CourseEntry.class);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        children.update();
    }
}
