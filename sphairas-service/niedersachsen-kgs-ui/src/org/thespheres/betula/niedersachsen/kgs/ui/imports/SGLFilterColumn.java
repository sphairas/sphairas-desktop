/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openide.*;
import org.openide.util.NbBundle;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.services.CommonStudentProperties;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.uiutil.AbstractImportWizardSettings;
import org.thespheres.betula.xmlimport.uiutil.DefaultColumns.DefaultMarkerColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn.Factory.Registration;
import org.thespheres.betula.xmlimport.uiutil.ImportTableColumn.Factory.Registrations;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;
import org.thespheres.betula.xmlimport.uiutil.MultiMarkerColumnPanel;
import org.thespheres.betula.xmlimport.utilities.AbstractLink;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides.AbstractItemListener;
import org.thespheres.betula.xmlimport.utilities.ColumnProperty;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SGLFilterColumn.columnName=Filter (Schulzweig)",
    "SGLFilterColumn.multipleFilters.label=Mehrere Filter",
    "SGLFilterColumn.multipleFilters.dialogTitle=Filterkombination",
    "SGLFilterColumn.multipleFilters.displayContextName=Schulzweige"})
public class SGLFilterColumn<IT extends ImportTarget & CommonDocuments & CommonStudentProperties, ITI extends ImportTargetsItem & ImportItem.CloneableImport> extends DefaultMarkerColumn<ITI, IT, AbstractImportWizardSettings<IT>, ImportTableModel<ITI, AbstractImportWizardSettings<IT>>> {

    final static Marker MULTIPLE_FILTERS = new AbstractMarker("null", "MULTIPLE_FILTERS", null) {
        @Override
        public String getLongLabel(Object... formattingArgs) {
            return NbBundle.getMessage(SGLFilterColumn.class, "SGLFilterColumn.multipleFilters.label");
        }

    };
    private AbstractSourceOverrides<ITI, ? extends AbstractItemListener, ? extends AbstractLink<String>, String, ?, ?> overrides;
    protected IT configuration;
    protected boolean permitMultipleFilters = true;

    protected SGLFilterColumn() {
        super("sglfilter", 270, 100);
        this.prependNull = true;
    }

    @Override
    public void initialize(final IT configuration, final AbstractImportWizardSettings<IT> wizard) {
        super.initialize(configuration, wizard);
        this.configuration = configuration;
        final Object p = wizard.getProperty(AbstractSourceOverrides.USER_SOURCE_OVERRIDES);
        if (p instanceof AbstractSourceOverrides) {
            overrides = (AbstractSourceOverrides<ITI, ?, ? extends AbstractLink<String>, String, ?, ?>) p;
        }
    }

    @Override
    protected Marker[] getTags(IT configuration) {
        final Marker[] arr = super.getTags(configuration);
        final Marker[] ret = Arrays.copyOf(arr, arr.length + (permitMultipleFilters ? 2 : 1));
        ret[arr.length] = SGLFilter.NO_SGL;
        if (permitMultipleFilters) {
            ret[arr.length + 1] = MULTIPLE_FILTERS;
        }
        return ret;
    }

    @Override
    protected MarkerConvention[] getMarkerConventions(final IT configuration) {
        return configuration.getStudentCareerConventions();
    }

    @Override
    public Marker getColumnValue(final ITI il) {
        if (configuration != null) {
            final SGLFilter<IT, ITI> filter = il.getFilter("sglfilter", () -> createFilter(il));
            final Marker[] arr = filter.getFilterMarkers();
            if (arr == null) {
                return null;
            } else {
                switch (arr.length) {
                    case 0:
                        return null;
                    case 1:
                        return arr[0];
                    default:
                        return createProxyMarker(il, arr);
                }
            }
        }
        return null;
    }

