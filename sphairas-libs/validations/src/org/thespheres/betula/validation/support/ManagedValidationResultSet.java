/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.support;

import java.util.concurrent.ExecutorService;
import org.thespheres.betula.validation.ValidationNodeSet;
import org.thespheres.betula.validation.ValidationResult;
import org.thespheres.betula.validation.ValidationResultSet;

/**
 *
 * @author boris.heithecker
 * @param <Model>
 * @param <Result>
 */
public interface ManagedValidationResultSet<Model, Result extends ValidationResult> extends ValidationResultSet<Model, Result> {

    public void setValidationNodeSet(ValidationNodeSet nodes) throws IllegalStateException;

    public void setExecutorService(ExecutorService executor) throws IllegalStateException;

}
