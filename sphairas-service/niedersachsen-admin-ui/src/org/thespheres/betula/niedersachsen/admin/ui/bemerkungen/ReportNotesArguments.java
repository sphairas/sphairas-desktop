/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "report-notes-arguments") //, namespace = "http://www.thespheres.org/xsd/betula/journal.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportNotesArguments {

    @XmlElementWrapper(name = "arguments")
    @XmlElement(name = "argument")
    private final List<Argument> args = new ArrayList<>();

    public Argument[] getArguments() {
        return args.stream()
                .toArray(Argument[]::new);
    }

    public Argument addArgument(String name, int index) {
        final Argument add = new Argument(name);
        args.add(index, add);
        return add;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Argument {

        @XmlElement
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String description;
        @XmlAttribute
        private String name;
        @XmlElement(name = "display-name")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String displayName;
        @XmlElement(name = "insert-format-parameter")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        private String insertFormatParameter;
        @XmlTransient
        private ReportNotesArguments parent;

        Argument() {
        }

        Argument(String name) {
            this.name = name;
        }

        void afterUnmarshal(Unmarshaller unmarshaller, Object parent) throws JAXBException {
            if (parent instanceof ReportNotesArguments) {
                this.parent = (ReportNotesArguments) parent;
            } else {
                throw new JAXBException("No ReportNotesArguments parent.");
            }
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return parent.args.indexOf(this);
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getInsertFormatParameter() {
            return insertFormatParameter;
        }

        public void setInsertFormatParameter(String insertFormatParameter) {
            this.insertFormatParameter = insertFormatParameter;
        }

        public void remove() {
            parent.args.remove(this);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + Objects.hashCode(this.name);
            return hash;
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
            final Argument other = (Argument) obj;
            return Objects.equals(this.name, other.name);
        }

    }
}
