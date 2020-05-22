/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "container") //, namespace = "http://www.thespheres.org/xsd/betula/container.xsd")
@XmlType(propOrder = {"paths", "files", "messages", "version"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Container implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlElementWrapper(name = "paths")
    @XmlElement(name = "path")
    private ArrayList<PathDescriptorElement> paths = new ArrayList<>();
    @XmlElements({
        @XmlElement(name = "entry", type = Entry.class),
        @XmlElement(name = "template", type = Template.class)
    })
    private ArrayList<Template> files = new ArrayList<>();
    @XmlElementWrapper(name = "messages")
    @XmlElement(name = "message")
    private ArrayList<Message> messages = new ArrayList<>();
    @XmlAttribute(name = "version")
    private String version = "1.0";

    public Container() {
    }

    public Container(Template node) {
        files.add(node);
    }

    public String getVersion() {
        return version;
    }

    public ArrayList<PathDescriptorElement> getPathElements() {
        return paths;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void beforeMarshal(Marshaller marshaller) throws JAXBException {
        int refid = 0;
        for (Template t : files) {
            t.setId(Integer.toString(++refid));
        }
        for (PathDescriptorElement pde : paths) {
            checkPE(pde);
        }
    }

    private void checkPE(PathDescriptorElement pde) throws JAXBException {
        if (pde.getChild() != null) {
            checkPE(pde.getChild());
        } else {
            Envelope t = pde.getEnvelope();
            if (t == null || t.getId() == null) {
                throw new JAXBException("No id set.");
            }
        }
    }

    public List<Template> getEntries() {
        return files;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Message implements Serializable {

        private static final long serialVersionUID = 1L;
        @XmlAttribute(required = true)
        private String source;
        @XmlValue
        private String message;

        public Message() {
        }

        public Message(String source, String message) {
            this.source = source;
            this.message = message;
        }

        @Override
        public String toString() {
            return "[" + source + "] " + message;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PathDescriptorElement implements Serializable {

        private static final long serialVersionUID = 1L;
        @XmlAttribute(required = true)
        protected String identifier;
        @XmlAttribute
        protected String version;
        @XmlIDREF
        @XmlAttribute(name = "node-ref")
        private Envelope node;
        @XmlElement(name = "path")
        private PathDescriptorElement child;
        @XmlElement(name = "description")
        private List<Description> description = new ArrayList<>();

        public PathDescriptorElement() {
        }

        public PathDescriptorElement(Object node, String name) {
            this(node, name, null);
        }

        public PathDescriptorElement(Object node, String name, String version) {
            this.identifier = name;
            this.version = version;
            setNode(node);
        }

        public String getIdentifier() {
            return identifier;
        }

        public PathDescriptorElement getChild() {
            return child;
        }

        public Envelope getEnvelope() {
            return node;
        }

        public String getVersion() {
            return version;
        }

        public List<Description> getDescription() {
            return description;
        }

        private void setNode(Object node) {
            if (!(node instanceof PathDescriptorElement || node instanceof Envelope)) {
                throw new IllegalArgumentException();
            }
            if (node instanceof PathDescriptorElement) {
                this.child = (PathDescriptorElement) node;
                this.node = null;
            } else {
                this.node = (Envelope) node;
                this.child = null;
            }
        }
    }
}
