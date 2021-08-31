/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class OpeningBalanceMakePaymentCustomData extends AccCustomData{
    private String OpeningBalanceMakePaymentId;
    private Payment OpeningBalanceMakePayment;
    private String moduleId;

    public Payment getOpeningBalanceMakePayment() {
        return OpeningBalanceMakePayment;
    }

    public void setOpeningBalanceMakePayment(Payment OpeningBalanceMakePayment) {
        this.OpeningBalanceMakePayment = OpeningBalanceMakePayment;
    }

  
    public String getOpeningBalanceMakePaymentId() {
        return OpeningBalanceMakePaymentId;
    }

    public void setOpeningBalanceMakePaymentId(String OpeningBalanceMakePaymentId) {
        this.OpeningBalanceMakePaymentId = OpeningBalanceMakePaymentId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
    
}
