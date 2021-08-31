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
package com.krawler.common.admin;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.authHandler.authHandler;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class RepeatedInvoices {

    private String id;
    private int intervalUnit;
    private String intervalType;  //Day, Week, Month
    private Date startDate;
    private Date nextDate;
    private Date expireDate;
    private int NoOfInvoicespost;   //Total Number of Recurring Invoices 
    private int NoOfRemainInvoicespost; //Number of Recurring Invoices Post. 
    private boolean isActivate; //Default True, mean recurring Invoice in active mode.
    private boolean ispendingapproval;  //Default true, So even user make recurring Invoice by mistake, it ll not recur.
    private String approver;    //Pending recurring Invoice Approval name
    private Date prevDate; //Notification mail ll send on previous date
    
    private boolean allowToEditRecurredDocument;  //Default false, to allow user to edit recurred document.
    private User approverOfEditedrecurredInvoice; // to store approver of edited recurring documet
    private Date invoiceAdvanceCreationDate; //Invoice will be created on this date ERM-87
    private int advanceNoofdays; //Invoice will be created on these many days in advance ERM-87

    public int getAdvanceNoofdays() {
        return advanceNoofdays;
    }

    public void setAdvanceNoofdays(int advanceNoofdays) {
        this.advanceNoofdays = advanceNoofdays;
    }

    public Date getInvoiceAdvanceCreationDate() {
        return invoiceAdvanceCreationDate;
    }

    public void setInvoiceAdvanceCreationDate(Date invoiceAdvanceCreationDate) {
        this.invoiceAdvanceCreationDate = invoiceAdvanceCreationDate;
    }

    public boolean isAllowToEditRecurredDocument() {
        return allowToEditRecurredDocument;
    }

    public void setAllowToEditRecurredDocument(boolean allowToEditRecurredDocument) {
        this.allowToEditRecurredDocument = allowToEditRecurredDocument;
    }

    public User getApproverOfEditedrecurredInvoice() {
        return approverOfEditedrecurredInvoice;
    }

    public void setApproverOfEditedrecurredInvoice(User approverOfEditedrecurredInvoice) {
        this.approverOfEditedrecurredInvoice = approverOfEditedrecurredInvoice;
    }
    
    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public boolean isIsActivate() {
        return isActivate;
    }

    public void setIsActivate(boolean isActivate) {
        this.isActivate = isActivate;
    }

    public boolean isIspendingapproval() {
        return ispendingapproval;
    }

    public void setIspendingapproval(boolean ispendingapproval) {
        this.ispendingapproval = ispendingapproval;
    }

    public Date getPrevDate() {
        return prevDate;
    }

    public void setPrevDate(Date prevDate) {
        this.prevDate = prevDate;
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

    public int getNoOfInvoicespost() {
        return NoOfInvoicespost;
    }

    public void setNoOfInvoicespost(int NoOfInvoicespost) {
        this.NoOfInvoicespost = NoOfInvoicespost;
    }

    public int getNoOfRemainInvoicespost() {
        return NoOfRemainInvoicespost;
    }

    public void setNoOfRemainInvoicespost(int NoOfRemainInvoicespost) {
        this.NoOfRemainInvoicespost = NoOfRemainInvoicespost;
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