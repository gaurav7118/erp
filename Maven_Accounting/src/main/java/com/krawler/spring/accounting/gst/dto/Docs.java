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
public class Docs {

    private int num;        //Serial Number	Integer
    private String from;    //From serial number	String(Max length: 16)
    private String to;      //To serial number	String(Max length: 16)
    private int totnum;     //Total Number	Integer
    private int cancel;     //Cancelled	Integer
    private int net_issue;  //Net issued	Integer

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getTotnum() {
        return totnum;
    }

    public void setTotnum(int totnum) {
        this.totnum = totnum;
    }

    public int getCancel() {
        return cancel;
    }

    public void setCancel(int cancel) {
        this.cancel = cancel;
    }

    public int getNet_issue() {
        return net_issue;
    }

    public void setNet_issue(int net_issue) {
        this.net_issue = net_issue;
    }

}
