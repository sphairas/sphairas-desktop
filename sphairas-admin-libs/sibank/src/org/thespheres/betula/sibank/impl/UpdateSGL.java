/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import org.openide.util.NbBundle;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.MarkerParsingException;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.CommonDocuments;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.sibank.SiBankImportStudentItem;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankKlasseItem;
import org.thespheres.betula.sibank.SiBankUpdaterService;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.Constants;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"UpdateSGL.message.noSGL=Kein Schulzweig bekannt für {0}.",
    "UpdateSGL.message.foundCareer=Schulzweig {1} durch durch SiBank-Import erkannt für {0}."})
public class UpdateSGL implements SiBankUpdaterService {

    private final Term term;

    UpdateSGL(Term current) {
        this.term = current;
    }

    @Override
    public Exception callService(ContainerBuilder builder, SiBankImportTarget config, TargetItemsUpdater<SiBankKlasseItem> update) {
        final HashMap<StudentId, Marker> sglMap = new HashMap<>();

        for (final SiBankKlasseItem sibankkurs : update.getItems()) {
            final UnitId u = sibankkurs.getUnitId();
            if (u != null) {
                for (final SiBankImportStudentItem isa : sibankkurs.getStudents().values()) {
                    if (!checkStudent(sibankkurs, u, isa.getStudentId(), update)) {
                        continue;
                    }
                    //TODO: check user setting
//                    isa.getClientProperty(sgltext)
                    Marker sgl = (Marker) isa.getClientProperty(Constants.PROP_STUDENT_CAREER);

                    if (sgl == null) {
                        sgl = findCareer(isa, config);
                        if (sgl != null) {
                            final String m = NbBundle.getMessage(KlassenUpdater.class, "UpdateSGL.message.foundCareer", isa.getSourceNodeLabel(), sgl.getLongLabel());
                            ImportUtil.getIO().getOut().println(m);
                        }
                    }

                    if (sgl == null) {
                        if (true) {
                            sglMap.put(isa.getStudentId(), Marker.NULL);
                        }
                        final String m = NbBundle.getMessage(KlassenUpdater.class, "UpdateSGL.message.noSGL", isa.getSourceNodeLabel());
                        ImportUtil.getIO().getOut().println(m);
                    } else {
//                        isa.
                        sglMap.put(isa.getStudentId(), sgl);
                    }
                }
            }
        }
        if (!sglMap.isEmpty()) {
            final DocumentId sglDoc = config.forName(CommonDocuments.STUDENT_CAREERS_DOCID);
            if (sglDoc == null) {
                throw new IllegalStateException("No student-bildungsgang-documentid set.");
            }
            final Template t = builder.createTemplate(null, sglDoc, null, Paths.STUDENTS_MARKERS_PATH, null, null);
            //TODO: set effective dates
            final Timestamp termBegin = new Timestamp(term.getBegin());
            sglMap.entrySet().stream()
                    .map(entry -> {
                        final Marker m = entry.getValue();
                        final Entry ret = new Entry(Action.FILE, entry.getKey(), Marker.isNull(m) ? null : new MarkerAdapter(m));
//                        ret.setTimestamp(termBegin);
//TODO set hint to increase doc version on level 2  1.1 -> 1.2 z.B.
                        return ret;
                    })
                    .forEach(t.getChildren()::add);
        }
        return null;
    }

    private boolean checkStudent(final SiBankKlasseItem iti, final UnitId unit, final StudentId student, final TargetItemsUpdater<SiBankKlasseItem> update) {
        return !update.getFilters().stream()
                .anyMatch(uf -> !uf.accept(iti, unit, student));
    }

    static Marker findCareer(final SiBankImportStudentItem isa, final SiBankImportTarget config) {
        final String sgltext = isa.getSourceStudentCareer();
        Marker sgl = null;
        if (sgltext != null) {
            sgl = Arrays.stream(config.getStudentCareerConventions())
                    .map(mc -> {
                        try {
                            return mc.parseMarker(sgltext);
                        } catch (MarkerParsingException ex) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(CollectionUtil.singleOrNull());
        }
        try {
            isa.setClientProperty(Constants.PROP_STUDENT_CAREER, sgl == null ? Marker.NULL : sgl);
        } catch (PropertyVetoException ex) {
        }
        return sgl;
    }
}
