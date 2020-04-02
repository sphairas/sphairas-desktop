/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation;

import java.time.LocalDateTime;

/**
 *
 * @author boris.heithecker
 */
public class ValidationResult {

    private final static long[] COUNTER = new long[]{1l};
    //Not recommended! Simply: no result --> everything okay, use listern to find out when validation is over
//    public static ValidationResult OK = new ValidationResult();
    private final long id;
    private String message;
    private final LocalDateTime time;

    protected ValidationResult(LocalDateTime time) {
        synchronized (COUNTER) {
            this.id = COUNTER[0]++;
        }
        this.time = time;
    }

    protected ValidationResult() {
        this(LocalDateTime.now());
    }

    public final long id() {
        return id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    protected void setMessage(String message) {
        this.message = message;
    }
}
