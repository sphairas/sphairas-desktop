/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

import java.io.Serializable;

/**
 *
 * @author boris.heithecker
 */
final class NullTag implements Tag, Serializable {

    NullTag() {
    }

    @Override
    public String getConvention() {
        return "null";
    }

    @Override
    public String getId() {
        return "null";
    }

    @Override
    public String getLongLabel(Object... args) {
        if (args != null && args.length > 0 && args[0] instanceof String) {
            return (String) args[0];
        }
        return "";
    }

    @Override
    public String getShortLabel() {
        return "";
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj == this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

}
