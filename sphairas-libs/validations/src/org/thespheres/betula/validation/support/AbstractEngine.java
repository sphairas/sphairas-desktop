/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.thespheres.betula.validation.Validation;
import org.thespheres.betula.validation.ValidationNodeSet;
import org.thespheres.betula.validation.ValidationResultSet;

/**
 *
 * @author boris.heithecker
 * @param <E>
 */
public abstract class AbstractEngine<E extends ExecutorService> extends ValidationEngine {

    protected final E executor;
    private final Map<String, ValidationResultSet<?, ?>> validations = new HashMap<>();

    public AbstractEngine(String engineId, E executor) {
        super(engineId);
        this.executor = executor;
    }

    public E getExecutor() {
        return executor;
    }

    protected void enqueue(String id, ManagedValidationResultSet<?, ?> start, Map<String, ValidationResultSet<?, ?>> dependent) {
        ManagedValidationNodeSet set = new ManagedValidationNodeSet();
        dependent.forEach(set::add);
        start.setValidationNodeSet(set);
        if (start instanceof ValidationResultSet.ValidationListener<?>) {
            set.nodes.forEach((v, s) -> s.addValidationListener((ValidationResultSet.ValidationListener) start));
        }
        if (executor != null) {
            start.setExecutorService(executor);
            set.nodes.forEach((v, s) -> executor.submit(s));
            executor.submit(start);
        } else {
            set.nodes.forEach((v, s) -> s.run());
            start.run();
        }
        //create links, 
    }

    protected ValidationResultSet<?, ?> forId(String id) {
        synchronized (validations) {
            return validations.computeIfAbsent(id, this::createValidationSet);
        }
    }

    protected abstract ValidationResultSet<?, ?> createValidationSet(String id);

    protected abstract Validation<?, ?> unwrap(String id, ValidationResultSet<?, ?> set);

    class ManagedValidationNodeSet implements ValidationNodeSet {

        private final ChangeSupport cSupport = new ChangeSupport(AbstractEngine.this);
        private final Map<String, ValidationResultSet<?, ?>> nodes = new HashMap<>();

        @Override
        public List<Validation<?, ?>> getNodes() {
            synchronized (nodes) {
                return nodes.entrySet().stream()
                        .map(e -> unwrap(e.getKey(), e.getValue()))
                        .collect(Collectors.toList());
            }
        }

        @Override
        public <V extends Validation<?, ?>> List<V> getNodes(Class<V> type) {
            return getNodes().stream()
                    .filter(type::isInstance)
                    .map(type::cast)
                    .collect(Collectors.toList());
        }

        private void add(String key, ValidationResultSet<?, ?> value) {
            synchronized (nodes) {
                nodes.put(key, value);
            }
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cSupport.removeChangeListener(listener);
        }

    }
}
