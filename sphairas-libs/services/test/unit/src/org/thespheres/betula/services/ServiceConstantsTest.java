/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.services;

import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author boris.heithecker
 */
public class ServiceConstantsTest {

    public static final Pattern VALID_PROVIDER2 = Pattern.compile("[\\p{Alpha}&&[^_]][\\p{Alnum}-&&[^_]]*[\\p{Alnum}&&[^_]]", Pattern.UNICODE_CHARACTER_CLASS);

    public static final Pattern VALID_PROVIDER3 = Pattern.compile("([\\p{Alpha}&&[^_]][\\p{Alnum}-&&[^_]]*[\\p{Alnum}&&[^_]]\\.)*[\\p{Alpha}&&[^_]][\\p{Alnum}-&&[^_]]*[\\p{Alnum}&&[^_]]", Pattern.UNICODE_CHARACTER_CLASS);

    public ServiceConstantsTest() {
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
    public void testEncodeProviderUrl() {
        boolean r0 = ServiceConstants.VALID_PROVIDER.matcher("kgs-neuburg").matches();
        boolean r1 = ServiceConstants.VALID_PROVIDER.matcher("www.kgs-neuburg.de").matches();
        boolean r2 = !ServiceConstants.VALID_PROVIDER.matcher("0ww.kgs-neuburg.de").matches();
        boolean r3 = !ServiceConstants.VALID_PROVIDER.matcher(".kgs-neuburg.de").matches();
        boolean r4 = !ServiceConstants.VALID_PROVIDER.matcher("www.kgs-neuburg.").matches();
        boolean r5 = !ServiceConstants.VALID_PROVIDER.matcher("www.kgs-neuburg.0").matches();
        boolean r6 = ServiceConstants.VALID_PROVIDER.matcher("www.kgs-neuburg.de0").matches();
        boolean r7 = !ServiceConstants.VALID_PROVIDER.matcher("www.kgs_neu-burg").matches();
        boolean r8 = ServiceConstants.VALID_PROVIDER.matcher("www.kgs-новград.de").matches();
        boolean r9 = ServiceConstants.VALID_PROVIDER.matcher("kgs-neuburg/tstest").matches();
        boolean r10 = !ServiceConstants.VALID_PROVIDER.matcher("kgs-neuburg/").matches();
        boolean r11 = ServiceConstants.VALID_PROVIDER.matcher("kgs-neuburg/tstest/2").matches();
        boolean r12 = !ServiceConstants.VALID_PROVIDER.matcher("kgs-neuburg/tstest/2/").matches();
        if (false) {
            fail();
        }
    }

    @Test
    public void testEncodeProviderUrl2() {
        boolean r0 = ServiceConstants.VALID_PROVIDER2.matcher("kgs-neuburg").matches();
        boolean r1 = ServiceConstants.VALID_PROVIDER2.matcher("www.kgs-neuburg.de").matches();
        boolean r2 = !ServiceConstants.VALID_PROVIDER2.matcher("0ww.kgs-neuburg.de").matches();
        boolean r3 = !ServiceConstants.VALID_PROVIDER2.matcher(".kgs-neuburg.de").matches();
        boolean r4 = !ServiceConstants.VALID_PROVIDER2.matcher("www.kgs-neuburg.").matches();
        boolean r5 = !ServiceConstants.VALID_PROVIDER2.matcher("www.kgs-neuburg.0").matches();
        boolean r6 = ServiceConstants.VALID_PROVIDER2.matcher("www.kgs-neuburg.de0").matches();
        boolean r7 = !ServiceConstants.VALID_PROVIDER2.matcher("www.kgs_tarms-tedt").matches();
        boolean r8 = ServiceConstants.VALID_PROVIDER2.matcher("www.kgs-тармштедт.de").matches();
        boolean r9 = ServiceConstants.VALID_PROVIDER2.matcher("kgs-neuburg/tstest").matches();
        boolean r10 = !ServiceConstants.VALID_PROVIDER2.matcher("kgs-neuburg/").matches();
        boolean r11 = ServiceConstants.VALID_PROVIDER2.matcher("kgs-neuburg/tstest/2").matches();
        boolean r12 = !ServiceConstants.VALID_PROVIDER2.matcher("kgs-neuburg/tstest/2/").matches();
        if (false) {
            fail();
        }
    }
}
