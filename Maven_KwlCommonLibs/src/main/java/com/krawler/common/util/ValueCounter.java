/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple counter that maintains the count of unique values passed into the
 * {@link #increment} and {@link #decrement} methods.
 */
public class ValueCounter {

    private Map /*
             * <Object, Integer>
             */ mValues = new HashMap();

    public void increment(Object value) {
        increment(value, 1);
    }

    public void decrement(Object value) {
        increment(value, -1);
    }

    public void increment(Object value, int delta) {
        Integer count = (Integer) mValues.get(value);
        if (count == null) {
            count = new Integer(delta);
        } else {
            count = new Integer(count.intValue() + delta);
        }
        mValues.put(value, count);
    }

    public int getCount(Object value) {
        Integer count = (Integer) mValues.get(value);
        if (count == null) {
            return 0;
        }
        return count.intValue();
    }

    public Iterator iterator() {
        return mValues.keySet().iterator();
    }

    public int size() {
        return mValues.size();
    }

    public int getTotal() {
        int total = 0;
        Iterator i = iterator();
        while (i.hasNext()) {
            total = total + getCount(i.next());
        }
        return total;
    }

    public void clear() {
        mValues.clear();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        Iterator i = iterator();
        while (i.hasNext()) {
            if (buf.length() != 0) {
                buf.append(", ");
            }
            Object value = i.next();
            buf.append(value + ": " + getCount(value));
        }
        return buf.toString();
    }
}
