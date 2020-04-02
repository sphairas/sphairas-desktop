/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation;

/**
 *
 * @author boris.heithecker
 * @param <Model>
 * @param <Result>
 */
//User ValidationResultSet.Provider to create one
public interface Validation<Model, Result extends ValidationResult> {

    public String getId();

    public String getDisplayName();

    public String getDescription();

    public ValidationResultSet<Model, Result> getResultSet();
}
