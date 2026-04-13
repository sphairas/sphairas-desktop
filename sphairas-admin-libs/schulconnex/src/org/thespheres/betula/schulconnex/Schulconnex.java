/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.schulconnex;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.xmlimport.model.Product;

/**
 *
 * @author boris.heithecker
 */
@Messages({"Schulconnex.product.displayName=Schulconnex"})
public class Schulconnex {

    private final static Product PRODUCT = new Product("schulconnex");

    static {
        PRODUCT.setDisplay(NbBundle.getMessage(Schulconnex.class, "Schulconnex.product.displayName"));
    }

    private Schulconnex() {
    }

    public static final Product getProduct() {
        return PRODUCT;
    }
}
