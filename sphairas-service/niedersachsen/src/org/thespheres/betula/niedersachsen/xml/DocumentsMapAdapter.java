/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.xml;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentsMapAdapter extends XmlAdapter<DocumentsMapAdapter, Map<String, DocumentId>> {

    @XmlElement(name = "Liste")
    private DocumentEntry[] entries;

    public DocumentsMapAdapter() {
        this.entries = new DocumentEntry[0];
    }

    DocumentsMapAdapter(final Map<String, DocumentId> props) {
        entries = props.entrySet().stream()
                .map(e -> new DocumentEntry(e.getKey(), e.getValue()))
                .toArray(DocumentEntry[]::new);
    }

    @Override
    public Map<String, DocumentId> unmarshal(final DocumentsMapAdapter v) throws Exception {
        return Arrays.stream(v.entries)
                .collect(Collectors.toMap(e -> e.key, e -> e.val));
    }

    @Override
    public DocumentsMapAdapter marshal(final Map<String, DocumentId> v) throws Exception {
        return new DocumentsMapAdapter(v);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DocumentEntry {

        @XmlAttribute(name = "Name", required = true)
        private String key;
        @XmlElement(name = "Listen-ID")
        private DocumentId val;

        public DocumentEntry() {
        }

        DocumentEntry(String key, DocumentId value) {
            this.key = key;
            this.val = value;
        }

    }

}
