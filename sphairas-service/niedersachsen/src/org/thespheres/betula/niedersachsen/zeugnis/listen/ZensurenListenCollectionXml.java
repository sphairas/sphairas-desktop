/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.zeugnis.listen;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "student-list-collection")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZensurenListenCollectionXml {

    @XmlElement(name = "footer")
    private Footer footer = new Footer();
    @XmlElement(name = "list")
    public final ArrayList<ZensurenListeXml> LISTS = new ArrayList<>();

    public void setFooterCenter(String text) {
        footer.center = text;
    }

    public String getFooterCenter() {
        return footer.center;
    }

    private static class Footer {

        @XmlElement(name = "center")
        private String center;
    }
}
