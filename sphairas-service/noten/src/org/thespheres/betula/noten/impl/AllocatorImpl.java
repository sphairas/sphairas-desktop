/*
 * AllocatorImpl.java
 *
 * Created on 17. November 2007, 11:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.noten.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.Allocator;
import org.thespheres.betula.assess.AssessmentContext;
import org.thespheres.betula.assess.AssessmentConvention;
import org.thespheres.betula.assess.Distribution;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author Boris Heithecker
 */
class AllocatorImpl implements Allocator<Int2> {

    private final AssessmentConvention.OfBiasable convention;
    private PropertyChangeSupport pSupport;
    private TreeMap<Int2, Grade> distribMap;  //Oder Concurrent Skip List Map? Navigable
    private HashMap<Grade, Int2> floorValues;
    private Int2 distCeiling;
    private AssessmentContext<StudentId, Int2> context;
    private Distribution currentDefaultDist = null;

    private AllocatorImpl(AssessmentContext<StudentId, Int2> ctx, AssessmentConvention.OfBiasable convention) {
        this.pSupport = new PropertyChangeSupport(ctx);
        this.context = ctx;
        this.convention = convention;
    }

    @SuppressWarnings({"OverridableMethodCallInConstructor"})
    AllocatorImpl(AssessmentContext<StudentId, Int2> ctx, AssessmentConvention.OfBiasable convention, Distribution<Int2> dist, final Int2 ceiling) {
        this(ctx, convention);
        distribute(dist, ceiling);
    }

    AllocatorImpl(AssessmentContext<StudentId, Int2> ctx, AssessmentConvention.OfBiasable convention, Int2 ceiling, Int2[] floorValues, String defaultDist) {
        this(ctx, convention);
        int gradelength = convention.getAllGradesUnbiased().length;
        int cap = (int) Math.ceil(gradelength / 0.75d);
        this.floorValues = new HashMap<>(cap, 0.75f);
        this.distribMap = new TreeMap<>();
        this.distCeiling = ceiling;
        //
        int i = 0;
        for (Grade g : convention) {
            this.floorValues.put(g, floorValues[i]);
            this.distribMap.put(floorValues[i], g);
            i++;
        }
        if (defaultDist != null) {
            for (Distribution d : context.getDefaultDistributions()) {
                if (defaultDist.equals(d.toString())) {
                    currentDefaultDist = d;
                }
            }
        }
    }

    void distribute(Distribution<Int2> dist, Int2 ceiling) throws IllegalArgumentException {
        int gradelength = convention.getAllGradesUnbiased().length;
        if (gradelength != dist.getDistributionValues().size()) {
            throw new IllegalArgumentException("Distribution size and grade count not equal.");
        }
        int cap = (int) Math.ceil(gradelength / 0.75d);
        HashMap<Grade, Int2> old = floorValues;
        this.floorValues = new HashMap<>(cap, 0.75f);
        this.distribMap = new TreeMap<>();
        this.distCeiling = ceiling;
        //
        for (Distribution d : context.getDefaultDistributions()) {
            if (d.equals(dist)) {
                currentDefaultDist = d;
            }
        }
        //
        List<Int2> distvals = dist.distribute(ceiling);
        int i = 0;
        for (Grade g : convention) {
            Int2 v = distvals.get(i++);
            this.floorValues.put(g, v);
            this.distribMap.put(v, g);
        }
        for (Grade g : convention) {
            Int2 v = floorValues.get(g);
            Int2 oldval = (old != null ? old.get(g) : null);
            pSupport.firePropertyChange(g.toString(), oldval, v);
        }
    }

    @Override
    public Grade allocate(Int2 value) {
        return distribMap.get(distribMap.floorKey(value));
    }

    @Override
    public Int2 getFloor(Grade grade) {
        return floorValues.get(grade);
    }

    @Override
    public void setFloor(Grade grade, Int2 value) {
        if (grade == null || value == null) {
            throw new NullPointerException();
        }
        Grade lower = grade.getNextLower();
        Int2 lowerValue = (lower != null ? getFloor(lower) : Int2.fromInternalValue(0));
        if (value.compareTo(getCeiling(grade)) > 0 || value.compareTo(lowerValue) < 0) {
            throw new IllegalArgumentException("Value must be greater then lower grade, or lesser than highter grade.");
        }
        Int2 oldValue = floorValues.get(grade);
        distribMap.remove(oldValue);
        distribMap.put(value, grade);
        floorValues.put(grade, value);
        //
        if (!oldValue.equals(value)) {
            currentDefaultDist = null;
            one:
            for (Distribution<Int2> d : context.getDefaultDistributions()) {
                List<Int2> l = d.distribute(distCeiling);
                int i = 0;
                for (Grade g : convention) {
                    if (!l.get(i++).equals(getFloor(g))) {
                        continue one;
                    }
                }
                this.currentDefaultDist = d;
                break;
            }
        }
        //
        pSupport.firePropertyChange(grade.toString(), oldValue, value);
    }

    @Override
    public Int2 getCeiling(Grade grade) {
        //Grade == null ???
        Grade next = grade.getNextHigher();
        int nextFloor = (next != null ? floorValues.get(next).getInternalValue() - 1 : distCeiling.getInternalValue());
        return Int2.fromInternalValue(nextFloor);
    }

    void setCeiling(Int2 ceiling) throws IllegalArgumentException {
        Int2 oldValue = distCeiling;
        if (this.floorValues.get(convention.getCeilingUnbiased()).compareTo(ceiling) > 0) {
            throw new IllegalArgumentException("Ceiling lesser than highest grade value assigned to highest grade.");
        }
        this.distCeiling = ceiling;
        pSupport.firePropertyChange("ceiling", oldValue, ceiling);
    }

    void addPropertyChangeListener(PropertyChangeListener l) {
        pSupport.addPropertyChangeListener(l);
    }

    void removePropertyChangeListener(PropertyChangeListener l) {
        pSupport.removePropertyChangeListener(l);
    }

    Distribution getCurrentDist() {
        if (currentDefaultDist != null) {
            return currentDefaultDist;
        } else {
            Int2[] val = new Int2[convention.getAllGrades().length];
            int i = 0;
            for (Grade g : convention) {
                val[i++] = Int2.percentageOfValue(getFloor(g), distCeiling);
            }
            return new DistributionImpl(null, val);
        }
    }
}
