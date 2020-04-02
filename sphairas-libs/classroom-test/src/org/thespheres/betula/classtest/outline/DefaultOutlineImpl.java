/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest.outline;

import java.util.ArrayList;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.classtest.Assessable;
import org.thespheres.betula.classtest.Basket;
import org.thespheres.betula.classtest.model.EditableBasket;
import org.thespheres.betula.classtest.model.EditableClassroomTest;
import org.thespheres.betula.classtest.model.EditableProblem;
import org.thespheres.betula.classtest.module2.ClasstestConfiguration;
import org.thespheres.betula.classtest.xml.XmlClassroomTestOutline;
import org.thespheres.betula.classtest.xml.XmlProblem;
import org.thespheres.betula.util.Utilities;

/**
 *
 * @author boris.heithecker
 */
class DefaultOutlineImpl extends XmlClassroomTestOutline {

//    @Override
    public String createId(EditableClassroomTest<?, ?, ?> test, EditableProblem<?> parent) {
        int pos = (parent != null && parent.isBasket()) ? ((EditableBasket) parent).getReferenced().size() : 0;
        String base = parent != null ? (parent.getId() + "-") : "";
        one:
        while (true) {
            String id = base + Utilities.createId(pos);
            for (EditableProblem ep : test.getEditableProblems()) {
                if (ep.getId().equals(id)) {
                    ++pos;
                    continue one;
                }
            }
            return id;
        }
    }

//    @Override
    public String createId(EditableClassroomTest<?, ?, ?> test, int pos) {
        int num = pos;
        one:
        while (true) {
            final String id = Utilities.createId(num);
            for (EditableProblem ep : test.getEditableProblems()) {
                if (ep.getId().equals(id)) {
                    ++num;
                    continue one;
                }
            }
            return id;
        }
    }

    @Override
    public XmlProblem createProblem(EditableClassroomTest<?, ?, ?> test, EditableProblem<?> parent) {
        String id;
        final XmlProblem p;
        if (parent != null) {
            id = createId(test, parent);//TODO: null -> selected context problem
            int pos = parent.getIndex() - test.findChildren(parent.getId()).length + 1;
            p = new XmlProblem(id, pos);
            final String displayName = findProblemDisplayName(test, parent, p);
            p.setDisplayName(displayName);
        } else {
            int pos = test.getEditableProblems().size();
            id = createId(test, pos);
            p = new XmlProblem(id, pos);
            final String displayName = ClasstestConfiguration.findProblemDisplayName(pos + 1, null, null);
            p.setDisplayName(displayName);
        }
        return p;
    }

//    @Override
    public String findProblemDisplayName(EditableClassroomTest<?, ?, ?> test, EditableProblem<?> parent, Assessable.Problem newProblem) {
        int num;
        if (newProblem instanceof Basket.Ref) {
            num = ((Basket.Ref) newProblem).getIndex() + 1;
        } else if (parent != null && parent.isBasket()) {
            num = ((EditableBasket) parent).getReferenced().size() + 1;
        } else {
            num = test.getEditableProblems().size() + 1;
        }
        if (parent == null) {
            return ClasstestConfiguration.findProblemDisplayName(num, null, null);
        } else {
            final ArrayList<Integer> sec = new ArrayList<>(3);
            sec.add(num);
            resolveSection(parent, sec);
            StringJoiner sj = new StringJoiner("");
            for (int c = 0; c < sec.size(); c++) {
                sj.add(resolveSectionDisplayPart(c, sec.get(c), c == sec.size() - 1));
            }
            return sj.toString();
        }
    }

    private void resolveSection(EditableProblem<?> parent, ArrayList<Integer> addTo) {
        while (parent != null) {
            final Assessable.Problem p = parent.getProblem();
            final int index = p instanceof Basket.Ref ? ((Basket.Ref) p).getIndex() : 0;
            addTo.add(0, index + 1);
            parent = parent.getParent();
        }

    }

    private String resolveSectionDisplayPart(final int level, int index, final boolean submostSection) {
        switch (level) {
            case 0:
                return Integer.toString(index) + (submostSection ? ".)" : "");
            case 1:
                return Character.toString((char) ((char) index - 1 + 'a')) + (submostSection ? ")" : "");
            case 2:
                return " " + StringUtils.repeat("i", index) + (submostSection ? ")" : "");
            default:
                return Integer.toString(index);
        }
    }

    @ServiceProvider(service = LookupProvider.class, path = "Loaders/text/betula-classtest-file+xml/Lookup")
    public static class ClasstestTableLookupProvider implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup base) {
            return Lookups.singleton(new DefaultOutlineImpl());
        }
    }
}
