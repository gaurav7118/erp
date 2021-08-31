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
public class RepeatedJE {

    private String id;
    private int intervalUnit;
    private String intervalType;  //Day, Week, Month
    private Date startDate;
    private Date nextDate;
    private Date expireDate;
    private int NoOfJEpost;   //Total Number of Recurring JE 
    private int NoOfRemainJEpost; //Number of Recurring JE Post. 
    private boolean isActivate; //Default True, mean recurring JE in active mode.
    private boolean ispendingapproval;  //Default true, So even user make recurring JE by mistake, it ll not recur.
    private String approver;    //Pending recurring JE Approval name
    private Date prevDate; //Notification mail ll send on previous date
    private boolean autoGenerateChequeNumber;
    
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

    public int getNoOfRemainJEpost() {
        return NoOfRemainJEpost;
    }

    public void setNoOfRemainJEpost(int NoOfRemainJEpost) {
        this.NoOfRemainJEpost = NoOfRemainJEpost;
    }

    public int getNoOfJEpost() {
        return NoOfJEpost;
    }

    public void setNoOfJEpost(int NoOfJEpost) {
        this.NoOfJEpost = NoOfJEpost;
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
