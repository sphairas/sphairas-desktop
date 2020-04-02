/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author boris.heithecker
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class FastMessage {

    //TODO FastMessage und FastMessageRef: ref mit _id -ref, fastmessage mit message
    private static final long serialVersionUID = 1L;
    @JsonProperty("_id")
    private String messageId;
    @JsonProperty("_rev")
    private String rev;
    private String header;
    private String text;
    private String author;
    private long date;
//    private org.thespheres.betula.tag.Status 
}
