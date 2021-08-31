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

/**
 *
 * @author krawler
 */

package com.krawler.spring.accounting.vendorpayment;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.authHandler.authHandler;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class RepeatedPayment{
    private String id;
    private int intervalUnit;
    private String intervalType;  //Day, Week, Month
    private Date startDate;
    private Date nextDate;
    private Date expireDate;
    private int NoOfpaymentspost;   //Total Number of Payments Invoices 
    private int NoOfRemainpaymentspost; //Number of Recurring Payments Post. 
    private Date prevDate;
    private boolean isActivate;
    private boolean autoGenerateChequeNumber;
    private boolean ispendingapproval;  //Default true, So even user make recurring JE by mistake, it ll not recur.
    private String approver;    //Pending recurring JE Approval name

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public boolean isIspendingapproval() {
        return ispendingapproval;
    }

    public void setIspendingapproval(boolean ispendingapproval) {
        this.ispendingapproval = ispendingapproval;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public int getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(int intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public String getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(String intervalType) {
        this.intervalType = intervalType;
    }

    public Date getNextDate() {
        return nextDate;
    }

    public void setNextDate(Date nextDate) {
        this.nextDate = nextDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getNoOfRemainpaymentspost() {
        return NoOfRemainpaymentspost;
    }

    public void setNoOfRemainpaymentspost(int NoOfRemainpaymentspost) {
        this.NoOfRemainpaymentspost = NoOfRemainpaymentspost;
    }

    public int getNoOfpaymentspost() {
        return NoOfpaymentspost;
    }

    public void setNoOfpaymentspost(int NoOfpaymentspost) {
        this.NoOfpaymentspost = NoOfpaymentspost;
    }

    public Date getPrevDate() {
        return prevDate;
    }

    public void setPrevDate(Date prevDate) {
        this.prevDate = prevDate;
    }

    public boolean isIsActivate() {
        return isActivate;
    }

    public void setIsActivate(boolean isActivate) {
        this.isActivate = isActivate;
    }

    public boolean isAutoGenerateChequeNumber() {
        return autoGenerateChequeNumber;
    }

    public void setAutoGenerateChequeNumber(boolean autoGenerateChequeNumber) {
        this.autoGenerateChequeNumber = autoGenerateChequeNumber;
    }


    public static Date calculateNextDate(Date startDate, int intervalUnit, String intervalType) throws SessionExpiredException {
        Date nxtdate = null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        if (intervalType.equalsIgnoreCase("day")) {
            cal.add(Calendar.DATE, intervalUnit);
        } else if (intervalType.equalsIgnoreCase("week")) {
            cal.add(Calendar.DATE, intervalUnit * 7);
        } else if (intervalType.equalsIgnoreCase("month")) {
            cal.add(Calendar.MONTH, intervalUnit);
        }
        try{
            nxtdate = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(cal.getTime()));        
        }catch(ParseException pe){
            nxtdate = cal.getTime();
        }
        return nxtdate;
    }
}
