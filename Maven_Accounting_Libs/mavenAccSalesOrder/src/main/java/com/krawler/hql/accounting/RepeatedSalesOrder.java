/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.authHandler.authHandler;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class RepeatedSalesOrder {

    private String id;
    private int intervalUnit;
    private String intervalType;  //Day, Week, Month
    private Date startDate;
    private Date nextDate;
    private Date expireDate;
    private int NoOfSOpost;   //Total Number of Recurring SO 
    private int NoOfRemainSOpost; //Number of Recurring SO Post. 
    private boolean isActivate; //Default True, mean recurring SO in active mode.
    private boolean ispendingapproval;  //Default true, So even user make recurring SO by mistake, it ll not recur.
    private String approver;    //Pending recurring SO Approval name
    private Date prevDate;

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

    public int getNoOfRemainSOpost() {
        return NoOfRemainSOpost;
    }

    public void setNoOfRemainSOpost(int NoOfRemainSOpost) {
        this.NoOfRemainSOpost = NoOfRemainSOpost;
    }

    public int getNoOfSOpost() {
        return NoOfSOpost;
    }

    public void setNoOfSOpost(int NoOfSOpost) {
        this.NoOfSOpost = NoOfSOpost;
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
