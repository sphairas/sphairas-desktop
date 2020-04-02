/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.io.IOException;
import org.openide.nodes.Node;
import org.thespheres.betula.document.model.MarkerDecoration;
import org.thespheres.betula.services.LocalProperties;

/**
 *
 * @author boris.heithecker
 */
public interface UnitOpenSupport {

    public Node getNodeDelegate();

    public LocalProperties findBetulaProjectProperties() throws IOException;

    public MarkerDecoration findMarkerDecoration() throws IOException;
}
