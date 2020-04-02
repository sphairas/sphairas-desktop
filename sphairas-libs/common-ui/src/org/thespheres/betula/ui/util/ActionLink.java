package org.thespheres.betula.ui.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author boris.heithecker
 */
@Retention(value = RetentionPolicy.SOURCE)
@Target(value = {ElementType.METHOD})
public @interface ActionLink {

    public String category();

    public String id();

    public int position() default Integer.MAX_VALUE;
}
