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
public class NilRatedData{
    private String chksum;  //Invoice Check sum value 	string(Max length:64)
    private NILItem inter;  //Inter state supplies	Items
    private NILItem intra;  //Inter state supplies	Items

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }

    public NILItem getInter() {
        return inter;
    }

    public void setInter(NILItem inter) {
        this.inter = inter;
    }

    public NILItem getIntra() {
        return intra;
    }

    public void setIntra(NILItem intra) {
        this.intra = intra;
    }
    
}