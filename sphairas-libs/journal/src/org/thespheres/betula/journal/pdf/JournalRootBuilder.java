/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.pdf;

import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.PageNumber;
import org.plutext.jaxb.xslfo.SimplePageMaster;
import org.plutext.jaxb.xslfo.TextAlignType;
import org.thespheres.betula.listprint.builder.RootBuilder;
import org.thespheres.betula.listprint.builder.Util;

/**
 *
 * @author boris.heithecker
 */
class JournalRootBuilder extends RootBuilder {

    JournalRootBuilder(String title, Object f) {
        super(title, f);
    }

    @Override
    protected SimplePageMaster createSimplePageMaster() {
        SimplePageMaster spm = super.createSimplePageMaster();
        spm.setPageHeight("29.7cm");
        spm.setPageWidth("21.0cm");
        spm.setMarginTop("1.7cm");
        spm.setMarginRight("2.0cm");
        return spm;
    }

    @Override
    protected Block createFooterBlock() {
        Block ret = Util.createBlock("0.0cm", "0.0cm", "0.0cm", "7pt", "#000000", TextAlignType.CENTER);
        ret.getContent().add("- ");
        ret.getContent().add(new PageNumber());
        ret.getContent().add(" -");
        return ret;
    }
}
