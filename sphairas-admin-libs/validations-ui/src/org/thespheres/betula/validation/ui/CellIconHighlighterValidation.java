/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.ui;

import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.ui.swingx.CellIconHighlighterDelegate;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.validation.ValidationResult;
import org.thespheres.betula.validation.ValidationResultSet;

/**
 *
 * @author boris.heithecker
 * @param <M>
 * @param <Model>
 * @param <R>
 */
public abstract class CellIconHighlighterValidation<M, Model, R extends ValidationResult> extends CellIconHighlighterDelegate implements HighlightPredicate, LookupListener, ValidationResultSet.ValidationListener<R> {

    protected final RequestProcessor RP = new RequestProcessor(CellIconHighlighterValidation.class);
    protected final Lookup.Result<M> result;
    protected M support;
    private ValidationResultSet<Model, R> validation;
    protected final Class<M> implementationType;

    @SuppressWarnings({"LeakingThisInConstructor", ""})
    protected CellIconHighlighterValidation(String iconBase, Class<M> impl, Lookup context) {
        super(iconBase);
        this.implementationType = impl;
        this.result = context.lookupResult(impl);
        this.result.addLookupListener(this);
        setHighlightPredicate(this);
        RP.post(this::init);
    }

    public ValidationResultSet<Model, R> getValidationResultSet() {
        return validation;
    }

    protected void setValidation(ValidationResultSet<Model, R> validation) {
        final ValidationResultSet<Model, R> b = getValidationResultSet();
        if (b != null) {
            b.removeValidationListener(this);
        }
        this.validation = validation;
        if (this.validation != null) {
            this.validation.addValidationListener(this);
        }
    }

    protected void init() {
        support = result.allInstances().stream()
                .map(implementationType::cast)
                .collect(CollectionUtil.singleOrNull());
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        RP.post(this::init);
    }

}
