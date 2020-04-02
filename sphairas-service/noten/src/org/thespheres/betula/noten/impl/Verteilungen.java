/*
 * Verteilungen.java
 *
 * Created on 18. Mai 2007, 13:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.thespheres.betula.noten.impl;

import org.thespheres.betula.util.Int2;
import org.thespheres.betula.assess.Distribution;


/**
 *
 * @author Boris Heithecker
 */
public class Verteilungen {
    
    /** Creates a new instance of Verteilungen */
    private Verteilungen() {}
    
    public static final Distribution RAHMENRICHTLINIEN;
    public static final Distribution GLEICHMAESSIG;
    public static final Distribution OBERSTUFE;
    
    static {
        RAHMENRICHTLINIEN = new DistributionImpl("Rahmenrichtlinien", new Int2[]{
           new Int2(0d),
           new Int2(20d),
           new Int2(50d),
           new Int2(65d),
           new Int2(80d),
           new Int2(95d)
        });
        GLEICHMAESSIG = new DistributionImpl("Gleichmäßig", new Int2[]{
           new Int2(0d),
           new Int2(20d),
           new Int2(50d),
           new Int2(62.5d),
           new Int2(75d),
           new Int2(87.5d)
        });
        OBERSTUFE = new DistributionImpl("Rahmenrichtlinien OS", new Int2[]{
           new Int2(0d),
           new Int2(20d),
           new Int2(27d),
           new Int2(33d),
           new Int2(40d),
           new Int2(45d),
           new Int2(50d),
           new Int2(55d),
           new Int2(60d),
           new Int2(65d),
           new Int2(70d),
           new Int2(75d),
           new Int2(80d),
           new Int2(85d),
           new Int2(90d),
           new Int2(95d)
        });
    }

    
}
