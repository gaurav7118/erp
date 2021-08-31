/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.writeOffInvoice;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceWriteOff;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class accWriteOffImpl extends BaseDAO implements accWriteOffServiceDao {

    private AccountingHandlerDAO accountingHandlerDAOobj;

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    @Override
    public KwlReturnObject saveInvoiceWriteOff(HashMap<String, Object> writeOffMap) throws ServiceException {
        List list = new ArrayList();
        InvoiceWriteOff invoiceWO = new InvoiceWriteOff();
        try {
            if (writeOffMap.containsKey("id") && writeOffMap.get("id") != null) {
                invoiceWO = (InvoiceWriteOff) get(InvoiceWriteOff.class, (String) writeOffMap.get("id"));
            }
            if (writeOffMap.containsKey("invoiceId") && writeOffMap.get("invoiceId") != null) {
                KwlReturnObject invoiceResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), (String) writeOffMap.get("invoiceId"));
                Invoice invoice = (Invoice) invoiceResult.getEntityList().get(0);
                invoiceWO.setInvoice(invoice);
            }
            if (writeOffMap.containsKey("jeId") && writeOffMap.get("jeId") != null) {
                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), (String) writeOffMap.get("jeId"));
                JournalEntry JE = (JournalEntry) jeResult.getEntityList().get(0);
                invoiceWO.setJournalEntry(JE);
            }
            if (writeOffMap.containsKey("reverseJeId") && writeOffMap.get("reverseJeId") != null) {
                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), (String) writeOffMap.get("reverseJeId"));
                JournalEntry JE = (JournalEntry) jeResult.getEntityList().get(0);
                invoiceWO.setReversejournalEntry(JE);
            }
            if (writeOffMap.containsKey("writtenOffAmountInInvoiceCurrency") && writeOffMap.get("writtenOffAmountInInvoiceCurrency") != null) {
                invoiceWO.setWrittenOffAmountInInvoiceCurrency((Double) writeOffMap.get("writtenOffAmountInInvoiceCurrency"));
            }
            if (writeOffMap.containsKey("writtenOffAmountInBaseCurrency") && writeOffMap.get("writtenOffAmountInBaseCurrency") != null) {
                invoiceWO.setWrittenOffAmountInBaseCurrency((Double) writeOffMap.get("writtenOffAmountInBaseCurrency"));
            }
            if (writeOffMap.containsKey("date") && writeOffMap.get("date") != null) {
                invoiceWO.setWriteOffDate((Date) writeOffMap.get("date"));
            }
            if (writeOffMap.containsKey("companyId") && writeOffMap.get("companyId") != null) {
                KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), (String) writeOffMap.get("companyId"));
                Company company = (Company) companyResult.getEntityList().get(0);
                invoiceWO.setCompany(company);
            }
            if (writeOffMap.containsKey("isRecovered") && writeOffMap.get("isRecovered") != null) {
                invoiceWO.setIsRecovered((Boolean) writeOffMap.get("isRecovered"));
            }
            if (writeOffMap.containsKey("memo") && writeOffMap.get("memo") != null) {
                invoiceWO.setMemo((String) writeOffMap.get("memo"));
            }
            save(invoiceWO);
            list.add(invoiceWO);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveInvoiceWriteOff : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getWrittenOfInvoices(HashMap<String, Object> map) throws ServiceException {
        String condition = "";
        String companyId = (String) (map.containsKey("companyId") ? map.get("companyId") : "");
        String ss = (String) (map.containsKey("ss") ? map.get("ss") : "");
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(companyId);
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"IWO.invoice.invoiceNumber", "IWO.memo", "IWO.invoice.customer.name"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 3);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            String query = "from InvoiceWriteOff IWO where IWO.company.companyID = ? " + condition;
            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accWriteOffImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accWriteOffImpl.getWrittenOfInvoices:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getWriteOffJEs(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String ss = (String) request.get(Constants.ss);
            
            ArrayList params = new ArrayList();
            String condition = "";
            params.add(companyid);
            if (request.containsKey("invoiceid") && request.get("invoiceid") != null) {
                String invoiceId = (String) request.get("invoiceid");
                condition += " and iwo.invoice.ID=? ";
                params.add(invoiceId);
            }
            
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and iwo.journalEntry.ID IN(" + jeIds + ")";
            }
            
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"iwo.journalEntry.entryNumber", "iwo.memo"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            
            String query = "from InvoiceWriteOff iwo where iwo.company.companyID=? " + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accWriteOffImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accWriteOffImpl.getWriteOffJEs:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    public KwlReturnObject getReverseWriteOffJEs(HashMap<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);
            String ss = (String) request.get(Constants.ss);

            ArrayList params = new ArrayList();
            String condition = "";
            params.add(companyid);
            if (request.containsKey("invoiceid") && request.get("invoiceid") != null) {
                String invoiceId = (String) request.get("invoiceid");
                condition += " and iwo.invoice.ID=? ";
                params.add(invoiceId);
            }
            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and iwo.reversejournalEntry.ID IN(" + jeIds + ")";
            }

            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"iwo.reversejournalEntry.entryNumber", "iwo.memo"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            String query = "from InvoiceWriteOff iwo where iwo.company.companyID=? " + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(accWriteOffImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accWriteOffImpl.getReverseWriteOffJEs:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public KwlReturnObject getInvoiceWriteOffEntries(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        String condition = "";
        try {
            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            if (requestParams.containsKey("invoiceid") && requestParams.get("invoiceid") != null) {
                String invoiceId = (String) requestParams.get("invoiceid");
                condition += " and IWO.invoice.ID=? ";
                params.add(invoiceId);
            }
            
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                String companyId = (String) requestParams.get("companyid");
                condition += " and IWO.company.companyID=? ";
                params.add(companyId);
            }
            if (requestParams.containsKey("asofdate") && requestParams.get("asofdate") != null) {
                String asOfDate = (String) requestParams.get("asofdate");
                condition += "  and IWO.writeOffDate<=? ";
                params.add(df.parse(asOfDate));
            }
            if (requestParams.containsKey("startDate") && requestParams.get("startDate") != null && requestParams.containsKey("endDate") && requestParams.get("endDate") != null) {
                Date startDate = (Date) requestParams.get("startDate");
                Date endDate = (Date) requestParams.get("endDate");
                condition += " and (IWO.writeOffDate >=? and IWO.writeOffDate <=?) ";
                params.add(startDate);
                params.add(endDate);
            }
            String selQuery = "from InvoiceWriteOff IWO  where IWO.isRecovered=false " + condition;
            list = executeQuery( selQuery, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accWriteOffImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getJEFromInvoiceWriteOff(String jeId, String CompanyId, Boolean isReverseJe) throws ServiceException {
        List list = new ArrayList();
        String query = "";
        try {
            if (isReverseJe) {
                query = "from InvoiceWriteOff IWO where IWO.reversejournalEntry.ID = ? and IWO.company.companyID = ?";
            } else {
                query = "from InvoiceWriteOff IWO where IWO.journalEntry.ID = ? and IWO.company.companyID = ?";
            }
            list = executeQuery( query, new Object[]{jeId, CompanyId});
        } catch (ServiceException ex) {
            Logger.getLogger(accWriteOffImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveReceiptWriteOff(HashMap<String, Object> writeOffMap) throws ServiceException {
        List list = new ArrayList();
        ReceiptWriteOff receiptWO = new ReceiptWriteOff();
        try {
            if (writeOffMap.containsKey("id") && writeOffMap.get("id") != null) {
                receiptWO = (ReceiptWriteOff) get(ReceiptWriteOff.class, (String) writeOffMap.get("id"));
            }
            if (writeOffMap.containsKey("receiptId") && writeOffMap.get("receiptId") != null) {
                KwlReturnObject Result = accountingHandlerDAOobj.getObject(Receipt.class.getName(), (String) writeOffMap.get("receiptId"));
                Receipt receipt = (Receipt) Result.getEntityList().get(0);
                receiptWO.setReceipt(receipt);
            }
            if (writeOffMap.containsKey("jeId") && writeOffMap.get("jeId") != null) {
                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), (String) writeOffMap.get("jeId"));
                JournalEntry JE = (JournalEntry) jeResult.getEntityList().get(0);
                receiptWO.setJournalEntry(JE);
            }
            if (writeOffMap.containsKey("reverseJeId") && writeOffMap.get("reverseJeId") != null) {
                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), (String) writeOffMap.get("reverseJeId"));
                JournalEntry JE = (JournalEntry) jeResult.getEntityList().get(0);
                receiptWO.setReversejournalEntry(JE);
            }
            if (writeOffMap.containsKey("writtenOffAmountInReceiptCurrency") && writeOffMap.get("writtenOffAmountInReceiptCurrency") != null) {
                receiptWO.setWrittenOffAmountInReceiptCurrency((Double) writeOffMap.get("writtenOffAmountInReceiptCurrency"));
            }
            if (writeOffMap.containsKey("writtenOffAmountInBaseCurrency") && writeOffMap.get("writtenOffAmountInBaseCurrency") != null) {
                receiptWO.setWrittenOffAmountInBaseCurrency((Double) writeOffMap.get("writtenOffAmountInBaseCurrency"));
            }
            if (writeOffMap.containsKey("date") && writeOffMap.get("date") != null) {
                receiptWO.setWriteOffDate((Date) writeOffMap.get("date"));
            }
            if (writeOffMap.containsKey("companyId") && writeOffMap.get("companyId") != null) {
                KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), (String) writeOffMap.get("companyId"));
                Company company = (Company) companyResult.getEntityList().get(0);
                receiptWO.setCompany(company);
            }
            if (writeOffMap.containsKey("isRecovered") && writeOffMap.get("isRecovered") != null) {
                receiptWO.setIsRecovered((Boolean) writeOffMap.get("isRecovered"));
            }
            if (writeOffMap.containsKey("memo") && writeOffMap.get("memo") != null) {
                receiptWO.setMemo((String) writeOffMap.get("memo"));
            }
            save(receiptWO);
            list.add(receiptWO);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveReceiptWriteOff : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getWrittenOfReceipts(HashMap<String, Object> map) throws ServiceException {
         String condition = "";
        String companyId = (String) (map.containsKey("companyId") ? map.get("companyId") : "");
        String ss = (String) (map.containsKey("ss") ? map.get("ss") : "");
        List list = new ArrayList();
        int count=0;
        try {
            ArrayList params = new ArrayList();
            params.add(companyId);
            String start = (String) map.get(Constants.start);
            String limit = (String) map.get(Constants.limit);
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"RWO.receipt.receiptNumber", "RWO.memo", "RWO.receipt.customer.name"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 3);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            String query = "from ReceiptWriteOff RWO where RWO.company.companyID = ? " + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (SQLException | ServiceException | NumberFormatException ex) {
            Logger.getLogger(accWriteOffImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accWriteOffImpl.getWrittenOfReceipts:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getJEFromReceiptWriteOff(String jeId, String CompanyId, Boolean isReverseJe) throws ServiceException {
         List list = new ArrayList();
        String query = "";
        try {
            if (isReverseJe) {
                query = "from ReceiptWriteOff IWO where IWO.reversejournalEntry.ID = ? and IWO.company.companyID = ?";
            } else {
                query = "from ReceiptWriteOff IWO where IWO.journalEntry.ID = ? and IWO.company.companyID = ?";
            }
            list = executeQuery( query, new Object[]{jeId, CompanyId});
        } catch (ServiceException ex) {
            Logger.getLogger(accWriteOffImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    } 
    
}
