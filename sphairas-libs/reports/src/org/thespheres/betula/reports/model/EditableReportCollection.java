/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.model;

import com.google.common.eventbus.EventBus;
import java.util.List;
import javax.swing.text.StyledDocument;
import org.openide.util.Lookup;
import org.thespheres.betula.util.CollectionChangeEvent;

/**
 *
 * @author boris.heithecker
 * @param <R>
 */
public abstract class EditableReportCollection<R extends EditableReport> {

    public static final String COLLECTION_REPORTS = "reports";
    protected final Lookup context;
    protected final StyledDocument document;
    private final EventBus events = new EventBus();
    
    protected EditableReportCollection(Lookup context, StyledDocument document) {
        this.context = context;
        this.document = document;
    }

    public Lookup getContext() {
        return context;
    }

    public StyledDocument getDocument() {
        return document;
    }

    public abstract List<R> getReports();
    
    protected void fireReportsChanged(R item, CollectionChangeEvent.Type type) {
       final CollectionChangeEvent cce = new CollectionChangeEvent(this, COLLECTION_REPORTS, item, type); 
       events.post(cce);
    }
}
