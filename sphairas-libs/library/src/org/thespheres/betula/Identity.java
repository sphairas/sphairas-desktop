/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

import java.util.StringJoiner;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public abstract class Identity<I> {

    public static final String AUTHORITY_DELIMITER = "@";
    public static final String VERSION_DELIMITER = "/";

    public abstract I getId();

    public abstract String getAuthority();

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(AUTHORITY_DELIMITER);
        sj.add(getId().toString());
        final String authority = getAuthority();
        if (authority != null) {
            sj.add(authority);
        }
        return sj.toString();
    }

}
