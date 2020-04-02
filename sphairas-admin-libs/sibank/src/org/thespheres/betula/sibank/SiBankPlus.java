/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.xmlimport.model.Product;

/**
 *
 * @author boris.heithecker
 */
@Messages({"SiBankPlus.product.displayName=SiBank Plus"})
public class SiBankPlus {

    private final static Product PRODUCT = new Product("sibank-plus");

    static {
        PRODUCT.setDisplay(NbBundle.getMessage(SiBankPlus.class, "SiBankPlus.product.displayName"));
    }

    private SiBankPlus() {
    }

    public static final Product getProduct() {
        return PRODUCT;
    }
}
