/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tag;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.Tag;
import org.thespheres.betula.document.Action;
import org.thespheres.betula.document.MarkerFactory;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TagAdapter implements Serializable {

    public static final long serialVersionUID = 1L;
    @XmlAttribute(name = "convention", required = true)
    protected String convention;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "action")
    protected Action action;

    public TagAdapter() {
    }

    public TagAdapter(final Tag tag) {
        this.convention = tag.getConvention();
        this.id = tag.getId();
    }

    public TagAdapter(final String convention, final String id) {
        if (StringUtils.isBlank(convention) || StringUtils.isBlank(id)) {
            throw new IllegalArgumentException();
        }
        this.convention = convention;
        this.id = id;
    }

    public Tag getTag() {
        if (getConvention().equals("null") && getId().equals("null")) {
            return Tag.NULL;
        }
        return MarkerFactory.find(convention, id);
    }

    public String getConvention() {
        return convention;
    }

    public String getId() {
        return id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public static class XmlTagAdapter extends XmlAdapter<TagAdapter, Tag> {

        public XmlTagAdapter() {
        }

        @Override
        public Tag unmarshal(TagAdapter v) throws Exception {
            return v != null ? v.getTag() : null;
        }

        @Override
        public TagAdapter marshal(Tag v) throws Exception {
            return v != null ? new TagAdapter(v) : null;
        }
    }
}
