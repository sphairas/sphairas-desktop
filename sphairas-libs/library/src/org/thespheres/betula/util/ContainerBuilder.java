/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.util.Arrays;
import org.thespheres.betula.Identity;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Description;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Template;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.document.util.TextAssessmentEntry;
import org.thespheres.betula.document.util.UnitEntry;

/**
 *
 * @author boris.heithecker
 */
public class ContainerBuilder {

    private final Container container = new Container();

    public Container getContainer() {
        return container;
    }

    public UnitEntry updateUnitAction(final DocumentId unitDocument, final UnitId unit, final StudentId[] students, final String[] pathIdentifiers, final Description[] unitFileDescriptions, final Action entryAction, final boolean fileUnit, final boolean fragment) {
        final Action unitAction = fileUnit ? Action.FILE : (students == null ? Action.REQUEST_COMPLETION : null);
        final UnitEntry ret = new UnitEntry(unitDocument, unit, unitAction, fragment);
        final Template root = createUnit(unit, unitFileDescriptions);
        root.getChildren().add(ret);
        addPath(pathIdentifiers, root);
        if (students != null) {
            for (final StudentId s : students) {
                final Entry<StudentId, ?> e = new Entry(entryAction, s);
                ret.getChildren().add(e);
            }
        }
        container.getEntries().add(root);
        return ret;
    }

    public <I extends Identity> TargetAssessmentEntry<I> createTargetAssessmentAction(final UnitId unit, final DocumentId targetDocument, final String[] pathIdentifiers, final Description[] unitFileDescriptions, final Action targetAction, boolean fragment) {
        final TargetAssessmentEntry<I> ret = new TargetAssessmentEntry<>(targetDocument, targetAction, fragment);
        Template root;
        if (unit != null) {
            root = createUnit(unit, unitFileDescriptions);
            root.getChildren().add(ret);
        } else {
            root = ret;
        }
        addPath(pathIdentifiers, root);
        container.getEntries().add(root);
        return ret;
    }

    public TextAssessmentEntry createTextAssessmentAction(final UnitId unit, final DocumentId targetDocument, final String[] pathIdentifiers, final Description[] unitFileDescriptions, final Action action, final boolean fragment) {
        final TextAssessmentEntry ret = new TextAssessmentEntry(targetDocument, action, fragment);
        Template root;
        if (unit != null) {
            root = createUnit(unit, unitFileDescriptions);
            root.getChildren().add(ret);
        } else {
            root = ret;
        }
        addPath(pathIdentifiers, root);
        container.getEntries().add(root);
        return ret;
    }

    public void add(Template<?> node, String[] pathIdentifiers) {
        addPath(pathIdentifiers, node);
        container.getEntries().add(node);
    }

    public Template<?> createTemplate(final String[] pathIdentifiers, final Description[] desc, final Action action) {
        return this.createTemplate(null, null, null, pathIdentifiers, desc, action);
    }

    public Template<?> createTemplate(final Template parent, final Identity id, final Object value, final String[] pathIdentifiers, final Description[] desc, final Action action) {
        final Template file;
        if (id == null) {
            file = new Template(action);
            //add note: no docId provided
        } else {
            file = new Entry(action, id);
        }
        if (value != null) {
            file.setValue(value);
        }
        if (desc != null && desc.length != 0) {
            Arrays.stream(desc)
                    .forEach(file.getDescription()::add);
        }
        if (parent != null) {
            parent.getChildren().add(file);
        } else {
            addPath(pathIdentifiers, file);
            container.getEntries().add(file);
        }
        return file;
    }

    private Entry createUnit(final UnitId unit, final Description[] description) {
        final Entry ret = new Entry(null, unit == null ? UnitId.NULL : unit);
        if (description != null && description.length != 0) {
            Arrays.stream(description)
                    .forEach(ret.getDescription()::add);
        }
        return ret;
    }

    private void addPath(String[] pathIdentifiers, Template<?> unitFile) {
        if (pathIdentifiers != null && pathIdentifiers.length > 0) {
            int l = pathIdentifiers.length;
            Container.PathDescriptorElement pathEl = new Container.PathDescriptorElement(unitFile, pathIdentifiers[--l]);
            while (l != 0) {
                pathEl = new Container.PathDescriptorElement(pathEl, pathIdentifiers[--l]);
            }
            container.getPathElements().add(pathEl);
        }
    }

}
