/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.swingx;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author boris.heithecker
 * @param <T>
 */
public abstract class AbstractCurrentNodeHighlighter<T> extends ColorHighlighter implements HighlightPredicate {

    protected final TopComponent component;
    protected final Lookup.Result<T> result;
    protected final RequestProcessor RP = new RequestProcessor(getClass());
    protected List<T> current = Collections.EMPTY_LIST;
    protected final boolean surviveFocusChange;
    private final Class<T> itemType;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    protected AbstractCurrentNodeHighlighter(JXTable table, TopComponent tc, boolean surviveFocusChange, Class<T> itemType) {
        this.component = tc;
        this.itemType = itemType;
        this.surviveFocusChange = surviveFocusChange;
        this.result = Utilities.actionsGlobalContext().lookupResult(itemType);
        this.setHighlightPredicate(this);
        this.result.addLookupListener(ev -> RP.post(this::updateState));
        RP.post(this::updateState);
    }

    protected void updateState() {
        List<T> cm = result.allInstances().stream()
                .map(itemType::cast)
                .collect(Collectors.toList());
        synchronized (this) {
            if (!cm.isEmpty() || !surviveFocusChange) {
                current = cm;
            }
        }
        Mutex.EVENT.writeAccess(this::fireStateChanged);
    }

}
