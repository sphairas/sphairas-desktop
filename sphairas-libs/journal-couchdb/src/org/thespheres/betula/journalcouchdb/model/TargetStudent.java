/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.Objects;
import org.thespheres.betula.StudentId;

/**
 *
 * @author boris.heithecker
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class TargetStudent {

    private String family;
    private String given;
    @JsonDeserialize(using = StudentStringDeserializer.class)
    private String student;
    @JsonIgnore
    private transient StudentId studentParsed;

    public TargetStudent() {
    }

    public TargetStudent(StudentId s, String family, String given) {
        this.student = s.toString();
        this.family = family;
        this.given = given;
    }

    public String getFamily() {
        return family;
    }

    public String getGiven() {
        return given;
    }

    public StudentId getStudent() {
        if (studentParsed == null && student != null) {
            studentParsed = IDUtilities.parseStudentId(student);
        }
        return studentParsed;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.student);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TargetStudent other = (TargetStudent) obj;
        return Objects.equals(this.student, other.student);
    }

    public static class StudentStringDeserializer extends JsonDeserializer<String> {

        @Override
        public String deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);
            return node.asText();
        }

    }
}
