/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.thespheres.betula.Identity;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Envelope;

/**
 *
 * @author boris.heithecker
 */
public class DocumentUtilities {

    private DocumentUtilities() {
    }

    public static <I extends Identity> boolean isEntryIdentity(Envelope node, Class<I> idType) {
        return node instanceof Entry && idType.isAssignableFrom(((Entry) node).getIdentity().getClass());
    }

    public static <I extends Identity> List<I> extractIdentityListChildren(Envelope node, Class<I> idType) {
        return extractIdentityListChildren(node, idType, null);
    }

    public static <I extends Identity> List<I> extractIdentityListChildren(Envelope node, Class<I> idType, Action required) {
        return node.getChildren().stream()
                .filter(n -> n instanceof Entry && idType.isAssignableFrom(((Entry) n).getIdentity().getClass()))
                .map(n -> (Entry<I, ?>) n)
                .filter(e -> required == null || required.equals(e.getAction()))
                .map(Entry::getIdentity)
                .collect(Collectors.toList());
    }

    public static <I extends Identity> List<Entry<I, ?>> extractEntryListChildren(Envelope node, Class<I> idType) {
        return extractEntryListChildren(node, idType, null);
    }

    public static <I extends Identity> List<Entry<I, ?>> extractEntryListChildren(Envelope node, Class<I> idType, Action required) {
        return node.getChildren().stream()
                .filter(n -> n instanceof Entry && idType.isAssignableFrom(((Entry) n).getIdentity().getClass()))
                .map(n -> (Entry<I, ?>) n)
                .filter(e -> required == null || required.equals(e.getAction()))
                .collect(Collectors.toList());
    }

    public static List<Envelope> findEnvelope(Container container, final String[] path) {
        final List<Envelope> ret = new ArrayList<>();
        for (Container.PathDescriptorElement pd : container.getPathElements()) {
            Envelope node;
            ArrayList<String> elements = new ArrayList<>();
            while (true) {
                elements.add(pd.getIdentifier());
                if (pd.getChild() != null) {
                    pd = pd.getChild();
                    continue;
                }
                node = pd.getEnvelope();
                break;
            }
            boolean ident = Arrays.equals(path, elements.toArray(new String[elements.size()]));
            if (ident) {
                ret.add(node);
            }
        }
        return ret;
    }

    public static <I extends Identity> List<Entry<I, ?>> findEntry(Container container, String[] path, Class<I> idClass) {
        List<Entry<I, ?>> ret = new ArrayList<>();
        List<Envelope> l = findEnvelope(container, path);
        l.stream()
                .filter(Entry.class::isInstance)
                .map(Entry.class::cast)
                .filter(n -> idClass.isAssignableFrom(n.getIdentity().getClass()))
                .forEach(ret::add);
        return ret;
    }

}
