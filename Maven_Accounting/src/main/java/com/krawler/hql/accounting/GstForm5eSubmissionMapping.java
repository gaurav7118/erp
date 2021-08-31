/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public class GstForm5eSubmissionMapping {
    private String taxRefNo;
    private String formType;
    private String dtPeriodStart;
    private String dtPeriodEnd;
    private String totStdSupply;
    private String totZeroSupply;
    private String totExemptSupply;
    private String totTaxPurchase;
    private String outputTaxDue;
    private String inputTaxRefund;
    private String totValueScheme;
    private String touristRefundChk;
    private String touristRefundAmt;
    private String badDebtChk;
    private String badDebtReliefClaimAmt;
    private String preRegistrationChk;
    private String preRegistrationClaimAmt;
    private String revenue;
    private String defImpPayableAmt;
    private String defTotalGoodsImp;
    private String declarantDesgtn;
    private String contactPerson;
    private String contactNumber;
    private String contactEmail;
    private String grp1BadDebtRecoveryChk="false";
    private String grp1PriorToRegChk="false";
    private String grp1OtherReasonChk="false";
    private String grp1OtherReasons="";
    private String grp2TouristRefundChk="false";
    private String grp2AppvBadDebtReliefChk="false";
    private String grp2CreditNotesChk="false";
    private String grp2OtherReasonsChk="false";
    private String grp2OtherReasons="";
    private String grp3CreditNotesChk="false";
    private String grp3OtherReasonsChk="false";
    private String grp3OtherReasons="";

    public void setTaxRefNo(String taxRefNo) {
        this.taxRefNo = taxRefNo;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public void setDtPeriodStart(String dtPeriodStart) {
        this.dtPeriodStart = dtPeriodStart;
    }

    public void setDtPeriodEnd(String dtPeriodEnd) {
        this.dtPeriodEnd = dtPeriodEnd;
    }
    
    public void setTotStdSupply(String totStdSupply) {
        this.totStdSupply = totStdSupply;
    }

    public void setTotZeroSupply(String totZeroSupply) {
        this.totZeroSupply = totZeroSupply;
    }

    public void setTotExemptSupply(String totExemptSupply) {
        this.totExemptSupply = totExemptSupply;
    }

    public void setTotTaxPurchase(String totTaxPurchase) {
        this.totTaxPurchase = totTaxPurchase;
    }

    public void setOutputTaxDue(String outputTaxDue) {
        this.outputTaxDue = outputTaxDue;
    }

    public void setInputTaxRefund(String inputTaxRefund) {
        this.inputTaxRefund = inputTaxRefund;
    }

    public void setTotValueScheme(String totValueScheme) {
        this.totValueScheme = totValueScheme;
    }

    public void setTouristRefundChk(String touristRefundChk) {
        this.touristRefundChk = touristRefundChk;
    }

    public void setTouristRefundAmt(String touristRefundAmt) {
        this.touristRefundAmt = touristRefundAmt;
    }

    public void setBadDebtChk(String badDebtChk) {
        this.badDebtChk = badDebtChk;
    }

    public void setBadDebtReliefClaimAmt(String badDebtReliefClaimAmt) {
        this.badDebtReliefClaimAmt = badDebtReliefClaimAmt;
    }

    public void setPreRegistrationChk(String preRegistrationChk) {
        this.preRegistrationChk = preRegistrationChk;
    }

    public void setPreRegistrationClaimAmt(String preRegistrationClaimAmt) {
        this.preRegistrationClaimAmt = preRegistrationClaimAmt;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public void setDefImpPayableAmt(String defImpPayableAmt) {
        this.defImpPayableAmt = defImpPayableAmt;
    }

    public void setDefTotalGoodsImp(String defTotalGoodsImp) {
        this.defTotalGoodsImp = defTotalGoodsImp;
    }

    public void setDeclarantDesgtn(String declarantDesgtn) {
        this.declarantDesgtn = declarantDesgtn;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setGrp1BadDebtRecoveryChk(String grp1BadDebtRecoveryChk) {
        this.grp1BadDebtRecoveryChk = grp1BadDebtRecoveryChk;
    }

    public void setGrp1PriorToRegChk(String grp1PriorToRegChk) {
        this.grp1PriorToRegChk = grp1PriorToRegChk;
    }

    public void setGrp1OtherReasonChk(String grp1OtherReasonChk) {
        this.grp1OtherReasonChk = grp1OtherReasonChk;
    }

    public void setGrp1OtherReasons(String grp1OtherReasons) {
        this.grp1OtherReasons = grp1OtherReasons;
    }

    public void setGrp2TouristRefundChk(String grp2TouristRefundChk) {
        this.grp2TouristRefundChk = grp2TouristRefundChk;
    }

    public void setGrp2AppvBadDebtReliefChk(String grp2AppvBadDebtReliefChk) {
        this.grp2AppvBadDebtReliefChk = grp2AppvBadDebtReliefChk;
    }

    public void setGrp2CreditNotesChk(String grp2CreditNotesChk) {
        this.grp2CreditNotesChk = grp2CreditNotesChk;
    }

    public void setGrp2OtherReasonsChk(String grp2OtherReasonsChk) {
        this.grp2OtherReasonsChk = grp2OtherReasonsChk;
    }

    public void setGrp2OtherReasons(String grp2OtherReasons) {
        this.grp2OtherReasons = grp2OtherReasons;
    }

    public void setGrp3CreditNotesChk(String grp3CreditNotesChk) {
        this.grp3CreditNotesChk = grp3CreditNotesChk;
    }

    public void setGrp3OtherReasonsChk(String grp3OtherReasonsChk) {
        this.grp3OtherReasonsChk = grp3OtherReasonsChk;
    }

    public void setGrp3OtherReasons(String grp3OtherReasons) {
        this.grp3OtherReasons = grp3OtherReasons;
    }

    public String getJSONForForm5eSubmission() {
        return "{"+
                "\"filingInfo\":{"+
                      " \"taxRefNo\": \""+taxRefNo+"\","+
                      " \"formType\": \""+formType+"\","+
                      " \"dtPeriodStart\": \""+dtPeriodStart+"\","+
                      " \"dtPeriodEnd\": \""+dtPeriodEnd+"\""+
                "},"+
                "\"supplies\":{"+
                      " \"totStdSupply\": \""+totStdSupply+"\","+
                      " \"totZeroSupply\": \""+totZeroSupply+"\","+
                      " \"totExemptSupply\": \""+totExemptSupply+"\""+
                "},"+
                "\"purchases\": { "+
                      " \"totTaxPurchase\": \""+totTaxPurchase+"\""+
                "},"+
                "\"taxes\": { "+
                      " \"outputTaxDue\": \""+outputTaxDue+"\","+
                      " \"inputTaxRefund\": \""+inputTaxRefund+"\""+
                "},"+
                "\"schemes\": {"+
                      " \"totValueScheme\": \""+totValueScheme+"\","+
                      " \"touristRefundChk\": \""+touristRefundChk+"\","+
                      " \"touristRefundAmt\": \""+touristRefundAmt+"\","+
                      " \"badDebtChk\": \""+badDebtChk+"\","+
                      " \"badDebtReliefClaimAmt\": \""+badDebtReliefClaimAmt+"\","+
                      " \"preRegistrationChk\": \""+preRegistrationChk+"\","+
                      " \"preRegistrationClaimAmt\": \""+preRegistrationClaimAmt+"\""+
                "},"+
                "\"revenue\": { "+
                      " \"revenue\":\""+revenue+"\""+
                "},"+
                "\"igdScheme\": { "+
                       " \"defImpPayableAmt\":\""+defImpPayableAmt+"\","+
                       " \"defTotalGoodsImp\":\""+defTotalGoodsImp+"\""+
                "}, "+
                "\"declaration\": { "+
                       " \"declarantDesgtn\":\""+declarantDesgtn+"\","+
                       " \"contactPerson\":\""+contactPerson+"\","+
                       " \"contactNumber\":\""+contactNumber+"\","+
                       " \"contactEmail\":\""+contactEmail+"\""+
                "},"+
                " \"reasons\": { "+
                       " \"grp1BadDebtRecoveryChk\":\""+grp1BadDebtRecoveryChk+"\","+
                       " \"grp1PriorToRegChk\":\""+grp1PriorToRegChk+"\","+
                       " \"grp1OtherReasonChk\":\""+grp1OtherReasonChk+"\","+
                       " \"grp1OtherReasons\":\""+grp1OtherReasons+"\","+
                       " \"grp2TouristRefundChk\":\""+grp2TouristRefundChk+"\","+
                       " \"grp2AppvBadDebtReliefChk\":\""+grp2AppvBadDebtReliefChk+"\","+
                       " \"grp2CreditNotesChk\":\""+grp2CreditNotesChk+"\","+
                       " \"grp2OtherReasonsChk\":\""+grp2OtherReasonsChk+"\","+
                       " \"grp2OtherReasons\":\""+grp2OtherReasons+"\","+
                       " \"grp3CreditNotesChk\":\""+grp3CreditNotesChk+"\","+
                       " \"grp3OtherReasonsChk\":\""+grp3OtherReasonsChk+"\","+
                       " \"grp3OtherReasons\":\""+grp3OtherReasons+"\""+
                " }"+
            " }";
    }
}
