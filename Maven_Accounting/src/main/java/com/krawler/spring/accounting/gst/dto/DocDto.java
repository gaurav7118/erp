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
public class DocDto{
    private int doc_num;                //Serial no type : "integer", minLength : 1
    /**
     *  Table 1.1.2 A : Document Table	
        Doc Num.	Nature of document

        1	Invoices for outward supply
        2	Invoices for inward supply from unregistered person 
        3	Revised Invoice
        4	Debit Note
        5	Credit Note
        6	Receipt voucher
        7	Payment Voucher
        8	Refund voucher
        9	Delivery Challan for job work
        10	Delivery Challan for supply on approval
        11	Delivery Challan in case of liquid gas
        12	Delivery Challan in cases other than by way of supply (excluding at S no. 9 to 11) 
    */
    private List<Docs> docs;    

    public int getDoc_num() {
        return doc_num;
    }

    public void setDoc_num(int doc_num) {
        this.doc_num = doc_num;
    }

    public List<Docs> getDocs() {
        return docs;
    }

    public void setDocs(List<Docs> docs) {
        this.docs = docs;
    }
}