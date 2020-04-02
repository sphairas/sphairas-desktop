/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.util.LocalDateTimeAdapter;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExceptionMessage {

    @XmlElement(name = "stack-trace-element")
    private String stackTraceElement;
    @XmlElement(name = "display-message")
    private String userMessage;
    @XmlElement(name = "log")
    private String logMessage;
    @XmlElement(name = "timestamp")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime date;

    public ExceptionMessage() {
    }

    private ExceptionMessage(String stackTraceElement, String userMessage, String logMessage, LocalDateTime date) {
        this.stackTraceElement = stackTraceElement;
        this.userMessage = userMessage;
        this.logMessage = logMessage;
        this.date = date;
    }

    public static ExceptionMessage create(RuntimeException e, String userMessage, String logMessage, LocalDateTime date) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        final String ste = sw.toString();
        return new ExceptionMessage(ste, userMessage, logMessage, date);
    }

    public String getStackTraceElement() {
        return stackTraceElement;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public LocalDateTime getDate() {
        return date;
    }

}
