/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import javax.swing.table.TableModel;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.openide.loaders.XMLDataObject;

/**
 *
 * @author boris.heithecker
 * @param <M>
 */
public abstract class AbstractXmlSupport<M extends TableModel> extends BaseAbstractXmlSupport implements NavigatorLookupHint {

    protected M model;

    protected AbstractXmlSupport(final XMLDataObject xmldo) {
        super(xmldo);
    }

    @Override
    public abstract String getContentType();

    public abstract M getModel();

}
