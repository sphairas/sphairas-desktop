/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.action;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.undo.AbstractUndoableEdit;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.OutputWriter;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admincontainer.util.TargetsUtil;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentEntry;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerConvention;
import org.thespheres.betula.document.util.BaseTargetAssessmentEntry;
import org.thespheres.betula.document.util.GenericXmlDocument;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.document.util.XmlMarkerSet;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 *
 * @author boris.heithecker
 */
@Messages({"EditRemoteTargetMarkersEdit.missingMarkersInSelection.message=Vorsicht! Es können nicht alle bereits \nvorhandenen Markierungen anzeigt werden!"})
class EditRemoteTargetMarkersEdit extends AbstractUndoableEdit {

    private final AbstractUnitOpenSupport support;
    private final List<EditRemoteTargetMarkersImportTargetsItem> items;
    private final WebServiceProvider service;
    private MarkerSelection[] selection;
    private final ConfigurableImportTarget importTarget;

    EditRemoteTargetMarkersEdit(AbstractUnitOpenSupport uos, List<EditRemoteTargetMarkersImportTargetsItem> items, WebServiceProvider wsp) throws IOException {
        this.support = uos;
        this.items = items;
        this.service = wsp;
        importTarget = TargetsUtil.findCommonImportTarget(support);
    }

    List<MarkerSelection> createSelectionList() throws IOException {
        final List<MarkerSelection> ret = new ArrayList<>();
//        final LocalFileProperties lfp = support.findBetulaProjectProperties();
//        final MarkerConvention[] smc = lfp.getSubjectMarkerConventions();
        final MarkerConvention[] smc = importTarget.getSubjectMarkerConventions();
        Arrays.stream(smc)
                .flatMap(mc -> Arrays.stream(mc.getAllMarkers()))
                .sorted(Comparator.comparing(m -> m.getLongLabel(), Collator.getInstance(Locale.getDefault())))
                .map(MarkerSelection::new)
                .forEach(ret::add);
//        final MarkerConvention[] rmc = lfp.getRealmMarkerConventions();
        final MarkerConvention[] rmc = importTarget.getRealmMarkerConventions();
        Arrays.stream(rmc)
                .flatMap(mc -> Arrays.stream(mc.getAllMarkers()))
                .sorted(Comparator.comparing(m -> m.getLongLabel(), Collator.getInstance(Locale.getDefault())))
                .map(MarkerSelection::new)
                .forEach(ret::add);
        final Set<Marker> selected = ret.stream()
                .map(MarkerSelection::getMarker)
                .collect(Collectors.toSet());
        boolean warn = items.stream()
                .map(i -> Arrays.asList(i.original))
                .filter(selected::retainAll)
                .count() > 1l;
        ret.stream()
                .filter(ms -> selected.contains(ms.getMarker()))
                .forEach(ms -> ms.setSelected(true));
        if (warn) {
            final String msg = NbBundle.getMessage(EditRemoteTargetMarkersEdit.class, "EditRemoteTargetMarkersEdit.missingMarkersInSelection.message");
            final OutputWriter err = ImportUtil.getIO().getErr();
            err.println("=====================================================");
            err.println(msg);
            err.println("=====================================================");
        }
        return ret;
    }

    public List<EditRemoteTargetMarkersImportTargetsItem> getItems() {
        return items;
    }

    public MarkerSelection[] getSelection() {
        return selection;
    }

    public void setSelection(MarkerSelection[] selection) {
        this.selection = selection;
    }

    void runAction() {
        final MarkerSelection[] sel = getSelection();
        if (sel == null) {
            return;
        }
        final Set<Marker> markers = Arrays.stream(sel)
                .filter(MarkerSelection::isSelected)
                .map(MarkerSelection::getMarker)
                .collect(Collectors.toSet());
        final Set<String> conventions = Stream.concat(Arrays.stream(importTarget.getSubjectMarkerConventionNames()), Arrays.stream(importTarget.getRealmMarkerConventionNames()))
                .collect(Collectors.toSet());
        getItems().stream()
                .forEach(it -> {
                    //Find all original/before markers NOT affected/possibly edited by this action/edit
                    final Set<Marker> remove = Arrays.stream(it.original)
                            .filter(m -> conventions.contains(m.getConvention()))
                            .filter(m -> !markers.contains(m))
                            .collect(Collectors.toSet());
                    //Concat with new marker sets
                    final Marker[] update = markers.stream()
                            .toArray(Marker[]::new);
                    it.setAllMarkers(update);
                    if (!remove.isEmpty()) {
                        it.setRemoveMarkers(remove.stream()
                                .toArray(Marker[]::new));
                    }
                });
        final MarkerUpdater update = new MarkerUpdater(items.stream().toArray(EditRemoteTargetMarkersImportTargetsItem[]::new), service, null, null);
        service.getDefaultRequestProcessor().post(() -> {
            update.run();
            if (update.getException() == null) {
                Mutex.EVENT.writeAccess(() -> support.getUndoSupport().postEdit(this));
            }
        });
    }

    class MarkerUpdater extends TargetItemsUpdater<EditRemoteTargetMarkersImportTargetsItem> {

        MarkerUpdater(EditRemoteTargetMarkersImportTargetsItem[] impKurse, WebServiceProvider provider, Term current, List<UpdaterFilter> filters) {
            super(impKurse, provider, current, filters);
        }

        @Override
        protected void writeUnitEntry(final EditRemoteTargetMarkersImportTargetsItem iti, final UnitEntry entry) {
            super.writeUnitEntry(iti, entry);
            writeRemoveMarkers(iti, entry);
        }

        @Override
        protected void writeTargetEntry(final EditRemoteTargetMarkersImportTargetsItem iti, final BaseTargetAssessmentEntry<TermId, StudentId> tae, final TargetDocumentProperties td) {
            super.writeTargetEntry(iti, tae, td);
            writeRemoveMarkers(iti, tae);
        }

        protected void writeRemoveMarkers(final EditRemoteTargetMarkersImportTargetsItem iti, final DocumentEntry<GenericXmlDocument> entry) {
            final Marker[] rm = iti.getRemoveMarkers();
            if (rm != null) {
                final XmlMarkerSet xml = (XmlMarkerSet) entry.getValue().getMarkerSet();
                for (final Marker m : rm) {
                    xml.add(m, Action.ANNUL);
                }
            }
        }

    }

    public static class MarkerSelection {

        private final Marker marker;
        private boolean selected = false;

        MarkerSelection(Marker m) {
            this.marker = m;
        }

        public Marker getMarker() {
            return marker;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

    }
}
