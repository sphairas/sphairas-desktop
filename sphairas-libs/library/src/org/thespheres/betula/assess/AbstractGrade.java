/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author boris.heithecker
 */
//If AbstractGrade is not Serializable, will not work correctly with ejb remote serialization
public class AbstractGrade implements Grade, Serializable {

    protected String gradeConvention;
    protected String gradeId;

    public AbstractGrade(String gradeConvention, String gradeId) {
        this.gradeConvention = gradeConvention;
        this.gradeId = gradeId;
    }

    @Override
    public String getConvention() {
        return gradeConvention;
    }

    @Override
    public String getId() {
        return gradeId;
    }

    @Override
    public String getLongLabel(Object... formattingArgs) {
        return "{" + getConvention() + "}" + getId();
    }

    @Override
    public String getShortLabel() {
        return getId();
    }

    @Override
    public Grade getNextLower() {
        return null;
    }

    @Override
    public Grade getNextHigher() {
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.gradeConvention);
        return 59 * hash + Objects.hashCode(this.gradeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Grade)) {
            return false;
        }
        final Grade other = (Grade) obj;
        if (!Objects.equals(this.gradeConvention, other.getConvention())) {
            return false;
        }
        return Objects.equals(this.gradeId, other.getId());
    }

    @Override
    public String toString() {
        return getConvention() + "#" + getId();
    }

    public Object writeReplace() {
        return new Replacer(gradeConvention, gradeId);
    }

    public static class Replacer implements Serializable {

        private final String convention;
        private final String id;

        public Replacer(String convention, String id) {
            this.convention = convention;
            this.id = id;
        }

        public Object readResolve() {
            Grade ret = null;
            if (id != null && convention != null) {
                ret = GradeFactory.find(convention, id);
            }
            if (ret == null) {
                ret = new AbstractGrade(convention, id);
            }
            return ret;
        }
    }
}
