/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "report", namespace = "http://www.thespheres.org/xsd/betula/reports.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class Report extends AbstractReportEntry {

    @XmlID
    @XmlAttribute(name = "id")
    private String name;
    @XmlTransient
    private String nameLocal;
    @XmlElement(name = "content", required = true)
    private final ReportText text = new ReportText();

    public Report() {
    }

    protected Report(DocumentId document) {
        super(document);
    }

    public String getId() {
        return name != null ? name : nameLocal;
    }

    public String getText() {
        return this.text.getText();
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReportText {

        @XmlValue()
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = StringUtils.trimToNull(text);
        }

        public boolean isEmpty() {
            return text == null;
        }
    }

    @XmlRootElement(name = "report-collection", namespace = "http://www.thespheres.org/xsd/betula/reports.xsd")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReportCollection extends AbstractReportEntry {

        @XmlAttribute(name = "id-sequence-tip", required = true)
        private int idTip = 1;
        @XmlElement(name = "report", required = true)
        private final List<Report> reports = new ArrayList<>();
        @XmlTransient
        private int idTipLocal = 1;

        public ReportCollection() {
        }

        public ReportCollection(DocumentId document) {
            super(document);
        }

        public List<Report> getReports() {
            return Collections.unmodifiableList(reports);
        }

        public Report addReport(String name) {
            return addReport(name, reports.size());
        }

        public Report addReport(String name, int index) {
            Report ret = createReport();
            setId(ret, false);
            reports.add(index, ret);
            return ret;
        }

        protected Report createReport() {
            return new Report(getDocument());
        }

        public void afterUnmarshal(Unmarshaller u, Object parent) throws UnmarshalException {
            final Set<String> ids = new HashSet<>();
            for (Report r : reports) {
                if (r.getId() == null) {
                    setId(r, true);
                }
                String n = r.getId();
                if (!ids.add(n)) {
                    throw new UnmarshalException("Name cannot occur more than one time.");
                }
            }
        }

        private void setId(final Report report, final boolean local) {
            synchronized (reports) {
                String base = local ? "reportLocal" : "report";
                String id = null;
                while (id == null || reports.stream()
                        .map(Report::getId)
                        .anyMatch(id::equals)) {
                    id = base + Integer.toHexString(local ? idTipLocal++ : idTip++);
                }
                if (local) {
                    report.nameLocal = id;
                } else {
                    report.name = id;
                }
            }
        }
    }
}
