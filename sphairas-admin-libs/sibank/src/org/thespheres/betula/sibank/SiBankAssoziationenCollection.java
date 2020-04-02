/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.xmlimport.utilities.AbstractLinkCollection;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "sibank-assoziationen")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class SiBankAssoziationenCollection extends AbstractLinkCollection<SiBankAssoziation, UniqueSatzDistinguisher> {

//    @XmlElement(name = "sibank-halbjahr")
//    private final List<SiBankAssoziationen> list = new ArrayList<>();
    @XmlElementWrapper(name = "sibank-schueler")
    @XmlElement(name = "schueler-assoziation")
    private final Set<SchuelerAssoziation> sus = new HashSet<>();

    public SiBankAssoziationenCollection() {
        super(SiBankAssoziation.class);
    }

    @Override
    protected SiBankAssoziation create(UniqueSatzDistinguisher id, int clone) {
        return new SiBankAssoziation(id, clone);
    }

    public Set<SchuelerAssoziation> getSchuelerAssoziationen() {
        return sus;
    }

//
//    public final void beforeMarshal(Marshaller m) {
////        list.clear();
//    }
//
//    public final void afterUnmarshal(Unmarshaller u, Object parent) {
//    }
//    @XmlAccessorType(value = XmlAccessType.FIELD)
//    public static final class SiBankAssoziationen {
//
//        @XmlElement(name = "term-id", required = true)
//        private TermId term;
//
//        public SiBankAssoziationen() {
//        }
//    }
}
