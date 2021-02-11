/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.naming;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.thespheres.betula.Identity;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalFileProperties;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.TermSchedule;

/**
 *
 * @author boris.heithecker
 */
public abstract class Naming {

    public static final String HINT_NURSTUFE = "naming.only.level";
    public static final String HINT_OHNE_FACH = "naming.no.subject";
    public static final String KLASSE_OHNE_SCHULJAHRESANGABE = "klasse.ohne.schuljahresangabe";
    protected static final Pattern AG_PATTERN = Pattern.compile("([äöüß\\p{Alpha}]+-)?ag-[äöüß\\p{Alpha}]+", 0);
    //    private static final Pattern PATTERN = Pattern.compile("([äöüß\\p{Alpha}]+-)?(abitur|([äöüß\\p{Alpha}]+-(abitur)?))(1|2)\\d\\d\\d-(jg(\\\\da?|10)-)?[äöüß\\p{Alpha}\\d]+(-[äöüß\\p{Alpha}\\d]+)*", 0);
    protected static final Pattern PATTERN = Pattern.compile("([äöüß\\p{Alpha}]+-)?(abitur|((?!\\.)[äöüß\\.\\p{Alpha}]+(?<!\\.)-(abitur)?))(1|2)\\d\\d\\d(-jg(\\\\da?|10))?(-[äöüß\\p{Alpha}\\d]+(-[äöüß\\p{Alpha}\\d]+)*)?", 0);
    public static final String FACH_KURZ = "fach.kurz";
    public static final String SUFFIX = "suffix";
    public static final String STUFE = "stufe";
    //Naming
    public static final String KURS_KLASSE_ID = "kurs_id";
    public static final String FACH = "fach";
    public static final String START_JAHR = "defining-year";
    public static final String KURS_KLASSE_ID2 = "kurs_id2"; //Schülerfirma
    public static final String SGL = "sgl";
    protected final Map<Identity, NamingResolver.Result> cache = new HashMap<>();
    protected final DocumentsModel baseDocs;
    protected final Map<UnitId, NamingResolver.Result> agNamen = new HashMap<>();
    protected String authority;
    protected final String provider;
    protected final TermSchedule schedule;
    protected final int baseStufe;
    protected final int baseAbitur;
    private final Map<String, String> props = new HashMap<>();
    public static final String FIRST_ELEMENT = "first-element";
    public static final String BASE_LEVEL = "base-level";
    public static final String BASE_LEVEL_ABITUR = "base-level-abitur";

    protected Naming(String provider, TermSchedule ts, String firstElement, int baseStufe, int baseAbitur) {
        this.provider = provider;
        this.schedule = ts;
        this.baseStufe = baseStufe;
        this.baseAbitur = baseAbitur;
        props.put(FIRST_ELEMENT, firstElement);
        props.put(BASE_LEVEL, Integer.toString(baseStufe));
        props.put(BASE_LEVEL_ABITUR, Integer.toString(baseAbitur));
        LocalFileProperties lfp = LocalFileProperties.find(provider);
        if (lfp != null) {
            DocumentsModel dm = new DocumentsModel();
            Map<String, String> pp = new HashMap<>(lfp.getProperties());
            authority = lfp.getProperty("authority");
            if (authority != null) {
                pp.put(DocumentsModel.PROP_AUTHORITY, authority);
            } else {
                Logger.getLogger(Naming.class.getName()).log(Level.CONFIG, "No authority set in Naming of provider {0}. Please fix!", provider);
            }
            dm.initialize(pp);
            baseDocs = dm;
        } else {
            throw new IllegalStateException("Could not instantiated DocumentsModel.");
        }
    }

    public Map<String, String> properties() {
        return props;
    }

    public NamingResolver.Result resolve(Identity identity) throws IllegalAuthorityException {
        if (authority != null && !identity.getAuthority().equals(authority)) {
            throw new IllegalAuthorityException();
        }
        //Funktioniert nicht, wenn in den Results hints gesetzt sind!!!!!
        //        synchronized(cache) {
        //            return cache.computeIfAbsent(identity, i -> createResult(i));
        //        }
        return createResult(identity);
    }

