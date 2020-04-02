/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.time.ZonedDateTime;

/**
 *
 * @author boris.heithecker
 */
public interface Document {

    public static final String CREATOR = "signee.creator";

    public boolean isFragment();

    public Validity getDocumentValidity();

    public SigneeInfo getCreationInfo();

    public interface SigneeInfo {

        public Signee getSignee();

        public Timestamp getTimestamp();
    }

    public interface Validity {

        public boolean isValid();

        public ZonedDateTime getExpirationDate();
    }

    public Marker[] markers();
}
