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
package com.krawler.esp.Search;

import java.util.ArrayList;

/**
 * A document summary dynamically generated to match a query.
 */
public class Summary {

    /**
     * A fragment of text within a summary.
     */
    public static class Fragment {

        private String text;

        /**
         * Constructs a fragment for the given text.
         */
        public Fragment(String text) {
            this.text = text;
        }

        /**
         * Returns the text of this fragment.
         */
        public String getText() {
            return text;
        }

        /**
         * Returns true iff this fragment is to be highlighted.
         */
        public boolean isHighlight() {
            return false;
        }

        /**
         * Returns true iff this fragment is an ellipsis.
         */
        public boolean isEllipsis() {
            return false;
        }

        /**
         * Returns an HTML representation of this fragment.
         */
        public String toString() {
            return Entities.encode(text);
        }
    }

    /**
     * A highlighted fragment of text within a summary.
     */
    public static class Highlight extends Fragment {

        /**
         * Constructs a highlighted fragment for the given text.
         */
        public Highlight(String text) {
            super(text);
        }

        /**
         * Returns true.
         */
        public boolean isHighlight() {
            return true;
        }

        /**
         * Returns an HTML representation of this fragment.
         */
        public String toString() {
            return "<b>" + super.toString() + "</b>";
        }
    }

    /**
     * An ellipsis fragment within a summary.
     */
    public static class Ellipsis extends Fragment {

        /**
         * Constructs an ellipsis fragment for the given text.
         */
        public Ellipsis() {
            super(" ... ");
        }

        /**
         * Returns true.
         */
        public boolean isEllipsis() {
            return true;
        }

        /**
         * Returns an HTML representation of this fragment.
         */
        public String toString() {
            return "<b> ... </b>";
        }
    }
    private ArrayList fragments = new ArrayList();
    private static final Fragment[] FRAGMENT_PROTO = new Fragment[0];

    /**
     * Constructs an empty Summary.
     */
    public Summary() {
    }

    /**
     * Adds a fragment to a summary.
     */
    public void add(Fragment fragment) {
        fragments.add(fragment);
    }

    /**
     * Returns an array of all of this summary's fragments.
     */
    public Fragment[] getFragments() {
        return (Fragment[]) fragments.toArray(FRAGMENT_PROTO);
    }

    /**
     * Returns an HTML representation of this fragment.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < fragments.size(); i++) {
            buffer.append(fragments.get(i));
        }
        return buffer.toString();
    }
}
