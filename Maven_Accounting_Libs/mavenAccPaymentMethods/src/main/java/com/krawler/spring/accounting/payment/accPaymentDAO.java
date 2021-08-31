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
package com.krawler.spring.accounting.payment;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.PayDetail;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accPaymentDAO {

    public KwlReturnObject addPaymentMethod(Map<String, Object> pmMap) throws ServiceException;

    public KwlReturnObject addChequeLayout(HashMap<String, Object> pmMap) throws ServiceException;
    public KwlReturnObject getChequeLayoutPaymentMethod(String bankId) throws ServiceException;

    public KwlReturnObject updateChequeLayout(HashMap<String, Object> pmMap) throws ServiceException;

    public KwlReturnObject getChequeLayout(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject deleteCard(String iD, String companyid) throws ServiceException;

    public KwlReturnObject updateCnAmount(String iD, double amount) throws ServiceException;

    public KwlReturnObject updateCnOpeningAmountDue(String iD, double amount) throws ServiceException;

    public KwlReturnObject deleteChequePermanently(String iD, String companyid) throws ServiceException;

    public KwlReturnObject deleteCheque(String iD, String companyid) throws ServiceException;

    public KwlReturnObject updatePaymentMethod(Map<String, Object> pmMap) throws ServiceException;
    
    public KwlReturnObject deletePaymentMethod(String methodid, String companyid) throws ServiceException;
    
    public KwlReturnObject searchPaymentMethod(String methodid, String companyid) throws ServiceException;

    public KwlReturnObject getPaymentMethod(HashMap<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getPayMtdMappedToCustomer(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject addPayDetail(HashMap hm) throws ServiceException;

    public KwlReturnObject savePayDetail(PayDetail payDetail) throws ServiceException;
    
    public PayDetail saveOrUpdatePayDetail(HashMap hm) throws ServiceException;

    public KwlReturnObject updatePayDetail(HashMap hm) throws ServiceException;

    public KwlReturnObject deletePayDetail(String iD, String companyid) throws ServiceException;


    
    public boolean isChequeSequenceNumberAvailable(HashMap hm) throws ServiceException;
    
    public KwlReturnObject getPaymentAmountofBadDebtGoodsReceipt(String invoiceid, boolean isBeforeClaimed) throws ServiceException;
    
    public KwlReturnObject approvePendingMakePayment(String cnID, String companyid, int status) throws ServiceException;
    
    public KwlReturnObject rejectPendingmakePayment(String cnid, String companyid) throws ServiceException;
    
    public KwlReturnObject getPaymentFromBadDebtClaimedInvoice(String invoiceid, boolean isBeforeClaimed , Date getPaymentFromBadDebtClaimedInvoice) throws ServiceException;
    
    public KwlReturnObject getPaymentAmountFromGoodsReceipt(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getPaymentDetailsLinkedWithGoodsReceipt(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject addCheque(HashMap hm) throws ServiceException;
    
    public KwlReturnObject getInvoiceInTemp(String document, String companyId,int moduleId) throws ServiceException;
    
    public KwlReturnObject insertInvoiceOrCheque(String document, String companyId,int moduleId,String bankId) throws ServiceException;
    
    public KwlReturnObject deleteUsedInvoiceOrCheque(String document, String companyId) throws ServiceException;
    
    public KwlReturnObject getSearchChequeNoTemp(String document, String companyid,int moduleId,String bankId) throws ServiceException;
    
    public KwlReturnObject updateCheque(HashMap hm) throws ServiceException;

    public KwlReturnObject addCard(HashMap hm) throws ServiceException;

    public KwlReturnObject updateCard(HashMap hm) throws ServiceException;

    public KwlReturnObject getPaymentMethodFromAccount(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject copyPaymentMethods(String companyid, HashMap hm) throws ServiceException;
    
    public void copyIndiaComplianceData(String companyid,HashMap hmPaymentMethod) throws ServiceException;

    public KwlReturnObject updateDnAmount(String iD, double amount) throws ServiceException;

    public KwlReturnObject updateDnOpeningAmountDue(String iD, double amount) throws ServiceException;

    public KwlReturnObject getTransactionCountForPayment(String iD, String companyid) throws ServiceException;
    public KwlReturnObject updateChequePrint(String paymentid,String companyid)throws ServiceException;
    
    public KwlReturnObject getDefaultPaymentMethod(String companyid,String paymentMethodAccountname, String paymentMethodName,int PaymentMethodDetailType) throws ServiceException;
    
    public KwlReturnObject updateCnOpeningBaseAmountDue(String Id, double amount) throws ServiceException;
    
    public KwlReturnObject getPaymentsForCustomer(String personId,String companyId) throws ServiceException;
    
    public KwlReturnObject getRepeatedPaymentChequeDetailsForPaymentMethod(HashMap hm) throws ServiceException; 
    
    public KwlReturnObject getPaymentMadeForJE(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject updateDisHonouredJEFromPayment(String paymentid,String company) throws ServiceException ;
    
    public KwlReturnObject getCreditNotePaymentDetails(HashMap<String, Object> reqMap) throws ServiceException;
    
    public KwlReturnObject searchPaymentMethodInFundTransferJE(String methodid, String companyid) throws ServiceException;
    public KwlReturnObject getChequeSequenceFormatFromAccount(String accountid, String companyid) throws ServiceException ;
    
    public KwlReturnObject getUOBBankDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAccountNameCount(String invoiceNo, String companyid) throws ServiceException;
    
    public KwlReturnObject getRefundNameCount(String refundNo, String companyid,String customerID) throws ServiceException;
    
    public KwlReturnObject getCurrency(String currencyname) throws ServiceException;
    
    public KwlReturnObject getPaymentMethodCount(String paymentMethodStr, String companyid) throws ServiceException;
    
    public KwlReturnObject getPaymentFromBillNo(String billno, String companyid) throws ServiceException;
    public List getSalesPaymentKnockOffTransactions(Map<String, Object> requestParams) throws ServiceException;
    
    public int updateRefundReceiptLinkedWithAdvance(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getRefundReceiptDetailsLinkedToAdvance(JSONObject paramJobj) throws ServiceException;
            
    public int updateRefundReceiptExternallyLinkedWithAdvance(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getRefundReceiptLinkDetailsLinkedWithAdvance(JSONObject paramJobj) throws ServiceException;
    
}
