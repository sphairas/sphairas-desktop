/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.impl;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.utilities.AbstractLinkCollection;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "untis-assoziationen")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class UntisSourceTargetLinks {

    @XmlElementWrapper(name = "schuljahre")
    @XmlElement(name = "schuljahr")
    private final List<Schuljahr> list = new ArrayList<>();

    public Schuljahr get(final int year, final boolean create) {
        synchronized (list) {
            return list.stream()
                    .filter(sj -> sj.getSchoolYear() == year)
                    .collect(CollectionUtil.singleton())
                    .orElseGet(() -> create ? createSchuljahr(year) : null);
        }
    }

    private Schuljahr createSchuljahr(final int year) {
        Schuljahr s = new Schuljahr(year);
        list.add(s);
        return s;
    }

    public boolean isEmpty() {
        return list.isEmpty() || list.stream().allMatch(sj -> sj.getItems().isEmpty());
    }

    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static final class Schuljahr extends AbstractLinkCollection<UntisTargetLink, String> {

        @XmlElement(name = "jahr-id", required = true)
        private int schoolYear;

        public Schuljahr() {
            super(UntisTargetLink.class);
        }

        private Schuljahr(int sj) {
            this();
            this.schoolYear = sj;
        }

        public int getSchoolYear() {
            return schoolYear;
        }

        @Override
        protected UntisTargetLink create(String lesson, int clone) {
            return new UntisTargetLink(lesson, clone);
        }

        public final void afterUnmarshal(Unmarshaller u, Object parent) {
//        list.stream().forEach(sba -> map.put(sba.term, new HashSet(sba.associations)));
//        items.stream()
//                .map(UntisTargetLink.class::cast)
//                .filter(sa -> sa.getUnitDeprected() != null && sa.getUnit() == null)
//                .forEach(sa -> {
//                    sa.setUnit(sa.getUnitDeprected());
//                    sa.setUnitDeprected(null);
//                    PlatformUtil.getCodeNameBaseLogger(UntisTargetLink.class).log(Level.INFO, "Set unit deprecated " + sa.getUnit().toString());
//                });
        }
    }
}
