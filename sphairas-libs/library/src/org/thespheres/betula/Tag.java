/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula;

/**
 *
 * @author boris.heithecker
 */
public interface Tag {

    public static final Tag NULL = new NullTag();

    public static boolean isNull(Tag t) {
        return t == null || t.equals(NULL) || "null".equals(t.getId());
    }

    public String getConvention();

    public String getId();

    public String getLongLabel(Object... formattingArgs);

    public String getShortLabel();

    @Override
    boolean equals(Object other);

}
