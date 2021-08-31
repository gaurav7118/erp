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

import com.krawler.spring.accounting.gst.dto.gstr2.ITCDetails;

/**
 *
 * @author krawler
 */
public class ItemDto{
    private int num;                //Serial no type : "integer", minLength : 1
    private ITCDetails itc;         //itc Details
    private ItemDetail itm_det;     //Item Details

    public int getNum() {
        return num;
    }

    public ITCDetails getItc() {
        return itc;
    }

    public void setItc(ITCDetails itc) {
        this.itc = itc;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public ItemDetail getItm_det() {
        return itm_det;
    }

    public void setItm_det(ItemDetail itm_det) {
        this.itm_det = itm_det;
    }
}