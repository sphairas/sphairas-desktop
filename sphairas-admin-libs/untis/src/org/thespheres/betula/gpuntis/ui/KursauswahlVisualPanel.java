/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.ui;

import java.util.Arrays;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.swing.etable.QuickFilter;
import org.openide.util.NbBundle;
import org.thespheres.betula.gpuntis.ImportedLesson;
import org.thespheres.betula.gpuntis.UntisImportData;
import org.thespheres.betula.xmlimport.uiutil.AbstractSelectNodesVisualPanel;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"KursauswahlVisualPanel.nodesColumnLabel=Lehrkräfte"})
class KursauswahlVisualPanel extends AbstractSelectNodesVisualPanel<KursauswahlOutlineModel> {

    KursauswahlVisualPanel() {
        super(new KursauswahlOutlineModel(), NbBundle.getMessage(KursauswahlVisualPanel.class, "KursauswahlVisualPanel.nodesColumnLabel"));
    }

    @Override
    protected void updateQuickSearch() {
        outline.setQuickFilter(0, new AuswahlQuickFilter(searchTextField.getText()));
    }

    private class AuswahlQuickFilter implements QuickFilter {

        private final String[] pattern;

        private AuswahlQuickFilter(String pattern) {
            this.pattern = StringUtils.split(pattern, null);
        }

        @Override
        public boolean accept(Object val) {
            val = ((DefaultMutableTreeNode) val).getUserObject();
            if (val instanceof ImportedLesson) {
                final String snl = ((ImportedLesson) val).getSourceNodeLabel();
                return Arrays.stream(pattern)
                        .allMatch(s -> StringUtils.containsIgnoreCase(snl, s));
            }
            return true;
        }

    }

    public static class KursauswahlPanel extends AbstractSelectNodesPanel<UntisImportData, KursauswahlOutlineModel, KursauswahlVisualPanel> {

        private boolean init;

        @Override
        protected KursauswahlVisualPanel createComponent() {
            return new KursauswahlVisualPanel();
        }

        @Override
        public void readSettings(UntisImportData wiz) {
            if (!init) {
                getComponent().getSelectNodesOutlineModel().initialize(wiz);
                init = true;
            }
        }

    }
}
