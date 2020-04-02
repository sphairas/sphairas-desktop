/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

/**
 *
 * @author boris.heithecker
 */
public class FormatException extends Exception {

    protected final String line;
    protected final int offset;

    public FormatException(String message, String line, int offset) {
        super(message);
        this.line = line;
        this.offset = offset;
    }

    public String getLine() {
        return line;
    }

    public int getOffset() {
        return offset;
    }

}
