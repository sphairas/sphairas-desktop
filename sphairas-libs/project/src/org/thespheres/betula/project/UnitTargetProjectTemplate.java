/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project;

import org.thespheres.betula.services.util.UnitTarget;
import java.util.Enumeration;
import java.util.Properties;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.project.UnitTargetProjectTemplate.UnitTargetSelection;
import org.thespheres.betula.project.ServiceProjectTemplate.Selection;

/**
 *
 * @author boris.heithecker
 */
public abstract class UnitTargetProjectTemplate extends ServiceProjectTemplate<UnitTargetSelection> {

    private final Properties template;

    public UnitTargetProjectTemplate(String provider, Properties template) {
        super(provider);
        this.template = template;
    }

    public class UnitTargetSelection extends UnitTarget implements Selection {

        protected UnitTargetSelection(UnitId unit, DocumentId base, String provider, String namingProv) {
            super(unit, base, provider, namingProv);
        }

        @Override
        public Properties createProjectProperties(Properties defaults) {
            final Properties prop = new Properties();
            final Enumeration<?> keys = template.propertyNames();
            while (keys.hasMoreElements()) {
                String k = (String) keys.nextElement();
                prop.setProperty(k, template.getProperty(k));
            }
            prop.setProperty("providerURL", getProvider());
            prop.setProperty("unit.id", getUnitId().getId());
            prop.setProperty("unit.authority", getUnitId().getAuthority());
            prop.setProperty("baseTarget.documentId", getTargetBase().getId());
            prop.setProperty("baseTarget.documentAuthority", getTargetBase().getAuthority());
            return prop;
        }
    }

}
