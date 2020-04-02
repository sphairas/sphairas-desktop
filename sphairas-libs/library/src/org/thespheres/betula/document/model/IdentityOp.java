/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import java.util.Objects;
import org.thespheres.betula.Identity;

/**
 *
 * @author boris.heithecker
 * @param <Source>
 * @param <Target>
 */
public interface IdentityOp<Source extends Identity<String>, Target> {

    public Target convert(Source s);

    public String match(Source s);

    default public boolean equal(Source source, Source target) {
        return Objects.equals(convert(source), convert(target));
    }

}
