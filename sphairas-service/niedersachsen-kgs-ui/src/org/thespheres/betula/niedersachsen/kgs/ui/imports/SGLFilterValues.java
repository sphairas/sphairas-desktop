/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.kgs.ui.imports;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openide.util.*;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admin.units.RemoteStudents;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.DocumentUtilities;
import org.thespheres.betula.document.util.MarkerAdapter;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportTarget;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"SGLFilterValues.message.noSGL=Kein Schulzweig für {0} gefunden!",
    "SGLFilterValues.message.noCareersDocument=Keine Schulzweigliste für {0} definiert."})
public class SGLFilterValues {
    
    private final static Map<String, SGLFilterValues> INSTANCES = new HashMap<>();
    private final ImportTarget config;
    private final Map<StudentId, Marker> sgl = new HashMap<>();
    private final Set<StudentId> warned = new HashSet<>();
    private final DocumentId studentCareersDocumentId;
    
    private SGLFilterValues(final ImportTarget support, final DocumentId carreers) {
        this.config = support;
        this.studentCareersDocumentId = carreers;
        if (studentCareersDocumentId == null) {
            final String msg = NbBundle.getMessage(SGLFilterValues.class, "SGLFilterValues.message.noCareersDocument", support.getProviderInfo().getDisplayName());
            ImportUtil.getIO().getErr().println(msg);
        }
    }
    
    public static SGLFilterValues get(final ImportTarget config, final Function<ImportTarget, DocumentId> careers) {
        final String url = config.getProviderInfo().getURL();
        return INSTANCES.computeIfAbsent(url, key -> new SGLFilterValues(config, careers.apply(config)));
    }
    
    public SGLFilterValues add(ImportTargetsItem kurs) {
        if (studentCareersDocumentId != null) {
            final StudentId[] all = kurs.getUnitStudents();
            if (all != null) {
                final Set<StudentId> studs = Arrays.stream(all)
                        .filter(s -> !sgl.containsKey(s))
                        .collect(Collectors.toSet());
                reload(studs);
            }
        }
        return this;
    }
    
    public Marker get(StudentId student) {
        final Marker ret = sgl.get(student);
        if (Marker.isNull(ret) && !warned.contains(student)) {
            final String fn = RemoteStudents.find(config.getProviderInfo().getURL(), student).getFullName();
            final String msg = NbBundle.getMessage(SGLFilterValues.class, "SGLFilterValues.message.noSGL", fn);
            ImportUtil.getIO().getErr().println(msg);
            warned.add(student);
            return null;
        }
        return ret;
    }
    
    private void reload(final Set<StudentId> students) {
        final ContainerBuilder builder = new ContainerBuilder();
        final Template t = builder.createTemplate(null, studentCareersDocumentId, null, Paths.STUDENTS_MARKERS_PATH, null, null);
//            t.getHints().put(support, support); date-as-of
        students.stream()
                .map(s -> new Entry(Action.REQUEST_COMPLETION, s))
                .forEach(ch -> t.getChildren().add(ch));
        Container response;
        try {
            response = NetworkSettings.suppressAuthenticationDialog(() -> config.getWebServiceProvider().createServicePort().solicit(builder.getContainer()));
        } catch (Exception ex) {
            ex.printStackTrace(ImportUtil.getIO().getErr());
            return;
        }
        final List<Envelope> l = DocumentUtilities.findEnvelope(response, Paths.STUDENTS_MARKERS_PATH);
        final Map<StudentId, Marker> m = l.stream()
                .filter(node -> node instanceof Entry && ((Entry) node).getIdentity() instanceof DocumentId && ((DocumentId) ((Entry) node).getIdentity()).equals(studentCareersDocumentId))
                .flatMap(node -> node.getChildren().stream())
                .filter(node -> node instanceof Entry && ((Entry) node).getIdentity() instanceof StudentId)
                .map(node -> (Entry<StudentId, ?>) node)
                .collect(Collectors.toMap(Entry::getIdentity, e -> e.getValue() instanceof MarkerAdapter ? ((MarkerAdapter) e.getValue()).getMarker() : Marker.NULL));
        students.stream()
                .filter(m::containsKey)
                .forEach(s -> sgl.put(s, m.get(s)));
    }
    
}
