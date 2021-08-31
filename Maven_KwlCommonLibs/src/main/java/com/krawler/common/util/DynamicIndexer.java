/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.common.util;

import com.googlecode.cqengine.CQEngine;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.ReflectiveAttribute;
import com.googlecode.cqengine.index.navigable.NavigableIndex;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class DynamicIndexer {

    /**
     * Generates attributes dynamically for the fields declared in the given POJO class.
     * <p/>
     * Implementation is currently limited to generating attributes for Comparable fields (String, Integer etc.).
     *
     * @param pojoClass A POJO class
     * @param <O> Type of the POJO class
     * @return Attributes for fields in the POJO
     */
    public static <O> Map<String, Attribute<O, Comparable>> generateAttributesForPojo(Class<O> pojoClass) {
        Map<String, Attribute<O, Comparable>> generatedAttributes = new LinkedHashMap<String, Attribute<O, Comparable>>();
        for (Field field : pojoClass.getDeclaredFields()) {
            if (Comparable.class.isAssignableFrom(field.getType())) {
                @SuppressWarnings({"unchecked"})
                Class<Comparable> fieldType = (Class<Comparable>) field.getType();
                generatedAttributes.put(field.getName(), ReflectiveAttribute.forField(pojoClass, fieldType, field.getName()));
            }
        }
        return generatedAttributes;
    }

    /**
     * Creates an IndexedCollection and adds NavigableIndexes for the given attributes.
     *
     * @param attributes Attributes for which indexes should be added
     * @param <O> Type of objects stored in the collection
     * @return An IndexedCollection configured with indexes on the given attributes.
     */
    public static <O> IndexedCollection<O> newAutoIndexedCollection(Iterable<Attribute<O, Comparable>> attributes) {
        IndexedCollection<O> autoIndexedCollection = CQEngine.newInstance();
        for (Attribute<O, ? extends Comparable> attribute : attributes) {
            // Add a NavigableIndex...
            autoIndexedCollection.addIndex(NavigableIndex.onAttribute(attribute));
        }
        return autoIndexedCollection;
    }

    /**
     * Private constructor, not used.
     */
    DynamicIndexer() {
    }
}