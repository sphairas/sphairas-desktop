/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation;

import java.util.Collection;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.tag.AbstractState;
import org.thespheres.betula.tag.State;

/**
 *
 * @author boris.heithecker
 * @param <Model>
 * @param <Result>
 */
public interface ValidationResultSet<Model, Result extends ValidationResult> extends Collection<Result>, Runnable {

    public static final State NONE = new ProcessorState(-1, "not.initialized");
    public static final State STARTED = new ProcessorState(0, "started");
    public static final State RESUMED = new ProcessorState(0, "resumed");
    public static final State FINISHED = new ProcessorState(1, "finished");

    public State getState();

    public ValidationResultSet<?, ?> getParentValidation();

    //Child tasks
    public ValidationNodeSet getNodesSet();

    public void addValidationListener(ValidationListener<Result> l);

    public void removeValidationListener(ValidationListener<Result> l);

    static final class ProcessorState extends AbstractState<ProcessorState> {

        private final static String NAME = "validation.result.set.state";

        ProcessorState(int level, String id) {
            super(level, false, NAME, id);
        }

    }

    public static interface ValidationListener<Result> {

        public void onStart(int size, Cancellable cancel);

        public void onStop();

        public void resultAdded(final Result result);

        public void resultRemoved(final Result result);

    }

    //Service to register etc..
    public static interface Provider<D, R extends ValidationResult, V extends Validation<D, R>> {

        public String getValidationId();

        default public ValidationResultSet<D, R> createValidationResultSet(D bundle) {
            return createValidationResultSet(Lookups.singleton(bundle));
        }

        public ValidationResultSet<D, R> createValidationResultSet(Lookup context);
    }
}
