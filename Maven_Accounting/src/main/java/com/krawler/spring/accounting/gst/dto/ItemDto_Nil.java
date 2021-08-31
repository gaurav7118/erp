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
public class ItemDto_Nil{
    private double nil_amt;                 //Total Nil rated outward supplies 	Decimal(11, 2)	1000.00
    private double expt_amt;                //Total Exempted outward supplies         Decimal(11, 2)	1100.00
    private double ngsup_amt;               //Total Non GST outward supplies          Decimal(11, 2)	1000.00
    private String sply_ty;                 //Supply Type	String (Length - 8)	INTRB2B/ INTRB2C/ INTRAB2B/ INTRAB2C

    public double getNil_amt() {
        return nil_amt;
    }

    public void setNil_amt(double nil_amt) {
        this.nil_amt = nil_amt;
    }

    public double getExpt_amt() {
        return expt_amt;
    }

    public void setExpt_amt(double expt_amt) {
        this.expt_amt = expt_amt;
    }

    public double getNgsup_amt() {
        return ngsup_amt;
    }

    public void setNgsup_amt(double ngsup_amt) {
        this.ngsup_amt = ngsup_amt;
    }

    public String getSply_ty() {
        return sply_ty;
    }

    public void setSply_ty(String sply_ty) {
        this.sply_ty = sply_ty;
    }
}