/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.beans;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.acer.MessageId;
import org.thespheres.betula.document.Signee;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MessageObject extends MessageContent {

    static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    @Deprecated
    private static DateTimeFormatter LEGACY = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    static final ZoneId ZONE = ZoneId.of("UTC");
    private MessageId messageId;
    private String channelName;
    private Signee author;
    private String datePublished;

    public MessageObject() {
    }

    public MessageObject(MessageId messageId, String channelName, String text, boolean isTextEncoded, Signee author, java.sql.Timestamp datePublished, int priority) {
        if (messageId == null || channelName == null || text == null || datePublished == null) {
            throw new IllegalArgumentException("None of messageId, channelName, text or datePublished can be null.");
        }
        this.messageId = messageId;
        this.channelName = channelName;
        this.text = text;
        this.isTextEncoded = isTextEncoded;
        this.author = author;
        this.datePublished = DTF.format(LocalDateTime.from(datePublished.toInstant().atZone(ZONE)));
        this.priority = priority;
    }

    public MessageId getMessageId() {
        return messageId;
    }

    public String getChannelName() {
        return channelName;
    }

    public Signee getAuthor() {
        return author;
    }

    public LocalDateTime getDatePublished() {
        try {
            return LocalDateTime.parse(datePublished, DTF).atZone(ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(datePublished, LEGACY).atZone(ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

}
