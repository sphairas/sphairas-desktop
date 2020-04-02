/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.thespheres.betula.admin.units.ConfigurationTopComponentModel;

/**
 *
 * @author boris.heithecker
 * @param <K>
 */
public abstract class ConfigNodeTopComponentNodeList<K> extends ConfigurationTopComponentModel {

    public Key<String> SINGLE_NODE_KEY = new Key("single-node-key");
    protected final ChangeSupport cSupport = new ChangeSupport(this);
    private final int preferredPosition;

    protected ConfigNodeTopComponentNodeList(String provider, int preferredPosition) {
        super(provider);
        this.preferredPosition = preferredPosition;
    }

    public int getPreferredPosition() {
        return preferredPosition;
    }

    public Node[] getNodes(final Key<K> key) {
        if (key.getKey().toString().equals("single-node-key")) {
            return new Node[]{getSingleNode()};
        }
        return null;
    }

    protected Node getSingleNode() {
        throw new UnsupportedOperationException("Must be implemented.");
    }

    public Key<K>[] getKeys() {
        return new Key[]{SINGLE_NODE_KEY};
    }

    public void addChangeListener(ChangeListener l) {
        cSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cSupport.removeChangeListener(l);
    }

    public class Key<K> {

        private final K key;

        public Key(final K key) {
            this.key = key;
        }

        public K getKey() {
            return key;
        }

        public ConfigNodeTopComponentNodeList getConfigNodeTopComponentNodeList() {
            return ConfigNodeTopComponentNodeList.this;
        }

    }

    public static abstract class Provider {

        protected final ChangeSupport cSupport = new ChangeSupport(this);

        public abstract List<ConfigNodeTopComponentNodeList> nodeLists();

        public void addChangeListener(ChangeListener l) {
            cSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cSupport.removeChangeListener(l);
        }

    }

    public static abstract class Factory<I extends ConfigNodeTopComponentNodeList> {

        private final String name;

        protected Factory(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public abstract I create(final String provider, final Map<String, Object> props);

        @Retention(RetentionPolicy.SOURCE)
        @Target({ElementType.TYPE})
        public @interface Registration {
        }

    }

}
