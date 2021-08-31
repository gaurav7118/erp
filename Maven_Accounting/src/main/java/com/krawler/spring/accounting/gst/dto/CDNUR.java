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
public class CDNUR {

    private String chksum;      //Invoice Check sum value 	String (Max length:64)
    private String typ="";         //EXPWP/ EXPWOP/ B2CL	String (Max length: 6)
    private String ntty;	//Credit/debit note type/ Refund Voucher	One character
    private String nt_num;	//redit note/debit note /  Refund Voucher Number	Alphanumeric(max length: 16)
    private String nt_dt;	//Credit Note/Debit Note/  Refund Voucher date	string (DD-MM-YYYY)
    private String rsn ="";    	//Reason for Issuing Dr./ Cr. Notes	String(Max length:50)
    private char p_gst;	//Pre GST Regime Dr./ Cr. Notes	One character(Y/ N)
    private String inum;	//Original invoice number	String(Max length:16)
    private String idt;	//Invoice date	string (DD-MM-YYYY)
    private double val;	//Total Note Value	Decimal(15, 2)
    private int stcode;
    List<ItemDto> itms;
    private String pos; 
    private String customerName="";// Place of supply i.e. billing address

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }
    
    //GSTR2
    private String rtin;        //Receiver Gstin	Alphanumeric with 15 characters  (Optional)

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getNtty() {
        return ntty;
    }

    public void setNtty(String ntty) {
        this.ntty = ntty;
    }

    public String getNt_num() {
        return nt_num;
    }

    public void setNt_num(String nt_num) {
        this.nt_num = nt_num;
    }

    public String getNt_dt() {
        return nt_dt;
    }

    public void setNt_dt(String nt_dt) {
        this.nt_dt = nt_dt;
    }

    public String getRsn() {
        return rsn;
    }

    public void setRsn(String rsn) {
        this.rsn = rsn;
    }

    public char getP_gst() {
        return p_gst;
    }

    public void setP_gst(char p_gst) {
        this.p_gst = p_gst;
    }

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

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public List<ItemDto> getItms() {
        return itms;
    }

    public void setItms(List<ItemDto> itms) {
        this.itms = itms;
    }

    public String getRtin() {
        return rtin;
    }

    public void setRtin(String rtin) {
        this.rtin = rtin;
    }
    
    public int getStcode() {
        return stcode;
    }

    public void setStcode(int stcode) {
        this.stcode = stcode;
    }
    
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
