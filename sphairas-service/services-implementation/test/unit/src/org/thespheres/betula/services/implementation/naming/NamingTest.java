/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services.implementation.naming;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.thespheres.betula.services.implementation.naming.Naming.PATTERN;

/**
 *
 * @author boris.heithecker
 */
public class NamingTest {

    public NamingTest() {
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
    public void testPattern() {
        System.out.println("pattern");
        final String name = "deutsch.lesen.a-2017-a";
        if (!PATTERN.matcher(name).matches()) {
            fail("The test case is a prototype.");
        }
    }

}
