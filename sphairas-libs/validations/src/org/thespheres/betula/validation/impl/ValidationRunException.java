/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.validation.impl;

/**
 *
 * @author boris.heithecker
 */
public class ValidationRunException extends RuntimeException {

    private int count = 0;

    public ValidationRunException() {
    }

    public ValidationRunException(String message) {
        super(message);
    }

    public ValidationRunException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationRunException(Throwable cause) {
        super(cause);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
