/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.ui.project;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.thespheres.betula.project.ServiceProjectTemplate;
import org.thespheres.betula.project.ServiceProjectTemplate.Selection;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.implementation.ui.project.PatternProvider.PatternSelection;
import org.thespheres.betula.xmlimport.parse.TranslateID;

/**
 *
 * @author boris.heithecker
 */
public class PatternProvider extends ServiceProjectTemplate<PatternSelection> {

    public static String PATTERN = "kgs-klasse-0000-[äöüß\\p{Alpha}\\p{Digit}]+";
//    public static MessageFormat PATTERN2 = new MessageFormat("kgs-klasse-{0}-'[äöüß\\p{Alpha}\\p{Digit}]+'");
    public static String REGEX = "[äöüß\\p{Alpha}\\p{Digit}]+";

    public PatternProvider(String provider) {
        super(provider);
    }

    @Override
    public List<PatternSelection> createList() throws IOException {
        return IntStream.range(2008, 2028)
                .mapToObj(PatternSelection::new)
                .collect(Collectors.toList());
    }

    public class PatternSelection implements Selection {

        private final int year;

        PatternSelection(int year) {
            this.year = year;
        }

        @Override
        public String getDisplayName() {
            return Integer.toString(year);
        }

        String pattern() {
//            return PATTERN.replace("0000", Integer.toString(year));
//            return PATTERN2.format(new Object[]{Integer.toString(year)});
            final StringJoiner sj = new StringJoiner("-");
            final NamingResolver nr = NamingResolver.find(provider);
            final String first = nr.properties().get("first-element");
            if (first != null) {
                sj.add(first);
            }
            sj.add(TranslateID.NO_SUBJECT_ELEMENT);
            sj.add(Integer.toString(year));
            sj.add(REGEX);
            return sj.toString();
        }

        @Override
        public Properties createProjectProperties(Properties defaults) {
            defaults.put("providerURL", provider);
            defaults.put("unitIdPattern", pattern());
            return defaults;
        }

    }
}
