/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <T>
 * @param <W>
 * @param <M>
 */
@Messages({"MultiSubjectColumn.multipleSubjects.label=Fachkombination",
    "MultiSubjectColumn.multipleSubjects.displayContextName=Fächer"})
public abstract class MultiSubjectColumn<I extends ImportTargetsItem, T extends ImportTarget, W extends AbstractImportWizardSettings<T>, M extends ImportTableModel<I, W>> extends DefaultColumns.DefaultMarkerColumn<I, T, W, M> {

    protected boolean permitMultipleSubjects = true;

    final static Marker MULTIPLE_SUBJECTS = new AbstractMarker("null", "MULTIPLE_SUBJECTS", null) {
        @Override
        public String getLongLabel(Object... formattingArgs) {
            return NbBundle.getMessage(MultiSubjectColumn.class, "MultiSubjectColumn.multipleSubjects.label");
        }

    };
    private Marker[] elements;

    protected MultiSubjectColumn(int position, int width) {
        super("subject", position, width);
    }

    @Override
    public Marker getColumnValue(I il) {
        final Marker[] arr = il.getSubjectMarkers();
        switch (arr.length) {
            case 0:
                return null;
            case 1:
                return arr[0];
            default:
                return createProxyMarker(il, arr);
        }
    }

    @Override
    public boolean setColumnValue(I il, Object value) {
        final Marker mValue = (Marker) value;
        if (mValue == null || !mValue.equals(MULTIPLE_SUBJECTS)) {
            il.setSubjectMarker((Marker) value);
        } else {
            final Marker[] arr = showDialog(il);
            il.setSubjectMarker(arr);
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DefaultColumns.class, "DefaultColumns.defaultColumnName." + columnId());
    }

    @Override
    public void initialize(final T configuration, final W wizard) {
        elements = getTags(configuration);
        final DefaultComboBoxModel<Marker> fcbm = new DefaultComboBoxModel<>(elements);
        if (permitMultipleSubjects) {
            fcbm.addElement(MULTIPLE_SUBJECTS);
        }
        box.setModel(fcbm);
    }

    Marker[] showDialog(final I item) {
        final String name = NbBundle.getMessage(MultiSubjectColumn.class, "MultiSubjectColumn.multipleSubjects.displayContextName");
        final MultiMarkerColumnPanel panel = new MultiMarkerColumnPanel(elements, item.allMarkers(), name);
//        panel.model.initialize(null, Lookup.EMPTY);
        final DialogDescriptor dd = new DialogDescriptor(
                panel, NbBundle.getMessage(MultiSubjectColumn.class, "MultiSubjectColumn.multipleSubjects.label"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        final Object result = DialogDisplayer.getDefault().notify(dd); //NOI18N
        if (DialogDescriptor.OK_OPTION == result) {
            return panel.model.list.stream()
                    .filter(s -> s.isSelected())
                    .map(s -> s.getMarker())
                    .toArray(Marker[]::new);
        }
        return null;
    }

    private Marker createProxyMarker(final I il, final Marker[] arr) {
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

}
