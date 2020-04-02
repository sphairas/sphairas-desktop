/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.model.XmlImport;

/**
 *
 * @author boris.heithecker
 * @param <I>
 * @param <S> The source identifier
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
public abstract class AbstractLinkCollection<I extends AbstractLink, S> extends XmlImport {

    private final Class<I> type;

    protected AbstractLinkCollection(Class<I> type) {
        this.type = type;
    }

    public List<I> getLinks(S sourceId) {
        return items.stream()
                .map(type::cast)
                .filter(utl -> utl.getSourceIdentifier().equals(sourceId))
                .collect(Collectors.toList());
    }

    public I getLink(S sourceId, int clone) {
        return items.stream()
                .map(type::cast)
                .filter(utl -> utl.getSourceIdentifier().equals(sourceId) && utl.getClone() == clone)
                .collect(CollectionUtil.requireSingleOrNull());
    }

    public I addLink(S sourceId) throws IOException {
        return addLink(sourceId, 0);
    }

    public I addLink(S sourceId, int clone) throws IOException {
        synchronized (items) {
            if (items.stream()
                    .map(type::cast)
                    .anyMatch(utl -> utl.getSourceIdentifier().equals(sourceId) && utl.getClone() == clone)) {
                throw new IOException("Lesson/clone exists.");
            }
            final I ret = create(sourceId, clone);
            items.add(ret);
            return ret;
        }
    }

    public void remove(I l) {
        synchronized (items) {
            items.remove(l);
        }
    }

    protected abstract I create(S id, int clone);
}
