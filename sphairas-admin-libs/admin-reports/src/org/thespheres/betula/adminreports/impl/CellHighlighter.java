/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.admin.units.PrimaryUnitOpenSupport;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.ui.swingx.CellIconHighlighterDelegate;
import org.thespheres.betula.ui.swingx.HighlighterInstanceFactory;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 */
class CellHighlighter extends CellIconHighlighterDelegate implements HighlightPredicate {

    private final Lookup context;
    private Set<DataObject> data;

    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    CellHighlighter(final Lookup context) {
        super("org/thespheres/betula/admin/units/resources/reports-stack.png");
        this.context = context;
        setHighlightPredicate(this);
    }

    private Set<DataObject> getData() {
        if (data == null) {
            final PrimaryUnitOpenSupport s = context.lookup(PrimaryUnitOpenSupport.class);
            if (s != null) {
                data = Arrays.stream(s.getProjectDirectory().getChildren())
                        .filter(fo -> fo.getMIMEType().equals(RemoteReportsDescriptorFileDataObject.FILE_MIME))
                        .map(fo -> {
                            try {
                                return DataObject.find(fo);
                            } catch (DataObjectNotFoundException ex) {
                                PlatformUtil.getCodeNameBaseLogger(CellHighlighter.class).log(Level.WARNING, ex.getLocalizedMessage(), ex);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
            } else {
                data = Collections.EMPTY_SET;
            }
        }
        return data;
    }

    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        if (adapter.getValue() instanceof RemoteStudent) {
            final StudentId student = ((RemoteStudent) adapter.getValue()).getStudentId();
            return getData().stream()
                    .map(d -> d.getLookup().lookup(RemoteReportsModel.class))
                    .filter(Objects::nonNull)
                    .flatMap(m -> m.getTargets().stream())
                    .anyMatch(t -> t.students().contains(student));
        }
        return false;
    }

    @MimeRegistration(mimeType = "application/betula-unit-context", service = HighlighterInstanceFactory.class)
    public static class HLFactory implements HighlighterInstanceFactory {

        @Override
        public Highlighter createHighlighter(JXTable table, TopComponent tc) {
            return new CellHighlighter(tc.getLookup());
        }

    }
}
