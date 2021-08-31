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
/**
 *
 * @author krawler
 */
public class B2CS{
    private String chksum;          //Invoice Check sum value string(Max length:64)
    private char flag;              //tax payer action	One Character(D)	D-Delete (For deleting invoices)
    private String sply_ty;         //Supply Type	String(Max length:5)	INTER/INTRA
    private double txval;           //Taxable value of Goods or Service as per invoice.            "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private String typ = "";             //Type 	string(Max length:2)	E/OE (Ecom/Other than Ecom)
    private String etin="";             //Ecom Operator Gstin	Alphanumeric (Max length:15)	e.g-27AHQPA7588L1ZJ
    private String pos;             //Maintained in GST System common database POS as provided in law / actual provision of service.       "type": "string",      "pattern": "^(3[0-7]|[12][0-9]|0[1-9]|99)$",      "maxLength": 2,      "minLength": 2
    private float rt;               //Rate. description : "rate" type : "number" multipleOf : 0.01  Decimal(3, 2)
    private double iamt;            //IGST Amount as per invoice       "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private double camt;            //IGST Amount as per invoice       "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private double samt;            //IGST Amount as per invoice       "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private double csamt;           //IGST Amount as per invoice       "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private int stcode;            //State Code 
    private String inum = "";   // Invoice number
    private String invid = "";  // Invoice UUID
    private String doctype = "";  // DOC type i.e. CI or CN
    private String gstin = "";  // GSTIN for customer
    private String customerName="";
    private String idt;  // Invoice Date
    public static final String DOCTYPE_SALES="Sales";
    public static final String DOCTYPE_CN="Credit Note";
    public static final String DOCTYPE_CSR="Cash Sales Refund";
    public static final String DOCTYPE_DN="Debit Note";

    public String getIdt() {
        return idt;
    }

    public void setIdt(String idt) {
        this.idt = idt;
    }

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public String getInvid() {
        return invid;
    }

    public void setInvid(String invid) {
        this.invid = invid;
    }

    public String getDoctype() {
        return doctype;
    }

    public void setDoctype(String doctype) {
        this.doctype = doctype;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public int getStcode() {
        return stcode;
    }

    public void setStcode(int stcode) {
        this.stcode = stcode;
    }
    
    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }
    
    public char getFlag() {
        return flag;
    }

    public void setFlag(char flag) {
        this.flag = flag;
    }

    public String getSply_ty() {
        return sply_ty;
    }

    public void setSply_ty(String sply_ty) {
        this.sply_ty = sply_ty;
    }

    public double getTxval() {
        return txval;
    }

    public void setTxval(double txval) {
        this.txval = txval;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getEtin() {
        return etin;
    }

    public void setEtin(String etin) {
        this.etin = etin;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public float getRt() {
        return rt;
    }

    public void setRt(float rt) {
        this.rt = rt;
    }

    public double getIamt() {
        return iamt;
    }

    public void setIamt(double iamt) {
        this.iamt = iamt;
    }

    public double getCamt() {
        return camt;
    }

    public void setCamt(double camt) {
        this.camt = camt;
    }

    public double getSamt() {
        return samt;
    }

    public void setSamt(double samt) {
        this.samt = samt;
    }

    public double getCsamt() {
        return csamt;
    }

    public void setCsamt(double csamt) {
        this.csamt = csamt;
    }
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}