/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

@NbBundle.Messages({"KursauswahlVisualPanel.step.name=Kursauswahl"})
public abstract class AbstractSelectNodesVisualPanel<M extends AbstractSelectNodesOutlineModel> extends JPanel implements DocumentListener {

    protected final M model;
    protected final OutlineModel outlineModel;
    private final List<TreePath> expanded = new ArrayList<>();

    @SuppressWarnings("LeakingThisInConstructor")
    protected AbstractSelectNodesVisualPanel(M model, String nodesColumnLabel) {
        this.model = model;
        initComponents();
        outlineModel = DefaultOutlineModel.createOutlineModel(model.getTreeModel(), model, true);
        ((DefaultOutlineModel) outlineModel).setNodesColumnLabel(nodesColumnLabel);
        outline.setRenderDataProvider(model);
        outline.setModel(outlineModel);
//        searchPanel.addPatternMatcher(this);
        searchTextField.getDocument().addDocumentListener(this);
    }

    public M getSelectNodesOutlineModel() {
        return model;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(AbstractSelectNodesVisualPanel.class, "KursauswahlVisualPanel.step.name");
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateQuickSearch();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateQuickSearch();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateQuickSearch();
    }

    protected abstract void updateQuickSearch();

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        outline = new org.netbeans.swing.outline.Outline();
        toolbar = new javax.swing.JToolBar();
        expandButton = new javax.swing.JToggleButton();
        searchTextLabel = new org.jdesktop.swingx.JXLabel();

        setLayout(new java.awt.BorderLayout());

        scrollPane.setPreferredSize(new java.awt.Dimension(900, 400));

        outline.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        outline.setFillsViewportHeight(true);
        outline.setRootVisible(false);
        scrollPane.setViewportView(outline);

        add(scrollPane, java.awt.BorderLayout.CENTER);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        expandButton.setIcon(new javax.swing.ImageIcon(AbstractSelectNodesVisualPanel.class.getResource("/org/thespheres/betula/xmlimport/resources/document-tree.png")));
        expandButton.setFocusable(false);
        expandButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        expandButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        expandButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandButtonActionPerformed(evt);
            }
        });
        toolbar.add(expandButton);

        searchTextLabel.setLabelFor(searchTextField);
        org.openide.awt.Mnemonics.setLocalizedText(searchTextLabel, org.openide.util.NbBundle.getMessage(AbstractSelectNodesVisualPanel.class, "AbstractSelectNodesVisualPanel.searchTextLabel.text")); // NOI18N
        toolbar.add(searchTextLabel);

        searchTextField.setColumns(15);
        toolbar.add(searchTextField);

        add(toolbar, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void expandButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandButtonActionPerformed
        if (expandButton.isSelected()) {
            for (int i = 0; i < model.getRoot().getChildCount(); i++) {
                DefaultMutableTreeNode cNode = (DefaultMutableTreeNode) model.getRoot().getChildAt(i);
                TreePath tp = new TreePath(cNode.getPath());
                if (!outlineModel.getTreePathSupport().isExpanded(tp)) {
                    outlineModel.getTreePathSupport().expandPath(tp);
                    expanded.add(tp);
                }
            }
        } else if (!expanded.isEmpty()) {
            Iterator<TreePath> it = expanded.iterator();
            while (it.hasNext()) {
                TreePath tp = it.next();
                outlineModel.getTreePathSupport().collapsePath(tp);
                it.remove();
            }
        }
    }//GEN-LAST:event_expandButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton expandButton;
    protected org.netbeans.swing.outline.Outline outline;
    private javax.swing.JScrollPane scrollPane;
    protected final org.jdesktop.swingx.JXTextField searchTextField = new org.jdesktop.swingx.JXTextField();
    private org.jdesktop.swingx.JXLabel searchTextLabel;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    public static abstract class AbstractSelectNodesPanel<Data, M extends AbstractSelectNodesOutlineModel, C extends AbstractSelectNodesVisualPanel<M>> implements WizardDescriptor.Panel<Data> {

        /**
         * The visual component that displays this panel. If you need to access
         * the component from this class, just use getComponent().
         */
        private C component;

        // Get the visual component for the panel. In this template, the component
        // is kept separate. This can be more efficient: if the wizard is created
        // but never displayed, or not all panels are displayed, it is better to
        // create only those which really need to be visible.
        @Override
        public C getComponent() {
            if (component == null) {
                component = createComponent();
            }
            return component;
        }

        protected abstract C createComponent();

        @Override
        public HelpCtx getHelp() {
            // Show no Help button for this panel:
            return HelpCtx.DEFAULT_HELP;
            // If you have context help:
            // return new HelpCtx("help.key.here");
        }

        @Override
        public boolean isValid() {
            // If it is always OK to press Next or Finish, then:
            return true;
            // If it depends on some condition (form filled out...) and
            // this condition changes (last form field filled in...) then
            // use ChangeSupport to implement add/removeChangeListener below.
            // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public void readSettings(Data wiz) {
        }

        @Override
        public void storeSettings(Data wiz) {
//        getComponent().model.
        }

    }
}
