/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis.xml;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.junit.Test;

/**
 *
 * @author boris.heithecker
 */
public class DocumentTest {

    @Test
    public void testGetVersion() throws Exception {
        JAXBContext jaxb = JAXBContext.newInstance(Document.class);
        Document doc = new Document();
        doc.general = new General();
        Marshaller m = jaxb.createMarshaller();
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        m.marshal(doc, System.out);
    }

}
