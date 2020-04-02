/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.openide.util.lookup.NamedServiceDefinition;

/**
 *
 * @author boris.heithecker
 */
@NamedServiceDefinition(path = "ConfigurationPanelComponent/@contentType()", serviceType = ConfigurationPanelComponentProvider.class)
@Retention(value = RetentionPolicy.SOURCE)
@Target(value = ElementType.TYPE)
public @interface ConfigurationPanelContentTypeRegistration {

    public String[] contentType();

    public int position() default Integer.MAX_VALUE;
}
