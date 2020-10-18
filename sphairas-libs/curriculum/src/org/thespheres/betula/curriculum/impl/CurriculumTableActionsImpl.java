/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculum.impl;

import com.google.common.eventbus.Subscribe;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.thespheres.betula.curriculum.CourseEntry;
import org.thespheres.betula.curriculum.Section;
import org.thespheres.betula.curriculum.util.CurriculumCourseSelectionChangeEvent;
import org.thespheres.betula.curriculum.util.CurriculumTableActions;
import org.thespheres.betula.curriculum.xml.XmlCurriculum;
import org.thespheres.betula.util.CollectionUtil;

/**
 *
 * @author boris.heithecker
 */
public class CurriculumTableActionsImpl extends CurriculumTableActions<XmlCurriculum> implements DropTargetListener {

    protected final Map<String, WeakReference<CourseNode>> courses = new HashMap<>();
    protected final Map<SectionKey, WeakReference<SectionNode>> sections = new HashMap<>();

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    CurriculumTableActionsImpl(XmlCurriculum node, Lookup lookup) {
        super(node, lookup);
        getEventBus().register(this);
    }

    void setXmlCurriculum(final XmlCurriculum node) {
        setCurriculum(node);
    }

    @Subscribe
    public void onModelChange(CurriculumCourseSelectionChangeEvent event) {
        setFileModified(true);
    }

    private void setFileModified(boolean mod) {
        getDataObject().setModified(mod);
    }

    public Node getNode(final Section section) {
        final Section found = getCurriculum().findSection(section.getBase(), section.getSequence());
        if (found == null) {
            throw new IllegalArgumentException();
        }
        final SectionKey k = new SectionKey(section);
        synchronized (sections) {
            final WeakReference<SectionNode> ref = sections.get(k);
            SectionNode c;
            if (ref == null || (c = ref.get()) == null) {
                c = createSectionNode(found);
                sections.put(k, new WeakReference<>(c));
            }
            return c;
        }
    }

    protected SectionNode createSectionNode(final Section course) {
        return new SectionNode(course);
    }

    @Override
    public Node getNode(final String id) {
        final CourseEntry found = getCurriculum().allCourses().stream()
                .filter(c -> c.getId().equals(id))
                .collect(CollectionUtil.requireSingleOrNull());
        if (found == null) {
            throw new IllegalArgumentException();
        }
        synchronized (courses) {
            final WeakReference<CourseNode> ref = courses.get(id);
            CourseNode c;
            if (ref == null || (c = ref.get()) == null) {
                c = createCourseNode(found);
                courses.put(id, new WeakReference<>(c));
            }
            return c;
        }
    }

    protected CourseNode createCourseNode(final CourseEntry course) {
        return new CourseNode(course);
    }

    @Override
    public void addCourse(CourseEntry course) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeCourse(String id) {
        final boolean mod = getCurriculum().removeEntry(id);
        if (mod) {
            setFileModified(true);
        }
    }

    @Override
    public void addSection(Section section) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSection(int base, int sequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
    }

    private class SectionKey {

        private final int base;
        private final int sequence;

        SectionKey(Section s) {
            this.base = s.getBase();
            this.sequence = s.getSequence();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + this.base;
            return 29 * hash + this.sequence;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SectionKey other = (SectionKey) obj;
            if (this.base != other.base) {
                return false;
            }
            return this.sequence == other.sequence;
        }

    }
}
