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
package com.krawler.spring.accounting.gst.dto.gstr1;

import java.util.List;

/**
 *
 * @author krawler
 */
public class GSTR_Summary {

    private String sec_nm;	//Return Section	String (Max length:5)
    private String ctin;	//Supplier TIN for B2B & CDN/ISD TIN for ISD/deductor TIN for TDS/ Ecommerce Portal TIN for TCS/	Alphanumeric with 15 char

    private String chksum;	//Invoice Check sum value 	String (Max length:64)
    private int ttl_rec;	//Total Record Count	Number
    private double ttl_val;	//Total records value	Decimal(15, 2)
    private double ttl_igst;	//	Total IGST 	Decimal(15, 2)
    private double ttl_cgst;	//	Total CGST 	Decimal(15, 2)
    private double ttl_sgst;	//	Total SGST 	Decimal(15, 2)
    private double ttl_cess;	//	Total Cess	Decimal(15, 2)
    private double ttl_tax;	//	Total taxable value of records	Decimal(15, 2)
    
    //GSTR2
    private double ttl_txpd_igst;	//TTotal Tax paid IGST	Decimal(15,2)
    private double ttl_txpd_sgst;	//TTotal Tax paid SGST	Decimal(15,2)
    private double ttl_txpd_cgst;	//TTotal Tax paid CGST	Decimal(15,2)
    private double ttl_txpd_cess;	//TTotal Tax paid CESS	Decimal(15,2)
    private double ttl_itcavld_igst;	//TITC Availed IGST	Decimal(15,2)
    private double ttl_itcavld_sgst;	//TITC Availed SGST	Decimal(15,2)
    private double ttl_itcavld_cgst;	//TITC Availed CGST	Decimal(15,2)
    private double ttl_itcavld_cess;	//TITC Availed CESS	Decimal(15,2)
    
    //State Code Summary
    private String state_cd;        //State Code	string(Max length:2)
    
    private List<GSTR_Summary> cpty_sum;     //Counter Party Summary

    public String getSec_nm() {
        return sec_nm;
    }

    public void setSec_nm(String sec_nm) {
        this.sec_nm = sec_nm;
    }

    public String getCtin() {
        return ctin;
    }

    public void setCtin(String ctin) {
        this.ctin = ctin;
    }

    public double getTtl_txpd_igst() {
        return ttl_txpd_igst;
    }

    public void setTtl_txpd_igst(double ttl_txpd_igst) {
        this.ttl_txpd_igst = ttl_txpd_igst;
    }

    public double getTtl_txpd_sgst() {
        return ttl_txpd_sgst;
    }

    public void setTtl_txpd_sgst(double ttl_txpd_sgst) {
        this.ttl_txpd_sgst = ttl_txpd_sgst;
    }

    public double getTtl_txpd_cgst() {
        return ttl_txpd_cgst;
    }

    public void setTtl_txpd_cgst(double ttl_txpd_cgst) {
        this.ttl_txpd_cgst = ttl_txpd_cgst;
    }

    public double getTtl_txpd_cess() {
        return ttl_txpd_cess;
    }

    public void setTtl_txpd_cess(double ttl_txpd_cess) {
        this.ttl_txpd_cess = ttl_txpd_cess;
    }

    public double getTtl_itcavld_igst() {
        return ttl_itcavld_igst;
    }

    public void setTtl_itcavld_igst(double ttl_itcavld_igst) {
        this.ttl_itcavld_igst = ttl_itcavld_igst;
    }

    public double getTtl_itcavld_sgst() {
        return ttl_itcavld_sgst;
    }

    public void setTtl_itcavld_sgst(double ttl_itcavld_sgst) {
        this.ttl_itcavld_sgst = ttl_itcavld_sgst;
    }

    public double getTtl_itcavld_cgst() {
        return ttl_itcavld_cgst;
    }

    public void setTtl_itcavld_cgst(double ttl_itcavld_cgst) {
        this.ttl_itcavld_cgst = ttl_itcavld_cgst;
    }

    public double getTtl_itcavld_cess() {
        return ttl_itcavld_cess;
    }

    public void setTtl_itcavld_cess(double ttl_itcavld_cess) {
        this.ttl_itcavld_cess = ttl_itcavld_cess;
    }

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }

    public int getTtl_rec() {
        return ttl_rec;
    }

    public void setTtl_rec(int ttl_rec) {
        this.ttl_rec = ttl_rec;
    }

    public double getTtl_val() {
        return ttl_val;
    }

    public void setTtl_val(double ttl_val) {
        this.ttl_val = ttl_val;
    }

    public double getTtl_igst() {
        return ttl_igst;
    }

    public void setTtl_igst(double ttl_igst) {
        this.ttl_igst = ttl_igst;
    }

    public double getTtl_cgst() {
        return ttl_cgst;
    }

    public void setTtl_cgst(double ttl_cgst) {
        this.ttl_cgst = ttl_cgst;
    }

    public double getTtl_sgst() {
        return ttl_sgst;
    }

    public void setTtl_sgst(double ttl_sgst) {
        this.ttl_sgst = ttl_sgst;
    }

    public double getTtl_cess() {
        return ttl_cess;
    }

    public void setTtl_cess(double ttl_cess) {
        this.ttl_cess = ttl_cess;
    }

    public double getTtl_tax() {
        return ttl_tax;
    }

    public void setTtl_tax(double ttl_tax) {
        this.ttl_tax = ttl_tax;
    }

    public String getState_cd() {
        return state_cd;
    }

    public void setState_cd(String state_cd) {
        this.state_cd = state_cd;
    }
    
    public List<GSTR_Summary> getCpty_sum() {
        return cpty_sum;
    }

    public void setCpty_sum(List<GSTR_Summary> cpty_sum) {
        this.cpty_sum = cpty_sum;
    }

}
