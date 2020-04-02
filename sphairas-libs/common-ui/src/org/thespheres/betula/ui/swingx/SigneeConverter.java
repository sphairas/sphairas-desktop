/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import java.util.Optional;
import java.util.function.BiFunction;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import org.jdesktop.swingx.renderer.StringValue;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.services.util.Signees;

/**
 *
 * @author boris.heithecker
 */
public class SigneeConverter extends ObjectToStringConverter implements StringValue {

    private Optional<Signees> signees;
    private final String provider;
    private final String nullValue;
    private final BiFunction<Signee, String, String> after;

    public SigneeConverter(String provider, String nullValue) {
        this(provider, nullValue, null);
    }

    public SigneeConverter(String provider, String nullValue, BiFunction<Signee, String, String> andThen) {
        this.provider = provider;
        this.nullValue = nullValue;
        after = andThen;
    }

    public SigneeConverter(String provider) {
        this(provider, null);
    }

    @Override
    public String getPreferredStringForItem(Object item) {
        return impl(item);
    }

    @Override
    public String getString(Object item) {
        return impl(item);
    }

    private String impl(Object v) {
        if (v instanceof Signee) {
            if (v.equals(Signee.NULL)) {
                return nullReturn(v);
            }
            final Signee rs = (Signee) v;
            return getSignees()
                    .map(s -> s.getSignee(rs))
                    .map(sn -> after().apply(rs, sn))
                    .orElse(rs.getId());
        }
        return nullReturn(v);
    }

    protected BiFunction<Signee, String, String> after() {
        return after != null ? after : (sig, sn) -> sn;
    }

    protected String nullReturn(Object v) {
        return nullValue;
    }

    protected Optional<Signees> getSignees() {
        if (signees == null) {
            signees = Signees.get(provider);
        }
        return signees;
    }

}
