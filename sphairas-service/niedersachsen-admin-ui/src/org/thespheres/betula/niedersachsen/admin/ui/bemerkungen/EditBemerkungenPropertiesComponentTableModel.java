/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.EventQueue;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Task;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author boris.heithecker
 */
@Messages({"EditBemerkungenPropertiesComponentTableModel.dirtyValue.default=Text fehlt noch."})
class EditBemerkungenPropertiesComponentTableModel extends AbstractTableModel {

    static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    static final NumberFormat NF = NumberFormat.getIntegerInstance();
    private EditableProperties current;
    private final List<String> keysSorted = new ArrayList<>();
    private EditBemerkungenEnv env;
    private final Map<String, PropertyNode> nodes = new HashMap<>();
    private final Set<String> removedKeys = new HashSet<>();

    static {
        NF.setMinimumIntegerDigits(4);
        NF.setGroupingUsed(false);
    }

    void initialize(final EditableProperties rum, final EditBemerkungenEnv env) {
        this.env = env;
        EventQueue.invokeLater(() -> doInitialize(rum));
    }

    PropertyNode nodeForRow(final int ri) {
        final String key = keysSorted.get(ri);
        return nodes.computeIfAbsent(key, PropertyNode::new);
    }

    private void doInitialize(final EditableProperties rum) {
        removedKeys.clear();
        keysSorted.clear();
        keysSorted.addAll(rum.keySet());
        this.current = rum;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return keysSorted.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int ri, int ci) {
        final PropertyNode n;
        try {
            n = nodeForRow(ri);
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
        if (ci == 0) {
            return n.getKeyFormatted();
        } else if (ci == 1 && current != null) {
            return n.getEditValue();
        }
        return null;
    }

    String getValueForKeyUnformatted(String key) {
        return current.get(key);
    }

    void updateForSave() {
        for (int i = 0; i < this.keysSorted.size(); i++) {
            final PropertyNode pn = nodeForRow(i);
            if (pn.isDirty()) {
                final String nv = pn.dirtyValue;
                current.setProperty(pn.getKey(), nv);
            }
        }
        this.removedKeys.stream()
                .forEach(current::remove);
    }

    void runAfterSave(final Task t) {
        Mutex.EVENT.writeAccess(() -> {
            for (int i = 0; i < this.keysSorted.size(); i++) {
                final PropertyNode pn = nodeForRow(i);
                if (pn.isDirty()) {
                    pn.setEditValue(null, false);
                }
            }
            removedKeys.clear();
            fireTableDataChanged();
        });
    }

    void addProperty() {
        int next = 1;
        for (int i = 0; i < this.keysSorted.size(); i++) {
            final PropertyNode pn = nodeForRow(i);
            if (pn.index >= next) {
                next = pn.index + 1;
            }
        }
        final PropertyNode nn = new PropertyNode(LocalDate.now(), next);
        this.keysSorted.add(nn.getKey());
        this.nodes.put(nn.getKey(), nn);
        final String text = NbBundle.getMessage(EditBemerkungenPropertiesComponentTableModel.class, "EditBemerkungenPropertiesComponentTableModel.dirtyValue.default");
        nn.setEditValue(text, true);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    void removeProperty(final String key) {
        EventQueue.invokeLater(() -> {
            this.keysSorted.remove(key);
            this.removedKeys.add(key);
            this.nodes.remove(key);
            env.setModified("properties");
            this.fireTableStructureChanged();
        });
    }

    class PropertyNode extends AbstractNode {

        private final String key;
        private final InstanceContent lkp;
        private String dirtyValue;
        private String keyFormatted;
        private int index = -1;
        private LocalDate date = null;

        PropertyNode(final String key) {
            this(Children.LEAF, new InstanceContent(), key);
        }

        PropertyNode(final LocalDate date, final int index) {
            this(Children.LEAF, new InstanceContent(), date, index);
        }

        @SuppressWarnings(value = {"LeakingThisInConstructor"})
        private PropertyNode(Children children, InstanceContent lookup, String key) {
            super(children, new AbstractLookup(lookup));
            this.lkp = lookup;
            this.key = key;
            lkp.add(this);
            lkp.add(EditBemerkungenPropertiesComponentTableModel.this);
            initKeys();
        }

        @SuppressWarnings(value = {"LeakingThisInConstructor"})
        private PropertyNode(Children children, InstanceContent lookup, LocalDate date, int index) {
            super(children, new AbstractLookup(lookup));
            this.lkp = lookup;
            this.key = FORMAT.format(date) + NF.format(index);
            lkp.add(this);
            lkp.add(EditBemerkungenPropertiesComponentTableModel.this);
            this.date = date;
            this.index = index;
            NodeAction na;
        }

        private void initKeys() {
            if (key.length() == 12) {
                final String d = key.substring(0, 8);
                final String n = key.substring(8);
                try {
                    index = Integer.parseInt(n);
                    date = LocalDate.parse(d, FORMAT);
                } catch (Exception ex) {
                }
            }
        }

        String getKey() {
            return key;
        }

        String getEditValue() {
            return dirtyValue != null ? dirtyValue : getValueForKeyUnformatted(key);
        }

        void setEditValue(final String text, final boolean setModified) {
            final String trim = StringUtils.trimToNull(text);
            if (!Objects.equals(trim, getValueForKeyUnformatted(key))) {
                this.dirtyValue = trim;
                if (setModified) {
                    env.setModified("properties");
                }
            }
        }

        boolean isDirty() {
            return this.dirtyValue != null;
        }

        boolean isTemplate() {
            return getValueForKeyUnformatted(key) == null;
        }

        String getKeyFormatted() {
            if (keyFormatted == null) {
                if (index != -1 && date != null) {
                    try {
                        keyFormatted = DISPLAY.format(date) + " (" + Integer.toString(index) + ")";
                    } catch (Exception ex) {
                    }
                }
                if (keyFormatted == null) {
                    keyFormatted = key;
                }
            }
            return keyFormatted;
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Messages({"PropertyNode.destroy.warning.title=Hinweis",
            "PropertyNode.destroy.warning.message=Texte sollten nur gelöscht werden, wenn sie mit absoluter Sicherheit noch für kein erstelltes Zeugnis verwendet wurden."})
        @Override
        public void destroy() throws IOException {
            if (isTemplate()) {
                removeProperty(key);
            } else {
                final String title = NbBundle.getMessage(PropertyNode.class, "PropertyNode.destroy.warning.title");
                final String message = NbBundle.getMessage(PropertyNode.class, "PropertyNode.destroy.warning.message");
                final NotifyDescriptor dd = new NotifyDescriptor(message, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE, null, null);
                DialogDisplayer.getDefault().notify(dd);
                if ((int) dd.getValue() == JOptionPane.OK_OPTION) {
                    removeProperty(key);
                }
            }
        }
    }
}
