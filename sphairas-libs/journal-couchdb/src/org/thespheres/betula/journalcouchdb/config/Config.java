/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *
 * @author boris.heithecker
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"target", "rev", "display", "convention", "authority"})//ensure these are deserialized before unit, id
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Config {

    public static final String CONFIG_DOCUMENT_ID = "user.config";
    @JsonProperty("_id")
    private String id;
    @JsonProperty("_rev")
    private String rev;
    @JsonProperty("updates-href")
    private String updatesHref;
    @JsonProperty("updates-creds")
    private String updatesCreds;

    public Config() {
    }

    private Config(String id) {
        this.id = id;
    }

    public static Config create() {
        return new Config(CONFIG_DOCUMENT_ID);
    }

    public String getUpdatesHref() {
        return updatesHref;
    }

    public void setUpdatesHref(String updatesHref) {
        this.updatesHref = updatesHref;
    }

    public String getUpdatesCreds() {
        return updatesCreds;
    }

    public void setUpdatesCreds(String updatesCreds) {
        this.updatesCreds = updatesCreds;
    }

}
