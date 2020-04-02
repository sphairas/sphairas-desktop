/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.listprint.builder;

import org.plutext.jaxb.xslfo.Block;
import org.plutext.jaxb.xslfo.Leader;
import org.plutext.jaxb.xslfo.LeaderPatternType;
import org.plutext.jaxb.xslfo.RuleStyleType;
import org.plutext.jaxb.xslfo.TextAlignLastType;
import org.plutext.jaxb.xslfo.TextAlignType;

/**
 *
 * @author boris.heithecker
 */
public class Util {

    public static Block createRule(final String lineHeight1, final String ruleThickness) {
        Block ret = new Block();
        ret.setTextAlignLast(TextAlignLastType.JUSTIFY);
        ret.setLineHeight(lineHeight1);
        ret.setSpaceAfter("0.1cm");
        Leader leader = new Leader();
        leader.setLeaderPattern(LeaderPatternType.RULE);
        leader.setRuleThickness(ruleThickness);
        leader.setRuleStyle(RuleStyleType.SOLID);
        ret.setColor("#ff9a33");
        ret.getContent().add(leader);
        return ret;
    }

    public static Block createBlock(String marginTop, String marginLeft, String marginRight, String fontSize, String color, TextAlignType textAlign) {
        Block ret = new Block();
        ret.setMarginTop(marginTop);
        ret.setMarginLeft(marginLeft);
        ret.setMarginRight(marginRight);
        ret.setFontSize(fontSize);
        ret.setColor(color);
        ret.setTextAlign(textAlign);
        return ret;
    }

    public static Block createBlock(String fontSize, TextAlignType textAlign) {
        final Block ret = new Block();
        ret.setFontSize(fontSize);
        ret.setTextAlign(textAlign);
        return ret;
    }

}
