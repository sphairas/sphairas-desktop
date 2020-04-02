/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.classtest;

import java.util.SortedSet;
import org.thespheres.betula.classtest.Basket.Ref;

/**
 *
 * @author boris.heithecker
 * @param <A>
 * @param <R>
 */
public interface Basket<A extends Assessable, R extends Ref> extends Assessable {

    public SortedSet<R> getReferences();

    public R getReference(int index);

    public R getReference(String id);

    public R addReference(A assessble);

    public R addReference(A assessble, int index);

    public boolean removeReference(String id);

    public interface Ref<N extends Number> extends Assessable {

        public static final String PROP_INDEX = "index";

        public int getIndex();
        
        public void setIndex(int index) throws UnsupportedOperationException;

        public N getWeight();

        public void setWeight(N weight);

        default public int comparePosition(Ref<N> o) {
            return getIndex() - o.getIndex();
        }
    }
}
