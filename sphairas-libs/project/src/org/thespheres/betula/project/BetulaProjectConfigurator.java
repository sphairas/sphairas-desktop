/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project;

import java.util.Properties;
import org.thespheres.betula.services.ProviderInfo;

/**
 *
 * @author boris.heithecker
 */
public interface BetulaProjectConfigurator {

    public ProviderInfo getProviderInfo();

    public UnitTargetProjectTemplate createProjectTemplate(Properties template);

}
