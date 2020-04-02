package org.thespheres.betula.noten.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.Distribution;

//TODO: use XmlPercentageDistribution
class DistributionImpl implements Distribution<Int2> {

    private final Int2[] values;
    private final String name;

    DistributionImpl(String name, Int2[] percentageValues) {
        this.name = name;
        this.values = percentageValues;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public List<Int2> distribute(Int2 ceiling) {
        return Collections.unmodifiableList(getValueSet(ceiling));
    }

    @Override
    public List<Int2> getDistributionValues() {
        ArrayList<Int2> ret = new ArrayList<>();
        ret.addAll(Arrays.asList(values));
        return Collections.unmodifiableList(ret);
    }

    private List<Int2> getValueSet(final Int2 ceiling) {
        ArrayList<Int2> ret = new ArrayList<>();
        for (Int2 v : values) {
            ret.add(Int2.valueOfPercentage(v, ceiling));
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Arrays.hashCode(this.values);
        return 47 * hash + Objects.hashCode(this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DistributionImpl other = (DistributionImpl) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Arrays.equals(this.values, other.values);
    }

}
