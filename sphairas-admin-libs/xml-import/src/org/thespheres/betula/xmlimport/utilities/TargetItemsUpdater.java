/*
 * To change this license header, choose License Headers in Project AppProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.xml.ws.WebServiceException;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.Container;
import org.thespheres.betula.document.Description;
import org.thespheres.betula.document.Entry;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Timestamp;
import org.thespheres.betula.document.model.DocumentsModel;
import org.thespheres.betula.document.util.BaseTargetAssessmentEntry;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.document.util.TextAssessmentEntry;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.services.LocalProperties;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.MessageUtil;
import org.thespheres.betula.services.ws.BetulaWebService;
import org.thespheres.betula.services.ws.Paths;
import org.thespheres.betula.services.ws.ServiceException;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.util.ContainerBuilder;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.ImportTargetsItem.GradeEntry;
import org.thespheres.betula.xmlimport.ImportUtil;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
@Messages({"Updater.message.start=Starte Import von {0} Elementen nach {1}.",
    "Updater.message.network=Senden über das Netzwerk ...",
    "Updater.message.finish=Es wurden {0} Kurs(e) in {1} ms nach {2} importiert.",
    "Updater.message.abort=Der Import wird abgebrochen.",
    "Updater.message.dryrun=Es wurden keine Kurse und Listen importiert, weil die Option Trockenlauf gewählt wurde."})
public class TargetItemsUpdater<I extends ImportTargetsItem> extends AbstractUpdater<I> {

    protected final Term term;
    protected final WebServiceProvider provider;
    protected final List<UpdaterFilter> filters;
    protected final ThreadLocal<Long> timeStart = new ThreadLocal();
    protected final ThreadLocal<Integer> numImport = new ThreadLocal();
    protected Exception exception;
    private final TargetItemsUpdaterDescriptions addDescriptions;

    public TargetItemsUpdater(final I[] impKurse, final WebServiceProvider provider, final Term current, final List<UpdaterFilter> filters) {
        this(impKurse, provider, current, filters, null);
    }

    public TargetItemsUpdater(final I[] impKurse, final WebServiceProvider provider, final Term current, final List<UpdaterFilter> filters, final TargetItemsUpdaterDescriptions descriptions) {
        super(impKurse);
        this.provider = provider;
        this.term = current; //.getScheduledItemId();
        this.filters = filters != null ? filters : Collections.EMPTY_LIST;
        this.addDescriptions = descriptions;
    }

    public Term getTerm() {
        return term;
    }

    public WebServiceProvider getProvider() {
        return provider;
    }

    public List<UpdaterFilter> getFilters() {
        return filters;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public void run() {
        final String msg = NbBundle.getMessage(TargetItemsUpdater.class, "Updater.message.start", items.length, provider.getInfo().getDisplayName());
        ImportUtil.getIO().getOut().println(msg);

        //kurszuordnungen, termtargets
        numImport.set(0);
        timeStart.set(System.currentTimeMillis());

        final ContainerBuilder builder;
        try {
            builder = createContainer();
        } catch (ImportConfigurationException e) {
            LOGGER.log(Level.CONFIG, e.getMessage(), e);
            ImportUtil.getIO().getErr().println(e.getMessage());
            final String msg2 = NbBundle.getMessage(TargetItemsUpdater.class, "Updater.message.abort");
            ImportUtil.getIO().getOut().println(msg2);
            return;
        }

        dumpContainer(builder.getContainer(), provider);

        if (!isDryRun()) {
            exception = callService(builder);
        } else {
            final String msg2 = NbBundle.getMessage(TargetItemsUpdater.class, "Updater.message.dryrun");
            ImportUtil.getIO().getOut().println(msg2);
        }
    }

    protected ContainerBuilder createContainer() {
        final DocumentsModel docModel = createDocumentsModel();
        final ContainerBuilder builder = new ContainerBuilder();
        Arrays.stream(items)
                .filter(this::checkImportTargetsItem)
                .forEach(importTargetsItem -> {
                    final UnitId unitId = importTargetsItem.getUnitId();
                    for (TargetDocumentProperties td : importTargetsItem.getImportTargets()) {
                        if (!checkTargetDocument(importTargetsItem, td)) {
                            continue;
                        }
                        final String tdMessage = createTargetDocumentMessage(importTargetsItem, td);
                        if (tdMessage != null) {
                            ImportUtil.getIO().getOut().println(tdMessage);
                        }
                        if (td.isTextValueTarget()) {
                            final TextAssessmentEntry txae = builder.createTextAssessmentAction(unitId, td.getDocument(), Paths.TEXT_UNITS_TARGETS_PATH, null, Action.FILE, td.isFragment());
                            writeTargetEntry(importTargetsItem, txae, td);
                        } else {
                            final TargetAssessmentEntry<TermId> tae = builder.createTargetAssessmentAction(unitId, td.getDocument(), Paths.UNITS_TARGETS_PATH, null, Action.FILE, td.isFragment());
                            writeTargetEntry(importTargetsItem, tae, td);
                        }
                    }
                    //Add students to unit
                    //TODO: better solution for "!sibankkurs.isKlassenfach), problem: primary units are not updated properly
                    if (!UnitId.isNull(unitId) && importTargetsItem.fileUnitParticipants()) {
                        final StudentId[] us = Arrays.stream(importTargetsItem.getUnitStudents())
                                .filter(s -> checkStudent(importTargetsItem, unitId, s))
                                .toArray(StudentId[]::new);
                        final UnitEntry uEntry = builder.updateUnitAction(docModel.convertToUnitDocumentId(unitId), unitId, us, Paths.UNITS_PARTICIPANTS_PATH, null, Action.FILE, importTargetsItem.fileUnitParticipants(), importTargetsItem.isFragment());
                        writeUnitEntry(importTargetsItem, uEntry);
                    }

                    //targets to primaryunit
                    numImport.set(numImport.get() + 1);
                });
        return builder;
    }

    protected DocumentsModel createDocumentsModel() {
        final DocumentsModel ret = new DocumentsModel();
        try {
            ret.initialize(LocalProperties.find(provider.getInfo().getURL()).getProperties());
            return ret;
        } catch (Exception e) {
            throw new ImportConfigurationException(e);
        }
    }

    protected void writeTargetEntry(final I importTargetsItem, final BaseTargetAssessmentEntry<TermId, StudentId> tae, final TargetDocumentProperties td) {
        final Marker[] markers = td.markers();
        if (markers != null && markers.length != 0) {
            tae.getValue().getMarkerSet().addAll(Arrays.asList(markers));
            if (addDescriptions != null) {
                final Description d = addDescriptions.createTargetMarkerDescription(tae, markers);
                tae.getDescription().add(d);
            }
        }
        tae.getHints().putAll(td.getProcessorHints());
        final String preferredConvention = td.getPreferredConvention();
        if (preferredConvention != null && tae instanceof TargetAssessmentEntry) {
            ((TargetAssessmentEntry) tae).setPreferredConvention(preferredConvention);
        }
        final String targetType = td.getTargetType();
        if (targetType != null) {
            tae.setTargetType(targetType);
        }
        final String subjectAlternativeName = importTargetsItem.getSubjectAlternativeName();
        if (subjectAlternativeName != null) {
            tae.setSubjectAlternativeName(subjectAlternativeName);
            if (addDescriptions != null) {
                final Description d = addDescriptions.createTargetSubjectAlternativeNameDescription(tae, subjectAlternativeName);
                tae.getDescription().add(d);
            }
        }
        final LocalDate deleteDate = td.getDeleteDate();
        if (deleteDate != null) {
            tae.setDocumentValidity(deleteDate.atStartOfDay(ZoneId.systemDefault()));
            if (addDescriptions != null) {
                final Description d = addDescriptions.createTargetDocumentValidityDescription(tae, deleteDate);
                tae.getDescription().add(d);
            }
        }
        td.getSignees().forEach((t, sig) -> {
            tae.getValue().addSigneeInfo(t, sig);
            if (addDescriptions != null) {
                final Description d = addDescriptions.createTargetSigneeInfoDescription(tae, t, sig);
                tae.getDescription().add(d);
            }
        });

        final StudentId[] unitStuds = importTargetsItem.getUnitStudents();
        Grade defaultGrade = null;
        try {
            defaultGrade = td.getDefaultGrade();
        } catch (UnsupportedOperationException e) {
        }
        String defaultText = null;
        try {
            defaultText = td.getDefaultText();
        } catch (UnsupportedOperationException e) {
        }
        if (unitStuds != null && term != null) {
            final TermId tid = term.getScheduledItemId();
            final Timestamp gradeTime = new Timestamp(term.getBegin());
            for (final StudentId stud : unitStuds) {
                if (!checkStudent(importTargetsItem, td, stud)) {
                    continue;
                }
                if (defaultGrade != null && tae instanceof TargetAssessmentEntry) {
                    final Entry<StudentId, Grade> se = ((TargetAssessmentEntry) tae).submit(stud, tid, defaultGrade, gradeTime, Action.FILE);
                    if (addDescriptions != null) {
                        final Description d = addDescriptions.createTargetStudentEntryDescription(stud, term, defaultGrade, gradeTime);
                        se.getDescription().add(d);
                    }
                } else if (defaultText != null && tae instanceof TextAssessmentEntry) {
                    final Entry<StudentId, String> se = ((TextAssessmentEntry) tae).submit(stud, tid, null, defaultText, gradeTime, Action.FILE);
                    if (addDescriptions != null) {
                        final Description d = addDescriptions.createTargetStudentTextEntryDescription(stud, term, defaultText, gradeTime);
                        se.getDescription().add(d);
                    }
                }
            }
            final long termBegin = term.getBegin().getTime();
            tae.getHints().put("keep.old.target.entries.after", Long.toString(termBegin));
        }
        if (tae instanceof TargetAssessmentEntry) {
            importTargetsItem.identities().forEach(t -> {
                importTargetsItem.students().forEach(s -> {
                    importTargetsItem.entry(s, t)
                            .filter(GradeEntry::isValid)
                            .ifPresent(ge -> {
                                if (checkGradeEntry(importTargetsItem, td, s, t, ge)) {
                                    final Entry<StudentId, Grade> se = ((TargetAssessmentEntry) tae).submit(s, ge.getTerm(), ge.getGrade(), ge.getTimestamp(), Action.FILE);
                                    if (addDescriptions != null) {
                                        final Description d = addDescriptions.createTargetStudentEntryDescription(s, ge);
                                        se.getDescription().add(d);
                                    }
                                }
                            });
                });
            });
        }
    }

    protected void writeUnitEntry(final I importTargetsItem, final UnitEntry uEntry) {
        final LocalDate deleteDate = importTargetsItem.getDeleteDate();
        if (deleteDate != null) {
            uEntry.setDocumentValidity(deleteDate.atStartOfDay(ZoneId.systemDefault()));
        }
        final String preferredTermSchedule = importTargetsItem.getPreferredTermSchedule();
        if (preferredTermSchedule != null) {
            uEntry.setPreferredTermSchedule(preferredTermSchedule);
        }
        final Marker[] markers = importTargetsItem.allMarkers();
        if (markers != null) {
            uEntry.getValue().getMarkerSet().addAll(Arrays.asList(markers));
        }
        if (importTargetsItem.importUnitDisplayName()) {
            uEntry.setCommonUnitName(importTargetsItem.getUnitDisplayName());
        }
    }

    protected Exception callService(final ContainerBuilder builder) {
        final BetulaWebService service;
        try {
            service = provider.createServicePort();
        } catch (IOException ex) {
            if (err != null) {
                ex.printStackTrace(ImportUtil.getIO().getErr());
            }
            return ex;
        }
        Container ret = null;
        final Container container = builder.getContainer();
        try {
            final String msg2 = NbBundle.getMessage(TargetItemsUpdater.class, "Updater.message.network");
            ImportUtil.getIO().getOut().println(msg2);
            ret = MessageUtil.suppressMessageDelivery(() -> service.solicit(container));
            long dur = System.currentTimeMillis() - timeStart.get();
            final String msg3 = NbBundle.getMessage(TargetItemsUpdater.class, "Updater.message.finish", numImport.get(), dur, provider.getInfo().getDisplayName());
            ImportUtil.getIO().getOut().println(msg3);
        } catch (Exception ex) {
            if (ex instanceof ServiceException || ex instanceof WebServiceException) {
                if (getErrorWriter() != null) {
                    getErrorWriter().println(ex.getLocalizedMessage());
                    ex.printStackTrace(getErrorWriter());
                }
                return ex;
            }
        }
        dumpReturnContainer(ret, provider);
        AbstractUpdater.handleExceptions(ret);
        final TargetItemsUpdaterEvent evt = new TargetItemsUpdaterEvent(this, container, ret);
        EVENTS.post(evt);
        return null;
    }

    @Messages({"TargetItemsUpdater.checkImportTargetsItem.notValid=Der Import {0} ist nicht gültig. Löschdatum: {1}; Gruppe: {2}; Basis-Liste: {3}; Fach: {4}; "})
    protected boolean checkImportTargetsItem(final I iti) {
        final boolean ret = !filters.stream()
                .anyMatch(uf -> !uf.accept(iti));
        if (!ret) {
            final String message = NbBundle.getMessage(TargetItemsUpdater.class, "TargetItemsUpdate.checkImportTargetsItem.notValid", new Object[]{iti.getSourceNodeLabel(),
                iti.getDeleteDate(),
                iti.getUnitId(),
                iti.getTargetDocumentIdBase(),
                iti.getSubjectMarker()});
            ImportUtil.getIO().getErr().println(message);
        }
        return ret;
    }

    protected boolean checkTargetDocument(final I iti, final TargetDocumentProperties td) {
        return !filters.stream()
                .anyMatch(uf -> !uf.accept(iti, td));
    }

    protected boolean checkStudent(final I iti, final TargetDocumentProperties td, final StudentId student) {
        return !filters.stream()
                .anyMatch(uf -> !uf.accept(iti, td, student));
    }

    protected boolean checkStudent(final I iti, final UnitId unit, final StudentId student) {
        return !filters.stream()
                .anyMatch(uf -> !uf.accept(iti, unit, student));
    }

    protected boolean checkGradeEntry(final I iti, TargetDocumentProperties td, final StudentId student, final TermId term, final GradeEntry entry) {
        return filters.stream()
                .allMatch(uf -> uf.accept(iti, td, student, term, entry));
    }

    @Messages({"Updater.targetDocumentMessage={0} --> {1} {2}"})
    protected String createTargetDocumentMessage(I item, TargetDocumentProperties td) {
        final String node = item.getSourceNodeLabel();
        final String document = td.getDocument().getId();
        final String hints = td.getProcessorHints().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(",", " [", "]"));
        return NbBundle.getMessage(TargetItemsUpdater.class, "Updater.targetDocumentMessage", node, document, hints);
    }

}
