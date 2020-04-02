/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.util;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.Unit;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.TargetAssessment;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Document;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.services.IllegalAuthorityException;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.NoProviderException;
import org.thespheres.betula.services.WebProvider;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.scheme.spi.TermSchedule;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;

/**
 *
 * @author boris.heithecker
 */
public abstract class TargetAssessmentExport implements Runnable {

    protected final TargetAssessment<Grade, ?> assessment;
    protected ZonedDateTime deleteDate;
    protected Set<Marker> markers;
    protected final NamingResolver namingResolver;
    protected String preferredConvention;
    protected final LocalProperties properties;
    private final String provider;
    protected Term selected;
    protected String signAlias;
    protected Map<String, Signee> signees;
    protected DocumentId target;
    protected String targetType;
    protected final TermSchedule termSchedule;
    protected final Unit unit;

    protected TargetAssessmentExport(String provider, TargetAssessment<Grade, ?> assessment, Unit unit, DocumentId target, TermSchedule ts, NamingResolver nr, LocalProperties lp) {
        this.provider = provider;
        this.assessment = assessment;
        this.target = target;
        this.unit = unit;
        this.properties = lp;
        this.namingResolver = nr;
        this.termSchedule = ts;
        this.selected = termSchedule == null ? null : termSchedule.getCurrentTerm();
    }

    public String getProvider() {
        return provider;
    }

    protected Container createContainer() {
        ContainerBuilder builder = new ContainerBuilder();
        TargetAssessmentEntry<TermId> tae = builder.createTargetAssessmentAction(unit.getUnitId(), target, Paths.UNITS_TARGETS_PATH, null, Action.FILE, false);
        if (markers != null) {
            tae.getValue().getMarkerSet().addAll(markers);
        }
        //        tae.getHints().putAll(td.getProcessorHints());
        if (preferredConvention != null) {
            tae.setPreferredConvention(preferredConvention);
        }
        if (targetType != null) {
            tae.setTargetType(targetType);
        }
        if (deleteDate != null) {
            tae.setDocumentValidity(deleteDate);
        }
        if (signees != null) {
            signees.forEach((String ent, Signee sig) -> tae.getValue().addSigneeInfo(ent, sig));
        }
        TermId current = selected == null ? null : selected.getScheduledItemId();
        assessment.students().forEach((StudentId s) -> {
            final Grade g = assessment.select(s);
            if (g != null) {
                if (current != null) {
                    tae.submit(s, current, g, assessment.timestamp(s));
                } else {
                    tae.submit(s, g, assessment.timestamp(s));
                }
            }
        });
        return builder.getContainer();
    }

    Term getSelectedTerm() {
        return selected;
    }

    Map<String, Signee> getSignees() {
        return signees;
    }

    DocumentId getTarget() {
        return target;
    }

    String getTargetNameResolved() {
        if (namingResolver != null && getSelectedTerm() != null) {
            try {
                return namingResolver.resolveDisplayNameResult(getTarget()).getResolvedName(getSelectedTerm());
            } catch (IllegalAuthorityException ex) {
            }
        }
        return getTarget().getId();
    }

    TermSchedule getTermSchedule() {
        return termSchedule;
    }

    Unit getUnit() {
        return unit;
    }

    protected void initializeProperties() throws IOException {
        if (provider != null && target != null) {
            WebServiceProvider wsp = null;
            try {
                wsp = WebProvider.find(provider, WebServiceProvider.class);
            } catch (NoProviderException ex) {
                throw new IOException(ex);
            }
            final TargetAssessmentEntry<TermId> entry;
            try {
                entry = wsp.getDefaultRequestProcessor().submit(() -> {
                    return Targets.get(provider).fetchTargetAssessment(unit.getUnitId(), target);
                }).get().getTargetAssessmentEntry();
            } catch (InterruptedException | ExecutionException | NoProviderException ex) {
                throw new IOException(ex);
            }
            preferredConvention = entry.getPreferredConvention();
            targetType = entry.getTargetType();
            deleteDate = entry.getDocumentValidity();
            signees = entry.getValue().getSigneeInfos().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (Map.Entry<String, Document.SigneeInfo> e) -> e.getValue().getSignee()));
        } else {
            preferredConvention = properties.getProperty("preferredConvention");
        }
    }

    String getSignAlias() {
        return signAlias;
    }

    void setSignAlias(String signAlias) {
        this.signAlias = signAlias;
    }

    @Override
    public void run() {
        try {
            initializeProperties();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        final Container container = createContainer();
        try {
            write(container);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void setSelectedTerm(Term selected) {
        this.selected = selected;
    }

    void setTarget(DocumentId target) {
        this.target = target;
    }

    protected abstract void write(Container container) throws IOException;

}
