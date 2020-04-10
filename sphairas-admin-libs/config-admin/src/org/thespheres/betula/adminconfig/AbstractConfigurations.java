package org.thespheres.betula.adminconfig;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author boris
 */
public abstract class AbstractConfigurations implements Configurations {

    @Override
    public <T> Configuration<T> readConfiguration(final String resource, final Class<T> type) throws IOException {
        return this.findConfiguration(resource, type);
    }

    protected <T> ConfigurationImpl<T> findConfiguration(final String query, final Class<T> returnType) throws IOException {
        final Enumeration<? extends FileObject> c = FileUtil.getConfigFile("/SyncedFiles/").getChildren(false);
        while (c.hasMoreElements()) {
            final FileObject fo = c.nextElement();
            final String clz = (String) fo.getAttribute("access-class");
            final String method = (String) fo.getAttribute("access-method");
            if (clz != null && method != null) {
                final String rn = (String) fo.getAttribute("resource-name");
                final List<String> ll = fo.asLines("utf-8");
                if (ll.size() == 1) {
                    final String res = ll.get(0);
                    if (rn != null && rn.equals(query) || rn == null && res.equals(query)) {
                        final T obj = readSingleResource(clz, method, res, returnType);
                        return new ConfigurationImpl<>(rn != null ? rn : res, new String[]{res}, obj);
                    }
                } else if (rn != null && ll.size() > 1) {
                    final T obj = readMultipleResource(clz, method, ll, returnType);
                    return new ConfigurationImpl<>(rn, ll.stream().toArray(String[]::new), obj);
                }
            }
        }
        return null;
    }

    protected <T> T readSingleResource(final String clz, final String method, final String res, final Class<T> retType) throws IOException {
        try {
            final Class<?> type = Class.forName(clz, true, Thread.currentThread().getContextClassLoader());
            final Method meth = type.getMethod(method, InputStream.class);
            final InputStream is = getResource(res);
            final Object ret = meth.invoke(null, is);
            return retType.cast(ret);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("Could not invode " + method + " on class " + clz, ex);
        }
    }

    protected <T> T readMultipleResource(final String clz, final String method, final List<String> res, final Class<T> retType) throws IOException {
        try {
            final Class<?> type = Class.forName(clz, true, Thread.currentThread().getContextClassLoader());
            final Method meth = type.getMethod(method, Map.class);
            final Map<String, Object> m = new HashMap<>();
            for (final String r : res) {
                final InputStream is = getResource(r);
                m.put(r, is);
            }
            final Object ret = meth.invoke(null, m);
            return retType.cast(ret);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("Could not invode " + method + " on class " + clz, ex);
        }
    }

    protected abstract InputStream getResource(final String res) throws IOException;

    protected class ConfigurationImpl<T> implements Configuration<T> {

        private final String name;
        private final String[] resources;
        private final T config;

        protected ConfigurationImpl(final String name, final String[] resources, final T config) {
            this.name = name;
            this.resources = resources;
            this.config = config;
        }

        @Override
        public T get() {
            return config;
        }

        @Override
        public String[] getResources() {
            return this.resources;
        }

        @Override
        public String getResourceName() {
            return this.name;
        }

    }

}
