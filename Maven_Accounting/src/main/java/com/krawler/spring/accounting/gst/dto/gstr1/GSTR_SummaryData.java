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
public class GSTR_SummaryData {

    private String gstin;       //GSTIN of the taxpayer	Alphanumeric with 15 characters
    private String ret_period;	//Return Period	MMYYYY
    private String chksum;      //Invoice Check sum value 	String (Max length:64)
    private char summ_typ;      //Short / long Summary	Character (S/ L)
    private List<GSTR_Summary> sec_sum;   //section_summary

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getRet_period() {
        return ret_period;
    }

    public void setRet_period(String ret_period) {
        this.ret_period = ret_period;
    }

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }

    public char getSumm_typ() {
        return summ_typ;
    }

    public void setSumm_typ(char summ_typ) {
        this.summ_typ = summ_typ;
    }

    public List<GSTR_Summary> getSec_sum() {
        return sec_sum;
    }

    public void setSec_sum(List<GSTR_Summary> sec_sum) {
        this.sec_sum = sec_sum;
    }
}