    protected NamingResolver.Result createResult(Identity identity) throws NumberFormatException {
        String suffix = null;
        if (identity instanceof DocumentId) {
            DocumentId did = (DocumentId) identity;
            suffix = baseDocs.getSuffix(did);
            DocumentId c = baseDocs.convert(did);
            if (c != null) {
                identity = c;
            }
        }
        final Object id = identity.getId();
        final NamingResolver.Result unresolved = new NamingResolver.Result(Collections.EMPTY_MAP) {
            @Override
            public String getResolvedName(Object... params) {
                return id.toString();
            }
        };
        unresolved.addResolverHint(NamingResolver.Result.HINT_UNRESOLVED);
        if (!(id instanceof String)) {
            return unresolved;
        }
        String name = (String) id;
        final String[] parts = name.split("-");
        final String firstElement = props.get(FIRST_ELEMENT);
        if (AG_PATTERN.matcher(name).matches()) {
            if (firstElement != null && !parts[0].equals(firstElement)) {
                return unresolved;
            }
            return agResult(unresolved, identity);
        }
        if (!PATTERN.matcher(name).matches()) {
            return unresolved;
        }
        if (firstElement != null && !parts[0].equals(firstElement)) {
            return unresolved;
        }
        final HashMap<String, String> elements = new HashMap<>();
        int pointer = firstElement != null ? 1 : 0;
        boolean jahrgang = false;
        boolean klasse = false;
        int jahr;
        int stufe = baseStufe;
        boolean aufsteigend = true;
        boolean sfa = false;
        //Klasse, Jahrgang, Fach
        String type = parts[pointer++];
        if (type.startsWith("abitur")) {
            jahrgang = true;
            --pointer;
        } else {
            switch (type) {
                case "klasse":
                    klasse = true;
                    break;
                case "jahrgang":
                    jahrgang = true;
                    break;
                case "schuelerfirma":
                    sfa = true;
                    break;
                default:
                    if (!findFachMarker(type, unresolved, elements)) {
                        return unresolved;
                    }
            }
        }
        //jahrgang
        String dj = parts[pointer++];
        final boolean abitur = dj.startsWith("abitur");
        if (abitur) {
            dj = dj.substring("abitur".length());
            stufe = baseAbitur;
        }
        jahr = Integer.parseInt(dj);
        if (parts.length > pointer) {
            String next = parts[pointer];
            if (next.startsWith("jg")) {
                if (next.endsWith("a")) {
                    stufe = Integer.parseInt(next.substring("jg".length(), next.length() - "a".length()));
                } else {
                    stufe = Integer.parseInt(next.substring("jg".length()));
                    aufsteigend = false;
                }
                pointer++;
            }
        }
        //
        if (parts.length > pointer) {
            String id1 = parts[pointer++];
            elements.put(KURS_KLASSE_ID, id1);
        }
        if (parts.length > pointer) {
            String id2 = parts[pointer++];
            elements.put(KURS_KLASSE_ID2, id2);
        }
        if (parts.length > pointer) {
            String sgl = parts[pointer++];
            elements.put(SGL, sgl);
        }
        if (suffix != null) {
            elements.put(SUFFIX, suffix);
        }
        return new NamingResultImpl(elements, jahrgang, klasse, jahr, stufe, aufsteigend, sfa, abitur, suffix, schedule);
    }

    protected NamingResolver.Result agResult(final NamingResolver.Result unresolved, Identity id) {
        final UnitId uid;
        if (id instanceof UnitId) {
            uid = (UnitId) id;
        } else if (id instanceof DocumentId) {
            uid = baseDocs.convertToUnitId((DocumentId) id);
        } else {
            return unresolved;
        }
        NamingResolver.Result cn;
        synchronized (agNamen) {
            cn = agNamen.computeIfAbsent(uid, k -> new CommonName(k, provider));
        }
        return cn;
    }

    protected abstract boolean findFachMarker(String type, NamingResolver.Result unresolved, HashMap<String, String> elements);

}
