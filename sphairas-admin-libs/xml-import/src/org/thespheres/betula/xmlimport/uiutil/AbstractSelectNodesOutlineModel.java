/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.netbeans.swing.outline.CheckRenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.util.ImageUtilities;
import org.thespheres.betula.xmlimport.ImportItem;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public abstract class AbstractSelectNodesOutlineModel<T extends ImportItem & OutlineModelNode> implements CheckRenderDataProvider, RowModel {

    protected final DefaultMutableTreeNode root;
    protected Set<T> selected;
    protected Map<T, Set<T>> clones;
    protected final DefaultTreeModel treeModel;
    private final String[] columns;
    protected final String product;

    protected AbstractSelectNodesOutlineModel(String sourceProd, String[] columnId) {
        this.root = new DefaultMutableTreeNode();
        treeModel = new DefaultTreeModel(root, true);
        this.columns = columnId;
        this.product = sourceProd;
    }

    @Override
    public final int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int i) {
        return getColumnDisplayName(columns[i]);
    }

    protected abstract String getColumnDisplayName(String id);

    @Override
    public Class getColumnClass(int i) {
        return getColumnClass(columns[i]);
    }

    protected Class getColumnClass(String id) {
        return String.class;
    }

    @Override
    public Object getValueFor(Object o, int i) {
        return getValueFor(o, columns[i]);
    }

    protected abstract Object getValueFor(Object o, String id);

    @Override
    public Color getBackground(Object o) {
        return Color.WHITE;
    }

    @Override
    public Color getForeground(Object o) {
        return Color.BLACK;
    }

    @Override //TODO: in nb 8.1 not necessary?
    public Icon getIcon(Object o) {
        return isCheckable(o) ? ImageUtilities.loadImageIcon("org/thespheres/betula/xmlimport/resources/width1icon.png", true) : null;
    }

    public DefaultMutableTreeNode getRoot() {
        return root;
    }

    protected TreeModel getTreeModel() {
        return treeModel;
    }

    protected abstract T extractNode(Object o);

    @Override
    public boolean isCellEditable(Object o, int i) {
        return extractNode(o) != null;
    }

    @Override
    public boolean isCheckEnabled(Object o) {
        return true;
    }

    @Override
    public boolean isCheckable(Object o) {
        return extractNode(o) != null;
    }

    @Override
    public boolean isHtmlDisplayName(Object o) {
        return true;
    }

    @Override
    public Boolean isSelected(Object o) {
        T l = extractNode(o);
        if (l != null) {
            return selected.contains(l);
        }
        return false;
    }

    @Override
    public void setSelected(Object o, Boolean bln) {
        T l = extractNode(o);
        if (l != null) {
            if (bln) {
                addSelection(l);
            } else {
                removeSelection(l);
                clones.getOrDefault(l, Collections.EMPTY_SET)
                        .forEach(le -> removeSelection((T) le));
            }
        }
    }

    protected void addSelection(T l) {
        selected.add(l);
    }

    protected void removeSelection(T l) {
        selected.remove(l);
    }

    @Override
    public void setValueFor(Object o, int i, Object o1) {
    }

    @Override
    public String getDisplayName(Object o) {
        T l = extractNode(o);
        if (l != null) {
            return l.getSourceNodeLabel();
        } else {
            return o.toString();
        }
    }

    @Override
    public String getTooltipText(Object o) {
        return "";
    }
}
