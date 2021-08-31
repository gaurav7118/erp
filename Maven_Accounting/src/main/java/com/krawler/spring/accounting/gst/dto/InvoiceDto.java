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
public class InvoiceDto{
    private char flag;              //Invoice Status One Character(A/R/N/U/P/D)- A-Accepted ,R-Rejected, N-No action ,U-Uploaded, P-Pending, D-Delete . The flag shows the invoice status from the supplier perspective
    private char updby;             //Uploaded by.  "R/S" S- Supplier, R- Receiver
    private String chksum;          //Invoice Check sum value. String (Max length:64)
    private String inum = "";            //Supplier Invoice Number       "maxLength": 16,      "minLength": 1,      "pattern": "^[a-zA-Z0-9-/]+$"
    private String idt;             //Supplier Invoice Date    string (DD-MM-YYYY)  "type": "string",      "minLength": 1,      "pattern": "^((0[1-9]|[12][0-9]|3[01])[-](0[1-9]|1[012])[-]((19|20)\\d\\d))$"
    private double val;             //Supplier Invoice Value      Decimal(15,2) "multipleOf": 0.01,      "maximum": 1E+15,      "minimum": 0
    private String pos;             //Maintained in GST System common database POS as provided in law / actual provision of service.       "type": "string",      "pattern": "^(3[0-7]|[12][0-9]|0[1-9]|99)$",      "maxLength": 2,      "minLength": 2
    private char rchrg = 'N' ;             //Reverse Charge       "type": "string",      "enum": ["Y","N"]
    private String etin="";            //Ecom Tin       "maxLength": 15,      "minLength": 15,      "pattern": "[0-9]{2}[a-zA-Z]{5}[0-9]{4}[a-zA-Z]{1}[1-9A-Za-z]{1}[C]{1}[0-9a-zA-Z]{1}"
    private String inv_typ ="";         //Invoice type   String (Max length: 2)  "enum": ["R","DE","SEWP","SEWOP"] R- Regular B2B Invoices,  DE – Deemed Exports, SEWP – SEZ Exports with payment,SEWOP – SEZ exports without payment
    private char cflag;             //Counter Party Flag. One Character(A/R/M/N/U/P) . A-Accepted ,R-Rejected, M-Modified,N-No action ,U-Uploaded, P-Pending
    private String opd;             //Original Period,  MMYYYY
    private int stcode;            //State Code 
    private String export_type="";
    private String supplierinvoiceno;
    private String customerName="";

  
   //Credit/Debit Invoices
    private String ntty;          //Credit/debit note type/ Refund Voucher	One character.  C/ D/ R
    private String nt_num;      //Credit note/debit note /  Refund Voucher Number	Alphanumeric(max length:30)
    private String nt_dt;	//Credit Note/Debit Note/  Refund Voucher date	string (DD-MM-YYYY)
    private String rsn ="";         //Reason for Issuing Dr./ Cr. Notes	String(Max length:50) . Post Sale Discount
    private char p_gst = 'N';         //Pre GST Regime Dr./ Cr. Notes	One character(Y/ N)
  
    //EXP
    private String sbpcode="";             //Shipping Bill Port Code	Alphanumeric (Max length:6)
    private String sbnum;               //Shipping Bill No. or Bill of Export No	Integer (Max length:7)
    private String sbdt="";                //Shipping Bill Date. or Bill of Export Date	string (DD-MM-YYYY)

    //Programming keys
    private double inv_iamt;
    private double inv_camt;
    private double inv_samt;
    private double inv_csamt;
    private String invoiceId;
    private String vendorname;

    public String getVendorname() {
        return vendorname;
    }

    public void setVendorname(String vendorname) {
        this.vendorname = vendorname;
    }
    
    private List<ItemDto> itms;
    private String gstrsubmissionid;
    private String submissionstatus;
    public char getFlag() {
        return flag;
    }

    public void setFlag(char flag) {
        this.flag = flag;
    }

    public char getUpdby() {
        return updby;
    }

    public void setUpdby(char updby) {
        this.updby = updby;
    }

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
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

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public char getRchrg() {
        return rchrg;
    }

    public void setRchrg(char rchrg) {
        this.rchrg = rchrg;
    }

    public String getEtin() {
        return etin;
    }

    public void setEtin(String etin) {
        this.etin = etin;
    }

    public String getInv_typ() {
        return inv_typ;
    }

    public void setInv_typ(String inv_typ) {
        this.inv_typ = inv_typ;
    }

    public char getCflag() {
        return cflag;
    }

    public void setCflag(char cflag) {
        this.cflag = cflag;
    }

    public String getOpd() {
        return opd;
    }

    public void setOpd(String opd) {
        this.opd = opd;
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

    public String getSbpcode() {
        return sbpcode;
    }

    public void setSbpcode(String sbpcode) {
        this.sbpcode = sbpcode;
    }

    public String getSbnum() {
        return sbnum;
    }

    public void setSbnum(String sbnum) {
        this.sbnum = sbnum;
    }

    public String getSbdt() {
        return sbdt;
    }

    public void setSbdt(String sbdt) {
        this.sbdt = sbdt;
    }
    
    public List<ItemDto> getItms() {
        return itms;
    }

    public void setItms(List<ItemDto> itms) {
        this.itms = itms;
    }

    public double getInv_iamt() {
        return inv_iamt;
}

    public void setInv_iamt(double inv_iamt) {
        this.inv_iamt = inv_iamt;
    }

    public double getInv_camt() {
        return inv_camt;
    }

    public void setInv_camt(double inv_camt) {
        this.inv_camt = inv_camt;
    }

    public double getInv_samt() {
        return inv_samt;
    }

    public void setInv_samt(double inv_samt) {
        this.inv_samt = inv_samt;
    }

    public double getInv_csamt() {
        return inv_csamt;
    }

    public void setInv_csamt(double inv_csamt) {
        this.inv_csamt = inv_csamt;
    } 
    public int getStcode() {
        return stcode;
    }

    public void setStcode(int stcode) {
        this.stcode = stcode;
    }

    public String getExport_type() {
        return export_type;
    }

    public void setExport_type(String export_type) {
        this.export_type = export_type;
    }

    public String getSupplierinvoiceno() {
        return supplierinvoiceno;
    }

    public void setSupplierinvoiceno(String supplierinvoiceno) {
        this.supplierinvoiceno = supplierinvoiceno;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getGstrsubmissionid() {
        return gstrsubmissionid;
    }

    public void setGstrsubmissionid(String gstrsubmissionid) {
        this.gstrsubmissionid = gstrsubmissionid;
    }

    public String getSubmissionstatus() {
        return submissionstatus;
    }

    public void setSubmissionstatus(String submissionstatus) {
        this.submissionstatus = submissionstatus;
    }
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

}
