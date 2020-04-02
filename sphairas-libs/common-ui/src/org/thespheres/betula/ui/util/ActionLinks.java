/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.util;

import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.openide.util.lookup.Lookups;
import org.thespheres.betula.ui.impl.ActionLinkInstance;

/**
 *
 * @author boris.heithecker
 */
public class ActionLinks {

    private ActionLinks() {
    }

    public static List<ActionListener> find(String actionCategory, String actionId, Object context) {
        return Lookups.forPath(ActionLinkInstance.findPath(actionCategory, actionId)).lookupAll(ActionLinkInstance.class).stream()
                .map(ali -> ali.contextAction(context))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
