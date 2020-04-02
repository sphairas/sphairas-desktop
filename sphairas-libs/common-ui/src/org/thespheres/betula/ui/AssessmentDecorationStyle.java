/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui;

/**
 *
 * @author boris.heithecker
 */
public class AssessmentDecorationStyle {

    private final String foreGround;
    private final String backGround;
    private final String fontStyle;
    private final String fontWeight;

    private final String hint;
    private final String name;

    public AssessmentDecorationStyle(String name, String foreGround, String backGround, String fontStyle, String fontWeight, String hint) {
        this.name = name;
        this.foreGround = foreGround;
        this.backGround = backGround;
        this.fontStyle = fontStyle;
        this.fontWeight = fontWeight;
        this.hint = hint;
    }

    public String getName() {
        return name;
    }

    public String getForeGround() {
        return foreGround;
    }

    public String getBackGround() {
        return backGround;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public String getHint() {
        return hint;
    }

}
