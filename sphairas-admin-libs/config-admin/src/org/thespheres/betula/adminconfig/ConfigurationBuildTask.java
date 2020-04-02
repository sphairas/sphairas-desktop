/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import org.thespheres.betula.services.WebProvider;

/**
 *
 * @author boris.heithecker
 */
public interface ConfigurationBuildTask {

    public String getLockToken();

    public Path getProviderBasePath();

    public URI resolveResource(String resource);

    public WebProvider getWebProvider();

    public byte[] getBytes(List<String> list);
    
    public void setLastModified(String resource, String lm);

    public void cancel(Exception ex);
}
