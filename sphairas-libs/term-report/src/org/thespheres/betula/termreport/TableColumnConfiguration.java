/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.termreport;

import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.decorator.AlignmentHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *
 * @author boris.heithecker
 */
public class TableColumnConfiguration {

    public static final String PROP_ASSESSMENTPROVIDER = "assessment.provider";
    private final Highlighter centerHL = new AlignmentHighlighter(SwingConstants.CENTER);
    protected final AssessmentProvider provider;

    public TableColumnConfiguration(AssessmentProvider provider) {
        this.provider = provider;
    }

    public void configureTableColumn(TableModel m, TableColumnExt columnExt) {
        columnExt.putClientProperty(PROP_ASSESSMENTPROVIDER, provider);
//        columnExt.setHeaderValue(provider.getDisplayName());
        columnExt.setToolTipText(provider.getDisplayName());
        columnExt.addHighlighter(centerHL);
    }

    public String getString(Object value) {
        return value != null ? value.toString() : "";
    }
}
