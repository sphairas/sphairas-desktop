package org.thespheres.betula.classtest.xml;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.thespheres.betula.classtest.StudentScores;
import org.thespheres.betula.assess.Grade;

/**
 *
 * @author boris.heithecker
 */
public class StudentScoresImpl implements StudentScores {

    private Grade grade;
    private boolean autoDistributing = true;
    private String note;
    private final HashMap<String, Double> map = new HashMap<>();
    private final DescriptiveStatistics values = new DescriptiveStatistics();

    public StudentScoresImpl() {
    }

    @Override
    public Double get(String key) {
        synchronized (map) {
            return map.get(key);
        }
    }

    @Override
    public void put(String key, Double value) {
        synchronized (map) {
            map.put(key, value);
        }
    }

    @Override
    public void remove(String key) {
        synchronized (map) {
            map.remove(key);
        }
    }

    @Override
    public Set<String> keys() {
        synchronized (map) {
            return map.keySet().stream()
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public Grade getGrade() {
        return grade;
    }

    @Override
    public Double sum() {
        synchronized (map) {
            values.clear();
            map.values().stream()
                    .filter(Objects::nonNull)
                    .forEach(values::addValue);
            final Sum s = new Sum();
            return s.evaluate(values.getValues());
        }
    }

    @Override
    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    @Override
    public boolean isAutoDistributing() {
        return autoDistributing;
    }

    @Override
    public void setAutoDistributing(boolean autoDistributing) {
        this.autoDistributing = autoDistributing;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public void setNote(String note) {
        this.note = note;
    }

}
