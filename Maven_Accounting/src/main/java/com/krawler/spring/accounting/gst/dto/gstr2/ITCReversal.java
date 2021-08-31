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

import com.krawler.spring.accounting.gst.dto.ItemDetail;

/**
 *
 * @author krawler
 */
public class ITCReversal {

    private String chksum;              //Invoice Check sum value	string(Max length:64)
    private ItemDetail rule2_2;         //Amount in terms of Rule 2(2) of ITC Rules	Items
    private ItemDetail rule7_1_m;	//Amount in terms of rule 7 (1) (m)of ITC Rules	Items
    private ItemDetail rule8_1_h;	//Amount in terms of rule 8(1) (h) of the ITC Rules	Items
    private ItemDetail rule7_2_a;	//Amount in terms of rule 7 (2)(a) of ITC Rules	Items
    private ItemDetail rule7_2_b;	//Amount in terms of rule 7(2)(b) of ITC Rules	Items
    private ItemDetail revitc;          //On account of amount paid subsequent to reversal of ITC	Items
    private ItemDetail other;           //Any other liability (Pl specify)	Items

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }

    public ItemDetail getRule2_2() {
        return rule2_2;
    }

    public void setRule2_2(ItemDetail rule2_2) {
        this.rule2_2 = rule2_2;
    }

    public ItemDetail getRule7_1_m() {
        return rule7_1_m;
    }

    public void setRule7_1_m(ItemDetail rule7_1_m) {
        this.rule7_1_m = rule7_1_m;
    }

    public ItemDetail getRule8_1_h() {
        return rule8_1_h;
    }

    public void setRule8_1_h(ItemDetail rule8_1_h) {
        this.rule8_1_h = rule8_1_h;
    }

    public ItemDetail getRule7_2_a() {
        return rule7_2_a;
    }

    public void setRule7_2_a(ItemDetail rule7_2_a) {
        this.rule7_2_a = rule7_2_a;
    }

    public ItemDetail getRule7_2_b() {
        return rule7_2_b;
    }

    public void setRule7_2_b(ItemDetail rule7_2_b) {
        this.rule7_2_b = rule7_2_b;
    }

    public ItemDetail getRevitc() {
        return revitc;
    }

    public void setRevitc(ItemDetail revitc) {
        this.revitc = revitc;
    }

    public ItemDetail getOther() {
        return other;
    }

    public void setOther(ItemDetail other) {
        this.other = other;
    }

}
