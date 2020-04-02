/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical;

/**
 *
 * @author boris.heithecker
 */
public class InvalidComponentException extends Exception {

    private final IComponent component;

    public InvalidComponentException(String message) {
        this(null, message);
    }

    public InvalidComponentException(IComponent component, String message) {
        super(message);
        this.component = component;
    }

    public InvalidComponentException(Exception cause) {
        this(null, cause);
    }

    public InvalidComponentException(IComponent component, Exception cause) {
        super();
        this.component = component;
        initCause(cause);
    }

    public IComponent getComponent() {
        return component;
    }

}
