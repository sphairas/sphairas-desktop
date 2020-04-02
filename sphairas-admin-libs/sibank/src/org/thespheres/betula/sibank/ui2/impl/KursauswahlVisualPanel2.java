/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.ui2.impl;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.swing.etable.QuickFilter;
import org.openide.util.NbBundle;
import org.thespheres.betula.sibank.SiBankImportData;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.ui2.KursauswahlOutlineModel2;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.AbstractSelectNodesVisualPanel;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"KursauswahlVisualPanel2.nodesColumnLabel=Jahrgang/Klasse"})
class KursauswahlVisualPanel2 extends AbstractSelectNodesVisualPanel<KursauswahlOutlineModel2> {

    KursauswahlVisualPanel2() {
        super(new KursauswahlOutlineModel2(), NbBundle.getMessage(KursauswahlVisualPanel2.class, "KursauswahlVisualPanel2.nodesColumnLabel"));
    }

    @Override
    protected void updateQuickSearch() {
        outline.setQuickFilter(3, new AuswahlQuickFilter(searchTextField.getText()));
    }

    private class AuswahlQuickFilter implements QuickFilter {

        private final String[] pattern;

        private AuswahlQuickFilter(String pattern) {
            this.pattern = StringUtils.split(pattern, null);
        }

        @Override
        public boolean accept(Object val) {
            final String text;
            if (val instanceof String && !(text = (String) val).isEmpty()) {
                return Arrays.stream(pattern)
                        .allMatch(s -> StringUtils.containsIgnoreCase(text, s));
            }
            return true;
        }

    }

    public static class KursauswahlPanel2 extends AbstractSelectNodesPanel<SiBankImportData, KursauswahlOutlineModel2, KursauswahlVisualPanel2> {

        private boolean init;

        @Override
        protected KursauswahlVisualPanel2 createComponent() {
            return new KursauswahlVisualPanel2();
        }

        @Override
        public void readSettings(SiBankImportData wiz) {
            if (!init) {
                SiBankImportTarget config = (SiBankImportTarget) wiz.getProperty(AbstractFileImportAction.IMPORT_TARGET);
                getComponent().getSelectNodesOutlineModel().initialize(config, wiz);
                init = true;
            }
        }

    }
}
