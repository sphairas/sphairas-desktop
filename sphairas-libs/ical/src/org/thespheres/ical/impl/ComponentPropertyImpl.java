/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.thespheres.ical.builder.VCardBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.ical.CalendarComponentProperty;
import org.thespheres.ical.CardComponentProperty;
import org.thespheres.ical.Parameter;
import org.thespheres.ical.builder.ICalendarBuilder;

/**
 *
 * @author boris.heithecker
 */
public class ComponentPropertyImpl implements CalendarComponentProperty, CardComponentProperty, Externalizable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String value; // TODO: make this final?
    private ParameterList paramsList = new ParameterList();

    public ComponentPropertyImpl() {
    }

    ComponentPropertyImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    ComponentPropertyImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    void setValue(String v) {
        this.value = v;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.unmodifiableList(paramsList.getList());
    }

    @Override
    public Set<String> getParameterNames() {
        return paramsList.getList().stream().map(Parameter::getName).collect(Collectors.toSet());
    }

    @Override
    public List<Parameter> getParameters(String name) {
        return paramsList.getList().stream().filter(p -> name.equals(p.getName())).collect(Collectors.toList());
    }

    @Override
    public Optional<String> getAnyParameter(String name) {
        return getParameters(name).stream().findAny().map(Parameter::getValue);
    }

    public Parameter addParameter(String name, String value) {
        return paramsList.add(name, value);
    }

    ICalendarBuilder.ParameterIterator iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (name == null || value == null) {
            throw new IllegalStateException("Name and/or value in " + getClass().getCanonicalName() + " cannot be null.");
        }
        out.writeUTF(name);
        out.writeUTF(value);
        out.writeObject(paramsList);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.name = in.readUTF();
        this.value = in.readUTF();
        this.paramsList = (ParameterList) in.readObject();
    }

    public void toString(StringBuilder sb) {
        propertyToString(sb, getName(), getValue(), paramsList);
    }

    public static void propertyToString(StringBuilder sb, String name, String value, ParameterList params) {
        sb.append(name);
        if (params != null) {
            params.toString(sb);
        }
        sb.append(":").append(value).append(VCardBuilder.NEWLINE);
    }

}
