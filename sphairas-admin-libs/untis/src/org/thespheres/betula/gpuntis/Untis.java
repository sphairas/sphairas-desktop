/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.gpuntis;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.xmlimport.model.Product;

/**
 *
 * @author boris.heithecker
 */
@Messages({"Units.product.displayName=Untis"})
public class Untis {

    private final static Product prod = new Product("untis");

    static {
        prod.setDisplay(NbBundle.getMessage(Untis.class, "Units.product.displayName"));
    }

    private Untis() {
    }

    public static final Product getProduct() {
        return prod;
    }
}
