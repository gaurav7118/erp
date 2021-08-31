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
package com.krawler.spring.auditTrailModule;

import java.util.List;

public class AuditIndexData {

    private List<String> indexNames;
    private List<Object> indexValues;
    private String indexPath;

    public AuditIndexData(List<String> indexNames, List<Object> indexValues, String indexPath) {
        this.indexNames = indexNames;
        this.indexValues = indexValues;
        this.indexPath = indexPath;
    }

    public List<String> getIndexNames() {
        return indexNames;
    }

    public String getIndexPath() {
        return indexPath;
    }

    public List<Object> getIndexValues() {
        return indexValues;
    }
    
    
}
