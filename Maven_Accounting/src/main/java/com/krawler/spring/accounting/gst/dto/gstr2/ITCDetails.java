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
public class ITCDetails {

    private double tx_i;	//Total Tax available as ITC IGST Amount	Decimal(11,2)
    private double tx_c;	//Total Tax available as ITC CGST Amount	Decimal(11,2)	147.80
    private double tx_s;	//Total Tax available as ITC SGST Amount	Decimal(11,2)	156.90
    private double tx_cs;	//Total Tax available as ITC CESS Amount	Decimal(11,2)	156.90
    private String elg;         //Eligibility	String(Max length : 2)	ip/cp/is/no

    public double getTx_i() {
        return tx_i;
    }

    public void setTx_i(double tx_i) {
        this.tx_i = tx_i;
    }
    
    public double getTx_c() {
        return tx_c;
    }

    public void setTx_c(double tx_c) {
        this.tx_c = tx_c;
    }

    public double getTx_s() {
        return tx_s;
    }

    public void setTx_s(double tx_s) {
        this.tx_s = tx_s;
    }

    public double getTx_cs() {
        return tx_cs;
    }

    public void setTx_cs(double tx_cs) {
        this.tx_cs = tx_cs;
    }

    public String getElg() {
        return elg;
    }

    public void setElg(String elg) {
        this.elg = elg;
    }

}
