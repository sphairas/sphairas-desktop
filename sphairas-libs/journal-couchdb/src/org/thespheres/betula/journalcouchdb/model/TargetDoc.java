/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journalcouchdb.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;

/**
 *
 * @author boris.heithecker
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"target", "rev", "display", "convention", "authority"})//ensure these are deserialized before unit, id
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class TargetDoc { //CouchDbDocument

    @JsonProperty("_id")
    private String id;
    @JsonIgnore
    private transient DocumentId parsed;
    @JsonProperty("_rev")
    private String rev;
    private String display;
//    String convention;vUnitIdDeserializer
    @JsonSerialize(using = UnitIdSerializer.class)
    @JsonDeserialize(using = UnitIdDeserializer.class)
    private UnitId unit;
//    @DocumentReferences(backReference = "target", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orderBy = "id")
    @JsonIgnore
    private transient Set<TimeDoc2> times;
    private TargetStudent[] students = new TargetStudent[0];

    public TargetDoc() {
    }

    public TargetDoc(DocumentId target) {
        this.id = createId(target);
        parsed = target;
    }

    public static String createId(DocumentId target) {
        return "tg_" + target.toString();
    }

    public String getId() {
        return id;
    }

    public DocumentId getDocumentId() {
        if (parsed == null) {
            parsed = IDUtilities.parseDocumentId(id);
        }
        return parsed;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public UnitId getUnit() {
        return unit;
    }

    public void setUnit(UnitId unit) {
        this.unit = unit;
    }

    public TargetStudent[] getStudents() {
        return students;
    }

    public void setStudents(TargetStudent[] students) {
        this.students = students;
    }

    public Set<TimeDoc2> getTimes() {
        if (times == null) {
            times = new HashSet<>();
        }
        return times;
    }
//
//    public void setTimes(Set<TimeDoc> times) {
//        this.times = times;
//    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.id);
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
        final TargetDoc other = (TargetDoc) obj;
        return Objects.equals(this.id, other.id);
    }

    public static class UnitIdSerializer extends JsonSerializer<UnitId> {

        @Override
        public void serialize(UnitId unit, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
            if (unit != null) {
//                jg.writeStartObject();
//                jg.writeStringField("authority", unit.getAuthority());
//                jg.writeStringField("id", unit.getId());
                jg.writeString(unit.toString());
//                jg.writeEndObject();
            }
        }

    }

    public static class UnitIdDeserializer extends JsonDeserializer<UnitId> {

        @Override
        public UnitId deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);
            String val = node.asText();
//            return new UnitId(node.get("authority").asText(), node.get("id").asText());
            if (StringUtils.isBlank(val)) {
                return null;
            }
            return IDUtilities.parseUnitId(val);
        }

    }
}
