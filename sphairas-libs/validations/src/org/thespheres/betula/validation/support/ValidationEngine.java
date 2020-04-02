/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.support;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.validation.Validation;
import org.thespheres.betula.validation.ValidationResult;

/**
 *
 * @author boris.heithecker
 */
public abstract class ValidationEngine {

    private final String engineId;

    protected ValidationEngine(String engineId) {
        this.engineId = engineId;
    }

    public String getEngineId() {
        return engineId;
    }

    public abstract Validation<?, ?> createValidation(String validationId, Lookup context);

    public <V extends Validation<?, ?>> V createValidation(String validationId, Class<V> resultType, Lookup context) {
        try {
            return (V) createValidation(validationId, context);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public <Model, V extends Validation<Model, ?>> V createValidation(String validationId, Class<V> resultType, Model context) {
        try {
            return (V) createValidation(validationId, Lookups.singleton(context));
        } catch (ClassCastException e) {
            return null;
        }
    }

    public <Result extends ValidationResult> Validation<?, Result> createValidation(String validationId, Lookup context, Class<Result> resultType) {
        try {
            return (Validation<?, Result>) createValidation(validationId, context);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public <Model, Result extends ValidationResult> Validation<Model, Result> createValidation(String validationId, Model model, Class<Result> resultType, Class<Model> modelType) {
        try {
            return (Validation<Model, Result>) createValidation(validationId, Lookups.singleton(model));
        } catch (ClassCastException e) {
            return null;
        }
    }
}
