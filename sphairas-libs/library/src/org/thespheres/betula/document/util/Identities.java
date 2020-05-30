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

    public static final Pattern IDPATTERN = Pattern.compile("[\\w+[\\w\\.]*]+(-[\\w+[\\w\\.]*]+)*", Pattern.UNICODE_CHARACTER_CLASS);
    public static final Pattern VERSIONPATTERN = Pattern.compile("[\\w+[\\w\\.]*]+", Pattern.UNICODE_CHARACTER_CLASS);
    public static final Pattern AUTHORITYPATTERN = Pattern.compile("([\\w+[\\w\\.]*]+(-[\\w+[\\w\\.]*]+)*)(\\/[\\w+[\\w\\.]*]+(-[\\w+[\\w\\.]*]+)*)", Pattern.UNICODE_CHARACTER_CLASS);
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
        return VERSIONPATTERN.matcher(value).matches();
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

    public static <I extends Identity<?>> I parse(final String value, final ParsedIdentityConverter<I> create) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (!value.equals(StringUtils.strip(value))) {
            return null;
        }
        final String[] parts = value.split(Identity.AUTHORITY_DELIMITER);
        if (parts.length > 2) {
            return null;
        }
        final String idPart = parts[0];
        final String[] idParts = idPart.split(Identity.VERSION_DELIMITER);
        if (idParts.length > 2) {
            return null;
        }
        final String id = idParts[0];
        if (!isValidStringId(id)) {
            return null;
        }
        final String version;
        if (idParts.length == 2) {
            version = idParts[1];
            if (!version.equals(StringUtils.strip(version))) {
                return null;
            }
            if (!isValidVersion(version)) {
                return null;
            }
        } else {
            version = null;
        }
        final String authority;
        if (parts.length == 2) {
            authority = parts[1];
            if (!isValidAuthority(authority)) {
                return null;
            }
        } else {
            authority = null;
        }
        return create.apply(authority, id, version);
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
