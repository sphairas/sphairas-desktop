/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui;
import java.util.List;
import org.thespheres.betula.admin.units.ConfigurationTopComponentModel;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class MessagesTopComponentModel extends ConfigurationTopComponentModel {

    public MessagesTopComponentModel(String provider) {
        super(provider);
    }

    public RemoteMessagesModel getRemoteMessagesModel() {
        return RemoteMessagesModel.find(getProviderInfo());
    }

    public static interface Provider {

        public List<MessagesTopComponentModel> findAll();

        default public MessagesTopComponentModel find(final String provider) {
            return findAll().stream()
                    .filter(m -> m.getProvider().equals(provider))
                    .collect(CollectionUtil.requireSingleOrNull());
        }

    }

}
