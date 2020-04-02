/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.model.ReportDocument;
import org.thespheres.betula.document.model.Subject;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "career-aware-grade-condition", namespace = "http://www.thespheres.org/xsd/niedersachsen/versetzung.xsd")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class CareerAwareGradeCondition extends Condition {

    @XmlAttribute(name = "career-convention", required = true)
    private String sglConvention;
//            final String mc = "kgs.schulzweige";

    //overrides configuration gradeConvention
    @XmlAttribute(name = "grade-convention")
    private String gradeConvention;
    @XmlList
    @XmlAttribute(name = "match", required = true)
    private String[] match;
    @XmlList
    @XmlAttribute(name = "default-pair")
    private String[] pair;
    @XmlElement(name = "pairing")
    private Pairing[] pairing;
//    @XmlAttribute(name = "num-occurrence")
//    private Integer occurrence;
//    @XmlAttribute(name = "min-occurrence")
//    private Integer minOccurrence;
//    @XmlAttribute(name = "max-occurrence")
//    private Integer maxOccurrence;
//    @XmlAttribute(name = "min-pairs")
//    private Integer minPairs;
    @XmlIDREF
    @XmlAttribute(name = "matcher")
    private Matcher matcher;

    public CareerAwareGradeCondition() {
    }

    public CareerAwareGradeCondition(String sglConvention, String match) {
        this(sglConvention, new String[]{match});
    }

    public CareerAwareGradeCondition(String sglConvention, String[] test) {
        this.match = test;
        this.sglConvention = sglConvention;
    }

    public void setReportDistinguishingMarkers(Marker[] reportDistinguishingMarkers) {
        this.reportDistinguishingMarkers = reportDistinguishingMarkers;
    }

    public String getCareerConvention() {
        return sglConvention;
    }

    public void setCareerConvention(String convention) {
        this.sglConvention = convention;
    }

    public String getGradeConvention() {
        return gradeConvention;
    }

    public void setGradeConvention(String convention) {
        this.gradeConvention = convention;
    }

    public String[] getMatch() {
        return match;
    }

    public void setMatch(String[] m) {
        this.match = m;
    }
//
//    public Integer getOccurrence() {
//        return occurrence;
//    }
//
//    public void setOccurrence(Integer occurrence) {
//        this.occurrence = occurrence;
//    }
//    public Integer getMinOccurrence() {
//        return minOccurrence;
//    }
//
//    public void setMinOccurrence(Integer minOccurrence) {
//        this.minOccurrence = minOccurrence;
//    }
//
//    public Integer getMaxOccurrence() {
//        return maxOccurrence;
//    }
//
//    public void setMaxOccurrence(Integer maxOccurrence) {
//        this.maxOccurrence = maxOccurrence;
//    }
//

    public String[] getDefaultPair() {
        return pair;
    }

    public void setDefaultPair(String[] pair) {
        this.pair = pair;
    }

//
//    public Integer getMinPairs() {
//        return minPairs;
//    }
//
//    public void setMinPairs(Integer minPairs) {
//        this.minPairs = minPairs;
//    }
    public Matcher getMatcher() {
        return matcher == null ? Matcher.DEFAULT : matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public Pairing[] getPairing() {
        return pairing;
    }

    public void setPairing(Pairing[] pairing) {
        this.pairing = pairing;
    }

    private Map<String, String[]> pairingMap() {
        return Arrays.stream(pairing)
                .map(p -> Arrays.stream(p.getKey())
                .collect(Collectors.toMap(Function.identity(), k -> p.getPair())))
                .collect(HashMap::new, Map::putAll, Map::putAll);
//                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
//        return pairing == null ? Collections.EMPTY_MAP : Arrays.stream(pairing).collect(Collectors.toMap(Pairing::getKey, Pairing::getPair));
    }

    @Override
    protected boolean evaluate(final Set<Subject> filtered, ReportDocument report, final PolicyRun props, final Policy policy) {
        final Matcher m = getMatcher();
        final Pair[] matches = filtered.stream()
                .filter(s -> m.matches(report.select(s), getMatch(), props))
                .map(Pair::new)
                .toArray(Pair[]::new);
//        if (matches.length != 2) {
//            return false;
//        }
        return pair(report, matches, filtered, props, policy);
    }

    protected boolean pair(final ReportDocument report, final Pair[] matches, Set<Subject> filtered, PolicyRun props, Policy policy) {
        final Matcher m = getMatcher();
        final Map<Subject, Set<Subject>> edges = new HashMap<>();
        for (final Pair p : matches) {
            Marker sgl = p.match.getRealmMarker();
            final String career = getCareerConvention();
            if (sgl == null || !sgl.getConvention().equals(career)) {
                sgl = Arrays.stream(report.markers())
                        .filter(marker -> marker.getConvention().equals(career))
                        .collect(CollectionUtil.requireSingleOrNull());
            }
            if (sgl == null) {
                props.log("No sgl.", null);
                return false;
            }
            for (Subject subject : filtered) {
                final Marker subjectSGL = subject.getRealmMarker();
                final String[] pairable = findPairable(sgl, subjectSGL);
                boolean pass = m.matches(report.select(subject), pairable, props);
                if (pass) {
                    p.pairing.add(subject);
                    edges.computeIfAbsent(subject, s -> new HashSet<>()).add(p.match);
                }
            }
        }

        for (Pair p : matches) {
            if (p.pairing.isEmpty()) {
                return false;
            }
        }

        final Set<Subject> intersection = Arrays.stream(matches)
                .flatMap(p -> p.pairing.stream())
                .collect(Collectors.toSet());
        Arrays.stream(matches)
                .forEach(p -> intersection.retainAll(p.pairing));
        if (intersection.size() >= matches.length) {
            return true;
        }

        return testPairs(matches);
    }

    public static boolean testPairs(final Pair[] matches) {
        final int[] test = new int[matches.length];
        int p = 0;
        test:
        while (!testSelection(matches, test)) {

            int last = test.length - 1;
            while (last > p) {
                final int sl = matches[last].pairing.size();
                if (test[last] + 1 < sl) {
                    test[last]++;
                    continue test;
                } else {
                    test[last] = 0;
                    last--;
                }
            }

            final int s = matches[p].pairing.size();
            if (test[p] + 1 < s) {
                test[p]++;
                continue;
            }
            return false;
        }
        return true;
    }

    static boolean testSelection(final Pair[] matches, int[] test) {
        final Set<Subject> claimed = new HashSet<>();
        for (int i = 0; i < matches.length; i++) {
            final int index = test[i];
            final Subject c = matches[i].pairing.get(index);
            if (!claimed.add(c)) {
                return false;
            }
        }
        return true;
    }

    private String[] findPairable(final Marker sgl, final Marker kursSGL) {
        if (kursSGL != null && getCareerConvention().equals(kursSGL.getConvention())) {
            final String unterschied = kursunterschied(sgl, kursSGL);
            return pairingMap().getOrDefault(unterschied, getDefaultPair());
//            switch (unterschied) {
//                case "1":
//                    return new String[]{"1", "2", "3", "4"};
//                case "-1":
//                    return new String[]{"1", "2"};
//            }
        }
        return getDefaultPair();
    }

    static String kursunterschied(final Marker sgl, final Marker kursSGL) {
        final String id = sgl.getId();
        final String kid = kursSGL.getId();
        return id + "-" + kid;
//        if ("rs".equals(id) && "gy".equals(kid)) {
//            return "1";
//        } else if ("rs".equals(id) && "hs".equals(kid)) {
//            return "-1";
//        } else if ("hs".equals(id) && "rs".equals(kid)) {
//            return "1";
//        } else if ("gy".equals(id) && "rs".equals(kid)) {
//            return "-1";
//        }
//        return null;
    }

    class Pair {

        //muss gepaart werden, z.B. die "5"
        private final Subject match;
        //paarungskandidaten
        private final List<Subject> pairing = new ArrayList<>();
        //
        private boolean resolved = false;

        Pair(Subject match) {
            this.match = match;
        }

    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class Pairing {

        @XmlList
        @XmlAttribute(name = "pairing-condition")
        private String[] key;
        @XmlList
        @XmlAttribute(name = "pair")
        private String[] pair;

        public Pairing() {
        }

        public Pairing(String[] key, String[] pair) {
            this.key = key;
            this.pair = pair;
        }

        public String[] getKey() {
            return key;
        }

        public String[] getPair() {
            return pair;
        }

    }
}
