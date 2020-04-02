/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.services.CommonStudentProperties;
import org.thespheres.betula.services.CommonTargetProperties;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportWizardSettings;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns.DefaultMarkerColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn.Factory.Registration;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.utilities.AbstractLink;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides.AbstractItemListener;
import org.thespheres.betula.xmlimport.utilities.ColumnProperty;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"KursartColumn.columnName.kursart=Kursart"})
public class KursartColumn<IT extends ImportTarget & CommonDocuments & CommonStudentProperties & CommonTargetProperties, ITI extends ImportTargetsItem & ImportItem.CloneableImport> extends DefaultMarkerColumn<ITI, IT, AbstractImportWizardSettings<IT>, ImportTableModel<ITI, AbstractImportWizardSettings<IT>>> {

//    private AbstractSourceOverrides<ITI, ? extends AbstractItemListener, ? extends AbstractLink<UniqueSatzDistinguisher>, UniqueSatzDistinguisher, ?, ?> overrides;
    private AbstractSourceOverrides<ITI, ? extends AbstractItemListener, ? extends AbstractLink<String>, String, ?, ?> overrides;
    public static final String PROP_KURSART = "kursart";
    private final PCL listener = new PCL();
    private final Map<ITI, MarkerListener> markers = new HashMap<>();
    private ImportTableModel<ITI, AbstractImportWizardSettings<IT>> model;

    private KursartColumn() {
        super("kursart", 220, true, 100, true);
    }

    @Override
    public void initialize(IT configuration, AbstractImportWizardSettings<IT> wizard) {
        super.initialize(configuration, wizard);
        Object p = wizard.getProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES);
        if (p instanceof AbstractSourceOverrides) {
//            this.overrides = (AbstractSourceOverrides<SiBankKursItem, ?, ? extends AbstractLink<UniqueSatzDistinguisher>, UniqueSatzDistinguisher, ?, ?>) p;
            overrides = (AbstractSourceOverrides<ITI, ?, ? extends AbstractLink<String>, String, ?, ?>) p;
            overrides.addChangeListener(listener);
        } else if (p == null) {
            wizard.addPropertyChangeListener(listener);
        }
    }

    @Override
    protected MarkerConvention[] getMarkerConventions(IT configuration) {
        return configuration.getRealmMarkerConventions();
    }

    @Override
    public Marker getColumnValue(ITI il) {
        return markers.get(il).value();
    }

    @Override
    protected void initialize(ITI il) {
        super.initialize(il);
        final MarkerListener ml = new MarkerListener(il);
        markers.put(il, ml);
        final ChangeListener cl = WeakListeners.change(listener, null);
        il.getUniqueMarkerSet().addChangeListener(cl);
        initializeOverrides(il);
    }

    private synchronized boolean initializeOverrides(ITI il) {
        if (overrides != null) {
            Optional<? extends AbstractItemListener> o = overrides.findListener(il);
            if (o.isPresent()) {
                AbstractSourceOverrides.AbstractItemListener ll = o.get();
                if (ll.getTargetLink() != null) {
                    final AbstractLink l = ll.getTargetLink();
                    ColumnProperty cp = l.getNonDefaultProperty("kursart");
                    if (cp instanceof KursartOverride) {
                        final Marker m = ((KursartOverride) cp).getMarker();
                        if (m != null) {
                            il.getUniqueMarkerSet().add(m);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean setColumnValue(ITI il, Object value) {
        final Marker m = (Marker) value;
        if (m == null) {
            final Marker before = getColumnValue(il);
            if (before != null) {
                il.getUniqueMarkerSet().remove(before);
            }
        } else {
            il.getUniqueMarkerSet().add(m);
        }
        if (overrides != null) {
            Optional<? extends AbstractItemListener> o = overrides.findListener(il);
            if (o.isPresent()) {
                AbstractSourceOverrides.AbstractItemListener ll = o.get();
                if (ll.getTargetLink() != null) {
                    final AbstractLink l = ll.getTargetLink();
                    l.removeNonDefaultProperty("kursart");
                    if (m != null) {
                        l.setNonDefaultProperty(new KursartOverride(m));
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(KursartColumn.class, "KursartColumn.columnName.kursart");
    }

    @Override
    public void configureTableColumn(ImportTableModel<ITI, AbstractImportWizardSettings<IT>> model, TableColumnExt col) {
        super.configureTableColumn(model, col);
        model.registerTableUpdatingProperty(PROP_KURSART);
        this.model = model;
    }

    private class PCL implements PropertyChangeListener, ChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final AbstractImportWizardSettings wizard = (AbstractImportWizardSettings) evt.getSource();
            Object p = wizard.getProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES);
            if (p instanceof AbstractSourceOverrides) {
//                overrides = (AbstractSourceOverrides<SiBankKursItem, ?, ? extends AbstractLink<UniqueSatzDistinguisher>, UniqueSatzDistinguisher, ?, ?>) p;
                overrides = (AbstractSourceOverrides<ITI, ?, ? extends AbstractLink<String>, String, ?, ?>) p;
                overrides.addChangeListener(this);
                wizard.removePropertyChangeListener(this);
            }

        }

        @Override
        public void stateChanged(ChangeEvent e) {
            markers.forEach((k, v) -> v.stateChanged(null));
        }
    }

    private class MarkerListener implements ChangeListener {

        private final WeakReference<ITI> item;
        private Marker beforeValue;
        private boolean isCheckedOverrides;

        private MarkerListener(ITI il) {
            this.item = new WeakReference<>(il);
        }

        Marker value() {
            final ITI it = item.get();
            if (it != null) {
                if (!isCheckedOverrides) {
                    boolean result = initializeOverrides(it);
                    isCheckedOverrides = result;
                }
                return beforeValue = it.getUniqueMarkerSet().getUnique("kgs.schulzweige", "kgs.unterricht");
            }
            return null;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            final ITI it = item.get();
            if (it != null) {
                final Marker before = beforeValue;
                final PropertyChangeEvent evt = new PropertyChangeEvent(it, PROP_KURSART, before, value());
                model.propertyChange(evt);
            }
        }
    }

    @ImportTableColumn.Factory.Registrations({
        @Registration(component = "DefaultCreateDocumentsVisualPanel"),
        @Registration(component = "SiBankCreateDocumentsVisualPanel")})
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new KursartColumn();
        }

    }
}
