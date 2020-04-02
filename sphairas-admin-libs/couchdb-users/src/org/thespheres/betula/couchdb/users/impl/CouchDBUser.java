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
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author boris.heithecker
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class CouchDBUser implements Serializable {

    public static final String ID_PREFIX = "org.couchdb.user:";
    public static final Pattern PATTERN = Pattern.compile("^[a-z][a-z0-9_$()+/-]*$");
    private static final long serialVersionUID = 1L;
    @JsonProperty("_id")
    private String id;
    @JsonProperty("_rev")
    private String rev;
    private String name;
    private String password;
    private String type;
    private String[] roles;

    public CouchDBUser() {
    }

    private CouchDBUser(String id, String name, String password, String type, String[] roles) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.type = type;
        this.roles = roles;
    }

    public static CouchDBUser create(String user, String password, String[] roles) {
        if (StringUtils.isBlank(user)) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (StringUtils.isBlank(password)) {
            password = user;
        }
        if (roles == null) {
            roles = new String[]{};
        }
        String id = createUserID(user);
        CouchDBUser ret = new CouchDBUser(id, user, password, "user", roles);
        return ret;
    }

    public static String createUserID(String user) {
        return ID_PREFIX + user;
    }

    public String getCouchDBUserID() {
        return id;
    }

    public String getUser() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getType() {
        return type;
    }

    public String[] getRoles() {
        return roles;
    }

}
