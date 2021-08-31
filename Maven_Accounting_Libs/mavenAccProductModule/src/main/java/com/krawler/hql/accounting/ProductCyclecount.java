///*
// * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
// * All rights reserved.
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 2
// * of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// */
//package com.krawler.hql.accounting;
//
//import com.krawler.common.admin.Company;
//import java.util.Date;
//import java.util.Set;
//
///**
// *
// * @author gaurav
// */
//public class ProductCyclecount {
//
//    private String ID;
//    private Date prevDate;
//    private Date nextDate;
//    private Product product;
//    private int status;
//    private int countInterval;
//    private int tolerance;
//
//    public int getTolerance() {
//        return tolerance;
//    }
//
//    public void setTolerance(int tolerance) {
//        this.tolerance = tolerance;
//    }
//
//    public int getCountInterval() {
//        return countInterval;
//    }
//
//    public void setCountInterval(int countInterval) {
//        this.countInterval = countInterval;
//    }
//
//    public int getStatus() {
//        return status;
//    }
//
//    public void setStatus(int status) {
//        this.status = status;
//    }
//
//    public String getID() {
//        return ID;
//    }
//
//    public void setID(String ID) {
//        this.ID = ID;
//    }
//
//    public Date getNextDate() {
//        if (nextDate == null) {
//            nextDate = new Date(0, 0, 1);
//        }
//        return nextDate;
//    }
//
//    public void setNextDate(Date nextDate) {
//        this.nextDate = nextDate;
//    }
//
//    public Date getPrevDate() {
//        if (prevDate == null) {
//            prevDate = new Date(0, 0, 1);
//        }
//        return prevDate;
//    }
//
//    public void setPrevDate(Date prevDate) {
//        this.prevDate = prevDate;
//    }
//
//    public Product getProduct() {
//        return product;
//    }
//
//    public void setProduct(Product product) {
//        this.product = product;
//    }
//}
