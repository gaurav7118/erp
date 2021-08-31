/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.spring.accounting.account.accAccountControllerCMNService;
import com.krawler.spring.accounting.receipt.AccReceiptServiceDAO;
import com.krawler.spring.accounting.reports.ExportLedger;
import com.krawler.spring.accounting.reports.accReportsController;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;



/**
 *
 * @author krawler
 */

public class SyncAllHandler implements Runnable {
    
    private String df = "yyyy-MM-dd";
    private String df_full = "yyyy-MM-dd hh:mm:ss";
    private String df_customfield = "MMM dd, yyyy hh:mm:ss aaa";
    private String EmailRegEx = "^[\\w-]+([\\w!#$%&'*+/=?^`{|}~-]+)*(\\.[\\w!#$%&'*+/=?^`{|}~-]+)*@[\\w-]+(\\.[\\w-]+)*(\\.[\\w-]+)$";
    private String TimeRegEx = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private int importLimit = 1500;
    private static String[] masterTables = {"MasterItem"};
    private HibernateTransactionManager txnManager;
    private ImportDAO importDao;
    private kwlCommonTablesDAO KwlCommonTablesDAOObj;
    private accAccountControllerCMNService accAccountControllerCMNServiceObj;
    private AccInvoiceModuleService accInvoiceModuleService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccReceiptServiceDAO accReceiptServiceDAO;
    private MessageSource messageSource;
    private authHandlerDAO authHandlerDAOObj;
   
    ArrayList processQueue = new ArrayList();

     public void setaccAccountControllerCMNServiceObj(accAccountControllerCMNService accAccountControllerCMNServiceObj) {
        this.accAccountControllerCMNServiceObj = accAccountControllerCMNServiceObj;
    }
      public void setAccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }
   public void setKwlCommonTablesDAO(kwlCommonTablesDAO KwlCommonTablesDAOObj1) {
        this.KwlCommonTablesDAOObj = KwlCommonTablesDAOObj1;
    }
   public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    public void setAccReceiptServiceDAO(AccReceiptServiceDAO accReceiptServiceDAO) {
        this.accReceiptServiceDAO = accReceiptServiceDAO;
    }

    public void add(HashMap<String, Object> requestParams) {
        try {
            processQueue.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(SyncAllHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
    try {
            HashMap<String, Object> requestParams1 = (HashMap<String, Object>) processQueue.get(0);
            while (!processQueue.isEmpty()) {
                HashMap<String, Object> requestParams = (HashMap<String, Object>) processQueue.get(0);
                String threadRequest = (String)requestParams.get("threadRequest");
                try {
                   
                    if (threadRequest.equalsIgnoreCase("Dimension")) {
                        
                        accAccountControllerCMNServiceObj.syncCustomFieldDataFromOtherProjects(requestParams);
                        
                    } else if (threadRequest.equalsIgnoreCase("Product")) {

                        accAccountControllerCMNServiceObj.getLMSCourcesAsProducts(requestParams);
                        
                    } else if (threadRequest.equalsIgnoreCase("Customer")) {
                        
                        accAccountControllerCMNServiceObj.getCustomerFromLMS(requestParams);
                        
                    } else if (threadRequest.equalsIgnoreCase("Invoice")) {
                        
                        accInvoiceModuleService.getInvoiceFromLMS(requestParams);
                        
                    }else if (threadRequest.equalsIgnoreCase("Receipt")) {
                        
                        accReceiptServiceDAO.getReceiptFromLMS(requestParams);
                        
                    }
                 
                } catch (Exception ex) {
                    Logger.getLogger(SyncAllHandler.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    processQueue.remove(requestParams);
                }
            }
             SendMail(requestParams1);  
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
        }
    }
    
     public void SendMail(HashMap requestParams) throws ServiceException {


        String loginUserId = (String) requestParams.get("userid");

        KwlReturnObject KWLUser = accountingHandlerDAOobj.getObject(User.class.getName(), loginUserId);
        User user = (User) KWLUser.getEntityList().get(0);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
       

        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = "Sync All From LMS Status";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                htmlTextC += "<br/>Sync data from LMS <b>has been completed  successfully.</b><br/>";

                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                plainMsgC += "\nSync data from LMS  <b> has been completed  successfully.</b><br/>\n";
                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nERP System\n";

                
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                try {
                    SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
                } catch (MessagingException ex1) {
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, "ExportLedger.SendMail :" + ex1.getMessage(), ex1);
                    SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
                }finally {
                    System.out.println("Mail Catch-1 Completed: " + new Date());
                }
                
            } catch (Exception ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }finally{
                System.out.println("Mail Catch-2 Completed: "+new Date());
            }
        }

    }
    
    
    
    
    
}
