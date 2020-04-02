/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.text.CloneableEditor;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.thespheres.betula.journal.module.JournalDataObject;
import org.thespheres.betula.journal.model.JournalEditor;
import org.thespheres.betula.ui.swingx.AbstractTableElement;
import org.thespheres.betula.ui.util.UIUtilities;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractJounalElement extends AbstractTableElement {

    protected JournalDataObject obj;
    protected JournalTableSupport support;
    protected final JScrollPane scrollPane = new JScrollPane();
    private Lookup.Result<JournalEditor> ctEditorResult;
    private JournalEditor currentEditor;
    private final Listener listener = new Listener();

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    protected AbstractJounalElement() {
        setLayout(new BorderLayout());
        setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    protected AbstractJounalElement(Lookup lkp) {
        this();
        obj = lkp.lookup(JournalDataObject.class);
    }

    protected void initializeComponent() {
        obj.addPropertyChangeListener(listener);
        support = obj.getLookup().lookup(JournalTableSupport.class);
        ctEditorResult = obj.getLookup().lookupResult(JournalEditor.class);
        setCurrentEditor();
//        LookupListener weakl = WeakListeners.create(LookupListener.class, listener, ctEditorResult);
//        ctEditorResult.addLookupListener(weakl);
        ctEditorResult.addLookupListener(listener);
    }

    protected JournalEditor getCurrentEditor() {
        JournalEditor ret;
        synchronized (this) {
            ret = currentEditor;
        }
        return ret;
    }

    protected final void setCurrentEditor() {
        synchronized (this) {
            AbstractJounalElement.this.currentEditor = ctEditorResult.allInstances().stream()
                    .map(JournalEditor.class::cast)
                    .collect(CollectionUtil.singleOrNull());
        }
    }

    protected abstract void initTable();

    protected JLabel initLoading() {
        JLabel loadingLbl = new JLabel(NbBundle.getMessage(CloneableEditor.class, "LBL_EditorLoading")); // NOI18N
        loadingLbl.setOpaque(true);
        loadingLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLbl.setBorder(new EmptyBorder(new Insets(11, 11, 11, 11)));
        loadingLbl.setVisible(false);
        return loadingLbl;
    }

    @Override
    protected void updateName() {
        if (callback != null) {
            Mutex.EVENT.writeAccess(() -> {
                TopComponent tc = callback.getTopComponent();
                if (obj != null && obj.isValid()) {
                    Node n = obj.getNodeDelegate();
                    boolean modif = obj.isModified();
                    boolean readOnly = !obj.getPrimaryFile().canWrite();
                    String displayName = UIUtilities.findDisplayName(obj);
                    tc.setDisplayName(UIUtilities.annotateName(displayName, false, modif, readOnly));
                    tc.setHtmlDisplayName(UIUtilities.annotateName(displayName, true, modif, readOnly));
                }
            });
        }
    }

    @Override
    protected void activatedNodes(List<Node> sel) {
        if (obj.isValid()) {
            final List<Node> selection = new ArrayList<>(sel);
            selection.add(obj.getNodeDelegate());
            setActivatedNodes(selection.toArray(new Node[selection.size()]));
            setIcon(obj.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
            updateName();
        }
    }

    @Override
    public void componentClosed() {
        if (ctEditorResult != null) {
            ctEditorResult.removeLookupListener(listener);
        }
        if (obj != null) {
            obj.removePropertyChangeListener(listener);
        }
    }

    @Override
    protected JPopupMenu createPopup(int modelCol, int modelRow, Point p, MouseEvent e) {
        return null;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if (obj != null && obj.isValid()) {
            out.writeObject(obj);
            out.writeInt(table.getSelectedRow());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object oi = in.readObject();
        if (oi instanceof JournalDataObject) {
            obj = (JournalDataObject) oi;
            int sel = in.readInt();
            if (sel >= 0 && table.getRowCount() > sel) {
                table.getSelectionModel().setSelectionInterval(sel, sel);
            }
        }
        initializeComponent();
        activatedNodes(Collections.EMPTY_LIST);
    }

    private final class Listener extends NodeAdapter implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            setCurrentEditor();
            initTable();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateName();
        }

    }

}
