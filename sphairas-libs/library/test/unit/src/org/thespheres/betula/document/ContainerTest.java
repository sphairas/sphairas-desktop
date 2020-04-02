/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.thespheres.betula.document.util.UnitEntry;

/**
 *
 * @author boris.heithecker
 */
public class ContainerTest {
    
    public ContainerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetVersion() throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(Container.class);
        UnitEntry ue = new UnitEntry(new DocumentId("text", "do", DocumentId.Version.LATEST), null, Action.REQUEST_COMPLETION, true);
        Container c = new Container();
        c.getEntries().add(ue);
         ctx.createMarshaller().marshal(c, System.out);
    }

    
}
