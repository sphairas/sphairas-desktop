/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.xmlimport.utilities.AbstractSourceOverrides.AbstractItemListener;
import org.thespheres.betula.util.ChangeSet;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.xmlimport.ImportItem;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.betula.xmlimport.uiutil.AbstractFileImportAction;
import org.thespheres.betula.xmlimport.uiutil.DefaultImportWizardSettings;
import org.thespheres.betula.xmlimport.utilities.AbstractLink.SourceValueNotEqualToOverridenException;

/**
 *
 * @author boris.heithecker
 * @param <T>
 * @param <L>
 * @param <I>
 * @param <C>
 * @param <K>
 * @param <S>
 */
public abstract class AbstractSourceOverrides<T extends ImportTargetsItem & ImportItem.CloneableImport, L extends AbstractItemListener, I extends AbstractLink, S, K, C extends AbstractLinkCollection<I, S>> implements ChangeSet.Listener<T>, PropertyChangeListener {

    public static final String USER_SOURCE_OVERRIDES = "user-source-overrides";
    protected K links;
    protected final List<L> listeners = new ArrayList<>();
    protected final DefaultImportWizardSettings wizard;
    protected final ChangeSet<T> nodes;
    protected final ChangeSupport cSupport = new ChangeSupport(this);

    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    protected AbstractSourceOverrides(DefaultImportWizardSettings wiz) {
        this.wizard = wiz;
        nodes = (ChangeSet<T>) wizard.getProperty(AbstractFileImportAction.SELECTED_NODES);
        nodes.addChangeListener(this);
        K l = (K) wiz.getProperty(AbstractFileImportAction.SOURCE_TARGET_LINKS);
        if (l == null) {
            wiz.addPropertyChangeListener(this);
        } else {
            doInitialize(l);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (AbstractFileImportAction.SOURCE_TARGET_LINKS.equals(evt.getPropertyName())) {
            final K l = (K) evt.getNewValue();
            doInitialize(l);
        }
    }

    protected void doInitialize(K l) {
        final K before = this.links;
        synchronized (listeners) {
            if (this.links != null) {
                listeners.forEach(AbstractItemListener::uninitialize);
            }
            this.links = l;
            if (this.links != null) {
                listeners.forEach(AbstractItemListener::initialize);
            }
        }
        if (!Objects.equals(this.links, before)) {
            cSupport.fireChange();
        }
    }

    public SourceUserOverridesHighlighter createHighlighter() {
        return new SourceUserOverridesHighlighter(this);
    }

    @Override
    public void setChanged(ChangeSet.SetChangeEvent<T> e) {
        switch (e.getAction()) {
            case ADD:
                addWatch(e.getElement());
                break;
            case REMOVE:
                removeWatch(e.getElement());
                break;
        }
    }

    protected void addWatch(T l) {
        synchronized (listeners) {
            L ret = createListener(l);
            ret.initialize();
            listeners.add(ret);
            S id = getSourceIdentifier(l);
            C k = getLinks(l, ret);
            if (l.id() == 0 && k != null) {
                k.getLinks(id).stream()
                        .filter((I utl) -> utl.getClone() != 0)
                        .forEach((I utl) -> addClone(utl, l));
            }
        }
    }

    protected C getLinks(T t, L listener) {
        synchronized (listeners) {
            return (C) links;
        }
    }

    protected abstract S getSourceIdentifier(T t);

    protected void addClone(I link, T lesson0) {
        final Map<T, Set<T>> clones = (Map<T, Set<T>>) wizard.getProperty(AbstractFileImportAction.CLONED_NODES);
        T clone = createClone(lesson0, link);
        clones.computeIfAbsent(lesson0, (T ls) -> new HashSet()).add(clone);
        nodes.add(clone);
    }

    protected abstract T createClone(T lesson0, I link);

    protected abstract L createListener(T l);

    protected void removeWatch(T l) {
        synchronized (listeners) {
            Iterator<L> it;
            for (it = listeners.iterator(); it.hasNext();) {
                final AbstractItemListener next = it.next();
                if (next.targetItem.equals(l)) {
                    next.uninitialize();
                    it.remove();
                    break;
                }
            }
        }
    }

    public Optional<L> findListener(T il) {
        synchronized (listeners) {
            return listeners.stream()
                    .filter(ll -> ll.targetItem.equals(il))
                    .collect(CollectionUtil.singleton());
        }
    }

    boolean hasSubjectOverride(T il) {
        return findListener(il)
                .map(ll -> ll.targetLink)
                .map(AbstractLink::hasSubjectMarker).
                orElse(Boolean.FALSE);
    }

    boolean hasUnitOverride(T il) {
        return findListener(il)
                .map(ll -> ll.targetLink)
                .map(AbstractLink::hasUnit)
                .orElse(Boolean.FALSE);
    }

    boolean hasDocumentBaseOverride(T il) {
        return false;
    }

    boolean hasSigneeOverride(T il) {
        return findListener(il).map(ll -> ll.targetLink)
                .map(AbstractLink::hasSignee)
                .orElse(Boolean.FALSE);
    }

    boolean hasDeleteDateOverride(T il) {
        return false;
    }

    boolean hasTargetIdOverride(T il) {
        return false;
    }

    boolean hasColumnOverride(T il, final String column) {
        return findListener(il)
                .map(ll -> ll.targetLink)
                .map(ll -> ll.hasColumnOverride(column))
                .orElse(Boolean.FALSE);
    }

    @Messages({"SourceUserOverrides.throwSourceValueNotEqualToOverriddenException.message=Der Quellwert der überschriebenen Eigenschaft „{0}“ für den Import von „{1}“ stimmt nicht mehr mit dem überschriebenen früheren Quellwert überein. \nQuellwert: „{2}“, Überschriebener Quellwert: „{3}“",
        "SourceUserOverrides.throwSourceValueNotEqualToOverriddenException.ignoreUnit.message=Ignoriert, Gruppen-Id wird „{0}“ gesetzt."})
    protected static void processSourceTargetLinkException(SourceTargetLinkException ex, ImportTargetsItem il) {
        if (ex instanceof AbstractLink.SourceValueNotEqualToOverridenException) {
            final AbstractLink.SourceValueNotEqualToOverridenException svne = (AbstractLink.SourceValueNotEqualToOverridenException) ex;
            String prop = svne.getProperty();
            String source = svne.getSourceValue();
            String found = svne.getFoundValue();
            String msg = NbBundle.getMessage(AbstractSourceOverrides.class, "SourceUserOverrides.throwSourceValueNotEqualToOverriddenException.message", prop, il.getSourceNodeLabel(), source, found);
            ImportUtil.getIO().getErr().println(msg);
        } else {
            ImportUtil.getIO().getErr().println(ex);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        cSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cSupport.removeChangeListener(listener);
    }

    public abstract class AbstractItemListener implements VetoableChangeListener {

        protected final T targetItem;
        protected I targetLink;
        protected boolean removeWhenUnitialize = false;

        protected AbstractItemListener(T lesson) {
            this.targetItem = lesson;
        }

        protected abstract void initialize();

        public I getTargetLink() {
            return targetLink;
        }

        protected void initUserOverrides() {
            if (doUpdate()) {
//            if (targetLink.getUnit() != null) {
//                lesson.setUnitId(targetLink.getUnit());
//            }
//                Marker fachOverride = targetLink.getSubjectMarker(lesson.getFachMarker());
//                if (fachOverride != null) {
//                    lesson.setFachMarker(fachOverride);
//                }
                if (targetLink.hasSubjectMarker()) {
                    try {
                        targetItem.setSubjectMarker(targetLink.getSubjectMarker(targetItem.getSubjectMarker()));
                    } catch (SourceTargetLinkException ex) {
                        processSourceTargetLinkException(ex, targetItem);
                    }
                }
//                Signee signeeOverride = targetLink.getSignee(lesson.getSignee());
                if (targetLink.hasSignee()) {
                    try {
                        targetItem.setSignee(targetLink.getSignee(targetItem.getSignee()));
                    } catch (SourceTargetLinkException ex) {
                        processSourceTargetLinkException(ex, targetItem);
                    }
                }
                if (targetLink.hasUnit()) {
                    try {
                        targetItem.setUnitId(targetLink.getUnit(targetItem.getUnitId()));
                    } catch (SourceTargetLinkException ex) {
                        processSourceTargetLinkException(ex, targetItem);
                        if (ex instanceof SourceValueNotEqualToOverridenException && ((SourceValueNotEqualToOverridenException) ex).getOverride() instanceof UnitId) {
                            UnitId u = (UnitId) ((SourceValueNotEqualToOverridenException) ex).getOverride();
                            String msg = NbBundle.getMessage(AbstractSourceOverrides.class, "SourceUserOverrides.throwSourceValueNotEqualToOverriddenException.ignoreUnit.message", u.getId());
                            ImportUtil.getIO().getOut().println(msg);
                            targetItem.setUnitId(u);
                        }
                    }
                }
            }
        }

        protected abstract boolean doUpdate();

        protected abstract void uninitialize();

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            switch (evt.getPropertyName()) {
                case ImportTargetsItem.PROP_UNIQUE_SUBJECT:
                    updateFach((Marker) evt.getOldValue(), (Marker) evt.getNewValue());
                    break;
                case ImportTargetsItem.PROP_SIGNEE:
                    updateSignee((Signee) evt.getOldValue(), (Signee) evt.getNewValue());
                    break;
                case ImportTargetsItem.PROP_UNITID:
                    updateUnit((UnitId) evt.getOldValue(), (UnitId) evt.getNewValue());
                    break;
            }
        }

        private void updateFach(Marker old, Marker newMarker) {
            if (doUpdate()) {
                removeWhenUnitialize = removeWhenUnitialize
                        & !targetLink.setSubjectMarker(old, newMarker);
            }
        }

        private void updateSignee(Signee old, Signee newSignee) {
            if (doUpdate()) {
                removeWhenUnitialize = removeWhenUnitialize
                        & !targetLink.setSignee(old, newSignee);
            }
        }

        private void updateUnit(UnitId old, UnitId newunit) {
            if (doUpdate()) {
                removeWhenUnitialize = removeWhenUnitialize
                        & !targetLink.setUnit(old, newunit);
            }
        }
    }
}