    @Override
    protected void initialize(final ITI il) {
        super.initialize(il);
        if (overrides != null) {
            Optional<? extends AbstractItemListener> o = overrides.findListener(il);
            if (o.isPresent()) {
                AbstractSourceOverrides.AbstractItemListener ll = o.get();
                if (ll.getTargetLink() != null) {
                    final AbstractLink l = ll.getTargetLink();
                    final ColumnProperty cp = l.getNonDefaultProperty("sglfilter");
                    if (cp instanceof SGLFilterOverride) {
                        final Marker[] m = ((SGLFilterOverride) cp).getFilters();
                        if (configuration != null) {
                            final SGLFilter filter = il.getFilter("sglfilter", () -> createFilter(il));
                            filter.setFilterMarkers(m);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean setColumnValue(ITI il, Object value) {
        final Marker m = (Marker) value;
        final Marker[] arr;
        if (m == null) {
            arr = null;
        } else if (m.equals(MULTIPLE_FILTERS)) {
            arr = showDialog(il);
        } else {
            arr = new Marker[]{m};
        }
        if (configuration != null) {
            final SGLFilter<IT, ITI> filter = il.getFilter("sglfilter", () -> createFilter(il));
            filter.setFilterMarkers(arr);
            if (overrides != null) {
                Optional<? extends AbstractItemListener> o = overrides.findListener(il);
                if (o.isPresent()) {
                    AbstractSourceOverrides.AbstractItemListener ll = o.get();
                    if (ll.getTargetLink() != null) {
                        final AbstractLink l = ll.getTargetLink();
                        l.removeNonDefaultProperty("sglfilter");
                        if (arr != null && arr.length != 0) {
                            l.setNonDefaultProperty(createOverride(filter));
                        }
                    }
                }
            }
        }
        return true;
    }

    protected SGLFilter<IT, ITI> createFilter(ITI il) {
        return new SGLFilter<>(il, configuration);
    }

    protected SGLFilterOverride createOverride(final SGLFilter<IT, ITI> filter) {
        throw new UnsupportedOperationException("When using overrides, SGLFilterColumn.createOverride must be implemented");
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SGLFilterColumn.class, "SGLFilterColumn.columnName");
    }

    Marker[] showDialog(final ITI item) {
        final MarkerConvention[] conventions = getMarkerConventions(configuration);
        if (conventions != null) {
            final Marker[] arr = Arrays.stream(conventions)
                    .flatMap(mc -> Arrays.stream(mc.getAllMarkers())
                    .sorted(Comparator.comparing(Marker::getLongLabel)))
                    .toArray(i -> new Marker[i + 1]);
            arr[arr.length - 1] = SGLFilter.NO_SGL;
            final String name = NbBundle.getMessage(SGLFilterColumn.class, "SGLFilterColumn.multipleFilters.displayContextName");
            final MultiMarkerColumnPanel panel = new MultiMarkerColumnPanel(arr, item.allMarkers(), name);
//        panel.model.initialize(null, Lookup.EMPTY);
            final DialogDescriptor dd = new DialogDescriptor(
                    panel, NbBundle.getMessage(SGLFilterColumn.class, "SGLFilterColumn.multipleFilters.dialogTitle"),
                    true,
                    DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION,
                    null);
            final Object result = DialogDisplayer.getDefault().notify(dd); //NOI18N
            if (DialogDescriptor.OK_OPTION == result) {
                return panel.getSelection();
            }
        }
        return null;
    }

    private Marker createProxyMarker(final ITI il, final Marker[] arr) {
        final String label = Arrays.stream(arr)
                .map(m -> m.getLongLabel())
                .collect(Collectors.joining(", "));
        return new AbstractMarker("null", il.getSourceNodeLabel(), null) {
            @Override
            public String getLongLabel(Object... formattingArgs) {
                return label;
            }

        };
    }

    @Registrations({
        @Registration(component = "DefaultCreateDocumentsVisualPanel")})
    public static final class Factory extends ImportTableColumn.Factory {

        @Override
        public ImportTableColumn createInstance() {
            return new SGLFilterColumn();
        }

    }
}
