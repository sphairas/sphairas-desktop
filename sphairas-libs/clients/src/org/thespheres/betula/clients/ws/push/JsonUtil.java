/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.clients.ws.push;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 */
class JsonUtil {

    static DocumentId extractDocumentId(JsonNode tree) throws IOException {
        final JsonNode d = tree.get("document");
        if (d != null) {
            String authority = findSingleJsonValue(d, "authority");
            String id = findSingleJsonValue(d, "id");
            String version = findSingleJsonValue(d, "version");
            return new DocumentId(authority, id, DocumentId.Version.parse(version));
        }
        throw new IOException();
    }

    static Timestamp extractTimestamp(JsonNode tree) throws IOException {
        List<JsonNode> ids = tree.findValues("timestamp");
        if (ids.size() == 1 && ids.get(0).isLong()) {
            long l = ids.get(0).asLong();
            if (l > 0l) {
                return new Timestamp(l);
            }
        }
        return null;
    }

    static String findSingleJsonValue(final JsonNode t, final String name) throws IOException {
        List<JsonNode> ids = t.findValues(name);
        if (ids.size() == 1 && ids.get(0).isTextual()) {
            String value = StringUtils.trimToNull(ids.get(0).asText().trim());
            if (value != null) {
                return value;
            }
        }
        throw new IOException();
    }
}
