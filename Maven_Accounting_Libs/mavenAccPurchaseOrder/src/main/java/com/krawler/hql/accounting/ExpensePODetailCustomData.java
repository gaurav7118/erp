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

package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class ExpensePODetailCustomData extends AccCustomData{
    private String expensePODetailID;
    private ExpensePODetail expensePODetail;
    private String moduleId;
    private String recdetailId;

    public ExpensePODetail getExpensePODetail() {
        return expensePODetail;
    }

    public void setExpensePODetail(ExpensePODetail expensePODetail) {
        this.expensePODetail = expensePODetail;
    }

    public String getExpensePODetailID() {
        return expensePODetailID;
    }

    public void setExpensePODetailID(String expensePODetailID) {
        this.expensePODetailID = expensePODetailID;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getRecdetailId() {
        return recdetailId;
    }

    public void setRecdetailId(String recdetailId) {
        this.recdetailId = recdetailId;
    }
}
