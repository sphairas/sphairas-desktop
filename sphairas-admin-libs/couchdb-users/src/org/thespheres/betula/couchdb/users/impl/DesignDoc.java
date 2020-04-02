/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.couchdb.users.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author boris.heithecker
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class DesignDoc implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String DESIGN_DOC_BASE = "_design/";
    @JsonProperty("_id")
    private String id;
    @JsonProperty("_rev")
    private String rev;
    @JsonProperty("language")
    private String language = "javascript";
    @JsonProperty("views")
    private Map<String, View> views = new HashMap<>();

    public DesignDoc() {
    }

    public DesignDoc(String design) {
        this.id = DESIGN_DOC_BASE + design;
    }

    public Map<String, View> getViews() {
        return views;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class View {

        @JsonProperty("map")
        private String map;

        public View() {
        }

        View(String map) {
            this.map = map;
        }

    }
}
