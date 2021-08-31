
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.salesreturn;

import com.krawler.accounting.integration.common.IntegrationCommonService;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.*;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.Acc_ConsignmentPurchaseReturn_ModuleId;
import static com.krawler.common.util.Constants.Acc_ConsignmentSalesReturn_ModuleId;
import static com.krawler.common.util.Constants.Acc_Purchase_Return_ModuleId;
import static com.krawler.common.util.Constants.Acc_Sales_Return_ModuleId;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.companypreferenceservice.AccCompanyPreferencesService;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.approval.consignment.Consignment;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stockmovement.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteService;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteService;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceHandler;
import com.krawler.spring.accounting.goodsreceipt.GoodsReceiptConstants;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptImpl;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.TransactionBatch;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccSalesReturnServiceImpl extends BaseDAO implements AccSalesReturnService, MessageSourceAware {
    private HibernateTransactionManager txnManager;

    private accInvoiceDAO accInvoiceDAOobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accSalesOrderDAO accSalesOrderDAOObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private fieldDataManager fieldDataManagercntrl;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private MessageSource messageSource;
    private accVendorPaymentDAO accVendorPaymentobj;
    private auditTrailDAO auditTrailObj;
    public ImportHandler importHandler;
    public accCustomerDAO accCustomerDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccInvoiceModuleService accInvoiceModuleService;
    private AccJournalEntryModuleService accJournalEntryModuleService;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accCreditNoteService accCreditNoteService;
    private accDebitNoteService accDebitNoteService;
    private accDebitNoteDAO accDebitNoteobj;
    private StockMovementService stockMovementService;
    private ConsignmentService consignmentService;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private accAccountDAO accAccountDAOobj;
    private AccProductModuleService accProductModuleService;
    private accTaxDAO accTaxObj;
    private IntegrationCommonService integrationCommonService;
    private AccCompanyPreferencesService accCompanyPreferencesService;

    public void setAccCompanyPreferencesService(AccCompanyPreferencesService accCompanyPreferencesService) {
        this.accCompanyPreferencesService = accCompanyPreferencesService;
    }
    
    public void setIntegrationCommonService(IntegrationCommonService integrationCommonService) {
        this.integrationCommonService = integrationCommonService;
    }

    @Override
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
     public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public ConsignmentService getConsignmentService() {
        return consignmentService;
    }

    public void setAccJournalEntryModuleService(AccJournalEntryModuleService accJournalEntryModuleService) {
        this.accJournalEntryModuleService = accJournalEntryModuleService;
    }

    public void setConsignmentService(ConsignmentService consignmentService) {
        this.consignmentService = consignmentService;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setAccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOObj) {
        this.accSalesOrderDAOObj = accSalesOrderDAOObj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setAccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }
    
    public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }
    
    public void setaccDebitNoteService(accDebitNoteService accDebitNoteService) {
        this.accDebitNoteService = accDebitNoteService;
    }

    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    
    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
     
    private void sendMailOnConsignmentSalesReturnCreationUpdation(String companyId, SalesReturn salesReturn, boolean isEdit, DocumentEmailSettings documentEmailSettings,String entryNumber) throws ServiceException {
        String  htmlTextC = "", subject = "";
        String [] toEmailIds = {};
        String srCreatorName = salesReturn.getCreatedby().getFullName();
        String srNumber = salesReturn.getSalesReturnNumber();
        if(StringUtil.isNullOrEmpty(srNumber)){
            srNumber=entryNumber;
        }
        htmlTextC += "<br/>Hi,<br/>";
        if (!isEdit) {
            htmlTextC += "<br/>User <b>" + srCreatorName + "</b> has created Sales Return <b>" + srNumber + "</b>.<br/>";
        } else {
            htmlTextC += "<br/>User <b>" + srCreatorName + "</b> has edited Sales Return <b>" + srNumber + "</b>.<br/>";
        }
        KwlReturnObject result = accountingHandlerDAOobj.getNotifications(companyId);
        List<NotificationRules> list = result.getEntityList();
        Iterator<NotificationRules> itr = list.iterator();
        while (itr.hasNext()) {
            NotificationRules nr = itr.next();
            if (nr != null && nr.getModuleId() == 201) {
                String otherEmails = nr.getEmailids();
                if ((Integer.parseInt(nr.getFieldid()) == 29)) {
                    subject = nr.getMailsubject();
                    htmlTextC = nr.getMailcontent();

                    subject = subject.replaceAll("#Customer_Alias#", (salesReturn.getCustomer() != null && salesReturn.getCustomer().getAliasname() != null) ? salesReturn.getCustomer().getAliasname() : "");
//                    subject = subject.replaceAll("#Sales_Person#", deliveryOrder.getSalesperson().getValue());
                    subject = subject.replaceAll("#Document_Number#", srNumber);
                    htmlTextC = htmlTextC.replaceAll("#Document_Number#", srNumber);
                    htmlTextC = htmlTextC.replaceAll("#User_Name#", srCreatorName);
                    
                    if (isEdit) {
                        subject = subject.replaceAll("Creation", "updation");
                        subject = subject.replaceAll("generation", "updation");
                        htmlTextC = htmlTextC.replaceAll("added", "updated");
                        htmlTextC = htmlTextC.replaceAll("created", "updated");
                    }
                    
                    
                    Set<SalesReturnDetail> dodSet = salesReturn.getRows();
                    Set<String> toEmailIdSet = new HashSet<String>();
                    Iterator<SalesReturnDetail> itr2 = dodSet.iterator();
                    if(nr.isMailToSalesPerson()){
                        while (itr2.hasNext()) {
                            SalesReturnDetail dod = itr2.next();
                            if (dod.getDodetails()!= null) {
                                MasterItem mi = dod.getDodetails().getDeliveryOrder().getSalesperson();
                                if (mi != null) {
                                    toEmailIdSet.add(mi.getEmailID());
                                }
                                if(dod.getDodetails().getSodetails() != null && dod.getDodetails().getSodetails().getSalesOrder() != null){
                                    MasterItem mi1 = dod.getDodetails().getSodetails().getSalesOrder().getSalesperson();
                                    if (mi1 != null) {
                                        toEmailIdSet.add(mi1.getEmailID());
                                    }
                                }
                            }
                        }
                    }
                    KwlReturnObject userRes = null;
                    if (!StringUtil.isNullOrEmpty(nr.getUsers())) {
                        String userIds[] = nr.getUsers().split(",");
                        for (String userId : userIds) {
                            User user = null;
                            userRes = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
                            user = (User) userRes.getEntityList().get(0);
                            if(user != null && !StringUtil.isNullOrEmpty(user.getEmailID())){
                                toEmailIdSet.add(user.getEmailID());
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(nr.getEmailids())) { //copy to emails address
                        String copyToEmailIdArr[] = nr.getEmailids().split(",");
                        for(String usermail : copyToEmailIdArr){
                            toEmailIdSet.add(usermail);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(otherEmails)) {
                        String[] otherMails = otherEmails.split(",");
                        for (String Mailid : otherMails) {
                            toEmailIdSet.add(Mailid);
                        }
                    }
                    
                    toEmailIds=StringUtils.split(StringUtils.join(toEmailIdSet, ","), ",");
                    break;
                }
            }
        }
        List<String> headerDOItemsList=new ArrayList<String>();
        List<String> headerItemsList=new ArrayList<String>();
        List rowDetailMapList=new ArrayList();
        List rowDODetailMapList=new ArrayList();
        
        boolean isFirst=true;
        boolean isDOFirst=true;
        String salePersonListStr="";
        
        Set<String> ReqSet=new HashSet<String>();
                String reqNoHeader = "DO No.";
       String warehouseHeader = "Warehouse";
       String locationHeader = "Location";
       String fromDateHeader = "From Date";
       String toDateHeader = "To Date";
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
       for (SalesReturnDetail srDetails : salesReturn.getRows()) {
           String doNo = "";
           String warehouse = "";
           String location = "";
           Date fromDate = null;
           Date toDate = null;

           SalesOrder so = null;
           if (srDetails.getDodetails() != null) {
               doNo = srDetails.getDodetails().getDeliveryOrder().getDeliveryOrderNumber();
               so = srDetails.getDodetails().getSodetails() != null ? srDetails.getDodetails().getSodetails().getSalesOrder() : null;

               if (isDOFirst) {
                   headerDOItemsList.add(reqNoHeader);
                   headerDOItemsList.add(warehouseHeader);
                   headerDOItemsList.add(locationHeader);
                   headerDOItemsList.add(fromDateHeader);
                   headerDOItemsList.add(toDateHeader);
                   isDOFirst = false;
               }
               if (!ReqSet.contains(doNo) && so != null) {
                   warehouse = (so.getRequestWarehouse() != null ? so.getRequestWarehouse().getName() : "");
                   location = (so.getRequestLocation() != null ? so.getRequestLocation().getName() : "");
                   fromDate = so.getFromdate();
                   toDate = so.getTodate();
                   String salesPersonName = "";
                   MasterItem mi1 = so.getSalesperson();
                   if (mi1 != null) {
                       salesPersonName = mi1.getValue();
                       if (!StringUtil.isNullOrEmpty(salesPersonName)) {
                           if (StringUtil.isNullOrEmpty(salePersonListStr)) {
                               salePersonListStr += salesPersonName;
                           } else {
                                salePersonListStr += ("," + salesPersonName);
                           }
                       }
                   }

                   Map soMap = new HashMap();
                   soMap.put(reqNoHeader, doNo);
                   soMap.put(warehouseHeader, warehouse);
                   soMap.put(locationHeader, location);
                   soMap.put(fromDateHeader, (fromDate != null ? sdf.format(fromDate) : ""));
                   soMap.put(toDateHeader,(toDate != null ? sdf.format(toDate) : ""));
                   rowDODetailMapList.add(soMap);
               }
                ReqSet.add(doNo);
            }
            
            String productIdHeader="Product ID";
            String productDescHeader="Product Description";
            String delQtyHeader="Delivered Quantity";
            String retQtyHeader="Return Quantity";
            String productUoMHeader="UoM";
            if(isFirst){
                headerItemsList.add(productIdHeader);
                headerItemsList.add(productDescHeader);
                headerItemsList.add(delQtyHeader);
                headerItemsList.add(retQtyHeader);
                headerItemsList.add(productUoMHeader);
                isFirst=false;
            }
            Map map=new HashMap();
            map.put(productIdHeader, srDetails.getProduct().getProductid());
            map.put(productDescHeader, srDetails.getProduct().getDescription());
            map.put(delQtyHeader, srDetails.getActualQuantity());
            map.put(retQtyHeader, srDetails.getReturnQuantity());
            map.put(productUoMHeader, srDetails.getProduct().getUnitOfMeasure() != null ? srDetails.getProduct().getUnitOfMeasure().getNameEmptyforNA() : "");
            rowDetailMapList.add(map);
        }
        subject = subject.replaceAll("#Sales_Person#",salePersonListStr );
        
        htmlTextC = htmlTextC.concat("<br/>");
        htmlTextC = htmlTextC.concat("<b>Customer Name : </b>"+ (salesReturn.getCustomer() != null ? salesReturn.getCustomer().getName() : "")+"<br/>");
        htmlTextC = htmlTextC.concat("<b>Customer Warehouse : </b>"+(salesReturn.getCustWarehouse() != null ? salesReturn.getCustWarehouse().getName() : "")+"<br/>");
        htmlTextC = htmlTextC.concat("<br/>");
        
        //for DO detail table 
       String soDetailTabularHTML="<table>";
       
       soDetailTabularHTML += "<tr>";
       for(String soHeader: headerDOItemsList){
           soDetailTabularHTML += "<th align='left'>"+soHeader+"</th>";
       }
       soDetailTabularHTML += "</tr>";
       
       for(Object obj: rowDODetailMapList){
           Map soDtlMap = (Map)obj;
           soDetailTabularHTML += "<tr>";
           for(String soHeader: headerDOItemsList){
               soDetailTabularHTML += "<td width='100px'>"+ soDtlMap.get(soHeader) +"</td>";
           }
           soDetailTabularHTML += "</tr>";
       }
       
       soDetailTabularHTML += "</table>";
       htmlTextC = htmlTextC.concat(soDetailTabularHTML);
       
       htmlTextC = htmlTextC.concat("<br/>");
       //for SR detail table
       
        String doDetailTabularHTML=accountingHandlerDAOobj.getTabularFormatHTMLForNotificationMail(headerItemsList, rowDetailMapList);
        htmlTextC = htmlTextC.concat(doDetailTabularHTML);
        accountingHandlerDAOobj.sendTransactionEmails(toEmailIds, "", subject, htmlTextC, htmlTextC, companyId);
    }
    
    public String getInvoiceStatusForSalesReturn(Invoice iv) throws ServiceException {
        Set<InvoiceDetail> ivDetail = iv.getRows();
        Iterator ite = ivDetail.iterator();
        String result = "Completely Returned";
        while (ite.hasNext()) {
            InvoiceDetail iDetail = (InvoiceDetail) ite.next();
            KwlReturnObject idresult = accInvoiceDAOobj.getSalesReturnIDFromInvoiceDetails(iDetail.getID());
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                SalesReturnDetail se = (SalesReturnDetail) ite1.next();
                qua += se.getInventory().getQuantity();
            }
            if (qua < iDetail.getInventory().getQuantity()) {
                result = "Completely Not Returned";
                break;
            }
        }
        return result;
    }
    
 @Override    
    public void updateOpenStatusFlagInDOForSR(String linkNumbers,String salesreturnId,boolean  isconsign,boolean  soReopen) throws ServiceException {
        HashMap hMap = new HashMap();
        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), linkNumbers);
        DeliveryOrder deliveryOrder = (DeliveryOrder) rdresult.getEntityList().get(0);
        Set<DeliveryOrderDetail> rows = deliveryOrder.getRows();
        boolean isOpen = false;
        try{
       
            String status = accInvoiceServiceDAO.getDeliveryReturnStatus(rows,salesreturnId,isconsign,soReopen);
            if (status.equalsIgnoreCase("Open")) {
                isOpen=true;
            }
             hMap.put("isFromSalesReturn", true);
            hMap.put("isOpen", isOpen);
            hMap.put("deliveryOrder", deliveryOrder);
            accInvoiceDAOobj.updateDeliveryOrderStatus(hMap);

       
    }catch (Exception ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
 
@Override 
    public void updateOpenStatusFlagInDOForSI(String linkNumbers) throws ServiceException {
        HashMap hMap = new HashMap();
        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkNumbers);
        Invoice invoice = (Invoice) rdresult.getEntityList().get(0);
        boolean isOpen = false;
        try {
            String status = getInvoiceStatusForSalesReturn(invoice);
            if (status.equalsIgnoreCase("Completely Not Returned")) {
                isOpen = true;
            }
            hMap.put("isOpenSR", isOpen);
            hMap.put("invoice", invoice);
            accInvoiceDAOobj.updateInvoiceLinkflag(hMap);
        } catch (Exception ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

@Override
    public void updateOpenStatusFlagInGRForPR(String linkNumbers, String companyid,String purchasereturnId) throws ServiceException {
        HashMap hMap = new HashMap();

        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), linkNumbers);
        GoodsReceiptOrder goodsReceiptOrder = (GoodsReceiptOrder) rdresult.getEntityList().get(0);
        Set<GoodsReceiptOrderDetails> rows = goodsReceiptOrder.getRows();
        boolean isOpen = false;
        try {

            String status = getGoodsReceiptOrderPRStatus(rows,purchasereturnId);
            if (status.equalsIgnoreCase("Open")) {
                isOpen = true;
            }
            hMap.put("isFromPurchaseReturn", true);
            hMap.put("isOpen", isOpen);
            hMap.put("goodsReceiptOrderID", goodsReceiptOrder.getID());
            hMap.put(Constants.companyKey, companyid);
            accInvoiceDAOobj.updateGoodsReceiptOrderStatus(hMap);


        } catch (Exception ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void updateOpenStatusFlagInVIForPR(String linkNumbers, String companyid) throws ServiceException {
        HashMap hMap = new HashMap();

        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), linkNumbers);
        GoodsReceipt goodsReceipt = (GoodsReceipt) rdresult.getEntityList().get(0);
        Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();
        boolean isOpen = false;
        try {

            String status = getInvoiceStatusForPurchaseReturn(rows);
            if (status.equalsIgnoreCase("Open")) {
                isOpen = true;
            }
            hMap.put("isFromPurchaseReturn", true);
            hMap.put("isOpen", isOpen);
            hMap.put("goodsReceiptID", goodsReceipt.getID());
            hMap.put(Constants.companyKey, companyid);
            accInvoiceDAOobj.updateGoodsReceiptStatus(hMap);


        } catch (Exception ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
   
    public String getGoodsReceiptOrderPRStatus(Set<GoodsReceiptOrderDetails> orderDetail,String purchasereturnId) throws ServiceException {
        String result = "Closed";
        for (GoodsReceiptOrderDetails soDetail:orderDetail) {
            KwlReturnObject idresult = accGoodsReceiptobj.getGRDIDFromPRD(soDetail.getID(), purchasereturnId);
            List<PurchaseReturnDetail> list = idresult.getEntityList();
            double qua = 0;
            int poupdatedcount = 0;
            for (PurchaseReturnDetail ge : list) {
                qua += ge.getInventory().getQuantity();
            }
            if (soDetail != null) {
                if (soDetail.getPodetails() != null && !StringUtil.isNullOrEmpty(soDetail.getPodetails().getID())) {
                    HashMap hMap = new HashMap();
                    hMap.put("podetails", soDetail.getPodetails().getID());
                    hMap.put(Constants.companyKey, soDetail.getCompany().getCompanyID());
                    hMap.put("balanceqty",qua);
                    hMap.put("add", true);
                    accCommonTablesDAO.updatePurchaseOrderStatus(hMap);
                     
                    /* Updating islineitemclosed & ispoclosed flag to false if GR is returned*/
                    soDetail.getPodetails().getPurchaseOrder().setIsPOClosed(false);
                    soDetail.getPodetails().setIsLineItemClosed(false);
                    poupdatedcount++;
                    if (poupdatedcount > 0) {
                        HashMap poMap = new HashMap();
                        if (soDetail.getPodetails().getPurchaseOrder() != null && !StringUtil.isNullOrEmpty(soDetail.getPodetails().getPurchaseOrder().getID())) {
                            poMap.put("purchaseOrder", soDetail.getPodetails().getPurchaseOrder());
                            poMap.put("isOpen", true);
                            poMap.put(Constants.companyKey, soDetail.getCompany().getCompanyID());
                            poMap.put("value", "2");
                            accGoodsReceiptobj.updatePOLinkflag(poMap);
                        }
                    }
                }
            }
            if (qua < soDetail.getActualQuantity()) {
                result = "Open";
            }
        }
        return result;
    }

 
    public String getInvoiceStatusForPurchaseReturn(Set<GoodsReceiptDetail> ivDetail) throws ServiceException {
        String result = "Closed";
        for (GoodsReceiptDetail iDetail:ivDetail) {
            KwlReturnObject idresult = accGoodsReceiptobj.getPurchaseReturnIDFromVendorInvoiceDetails(iDetail.getID());
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                PurchaseReturnDetail prd = (PurchaseReturnDetail) ite1.next();
                qua += prd.getInventory().getQuantity();
            }
            if (qua < iDetail.getInventory().getQuantity()) {
                result = "Open";
                break;
            }
        }
        return result;
    }
      
        
    public void deleteJEArray(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {      //delete old invoice
            JournalEntryDetail jed = null;
            if (!StringUtil.isNullOrEmpty(oldjeid)) {
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    jed = (JournalEntryDetail) itr.next();
                    //Sagar - No need to revert entry from optimized table as entries are already reverted from calling main function in edit case.
                    result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }
                result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    private void deleteJEDetailsCustomData(String jeid) throws ServiceException {
        KwlReturnObject cap = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeid);
        JournalEntry salesOrderDetails = (JournalEntry) cap.getEntityList().get(0);
        Set<JournalEntryDetail> journalEntryDetails = salesOrderDetails.getDetails();
        for (JournalEntryDetail journalEntryDetail : journalEntryDetails) {
            String jeDetailsId = journalEntryDetail.getID();
            KwlReturnObject jedresult1 = accJournalEntryobj.deleteJEDetailsCustomData(jeDetailsId);
        }
    }

    private void addStockMovementForConsignmentQA(ExtraCompanyPreferences extraCompanyPreferences, List<Consignment> ConsignmentList) throws ServiceException {
        try {
            Store QAStore = null;
            Location QALocation = null;
            if (extraCompanyPreferences.isActivateQAApprovalFlow()) {
                String qaInspectionStore = extraCompanyPreferences.getInspectionStore();
                if (!StringUtil.isNullOrEmpty(qaInspectionStore)) {
                    KwlReturnObject storeObj = accountingHandlerDAOobj.getObject(Store.class.getName(), qaInspectionStore);
                    QAStore = (Store) storeObj.getEntityList().get(0);
                    if (QAStore != null) {
                        QALocation = QAStore.getDefaultLocation();
                    }
                }

            }
            if (ConsignmentList.size() > 0) {

                for (Consignment stk : ConsignmentList) {

                StockMovement stockmovment = new StockMovement();
                Set<StockMovementDetail> smdetail=new HashSet<StockMovementDetail>();
                
                stockmovment.setCompany(stk.getCompany());
                stockmovment.setCustomer(stk.getCustomer());
                stockmovment.setModuleRefId(stk.getModuleRefId());
                stockmovment.setModuleRefDetailId(stk.getModuleRefId());
                stockmovment.setProduct(stk.getProduct());
                stockmovment.setQuantity(stk.getReturnQuantity());
                stockmovment.setRemark("Return stock added for QA Inspection.");
                KwlReturnObject srObj = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), stk.getModuleRefId());
                SalesReturn salesReturn = (SalesReturn) srObj.getEntityList().get(0);
                if(salesReturn != null){
                    stockmovment.setMemo(salesReturn.getMemo());
                }
                stockmovment.setStockUoM(stk.getUom());
                stockmovment.setStore(QAStore);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date newdate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
                String userdiff = stk.getCompany().getCreator().getTimeZone()!=null?stk.getCompany().getCreator().getTimeZone().getDifference() : stk.getCompany().getTimeZone().getDifference();
                sdf.setTimeZone(TimeZone.getTimeZone(userdiff));
                String dtStr=sdf.format(newdate);
                newdate = sdf.parse(dtStr);
                stockmovment.setTransactionDate(newdate);
                stockmovment.setTransactionModule(TransactionModule.ERP_SALES_RETURN);
                stockmovment.setTransactionNo(stk.getTransactionNo());
                stockmovment.setTransactionType(TransactionType.IN);
                stockmovment.setPricePerUnit(stk.getUnitPrice());
                stockmovment.setCostCenter(stk.getCostcenter());
                
                for(ConsignmentApprovalDetails approvaldtl:stk.getConsignmentApprovalDetails()){
                    StockMovementDetail smd=new StockMovementDetail();
                    smd.setBatchName(approvaldtl.getBatchName());
                    smd.setLocation(QALocation);
                    NewProductBatch productBatch = approvaldtl.getBatch();
                    StoreMaster row = null;
                    StoreMaster rack = null;
                    StoreMaster bin = null;
                    if(productBatch != null){
                        row = productBatch.getRow();
                        rack = productBatch.getRack();
                        bin = productBatch.getBin();
                    }
                    smd.setRow(row);
                    smd.setRack(rack);
                    smd.setBin(bin);
                    smd.setQuantity(approvaldtl.getQuantity());
                    smd.setSerialNames(approvaldtl.getSerialName());
                    smd.setStockMovement(stockmovment);
                    smdetail.add(smd);
                    stockMovementService.stockMovementInERP(true, stockmovment.getProduct(), QAStore, QALocation , row, rack, bin, smd.getBatchName(), smd.getSerialNames(), stockmovment.getQuantity(), false);
//                     stockService.updateERPInventory(false, stockmovment.getTransactionDate(), stockmovment.getProduct(), null, stockmovment.getStockUoM(), smd.getQuantity(), "");
                }
                stockmovment.setStockMovementDetails(smdetail);
                stockMovementService.addStockMovement(stockmovment);

                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Override
    public List savePurchaseReturn(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        String debitNoteNumber="";
        String debitNoteId="";
        PurchaseReturn salesReturn = null;
        String oldjeid = "";
        String jeid = "";
        String linkedDocuments="";
        String unlinkMessage="";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = (request.getParameter(Constants.currencyKey) == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter(Constants.currencyKey));
            double externalCurrencyRate = StringUtil.getDouble(!StringUtil.isNullOrEmpty(request.getParameter(Constants.externalcurrencyrate))?request.getParameter(Constants.externalcurrencyrate):"0.0");
            String entryNumber = request.getParameter("number");
            String costCenterId = request.getParameter(Constants.costcenter);

            debitNoteNumber=StringUtil.isNullOrEmpty(request.getParameter("cndnnumber"))?"":request.getParameter("cndnnumber");
            String debitNoteSequenceFormat=StringUtil.isNullOrEmpty(request.getParameter("cndnsequenceformat"))?"NA":request.getParameter("cndnsequenceformat");           
            String supplierinvoiceno = !StringUtil.isNullOrEmpty(request.getParameter(Constants.SUPPLIERINVOICENO)) ? request.getParameter(Constants.SUPPLIERINVOICENO) : "";
            String mvattransactionno = !StringUtil.isNullOrEmpty(request.getParameter(Constants.MVATTRANSACTIONNO)) ? request.getParameter(Constants.MVATTRANSACTIONNO) : "";
           
            boolean isNoteAlso = false;
            boolean isInsertAudTrail = false; // This flag is added for showing Debit No in Audit Trial ERP-18558
            boolean isnegativestockforlocwar = false;
            boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isConsignment))) ? Boolean.parseBoolean(request.getParameter(Constants.isConsignment)) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isFixedAsset))) ? Boolean.parseBoolean(request.getParameter(Constants.isFixedAsset)) : false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteAlso"))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
                isNoteAlso = Boolean.parseBoolean(request.getParameter("isNoteAlso"));
            }
            boolean gstIncluded = (!StringUtil.isNullOrEmpty(request.getParameter("includingGST"))) ? Boolean.parseBoolean(request.getParameter("includingGST")) : false;
            boolean EWAYApplicable = (!StringUtil.isNullOrEmpty(request.getParameter("EWAYApplicable"))) ? Boolean.parseBoolean(request.getParameter("EWAYApplicable")) : false;
            boolean isApplyTaxToTerm = (!StringUtil.isNullOrEmpty(request.getParameter("isApplyTaxToTerms"))) ? Boolean.parseBoolean(request.getParameter("isApplyTaxToTerms")) : false;
            double tdsAmount = 0.0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("tdsamount"))) {
                tdsAmount = Double.parseDouble(request.getParameter("tdsamount"));
            }

            String srid = request.getParameter("srid");
            String isfavourite = request.getParameter("isfavourite");
            String sequenceformat = request.getParameter(Constants.sequenceformat);
            String deletedLinkedDocumentID = request.getParameter("deletedLinkedDocumentId");

            KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmp.getEntityList().get(0);
            
            String nextAutoNumber = "";
            HashMap<String, Object> doDataMap = new HashMap<String, Object>();
            long createdon = System.currentTimeMillis();
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long updatedon = createdon;
            

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            isnegativestockforlocwar = extraCompanyPreferences.isIsnegativestockforlocwar();
            
            int countryid = 0;
            if(extraCompanyPreferences != null && extraCompanyPreferences.getCompany().getCountry() != null){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            synchronized (this) {
                if (!StringUtil.isNullOrEmpty(srid)) {   //Edit case
                    KwlReturnObject socnt = accGoodsReceiptobj.getPurchaseReturnCountEdit(entryNumber, companyid, srid);
                    // in case of edition of Fixed Asset Purchase Return you need handle assetdetails table data, and need to 
                    // delete entry from assetdetailsinvdetailmapping table
                    if (isFixedAsset) {
                        HashMap<String, Object> deleteParams = new HashMap<String, Object>();
                        deleteParams.put("prid", srid);
                        deleteParams.put(Constants.companyKey, companyid);
                        accGoodsReceiptobj.deleteAssetDetailsLinkedWithPurchaseReturn(deleteParams);
                    }
                    if (!sequenceformat.equals("NA")) {
                        nextAutoNumber = entryNumber;
                    }
                    doDataMap.put(Constants.Acc_id, srid);
                    KwlReturnObject result = accGoodsReceiptobj.getPurchaseReturnInventory(srid);
                    KwlReturnObject resultBatch = accGoodsReceiptobj.getPurchaseReturnBatches(srid, companyid);

                    requestParams.put("prid", srid);
                    requestParams.put(Constants.companyKey, companyid);
                    requestParams.put("isnegativestockforlocwar", isnegativestockforlocwar);
                    accGoodsReceiptobj.updatePOBalanceQtyAfterPR(srid, companyid);
                    accGoodsReceiptobj.deletePurchasesBatchSerialDetails(requestParams); //dlete serial no and mapping

                     accGoodsReceiptobj.deletePurchaseReturnDetails(srid, companyid);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        String inventoryid = (String) itr.next();
                        accProductObj.deleteInventory(inventoryid, companyid);
                    }


                    List listBatch = resultBatch.getEntityList();
                    Iterator itrBatch = listBatch.iterator();
                    while (itrBatch.hasNext()) {
                        String batchid = (String) itrBatch.next();
                        accCommonTablesDAO.deleteBatches(batchid, companyid);
                    }
                    
                    // Delete Purcahse Return Term Map
                    HashMap<String, Object> termReqMap = new HashMap<String, Object>();
                    termReqMap.put("purchasereturnid", srid);
                    accInvoiceDAOobj.deletePurchaseReturnTermMap(termReqMap);

                   /* Updating Isopen Flag=0 of invoice & Goods receipt during Editing PR*/
                    if (!StringUtil.isNullOrEmpty(deletedLinkedDocumentID)) {
                        String[] deletedLinkedDocumentIDArr = deletedLinkedDocumentID.split(",");
                        HashMap<String, Object> linkRequestParams = new HashMap<String, Object>();
                        for (int i = 0; i < deletedLinkedDocumentIDArr.length; i++) {
                            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), deletedLinkedDocumentIDArr[i]);
                            GoodsReceipt goodsreceipt = (GoodsReceipt) venresult.getEntityList().get(0);
                            if (goodsreceipt != null) {
                                linkRequestParams.put("isOpen", true);
                                linkRequestParams.put("goodsReceiptID", goodsreceipt.getID());
                                linkRequestParams.put(Constants.companyKey, companyid);
                                accInvoiceDAOobj.updateGoodsReceiptStatus(linkRequestParams);
                                if (i == 0) {
                                    unlinkMessage += " from Purchase Invoice(s) ";
                                }
                                if (unlinkMessage.indexOf(goodsreceipt.getGoodsReceiptNumber()) == -1) {
                                    unlinkMessage += goodsreceipt.getGoodsReceiptNumber() + ", ";
                                }
                            } else {
                                venresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), deletedLinkedDocumentIDArr[i]);
                                GoodsReceiptOrder grorder = (GoodsReceiptOrder) venresult.getEntityList().get(0);
                                if (grorder != null) {
                                    linkRequestParams.put("isOpen", true);
                                    linkRequestParams.put("goodsReceiptOrderID", grorder.getID());
                                    linkRequestParams.put(Constants.companyKey, companyid);
                                    accInvoiceDAOobj.updateGoodsReceiptOrderStatus(linkRequestParams);
                                    if (i == 0) {
                                        unlinkMessage += " from Goods Receipt(s) ";
                                    }
                                    if (unlinkMessage.indexOf(grorder.getGoodsReceiptOrderNumber()) == -1) {
                                        unlinkMessage += grorder.getGoodsReceiptOrderNumber() + ", ";
                                    }
                                }
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(unlinkMessage) && unlinkMessage.endsWith(", ")) {
                        unlinkMessage = unlinkMessage.substring(0, unlinkMessage.length() - 2);
                    }

                }
                
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Purchase_Return_ModuleId, entryNumber, companyid);
                    if (!resultList.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                        String formatName = (String) resultList.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException("Entered document number " + " <b>" + entryNumber + "</b> " + " is belongs to Auto Sequence Format " + " <b>" + formatName + "</b>. " + "Please select Sequence Format " + " <b>" + formatName + "</b> " + " instead of entering document number manually.");
                        }
                    }
                }
            }
            
            double totalTermAmount = 0;
            double totalTermTaxAmount = 0;
            String InvoiceTerms = request.getParameter("invoicetermsmap");
            if (!StringUtil.isNullOrEmpty(InvoiceTerms)) {
                JSONArray termsArr = new JSONArray(InvoiceTerms);
                for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                    double termamount = 0;
                    if (gstIncluded) {
                        termamount = termsArr.getJSONObject(cnt).optDouble("termAmountExcludingTax",0);
                    } else {
                        termamount = termsArr.getJSONObject(cnt).optDouble("termamount",0);
                    }
                    totalTermAmount += termamount;
                    double termTaxAmount = termsArr.getJSONObject(cnt).optDouble("termtaxamount", 0);
                    totalTermTaxAmount += termTaxAmount;
                }
            }

            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
            DateFormat df = authHandler.getDateOnlyFormat(request);
            if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(srid)) {
                doDataMap.put("entrynumber", entryNumber);
            } else {
                doDataMap.put("entrynumber", "");
            }
//            String taxid = request.getParameter("taxid");;
            String taxid = "";
            if (request.getParameter("taxid")!=null && request.getParameter("taxid").equalsIgnoreCase("None")) {
                taxid = null;
            } else {
                taxid = request.getParameter("taxid");
            }
            doDataMap.put("taxid", taxid);//    ERP-28612
            doDataMap.put("autogenerated", sequenceformat.equals("NA") ?  false : true);
            doDataMap.put(Constants.memo, request.getParameter(Constants.memo));
            doDataMap.put("externalCurrencyRate", externalCurrencyRate);
            doDataMap.put(Constants.posttext, request.getParameter(Constants.posttext) == null ? "" : request.getParameter(Constants.posttext));
            doDataMap.put("vendor", request.getParameter("vendor"));
            if (request.getParameter(Constants.shipdate) != null && !StringUtil.isNullOrEmpty(request.getParameter(Constants.shipdate))) {
                doDataMap.put(Constants.shipdate, df.parse(request.getParameter(Constants.shipdate)));
            }
            doDataMap.put(Constants.shipvia, request.getParameter(Constants.shipvia));
            doDataMap.put(Constants.fob, request.getParameter(Constants.fob));
            doDataMap.put("orderdate", df.parse(request.getParameter(Constants.BillDate)));
            doDataMap.put("transactiondate", formatter.parse(request.getParameter(Constants.BillDate)));

            doDataMap.put("isfavourite", isfavourite);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                doDataMap.put("costCenterId", costCenterId);
            }
            doDataMap.put(Constants.companyKey, companyid);
            doDataMap.put(Constants.currencyKey, currencyid);

            doDataMap.put("createdon", createdon);
            doDataMap.put("createdby", createdby);
            doDataMap.put("modifiedby", modifiedby);
            doDataMap.put("updatedon", updatedon);
            doDataMap.put("isNoteAlso", isNoteAlso);
            doDataMap.put(Constants.isConsignment, isConsignment);
            doDataMap.put(Constants.isFixedAsset, isFixedAsset);
            doDataMap.put("gstIncluded", gstIncluded);            
            doDataMap.put(Constants.EWAYApplicable, EWAYApplicable);
            doDataMap.put("isApplyTaxToTerms", isApplyTaxToTerm);
            if (!StringUtil.isNullOrEmpty(supplierinvoiceno)) {
                doDataMap.put(Constants.SUPPLIERINVOICENO, supplierinvoiceno);
            }
            if (!StringUtil.isNullOrEmpty(mvattransactionno)) {
                doDataMap.put(Constants.MVATTRANSACTIONNO, mvattransactionno);
            }else{
                doDataMap.put(Constants.MVATTRANSACTIONNO, "");
            }
            double totalAmt = 0, totalRowDiscount = 0, totalAmountinbase = 0;
            double subTotal = 0, taxAmt = 0;
            JSONArray jArr = new JSONArray(request.getParameter(Constants.detail));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);

                double qrate = authHandler.roundUnitPrice(jobj.optDouble("rate", 0), companyid);
                double quantity = authHandler.roundQuantity(jobj.getDouble("quantity"), companyid);
                double quotationPrice = authHandler.round(quantity * qrate, companyid);
                double discountQD = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                double discountPerRow = 0;

                if (jobj.optInt("discountispercent", 0) == 1) {//percent discount
                    discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                } else {//flat discount
                    discountPerRow = discountQD;
                }

                totalRowDiscount += discountPerRow;
                
                /*
                 * In case of Asset & Consignment Module getting amount calcuted
                 * with discount & tax included
                 */
                if (isFixedAsset || isConsignment) {
                    subTotal += Double.parseDouble((String) jobj.get("amount"));
                }
              /*
                 * If line level tax applied then it will execute only in
                 * dashbord transaction
                 */
                if (!isFixedAsset && !isConsignment) {
                    if (jobj.getDouble("taxamount") != 0) {
                        taxAmt += jobj.getDouble("taxamount");
                    }
                }
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("subTotal"))) {
                subTotal = Double.parseDouble(request.getParameter("subTotal"));
            }
            /*
             * if global level tax applied it will execute only for dashbord
             * transaction
             */
            if (taxAmt == 0) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("taxamount"))) {
                    taxAmt = Double.parseDouble(request.getParameter("taxamount"));
                    if(!StringUtil.isNullOrEmpty(taxid)){
                        taxAmt -= totalTermTaxAmount;
                    }
                }
            }

            totalAmt = subTotal + taxAmt + totalTermAmount + totalTermTaxAmount;
            totalRowDiscount = authHandler.round(totalRowDiscount, companyid);
            doDataMap.put("totallineleveldiscount", totalRowDiscount);
            doDataMap.put("totalamount", totalAmt);

            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filterRequestParams.put(Constants.companyKey, companyid);
            filterRequestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmt, currencyid, df.parse(request.getParameter(Constants.BillDate)), externalCurrencyRate);
            totalAmountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            doDataMap.put("totalamountinbase", totalAmountinbase);

            KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalRowDiscount, currencyid, df.parse(request.getParameter(Constants.BillDate)), externalCurrencyRate);
            double descountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);

            doDataMap.put("discountinbase", descountinBase);
            if (extraCompanyPreferences.isIsNewGST()) {
                /**
                 * ERP-32829
                 */
                boolean gstapplicable = !StringUtil.isNullOrEmpty(request.getParameter("GSTApplicable"));
                doDataMap.put("gstapplicable", gstapplicable);
            }
            
            KwlReturnObject doresult = accGoodsReceiptobj.savePurchaseReturn(doDataMap);
            salesReturn = (PurchaseReturn) doresult.getEntityList().get(0);
            /**
             * Save GST History Customer/Vendor data.
             */
            if (salesReturn.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                JSONObject paramJobj=new JSONObject();
                paramJobj.put("docid", salesReturn.getID());
                paramJobj.put("gstdochistoryid", request.getParameter("gstdochistoryid"));
                paramJobj.put("CustomerVendorTypeId", request.getParameter("CustomerVendorTypeId"));
                paramJobj.put("GSTINRegistrationTypeId", request.getParameter("GSTINRegistrationTypeId"));
                paramJobj.put("gstin", request.getParameter("gstin"));
                paramJobj.put("moduleid", Constants.Acc_Purchase_Return_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
            String jeentryNumber = "";
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            String addressID="";
            boolean isEditNote = false;
            Date creationDate = df.parse(request.getParameter(Constants.BillDate));
            DebitNote debitnote = null;
            KwlReturnObject crResult;
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            JournalEntry journalEntry = null;
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
            KwlReturnObject jeresult = null;
            HashMap<String, Object> dnhm = new HashMap<String, Object>();
            Map<String, Object> grAmountsmap = new HashMap<>();
            
            if (isNoteAlso) {
                JournalEntry jetemp = null;
                if (!StringUtil.isNullOrEmpty(srid)) {  //check if its purchase return edit case 
                    KwlReturnObject idresult = accDebitNoteobj.getDebitNoteIdFromPRId(srid, companyid);
                    if (!(idresult.getEntityList().isEmpty())) {
                        debitnote = (DebitNote) idresult.getEntityList().get(0);
                    }
                    isEditNote = true;
                    if (debitnote != null) {
                          /**
                         * check the goods receipts linked to purchase return.
                         */
                        KwlReturnObject PRLObj = accGoodsReceiptobj.getGoodsReceiptsLinkedWithPR(salesReturn.getID());
                        /**
                         * check the goods receipts linked to Debit note created
                         * with purchase return.
                         */
                        boolean isEditPRwithDN = true;
                        KwlReturnObject dnObj = accDebitNoteobj.getDNLinkedWithGoodsReceipts(debitnote.getID(), companyid);
                        /**
                         * Function to allow edit the PR with DN document or not
                         */
                        HashMap<String,Object> PRwithDNparams = new HashMap<>();
                        PRwithDNparams.put("PRLObj", PRLObj);
                        PRwithDNparams.put("dnObj", dnObj);
                        isEditPRwithDN = isEditPRwithDNDocument(PRwithDNparams);
                        
                        if (!isEditPRwithDN) {
                            throw new AccountingException(messageSource.getMessage("acc.dimension.module.3", null, RequestContextUtils.getLocale(request))+" (" +debitNoteNumber+ ") "+messageSource.getMessage("acc.purchaseretutrnwithDN.cannoteditprwithdn", null, RequestContextUtils.getLocale(request)));
                        }
                        /* Deleting Linking information of Purchase Return during Editing Purchase Return*/
                        accGoodsReceiptobj.deleteLinkingInformationOfPR(requestParams);
                        /**
                         * Get original amounts of goods receipts before deletion of dndetails.
                         */
                        Set<DebitNoteDetail> debitNoteDetail = debitnote.getRows();
                        for (DebitNoteDetail detail : debitNoteDetail) {
                            if (detail.getGoodsReceipt() != null) {
                                grAmountsmap.put(detail.getGoodsReceipt().getID(), detail.getDiscount().getOriginalAmount());
                            }
                        }
                        /*
                         Check Debit Note link to Payment or not if it is linked then throw exception
                         */
                        dnObj = accDebitNoteobj.getDNLinkedWithPayment(debitnote.getID(), companyid);
                        
                        if (dnObj.getEntityList() != null && !dnObj.getEntityList().isEmpty()) {
                            DebitNotePaymentDetails debitNotePaymentDetails = (DebitNotePaymentDetails) dnObj.getEntityList().get(0);
                            throw new AccountingException("Debit Note (" + debitNotePaymentDetails.getDebitnote().getDebitNoteNumber() + ") created from this Purchase Return is linked with Payment. so it cannot be edited.");
                        }
                        oldjeid = debitnote.getJournalEntry().getID();
                        jetemp = debitnote.getJournalEntry();
                        addressID=debitnote.getBillingShippingAddresses()!=null?debitnote.getBillingShippingAddresses().getID():"";
                        if (debitnote.getDnTaxEntryDetails()!= null && !debitnote.getDnTaxEntryDetails().isEmpty()) {
                            String ids = "";
                            for (DebitNoteTaxEntry noteTaxEntry : debitnote.getDnTaxEntryDetails()) {
                                ids += "'" + noteTaxEntry.getID() + "',";
                            }
                            if(!StringUtil.isNullOrEmpty(ids)){
                                accDebitNoteobj.deleteDebitNoteDetailTermMap(ids.substring(0, ids.length() - 1));
                            }
                        }
                    }
                    if (jetemp != null) {  //in edit case get all the detail
                        jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                        jeautogenflag = jetemp.isAutoGenerated();
                        jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                        jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    }

                    if (debitnote != null) {
                        accDebitNoteService.updateOpeningInvoiceAmountDue(debitnote.getID(), companyid);
                    }
                    crResult = accDebitNoteobj.deleteDebitNoteDetails(debitnote.getID(), companyid);
                    crResult = accDebitNoteobj.deleteDebitTaxDetails(debitnote.getID(), companyid);

                    //Delete old entries and insert new entries again from optimized table in edit case.
                    accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                    deleteJEDetailsCustomData(oldjeid);
                    dnhm.put("dnid", debitnote.getID());
                }

                if (StringUtil.isNullOrEmpty(oldjeid)) {  //in create new case           
                    synchronized (this) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put(Constants.companyKey, companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false,creationDate);
                        jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        jeSeqFormatId = format.getID();
                        jeautogenflag = true;
                    }
                }
                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put("autogenerated", jeautogenflag);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                jeDataMap.put("entrydate", creationDate);
                jeDataMap.put(Constants.companyKey, companyid);
                jeDataMap.put(Constants.memo, "Debit Note For Purchase Return " + entryNumber);
                jeDataMap.put(Constants.currencyKey, currencyid);
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                jeid = journalEntry.getID();
                jeDataMap.put("jeid", jeid);
            }
            // Purchase Return
            Set<JournalEntryDetail> inventoryJEDetails = new HashSet<>();
            String inventoryJEid = "";
            JournalEntry inventoryJE = null;
            boolean postInventoryJournalEntry = false;
            /*
             * Check if there is any non-inventory present or not, as Inventory Journal Entry should not be posted for non-inventory products.
             */
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.productid))) {
                    KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString(Constants.productid));
                    Product product = (Product) proresult.getEntityList().get(0);
                    /**
                     * Added (!product.isAsset) -: Asset PR - JE should not be posted ERP-38879.
                     */
                    if (product != null && product.getProducttype() != null && !(product.getProducttype().getID().equals(Producttype.SERVICE) || product.getProducttype().getID().equals(Producttype.NON_INVENTORY_PART) || product.getProducttype().getID().equals(Producttype.Inventory_Non_Sales)) && !product.isAsset()) {
                        postInventoryJournalEntry = true;
                        break;
                    }
                }
            }
            try {
                if (extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) && postInventoryJournalEntry) {
                    String oldjeid1 = null;
                    String jeentryNumber1 = null;
                    boolean jeautogenflag1 = false;
                    String jeIntegerPart1 = "";
                    String jeDatePrefix1 = "";
                    String jeDateAfterPrefix1 = "";
                    String jeDateSuffix1 = "";
                    String jeSeqFormatId1 = "";
                    if (salesReturn != null && salesReturn.getInventoryJE() != null) {
                        jeentryNumber1 = salesReturn.getInventoryJE().getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                        jeautogenflag1 = salesReturn.getInventoryJE().isAutoGenerated();
                        jeSeqFormatId1 = salesReturn.getInventoryJE().getSeqformat() == null ? "" : salesReturn.getInventoryJE().getSeqformat().getID();
                        jeIntegerPart1 = String.valueOf(salesReturn.getInventoryJE().getSeqnumber());
                        jeDatePrefix1 = salesReturn.getInventoryJE().getDatePreffixValue();
                        jeDateAfterPrefix1 = salesReturn.getInventoryJE().getDateAfterPreffixValue();
                        jeDateSuffix1 = salesReturn.getInventoryJE().getDateSuffixValue();
                        oldjeid1 = salesReturn.getInventoryJE().getID();
                        salesReturn.setInventoryJE(null);
                        accGoodsReceiptobj.updatePurchaseReturnSetNull(salesReturn);
                        accJournalEntryobj.deleteJournalEntryPermanent(oldjeid1, companyid);
                    }
                    Map<String, Object> jeDataMap1 = AccountingManager.getGlobalParams(request);
                    if (StringUtil.isNullOrEmpty(oldjeid1)) {
                        jeDataMap1.put("entrynumber", "");
                        jeDataMap1.put("autogenerated", true);
                    } else {
                        jeDataMap1.put("entrynumber", jeentryNumber1);
                        jeDataMap1.put(Constants.SEQFORMAT, jeSeqFormatId1);
                        jeDataMap1.put(Constants.SEQNUMBER, jeIntegerPart1);
                        jeDataMap1.put(Constants.DATEPREFIX, jeDatePrefix1);
                        jeDataMap1.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix1);
                        jeDataMap1.put(Constants.DATESUFFIX, jeDateSuffix1);
                        jeDataMap1.put("autogenerated", jeautogenflag1);
                    }
                    jeDataMap1.put("entrydate", creationDate);
                    jeDataMap1.put(Constants.companyKey, companyid);
                    jeDataMap1.put("createdby", createdby);
                    jeDataMap1.put(Constants.memo, request.getParameter(Constants.memo));
                    jeDataMap1.put(Constants.currencyKey, currencyid);
                    jeDataMap1.put("costcenterid", costCenterId);
                    jeDataMap1.put("transactionModuleid", Constants.Acc_Purchase_Return_ModuleId);
                    jeDataMap1.put("transactionId", salesReturn.getID());
                    jeDataMap1.put(JournalEntryConstants.EXTERNALCURRENCYRATE, salesReturn.getExternalCurrencyRate());
                    KwlReturnObject jeresult1 = accJournalEntryobj.saveJournalEntry(jeDataMap1);
                    inventoryJE = (JournalEntry) jeresult1.getEntityList().get(0);
                    inventoryJEid = inventoryJE.getID();
                    salesReturn.setInventoryJE(inventoryJE);
                }
            } catch (Exception ex) {
                Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
            }

            doDataMap.put(Constants.Acc_id, salesReturn.getID());
            List dodetails = savePurchaseReturnRows(request, salesReturn, companyid, journalEntry,inventoryJEDetails,inventoryJEid);
            if (inventoryJE != null && inventoryJEDetails != null && extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                inventoryJE.setDetails(inventoryJEDetails);
                accJournalEntryobj.saveJournalEntryDetailsSet(inventoryJEDetails);
            }
            // Create Debit Note
            if (isNoteAlso) {
                KwlReturnObject debitnoteresult=null;
                if (StringUtil.isNullOrEmpty(srid)) {// create new case
                    synchronized (this) {
                        if (debitNoteSequenceFormat.equalsIgnoreCase("NA")) { //create new case with sequence format NA
                            dnhm.put("autogenerated", false);
                        } else { //create new case with sequence format other than NA
                            dnhm.put("autogenerated", true);
                        }
                    }
                } else { // edit case
                    if (debitNoteSequenceFormat.equalsIgnoreCase("NA")) { //create new case with sequence format NA
                        debitnoteresult = accDebitNoteobj.getDNFromNoteNoAndId(debitNoteNumber, companyid, debitnote.getID());
                        if (debitnoteresult.getRecordTotalCount() > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.Debitnotenumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }
                if (debitNoteSequenceFormat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Debit_Note_ModuleId, debitNoteNumber, companyid);
                    if (!resultList.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                        String formatName = (String) resultList.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + debitNoteNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }
                HashSet<DebitNoteDetail> dndetails = (HashSet) dodetails.get(1);
                HashSet<DebitNoteTaxEntry> dnTaxEntryDetails = (HashSet<DebitNoteTaxEntry>) dodetails.get(3);
                jedetails = (HashSet) dodetails.get(2);

                double totalDNAmt = (Double) dodetails.get(4);
                double totalDNAmtExludingTax = (Double) dodetails.get(5);
                double totalDiscountAmt = (Double) dodetails.get(6);

                KwlReturnObject vendorResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), request.getParameter("vendor"));
                Vendor vendor = (Vendor) vendorResult.getEntityList().get(0);
                if (debitNoteSequenceFormat.equals("NA") || !StringUtil.isNullOrEmpty(srid)) {
                    dnhm.put("entrynumber", debitNoteNumber);
                } else {
                    dnhm.put("entrynumber","");
                }
                dnhm.put("oldRecord", false);
                
                // Adjust TDS amount for JE
                if(extraCompanyPreferences.isTDSapplicable() && tdsAmount>0 && vendor.getNatureOfPayment()!=null){
                    totalDNAmt-=tdsAmount;
                }
                
                //saving default address of vendor to DN
                Map<String, Object> addressParams = Collections.EMPTY_MAP;
                addressParams = AccountingAddressManager.getDefaultVendorAddressParams(vendor.getID(), companyid, accountingHandlerDAOobj);
                /**
                 * While creating Sales Return With Credit Note get address
                 * details from linked invoice if any invoice linked
                 */
                JSONObject requestParamsJSON = new JSONObject();
                requestParamsJSON = StringUtil.convertRequestToJsonObject(request);
                String linkMode = requestParamsJSON.optString("fromLinkCombo", "");
                String[] linkNumbers = requestParamsJSON.optString("linkNumber", "").split(",");
                if (StringUtil.isNullOrEmpty(addressID) && !StringUtil.isNullOrEmpty(linkMode)  && linkMode.equalsIgnoreCase("Purchase Invoice") 
                        && linkNumbers != null && linkNumbers.length == 1 && !StringUtil.isNullOrEmpty(linkNumbers[0])) {
                    JSONObject addressparamsJSON = new JSONObject();
                    KwlReturnObject PIresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), linkNumbers[0]);
                    GoodsReceipt goodsReceipt = PIresult !=null? (GoodsReceipt) PIresult.getEntityList().get(0) : null;
                    if (goodsReceipt != null) {
                        addressparamsJSON = AccountingAddressManager.getTransactionAddressJSON(addressparamsJSON, goodsReceipt.getBillingShippingAddresses(), true);
                        addressParams = AccountingAddressManager.getAddressParamsJson(addressparamsJSON,true);
                    }
                }
                /**
                 * In Edit case get address details from existing records.
                 */
                if(!StringUtil.isNullOrEmpty(addressID)){//If Edit case then updating existing CN address
                   KwlReturnObject addressResult = accountingHandlerDAOobj.getObject(BillingShippingAddresses.class.getName(), addressID);
                   BillingShippingAddresses billingShippingAddresses = addressResult !=null? (BillingShippingAddresses) addressResult.getEntityList().get(0) : null;
                    if (billingShippingAddresses != null) {
                        JSONObject addressparamsJSON = new JSONObject();
                        /**
                         * Get Data In address new params JSON in Edit case
                         */
                        addressparamsJSON = AccountingAddressManager.getTransactionAddressJSON(addressparamsJSON, billingShippingAddresses, true);
                        addressParams = AccountingAddressManager.getAddressParamsJson(addressparamsJSON,true);
                    }
                   addressParams.put(Constants.Acc_id, addressID);
                }
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                dnhm.put("billshipAddressid", bsa.getID());
                
                dnhm.put(Constants.memo, "Debit Note for Purchase Return " + entryNumber);
                dnhm.put(Constants.companyKey, companyid);
                dnhm.put(Constants.currencyKey, currencyid);
                dnhm.put("createdby", createdby);
                dnhm.put("modifiedby", modifiedby);
                dnhm.put("createdon", createdon);
                dnhm.put("updatedon", updatedon);
                dnhm.put("purchaseReturnId", salesReturn.getID());
                dnhm.put("includingGST", gstIncluded);
                dnhm.put("creationDate", creationDate);

                dnhm.put("journalentryid", jeid);
                dnhm.put("vendorid", vendor.getID());

                dnhm.put("otherwise", true);
                dnhm.put("openflag", true);

                dnhm.put("dnamount", totalDNAmt);
                if (!StringUtil.isNullOrEmpty(supplierinvoiceno)) {
                    dnhm.put(Constants.SUPPLIERINVOICENO, supplierinvoiceno);
                }
               /*
                * Saving the total amount in base currency 
                */
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalDNAmt, currencyid, creationDate, externalCurrencyRate);
                double dnamountinbase = (Double) baseAmount.getEntityList().get(0);
                dnamountinbase = authHandler.round(dnamountinbase, companyid);
                dnhm.put("dnamountinbase", dnamountinbase);        
                dnhm.put("dnamountdue", totalDNAmt);
                /**
                 * while saving PR with DN document, DN type is changed to 2
                 * so that DN will treat like DN otherwise document.
                 */
                dnhm.put("cntype", 2);
                dnhm.put("accountId", vendor.getAccount().getID());
                KwlReturnObject result = null;
                if (isEditNote) {
                    result = accDebitNoteobj.updateDebitNote(dnhm);
                } else {
                    dnhm.put("approvestatuslevel", 11);
                    result = accDebitNoteobj.addDebitNote(dnhm);
                }

                debitnote = (DebitNote) result.getEntityList().get(0);

                dnhm.put("dnid", debitnote.getID());
                debitNoteId=debitnote.getID();
                for (DebitNoteDetail cnd:dndetails) {
                    cnd.setDebitNote(debitnote);
                }
                dnhm.put("dndetails", dndetails);
                double srTaxAmount = 0;                      //calculated total cn tax amount
                if (salesReturn.getTax() != null) {
                    double taxPercent = 0;
                    KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid, salesReturn.getOrderDate(), salesReturn.getTax().getID());
                    taxPercent = (Double) taxresult.getEntityList().get(0);
                    srTaxAmount = (taxPercent == 0 ? 0 : authHandler.round((totalDNAmtExludingTax * taxPercent / 100), companyid));
                } else {
                    srTaxAmount = authHandler.round(taxAmt, companyid);
                }
                double dnTaxAmount = 0;
                int ind = 0;
                for (DebitNoteTaxEntry noteTaxEntry:dnTaxEntryDetails) {
                    noteTaxEntry.setDebitNote(debitnote);
                    dnTaxAmount+=noteTaxEntry.getTaxamount();                 //calculated total dn tax amount
                    /**
                     * Save Term details for CN in Sales Return case
                     */
                    if (extraCompanyPreferences.isIsNewGST()) {
                        JSONArray jArrr = new JSONArray(StringUtil.isNullOrEmpty(request.getParameter(Constants.detail)) ? "[]" : request.getParameter(Constants.detail));
                        JSONObject jobj1 = jArrr.getJSONObject(ind);
                        if (!StringUtil.isNullOrEmpty((String) jobj1.optString("LineTermdetails"))) {
                            JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj1.optString("LineTermdetails")));
                            for (int j = 0; j < termsArray.length(); j++) {
                                JSONObject termObject = termsArray.getJSONObject(j);
                                /**
                                 * Save GST Terms details in the Term table
                                 * for payment
                                 */
                                HashMap<String, Object> dnDetailsTermsMap = new HashMap<>();
                                if (termObject.has("termid")) {
                                    dnDetailsTermsMap.put("term", termObject.get("termid"));
                                }
                                if (termObject.has("termamount")) {
                                    dnDetailsTermsMap.put("termamount", termObject.get("termamount"));
                                }
                                if (termObject.has("termpercentage")) {
                                    dnDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                                }
                                if (termObject.has("purchasevalueorsalevalue")) {
                                    dnDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                                }
                                if (termObject.has("deductionorabatementpercent")) {
                                    dnDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                                }
                                if (termObject.has("assessablevalue")) {
                                    dnDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                                }
                                if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                                    dnDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                                    if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                        if (termObject.getInt("taxtype") == 0) { // If Flat
                                            dnDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                        } else { // Else Percentage
                                            dnDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                        }
                                    }
                                }
                                if (termObject.has("id")) {
                                    dnDetailsTermsMap.put("id", termObject.get("id"));
                                }
                                dnDetailsTermsMap.put("debitNoteTaxEntry", noteTaxEntry.getID());
                                dnDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                                dnDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                                dnDetailsTermsMap.put("userid", debitnote.getCreatedby().getUserID());
                                dnDetailsTermsMap.put("product", termObject.opt("productid"));
                                dnDetailsTermsMap.put("createdOn", new Date());
                                accDebitNoteobj.saveDebitNoteDetailTermMap(dnDetailsTermsMap);
                            }
                        }
                    }
                    ind++;
                }
                dnTaxAmount=authHandler.round(dnTaxAmount, companyid);
                dnhm.put("debitNoteTaxEntryDetails", dnTaxEntryDetails);
                result = accDebitNoteobj.updateDebitNote(dnhm);
                debitnote = (DebitNote) result.getEntityList().get(0);
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put(Constants.companyKey, companyid);
                if (srTaxAmount != dnTaxAmount) {
                    jedjson.put("amount", srTaxAmount - dnTaxAmount < 0 ? totalDNAmt - authHandler.round(Math.abs(srTaxAmount - dnTaxAmount), companyid) : totalDNAmt + authHandler.round(srTaxAmount - dnTaxAmount, companyid));
                } else {
                    jedjson.put("amount", totalDNAmt);
                }
                jedjson.put("accountid", vendor.getAccount().getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
                dnhm.put("vendorentry", jed.getID());
                if (totalDiscountAmt > 0) {
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put(Constants.companyKey, companyid);
                    jedjson.put("amount", totalDiscountAmt);
                    jedjson.put("accountid", preferences.getDiscountReceived().getID());
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
                if (srTaxAmount != dnTaxAmount) {                        //if cn tax is diffrent than sr tax then add rounding diffrence jedetail
                    JSONObject jedjsonTD = new JSONObject();
                    jedjsonTD.put("srno", jedetails.size() + 1);
                    jedjsonTD.put(Constants.companyKey, companyid);
                    jedjsonTD.put("amount", srTaxAmount-dnTaxAmount<0?-(srTaxAmount-dnTaxAmount):srTaxAmount-dnTaxAmount);
                    jedjsonTD.put("accountid", preferences.getRoundingDifferenceAccount() !=null?preferences.getRoundingDifferenceAccount().getID():null);
                    jedjsonTD.put("debit", srTaxAmount-dnTaxAmount>0?false:true);
                    jedjsonTD.put("setroundingdifferencedetail", true);
                    jeDataMap.put("isroundingdfinCNtax", true);
                    jedjsonTD.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonTD);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
                // add TDS entry in Debit Note JE (Purchase return with debit note case)
                if (extraCompanyPreferences.isTDSapplicable() && tdsAmount > 0) {
                    HashSet<PurchaseReturnDetail> prdetails = (HashSet) dodetails.get(0);
                    if (dodetails != null && !dodetails.isEmpty()) { //update balance quantity to po when creating gr link to pi and pi link to po
                        for (PurchaseReturnDetail prDetail : prdetails) {
                            if (prDetail.getTdsLineAmount() != 0.0 && prDetail.getTdsPayableAccount()!=null) {
                                jedjson = new JSONObject();
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put("companyid", companyid);
                                jedjson.put("amount", prDetail.getTdsLineAmount());
                                jedjson.put("accountid", prDetail.getTdsPayableAccount().getID());
                                jedjson.put("debit", true);
                                jedjson.put("jeid", jeid);
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                        }
                    }
                }
                
                externalCurrencyRate = StringUtil.getDouble(request.getParameter(Constants.externalcurrencyrate));
                result = accDebitNoteobj.updateDebitNote(dnhm);
              
                jeDataMap.put("jedetails", jedetails);
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("transactionId", debitnote.getID());
                jeDataMap.put("transactionModuleid", Constants.Acc_Debit_Note_ModuleId);
                jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
                jeDataMap.remove("isroundingdfinCNtax");
                
                JournalEntry cnJE = (JournalEntry) jeresult.getEntityList().get(0);               //if cn tax is diffrent than sr tax then add rounding diffrence cn tax entry
                Set<JournalEntryDetail> cnJED = cnJE.getDetails();
                for (JournalEntryDetail jedCN : cnJED) {
                    if (jedCN.isRoundingDifferenceDetail() && srTaxAmount != dnTaxAmount && (jedCN.getAmount() == Math.abs(srTaxAmount - dnTaxAmount))) {
                        DebitNoteTaxEntry taxEntry = new DebitNoteTaxEntry();
                        String CreditNoteTaxID = StringUtil.generateUUID();
                        taxEntry.setID(CreditNoteTaxID);
                        taxEntry.setAccount(jedCN.getAccount());
                        taxEntry.setAmount(jedCN.getAmount());
                        taxEntry.setCompany(salesReturn.getCompany());
                        taxEntry.setTax(null);
                        taxEntry.setSrNoForRow(1);
                        String reasonID = accCreditNoteService.getReasonIDByName("Rounding Difference", companyid);
                        KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), reasonID);
                        MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
                        taxEntry.setReason(reason);
                        taxEntry.setDescription("Rounding Difference");
                        taxEntry.setDebitForMultiCNDN(true);
                        taxEntry.setTotalJED(jedCN);
                        taxEntry.setIsForDetailsAccount(jedCN.isDebit());
                        taxEntry.setTaxamount(0);
                        taxEntry.setDebitNote(debitnote);
                        dnTaxEntryDetails.add(taxEntry);
                        dnhm.put("dnamount", totalDNAmtExludingTax + srTaxAmount);
                        dnhm.put("dnamountdue", totalDNAmtExludingTax + srTaxAmount);
                        baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalDNAmtExludingTax + srTaxAmount, currencyid, creationDate, externalCurrencyRate);
                        dnamountinbase = (Double) baseAmount.getEntityList().get(0);
                        dnamountinbase = authHandler.round(dnamountinbase, companyid);
                        dnhm.put("dnamountinbase", dnamountinbase);
                        break;
                    }
                }
                dnhm.put("debitNoteTaxEntryDetails", dnTaxEntryDetails);
                result = accDebitNoteobj.updateDebitNote(dnhm);
                journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                String customfield = request.getParameter(Constants.customfield);
                // save custom field details for auto created case
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    JSONArray jcustomarray = new JSONArray(customfield);
                    jcustomarray = fieldDataManagercntrl.getComboValueIdsForCurrentModule(jcustomarray, Constants.Acc_Debit_Note_ModuleId, companyid, 0);            // 1= for line item
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                    customrequestParams.put("modulerecid", journalEntry.getID());
                    customrequestParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        jeDataMap.put("accjecustomdataref", journalEntry.getID());
                        jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                    }
                }
                deleteJEArray(oldjeid, companyid);   //deleted old je in edit case
                
                //code for linking DN with Purchase invoice
                if (!StringUtil.isNullOrEmpty(request.getParameter("linkNumber"))) {//this will execute only when invoices linked in Sales return
                    List li = accDebitNoteService.linkDebitNote(request, debitnote.getID(),isInsertAudTrail,grAmountsmap);
                }
            }


            String customfield = request.getParameter(Constants.customfield);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_PurchaseReturn_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_PurchaseReturnId);
                customrequestParams.put("modulerecid", salesReturn.getID());
                customrequestParams.put(Constants.moduleid, isConsignment?Constants.Acc_ConsignmentPurchaseReturn_ModuleId:isFixedAsset?Constants.Acc_FixedAssets_Purchase_Return_ModuleId:Constants.Acc_Purchase_Return_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseReturn_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    doDataMap.put("accpurchasereturncustomdataref", salesReturn.getID());
                    KwlReturnObject accresult = accGoodsReceiptobj.updatePurchaseReturnCustomData(doDataMap);
                }
            }
     
            String linkMode = request.getParameter("fromLinkCombo");
            String[] linkNumbers = request.getParameter("linkNumber").split(",");
            if (linkMode.equalsIgnoreCase(Constants.Goods_Receipt) || linkMode.equalsIgnoreCase("Consignment Goods Receipt") || linkMode.equalsIgnoreCase("Asset Goods Receipt")) {
                HashSet<PurchaseReturnDetail> prdetails = (HashSet) dodetails.get(0);
                if (dodetails != null && !dodetails.isEmpty()) { //update balance quantity to po when creating gr link to pi and pi link to po
                    for (PurchaseReturnDetail cnt : prdetails) {
                        if (cnt.getGrdetails() != null && cnt.getGrdetails().getVidetails()!=null&&cnt.getGrdetails().getVidetails().getPurchaseorderdetail() != null) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), cnt.getGrdetails().getVidetails().getPurchaseorderdetail().getID());
                            PurchaseOrderDetail purchaseorderdetails = (PurchaseOrderDetail) rdresult.getEntityList().get(0);
                            HashMap poMap = new HashMap();
                            poMap.put("podetails", purchaseorderdetails.getID());
                            poMap.put(Constants.companyKey, purchaseorderdetails.getCompany().getCompanyID());
                            poMap.put("balanceqty", cnt.getReturnQuantity());
                            poMap.put("add", true);
                            /* Updating islineitemclosed & ispoclosed flag to false if GR is returned*/
                            cnt.getGrdetails().getVidetails().getPurchaseorderdetail().setIsLineItemClosed(false);
                            cnt.getGrdetails().getVidetails().getPurchaseorderdetail().getPurchaseOrder().setIsPOClosed(false);
                            accCommonTablesDAO.updatePurchaseOrderStatus(poMap);
                        }
                    }
                }
                for (int i = 0; i < linkNumbers.length; i++) {
                    if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                        updateOpenStatusFlagInGRForPR(linkNumbers[i], companyid,salesReturn.getID());
                        
                        /* Saving Linking information of Purchasereturn */
                        HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                        requestParamsLinking.put("linkeddocid", salesReturn.getID());
                        requestParamsLinking.put("docid", linkNumbers[i]);
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
                        requestParamsLinking.put("linkeddocno", entryNumber);
                        requestParamsLinking.put("sourceflag", 0);
                        KwlReturnObject result = accGoodsReceiptobj.saveGRLinking(requestParamsLinking);
                        requestParamsLinking.put("linkeddocid", linkNumbers[i]);
                        requestParamsLinking.put("docid", salesReturn.getID());
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Goods_Receipt_ModuleId);
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), linkNumbers[i]);
                        GoodsReceiptOrder goodsreceiptorder = (GoodsReceiptOrder) rdresult.getEntityList().get(0);
                        String goodsreceiptorderno = goodsreceiptorder.getGoodsReceiptOrderNumber();
                        requestParamsLinking.put("linkeddocno", goodsreceiptorderno);
                        requestParamsLinking.put("sourceflag", 1);
                        result = accGoodsReceiptobj.savePRLinking(requestParamsLinking);
                        linkedDocuments +=goodsreceiptorderno+ " ,";
                    }
                }
                 linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
            } else if (linkMode.equalsIgnoreCase("Purchase Invoice")) {
                for (int i = 0; i < linkNumbers.length; i++) {
                    if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                        updateOpenStatusFlagInVIForPR(linkNumbers[i], companyid);
                        
                         /* Saving Linking information of Purchasereturn */
                        HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                        requestParamsLinking.put("linkeddocid", salesReturn.getID());
                        requestParamsLinking.put("docid", linkNumbers[i]);
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
                        requestParamsLinking.put("linkeddocno", entryNumber);
                        requestParamsLinking.put("sourceflag", 0);
                        KwlReturnObject result = accGoodsReceiptobj.saveVILinking(requestParamsLinking);//saving linking information in pi
                        
                        
                        requestParamsLinking.put("linkeddocid", linkNumbers[i]);
                        requestParamsLinking.put("docid", salesReturn.getID());
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), linkNumbers[i]);
                        GoodsReceipt goodsreceipt = (GoodsReceipt) rdresult.getEntityList().get(0);
                        String goodsreceiptno = goodsreceipt.getGoodsReceiptNumber();
                        requestParamsLinking.put("linkeddocno", goodsreceiptno);
                        requestParamsLinking.put("sourceflag", 1);
                        result = accGoodsReceiptobj.savePRLinking(requestParamsLinking);
                        linkedDocuments +=goodsreceiptno+ " ,";

                    }
                }
                linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
            } else if (linkMode.equalsIgnoreCase("Consignment Goods Receipt")) {
                for (int i = 0; i < linkNumbers.length; i++) {
                    if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                        updateOpenStatusFlagInGRForPR(linkNumbers[i], companyid,salesReturn.getID());
                    }
                }
            }
            String moduleName =Constants.moduleID_NameMap.get(Acc_Purchase_Return_ModuleId);
            if(isFixedAsset){
                moduleName = Constants.ASSET_PURCHASE_RETURN;
            }
            if(isConsignment){
                moduleName = Constants.moduleID_NameMap.get(Acc_ConsignmentPurchaseReturn_ModuleId);
            }
            
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(srid)) { 
                    if (isFixedAsset && documentEmailSettings.isAssetPurchaseReturnGenerationMail())  {
                        sendmail = true;
                    }else if (isConsignment && documentEmailSettings.isConsignmentPReturnGenerationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isPurchaseReturnGenerationMail()) {
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (isFixedAsset && documentEmailSettings.isAssetPurchaseReturnUpdationMail())  {
                        sendmail = true;
                    }else if (isConsignment && documentEmailSettings.isConsignmentPReturnUpdationMail()) {
                        sendmail = true;
                    } else if (documentEmailSettings.isPurchaseReturnUpdationMail()) {
                        sendmail = true;
                    }
                }
                if (sendmail) {           //if allow to send alert mail when option selected in companypreferences
                    String userMailId="",userName="",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> sendmailrequestParams= AccountingManager.getEmailNotificationParams(request);
                    if(sendmailrequestParams.containsKey("userfullName")&& sendmailrequestParams.get("userfullName")!=null){
                        userName=(String)sendmailrequestParams.get("userfullName");
                    }
                    if(sendmailrequestParams.containsKey("usermailid")&& sendmailrequestParams.get("usermailid")!=null){
                        userMailId=(String)sendmailrequestParams.get("usermailid");
                    }
                    if(sendmailrequestParams.containsKey(Constants.useridKey)&& sendmailrequestParams.get(Constants.useridKey)!=null){
                        currentUserid=(String)sendmailrequestParams.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    /*
                     if Edit mail option is true then get userid and Email id of document creator.
                     */
                    if (isEditMail) {
                        if (salesReturn != null && salesReturn.getCreatedby() != null) {
                            createdByEmail = salesReturn.getCreatedby().getEmailID();
                            createdById = salesReturn.getCreatedby().getUserID();
                        }
                        /*
                         if current user userid == document creator userid then don't add creator email ID in List.
                         */
                        if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                            mailIds.add(createdByEmail);
                        }
                    }             
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String prNumber = entryNumber;
                    
                    if((documentEmailSettings.isPurchaseReturnUpdationMail() || documentEmailSettings.isPurchaseReturnGenerationMail()) && isConsignment){
                        sendMailOnPurchaseReturnCreationUpdation(companyid, salesReturn, isEditMail, tomailids, prNumber);
                    }else{
                        accountingHandlerDAOobj.sendSaveTransactionEmails(prNumber, moduleName, tomailids, userName, isEditMail, companyid);
                    }
                }
            }

        } catch (ParseException ex) {
            throw ServiceException.FAILURE("savePurchaseReturn : " + ex.getMessage(), ex);
        }
        returnList.add(salesReturn);
        returnList.add(debitNoteNumber);
        returnList.add(debitNoteId);
        returnList.add(linkedDocuments);
        returnList.add(unlinkMessage);
        return returnList;
    }
    
    /**
     * Block is used to check the Goods receipts linked with PR are also linked with debit note or not,
     * 1)if above case is true then goods receipts are linked internally to DN,
     * a.if amount due of DN is zero then so PR with DN document will not be edited so exception is thrown for this
     * b.if amount due of DN is greater than zero then so PR with DN document can be edited
     * 2)if above case is false then then goods receipts are linked Externally to DN , so PR with DN document will not be edited so exception is thrown.
     * @param PRwithDNparams
     * @return 
    */
    public boolean isEditPRwithDNDocument(HashMap PRwithDNparams) {
        boolean isEditPRwithDN = true;
        List<DebitNoteDetail> notedetail = null;
        PurchaseReturnLinking GRlinkingwithPR=null;
        KwlReturnObject PRLObj=null;
        KwlReturnObject dnObj=null;
        if (PRwithDNparams.containsKey("PRLObj")) {
            PRLObj = (KwlReturnObject) PRwithDNparams.get("PRLObj");
        }
        if (PRwithDNparams.containsKey("dnObj")) {
            dnObj = (KwlReturnObject) PRwithDNparams.get("dnObj");
        }
        if (PRLObj != null && PRLObj.getEntityList() != null && !PRLObj.getEntityList().isEmpty()) {
            List GRlinkedWithPR = null;
            GRlinkedWithPR = PRLObj.getEntityList();
            Iterator GRitr = GRlinkedWithPR.iterator();
            while (GRitr.hasNext()) {
                GRlinkingwithPR = (PurchaseReturnLinking) GRitr.next();
                if (dnObj != null && dnObj.getEntityList() != null && !dnObj.getEntityList().isEmpty()) {
                    notedetail = dnObj.getEntityList();
                    for (DebitNoteDetail dnlist : notedetail) {
                        String goodsReceipt = dnlist.getGoodsReceipt().getID();
                        double DNamount = dnlist.getDebitNote().getDnamount();
                        double DNamountdue = dnlist.getDebitNote().getDnamountdue();
                            if (DNamount != DNamountdue) {
                            /**
                             * this block checks the GR are linked externally or
                             * internally with DN.
                             */
                            if (!GRlinkingwithPR.getLinkedDocID().equalsIgnoreCase(goodsReceipt)) {
                                isEditPRwithDN = false;
                                break;
                            }
                        }
                    }

                }
            }
        }
        return isEditPRwithDN;
    }
    
    /**
     * Block is used to check the invoices linked with PR are also linked with debit note or not,
     * 1)if above case is true then invoices are linked internally to DN,
     * a.if amount due of DN is zero then so PR with DN document will not be edited so exception is thrown for this
     * b.if amount due of DN is greater than zero then so PR with DN document can be edited
     * 2)if above case is false then then invoices are linked Externally to DN , so PR with DN document will not be edited so exception is thrown.
     * @param PRwithDNparams
     * @return 
    */
    public boolean isEditSRwithCNDocument(HashMap SRwithCNparams) {
        boolean isEditSRwithCN = true;
        List<CreditNoteDetail> notedetail = null;
        SalesReturnLinking SIlinkingwithSR=null;
        KwlReturnObject SRLObj=null;
        KwlReturnObject cnObj=null;
        if (SRwithCNparams.containsKey("SRLObj")) {
            SRLObj = (KwlReturnObject) SRwithCNparams.get("SRLObj");
        }
        if (SRwithCNparams.containsKey("cnObj")) {
            cnObj = (KwlReturnObject) SRwithCNparams.get("cnObj");
        }
        /**
         * traversing the list of invoices linked with SR
         */
        if (SRLObj != null && SRLObj.getEntityList() != null && !SRLObj.getEntityList().isEmpty()) {
            List SIlinkedWithSR = null;
            SIlinkedWithSR = SRLObj.getEntityList();
            Iterator SIitr = SIlinkedWithSR.iterator();
            while (SIitr.hasNext()) {
                SIlinkingwithSR = (SalesReturnLinking) SIitr.next();
                /**
                 * traversing the list of invoices linked with CN
                 */
                if (cnObj != null && cnObj.getEntityList() != null && !cnObj.getEntityList().isEmpty()) {
                    notedetail = cnObj.getEntityList();
                    for (CreditNoteDetail cnlist : notedetail) {
                        String invoice = cnlist.getInvoice().getID();
                        double CNamount = cnlist.getCreditNote().getCnamount();
                        double CNamountdue = cnlist.getCreditNote().getCnamountdue();
                            if (CNamount != CNamountdue) {
                            /**
                             * this block checks the invoice are linked externally or
                             * internally with DN.
                             */
                            if (!SIlinkingwithSR.getLinkedDocID().equalsIgnoreCase(invoice)) {
                                isEditSRwithCN = false;
                                break;
                            }
                        }
                    }

                }
            }
        }
        return isEditSRwithCN;
    }
    private void sendMailOnPurchaseReturnCreationUpdation(String companyId, PurchaseReturn pr, boolean isEdit, String[] toEmailIds, String PRNumber) throws ServiceException {
        String htmlTextC = "", subject = "";
        String PRCreatorName = pr.getCreatedby().getFullName();

        htmlTextC += "<br/>Hi,<br/>";
        if (!isEdit) {
            htmlTextC += "<br/>User <b>" + PRCreatorName + "</b> has created new Consignment Purchase Return  <b>" + PRNumber + "</b>.<br/>";
        } else {
            htmlTextC += "<br/>User <b>" + PRCreatorName + "</b> has edited Consignment Purchase Return <b>" + PRNumber + "</b>.<br/>";
        }

        KwlReturnObject result = accountingHandlerDAOobj.getNotifications(companyId);
        List<NotificationRules> list = result.getEntityList();
        for (NotificationRules nr:list) {
            if (nr != null && nr.getModuleId() == 202 && Integer.parseInt(nr.getFieldid()) == 34) {
                String toUserListStr = StringUtil.join(",", toEmailIds);
                
                if (nr.isMailToSalesPerson()) {
                    Set<VendorAgentMapping> agentList=pr.getVendor().getAgent();
                    Iterator agntItr=agentList.iterator();
                    while(agntItr.hasNext()){
                        VendorAgentMapping vam=(VendorAgentMapping)agntItr.next();
                        if (vam != null && vam.getAgent() != null) {
                            if (!StringUtil.isNullOrEmpty(vam.getAgent().getEmailID())) {
                                if (StringUtil.isNullOrEmpty(toUserListStr)) {
                                    toUserListStr += vam.getAgent().getEmailID();
                                } else {
                                    toUserListStr += "," + vam.getAgent().getEmailID();
                                }
                            }
                        }
                    }
                }

                toEmailIds = toUserListStr.split(",");

                subject = nr.getMailsubject();
                htmlTextC = nr.getMailcontent();

                subject = subject.replaceAll("#Vendor_Alias#", pr.getVendor().getAliasname());
//                    subject = subject.replaceAll("#Sales_Person#", deliveryOrder.getSalesperson().getValue());
                subject = subject.replaceAll("#Document_Number#", PRNumber);
                htmlTextC = htmlTextC.replaceAll("#Document_Number#", PRNumber);
                htmlTextC = htmlTextC.replaceAll("#User_Name#", PRCreatorName);

                if (isEdit) {
                    subject = subject.replaceAll("Creation", "updation");
                    subject = subject.replaceAll("generation", "updation");
                    htmlTextC = htmlTextC.replaceAll("added", "updated");
                    htmlTextC = htmlTextC.replaceAll("created", "updated");
                }
                break;
            }
        }
        accountingHandlerDAOobj.sendTransactionEmails(toEmailIds, "", subject, htmlTextC, htmlTextC, companyId);
    }
    
    @Override
    public boolean updatePurchasereturnAmount(PurchaseReturn purchaserturn, JSONObject json) throws ServiceException {
        boolean success = true;
        try {
            String companyid = json.optString("companyid");
            if (purchaserturn != null) {
                if (json.has("totalamountinbase")) { // Purchase Return amount in base currency
                    purchaserturn.setTotalamountinbase(authHandler.round(Double.valueOf(json.get("totalamountinbase").toString()), companyid));
                }
                if (json.has("totalamount")) { // Total amount  in document currency
                    purchaserturn.setTotalamount(authHandler.round(Double.valueOf(json.get("totalamount").toString()), companyid));
                }
                if (json.has("totallineleveldiscount")) {  // discount amount in document currency
                    purchaserturn.setTotallineleveldiscount(authHandler.round(Double.valueOf(json.get("totallineleveldiscount").toString()), companyid));
                }
                if (json.has("discountinbase")) { // discount amount in base currency
                    purchaserturn.setDiscountinbase(authHandler.round(Double.valueOf(json.get("discountinbase").toString()), companyid));
                }
                saveOrUpdate(purchaserturn);
            }

        } catch (Exception ex) {
            success = false;
            System.out.println("AccSalesReturnServiceImpl:updatePurchasereturnAmount " + ex.getMessage());
        }
        return success;
    }
    
     @Override
    public boolean updateSalesreturnAmount(SalesReturn salesrturn, JSONObject json) throws ServiceException {
        boolean success = true;
        try {
            String companyid = json.optString("companyid");
            if (salesrturn != null) {
                if (json.has("totalamountinbase")) { // Sales Return amount in base currency
                    salesrturn.setTotalamountinbase(authHandler.round(Double.valueOf(json.get("totalamountinbase").toString()), companyid));
                }
                if (json.has("totalamount")) { // Total amount  in document currency
                    salesrturn.setTotalamount(authHandler.round(Double.valueOf(json.get("totalamount").toString()), companyid));
                }
                if (json.has("totallineleveldiscount")) {  // discount amount in document currency
                    salesrturn.setTotallineleveldiscount(authHandler.round(Double.valueOf(json.get("totallineleveldiscount").toString()), companyid));
                }
                if (json.has("discountinbase")) { // discount amount in base currency
                    salesrturn.setDiscountinbase(authHandler.round(Double.valueOf(json.get("discountinbase").toString()), companyid));
                }
                saveOrUpdate(salesrturn);
            }

        } catch (Exception ex) {
            success = false;
            System.out.println("AccSalesReturnServiceImpl:updatePurchasereturnAmount " + ex.getMessage());
        }
        return success;
    }

    public List savePurchaseReturnRows(HttpServletRequest request, PurchaseReturn purchaseReturn, String companyid, JournalEntry je,Set<JournalEntryDetail> inventoryEntryDetails, String invJEId) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        Set rows = new HashSet();
        HashSet cndetails = new HashSet();
        HashSet jedetails = new HashSet();
        HashSet dnTaxEntryDetails = new HashSet();
        double totalDiscountAmt = 0;
        double totalDNAmt = 0;
        double totalDNAmtExludingTax = 0;
        try {

            boolean isNoteAlso = false;
            GoodsReceiptDetail id = null;
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) comp.getEntityList().get(0);
            int countryid = Integer.parseInt(company.getCountry().getID());
            boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isConsignment))) ? Boolean.parseBoolean(request.getParameter(Constants.isConsignment)) : false;
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isFixedAsset))) ? Boolean.parseBoolean(request.getParameter(Constants.isFixedAsset)) : false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteAlso"))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
                isNoteAlso = Boolean.parseBoolean(request.getParameter("isNoteAlso"));
            }

            String globalTaxID = request.getParameter("taxid");
            double globalTaxPercent = 0;
            if (!StringUtil.isNullOrEmpty(globalTaxID)) {
                globalTaxPercent = StringUtil.isNullOrEmpty(request.getParameter("globalTaxPercent")) ? 0 : Double.parseDouble(request.getParameter("globalTaxPercent"));
            }
            JSONArray jArr = new JSONArray(request.getParameter(Constants.detail));
//
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            Map<String, List<TransactionBatch>> priceValuationMap = new HashMap<>();
            List<StockMovement> stockMovementsList=new ArrayList<StockMovement>();
            String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> dodDataMap = new HashMap<String, Object>();
                
                if(jobj.has("srno")) {
                    dodDataMap.put("srno", jobj.getInt("srno"));
                }
                
                dodDataMap.put(Constants.companyKey, companyid);
                dodDataMap.put("srid", purchaseReturn.getID());
                dodDataMap.put(Constants.productid, jobj.getString(Constants.productid));
                
                if (jobj.has("priceSource") && jobj.get("priceSource") != null) {
                    dodDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource")) ?  StringUtil.DecodeText(jobj.optString("priceSource")) : "");
                }
                if (jobj.has("pricingbandmasterid") && jobj.get("pricingbandmasterid") != null) {
                    dodDataMap.put("pricingbandmasterid", !StringUtil.isNullOrEmpty(jobj.optString("pricingbandmasterid")) ?  StringUtil.DecodeText(jobj.optString("pricingbandmasterid")) : "");
                }
                
                String linkMode = request.getParameter("fromLinkCombo");

                dodDataMap.put("description", jobj.getString("description"));
                dodDataMap.put("partno", jobj.getString("partno"));

                double actquantity = authHandler.roundQuantity((jobj.getDouble("quantity")), companyid);
                double dquantity = authHandler.roundQuantity((jobj.getDouble("dquantity")), companyid);
                double baseuomrate = 1;
                if (jobj.has("baseuomrate") && jobj.get("baseuomrate") != null) {
                    baseuomrate = jobj.getDouble("baseuomrate");
                }
                dodDataMap.put("quantity", actquantity);
                dodDataMap.put("returnquantity", dquantity);
                dodDataMap.put("baseuomrate", baseuomrate);
                if (jobj.has("uomid")) {
                    dodDataMap.put("uomid", jobj.getString("uomid"));
                }
                dodDataMap.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(actquantity, baseuomrate, companyid));
                dodDataMap.put("baseuomreturnquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate, companyid));

                double receivedQty = dquantity*baseuomrate;

                dodDataMap.put("remark", jobj.optString("remark"));
                dodDataMap.put("reason", jobj.optString("reason"));

                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString("prtaxid", null)) && jobj.optString("prtaxid", null).equalsIgnoreCase("None")) {
                    rowtaxid = null;
                } else {
                    rowtaxid = jobj.optString("prtaxid", null);
                }
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    double rowtaxamountFromJS = StringUtil.getDouble(jobj.getString("taxamount"));

                    if (rowtax == null) {
                        throw new AccountingException("The Tax code(s) used in this transaction has been deleted.");//messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        dodDataMap.put("taxamount", rowtaxamountFromJS);
                    }
                }
                dodDataMap.put(Constants.isUserModifiedTaxAmount, jobj.optBoolean(Constants.isUserModifiedTaxAmount, false));
                dodDataMap.put("prtaxid", rowtaxid);//    ERP-28612
                int discountispercent = jobj.optInt("discountispercent", 1);
                
                double prdiscount = jobj.optDouble("prdiscount", 0);
                
                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    dodDataMap.put("discount", prdiscount);
                }
                if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    dodDataMap.put("discountispercent", discountispercent);
                }

                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore"))) {
                    dodDataMap.put("invstoreid", jobj.optString("invstore"));
                } else {
                    dodDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation"))) {
                    dodDataMap.put("invlocationid", jobj.optString("invlocation"));
                } else {
                    dodDataMap.put("invlocationid", "");
                }
                
                if (jobj.has("rateIncludingGst")) {
                    dodDataMap.put("rateIncludingGst", jobj.optDouble("rateIncludingGst",0));
                }

                if (!StringUtil.isNullOrEmpty(linkMode)) {
                    if (linkMode.equalsIgnoreCase(Constants.Goods_Receipt) || linkMode.equalsIgnoreCase("Consignment Goods Receipt") || linkMode.equalsIgnoreCase("Asset Goods Receipt")) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), jobj.getString("rowid"));
                        GoodsReceiptOrderDetails sod = (GoodsReceiptOrderDetails) rdresult.getEntityList().get(0);
                        dodDataMap.put("GoodReceiptDetail", sod);
                    } else if (linkMode.equalsIgnoreCase("Vendor Invoice") || linkMode.equalsIgnoreCase("Purchase Invoice")) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptDetail.class.getName(), jobj.getString("rowid"));
                        id = (GoodsReceiptDetail) rdresult.getEntityList().get(0);
                        dodDataMap.put("InvoiceDetail", id);
                    }
                }

                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put(Constants.productid, jobj.getString(Constants.productid));
                inventoryjson.put("description", jobj.getString("description"));
                inventoryjson.put("quantity", dquantity);
                if (jobj.has("uomid")) {
                    inventoryjson.put("uomid", jobj.getString("uomid"));
                }
                if (!isConsignment) {
                inventoryjson.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(dquantity,baseuomrate, companyid));
                }
                inventoryjson.put("baseuomrate", baseuomrate);
                inventoryjson.put("carryin", false);
                inventoryjson.put("defective", false);
                inventoryjson.put("newinventory", false);
                inventoryjson.put(Constants.companyKey, companyid);
                inventoryjson.put("updatedate", authHandler.getDateOnlyFormat(request).parse(request.getParameter(Constants.BillDate)));
                if (isConsignment) {
                    inventoryjson.put("venconsignuomquantity",-(authHandler.calculateBaseUOMQuatity(dquantity,baseuomrate, companyid)));
                    inventoryjson.put(Constants.isConsignment, isConsignment);
                }
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);

                dodDataMap.put("Inventory", inventory);
                double unitPrice = 0;
                if (jobj.has("rate")) {
                    dodDataMap.put("rate", jobj.optDouble("rate",0));
                    unitPrice = jobj.optDouble("rate",0);
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("recTermAmount"))) {
                    dodDataMap.put("recTermAmount", jobj.optString("recTermAmount"));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("OtherTermNonTaxableAmount"))) {
                    dodDataMap.put("OtherTermNonTaxableAmount", jobj.optString("OtherTermNonTaxableAmount"));
                }
                // Purchase Return Rows
                if (extraCompanyPreferences != null && !StringUtil.isNullOrEmpty(invJEId) && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString(Constants.productid));
                    Product product = (Product) txresult.getEntityList().get(0);
                    if (product != null && !(product.getProducttype().getID().equals(Producttype.SERVICE) || product.getProducttype().getID().equals(Producttype.NON_INVENTORY_PART) || product.getProducttype().getID().equals(Producttype.Inventory_Non_Sales))) {
                    if (product != null && product.getInventoryAccount() != null && product.getPurchaseAccount() != null && jobj.has("rate")) {
                        HashMap<String, Object> requestMap = new HashMap<>();
                        requestMap.put(Constants.productid, jobj.getString(Constants.productid));
                        requestMap.put("productId", jobj.getString(Constants.productid));
                        requestMap.put(Constants.companyKey, companyid);
                        requestMap.put(Constants.df, authHandler.getDateOnlyFormat(request));
                        requestMap.put(Constants.globalCurrencyKey, gcurrencyid);
                        requestMap.put("GlobalParams", GlobalParams);
                        requestMap.put(Constants.REQ_enddate, request.getParameter(Constants.BillDate));
                        requestMap.put("dquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate, companyid));
                        Map<String, Double> batchQuantityMap = new HashMap<>();
                        if (jobj.has("batchdetails") && jobj.get("batchdetails") != null && !StringUtil.isNullOrEmpty(jobj.get("batchdetails").toString())) {
                            String batchSerialIds = accInvoiceModuleService.getBatchSerialIDs(jobj.getString("batchdetails"), product, batchQuantityMap);
                            if (!StringUtil.isNullOrEmpty(batchSerialIds)) {
                                requestMap.put("batchSerialId", batchSerialIds.split(","));
                            }
                        }
                        // VALUATION- GIVES YOU AMOUNT WITH WHICH PR IS DONE
//                        double valuation = accInvoiceModuleService.getValuationForDOAndSR(requestMap, priceValuationMap, batchQuantityMap);
                        double valuation=0;                        
                        // Inventory Account
                        JSONObject jedjson = new JSONObject();
                        jedjson.put(GoodsReceiptConstants.SRNO, jedetails.size() + 1);
                        jedjson.put(GoodsReceiptConstants.COMPANYID, companyid);
                        jedjson.put(GoodsReceiptConstants.AMOUNT, valuation);
                        jedjson.put(GoodsReceiptConstants.ACCOUNTID, product.getInventoryAccount() != null ? product.getInventoryAccount().getID() : "");
                        jedjson.put(GoodsReceiptConstants.DEBIT, false);
                        jedjson.put(GoodsReceiptConstants.JEID, invJEId);
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        dodDataMap.put("inventoryjedetailid", jed.getID());
                        inventoryEntryDetails.add(jed);
                        // Accrued Purchase Account
                        jedjson = new JSONObject();
                        jedjson.put(GoodsReceiptConstants.SRNO, jedetails.size() + 1);
                        jedjson.put(GoodsReceiptConstants.COMPANYID, companyid);
                        jedjson.put(GoodsReceiptConstants.AMOUNT, valuation);
                        jedjson.put(GoodsReceiptConstants.ACCOUNTID, product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : "");
                        jedjson.put(GoodsReceiptConstants.DEBIT, true);
                        jedjson.put(GoodsReceiptConstants.JEID, invJEId);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        dodDataMap.put("purchasesjedetailid", jed.getID());
                        inventoryEntryDetails.add(jed);
                    }
                }
                }
                // Save TDS related details
                if (!StringUtil.isNullOrEmpty(jobj.optString("appliedTDS")) && company.getCountry() != null && !StringUtil.isNullOrEmpty(company.getCountry().getID()) && Constants.indian_country_id == Integer.parseInt(company.getCountry().getID())) {// only for indian country
                    JSONArray jArrAppliedTDS = new JSONArray(jobj.optString("appliedTDS"));
                    if (jArrAppliedTDS.length() > 0) {
                        JSONObject jobjAppliedTDS = jArrAppliedTDS.getJSONObject(0);
                        KwlReturnObject assObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jobjAppliedTDS.getString("natureofpayment"));
                        KwlReturnObject tdsAccresult = accountingHandlerDAOobj.getObject(Account.class.getName(), jobjAppliedTDS.getString("tdsaccountid"));
                        MasterItem nop = (MasterItem) assObj.getEntityList().get(0);
                        Account tdsPayableAccount = (Account) tdsAccresult.getEntityList().get(0);
                        dodDataMap.put("tdsAssessableAmount",jobjAppliedTDS.getDouble("tdsAssessableAmount"));
                        dodDataMap.put("natureofpayment",nop);
                        dodDataMap.put("tdsruleid",jobjAppliedTDS.getInt("ruleid"));
                        dodDataMap.put("tdspercentage",jobjAppliedTDS.getDouble("tdspercentage"));
                        dodDataMap.put("tdsamount",jobjAppliedTDS.getDouble("tdsamount"));
                        dodDataMap.put("tdspayableaccount",tdsPayableAccount);
                    }
                }
                KwlReturnObject result = accGoodsReceiptobj.savePurchaseReturnDetails(dodDataMap);
                PurchaseReturnDetail row = (PurchaseReturnDetail) result.getEntityList().get(0);

                
                // Create Debit Nore
                if (isNoteAlso) {
                    String purchase_accid = "";
                    KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString(Constants.productid));
                    Product product = (Product) proresult.getEntityList().get(0);
                    
                    double discountVal = 0d;
                    double totalAmt = unitPrice * receivedQty;
                    if (discountispercent == 1) {
                        discountVal = totalAmt * prdiscount / 100;
                    } else {
                        discountVal = prdiscount;
                    }
                    totalDiscountAmt += discountVal;
                    
                    double rowtaxamount = 0d;
                    double amountExcludingTax = 0;

                    /*
                     * Here handling three cases #1 tax given at line level #2
                     * tax given at global level #3 No tax is given. When tax
                     * given at line level amount comes with tax When tax given
                     * at global level amount comes without tax
                     */
                    Tax rowtax = null;
                    double dnRowAmount=jobj.optDouble("amount", 0);
                    
                    if(!StringUtil.isNullOrEmpty(globalTaxID)){//when tax given at global Level
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), globalTaxID);
                        rowtax = (Tax) txresult.getEntityList().get(0);
                        amountExcludingTax = dnRowAmount;// it comes excluding tax
                        rowtaxamount= authHandler.round(dnRowAmount * (globalTaxPercent/100), companyid);
                        totalDNAmt += amountExcludingTax+rowtaxamount;
                        totalDNAmtExludingTax += amountExcludingTax; 
                    } else if(!StringUtil.isNullOrEmpty(rowtaxid)) {
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid);
                        rowtax = (Tax) txresult.getEntityList().get(0);
                        totalDNAmt += dnRowAmount;
                        rowtaxamount = jobj.optDouble("taxamount", 0);
                        amountExcludingTax = dnRowAmount - rowtaxamount;
                        totalDNAmtExludingTax += amountExcludingTax;
                    } else {// No tax applied
                        totalDNAmt += dnRowAmount;
                        rowtaxamount = 0;
                        amountExcludingTax = dnRowAmount ;
                        totalDNAmtExludingTax += dnRowAmount;
                    }

                    String DebitNoteDetailID = StringUtil.generateUUID();
                    DebitNoteDetail debitNoteDetailRow = new DebitNoteDetail();
                    debitNoteDetailRow.setSrno(i + 1);
                    debitNoteDetailRow.setID(DebitNoteDetailID);
                    debitNoteDetailRow.setTotalDiscount(0.00);
                    debitNoteDetailRow.setCompany(company);
                    debitNoteDetailRow.setMemo("");
//                    if(id != null && countryid == Constants.indian_country_id && id.getPurchaseJED() != null){//id is Goodsreceiptdetailid
//                        purchase_accid = id.getPurchaseJED().getAccount().getID();//account used in goodsreceiptdetail
//                    }else{
                        purchase_accid = product.getPurchaseReturnAccount().getID();
//                    }
                    
                    DebitNoteTaxEntry taxEntry = new DebitNoteTaxEntry();
                    String DebitNoteTaxID = StringUtil.generateUUID();
                    taxEntry.setID(DebitNoteTaxID);

                    if (i == 0) {// create  dndetail entry only once in this case i.e if multitple accounts are linked.
                        cndetails.add(debitNoteDetailRow);
                    }

                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put(Constants.companyKey, company.getCompanyID());
                    jedjson.put("amount", amountExcludingTax+discountVal);
                    jedjson.put("accountid", purchase_accid);
                    jedjson.put("debit", false);
                    jedjson.put("jeid", je.getID());
                    jedjson.put("description", "");
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    JournalEntryDetail jedTotal = (JournalEntryDetail) jedresult.getEntityList().get(0);//ERP-17888                    
                    jedetails.add(jed);
                    
                    String rowTaxJeId = "";
                    if (rowtax != null) {
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put(Constants.companyKey, company.getCompanyID());
                        jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                        jedjson.put("accountid", rowtax.getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", je.getID());
                        jedjson.put("description", "");
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);

                        rowTaxJeId = jed.getID();
                    }
                     // Row TAX JE POST When Sales Return with CN - it is only for Indian Company. 
                    if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                        JSONArray termsArray = new JSONArray( StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                        for (int j = 0; j < termsArray.length(); j++) {
                            JSONObject termObject = termsArray.getJSONObject(j);
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + j + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("amount", termObject.get("termamount"));
                            jedjson.put("accountid", termObject.get("accountid"));
                            jedjson.put("debit",false);
                            jedjson.put("jeid", je.getID());
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                            totalDNAmt+=termObject.getDouble("termamount");
                            rowtaxamount+=termObject.getDouble("termamount");
                        }
                    } 
                    if (extraCompanyPreferences.getLineLevelTermFlag()==1) {
                        /**
                         * Save GST History Customer/Vendor data.
                         */
                        jobj.put("detaildocid", row.getID());
                        jobj.put("moduleid", Constants.Acc_Purchase_Return_ModuleId);
                        fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                    }
                    // save custom field details for auto created case
                    if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                        jcustomarray = fieldDataManagercntrl.getComboValueIdsForCurrentModule(jcustomarray, Constants.Acc_Debit_Note_ModuleId, companyid, 1);            // 1= for line item
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", jed.getID());
                        customrequestParams.put("recdetailId", taxEntry.getID());
                        customrequestParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                        customrequestParams.put(Constants.companyKey, company.getCompanyID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            JSONObject tempjedjson = new JSONObject();
                            tempjedjson.put("accjedetailcustomdata", jed.getID());
                            tempjedjson.put("jedid", jed.getID());
                            jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                        }
                    }
                    KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), purchase_accid);
                    Account account = (Account) accountresult.getEntityList().get(0);
                    
                    double dnAmount=0d;
                    if (purchaseReturn.isGstIncluded()) {
                        dnAmount = amountExcludingTax + rowtaxamount;
                    } else {
                        dnAmount = amountExcludingTax;
                    }

                    taxEntry.setAccount(account);
                    taxEntry.setAmount(dnAmount);
                    taxEntry.setCompany(company);
                    if (!StringUtil.isNullOrEmpty(jobj.optString("reason", ""))) {
                        KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                        MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
                        taxEntry.setReason(reason);
                    }
                    taxEntry.setDescription("");
                    taxEntry.setIsForDetailsAccount(true);
                    taxEntry.setDebitForMultiCNDN(false);
                    taxEntry.setTax(rowtax);
                    taxEntry.setTaxJedId(rowTaxJeId);
                    taxEntry.setTaxamount(rowtaxamount);      
                    taxEntry.setRateIncludingGst(amountExcludingTax);      
                    taxEntry.setTotalJED(jedTotal); //ERP-17888
                    dnTaxEntryDetails.add(taxEntry);
                }

                if (jobj.has("batchdetails") && jobj.getString("batchdetails") != null) {
                    String batchDetails = jobj.getString("batchdetails");
                    if (!StringUtil.isNullOrEmpty(batchDetails)) {
                        savePRNewBatch(batchDetails, inventory,request,row,stockMovementsList);
                    }
                }
                
                if (isFixedAsset) {
                    boolean isFromPurchaseReturn = true;
                    /*Get request parameters */
                    JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                    Set<AssetDetails> assetDetailsSet = accInvoiceModuleService.saveAssetDetails(paramJobj, jobj.getString(Constants.productid), jobj.getString("assetDetails"), 0, false, false, false, false, isFixedAsset, isFromPurchaseReturn, 0);
                    Set<AssetInvoiceDetailMapping> assetInvoiceDetailMappings = accInvoiceModuleService.saveAssetInvoiceDetailMapping(row.getID(), assetDetailsSet, companyid, Constants.Acc_FixedAssets_Purchase_Return_ModuleId);
                }

                String customfield = jobj.getString(Constants.customfield);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> PRMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "PurchaseReturnDetail");
                    customrequestParams.put("moduleprimarykey", "PurchaseReturnDetailId");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    PRMap.put(Constants.Acc_id, row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_PurchaseReturnDetailsCustomDate_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        PRMap.put("gRDetailscustomdataref", row.getID());
                        accGoodsReceiptobj.updatePRDetailsCustomData(PRMap);
                    }
                }

                 // Add Custom fields details for Product
                if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                    JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                    HashMap<String, Object> prMap = new HashMap<String, Object>();
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "PrProductCustomData");
                    customrequestParams.put("moduleprimarykey", "PrDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    prMap.put(Constants.Acc_id, row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_PRDetail_Productcustom_data_classpath);
                    /*
                     * Rich Text Area is put in json if User have not selected any data for this field. ERP-ERP-37624
                     */
                    customrequestParams.put("productIdForRichRext", row.getProduct().getID());                    
                    fieldDataManagercntrl.setRichTextAreaForProduct(customrequestParams);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        prMap.put("prdetailscustomdataref", row.getID());
                        accGoodsReceiptobj.updatePRDetailsProductCustomData(prMap);
                    }
                }
                
                if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray( StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> PurchaseReturnDetailsTermsMap = new HashMap<String, Object>();
                        JSONObject termObject = termsArray.getJSONObject(j);
             
                        if (termObject.has("termid")) {
                            PurchaseReturnDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            PurchaseReturnDetailsTermsMap.put("termamount", termObject.get("termamount"));
                        }
                        if (termObject.has("termpercentage")) {
                            PurchaseReturnDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("assessablevalue")) {
                            PurchaseReturnDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            PurchaseReturnDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            PurchaseReturnDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                            PurchaseReturnDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                if(termObject.getInt("taxtype")==0){ // If Flat
                                    PurchaseReturnDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    PurchaseReturnDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        PurchaseReturnDetailsTermsMap.put("PurchaseReturnDetailID", row.getID());
                        /**
                         * ERP-32829 
                         */
                        PurchaseReturnDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                        PurchaseReturnDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                        PurchaseReturnDetailsTermsMap.put("product", jobj.get(Constants.productid));
                        PurchaseReturnDetailsTermsMap.put("userid", sessionHandlerImpl.getUserid(request));
                        accGoodsReceiptobj.savePurchaseReturnDetailsTermMap(PurchaseReturnDetailsTermsMap);
                    }
                }
                
                rows.add(row);
                
            }
            returnList.add(rows);
            returnList.add(cndetails);
            returnList.add(jedetails);
            returnList.add(dnTaxEntryDetails);
            returnList.add(totalDNAmt);
            returnList.add(totalDNAmtExludingTax);
            returnList.add(totalDiscountAmt);
            if(extraCompanyPreferences!=null && extraCompanyPreferences.isActivateInventoryTab()&& !stockMovementsList.isEmpty()){
                    stockMovementService.addOrUpdateBulkStockMovement(purchaseReturn.getCompany(), purchaseReturn.getID(), stockMovementsList);
            }
        } catch (InventoryException ex) {
            throw ServiceException.FAILURE("savePurchaseReturnRows : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("savePurchaseReturnRows : " + ex.getMessage(), ex);
        }
        return returnList;
    }


    public void savePRNewBatch(String batchJSON, Inventory inventory, HttpServletRequest request, PurchaseReturnDetail purchaseReturnDetail, List<StockMovement> stockMovementsList) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);
        KwlReturnObject kmsg = null;
        double ActbatchQty = 1;
        double batchQty = 0;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        boolean isnegativestockforlocwar = false;
        int serialsequence = 1 , batchsequence = 1; // for user selected sequence of batch and serial while creating PR.
        boolean isConsignment = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isConsignment))) ? Boolean.parseBoolean(request.getParameter(Constants.isConsignment)) : false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        ExtraCompanyPreferences extraCompanyPreferences = null;
        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
        extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        isnegativestockforlocwar = extraCompanyPreferences.isIsnegativestockforlocwar();
        
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        
        if (!StringUtil.isNullOrEmpty(inventory.getProduct().getID())) {
            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), inventory.getProduct().getID());
            Product product = (Product) prodresult.getEntityList().get(0);
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
              isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
        }

        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        StockMovementDetail smd=null;
        StockMovement stockMovement=null;
        NewProductBatch productBatch = null;
        String productBatchId = "";
        String entryNumber = request.getParameter("number");
        Map<Store, StockMovement> storeWiseStockMovement = new HashMap<Store, StockMovement>();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
           if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined") && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
                
                KwlReturnObject warehouseObj = accountingHandlerDAOobj.getObject(Store.class.getName(),jSONObject.getString("warehouse"));
                Store store = (Store) warehouseObj.getEntityList().get(0);            
                if (isWarehouseForProduct && isLocationForProduct) {
                    if (storeWiseStockMovement.containsKey(store)) {
                        stockMovement = storeWiseStockMovement.get(store);
                        stockMovement.setQuantity(stockMovement.getQuantity() + jSONObject.optDouble("quantity", 0.0));
                    } else {
                        stockMovement = new StockMovement();
                        if (store != null) {
                            stockMovement.setStore(store);
                        }
                        stockMovement.setCompany(inventory.getCompany());
                        stockMovement.setProduct(inventory.getProduct());
                        stockMovement.setStockUoM(inventory.getProduct().getUnitOfMeasure());
                        stockMovement.setPricePerUnit(purchaseReturnDetail.getBaseuomrate() < 1 ? (purchaseReturnDetail.getRate() * (1/purchaseReturnDetail.getBaseuomrate())) : purchaseReturnDetail.getRate() / purchaseReturnDetail.getBaseuomrate());
                        stockMovement.setQuantity(jSONObject.optDouble("quantity", 0.0));
                        stockMovement.setTransactionDate(purchaseReturnDetail.getPurchaseReturn().getOrderDate());
                        stockMovement.setModuleRefId(purchaseReturnDetail.getPurchaseReturn().getID());
                        stockMovement.setModuleRefDetailId(purchaseReturnDetail.getID());
                        stockMovement.setVendor(purchaseReturnDetail.getPurchaseReturn().getVendor());
                        stockMovement.setCostCenter(purchaseReturnDetail.getPurchaseReturn().getCostcenter());
                        stockMovement.setTransactionNo(StringUtil.isNullOrEmpty(purchaseReturnDetail.getPurchaseReturn().getPurchaseReturnNumber())?entryNumber:purchaseReturnDetail.getPurchaseReturn().getPurchaseReturnNumber());
                        stockMovement.setTransactionModule(TransactionModule.ERP_PURCHASE_RETURN);
                        stockMovement.setTransactionType(TransactionType.OUT);
                        storeWiseStockMovement.put(store, stockMovement);
                    }
                }
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct  || isBinForProduct) && (batchQty == ActbatchQty)) {
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.getString("quantity"));
                documentMap.put("batchmapid", jSONObject.getString("purchasebatchid"));
                documentMap.put("documentid", purchaseReturnDetail.getID());
                if (isConsignment) {
                    /**
                     * Cast moduleID to String for saving into locationbatchdocumentmapping. 
                     */
                    documentMap.put("transactiontype", ""+Constants.Acc_ConsignmentPurchaseReturn_ModuleId);
                } else {
                    documentMap.put("transactiontype", ""+Constants.Acc_Purchase_Return_ModuleId);//This is PR Type Tranction  
                }
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expdate")));
                }
                documentMap.put("purchasereturn", "true");
                 
                KwlReturnObject locationUpdate = accountingHandlerDAOobj.getObject(Location.class.getName(), jSONObject.getString("location"));
                Location locationObj = (Location) locationUpdate.getEntityList().get(0);
                if (isWarehouseForProduct && isLocationForProduct) {
                    smd = new StockMovementDetail();
                    if (locationObj != null) {
                        smd.setLocation(locationObj);
                    }
                    if(isRowForProduct){
                        KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("row"));
                        StoreMaster row = (StoreMaster) krObject.getEntityList().get(0);
                        smd.setRow(row);
                    }
                    if(isRackForProduct){
                        KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("rack"));
                        StoreMaster rack = (StoreMaster) krObject.getEntityList().get(0);
                        smd.setRack(rack);
                    }
                    if(isBinForProduct){
                        KwlReturnObject krObject = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), jSONObject.optString("bin"));
                        StoreMaster bin = (StoreMaster) krObject.getEntityList().get(0);
                        smd.setBin(bin);
                    }
                    smd.setQuantity(Double.parseDouble(jSONObject.getString("quantity")));
                    smd.setBatchName("");
                    smd.setStockMovement(stockMovement);
                    stockMovement.getStockMovementDetails().add(smd);
                }
                if (!isBatchForProduct && !isSerialForProduct) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    filter_names.add("company.companyID");
                    filter_params.add(sessionHandlerImpl.getCompanyid(request));

                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("warehouse"))) {
                        String warehouse = jSONObject.getString("warehouse");
                        filter_names.add("warehouse.id");
                        filter_params.add(warehouse);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("location"))) {
                        String location = jSONObject.getString("location");
                        filter_names.add("location.id");
                        filter_params.add(location);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.optString("row", ""))) {
                        String row = jSONObject.getString("row");
                        filter_names.add("row.id");
                        filter_params.add(row);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.optString("rack", ""))) {
                        String rack = jSONObject.getString("rack");
                        filter_names.add("rack.id");
                        filter_params.add(rack);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.optString("bin", ""))) {
                        String bin = jSONObject.getString("bin");
                        filter_names.add("bin.id");
                        filter_params.add(bin);
                    }

                    filter_names.add("product");
                    filter_params.add(inventory.getProduct().getID());
                    
                    filterRequestParams.put(Constants.filterNamesKey, filter_names);
                    filterRequestParams.put(Constants.filterParamsKey, filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams,false,false);
                    List listResult = result.getEntityList();
                     double bcount=0;
                    if ((isnegativestockforlocwar && !(isBatchForProduct || isSerialForProduct)) && listResult.isEmpty()) { //item level check for Negatice stock
                        HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                        pdfTemplateMap.put(Constants.companyKey, inventory.getCompany().getCompanyID());
                        pdfTemplateMap.put("name",  StringUtil.DecodeText(jSONObject.optString("batch")));
                        if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                            pdfTemplateMap.put("mfgdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("mfgdate")));
                        }
                        if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                            pdfTemplateMap.put("expdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expdate")));
                        }
                        String batchQuantity = jSONObject.optString("quantity");
                        if (!StringUtil.isNullOrEmpty(batchQuantity)) {
                            bcount = Double.parseDouble(batchQuantity);
                        }
                        if (jSONObject.has("quantity") && !StringUtil.isNullOrEmpty(jSONObject.getString("quantity"))) {
                            pdfTemplateMap.put("quantity", String.valueOf(-(bcount)));
                        }
                        if (jSONObject.has("balance") && !StringUtil.isNullOrEmpty(jSONObject.getString("balance"))) {
                            pdfTemplateMap.put("balance", jSONObject.getString("balance"));
                        }
                        if (jSONObject.has("location") && !StringUtil.isNullOrEmpty(jSONObject.getString("location"))) {
                            pdfTemplateMap.put("location", jSONObject.getString("location"));
                        }
                        pdfTemplateMap.put("product", inventory.getProduct().getID());
                        if (jSONObject.has("warehouse") && !StringUtil.isNullOrEmpty(jSONObject.getString("warehouse"))) {
                            pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
                        }
                        if (jSONObject.has("row") && !StringUtil.isNullOrEmpty(jSONObject.getString("row"))) {
                            pdfTemplateMap.put("row", jSONObject.getString("row"));
                        }
                        if (jSONObject.has("rack") && !StringUtil.isNullOrEmpty(jSONObject.getString("rack"))) {
                            pdfTemplateMap.put("rack", jSONObject.getString("rack"));
                        }
                        if (jSONObject.has("bin") && !StringUtil.isNullOrEmpty(jSONObject.getString("bin"))) {
                            pdfTemplateMap.put("bin", jSONObject.getString("bin"));
                        }

                        pdfTemplateMap.put("isopening", false);
                        pdfTemplateMap.put("transactiontype", "28");//This is GRN Type Tranction  
                        pdfTemplateMap.put("ispurchase", true);
                        kmsg = accCommonTablesDAO.saveNewBatchForProduct(pdfTemplateMap);

                        if (kmsg != null && kmsg.getEntityList().size() != 0) {
                            productBatch = (NewProductBatch) kmsg.getEntityList().get(0);
                            productBatchId = productBatch.getId();
                        }
                        documentMap.put("batchmapid", productBatchId);
                    } else {
                    Iterator itrResult = listResult.iterator();
                    Double quantityToDue = ActbatchQty;
                    while (itrResult.hasNext()) {
                         NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
                        if (quantityToDue > 0) {                           
                            double dueQty = newProductBatch.getQuantitydue();
                            HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                            batchUpdateQtyMap.put(Constants.Acc_id, newProductBatch.getId());
                            if (dueQty > 0) {
                                if ((quantityToDue > dueQty) && pref.getNegativeStockPR()==1) {
                                    batchUpdateQtyMap.put("qty", String.valueOf(-(dueQty)));
                                    if(newProductBatch.getQuantitydue()>=newProductBatch.getLockquantity()){
                                         batchUpdateQtyMap.put("lockquantity", String.valueOf(-newProductBatch.getLockquantity()));
                                    }
                                     quantityToDue = quantityToDue - dueQty;
                                    
                                } else {
                                    batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
                                    if((newProductBatch.getQuantitydue()-newProductBatch.getLockquantity())<=quantityToDue){
                                        batchUpdateQtyMap.put("lockquantity", String.valueOf(-newProductBatch.getLockquantity()));
                                    }
                                    quantityToDue = quantityToDue - quantityToDue;

                                }
                                 documentMap.put("batchmapid", newProductBatch.getId());   
                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);

                            } else if (isnegativestockforlocwar) {
                                batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
                                batchUpdateQtyMap.put("quantity", String.valueOf(-(quantityToDue)));

                                documentMap.put("batchmapid", newProductBatch.getId());
                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                            }
                        }

                    }

                    }

                } else {

                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("qty", String.valueOf(-(Double.parseDouble(jSONObject.getString("quantity")))));
                    batchUpdateQtyMap.put(Constants.Acc_id, jSONObject.getString("purchasebatchid"));
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                    //Code to Send Batch
                    KwlReturnObject batchObj = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), jSONObject.getString("purchasebatchid"));
                    NewProductBatch newProductBatch1 = (NewProductBatch) batchObj.getEntityList().get(0);
                    if (isWarehouseForProduct && isLocationForProduct) {
                        smd.setBatchName(newProductBatch1.getBatchname());
                    }

                }
                /**
                 * added selected sequence for batch selected by user while
                 * creating PR.
                 */
                documentMap.put("batchsequence", batchsequence++);
                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);


            }
            batchQty--;

            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", 1);
                documentMap.put("serialmapid", jSONObject.getString("purchaseserialid"));
                documentMap.put("documentid", purchaseReturnDetail.getID());
                documentMap.put("transactiontype", "31");//This is GRN Type Tranction  
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", authHandler.getDateOnlyFormat(request).parse(jSONObject.getString("expend")));
                }
                 documentMap.put("purchasereturn", "true");
                
              //  accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
                /**
                 * added selected sequence for serial selected by user while
                 * creating PR.
                 */
                documentMap.put("serialsequence", serialsequence++);
                KwlReturnObject krObj = accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) krObj.getEntityList().get(0);
                if (jSONObject.has(Constants.customfield)) {
                    String customfield = jSONObject.getString(Constants.customfield);
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SerialDocumentMapping");
                        customrequestParams.put("moduleprimarykey", "SerialDocumentMappingId");
                        customrequestParams.put("modulerecid", serialDocumentMapping.getId());
                        customrequestParams.put(Constants.moduleid, Constants.SerialWindow_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyid);
                        DOMap.put(Constants.Acc_id, serialDocumentMapping.getId());
                        customrequestParams.put("customdataclasspath", Constants.Acc_Serial_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("serialcustomdataref", serialDocumentMapping.getId());
                            accCommonTablesDAO.updateserialcustomdata(DOMap);
                        }
                    }
                }
                
                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                serialUpdateQtyMap.put("qty", "-1");
                serialUpdateQtyMap.put("lockquantity", "-1");
                serialUpdateQtyMap.put(Constants.Acc_id, jSONObject.getString("purchaseserialid"));
                serialUpdateQtyMap.put("purchasereturn", true);
                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap); 
                //Code to Send Serial Numbers to Inventory
                KwlReturnObject serialObj = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), jSONObject.getString("purchaseserialid"));
                NewBatchSerial newBatchSerial = (NewBatchSerial) serialObj.getEntityList().get(0);
                if (newBatchSerial != null) {
                    if (isWarehouseForProduct && isLocationForProduct) {
                        smd.addSerialName(newBatchSerial.getSerialname());
                    }
                }

            }else{
               batchQty=0; 
            }
        }
        if (isWarehouseForProduct && isLocationForProduct) {
            for (Map.Entry<Store, StockMovement> entry : storeWiseStockMovement.entrySet()) {
                stockMovementsList.add(entry.getValue());
            }
        }

    }
    
 @Override   
   public KwlReturnObject getPendingConsignmentRequests(String companyid,String productid) throws ServiceException {

        KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);
        List<String> dataList = new ArrayList();
        List params = new ArrayList();
        List<SalesOrder> soList = new ArrayList();


        String qry = " SELECT DISTINCT so.id FROM salesorder so INNER JOIN  sodetails sod ON sod.salesorder=so.id WHERE so.company= ? "
                + " AND sod.product=? AND so.isconsignment='T' AND so.lockquantityflag=1  AND sod.lockquantitydue > 0 "
                + " AND so.fromdate is NOT NULL AND so.fromdate >= ? "
                + " ORDER BY sod.product,so.fromdate ";

        params.add(companyid);
        params.add(productid);
        params.add(new Date());

        try {
            dataList = executeSQLQuery( qry, params.toArray());

            if (!dataList.isEmpty() && dataList != null) {
                for (int i = 0; i < dataList.size(); i++) {
                    String salesorderid = dataList.get(i);
                    if (!StringUtil.isNullOrEmpty(salesorderid)) {
                        SalesOrder so = (SalesOrder) get(SalesOrder.class, salesorderid);
                        if (so != null) {
                            soList.add(so);
                        }
                    }
                }
                retObj = new KwlReturnObject(true, null, null, soList, soList.size());
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.getStackTrace();
            Logger.getLogger(accGoodsReceiptImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retObj;
        }

    }
  
 @Override   
    public KwlReturnObject getConsignmentRequestApproverList(String ruleid) throws ServiceException {
        String query = "select crm.approver from ConsignmentRequestApproverMapping crm where crm.consignmentRequestRule.ID= ?  " ;//and crm.approver.userID != '0796ad1c-b33c-11e3-986d-7777670e1453'
        List list = executeQuery( query, new Object[]{ruleid});
        int count=list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
     
 @Override
    public KwlReturnObject getConsignmentSalesQAReport(Company company, Date fromDate, Date toDate, String statusType, String customerId, String searchString, Paging paging) throws ServiceException{
        
        List params = new ArrayList();
        StringBuilder hql = new StringBuilder(" FROM ConsignmentApprovalDetails cad  WHERE cad.consignment.company=? AND Date(cad.consignment.createdOn) >= ? AND Date(cad.consignment.createdOn) <= ?") ;
        params.add(company);
        params.add(fromDate);
        params.add(toDate);
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND ( cad.consignment.product.productid LIKE ? OR cad.consignment.product.name LIKE ? OR  cad.consignment.transactionNo LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
      
        if(!StringUtil.isNullOrEmpty(customerId)){
            hql.append(" AND cad.consignment.customer.ID = ?");
            params.add(customerId);
        }
        
        hql.append(" ORDER BY cad.consignment.transactionNo DESC");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null && paging.isValid()) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return new KwlReturnObject(true, "", null, list, totalCount);
        
    }
    
@Override
    public KwlReturnObject getStockRequestOnLoanReport(Company company, Date fromDate, Date toDate, String documenttype, String customerId, String searchString, Paging paging) throws ServiceException{
        
        List params = new ArrayList();
        String qryForStockAdj ="";
        String qryForSO  ="";
//        String qryForDO ="";
        String finalQry="";
        
        String sadjSearchString = " ";
        String soSearchString = " ";
//        String doSearchString = " ";
        
        if (!StringUtil.isNullOrEmpty(searchString)) {
//            sadjSearchString = " AND (p.productid LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%' OR  s.seqno LIKE '%" + searchString + "%') ";
            soSearchString = " AND (p.productid LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%' OR so.sonumber LIKE '%" + searchString + "%') ";
//            doSearchString = " AND (p.productid LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%' OR do.donumber LIKE '%" + searchString + "%') ";
        }
        
//        if (StringUtil.isNullOrEmpty(documenttype) || "Stock".equalsIgnoreCase(documenttype)) {
//             qryForStockAdj = " SELECT sad.id as id,s.seqno as transactionno, p.productid as productid , p.description as productdescription,'Stock' as documenttype,"
//                    + " cc.ccid as costcenter,Date(s.createdon) as createdon,NULL as salesperson,"
//                    + "NULL as status,"
//                    + " str.abbrev as warehouse,l.name as location, NULL as assetno,"
//                    + " NULL as requestqunatity,sad.finalquantity as stockquantity,NULL as loanquantity,NULL as customer,mi.value as purpose,NULL as country,NULL as fromdate,"
//                    + " NULL as todate,NULL as transactiontype,p.id as pid , "
//                    + " '0' as rejectedbasequantity,'0' as baseuomquantity ,'0' as approvedbasequantity "
//                    + " FROM in_sa_detail sad INNER JOIN in_stockadjustment s ON s.id=sad.stockadjustment INNER JOIN product p ON s.product=p.id INNER JOIN in_location l "
//                    + " ON l.id=sad.location INNER JOIN in_storemaster str ON str.id=s.store LEFT JOIN costcenter cc ON s.costcenter=cc.id LEFT JOIN masteritem mi ON s.reason=mi.id"
//                    + " WHERE s.company=? AND Date(s.createdon) >= ? AND Date(s.createdon) <= ? " + sadjSearchString;
//            params.add(company);
//            params.add(fromDate);
//            params.add(toDate);
//       
//            finalQry=qryForStockAdj;
//        }
//       
        if (StringUtil.isNullOrEmpty(documenttype) || "Request".equalsIgnoreCase(documenttype)) {
            qryForSO = " SELECT sod.id as id,so.sonumber  as transactionno, p.productid as productid , p.description as productdescription,"
                    + " 'Request' as documenttype,cc.ccid as costcenter,"
                    + " FROM_UNIXTIME(so.createdon/1000, '%Y-%m-%d') as  createdon,"
                    + "mi.value as salesperson ,'' as status,"
//                    + " IF(sod.rejectedbasequantity=sod.baseuomquantity,'Rejected',IF(sod.approvedbasequantity+sod.rejectedbasequantity=sod.baseuomquantity,'Approved',IF(sod.approvedbasequantity+sod.rejectedbasequantity=0,'Pending Approval',IF(sod.approvedbasequantity+sod.rejectedbasequantity < sod.baseuomquantity,'Partially Approved',''))))  as status, "
                    + " w.name as warehouse,l.name as location ,"
                    + " '' as assetno, sod.baseuomquantity as requestqunatity,'' as stockquantity,'' as loanquantity,cst.name as customer,"
                    + " so.memo as purpose,'' as country,"
                    + " Date(so.fromdate) as fromdate,Date(so.todate) as todate,mi2.value as transactiontype,p.id as pid,   "
                    + " sod.rejectedbasequantity as rejectedbasequantity,sod.baseuomquantity as baseuomquantity ,sod.approvedbasequantity as approvedbasequantity,w.id as wid,l.id as lid "
                    + " FROM  sodetails sod INNER JOIN salesorder so ON so.id=sod.salesorder INNER JOIN product p "
                    + " ON p.id=sod.product LEFT JOIN costcenter cc ON so.costcenter=cc.id LEFT JOIN masteritem mi ON so.salesperson=mi.id "
                    + " LEFT JOIN inventorywarehouse w "
                    + " ON w.id=so.requestwarehouse LEFT JOIN inventorylocation l ON l.id=so.requestlocation LEFT JOIN customer cst ON so.customer=cst.id "
                    + " LEFT JOIN masteritem mi2 ON so.movementtype=mi2.id "
                    + " WHERE sod.company=? AND  FROM_UNIXTIME(so.createdon/1000) >= ? AND FROM_UNIXTIME(so.createdon/1000) <= ? "
                    + " AND sod.id NOT IN( SELECT id FROM(SELECT sod.id,dod.deliveredquantity as dodqty,SUM(srd.returnquantity) as srdqty,"
                    + " sod.baseuomquantity as soqty,sod.rejectedbasequantity as sorejectedqty from salesorder so INNER JOIN sodetails sod  ON sod.salesorder=so.id"
                    + " INNER JOIN dodetails dod ON dod.sodetails= sod.id INNER JOIN srdetails srd ON srd.dodetails=dod.id "
                    + " WHERE sod.company= ? AND  FROM_UNIXTIME(so.createdon/1000) >= ? AND FROM_UNIXTIME(so.createdon/1000) <= ? "
                    + " AND (sod.approvedbasequantity+sod.rejectedbasequantity=sod.baseuomquantity) GROUP BY so.id,sod.product  "
                    + " HAVING ((soqty-sorejectedqty)=srdqty AND srdqty > 0 )) as tbl1 ) and so.freezeflag='F' "
                    + soSearchString;
            params.add(company);
            params.add(fromDate);
            params.add(toDate);
            params.add(company);
            params.add(fromDate);
            params.add(toDate);
        
            finalQry=qryForSO;
        }
        
//        if (StringUtil.isNullOrEmpty(documenttype) || "DO".equalsIgnoreCase(documenttype)) {
//            qryForDO = " SELECT dod.id as id,do.donumber  as transactionno, p.productid as productid , p.description as productdescription,'DO' as documenttype,cc.ccid as costcenter,"
//                    + " FROM_UNIXTIME(do.createdon/1000, '%Y-%m-%d') as  createdon,"
//                    + "mi.value as salesperson ,"
//                    + "NULL as status,"
//                    + " w.name as warehouse,l.name as location ,"
//                    + " NULL as assetno, NULL as requestqunatity,NULL as stockquantity,dod.baseuomdeliveredquantity  as loanquantity,cst.name as customer,do.memo as purpose,"
//                    + " NULL as country,Date(so.fromdate) as fromdate,Date(so.todate) as todate,mi2.value as transactiontype,p.id as pid,  "
//                    + " '0' as rejectedbasequantity,'0' as baseuomquantity ,'0' as approvedbasequantity "
//                    + " FROM  dodetails dod INNER JOIN deliveryorder do ON do.id=dod.deliveryorder "
//                    + " INNER JOIN product p ON p.id=dod.product LEFT JOIN costcenter cc ON do.costcenter=cc.id LEFT JOIN masteritem mi2 ON do.movementtype=mi2.id "
//                    + " LEFT JOIN customer cst ON do.customer=cst.id  LEFT JOIN masteritem mi1 ON mi1.id=do.status LEFT JOIN sodetails sod ON dod.sodetails=sod.id "
//                    + " LEFT JOIN salesorder so ON so.id=sod.salesorder LEFT JOIN masteritem mi ON so.salesperson=mi.id  LEFT JOIN inventorywarehouse w ON w.id=so.requestwarehouse LEFT JOIN inventorylocation l ON l.id=so.requestlocation "
//                    + " WHERE dod.company=? AND  FROM_UNIXTIME(do.createdon/1000) >= ? AND FROM_UNIXTIME(do.createdon/1000) <= ? "+doSearchString;
//            params.add(company);
//            params.add(fromDate);
//            params.add(toDate);
//            
//            finalQry=qryForDO;
//        }
            
        
       
        if (StringUtil.isNullOrEmpty(documenttype)) {
//            finalQry = qryForStockAdj + " UNION  " + qryForSO + " UNION  " + qryForDO + " ORDER BY documenttype ASC ,transactionno DESC  " ;
//            finalQry = qryForStockAdj + " UNION  " + qryForSO + " ORDER BY createdon DESC  " ; //" UNION  " + qryForDO + 
        } 
          
        List list = executeSQLQuery( finalQry, params.toArray());
        int totalCount = list.size();
        
        if (paging != null && paging.isValid()) {
            finalQry += " LIMIT " + paging.getOffset() + "," + paging.getLimit();
        }
        list = executeSQLQuery( finalQry, params.toArray());
        
        return new KwlReturnObject(true, "", null, list, totalCount);
        
    }

    
    /**
     * Description : Method is used to Build Purchase Invoice record Json linked
     * in Purchase Return
     *
     * @param <jsonarray> Used to build array of Linked documents Purchase
     * Invoice in Purchase Return
     * @param <listcq> contains id of purchase Invoice Linked in Selected
     * Purchase Return
     * @param <currency> Currency used in documents
     * @param <linkType> Contains Type when Purchase Invoice linked with
     * Purchase Return
     * @param <userdf> Object Of user Date Format
     * @return :JSONArray
     */
    
    @Override
    public JSONArray getPurchaseInvoiceJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid,int type) {
        try {
            Iterator itrcq = listcq.iterator();
            while (itrcq.hasNext()) {
                JSONObject obj = new JSONObject();
                GoodsReceipt goodsreceipt = (GoodsReceipt) itrcq.next();
                Vendor vendor = goodsreceipt.getVendor();
                obj.put(Constants.billid, goodsreceipt.getID());
                obj.put(Constants.companyKey, goodsreceipt.getCompany().getCompanyID());
                obj.put("companyname", goodsreceipt.getCompany().getCompanyName());
                obj.put("withoutinventory", "");
                obj.put("transactionNo", goodsreceipt.getGoodsReceiptNumber());   //delivery order no
                obj.put("billno", goodsreceipt.getGoodsReceiptNumber());
                obj.put(Constants.duedate, goodsreceipt.getDueDate() != null ? userdf.format(goodsreceipt.getDueDate()) : "");
//                obj.put("date", userdf.format(goodsreceipt.getJournalEntry().getEntryDate()));
                obj.put("date", userdf.format(goodsreceipt.getCreationDate()));
                obj.put("journalEntryId", goodsreceipt.getJournalEntry().getID());
                obj.put("journalEntryNo", goodsreceipt.getJournalEntry().getEntryNumber());  //journal entry no
                obj.put(Constants.shipvia, goodsreceipt.getShipvia() == null ? "" : goodsreceipt.getShipvia());
                obj.put(Constants.fob, goodsreceipt.getFob() == null ? "" : goodsreceipt.getFob());
                obj.put(Constants.shipdate, goodsreceipt.getShipDate() == null ? "" : userdf.format(goodsreceipt.getShipDate()));
                obj.put(Constants.memo, goodsreceipt.getMemo());
                obj.put("agent", goodsreceipt.getMasterAgent() == null ? "" : goodsreceipt.getMasterAgent().getID());
                obj.put("agentname", goodsreceipt.getMasterAgent() == null ? "" : goodsreceipt.getMasterAgent().getValue());
                obj.put("termname", vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getTermname()));
                obj.put("termid", vendor == null ? "" : ((vendor.getDebitTerm() == null) ? "" : vendor.getDebitTerm().getID()));//ERP-16831
                obj.put(Constants.currencyKey, currency.getCurrencyID());
                obj.put("currencysymbol", (goodsreceipt.getCurrency() == null ? currency.getSymbol() : goodsreceipt.getCurrency().getSymbol()));

                if (goodsreceipt.isIsconsignment()) {
                    obj.put("mergedCategoryData", "Consignment Vendor Invoice");  //type of data
                } else if (goodsreceipt.isFixedAssetInvoice()) {
                    obj.put("mergedCategoryData", "Fixed Asset Acquired Invoice");  //type of data
                } else {
                    obj.put("mergedCategoryData", "Vendor Invoice");  //type of data
                }
                obj.put("personname", vendor.getName());
                obj.put("personid", vendor.getID());
                if (goodsreceipt.getModifiedby() != null) {
                    obj.put("lasteditedby", StringUtil.getFullName(goodsreceipt.getModifiedby()));
                }
                obj.put("type", type);

                /*
                 * Method is used for building address json
                 *
                 * showing in view mode of Purchase Invoice
                 */

                obj = AccountingAddressManager.getTransactionAddressJSON(obj, goodsreceipt.getBillingShippingAddresses(), true);
                jsonArray.put(obj);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }

    }

    /**
     * Description : Method is used to Build Goods Receipt record Json
     *
     * @param <jsonarray> Used to build array of Linked documents Goods Receipt
     * in Purchase Return
     *
     * @param <listcq> contains id of Goods Receipts Linked in Selected
     * Purchase Return
     * @param <currency> Currency used in documents
     * @param <userdf> Object Of user Date Format
     * @return :JSONArray
     */
    
    @Override
    public JSONArray getGoodsReceiptJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid,int type) {
        try {
            Iterator itrcq = listcq.iterator();
            while (itrcq.hasNext()) {
                JSONObject obj = new JSONObject();
                GoodsReceiptOrder grOrder = (GoodsReceiptOrder) itrcq.next();
                Vendor vendor = grOrder.getVendor();

                obj.put(Constants.billid, grOrder.getID());
                obj.put(Constants.companyKey, grOrder.getCompany().getCompanyID());
                obj.put("companyname", grOrder.getCompany().getCompanyName());
                obj.put("withoutinventory", false);
                obj.put("personid", vendor.getID());
                obj.put("transactionNo", grOrder.getGoodsReceiptOrderNumber());
                obj.put("date", userdf.format(grOrder.getOrderDate()));
                obj.put("personname", vendor.getName());
                obj.put(Constants.externalcurrencyrate, grOrder.getExternalCurrencyRate());
                obj.put("withoutinventory", false);
                obj.put("personid", vendor.getID());
                obj.put("billno", grOrder.getGoodsReceiptOrderNumber());
                obj.put("date", userdf.format(grOrder.getOrderDate()));
                obj.put("personname", vendor.getName());
                obj.put("aliasname", StringUtil.isNullOrEmpty(vendor.getAliasname()) ? "" : vendor.getAliasname());
                obj.put("personemail", vendor.getEmail());
                obj.put(Constants.memo, grOrder.getMemo());
                obj.put("agent", grOrder.getMasterAgent() == null ? "" : grOrder.getMasterAgent().getID());
                obj.put("agentname", grOrder.getMasterAgent() == null ? "" : grOrder.getMasterAgent().getValue());
                obj.put(Constants.posttext, grOrder.getPostText() == null ? "" : grOrder.getPostText());
                obj.put("costcenterid", grOrder.getCostcenter() == null ? "" : grOrder.getCostcenter().getID());
                obj.put("costcenterName", grOrder.getCostcenter() == null ? "" : grOrder.getCostcenter().getName());
                obj.put("statusID", grOrder.getStatus() == null ? "" : grOrder.getStatus().getID());
                obj.put("status", grOrder.getStatus() == null ? "" : grOrder.getStatus().getValue());
                obj.put(Constants.shipdate, grOrder.getShipdate() == null ? "" : userdf.format(grOrder.getShipdate()));
                obj.put(Constants.shipvia, grOrder.getShipvia() == null ? "" : grOrder.getShipvia());
                obj.put(Constants.fob, grOrder.getFob() == null ? "" : grOrder.getFob());
                obj.put("permitNumber", grOrder.getPermitNumber() == null ? "" : grOrder.getPermitNumber());
                obj.put("termid", grOrder.getTerm() == null ? "" : grOrder.getTerm().getID());
                obj.put("termdetails", AccGoodsReceiptServiceHandler.getGRTermDetails(grOrder.getID(), accGoodsReceiptobj));
                obj.put(Constants.currencyKey, (grOrder.getCurrency() == null ? "" : grOrder.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (grOrder.getCurrency() == null ? "" : grOrder.getCurrency().getSymbol()));
                obj.put(Constants.SEQUENCEFORMATID, grOrder.getSeqformat() != null ? grOrder.getSeqformat().getID() : "");

                if (grOrder.getModifiedby() != null) {
                    obj.put("lasteditedby", StringUtil.getFullName(grOrder.getModifiedby()));
                }
                obj.put("type", type);

                /*
                 * Method is used for building address json
                 *
                 * showing in view mode of Goods Receipt
                 */

                obj = AccountingAddressManager.getTransactionAddressJSON(obj, grOrder.getBillingShippingAddresses(), true);

                if (grOrder.isFixedAssetGRO()) {
                    obj.put("mergedCategoryData", "Fixed Asset Goods Receipt");
                } else {
                    obj.put("mergedCategoryData", Constants.Goods_Receipt);  //type of data
                }
                jsonArray.put(obj);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }

    
    /**
     * Description : Method is used to Build Debit Note record Json
     *
     * @param <jsonarray> Used to build array of Linked documents Debit Note
     * in Purchase Return
     *
     * @param <listcq> contains id of Debit Notes Linked in Selected Purchase
     * Return
     * @param <currency> Currency used in documents
     * @param <userdf> Object Of user Date Format
     * @return :JSONArray
     */
    
    @Override
    public JSONArray getDebitNoteJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid,int type) {
        try {
            Iterator itrcq = listcq.iterator();
            while (itrcq.hasNext()) {
                DebitNote debitNote = (DebitNote) itrcq.next();
                JSONObject obj = new JSONObject();

                JournalEntry je = debitNote.getJournalEntry();
                KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), debitNote.getVendorEntry().getID());
                JournalEntryDetail details = (JournalEntryDetail) resultObject.getEntityList().get(0);
                double amountdue = debitNote.isOtherwise() ? debitNote.getDnamountdue() : 0;
                double amountDueOriginal = debitNote.isOtherwise() ? debitNote.getDnamountdue() : 0;
                obj.put("transactionNo", debitNote.getDebitNoteNumber());
                obj.put(Constants.billid, debitNote.getID());
                obj.put("isReturnNote", debitNote.getPurchaseReturn() == null ? false : true);
                obj.put(Constants.companyKey, debitNote.getCompany().getCompanyID());
                obj.put("withoutinventory", false);
//                obj.put("date", (debitNote.isIsOpeningBalenceDN()) ? (userdf.format(debitNote.getCreationDate())) : userdf.format(debitNote.getJournalEntry().getEntryDate()));  //date of delivery order
                obj.put("date", userdf.format(debitNote.getCreationDate()));  //date of delivery order
                obj.put("journalEntryNo", (debitNote.isIsOpeningBalenceDN()) ? "" : debitNote.getJournalEntry().getEntryNumber());  //journal entry no
                obj.put("mergedCategoryData", "Debit Note");  //type of data
                obj.put("cntype", debitNote.getDntype());
                obj.put("costcenterid", debitNote.getCostcenter() == null ? "" : debitNote.getCostcenter().getID());
                obj.put("costcenterName", debitNote.getCostcenter() == null ? "" : debitNote.getCostcenter().getName());
                obj.put(Constants.memo, debitNote.getMemo());
                obj.put("journalentryid", je.getID());
                obj.put("currencysymbol", (debitNote.getCurrency() == null ? currency.getSymbol() : debitNote.getCurrency().getSymbol()));
                obj.put(Constants.currencyKey, (debitNote.getCurrency() == null ? currency.getCurrencyID() : debitNote.getCurrency().getCurrencyID()));
                obj.put("entryno", je.getEntryNumber());
                obj.put("noteno", debitNote.getDebitNoteNumber());
                obj.put("noteid", debitNote.getID());
                obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
//                obj.put("date", userdf.format(je.getEntryDate()));
                obj.put("date", userdf.format(debitNote.getCreationDate()));
                obj.put("personid", debitNote.getVendor().getID());//To show vendor in view mode

                obj.put("currencycode", (debitNote.getCurrency() == null ? currency.getCurrencyCode() : debitNote.getCurrency().getCurrencyCode()));
                obj.put(Constants.SEQUENCEFORMATID, debitNote.getSeqformat() != null ? debitNote.getSeqformat().getID() : "");
                obj.put("type", type);
                jsonArray.put(obj);

            }


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }

    
    /*Method used for adding Entry in linking table of Debit Note & Purchase Return If any Purchase Return is created with Debit Note*/
    @Override
    public void addLinkingInformationCreatingPRwithDN(PurchaseReturn purchasereturn, List debitnoteList) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        try {

            /* Saving Linking information of Purchasereturn */
            String debitNoteID = (String) debitnoteList.get(0);
            String debitNoteNo = (String) debitnoteList.get(1);
            String purchaseReturnNo=(String)debitnoteList.get(2);
            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
            requestParamsLinking.put("linkeddocid", debitNoteID);
            requestParamsLinking.put("docid", purchasereturn.getID());
            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
            requestParamsLinking.put("linkeddocno", debitNoteNo);
            requestParamsLinking.put("sourceflag", 0);
            KwlReturnObject result = accGoodsReceiptobj.savePRLinking(requestParamsLinking); 
            
           /* Saving Linking information of Debit Note  */
            requestParamsLinking.put("linkeddocid", purchasereturn.getID());
            requestParamsLinking.put("docid", debitNoteID);
            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
            requestParamsLinking.put("linkeddocno", purchaseReturnNo);
            requestParamsLinking.put("sourceflag", 1);
            result = accDebitNoteobj.saveDebitNoteLinking(requestParamsLinking);

            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("addLinkingInformationCreatingPRwithDN : " + ex.getMessage(), ex);
        }
    }
/*Method used for adding Entry in linking table of Credit Note & Sales Return If any Sales Return is created with Credit Note*/
    @Override
    public void addLinkingInformationCreatingSRwithCN(SalesReturn salesreturn, List creditnotelist) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        try {

            /* Saving Linking information of Sales Return */
            
            String creditNoteID = (String) creditnotelist.get(0);
            String creditNoteNo = (String) creditnotelist.get(1);
            String salesReturnNo = (String) creditnotelist.get(2);
            HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
            requestParamsLinking.put("linkeddocid", creditNoteID);
            requestParamsLinking.put("docid", salesreturn.getID());
            requestParamsLinking.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
            requestParamsLinking.put("linkeddocno", creditNoteNo);
            requestParamsLinking.put("sourceflag", 0);
            KwlReturnObject result = accInvoiceDAOobj.saveSalesReturnLinking(requestParamsLinking);
            
            /*
             *Add linking entry in paymentlinkikng table 
             */ 
            if (salesreturn.isIsPayment()) {
                requestParamsLinking.put("linkeddocid", salesreturn.getID());
                requestParamsLinking.put("docid", creditNoteID);
                requestParamsLinking.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
                requestParamsLinking.put("linkeddocno", salesReturnNo);
                requestParamsLinking.put("sourceflag", 1);
                result = accVendorPaymentobj.savePaymentLinking(requestParamsLinking);
            }
            if (!salesreturn.isIsPayment()) {
                /*
                 * Saving Linking information of Credit Note
                 */
                requestParamsLinking.put("linkeddocid", salesreturn.getID());
                requestParamsLinking.put("docid", creditNoteID);
                requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                requestParamsLinking.put("linkeddocno", salesReturnNo);
                requestParamsLinking.put("sourceflag", 1);
                result = accCreditNoteDAOobj.saveCreditNoteLinking(requestParamsLinking);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("addLinkingInformationCreatingSRwithCN : " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Description : Method is used to Build Sales Invoice record Json
     *
     * @param <jsonarray> Used to build array of Linked documents Sales Invoice
     * in Sales Return
     *
     * @param <listcq> contains id of Sales Invoice Linked in Selected Sales
     * Return
     * @param <currency> Currency used in documents
     * @param <userdf> Object Of user Date Format
     * @return :JSONArray
     */
 @Override
    public JSONArray getSalesInvoiceJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid, int type) {
        try {
            Iterator itrcq = listcq.iterator();
            while (itrcq.hasNext()) {
                JSONObject obj = new JSONObject();
                Invoice invoice = (Invoice) itrcq.next();
                Customer customer = invoice.getCustomer();
                obj.put(Constants.billid, invoice.getID());
                obj.put(Constants.companyKey, invoice.getCompany().getCompanyID());
                obj.put("companyname", invoice.getCompany().getCompanyName());
                obj.put("transactionNo", invoice.getInvoiceNumber());   //delivery order no
                obj.put("personname", customer.getName());
                obj.put("billno", invoice.getInvoiceNumber());
                obj.put("personid",  customer.getID());
                obj.put("aliasname", customer == null ? "" : customer.getAliasname());
                obj.put("customername", customer.getName());
                obj.put("accountid", invoice.getAccount() == null ? "" : invoice.getAccount().getID());
                obj.put(Constants.duedate, invoice.getDueDate() != null ? userdf.format(invoice.getDueDate()) : "");
//                obj.put("date", userdf.format(invoice.getJournalEntry().getEntryDate()));
                obj.put("date", userdf.format(invoice.getCreationDate()));
                obj.put("journalEntryId", invoice.getJournalEntry().getID());
                obj.put("journalEntryNo", invoice.getJournalEntry().getEntryNumber());  //journal entry no
                obj.put(Constants.shipvia, invoice.getShipvia() == null ? "" : invoice.getShipvia());
                obj.put(Constants.fob, invoice.getFob() == null ? "" : invoice.getFob());
                obj.put(Constants.shipdate, invoice.getShipDate() == null ? "" : userdf.format(invoice.getShipDate()));
                obj.put(Constants.memo, invoice.getMemo());
                obj.put("termname", customer == null ? "" : ((customer.getCreditTerm() == null) ? "" : customer.getCreditTerm().getTermname()));
                obj.put("termid", customer == null ? "" : ((customer.getCreditTerm() == null) ? "" : customer.getCreditTerm().getID()));//ERP-16831
                obj.put(Constants.currencyKey, currency.getCurrencyID());
                obj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
          
                 obj.put("mergedCategoryData", "Customer Invoice");  //type of data
      
                obj.put("personname", invoice.getSalesperson());
                obj.put("personid", customer.getID());
                if (invoice.getModifiedby() != null) {
                    obj.put("lasteditedby", StringUtil.getFullName(invoice.getModifiedby()));
                }
                obj.put("type", type);

                /*
                 * Method is used for building address json
                 *
                 * showing in view mode of Sales Invoice
                 */
                obj = AccountingAddressManager.getTransactionAddressJSON(obj, invoice.getBillingShippingAddresses(), true);
                jsonArray.put(obj);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }

    /**
     * Description : Method is used to Build Delivery Order record Json
     *
     * @param <jsonarray> Used to build array of Linked documents Delivery Order
     * in Sales Return
     *
     * @param <listcq> contains id of Delivery Order Linked in Selected Sales
     * Return
     * @param <currency> Currency used in documents
     * @param <userdf> Object Of user Date Format
     * @return :JSONArray
     */
    
    @Override
    public JSONArray getDeliveryOrderJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid, int type) {
        try {
            Iterator itrcq = listcq.iterator();
            while (itrcq.hasNext()) {
                DeliveryOrder deliveryOrder = (DeliveryOrder) itrcq.next();
                JSONObject obj = new JSONObject();
              
                Customer customer = deliveryOrder.getCustomer();
                obj.put(Constants.billid, deliveryOrder.getID());
                obj.put(Constants.companyKey, deliveryOrder.getCompany().getCompanyID());
                obj.put("transactionNo", deliveryOrder.getDeliveryOrderNumber());   //delivery order no
                obj.put("date", userdf.format(deliveryOrder.getOrderDate()));  //date of delivery order
                obj.put("journalEntryNo", deliveryOrder.getJournalEntry() != null ? deliveryOrder.getJournalEntry().getEntryNumber() : "");  //journal entry no
                obj.put("mergedCategoryData", Constants.Delivery_Order);  //type of data
                obj.put("personname", customer.getName());
                obj.put("companyname", deliveryOrder.getCompany().getCompanyName());
                obj.put(Constants.externalcurrencyrate, deliveryOrder.getExternalCurrencyRate());
                obj.put("personid", customer.getID());
                obj.put("billno", deliveryOrder.getDeliveryOrderNumber());               
                if (deliveryOrder.getModifiedby() != null) {
                    obj.put("lasteditedby", StringUtil.getFullName(deliveryOrder.getModifiedby()));
                }
                obj.put("personname", customer.getName());
                HashMap<String, Object> addressParams = new HashMap<String, Object>();
                addressParams.put(Constants.companyKey, deliveryOrder.getCompany().getCompanyID());
                addressParams.put("customerid", customer.getID());
                CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                obj.put("personemail", customerAddressDetails != null ? customerAddressDetails.getEmailID() : "");
                obj.put("customername", customer.getName());
                obj.put("aliasname", customer.getAliasname());
                obj.put("customercode", customer.getAcccode() == null ? "" : customer.getAcccode());
                obj.put("billtoaddress", deliveryOrder.getBillingShippingAddresses() != null ? CommonFunctions.getBillingShippingAddress(deliveryOrder.getBillingShippingAddresses(), true) : "");
                obj.put("shiptoaddress", deliveryOrder.getBillingShippingAddresses() != null ? CommonFunctions.getBillingShippingAddress(deliveryOrder.getBillingShippingAddresses(), false) : "");
                obj.put("dateinuserformat", userdf.format(deliveryOrder.getOrderDate()));
                obj.put("createdby", deliveryOrder.getCreatedby() == null ? "" : StringUtil.getFullName(deliveryOrder.getCreatedby()));
                obj.put("termdays", customer.getCreditTerm() == null ? 0 : customer.getCreditTerm().getTermdays());
                obj.put("salesPerson", deliveryOrder.getSalesperson() == null ? "" : deliveryOrder.getSalesperson().getID());
                obj.put("salesPersonCode", deliveryOrder.getSalesperson() == null ? "" : deliveryOrder.getSalesperson().getCode());
                obj.put("createdby", deliveryOrder.getCreatedby() == null ? "" : deliveryOrder.getCreatedby().getFullName());
                obj.put("mapSalesPersonName", deliveryOrder.getSalesperson() == null ? "" : deliveryOrder.getSalesperson().getValue());
                obj.put("termname", customer.getCreditTerm() == null ? 0 : customer.getCreditTerm().getTermname());
                obj.put(Constants.memo, deliveryOrder.getMemo());
                obj.put("costcenterid", deliveryOrder.getCostcenter() == null ? "" : deliveryOrder.getCostcenter().getID());
                obj.put("costcenterName", deliveryOrder.getCostcenter() == null ? "" : deliveryOrder.getCostcenter().getName());
                obj.put(Constants.shipdate, deliveryOrder.getShipdate() == null ? "" : userdf.format(deliveryOrder.getShipdate()));
                obj.put(Constants.shipvia, deliveryOrder.getShipvia() == null ? "" : deliveryOrder.getShipvia());
                obj.put(Constants.fob, deliveryOrder.getFob() == null ? "" : deliveryOrder.getFob());

                obj.put(Constants.currencyKey, (deliveryOrder.getCurrency() == null ? "" : deliveryOrder.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (deliveryOrder.getCurrency() == null ? "" : deliveryOrder.getCurrency().getSymbol()));
                obj.put("currencyCode", (deliveryOrder.getCurrency() == null ? "" : deliveryOrder.getCurrency().getCurrencyCode()));
                obj.put(Constants.SEQUENCEFORMATID, deliveryOrder.getSeqformat() != null ? deliveryOrder.getSeqformat().getID() : "");
                    
                BillingShippingAddresses addresses = deliveryOrder.getBillingShippingAddresses();
                AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                obj.put("type", type);
                jsonArray.put(obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }

    
    /**
     * Description : Method is used to Build Credit Note record Json
     *
     * @param <jsonarray> Used to build array of Linked documents Credit Note in
     * Sales Return
     *
     * @param <listcq> contains id of Credit Notes Linked in Selected Sales
     * Return
     * @param <currency> Currency used in documents
     * @param <userdf> Object Of user Date Format
     * @return :JSONArray
     */
    
    
     @Override
    public JSONArray getPaymentJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid, int type) {
        Iterator itrcq = listcq.iterator();
        try {
            while (itrcq.hasNext()) {
                Payment payment = (Payment) itrcq.next();

                JSONObject obj = new JSONObject();
                JournalEntryDetail details = null;
                obj.put(Constants.billid, payment.getID());
                obj.put(Constants.companyKey, payment.getCompany().getCompanyID());
                obj.put("transactionNo", payment.getPaymentNumber());   
//                obj.put("date", (payment.isIsOpeningBalencePayment()) ? (userdf.format(payment.getCreationDate())) : userdf.format(payment.getJournalEntry().getEntryDate()));  //date of delivery order
                obj.put("date", userdf.format(payment.getCreationDate()));  //date of delivery order
                obj.put("mergedCategoryData", "Payment For Refund");  //type of data
                obj.put("noteid", payment.getID());
                obj.put("deleted", payment.isDeleted());
                obj.put(Constants.companyKey, payment.getCompany().getCompanyID());
                obj.put("companyname", payment.getCompany().getCompanyName());
                obj.put("paymentwindowtype", "2");
                obj.put("type","2");
                obj.put("noteno", payment.getPaymentNumber());
                obj.put("journalEntryNo", payment.getJournalEntry() != null ? payment.getJournalEntry().getEntryNumber() : "");
                obj.put("personid", payment.getID());
//                obj.put("date", payment.isIsOpeningBalencePayment() ? userdf.format(payment.getCreationDate()) : userdf.format(payment.getJournalEntry().getEntryDate()));
                obj.put("date", userdf.format(payment.getCreationDate()));
                obj.put(Constants.memo, payment.getMemo());             
                obj.put(Constants.externalcurrencyrate, !payment.isIsOpeningBalencePayment() ? payment.getJournalEntry().getExternalCurrencyRate() : payment.getExternalCurrencyRate());
                obj.put("currencysymbol", (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol()));
                obj.put(Constants.currencyKey, (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID()));
                obj.put("journalentryid", payment.getJournalEntry() != null ? payment.getJournalEntry().getID() : "");
                String reason = "";

                if (!StringUtil.isNullOrEmpty(reason)) {
                    obj.put("reason", reason.substring(0, reason.length() - 1));
                } else {
                    obj.put("reason", reason);
                }
//                obj.put("type",type);

                obj.put(Constants.SEQUENCEFORMATID, payment.getSeqformat() != null ? payment.getSeqformat().getID() : "");

                jsonArray.put(obj);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }
    
     @Override
    public JSONArray getCreditNoteJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid, int type) {
        Iterator itrcq = listcq.iterator();
        try {
            while (itrcq.hasNext()) {
                CreditNote creditNote = (CreditNote) itrcq.next();

                JSONObject obj = new JSONObject();
                JournalEntry je = creditNote.getJournalEntry();
                JournalEntryDetail details = null;
                if (!creditNote.isOtherwise()) {
                    KwlReturnObject resultObject = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), creditNote.getCustomerEntry().getID());
                    details = (JournalEntryDetail) resultObject.getEntityList().get(0);
                }
                Customer customer = creditNote.getCustomer();
                obj.put(Constants.billid, creditNote.getID());
                obj.put(Constants.companyKey, creditNote.getCompany().getCompanyID());
                obj.put("transactionNo", creditNote.getCreditNoteNumber());   
//                obj.put("date", (creditNote.isIsOpeningBalenceCN()) ? (userdf.format(creditNote.getCreationDate())) : userdf.format(creditNote.getJournalEntry().getEntryDate()));  //date of delivery order
                obj.put("date", userdf.format(creditNote.getCreationDate()));  //date of delivery order
                obj.put("mergedCategoryData", "Credit Note");  //type of data
                obj.put("personname", customer.getName());
                obj.put("noteid", creditNote.getID());
                obj.put("deleted", creditNote.isDeleted());
                obj.put("isOldRecord", creditNote.isOldRecord());
                obj.put(Constants.companyKey, creditNote.getCompany().getCompanyID());
                obj.put("companyname", creditNote.getCompany().getCompanyName());
                obj.put("noteno", creditNote.getCreditNoteNumber());
                obj.put("journalEntryNo", je != null ? je.getEntryNumber() : "");
                obj.put("personid", customer.getID());
//                obj.put("date", creditNote.isIsOpeningBalenceCN() ? userdf.format(creditNote.getCreationDate()) : userdf.format(je.getEntryDate()));
                obj.put("date", userdf.format(creditNote.getCreationDate()));
                obj.put(Constants.memo, creditNote.getMemo());             
                obj.put("cntype", creditNote.getCntype());
                obj.put("costcenterid", creditNote.getCostcenter() == null ? "" : creditNote.getCostcenter().getID());
                obj.put("costcenterName", creditNote.getCostcenter() == null ? "" : creditNote.getCostcenter().getName());
                obj.put(Constants.externalcurrencyrate, !creditNote.isIsOpeningBalenceCN() ? je.getExternalCurrencyRate() : creditNote.getExternalCurrencyRate());
                obj.put("currencysymbol", (creditNote.getCurrency() == null ? currency.getSymbol() : creditNote.getCurrency().getSymbol()));
                obj.put(Constants.currencyKey, (creditNote.getCurrency() == null ? currency.getCurrencyID() : creditNote.getCurrency().getCurrencyID()));
                obj.put("journalentryid", je != null ? je.getID() : "");
                String reason = "";

                if (!StringUtil.isNullOrEmpty(reason)) {
                    obj.put("reason", reason.substring(0, reason.length() - 1));
                } else {
                    obj.put("reason", reason);
                }
                obj.put("type",type);

                obj.put(Constants.SEQUENCEFORMATID, creditNote.getSeqformat() != null ? creditNote.getSeqformat().getID() : "");

                jsonArray.put(obj);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }
    
  @Override  
   public JSONObject saveSalesReturn(JSONObject paramJobj) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        KwlReturnObject result = null;
        String billid = "";
        String srInventoryJENo = "";//When MRP activated then Sales Return created with JE. This variable used for displaying JE number in sucess message.
        boolean issuccess = false;
        boolean isConsignment=false;
        boolean isAccountingExe = false;
        boolean isTaxDeactivated = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        Map<String,String> deleteparam=null;
        int srcnflag=0;
        int srPaymentFlag=1;
        try {
            boolean isFixedAsset = paramJobj.optString(Constants.isFixedAsset,null) != null ? Boolean.parseBoolean(paramJobj.getString(Constants.isFixedAsset)) : false;
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isLeaseFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isLeaseFixedAsset)) : false;
            isConsignment = paramJobj.optString(Constants.isConsignment,null) != null ? Boolean.parseBoolean(paramJobj.getString(Constants.isConsignment)) : false;
            boolean isNoteAlso = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteAlso",null))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note
                isNoteAlso = Boolean.parseBoolean(paramJobj.getString("isNoteAlso"));
            }
            /*
             * Its true while payment is creating from sales return
             */ 
            boolean isPayment = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isPayment",null))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note
                isPayment = Boolean.parseBoolean(paramJobj.getString("isPayment"));
            }
            String entryNumber = paramJobj.optString("number",null);
            //For ERP-IOS & Android paramJobj.optBoolean(Constants.isdefaultHeaderMap) =true && if sequenceformat is given then calculate next autonumber and put in code.In edit number is passed
            if (StringUtil.isNullOrEmpty(entryNumber) && paramJobj.optBoolean(Constants.isdefaultHeaderMap) && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {
                JSONObject noObj = accCompanyPreferencesService.getNextAutoNumber(paramJobj);
                entryNumber = noObj.optString(Constants.RES_data, null);
                paramJobj.put("number", entryNumber);
            }
            String fromLinkCombo = paramJobj.optString("fromLinkCombo",null) != null ? paramJobj.getString("fromLinkCombo") : "";
            String creditNoteNumber="";
            String paymentNumber= StringUtil.isNullOrEmpty(paramJobj.optString("paymentNumber",null)) ? "" : paramJobj.getString("paymentNumber");
            String srid =null;
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                srid = paramJobj.optString(Constants.billid, null);
            } else {
                srid = paramJobj.optString("srid", null);
            }
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("AssignSRNumberntocn",null))) {
                creditNoteNumber = entryNumber;
            } else {
                creditNoteNumber = StringUtil.isNullOrEmpty(paramJobj.optString("cndnnumber",null)) ? "" : paramJobj.getString("cndnnumber");
            }
            String creditNoteSequenceFormat=StringUtil.isNullOrEmpty(paramJobj.optString("cndnsequenceformat",null))?"NA":paramJobj.getString("cndnsequenceformat");
            String paymentSequenceFormat=StringUtil.isNullOrEmpty(paramJobj.optString("paymentSequenceFormat",null))?"NA":paramJobj.getString("paymentSequenceFormat");
            String companyid = paramJobj.getString(Constants.companyKey);
            
            //If sequenceformat key is not present
            if (!paramJobj.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {
                String sequenceformatid = null;
                Map<String, Object> sfrequestParams = new HashMap<String, Object>();
                sfrequestParams.put(Constants.companyKey, paramJobj.get(Constants.companyKey));
                sfrequestParams.put("modulename", "autosr");
                KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
                List<SequenceFormat> ll = seqFormatResult.getEntityList();
                if (ll.size()>0) {
                    SequenceFormat format = (SequenceFormat) ll.get(0);
                    sequenceformatid = format.getID();
                    paramJobj.put(Constants.sequenceformat, sequenceformatid);
                } else if (!StringUtil.isNullOrEmpty(entryNumber)) {
                    paramJobj.put(Constants.sequenceformat, "NA");
                }
            }//end of sequenceformat

            if (StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {
                JSONObject response = StringUtil.getErrorResponse("acc.common.erp33", paramJobj, "Sequence Format Details are missing.", messageSource);
                throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
            }
            String sequenceformat = paramJobj.optString(Constants.sequenceformat,null);
            
            KwlReturnObject socnt=null;
            KwlReturnObject creditnotecount=null;
            deleteparam=new HashMap<String, String>();
            deleteparam.put("salesreturnno", entryNumber);
            deleteparam.put("cnno", creditNoteNumber);
            deleteparam.put(Constants.companyKey, companyid);
            if(isNoteAlso){
                deleteparam.put("isautocreatecn", paramJobj.getString("isNoteAlso"));
            }
            if (!StringUtil.isNullOrEmpty(srid)) {//Edit case
                CreditNote creditnote=null;
                String cnid="";
                KwlReturnObject idresult = accCreditNoteDAOobj.getCreditNoteIdFromSRId(srid, companyid);
                if (!(idresult.getEntityList().isEmpty())) {
                    creditnote = (CreditNote) idresult.getEntityList().get(0);
                }
                if (creditnote != null) {  
                    cnid=creditnote.getID();
                }
                
                socnt = accInvoiceDAOobj.getSalesReturnCountForEdit(entryNumber, companyid, srid);
                if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    srcnflag=0;
                    if(isConsignment){
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CR.consignmentreturnno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }else{
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.SR.salesreturnno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                if (isNoteAlso && creditNoteSequenceFormat.equalsIgnoreCase("NA")) {//Autocreate credit note check true
                    creditnotecount = accCreditNoteDAOobj.getCNFromNoteNoAndId(creditNoteNumber, companyid, cnid);//checks for duplicate number
                    if (creditnotecount.getRecordTotalCount() > 0) {
                        srcnflag=1;
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CN.creditnoteno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + creditNoteNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
            } else {//create new case
                socnt = accInvoiceDAOobj.getSalesReturnCount(entryNumber, companyid);
                if (socnt.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    srcnflag=0;
                    if(isConsignment){
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CR.consignmentreturnno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else if (isLeaseFixedAsset) {
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.LR.salesreturnno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.SR.salesreturnno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                if (isNoteAlso && creditNoteSequenceFormat.equalsIgnoreCase("NA")) {//Autocreate credit note check true
                    creditnotecount = accCreditNoteDAOobj.getCNFromNoteNo(creditNoteNumber, companyid);    //checks for duplicate number
                    if (creditnotecount.getRecordTotalCount() > 0) {
                        srcnflag=1;
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CN.creditnoteno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + creditNoteNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                /*
                 * if entered payment is duplicste
                 */
                if (isPayment && paymentSequenceFormat.equalsIgnoreCase("NA")) {
                    result = accVendorPaymentobj.getPaymentFromNo(paymentNumber, companyid);
                    if (result.getRecordTotalCount() > 0 && paymentSequenceFormat.equals("NA")) {
                        srPaymentFlag=0;
                        srcnflag=1;
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.payment.paymentno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + paymentNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                //Check Deactivate Tax in New Transaction.
                if (!fieldDataManagercntrl.isTaxActivated(paramJobj)) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }
            
            synchronized (this) {//Checks duplicate number for simultaneous transactions
                
                status = txnManager.getTransaction(def);
                KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(entryNumber, companyid, Constants.Acc_Sales_Return_ModuleId);//Get entry from temporary table
                if (resultInv.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    srcnflag=0;
                    if(isConsignment){
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CR.selectedConsignmentreturnno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }else{
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.SR.selectedsalesreturnno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                } else {
                    accCommonTablesDAO.insertTransactionInTemp(entryNumber, companyid, Constants.Acc_Sales_Return_ModuleId);//Insert entry into temporary table
                }
                
                if (isNoteAlso && creditNoteSequenceFormat.equals("NA")) {
                    resultInv = accCommonTablesDAO.getTransactionInTemp(creditNoteNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Get entry from temporary table
                    if (resultInv.getRecordTotalCount() > 0) {
                        srcnflag=1;
                        isAccountingExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.CN.selectedcreditnoteno", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + creditNoteNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(creditNoteNumber, companyid, Constants.Acc_Credit_Note_ModuleId);//Insert entry into temporary table
                    }
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
            //Saving SalesReturn 
            List salesReturnList = saveSalesReturnJson(paramJobj);
            SalesReturn salesReturn = (SalesReturn) salesReturnList.get(0);
            billno = salesReturn.getSalesReturnNumber();
            billid = salesReturn.getID();
            String noteMsg = "";
            String creditNoteId = "";
            String paymentNumberId = "";
            if(salesReturn.isIsNoteAlso()){
                creditNoteNumber = (String) salesReturnList.get(1);
                creditNoteId = (String) salesReturnList.get(2);
                     noteMsg =messageSource.getMessage("acc.salesreturn.withCreditNote", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) ;
                            noteMsg = noteMsg +"  ("  + creditNoteNumber + ")  " ;
            }
            /*
             * 
             *Appending msg for show while saveing document
             */ 
            if (salesReturn.isIsPayment()) {
                paymentNumber = (String) salesReturnList.get(5);
                paymentNumberId = (String) salesReturnList.get(6);
                noteMsg = messageSource.getMessage("acc.salesreturn.withPayment", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                noteMsg = noteMsg + "(" + paymentNumber + ")";
            }
            issuccess = true;

            txnManager.commit(status);
            status=null;
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    //for pos and android- linking case
                    if(paramJobj.optBoolean(Constants.isdefaultHeaderMap)){
                        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    } else {
                        def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    }
                    
                    AutoNoStatus = txnManager.getTransaction(def1);
                    if (!sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(srid) && !StringUtil.isNullOrEmpty(sequenceformat)) {
                        boolean seqformat_oldflag = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                        String nextAutoNumber = "";
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_SALESRETURN, sequenceformat);
                            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNumber);
                        } else {
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_SALESRETURN, sequenceformat, seqformat_oldflag, salesReturn.getOrderDate());
                        }
                        seqNumberMap.put(Constants.DOCUMENTID, billid);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        billno = accSalesOrderDAOObj.updateSREntryNumberForNewSR(seqNumberMap);
                        if (isConsignment) {
                            accSalesOrderDAOObj.updateConsignmentEntryNumber(billid, billno, companyid);
                        }
                    }
                    if (salesReturn.isIsNoteAlso()) {
                        if (!creditNoteSequenceFormat.equalsIgnoreCase("NA")  && StringUtil.isNullOrEmpty(srid)) { //create new case with sequence format other than NA

                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_CREDITNOTE, creditNoteSequenceFormat, false, salesReturn.getOrderDate());
                            seqNumberMap.put(Constants.DOCUMENTID, creditNoteId);
                            seqNumberMap.put(Constants.companyKey, companyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, creditNoteSequenceFormat);
                            creditNoteNumber = accSalesOrderDAOObj.updateCNEntryNumberForNewSR(seqNumberMap);
                            noteMsg =messageSource.getMessage("acc.salesreturn.withCreditNote", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) ;
                            noteMsg = noteMsg +" (" + creditNoteNumber + ") " ;
                        }
                        /* Saving Linking Information in SR & CN linking table if Creating Sales return with Credit Note*/
                     if (salesReturn.isIsNoteAlso()) {
                            List creditNoteList = new ArrayList();
                            creditNoteList.add(creditNoteId);
                            creditNoteList.add(creditNoteNumber);
                            creditNoteList.add(entryNumber);
                            addLinkingInformationCreatingSRwithCN(salesReturn, creditNoteList);
                        }
                    }
                      /*
                       * Its true while payment is creating from sales return
                       */ 
                     if (salesReturn.isIsPayment()) {
                        if (!paymentSequenceFormat.equalsIgnoreCase("NA")  && StringUtil.isNullOrEmpty(srid)) { //create new case with sequence format other than NA

                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_PAYMENT, paymentSequenceFormat, false, salesReturn.getOrderDate());
                            seqNumberMap.put(Constants.DOCUMENTID, paymentNumberId);
                            seqNumberMap.put(Constants.companyKey, companyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, paymentSequenceFormat);
                            paymentNumber = accVendorPaymentobj.UpdatePaymentEntry(seqNumberMap);
                            noteMsg =messageSource.getMessage("acc.salesreturn.withPayment", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) ;
                            noteMsg = noteMsg +"(" + paymentNumber + ")" ;
                        }
                        /* Saving Linking Information in SR & CN linking table if Creating Sales return with Credit Note*/
                     if (salesReturn.isIsPayment()) {
                            List creditNoteList = new ArrayList();
                            creditNoteList.add(paymentNumberId);
                            creditNoteList.add(creditNoteNumber);
                            creditNoteList.add(paymentNumber);
                            addLinkingInformationCreatingSRwithCN(salesReturn, creditNoteList);
                        }
                    }
                    if (salesReturn.getInventoryJE() != null && StringUtil.isNullOrEmpty(srid)) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put(Constants.companyKey, companyid);
                        JEFormatParams.put("isdefaultFormat", true);
                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
                        accJournalEntryModuleService.updateJEEntryNumberForNewJE(jeDataMap, salesReturn.getInventoryJE(), companyid, format.getID(), salesReturn.getInventoryJE().getPendingapproval());
                        /**
                         * Get Sales Return JE Number as it is required to display in prompt while saving 
                         * Sales Return document.
                         */
                        srInventoryJENo = salesReturn.getInventoryJE()!=null?salesReturn.getInventoryJE().getEntryNumber():"";
                    }
                    
                    String InvoiceTerms = paramJobj.optString("invoicetermsmap", "[]");
                    if (StringUtil.isAsciiString(InvoiceTerms)) {
                        boolean isSR = true;
                        mapSalesPurcahseReturnTerms(InvoiceTerms, salesReturn.getID(), paramJobj.optString(Constants.useridKey), isSR);
                    }
                    
                    txnManager.commit(AutoNoStatus);
                }
            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                deleteEntryInTemp(deleteparam);//Delete entry in temporary table
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (isConsignment) {
                msg = messageSource.getMessage("acc.Consignment.SalesReturnhasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " : <b>" + billno + "</b>";
            } else if (isFixedAsset) {
                msg = messageSource.getMessage("acc.field.assetSalesReturnHasBeenSavedSuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " : <b>" + billno + "</b>";
            } else if(!StringUtil.isNullOrEmpty(srInventoryJENo)){//when MRP module is activated then there will be JE Generation. In this case we need to show JE number As well
                msg = messageSource.getMessage("acc.up.34", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)))+" "+noteMsg+messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)))+ "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " : <b>" + billno + "</b>, "+messageSource.getMessage("acc.field.JENo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + ": <b>" + srInventoryJENo + "</b>";
            } else {
                msg = messageSource.getMessage("acc.up.34", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)))+" "+noteMsg+messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)))+ "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " : <b>" + billno + "</b>";
            }
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit")) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv")) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            String action = "added a new";
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }
            if(isLeaseFixedAsset) {
                action += " Lease";
            }
            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            /* Preparing Audit trial message if document is linking at the time of creating */
            String linkedDocuments = (String) salesReturnList.get(3);
            String linkingMessages = "";
            if (!StringUtil.isNullOrEmpty(linkedDocuments) && !StringUtil.isNullOrEmpty(fromLinkCombo)) {
                linkingMessages = " by Linking to " + fromLinkCombo + " " + linkedDocuments;
            }
                
            String invoiceDetails = paramJobj.optString("invoicedetails",null);
            if (!StringUtil.isNullOrEmpty(invoiceDetails)) {
                JSONArray jArr = new JSONArray(invoiceDetails);
                for (int k = 0; k < jArr.length(); k++) {
                    JSONObject invjobj = jArr.getJSONObject(k);

                    auditTrailObj.insertAuditLog(AuditAction.CREDIT_NOTE_CREATED, "User " +  paramJobj.getString(Constants.userfullname)  + " has linked Credit Note " + creditNoteNumber + " with Customer Invoice " + invjobj.getString("billno") + ".",auditRequestParams, creditNoteId);
                }
            }
           /* Updating entry in Audit Trial while unlinking transaction through Editing*/
            String unlinkMessage = (String) salesReturnList.get(4);
            if (!StringUtil.isNullOrEmpty(unlinkMessage)) {
                auditTrailObj.insertAuditLog(AuditAction.SALES_RETURN, "User " +  paramJobj.getString(Constants.userfullname)  + " has unlinked " + "Sales Return(s) " + billno + unlinkMessage + ".", auditRequestParams, billno);
            }
            String auditTrialDetailMsg="";
            try{
//                auditTrialDetailMsg=getDetailedMsgForSRAuditTrial(salesReturn);
            }catch(Exception ee){
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ee);  
            }
            auditTrailObj.insertAuditLog(AuditAction.SALES_RETURN, "User " +  paramJobj.getString(Constants.userfullname)  + " has " + action + " Sales Return " + billno+linkingMessages, auditRequestParams, salesReturn.getID());
            
            //Allocating quantity to Blocked SO Quantity 
            boolean activateCRblockingWithoutStock=false;
            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), salesReturn.getCompany().getCompanyID());
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            activateCRblockingWithoutStock=extraCompanyPreferences.isActivateCRblockingWithoutStock();
            if(activateCRblockingWithoutStock && salesReturn!=null){
            TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
                try {
                    Set<SalesReturnDetail> srDetailsObj = salesReturn.getRows();
                    for (SalesReturnDetail row : srDetailsObj) {
                        Product product = row.getProduct();
                        Inventory inventory = row.getInventory();
                        HashMap<Integer, Object[]> srBatchdetalisMap = new HashMap<Integer, Object[]>();
                        boolean isLocationForProduct = false;
                        boolean isWarehouseForProduct = false;
                        boolean isBatchForProduct = false;
                        boolean isSerialForProduct = false;
                        boolean isRowForProduct = false;
                        boolean isRackForProduct = false;
                        boolean isBinForProduct = false;
                        if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                            isBatchForProduct = product.isIsBatchForProduct();
                            isSerialForProduct = product.isIsSerialForProduct();
                            isLocationForProduct = product.isIslocationforproduct();
                            isWarehouseForProduct = product.isIswarehouseforproduct();
                            isRowForProduct = product.isIsrowforproduct();
                            isRackForProduct = product.isIsrackforproduct();
                            isBinForProduct = product.isIsbinforproduct();

                        }
                        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), row.getCompany().getCompanyID());
                        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                        if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                            if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                                //getNewBatchJson(row.getProduct(), request, row.getID(),grBatchdetalisMap);
                                updateBatchDetailsForSO(srBatchdetalisMap, row.getProduct().getID(), inventory, paramJobj, row.getID());
                            }
                        }

                    }
                    txnManager.commit(statusforBlockSOQty);

                } catch (Exception ex) {
                    txnManager.rollback(statusforBlockSOQty);
                    Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            status = txnManager.getTransaction(def);
            deleteEntryInTemp(deleteparam);//Delete entry in temporary table
            txnManager.commit(status);

            //==============Create Rounding JE Start=====================
            try {
                if(salesReturn.isIsNoteAlso() && !StringUtil.isNullOrEmpty(invoiceDetails)){//Sales Return with CN having Invoices linked
                    paramJobj.put("SRWithCN",true);
                    paramJobj.put("isEdit",isEdit);
                    paramJobj.put("cnid", creditNoteId);
                    accCreditNoteService.postRoundingJEAfterLinkingInvoiceInCreditNote(paramJobj);
                }    
            } catch (ServiceException ex) {
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
            //==============Create Rounding JE End=======================
            
            /**
             * ERM-294
             * Commit tax to Avalara and save into database, in case of Avalara Integration
             * isPayment check is added to handle cash sale refund.
             */
            if (extraCompanyPreferences.isAvalaraIntegration() && salesReturn != null && (isNoteAlso || isPayment) && extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                JSONObject paramsJobj = new JSONObject();
                paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                paramsJobj.put(Constants.companyKey, companyid);
                boolean isCommit = integrationCommonService.isTaxCommittingEnabled(paramsJobj);
                if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {//Commit taxes to Avalara and save taxes only if Tax Committing is enabled in System Controls
                    accountingHandlerDAOobj.flushHibernateSession();//Synchronize hibernate session with database, i.e. save unsaved changes into database
                    accountingHandlerDAOobj.evictObj(salesReturn);//Remove current 'salesReturn' object from Hibernate session so that when we fetch salesReturn object again, we get latest record from database
                    KwlReturnObject tempKwlObj = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), billid);
                    salesReturn = (SalesReturn) tempKwlObj.getEntityList().get(0);
                    paramJobj.put("linkDocId", isNoteAlso ? creditNoteId : paymentNumberId);
                    paramJobj.put("linkDocTableName", isNoteAlso ? "CreditNote" : "Payment");
                    JSONObject tempJobj = commitTaxToAvalaraAndSave(paramJobj, salesReturn, billid, billno, companyid, isEdit, msg, isCommit, fromLinkCombo);
                    msg = tempJobj.optString(Constants.RES_msg, msg);
                } else {
                    msg += "<br><br><b>NOTE:</b> " + messageSource.getMessage("acc.integration.taxNotCommitted", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
                }
            }
            
            
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
                deleteEntryInTemp(deleteparam);//Delete entry in temporary table
                msg = "" + ex.getMessage();
            Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }catch (Exception ex) {
            if(status!=null) {
                txnManager.rollback(status);
            }
                deleteEntryInTemp(deleteparam);//Delete entry in temporary table
                msg = "" + ex.getMessage();
                if (ex.getMessage() == null) {
                    msg = ex.getCause().getMessage();
                }
            Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.billid, billid);     //To get Billid & Billno in response in PR copy case
                jobj.put("billno", billno);
                jobj.put(Constants.isConsignment, isConsignment); 
                jobj.put("accException", isAccountingExe);
                jobj.put("srcnflag", srcnflag);
                jobj.put("srPaymentFlag", srPaymentFlag);
                String channelName = "/SalesReturnReport/gridAutoRefresh";
                jobj.put(Constants.channelName, channelName);
                jobj.put(Constants.isTaxDeactivated, isTaxDeactivated);

            } catch (JSONException ex) {
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
  
  
  public List mapSalesPurcahseReturnTerms(String InvoiceTerms, String ID, String userid, boolean isSR) throws ServiceException {
        List ll = new ArrayList();
        try {
            JSONArray termsArr = new JSONArray(InvoiceTerms);
            for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                JSONObject temp = termsArr.getJSONObject(cnt);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                termMap.put("term", temp.getString("id"));
                termMap.put("termamount", Double.parseDouble(temp.getString("termamount")));
                termMap.put("termtaxamount", temp.optDouble("termtaxamount",0));
                termMap.put("termtaxamountinbase", temp.optDouble("termtaxamountinbase",0));
                termMap.put("termtax", temp.optString("termtax",null));
                termMap.put("termAmountExcludingTax", temp.optDouble("termAmountExcludingTax",0));
                termMap.put("termAmountExcludingTaxInBase", temp.optDouble("termAmountExcludingTaxInBase",0));
                termMap.put("termamountinbase", temp.optDouble("termamountinbase",0));
                double percentage = 0;
                if (!StringUtil.isNullOrEmpty(temp.getString("termpercentage"))) {
                    percentage = Double.parseDouble(temp.getString("termpercentage"));
                }
                termMap.put("termpercentage", percentage);
                termMap.put("creationdate", new Date());
                termMap.put("userid", userid);
                if (isSR) {
                    termMap.put("salesReturnID", ID);
                    accInvoiceDAOobj.saveSalesReturnTermMap(termMap);
                } else {
                    termMap.put("purchaseReturnID", ID);
                    accGoodsReceiptobj.savePurchaseReturnTermMap(termMap);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }
  
   
    /**
     * Below method is executed only when Avalara Integration is enabled
     * The code voids previously committed taxes on Avalara in case of edit, then it commits new taxes to Avalara, 
     * and saves the tax details in database table TransactionDetailAvalaraTaxMapping
     * Finally the code updates the taxCommittedOnAvalara flag in SalesReturn table, which indicates whether or not taxes were successfully committed
     * @param paramJobj
     * @param salesReturn
     * @param srid
     * @param srno
     * @param companyid
     * @param isEdit
     * @param msg
     * @param fromLinkCombo
     * @return
     * @throws JSONException 
     */
    private JSONObject commitTaxToAvalaraAndSave(JSONObject paramJobj, SalesReturn salesReturn, String srid, String srno, String companyid, boolean isEdit, String msg, boolean isCommit, String fromLinkCombo) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        HashMap paramsMap = null;
        try {
            if (isEdit && salesReturn.isIsTaxCommittedOnAvalara()) {//Void previously committed tax in case of edit
                JSONObject cancelAvalaraTaxJobj = new JSONObject();
                cancelAvalaraTaxJobj.put("CancelCode", "DocDeleted");
                cancelAvalaraTaxJobj.put("DocCode", srno);
                cancelAvalaraTaxJobj.put(Constants.moduleid, String.valueOf(Constants.Acc_Sales_Return_ModuleId));
                cancelAvalaraTaxJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                cancelAvalaraTaxJobj.put(Constants.companyKey, companyid);
                try {
                    cancelAvalaraTaxJobj.put(IntegrationConstants.integrationOperationIdKey, IntegrationConstants.avalara_cancelTax);
                    JSONObject cancelTaxResponseJobj = integrationCommonService.processIntegrationRequest(cancelAvalaraTaxJobj);
                    if (!cancelTaxResponseJobj.optBoolean(Constants.RES_success, false)) {
                        throw new AccountingException(messageSource.getMessage("acc.integration.taxCommitFailureMsg1", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        paramsMap = new HashMap<String, Object>();
                        paramsMap.put(Constants.Acc_id, srid);
                        paramsMap.put("isTaxCommittedOnAvalara", false);
                        /**
                         * Adding below keys because these are mandatorily required by 'accInvoiceDAOobj.saveSalesReturn' method
                         * No changes are being made to these
                         */
                        if (salesReturn.getCostcenter() != null) {
                            paramsMap.put("costCenterId", salesReturn.getCostcenter().getID());
                        }
                        accInvoiceDAOobj.saveSalesReturn(paramsMap);//Update 'isTaxCommittedOnAvalara' flag to false after tax-void
                    }
                } catch (Exception ex) {
                    Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    String tempMsg = StringUtil.isNullOrEmpty(ex.getMessage()) ? messageSource.getMessage("acc.integration.taxCommitFailureMsg1", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) : ex.getMessage();
                    throw new AccountingException(tempMsg);
                }
            }

            JSONArray lineDetailJarr = new JSONArray();
            Set<SalesReturnDetail> salesReturnDetailsSet = salesReturn.getRows();
            for (SalesReturnDetail salesReturnDetail : salesReturnDetailsSet) {
                if (salesReturnDetail.getInventory() != null) {
                    JSONObject lineDetailjobj = new JSONObject();
                    lineDetailjobj.put(Constants.productid, salesReturnDetail.getInventory().getProduct().getID());
                    lineDetailjobj.put("pid", salesReturnDetail.getInventory().getProduct().getProductid());
                    lineDetailjobj.put("desc", salesReturnDetail.getDescription());
                    lineDetailjobj.put(Constants.quantity, salesReturnDetail.getInventory().getQuantity());
                    double amountwithouttax = 0D;
                    double discount = salesReturnDetail.getDiscount();
                    if (salesReturnDetail.getCidetails() != null) {//if linked with invoice, take amount from invoice to handle partial invoice case
                        amountwithouttax = salesReturnDetail.getCidetails().getRowExcludingGstAmount();
                    } else if (discount != 0) {
                        if (salesReturnDetail.getDiscountispercent() == 1) {
                            amountwithouttax = ((salesReturnDetail.getRate() * salesReturnDetail.getInventory().getQuantity()) * (100.0D - discount)) / (100.0D);
                        } else {
                            amountwithouttax = (salesReturnDetail.getRate() * salesReturnDetail.getInventory().getQuantity()) - discount;
                        }
                    } else {
                        amountwithouttax = salesReturnDetail.getRate() * salesReturnDetail.getInventory().getQuantity();
                    }
                    lineDetailjobj.put("amountwithouttax", amountwithouttax);
                    lineDetailjobj.put(IntegrationConstants.parentRecordID, salesReturnDetail.getID());

                    /**
                     * In case of link with Sales Invoice, add additional 
                     * parameters which are used for tax override in Avalara 
                     * service call
                     */
                    if (fromLinkCombo.equalsIgnoreCase("Sales Invoice") && salesReturnDetail.getCidetails() != null && salesReturnDetail.getCidetails().getInvoice() != null) {
                        String linkedInvoiceId = salesReturnDetail.getCidetails().getInvoice().getID();
                        lineDetailjobj.put(IntegrationConstants.taxOverrideDocModuleId, Constants.Acc_Invoice_ModuleId);
                        lineDetailjobj.put(IntegrationConstants.taxOverrideDocId, linkedInvoiceId);
                        lineDetailjobj.put(IntegrationConstants.taxOverrideType, "TaxDate");
                        lineDetailjobj.put(IntegrationConstants.taxOverrideReason, "Return");

                        JournalEntry tempJE = salesReturnDetail.getCidetails().getInvoice().getJournalEntry();
                        if (tempJE != null) {
                            lineDetailjobj.put("journalentryId", tempJE.getID());
                        }

                        BillingShippingAddresses billingShippingAddresses = salesReturnDetail.getCidetails().getInvoice().getBillingShippingAddresses();
                        if (billingShippingAddresses != null) {
                            JSONObject addrObject = new JSONObject();
                            addrObject.put("address", billingShippingAddresses.getShippingAddress() != null ? billingShippingAddresses.getShippingAddress() : "");
                            addrObject.put("city", billingShippingAddresses.getShippingCity() != null ? billingShippingAddresses.getShippingCity() : "");
                            addrObject.put("state", billingShippingAddresses.getShippingState() != null ? billingShippingAddresses.getShippingState() : "");
                            addrObject.put("country", billingShippingAddresses.getShippingCountry() != null ? billingShippingAddresses.getShippingCountry() : "");
                            addrObject.put("postalCode", billingShippingAddresses.getShippingPostal() != null ? billingShippingAddresses.getShippingPostal() : "");
                            addrObject.put("recipientName", billingShippingAddresses.getShippingRecipientName() != null ? billingShippingAddresses.getShippingRecipientName() : "");
                            lineDetailjobj.put(IntegrationConstants.shipToAddressForAvalara, addrObject.toString());
                        }
                    }

                    lineDetailJarr.put(lineDetailjobj);
                }
            }

            Customer customer = salesReturn.getCustomer();
            paramsMap = new HashMap<String, Object>();
            paramsMap.put(Constants.customerid, customer.getID());
            paramsMap.put(Constants.companyKey, companyid);
            paramsMap.put("isBillingAddress", false);
            KwlReturnObject kwlObj = accountingHandlerDAOobj.getCustomerAddressDetails(paramsMap);

            JSONObject addrObject = new JSONObject();
            if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                CustomerAddressDetails cad = (CustomerAddressDetails) kwlObj.getEntityList().get(0);
                addrObject.put("address", cad.getAddress() != null ? cad.getAddress() : "");
                addrObject.put("city", cad.getCity() != null ? cad.getCity() : "");
                addrObject.put("state", cad.getState() != null ? cad.getState() : "");
                addrObject.put("country", cad.getCountry() != null ? cad.getCountry() : "");
                addrObject.put("postalCode", cad.getPostalCode() != null ? cad.getPostalCode() : "");
                addrObject.put("recipientName", cad.getRecipientName() != null ? cad.getRecipientName() : "");
            }
            /**
             * ERM-294
             * Change table name parameter for cash sale refund.
             */
            JSONObject avalaraSaveTaxJobj = new JSONObject();
            paramsMap = new HashMap<String, String>();
            paramsMap.put("fetchColumn", "journalEntry.entryNumber");
            paramsMap.put("tableName", paramJobj.optString("linkDocTableName"));
            paramsMap.put("companyColumn", Constants.ID);
            paramsMap.put(Constants.companyKey, paramJobj.optString("linkDocId"));
            kwlObj = accountingHandlerDAOobj.populateMasterInformation(paramsMap);
            if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                String jeNumber = (String) kwlObj.getEntityList().get(0);
                avalaraSaveTaxJobj.put("jeNumber", jeNumber);
            }

            avalaraSaveTaxJobj.put(IntegrationConstants.shipToAddressForAvalara, addrObject.toString());
            avalaraSaveTaxJobj.put(Constants.detail, lineDetailJarr.toString());
            //avalaraExemptionCode --> Value of 'AvaTax Exemption Code' dimension
            String avalaraExemptionCode = null;
            if (paramJobj.has(IntegrationConstants.avalaraExemptionCode)) {
                avalaraExemptionCode = paramJobj.getString(IntegrationConstants.avalaraExemptionCode);
            } else if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.customfield, null))) {
                avalaraExemptionCode = integrationCommonService.getExemptionCodeFromCustomFieldsJson(paramJobj);
            } else {
                avalaraExemptionCode = integrationCommonService.getExemptionCodeFromRefModule(companyid, null, Constants.Acc_Sales_Return_ModuleId, srid);
            }
            avalaraSaveTaxJobj.put(IntegrationConstants.avalaraExemptionCode, avalaraExemptionCode);
            avalaraSaveTaxJobj.put(IntegrationConstants.commit, isCommit);
            if (isCommit) {
                avalaraSaveTaxJobj.put(IntegrationConstants.avalaraDocCode, srno);
            }
            avalaraSaveTaxJobj.put(Constants.billid, srid);
            avalaraSaveTaxJobj.put(Constants.billno, srno);
            avalaraSaveTaxJobj.put(Constants.BillDate, authHandler.getDateOnlyFormat().format(salesReturn.getOrderDate()));
            avalaraSaveTaxJobj.put(Constants.currencyKey, salesReturn.getCurrency() != null ? salesReturn.getCurrency().getCurrencyID() : null);
            avalaraSaveTaxJobj.put("currencyCode", salesReturn.getCurrency() != null ? salesReturn.getCurrency().getCurrencyCode() : null);
            double exchangeRate = salesReturn.getExternalCurrencyRate() != 0 ? (1.0d / salesReturn.getExternalCurrencyRate()) : 0;
            avalaraSaveTaxJobj.put("exchangeRate", exchangeRate != 0 ? exchangeRate : null);
            avalaraSaveTaxJobj.put(Constants.customerid, customer.getID());
            avalaraSaveTaxJobj.put("customerCode", customer.getAcccode());
            avalaraSaveTaxJobj.put("salesPersonCode", salesReturn.getSalesperson() != null ? salesReturn.getSalesperson().getCode() : "");
            avalaraSaveTaxJobj.put("salespersonid", salesReturn.getSalesperson() != null ? salesReturn.getSalesperson().getID() : "");
            avalaraSaveTaxJobj.put(Constants.moduleid, String.valueOf(Constants.Acc_Sales_Return_ModuleId));
            avalaraSaveTaxJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
            avalaraSaveTaxJobj.put(Constants.companyKey, companyid);

            /**
             * Commit tax to Avalara and save in database
             */
            JSONObject taxCommitResponseJobj = integrationCommonService.commitAndSaveTax(avalaraSaveTaxJobj);

            try {
                boolean isCommitSuccessful = taxCommitResponseJobj.optBoolean(Constants.RES_success, false);
                paramsMap = new HashMap<String, Object>();
                paramsMap.put("id", srid);
                paramsMap.put("isTaxCommittedOnAvalara", isCommitSuccessful);
                /**
                 * Adding below keys because these are mandatorily required by 'accInvoiceDAOobj.saveSalesReturn' method
                 * No changes are being made to these
                 */
                if (salesReturn.getCostcenter() != null) {
                    paramsMap.put("costCenterId", salesReturn.getCostcenter().getID());
                }
                accInvoiceDAOobj.saveSalesReturn(paramsMap);//Update 'isTaxCommittedOnAvalara' flag after tax commit
            } catch (Exception ex) {
                Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw new AccountingException(messageSource.getMessage("acc.integration.taxCommittedMsg", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }

            msg += "<br><br><b>NOTE:</b> " + taxCommitResponseJobj.optString(Constants.RES_msg);
//                    }
        } catch (AccountingException ex) {
            msg += "<br><br><b>NOTE:</b> " + (StringUtil.isNullOrEmpty(ex.getMessage()) ? messageSource.getMessage("acc.integration.taxCommitFailureMsg", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) : ex.getMessage());
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg += "<br><br><b>NOTE:</b> " + messageSource.getMessage("acc.integration.taxCommitFailureMsg", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
        } finally {
            returnJobj.put(Constants.RES_msg, msg);
        }
        return returnJobj;
    }
  
    private String getDetailedMsgForSRAuditTrial(SalesReturn salesReturn) {
        String msg = "";
        Set<SalesReturnDetail> srDetailsObj = salesReturn.getRows();
        int count=0;
        for (SalesReturnDetail row : srDetailsObj) {
            Product product = row.getProduct();
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            String srDetailId=row.getID();
            double srdQty=row.getBaseuomquantity();
            if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                isBatchForProduct = product.isIsBatchForProduct();
                isSerialForProduct = product.isIsSerialForProduct();
            }
            
            msg +=  " ("+(count+1)+")  <b> Product Id : </b>  "+product.getProductid()+" <b>Quantity : </b>  "+srdQty;
            if(isBatchForProduct && isSerialForProduct){
                
            }else if(isBatchForProduct && !isSerialForProduct){
                
            }else if(!isBatchForProduct && isSerialForProduct){
                
            }
            count++;
        }
        return msg;
    }
  
  
  /**
   * Description : This Method is used to update the delivery order data
   * @param paramJobj
   * @return
   * @throws JSONException
   * @throws SessionExpiredException
   * @throws ServiceException 
   */
    @Override
   public JSONObject updateSalesReturn(JSONObject paramJobj) throws JSONException, SessionExpiredException, ServiceException {
       JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        boolean issuccess = false;
        boolean isAccountingExe = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        SalesReturn salesReturn = null;
        int srcnflag=0;
        boolean isFixedAsset = false;
        boolean isConsignment = false;
        boolean isLeaseFixedAsset = false;
        String companyid = "";
        try {
            status = txnManager.getTransaction(def);
            String srid = paramJobj.optString("srid", null);
            HashMap<String, Object> srDataMap = new HashMap<>();
            DateFormat df = authHandler.getDateOnlyFormat();
            String isfavourite = paramJobj.optString("isfavourite",null);
            String costCenterId = paramJobj.optString(Constants.costcenter,null);
            isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isFixedAsset)) : false;
            isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isLeaseFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isLeaseFixedAsset)) : false;
            isConsignment = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isConsignment,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isConsignment)) : false;
            long createdon = System.currentTimeMillis();
            String createdby = paramJobj.getString(Constants.useridKey);
            String modifiedby = paramJobj.getString(Constants.useridKey);
            long updatedon = createdon;
            
            if (paramJobj.has("companyid") && paramJobj.get("companyid") != null) {
                companyid = (String) paramJobj.get("companyid");
            }
            srDataMap.put(Constants.Acc_id, srid);
            srDataMap.put("createdon", createdon);
            srDataMap.put("createdby", createdby);
            srDataMap.put("modifiedby", modifiedby);
            srDataMap.put("updatedon", updatedon);
            srDataMap.put(Constants.memo, paramJobj.optString(Constants.memo,""));
              if (!StringUtil.isNullOrEmpty(costCenterId)) {
                srDataMap.put("costCenterId", costCenterId);
            }
            srDataMap.put(Constants.posttext, paramJobj.optString(Constants.posttext,null) == null ? "" : paramJobj.getString(Constants.posttext));
            if (paramJobj.optString(Constants.shipdate,null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.shipdate,null))) {
                srDataMap.put(Constants.shipdate, df.parse(paramJobj.getString(Constants.shipdate)));
            }
            srDataMap.put("orderdate", df.parse(paramJobj.optString(Constants.BillDate,null)));
            srDataMap.put(Constants.Checklocktransactiondate, paramJobj.optString(Constants.BillDate,null));//ERP-16800-Without parsing date
            srDataMap.put("isfavourite", isfavourite);
            srDataMap.put(Constants.companyKey, companyid);
            srDataMap.put(Constants.isConsignment, isConsignment);
            KwlReturnObject doresult = accInvoiceDAOobj.saveSalesReturn(srDataMap);
            salesReturn = (SalesReturn) doresult.getEntityList().get(0);
            billno=salesReturn.getSalesReturnNumber();
            billid=salesReturn.getID();
            paramJobj.put("id", salesReturn.getID());

            HashSet<SalesReturnDetail> srDetails = updateSalesReturnRows(paramJobj);
            String customfield = paramJobj.optString(Constants.customfield, null);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_SalesReturn_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_SalesReturnId);
                customrequestParams.put("modulerecid", salesReturn.getID());
                customrequestParams.put(Constants.moduleid, isConsignment ? Constants.Acc_ConsignmentSalesReturn_ModuleId : isFixedAsset ? Constants.Acc_FixedAssets_Sales_Return_ModuleId : isLeaseFixedAsset ? Constants.Acc_Lease_Return : Constants.Acc_Sales_Return_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_SalesReturn_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    srDataMap.put("accsalesreturncustomdataref", salesReturn.getID());
                    KwlReturnObject accresult = accInvoiceDAOobj.updateSalesReturnCustomData(srDataMap);
                }
            }
            if (isConsignment) {
                msg = messageSource.getMessage("acc.Consignment.SalesReturnhasbeenupdatedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " : <b>" + billno + "</b>";
            } 
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit")) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("copyInv")) ? false : Boolean.parseBoolean(paramJobj.getString("copyInv"));
            String action = "added a new";
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            auditTrailObj.insertAuditLog(AuditAction.SALES_RETURN, "User " +  paramJobj.getString(Constants.userfullname)  + " has " + action + " Sales Return " + billno, auditRequestParams, salesReturn.getID());
            txnManager.commit(status);
            issuccess = true; 
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateSalesReturnRows : " + ex.getMessage(), ex);
        } catch (Exception ex) {
           throw ServiceException.FAILURE("updateSalesReturnRows : " + ex.getMessage(), ex);
         } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.billid, billid);     //To get Billid & Billno in response in PR copy case
                jobj.put("billno", billno);
                jobj.put(Constants.isConsignment, isConsignment); 
                jobj.put("accException", isAccountingExe);
                jobj.put("srcnflag", srcnflag);
                String channelName = "/SalesReturnReport/gridAutoRefresh";
                jobj.put(Constants.channelName, channelName);

            } catch (JSONException ex) {
                Logger.getLogger(accSalesReturnControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        

        return jobj;
    }

    /**
     * Description : This Method is used to update the Sales Return line level
     * @param paramJobj 
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    private HashSet updateSalesReturnRows(JSONObject paramJobj) throws ServiceException, JSONException {
        HashSet<SalesReturnDetail> rows = new HashSet<>();
        String detail = "";
        boolean isFixedAsset = false;
        boolean isConsignment = false;
        boolean isLeaseFixedAsset = false;
        String companyid = "";
        try {

            if (paramJobj.has("companyid") && paramJobj.get("companyid") != null) {
                companyid = (String) paramJobj.get("companyid");
            }
            
            isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isFixedAsset)) : false;
            isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isLeaseFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isLeaseFixedAsset)) : false;
            isConsignment = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isConsignment,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isConsignment)) : false;

            if (paramJobj.has("detail") && paramJobj.get("detail") != null) {
                detail = (String) paramJobj.get("detail");
            }
            JSONArray jArr = new JSONArray(detail);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                SalesReturnDetail row = null;
                if (jobj.has("originalTransactionRowid")) {
                    KwlReturnObject srDetailsResult = accountingHandlerDAOobj.getObject(SalesReturnDetail.class.getName(),jobj.getString("originalTransactionRowid"));
                    row = (SalesReturnDetail) srDetailsResult.getEntityList().get(0);
                }

                if (row != null) {

                    if (jobj.has("srno")) {
                        row.setSrno(jobj.getInt("srno"));
                    }

                    if (!StringUtil.isNullOrEmpty(jobj.optString("description"))) {
                        try {
                            row.setDescription( StringUtil.DecodeText(jobj.optString("description")));
                        } catch (Exception ex) {
                            row.setDescription(jobj.optString("description"));
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(jobj.optString("remark"))) { // Updating Remark feild after Link in Document's
                        try {
                            row.setRemark( StringUtil.DecodeText(jobj.optString("remark")));
                        } catch (Exception ex) {
                            row.setRemark(jobj.optString("remark"));
                        }
                    }

                    String customfield = jobj.optString(Constants.customfield, null);
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> srMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SalesReturnDetail");
                        customrequestParams.put("moduleprimarykey", "SalesReturnDetailId");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put(Constants.moduleid, isConsignment ? Constants.Acc_ConsignmentSalesReturn_ModuleId : isFixedAsset ? Constants.Acc_FixedAssets_Sales_Return_ModuleId : isLeaseFixedAsset ? Constants.Acc_Lease_Return : Constants.Acc_Sales_Return_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyid);
                        srMap.put(Constants.Acc_id, row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_SalesReturnDetails_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            srMap.put("srdetailscustomdataref", row.getID());
                            accInvoiceDAOobj.updateSRDetailsCustomData(srMap);
                        }
                    }

                    // Add Custom fields details for Product
                    if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                        HashMap<String, Object> srMap = new HashMap<String, Object>();
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SrProductCustomData");
                        customrequestParams.put("moduleprimarykey", "SrDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyid);
                        srMap.put(Constants.Acc_id, row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_SRDetail_Productcustom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            srMap.put("srdetailscustomdataref", row.getID());
                            accInvoiceDAOobj.updateSRDetailsProductCustomData(srMap);
                        }
                    }
                    rows.add(row);
                }
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("updateSalesReturnRows : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateSalesReturnRows : " + ex.getMessage(), ex);
        }
        return rows;
    }
  
  @Override
  /*Request Dependency Removed*/
    public List saveSalesReturnJson(JSONObject paramJobj) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        List returnList = new ArrayList();
        SalesReturn salesReturn = null;
        String creditNoteNumber = "";
        String creditNoteId = "";
        String oldjeid = "";
        String jeid = "";
        String linkedDocuments = "";
        String unlinkMessage = "";
        String paymentID="";
        String paymentNumber="";
         HashMap<String, Object> linkRequestParams = new HashMap<String, Object>();
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String currencyid = (paramJobj.optString(Constants.currencyKey,null) == null ? paramJobj.getString(Constants.globalCurrencyKey): paramJobj.getString(Constants.currencyKey));
            String entryNumber = paramJobj.optString("number",null);
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString(Constants.externalcurrencyrate,"") != "" ? paramJobj.getString(Constants.externalcurrencyrate) : "0.0");
            String costCenterId = paramJobj.optString(Constants.costcenter,null);
            String salesPersonID = !StringUtil.isNullOrEmpty(paramJobj.optString("salesPerson",null)) ? paramJobj.getString("salesPerson") : "";
            boolean assignSRnumbertocn= !StringUtil.isNullOrEmpty(paramJobj.optString("AssignSRNumberntocn",null)) && !paramJobj.optString("AssignSRNumberntocn",null).equals("false")?true:false;
            if (assignSRnumbertocn) {
                creditNoteNumber = entryNumber;
            } else {
                creditNoteNumber = StringUtil.isNullOrEmpty(paramJobj.optString("cndnnumber",null)) ? "" : paramJobj.getString("cndnnumber");
            }
            paymentNumber= StringUtil.isNullOrEmpty(paramJobj.optString("paymentNumber",null)) ? "" : paramJobj.getString("paymentNumber");
            String creditNoteSequenceFormat = StringUtil.isNullOrEmpty(paramJobj.optString("cndnsequenceformat",null)) ? "NA" : paramJobj.getString("cndnsequenceformat");
            String paymentSequenceFormat=StringUtil.isNullOrEmpty(paramJobj.optString("paymentSequenceFormat",null))?"NA":paramJobj.getString("paymentSequenceFormat");
            boolean isNoteAlso = false;
            /*
             * isPayment is true while payment is creating from sales return
             */ 
            boolean isPayment = false;
            boolean isInsertAuditTrail = false;   // This flag is added for showing Credit No in Audit Trial ERP-18558
            boolean isnegativestockforlocwar = false;

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteAlso",null))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
                isNoteAlso = Boolean.parseBoolean(paramJobj.getString("isNoteAlso"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isPayment",null))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
                isPayment = Boolean.parseBoolean(paramJobj.getString("isPayment"));
            }

            String srid = null;

            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                srid = paramJobj.optString(Constants.billid, null);
            } else {
                srid = paramJobj.optString("srid", null);
            }

            String isfavourite = paramJobj.optString("isfavourite",null);
            String sequenceformat = paramJobj.optString(Constants.sequenceformat,null);
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isFixedAsset)) : false;
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isLeaseFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isLeaseFixedAsset)) : false;
            boolean isConsignment = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isConsignment,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isConsignment)) : false;
            boolean RCMApplicable = paramJobj.optBoolean("GTAApplicable",false);//ERP-34970(ERM-534)
            boolean EWAYApplicable = paramJobj.optBoolean("EWAYApplicable",false);
            long createdon = System.currentTimeMillis();
            boolean gstIncluded = (!StringUtil.isNullOrEmpty(paramJobj.optString("includingGST", ""))) ? Boolean.parseBoolean(paramJobj.getString("includingGST")) : false;
            boolean isApplyTaxToTerm = (!StringUtil.isNullOrEmpty(paramJobj.optString("isApplyTaxToTerms", ""))) ? Boolean.parseBoolean(paramJobj.getString("isApplyTaxToTerms")) : false;
            String createdby = paramJobj.getString(Constants.useridKey);
            String modifiedby = paramJobj.getString(Constants.useridKey);
            long updatedon = createdon;
            String custWarehouse = paramJobj.optString("custWarehouse",null);
            String movementtype = paramJobj.optString("movementtype",null);
            String deletedLinkedDocumentID = paramJobj.optString("deletedLinkedDocumentId",null);
            HashMap<String, Object> doDataMap = new HashMap<String, Object>();

            KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmp.getEntityList().get(0);

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
            extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            isnegativestockforlocwar = extraCompanyPreferences.isIsnegativestockforlocwar();
            int countryid = 0;
            if(extraCompanyPreferences != null && extraCompanyPreferences.getCompany().getCountry() != null){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            synchronized (this) {
                if (!StringUtil.isNullOrEmpty(srid)) {//Edit case
                    // in case of edition of Lease Sales Return you need handle assetdetails table data, and need to 
                    // delete entry from assetdetailsinvdetailmapping table
                    if (isLeaseFixedAsset) {
                        HashMap<String, Object> deleteParams = new HashMap<String, Object>();
                        deleteParams.put("srid", srid);
                        deleteParams.put(Constants.companyKey, companyid);
                        accInvoiceDAOobj.deleteAssetDetailsLinkedWithSalesReturn(deleteParams);
                    }

                    if (isFixedAsset) {
                        HashMap<String, Object> deleteParams = new HashMap<String, Object>();
                        deleteParams.put("srid", srid);
                        deleteParams.put(Constants.companyKey, companyid);
                        accInvoiceDAOobj.deleteAssetDetailsLinkedWithAssetSalesReturn(deleteParams);
                    }
                    HashMap<String, Object> requestParamsForDeleteBatch = new HashMap<String, Object>();
                    requestParamsForDeleteBatch.put("srid", srid);
                    requestParamsForDeleteBatch.put(Constants.companyKey, companyid);
                    requestParamsForDeleteBatch.put(Constants.isConsignment, isConsignment);
                    requestParamsForDeleteBatch.put("isnegativestockforlocwar", isnegativestockforlocwar);

                    doDataMap.put(Constants.Acc_id, srid);

                    KwlReturnObject result = accInvoiceDAOobj.getSalesReturnInventory(srid);
                    KwlReturnObject resultBatch = accInvoiceDAOobj.getSalesReturnBatches(srid, companyid);
                    if (!isConsignment && extraCompanyPreferences.isSalesorderreopen()) {
                        accInvoiceDAOobj.updateSOBalanceQtyAfterSR(srid, companyid);
                    }
                    accInvoiceDAOobj.deleteSalesReturnsBatchSerialDetails(requestParamsForDeleteBatch); //dlete serial no and mapping
                    stockMovementService.removeStockMovementByReferenceId(company, srid);

                    if (extraCompanyPreferences.isAvalaraIntegration()) {//In case of edit, if Avalara Integration is enabled, delete tax mapping from table 'TransactionDetailAvalaraTaxMapping'
                        KwlReturnObject tempResult = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), srid);
                        SalesReturn sr = (SalesReturn) tempResult.getEntityList().get(0);
                        deleteAvalaraTaxMappingForSR(sr.getRows());
                    }
                    
                    accInvoiceDAOobj.deleteSalesReturnDetails(srid, companyid);
                    List<String> list = result.getEntityList();
                    for (String inventoryid :list) {
                        accProductObj.deleteInventory(inventoryid, companyid);
                    }
                    List<String> listBatch = resultBatch.getEntityList();
                    for (String batchid: listBatch) {
                        if (!StringUtil.isNullOrEmpty(batchid)) {
                            accCommonTablesDAO.deleteBatches(batchid, companyid);
                        }
                    }
                    
                    // Delete Sales Return Term Map
                    HashMap<String, Object> termReqMap = new HashMap<String, Object>();
                    termReqMap.put("salesreturnid", srid);
                    accInvoiceDAOobj.deleteSalesReturnTermMap(termReqMap);
                   
                    linkRequestParams.put("srid", srid);
                   
                    /*
                     * Updating Isopen Flag=0 of invoice & Delivery Order during
                     * Editing SR
                     */
                    if (!StringUtil.isNullOrEmpty(deletedLinkedDocumentID)) {
                        String[] deletedLinkedDocumentIDArr = deletedLinkedDocumentID.split(",");

                        for (int i = 0; i < deletedLinkedDocumentIDArr.length; i++) {
                            KwlReturnObject venresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), deletedLinkedDocumentIDArr[i]);
                            Invoice invoice = (Invoice) venresult.getEntityList().get(0);
                            if (invoice != null) {
                                linkRequestParams.put("isOpenSR", true);
                                linkRequestParams.put("invoice", invoice);
                                accInvoiceDAOobj.updateInvoiceLinkflag(linkRequestParams);
                                if (i == 0) {
                                    unlinkMessage += " from the Sales Invoice(s) ";
                                }
                                if (unlinkMessage.indexOf(invoice.getInvoiceNumber()) == -1) {
                                    unlinkMessage += invoice.getInvoiceNumber() + ", ";
                                }
                            } else {
                                venresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), deletedLinkedDocumentIDArr[i]);
                                DeliveryOrder deliveryOrder = (DeliveryOrder) venresult.getEntityList().get(0);
                                if (deliveryOrder != null) {
                                    linkRequestParams.put("isFromSalesReturn", true);
                                    linkRequestParams.put("isOpen", true);
                                    linkRequestParams.put("deliveryOrder", deliveryOrder);
                                    accInvoiceDAOobj.updateDeliveryOrderStatus(linkRequestParams);
                                    if (i == 0) {
                                        unlinkMessage += " from the Delivery Order(s) ";
                                    }
                                    if (unlinkMessage.indexOf(deliveryOrder.getDeliveryOrderNumber()) == -1) {
                                        unlinkMessage += deliveryOrder.getDeliveryOrderNumber() + ", ";
                                    }
                                }
                            }
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(unlinkMessage) && unlinkMessage.endsWith(", ")) {
                        unlinkMessage = unlinkMessage.substring(0, unlinkMessage.length() - 2);
                    }

                }
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not 
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Sales_Return_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException("Entered document number " + " <b>" + entryNumber + "</b> " + " is belongs to Auto Sequence Format " + " <b>" + formatName + "</b>. " + "Please select Sequence Format " + " <b>" + formatName + "</b> " + " instead of entering document number manually.");
                        }
                    }
                }
            }
            
            double totalTermAmount = 0;
            double totalTermTaxAmount = 0;
            String InvoiceTerms = paramJobj.optString("invoicetermsmap");
            if (!StringUtil.isNullOrEmpty(InvoiceTerms)) {
                JSONArray termsArr = new JSONArray(InvoiceTerms);
                for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                    double termamount = 0;
                    if (gstIncluded) {
                        termamount = termsArr.getJSONObject(cnt).optDouble("termAmountExcludingTax",0);
                    } else {
                        termamount = termsArr.getJSONObject(cnt).optDouble("termamount",0);
                    }
                    totalTermAmount += termamount;
                    double termTaxAmount = termsArr.getJSONObject(cnt).optDouble("termtaxamount", 0);
                    totalTermTaxAmount += termTaxAmount;
                }
            }

            DateFormat df = authHandler.getDateOnlyFormat();
            if (sequenceformat.equals("NA") || !StringUtil.isNullOrEmpty(srid)) {
                doDataMap.put("entrynumber", entryNumber);
            } else {
                doDataMap.put("entrynumber", "");
            }
//            String taxid = paramJobj.optString("taxid",null);//    ERP-28612
            String taxid = "";
            if (paramJobj.optString("taxid").equalsIgnoreCase("None")) {
                taxid = null;
            } else {
                taxid = paramJobj.optString("taxid",null);
            }
            doDataMap.put("gstIncluded", gstIncluded);
            doDataMap.put("isApplyTaxToTerms", isApplyTaxToTerm);
            doDataMap.put("taxid", taxid);
            doDataMap.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            doDataMap.put(Constants.memo, paramJobj.optString(Constants.memo,""));
            doDataMap.put("externalCurrencyRate", externalCurrencyRate);
            doDataMap.put(Constants.isLeaseFixedAsset, isLeaseFixedAsset);
            doDataMap.put(Constants.isFixedAsset, isFixedAsset);
            doDataMap.put(Constants.posttext, paramJobj.optString(Constants.posttext,null) == null ? "" : paramJobj.getString(Constants.posttext));
            doDataMap.put("customerid", paramJobj.optString("customer",null));
            if (paramJobj.optString(Constants.shipdate,null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.shipdate,null))) {
                doDataMap.put(Constants.shipdate, df.parse(paramJobj.getString(Constants.shipdate)));
            }
            doDataMap.put(Constants.shipvia, paramJobj.optString(Constants.shipvia,null));
            doDataMap.put(Constants.fob, paramJobj.optString(Constants.fob,null));
            doDataMap.put("orderdate", df.parse(paramJobj.optString(Constants.BillDate,null)));
            doDataMap.put(Constants.Checklocktransactiondate, paramJobj.optString(Constants.BillDate,null));//ERP-16800-Without parsing date

            doDataMap.put("isfavourite", isfavourite);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                doDataMap.put("costCenterId", costCenterId);
            }
            doDataMap.put(Constants.companyKey, companyid);
            doDataMap.put(Constants.currencyKey, currencyid);

            doDataMap.put(Constants.isConsignment, isConsignment);
            doDataMap.put("isNoteAlso", isNoteAlso);
            doDataMap.put("isPayment", isPayment);
            doDataMap.put("salesPerson", salesPersonID);
            doDataMap.put("isAssignSRNumberntocn", assignSRnumbertocn);
            if (!StringUtil.isNullOrEmpty(custWarehouse)) {
                doDataMap.put("custWarehouse", custWarehouse);
            }
            if (!StringUtil.isNullOrEmpty(movementtype)) {
                doDataMap.put("movementtype", movementtype);
            }
            if (paramJobj.optString("contractid",null) == null) {
                String linkMode = paramJobj.optString("fromLinkCombo","");
                String linkNumber = paramJobj.optString("linkNumber","");
                Contract contract = null;
                if (!StringUtil.isNullOrEmpty(linkMode)) {
                    if (linkMode.equalsIgnoreCase(Constants.Delivery_Order) || linkMode.equalsIgnoreCase("Asset Delivery Order") || linkMode.equalsIgnoreCase("Lease Delivery Order")) {

                        String[] linkNumbers = paramJobj.optString("linkNumber","").split(",");

                        for (int i = 0; i < linkNumbers.length; i++) {
                            linkNumber = linkNumbers[i];
                            if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), linkNumbers[i]);
                                if (rdresult.getEntityList() != null && !rdresult.getEntityList().isEmpty()) {
                                    DeliveryOrder deliveryOrder = (DeliveryOrder) rdresult.getEntityList().get(0);

                                    Set<DOContractMapping> dOContractMappings = deliveryOrder.getdOContractMappings();
                                    if (dOContractMappings != null && !dOContractMappings.isEmpty()) {

                                        for (DOContractMapping contractMapping : dOContractMappings) {
                                            contract = contractMapping.getContract();
                                        }
                                    }
                                }
                            }
                        }
                    } else if (linkMode.equalsIgnoreCase("Customer Invoice")) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkNumber);
                        if (rdresult.getEntityList() != null && !rdresult.getEntityList().isEmpty()) {
                            Invoice invoice = (Invoice) rdresult.getEntityList().get(0);

                            Set<InvoiceContractMapping> invContractMappings = invoice.getContractMappings();
                            if (invContractMappings != null && !invContractMappings.isEmpty()) {
                                for (InvoiceContractMapping contractMapping : invContractMappings) {
                                    contract = contractMapping.getContract();
                                }
                            }
                        }
                    }
                }
                doDataMap.put("contractid", contract != null ? contract.getID() : "");
            } else {
                doDataMap.put("contractid", paramJobj.optString("contractid",null));
            }

            doDataMap.put("createdon", createdon);
            doDataMap.put("createdby", createdby);
            doDataMap.put("modifiedby", modifiedby);
            doDataMap.put("updatedon", updatedon);
            if (extraCompanyPreferences.isIsNewGST()) {
                /**
                 * ERP-32829
                 */
                doDataMap.put("gstapplicable", paramJobj.optBoolean("GSTApplicable"));
                doDataMap.put(Constants.RCMApplicable, RCMApplicable);//ERP-34970(ERM-534)
            }
            if (countryid == Constants.indian_country_id && paramJobj.has("formtypeid") && paramJobj.optString("formtypeid",null) != null) {
                doDataMap.put("formtype", (String) paramJobj.get("formtypeid"));
                doDataMap.put(Constants.MVATTRANSACTIONNO, (paramJobj.has(Constants.MVATTRANSACTIONNO) && paramJobj.get(Constants.MVATTRANSACTIONNO)!=null)?(String) paramJobj.get(Constants.MVATTRANSACTIONNO):"");
            }
                doDataMap.put(Constants.EWAYApplicable, EWAYApplicable);
            double totalAmt = 0, totalRowDiscount = 0, totalAmountinbase = 0, subTotal = 0, taxAmt = 0;
            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.detail,"[]"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                double qrate = authHandler.roundUnitPrice(jobj.getDouble("rate"), companyid);
                double quantity = authHandler.roundQuantity(jobj.getDouble("quantity"), companyid);
                double quotationPrice = authHandler.round(quantity * qrate, companyid);
                double discountQD = authHandler.round(jobj.optDouble("prdiscount", 0), companyid);
                double discountPerRow = 0;
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    if (jobj.optInt("discountType", 0) == 1) { // percent discount
                        discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                    } else { //flat discount
                        discountPerRow = discountQD;
                    }
                } else {
                    if (jobj.optInt("discountispercent", 0) == 1) { // percent discount
                        discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                    } else { //flat discount
                        discountPerRow = discountQD;
                    }
                }
                

                totalRowDiscount += discountPerRow;
                /*
                 * In case of Asset ,Lease & Consignment Module getting amount
                 * calcuted with discount & tax included
                 */
                if (isFixedAsset || isConsignment || isLeaseFixedAsset) {
                    subTotal += Double.parseDouble((String) jobj.get("amount"));
                }
                /*
                 * If line level tax applied then it will execute only in
                 * dashbord transaction
                 */
                if (!isFixedAsset && !isConsignment && !isLeaseFixedAsset) {
                    if (jobj.optDouble("taxamount",0) != 0) {
                        taxAmt += jobj.optDouble("taxamount",0.0);
                    }
                }

            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("subTotal",null))) {
                subTotal = Double.parseDouble(paramJobj.getString("subTotal"));
            }
            /*
             * if global level tax applied it will execute only for dashbord
             * transaction
             */
            if (taxAmt == 0) {
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxamount",null))) {
                    taxAmt = Double.parseDouble(paramJobj.getString("taxamount"));
                    if(!StringUtil.isNullOrEmpty(taxid)){
                        taxAmt -= totalTermTaxAmount;
                    }
                }
            }
            totalAmt = subTotal + taxAmt + totalTermTaxAmount + totalTermAmount ;
            totalRowDiscount = authHandler.round(totalRowDiscount, companyid);
            doDataMap.put("totallineleveldiscount", totalRowDiscount);
            doDataMap.put("totalamount", totalAmt);
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filterRequestParams.put(Constants.companyKey, companyid);
            filterRequestParams.put(Constants.globalCurrencyKey, paramJobj.getString(Constants.globalCurrencyKey));
            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalAmt, currencyid, df.parse(paramJobj.optString(Constants.BillDate,null)), externalCurrencyRate);
            totalAmountinbase = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
            doDataMap.put("totalamountinbase", totalAmountinbase);

            KwlReturnObject descbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalRowDiscount, currencyid, df.parse(paramJobj.optString(Constants.BillDate,null)), externalCurrencyRate);
            double descountinBase = authHandler.round((Double) descbAmtTax.getEntityList().get(0), companyid);
            doDataMap.put("discountinbase", descountinBase);
            doDataMap.put(Constants.generatedSource, (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.generatedSource,null))) ?Integer.parseInt(paramJobj.optString(Constants.generatedSource,Constants.RECORD_WEB_Application)):null);
            KwlReturnObject doresult = accInvoiceDAOobj.saveSalesReturn(doDataMap);
            salesReturn = (SalesReturn) doresult.getEntityList().get(0);

            doDataMap.put(Constants.Acc_id, salesReturn.getID());
            /**
             * Save GST History Customer/Vendor data.
             */
            if (salesReturn.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                paramJobj.put("docid", salesReturn.getID());
                paramJobj.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(paramJobj);
            }
            String jeentryNumber = "";
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            String addressID = "";
            boolean isEditNote = false;
            String customerId = paramJobj.optString("customer",null);
            KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerId);
            Customer customer = (Customer) customerresult.getEntityList().get(0);

            Date creationDate = df.parse(paramJobj.optString(Constants.BillDate,null));
            CreditNote creditnote = null;
            KwlReturnObject crResult;
            HashSet<JournalEntryDetail> jedetails = new HashSet();
            JournalEntry journalEntry = null;
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
            KwlReturnObject jeresult = null;
            HashMap<String, Object> credithm = new HashMap<String, Object>();
            Map<String, Object> SIAmountsmap = new HashMap<>();
            if (isNoteAlso) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
                JournalEntry jetemp = null;
                if (!StringUtil.isNullOrEmpty(srid)) {  //check if its sales return edit case 
                    KwlReturnObject idresult = accCreditNoteDAOobj.getCreditNoteIdFromSRId(srid, companyid);
                    if (!(idresult.getEntityList().isEmpty())) {
                        creditnote = (CreditNote) idresult.getEntityList().get(0);
                    }
                    isEditNote = true;
                    if (creditnote != null) {

                        /**
                         * check the Invoices linked to Sales return.
                         */
                        KwlReturnObject SRLObj = accCreditNoteDAOobj.getinvoicesLinkedWithSR(salesReturn.getID());
                        // Check whether this note is linked with any invoice or not if linked then throw exception

                        KwlReturnObject cnObj = accCreditNoteDAOobj.getCNLinkedWithCustomerInvoice(creditnote.getID(), companyid);

                        /**
                         * Function to allow edit the SR with CN document or not
                         */
                        HashMap<String,Object> SRwithCNparams = new HashMap<>();
                        SRwithCNparams.put("SRLObj", SRLObj);
                        SRwithCNparams.put("cnObj", cnObj);
                        boolean isEditSRwithCN = isEditSRwithCNDocument(SRwithCNparams);
                        
                        if (!isEditSRwithCN) {
                            throw new AccountingException(messageSource.getMessage("acc.module.name.12", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)))+" (" +creditnote.getCreditNoteNumber()+ ") "+messageSource.getMessage("acc.salesreturn.cannoteditsrwithcn", null,Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                        /*
                         * Deleting Linking information of Sales Return during
                         * Editing Sales Return
                         */
                        accInvoiceDAOobj.deleteLinkingInformationOfSR(linkRequestParams);
                        /**
                         * Get original amounts of invoices before deletion of cndetails.
                         */
                        Set<CreditNoteDetail> creditNoteDetails = creditnote.getRows();
                        for (CreditNoteDetail detail : creditNoteDetails) {
                            if (detail.getInvoice() != null) {
                                SIAmountsmap.put(detail.getInvoice().getID(), detail.getDiscount().getOriginalAmount());
                            }
                        }

//                        if (cnObj.getEntityList() != null && !cnObj.getEntityList().isEmpty()) {
//                            CreditNoteDetail noteDetail = (CreditNoteDetail) cnObj.getEntityList().get(0);
//                            throw new AccountingException("Credit Note (" + noteDetail.getCreditNote().getCreditNoteNumber() + ") created from this Sales Return is linked with Sales Invoice(s). so it cannot be edited. please delete this Sales Return and create new one");
//                        }
                        /*
                         Check Credit Note link to Payment or not if it is linked then throw exception
                         */
                        cnObj = accCreditNoteDAOobj.getCNLinkedWithPayment(creditnote.getID(), companyid);

                        if (cnObj.getEntityList() != null && !cnObj.getEntityList().isEmpty()) {
                            CreditNotePaymentDetails creditNotePaymentDetails = (CreditNotePaymentDetails) cnObj.getEntityList().get(0);
                            throw new AccountingException("Credit Note (" + creditNotePaymentDetails.getCreditnote().getCreditNoteNumber() + ") created from this Sales Return is linked with Payment. so it cannot be edited.");
                        }

                        oldjeid = creditnote.getJournalEntry().getID();
                        jetemp = creditnote.getJournalEntry();
                        addressID = creditnote.getBillingShippingAddresses() != null ? creditnote.getBillingShippingAddresses().getID() : "";
                        if (creditnote.getCnTaxEntryDetails() != null && !creditnote.getCnTaxEntryDetails().isEmpty()) {
                            String ids = "";
                            for (CreditNoteTaxEntry noteTaxEntry : creditnote.getCnTaxEntryDetails()) {
                                ids += "'" + noteTaxEntry.getID() + "',";
                            }
                            if(!StringUtil.isNullOrEmpty(ids)){
                                accCreditNoteDAOobj.deleteCreditNoteDetailTermMap(ids.substring(0, ids.length() - 1));
                            }
                        }
                    }
                    if (jetemp != null) {  //in edit case get all the detail
                        jeentryNumber = jetemp.getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                        jeautogenflag = jetemp.isAutoGenerated();
                        jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                        jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    }
                     
                    if (creditnote != null) {
                        accCreditNoteService.updateOpeningInvoiceAmountDue(creditnote.getID(), companyid);
                    }
                    crResult = accCreditNoteDAOobj.deleteCreditNoteDetails(creditnote.getID(), companyid);
                    crResult = accCreditNoteDAOobj.deleteCreditTaxDetails(creditnote.getID(), companyid);

                    //Delete old entries and insert new entries again from optimized table in edit case.
                    accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
                    deleteJEDetailsCustomData(oldjeid);
                    credithm.put("cnid", creditnote.getID());
                }

                if (StringUtil.isNullOrEmpty(oldjeid)) {  //in create new case 
                    synchronized (this) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put(Constants.companyKey, companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false,creationDate);
                        jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        jeSeqFormatId = format.getID();
                        jeautogenflag = true;
                    }
                }

                jeDataMap.put("entrynumber", jeentryNumber);
                jeDataMap.put("autogenerated", jeautogenflag);
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                jeDataMap.put("entrydate", creationDate);
                jeDataMap.put(Constants.companyKey, companyid);
                jeDataMap.put(Constants.memo, "Credit Note Created for Sales Return " + entryNumber);
                jeDataMap.put(Constants.currencyKey, currencyid);

                jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
                journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                jeid = journalEntry.getID();
                jeDataMap.put("jeid", jeid);
            }
            // Post JE if MRP module is activated
            Set<JournalEntryDetail> inventoryJEDetails = new HashSet<>();
            String inventoryJEid = "";
            JournalEntry inventoryJE = null;
            boolean postInventoryJournalEntry = false;
            /*
             * Check if there is any non-inventory present or not, as Inventory Journal Entry should not be posted for non-inventory products.
             */
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString(Constants.productid))) {
                    KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString(Constants.productid));
                    Product product = (Product) proresult.getEntityList().get(0);
                    /**
                     * Added (!product.isAsset) -: Asset SR - JE should not be posted ERP-38879.
                     */
                    if (product != null && product.getProducttype() != null && !(product.getProducttype().getID().equals(Producttype.SERVICE) || product.getProducttype().getID().equals(Producttype.NON_INVENTORY_PART) || product.getProducttype().getID().equals(Producttype.Inventory_Non_Sales)) && !product.isAsset()) {
                        postInventoryJournalEntry = true;
                        break;
                    }
                }
            }
            try {
                if (extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) && postInventoryJournalEntry) {
                    String oldjeid1 = null;
                    String jeentryNumber1 = null;
                    boolean jeautogenflag1 = false;
                    String jeIntegerPart1 = "";
                    String jeDatePrefix1 = "";
                    String jeDateAfterPrefix1 = "";
                    String jeDateSuffix1 = "";
                    String jeSeqFormatId1 = "";
                    if (salesReturn != null && salesReturn.getInventoryJE() != null) {
                        jeentryNumber1 = salesReturn.getInventoryJE().getEntryNumber(); //preserving these data to generate same JE number in edit case                    
                        jeautogenflag1 = salesReturn.getInventoryJE().isAutoGenerated();
                        jeSeqFormatId1 = salesReturn.getInventoryJE().getSeqformat() == null ? "" : salesReturn.getInventoryJE().getSeqformat().getID();
                        jeIntegerPart1 = String.valueOf(salesReturn.getInventoryJE().getSeqnumber());
                        jeDatePrefix1 = salesReturn.getInventoryJE().getDatePreffixValue();
                        jeDateAfterPrefix1 = salesReturn.getInventoryJE().getDateAfterPreffixValue();
                        jeDateSuffix1 = salesReturn.getInventoryJE().getDateSuffixValue();
                        oldjeid1 = salesReturn.getInventoryJE().getID();
                        salesReturn.setInventoryJE(null);
                        accInvoiceDAOobj.updateSalesReturnInventoryJESetNull(salesReturn);
                        accJournalEntryobj.deleteJournalEntryPermanent(oldjeid1, companyid);
                    }
                    Map<String, Object> jeDataMap1 = AccountingManager.getGlobalParamsJson(paramJobj);
                    if (StringUtil.isNullOrEmpty(oldjeid1)) {
                        jeDataMap1.put("entrynumber", "");
                        jeDataMap1.put("autogenerated", true);
                    } else {
                        jeDataMap1.put("entrynumber", jeentryNumber1);
                        jeDataMap1.put(Constants.SEQFORMAT, jeSeqFormatId1);
                        jeDataMap1.put(Constants.SEQNUMBER, jeIntegerPart1);
                        jeDataMap1.put(Constants.DATEPREFIX, jeDatePrefix1);
                        jeDataMap1.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix1);
                        jeDataMap1.put(Constants.DATESUFFIX, jeDateSuffix1);
                        jeDataMap1.put("autogenerated", jeautogenflag1);
                    }
                    jeDataMap1.put("entrydate", creationDate);
                    jeDataMap1.put(Constants.companyKey, companyid);
                    jeDataMap1.put("createdby", createdby);
                    jeDataMap1.put(Constants.memo, paramJobj.optString(Constants.memo,""));
                    jeDataMap1.put(Constants.currencyKey, currencyid);
                    jeDataMap1.put("costcenterid", costCenterId);
                    jeDataMap1.put("transactionModuleid", Constants.Acc_Sales_Return_ModuleId);
                    jeDataMap1.put("transactionId", salesReturn.getID());
                    jeDataMap1.put(JournalEntryConstants.EXTERNALCURRENCYRATE, salesReturn.getExternalCurrencyRate());
                    KwlReturnObject jeresult1 = accJournalEntryobj.saveJournalEntry(jeDataMap1);
                    inventoryJE = (JournalEntry) jeresult1.getEntityList().get(0);
                    inventoryJEid = inventoryJE.getID();
                    salesReturn.setInventoryJE(inventoryJE);
                }
            } catch (Exception ex) {
                Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
            }
            /*
             * Saving Sales Returns Product Information
             */
//            if (countryid == Constants.indian_country_id && customer != null && customer.getGSTRegistrationType() != null) {
//                MasterItem gstRegistrationType = customer.getGSTRegistrationType();
//                if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null && !StringUtil.isNullOrEmpty(gstRegistrationType.getDefaultMasterItem().getID())) {
//                    paramJobj.put("isUnRegisteredDealer", gstRegistrationType.getDefaultMasterItem().getID().equals(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)));;
//                }
//            }
            List dodetails = saveSalesReturnRows(paramJobj, salesReturn, companyid, journalEntry, inventoryJEDetails, inventoryJEid);
            if (inventoryJE != null && inventoryJEDetails != null && extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                inventoryJE.setDetails(inventoryJEDetails);
                accJournalEntryobj.saveJournalEntryDetailsSet(inventoryJEDetails);
            }
            Set<SalesReturnDetail> SRD = salesReturn.getRows();
            for (SalesReturnDetail srd: SRD) {
                if (srd.getCidetails() != null) {
                    HashMap hmap = new HashMap();
                    InvoiceDetail invoiced = (InvoiceDetail) srd.getCidetails();
                    Invoice invoice = (Invoice) invoiced.getInvoice();
                    hmap.put("invoice", invoice);
                    if (StringUtil.equal(getInvoiceStatusForSalesReturn(invoice), "Completely Returned")) {//Completely Returned
                        hmap.put("isOpenSR", false);
                    } else {
                        hmap.put("isOpenSR", true);
                    }
                    accInvoiceDAOobj.updateInvoiceLinkflag(hmap);
                }
            }

            KwlReturnObject creditnoteresult = null;
            if (isNoteAlso) {
                if (StringUtil.isNullOrEmpty(srid)) {// create new case
                    synchronized (this) {
                        if (creditNoteSequenceFormat.equalsIgnoreCase("NA")) { //create new case with sequence format NA
                            credithm.put("autogenerated", false);
                        } else { //create new case with sequence format other than NA
                            credithm.put("autogenerated", true);
                        }
                    }
                } else {//edit case
                    if (creditNoteSequenceFormat.equalsIgnoreCase("NA")) { //Edit case with sequence format NA
                        creditnoteresult = accCreditNoteDAOobj.getCNFromNoteNoAndId(creditNoteNumber, companyid, creditnote.getID());//checks for duplicate number
                        if (creditnoteresult.getRecordTotalCount() > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.Creditnotenumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + creditNoteNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                    }
                }

                if (creditNoteSequenceFormat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Credit_Note_ModuleId, creditNoteNumber, companyid);
                    if (!resultList.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                        String formatName = (String) resultList.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + creditNoteNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                        }
                    }
                }
                HashSet<CreditNoteDetail> cndetails = (HashSet) dodetails.get(1);
                HashSet<CreditNoteTaxEntry> creditNoteTaxEntryDetails = (HashSet<CreditNoteTaxEntry>) dodetails.get(3);
                jedetails = (HashSet) dodetails.get(2);

                double totalCNAmt = (Double) dodetails.get(4);
                double totalCNAmtExludingTax = (Double) dodetails.get(5);
                double totalDiscountAmt = (Double) dodetails.get(6);
                double sumOfDiscountAmount = (Double) dodetails.get(7);

                // Save Credit Note Here
                credithm.put("journalentryid", jeid);
                if (creditNoteSequenceFormat.equals("NA") || !StringUtil.isNullOrEmpty(srid)) {
                    credithm.put("entrynumber", creditNoteNumber);
                } else {
                    credithm.put("entrynumber", "");
                }

                credithm.put("oldRecord", false);

                Integer seqNumber = null;
                KwlReturnObject result = null;
                if (!creditNoteSequenceFormat.equals("NA") && !StringUtil.isNullOrEmpty(creditNoteSequenceFormat)) {
                    result = accCreditNoteDAOobj.getCNSequenceNofromsequenceformat(companyid,creditNoteSequenceFormat);
                    List seqlist = result.getEntityList();
                    if (seqlist != null && !seqlist.isEmpty() && seqlist.get(0) != null) {
                        seqNumber = (int) seqlist.get(0);
                    }
                    credithm.put("sequence", seqNumber);
                    credithm.put(Constants.SEQFORMAT, creditNoteSequenceFormat);
                    credithm.put(Constants.SEQNUMBER, seqNumber);
                }

                //saving default address of customer to CN
                Map<String, Object> addressParams = Collections.EMPTY_MAP;
                addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(customerId, companyid, accountingHandlerDAOobj);
                /**
                 * While creating Sales Return With Credit Note get address
                 * details from linked invoice if any invoice linked
                 */
                String linkMode = paramJobj.optString("fromLinkCombo", "");
                String[] linkNumbers = paramJobj.optString("linkNumber", "").split(",");
                if (StringUtil.isNullOrEmpty(addressID) && !StringUtil.isNullOrEmpty(linkMode) && linkMode.equalsIgnoreCase("Sales Invoice")
                        && linkNumbers != null && linkNumbers.length == 1 && !StringUtil.isNullOrEmpty(linkNumbers[0])) {
                    JSONObject addressparamsJSON = new JSONObject();
                    KwlReturnObject PIresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkNumbers[0]);
                    Invoice invoice = PIresult != null ? (Invoice) PIresult.getEntityList().get(0) : null;
                    if (invoice != null) {
                        /**
                         * Get Data in new address params JSON
                         */
                        addressparamsJSON = AccountingAddressManager.getTransactionAddressJSON(addressparamsJSON, invoice.getBillingShippingAddresses(), false);
                        addressParams = AccountingAddressManager.getAddressParamsJson(addressparamsJSON, true);
                    }
                }
                /**
                 * In Edit case get address details from existing records.
                 */
                if (!StringUtil.isNullOrEmpty(addressID)) {//If Edit case then updating existing CN address
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getObject(BillingShippingAddresses.class.getName(), addressID);
                    BillingShippingAddresses billingShippingAddresses = addressResult != null ? (BillingShippingAddresses) addressResult.getEntityList().get(0) : null;
                    if (billingShippingAddresses != null) {
                        /**
                         * Get Existing address details from database in edit case
                         */
                        JSONObject addressparamsJSON = new JSONObject();
                        addressparamsJSON = AccountingAddressManager.getTransactionAddressJSON(addressparamsJSON, billingShippingAddresses, true);
                        addressParams = AccountingAddressManager.getAddressParamsJson(addressparamsJSON, true);
                    }
                    addressParams.put(Constants.Acc_id, addressID);
                }
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                credithm.put("billshipAddressid", bsa.getID());

                credithm.put(Constants.memo, "Credit Note For Sales Return " + entryNumber);
                credithm.put(Constants.companyKey, companyid);
                credithm.put(Constants.currencyKey, currencyid);
                credithm.put("createdby", createdby);
                credithm.put("modifiedby", modifiedby);
                credithm.put("createdon", createdon);
                credithm.put("updatedon", updatedon);
                credithm.put("salesreturnId", salesReturn.getID());
                credithm.put("customerid", paramJobj.optString("customer",null));
                credithm.put("otherwise", true);
                credithm.put("openflag", true);
                credithm.put("includingGST", gstIncluded);
                double cnamount = totalCNAmt;
                double cnamountdue = totalCNAmt;
                credithm.put("cnamount", cnamount);
                /*
                 * Saving the total amount in base currency
                 */
                KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, cnamount, currencyid, creationDate, externalCurrencyRate);
                double cnamountinbase = (Double) baseAmount.getEntityList().get(0);
                cnamountinbase = authHandler.round(cnamountinbase, companyid);
                credithm.put("cnamountinbase", cnamountinbase);
                credithm.put("cnamountdue", cnamountdue);
                credithm.put("approvestatuslevel", 11);
                credithm.put("salesPersonID", salesPersonID);
                /**
                 * while saving SR with CN document, CN type is changed to 2
                 * so that CN will treat like CN otherwise document.
                 */
                credithm.put("cntype", 2);
                credithm.put("accountId", customer.getAccount().getID());
                credithm.put("creationDate", creationDate);
                if (isEditNote) {
                    result = accCreditNoteDAOobj.updateCreditNote(credithm);
                } else {
                    result = accCreditNoteDAOobj.addCreditNote(credithm);
                }
                creditnote = (CreditNote) result.getEntityList().get(0);

                credithm.put("cnid", creditnote.getID());
                creditNoteId = creditnote.getID();
                for ( CreditNoteDetail cnd :cndetails) {
                    cnd.setCreditNote(creditnote);
                }
                credithm.put("cndetails", cndetails);
                double cnTaxAmount=0;
                Iterator cntaxitr = creditNoteTaxEntryDetails.iterator();
                int ind = 0;
                while (cntaxitr.hasNext()) {
                    CreditNoteTaxEntry noteTaxEntry = (CreditNoteTaxEntry) cntaxitr.next();
                    noteTaxEntry.setCreditNote(creditnote);
                    cnTaxAmount+=noteTaxEntry.getTaxamount();                 //calculated total cn tax amount
                    
                    /**
                     * Save Term details for DN in Purchase Return case
                     */
                    if (extraCompanyPreferences.isIsNewGST()) {
                        JSONArray jArrr = new JSONArray(paramJobj.optString(Constants.detail, "[]"));
                        JSONObject jobj1 = jArrr.getJSONObject(ind);
                        if (!StringUtil.isNullOrEmpty((String) jobj1.optString("LineTermdetails"))) {
                            JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj1.optString("LineTermdetails")));
                            for (int j = 0; j < termsArray.length(); j++) {
                                JSONObject termObject = termsArray.getJSONObject(j);
                                /**
                                 * Save GST Terms details in the Term table
                                 * for payment
                                 */
                                HashMap<String, Object> cnDetailsTermsMap = new HashMap<>();
                                if (termObject.has("termid")) {
                                    cnDetailsTermsMap.put("term", termObject.get("termid"));
                                }
                                if (termObject.has("termamount")) {
                                    cnDetailsTermsMap.put("termamount", termObject.get("termamount"));
                                }
                                if (termObject.has("termpercentage")) {
                                    cnDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                                }
                                if (termObject.has("purchasevalueorsalevalue")) {
                                    cnDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                                }
                                if (termObject.has("deductionorabatementpercent")) {
                                    cnDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                                }
                                if (termObject.has("assessablevalue")) {
                                    cnDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                                }
                                if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                                    cnDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                                    if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                        if (termObject.getInt("taxtype") == 0) { // If Flat
                                            cnDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                        } else { // Else Percentage
                                            cnDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                        }
                                    }
                                }
                                if (termObject.has("id")) {
                                    cnDetailsTermsMap.put("id", termObject.get("id"));
                                }
                                cnDetailsTermsMap.put("creditNoteTaxEntry", noteTaxEntry.getID());
                                cnDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                                cnDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                                cnDetailsTermsMap.put("userid", creditnote.getCreatedby().getUserID());
                                cnDetailsTermsMap.put("product", termObject.opt("productid"));
                                cnDetailsTermsMap.put("createdOn", new Date());
                                accCreditNoteDAOobj.saveCreditNoteDetailTermMap(cnDetailsTermsMap);
                            }
                        }
                    }
                    ind++;
                }
                cnTaxAmount=authHandler.round(cnTaxAmount, companyid);
                credithm.put("creditNoteTaxEntryDetails", creditNoteTaxEntryDetails);
                double srTaxAmount=0;                      //calculated total cn tax amount
                if (salesReturn.getTax() != null) {
                    double taxPercent = 0;
                    KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid, salesReturn.getOrderDate(), salesReturn.getTax().getID());
                    taxPercent = (Double) taxresult.getEntityList().get(0);
                    srTaxAmount = (taxPercent == 0 ? 0 : authHandler.round((totalCNAmtExludingTax* taxPercent / 100), companyid));
                }else{
                    srTaxAmount=authHandler.round(taxAmt,companyid);
                }
                result = accCreditNoteDAOobj.updateCreditNote(credithm);
                creditnote = (CreditNote) result.getEntityList().get(0);
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put(Constants.companyKey, companyid);
                if (srTaxAmount != cnTaxAmount) {
                    jedjson.put("amount", srTaxAmount - cnTaxAmount < 0 ? totalCNAmt - authHandler.round(Math.abs(srTaxAmount - cnTaxAmount), companyid) : totalCNAmt + authHandler.round(srTaxAmount - cnTaxAmount, companyid));
                } else {
                    jedjson.put("amount", totalCNAmt);
                }
                jedjson.put("accountid", customer.getAccount().getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);
                credithm.put("customerentry", jed.getID());
                if (sumOfDiscountAmount > 0) {
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put(Constants.companyKey, companyid);
                    jedjson.put("amount", sumOfDiscountAmount);
                    jedjson.put("accountid", preferences.getDiscountGiven().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
                if (srTaxAmount != cnTaxAmount) {                        //if cn tax is diffrent than sr tax then add rounding diffrence jedetail
                    JSONObject jedjsonTD = new JSONObject();
                    jedjsonTD.put("srno", jedetails.size() + 1);
                    jedjsonTD.put(Constants.companyKey, companyid);
                    jedjsonTD.put("amount", srTaxAmount-cnTaxAmount<0?-(srTaxAmount-cnTaxAmount):srTaxAmount-cnTaxAmount);
                    jedjsonTD.put("accountid", preferences.getRoundingDifferenceAccount() !=null?preferences.getRoundingDifferenceAccount().getID():null);
                    jedjsonTD.put("debit", srTaxAmount-cnTaxAmount<0?false:true);
                    jedjsonTD.put("setroundingdifferencedetail", true);
                    jeDataMap.put("isroundingdfinCNtax", true);
                    jedjsonTD.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonTD);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }

                externalCurrencyRate = StringUtil.getDouble(paramJobj.optString(Constants.externalcurrencyrate, "1"));
                result = accCreditNoteDAOobj.updateCreditNote(credithm);
                jeDataMap.put("jedetails", jedetails);
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
                jeDataMap.put("transactionId", creditnote.getID());
                jeDataMap.put("transactionModuleid", Constants.Acc_Credit_Note_ModuleId);
                jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
                jeDataMap.remove("isroundingdfinCNtax");
                JournalEntry cnJE = (JournalEntry) jeresult.getEntityList().get(0);               //if cn tax is diffrent than sr tax then add rounding diffrence cn tax entry
                Set<JournalEntryDetail> cnJED = cnJE.getDetails();
                for (JournalEntryDetail jedCN : cnJED) {
                    if (jedCN.isRoundingDifferenceDetail() &&  srTaxAmount != cnTaxAmount && (jedCN.getAmount()==Math.abs(srTaxAmount - cnTaxAmount))) {
                        CreditNoteTaxEntry taxEntry = new CreditNoteTaxEntry();
                        String CreditNoteTaxID = StringUtil.generateUUID();
                        taxEntry.setID(CreditNoteTaxID);
                        taxEntry.setAccount(jedCN.getAccount());
                        taxEntry.setAmount(jedCN.getAmount());
                        taxEntry.setCompany(company);
                        taxEntry.setTax(null);
                        taxEntry.setSrNoForRow(1);
                        String reasonID=accCreditNoteService.getReasonIDByName("Rounding Difference", companyid);
                        KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), reasonID);
                        MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
                        taxEntry.setReason(reason);
                        taxEntry.setDescription("Rounding Difference");
                        taxEntry.setDebitForMultiCNDN(false);
                        taxEntry.setTotalJED(jedCN);
                        taxEntry.setIsForDetailsAccount(jedCN.isDebit());
                        taxEntry.setTaxamount(0);
                         taxEntry.setCreditNote(creditnote);
                         creditNoteTaxEntryDetails.add(taxEntry);
                         credithm.put("cnamount", totalCNAmtExludingTax+srTaxAmount);
                         credithm.put("cnamountdue", totalCNAmtExludingTax+srTaxAmount);
                        baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(filterRequestParams, totalCNAmtExludingTax+srTaxAmount, currencyid, creationDate, externalCurrencyRate);
                        cnamountinbase = (Double) baseAmount.getEntityList().get(0);
                        cnamountinbase = authHandler.round(cnamountinbase, companyid);
                        credithm.put("cnamountinbase", cnamountinbase);
                        break;
                    }
                }
                credithm.put("creditNoteTaxEntryDetails", creditNoteTaxEntryDetails);
                result = accCreditNoteDAOobj.updateCreditNote(credithm);
                // save custom field details for auto created case
                String customfield = paramJobj.optString(Constants.customfield, null);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    JSONArray jcustomarray = new JSONArray(customfield);
                    jcustomarray = fieldDataManagercntrl.getComboValueIdsForCurrentModule(jcustomarray, Constants.Acc_Credit_Note_ModuleId, companyid, 0);            // 1= for line item
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                    customrequestParams.put("modulerecid", journalEntry.getID());
                    customrequestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        jeDataMap.put("accjecustomdataref", journalEntry.getID());
                        jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                    }
                }
                //delete old je in case of edit
                deleteJEArray(oldjeid, companyid);

                if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkNumber",null))) {
                    List li = accCreditNoteService.linkCreditNotewithoutRequest(paramJobj, creditnote.getID(), isInsertAuditTrail,SIAmountsmap);
                }
            }
            
            /*
             * isPayment is true while payment is creating from sales return
             */ 
            if (isPayment) {
                JSONArray jSONArrayGL = new JSONArray();
                Payment editPaymentObject = null;
                if (!StringUtil.isNullOrEmpty(srid)) {// for edit case
                    KwlReturnObject paymentResult = accVendorPaymentobj.getPaymentIdFromSRId(salesReturn.getID(), companyid);
                    if (!paymentResult.getEntityList().isEmpty()) {
                        editPaymentObject = (Payment) paymentResult.getEntityList().get(0);
                    }
                }
                Payment payment = createPaymentObject(paramJobj, editPaymentObject, salesReturn);
                int approvalStatusLevel = 11;
                List list = new ArrayList();
                list.add(payment.getID());
                /*
                 *Saving payment method details
                 */ 
                PayDetail payDetail = getPayDetailObject(paramJobj, editPaymentObject, payment);
                payment.setPayDetail(payDetail);
                payment.setApprovestatuslevel(approvalStatusLevel);
                /*
                 * creatng  journal Entry
                 */ 
                JournalEntry journalEntryPayment = journalEntryObject(paramJobj, editPaymentObject);
                if (approvalStatusLevel == 11) {
                    journalEntryPayment.setPendingapproval(0);
                } else {
                    journalEntryPayment.setPendingapproval(1);
                }
                
                /*
                 * saving journal entry
                 */ 
                payment.setJournalEntry(journalEntryPayment);
                List<Payment> payments = new ArrayList<Payment>();
                payments.add(payment);
                accJournalEntryobj.saveJournalEntryByObject(journalEntryPayment);
                accVendorPaymentobj.savePaymentObject(payments);
                paymentID = payments.get(0).getID();
                /**
                 * Save Custom data
                 */
                String customfield = paramJobj.optString(Constants.customfield, null);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    JSONArray jcustomarray = new JSONArray(customfield);
                    jcustomarray = fieldDataManagercntrl.getComboValueIdsForCurrentModule(jcustomarray, Constants.Acc_Make_Payment_ModuleId, companyid, 0);            // 1= for line item
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                    customrequestParams.put("modulerecid", journalEntryPayment.getID());
                    customrequestParams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        journalEntryPayment.setAccBillInvCustomData((AccJECustomData) get(AccJECustomData.class, journalEntryPayment.getID()));
                        accJournalEntryobj.saveJournalEntryByObject(journalEntryPayment);
                    }
                }
                
                Set<SalesReturnDetail> salesDetails = salesReturn.getRows();
                int count = 0;
                for (SalesReturnDetail srd : salesDetails) {
                    count++;
                    JSONObject details = new JSONObject();
                    details.put("type", "4");
                    details.put("debit", "true");
                    details.put("currencysymbol", salesReturn.getCurrency().getSymbol());
                    details.put("currencyname", "");
                    details.put("currencyid", "");
                    details.put("payment", "");
                    details.put("documentno", srd.getProduct().getSalesReturnAccount().getAccountName());
                    details.put("documentid", srd.getProduct().getSalesReturnAccount().getID());
                    details.put("description", "");
                    if (srd.getTax() != null) {
                        details.put("prtaxid", srd.getTax().getID());
                    } else {
                        details.put("prtaxid", "0");
                    }
                    details.put("gsttax", customer.getAccount().getID());
                    details.put("gstTaxAmount", authHandler.round(taxAmt,companyid));
                    details.put("taxamount", srd.getRowTaxAmount());
                    double qrate = authHandler.roundUnitPrice(srd.getRate(), companyid);
                    double quantity = authHandler.roundQuantity(srd.getReturnQuantity(), companyid);
                    double quotationPrice = authHandler.round(quantity * qrate, companyid);
                    double discountQD = authHandler.round(srd.getDiscount(), companyid);
                    double discountPerRow = 0;
                    if (srd.getDiscountispercent() == 1) { // percent discount
                        discountPerRow = authHandler.round((quotationPrice * discountQD / 100), companyid);
                    } else { //flat discount
                        discountPerRow = discountQD;
                    }
                    details.put("discountAmount", discountPerRow);
                    details.put("discountAccountId", preferences.getDiscountGiven().getID());
                    details.put("amountDueOriginal", "0");
                    details.put("amountDueOriginalSaved", "0");
                    details.put("amountdue", "0");
                    details.put("tdsamount", "0");
                    details.put("enteramount", (srd.getRate() * srd.getReturnQuantity()));
                    details.put("exchangeratefortransaction", salesReturn.getExternalCurrencyRate());
                    details.put("gstCurrencyRate", "0.0");
                    details.put("srNoForRow", count);
                    details.put("modified", true);
                    jSONArrayGL.put(details);
                }
                Set<JournalEntryDetail> details = null;
                Set<PaymentDetailOtherwise> paymentDetailOtherwises = payment.getPaymentDetailOtherwises();
               
                /*
                 *deleting paymentotherwise details while editing same document 
                 */ 
                        
                if (payment.getPaymentDetailOtherwises() != null && !paymentDetailOtherwises.isEmpty()) {
                    JSONObject reqPrams = new JSONObject();
                    String ids = "";
                    for (PaymentDetailOtherwise paymentDetailOtherwise : paymentDetailOtherwises) {
                        ids += "'" + paymentDetailOtherwise.getID() + "',";
                    }
                    reqPrams.put("paymentdetailotherwiseid", ids.substring(0, ids.length() - 1));
                    accVendorPaymentobj.deleteAdvanceDetailsTerm(reqPrams);
                    accVendorPaymentobj.deletePaymentsDetailsOtherwise(payment.getID());
                }
                details = journalEntryDetailObject(paramJobj, jSONArrayGL, journalEntryPayment, payment, Constants.GLPayment);
                if (journalEntryPayment.getDetails() != null) {
                    /**
                     * Edit case of Refund
                     */
                    journalEntryPayment.getDetails().clear();
                    journalEntryPayment.getDetails().addAll(details);
                } else {
                    /**
                     * Create new case
                     */
                    journalEntryPayment.setDetails(details);
                }
                journalEntryPayment.setTransactionId(paymentID);
                journalEntryPayment.setTransactionModuleid(Constants.Acc_Make_Payment_ModuleId);
                accJournalEntryobj.saveJournalEntryDetailsSet(details);

            }

            String customfield = paramJobj.optString(Constants.customfield,null);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_SalesReturn_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_SalesReturnId);
                customrequestParams.put("modulerecid", salesReturn.getID());
                customrequestParams.put(Constants.moduleid, isConsignment ? Constants.Acc_ConsignmentSalesReturn_ModuleId : isFixedAsset ? Constants.Acc_FixedAssets_Sales_Return_ModuleId : isLeaseFixedAsset ? Constants.Acc_Lease_Return : Constants.Acc_Sales_Return_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_SalesReturn_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    doDataMap.put("accsalesreturncustomdataref", salesReturn.getID());
                    KwlReturnObject accresult = accInvoiceDAOobj.updateSalesReturnCustomData(doDataMap);
                }
            }
            String linkMode = paramJobj.optString("fromLinkCombo","");
            String[] linkNumbers = paramJobj.optString("linkNumber","").split(",");
            if (linkMode.equalsIgnoreCase(Constants.Delivery_Order) || linkMode.equalsIgnoreCase("Consignment Delivery Order") || linkMode.equalsIgnoreCase("Asset Delivery Order") || linkMode.equalsIgnoreCase("Lease Delivery Order")) {
                HashSet<SalesReturnDetail> srdetails = (HashSet) dodetails.get(0);
                if (srdetails != null && !srdetails.isEmpty()) { //update balance quantity to po when creating gr link to pi and pi link to po
                    for (SalesReturnDetail cnt : srdetails) {
                        if (cnt.getDodetails() != null && cnt.getDodetails().getCidetails() != null && cnt.getDodetails().getCidetails().getSalesorderdetail() != null) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), cnt.getDodetails().getCidetails().getSalesorderdetail().getID());
                            SalesOrderDetail salesorderdetails = (SalesOrderDetail) rdresult.getEntityList().get(0);
                            HashMap soMap = new HashMap();
                            soMap.put("sodetails", salesorderdetails.getID());
                            soMap.put(Constants.companyKey, salesorderdetails.getCompany().getCompanyID());
                            soMap.put("balanceqty", cnt.getReturnQuantity());
                            soMap.put("add", true);
                            /*
                             * Updating islineitemclosed & issoclosed flag to
                             * false if DO is returned
                             */
                            cnt.getDodetails().getCidetails().getSalesorderdetail().setIsLineItemClosed(false);
                            cnt.getDodetails().getCidetails().getSalesorderdetail().getSalesOrder().setIsSOClosed(false);
                            if (!isConsignment && extraCompanyPreferences.isSalesorderreopen()) {
                                accCommonTablesDAO.updateSalesorderOrderStatus(soMap);
                            }
                            //updateOpenStatusFlagInVIForPR(cnt.getGrdetails().getVidetails().getGoodsReceipt().getID(), companyid);//status update for vi
                        }
                    }
                }
                for (int i = 0; i < linkNumbers.length; i++) {
                    if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                        updateOpenStatusFlagInDOForSR(linkNumbers[i], salesReturn.getID(),isConsignment,extraCompanyPreferences.isSalesorderreopen());

                        /*
                         * Saving linking information of Delivery Order while
                         * linking with Sales Return
                         */
                        HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                        requestParamsLinking.put("linkeddocid", salesReturn.getID());
                        requestParamsLinking.put("docid", linkNumbers[i]);
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                        requestParamsLinking.put("linkeddocno", entryNumber);
                        requestParamsLinking.put("sourceflag", 0);
                        KwlReturnObject result3 = accInvoiceDAOobj.saveDeliveryOrderLinking(requestParamsLinking);


                        /*
                         * saving linking informaion of Sales Return while
                         * linking with Delivery Order
                         */
                        requestParamsLinking.put("linkeddocid", linkNumbers[i]);
                        requestParamsLinking.put("docid", salesReturn.getID());
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), linkNumbers[i]);
                        DeliveryOrder deliveryorder = (DeliveryOrder) rdresult.getEntityList().get(0);
                        requestParamsLinking.put("linkeddocno", deliveryorder.getDeliveryOrderNumber());
                        requestParamsLinking.put("sourceflag", 1);
                        result3 = accInvoiceDAOobj.saveSalesReturnLinking(requestParamsLinking);
                        linkedDocuments += deliveryorder.getDeliveryOrderNumber() + " ,";
                    }
                }
                linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
            } else if (linkMode.equalsIgnoreCase("Sales Invoice")) {

                for (int i = 0; i < linkNumbers.length; i++) {
                    if (!StringUtil.isNullOrEmpty(linkNumbers[i])) {
                        /*
                         * Saving linking information of Sales Invoice while
                         * linking with Sales Return
                         */
                        HashMap<String, Object> requestParamsLinking = new HashMap<String, Object>();
                        requestParamsLinking.put("linkeddocid", salesReturn.getID());
                        requestParamsLinking.put("docid", linkNumbers[i]);
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                        requestParamsLinking.put("linkeddocno", entryNumber);
                        requestParamsLinking.put("sourceflag", 0);
                        KwlReturnObject result3 = accInvoiceDAOobj.saveInvoiceLinking(requestParamsLinking);


                        /*
                         * saving linking informaion of Sales Return while
                         * linking with Sales Invoice
                         */
                        requestParamsLinking.put("linkeddocid", linkNumbers[i]);
                        requestParamsLinking.put("docid", salesReturn.getID());
                        requestParamsLinking.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);

                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkNumbers[i]);
                        Invoice invoice = (Invoice) rdresult.getEntityList().get(0);
                        requestParamsLinking.put("linkeddocno", invoice.getInvoiceNumber());
                        requestParamsLinking.put("sourceflag", 1);
                        result3 = accInvoiceDAOobj.saveSalesReturnLinking(requestParamsLinking);
                        linkedDocuments += invoice.getInvoiceNumber() + " ,";
                    }
                }
                linkedDocuments = linkedDocuments.substring(0, linkedDocuments.length() - 1);
            }

            String moduleName = Constants.moduleID_NameMap.get(Acc_Sales_Return_ModuleId);
            if (isLeaseFixedAsset) {
                moduleName = Constants.Lease_Sales_RETURN;
            }
            if (isFixedAsset) {
                moduleName = Constants.Asset_Sales_RETURN;
            }
            if (isConsignment) {
                moduleName = Constants.moduleID_NameMap.get(Acc_ConsignmentSalesReturn_ModuleId);
            }
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), paramJobj.getString(Constants.companyKey));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(srid)) {
                    if (isFixedAsset && documentEmailSettings.isAssetSalesReturnGenerationMail()) {
                        sendmail = true;
                    } else if (isLeaseFixedAsset && documentEmailSettings.isLeaseReturnGenerationMail()) {
                        sendmail = true;
                    } //                    else if (isConsignment && documentEmailSettings.isConsignmentReturnGenerationMail()) {
                    //                        sendmail = true;
                    //                    } 
                    else if (documentEmailSettings.isSalesReturnGenerationMail()) {
                        sendmail = true;
                    }

                } else {
                    isEditMail = true;
                    if (isFixedAsset && documentEmailSettings.isAssetSalesReturnUpdationMail()) {
                        sendmail = true;
                    } else if (isLeaseFixedAsset && documentEmailSettings.isLeaseReturnUpdationMail()) {
                        sendmail = true;
                    } 
                    else if (documentEmailSettings.isSalesReturnUpdationMail()) {
                        sendmail = true;
                    }
                }
                if (sendmail) {           //if allow to send alert mail when option selected in companypreferences
                    String userMailId = "", userName = "",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams = AccountingManager.getEmailNotificationParamsJson(paramJobj);
                    if (requestParams.containsKey("userfullName") && requestParams.get("userfullName") != null) {
                        userName = (String) requestParams.get("userfullName");
                    }
                    if (requestParams.containsKey("usermailid") && requestParams.get("usermailid") != null) {
                        userMailId = (String) requestParams.get("usermailid");
                    }
                    if(requestParams.containsKey(Constants.useridKey)&& requestParams.get(Constants.useridKey)!=null){
                        currentUserid=(String)requestParams.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    
                    /*
                     if Edit mail option is true then get userid and Email id of document creator.
                     */
                    if (isEditMail) {
                        if (salesReturn != null && salesReturn.getCreatedby() != null) {
                            createdByEmail = salesReturn.getCreatedby().getEmailID();
                            createdById = salesReturn.getCreatedby().getUserID();
                        }
                        /*
                         if current user userid == document creator userid then don't add creator email ID in List.
                         */
                        if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                            mailIds.add(createdByEmail);
                        }
                    }
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String srNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(srNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
                if ((documentEmailSettings.isConsignmentReturnGenerationMail() && !isEditMail && isConsignment) || (documentEmailSettings.isConsignmentReturnUpdationMail() && isEditMail && isConsignment)) {
                    sendMailOnConsignmentSalesReturnCreationUpdation(companyid, salesReturn, isEditMail, documentEmailSettings,entryNumber);
                }
            }

        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveDeliveryOrder : " + ex.getMessage(), ex);
        }
        returnList.add(salesReturn);
        returnList.add(creditNoteNumber);
        returnList.add(creditNoteId);
        returnList.add(linkedDocuments);
        returnList.add(unlinkMessage);
        returnList.add(paymentNumber);
        returnList.add(paymentID);
        return returnList;
    }
    
    private void deleteAvalaraTaxMappingForSR(Set<SalesReturnDetail> srDetailSet) throws ServiceException, JSONException {
        List srDetailIDsList = new ArrayList<String>();
        for (SalesReturnDetail srd : srDetailSet) {
            srDetailIDsList.add(srd.getID());
        }
        if (!srDetailIDsList.isEmpty()) {
            //to create a comma separated string of SalesReturnDetail IDs for 'IN' subquery
            String srDetailIDsStr = org.springframework.util.StringUtils.collectionToDelimitedString(srDetailIDsList, ",", "'", "'");
            JSONObject avalaraTaxDeleteJobj = new JSONObject();
            avalaraTaxDeleteJobj.put(IntegrationConstants.parentRecordID, srDetailIDsStr);
            integrationCommonService.deleteTransactionDetailTaxMapping(avalaraTaxDeleteJobj);
        }
    }
  
      public PayDetail getPayDetailObject(JSONObject paramJobj, Payment editPaymentObject, Payment payment) throws SessionExpiredException, ServiceException, AccountingException, ParseException {
        PayDetail pdetail = null;

        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            String methodid = paramJobj.optString("pmtmethod");
            sessionHandlerImpl sess = new sessionHandlerImpl();
            sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", companyid);

            pdetail = accVendorPaymentobj.saveOrUpdatePayDetail(pdetailhm);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return pdetail;
    }

    public Set<JournalEntryDetail> journalEntryDetailObject(JSONObject paramJobj, JSONArray detailsJSONArray, JournalEntry journalEntry, Payment payment, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
//        StringBuffer billno = new StringBuffer();
        Set jedetails = new HashSet();

        try {
            Account dipositTo = null;
            String companyid = paramJobj.optString(Constants.companyKey);
            String currencyid = paramJobj.optString(Constants.currencyKey);
            String methodid = paramJobj.optString("pmtmethod");
            sessionHandlerImpl sess = new sessionHandlerImpl();
            sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            double rowTotaltaxamount = 0;
            double gstTaxAmount = 0;
            double gstTaxtotalAmount = 0;
            double rowTotalDiscountAmount = 0;

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();
            payment.setJcustomarrayMap(jcustomarrayMap);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String accountId = "";
            /*
             * Check if Customer/Vendor and set PaymentWindow type accordingly
             */
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("customer"))) {
                KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.optString("customer"));
                if (!resultCVAccount.getEntityList().isEmpty()) {
                    Customer customer = (Customer) resultCVAccount.getEntityList().get(0);
                    accountId = customer.getAccount().getID();
                    payment.setPaymentWindowType(2);
                }
            }

            amount = authHandler.round(Double.parseDouble(paramJobj.optString("subTotal")), companyid);
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            JournalEntryDetail JEdeatilId = null;
            List payentOtherwiseList = new ArrayList();
            HashMap paymentdetailotherwise = new HashMap();

            if (type == Constants.GLPayment) {
                JSONArray drAccArr = detailsJSONArray;
                for (int i = 0; i < drAccArr.length(); i++) {
                    gstTaxAmount=0.0;
                    JSONObject jobj = drAccArr.getJSONObject(i);

                    boolean isdebit = jobj.has("isdebit") ? Boolean.parseBoolean(jobj.getString("isdebit")) : true;
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    double enterAmount = authHandler.round(jobj.getDouble("enteramount"), companyid);
                    double discountAmount = authHandler.round(jobj.getDouble("discountAmount"), companyid);

                    double jedAmount = authHandler.round((enterAmount), companyid);
                    jedjson.put("amount", jedAmount);
                    jedjson.put("accountid", jobj.getString("documentid"));//Changed account Id 
                    jedjson.put("debit", isdebit);//true);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.optString("description"));
                    KwlReturnObject JEdeatilIdresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JEdeatilId = (JournalEntryDetail) JEdeatilIdresult.getEntityList().get(0);
                    jedetails.add(JEdeatilId);
                    drAccArr.getJSONObject(i).put("rowjedid", JEdeatilId.getID());
                    drAccArr.getJSONObject(i).put("jedetail", JEdeatilId.getID());
                    double rowtaxamount = 0;
                    if (type == Constants.GLPayment) {
                        PaymentDetailOtherwise paymentDetailOtherwise = null;
                        String rowtaxid = jobj.getString("prtaxid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                        Tax rowtax = (Tax) txresult.getEntityList().get(0);
                        if (discountAmount != 0) {
                            rowTotalDiscountAmount += discountAmount;
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(discountAmount, companyid));
                            jedjson.put("accountid", jobj.getString("discountAccountId"));
                            jedjson.put("debit", false);//true);
                            jedjson.put("jeid", jeid);

                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                        }
                        if (company.getCountry().getID().equals("105")) {
                            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.detail, "[]"));
                            JSONObject jobj1 = jArr.getJSONObject(i);
                            if (!StringUtil.isNullOrEmpty((String) jobj1.optString("LineTermdetails"))) {
                                JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj1.optString("LineTermdetails")));
                                for (int j = 0; j < termsArray.length(); j++) {
                                    JSONObject termObject = termsArray.getJSONObject(j);
                                    jedjson = new JSONObject();
                                    jedjson.put("srno", jedetails.size() + j + 1);
                                    jedjson.put(Constants.companyKey, companyid);
                                    jedjson.put("amount", termObject.get("termamount"));
                                    jedjson.put("accountid", termObject.get("accountid"));
                                    jedjson.put("debit", true);
                                    jedjson.put("jeid", jeid);
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jedetails.add(jed);
                                    gstTaxAmount += termObject.getDouble("termamount");
                                }
                            }

                            rowTotaltaxamount += gstTaxAmount;
                        }
                        if (rowtax == null || rowtaxid.equalsIgnoreCase("-1")) {
                            rowTotaltaxamount += rowtaxamount;
                            paymentdetailotherwise.put("amount", Double.parseDouble(jobj.getString("enteramount")) - discountAmount);
                            paymentdetailotherwise.put("taxjedid", "");
                            paymentdetailotherwise.put("tax", rowtaxid.equalsIgnoreCase("-1") ? "None" : "");
                            paymentdetailotherwise.put("accountid", jobj.getString("documentid"));
                            paymentdetailotherwise.put("isdebit", isdebit);
                            paymentdetailotherwise.put("taxamount", gstTaxAmount);
                            paymentdetailotherwise.put("description", jobj.optString("description"));
                            paymentdetailotherwise.put("payment", payment.getID());
                            if (jobj.has("srNoForRow")) {
                                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                                paymentdetailotherwise.put("srNoForRow", srNoForRow);
                            }
                            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                                paymentdetailotherwise.put("jedetail", (String) jobj.get("jedetail"));
                            } else {
                                paymentdetailotherwise.put("jedetail", "");
                            }
                            result = accVendorPaymentobj.savePaymentDetailOtherwise(paymentdetailotherwise);
                            paymentdetailotherwise.clear();
                            paymentDetailOtherwise = (PaymentDetailOtherwise) result.getEntityList().get(0);
                            payentOtherwiseList.add(paymentDetailOtherwise.getID());


                        } else {
                            rowtaxamount = Double.parseDouble(jobj.optString("taxamount", "0.0"));
                            rowTotaltaxamount += rowtaxamount;
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                            jedjson.put("accountid", rowtax.getAccount().getID());
                            jedjson.put("debit", isdebit);//true);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.optString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                            paymentdetailotherwise.put("amount", Double.parseDouble(jobj.getString("enteramount")) - discountAmount);
                            paymentdetailotherwise.put("taxjedid", jed.getID());
                            paymentdetailotherwise.put("tax", rowtax.getID());
                            paymentdetailotherwise.put("accountid", jobj.getString("documentid"));
                            paymentdetailotherwise.put("isdebit", isdebit);
                            paymentdetailotherwise.put("taxamount", rowtaxamount);
                            paymentdetailotherwise.put("description", jobj.optString("description"));
                            paymentdetailotherwise.put("payment", payment.getID());
                            if (jobj.has("srNoForRow")) {
                                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                                paymentdetailotherwise.put("srNoForRow", srNoForRow);
                            }
                            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                                paymentdetailotherwise.put("jedetail", (String) jobj.get("jedetail"));
                            } else {
                                paymentdetailotherwise.put("jedetail", "");
                            }
                            paymentdetailotherwise.put("taxjedetail", jed.getID());
                            result = accVendorPaymentobj.savePaymentDetailOtherwise(paymentdetailotherwise);
                            paymentdetailotherwise.clear();
                            paymentDetailOtherwise = (PaymentDetailOtherwise) result.getEntityList().get(0);
                            payentOtherwiseList.add(paymentDetailOtherwise.getID());
                        }
                        /**
                         * Save Term details for payment in Sales Return fund case
                         */
                        if (company.getCountry().getID().equals("" + Constants.indian_country_id)) {
                            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.detail, "[]"));
                            JSONObject jobj1 = jArr.getJSONObject(i);
                            if (!StringUtil.isNullOrEmpty((String) jobj1.optString("LineTermdetails"))) {
                                JSONArray termsArray = new JSONArray(StringUtil.DecodeText((String) jobj1.optString("LineTermdetails")));
                                for (int j = 0; j < termsArray.length(); j++) {
                                    JSONObject termObject = termsArray.getJSONObject(j);

                                    /**
                                     * Save GST Terms details in the Term table
                                     * for payment
                                     */
                                    HashMap<String, Object> paymentDetailsTermsMap = new HashMap<>();
                                    if (termObject.has("termid")) {
                                        paymentDetailsTermsMap.put("term", termObject.get("termid"));
                                    }
                                    if (termObject.has("termamount")) {
                                        paymentDetailsTermsMap.put("termamount", termObject.get("termamount"));
                                    }
                                    if (termObject.has("termpercentage")) {
                                        paymentDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                                    }
                                    if (termObject.has("purchasevalueorsalevalue")) {
                                        paymentDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                                    }
                                    if (termObject.has("deductionorabatementpercent")) {
                                        paymentDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                                    }
                                    if (termObject.has("assessablevalue")) {
                                        paymentDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                                    }
                                    if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                                        paymentDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                                        if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                            if (termObject.getInt("taxtype") == 0) { // If Flat
                                                paymentDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                            } else { // Else Percentage
                                                paymentDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                            }
                                        }
                                    }
                                    if (termObject.has("id")) {
                                        paymentDetailsTermsMap.put("id", termObject.get("id"));
                                    }
                                    paymentDetailsTermsMap.put("paymentdetailotherwiseid", paymentDetailOtherwise.getID());
                                    paymentDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                                    paymentDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                                    paymentDetailsTermsMap.put("userid", payment.getCreatedby().getUserID());
                                    paymentDetailsTermsMap.put("product", termObject.opt("productid"));
                                    paymentDetailsTermsMap.put("createdOn", new Date());
                                    accVendorPaymentobj.saveAdvanceDetailsTermMap(paymentDetailsTermsMap);
                                }
                            }
                        }
                    }
                }
            }
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                jedjson.put("amount", (amount + rowTotaltaxamount));
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", false);
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
                payment.setDeposittoJEDetail(jed);
                payment.setDepositAmount(amount);       // put amount excluding bank charges
                try {
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    GlobalParams.put("companyid", companyid);
                    GlobalParams.put("gcurrencyid", currencyid);
                    String transactionCurrency = payment.getCurrency() != null ? payment.getCurrency().getCurrencyID() : payment.getCompany().getCurrency().getCurrencyID();
                    KwlReturnObject baseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, (amount + rowTotaltaxamount), transactionCurrency, journalEntry.getEntryDate(), journalEntry.getExternalCurrencyRate());
                    double depositamountinbase = (Double) baseAmount.getEntityList().get(0);
                    depositamountinbase = authHandler.round(depositamountinbase, companyid);
                    payment.setDepositamountinbase(depositamountinbase);
                    payment.setDepositAmount((amount + rowTotaltaxamount));
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

    public JournalEntry journalEntryObject(JSONObject paramJobj, Payment editPaymentObject) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            String companyid = paramJobj.optString(Constants.companyKey);

            DateFormat df = authHandler.getDateOnlyFormat();
            String currencyid = paramJobj.optString(Constants.currencyKey);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));
            String methodid = paramJobj.optString("pmtmethod");
            sessionHandlerImpl sess = new sessionHandlerImpl();
            sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
            String createdby = paramJobj.optString(Constants.useridKey);
            String jeid = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeSeqFormatId = "";
            Date entryDate = df.parse(paramJobj.optString("creationdate"));
            double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
            double PaymentCurrencyToPaymentMethodCurrencyRate = StringUtil.getDouble(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate"));
            boolean ismulticurrencypaymentje = StringUtil.getBoolean(paramJobj.optString("ismulticurrencypaymentje"));
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            boolean isFromEclaim = paramJobj.has("isFromEclaim") ? paramJobj.getBoolean("isFromEclaim") : false;
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);

            if (editPaymentObject == null) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                    jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                }
            } else if (editPaymentObject != null && editPaymentObject.getJournalEntry() != null) {
                JournalEntry entry = editPaymentObject.getJournalEntry();
                jeid = editPaymentObject.getJournalEntry().getID();
                jeDataMap.put("jeid", jeid);
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, entry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            }
            jeDataMap.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
            jeDataMap.put("entrydate", entryDate);
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", paramJobj.optString("memo"));
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("ismulticurrencypaymentje", ismulticurrencypaymentje);
            jeDataMap.put("createdby", createdby);
            jeDataMap.put("isFromEclaim", isFromEclaim);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);

        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return journalEntry;
    }

    public Payment createPaymentObject(JSONObject paramJobj, Payment editPaymentObject, SalesReturn salesReturn) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
        List list = new ArrayList();
        KwlReturnObject result = null;
        Customer cust = null;
        Payment paymentObject = null;
        String sequenceformat = paramJobj.optString("paymentSequenceFormat") != null ? paramJobj.optString("paymentSequenceFormat") : "NA";
        String companyid = paramJobj.optString(Constants.companyKey);
        double externalCurrencyRate = StringUtil.getDouble(paramJobj.optString("externalcurrencyrate"));
        String currencyid = paramJobj.optString(Constants.currencyKey);
        String entryNumber = StringUtil.isNullOrEmpty(paramJobj.optString("paymentNumber", null)) ? "" : paramJobj.getString("paymentNumber");;
        String exciseunit = paramJobj.optString("exciseunit") != null ? paramJobj.optString("exciseunit") : "";
        String methodid = paramJobj.optString("pmtmethod");
        String payee = paramJobj.optString("payee");
        double paymentCurrencyToPaymentMethodCurrencyRate = StringUtil.getDouble(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate"));
        sessionHandlerImpl sess = new sessionHandlerImpl();
        sess.updatePaymentMethodIDForPayment(paramJobj, methodid);
        DateFormat df = authHandler.getDateFormatter(paramJobj);
        Date creationDate = null;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("creationdate"))) {
            try {
                creationDate = df.parse(paramJobj.optString("creationdate"));
            } catch (Exception ex) {
                throw ServiceException.FAILURE("createPaymentObject : " + ex.getMessage(), ex);
            }
        }
        boolean rcmApplicable = !StringUtil.isNullOrEmpty(paramJobj.optString("rcmApplicable")) ? Boolean.parseBoolean(paramJobj.optString("rcmApplicable")) : false;
        HashMap paymenthm = new HashMap();
        boolean isCustomer = false;
        paramJobj.optString("customer", null);
        boolean isVendor = false;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("customer"))) {
            KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.optString("customer"));
            if (custObj.getEntityList().get(0) != null) {
                cust = (Customer) custObj.getEntityList().get(0);
            }
            if (cust != null) {
                isCustomer = true;
            }

        }
        if (isCustomer) {
            paymenthm.put("customer", paramJobj.optString("customer"));
        }
        String createdby = paramJobj.optString(Constants.useridKey);
        String modifiedby = paramJobj.optString(Constants.useridKey);
        long createdon = System.currentTimeMillis();
        long updatedon = System.currentTimeMillis();


        synchronized (this) { //this block is used to generate auto sequence number if number is not duplicate
            String nextAutoNo = "";
            String nextAutoNoInt = "";
            int count = 0;
            if (editPaymentObject != null) {
                if (sequenceformat.equals("NA")) {
                    if (!entryNumber.equals(editPaymentObject.getPaymentNumber())) {
                        result = accVendorPaymentobj.getPaymentFromNo(entryNumber, companyid);
                        paymenthm.put("entrynumber", entryNumber);
                        paymenthm.put("autogenerated", entryNumber.equals(nextAutoNo));
                        count = result.getRecordTotalCount();
                    }
                    if (count > 0) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
            } else {
                if (!sequenceformat.equals("NA")) {
                    paymenthm.put(Constants.SEQFORMAT, sequenceformat);
                    paymenthm.put(Constants.SEQNUMBER, "");
                }
                if (sequenceformat.equals("NA")) {
                    paymenthm.put("entrynumber", entryNumber);
                } else {
                    paymenthm.put("entrynumber", "");
                }
                paymenthm.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            }
        }
        if (sequenceformat.equals("NA")) {

            //In case of NA checks wheather this number can also be generated by a sequence format or not
            List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Make_Payment_ModuleId, entryNumber, companyid);
            if (!resultList.isEmpty()) {
                boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                String formatName = (String) resultList.get(1);
                if (!isvalidEntryNumber) {
                    throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                }
            }
        }
        KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) cmpresult.getEntityList().get(0);

        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        currencyid = (paramJobj.optString("currencyid") == null ? currency.getCurrencyID() : paramJobj.optString("currencyid"));

        paymenthm.put("currencyid", currencyid);
        paymenthm.put("externalCurrencyRate", externalCurrencyRate);
        result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod"));
        PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
        paymenthm.put("memo", "Payment is generated from sales return "+salesReturn.getSalesReturnNumber());
        paymenthm.put("nonRefundable", !StringUtil.isNullOrEmpty(paramJobj.optString("NonRefundable")));
        paymenthm.put("cinno", !StringUtil.isNullOrEmpty(paramJobj.optString("cinno")) ? paramJobj.optString("cinno") : "");
        paymenthm.put("isLinkedToClaimedInvoice", !StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedToClaimedInvoice")) ? Boolean.parseBoolean(paramJobj.optString("isLinkedToClaimedInvoice")) : false);
        paymenthm.put("companyid", companyid);
        paymenthm.put("createdby", createdby);
        paymenthm.put("modifiedby", modifiedby);
        paymenthm.put("creationDate", creationDate);
        paymenthm.put("salesReturn", salesReturn.getID());

        paymenthm.put(Constants.Checklocktransactiondate, paramJobj.optString("creationdate"));//ERP-16800-Without parsing date
        paymenthm.put("createdon", createdon);
        paymenthm.put("updatedon", updatedon);
        paymenthm.put("PaymentCurrencyToPaymentMethodCurrencyRate", paymentCurrencyToPaymentMethodCurrencyRate);
        if (editPaymentObject != null) {
            paymenthm.put("paymentid", editPaymentObject.getID());
        }
        paymenthm.put("payee", payee);
        paymenthm.put("exciseunit", exciseunit);
        paymenthm.put("rcmApplicable", rcmApplicable);
        paymentObject = accVendorPaymentobj.getPaymentObj(paymenthm);


        return paymentObject;
    }
  
  @Override
    public List saveSalesReturnRows(JSONObject paramJobj, SalesReturn salesReturn, String companyid, JournalEntry je, Set<JournalEntryDetail> inventoryJEDetails, String inventoryJEid) throws ServiceException, AccountingException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        List returnDetails = new ArrayList();
        Set<SalesReturnDetail> srRowsDetails = new HashSet<SalesReturnDetail>();
        double totalCNAmt = 0;
        double totalDiscountAmt = 0;
        double totalCNAmtExludingTax = 0;
        InvoiceDetail id = null ;
        KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) comp.getEntityList().get(0);
        int countryid = Integer.parseInt(company.getCountry().getID());
        double previousIssueCount = 0;
        double actquantity = 0.0f;
        double dquantity = 0.0f;
        double sumOfDiscountAmount = 0.0;
        HashSet cndetails = new HashSet();
        HashSet cnTaxEntryDetails = new HashSet();
        HashSet jedetails = new HashSet();
        try {
            boolean isNoteAlso = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteAlso",null))) {// isNoteAlso flag will be true if you are creating Sale/Purchase Return with Credit/Debit Note also
                isNoteAlso = Boolean.parseBoolean(paramJobj.getString("isNoteAlso"));
            }
            boolean isQAinspection = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isQAinspection",null))) {//this flag is for saving consignment return and it will true if return quantity will go through QA Inspection process otherwise it will be false
                isQAinspection = Boolean.parseBoolean(paramJobj.getString("isQAinspection"));
            }
            String globalTaxID= paramJobj.optString("taxid",null);
            double globalTaxPercent = 0;
            if (!StringUtil.isNullOrEmpty(globalTaxID)){
                globalTaxPercent = StringUtil.isNullOrEmpty(paramJobj.optString("globalTaxPercent",null))?0:Double.parseDouble(paramJobj.getString("globalTaxPercent"));
            }
            
            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.detail,"[]"));

            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParamsJson(paramJobj);
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isFixedAsset)) : false;
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isLeaseFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isLeaseFixedAsset)) : false;
            boolean isConsignment = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isConsignment,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isConsignment)) : false;
            boolean RCMApplicable = paramJobj.optBoolean("GTAApplicable",false);//ERP-34970(ERM-534)
            List<StockMovement> stockMovementsList=new ArrayList<StockMovement>();
            List<Consignment> ConsignmentList=new ArrayList<Consignment>();
            Map<String, List<TransactionBatch>> priceValuationMap = new HashMap<>();
            Set<String> productNameRCMNotActivate = new HashSet<String>();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> dodDataMap = new HashMap<String, Object>();
                
                if(jobj.has("srno")) {
                    dodDataMap.put("srno", jobj.getInt("srno"));
                }
                
                dodDataMap.put(Constants.companyKey, companyid);
                dodDataMap.put("srid", salesReturn.getID());
                dodDataMap.put(Constants.productid, jobj.getString(Constants.productid));
                
                if (jobj.has("priceSource") && jobj.get("priceSource") != null) {
                    dodDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource",null)) ?  StringUtil.DecodeText(jobj.optString("priceSource")) : "");
                }
                if (jobj.has("pricingbandmasterid") && jobj.get("pricingbandmasterid") != null) {
                    dodDataMap.put("pricingbandmasterid", !StringUtil.isNullOrEmpty(jobj.optString("pricingbandmasterid", null)) ? StringUtil.DecodeText(jobj.optString("pricingbandmasterid")) : "");
                }
                
                String linkMode = paramJobj.optString("fromLinkCombo",null);
                boolean isFromVendorConsign=false;
                if (jobj.has("isFromVendorConsign") && jobj.get("isFromVendorConsign") != null) {
                    isFromVendorConsign = jobj.optBoolean("isFromVendorConsign", false);
                }
                KwlReturnObject proresult = accountingHandlerDAOobj.getObject(Product.class.getName(), jobj.getString(Constants.productid));
                Product product = (Product) proresult.getEntityList().get(0);
                
                /**
                 * //ERP-34970(ERM-534)
                 * IF Invoice is RCM Applicable and Product is not RCM
                 * Applicable
                 */
                if (countryid == Constants.indian_country_id && RCMApplicable) {
                    if (product != null && !product.isRcmApplicable()) {
                        productNameRCMNotActivate.add(product.getName());
                        //throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.SR.text", new Object[]{product.getName()}, null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("totalissuecount","0"))) {
                    previousIssueCount = Double.parseDouble(jobj.optString("totalissuecount","0"));
                    dodDataMap.put("previousissuecount",previousIssueCount);
                }
                String desc = null;
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    desc = jobj.optString("desc","");
                } else {
                    desc = jobj.optString("description","");
                }
                
                dodDataMap.put("description", desc);
                dodDataMap.put("partno", jobj.optString("partno",""));                   
                     if (jobj.has("quantity") && jobj.get("quantity") != null) {
                         actquantity = jobj.getDouble("quantity");
                     }
                     if (jobj.has("dquantity") && jobj.get("dquantity") != null) {
                         dquantity = jobj.getDouble("dquantity");
                     }
                double baseuomrate = 1;
                if (jobj.has("baseuomrate") && jobj.get("baseuomrate") != null) {
                    baseuomrate = jobj.getDouble("baseuomrate");
                }
                dodDataMap.put("quantity", actquantity);
                dodDataMap.put("returnquantity", dquantity);
                dodDataMap.put("baseuomrate", baseuomrate);
                
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    if (jobj.has("uomname")) {
                        dodDataMap.put("uomid", jobj.getString("uomname"));
                    }
                } else {
                    if (jobj.has("uomid")) {
                        dodDataMap.put("uomid", jobj.getString("uomid"));
                    }
                }

                dodDataMap.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(actquantity, baseuomrate, companyid));
                dodDataMap.put("baseuomreturnquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate, companyid));
                dodDataMap.put("remark", jobj.optString("remark"));
                if(!StringUtil.isNullOrEmpty(jobj.optString("reason",""))){
                    dodDataMap.put("reason", jobj.getString("reason"));
                }
                
                String rowtaxid = "";
                if (!StringUtil.isNullOrEmpty(jobj.optString("prtaxid", null)) && jobj.optString("prtaxid", null).equalsIgnoreCase("None")) {
                    rowtaxid = null;
                } else {
                    rowtaxid = jobj.optString("prtaxid", null);
                }
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    double rowtaxamountFromJS = jobj.optDouble("taxamount", 0);

                    if (rowtax == null) {
                        throw new AccountingException("The Tax code(s) used in this transaction has been deleted.");//messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        
                        dodDataMap.put("taxamount", rowtaxamountFromJS);
                    }
                }
                dodDataMap.put("prtaxid", rowtaxid);//    ERP-28612
                dodDataMap.put(Constants.isUserModifiedTaxAmount, jobj.optBoolean(Constants.isUserModifiedTaxAmount, false));
                
                int discountispercent =1;
                double prdiscount = jobj.optDouble("prdiscount", 0);
                
                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    dodDataMap.put("discount", prdiscount);
                }
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    discountispercent = jobj.optInt("discountType", 1);
                    if (jobj.has("discountType") && jobj.get("discountType") != null) {
                        dodDataMap.put("discountispercent", discountispercent);
                    }
                } else {
                    discountispercent = jobj.optInt("discountispercent", 1);
                    if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                        dodDataMap.put("discountispercent", discountispercent);
                    }
                }
                if (jobj.has("discountjson")) {
                    String discountjson = jobj.optString("discountjson", "");
                    discountjson = !StringUtil.isNullOrEmpty(discountjson) ? StringUtil.decodeString(discountjson) : "";
                    dodDataMap.put("discountjson", discountjson);
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore",null))) {
                    dodDataMap.put("invstoreid", jobj.optString("invstore"));
                } else {
                    dodDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation",null))) {
                    dodDataMap.put("invlocationid", jobj.optString("invlocation"));
                } else {
                    dodDataMap.put("invlocationid", "");
                }

                if (!StringUtil.isNullOrEmpty(linkMode)) {
                    if (linkMode.equalsIgnoreCase(Constants.Delivery_Order) || linkMode.equalsIgnoreCase("Consignment Delivery Order") || linkMode.equalsIgnoreCase("Asset Delivery Order") || linkMode.equalsIgnoreCase("Lease Delivery Order")) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), jobj.getString("rowid"));
                        DeliveryOrderDetail sod = (DeliveryOrderDetail) rdresult.getEntityList().get(0);
                        dodDataMap.put("DeliveryOrderDetail", sod);
                    } else if (linkMode.equalsIgnoreCase("Customer Invoice") || linkMode.equalsIgnoreCase("Sales Invoice")||linkMode.equalsIgnoreCase("Cash Sales")) {
                     /*
                      * link case  is cash sales if payment is creating from sales return
                      */ 
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(InvoiceDetail.class.getName(), jobj.getString("rowid"));
                        id = (InvoiceDetail) rdresult.getEntityList().get(0);
                        dodDataMap.put("InvoiceDetail", id);
                    }
                }

                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put(Constants.productid, jobj.getString(Constants.productid));
                inventoryjson.put("description",desc);
                inventoryjson.put("quantity", dquantity);
                
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    if (jobj.has("uomname")) {
                        inventoryjson.put("uomid", jobj.getString("uomname"));
                    }
                } else {
                    if (jobj.has("uomid")) {
                        inventoryjson.put("uomid", jobj.getString("uomid"));
                    }
                }

                double receivedQty = dquantity*baseuomrate;
                double venQty = 0;
                String batchDetails = null;
                boolean isForPOS= paramJobj.optBoolean(Constants.isForPos);
                if (jobj.has("batchdetails") && jobj.getString("batchdetails") != null && !StringUtil.isNullOrEmpty(jobj.optString("batchdetails"))) {
                    batchDetails = jobj.optString("batchdetails","[]");
                    if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true && product.isIsBatchForProduct() && product.isIsSerialForProduct()) {
                        JSONObject jsobj = accProductModuleService.manipulateBatchDetailsforMobileApps(batchDetails, jobj.getString(Constants.productid), paramJobj);
                        if (jsobj.get("batchdetails") != null && !StringUtil.isNullOrEmpty(jsobj.optString("batchdetails", null))) {
                            batchDetails = jsobj.getString("batchdetails");
                        }
                    }
                }else if(isForPOS && StringUtil.isNullOrEmpty(jobj.optString("batchdetails",null)) && paramJobj.optBoolean(Constants.isdefaultHeaderMap, false)){
                         jobj =buildBatchSerialJson(paramJobj, jobj,product);
                         batchDetails = jobj.optString("batchdetails","[]");
                }
                if (!isConsignment&&jobj.has("batchdetails") && jobj.getString("batchdetails") != null && !"".equals(jobj.getString("batchdetails"))) {
                    JSONArray batchDtJArr = new JSONArray(batchDetails);
                    for (int j = 0; j < batchDtJArr.length(); j++) {
                        JSONObject jsnObj = batchDtJArr.getJSONObject(j);
                        double qty = (jsnObj.has("quantity") && !StringUtil.isNullOrEmpty(jsnObj.getString("quantity"))) ? jsnObj.getDouble("quantity") : 0;
                        venQty += (jsnObj.has("stocktype") && "0".equals(jsnObj.getString("stocktype"))) ? qty : 0;
                        inventoryjson.put("venconsignuomquantity", (venQty * baseuomrate));
                    }
                }
                if (isQAinspection) {  //if isQAinspection is set true then  do not update quantity
                    inventoryjson.put("baseuomquantity", 0);
                } else if (isFromVendorConsign) {   // is sales return is made for such dodetails whose do is made from vendor consignment quantity
                    inventoryjson.put("venconsignuomquantity", (dquantity * baseuomrate));
                }else if(venQty>0&&!isConsignment){
                    inventoryjson.put("venconsignuomquantity", (venQty * baseuomrate));
                     inventoryjson.put("baseuomquantity", authHandler.calculateBaseUOMQuatity((dquantity-venQty), baseuomrate, companyid));
                } else {
                    inventoryjson.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate, companyid));
                }
                
                inventoryjson.put("baseuomrate", baseuomrate);
                if (isLeaseFixedAsset) {
                    inventoryjson.put("leaseFlag", isLeaseFixedAsset);
                }
                if (isConsignment) {
                    inventoryjson.put("consignuomquantity", -(dquantity * baseuomrate)); //we will update the consignment Quantity and it will added to inventory after inspection
                    inventoryjson.put(Constants.isConsignment, isConsignment);
                }
                if(jobj.has("productweightperstockuom")) {
                    inventoryjson.put("productweightperstockuom", jobj.optDouble("productweightperstockuom",0));
                }
                if(jobj.has("productweightincludingpakagingperstockuom")) {
                    inventoryjson.put("productweightincludingpakagingperstockuom", jobj.optDouble("productweightincludingpakagingperstockuom",0));
                }
                if(jobj.has("productvolumeperstockuom")) {
                    inventoryjson.put("productvolumeperstockuom", jobj.optDouble("productvolumeperstockuom",0));
                }
                if(jobj.has("productvolumeincludingpakagingperstockuom")) {
                    inventoryjson.put("productvolumeincludingpakagingperstockuom", jobj.optDouble("productvolumeincludingpakagingperstockuom",0));
                }
                inventoryjson.put("carryin", true);
                inventoryjson.put("defective", false);
                inventoryjson.put("newinventory", false);
                inventoryjson.put(Constants.companyKey, companyid);
                inventoryjson.put("updatedate", authHandler.getDateOnlyFormat().parse(paramJobj.optString(Constants.BillDate,null)));
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                Inventory inventory = (Inventory) invresult.getEntityList().get(0);

                dodDataMap.put("Inventory", inventory);
                double unitPrice = 0;
                if (jobj.has("rate")) {
                    dodDataMap.put("rate", jobj.getString("rate"));
                    unitPrice = jobj.optDouble("rate",0);
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("recTermAmount",null))) {
                    dodDataMap.put("recTermAmount", jobj.optString("recTermAmount"));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("OtherTermNonTaxableAmount",null))) {
                    dodDataMap.put("OtherTermNonTaxableAmount", jobj.optString("OtherTermNonTaxableAmount"));
                }
                if (jobj.has("rateIncludingGst")) {
                    dodDataMap.put("rateIncludingGst", jobj.optDouble("rateIncludingGst",0));
                }
                // save sales return rows
                if (!(product.getProducttype().getID().equals(Producttype.SERVICE) || product.getProducttype().getID().equals(Producttype.NON_INVENTORY_PART) || product.getProducttype().getID().equals(Producttype.Inventory_Non_Sales))) {
                    if (extraCompanyPreferences != null && !StringUtil.isNullOrEmpty(inventoryJEid) && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                        if (product != null && product.getInventoryAccount() != null && product.getCostOfGoodsSoldAccount() != null && jobj.has("rate")) {
                            HashMap<String, Object> requestMap = new HashMap<>();
                            requestMap.put(Constants.productid, jobj.getString(Constants.productid));
                            requestMap.put("productId", jobj.getString(Constants.productid));
                            requestMap.put(Constants.companyKey, companyid);
                            requestMap.put(Constants.df, authHandler.getDateOnlyFormat());
                            requestMap.put(Constants.globalCurrencyKey, paramJobj.getString(Constants.globalCurrencyKey));
                            requestMap.put("GlobalParams", GlobalParams);
                            requestMap.put(Constants.REQ_enddate, paramJobj.optString(Constants.BillDate, null));
                            requestMap.put("dquantity", authHandler.calculateBaseUOMQuatity(dquantity, baseuomrate, companyid));
                            requestMap.put("isSalesReturn", true);
                            Map<String, Double> batchQuantityMap = new HashMap<>();
                            if (jobj.has("batchdetails") && jobj.get("batchdetails") != null && !StringUtil.isNullOrEmpty(jobj.get("batchdetails").toString())) {
                                String batchSerialIds = "";
                                if(!extraCompanyPreferences.isAutoFillBatchDetails()){
                                    batchSerialIds = accInvoiceModuleService.getBatchSerialIDs(jobj.getString("batchdetails"), product, batchQuantityMap);
                                }
                                else{
                                    batchSerialIds = accInvoiceModuleService.getBatchSerialIDsForAutofillBatchSerial(jobj.getString("batchdetails"), product, batchQuantityMap);
                                }
                                if (!StringUtil.isNullOrEmpty(batchSerialIds)) {
                                    requestMap.put("batchSerialId", batchSerialIds.split(","));
                                }
                            }
//                            double valuation = accInvoiceModuleService.getValuationForDOAndSR(requestMap, priceValuationMap, batchQuantityMap);
                            double valuation=0;
                            // Inventory Account-Debit
                            JSONObject jedjson = new JSONObject();
                            jedjson.put(GoodsReceiptConstants.SRNO, jedetails.size() + 1);
                            jedjson.put(GoodsReceiptConstants.COMPANYID, companyid);
                            jedjson.put(GoodsReceiptConstants.AMOUNT, valuation);
                            jedjson.put(GoodsReceiptConstants.ACCOUNTID, product.getInventoryAccount() != null ? product.getInventoryAccount().getID() : "");
                            jedjson.put(GoodsReceiptConstants.DEBIT, true);
                            jedjson.put(GoodsReceiptConstants.JEID, inventoryJEid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            dodDataMap.put("inventoryjedetailid", jed.getID());
                            inventoryJEDetails.add(jed);
                            // Cost of Goods Sold Account-Credit
                            jedjson = new JSONObject();
                            jedjson.put(GoodsReceiptConstants.SRNO, jedetails.size() + 1);
                            jedjson.put(GoodsReceiptConstants.COMPANYID, companyid);
                            jedjson.put(GoodsReceiptConstants.AMOUNT, valuation);
                            jedjson.put(GoodsReceiptConstants.ACCOUNTID, product.getCostOfGoodsSoldAccount() != null ? product.getCostOfGoodsSoldAccount().getID() : "");
                            jedjson.put(GoodsReceiptConstants.DEBIT, false);
                            jedjson.put(GoodsReceiptConstants.JEID, inventoryJEid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            dodDataMap.put("cogsjedetailid", jed.getID());
                            inventoryJEDetails.add(jed);
                        }
                    }
                }
                KwlReturnObject result = accInvoiceDAOobj.saveSalesReturnDetails(dodDataMap);
                SalesReturnDetail row = (SalesReturnDetail) result.getEntityList().get(0);
                String prductID=jobj.getString(Constants.productid);
                boolean isEdit=(!StringUtil.isNullOrEmpty(paramJobj.optString("isEdit",null)))?Boolean.parseBoolean(paramJobj.getString("isEdit")):false;
                if (jobj.has("totalissuecount") && !StringUtil.isNullOrEmpty(jobj.optString("totalissuecount","0"))) {
                    double totalIssueCount = Double.parseDouble(jobj.optString("totalissuecount","0"));
                    KwlReturnObject rrt = accProductObj.updateTotalIssueCount(totalIssueCount, prductID, companyid, false,isEdit);
                }

                // Create CN Details
                if (isNoteAlso) {
                    String sales_accid ="";
                    String CreditNoteDetailID = StringUtil.generateUUID();
                    CreditNoteDetail cnDetailRow = new CreditNoteDetail();
                    cnDetailRow.setSrno(i + 1);
                    cnDetailRow.setID(CreditNoteDetailID);
                    cnDetailRow.setTotalDiscount(0.00);
                    cnDetailRow.setCompany(company);
                    CreditNoteTaxEntry taxEntry = new CreditNoteTaxEntry();
                    String CreditNoteTaxID = StringUtil.generateUUID();
                    taxEntry.setID(CreditNoteTaxID);
//                    if(id != null && countryid == Constants.indian_country_id && id.getSalesJED() != null){// id is invoicedetailid.
//                        sales_accid = id.getSalesJED().getAccount().getID();//account used in invoicedetail
//                    }else{
                    sales_accid = product.getSalesReturnAccount().getID();
//                    }

                    if (i == 0) {// create  cndetail entry only once in this case i.e if multitple Products are linked.
                        cndetails.add(cnDetailRow);
                    }

                    double discountVal = 0d;
                    double totalAmt = unitPrice * receivedQty;
                    if (discountispercent == 1) {
                        discountVal = totalAmt * prdiscount / 100;
                    } else {
                        discountVal = prdiscount;
                    }
                    totalDiscountAmt += authHandler.round(discountVal, companyid);                        //rounded dicount amount  to jedetail 
                    
                    double rowtaxamount = 0d;
                    double amountExcludingTax = 0;

                    /*
                     * Here handling three cases #1 tax given at line level #2
                     * tax given at global level #3 No tax is given. When tax
                     * given at line level amount comes with tax When tax given
                     * at global level amount comes without tax
                     */
                    Tax rowtax = null;
                    double cnRowAmount=jobj.optDouble("amount", 0);
                    
                    if(!StringUtil.isNullOrEmpty(globalTaxID)){//when tax given at global Level
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), globalTaxID);
                        rowtax = (Tax) txresult.getEntityList().get(0);
                        amountExcludingTax = cnRowAmount;//in global level tax row level amount is without tax
                        rowtaxamount= authHandler.round(cnRowAmount * (globalTaxPercent/100), companyid);//calculating tax at live level
                        totalCNAmt += amountExcludingTax+rowtaxamount;//calculating total cn amount with tax etc
                        totalCNAmtExludingTax += amountExcludingTax; // calculating total cn amount without tax etc
                    } else if(!StringUtil.isNullOrEmpty(rowtaxid)) {
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid);
                        rowtax = (Tax) txresult.getEntityList().get(0);
                        totalCNAmt += cnRowAmount;// At live level amount will come with tax
                        rowtaxamount = jobj.optDouble("taxamount", 0);// tax amount comes from js sode so no need to calculate
                        amountExcludingTax = cnRowAmount - rowtaxamount;
                        totalCNAmtExludingTax += amountExcludingTax;
                    } else {// No tax applied
                        totalCNAmt += cnRowAmount;
                        rowtaxamount = 0;
                        amountExcludingTax = cnRowAmount ;
                        totalCNAmtExludingTax += cnRowAmount;
                    }
                    
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put(Constants.companyKey, company.getCompanyID());
                    jedjson.put("amount", amountExcludingTax+authHandler.round(discountVal, companyid));                  //rounded dicount amount  to jedetail 
                    jedjson.put("accountid", sales_accid);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", je.getID());
                    jedjson.put("description", desc);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                        jcustomarray = fieldDataManagercntrl.getComboValueIdsForCurrentModule(jcustomarray, Constants.Acc_Credit_Note_ModuleId, companyid, 1);            // 1= for line item
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", jed.getID());
                        customrequestParams.put("recdetailId", taxEntry.getID());
                        customrequestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                        customrequestParams.put(Constants.companyKey, company.getCompanyID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            JSONObject tempjedjson = new JSONObject();
                            tempjedjson.put("accjedetailcustomdata", jed.getID());
                            tempjedjson.put("jedid", jed.getID());
                            jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                        }
                    }
                    taxEntry.setTotalJED(jed);
                    /*
                     * Below code is used to post Reverse JE for each discount and mapped or applied to product
                     * if user applies discount manually then the JE will posted against the account which is mapped in discount given goes to in company preferences
                     */
                    double discountAmt = 0.0;
                    if (totalDiscountAmt != 0.0) {
                        JSONArray discountJArr = new JSONArray();
                        String discountjsonStr = "";
                        double quantityForDiscount = jobj.optDouble("dquantity", 0);
                        JSONObject discountmasterObj = null;
                        if (jobj.has("discountjson")) {
                            discountjsonStr = jobj.optString("discountjson", "");
                        }
                        if (!StringUtil.isNullOrEmpty(discountjsonStr)) {
                            discountjsonStr = StringUtil.DecodeText(discountjsonStr);
                            discountmasterObj = new JSONObject(discountjsonStr);
                        }
                        if (discountmasterObj != null && discountmasterObj.has("data")) {
                            discountJArr = discountmasterObj.getJSONArray("data");
                        }
                        if (discountJArr.length() > 0) {
                            for (int j = 0; j < discountJArr.length(); j++) {
                                JSONObject discountMasterJObj = discountJArr.getJSONObject(j);
                                discountAmt = discountMasterJObj.optInt("discounttype") == 1 ? ((unitPrice * quantityForDiscount) * (discountMasterJObj.optDouble("discountvalue") / 100)) : discountMasterJObj.optDouble("discountvalue");
                                discountAmt = authHandler.round(discountAmt, companyid);
                                jedjson.put("srno", jedetails.size() + 1);
                                jedjson.put(Constants.companyKey, companyid);
                                jedjson.put("amount", discountAmt);
                                jedjson.put("accountid", discountMasterJObj.optString("discountaccount"));
                                jedjson.put("debit", false);
                                jedjson.put("jeid", je.getID());
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jedetails.add(jed);
                            }
                        } else {
                            discountAmt = discountispercent == 1 ? ((unitPrice * quantityForDiscount) * (prdiscount / 100)) : prdiscount;
                            sumOfDiscountAmount += authHandler.round(discountAmt,companyid);
                        }
                    }
                    // Entering data for CN Tax Entry
                    String rowTaxJeId = "";
                    if (rowtax != null) {
                        jedjson = new JSONObject();
                        jedjson.put("srno", jedetails.size() + 1);
                        jedjson.put(Constants.companyKey, company.getCompanyID());
                        jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                        jedjson.put("accountid", rowtax.getAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", je.getID());
                        jedjson.put("description", desc);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jedetails.add(jed);

                        rowTaxJeId = jed.getID();
                    }
                    
                    // Row TAX JE POST When Sales Return with CN - it is only for Indian Company. 
                    // In RCM case No JE tax Details Added for INDIA
                    if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails")) && !RCMApplicable) {
                        JSONArray termsArray = new JSONArray( StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                        for (int j = 0; j < termsArray.length(); j++) {
                            JSONObject termObject = termsArray.getJSONObject(j);
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + j + 1);
                            jedjson.put(Constants.companyKey, companyid);
                            jedjson.put("amount", termObject.get("termamount"));
                            jedjson.put("accountid", termObject.get("accountid"));
                            jedjson.put("debit", true);
                            jedjson.put("jeid", je.getID());
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                            totalCNAmt+=termObject.getDouble("termamount");
                            rowtaxamount+=termObject.getDouble("termamount");
                        }
                    }                   
                   
                    // save custom field details for auto created case
                    if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.customfield, ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString(Constants.customfield, "[]"));
                        jcustomarray = fieldDataManagercntrl.getComboValueIdsForCurrentModule(jcustomarray, Constants.Acc_Credit_Note_ModuleId, companyid, 1);            // 1= for line item
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", jed.getID());
                        customrequestParams.put("recdetailId", taxEntry.getID());
                        customrequestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                        customrequestParams.put(Constants.companyKey, company.getCompanyID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            JSONObject tempjedjson = new JSONObject();
                            tempjedjson.put("accjedetailcustomdata", jed.getID());
                            tempjedjson.put("jedid", jed.getID());
                            jedresult = accJournalEntryobj.updateJournalEntryDetails(tempjedjson);
                        }
                    }
                    KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), sales_accid);
                    Account account = (Account) accountresult.getEntityList().get(0);
                    double cnAmount=0d;
                    if(salesReturn.isGstIncluded()){
                        cnAmount=amountExcludingTax + rowtaxamount;
                    }else{
                        cnAmount=amountExcludingTax;
                    }

                    taxEntry.setAccount(account);
                    taxEntry.setAmount(cnAmount);
                    taxEntry.setCompany(company);
                    if (!StringUtil.isNullOrEmpty(jobj.optString("reason", ""))) {
                        KwlReturnObject reasonresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jobj.optString("reason", ""));
                        MasterItem reason = (MasterItem) reasonresult.getEntityList().get(0);
                        taxEntry.setReason(reason);
                    }
                    taxEntry.setDescription(desc);
                    taxEntry.setIsForDetailsAccount(true);
                    taxEntry.setDebitForMultiCNDN(true);
                    taxEntry.setTax(rowtax);
                    taxEntry.setTaxJedId(rowTaxJeId);
                    taxEntry.setTaxamount(rowtaxamount);
                    taxEntry.setRateIncludingGst(amountExcludingTax);

                    cnTaxEntryDetails.add(taxEntry);
                    
                }
                
                // while doing consignment return we are moving stock from customer warehouse to company warehouse
                if (jobj.has("batchdetails") && jobj.getString("batchdetails") != null) {
//                    String batchDetails = jobj.getString("batchdetails");

                    if (!StringUtil.isNullOrEmpty(batchDetails)) {
                        if (isConsignment) {
                            // following functions used to reduce the data from customer warehouse (as after consignment DO stock is added to customer warehouse after return this stock should be removed
//                             accInvoiceModuleService.saveNewConsignSRBatch(batchDetails, inventory, request, row,ConsignmentList,stockMovementsListForConsignment);
                            accInvoiceModuleService.saveConsignSRBatch(batchDetails, inventory, paramJobj, row);
                        }
                        //following function is used to return data to company again from where it was taken
//                        accInvoiceModuleService.saveNewSRBatch(batchDetails, inventory,request,row,stockMovementsList);
                        accInvoiceModuleService.saveSRBatch(batchDetails, inventory,paramJobj,row,stockMovementsList,ConsignmentList,jobj.optString("rowid"));
                    }
                }

                String contractid = paramJobj.optString("contractid",null);
                if (!StringUtil.isNullOrEmpty(linkMode) && !StringUtil.isNullOrEmpty(contractid)) {
                    if (linkMode.equalsIgnoreCase(Constants.Delivery_Order)) {
                        String status = "Open";
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), jobj.getString("rowid"));

                        DeliveryOrderDetail sod = (DeliveryOrderDetail) rdresult.getEntityList().get(0);

                        Set<DOContractMapping> dOContractMappings = sod.getDeliveryOrder().getdOContractMappings();
                        if (dOContractMappings != null && !dOContractMappings.isEmpty()) {
                            Contract contract = null;
                            for (DOContractMapping contractMapping : dOContractMappings) {
                                contract = contractMapping.getContract();
                            }
                            if (contract != null) {

                                status = accInvoiceModuleService.getDeliveryReturnStatus(sod.getDeliveryOrder(),"");
//                        ['1','Pending'],['2','Pending & Closed'],['3','Done'],['4','Done & Closed']]
                                int contractSRStatus = 1;

                                if (contract.getSrstatus() == 2 && status.equalsIgnoreCase("Closed")) {
                                    contractSRStatus = 4;
                                } else if (contract.getSrstatus() == 2 && status.equalsIgnoreCase("Open")) {
                                    contractSRStatus = 2;
                                } else if (contract.getSrstatus() == 1 && status.equalsIgnoreCase("Closed")) {
                                    contractSRStatus = 3;
                                }
                                accSalesOrderDAOObj.changeContractSRStatus(contract.getID(), contractSRStatus);
                            }
                        }
                    }
                }

                boolean isFromSalesReturn = true;

                if ((isLeaseFixedAsset || isFixedAsset) && product.isAsset()) {
                    int moduleID = isFixedAsset? Constants.Acc_FixedAssets_Sales_Return_ModuleId : Constants.Acc_Sales_Return_ModuleId;
                    Set<AssetDetails> assetDetailsSet = accInvoiceModuleService.saveAssetDetails(paramJobj, jobj.getString(Constants.productid), jobj.getString("assetDetails"), 0, false, false, isLeaseFixedAsset, isFromSalesReturn, isFixedAsset, false, 0);
                    
                    Set<AssetInvoiceDetailMapping> assetInvoiceDetailMappings = accInvoiceModuleService.saveAssetInvoiceDetailMapping(row.getID(), assetDetailsSet, companyid, moduleID);
                }


                String customfield = jobj.optString(Constants.customfield,null);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> DOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "SalesReturnDetail");
                    customrequestParams.put("moduleprimarykey", "SalesReturnDetailId");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put(Constants.moduleid, isConsignment?Constants.Acc_ConsignmentSalesReturn_ModuleId:isFixedAsset?Constants.Acc_FixedAssets_Sales_Return_ModuleId:isLeaseFixedAsset?Constants.Acc_Lease_Return:Constants.Acc_Sales_Return_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    DOMap.put(Constants.Acc_id, row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_SalesReturnDetails_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        DOMap.put("srdetailscustomdataref", row.getID());
                        accInvoiceDAOobj.updateSRDetailsCustomData(DOMap);
                    }
                }
                
                    // Add Custom fields details for Product
                if (!StringUtil.isNullOrEmpty(jobj.optString("productcustomfield", ""))) {
                    JSONArray jcustomarray = new JSONArray(jobj.optString("productcustomfield", "[]"));
                    HashMap<String, Object> srMap = new HashMap<String, Object>();
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "SrProductCustomData");
                    customrequestParams.put("moduleprimarykey", "SrDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                    customrequestParams.put(Constants.companyKey, companyid);
                    srMap.put(Constants.Acc_id, row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_SRDetail_Productcustom_data_classpath);
                    /*
                     * Rich Text Area is put in json if User have not selected any data for this field. ERP-ERP-37624
                     */
                    customrequestParams.put("productIdForRichRext", row.getInventory().getProduct().getID());                    
                    fieldDataManagercntrl.setRichTextAreaForProduct(customrequestParams);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        srMap.put("srdetailscustomdataref", row.getID());
                        accInvoiceDAOobj.updateSRDetailsProductCustomData(srMap);
                    }
                }
                
                if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray( StringUtil.DecodeText((String) jobj.optString("LineTermdetails")));
                    if (extraCompanyPreferences.isAvalaraIntegration()) {//When Avalara Integration is enabled, we save tax details in table 'TransactionDetailAvalaraTaxMapping'
                        JSONObject paramsJobj = new JSONObject();
                        paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                        paramsJobj.put(Constants.companyKey, companyid);
                        if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {
                            JSONObject saveTaxParamsJobj = new JSONObject();
                            saveTaxParamsJobj.put(IntegrationConstants.parentRecordID, row.getID());
                            saveTaxParamsJobj.put(IntegrationConstants.avalaraTaxDetails, StringUtil.DecodeText(jobj.optString("LineTermdetails")));
                            integrationCommonService.saveTransactionDetailTaxMapping(saveTaxParamsJobj);
                        }
                    } else {
                        for (int j = 0; j < termsArray.length(); j++) {
                            HashMap<String, Object> SalesReturnDetailsTermsMap = new HashMap<String, Object>();
                            JSONObject termObject = termsArray.getJSONObject(j);

                            if (termObject.has("termid")) {
                                SalesReturnDetailsTermsMap.put("term", termObject.get("termid"));
                            }
                            if (termObject.has("termamount")) {
                                SalesReturnDetailsTermsMap.put("termamount", termObject.get("termamount"));
                            }
                            if (termObject.has("termpercentage")) {
                                SalesReturnDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                            }
                            if (termObject.has("assessablevalue")) {
                                SalesReturnDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                            }
                            if (termObject.has("purchasevalueorsalevalue")) {
                                SalesReturnDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                            }
                            if (termObject.has("deductionorabatementpercent")) {
                                SalesReturnDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                            }

                            if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                                SalesReturnDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                                if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                    if(termObject.getInt("taxtype")==0){ // If Flat
                                        SalesReturnDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                    } else { // Else Percentage
                                        SalesReturnDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                    }
                                }
                            }
                            SalesReturnDetailsTermsMap.put("salesReturnDetailID", row.getID());
                            /**
                             * ERP-32829 
                             */
                            SalesReturnDetailsTermsMap.put("isDefault", termObject.optString("isDefault", "false"));
                            SalesReturnDetailsTermsMap.put("productentitytermid", termObject.optString("productentitytermid"));
                            SalesReturnDetailsTermsMap.put("product", jobj.get(Constants.productid));
                            SalesReturnDetailsTermsMap.put("userid", paramJobj.getString(Constants.useridKey));
                            accInvoiceDAOobj.saveSalesReturnDetailsTermMap(SalesReturnDetailsTermsMap);
                        }
                        if (extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                            /**
                             * Save GST History Customer/Vendor data.
                             */
                            jobj.put("detaildocid", row.getID());
                            jobj.put("moduleid", Constants.Acc_Sales_Return_ModuleId);
                            fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jobj);
                        }
                    }
                }
                  
                srRowsDetails.add(row);
                
            }
            if (countryid == Constants.indian_country_id && RCMApplicable && !productNameRCMNotActivate.isEmpty()) {
                throw new AccountingException(messageSource.getMessage("acc.common.rcmforproductnotactivated.SR.text", new Object[]{StringUtils.join(productNameRCMNotActivate, ", ")}, null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
            }
            salesReturn.setRows(srRowsDetails);
            returnDetails.add(srRowsDetails);
            returnDetails.add(cndetails);
            returnDetails.add(jedetails);
            returnDetails.add(cnTaxEntryDetails);
            returnDetails.add(totalCNAmt);
            returnDetails.add(totalCNAmtExludingTax);
            returnDetails.add(totalDiscountAmt);
            returnDetails.add(sumOfDiscountAmount);
            
            //if consignment Return is done without QA approval then entry will go here
            if(extraCompanyPreferences!=null && extraCompanyPreferences.isActivateInventoryTab()&& !stockMovementsList.isEmpty() && !isQAinspection){
                    stockMovementService.addOrUpdateBulkStockMovement(salesReturn.getCompany(), salesReturn.getID(), stockMovementsList);
            }
            //if consignment Return is done without QA approval then entry will go here
            if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !ConsignmentList.isEmpty() && isConsignment && isQAinspection) {
                consignmentService.deletePreviousConsignmentQAForSR(salesReturn.getCompany(), salesReturn.getID()); // this is for salesreturn edit case #ERP-19731
                consignmentService.addOrUpdateConsignment(salesReturn.getCompany(), salesReturn.getID(), ConsignmentList);
                addStockMovementForConsignmentQA(extraCompanyPreferences, ConsignmentList);
            }
            
        } catch (InventoryException ex) {
            throw ServiceException.FAILURE("saveSalesReturnRows : " + ex.getMessage(), ex);    
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveSalesReturnRows : " + ex.getMessage(), ex);
        }
        return returnDetails;
    } 
  
  @Override
    //Function used to delete entries in temporary table
    public void deleteEntryInTemp(Map deleteparam) {

        try {
            String companyId = deleteparam.get(Constants.companyKey).toString();
            /*
             * Delete Data from temprorary table for Sales Return
             */
            if (deleteparam.containsKey("salesreturnno")) {
                String salesreturnno = deleteparam.get("salesreturnno").toString();
                String cnno = deleteparam.get("cnno").toString();
                accCommonTablesDAO.deleteTransactionInTemp(salesreturnno, companyId, Constants.Acc_Sales_Return_ModuleId);
                if (deleteparam.containsKey("isautocreatecn") && deleteparam.get("isautocreatecn") != null) {
                    accCommonTablesDAO.deleteTransactionInTemp(cnno, companyId, Constants.Acc_Credit_Note_ModuleId);
                }

                /*
                 * Delete Data from temprorary table for Purchase Return
                 */
            } else if (deleteparam.containsKey("purchaseReturnNo")) {
                String purchaseReturnNo = deleteparam.get("purchaseReturnNo").toString();
                String dnNo = deleteparam.get("dnNo").toString();
                accCommonTablesDAO.deleteTransactionInTemp(purchaseReturnNo, companyId, Constants.Acc_Purchase_Return_ModuleId);
                if (deleteparam.containsKey("isAutoCreateDn") && deleteparam.get("isAutoCreateDn") != null) {
                    accCommonTablesDAO.deleteTransactionInTemp(dnNo, companyId, Constants.Acc_Debit_Note_ModuleId);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
  
    public void updateBatchDetailsForSO(HashMap<Integer, Object[]> grBatchdetalisMap, String productId, Inventory inventory, JSONObject paramJobj, String documentId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException {
        DateFormat df = authHandler.getDateOnlyFormat();
        String companyid = paramJobj.getString(Constants.companyKey);
        String userid = paramJobj.getString(Constants.useridKey);
        boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit")) ? false : Boolean.parseBoolean(paramJobj.getString("isEdit"));
        boolean activateCRblockingWithoutStock = false;
        User user = null;
        KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
        activateCRblockingWithoutStock = extraCompanyPreferences.isActivateCRblockingWithoutStock();
        if (!StringUtil.isNullOrEmpty(userid)) {
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            user = (User) jeresult.getEntityList().get(0);
        }
        // if CRBlockingWithoutStock feature is activated then procceed further
        if (activateCRblockingWithoutStock) {

            // get Pending consignment requests 
            KwlReturnObject pendingReqList = getPendingConsignmentRequests(companyid, productId);

            if (pendingReqList != null && pendingReqList.isSuccessFlag() && pendingReqList.getRecordTotalCount() > 0) {

                List<SalesOrder> consReqList = (List<SalesOrder>) pendingReqList.getEntityList();

                /*
                 * this set is used to check whether serial is locked already or
                 * not.this has to be used bcoz somewhere in code sql query is
                 * used and somewhere hql is used (so hibernatetemplates session
                 * will be different) so changes made in Objects will not get
                 * reflected due to different hibernate session.So for this ,
                 * map is used to save locked serial until commit operation is
                 * performed.
                 */
                Set usedProductBatchSerialSet = new HashSet();

                // Sales Order for loop
                for (int i = 0; i < consReqList.size(); i++) {

                    SalesOrder so = consReqList.get(i);
                    String serialNames = "", batchNames = "", auditMessage = "", sonumber = "";
                    sonumber = so.getSalesOrderNumber();
                    Set<SalesOrderDetail> rows = so.getRows();
                    //Sales Order Detail for loop
                    for (SalesOrderDetail soDetail : rows) {

                        try {
                            Product product = soDetail.getProduct();
                            HashMap<Integer, Object[]> BatchdetalisMap = new HashMap<Integer, Object[]>();
                            KwlReturnObject kmsg = null;
                            int batchcnt = 0;
                            int cnt = 0;
                            boolean isquantityNotavl = false;  //this flag is used to check whether serial batch quantity is avilabale 

                            //get products batch serial list that is available (ie. non-locked)
                            if (product.isIsBatchForProduct() && product.isIsSerialForProduct() && product.isIslocationforproduct() && product.isIswarehouseforproduct()) {
                                kmsg = accCommonTablesDAO.getBatchSerialDetailsforProduct(productId, product.isIsSerialForProduct(), isEdit, paramJobj);
                                List batchList = kmsg.getEntityList();
                                Iterator bitr = batchList.iterator();
                                while (bitr.hasNext()) {
                                    Object[] ObjBatchrow = (Object[]) bitr.next();
                                    BatchdetalisMap.put(cnt++, ObjBatchrow);
                                }
                            }

                            String sodetailsid = soDetail.getID();
                            double lockquantitydue = soDetail.getLockquantitydue();
                            int cntp = (int) lockquantitydue;

                            for (int j = 0; j < cntp; j++) {

                                for (int serialCnt = 0; serialCnt < cnt; serialCnt++) {

                                    Object[] objArr = BatchdetalisMap.get(serialCnt);

                                    if (objArr != null) {

                                        String serialId = objArr[0] != null ? (String) objArr[0] : "";
                                        String batchId = objArr[1] != null ? (String) objArr[1] : "";
                                        String batchname = objArr[2] != null ? (String) objArr[2] : "";
                                        String warehouse = objArr[10] != null ? (String) objArr[10] : "";
                                        String location = objArr[11] != null ? (String) objArr[11] : "";
                                        String serialname = objArr[12] != null ? (String) objArr[12] : "";

                                        Date mfgDateObj = null;
                                        Date expDateObj = null;

                                        String checkInSet = product.getID() + batchId + serialId;

                                        if (!usedProductBatchSerialSet.contains(checkInSet)) {

                                            if (objArr[3] != null) { //ie mfgdate is not null
                                                java.sql.Timestamp mfgdatets = (java.sql.Timestamp) objArr[3];
                                                mfgDateObj = new Date(mfgdatets.getTime());
                                            }
                                            if (objArr[4] != null) { //ie expdate is not null
                                                java.sql.Timestamp expdatets = (java.sql.Timestamp) objArr[4];
                                                expDateObj = new Date(expdatets.getTime());
                                            }

                                            if (!StringUtil.isNullOrEmpty(serialname)) {
                                                serialNames += "'" + serialname + "',";
                                            }

                                            if (!StringUtil.isNullOrEmpty(serialNames)) {
                                                serialNames = serialNames.substring(0, serialNames.length() - 1);
                                            }
                                            if (!StringUtil.isNullOrEmpty(batchname)) {
                                                batchNames += "'" + batchname + "',";
                                            }

                                            if (!StringUtil.isNullOrEmpty(batchNames)) {
                                                batchNames = batchNames.substring(0, batchNames.length() - 1);
                                            }

                                            if (!StringUtil.isNullOrEmpty(sodetailsid) && !StringUtil.isNullOrEmpty(batchId) && !StringUtil.isNullOrEmpty(serialId)) {
                                                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                                                documentMap.put("quantity", "1");
                                                documentMap.put("documentid", sodetailsid);
                                                documentMap.put("transactiontype", "20");//This is SO Type Tranction   sales order moduleid

                                                if (mfgDateObj != null) {
                                                    documentMap.put("mfgdate", mfgDateObj);
                                                }
                                                if (expDateObj != null) {
                                                    documentMap.put("expdate", expDateObj);
                                                }
                                                documentMap.put("batchmapid", batchId);
                                                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);

                                                HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                                                batchUpdateQtyMap.put(Constants.Acc_id, batchId);
                                                batchUpdateQtyMap.put("lockquantity", "1");
                                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);



                                                HashMap<String, Object> serialdocumentMap = new HashMap<String, Object>();
                                                serialdocumentMap.put("quantity", "1");
                                                serialdocumentMap.put("documentid", sodetailsid);

                                                if (mfgDateObj != null) {
                                                    serialdocumentMap.put("mfgdate", mfgDateObj);
                                                }
                                                if (expDateObj != null) {
                                                    serialdocumentMap.put("expdate", expDateObj);
                                                }
                                                serialdocumentMap.put("serialmapid", serialId);
                                                serialdocumentMap.put("transactiontype", "20");//This is so Type Tranction  

                                                HashMap<String, Object> requestParams = new HashMap<String, Object>();

                                                requestParams.put(Constants.companyKey, companyid);
                                                if (!StringUtil.isNullOrEmpty(userid)) {
                                                    requestParams.put("requestorid", userid);
                                                }
                                                if (!StringUtil.isNullOrEmpty(warehouse)) {
                                                    requestParams.put("warehouse", warehouse);
                                                }
                                                if (!StringUtil.isNullOrEmpty(location)) {
                                                    requestParams.put("location", location);
                                                }

                                                //code to Apply Pending Approval Rule
                                                KwlReturnObject ruleResult = accMasterItemsDAOobj.CheckRuleForPendingApproval(requestParams);
                                                List<ConsignmentRequestApprovalRule> list = ruleResult.getEntityList();
                                                Set<User> approverSet = null;
                                                boolean isRequestPending = false;
                                                for (ConsignmentRequestApprovalRule approvalRule : list) {
                                                    if (approvalRule != null) {
                                                        KwlReturnObject res = getConsignmentRequestApproverList(approvalRule.getID());
                                                        List<User> userlist = res.getEntityList();
                                                        Set<User> users = new HashSet<User>();;
                                                        for (User us : userlist) {
                                                            users.add(us);
                                                        }
                                                        approverSet = users;
                                                        isRequestPending = true;
                                                        break;
                                                    }
                                                }
                                                if (isRequestPending) {
                                                    serialdocumentMap.put("requestpendingapproval", RequestApprovalStatus.PENDING);
                                                    serialdocumentMap.put("approver", approverSet);
                                                }

                                                accCommonTablesDAO.saveSerialDocumentMapping(serialdocumentMap);

                                                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                                                serialUpdateQtyMap.put("lockquantity", "1");
                                                serialUpdateQtyMap.put(Constants.Acc_id, serialId);
                                                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);

                                                String setName = product.getID() + batchId + serialId;
                                                usedProductBatchSerialSet.add(setName);

                                                batchcnt += 1;
                                                break;
                                            }
                                        }

                                    } else {
                                        isquantityNotavl = true;  //if quantity is not available then break and come out of for loop
                                        break;
                                    }

                                }
                            }
                            accCommonTablesDAO.updateSOLockQuantitydue(sodetailsid, batchcnt, companyid);
                            if (isquantityNotavl) {
                                break;
                            }

                        } catch (Exception ex) {
                            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                    auditMessage = "User " + user.getFullName() + " has assigned Stock for Request No: " + sonumber + " With Batch: " + batchNames + " and With Serials: " + serialNames + ", " + auditMessage;
                    auditTrailObj.insertAuditLog(AuditAction.STOCK_AUTOASSIGNED, auditMessage, auditRequestParams, "0");
                }

            }
        }
    }

    @Override
    public JSONObject validateToedit(String formRecord, String billid, boolean isConsignment, Company company) {
        JSONObject jObj = new JSONObject();

        try {
            String msg = accSalesOrderDAOObj.validateToedit(formRecord, billid, isConsignment, company);
            if (!StringUtil.isNullOrEmpty(msg)) {

                jObj.put("msg", msg);
                jObj.put("success", false);
            } else {
                jObj.put("msg", msg);
                jObj.put("success", true);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jObj;
    }
    
    @Override //erp-278076 
    public JSONObject getSalesReturnSummaryReport(JSONObject paramJobj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = accInvoiceServiceDAO.getDeliveryOrdersMapJSON(paramJobj);
            String companyid = paramJobj.getString(Constants.companyKey);
            String gcurrencyid = paramJobj.getString(Constants.globalCurrencyKey);
            String moduleid = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.moduleid, null)) ? "" : paramJobj.getString(Constants.moduleid);
            requestParams.put(Constants.moduleid, moduleid);
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put(Constants.globalCurrencyKey, gcurrencyid);
            KwlReturnObject extracompanyResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) extracompanyResult.getEntityList().get(0);
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                Map<String, Object> salesPersonParams = new HashMap<>();
                salesPersonParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                salesPersonParams.put(Constants.companyKey, companyid);
                salesPersonParams.put("grID", "15");
                KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
                List<MasterItem> masterItems = masterItemByUserList.getEntityList();
                String salesPersons = "";
                StringBuffer salesPersonids = new StringBuffer();
                for (Object obj : masterItems) {
                    if (obj != null) {
                        salesPersonids.append(obj.toString() + ",");
}
                }
                if (salesPersonids.length() > 0) {
                    salesPersons = salesPersonids.substring(0, (salesPersonids.length() - 1));
                    requestParams.put("salesPersonid", salesPersons);
                }
            }
            boolean isSalesReturnCreditNote = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isNoteReturns", null))) {
                isSalesReturnCreditNote = Boolean.parseBoolean(paramJobj.getString("isNoteReturns"));
            }
            requestParams.put("isSalesReturnCreditNote", isSalesReturnCreditNote);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.Acc_Search_Json, null))) {
                requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json, null));
                requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid));
                requestParams.put("filterConjuctionCriteria", paramJobj.optString("filterConjuctionCriteria"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("linknumber", null))) {
                requestParams.put("linknumber", paramJobj.getString("linknumber"));
            }
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir", null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("sort", null))) {
                dir = paramJobj.getString("dir");
                sort = paramJobj.getString("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.prodfiltercustid, null))) {
                requestParams.put(Constants.prodfiltercustid, paramJobj.optString(Constants.prodfiltercustid));
            }
            
            KwlReturnObject result = accInvoiceDAOobj.getSalesReturn(requestParams);
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getSalesReturnSummaryReportByCustomerJson(paramJobj, result.getEntityList(), requestParams).getJSONArray("data");
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccSalesReturnServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                throw ServiceException.FAILURE("AccSalesOrderServiceImpl.getSalesReturnSummaryReport : " + ex.getMessage(), ex);
            }
        }
        return jobj;
    }

    //Sales Return Summary Report by Customer   
    public JSONObject getSalesReturnSummaryReportByCustomerJson(JSONObject paramJObj, List<Object[]> list, Map requestParams) throws SessionExpiredException, ServiceException, UnsupportedEncodingException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            String companyid = paramJObj.getString(Constants.companyKey);

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            cap = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) cap.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJObj.getString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//Iterating SalesReturn ids
            for (Object[] oj : list) {
                double totalamount=0;
                String orderid = oj[0].toString();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), orderid);
                SalesReturn salesReturn = (SalesReturn) objItr.getEntityList().get(0);
                CreditNote creditNote = null;
                if (salesReturn.isIsNoteAlso()) {
                    KwlReturnObject creditnoteresult = accCreditNoteDAOobj.getCreditNoteIdFromSRId(salesReturn.getID(), companyid);
                    if (!creditnoteresult.getEntityList().isEmpty()) {
                        creditNote = (CreditNote) creditnoteresult.getEntityList().get(0);
                    }
                }
                Customer customer = salesReturn.getCustomer();
                KwlReturnObject resultavaibaleQty = null;

                HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                filter_names.add("salesReturn.ID");
                order_by.add("srno");
                order_type.add("asc");
                doRequestParams.put("filter_names", filter_names);
                doRequestParams.put("filter_params", filter_params);
                doRequestParams.put("order_by", order_by);
                doRequestParams.put("order_type", order_type);
                filter_params.add(salesReturn.getID());
                KwlReturnObject podresult = accInvoiceDAOobj.getSalesReturnDetails(doRequestParams);
                List<SalesReturnDetail> salesreturndetaillist = podresult.getEntityList();

                //SalesReturn Row Details
                for (SalesReturnDetail row : salesreturndetaillist) {
                    JSONObject obj = new JSONObject();
                    boolean isLocationForProduct = false;
                    boolean isWarehouseForProduct = false;
                    boolean isBatchForProduct = false;
                    boolean isSerialForProduct = false;
                    boolean isRowForProduct = false;
                    boolean isRackForProduct = false;
                    boolean isBinForProduct = false;
                    String description = "";
                    obj.put("customerNameValue", customer.getName());
                    obj.put(Constants.billid, salesReturn.getID());
                    obj.put("billno", salesReturn.getSalesReturnNumber());
                    obj.put("externalcurrencyrate", salesReturn.getExternalCurrencyRate());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("currencysymbol", (salesReturn.getCurrency() == null ? currency.getSymbol() : salesReturn.getCurrency().getSymbol()));
                    obj.put(Constants.productid, row.getProduct().getID());
                    obj.put("prtaxid", (row.getTax() != null) ? row.getTax().getID() : "");
                    obj.put("reason", (row.getReason() != null) ? row.getReason().getID() : "");
                    if (paramJObj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                        obj.put("productidValue", row.getProduct().getName());
                        obj.put("productID", row.getProduct().getProductid());
                        if (row.getInventory().getUom() != null) {
                            obj.put("uomname", row.getInventory().getUom().getID());
                        } else {
                            obj.put("uomname", row.getInventory().getProduct().getUnitOfMeasure() != null ? row.getInventory().getProduct().getUnitOfMeasure().getID() : "");
                        }
                        obj.put("uomnameValue", row.getInventory().getUom() != null ? row.getInventory().getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                        obj.put(Constants.prtaxidValue, row.getTax() == null ? "" : row.getTax().getName());
                        obj.put("reasonValue", (row.getReason() != null) ? row.getReason().getValue() : "");
                    } else {
                        obj.put("pid", row.getProduct().getProductid());
                        if (row.getInventory().getUom() != null) {
                            obj.put("uomid", row.getInventory().getUom().getID());
                        } else {
                            obj.put("uomid", row.getInventory().getProduct().getUnitOfMeasure() != null ? row.getInventory().getProduct().getUnitOfMeasure().getID() : "");
                        }
                        obj.put("productname", row.getProduct().getName());
                        obj.put("unitname", row.getInventory().getUom() != null ? row.getInventory().getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    }

                    obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                        description = row.getDescription();
                    } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                        description = row.getProduct().getDescription();
                    } else {
                        description = "";
                    }
                    obj.put("taxamount", row.getRowTaxAmount());
                    obj.put("taxamountforlinking", row.getRowTaxAmount());
                    if (paramJObj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                        obj.put("discountType", row.getDiscountispercent());
                    } else {
                        obj.put("discountispercent", row.getDiscountispercent());
                    }
                    obj.put("prdiscount", row.getDiscount());
                    obj.put("quantity", row.getActualQuantity());
                    obj.put("dquantity", row.getReturnQuantity());
                    double baseuomrate = row.getInventory().getBaseuomrate();
                    obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(row.getReturnQuantity(), baseuomrate, companyid));
                    obj.put("baseuomrate", baseuomrate);
                    obj.put("isNoteAlso", salesReturn.isIsNoteAlso());

                    if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                        if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                            obj.put("batchdetails", accInvoiceServiceDAO.getNewBatchJson(row.getProduct(), paramJObj, row.getID()));
                        }
                    }

                    obj.put("copyquantity", row.getReturnQuantity());
                    
                    String linedesc = "";//Description is encoded for Web-application & Mobile Apps
                    try {
                        linedesc =  StringUtil.DecodeText(description);
                    } catch (Exception ex) {
                        linedesc = description;
                    }
                    obj.put("desc", linedesc);
                    obj.put("description", linedesc);
                    obj.put("partno", (row.getPartno() != null) ? row.getPartno() : "");
                    obj.put("memo", row.getRemark());
                    obj.put("remark", row.getRemark());
                    obj.put("rate", row.getRate());
                    obj.put("priceSource", row.getPriceSource() != null ? row.getPriceSource() : "");

                    if (paramJObj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                        double amount = row.getRate() * row.getReturnQuantity();
                        double rdisc = row.getDiscount();
                        double discountvalue = 0;
                        if (row.getDiscountispercent() == 1 && rdisc != 0) {//rdisc!=0 because it gives Nan Value
                            discountvalue = (rdisc / amount) * 100;
                        } else {
                            discountvalue = rdisc;
                        }
                        double rowtaxamount = row.getRowTaxAmount();
                        double amountWithTax = amount - discountvalue + rowtaxamount;
                        totalamount +=amountWithTax;
                        obj.put("amount", amountWithTax);
                        //ERP-41214:Show asterisk to unit price and amount 
                        //Handled for mobile Apps
                        if (paramJObj.has("displayUnitPriceAndAmountInSalesDocument") && !paramJObj.optBoolean("displayUnitPriceAndAmountInSalesDocument")) {
                            obj.put("amount", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS); 
                            obj.put("rate", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS);
                        }
                    }

                    if (extraCompanyPreferences.getProductOptimizedFlag() != Constants.Show_all_Products) {
                        resultavaibaleQty = accProductObj.getQuantity(row.getProduct().getID());
                        obj.put("availablequantity", (resultavaibaleQty.getEntityList().get(0) == null ? 0 : resultavaibaleQty.getEntityList().get(0)));

                        KwlReturnObject result2 = accProductObj.getAssemblyLockQuantity(row.getProduct().getID());//get the lock quantity of assembly type of product locked in SO
                        Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));

                        KwlReturnObject result1 = accProductObj.getLockQuantity(row.getProduct().getID());
                        Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));

                        obj.put("lockquantity", assmblyLockQuantity + SoLockQuantity);
                    }
                    obj.put("recTermAmount", row.getRowtermamount());
                    jArr.put(obj);
                }//end of salesreturndetails
            }//end of salesreturn
            
            jobj.put(Constants.data, jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInvoiceJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    
    /**
     * Description: build default Batch Serial JSON for warehouse and location-Mobile Apps (POS and Android)
     * @param paramJobj
     * @return JSONObject
     */
    public JSONObject buildBatchSerialJson(JSONObject paramJObj, JSONObject detailObj, Product prodObj) throws ServiceException, ParseException, SessionExpiredException {
        JSONObject detailsJSON = detailObj;
        JSONArray jArray = new JSONArray();
        try {

            if (prodObj != null && paramJObj.has("storeid") && !StringUtil.isNullOrEmpty(paramJObj.optString("storeid"))) {
                JSONObject jObj = new JSONObject();
                jObj.put("location", prodObj.getLocation() != null ? prodObj.getLocation().getId() : "");
                jObj.put("warehouse", paramJObj.optString("storeid"));
                jObj.put("productid", prodObj.getID());
                jObj.put("documentid", "");
                jObj.put("purchasebatchid", "");
                jObj.put("quantity", detailObj.optString("dquantity"));
                jObj.put("stocktype","1");
                jArray.put(jObj);
            }
            detailsJSON.put("batchdetails", jArray.toString());
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        }
        return detailsJSON;
    } 
    
    
}
