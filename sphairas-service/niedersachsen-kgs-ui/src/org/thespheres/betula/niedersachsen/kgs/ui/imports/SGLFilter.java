/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports;

import java.util.Arrays;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.services.CommonStudentProperties;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.utilities.TargetDocumentProperties;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SGLFilter.noSGL.label=Ohne Schulzweig"})

public class SGLFilter<IT extends ImportTarget & CommonDocuments & CommonStudentProperties, ITI extends ImportTargetsItem & ImportItem.CloneableImport> implements UpdaterFilter<ITI, TargetDocumentProperties> {

    final static Marker NO_SGL = new AbstractMarker("niedersachsen.kgs.ui", "kgs.schulzweige.no.sgl", null) {
        @Override
        public String getLongLabel(Object... formattingArgs) {
            return NbBundle.getMessage(SGLFilter.class, "SGLFilter.noSGL.label");
        }
    };
    protected final ITI kurs;
    protected Marker[] filter;
    protected final SGLFilterValues values;
    private final RequestProcessor.Task init;

    public SGLFilter(final ITI item, final IT target) {
        kurs = item;
        values = SGLFilterValues.get(target, o -> target.forName(CommonDocuments.STUDENT_CAREERS_DOCID));
        init = target.getWebServiceProvider().getDefaultRequestProcessor().post(() -> values.add(kurs));
    }

    public Marker[] getFilterMarkers() {
        return filter;
    }

    public void setFilterMarkers(final Marker[] m) {
        this.filter = m;
    }

    @Override
    public final boolean accept(final ITI iti, final TargetDocumentProperties td, final StudentId student) {
        return doAccept(iti, student);
    }

    protected boolean doAccept(final ITI iti, final StudentId student) {
        if (filter != null && iti.equals(kurs)) {
            if (!init.isFinished()) {
                init.waitFinished();
            }
            final Marker m = values.get(student);
            return contains(m);
        }
        return true;
    }

    protected boolean contains(final Marker m) {
        if (!Marker.isNull(m)) {
            return Arrays.stream(filter).anyMatch(m::equals);
        } else {
            return Arrays.stream(filter).anyMatch(SGLFilter.NO_SGL::equals);
        }
    }

}
