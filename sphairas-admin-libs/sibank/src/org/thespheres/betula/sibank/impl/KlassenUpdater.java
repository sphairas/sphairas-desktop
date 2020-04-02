/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank.impl;

import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.openide.util.NbBundle;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.services.vcard.VCardStudentsCollection;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ui.util.HttpUtilities;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.sibank.SiBankImportTarget;
import org.thespheres.betula.sibank.SiBankKlasseItem;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.utilities.TargetItemsUpdater;
import org.thespheres.betula.xmlimport.utilities.UpdaterFilter;
import org.thespheres.betula.xmlimport.utilities.VCardStudentsUtil;

/**
 *
 * @author boris.heithecker
 */
@NbBundle.Messages({"KlassenUpdater.message.updateVCards.network=... Schülerdaten ...",
    "KlassenUpdater.message.updateVCards.finish=Es wurden {0} Schülerinnen/Schüler in {1} ms importiert."})
public class KlassenUpdater extends TargetItemsUpdater<SiBankKlasseItem> {

    private static JAXBContext jaxb;

    private final SiBankImportTarget configuration;
    private final UpdateSGL sglUpdate;

    public KlassenUpdater(SiBankKlasseItem[] impKurse, WebServiceProvider provider, Term current, List<UpdaterFilter> filters, SiBankImportTarget config) {
        super(impKurse, provider, current, filters);
        configuration = config;
        sglUpdate = new UpdateSGL(current);
    }

    @Override
    protected Exception callService(ContainerBuilder builder) throws MissingResourceException {
        //update VCards first, so a new student's name can always be resolved
        //otherwise table immed. after update show only id number.
//        updateVCards();
        Exception ex = updateVCards2();
        if (ex != null) {
            return ex;
        }
//        updateSGL(builder);
        ex = sglUpdate.callService(builder, configuration, this);
        if (ex != null) {
            return ex;
        }
        ex = super.callService(builder);
        try {
            VCardStudents sbs = VCardStudentsUtil.findFromConfiguration(configuration);
            sbs.forceReload();
        } catch (IOException ioex) {
            ioex.printStackTrace(ImportUtil.getIO().getErr());
        }
        return ex;
    }

//    @Deprecated
//    private void updateVCards() {
//        String msg2 = NbBundle.getMessage(KlassenUpdater.class, "KlassenUpdater.message.updateVCards.network");
//        ImportUtil.getIO().getOut().println(msg2);
//        long ts = System.currentTimeMillis();
//        int[] ni = new int[]{0};
//        RemoteLookup rlp = configuration.getRemoteLookup();
//        StudentCardBean scb = rlp.lookup(StudentCardBean.class);
//        if (scb != null) {
//            Arrays.stream(items)
//                    .filter(SiBankKlasseItem::isValid)
//                    .filter(this::checkImportTargetsItem)
//                    .forEach(ik -> {
//                        UnitId u = ik.getUnitId();
//                        if (u != null) {
//                            ik.getStudents().values().stream()
//                                    .filter(isi -> checkStudent(ik, u, isi.getStudentId()))
//                                    .distinct()
//                                    .forEach(isi -> {
//                                        scb.updateVCard(isi.getStudentId(), isi.getVCard(), true);
//                                        ni[0]++;
//                                    });
//                        }
//                    });
//        }
//        long dur = System.currentTimeMillis() - ts;
//        msg2 = NbBundle.getMessage(KlassenUpdater.class, "KlassenUpdater.message.updateVCards.finish", ni[0], dur);
//        ImportUtil.getIO().getOut().println(msg2);
//    }
    private Exception updateVCards2() {
        String msg2 = NbBundle.getMessage(KlassenUpdater.class, "KlassenUpdater.message.updateVCards.network");
        ImportUtil.getIO().getOut().println(msg2);
        long ts = System.currentTimeMillis();
        final VCardStudentsCollection map = new VCardStudentsCollection();
        Arrays.stream(items)
                .filter(SiBankKlasseItem::isValid)
                .filter(this::checkImportTargetsItem)
                .forEach(ik -> {
                    UnitId u = ik.getUnitId();
                    if (u != null) {
                        ik.getStudents().values().stream()
                                .filter(isi -> checkStudent(ik, u, isi.getStudentId()))
                                .distinct()
                                .forEach(isi -> map.put(isi.getStudentId(), isi.getVCard()));
                    }
                });
        URI uri = VCardStudentsUtil.findStudentsURI(configuration);

        try {
            final Marshaller m = getJAXB().createMarshaller();
            HttpUtilities.post(provider, uri, os -> {
                try {
                    m.marshal(map, os);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }, null);
        } catch (JAXBException | IOException ex) {
            return ex;
        }

        long dur = System.currentTimeMillis() - ts;
        msg2 = NbBundle.getMessage(KlassenUpdater.class, "KlassenUpdater.message.updateVCards.finish", map.size(), dur);
        ImportUtil.getIO().getOut().println(msg2);
        return null;
    }

    static JAXBContext getJAXB() {
        if (jaxb == null) {
            try {
                jaxb = JAXBContext.newInstance(VCardStudentsCollection.class);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return jaxb;
    }

//    private void updateSGL(ContainerBuilder builder) {
//        final HashMap<StudentId, Marker> sglMap = new HashMap<>();
//
//        for (SiBankKlasseItem sibankkurs : items) {
//            final UnitId u = sibankkurs.getUnitId();
//            if (u != null) {
//                for (SiBankImportStudentItem isa : sibankkurs.getStudents().values()) {
//                    if (!checkStudent(sibankkurs, u, isa.getStudentId())) {
//                        continue;
//                    }
//                    String sgltext = isa.getSourceStudentCareer();
//                    Marker sgl = null;
//                    if (sgltext != null) {
//                        for (MarkerConvention mc : configuration.getStudentCareerConventions()) {
//                            try {
//                                sgl = mc.parseMarker(sgltext);
//                                break;
//                            } catch (MarkerParsingException ex) {
//                            }
//                        }
//                    }
//                    //TODO: check user setting
////                    isa.getClientProperty(sgltext)
//                    if (sgl == null) {
//                        String m = NbBundle.getMessage(KlassenUpdater.class, "KlassenUpdater.message.noSGL", isa.getSourceNodeLabel());
//                        ImportUtil.getIO().getOut().println(m);
//                    } else {
//                        sglMap.put(isa.getStudentId(), sgl);
//                    }
//                }
//            }
//        }
//        if (!sglMap.isEmpty()) {
//            final Template t = builder.createTemplate(null, configuration.getStudentCareersDocumentId(), null, Paths.STUDENTS_MARKER_PATH, null, null);
//            sglMap.entrySet().stream()
//                    .map(entry -> new Entry(Action.FILE, entry.getKey(), new MarkerAdapter(entry.getValue())))
//                    .forEach(ch -> t.getChildren().add(ch));
//        }
//    }
}
