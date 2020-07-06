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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.thespheres.ical.Parameter;

/**
 *
 * @author boris.heithecker
 */
public class ParameterList implements Externalizable {

    private static final long serialVersionUID = 1L;
    private final ArrayList<ParameterImpl> list = new ArrayList<>();

    public static ParameterList parse(String dbData) {
        ParameterList ret = new ParameterList();
        if (dbData != null && dbData.length() > 0) {
            for (String ep : dbData.split(";", 1)) {
                if (!ep.isEmpty()) {
                    String[] p = ep.split("=");
                    if (p.length == 2) {
                        String name = p[0];
                        String value = p[1];
                        if (!name.isEmpty() && !value.isEmpty()) {
                            ret.list.add(new ParameterImpl(name, value));
                        }
                    }
                }
            }
        }
        return ret;
    }

    public List<Parameter> getList() {
        return Collections.unmodifiableList(list);
    }

    public Parameter add(String name, String value) {
        synchronized (list) {
            ParameterImpl ret = new ParameterImpl(name, value);
            list.add(ret);
            return ret;
        }
    }

    public Parameter add(Parameter parameter) {
        synchronized (list) {
            ParameterImpl ret = new ParameterImpl(parameter.getName(), parameter.getValue());
            list.add(ret);
            return ret;
        }
    }

    public boolean remove(final String name) {
        boolean ret = false;
        synchronized (list) {
            final ListIterator<ParameterImpl> it = list.listIterator();
            while (it.hasNext()) {
                if (it.next().getName().equals(name)) {
                    it.remove();
                    ret = true;
                }
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(list.size());
        for (Parameter p : list) {
            out.writeObject(p);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int s = in.readInt();
        for (int i = 0; i++ < s;) {
            ParameterImpl p = (ParameterImpl) in.readObject();
            list.add(p);
        }
    }

    public void toString(final StringBuilder sb) {
        list.stream()
                .forEach(p -> p.toString(sb));
    }

    private static class ParameterImpl extends Parameter implements Externalizable {

        private static final long serialVersionUID = 1L;

        //For externalization
        public ParameterImpl() {
        }

        private ParameterImpl(String name, String value) {
            super(name, value);
        }

        private void setValue(String value) {
            this.value = value;
        }

    }

}
