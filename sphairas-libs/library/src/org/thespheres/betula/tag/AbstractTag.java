/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tag;

import org.thespheres.betula.document.*;
import java.io.Serializable;
import java.util.Objects;
import org.thespheres.betula.Tag;

/**
 *
 * @author boris.heithecker
 */
//If AbstractGrade is not Serializable, will not work correctly with ejb remote serialization
public class AbstractTag implements Tag, Serializable {

    protected String markerConvention;
    protected String markerId;

    public AbstractTag(String convention, String gradeId) {
        this.markerConvention = convention;
        this.markerId = gradeId;
    }

    @Override
    public String getConvention() {
        return markerConvention;
    }

    @Override
    public String getId() {
        return markerId;
    }

    @Override
    public String getLongLabel(Object... formattingArgs) {
        return "{" + getConvention() + "}" + getId();
    }

    @Override
    public String getShortLabel() {
        return getLongLabel();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.markerConvention);
        return 23 * hash + Objects.hashCode(this.markerId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Tag)) {
            return false;
        }
        final Tag other = (Tag) obj;
        if (!Objects.equals(this.markerConvention, other.getConvention())) {
            return false;
        }
        return Objects.equals(this.markerId, other.getId());
    }

    public Object writeReplace() {
        return new Replacer(markerConvention, markerId);
    }

    public static class Replacer implements Serializable {

        private final String convention;
        private final String id;

        public Replacer(String convention, String id) {
            this.convention = convention;
            this.id = id;
        }

        public Object readResolve() {
            Tag ret = null;
            if (id != null && convention != null) {
                ret = MarkerFactory.find(convention, id);
            }
            if (ret == null) {
                ret = new AbstractTag(convention, id);
            }
            return ret;
        }
    }
}
