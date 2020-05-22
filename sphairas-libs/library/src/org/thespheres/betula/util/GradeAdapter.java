/*
 * GradeAdapter.java
 *
 * Created on 17. Mai 2007, 23:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.thespheres.betula.assess.AbstractGrade;
import org.thespheres.betula.assess.Grade;
import org.thespheres.betula.assess.GradeFactory;

/**
 *
 * @author Boris Heithecker
 */
//Später wieder modifier = internal !!!!!!!!
//@XmlType(name = "grade", namespace = "http://www.thespheres.org/xsd/betula/classroomtest.xsd")
@XmlRootElement(name = "grade", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlType(name = "gradeAdapterType", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public final class GradeAdapter extends XmlAdapter<GradeAdapter, Grade> implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlAttribute(name = "convention", required = true)
    private String convention;
    @XmlAttribute(name = "grade-id", required = true)
    private String gradeId;
    @XmlAttribute(name = "cookie")
    private String cookie;
    @XmlAttribute(name = "context") //LEGACY
    private String context;
    @XmlAttribute(name = "id") //LEGACY
    private String id;

    public GradeAdapter() {
    }

    public GradeAdapter(Grade grade) {
        this.convention = grade.getConvention();
        this.gradeId = grade.getId();
    }

    private GradeAdapter(String convention, String id) {
        this.convention = convention;
        this.gradeId = id;
    }

    public String getConvention() {
        return convention;
    }

    public String getId() {
        return gradeId;
    }

    public Grade getGrade() {
        return unmarshal(this);
    }

    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (context != null && convention == null) {
            convention = context;
            context = null;
        }
        if (id != null && gradeId == null) {
            gradeId = id;
            id = null;
        }
    }

    @Override
    public Grade unmarshal(GradeAdapter v) {
        Grade ret = GradeFactory.find(v.convention, v.gradeId);
        if (ret == null) {
            ret = new AbstractGrade(v.convention, v.gradeId);
        } else if (ret instanceof Grade.Cookie && v.cookie != null) {
            try {
                Method m = ret.getClass().getDeclaredMethod("setCookie", String.class);
                m.invoke(ret, v.cookie);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(GradeAdapter.class.getName()).log(Level.WARNING, "Grade.Cookie " + ret.toString() + " has no method setCookie(String.class). Cannot unmarshal from xml.", ex);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(GradeAdapter.class.getName()).log(Level.SEVERE, "An error occurred unmarshalling Grade.Cookie " + ret.toString() + ".", ex);
            }
        }
        return ret;
    }

    @Override
    public GradeAdapter marshal(Grade v) throws Exception {
        final GradeAdapter ret = v == null ? null : new GradeAdapter(v.getConvention(), v.getId());
        if (ret != null && v instanceof Grade.Cookie) {
            final Object c = ((Grade.Cookie) v).getCookie();
            try {
                String s = String.class.cast(c);
                ret.cookie = s;
            } catch (ClassCastException e) {
                Logger.getLogger(GradeAdapter.class.getName()).log(Level.WARNING, "Cookie of Grade.Cookie " + v.toString() + " not assignable to String.class. Will not persist to xml.", e);
            }
        }
        return ret;
    }
}
