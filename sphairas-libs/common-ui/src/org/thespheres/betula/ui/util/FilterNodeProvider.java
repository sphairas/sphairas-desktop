/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author boris.heithecker
 * @param <O>
 */
@MimeLocation(subfolderName = "FilterNodeProvider")
public interface FilterNodeProvider<O> {

    public FilterNode createFilterNode(Node original, O object);
}
