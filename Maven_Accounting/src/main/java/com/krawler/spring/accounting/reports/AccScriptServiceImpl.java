/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.Modules;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.Product;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerControllerCMNService;
import com.krawler.spring.accounting.entitygst.AccEntityGstDao;
import com.krawler.spring.accounting.entitygst.GSTR1ExportToExcel;
import com.krawler.spring.accounting.gst.services.GSTRConstants;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportDAOImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.mysql.jdbc.PreparedStatement;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.krawler.spring.accounting.entitygst.AccEntityGstService;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.repeatedtransaction.AccRepeateInvoiceService;
import com.krawler.spring.accounting.vendor.accVendorControllerCMNService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.export.oasis.CellStyle;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.velocity.exception.ParseErrorException;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class AccScriptServiceImpl implements AccScriptService {

    private MessageSource messageSource;
    private AccScriptDao accScriptDao;
    private accAccountDAO accAccountDAOobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccEntityGstService accEntityGstService;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accProductDAO accProductObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccRepeateInvoiceService accRepeateInvoiceServiceObj;
    private accCustomerControllerCMNService accCustomerControllerCMNServiceObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private accCurrencyDAO accCurrencyDAOobj;
    private accVendorControllerCMNService accVendorcontrollerCMNService;
    private AccEntityGstDao accEntityGstDao;
    private accGoodsReceiptDAO accGoodsReceiptobj;

    public void setAccScriptDao(AccScriptDao accScriptDao) {
        this.accScriptDao = accScriptDao;
    }

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
       public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    public void setAccEntityGstService(AccEntityGstService accEntityGstService) {
        this.accEntityGstService = accEntityGstService;
    }
    public void setKwlCommonTablesDAO(kwlCommonTablesDAO KwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = KwlCommonTablesDAOObj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    } 
    public void setAccRepeateInvoiceServiceObj(AccRepeateInvoiceService accRepeateInvoiceServiceObj) {
        this.accRepeateInvoiceServiceObj = accRepeateInvoiceServiceObj;
    }    
    public void setaccCustomerControllerCMNServiceObj(accCustomerControllerCMNService accCustomerControllerCMNServiceObj) {
        this.accCustomerControllerCMNServiceObj = accCustomerControllerCMNServiceObj;
    }  
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }   
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }  
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }  
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }   
    public void setaccVendorcontrollerCMNService(accVendorControllerCMNService accVendorcontrollerCMNService) {
        this.accVendorcontrollerCMNService = accVendorcontrollerCMNService;
    }
    public void setAccEntityGstDao(AccEntityGstDao accEntityGstDao) {
        this.accEntityGstDao = accEntityGstDao;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }    
    public Map getCNDNForGainLossNotPosted(Map<String, Object> request) throws ServiceException {
        Map dnMap = new HashMap();
        Map cnMap = new HashMap();
        Map map = new HashMap();
        boolean success = false;
        Writer writer = null;
        try {
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));

            } else {
                writer = new FileWriter("/home/krawler/ScriptData/CNDNForGainLossNotPosted.csv");
            }
            writer.write("Module name,ID,Transaction No,Transaction Date,Linked Transaction No,Linked Transaction Date,Currency Of Transaction\n");
            List dnlist = accScriptDao.getDNForGainLossNotPosted(request);
            Iterator itr = dnlist.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String dnid = oj[0].toString();
                String dnno = oj[1].toString();
                String dndate = oj[2].toString();
                String grno = oj[3].toString();
                String grdate = oj[4].toString();
                String currency = oj[5].toString();
                writer.write("Debit Note," + dnid + "," + dnno + "," + dndate + "," + grno + "," + grdate + "," + currency + "\n");
            }
            List cnlist = accScriptDao.getCNForGainLossNotPosted(request);
            Iterator itr1 = cnlist.iterator();
            while (itr1.hasNext()) {
                Object[] oj = (Object[]) itr1.next();
                String dnid = oj[0].toString();
                String dnno = oj[1].toString();
                String dndate = oj[2].toString();
                String grno = oj[3].toString();
                String grdate = oj[4].toString();
                String currency = oj[5].toString();
                writer.write("Credit Note," + dnid + "," + dnno + "," + dndate + "," + grno + "," + grdate + "," + currency + "\n");
            }
            writer.flush();
            writer.close();
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return map;
    }

    public Map getPaymentReceiptForGainLossNotPosted(Map<String, Object> request) throws ServiceException {
        Map dnMap = new HashMap();
        Map cnMap = new HashMap();
        Map map = new HashMap();
        boolean success = false;
        Writer writer = null;
        try {
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));

            } else {
                writer = new FileWriter("/home/krawler/ScriptData/PaymentReceiptForGainLossNotPosted.csv");
            }
            writer.write("Module name,ID,Transaction No,Transaction Date,Linked Transaction No,Linked Transaction Date,Currency Of Transaction\n");
            List dnlist = accScriptDao.getPaymentForGainLossNotPosted(request);
            Iterator itr = dnlist.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String dnid = oj[0].toString();
                String dnno = oj[1].toString();
                String dndate = oj[2].toString();
                String grno = oj[3].toString();
                String grdate = oj[4].toString();
                String currency = oj[5].toString();
                writer.write("Payment," + dnid + "," + dnno + "," + dndate + "," + grno + "," + grdate + "," + currency + "\n");
            }
            List cnlist = accScriptDao.getReceiptForGainLossNotPosted(request);
            Iterator itr1 = cnlist.iterator();
            while (itr1.hasNext()) {
                Object[] oj = (Object[]) itr1.next();
                String dnid = oj[0].toString();
                String dnno = oj[1].toString();
                String dndate = oj[2].toString();
                String grno = oj[3].toString();
                String grdate = oj[4].toString();
                String currency = oj[5].toString();
                writer.write("Receipt," + dnid + "," + dnno + "," + dndate + "," + grno + "," + grdate + "," + currency + "\n");
            }
            writer.flush();
            writer.close();
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return map;
    }

    public Map getTrnsactionsOtherThanControlAccountForVendor(Map<String, Object> request) throws ServiceException {
        Map dnMap = new HashMap();
        Map cnMap = new HashMap();
        Map map = new HashMap();
        Writer writer = null;
        boolean success = false;
        try {
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));

            } else {
                writer = new FileWriter("/home/krawler/ScriptData/TransactionsOtherThanControlAccountForVendor.csv");
            }
            writer.write("Module name,Transaction No\n");
            map = accScriptDao.getTrnsactionsOtherThanControlAccountForVendor(request);
            List goodsreceipt = map.containsKey("goodsreceipt") ? ((List) map.get("goodsreceipt")) : new ArrayList();
            for (Iterator it = goodsreceipt.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Goods Receipt," + transactionno + "\n");
            }
            List payment = map.containsKey("payment") ? ((List) map.get("payment")) : new ArrayList();
            for (Iterator it = payment.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Payment," + transactionno + "\n");
            }
            List receipt = map.containsKey("receipt") ? ((List) map.get("receipt")) : new ArrayList();
            for (Iterator it = receipt.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Receipt," + transactionno + "\n");
            }
            List DN = map.containsKey("DN") ? ((List) map.get("DN")) : new ArrayList();
            for (Iterator it = DN.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Debit Note," + transactionno + "\n");
            }
            List CN = map.containsKey("CN") ? ((List) map.get("CN")) : new ArrayList();
            for (Iterator it = CN.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Credit Note," + transactionno + "\n");
            }

            map = accScriptDao.getOpeningTrnsactionsOtherThanControlAccountForVendor(request);
            goodsreceipt = map.containsKey("goodsreceipt") ? ((List) map.get("goodsreceipt")) : new ArrayList();
            for (Iterator it = goodsreceipt.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Goods Receipt," + transactionno + "\n");
            }
            payment = map.containsKey("payment") ? ((List) map.get("payment")) : new ArrayList();
            for (Iterator it = payment.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Payment," + transactionno + "\n");
            }
            receipt = map.containsKey("receipt") ? ((List) map.get("receipt")) : new ArrayList();
            for (Iterator it = receipt.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Receipt," + transactionno + "\n");
            }
            DN = map.containsKey("DN") ? ((List) map.get("DN")) : new ArrayList();
            for (Iterator it = DN.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Debit Note," + transactionno + "\n");
            }
            CN = map.containsKey("CN") ? ((List) map.get("CN")) : new ArrayList();
            for (Iterator it = CN.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Credit Note," + transactionno + "\n");
            }
            writer.flush();
            writer.close();
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return map;
    }

    public Map getTrnsactionsOtherThanControlAccountForCustomer(Map<String, Object> request) throws ServiceException {
        Map dnMap = new HashMap();
        Map cnMap = new HashMap();
        Map map = new HashMap();
        boolean success = false;
        Writer writer = null;
        try {
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));

            } else {
                writer = new FileWriter("/home/krawler/ScriptData/TransactionsOtherThanControlAccountForCustomer.csv");
            }

            writer.write("Module name,Transaction No\n");
            map = accScriptDao.getTrnsactionsOtherThanControlAccountForCustomer(request);
            List goodsreceipt = map.containsKey("invoice") ? ((List) map.get("invoice")) : new ArrayList();
            List payment = map.containsKey("payment") ? ((List) map.get("payment")) : new ArrayList();
            List receipt = map.containsKey("receipt") ? ((List) map.get("receipt")) : new ArrayList();
            List DN = map.containsKey("DN") ? ((List) map.get("DN")) : new ArrayList();
            List CN = map.containsKey("CN") ? ((List) map.get("CN")) : new ArrayList();
            for (Iterator it = goodsreceipt.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Invoice," + transactionno + "\n");
            }
            for (Iterator it = payment.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Payment," + transactionno + "\n");
            }
            for (Iterator it = receipt.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Receipt," + transactionno + "\n");
            }
            for (Iterator it = DN.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Debit Note," + transactionno + "\n");
            }
            for (Iterator it = CN.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Credit Note," + transactionno + "\n");
            }
            map = accScriptDao.getOpeningTrnsactionsOtherThanControlAccountForCustomer(request);
            goodsreceipt = map.containsKey("invoice") ? ((List) map.get("invoice")) : new ArrayList();
            payment = map.containsKey("payment") ? ((List) map.get("payment")) : new ArrayList();
            receipt = map.containsKey("receipt") ? ((List) map.get("receipt")) : new ArrayList();
            DN = map.containsKey("DN") ? ((List) map.get("DN")) : new ArrayList();
            CN = map.containsKey("CN") ? ((List) map.get("CN")) : new ArrayList();
            for (Iterator it = goodsreceipt.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Invoice," + transactionno + "\n");
            }
            for (Iterator it = payment.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Payment," + transactionno + "\n");
            }
            for (Iterator it = receipt.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Receipt," + transactionno + "\n");
            }
            for (Iterator it = DN.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Debit Note," + transactionno + "\n");
            }
            for (Iterator it = CN.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Opening Credit Note," + transactionno + "\n");
            }
            writer.flush();
            writer.close();
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return map;
    }

    public Map getJournalEntryRecordForControlAccounts(Map<String, Object> request) throws ServiceException {
        Map dnMap = new HashMap();
        Map cnMap = new HashMap();
        Map map = new HashMap();
        boolean success = false;
        Writer writer = null;
        try {
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));

            } else {
                writer = new FileWriter("/home/krawler/ScriptData/ManualJournalEntryRecordForControlAccounts.csv");
            }

            writer.write("Module name,Transaction No,Description\n");
            map = accScriptDao.getManualJEForControlAccount(request);
            List manualJE = map.containsKey("ManualJE") ? (List) map.get("ManualJE") : new ArrayList();
            for (Iterator it = manualJE.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("JournalEntry," + transactionno + ",Manual JE created with Vendor Control Account \n");
            }
            List PartyJEWithoutCNDN = map.containsKey("PartyJEWithoutCNDN") ? (List) map.get("PartyJEWithoutCNDN") : new ArrayList();
            for (Iterator it = PartyJEWithoutCNDN.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("JournalEntry," + transactionno + ",Manual JE created withount CN DN \n");
            }
            List PartyJEWithVenCustNull = map.containsKey("PartyJEWithVenCustNull") ? (List) map.get("PartyJEWithVenCustNull") : new ArrayList();
            for (Iterator it = PartyJEWithVenCustNull.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("JournalEntry," + transactionno + ",Manual JE created with Vendor/Customer Id NULL \n");
            }
            writer.flush();
            writer.close();
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return map;
    }

    public Map getInvoicesAmountDiffThanJEAmount(Map<String, Object> request) throws ServiceException {
        Map dnMap = new HashMap();
        Map cnMap = new HashMap();
        Map map = new HashMap();
        boolean success = false;
        Writer writer = null;
        try {
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));

            } else {
                writer = new FileWriter("/home/krawler/ScriptData/InvoicesAmountDiffThanJEAmount.csv");
            }
            writer.write("Module name,Transaction No,Transaction Amount ,JE Number,JE Amount \n");
            List dnlist = accScriptDao.getInvoicesAmountDiffThanJEAmount(request);
            Iterator itr = dnlist.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String grno = oj[0].toString();
                String gramount = oj[1].toString();
                String jeno = oj[2].toString();
                String jeamount = oj[3].toString();
                writer.write("Invoice," + grno + "," + gramount + "," + jeno + "," + jeamount + "\n");
            }
            List cnlist = accScriptDao.getGoodsReceiptAmountDiffThanJEAmount(request);
            Iterator itr1 = cnlist.iterator();
            while (itr1.hasNext()) {
                Object[] oj = (Object[]) itr1.next();
                String grno = oj[0].toString();
                String gramount = oj[1].toString();
                String jeno = oj[2].toString();
                String jeamount = oj[3].toString();
                writer.write("Goods Receipt," + grno + "," + gramount + "," + jeno + "," + jeamount + "\n");
            }
            writer.flush();
            writer.close();
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return map;
    }

    public Map getDifferentPaymentReceiptAndGainLossJEAccount(Map<String, Object> request) throws ServiceException {
        Map dnMap = new HashMap();
        Map cnMap = new HashMap();
        Writer writer = null;
        Map map = new HashMap();
        boolean success = false;
        try {
            if (request.containsKey("filename")) {
                writer = new FileWriter((String) request.get("filename"));

            } else {
                writer = new FileWriter("/home/krawler/ScriptData/DifferentPaymentReceiptAndGainLossJEAccount.csv");
            }
            writer.write("Module name,Transaction No,Description\n");
            List l = accScriptDao.getDifferentPaymentAndGainLossJEAccount(request);
            for (Iterator it = l.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Payment," + transactionno + ",Different Payment And GainLossJE Account \n");
            }
            l = accScriptDao.getDifferentReceiptAndGainLossJEAccount(request);
            for (Iterator it = l.iterator(); it.hasNext();) {
                String transactionno = (String) it.next();
                writer.write("Receipt," + transactionno + ",Different Receipt And GainLossJE Account \n");
            }
            writer.flush();
            writer.close();
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    map = new HashMap();
                    map.put("success", success);
                } catch (IOException ex) {
                    Logger.getLogger(AccScriptDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return map;
    }/**
     * 
     * @param params
     * @return
     * @throws ServiceException 
     * @ Service to copy data from master to dimension
     */
    public JSONObject copyDataFromMasterToDimension(JSONObject params) throws ServiceException {
        KwlReturnObject kwlReturnObject = null;
        JSONObject jSONObject = new JSONObject();
        List returnlist=new ArrayList();
        try {
            String subdomain = params.optString("subdomain");
            kwlReturnObject = accScriptDao.getAllCompanyOfBrandDiscount(params);
            List companyList = kwlReturnObject.getEntityList();
            Iterator iterator = companyList.iterator();
            
            while (iterator.hasNext()) {
                String companyid = (String) iterator.next();
                params.put("companyid", companyid);
                /*
                 Create dimension Product Brand in Product module 
                 */
                createDimension(params);

                /**
                 * Set Dimension value to each product using Brand Value.
                 */
                insertOrUpdateDimensionData(params);
                String data = params.optString("responselist");
                returnlist.add(data);

            }
            params.put("responselist",returnlist);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return params;
    }
/**
 * 
 * @param params
 * @return
 * @throws ServiceException 
 * @Desc : Function to insert or update product custom data
 */
    public JSONObject insertOrUpdateDimensionData(JSONObject params) throws ServiceException {
        try {
            /*
             Get product list for subdomain having brand value tagged to it
             */
            KwlReturnObject kwlReturnObject = null;
            kwlReturnObject = accScriptDao.getProductsHavingBrand(params);
            List<Object[]> list = kwlReturnObject.getEntityList();
            Map requestparams = new HashMap();
            JSONObject fcddataobj = params.getJSONObject("fcddata");
            List list1=new ArrayList();
            list1.add("Data Updated for Company = "+params.optString("subdomain"));
            for (Object[] objects : list) {
                String brandValue = fcddataobj.optString(objects[1].toString());
                requestparams.put("productid", objects[0].toString());
                requestparams.put("brandvalue", brandValue);
                requestparams.put("companyid", params.optString("companyid"));
                requestparams.put("colnum", params.optInt("colnum"));
                accScriptDao.updateProductCustomData(requestparams,list1);
                
            }
            params.put("responselist", list1);
            /**
             * Update Product Brand discount w.r.t. dimension values
             */
            accScriptDao.updateBrandDiscount(fcddataobj);

        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return params;
    }
/**
 * 
 * @param params
 * @return
 * @throws ServiceException 
 * @Desc : Create new dimension
 */
    public JSONObject createDimension(JSONObject params) throws ServiceException {
        try {
            /*
             Step 1 : Create dimension Brand for product module
             */
            String companyid = params.optString("companyid");
            Integer moduleid = 30;
            Integer fieldtype = 4;
            HashMap<String, Object> colParams = null;
            HashMap<String, Object> RefcolParams = null;
            HashMap<String, Object> requestParams;
            /**
             * Get column number for new dimension
             */
            colParams = getcolumn_number(companyid, 30, fieldtype, 0);
            if (Boolean.parseBoolean((String) colParams.get("success"))) {
                requestParams = new HashMap<String, Object>();
                String fieldlabel = "Product Brand";
                Integer fieldmaxlen = 100;
                Integer validationtype = 0;
                int isCustomField = 0;
                int lineitem = 0;
                int essential = 0;
                boolean allowmapping = false;
                requestParams.put("Maxlength", fieldmaxlen);
                requestParams.put("Isessential", essential);
                requestParams.put("Fieldtype", fieldtype);
                requestParams.put("Validationtype", validationtype);
                requestParams.put("Customregex", "");
                requestParams.put("Fieldname", Constants.Custom_Record_Prefix + fieldlabel);
                requestParams.put("Fieldlabel", fieldlabel);
                requestParams.put("Companyid", companyid);
                requestParams.put("Moduleid", moduleid);
                requestParams.put("Customfield", isCustomField);
                requestParams.put("Customcolumn", lineitem);
                String RefModule = null;
                String RefDataColumn = null;
                String RefFetchColumn = null;
                String comboid = "";
                requestParams.put("Comboname", "");
                requestParams.put("Comboid", comboid);
                requestParams.put("Moduleflag", 0);
                requestParams.put("Colnum", colParams.get("column_number"));
                String Refcolumn_number = "0";
                String refcolumnname = null;
                KwlReturnObject kmsg = null;
                FieldParams fp = null;
                /**
                 * Check if present already or not
                 */
                String module="'"+moduleid+"'";
                boolean isdimpresent=false;
                kmsg=accAccountDAOobj.getFieldParamsforEdit(fieldlabel,module,companyid);
                if(kmsg.getEntityList().size() > 0 && kmsg.getEntityList().get(0)!=null){
                    isdimpresent=true;
                }
                if(!isdimpresent){
                kmsg = accAccountDAOobj.insertfield(requestParams);
                }
                requestParams.put("success", kmsg.isSuccessFlag() ? 1 : 0);
                JSONObject fieldcmbdata = new JSONObject();
                if (kmsg.isSuccessFlag()) {
                    fp = (FieldParams) kmsg.getEntityList().get(0);
                    ArrayList filter_names1 = new ArrayList();
                    ArrayList filter_params = new ArrayList();
                                            filter_names1.add("masterGroup.ID");
                filter_params.add("53");
                filter_names1.add("company.companyID");
                filter_params.add(companyid);
                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                filterRequestParams.put("filter_names", filter_names1);
                filterRequestParams.put("filter_params", filter_params);
                KwlReturnObject result = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
//                    KwlReturnObject masterobj = accMasterItemsDAOobj.getMasterItem("53");
                    List<MasterItem> items = result.getEntityList();
                    String subdomain="";
                    for (MasterItem masterItem : items) {
                        HashMap<String, Object> comborequestParams = new HashMap<String, Object>();
                        comborequestParams.put("Fieldid", fp.getId());
                        comborequestParams.put("Value", masterItem.getValue());
                        comborequestParams.put("Activatedeactivatedimensionvalue", true);
                        /**
                         * Check if value is present already
                         */
                        KwlReturnObject kmsg1 =null;
                        boolean isvaluepresent=false;
                        kmsg1=accAccountDAOobj.getfieldcombodata(comborequestParams);
                        if(kmsg1.getEntityList().size()>0 && kmsg1.getEntityList().get(0)!=null) {
                            isvaluepresent=true;
                        }
                        if(!isvaluepresent){
                            kmsg1 = accAccountDAOobj.insertfieldcombodata(comborequestParams);
                        }
                        FieldComboData comboDatas = (FieldComboData) kmsg1.getEntityList().get(0);
                        fieldcmbdata.put(masterItem.getID(), comboDatas.getId());
                        subdomain=masterItem.getCompany().getSubDomain();
                    }
                    params.put("fcddata", fieldcmbdata);
                    params.put("fieldparam", fp);
                    params.put("colnum", fp.getColnum());
                    params.put("subdomain", subdomain);
                } else {
                    params.put("success", false);
                }
            }

            params.put("success", true);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        return params;
    }
/**
 * 
 * @param companyid
 * @param moduleid
 * @param fieldtype
 * @param moduleflag
 * @return
 * @throws SessionExpiredException
 * @throws JSONException
 * @throws ServiceException 
 */
    private HashMap<String, Object> getcolumn_number(String companyid, Integer moduleid, Integer fieldtype, int moduleflag) throws SessionExpiredException, JSONException, ServiceException {
        KwlReturnObject result = null;
        JSONObject jobj = new JSONObject();
        boolean Notreachedlimit = true;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            Integer custom_column_start = 0, Custom_Column_limit = 0;

            switch (fieldtype) {
//                case Constants.TEXTFIELD: //text field
//                case Constants.NUMBERFIELD: //Number field
//                case Constants.DATEFIELD: //Date
//                case Constants.TIMEFIELD:
//                case Constants.AUTONUMBER://  auto number
//                case 6:
//                case Constants.TEXTAREA: // Text Area
//                case Constants.RICHTEXTAREA: // Rich Text Area
//                case Constants.FIELDSET:
//                    custom_column_start = Constants.Custom_Column_Normal_start;
//                    Custom_Column_limit = Constants.Custom_Column_Normal_limit;
//                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype", ">colnum", "<=colnum"));
//                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "1,2,3,5,6,7,9,12,13,15", custom_column_start, custom_column_start + Custom_Column_limit));
//                    break;
//                case Constants.CHECKBOX:
//                    custom_column_start = Constants.Custom_Column_Check_start;
//                    Custom_Column_limit = Constants.Custom_Column_Check_limit;
//                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype", ">colnum", "<=colnum"));
//                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "11", custom_column_start, custom_column_start + Custom_Column_limit));
//                    break;

                case Constants.SINGLESELECTCOMBO:
                case Constants.MULTISELECTCOMBO:
                    custom_column_start = Constants.Custom_Column_Combo_start;
                    Custom_Column_limit = Constants.Custom_Column_Combo_limit;
                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "4,7"));
                    break;
//                case Constants.REFERENCECOMBO:
//                    if (moduleflag == 1) {
//                        custom_column_start = Constants.Custom_Column_User_start;
//                        Custom_Column_limit = Constants.Custom_Column_User_limit;
//                    } else {
//                        custom_column_start = Constants.Custom_Column_Master_start;
//                        Custom_Column_limit = Constants.Custom_Column_Master_limit;
//                    }
//
//                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "fieldtype", "moduleflag"));
//                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, fieldtype, moduleflag));
//                    break;
                    }
            Integer colcount = 1;

            result = accAccountDAOobj.getFieldParams(requestParams);
            List lst = result.getEntityList();
            colcount = lst.size();
            if (colcount == Custom_Column_limit) {
                jobj.put("success", "msg");
                jobj.put("title", "Alert");
                jobj.put("msg", "Cannot add new field. Maximum custom field limit reached.");
                jobj.put("moduleName", "Products & Services");
                Notreachedlimit = false;
            }
            if (Notreachedlimit) {
                Iterator ite = lst.iterator();
                int[] countchk = new int[Custom_Column_limit + 1];
                while (ite.hasNext()) {
                    FieldParams tmpcontyp = (FieldParams) ite.next();

                    // check added to refer to reference column in case of multiselect combo field instead of refering to column number field
                    if ((fieldtype == 4 || fieldtype == 7) && tmpcontyp.getFieldtype() == 7) {//FieldComboData as drop-down.  Start from col1
                        countchk[tmpcontyp.getRefcolnum() - custom_column_start] = 1;
                    } else {
                        countchk[tmpcontyp.getColnum() - custom_column_start] = 1;
                    }
                }
                for (int i = 1; i <= Custom_Column_limit; i++) {
                    if (countchk[i] == 0) {
                        colcount = i;
                        break;
                    }
                }
            }
            requestParams.put("response", jobj);
            requestParams.put("column_number", colcount + custom_column_start);
            requestParams.put("success", Notreachedlimit ? "True" : "false");
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return requestParams;
    }    
    
    /*
     * This is used to create dimensions GST related dimensions like Entity, Product Tax Class, Unit Quantity Code.
    */
        @Transactional(propagation = Propagation.REQUIRED)    
        public JSONObject createEntityAndProductCategory(JSONObject params) throws ServiceException {
        KwlReturnObject kwlReturnObject = null;
        JSONObject jSONObject = new JSONObject();
        List returnlist=new ArrayList();
        try {
            boolean afterSetUp=true;                   
            kwlReturnObject = accScriptDao.getAllCompanyIndiaUS(params);           
            List<Object[]> companyList = kwlReturnObject.getEntityList();
            boolean skippForProductModule=false;
            
            for (Object object[] : companyList) {
                String companyid = (String) object[0]; // "94548c26-fd5a-4647-91df-c1a09be4cb5f";
                String countryid = (String) object[1];
                String DefaultValue = (String) object[2];
                String setUpDOne = String.valueOf(object[3]);                
                if ( !StringUtil.isNullOrEmpty(setUpDOne) && setUpDOne.equalsIgnoreCase("T"))  {
                    if (Integer.parseInt(countryid) == Constants.indian_country_id || Integer.parseInt(countryid) == Constants.USA_country_id) {
                        /*
                         Create Entity
                         */
                        System.out.println("=======================================");
                        System.out.println("Subdomain :- " + DefaultValue);
                        HashMap<String, Object> requestParams = new HashMap<>();
                        requestParams.put(Constants.companyid, companyid);
                        requestParams.put(Constants.COUNTRY_ID, countryid);
                        requestParams.put("DefaultValue", DefaultValue);
                        requestParams.put(Constants.isMultiEntity, true);
                        requestParams.put("skippForProductModule", skippForProductModule);
                        boolean success = accAccountDAOobj.insertDefaultCustomeFields(requestParams);
                        if (success) {
                            System.out.println("Entity.. Added");                            
                        }
                        /*
                         Create Product Category
                         */
                        HashMap<String, Object> requestParams1 = new HashMap<>();
                        requestParams1.put("filter_names", Arrays.asList("companyid", "fieldlabel","moduleId"));
                        requestParams1.put("filter_values", Arrays.asList(companyid, Constants.GSTProdCategory,Constants.Acc_Product_Master_ModuleId));
                        /**
                         * checking if custom field with name Constants.GSTProdCategory is present for Product module..
                         */
                        KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams1);
                        List lst = result.getEntityList();
                        if (lst.size() > 0) {
                            Iterator ite = lst.iterator();
                            while (ite.hasNext()) {
                                FieldParams tmpcontyp = null;
                                tmpcontyp = (FieldParams) ite.next();
                                int moduleid = tmpcontyp.getModuleid();
                                if (moduleid == Constants.Acc_Product_Master_ModuleId) {
                                    skippForProductModule = true;
                                }
                            }
                        }
                        requestParams.put(Constants.isMultiEntity, false);
                        requestParams.put("afterSetUp", afterSetUp);                        
                        requestParams.remove("DefaultValue"); // No Default value for Second Call (Not for *Entity*)
                        accAccountDAOobj.insertDefaultCustomeFields(requestParams);
                        System.out.println(Constants.GSTProdCategory+"...Added");
                        /**
                         * Making isNEWGST = true also lineleveltermflag = true
                         */
                        HashMap<String, Object> extraCompParams = new HashMap<>();
                        extraCompParams.put(Constants.companyid, companyid);
                        int rows = accAccountDAOobj.updateExtraComPreferences(extraCompParams);
                        if (rows > 0) {
                            System.out.println("Set isNEWGST='T' and lineleveltermflag=1");                            
                        }
                        System.out.println("=======================================");
                        //break;
                    }
                }
                if ( !StringUtil.isNullOrEmpty(setUpDOne) && setUpDOne.equalsIgnoreCase("F"))  {
                    System.out.println("=======================================");
                    System.out.println("Set Up not yet done for this company:- "+DefaultValue+" so performed No Action.");
                    System.out.println("=======================================");
                }
                
            }
            params.put("responselist",returnlist);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return params;
    }
        
        @Transactional(propagation = Propagation.REQUIRED)    
        public JSONObject createRemainingCustomFields(JSONObject params) throws ServiceException {
        KwlReturnObject kwlReturnObject = null;
        JSONObject jSONObject = new JSONObject();
        List returnlist=new ArrayList();
        try {
            boolean isForGSTMigration=true;                   
            kwlReturnObject = accScriptDao.getAllCompanisOfIndiaOnly(params);           
            List<Object[]> companyList = kwlReturnObject.getEntityList();            
            
            for (Object object[] : companyList) {
                String companyid = (String) object[0]; // "94548c26-fd5a-4647-91df-c1a09be4cb5f";
                String countryid = (String) object[1];
                String DefaultValue = (String) object[2];
                String setUpDOne = String.valueOf(object[3]);                
                if ( !StringUtil.isNullOrEmpty(setUpDOne) && setUpDOne.equalsIgnoreCase("T"))  {
                    if (Integer.parseInt(countryid) == Constants.indian_country_id) {
                      
                        System.out.println("=======================================");
                        System.out.println("Subdomain :- " + DefaultValue);
                        HashMap<String, Object> requestParams = new HashMap<>();
                        requestParams.put(Constants.companyid, companyid);
                        requestParams.put(Constants.COUNTRY_ID, countryid);                        
                        requestParams.put(Constants.isMultiEntity, false);   
                        requestParams.put("isForGSTMigration",isForGSTMigration);                           
                        boolean success = accAccountDAOobj.insertDefaultCustomeFields(requestParams);                        
                            System.out.println("Fields.. Added");                                                                                                                        
                    }
                }
                if ( !StringUtil.isNullOrEmpty(setUpDOne) && setUpDOne.equalsIgnoreCase("F"))  {
                    System.out.println("=======================================");
                    System.out.println("Set Up not yet done for this company:- "+DefaultValue+" so performed No Action.");
                    System.out.println("=======================================");
                }
                
            }
            params.put("responselist",returnlist);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return params;
    } 
        @Transactional(propagation = Propagation.REQUIRED)    
        public JSONObject insertRemainingFieldcomboData(JSONObject params) throws ServiceException {
        KwlReturnObject kwlReturnObject = null;
        try {
            kwlReturnObject = accScriptDao.getAllCompanisOfIndiaOnly(params);   // Fetching all companies having countryid = 105        
            List<Object[]> companyList = kwlReturnObject.getEntityList();            
            
            for (Object object[] : companyList) {
                String companyid = (String) object[0]; // company id
                String countryid = (String) object[1]; // country id
                String subdomain = (String) object[2]; // subdomain
                String setUpDone = String.valueOf(object[3]); // status of setup done               
                if ( !StringUtil.isNullOrEmpty(setUpDone) && setUpDone.equalsIgnoreCase("T"))  {
                    if (Integer.parseInt(countryid) == Constants.indian_country_id) {
                      
                        System.out.println("=======================================");
                        System.out.println("Subdomain :- " + subdomain);
                        HashMap<String, Object> requestParams = new HashMap<>();
                        requestParams.put(Constants.companyid, companyid);
                        requestParams.put(Constants.COUNTRY_ID, countryid);                        
                        requestParams.put(Constants.isMultiEntity, false);   
                        boolean success = accAccountDAOobj.insertFieldcomboValues(requestParams);                        
                        System.out.println("Fields.. Added");                                                                                                                        
                    }
                }
                if ( !StringUtil.isNullOrEmpty(setUpDone) && setUpDone.equalsIgnoreCase("F"))  {
                    System.out.println("=======================================");
                    System.out.println("Set Up not yet done for this company:- "+subdomain+" so performed No Action.");
                    System.out.println("=======================================");
                }
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } 
        return params;
    } 
    public static void writedataFor_TaxDoc(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        if (wb == null) {
            wb = new HSSFWorkbook();
        }
        HSSFSheet sheet = null;
        sheet = wb.createSheet(jobj.optString("subdomain", "taxdoc"));
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        HSSFCellStyle rowstyle = wb.createCellStyle();
        HSSFCellStyle cellstyle = wb.createCellStyle();
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        sheet.autoSizeColumn(cellnum);
        try {
            JSONArray b2bArray = jobj.getJSONArray("data");

            /////////// Header Start ////////////
            cellnum = 0;
            HSSFRow headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("tablename");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("docid");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("subdomain");

            for (int i = 0; i < b2bArray.length(); i++) {

                JSONObject invDetails = b2bArray.getJSONObject(i);
                cellnum = 0;
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("tablename"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("docid"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("subdomain"));
            }

        } catch (JSONException exception) {
        }
    }

    public HSSFWorkbook deleteTaxFromIndianCompany(JSONObject params) throws ServiceException {
        KwlReturnObject kwlReturnObject = null;
        JSONObject jSONObject = new JSONObject();
        HSSFWorkbook wb = new HSSFWorkbook();
        List returnlist = new ArrayList();
        try {

            /**
             * Get all Indian company
             */
            JSONArray array = new JSONArray();
            List companyList = accScriptDao.getCompanyData(params);
            JSONObject copyParams = params;
            Iterator iterator = companyList.iterator();
            while (iterator.hasNext()) {
                params = copyParams;
                Object[] objects = (Object[]) iterator.next();
                String companyid = (String) objects[0];
                String subdomain = (String) objects[3];
                params.put("companyid", companyid);

                /**
                 * Get all foreign key references
                 */
                List tables = accScriptDao.getForegnReferencesForTax(params);
                Iterator tablesiterator = tables.iterator();
                while (tablesiterator.hasNext()) {
                    Object[] tableobjects = (Object[]) tablesiterator.next();
                    String tablename = (String) tableobjects[0];
                    String columnname = (String) tableobjects[1];
                    params.put("tablename", tablename);
                    params.put("columnname", columnname);
                    params.put("companyid", companyid);
                    List doclist = accScriptDao.getDocumentDetailsForTax(params);
                    Iterator docit = doclist.iterator();
                    while (docit.hasNext()) {
                        String docid = (String) docit.next();
                        JSONObject nObject = new JSONObject();
                        nObject.put("docid", docid);
                        nObject.put("tablename", tablename);
                        nObject.put("subdomain", subdomain);
                        array.put(nObject);
                    }
                }
            }
            params.put("data", array);
            if (array.length() > 1) {
                writedataFor_TaxDoc(wb, params);
            }
            params.put("responselist", returnlist);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return wb;
    }

    @Override
    public JSONObject DeleteEmptyValuedFieldcomboValuesMappedToEntityCustomField(JSONObject params) throws ServiceException {

        KwlReturnObject kwlReturnObject = null;
        JSONObject jSONObject = new JSONObject();

        Map map = new HashMap();
        String message = "";
        String message1 = "";
        Connection conn = null;
        JSONObject jobj = null;
        JSONArray Jarr = null;
      

        try {
            File tempDir = new File(storageHandlerImpl.GetDocStorePath() + "Scriptdata");
            File file2 = new File(storageHandlerImpl.GetDocStorePath() + "Scriptdata.zip");
            if (tempDir.exists()) {
                FileUtils.deleteDirectory(tempDir);

                tempDir.delete();
}
            if (file2.exists()) {

                FileUtils.deleteQuietly(file2);
            }
            if (!tempDir.exists()) {
                // attempt to create the directory here
                boolean successful = tempDir.mkdir();
                if (successful) {
                    // creating the directory succeeded
                    //to do
                    System.out.println("directory was created successfully");
                } else {
                    // creating the directory failed
                    System.out.println("failed trying to create the directory");
                }
            }
            String serverip = params.getString("serverip");
            String port = "3306";
            String dbName = params.getString("dbname");
            String userName = params.getString("username");
            String password = params.getString("password");

            String subDomain = params.has("subdomain") ? params.getString("subdomain") : "";
            String fieldlabel = params.getString("fieldlabel");
            if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
                throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
            }
            String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            String driver = "com.mysql.jdbc.Driver";

            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectString, userName, password);
            String subdomainQuery = "";
            if (!StringUtil.isNullOrEmpty(subDomain)) {
                subdomainQuery = " WHERE c.subdomain='" + subDomain + "'";
            }

            String query1 = "";
            ResultSet rst1 = null;
            PreparedStatement pst1 = null;
            int companycount = 0;

            query1 = "SELECT c.companyid, c.subdomain FROM company c " + subdomainQuery;
            pst1 = (PreparedStatement) conn.prepareStatement(query1);
            rst1 = pst1.executeQuery();
            while (rst1.next()) {
                HSSFWorkbook wb = null;
                subDomain = rst1.getString("subdomain");
                companycount++;
                if (!StringUtil.isNullOrEmpty(subDomain) && !StringUtil.isNullOrEmpty(fieldlabel)) {

                    PreparedStatement stmtquery;
                    String query = "";
                    ResultSet rs;
                    String company = "";
                    String customquery = "";
                    ResultSet custrs;

//             get fieldid and column no for respective dimention
                    customquery = "select id,colnum,companyid,moduleid,customcolumn,relatedmoduleid from fieldparams where fieldlabel=? and companyid in (select companyid from company where subdomain=?)";
                    stmtquery = (PreparedStatement) conn.prepareStatement(customquery);
                    stmtquery.setString(1, fieldlabel);
                    stmtquery.setString(2, subDomain);
                    custrs = stmtquery.executeQuery();

                    while (custrs.next()) {
                        String fieldId = "";
                        long column = 1;
                        int globalcount = 0;
                        String globalTable = "";
                        String DetailTable = "";
                        String DetailTableid = "";
                        String customgolbaltable = "";
                        String customgolbaltableid = "";
                        String customdetailTable = "";
                        String refkey = "";
                        String productMasterTable = "";

                        fieldId = custrs.getString("id");
                        column = custrs.getLong("colnum");
                        company = custrs.getString("companyid");
                        int module = custrs.getInt("moduleid");
                        int customColumn = custrs.getInt("customcolumn");
                        String relatedModuleIds = custrs.getString("relatedmoduleid");
                        switch (module) {
                            case 27:
                            case 41:
                            case 51:
                            case 67:
                                globalTable = "deliveryorder";
                                DetailTable = "dodetails";
                                DetailTableid = "dodetailsid";          //id in customdetail table
                                customgolbaltable = "deliveryordercustomdata";
                                customgolbaltableid = "deliveryOrderId";
                                customdetailTable = "dodetailscustomdata";
                                //                    customdetailTableid="dodetailsid";
//                        refid = "accdodetailscustomdataref";
                                break;
                            case 28:
                            case 40:
                            case 57:
                                globalTable = "grorder";
                                DetailTable = "grodetails";
                                DetailTableid = "grodetailsid";
                                customgolbaltable = "grordercustomdata";
                                customgolbaltableid = "goodsreceiptorderid";
                                customdetailTable = "grodetailscustomdata";
                                //                    customdetailTableid="grodetailsid";
//                        refid = "accgrodetailscustomdataref";
                                break;
                            case 18:        //PO
                            case 63:
                            case 90:
                                globalTable = "purchaseorder";
                                DetailTable = "podetails";
                                DetailTableid = "poDetailID";
                                customgolbaltable = "purchaseordercustomdata";
                                customgolbaltableid = "poID";
                                customdetailTable = "purchaseorderdetailcustomdata";
                                //                    customdetailTableid="grodetailsid";
//                        refid = "purchaseorderdetailcustomdataref";
                                break;
                            case 20:            //SO
                            case 36:            //LO
                            case 50:
                                globalTable = "salesorder";
                                DetailTable = "sodetails";
                                DetailTableid = "soDetailID";
                                customgolbaltable = "salesordercustomdata";
                                customgolbaltableid = "soID";
                                customdetailTable = "salesorderdetailcustomdata";
                                //                    customdetailTableid="grodetailsid";
//                        refid = "salesorderdetailcustomdataref";
                                break;
                            case 23:        //VQ
                            case 89:
                                globalTable = "vendorquotation";
                                DetailTable = "vendorquotationdetails";
                                DetailTableid = "vendorquotationdetailsid";
                                customgolbaltable = "vendorquotationcustomdata";
                                customgolbaltableid = "vendorquotationid";
                                customdetailTable = "vendorquotationdetailscustomdata";
                                //                    customdetailTableid="grodetailsid";
//                        refid = "accvendorquotationdetailscustomdataref";
                                break;
                            case 22:        //Cq
                            case 65:
                                globalTable = "quotation";
                                DetailTable = "quotationdetails";
                                DetailTableid = "quotationdetailsid";
                                customgolbaltable = "quotationcustomdata";
                                customgolbaltableid = "quotationid";
                                customdetailTable = "quotationdetailscustomdata";
                                //                    customdetailTableid="grodetailsid";
//                        refid = "accquotationdetailscustomdataref";
                                break;
                            case 29:        //ScreateExcelR
                            case 53:
                            case 68:
                            case 98:
                                globalTable = "salesreturn";
                                DetailTable = "srdetails";
                                DetailTableid = "srdetailsid";
                                customgolbaltable = "salesreturncustomdata";
                                customgolbaltableid = "salesreturnid";
                                customdetailTable = "srdetailscustomdata";
                                //                    customdetailTableid="grodetailsid";
//                        refid = "accsrdetailsscustomdataref";
                                break;
                            case 31:        //PR
                            case 59:
                            case 96:
                                globalTable = "purchasereturn";
                                DetailTable = "prdetails";
                                DetailTableid = "prdetailsid";
                                customgolbaltable = "purchasereturncustomdata";
                                customgolbaltableid = "purchasereturnid";
                                customdetailTable = "prdetailscustomdata";
                                //                    customdetailTableid="grodetailsid";
//                        refid = "accprdetailscustomdataref";
                                break;
                            case 32:        //PRqui
                            case 87:
                                globalTable = "purchaserequisition";
                                DetailTable = "purchaserequisitiondetail";
                                DetailTableid = "purchaserequisitiondetailid";
                                customgolbaltable = "purchaserequisitioncustomdata";
                                customgolbaltableid = "purchaserequisitionid";
                                customdetailTable = "purchaserequisitiondetailcustomdata";
                                //                    customdetailTableid="grodetailsid";
//                        refid = "accpurchaserequisitiondetailcustomdataref";
                                break;
                            case 33:
                            case 88:        //RFQ
                                customgolbaltable = "rfqcustomdata";
                                customgolbaltableid = "rfqid";
                                customdetailTable = "requestforquotationdetailcustomdata";
                                DetailTableid = "requestforquotationdetailid";
                                break;
                            case 6:     //VI
                            case 39:     //VI
                            case 58:
                            case 2:     //CI
                            case 38:     //CI
                            case 52:
                            case 14:    //MP
                            case 16:        //RP
                            case 10:        //DN
                            case 12:        //CN
                            case 93:
                                customgolbaltable = "accjecustomdata";
                                customgolbaltableid = "journalentryId";
                                customdetailTable = "accjedetailcustomdata";
                                DetailTableid = "jedetailId";
                                break;
                            case 30:        //product
                            case 42:        //group
                                customgolbaltable = "accproductcustomdata";
                                customgolbaltableid = "productId";
                                break;
                            case 34:        //account
                                customgolbaltable = "accountcustomdata";
                                customgolbaltableid = "accountId";
                                break;
                            case 24:        //JE
                                customgolbaltable = "accjecustomdata";
                                customgolbaltableid = "journalentryId";
                                customdetailTable = "accjedetailcustomdata";
                                DetailTableid = "jedetailId";
                                break;
                            case 35:        //contract
                                customgolbaltable = "contractcustomdata";
                                customgolbaltableid = "contractid";
                                break;
                            case 25:        //Customer
                                customgolbaltable = "customercustomdata";
                                customgolbaltableid = "customerId";
                                break;
                            case 26:        //Vendor
                                customgolbaltable = "vendorcustomdata";
                                customgolbaltableid = "vendorId";
                                break;
                            ///////
                            case 64:
                                customgolbaltable = "contractcustomdata";
                                customgolbaltableid = "contractid";
                                customdetailTable = "contractdetailcustomdata";
                                DetailTableid = "scDetailID";
                                break;
                            case 92:
                            case 1001:
                                customgolbaltable = "stockcustomdata";
                                customgolbaltableid = "stockId";
                                customdetailTable = "stockcustomdata";
                                DetailTableid = "stockId";
                                break;
                            case 1002:
                            case 1003:
                                customgolbaltable = "in_interstoretransfer_customdata";
                                customgolbaltableid = "istid";
                                customdetailTable = "in_interstoretransfer_customdata";
                                DetailTableid = "istid";
                                break;
                            case 1004:
                                customgolbaltable = "cyclecountcustomdata";
                                customgolbaltableid = "ccid";
                                break;
                            case 1101:
                                customgolbaltable = "labourcustomdata";
                                customgolbaltableid = "labourId";
                                customdetailTable = "labourcustomdata";
                                DetailTableid = "labourId";
                                break;
                            case 1102:
                                customgolbaltable = "workcentrecustomdata";
                                customgolbaltableid = "workCentreId";
                                customdetailTable = "workcentrecustomdata";
                                DetailTableid = "workCentreId";
                                break;
                            case 1103:
                                customgolbaltable = "machinecustomdata";
                                customgolbaltableid = "machineId";
                                customdetailTable = "machinecustomdata";
                                DetailTableid = "machineId";
                                break;
                            case 1104:
                                customgolbaltable = "jobworkcustomdata";
                                customgolbaltableid = "jobworkId";
                                customdetailTable = "jobworkcustomdata";
                                DetailTableid = "jobworkId";
                                break;
                            case 1105:
                                customgolbaltable = "workordercustomdata";
                                customgolbaltableid = "jobworkId";
                                customdetailTable = "workordercustomdata";
                                DetailTableid = "jobworkId";
                                break;
                            case 1106:
                                customgolbaltable = "mrpcontractcustomdata";
                                customgolbaltableid = "contractId";
                                customdetailTable = "mrpcontractdetailscustomdata";
                                DetailTableid = "contractDetailsId";
                                break;
                            case 1107:
                                customgolbaltable = "routingtemplatecustomdata";
                                customgolbaltableid = "routingTemplateId";
                                customdetailTable = "routingtemplatecustomdata";
                                DetailTableid = "routingTemplateId";
                                break;
                            case 1114:
                                customgolbaltable = "salesordercustomdata";
                                customgolbaltableid = "soID";
                                customdetailTable = "salesorderdetailcustomdata";
                                DetailTableid = "soDetailID";
                                break;
                            case 1115:
                                customgolbaltable = "purchaseordercustomdata";
                                customgolbaltableid = "poID";
                                customdetailTable = "purchaseorderdetailcustomdata";
                                DetailTableid = "poDetailID";
                                break;
                            case 95:
                                customgolbaltable = "in_stockadjustment_customdata";
                                customgolbaltableid = "stockadjustmentid";
                                customdetailTable = "in_stockadjustment_customdata";
                                DetailTableid = "stockadjustmentid";
                                break;
                            case 79:        //Serial Window
                                customgolbaltable = "serialcustomdata";
                                customgolbaltableid = "serialdocumentmappingid";
                                break;
                            ///////
                            case 1200:        //multientity
                                customgolbaltable = "entitybasedlineleveltermsrate";
                                customgolbaltableid = "id";
                                break;

                            case 121:
                                customgolbaltable = "assetdetailcustomdata";
                                customgolbaltableid = "assetDetailsId";
                                break;
                        }

                        if (customColumn == 0) {  //If global field
                            if (module != 30) {
                                //delete from custom global table
                                map = udpateCustomTable(conn, customgolbaltableid, customgolbaltable, column, module, company, fieldlabel);
                                if (map.containsKey("recordsList")) {
                                    Jarr = (JSONArray) map.get("recordsList");
                                    jobj = new JSONObject();
                                    jobj.put("data", Jarr);
                                    jobj.put("sheetname", ""+ module);
                                  wb=createExcel(wb, jobj);
                                    int count = (Integer) map.get("count");
                                    globalcount = globalcount + count;

                                }


                            } else {
                                if (!StringUtil.isNullOrEmpty(relatedModuleIds)) {
                                    String[] moduleIdStr = relatedModuleIds.split(",");
                                    for (int cnt = 0; cnt < moduleIdStr.length; cnt++) {
                                        int relatedmodule = Integer.parseInt(moduleIdStr[cnt]);
                                        switch (relatedmodule) {

                                            case 27:
                                                productMasterTable = "dodetailproductcustomdata";
                                                refkey = "doDetailID";
                                                break;
                                            case 18:        //PO
                                                productMasterTable = "podetailproductcustomdata";
                                                refkey = "poDetailID";
                                                break;
                                            case 20:            //SO
                                                productMasterTable = "sodetailproductcustomdata";
                                                refkey = "soDetailID";
                                                break;
                                            case 2:     //CI
                                            case 6:     //VI
                                                productMasterTable = "accjedetailproductcustomdata";
                                                refkey = "jedetailId";
                                                break;
                                        }
                                         int rscount = 0;
                                        JSONArray recordsList = new JSONArray();
                                        String fieldcombodataid = "";
                                        String colquery = "select fcd.id  as fieldcombodataid,c.subdomain as companyname  from fieldcombodata fcd   inner join fieldparams on fieldparams.id=fcd.fieldid  inner join company c on c.companyid=fieldparams.companyid    where  fieldparams.fieldlabel='" + fieldlabel + "'  and (fcd.value='' or fcd.value='None') and fieldparams.moduleId=? and fieldparams.companyid=?";
                                         stmtquery = (PreparedStatement) conn.prepareStatement(colquery);
                                        stmtquery.setInt(1, module);
                                        stmtquery.setString(2, company);
                                        ResultSet resultSet1 = stmtquery.executeQuery();
                                        String companyname = resultSet1.getString("companyname");

                                        while (resultSet1.next()) {
                                            fieldcombodataid = resultSet1.getString("fieldcombodataid");

                                            String columnData = "SELECT " + refkey + " from " + productMasterTable + " WHERE col" + column + "=" + fieldcombodataid + "  AND moduleId=? AND company=?";
                                            System.out.println(columnData);
                                            stmtquery = (PreparedStatement) conn.prepareStatement(columnData);
                                            stmtquery.setInt(1, relatedmodule);
                                            stmtquery.setString(2, company);
                                            ResultSet globaltablers = stmtquery.executeQuery();
    

                                            while (globaltablers.next()) {
                                                jobj = new JSONObject();
                                                jobj.put("tablename", productMasterTable);
                                                jobj.put("id", refkey);
                                                jobj.put("recordno", globaltablers.getString(refkey));
                                                jobj.put("fieldcombodataid", fieldcombodataid);
                                                jobj.put("companyname", companyname);
                                                recordsList.put(jobj);
                                                rscount++;
                                                globalcount++;
                                            }
                                            if (rscount == 0) {
                                                            colquery = " delete from fieldcombodata where id=? ";
                                                            PreparedStatement preparedStatement = (PreparedStatement) conn.prepareStatement(colquery);
                                                            preparedStatement.setString(1, fieldcombodataid);
                                                            int count3 = preparedStatement.executeUpdate();
                                                            System.out.println("Entry deleted for " + fieldcombodataid + "" + colquery + "" + count3 + "");
                                                        } 

                                            if (rscount > 0) {

                                                Jarr = (JSONArray) map.get("recordsList");
                                                jobj.put("data", Jarr);
                                                jobj.put("sheetname", ""+ module);
                                                wb = createExcel(wb, jobj);
                                            }
                                        }
                                          //to do
                                    }
                                }
                                map = udpateCustomTable(conn, customgolbaltableid, customgolbaltable, column, module, company, fieldlabel);
                                if (map.containsKey("recordsList")) {
                                    Jarr = (JSONArray) map.get("recordsList");
                                    jobj.put("data", Jarr);
                                     jobj.put("sheetname", ""+ module);
                                  wb=createExcel(wb, jobj);
                                    int count = (Integer) map.get("count");
                                    globalcount = globalcount + count;

                                }
                            }

                        } else if (customColumn == 1 && StringUtil.isNullOrEmpty(customdetailTable)) {
                            //delete from custom global table
                            map = udpateCustomTable(conn, customgolbaltableid, customgolbaltable, column, module, company, fieldlabel);
                            if (map.containsKey("recordsList")) {
                                Jarr = (JSONArray) map.get("recordsList");
                                jobj.put("data", Jarr);
                                 jobj.put("sheetname", ""+ module);
                              wb=createExcel(wb, jobj);
                                int count = (Integer) map.get("count");
                                globalcount = globalcount + count;

                            }

                        } else {
                            //delete from custom detail table.
                            map = udpateCustomTable(conn, DetailTableid, customdetailTable, column, module, company, fieldlabel);
                            if (map.containsKey("recordsList")) {
                                Jarr = (JSONArray) map.get("recordsList");
                                jobj.put("data", Jarr);
                                 jobj.put("sheetname", ""+ module);
                               wb=createExcel(wb, jobj);
                                int count = (Integer) map.get("count");
                                globalcount = globalcount + count;

                            }
                        }

                        message1 += "<br><b>Module: </b>" + module + "&nbsp;&nbsp;&nbsp;&nbsp;<b>Count: </b>" + globalcount;
                    }
                }

                message += "<b>Data updated for Dimension/Custom Field Values.<b>";
                message += "<br><b>Subdomain:</b>" + subDomain + "<br><b>Count:</b>" + message1;

                // writeXLSDataToFile("CourruptFieldCombodataRecords", "xls", wb, response);
                File f1 = null;
                try {
                    if(wb!=null){
                    f1 = new File(storageHandlerImpl.GetDocStorePath() + "Scriptdata/" + subDomain + ".xls");
                    FileOutputStream outputStream = new FileOutputStream(f1);
                        wb.write(outputStream);
                    outputStream.flush();
                    outputStream.close();
                    }
                    wb = null;

                } catch (Exception e) {
                    e.printStackTrace();

                }


//                FileOutputStream fos;
//
//                fos = new FileOutputStream(storageHandlerImpl.GetDocStorePath() + "Scriptdata.zip");
//                try (ZipOutputStream zipOS = new ZipOutputStream(fos)) {
//                    writeToZipFile(storageHandlerImpl.GetDocStorePath() + "Scriptdata/" + subDomain + ".xls", zipOS);
//                }
//                fos.close();

            }
            FileOutputStream fos;

            fos = new FileOutputStream(storageHandlerImpl.GetDocStorePath() + "Scriptdata.zip");

            try (ZipOutputStream zipOS = new ZipOutputStream(fos)) {
                query1 = "SELECT c.companyid, c.subdomain FROM company c " + subdomainQuery;
                pst1 = (PreparedStatement) conn.prepareStatement(query1);
                rst1 = pst1.executeQuery();
                while (rst1.next()) {
                    subDomain = rst1.getString("subdomain");
                    writeToZipFile(storageHandlerImpl.GetDocStorePath() + "Scriptdata/" + subDomain + ".xls", zipOS);
                }
            }
            fos.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return params;
    }
    /**
     * Delete dimension values if not used and if used in transaction then export with excel file
     * @param params
     * @return
     * @throws ServiceException 
     */
    @Override
    public JSONObject deleteEmptyAndNoneValuesFromCustomDimension(JSONObject params) throws ServiceException {
        Map map = new HashMap();
        JSONObject jobj = null;
        JSONArray Jarr = null;
        try {
            /**
             * Create temporary directories to export used data subdomain wise
             */
            File tempDir = new File(storageHandlerImpl.GetDocStorePath() + "DelDimValueScriptdata");
            File file2 = new File(storageHandlerImpl.GetDocStorePath() + "DelDimValueScriptdata.zip");
            if (tempDir.exists()) {
                FileUtils.deleteDirectory(tempDir);
                tempDir.delete();
            }
            if (file2.exists()) {
                FileUtils.deleteQuietly(file2);
            }
            if (!tempDir.exists()) {
                // attempt to create the directory here
                boolean successful = tempDir.mkdir();
                if (successful) {
                    // creating the directory succeeded
                    //to do
                    System.out.println("directory was created successfully");
                } else {
                    // creating the directory failed
                    System.out.println("failed trying to create the directory");
                }
            }
            String subDomainSpecific = params.optString("subdomain");
            String fieldlabel = params.optString("fieldlabel", "");
            /**
             * delete not used records if isDeleteUnUsedRecords flag true
             */
            boolean isDeleteUnUsedRecords = params.optBoolean("isDeleteUnUsedRecords", false);
            String excelSheetName = "";
            String companyQuery = "";
            companyQuery = "SELECT c.companyid, c.subdomain FROM company c inner join extracompanypreferences ecp on c.companyid=ecp.id  where c.country=105 and (ecp.isnewgst='T' or ecp.isnewgst=1) ";

            List queryParams = new ArrayList();
            String subdomainQuery = "";
            if (!StringUtil.isNullOrEmpty(subDomainSpecific)) {
                subdomainQuery = " and c.subdomain= ?";
                queryParams.add(subDomainSpecific);
            }
            List<Object[]> companyResult = accScriptDao.getCommonQueryResultForDimensionValueScript(params, companyQuery + subdomainQuery, queryParams, false);

            /*
            * Get Custom Table and its All details
            */
            
            KwlReturnObject kwlReturnObject = accMasterItemsDAOobj.getCustomTableName(new JSONObject());
            List customTableList = kwlReturnObject.getEntityList();
            JSONArray bulkData = accMasterItemsDAOobj.createJsonForCustomTableList(customTableList);
            
            if (companyResult != null) {
                for (Object companyResultObj[] : companyResult) {
                    String subDomain = (String) companyResultObj[1];
                    String companyid = (String) companyResultObj[0];
                    HSSFWorkbook wb = null;
                    if (!StringUtil.isNullOrEmpty(subDomain)) {
                        //get fieldid and column no for respective dimention
                        String customquery = "select id,colnum,moduleid,customcolumn,relatedmoduleid, gstmappingcolnum, fieldlabel from fieldparams where customfield=0 and companyid=? ";
                        queryParams = new ArrayList();
                        queryParams.add(companyid);
                        List<Object[]> fielParamsList = accScriptDao.getCommonQueryResultForDimensionValueScript(params, customquery, queryParams,false);
                        if (fielParamsList != null) {
                            for (Object fielParamsListObj[] : fielParamsList) {
                                int column = 1;
                                int globalcount = 0;
                                String DetailTableid = "";
                                String customgolbaltable = "";
                                String customgolbaltableid = "";
                                String customdetailTable = "";
                                String refkey = "";
                                String productMasterTable = "";
                                /**
                                 * Get Dimension fields details
                                 */
                                column = Integer.parseInt(fielParamsListObj[1].toString());
                                int module = Integer.parseInt(fielParamsListObj[2].toString());
                                int customColumn = Integer.parseInt(fielParamsListObj[3].toString());
                                String relatedModuleIds = (String) fielParamsListObj[4];
                                int gstmappingcolnum = Integer.parseInt(fielParamsListObj[5].toString());

                                fieldlabel = (String) fielParamsListObj[6];
                                if (StringUtil.isNullOrEmpty(fieldlabel)) {
                                    continue;
                                }
                                HashMap<String, Object> moduleParams = new HashMap<String, Object>();
                                moduleParams.put("filter_names", Arrays.asList("id"));
                                moduleParams.put("filter_values", Arrays.asList(String.valueOf(module)));
                                KwlReturnObject kmsg = accAccountDAOobj.getModules(moduleParams);
                                excelSheetName = String.valueOf(module);
                                if (kmsg!=null && kmsg.isSuccessFlag() && kmsg.getEntityList()!=null && !kmsg.getEntityList().isEmpty()) {
                                    Modules modObj = (Modules) kmsg.getEntityList().get(0);
                                    excelSheetName = modObj.getModuleName() + "("+module+")_" + fieldlabel.replaceAll("[^a-zA-Z0-9]", "");
                                }
                                /**
                                 * Get Custom tables details based on module Id
                                 */
                                JSONObject obj = new JSONObject();
                                obj.put("module", module);
                                JSONArray customTablesArrayObj = StringUtil.findAllJsonObjectFromJsonArray(bulkData, obj);
                                if (customTablesArrayObj.length() >= 1) {
                                    JSONObject globalObj = customTablesArrayObj.getJSONObject(0);
                                    customgolbaltable = globalObj.optString("reftable", "");
                                    customgolbaltableid = globalObj.optString("refprimarykey", "");
                                }
                                if (customTablesArrayObj.length() >= 2) {
                                    JSONObject detailsObj = customTablesArrayObj.getJSONObject(1);
                                    customdetailTable = detailsObj.optString("reftable", "");
                                    DetailTableid = detailsObj.optString("refprimarykey", "");
                                }
                                /**
                                 * Entity Rule module table name and table primary key column name
                                 */
                                if (module == 1200) {
                                    customgolbaltable = "entitybasedlineleveltermsrate";
                                    customgolbaltableid = "id";
                                }
                                if (customColumn == 0) {  //If global field
                                    if (module != 30) {
                                        //delete from custom global table
                                        map = udpateCustomTableDataIfNotUsed(params, customgolbaltableid, customgolbaltable, column, module, companyid, fieldlabel, gstmappingcolnum, subDomain);
                                        if (map.containsKey("recordsList")) {
                                            Jarr = (JSONArray) map.get("recordsList");
                                            jobj = new JSONObject();
                                            jobj.put("data", Jarr);
                                            jobj.put("sheetname", excelSheetName);
                                            wb = createExcelForCustomDimensionDelete(wb, jobj);
                                            int count = (Integer) map.get("count");
                                            globalcount = globalcount + count;
                                        }
                                    } else {
                                        if (!StringUtil.isNullOrEmpty(relatedModuleIds)) {
                                            String[] moduleIdStr = relatedModuleIds.split(",");
                                            for (int cnt = 0; cnt < moduleIdStr.length; cnt++) {
                                                int relatedmodule = Integer.parseInt(moduleIdStr[cnt]);
                                                switch (relatedmodule) {

                                                    case 27:
                                                        productMasterTable = "dodetailproductcustomdata";
                                                        refkey = "doDetailID";
                                                        break;
                                                    case 18:        //PO
                                                        productMasterTable = "podetailproductcustomdata";
                                                        refkey = "poDetailID";
                                                        break;
                                                    case 20:            //SO
                                                        productMasterTable = "sodetailproductcustomdata";
                                                        refkey = "soDetailID";
                                                        break;
                                                    case 2:     //CI
                                                    case 6:     //VI
                                                        productMasterTable = "accjedetailproductcustomdata";
                                                        refkey = "jedetailId";
                                                        break;
                                                }
                                                int rscount = 0;
                                                JSONArray recordsList = new JSONArray();
                                                String fieldcombodataid = "";
                                                String relatedModuleQuery = "select fcd.id , fcd.value  from fieldcombodata fcd   inner join fieldparams "
                                                        + " on fieldparams.id=fcd.fieldid   inner join company c on c.companyid=fieldparams.companyid   "
                                                        + "  where  fieldparams.fieldlabel=?  and (fcd.value='' or fcd.value='None') and fieldparams.moduleId=? "
                                                        + " and fieldparams.companyid=?";
                                                queryParams = new ArrayList();
                                                queryParams.add(fieldlabel);
                                                queryParams.add(module);
                                                queryParams.add(companyid);
                                                List<Object[]> relatedModuleDataList = accScriptDao.getCommonQueryResultForDimensionValueScript(params, relatedModuleQuery, queryParams, false);
                                                if (relatedModuleDataList != null) {
                                                    for (Object relatedModuleDataListObj[] : relatedModuleDataList) {
                                                        fieldcombodataid = (String) relatedModuleDataListObj[0];
                                                        String value = (String) relatedModuleDataListObj[1];
                                                        String CustomDataQuery = "SELECT " + refkey + " , 'CustomTable' as CustomTable from " + productMasterTable + " WHERE col" + column + "=" + fieldcombodataid + "  AND moduleId=? AND company=?";
                                                        queryParams = new ArrayList();
                                                        queryParams.add(fieldlabel);
                                                        queryParams.add(module);
                                                        queryParams.add(companyid);
                                                        List<Object[]> relatedCustomModuleDataList = accScriptDao.getCommonQueryResultForDimensionValueScript(params, CustomDataQuery, queryParams, false);
                                                        if (relatedCustomModuleDataList != null) {
                                                            for (Object relatedCustomModuleDataListObj[] : relatedCustomModuleDataList) {
                                                                String custmtableRecordId = (String) relatedCustomModuleDataListObj[0];
                                                                jobj = new JSONObject();
                                                                jobj.put("tablename", productMasterTable);
                                                                jobj.put("id", refkey);
                                                                /**
                                                                 * Get Product name where this dimension value used 
                                                                 */
                                                                if (!StringUtil.isNullOrEmpty(custmtableRecordId)) {
                                                                    Map<String, Object> reqmap = new HashMap<>();
                                                                    Object res = null;
                                                                    reqmap.put("ID", custmtableRecordId);
                                                                    reqmap.put("company", companyid);
                                                                    res = kwlCommonTablesDAOObj.getRequestedObjectFields(Product.class, new String[]{"productid"}, reqmap);
                                                                    String productName = res != null ? (String) res : "";
                                                                    jobj.put("productName", productName);
                                                                }
                                                                jobj.put("recordno", custmtableRecordId);
                                                                jobj.put("fieldcombodataid", fieldcombodataid);
                                                                jobj.put("companyname", subDomain);
                                                                jobj.put("fieldlabel", fieldlabel);
                                                                jobj.put("value", value);
                                                                jobj.put("isUsed", true);
                                                                jobj.put("isDeleted", false);
                                                                recordsList.put(jobj);
                                                                rscount++;
                                                                globalcount++;
                                                            }
                                                        }
                                                        if (relatedCustomModuleDataList == null || relatedCustomModuleDataList.isEmpty()) {
                                                            jobj = new JSONObject();
                                                            jobj.put("tablename", productMasterTable);
                                                            jobj.put("id", refkey);
                                                            jobj.put("recordno", "");
                                                            jobj.put("fieldcombodataid", fieldcombodataid);
                                                            jobj.put("companyname", subDomain);
                                                            jobj.put("value", value);
                                                            jobj.put("fieldlabel", fieldlabel);
                                                            jobj.put("isDeleted", isDeleteUnUsedRecords ? true : false);
                                                            jobj.put("isUsed", false);
                                                            recordsList.put(jobj);
                                                        }
                                                        /**
                                                         * Delete dimension value from databases if not used
                                                         */
                                                        if (rscount == 0) {
                                                            if (isDeleteUnUsedRecords) {
                                                                String deleteQuery = " delete from fieldcombodata where id=? ";
                                                                queryParams = new ArrayList();
                                                                queryParams.add(fieldcombodataid);
                                                                accScriptDao.getCommonQueryResultForDimensionValueScript(params, deleteQuery, queryParams, true);
                                                            }
                                                        }
                                                        if (rscount > 0) {
                                                            Jarr = (JSONArray) map.get("recordsList");
                                                            jobj = new JSONObject();
                                                            jobj.put("data", Jarr);
                                                            jobj.put("sheetname", excelSheetName);
                                                            wb = createExcelForCustomDimensionDelete(wb, jobj);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        map = udpateCustomTableDataIfNotUsed(params, customgolbaltableid, customgolbaltable, column, module, companyid, fieldlabel, gstmappingcolnum, subDomain);
                                        if (map.containsKey("recordsList")) {
                                            Jarr = (JSONArray) map.get("recordsList");
                                            jobj = new JSONObject();
                                            jobj.put("data", Jarr);
                                            jobj.put("sheetname", excelSheetName);
                                            wb = createExcelForCustomDimensionDelete(wb, jobj);
                                            int count = (Integer) map.get("count");
                                            globalcount = globalcount + count;

                                        }
                                    }
                                } else if (customColumn == 1 && StringUtil.isNullOrEmpty(customdetailTable)) {
                                    //delete from custom global table
                                    map = udpateCustomTableDataIfNotUsed(params, customgolbaltableid, customgolbaltable, column, module, companyid, fieldlabel, gstmappingcolnum, subDomain);
                                    if (map.containsKey("recordsList")) {
                                        Jarr = (JSONArray) map.get("recordsList");
                                        jobj = new JSONObject();
                                        jobj.put("data", Jarr);
                                        jobj.put("sheetname", excelSheetName);
                                        wb = createExcelForCustomDimensionDelete(wb, jobj);
                                        int count = (Integer) map.get("count");
                                        globalcount = globalcount + count;
                                    }
                                } else {
                                    //delete from custom detail table.
                                    map = udpateCustomTableDataIfNotUsed(params, DetailTableid, customdetailTable, column, module, companyid, fieldlabel, gstmappingcolnum, subDomain);
                                    if (map.containsKey("recordsList")) {
                                        Jarr = (JSONArray) map.get("recordsList");
                                        jobj = new JSONObject();
                                        jobj.put("data", Jarr);
                                        jobj.put("sheetname", excelSheetName);
                                        wb = createExcelForCustomDimensionDelete(wb, jobj);
                                        int count = (Integer) map.get("count");
                                        globalcount = globalcount + count;
                                    }
                                }
                            }
                        }
                    }
                    /**
                     * Get All Dimension Empty Data used transaction details
                     */
                    JSONObject dimensionEmptyJSONObj = getDimensionEmptyRecords(params, companyid, subDomain, bulkData);
                    if (dimensionEmptyJSONObj.has("recordsList")) {
                        Jarr = dimensionEmptyJSONObj.optJSONArray("recordsList");
                        jobj = new JSONObject();
                        jobj.put("data", Jarr);
                        jobj.put("isAddDeleteAndUsedColumn", false);
                        jobj.put("sheetname", "Dimension Empty Records");
                        wb = createExcelForCustomDimensionDelete(wb, jobj);
                    }
                    /**
                     * Create Excel file for Subdomain 
                     */
                    File f1 = null;
                    try {
                        if (wb != null) {
                            f1 = new File(storageHandlerImpl.GetDocStorePath() + "DelDimValueScriptdata/" + subDomain + ".xls");
                            FileOutputStream outputStream = new FileOutputStream(f1);
                            wb.write(outputStream);
                            outputStream.flush();
                            outputStream.close();
                        }
                        wb = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            /**
             * Create ZIP file for All subdomain or given subdomain
             */
            FileOutputStream fos;
            fos = new FileOutputStream(storageHandlerImpl.GetDocStorePath() + "DelDimValueScriptdata.zip");
            try (ZipOutputStream zipOS = new ZipOutputStream(fos)) {
                queryParams = new ArrayList();
                subdomainQuery = "";
                if (!StringUtil.isNullOrEmpty(subDomainSpecific)) {
                    subdomainQuery = " and c.subdomain= ?";
                    queryParams.add(subDomainSpecific);
                }
                List<Object[]> companyResultForZip = accScriptDao.getCommonQueryResultForDimensionValueScript(params, companyQuery + subdomainQuery, queryParams, false);
                if (companyResult != null) {
                    for (Object companyResultForZipObj[] : companyResultForZip) {
                        String subDomain = (String) companyResultForZipObj[1];
                        writeToZipFile(storageHandlerImpl.GetDocStorePath() + "DelDimValueScriptdata/" + subDomain + ".xls", zipOS);
                    }
                }
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * Check Custom Dimension data used in Custom tables if not used then delete entries 
     * if used then add to excel sheet
     * @param conn
     * @param customgolbaltableid
     * @param customgolbaltable
     * @param column
     * @param module
     * @param company
     * @param fieldlabel
     * @param gstmappingcolnum
     * @return
     */
    public Map udpateCustomTableDataIfNotUsed(JSONObject params, String customgolbaltableid, String customgolbaltable, long column, int moduleid, String companyid, String fieldlabel, int gstmappingcolnum, String subDomain) {
        int count = 0;
        int rscount = 0;
        JSONObject jobj = null;
        Map map = new HashMap();
        JSONArray recordsList = new JSONArray();
        String fieldcombodataid = "";
        /**
         * delete not used records if isDeleteUnUsedRecords flag true
         */
        boolean isDeleteUnUsedRecords = params.optBoolean("isDeleteUnUsedRecords", false);
        try {
            String fieldComboDataQuery = "select fcd.id ,fcd.value , c.subdomain from fieldcombodata fcd  "
                    + " inner join fieldparams on fieldparams.id=fcd.fieldid  inner join company c on c.companyid=fieldparams.companyid "
                    + " where  fieldparams.fieldlabel=?  and (fcd.value='' or fcd.value='None') and fieldparams.moduleId=? "
                    + " and fieldparams.companyid=?";
            List queryParams = new ArrayList();
            queryParams.add(fieldlabel);
            queryParams.add(moduleid);
            queryParams.add(companyid);
            List<Object[]> fielComboDataList = accScriptDao.getCommonQueryResultForDimensionValueScript(params, fieldComboDataQuery, queryParams,false);
            if (fielComboDataList != null) {
                for (Object fielComboDataListObj[] : fielComboDataList) {
                    fieldcombodataid = (String) fielComboDataListObj[0];
                    String value = (String) fielComboDataListObj[1];
                    String customTableDataQuery = "";
                    /**
                     * If module id not 1200 check data and delete if not used
                     */
                    if (moduleid != 1200) {
                        customTableDataQuery = "select " + customgolbaltableid + " , 'CustomTable' as CustomTable  from " + customgolbaltable + "   where col" + column + "='" + fieldcombodataid + "' and moduleId=? and company=?";
                    } else if (moduleid == 1200) {
                        /**
                         * If module id 1200 then check data in GST rules setup
                         */
                        String whereConditionID = " entity ";
                        if (fieldlabel.equalsIgnoreCase(Constants.STATE)) {
                            /**
                             * If field label State then need to check column separately
                             * shippedloc1, shippedloc2, shippedloc3 etc
                             */
                            whereConditionID = "shippedloc" + gstmappingcolnum;
                        }
                        customTableDataQuery = "select " + customgolbaltableid + " , 'CustomTable' as CustomTable  from " + customgolbaltable + "   where " + whereConditionID + "='" + fieldcombodataid + "' ";
                    }
                    queryParams = new ArrayList();
                    if (moduleid != 1200) {
                        queryParams.add(moduleid);
                        queryParams.add(companyid);
                    }
                    rscount = 0;
                    List<Object[]> customTableDataList = accScriptDao.getCommonQueryResultForDimensionValueScript(params, customTableDataQuery, queryParams, false);
                    if (customTableDataList != null) {
                        for (Object customTableDataListObj[] : customTableDataList) {
                            String custmtableRecordId = (String) customTableDataListObj[0];
                            jobj = new JSONObject();
                            jobj.put("tablename", customgolbaltable);
                            jobj.put("id", customgolbaltableid);
                            jobj.put("recordno", custmtableRecordId);
                            jobj.put("fieldcombodataid", fieldcombodataid);
                            jobj.put("companyname", subDomain);
                            jobj.put("value", value);
                            jobj.put("fieldlabel", fieldlabel);
                            jobj.put("isDeleted", false);
                            jobj.put("isUsed", true);
                            /**
                             * If module id 30 then get values used in custom table with Product id/ Name 
                             * where value used 
                             */
                            if (moduleid == 30) {
                                if (!StringUtil.isNullOrEmpty(custmtableRecordId)) {
                                    Map<String, Object> reqmap = new HashMap<>();
                                    Object res = null;
                                    reqmap.put("ID", custmtableRecordId);
                                    reqmap.put("company.companyID", companyid);
                                    res = kwlCommonTablesDAOObj.getRequestedObjectFields(Product.class, new String[]{"productid"}, reqmap);
                                    String productName = res != null ? (String) res : "";
                                    jobj.put("productName", productName);
                                }
                            }
                            /**
                             * get Document number from custom table
                             */
                            if (moduleid != 30 && moduleid != 1200 && IndiaComplianceConstants.globalTableNames.containsKey(moduleid)
                                    && IndiaComplianceConstants.globalTableNames.get(moduleid) != null
                                    && !StringUtil.isNullOrEmpty(custmtableRecordId)) {
                                JSONObject globalTableDetails = IndiaComplianceConstants.globalTableNames.get(moduleid);
                                String globaleTableName = globalTableDetails.optString(IndiaComplianceConstants.GLOBALTABLENAME, "");
                                String documentNumberColumn = globalTableDetails.optString(IndiaComplianceConstants.DOCUMENT_NUMBER_COLUMN, "");
                                String globalTableColumn = (moduleid==2 || moduleid==6) ? " journalentry " : " id ";
                                String documentNunberQuery = " select " + documentNumberColumn + " from " + globaleTableName + " where " + globalTableColumn + "='" + custmtableRecordId + "'";
                                List<String> documentNUmberList = accScriptDao.getCommonQueryResultForDimensionValueScript(params, documentNunberQuery, new ArrayList(), false);
                                if (documentNUmberList != null && !documentNUmberList.isEmpty() && !StringUtil.isNullOrEmpty(documentNUmberList.get(0))) {
                                    jobj.put("documentNumber", documentNUmberList.get(0));
                                }
                            }
                            /**
                             * For module ID 1200 check GST rule used or not if used then get transaction number 's
                             */
                            if (moduleid == 1200) {
                                JSONObject GSTRuleCheckJobj = new JSONObject();
                                GSTRuleCheckJobj.put(Constants.id1, custmtableRecordId);
                                JSONArray GSTRuleCheckjArr = new JSONArray();
                                GSTRuleCheckjArr.put(GSTRuleCheckJobj);
                                JSONObject GSTRuleCheckParam = new JSONObject();
                                GSTRuleCheckParam.put(Constants.data, GSTRuleCheckjArr);
                                GSTRuleCheckParam.put("isGetDocumentNumber", true);
                                KwlReturnObject res = accEntityGstService.checkTermsUsed(GSTRuleCheckParam);
                                if (res != null && res.isSuccessFlag()) {
                                    List result = res.getEntityList();
                                    String usedTerms = "", usedModules = "", documentNumber = "";
                                    if (result.get(0) != null) {
                                        Map<String, String> terms = (Map) result.get(0);
                                        if (terms != null && terms.containsKey(Constants.modulename)) {
                                            usedModules += terms.get(Constants.modulename);
                                        }
                                        if (terms != null && terms.containsKey(Constants.usedTerms)) {
                                            usedTerms += terms.get(Constants.usedTerms);
                                        }
                                        if (terms != null && terms.containsKey("documentNumber")) {
                                            documentNumber += terms.get("documentNumber");
                                        }
                                    }
                                    jobj.put("usedTerms", usedTerms);
                                    jobj.put("usedModules", usedModules);
                                    jobj.put("documentNumber", documentNumber);
                                    recordsList.put(jobj);
                                    rscount++;
                                } else {
                                    /**
                                     * for 1200 module if GST rule created but not used in transaction then directly delete this entry
                                     */
                                    if (isDeleteUnUsedRecords) {
                                        String deleteQuery = " delete from prodcategorygstmapping where entitytermrate=? ";
                                        queryParams = new ArrayList();
                                        queryParams.add(custmtableRecordId);
                                        accScriptDao.getCommonQueryResultForDimensionValueScript(params, deleteQuery, queryParams, true);

                                        deleteQuery = " delete from entitybasedlineleveltermsrate where id=? ";
                                        accScriptDao.getCommonQueryResultForDimensionValueScript(params, deleteQuery, queryParams, true);
                                        jobj.put("isDeleted", true);
                                    }
                                    jobj.put("isUsed", false);
                                    recordsList.put(jobj);
                                }
                            } else {
                                recordsList.put(jobj);
                                rscount++;
                            }
                        }
                    }
                    if (customTableDataList == null || customTableDataList.isEmpty()) {
                        jobj = new JSONObject();
                        jobj.put("tablename", customgolbaltable);
                        jobj.put("id", customgolbaltableid);
                        jobj.put("recordno", "");
                        jobj.put("fieldcombodataid", fieldcombodataid);
                        jobj.put("companyname", subDomain);
                        jobj.put("value", value);
                        jobj.put("fieldlabel", fieldlabel);
                        jobj.put("isDeleted", isDeleteUnUsedRecords ? true : false);
                        jobj.put("isUsed", false);
                        recordsList.put(jobj);
                    }
                    /**
                     * Delete value if not used in transaction 
                     */
                    if (rscount == 0) {
                        if (isDeleteUnUsedRecords) {
                            String deleteQuery = " delete from fieldcombodata where id=? ";
                            queryParams = new ArrayList();
                            queryParams.add(fieldcombodataid);
                            accScriptDao.getCommonQueryResultForDimensionValueScript(params, deleteQuery, queryParams, true);
                        }
                    }
                    count++;
                }
            }
            if (count > 0) {
                map.put("recordsList", recordsList);
                map.put("count", count);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    /**
     * Get transaction where dimension value not inserted
     *
     * @param params
     * @param companyid
     * @param subDomain
     * @param bulkData
     * @return
     */
    public JSONObject getDimensionEmptyRecords(JSONObject params, String companyid, String subDomain, JSONArray bulkData) {
        JSONObject returnObj = new JSONObject();
        JSONArray recordsList = new JSONArray();
        List<String> fieldLabelList = new ArrayList<String>();
        try {
            fieldLabelList.add(Constants.STATE); // State
            fieldLabelList.add(Constants.HSN_SACCODE); // HSN/ SAC Code
            fieldLabelList.add(Constants.ENTITY); // Entity
            fieldLabelList.add(Constants.GSTProdCategory); // Product Tax Class
            String idQueryString = "";
            for (String fieldLabel : fieldLabelList) {
                StringBuilder queryBuilder = new StringBuilder();
                /**
                 * Create a query to check field value empty for transactions
                 */
                for (int i = 0; i < bulkData.length(); i++) {
                    JSONObject moduleObj = bulkData.getJSONObject(i);
                    if (moduleObj.optInt("lineitem", -1) == 0) {
                        int moduleid = moduleObj.optInt("module");
                        String reftable = moduleObj.optString("reftable");
                        String refprimarykey = moduleObj.optString("refprimarykey");
                        Map<String, Object> map = new HashMap<>();
                        map.put(Constants.fieldlabel, fieldLabel);
                        map.put(Constants.companyid, companyid);
                        map.put(Constants.moduleid, moduleid);
                        map.put("customcolumn", 0);
                        /**
                         * For state dimension no need to check in Product, Asset Group, GL Modules
                         */
                        if (fieldLabel.equalsIgnoreCase(Constants.STATE) && (moduleid == 30 || moduleid == 42 || moduleid == 24)) {
                            continue;
                        }
                        List columnList = kwlCommonTablesDAOObj.getRequestedObjectFieldsInCollection(FieldParams.class, new String[]{"colnum", "id"}, map);
                        int colnum = -1;
                        if (columnList != null && !columnList.isEmpty()) {
                            for (Object object : columnList) {
                                Object[] columnPref = (Object[]) object;
                                colnum = columnPref[0] != null ? ((Integer) columnPref[0]) : -1;
                            }
                        }
                        /**
                         * Build module wise query string to check dimension value is empty
                         */
                        if (colnum != -1 && !StringUtil.isNullOrEmpty(reftable) && !StringUtil.isNullOrEmpty(refprimarykey)
                                && IndiaComplianceConstants.globalTableNames.containsKey(moduleid)
                                && IndiaComplianceConstants.globalTableNames.get(moduleid) != null) {
                            JSONObject globalTableDetails = IndiaComplianceConstants.globalTableNames.get(moduleid);
                            String globaleTableName = globalTableDetails.optString(IndiaComplianceConstants.GLOBALTABLENAME, "");
                            String documentNumberColumn = globalTableDetails.optString(IndiaComplianceConstants.DOCUMENT_NUMBER_COLUMN, "");
                            String ModuleName = globalTableDetails.optString(IndiaComplianceConstants.MODULE_NAME, "");
                            //6,39,58,2,38,52,14,16,10,12,93
                            idQueryString = (moduleid==6 || moduleid==39 || moduleid==58|| moduleid==2 || moduleid==38 || moduleid==52 || moduleid==14 || moduleid==16 || moduleid==10 || moduleid==12 || moduleid==93) ? " journalentry in " : " id in ";
                            String query = " SELECT '" + ModuleName + "' as modulename, " + documentNumberColumn + " as documentNumber,id FROM " + globaleTableName
                                    + " WHERE " + idQueryString + " ( "
                                    + " SELECT " + refprimarykey + " FROM " + reftable + " WHERE "
                                    + " ( Col" + colnum + " is null or  Col" + colnum + " ='') "
                                    + " and company='" + companyid + "' and moduleId=" + moduleid + ") "
                                    + " and company='" + companyid + "' GROUP BY " + documentNumberColumn
                                    + " UNION ";
                            queryBuilder.append(query);
                        }
                    }
                }
                /**
                 * Execute particular field label query for all modules
                 */
                String emtpyDataQuery = queryBuilder.length() > 0 ? queryBuilder.substring(0, queryBuilder.lastIndexOf(" UNION ")) : "";
                if (!StringUtil.isNullOrEmpty(emtpyDataQuery.replaceAll("\"", "").trim())) {
                    List queryParams = new ArrayList();
                    List<Object[]> emtpyDataList = accScriptDao.getCommonQueryResultForDimensionValueScript(params, emtpyDataQuery, queryParams, false);
                    if (emtpyDataList != null) {
                        for (Object emtpyDataListObj[] : emtpyDataList) {
                            String modulename = (String) emtpyDataListObj[0];
                            String documentNumber = (String) emtpyDataListObj[1];
                            String documentid = (String) emtpyDataListObj[2];
                            JSONObject jobj = new JSONObject();
                            jobj.put("documentNumber", documentNumber);
                            jobj.put("documentid", documentid);
                            jobj.put("companyname", subDomain);
                            jobj.put("usedModules", modulename);
                            jobj.put("fieldlabel", fieldLabel);
                            recordsList.put(jobj);
                        }
                    }
                }
            }
            if (recordsList.length() > 0) {
                returnObj.put("recordsList", recordsList);
                returnObj.put("count", 0);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccScriptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnObj;
    }

    /**
     *
     * @param wb
     * @param jobj
     * @return
     * @throws JSONException
     */
    public static HSSFWorkbook createExcelForCustomDimensionDelete(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        if (wb == null) {
            wb = new HSSFWorkbook();
        }
        HSSFSheet sheet = null;
        sheet = wb.createSheet(jobj.optString("sheetname", "ABC"));
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        HSSFCellStyle rowstyle = wb.createCellStyle();
        HSSFCellStyle cellstyle = wb.createCellStyle();
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        sheet.autoSizeColumn(cellnum);
        try {
            JSONArray dataArray = jobj.getJSONArray("data");
            cellnum = 0;
            totalHeaderRow = sheet.createRow(rownum++);
            cellnum++;
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellValue(dataArray.length());
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellValue("");
            cellnum = cellnum + 2;
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(F5:F40000)");
            cellnum++;
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(H5:H40000)");
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(I5:I40000)");

            /////////// Header Start ////////////
            cellnum = 0;
            HSSFRow headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Field label");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Table Name");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Custom Table Column Name");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Custom Table Record Id");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Field ComboData Id");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Product ID");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Field combo data Value");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Subdomain Name");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Transaction Where GST Term Rule Used");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Transaction Number");
            if (jobj.optBoolean("isAddDeleteAndUsedColumn", true)) {
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue("IS Deleted");
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue("IS Used");
            }

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject invDetails = dataArray.getJSONObject(i);
                cellnum = 0;
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("fieldlabel", ""));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("tablename"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("id"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("recordno"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("fieldcombodataid"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("productName"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("value", ""));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("companyname"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("usedModules", ""));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("documentNumber", ""));
                if (jobj.optBoolean("isAddDeleteAndUsedColumn", true)) {
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(invDetails.optBoolean("isDeleted", false));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(invDetails.optBoolean("isUsed", false));
                }
            }
        } catch (JSONException exception) {
        }
        return wb;
    }
       /**
     * Add a file into Zip archive in Java.
     * 
     * @param fileName
     * @param zos
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void writeToZipFile(String path, ZipOutputStream zipStream)
            throws FileNotFoundException, IOException {

        System.out.println("Writing file : '" + path + "' to zip file");

        File aFile = new File(path);
        if(aFile.exists()){
        Path p = Paths.get(path);
        String fileName = p.getFileName().toString();
        FileInputStream fis = new FileInputStream(aFile);
        ZipEntry zipEntry = new ZipEntry("OutputFor_"+fileName);
        zipStream.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipStream.write(bytes, 0, length);
        }

        zipStream.closeEntry();
        fis.close();    
        }
        
    }

   public Map udpateCustomTable(Connection conn, String customgolbaltableid, String customgolbaltable, long column, int module, String company,String fieldlabel) {
        String recvalue = "";
        int count = 0;
        int count1 = 0;
        int rscount = 0;
        JSONObject jobj = null;
        String companyname = "";
        Map map = new HashMap();
        JSONArray recordsList = new JSONArray();
        String fieldcombodataid = "";
        try {

            String colquery = "select fcd.id  as fieldcombodataid,c.subdomain as companyname  from fieldcombodata fcd   inner join fieldparams on fieldparams.id=fcd.fieldid  inner join company c on c.companyid=fieldparams.companyid    where  fieldparams.fieldlabel='" + fieldlabel + "'  and (fcd.value='' or fcd.value='None') and fieldparams.moduleId=? and fieldparams.companyid=?";
            PreparedStatement stmtquery = (PreparedStatement) conn.prepareStatement(colquery);
            stmtquery.setInt(1, module);
            stmtquery.setString(2, company);
            ResultSet resultSet1 = stmtquery.executeQuery();

            while (resultSet1.next()) {
                fieldcombodataid = resultSet1.getString("fieldcombodataid");
                companyname = resultSet1.getString("companyname");
                if(module!=1200){
                colquery = "select " + customgolbaltableid + " from " + customgolbaltable + "   where col" + column + "='" + fieldcombodataid + "' and moduleId=? and company=?";
                }else{
                     colquery = "select " + customgolbaltableid + " from " + customgolbaltable + "   where entity='" + fieldcombodataid + "' ";
                }
                
                stmtquery = (PreparedStatement) conn.prepareStatement(colquery);
                  if(module!=1200){
                stmtquery.setInt(1, module);
                stmtquery.setString(2, company);
                }
              
                ResultSet resultSet2 = stmtquery.executeQuery();

                while (resultSet2.next()) {
                    recvalue = resultSet2.getString(customgolbaltableid);
                    jobj = new JSONObject();
                    jobj.put("tablename", customgolbaltable);
                    jobj.put("id", customgolbaltableid);
                    jobj.put("recordno", resultSet2.getString(customgolbaltableid));
                    jobj.put("fieldcombodataid", fieldcombodataid);
                    jobj.put("companyname", companyname);
                    recordsList.put(jobj);
                    rscount++;


                }
                if (rscount == 0) {
                    colquery = " delete from fieldcombodata where id=? ";
                    PreparedStatement preparedStatement = (PreparedStatement) conn.prepareStatement(colquery);
                    preparedStatement.setString(1, fieldcombodataid);
                    int count3 = preparedStatement.executeUpdate();
                    System.out.println("Entry deleted for " + fieldcombodataid + "" + colquery + "" + count3 + "");
                } 

                count++;

            }
            if (rscount > 0) {
                map.put("recordsList", recordsList);
                map.put("count", count);

            }



        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return map;
    }
    public static HSSFWorkbook createExcel(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        
       if(wb==null){
           wb=new HSSFWorkbook();
       }
        HSSFSheet sheet = null;
            sheet = wb.createSheet(jobj.optString("sheetname", "b2b"));
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        HSSFCellStyle rowstyle = wb.createCellStyle();
        HSSFCellStyle cellstyle = wb.createCellStyle();
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        sheet.autoSizeColumn(cellnum);
        try {
            JSONArray b2bArray = jobj.getJSONArray("data");



            cellnum = 0;
            totalHeaderRow = sheet.createRow(rownum++);
            cellnum++;
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellValue(b2bArray.length());
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellValue("");
            cellnum = cellnum + 2;
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(F5:F40000)");
            cellnum++;
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(H5:H40000)");
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(I5:I40000)");

            /////////// Header Start ////////////
            cellnum = 0;
            HSSFRow headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("TableName");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("Id");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("recordno");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("fieldcombodataid");
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("CompanyName");

            for (int i = 0; i < b2bArray.length(); i++) {

                JSONObject invDetails = b2bArray.getJSONObject(i);
                cellnum = 0;
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("tablename"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("id"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("recordno"));

                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("fieldcombodataid"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("companyname"));

            }
            
        } catch (JSONException exception) {
        }
         return wb;
    }
         public void writeXLSDataToFile(String filename, String fileType, HSSFWorkbook wb, HttpServletResponse response) throws ServiceException {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".xls\"");
            wb.write(response.getOutputStream());
            response.getOutputStream().close();
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            try {
                response.getOutputStream().println("{\"valid\": false}");
            } catch (Exception ex) {
                Logger.getLogger(exportDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Function to insert GST Fields data
     *
     * @param params
     * @return
     * @throws ServiceException
     */
    public JSONObject insertGSTFieldsData(JSONObject params) throws ServiceException {
        KwlReturnObject kwlReturnObject = null;
        JSONObject jSONObject = new JSONObject();
        List returnlist = new ArrayList();
        try {
            /**
             * Get all Indian company
             */
            List companyList = accScriptDao.getCompanyData(params);
            JSONObject copyParams = params;
            Iterator iterator = companyList.iterator();
            while (iterator.hasNext()) {
                params = copyParams;
                Object[] objects = (Object[]) iterator.next();
                String companyid = (String) objects[0];
                String userid = (String) objects[1];
                Date firstfyfrom = (Date) objects[2];
                params.put("companyid", companyid);
                params.put("userid", userid);
                params.put("firstfyfrom", firstfyfrom);
                /**
                 * Insert Product master data
                 */
                insertProductTaxClassData(params);

                /**
                 * Insert Customer GST field data
                 */
                accScriptDao.insertCustomerGSTHistory(params);

                /**
                 * Insert Vendor GST field data
                 */
                accScriptDao.insertVendorGSTHistory(params);

                /**
                 * Insert Transaction data
                 */
                insertTransactionGSTFieldsData(params);

                /**
                 * Asset migration
                 */
                insertProductTaxClassDataForAsset(params);
                insertTransactionGSTFieldsDataForAsset(params);
            }
            params.put("responselist", returnlist);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return params;
    }

    /**
     * Function to insert Product Tax Class history for products
     *
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject insertProductTaxClassData(JSONObject params) throws ServiceException, JSONException {
        /**
         * get Field Param id and column no for tax class field
         */
        params.put("fieldname", Constants.GSTProdCategory);
        params.put("moduleid", Constants.Acc_Product_Master_ModuleId);
        List fieldparamsList = accScriptDao.getFieldParamsData(params);
        Iterator iterator = fieldparamsList.iterator();
        while (iterator.hasNext()) {
            Object[] objects = (Object[]) iterator.next();
            String fieldid = (String) objects[0];
            int colnum = (Short) objects[1];
            params.put("taxclassfieldid", fieldid);
            params.put("taxclasscolnum", colnum);
        }
        /**
         * Iterate all products one by one
         */
        List productList = accScriptDao.getProductsForTaxClassHistory(params);
        iterator = productList.iterator();
        while (iterator.hasNext()) {
            Object[] objects = (Object[]) iterator.next();
            String productid = (String) objects[0];
            String taxclassvalue = (String) objects[1];
            Date creationdate = objects[2] != null ? (Date) objects[2] : new Date();
            params.put("productid", productid);
            params.put("taxclassvalue", taxclassvalue);
            params.put("creationdate", creationdate);

            /**
             * Insert data into product custom history
             */
            accScriptDao.insertProductTaxClassInHistory(params);

        }
        return null;
    }

    /**
     * Function to insert Product Tax Class history for Asset
     *
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject insertProductTaxClassDataForAsset(JSONObject params) throws ServiceException, JSONException {
        /**
         * get Field Param id and column no for tax class field
         */
        params.put("fieldname", Constants.GSTProdCategory);
        params.put("moduleid", Constants.Acc_FixedAssets_AssetsGroups_ModuleId);
        List fieldparamsList = accScriptDao.getFieldParamsData(params);
        Iterator iterator = fieldparamsList.iterator();
        while (iterator.hasNext()) {
            Object[] objects = (Object[]) iterator.next();
            String fieldid = (String) objects[0];
            int colnum = (Short) objects[1];
            params.put("taxclassfieldid", fieldid);
            params.put("taxclasscolnum", colnum);
        }
        /**
         * Iterate all products one by one
         */
        List productList = accScriptDao.getProductsForTaxClassHistory(params);
        iterator = productList.iterator();
        while (iterator.hasNext()) {
            Object[] objects = (Object[]) iterator.next();
            String productid = (String) objects[0];
            String taxclassvalue = (String) objects[1];
            Date creationdate = objects[2] != null ? (Date) objects[2] : new Date();
            params.put("productid", productid);
            params.put("taxclassvalue", taxclassvalue);
            params.put("creationdate", params.opt("firstfyfrom"));

            /**
             * Insert data into product custom history
             */
            accScriptDao.insertProductTaxClassInHistory(params);

        }
        return null;
    }

    /**
     * Function to insert transaction GST history
     *
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject insertTransactionGSTFieldsData(JSONObject params) throws ServiceException, JSONException {
        /**
         * Sales side transaction
         */
        params.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
        params.put("maintable", "quotation");
        params.put("linetable", "quotationdetails");
        params.put("datecolumn", "quotationdate");
        params.put("assetcondition", "");
        accScriptDao.insertSalesTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
        params.put("maintable", "salesorder");
        params.put("linetable", "sodetails");
        params.put("datecolumn", "orderdate");
        params.put("assetcondition", "");
        accScriptDao.insertSalesTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Invoice_ModuleId);
        params.put("maintable", "invoice");
        params.put("linetable", "invoicedetails");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", " and mt.isfixedassetinvoice=0 ");
        accScriptDao.insertSalesTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
        params.put("maintable", "deliveryorder");
        params.put("linetable", "dodetails");
        params.put("datecolumn", "orderdate");
        params.put("assetcondition", "");
        accScriptDao.insertSalesTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
        params.put("maintable", "salesreturn");
        params.put("linetable", "srdetails");
        params.put("datecolumn", "orderdate");
        params.put("assetcondition", "");
        accScriptDao.insertSalesTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
        params.put("maintable", "receipt");
        params.put("linetable", "receiptadvancedetail");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", "");
        accScriptDao.insertSalesTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
        params.put("maintable", "creditnote");
        params.put("linetable", "cntaxentry");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", "");
        accScriptDao.insertSalesTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Credit_Note_ModuleId);
        params.put("maintable", "creditnote");
        params.put("linetable", "cntaxentry");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", "");
        accScriptDao.insertPurcaseTransactionHistoryData(params);
        /**
         * Purchase side transaction
         */
        params.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
        params.put("maintable", "vendorquotation");
        params.put("linetable", "vendorquotationdetails");
        params.put("datecolumn", "quotationdate");
        params.put("assetcondition", " and mt.isfixedassetvq=0 ");
        accScriptDao.insertPurcaseTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
        params.put("maintable", "purchaseorder");
        params.put("linetable", "podetails");
        params.put("datecolumn", "orderdate");
        params.put("assetcondition", " and mt.isfixedassetpo=0 ");
        accScriptDao.insertPurcaseTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
        params.put("maintable", "goodsreceipt");
        params.put("linetable", "grdetails");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", " and mt.isfixedassetinvoice=0");
        accScriptDao.insertPurcaseTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
        params.put("maintable", "grorder");
        params.put("linetable", "grodetails");
        params.put("datecolumn", "grorderdate");
        params.put("assetcondition", "");
        accScriptDao.insertPurcaseTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Purchase_Return_ModuleId);
        params.put("maintable", "purchasereturn");
        params.put("linetable", "prdetails");
        params.put("datecolumn", "orderdate");
        params.put("assetcondition", "");
        accScriptDao.insertPurcaseTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
        params.put("maintable", "payment");
        params.put("linetable", "advancedetail");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", "");
        accScriptDao.insertPurcaseTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
        params.put("maintable", "debitnote");
        params.put("linetable", "dntaxentry");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", "");
        accScriptDao.insertPurcaseTransactionHistoryData(params);
        params.put("moduleid", Constants.Acc_Debit_Note_ModuleId);
        params.put("maintable", "debitnote");
        params.put("linetable", "dntaxentry");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", "");
        accScriptDao.insertSalesTransactionHistoryData(params);

        return null;
    }

    /**
     * * Function to insert asset transaction GST history
     *
     * @param params
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject insertTransactionGSTFieldsDataForAsset(JSONObject params) throws ServiceException, JSONException {
        params.put("moduleid", Constants.Acc_FixedAssets_DisposalInvoice_ModuleId);
        params.put("maintable", "invoice");
        params.put("linetable", "invoicedetails");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", " and mt.isfixedassetinvoice=1 ");
        accScriptDao.insertLineDataForTransaction(params);
        params.put("moduleid", Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
        params.put("maintable", "vendorquotation");
        params.put("linetable", "vendorquotationdetails");
        params.put("datecolumn", "quotationdate");
        params.put("assetcondition", " and mt.isfixedassetvq=1 ");
        accScriptDao.insertLineDataForTransaction(params);
        params.put("moduleid", Constants.Acc_FixedAssets_Purchase_Order_ModuleId);
        params.put("maintable", "purchaseorder");
        params.put("linetable", "podetails");
        params.put("datecolumn", "orderdate");
        params.put("assetcondition", " and mt.isfixedassetpo=1 ");
        accScriptDao.insertLineDataForTransaction(params);
        params.put("moduleid", Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
        params.put("maintable", "goodsreceipt");
        params.put("linetable", "grdetails");
        params.put("datecolumn", "creationdate");
        params.put("assetcondition", " and mt.isfixedassetinvoice=1");
        accScriptDao.insertLineDataForTransaction(params);
        return null;
    }
    /**
     * Function to update transactions created with Empty state.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {SessionExpiredException.class, AccountingException.class, ServiceException.class, JSONException.class, ParseErrorException.class})
    public JSONObject updateGSTTransactions(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        /**
         * Get all Indian company.
         */
        List companyList = accScriptDao.getCompanyData(reqParams);
        Iterator iterator = companyList.iterator();
        StringBuilder msg = new StringBuilder();
        while (iterator.hasNext()) {
            Object[] objects = (Object[]) iterator.next();
            String companyid = (String) objects[0];
            String userid = (String) objects[1];
            Date firstfyfrom = (Date) objects[2];
            reqParams.put("companyid", companyid);
            reqParams.put("userid", userid);
            /**
             * Get Custom Table and its All details.
             */
            KwlReturnObject kwlReturnObject = accMasterItemsDAOobj.getCustomTableName(new JSONObject());
            List customTableList = kwlReturnObject.getEntityList();
            JSONArray bulkData = accMasterItemsDAOobj.createJsonForCustomTableList(customTableList);
            /**
             * Get list of documents.
             */
            JSONObject modulesdataObj = getDimensionEmptyRecords(reqParams, companyid, userid, bulkData);
            JSONArray moduledataarr = modulesdataObj.optJSONArray("recordsList");
            /**
             * Process request to update documents.
             */
            if (moduledataarr != null && moduledataarr.length() > 0) {
                String docnomsg = processModuleWiseRequest(reqParams, moduledataarr);
                msg.append("<b>Subdomain : " + (String) objects[3] + " = " + docnomsg);
            }
        }
        return new JSONObject().put("msg", msg);
    }

    /**
     * Function to Process request to update module wise documents.
     * @param reqParams
     * @param moduledataarr
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    public String processModuleWiseRequest(JSONObject reqParams, JSONArray moduledataarr) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        StringBuilder pidocno = new StringBuilder("Purchase Invoices : ");
        StringBuilder cidocno = new StringBuilder("Sales Invoices : ");
        JSONObject returnObj = new JSONObject();
        for (int i = 0; i < moduledataarr.length(); i++) {
            JSONObject dataobj = moduledataarr.optJSONObject(i);
            String modulename = dataobj.optString("usedModules");
            JSONObject params = new JSONObject(reqParams, JSONObject.getNames(reqParams));
            switch (modulename) {
                case "Customer Invoices":
                    params.put("documentNumber", dataobj.optString("documentNumber"));
                    params.put("documentid", dataobj.optString("documentid"));
                    params.put("isSales", true);
                    returnObj = updateInvoices(params);
                    cidocno.append(returnObj.optString("docno"));
                    break;
                case "Vendor Invoice":
                    params.put("documentNumber", dataobj.optString("documentNumber"));
                    params.put("documentid", dataobj.optString("documentid"));
                    params.put("isSales", false);
                    returnObj = updateVendorInvoices(params);
                    pidocno.append(returnObj.optString("docno"));
                    break;
            }
        }
        return pidocno.toString() + " \n " + cidocno.toString();
    }

    /**
     * Function to update invoice created with empty state.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    private JSONObject updateInvoices(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        JSONObject returnObj = new JSONObject();
        /**
         * Before edit check whether document link to other document or not If
         * it is link some where then should not be edit the same.
         */
        reqParams.put("linkingtable", "invoicelinking");
        List list = accScriptDao.getLinkedDocument(reqParams);
        if (list != null && !list.isEmpty() && list.size() > 0 && list.get(0) != null && !StringUtil.isNullOrEmpty(list.get(0).toString())) {
            return new JSONObject();
        }
        /**
         * Get state value for the document.
         */
        reqParams.put("documenttable", "invoice");
        reqParams.put("documentnumbercolumn", "invoicenumber");
        String state = getStateValueForDocument(reqParams);
        if (!StringUtil.isNullOrEmpty(state)) {
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(Invoice.class.getName(), reqParams.optString("documentid"));
            Invoice invoice = (Invoice) cap.getEntityList().get(0);
            if (invoice != null) {
                JSONObject tempParams = new JSONObject();
                /**
                 * Get unique case and customer type of the by checking customer
                 * history.
                 */
                tempParams.put("isSales", reqParams.optBoolean("isSales"));
                tempParams.put("customerid", invoice.getCustomer().getID());
                tempParams.put("transactiondate", authHandler.getDateOnlyFormat().format(invoice.getCreationDate()));
                JSONObject hisObject = getUniqueCaseForMaster(tempParams);
                if (isExportImport(hisObject.optString("CustVenTypeDefaultMstrID"))) {
                    return new JSONObject();
                }
                int uniqueCase = hisObject.optInt("uniqueCase", 0);

                /**
                 * Set state value to custom data.
                 */
                tempParams.put("statevalue", state);
                tempParams.put("jeid", invoice.getJournalEntry().getID());
                tempParams.put("companyid", reqParams.optString("companyid"));
                tempParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                setStateValueToDocument(tempParams);

                /**
                 * get dimension array for GST calculation.
                 */
                JSONArray dimArr = accRepeateInvoiceServiceObj.createDimensionArrayToCalculateGSTForInvoice(invoice, reqParams.optString("companyid"));

                /**
                 * Delete JE details of GST terms.
                 */
                accScriptDao.deleteGSTJEDetails(tempParams);
                /**
                 * update invoice details.
                 */
                tempParams.put("dimArr", dimArr);
                tempParams.put("uniqueCase", uniqueCase);
                returnObj = recalculateGSTForInvoice(tempParams, invoice);
                tempParams = null;
            }
        }
        return returnObj;
    }

    /**
     * Function to check if customer id of Import/export type
     * @param CustVenTypeDefaultMstrID
     * @return
     */
    public boolean isExportImport(String CustVenTypeDefaultMstrID) {
        if (CustVenTypeDefaultMstrID.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Export (WPAY)"))
                || CustVenTypeDefaultMstrID.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Export (WOPAY)"))
                || CustVenTypeDefaultMstrID.equalsIgnoreCase(Constants.CUSTVENTYPE.get("Import"))) {
            return true;
        }
        return false;
    }

    /**
     * Function to get state value for document.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    private String getStateValueForDocument(JSONObject reqParams) throws ServiceException, JSONException {
        String state = "";
        List returnList = null;
        if (reqParams.optBoolean("isSales")) {
            returnList = accScriptDao.getStateValueForSalesDocument(reqParams);
        } else {
            returnList = accScriptDao.getStateValueForPurchaseDocument(reqParams);
        }
        if (returnList != null && !returnList.isEmpty() && returnList.get(0) != null) {
            state = (String) returnList.get(0);
        }
        return state;
    }

    /**
     * Function to get unique case based on customer history.
     * @param tempParams
     * @param invoice
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    public JSONObject getUniqueCaseForMaster(JSONObject tempParams) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        tempParams.put("returnalldata", true);
        tempParams.put("isfortransaction", true);
        JSONObject jSONObject = new JSONObject();
        if (tempParams.optBoolean("isSales")) {
            jSONObject = accCustomerControllerCMNServiceObj.getCustomerGSTHistory(tempParams);
        } else {
            jSONObject = accVendorcontrollerCMNService.getVendorGSTHistory(tempParams);
        }
        jSONObject = jSONObject.optJSONArray("data").optJSONObject(0);
        return jSONObject;
    }

    /**
     * Function to set state dimension value to invoice module.
     * @param temp
     * @throws ServiceException
     * @throws JSONException
     */
    public void setStateValueToDocument(JSONObject temp) throws ServiceException, JSONException {
        /**
         * get Field Id and column number of state.
         */
        temp.put("fieldname", Constants.STATE);
        List fieldparamsList = accScriptDao.getFieldParamsData(temp);
        Iterator iterator = fieldparamsList.iterator();
        while (iterator.hasNext()) {
            Object[] objects = (Object[]) iterator.next();
            String fieldid = (String) objects[0];
            int colnum = (Short) objects[1];
            temp.put("statefiedid", fieldid);
            temp.put("statecolnum", colnum);
        }
        /**
         * Set state value to custom data.
         */
        accScriptDao.setStateValueToInvoice(temp);
    }

    /**
     * Function to recalculate GST for each product of invoice details.
     * @param reqParams
     * @param invoice
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    public JSONObject recalculateGSTForInvoice(JSONObject reqParams, Invoice invoice) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        int uniqueCase = reqParams.optInt("uniqueCase");
        JSONArray dimArr = reqParams.optJSONArray("dimArr");
        String companyid = reqParams.optString("companyid");
        Map<Inventory, List<HashMap>> FinalTerm = new HashMap<Inventory, List<HashMap>>();
        Set s = invoice.getJournalEntry().getDetails();
        HashSet<JournalEntryDetail> jeDetails = new HashSet<>(s);//HashSet<JournalEntryDetail>) invoice.getJournalEntry().getDetails();
        double finaltaxamt = 0d;
        s = invoice.getRows();
        HashSet<InvoiceDetail> invoicedetails = new HashSet<>(s);//(HashSet<InvoiceDetail>) invoice.getRows();
        for (InvoiceDetail invoiceDetail : invoicedetails) {

            /**
             * Delete existing term details if any
             *
             */
            accInvoiceDAOobj.deleteInvoiceDetailsTermMap(invoiceDetail.getID());
            /**
             * Get one by one product to update from invoice details.
             */
            double quantity = invoiceDetail.getInventory().getQuantity();
            double rate = invoiceDetail.getRate();
            double discount = invoiceDetail.getDiscount() != null ? invoiceDetail.getDiscount().getDiscount() : 0d;
            double subtotal = quantity * rate;
            if (invoiceDetail.getDiscount() != null && invoiceDetail.getDiscount().isInPercent()) {
                discount = (subtotal * invoiceDetail.getDiscount().getDiscount()) / 100;
            }
            subtotal = subtotal - discount;
            /**
             * Get GST for product.
             */
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put("df", authHandler.getOnlyDateFormat());
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("productids", invoiceDetail.getInventory().getProduct().getID());
            jSONObject.put("transactiondate", authHandler.getOnlyDateFormat().format(invoice.getCreationDate()));
            jSONObject.put("termSalesOrPurchaseCheck", true);
            jSONObject.put("uniqueCase", uniqueCase);
            jSONObject.put("dimArr", dimArr);
            jSONObject.put("companyid", companyid);
            jSONObject = accEntityGstService.getGSTForProduct(jSONObject, requestParams);
            /**
             * calculate term amount and apply on product row.
             */

            if (jSONObject.optBoolean("success")) {
                List termlist = new ArrayList();
                String prodTermArray = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("LineTermdetails");
                String taxclass = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("taxclass");
                JSONArray termarr = new JSONArray(prodTermArray);
                double rowttermamount = 0d;
                for (int i = 0; i < termarr.length(); i++) {
                    JSONObject termObj = termarr.optJSONObject(i);
                    JSONObject jedTermjson = new JSONObject();
                    double termamt = subtotal * termObj.optDouble("taxvalue") / 100;
                    termamt = authHandler.round(termamt, companyid);
                    if (!invoice.isRcmapplicable()) {
                        rowttermamount += termamt;
                        jedTermjson.put("srno", jeDetails.size() + 1);
                        jedTermjson.put("companyid", invoiceDetail.getCompany().getCompanyID());
                        jedTermjson.put("amount", subtotal * termObj.optDouble("taxvalue") / 100);
                        jedTermjson.put("accountid", termObj.optString("accountid")); //GST Account ID
                        jedTermjson.put("debit", false);
                        jedTermjson.put("jeid", invoice.getJournalEntry().getID());
                        KwlReturnObject jedresultobj = accJournalEntryobj.addJournalEntryDetails(jedTermjson);
                        JournalEntryDetail jedobj = (JournalEntryDetail) jedresultobj.getEntityList().get(0);
                        jeDetails.add(jedobj);
                    }
                    termObj.put("assessablevalue", subtotal);
                    termObj.put("termamount", termamt);
                    List list = accRepeateInvoiceServiceObj.mapInvoiceDetailTerms(termObj, invoiceDetail.getInvoice().getCreatedby().getUserID());
                    termlist.addAll(list);
                }
                FinalTerm.put(invoiceDetail.getInventory(), termlist);
                /**
                 * Update term amount in invoice details.
                 */
                jSONObject.put("rowttermamount", rowttermamount);
                jSONObject.put("invd", invoiceDetail.getID());
                jSONObject.put("tablename", "invoicedetails");
                accScriptDao.setInvoiceDetailsTermAmount(jSONObject);
                finaltaxamt += rowttermamount;
                /**
                 * Save GST tax class history.
                 */
                jSONObject = new JSONObject();
                jSONObject.put("taxclass", taxclass);
                jSONObject.put("moduleid", invoice.isFixedAssetInvoice() ? Constants.Acc_FixedAssets_DisposalInvoice_ModuleId : Constants.Acc_Invoice_ModuleId);
                jSONObject.put("detaildocid", invoiceDetail.getInventory().getID());
                fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jSONObject);
                jSONObject = null;
            }
        }
        /**
         * Update Amount in JEDETAILS of Customer.
         */
        HashMap<String, Object> requestParams = new HashMap();
        requestParams.put(Constants.companyid, invoice.getCompany().getCompanyID());
        requestParams.put("gcurrencyid", invoice.getCompany().getCurrency().getCurrencyID());
        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoice.getExcludingGstAmount() + finaltaxamt, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
        double invoiceamountdueinbase = (Double) bAmt.getEntityList().get(0);
        JSONObject tempObject = new JSONObject();
        tempObject.put("amount", invoice.getExcludingGstAmount() + finaltaxamt);
        tempObject.put("amountinbase", invoiceamountdueinbase);
        tempObject.put("jedid", invoice.getCustomerEntry().getID());
        accScriptDao.updateAmountInCustomerJEDetails(tempObject);
        tempObject = null;

        /**
         * Update Invoice amounts.
         */
        tempObject = new JSONObject();
//        requestParams.put(Constants.companyid, invoice.getCompany().getCompanyID());
//        requestParams.put("gcurrencyid", invoice.getCompany().getCurrency().getCurrencyID());
//        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoice.getExcludingGstAmount() + finaltaxamt, invoice.getCurrency().getCurrencyID(), invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
//        double invoiceamountdueinbase = (Double) bAmt.getEntityList().get(0);
        tempObject.put(Constants.invoiceamountdue, invoice.getExcludingGstAmount() + finaltaxamt);
        tempObject.put(Constants.invoiceamount, invoice.getExcludingGstAmount() + finaltaxamt);
        tempObject.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
        tempObject.put(Constants.invoiceamountinbase, invoiceamountdueinbase);
        tempObject.put("invoiceid", invoice.getID());
        accInvoiceDAOobj.updateInvoice(tempObject, invoicedetails);
        /**
         * Save Term details.
         */
        saveTermDetails(invoicedetails, FinalTerm);
        return new JSONObject().put("docno", invoice.getInvoiceNumber() + ",");
    }

    /**
     * Function to save Invoice details term Map.
     * @param invoicedetails
     * @param FinalTerm
     * @throws ServiceException
     */
    public void saveTermDetails(HashSet<InvoiceDetail> invoicedetails, Map<Inventory, List<HashMap>> FinalTerm) throws ServiceException {
        for (InvoiceDetail invoiceDetail : invoicedetails) {
            if (invoiceDetail.getInventory() != null && FinalTerm != null && ((List) FinalTerm.get(invoiceDetail.getInventory()) != null)) {
                List ll2 = (List) FinalTerm.get(invoiceDetail.getInventory());
                Iterator itr2 = ll2.iterator();
                while (itr2.hasNext()) {
                    HashMap<String, Object> termHashMap = (HashMap<String, Object>) itr2.next();
                    termHashMap.put("invoiceDetail", invoiceDetail);
                    accInvoiceDAOobj.saveInvoiceDetailTermMap(termHashMap);
                }
            }
        }
    }

    /**
     * Function to update invoice created with empty state.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    private JSONObject updateVendorInvoices(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        JSONObject returnObj = new JSONObject();
        /**
         * Before edit check whether document link to other document or not If
         * it is link some where then should not be edit the same.
         */
        reqParams.put("linkingtable", "goodsreceiptlinking");
        List list = accScriptDao.getLinkedDocument(reqParams);
        if (list != null && !list.isEmpty() && list.size() > 0 && list.get(0) != null && !StringUtil.isNullOrEmpty(list.get(0).toString())) {
            return new JSONObject();
        }
        /**
         * Get state value for the document.
         */
        reqParams.put("documenttable", "goodsreceipt");
        reqParams.put("documentnumbercolumn", "grnumber");
        String state = getStateValueForDocument(reqParams);
        if (!StringUtil.isNullOrEmpty(state)) {
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), reqParams.optString("documentid"));
            GoodsReceipt goodsReceipt = (GoodsReceipt) cap.getEntityList().get(0);
            if (goodsReceipt != null) {
                JSONObject tempParams = new JSONObject();
                /**
                 * Get unique case and customer type of the by checking customer
                 * history.
                 */
                tempParams.put("isSales", reqParams.optBoolean("isSales"));
                tempParams.put("vendorid", goodsReceipt.getVendor().getID());
                tempParams.put("transactiondate", authHandler.getDateOnlyFormat().format(goodsReceipt.getCreationDate()));
                JSONObject hisObject = getUniqueCaseForMaster(tempParams);
                if (isExportImport(hisObject.optString("CustVenTypeDefaultMstrID"))) {
                    return new JSONObject();
                }
                int uniqueCase = hisObject.optInt("uniqueCase", 0);

                /**
                 * IF vendor is Unregistered the apply no GST
                 */
                if (!goodsReceipt.isGtaapplicable() && hisObject.optString("GSTINRegTypeDefaultMstrID").equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered))) {
                    uniqueCase = Constants.NOGST;
                }
                /**
                 * Set state value to custom data.
                 */
                tempParams.put("statevalue", state);
                tempParams.put("jeid", goodsReceipt.getJournalEntry().getID());
                tempParams.put("companyid", reqParams.optString("companyid"));
                tempParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                setStateValueToDocument(tempParams);

                /**
                 * get dimension array for GST calculation.
                 */
                JSONArray dimArr = createDimensionArrayToCalculateGST(reqParams.optString("companyid"), goodsReceipt.getJournalEntry().getID(), Constants.Acc_Vendor_Invoice_ModuleId);

                /**
                 * Delete JE details of GST terms.
                 */
                accScriptDao.deleteGSTJEDetails(tempParams);
                /**
                 * update invoice details.
                 */
                tempParams.put("dimArr", dimArr);
                tempParams.put("uniqueCase", uniqueCase);
                returnObj = recalculateGSTForPurchaseInvoice(tempParams, goodsReceipt);
                tempParams = null;
            }
        }
        return returnObj;
    }

    /**
     * Function to create dimension array for GST calculation purpose
     * @param companyid
     * @param jeid
     * @param moduleid
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONArray createDimensionArrayToCalculateGST(String companyid, String jeid, int moduleid) throws JSONException, ServiceException {
        JSONArray dimArr = new JSONArray();
        JSONObject tempParams = new JSONObject();
        JSONObject jSONObject = new JSONObject();
        tempParams = new JSONObject();
        tempParams.put("companyid", companyid);
        tempParams.put("moduleid", moduleid);
        List returnList = accEntityGstDao.getGSTDimensionsDetailsForGSTCalculations(tempParams);
        Iterator iterator = returnList.iterator();
        String selectstate = "";
        String selectentity = "";
        while (iterator.hasNext()) {
            jSONObject = new JSONObject();
            Object[] object = (Object[]) iterator.next();
            int gstconfigtype = Integer.parseInt(object[2].toString());
            jSONObject.put("fieldname", (String) object[0]);
            jSONObject.put("gstmappingcolnum", Integer.parseInt(object[3].toString()));
            jSONObject.put("gstconfigtype", Integer.parseInt(object[2].toString()));
            dimArr.put(jSONObject);
            if (gstconfigtype == 1) {
                selectentity = "Col" + Integer.parseInt(object[1].toString());
            } else {
                selectstate = "Col" + Integer.parseInt(object[1].toString());
            }
        }
        /**
         * Get data from custom tables.
         */
        tempParams.put("selectstate", selectstate);
        tempParams.put("selectentity", selectentity);
        tempParams.put("customtable", "accjecustomdata");
        tempParams.put("primarykey", "journalentryId");
        tempParams.put("primaryid", jeid);
        returnList = accEntityGstDao.getGSTDimensionDataFromCustomTableForGSTCalculations(tempParams);
        iterator = returnList.iterator();
        while (iterator.hasNext()) {
            Object[] object = (Object[]) iterator.next();
            for (int i = 0; i < dimArr.length(); i++) {
                jSONObject = dimArr.optJSONObject(i);
                if (jSONObject.optInt("gstconfigtype") == 1) {
                    dimArr.getJSONObject(i).put("dimvalue", (String) object[0]);
                } else {
                    dimArr.getJSONObject(i).put("dimvalue", (String) object[1]);
                }
            }
        }
        return dimArr;
    }

    /**
     * Function to recalculate GST for each product of invoice details.
     * @param reqParams
     * @param invoice
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    public JSONObject recalculateGSTForPurchaseInvoice(JSONObject reqParams, GoodsReceipt goodsReceipt) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        int uniqueCase = reqParams.optInt("uniqueCase");
        JSONArray dimArr = reqParams.optJSONArray("dimArr");
        String companyid = reqParams.optString("companyid");
        Map<Inventory, List<HashMap>> FinalTerm = new HashMap<Inventory, List<HashMap>>();
        Set s = goodsReceipt.getJournalEntry().getDetails();
        HashSet<JournalEntryDetail> jeDetails = new HashSet<>(s);//HashSet<JournalEntryDetail>) invoice.getJournalEntry().getDetails();
        double finaltaxamt = 0d;
        s = goodsReceipt.getRows();
        HashSet<GoodsReceiptDetail> goodsReceiptDetails = new HashSet<>(s);//(HashSet<InvoiceDetail>) invoice.getRows();
        for (GoodsReceiptDetail receiptDetail : goodsReceiptDetails) {

            /**
             * Delete existing term details if any
             *
             */
            accGoodsReceiptobj.deleteGRDetailsTermMap(receiptDetail.getID());
            /**
             * Get one by one product to update from invoice details.
             */
            double quantity = receiptDetail.getInventory().getQuantity();
            double rate = receiptDetail.getRate();
            double discount = receiptDetail.getDiscount() != null ? receiptDetail.getDiscount().getDiscount() : 0d;
            double subtotal = quantity * rate;
            if (receiptDetail.getDiscount() != null && receiptDetail.getDiscount().isInPercent()) {
                discount = (subtotal * receiptDetail.getDiscount().getDiscount()) / 100;
            }
            subtotal = subtotal - discount;
            /**
             * Get GST for product.
             */
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put("df", authHandler.getOnlyDateFormat());
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("productids", receiptDetail.getInventory().getProduct().getID());
            jSONObject.put("transactiondate", authHandler.getOnlyDateFormat().format(goodsReceipt.getCreationDate()));
            jSONObject.put("termSalesOrPurchaseCheck", goodsReceipt.isGtaapplicable() ? true : false);
            jSONObject.put("uniqueCase", uniqueCase);
            jSONObject.put("dimArr", dimArr);
            jSONObject.put("companyid", companyid);
            jSONObject = accEntityGstService.getGSTForProduct(jSONObject, requestParams);
            /**
             * calculate term amount and apply on product row.
             */

            if (jSONObject.optBoolean("success")) {
                List termlist = new ArrayList();
                String prodTermArray = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("LineTermdetails");
                String taxclass = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("taxclass");
                JSONArray termarr = new JSONArray(prodTermArray);
                double rowttermamount = 0d;
                for (int i = 0; i < termarr.length(); i++) {
                    JSONObject termObj = termarr.optJSONObject(i);
                    JSONObject jedTermjson = new JSONObject();
                    double termamt = subtotal * termObj.optDouble("taxvalue") / 100;
                    termamt = authHandler.round(termamt, companyid);
                    rowttermamount += termamt;
                    jedTermjson.put("srno", jeDetails.size() + 1);
                    jedTermjson.put("companyid", receiptDetail.getCompany().getCompanyID());
                    jedTermjson.put("amount", subtotal * termObj.optDouble("taxvalue") / 100);
                    jedTermjson.put("accountid", termObj.optString("accountid")); //GST Account ID
                    jedTermjson.put("debit", goodsReceipt.isGtaapplicable() ? false : true);
                    jedTermjson.put("jeid", goodsReceipt.getJournalEntry().getID());
                    KwlReturnObject jedresultobj = accJournalEntryobj.addJournalEntryDetails(jedTermjson);
                    JournalEntryDetail jedobj = (JournalEntryDetail) jedresultobj.getEntityList().get(0);
                    jeDetails.add(jedobj);
                    /**
                     * If RCM type of Invoice then post JE detail against
                     * advance account
                     */
                    if (goodsReceipt.isGtaapplicable()) {
                        jedTermjson = new JSONObject();
                        jedTermjson.put("srno", jeDetails.size() + 1);
                        jedTermjson.put("companyid", receiptDetail.getCompany().getCompanyID());
                        jedTermjson.put("amount", subtotal * termObj.optDouble("taxvalue") / 100);
                        jedTermjson.put("accountid", termObj.optString("payableaccountid")); //GST Account ID
                        jedTermjson.put("debit", true);
                        jedTermjson.put("jeid", goodsReceipt.getJournalEntry().getID());
                        jedresultobj = accJournalEntryobj.addJournalEntryDetails(jedTermjson);
                        jedobj = (JournalEntryDetail) jedresultobj.getEntityList().get(0);
                        jeDetails.add(jedobj);
                    }
                    termObj.put("assessablevalue", subtotal);
                    termObj.put("termamount", termamt);
                    List list = mapInvoiceDetailTerms(termObj, receiptDetail.getGoodsReceipt().getCreatedby().getUserID());
                    termlist.addAll(list);
                }
                FinalTerm.put(receiptDetail.getInventory(), termlist);
                /**
                 * Update term amount in invoice details.
                 */
                if (!goodsReceipt.isGtaapplicable()) {
                    jSONObject.put("rowttermamount", rowttermamount);
                    jSONObject.put("invd", receiptDetail.getID());
                    jSONObject.put("tablename", "grdetails");
                    accScriptDao.setInvoiceDetailsTermAmount(jSONObject);
                    finaltaxamt += rowttermamount;
                }

                /**
                 * Save GST tax class history.
                 */
                jSONObject = new JSONObject();
                jSONObject.put("taxclass", taxclass);
                jSONObject.put("moduleid", goodsReceipt.isFixedAssetInvoice() ? Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId : Constants.Acc_Vendor_Invoice_ModuleId);
                jSONObject.put("detaildocid", receiptDetail.getInventory().getID());
                fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jSONObject);
                jSONObject = null;
            }
        }
        /**
         * Update Amount in JEDETAILS of Customer.
         */
        if (goodsReceipt.isGtaapplicable()) {
            finaltaxamt = 0d;
        }
        HashMap<String, Object> requestParams = new HashMap();
        requestParams.put(Constants.companyid, goodsReceipt.getCompany().getCompanyID());
        requestParams.put("gcurrencyid", goodsReceipt.getCompany().getCurrency().getCurrencyID());
        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, goodsReceipt.getExcludingGstAmount() + finaltaxamt, goodsReceipt.getCurrency().getCurrencyID(), goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
        double invoiceamountdueinbase = (Double) bAmt.getEntityList().get(0);
        JSONObject tempObject = new JSONObject();
        tempObject.put("amount", goodsReceipt.getExcludingGstAmount() + finaltaxamt);
        tempObject.put("amountinbase", invoiceamountdueinbase);
        tempObject.put("jedid", goodsReceipt.getVendorEntry().getID());
        accScriptDao.updateAmountInCustomerJEDetails(tempObject);
        tempObject = null;

        /**
         * Update Invoice amounts.
         */
        Map<String, Object> invjson = new HashMap<String, Object>();
        tempObject = new JSONObject();
