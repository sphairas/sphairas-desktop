/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tag;

import org.thespheres.betula.Tag;

/**
 *
 * @author boris.heithecker
 * @param <S> The actual subtype
 */
public interface State<S extends State> extends Tag {

    public boolean satisfies(S stage);

    public boolean isError();
}
