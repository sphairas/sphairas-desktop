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
import org.thespheres.ical.builder.AbstractComponentBuilder;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.thespheres.ical.IComponent;
/**
 *
 * @author boris.heithecker
 */
public abstract class ComponentImpl<I extends ComponentPropertyImpl> implements Externalizable {

    private static final long serialVersionUID = 1L;
    private String name;
    protected final ArrayList<I> properties = new ArrayList<>();

    public ComponentImpl() {
    }

    ComponentImpl(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<String> getPropertyNames() {
        return properties.stream().map(ComponentPropertyImpl::getName).collect(Collectors.toSet());
    }

    public I getAnyProperty(String name) {
        return properties.stream()
                .filter(p -> name.equals(p.getName()))
                .findAny()
                .orElse(null);
    }

    public Optional<String> getAnyPropertyValue(String name) {
        return properties.stream()
                .filter(p -> name.equals(p.getName()))
                .findAny()
                .map(ComponentPropertyImpl::getValue);
    }

    public abstract I createProperty(String name, String value);

    public I addProperty(String name, String value) {
        I toAdd = createProperty(name, value);
        return addProperty(toAdd);
    }

    I addProperty(I property) {
        synchronized (properties) {
            properties.add(property);
        }
        return property;
    }

    protected void toString(final StringBuilder sb) {
        properties.forEach(pi -> {
            pi.toString(sb);
        });
    }

    public static String toIComponent(ComponentImpl c) {
        StringBuilder sb = new StringBuilder();
        sb.append(IComponent.BEGIN).append(":").append(c.getName()).append(AbstractComponentBuilder.NEWLINE);
        c.toString(sb);
        sb.append(IComponent.END).append(":").append(c.getName()).append(AbstractComponentBuilder.NEWLINE);
        return sb.toString();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (name == null) {
            throw new IllegalStateException("Name in " + getClass().getCanonicalName() + " cannot be null.");
        }
        out.writeUTF(name);
        out.writeInt(properties.size());
        for (I i : properties) {
            out.writeObject(i);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        int s = in.readInt();
        for (int i = 0; i++ < s;) {
            I o = (I) in.readObject();
            properties.add(o);
        }
    }

    @Override
    public String toString() {
        return toIComponent(this);
    }
}
