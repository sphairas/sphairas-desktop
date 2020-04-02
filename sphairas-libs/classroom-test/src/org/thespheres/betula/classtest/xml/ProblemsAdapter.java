/*
 * ProblemsAdapter.java
 *
 * Created on 25. April 2007, 21:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.xml;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProblemsAdapter extends XmlAdapter<ProblemsAdapter, Map<String, XmlProblem>> {

    @XmlElement(name = "problem-entry")
    private ProblemEntry[] entries;

    @Override
    public Map<String, XmlProblem> unmarshal(ProblemsAdapter v) throws Exception {
        final Map<String, XmlProblem> ret = Arrays.stream(v.entries)
                .collect(Collectors.toMap(e -> e.id, e -> e));
        for (XmlProblem xp : ret.values()) {
            ((ProblemEntry) xp).setReferences(ret);
        }
        return ret;
    }

    @Override
    public ProblemsAdapter marshal(Map<String, XmlProblem> v) throws Exception {
        ProblemsAdapter ret = new ProblemsAdapter();
        ret.entries = v.entrySet().stream()
                .map(e -> new ProblemEntry(e.getKey(), e.getValue()))
                .toArray(ProblemEntry[]::new);
        return ret;
    }

    public static final class ProblemEntry extends XmlProblem {

        @XmlID
        @XmlAttribute(name = "id", required = true)
        private String id;
        @XmlList
        @XmlElement(name = "references")
        private String[] references;

        public ProblemEntry() {
        }

        private ProblemEntry(final String key, final XmlProblem p) {
            this.maxscore = p.getMaxScore();
            this.weight = p.getWeight();
            this.displayName = p.getDisplayName();
            this.index = p.getIndex();
            this.id = key;
            this.parentId = p.getParentId();
            references = p.refs.stream()
                    .map(e -> e.getId() + ":" + Double.toString(e.getWeight()))
                    .toArray(String[]::new);
        }

        @Override
        public String getId() {
            return id;
        }

        private void setReferences(Map<String, XmlProblem> m) throws UnmarshalException {
            if (references != null) {
                for (String s : references) {
                    String[] sp = s.split(":");
                    if (sp.length != 2) {
                        throw new UnmarshalException("Cannot parse problem references.");
                    }
                    XmlProblem p = m.get(sp[0]);
                    if (p == null) {
                        throw new UnmarshalException("Cannot parse problem references.");
                    }
                    Double w = null;
                    try {
                        w = Double.valueOf(sp[1]);
                    } catch (NumberFormatException nex) {
                        throw new UnmarshalException(nex);
                    }
                    XmlProblemRef ref = addReference(p);
                    ref.setWeight(w);
                }
            }
            references = null;
        }
    }
}
