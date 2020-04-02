/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.scheme.spi;

import java.util.Objects;

/**
 *
 * @author boris.heithecker
 */
public abstract class SchemeType {

    private final String type;

    protected SchemeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return 83 * hash + Objects.hashCode(this.type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchemeType other = (SchemeType) obj;
        return Objects.equals(this.type, other.type);
    }

}
