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

public class ArrayUtil {

    /**
     * Returns
     * <code>true</code> if the given array is
     * <code>null</code> or empty.
     */
    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    public boolean byteArrayContains(byte[] array, byte val) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == val) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param a1
     * @param a2
     * @return the UNION of the two byte arrays
     */
    public byte[] combineByteArraysNoDups(byte[] a1, byte[] a2) {
        byte[] tmp = new byte[a2.length];
        int tmpOff = 0;

        for (int i = a2.length - 1; i >= 0; i--) {
            if (!byteArrayContains(a1, a2[i])) {
                tmp[tmpOff++] = a2[i];
            }
        }

        byte[] toRet = new byte[tmpOff + a1.length];

        System.arraycopy(tmp, 0, toRet, 0, tmpOff);
        System.arraycopy(a1, 0, toRet, tmpOff, a1.length);

        return toRet;
    }

    public byte[] addToByteArrayNoDup(byte[] array, byte val) {
        if (!byteArrayContains(array, val)) {
            byte[] toRet = new byte[array.length + 1];
            System.arraycopy(array, 0, toRet, 0, array.length);
            toRet[toRet.length - 1] = val;
            return toRet;
        }
        return array;
    }

    /**
     * @param a1
     * @param a2
     * @return the INTERSECTION of the two byte arrays
     */
    public byte[] intersectByteArrays(byte[] a1, byte[] a2) {
        byte[] tmp = new byte[a1.length + a2.length];
        int tmpOff = 0;

        for (int i = a1.length - 1; i >= 0; i--) {
            if (byteArrayContains(a2, a1[i])) {
                tmp[tmpOff++] = a1[i];
            }
        }

        byte[] toRet = new byte[tmpOff];
        for (int i = 0; i < tmpOff; i++) {
            toRet[i] = tmp[i];
        }

        // FIXME testing code only!
        for (int i = toRet.length - 1; i >= 0; i--) {
            assert (byteArrayContains(a1, toRet[i]) && byteArrayContains(a2,
                    toRet[i]));
        }

        return toRet;
    }
}
