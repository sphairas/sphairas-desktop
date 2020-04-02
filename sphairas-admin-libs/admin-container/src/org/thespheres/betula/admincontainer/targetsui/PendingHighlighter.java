/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admincontainer.targetsui;

import org.thespheres.betula.admincontainer.util.TargetsUtil;
import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.util.Optional;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.thespheres.betula.admin.units.AbstractUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteGradeEntry;
import org.thespheres.betula.admin.units.TargetsSelectionElementEnv2;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 */
abstract class PendingHighlighter extends ColorHighlighter implements HighlightPredicate {

    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    PendingHighlighter(final Lookup context) {
        super(Color.YELLOW, null, Color.ORANGE, null);
        setHighlightPredicate(this);
    }

    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        if (adapter.getValue() instanceof Optional) {
            try {
                final Optional<RemoteGradeEntry> value = (Optional<RemoteGradeEntry>) adapter.getValue();
                final Grade test = getPending();
                return value
                        .map(ga -> ga.getGrade().equals(test))
                        .orElse(false);
            } catch (ClassCastException | IOException e) {
            }
        }
        return false;
    }

    protected abstract Grade getPending() throws IOException;

    @MimeRegistration(mimeType = "application/betula-unit-context", service = HighlighterInstanceFactory.class)
    public static class HLFactory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new PendingHighlighter(tc.getLookup()) {
                private final Lookup.Result<AbstractUnitOpenSupport> result = tc.getLookup().lookupResult(AbstractUnitOpenSupport.class);
                private Object pending;

                @Override
                protected Grade getPending() throws IOException {
                    if (pending == null) {
                        try {
                            final AbstractUnitOpenSupport s = result.allInstances().stream()
                                    .collect(CollectionUtil.requireSingleOrNull());
                            final ConfigurableImportTarget t = TargetsUtil.findCommonImportTarget(s);
                            pending = t.getDefaultValue(null, null);
                        } catch (IllegalStateException e) {
                            pending = new IOException(e);
                        } catch (IOException e) {
                            pending = e;
                        }
                    }
                    if (pending instanceof IOException) {
                        throw (IOException) pending;
                    }
                    return (Grade) pending;
                }
            };
        }

    }

    @MimeRegistration(mimeType = "application/betula-targets-context", service = HighlighterInstanceFactory.class)
    public static class SelHLFactory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new PendingHighlighter(tc.getLookup()) {
                private final Lookup.Result<TargetsSelectionElementEnv2> result = tc.getLookup().lookupResult(TargetsSelectionElementEnv2.class);
                private Object pending;

                @Override
                protected Grade getPending() throws IOException {
                    if (pending == null) {
                        try {
                            final TargetsSelectionElementEnv2 env = result.allInstances().stream()
                                    .collect(CollectionUtil.requireSingleOrNull());
                            final ConfigurableImportTarget t = TargetsUtil.findCommonImportTarget(env.getProvider());
                            pending = t.getDefaultValue(null, null);
                        } catch (IllegalStateException e) {
                            pending = new IOException(e);
                        } catch (IOException e) {
                            pending = e;
                        }
                    }
                    if (pending instanceof IOException) {
                        throw (IOException) pending;
                    }
                    return (Grade) pending;
                }
            };
        }

    }
}
