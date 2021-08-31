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

/**
 *
 * @author krawler
 */
public class IMPGItem {

    private double rt;          //      Tax Rate	Decimal(3,2)
    private double iamt;	//	IGST amount	Decimal(11,2)
    private double csamt;	//	CESS amount	Decimal(11,2)
    private String elg;         //	Eligibility for ITC	String(Max length:2)   ip/cp/no
    private double tx_i;	//	Total IGST available as ITC 	Decimal(11,2)
    private double tx_cs;	//	Total CESS available as ITC 	Decimal(11,2)
    private double txval;	//	Taxable value of Goods or Service as per invoice	Decimal(11,2)
    private int num;            //	Item Number	Integer

    public double getRt() {
        return rt;
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public double getIamt() {
        return iamt;
    }

    public void setIamt(double iamt) {
        this.iamt = iamt;
    }

    public double getCsamt() {
        return csamt;
    }

    public void setCsamt(double csamt) {
        this.csamt = csamt;
    }

    public String getElg() {
        return elg;
    }

    public void setElg(String elg) {
        this.elg = elg;
    }

    public double getTx_i() {
        return tx_i;
    }

    public void setTx_i(double tx_i) {
        this.tx_i = tx_i;
    }

    public double getTx_cs() {
        return tx_cs;
    }

    public void setTx_cs(double tx_cs) {
        this.tx_cs = tx_cs;
    }

    public double getTxval() {
        return txval;
    }

    public void setTxval(double txval) {
        this.txval = txval;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

}
