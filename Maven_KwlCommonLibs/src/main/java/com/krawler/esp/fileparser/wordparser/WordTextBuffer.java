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
package com.krawler.esp.fileparser.wordparser;

public class WordTextBuffer {

    StringBuffer _buf;
    boolean _hold;

    public WordTextBuffer() {
        _buf = new StringBuffer();
        _hold = false;
    }

    public void append(String text) {
        char[] letters = text.toCharArray();
        for (int x = 0; x < letters.length; x++) {
            switch (letters[x]) {
                case '\r':
                    _buf.append("\r\n");
                    break;
                case 0x13:
                    _hold = true;
                    break;
                case 0x14:
                    _hold = false;
                    break;
                default:
                    if (!_hold) {
                        _buf.append(letters[x]);
                    }
                    break;
            }
        }
    }

    public String toString() {
        return _buf.toString();
    }
}
