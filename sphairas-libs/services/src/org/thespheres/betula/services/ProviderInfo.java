/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author boris.heithecker
 */
public interface ProviderInfo extends Serializable {

    public String getDescription();

    //unique === authority
    public String getURL();

    public String getDisplayName();

    @Target(value = {ElementType.PACKAGE, ElementType.TYPE})
    @Retention(value = RetentionPolicy.SOURCE)
    public @interface Registration {

        public String url();

        public String displayName();

        public String description() default "";

        public String codeNameBase() default "null";
    }

}
