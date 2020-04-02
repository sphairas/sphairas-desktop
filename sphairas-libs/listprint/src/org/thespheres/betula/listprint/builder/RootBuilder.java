/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.listprint.builder;

import java.util.ArrayList;
import java.util.List;
import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.BreaksType;
import org.plutext.jaxb.xslfo.Flow;
import org.plutext.jaxb.xslfo.LanguageType;
import org.plutext.jaxb.xslfo.LayoutMasterSet;
import org.plutext.jaxb.xslfo.PageSequence;
import org.plutext.jaxb.xslfo.RegionAfter;
import org.plutext.jaxb.xslfo.RegionBody;
import org.plutext.jaxb.xslfo.Root;
import org.plutext.jaxb.xslfo.SimplePageMaster;
import org.plutext.jaxb.xslfo.StaticContent;
import org.plutext.jaxb.xslfo.TextAlignType;

/**
 *
 * @author boris.heithecker
 */
public class RootBuilder {

    private final Root root = new Root();
    private final List<Object> flow = new ArrayList<>();
    private String titleContent;

    protected RootBuilder() {
    }

    public RootBuilder(String title, Object f) {
        this.titleContent = title;
        this.flow.add(f);
    }

    public void addFlow(Object f) {
        flow.add(f);
    }

    public Root build() {
        root.setLayoutMasterSet(createLayoutMasterSet());
        root.getPageSequence().add(createPageSequence());
        return root;
    }

    protected LayoutMasterSet createLayoutMasterSet() {
        LayoutMasterSet ret = new LayoutMasterSet();
        SimplePageMaster spm = createSimplePageMaster();
        RegionBody body = new RegionBody();
        body.setRegionName("body");
        spm.setRegionBody(body);
        RegionAfter after = new RegionAfter();
        after.setRegionName("footer");
        after.setExtent("0.5cm");
        spm.setRegionAfter(after);
        ret.getSimplePageMasterOrPageSequenceMaster().add(spm);
        return ret;
    }

    protected SimplePageMaster createSimplePageMaster() {
        SimplePageMaster spm = new SimplePageMaster();
        spm.setMasterName("master");
        spm.setPageWidth("29.7cm");
        spm.setPageHeight("21.0cm");
        spm.setMarginTop("2.0cm");
        spm.setMarginBottom("1.0cm");
        spm.setMarginLeft("2.0cm");
        spm.setMarginRight("1.7cm");
        return spm;
    }

    protected PageSequence createPageSequence() {
        PageSequence ret = new PageSequence();
        ret.setMasterReference("master");
        ret.setLanguage(LanguageType.DE);
        StaticContent footer = new StaticContent();
        footer.setFlowName("footer");
        Block fBlock = createFooterBlock();
        footer.getBlockOrBlockContainerOrTable().add(fBlock);
        ret.getStaticContent().add(footer);
        Flow f = createFlow();
        ret.setFlow(f);
        return ret;
    }

    protected Block createFooterBlock() {
        Block ret = Util.createBlock("0.0cm", "0.0cm", "0.0cm", "7pt", "#000000", TextAlignType.CENTER);
        final String footerContent = getFooterContent();
        if (footerContent != null) {
            ret.getContent().add(footerContent);
        }
        return ret;
    }

    protected String getFooterContent() {
        return null;
    }

    protected Flow createFlow() {
        Flow ret = new Flow();
        ret.setFlowName("body");
        Block title = createTitle();
        ret.getMarkerOrBlockOrBlockContainer().add(title);
        Block ruler = Util.createRule("2pt", "2.0pt");
        ret.getMarkerOrBlockOrBlockContainer().add(ruler);
        flow.stream()
                .forEach(o -> ret.getMarkerOrBlockOrBlockContainer().add(o));
        return ret;
    }

    protected Block createTitle() {
        Block ret = new Block();
        ret.setFontSize("12pt");
        ret.setFontFamily("SansSerif");
        ret.setFontWeight("bold");
        ret.setSpaceAfter("0.1cm");
        ret.setBreakBefore(BreaksType.PAGE);
        final String tc = createTitleContent();
        if (tc != null) {
            ret.getContent().add(tc);
        }
        return ret;
    }

    protected String createTitleContent() {
        return this.titleContent;
    }

}
