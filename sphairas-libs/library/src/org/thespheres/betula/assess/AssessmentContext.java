/*
 * AssessmentContext.java
 *
 * Created on 1. Mai 2007, 08:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.assess;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JComponent;
import javax.swing.event.UndoableEditListener;
import org.thespheres.betula.Identity;

/**
 *
 * @author Boris Heithecker
 * @param <I>
 * @param <T>
 */
public interface AssessmentContext<I extends Identity, T extends Comparable> {

    public String getName();

    public String getDisplayName();

    public Allocator<T> getAllocator();

    public AssessorMap<I, T> getAssessorList();

    public void setRangeMaximum(T value);

    public T getRangeMaximum();

    public void applyDistribution(Distribution d);

    public Distribution[] getDefaultDistributions();

    public Distribution getCurrentDistribution();

    public void write(OutputStream out) throws IOException;

    public void addPropertyChangeListener(PropertyChangeListener l);

    public void removePropertyChangeListener(PropertyChangeListener l);

    public interface CustomizerProvider {

        public JComponent getCustomizer();

        public void addUndoableEditListener(UndoableEditListener l);

        public void removeUndoableEditListener(UndoableEditListener l);
    }

    public static interface Factory<I extends Identity, T extends Comparable> {

        public String getName();

        public String getDisplayName();

        public AssessmentContext<I, T> create();
    }
}
