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
public class IMPGInvoiceData {

    private String boe_num;	//      Bill of Entry Number	Numeric (length:7)
    private String boe_dt;	//	Bill of Entry Date	string (DD-MM-YYYY)
    private double boe_val;	//	Bill of Entry Value	Decimal(15,2)
    private String is_sez;	//	flag to determine if it is sez or not	String(Max length:1)
    private String stin;	//	GSTIN/UID of the Supplier taxpayer(if is_sez = Y then only present)	Alphanumeric with 15 characters
    private String chksum;	//	Invoice Check sum value	string(Max length:64)
    private String port_code;	//	Port Code	string(length:6)
    private List<IMPGItem> itms;//      Bill Item Details

    public String getBoe_num() {
        return boe_num;
    }

    public void setBoe_num(String boe_num) {
        this.boe_num = boe_num;
    }

    public String getBoe_dt() {
        return boe_dt;
    }

    public void setBoe_dt(String boe_dt) {
        this.boe_dt = boe_dt;
    }

    public double getBoe_val() {
        return boe_val;
    }

    public void setBoe_val(double boe_val) {
        this.boe_val = boe_val;
    }

    public String getIs_sez() {
        return is_sez;
    }

    public void setIs_sez(String is_sez) {
        this.is_sez = is_sez;
    }

    public String getStin() {
        return stin;
    }

    public void setStin(String stin) {
        this.stin = stin;
    }

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }

    public String getPort_code() {
        return port_code;
    }

    public void setPort_code(String port_code) {
        this.port_code = port_code;
    }

    public List<IMPGItem> getItms() {
        return itms;
    }

    public void setItms(List<IMPGItem> itms) {
        this.itms = itms;
    }

}
