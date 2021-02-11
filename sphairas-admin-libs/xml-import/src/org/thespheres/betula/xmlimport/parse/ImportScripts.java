/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.parse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker@gmx.net
 */
@Messages({"ImportScripts.ScriptException.message=Exception thrown invoking funktion {0} with resolved name \"{1}\" and params {2}"})
public class ImportScripts {

    public static final String FUNCTION_PARSE_UNIT_NAME = "parseUnitName";
    private final Invocable engine;
    private final String provider;
    private final Map<String, String> configuration = new HashMap<>();

    private ImportScripts(final String provider, final Map<String, String> config, final Invocable engine) {
        this.provider = provider;
        this.engine = engine;
        this.configuration.putAll(config);
    }

    public static ImportScripts create(final String provider, final Map<String, String> config, final InputStream source) throws IOException {
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("nashorn");
        try (final Reader fr = new InputStreamReader(source)) {
            engine.eval(fr);
        } catch (final ScriptException ex) {
            throw new IOException(ex);
        }
        return new ImportScripts(provider, config, (Invocable) engine);
    }

    public String parseUnitName(final String resolvedName, final Marker subject, final int referenzjahr, final Integer checkLevel) {
        final Map<String, Object> props = new HashMap<>();
        props.put("year", referenzjahr);
        if (subject != null) {
            props.put("subject", subject.toString());
        }
        if (checkLevel != null) {
            props.put("level", checkLevel);
        }
        props.put("provider", provider);
        props.putAll(this.configuration);
        final Object res;
        try {
            res = engine.invokeFunction(FUNCTION_PARSE_UNIT_NAME, resolvedName, props);
        } catch (final ScriptException scex) {
            final String params = props.entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue())
                    .collect(Collectors.joining(";", "\"", "\""));
            final String message = NbBundle.getMessage(ImportScripts.class, "ImportScripts.ScriptException.message", new Object[]{FUNCTION_PARSE_UNIT_NAME, resolvedName, params});
            PlatformUtil.getCodeNameBaseLogger(ImportScripts.class).log(Level.WARNING, message);
            return null;
        } catch (final NoSuchMethodException nsmex) {
            return null;
        }
        if (res instanceof String) {
            return (String) res;
        }
        return null;
    }

}
