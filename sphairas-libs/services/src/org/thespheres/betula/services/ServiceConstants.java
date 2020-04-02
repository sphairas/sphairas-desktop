/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import org.thespheres.betula.document.AbstractMarker;
import org.thespheres.betula.document.Marker;

/**
 *
 * @author boris.heithecker
 */
public class ServiceConstants {

    public static final Pattern VALID_PROVIDER2 = Pattern.compile("^(?!\\p{Digit})(?<validtoken>[\\p{Alpha}&&[^_]][\\p{Alnum}-&&[^_]]{0,63}(?<!-))+(\\.\\k<validtoken>)*(/\\k<validtoken>)*", Pattern.UNICODE_CHARACTER_CLASS);

    public static final Pattern VALID_PROVIDER = Pattern.compile("([\\p{Alpha}&&[^_]][\\p{Alnum}-&&[^_]]*[\\p{Alnum}&&[^_]]\\.)*[\\p{Alpha}&&[^_]][\\p{Alnum}-&&[^_]]*[\\p{Alnum}&&[^_]](/[\\p{Alnum}-&&[^_]]+)*", Pattern.UNICODE_CHARACTER_CLASS);
    public static final String APP_RESOURCES = "app-resources";
    public static final Marker BETULA_PRIMARY_UNIT_MARKER = new AbstractMarker("betula-db", "primary-unit", null);
    private static final boolean VALIDATE_PROVIDER_NAME = true;

    public static String providerToDirName(final String provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null.");
        }
        if (VALIDATE_PROVIDER_NAME && !VALID_PROVIDER.matcher(provider).matches()) {
            throw new IllegalArgumentException("Provider name is not valid.");
        }
        return provider.replace('.', '_')
                .replace('/', '$');
    }

    public static Path configBase() {
        final String nbuser = System.getProperty("netbeans.user");
        final String instance = System.getProperty("com.sun.aas.instanceRoot");
        final Path ret;
        if (nbuser != null) {
            ret = Paths.get(nbuser);
        } else if (instance != null) {
            ret = Paths.get(instance).resolve(APP_RESOURCES);
        } else {
            ret = Paths.get(System.getProperty("user.home"));
        }
        return ret;
    }

    public static Path providerConfigBase() {
        return configBase().resolve("provider");
    }

    public static Path providerConfigBase(final String provider) {
        final Path base = providerConfigBase();
        final String enc;
        try {
            enc = URLEncoder.encode(provider, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex); //Should never happen.
        }
        return base.resolve(enc);
    }

    public static Path relativizeToProviderBase(final Path file) {
        final Path normalized = file.normalize();
        if (normalized.startsWith(providerConfigBase())) {
            final Path relative = providerConfigBase().relativize(normalized);
            if (relative.getNameCount() > 0) {
                final Path providerBase = providerConfigBase().resolve(relative.getName(0));
                return providerBase.relativize(normalized);
            }
        }
        return null;
    }

    public static String findProviderName(final Path file) {
        final Path normalized = file.normalize();
        if (normalized.startsWith(providerConfigBase())) {
            final Path relative = providerConfigBase().relativize(normalized);
            if (relative.getNameCount() > 0) {
                final Path providerBase = providerConfigBase().resolve(relative.getName(0));
                final Path providerBaseNameFile = providerBase.resolve("provider");
                if (Files.exists(providerBaseNameFile)) {
                    try {
                        final List<String> l = Files.readAllLines(providerBaseNameFile, StandardCharsets.UTF_8);
                        String name;
                        if (l.size() == 1 && !(name = l.get(0).trim()).isEmpty()) {
                            return name;
                        }
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex); //Should never happen.
                    }
                }
            }
        }
        return null;
    }

}
