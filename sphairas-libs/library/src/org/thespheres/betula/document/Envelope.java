/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.Serializable;
import org.thespheres.betula.util.XmlZonedDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.document.util.ContentValueEntry;
import org.thespheres.betula.document.util.JournalEntry;
import org.thespheres.betula.document.util.TargetAssessmentEntry;
import org.thespheres.betula.document.util.TextAssessmentEntry;
import org.thespheres.betula.document.util.TicketEntry;
import org.thespheres.betula.document.util.UnitEntry;
import org.thespheres.betula.document.util.UserTargetAssessmentEntry;
import org.thespheres.betula.document.util.XmlDocumentEntry;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "envelopeType",
        propOrder = {"id", "action", "children", "hints", "description", "time", "exception"})
public class Envelope implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String PROP_ID = "PROP_ID"; //im container
    public static final String PROP_METHOD = "PROP_METHOD";
    public static final String PROP_CHILDREN = "PROP_CHILDREN";
    public static final String PROP_DESCRIPTION = "PROP_DESCRIPTION";
    @XmlElementWrapper(name = "descriptions")
    @XmlElement(name = "description")
    protected List<Description> description;
    @XmlAttribute(name = "ID")
    @XmlID
    protected String id;
    @XmlElement(name = "action")
    protected Action action;
    //TODO: wrapper element: nodes
    @XmlElementWrapper(name = "nodes")
    @XmlElements(value = {
        @XmlElement(name = "document-entry", type = DocumentEntry.class),
        @XmlElement(name = "unit-entry", type = UnitEntry.class),
        @XmlElement(name = "target-assessment-entry", type = TargetAssessmentEntry.class),
        @XmlElement(name = "user-target-assessment-entry", type = UserTargetAssessmentEntry.class),
        @XmlElement(name = "text-assessment-entry", type = TextAssessmentEntry.class),
        @XmlElement(name = "journal-entry", type = JournalEntry.class),
        @XmlElement(name = "content-value-entry", type = ContentValueEntry.class),
        @XmlElement(name = "ticket-entry", type = TicketEntry.class),
        @XmlElement(name = "betula-xml-document", type = XmlDocumentEntry.class),
        @XmlElement(name = "entry", type = Entry.class),
        @XmlElement(name = "template", type = Template.class)})
    protected List<Template> children;
//        @XmlElementWrapper(name = "hints")
    @XmlElement(name = "processor-hints")
    @XmlJavaTypeAdapter(value = ProcessorHintsAdapter.class)
    protected Map<String, String> hints = new HashMap<>();
    @XmlElement(name = "exception")
    private ExceptionMessage exception;
    @XmlElement(name = "time")
    @XmlJavaTypeAdapter(XmlZonedDateTime.ZonedDateTimeAdapter.class)
    private ZonedDateTime time;

    public Envelope() {
    }

    public Envelope(Action action) {
        this.action = action;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action method) {
        this.action = method;
    }

    public List<Template> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public Map<String, String> getHints() {
        return hints;
    }

    public List<Description> getDescription() {
        if (description == null) {
            description = new ArrayList<>();
        }
        return description;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public ExceptionMessage getException() {
        return exception;
    }

    public void setException(ExceptionMessage annot) {
        this.exception = annot;
    }

    public void beforeMarshal(Marshaller marshaller) throws JAXBException {
        if (description != null && description.isEmpty()) {
            description = null;
        }
    }

    private static final class ProcessorHintsAdapter extends XmlAdapter<ProcessorHintList, Map<String, String>> {

        @Override
        public Map<String, String> unmarshal(ProcessorHintList l) throws Exception {
            return l.list.stream().collect(Collectors.toMap(ProcessorHint::getKey, ProcessorHint::getValue));
        }

        @Override
        public ProcessorHintList marshal(Map<String, String> m) throws Exception {
            return (m == null || m.isEmpty()) ? null : new ProcessorHintList(m.entrySet().stream().map(e -> new ProcessorHint(e.getKey(), e.getValue())).collect(Collectors.toCollection(ArrayList::new)));
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static final class ProcessorHintList {

        @XmlElement(name = "processor-hint")
        private ArrayList<ProcessorHint> list = new ArrayList<>();

        public ProcessorHintList() {
        }

        private ProcessorHintList(ArrayList<ProcessorHint> l) {
            list = l;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    private static final class ProcessorHint {

        @XmlAttribute(name = "key")
        private String key;
        @XmlAttribute(name = "value")
        private String value;

        public ProcessorHint() {
        }

        private ProcessorHint(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private String getKey() {
            return key;
        }

        private String getValue() {
            return value;
        }

    }
}
