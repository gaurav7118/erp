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

public class OrderClause implements Clause {

    private static final String ORDER_STR = " order by ";
    StringBuffer queryString;

    public OrderClause(String sortField) {
        this(sortField, false);
    }

    public OrderClause(String sortField, boolean reverse) {
        queryString = new StringBuffer();
        doAppend(sortField, reverse);
    }

    private void doAppend(String sortField, boolean reverse) {
        queryString.append(sortField);
        if (reverse) {
            queryString.append(" DESC");
        }
    }

    public void addOrder(String sortField, boolean reverse) {
        queryString.append(',');
        doAppend(sortField, reverse);
    }

    @Override
    public String getQueryString() {
        return ORDER_STR + queryString.toString();
    }

    @Override
    public int getPriority() {
        return 8;
    }

    @Override
    public int compareTo(Clause o) {
        return getPriority() - o.getPriority();
    }
}
