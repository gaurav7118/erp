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
package com.krawler.common.query;

public class FilterClause implements Clause {

    private static final String FILTER_STR = " where ";
    public static final int AND = 0;
    public static final int OR = 1;
    private StringBuffer queryString;

    public FilterClause(String queryStr) {
        this.queryString = new StringBuffer(queryStr);
    }

    public FilterClause addFilter(int mode, String queryStr) {
        switch (mode) {
            case AND:
                queryString.append(" and ").append(queryStr);
                break;
            case OR:
                queryString.append(" or ").append(queryStr);
                break;
        }
        return this;
    }

    public FilterClause addFilter(int mode, FilterClause clause) {
        switch (mode) {
            case AND:
                queryString.append(" and ").append(clause.queryString);
                break;
            case OR:
                queryString.append(" or ").append(clause.queryString);
                break;
        }
        return this;
    }

    public FilterClause wrap() {
        queryString.insert(0, '(').append(')');
        return this;
    }

    @Override
    public String getQueryString() {
        return FILTER_STR + queryString.toString();
    }

    public String getQueryString(String preStr) {
        return preStr + queryString.toString();
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public int compareTo(Clause o) {
        return getPriority() - o.getPriority();
    }
}
