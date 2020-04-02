/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units;

import java.util.List;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import org.openide.nodes.Node;
import org.thespheres.betula.admin.units.configui.RemoteSigneeNode;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.Signees;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class SigneesTopComponentModel extends ConfigurationTopComponentModel {

    protected final UndoableEditSupport undoSupport = new UndoableEditSupport(this);

    public SigneesTopComponentModel(String provider) {
        super(provider);
    }

    public String getSigneesProviderUrl() {
        return provider;
    }

    public Node nodeForKey(Signee key) {
        return Signees.get(getSigneesProviderUrl())
                .map(s -> RemoteSignees.find(s, key))
                .map(this::createNode)
                .orElse(null);
    }

    protected Node createNode(RemoteSignee remote) {
        return new RemoteSigneeNode(remote, this);
    }

    public UndoableEditSupport getUndoSupport() {
        return undoSupport;
    }

    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        undoSupport.addUndoableEditListener(l);
    }

    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        undoSupport.removeUndoableEditListener(l);
    }

    public static interface Provider {

        public List<SigneesTopComponentModel> findAll();

        default public SigneesTopComponentModel find(final String provider) {
            return findAll().stream()
                    .filter(m -> m.getProvider().equals(provider))
                    .collect(CollectionUtil.requireSingleOrNull());
        }

    }

}
