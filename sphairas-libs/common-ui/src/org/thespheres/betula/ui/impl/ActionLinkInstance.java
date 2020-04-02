/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.ui.impl;

import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.openide.util.Lookup;

/**
 *
 * @author boris.heithecker
 */
public class ActionLinkInstance {

    private final Class<?> type;
    private final String method;

    private ActionLinkInstance(Class<?> type, String method) {
        this.type = type;
        this.method = method;
    }

    public ActionListener contextAction(Object context) {
        for (Method m : type.getDeclaredMethods()) {
            if (m.getName().equals(method)) {
                if (context == null && m.getParameterCount() == 0) {
                    try {
                        return (ActionListener) m.invoke(null);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new IllegalStateException(ex);
                    }
                } else if (context != null && m.getParameterCount() == 1) {
                    if (m.getParameters()[0].getType().isAssignableFrom(context.getClass())) {
                        try {
                            return (ActionListener) m.invoke(null, context);
                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String findPath(String cat, String id) {
        return "ActionLinks" + "/" + cat + "/" + id.replace('.', '-');
    }

    public static ActionLinkInstance create(Map<String, ?> params) {
        String clazz = (String) params.get("class");
        String method = (String) params.get("method");
        try {
            final ClassLoader sysCl = Lookup.getDefault().lookup(ClassLoader.class);
            Class<?> clz = Class.forName(clazz, true, sysCl);
            return new ActionLinkInstance(clz, method);
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }
}
