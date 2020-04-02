/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.stream.Stream;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author boris.heithecker
 */
public class JAXBUtil {

    private JAXBUtil() {
    }

    public static Class[] lookupJAXBTypes(String target, Class... other) {
        return Stream.concat(Arrays.stream(other),
                Lookups.forPath(target + "/JAXBTypes").lookupAll(Class.class).stream())
                .toArray(Class[]::new);

    }

    @Retention(value = RetentionPolicy.SOURCE)
    @Target(value = {ElementType.TYPE})
    public @interface JAXBRegistration {

        public String target();

    }

    @Retention(value = RetentionPolicy.SOURCE)
    @Target(value = {ElementType.TYPE})
    public @interface JAXBRegistrations {

        public JAXBRegistration[] value();

    }
}
