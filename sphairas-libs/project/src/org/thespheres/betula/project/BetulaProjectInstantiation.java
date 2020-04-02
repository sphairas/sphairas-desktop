/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 *
 * @author boris.heithecker
 */
public interface BetulaProjectInstantiation {

    public void instatiate(Path project, Path sphairas, Properties prop, BetulaProjectType... type) throws IOException;

}
