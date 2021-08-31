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
public class DocIssued{
    private char flag;      	//tax payer action	One Character(D)
    private String chksum;	//Invoice Check sum value 	string(Max length:64)
    List<DocDto> doc_det;

    public char getFlag() {
        return flag;
    }

    public void setFlag(char flag) {
        this.flag = flag;
    }

    public String getChksum() {
        return chksum;
    }

    public void setChksum(String chksum) {
        this.chksum = chksum;
    }

    public List<DocDto> getDoc_det() {
        return doc_det;
    }

    public void setDoc_det(List<DocDto> doc_det) {
        this.doc_det = doc_det;
    }
}