//        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, goodsReceipt.getExcludingGstAmount() + finaltaxamt, goodsReceipt.getCurrency().getCurrencyID(), goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
//        double invoiceamountdueinbase = (Double) bAmt.getEntityList().get(0);
        invjson.put(Constants.invoiceamountdue, goodsReceipt.getExcludingGstAmount() + finaltaxamt);
        invjson.put(Constants.invoiceamount, goodsReceipt.getExcludingGstAmount() + finaltaxamt);
        invjson.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
        invjson.put(Constants.invoiceamountinbase, invoiceamountdueinbase);
        invjson.put("grid", goodsReceipt.getID());
        accGoodsReceiptobj.updateGoodsReceipt(invjson);
        /**
         * Save Term details.
         */
        savePurchaseInvoiceTermDetails(goodsReceiptDetails, FinalTerm);
        return new JSONObject().put("docno", goodsReceipt.getGoodsReceiptNumber() + ",");
    }

    public List mapInvoiceDetailTerms(JSONObject termObj, String userid) throws ServiceException {
        List ll = new ArrayList();
        try {
            HashMap<String, Object> termMap = new HashMap<String, Object>();
            termMap.put("term", termObj.optString("termid"));
            termMap.put("termamount", termObj.optDouble("termamount"));
            termMap.put("termpercentage", termObj.optDouble("taxvalue"));
            termMap.put("assessablevalue", termObj.optDouble("assessablevalue"));
            termMap.put("creationdate", new Date());
            termMap.put("accountid", termObj.optString("accountid"));
            termMap.put("userid", userid);
            termMap.put("productentitytermid", termObj.optString("productentitytermid"));
            termMap.put("taxtype", termObj.optInt("taxtype"));
            ll.add(termMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }

    /**
     * Function to save Invoice details term map
     * @param goodsReceiptDetails
     * @param FinalTerm
     * @throws ServiceException
     */
    public void savePurchaseInvoiceTermDetails(HashSet<GoodsReceiptDetail> goodsReceiptDetails, Map<Inventory, List<HashMap>> FinalTerm) throws ServiceException {
        for (GoodsReceiptDetail receiptDetail : goodsReceiptDetails) {
            if (receiptDetail.getInventory() != null && FinalTerm != null && ((List) FinalTerm.get(receiptDetail.getInventory()) != null)) {
                List ll2 = (List) FinalTerm.get(receiptDetail.getInventory());
                Iterator itr2 = ll2.iterator();
                while (itr2.hasNext()) {
                    HashMap<String, Object> termHashMap = (HashMap<String, Object>) itr2.next();
                    termHashMap.put("goodsReceiptDetail", receiptDetail);
                    accGoodsReceiptobj.saveInvoiceDetailTermMap(termHashMap);
                }
            }
        }
    }
}
