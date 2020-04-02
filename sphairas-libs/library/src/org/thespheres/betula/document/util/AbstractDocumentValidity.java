/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import org.thespheres.betula.document.Document.Validity;

/**
 *
 * @author boris.heithecker
 */
public class AbstractDocumentValidity implements Validity {

    private final ZonedDateTime date;

    public AbstractDocumentValidity() {
        this(ZonedDateTime.of(9999, Month.DECEMBER.getValue(), 31, 0, 0, 0, 0, ZoneId.systemDefault()));
    }

    public AbstractDocumentValidity(Date date) {
        this(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    public AbstractDocumentValidity(ZonedDateTime date) {
        this.date = date;
    }

    @Override
    public boolean isValid() {
        return getExpirationDate() == null ? true : getExpirationDate().isAfter(Instant.now().atZone(getExpirationDate().getZone()));
    }

    @Override
    public ZonedDateTime getExpirationDate() {
        return date;
    }

}
