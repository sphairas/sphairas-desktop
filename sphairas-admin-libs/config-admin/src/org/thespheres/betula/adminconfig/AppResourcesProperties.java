/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * @author boris
 * @param <I>
 */
public abstract class AppResourcesProperties<I extends AppResourcesProperty> {

    protected final RequestProcessor RP = new RequestProcessor(AppResourcesProperties.class.getCanonicalName(), 5, true);
    protected final String provider;
    protected final List<I> items = new ArrayList<>();
    protected final ChangeSupport cSupport = new ChangeSupport(this);

    protected AppResourcesProperties(final String provider) {
        this.provider = provider;
    }

    public int size() {
        return items.size();
    }

    public I getItemAt(final int row) {
        return items.get(row);
    }

    public boolean isModified() {
        return items.stream()
                .anyMatch(AppResourcesProperty::isModified);
    }

    public boolean isReadOnly() {
        return true;
    }

    public abstract I createTemplateProperty();

    public abstract void save() throws IOException;

    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

}
