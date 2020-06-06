/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.database.configui;

import java.io.IOException;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.thespheres.betula.adminconfig.AppResourcesConfigTableColumn;
import org.thespheres.betula.admin.database.configui.DefaultAppResourcesConfigTableModel.AppResourcesConfigColFactory;
import org.thespheres.betula.adminconfig.AppResourcesProperties;
import org.thespheres.betula.adminconfig.AppResourcesProperty;
import org.thespheres.betula.ui.util.AbstractPluggableTableModel;
import org.thespheres.betula.ui.util.PluggableTableColumn;

/**
 *
 * @author boris.heithecker
 */
public class DefaultAppResourcesConfigTableModel extends AbstractPluggableTableModel<AppResourcesProperties, AppResourcesProperty, PluggableTableColumn<AppResourcesProperties, AppResourcesProperty>, AppResourcesConfigColFactory> implements ChangeListener {

    private DefaultAppResourcesConfigTableModel(final Set<? extends PluggableTableColumn<AppResourcesProperties, AppResourcesProperty>> s) {
        super("AppResourcesConfigTableModel", s);
    }

    static DefaultAppResourcesConfigTableModel create(final String mime) {
        final Set<PluggableTableColumn<AppResourcesProperties, AppResourcesProperty>> s = AppResourcesConfigTableColumn.createDefaultSet();
        MimeLookup.getLookup(mime)
                .lookupAll(AppResourcesConfigTableColumn.Factory.class).stream()
                .map(AppResourcesConfigTableColumn.Factory::createInstance)
                .forEach(s::add);
        return new DefaultAppResourcesConfigTableModel(s);
    }

    protected void addDefaultToolbarActions(final JToolBar toolbar) {
        final ImageIcon saveImage = ImageUtilities.loadImageIcon("org/thespheres/betula/admin/database/resources/disk.png", true);
        final JButton saveButton = new JButton(saveImage);
        saveButton.addActionListener(e -> {
            final AppResourcesProperties p = getItemsModel();
            if (p != null) {
                try {
                    p.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        toolbar.add(saveButton);
        final ImageIcon addImage = ImageUtilities.loadImageIcon("org/thespheres/betula/admin/database/resources/plus-button.png", true);
        final JButton addButton = new JButton(addImage);
        addButton.addActionListener(e -> {
            final AppResourcesProperties p = getItemsModel();
            if (p != null) {
                p.createTemplateProperty();
            }
        });
        toolbar.add(addButton);
    }

    @Override
    public void initialize(final AppResourcesProperties m, final Lookup context) {
        if (this.model != null) {
            this.model.removeChangeListener(this);
        }
        super.initialize(m, context);
        this.model.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        fireTableStructureChanged();
    }

    @Override
    protected AppResourcesConfigColFactory createColumnFactory() {
        return new AppResourcesConfigColFactory();
    }

    @Override
    protected int getItemSize() {
        return model.size();
    }

    @Override
    protected AppResourcesProperty getItemAt(int row) {
        return model.getItemAt(row);
    }

    public class AppResourcesConfigColFactory extends PluggableColumnFactory {
    }
}
