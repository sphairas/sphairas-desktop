/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.impl;

import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.model.XmlItem;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
public abstract class AbstractXmlCsvImportItem<I extends XmlItem> extends ImportTargetsItem {

    private final I source;
    private boolean selected;

    protected AbstractXmlCsvImportItem(final String sourceNode, final I source) {
        super(sourceNode);
        this.source = source;
    }

    protected AbstractXmlCsvImportItem(final String sourceNode, I source, String sourceSubject, String sourceSignee) {
        super(sourceNode, sourceSubject, sourceSignee);
        this.source = source;
    }

    public I getSource() {
        return source;
    }

    public Term getTerm() {
        return (Term) getClientProperty(ImportTargetsItem.PROP_SELECTED_TERM);
    }

    public ConfigurableImportTarget getConfiguration() {
        return (ConfigurableImportTarget) getClientProperty(PROP_IMPORT_TARGET);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}
