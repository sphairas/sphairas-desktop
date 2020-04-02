/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.thespheres.betula.Identity;

/**
 *
 * @author boris.heithecker
 * @param <Source>
 * @param <Target>
 */
public abstract class GroupingByIdentity<Source extends Identity<String>, Target> implements IdentityOp<Source, Target> {

    protected Pattern pattern;
    protected String authority;
    private final Map<Source, Target> cache = new HashMap<>();

    protected void checkInitialized() {
        if (pattern == null) {
            throw new IllegalStateException(getClass().getName() + " not initialized.");
        }
    }

    protected String getAuthority() {
        return authority;
    }

    private Target convertImpl(Source s) {
        checkInitialized();
        final Matcher m = pattern.matcher(s.getId());
        final String nid;
        if (m.find()) {
            final StringBuffer sb = new StringBuffer();
            m.appendReplacement(sb, "");
            nid = sb.toString();
        } else {
            nid = s.getId();
        }
        if (nid != null) {
            try {
                return createIdentity(s, nid);
            } catch (IllegalArgumentException illex) {
            }
        }
        return null;
    }

    @Override
    public Target convert(Source s) {
        if (!canConvert(s)) {
            return null;
        }
        synchronized (cache) {
            return cache.computeIfAbsent(s, this::convertImpl);
        }
    }

    @Override
    public String match(Source s) {
        if (!canConvert(s)) {
            return null;
        }
        final Matcher m = pattern.matcher(s.getId());
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    protected boolean canConvert(Source s) {
        checkInitialized();
        return s != null
                && (getAuthority() == null
                || s.getAuthority().equals(getAuthority()));
    }

    protected abstract Target createIdentity(Source s, String toString) throws IllegalArgumentException;
}
