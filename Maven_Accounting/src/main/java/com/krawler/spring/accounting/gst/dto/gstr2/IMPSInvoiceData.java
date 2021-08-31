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
package com.krawler.spring.accounting.gst.dto.gstr2;

import java.util.List;

/**
 *
 * @author krawler
 */
public class IMPSInvoiceData {

    private String inum;	//      Invoice No.	Numeric (Max length:16)
    private String idt;	//	Invoice Date	string (DD-MM-YYYY)
    private double ival;	//	Invoice Value	Decimal(15,2)
    private String chksum;	//	Invoice Check sum value	string(Max length:64)
    private String pos;	//	Point of sale	string(Max length:2)
    private List<IMPGItem> itms;//      Bill Item Details

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public String getIdt() {
        return idt;
    }

    public void setIdt(String idt) {
        this.idt = idt;
    }

    public double getIval() {
        return ival;
    }

    public void setIval(double ival) {
        this.ival = ival;
    }

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public List<IMPGItem> getItms() {
        return itms;
    }

    public void setItms(List<IMPGItem> itms) {
        this.itms = itms;
    }

}
