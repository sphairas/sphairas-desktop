/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.table.ColumnFactory;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.thespheres.betula.Tag;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerFactory;
import org.thespheres.betula.niedersachsen.zeugnis.Constants;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.Element;
import org.thespheres.betula.niedersachsen.zeugnis.TermReportNoteSetTemplate.MarkerItem;
import org.thespheres.betula.ui.swingx.treetable.NbSwingXTreeTableModel;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
class EditBemerkungenSetModel extends NbSwingXTreeTableModel implements ChangeListener {

    private final EditBemerkungenSetRootChildren children;
    private EditBemerkungenEnv env;

    EditBemerkungenSetModel() {
        this(new EditBemerkungenSetRootChildren());
    }

    @SuppressWarnings(value = {"OverridableMethodCallInConstructor",
        "LeakingThisInConstructor"})
    private EditBemerkungenSetModel(EditBemerkungenSetRootChildren root) {
        super(root.getRoot());
        this.children = root;
        children.setModel(this);
    }

    @Override
    public ColumnFactory createColumnFactory() {
        return new EditBemerkungenSetModelColumnFactory();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    EditBemerkungenSetRootChildren getChildren() {
        return children;
    }

    void setEnv(EditBemerkungenEnv value) {
        env = value;
        children.setTemplate(value.getTemplate(), env);
    }

    void addCategory(final String name, final boolean multiple) {
        final int index = env.getTemplate().getElements().size();
        if (multiple) {
            env.getTemplate().addElement(index, new Marker[0], name);
        } else {
            env.getTemplate().addElement(index, new Marker[0], 0, true, name);
        }
        setModified();
        children.update();
    }

    void setModified() {
        if (env != null) {
            env.setModified("set");
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        final Node n = Visualizer.findNode(node);
        final MarkerItem m = n.getLookup().lookup(MarkerItem.class);
        final Element el = n.getLookup().lookup(Element.class);
        if (el != null && m != null) {
            return !Marker.isNull(m.getMarker()) && column != 0 && column != 4;
        } else if (el != null) {
            return column != 0;
        }
        return false;
    }

    @Override
    public Object getValueAt(Object node, int column) {
        final Node n = Visualizer.findNode(node);
        final MarkerItem m = n.getLookup().lookup(MarkerItem.class);
        final Element el = n.getLookup().lookup(Element.class);
        if (el != null && m != null) {
            return getValue(m, column);
        } else if (el != null) {
            return getValue(el, column);
        }
        return null;
    }

    private Object getValue(Element el, int column) {
//        final ReportData2 sd = ReportContextListener.getDefault().getCurrentReportData();
        switch (column) {
            case 0:
                return null;
            case 1:
                return !el.isHidden();
            case 2:
                return getShowTerm(el.getDisplayHints());
            case 3:
                return getShowLevel(el.getDisplayHints());
            case 4:
                return el.getDisplayHints().contains(Constants.SCHLUSS_BEMERKUNG);
        }
        return null;
    }

    private Object getValue(MarkerItem m, int column) {
        final boolean nullMarker = Marker.isNull(m.getMarker());
        switch (column) {
            case 0:
                return null;
            case 1:
                return nullMarker ? null : !m.isHidden();
            case 2:
                return nullMarker ? Tag.NULL : getShowTerm(m.getDisplayHint());
            case 3:
                return nullMarker ? null : getShowLevel(m.getDisplayHint());
        }
        return null;
    }

    private Tag getShowTerm(final List<Tag> tags) {
        return tags.stream()
                .filter(t -> t.getConvention().equals("de.halbjahre"))
                .collect(CollectionUtil.singleton())
                .orElse(Tag.NULL);
    }

    private String getShowLevel(final List<Tag> tags) {
        return tags.stream()
                .filter(t -> t.getConvention().equals("de.stufen"))
                .map(Tag::getShortLabel)
                .collect(Collectors.joining(","));
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        final Node n = Visualizer.findNode(node);
        final MarkerItem m = n.getLookup().lookup(MarkerItem.class);
        final Element el = n.getLookup().lookup(Element.class);
        if (el != null && m != null) {
            setMarkerValue(m, column, value);
        } else if (el != null) {
            setElementValue(el, column, value);
        }
    }

    private void setElementValue(Element el, int column, Object value) {
        final Iterator<Tag> it = el.getDisplayHints().iterator();
        switch (column) {
            case 1:
                final boolean h = (boolean) value;
                el.setHidden(!h);
                setModified();
                break;
            case 2:
                final Tag t = (Tag) value;
                while (it.hasNext()) {
                    if (it.next().getConvention().equals("de.halbjahre")) {
                        it.remove();
                        setModified();
                    }
                }
                if (t != null) {
                    el.getDisplayHints().add(t);
                    setModified();
                }
                break;
            case 3:
                final String vv = (String) value;
                while (it.hasNext()) {
                    if (it.next().getConvention().equals("de.stufen")) {
                        it.remove();
                        setModified();
                    }
                }
                if (vv != null && !StringUtils.isBlank(vv)) {
                    for (final String iv : vv.split(",")) {
                        final Tag st = MarkerFactory.find("de.stufen", iv.trim());
                        if (st != null) {
                            el.getDisplayHints().add(st);
                            setModified();
                        }
                    }
                }
                break;
            case 4:
                final boolean last = (boolean) value;
                el.getDisplayHints().removeIf(Constants.SCHLUSS_BEMERKUNG::equals);
                if (last) {
                    el.getDisplayHints().add(Constants.SCHLUSS_BEMERKUNG);
                    setModified();
                }
        }
    }

    private void setMarkerValue(MarkerItem m, int column, Object value) {
        final Iterator<Tag> it = m.getDisplayHint().iterator();
        switch (column) {
            case 1:
                final boolean h = (boolean) value;
                m.setHidden(!h);
                setModified();
                break;
            case 2:
                final Tag t = (Tag) value;
                while (it.hasNext()) {
                    if (it.next().getConvention().equals("de.halbjahre")) {
                        it.remove();
                        setModified();
                    }
                }
                if (t != null) {
                    m.getDisplayHint().add(t);
                    setModified();
                }
                break;
            case 3:
                final String vv = (String) value;
                while (it.hasNext()) {
                    if (it.next().getConvention().equals("de.stufen")) {
                        it.remove();
                        setModified();
                    }
                }
                if (vv != null && !StringUtils.isBlank(vv)) {
                    for (final String iv : vv.split(",")) {
                        final Tag st = MarkerFactory.find("de.stufen", iv.trim());
                        if (st != null) {
                            m.getDisplayHint().add(st);
                            setModified();
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        children.update(); //refresh();
    }
}
