/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis;

import org.thespheres.betula.niedersachsen.xml.NdsZeugnisSchulvorlage;
import java.io.IOException;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.MultiSubject;
import org.thespheres.betula.niedersachsen.ASVAssessmentConvention;
import org.thespheres.betula.niedersachsen.Profile;
import org.thespheres.betula.niedersachsen.kgs.SGL;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.ws.CommonDocuments;

/**
 *
 * @author boris.heithecker
 */
public class NdsReportBuilderFactory implements CommonDocuments {

    public static final String SGL_NAME = "sgl-name";

    public static final String SIGNEE_BEMERKUNGEN_FILE = "signee/bemerkungen.xml";
    public static final String SCHULVORLAGE_FILE = "schulvorlage.xml";
    public static final Collector<CharSequence, ?, String> SUBJECT_JOINING_COLLECTOR = Collectors.joining(" - ", "(", ")"); //Collectors.joining(", ", "(", ")");
    public static final SubjectOrderDefinition FACH_COMPARATOR;
    private static final SubjectOrderDefinition FACH_COMPARATOR_HS;
    private static final SubjectOrderDefinition FACH_COMPARATOR_RS;
    private static final SubjectOrderDefinition FACH_COMPARATOR_GY;

    private final NdsZeugnisSchulvorlage template;

    static {
        try {
            FACH_COMPARATOR = SubjectOrderDefinition.load(SubjectOrderDefinition.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/default_order.xml"));
            FACH_COMPARATOR_HS = SubjectOrderDefinition.load(SubjectOrderDefinition.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/order_hs.xml"));
            FACH_COMPARATOR_RS = SubjectOrderDefinition.load(SubjectOrderDefinition.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/order_rs.xml"));
            FACH_COMPARATOR_GY = SubjectOrderDefinition.load(SubjectOrderDefinition.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/resources/order_gy.xml"));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public NdsReportBuilderFactory(NdsZeugnisSchulvorlage template) {
        this.template = template;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return template.getProviderInfo();
    }

    static SubjectOrderDefinition forSGL(final Marker sgl) {// ===> factory 
        if (sgl != null && SGL.NAME.equals(sgl.getConvention())) {
            switch (sgl.getId()) {
                case "hs":
                    return FACH_COMPARATOR_HS;
                case "rs":
                    return FACH_COMPARATOR_RS;
                case "gy":
                    return FACH_COMPARATOR_GY;
            }
        }
//        TODO: use template.getSubjectOrder();
        return FACH_COMPARATOR;
    }

    public String careerMarkerConvention() {
        return SGL.NAME;
    }

    public NdsZeugnisSchulvorlage getSchulvorlage() {
        return template;
    }

    public NdsReportBuilder newBuilder(final String familyName, final String typ, final Marker sgl) {
        return new NdsReportBuilder(familyName, typ, sgl, this);
    }

    public SubjectOrderDefinition forCareer(final Marker c) {
        return forSGL(c);
    }

    public int tier(final MultiSubject subject) {
        final Marker[] fach = subject.getSubjectMarkerSet().stream()
                .toArray(Marker[]::new);
        final Marker realm = subject.getRealmMarker();
        if (realm != null && ("kgs.unterricht".equals(realm.getConvention()) || "niedersachsen.unterricht.art".equals(realm.getConvention())) && "wpk".equals(realm.getId()) && realm.getSubset() == null) {
            return 1;
        } else if (fach.length == 1 && fach[0].getConvention().equals(Profile.CONVENTION_NAME)) {
            return 2;
        } else {
            return 0;
        }
    }

    Collector<CharSequence, ?, String> subjectJoiningCollector() {
        return SUBJECT_JOINING_COLLECTOR;
    }

    @Override
    public DocumentId forName(String name) {
        return template.forName(name);
    }

    public boolean requireAVSVReason(final Grade avsv) {
        if (avsv != null && (ASVAssessmentConvention.AV_NAME.equals(avsv.getConvention()) || ASVAssessmentConvention.SV_NAME.equals(avsv.getConvention()))) {
            return "d".equals(avsv.getId()) || "e".equals(avsv.getId());
        }
        return false;
    }

}
