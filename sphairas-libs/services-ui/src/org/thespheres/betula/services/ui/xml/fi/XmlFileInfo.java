/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.ui.xml.fi;

import org.thespheres.betula.ui.FileInfo;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.w3c.dom.Element;

/**
 *
 * @author boris.heithecker
 */
@XmlSeeAlso(XmlFileInfoElement.class)
@XmlRootElement(name = "file-info")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlFileInfo implements FileInfo {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name = "file-display-name", required = true)
    private String fileDisplayName;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name = "file-description")
    private String fileDescription;
    @XmlElementRef
    protected List<XmlFileInfoElement> properties;
    @XmlAnyElement
    private Element[] others;

    public XmlFileInfo() {
    }

    public XmlFileInfo(final String fileDisplayName) {
        this.fileDisplayName = fileDisplayName;
    }

    @Override
    public String getFileDisplayName() {
        return fileDisplayName;
    }

    @Override
    public void setFileDisplayName(final String fileDisplayName) {
        this.fileDisplayName = fileDisplayName;
    }

    @Override
    public String getFileDescription() {
        return fileDescription;
    }

    @Override
    public void setFileDescription(final String fileDescription) {
        this.fileDescription = fileDescription;
    }

}
