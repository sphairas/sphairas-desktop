/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.Identity;

/**
 *
 * @author boris
 */
//TODO replace IdFormatter and ServiceConstants
public final class Identities {

    public static final Pattern IDPATTERN = Pattern.compile("^\\w+([\\-\\.]\\w+)*$", Pattern.UNICODE_CHARACTER_CLASS);
    //public static final Pattern VERSIONPATTERN = Pattern.compile("[\\w+[\\w\\.]*]+", Pattern.UNICODE_CHARACTER_CLASS);
    public static final Pattern AUTHORITYPATTERN = Pattern.compile("^\\w+(\\.\\w+)*(\\/\\w+(\\.\\w+)*)*$", Pattern.UNICODE_CHARACTER_CLASS);
    public static final int ID_MAX_LENGTH = 256;
    public static final int VERSION_MAX_LENGTH = 32;
    public static final int AUTHORITY_MAX_LENGTH = 64;

    private Identities() {
    }

    public static boolean isValidStringId(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        if (!value.equals(StringUtils.strip(value))) {
            return false;
        }
        if (value.length() > ID_MAX_LENGTH) {
            return false;
        }
        return IDPATTERN.matcher(value).matches();
    }

    public static boolean isValidVersion(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        if (!value.equals(StringUtils.strip(value))) {
            return false;
        }
        if (value.length() > VERSION_MAX_LENGTH) {
            return false;
        }
        return IDPATTERN.matcher(value).matches();
    }

    public static boolean isValidAuthority(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        if (!value.equals(StringUtils.strip(value))) {
            return false;
        }
        if (value.length() > AUTHORITY_MAX_LENGTH) {
            return false;
        }
        return AUTHORITYPATTERN.matcher(value).matches();
    }

    public static <I extends Identity<?>> I resolve(final String value, final ParsedIdentityConverter<I> create, final String defaultAuthority, final String defaultVersion) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (!value.equals(StringUtils.strip(value))) {
            throw new IllegalArgumentException("Untrimmed input \"" + value + "\"");
        }
        final String[] parts = value.split(Identity.AUTHORITY_DELIMITER);
        final String idPart;
        final String authority;
        switch (parts.length) {
            case 1:
                idPart = parts[0];
                authority = defaultAuthority;
                break;
            case 2:
                idPart = parts[0];
                authority = parts[1];
                if (!isValidAuthority(authority)) {
                    throw new IllegalArgumentException("String " + authority + " is not a valid authority.");
                }
                break;
            default:
                throw new IllegalArgumentException("Identity can have only one " + Identity.AUTHORITY_DELIMITER + " (authority delimiter) character.");
        }
        final String[] idParts = idPart.split(Identity.VERSION_DELIMITER);
        final String id;
        switch (idParts.length) {
            case 1:
            case 2:
                id = idParts[0];
                break;
            default:
                throw new IllegalArgumentException("Identity id can have only one " + Identity.VERSION_DELIMITER + " (version delimiter) character.");
        }
        if (!isValidStringId(id)) {
            throw new IllegalArgumentException("String " + id + " is not a valid id.");
        }
        final String version;
        if (idParts.length == 2) {
            version = idParts[1];
            if (!version.equals(StringUtils.strip(version))) {
                throw new IllegalArgumentException("Untrimmed input \"" + version + "\"");
            }
            if (!isValidVersion(version)) {
                throw new IllegalArgumentException("String " + version + " is not a valid version.");
            }
        } else {
            version = defaultVersion;
        }
        if (StringUtils.isBlank(authority)) {
            throw new IllegalArgumentException("Authoritiy cannot be null or empty.");
        }
        try {
            return create.apply(authority, id, version);
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @FunctionalInterface
    public static interface ParsedIdentityConverter<I extends Identity> {

        I apply(String authority, String id, String version);

        default <N extends Identity> ParsedIdentityConverter<N> andThen(Function<? super I, ? extends N> after) {
            Objects.requireNonNull(after);
            return (a, i, v) -> after.apply(apply(a, i, v));
        }
    }

}
