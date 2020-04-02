/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.FontHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.StringValue;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.Convention;
import org.thespheres.betula.Tag;
import org.thespheres.betula.ui.util.LocalizedIllegalArgumentException;
import org.thespheres.betula.ui.util.LogLevel;
import org.thespheres.betula.ui.util.PlatformUtil;

/**
 *
 * @author boris.heithecker
 * @param <C>
 * @param <T>
 */
public abstract class AbstractConventionComboBox<C extends Convention, T extends Tag> extends DefaultComboBoxModel implements StringValue, ItemListener {

    protected String preferredConvention;
    protected boolean longLabel = true;
    protected final boolean addOthersBehaviour;
    protected boolean highlightPrefCon;
    protected final boolean addNull;
    protected String nullLabel = " ";

    protected AbstractConventionComboBox(String[] conventions, boolean addOthersBehaviour, boolean addNull) {
        this.addOthersBehaviour = addOthersBehaviour;
        this.addNull = addNull;
        init(conventions);
    }

    public void setUseLongLabel(boolean longLabel) {
        this.longLabel = longLabel;
    }

    public void setPreferredConvention(String convention) {
        setConventions(!StringUtils.isBlank(convention) ? new String[]{convention} : new String[0]);
    }

    public void setConventions(final String[] conventions) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(() -> setConventions(conventions));
            return;
        }
        init(conventions);
    }

    public String getNullValueLabel() {
        return nullLabel;
    }

    public void setNullValueLabel(String nullLabel) {
        this.nullLabel = nullLabel;
    }

    public void initialize(JXComboBox box) {
        box.setHighlighters(createHighlighters(box));
        box.addItemListener(this);
    }

    protected final void init(final String[] conventions) {
        final List<C> l = allConventions().stream()
                //                .filter(cv -> !(cv instanceof AbstractAssessmentConvention && ((AbstractAssessmentConvention) cv).isLegacy()))
                .sorted(Comparator.comparing(Convention::getDisplayName, Collator.getInstance(Locale.getDefault())))
                .collect(Collectors.toList());
        Convention[] arr = allConventions().stream().toArray(Convention[]::new);
        C precon = null;
        int conventionsSize = 0;
        if (conventions != null) {
            conventions:
            for (int i = 0; i < conventions.length; i++) {
                for (Convention ac : arr) {
                    if (ac.getName().equals(conventions[i])) {
                        if (i == 0) {
                            precon = (C) ac;
                        }
                        l.remove((C) ac);
                        l.add(conventionsSize++, (C) ac);
                        continue conventions;
                    }
                }
            }
        }
        removeAllElements();
        if (addOthersBehaviour) {
            conventionsSize = l.size();
        }
        if (addNull) {
            addElement(null);
        }
        
        for (C ac : l.subList(0, conventionsSize)) {
            if (precon == null || !ac.getName().equals(precon.getName())) {
                addElement(ac);
            }
            allTags(ac).stream().forEach(this::addElement);
        }
        highlightPrefCon = (conventions != null && conventions.length != 1);
        preferredConvention = precon != null ? precon.getName() : null;
        fireContentsChanged(this, 0, getSize() - 1);
    }

    protected Highlighter[] createHighlighters(JXComboBox box) {
        Highlighter[] ret = new Highlighter[]{new ColorHighlighter(new ConHL(), null, Color.RED), new FontHighlighter(new ConHL(), box.getFont().deriveFont(Font.ITALIC)), new FontHighlighter(new PrefConHL(), box.getFont().deriveFont(Font.BOLD))};
        return ret;
    }

    protected abstract List<T> allTags(C convention);

    protected abstract List<C> allConventions();

    @Messages({"AbstractConventionComboBox.illegalItemValue.message=ComboBox item must be of type Tag, Convention, or null.",
        "AbstractConventionComboBox.illegalItemValue.localizedMessage=Fehler: Das Element muss vom Typ Tag, Convention oder null sein."})
    @Override
    public String getString(Object o) {
        if (o instanceof Convention) {
            return ((Convention) o).getDisplayName();
        } else if (o instanceof Tag) {
            String s = longLabel ? ((Tag) o).getLongLabel() : ((Tag) o).getShortLabel();
            return " " + s;
        } else if (o == null) {
            return nullLabel;
        }
        final LocalizedIllegalArgumentException illex = new LocalizedIllegalArgumentException(NbBundle.getMessage(AbstractConventionComboBox.class, "AbstractConventionComboBox.illegalItemValue.message"));
        illex.setLocalizedMessage(NbBundle.getMessage(AbstractConventionComboBox.class, "AbstractConventionComboBox.illegalItemValue.localizedMessage"));
        //TODO:
        final Object[] sources = new Object[]{this, o};
//        UIExceptions.handle(illex, sources);
        PlatformUtil.getCodeNameBaseLogger(AbstractConventionComboBox.class).log(LogLevel.INFO_WARNING, illex.getMessage(), illex);
        return illex.getLocalizedMessage();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object sel = getSelectedItem();
        if (sel instanceof Convention && addOthersBehaviour) {
            init(new String[]{((Convention) sel).getName()});
        }
    }

    public T getSelectedValue() {
        Object sel = getSelectedItem();
        try {
            return (T) sel;
        } catch (ClassCastException cce) {
            return null;
        }
    }

    private class PrefConHL implements HighlightPredicate {

        @Override
        public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
            if (highlightPrefCon) {
                int r = ca.convertColumnIndexToModel(ca.row);
                Object el = AbstractConventionComboBox.this.getElementAt(r);
                if (el instanceof Tag && preferredConvention != null) {
                    return ((Tag) el).getConvention().equals(preferredConvention);
                }
            }
            return false;
        }
    }

    private class ConHL implements HighlightPredicate {

        @Override
        public boolean isHighlighted(Component cmpnt, ComponentAdapter ca) {
            int r = ca.convertColumnIndexToModel(ca.row);
            Object el = AbstractConventionComboBox.this.getElementAt(r);
            return el instanceof Convention;
        }
    }
}
