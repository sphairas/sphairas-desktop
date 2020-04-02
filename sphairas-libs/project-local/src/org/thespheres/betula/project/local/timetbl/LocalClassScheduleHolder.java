/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.timetbl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.thespheres.betula.project.local.unit.LocalUnit;

/**
 *
 * @author boris.heithecker
 */
class LocalClassScheduleHolder {

    private static JAXBContext jaxb;
    private final URI configPath;
    private final static Map<URI, LocalClassScheduleHolder> INSTANCES = new HashMap<>();
    private final Object[] lcs = new Object[]{null};

    private LocalClassScheduleHolder(URI configPath) {
        this.configPath = configPath;
        load();
    }

    static LocalClassScheduleHolder get(URI configPath) {
        synchronized (INSTANCES) {
            return INSTANCES.computeIfAbsent(configPath, uri -> new LocalClassScheduleHolder(uri));
        }
    }

    static JAXBContext getJAXBContext() {
        synchronized (LocalUnit.class) {
            if (jaxb == null) {
                try {
                    jaxb = JAXBContext.newInstance(LocalClassSchedule.class);
                } catch (JAXBException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return jaxb;
        }
    }

    LocalClassSchedule getLocalClassSchedule() throws IOException {
        synchronized (lcs) {
            if (lcs[0] == null) {
                lcs[0] = load();
            }
        }
        if (lcs[0] instanceof IOException) {
            throw (IOException) lcs[0];
        }
        return (LocalClassSchedule) lcs[0];
    }

    void invalidate() {
        synchronized (lcs) {
            lcs[0] = null;
        }
    }

    private Object load() {
        Path plcs = Paths.get(configPath).resolve(LocalClassSchedule.CLASS_SCHEDULE_FILE);
        if (!Files.exists(plcs)) {
            LocalClassSchedule o = new LocalClassSchedule();
            for (int i = 1; i < 11; i++) {
                o.getTimes().add(new LocalClassSchedule.Time(null, null, i));
            }
            return o;
        }
        JAXBContext ctx = getJAXBContext();
        try {
            return (LocalClassSchedule) ctx.createUnmarshaller().unmarshal(Files.newInputStream(plcs));
        } catch (JAXBException | IOException ex) {
            return ex;
        }
    }

}
