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

package com.krawler.spring.accounting.gst.dto;

import java.util.List;

/**
 *
 * @author krawler
 */
public class B2B{
    private String ctin;            //GSTIN/UID of the Receiver taxpayer/UN, Govt Bodies	Alphanumeric with 15 characters
    private char cfs;               //GSTR2 filing status of counter party	Character (Y/N)
    private List<InvoiceDto> inv;

    public String getCtin() {
        return ctin;
    }

    public void setCtin(String ctin) {
        this.ctin = ctin;
    }

    public char getCfs() {
        return cfs;
    }

    public void setCfs(char cfs) {
        this.cfs = cfs;
    }
    
    public List<InvoiceDto> getInv() {
        return inv;
    }

    public void setInv(List<InvoiceDto> inv) {
        this.inv = inv;
    }
}