/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.thespheres.betula.Unit;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "couchdb-journal-mappings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CouchDBMappings implements Runnable {

    public static final String MAPPINGS_FILE = "/journal-couchdb/mappings.xml";
    static final int SAVE_DELAY = 2000;
    private static CouchDBMappings INSTANCE;
    private static JAXBContext JAXB;
    private final static RequestProcessor RP = new RequestProcessor(CouchDBMappings.class);
    @XmlTransient
    private final RequestProcessor.Task SAVE = RP.create(this);
    @XmlElement(name = "mapping")
    private final List<Entry> entries = new ArrayList<>();

    public static CouchDBMappings getInstance() {
        if (INSTANCE == null) {
            loadInstance();
        }
        return INSTANCE;
    }

    private synchronized static void loadInstance() {
        if (JAXB == null) {
            try {
                JAXB = JAXBContext.newInstance(CouchDBMappings.class);
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }
        final FileObject mf = FileUtil.getConfigFile(MAPPINGS_FILE);
        if (mf != null) {
            try {
                try (final InputStream is = mf.getInputStream()) {
                    INSTANCE = (CouchDBMappings) JAXB.createUnmarshaller().unmarshal(is);
                }
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        } else {
            INSTANCE = new CouchDBMappings();
            INSTANCE.scheduleForSave();
        }
    }

    void scheduleForSave() {
        SAVE.schedule(SAVE_DELAY);
    }

    @Override
    public void run() {
        FileObject mf = FileUtil.getConfigFile(MAPPINGS_FILE);
        if (mf == null) {
            try {
                mf = FileUtil.createData(FileUtil.getConfigRoot(), MAPPINGS_FILE);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        try (final OutputStream os = mf.getOutputStream()) {
            final Marshaller m = JAXB.createMarshaller();
            m.setProperty("jaxb.formatted.output", Boolean.TRUE);
            m.marshal(INSTANCE, os);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public DocumentId mapTarget(final DocumentId extracted) {
        return entries.stream()
                .filter(e -> Objects.equals(e.mappedTarget, extracted))
                .collect(CollectionUtil.singleton())
                .map(e -> e.target)
                .orElse(extracted);
    }

    public UnitId mapUnit(final UnitId extracted) {
        return entries.stream()
                .filter(e -> Objects.equals(e.mappedUnit, extracted))
                .collect(CollectionUtil.singleton())
                .map(e -> e.unit)
                .orElse(extracted);
    }

    public String getDisplayName(final Unit unit) {
        return entries.stream()
                .filter(e -> Objects.equals(e.mappedUnit, unit.getUnitId()))
                .collect(CollectionUtil.singleton())
                .map(e -> {
                    try {
                        return NamingResolver.find(e.provider).resolveDisplayName(e.unit);
                    } catch (IllegalAuthorityException ex) {
                        return null;
                    }
                })
                .orElse(unit.getDisplayName());
    }

    public void map(final UnitId mappedUnit, final DocumentId mappedTarget, final UnitId toUnit, final DocumentId toTarget, final String provider) {
        final Entry e = new Entry(mappedUnit, mappedTarget, toUnit, toTarget, provider);
        if (entries.contains(e)) {
            entries.remove(e);
        }
        entries.add(e);
        scheduleForSave();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Entry {

        @XmlElement(name = "mapped-target")
        private DocumentId mappedTarget;
        @XmlElement(name = "mapped-unit")
        private UnitId mappedUnit;
        @XmlAttribute(name = "provider", required = true)
        private String provider;
//        @XmlElementWrapper(name = "targets")
        @XmlElement(name = "target")
        private DocumentId target;
//        @XmlElementWrapper(name = "units")
        @XmlElement(name = "unit")
        private UnitId unit;

        public Entry() {
        }

        Entry(UnitId mappedUnit, DocumentId mappedTarget, UnitId unit, DocumentId target, String provider) {
            this.mappedTarget = mappedTarget;
            this.mappedUnit = mappedUnit;
            this.provider = provider;
            this.target = target;
            this.unit = unit;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + Objects.hashCode(this.mappedTarget);
            return 59 * hash + Objects.hashCode(this.mappedUnit);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if (!Objects.equals(this.mappedTarget, other.mappedTarget)) {
                return false;
            }
            return Objects.equals(this.mappedUnit, other.mappedUnit);
        }

    }
}
