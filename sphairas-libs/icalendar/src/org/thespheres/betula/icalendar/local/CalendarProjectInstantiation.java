/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.icalendar.local;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Properties;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.project.BetulaProjectInstantiation;
import org.thespheres.betula.project.BetulaProjectType;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = BetulaProjectInstantiation.class)
public class CalendarProjectInstantiation implements BetulaProjectInstantiation {
    
    public static final String LOCAL_CALENDAR_FILENAME = "local.ics";
    
    @Override
    public void instatiate(Path project, Path sphairas, Properties prop, BetulaProjectType... type) throws IOException {
        if (Arrays.stream(type).anyMatch(t -> t.equals(BetulaProjectType.LOCAL) || t.equals(BetulaProjectType.PROVIDER))) {
            Path calendar = sphairas.resolve(LOCAL_CALENDAR_FILENAME);
            try (InputStream cis = CalendarProjectInstantiation.class.getClassLoader().getResourceAsStream("/org/thespheres/betula/icalendar/resources/template.ics")) {
                Files.copy(cis, calendar, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    
}
