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
package com.krawler.spring.accounting.handler;

import com.krawler.hql.accounting.GroupCompanyProcessMapping;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CustomerAddressDetails;
import com.krawler.common.admin.ExtraCompanyPreferences;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.companypreferenceservice.AccCompanyPreferencesServiceImpl;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.valuation.InventoryValuationProcess;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.companypreferences.UpdateExistingTransactionsWithMatchedSequenceformate;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.groupcompany.AccGroupCompanyDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.exportFuctionality.CreatePDF;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.base.JSONArray;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.security.auth.login.AccountException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AopAdvisor implements MethodInterceptor,MessageSourceAware {
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private accCreditNoteDAO accCreditNoteDao;
    private accReceiptDAO accReceiptDao;
    private accDebitNoteDAO accDebitNoteDao;
    private accVendorPaymentDAO accVendorPaymentDao;
    private CreatePDF CreatePDFObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accPurchaseOrderDAO accPurchaseOrderDAOobj;
    private MessageSource messageSource;
    private accInvoiceDAO accInvoiceDAOobj;
    private InventoryValuationProcess inventoryValuationProcess;
    private UpdateExistingTransactionsWithMatchedSequenceformate updateExistingTransactionsWithMatchedSequenceformate;

    public void setUpdateExistingTransactionsWithMatchedSequenceformate(UpdateExistingTransactionsWithMatchedSequenceformate updateExistingTransactionsWithMatchedSequenceformate) {
        this.updateExistingTransactionsWithMatchedSequenceformate = updateExistingTransactionsWithMatchedSequenceformate;
    }
    private HibernateTransactionManager txnManager;
    private AccGroupCompanyDAO accGroupCompanyDAO;
    private companyDetailsDAO companyDetailsDAOObj;

    public void setaccGroupCompanyDAO(AccGroupCompanyDAO accGroupCompanyDAO) {
        this.accGroupCompanyDAO = accGroupCompanyDAO;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj) {
        this.companyDetailsDAOObj = companyDetailsDAOObj;
    }
    
    public void setAccPurchaseOrderDAOobj(accPurchaseOrderDAO accPurchaseOrderDAOobj) {
        this.accPurchaseOrderDAOobj = accPurchaseOrderDAOobj;
    }
  
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
     public void setAccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
     public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDao) {
        this.accCreditNoteDao = accCreditNoteDao;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDao) {
        this.accReceiptDao = accReceiptDao;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteDao) {
        this.accDebitNoteDao = accDebitNoteDao;
    }

    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentDao) {
        this.accVendorPaymentDao = accVendorPaymentDao;
    }
    public void setCreatePDF(CreatePDF CreatePDFObj) {
        this.CreatePDFObj = CreatePDFObj;
    }
     public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setInventoryValuationProcess(InventoryValuationProcess inventoryValuationProcess) {
        this.inventoryValuationProcess = inventoryValuationProcess;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    
    public Object invoke(MethodInvocation mi) throws Throwable {
        Object valueReturn = null;
        
        ServletRequestAttributes ss = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        Object obj = mi.getThis();
        Class cls = mi.getThis().getClass();
        String className = cls.getSimpleName();
        boolean issuccess = true;

        if (className.equals("accInvoiceController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforInvoiceSaveMethods(ss.getRequest(), methodName);
                beforBillingInvoiceSaveMethods(ss.getRequest(), methodName);
                beforDeliveryOrderSaveMethods(ss.getRequest(), methodName);
                beforSalesReturnSaveMethods(ss.getRequest(), methodName);
                
                beforDeliveryOrderDeleteMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if(className.equals("AccWorkOrderControllerCMN")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforInvoiceSaveMethods(ss.getRequest(), methodName);
                beforBillingInvoiceSaveMethods(ss.getRequest(), methodName);
                beforDeliveryOrderSaveMethods(ss.getRequest(), methodName);
                beforSalesReturnSaveMethods(ss.getRequest(), methodName);
                
                beforDeliveryOrderDeleteMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                //afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn); //NO Need To Perform valuaion after work order close.
            }
        } else if (mi.getMethod().getName().equals("saveInventoryConsumption")) {
            String methodName =mi.getMethod().getName();
            className="MasterServiceImpl";
            JSONObject jobj = new JSONObject();
            String msg = "";
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
            if (issuccess) {
                valueReturn = mi.proceed();
                //afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn); //problem in ss and class name proxy otherwise OK
                afterrReturningSaveInventoryConsumptionMethod(className, methodName, valueReturn);
            }
        } else if (className.equals("accGoodsReceiptController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforGoodsReceiptSaveMethods(ss.getRequest(), methodName);
                beforBillingGoodsReceiptSaveMethods(ss.getRequest(), methodName);
                beforGoodsReceiptOrderSaveMethods(ss.getRequest(), methodName);
                beforPurchaseReturnSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accPurchaseOrderController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforRequisitionSaveMethods(ss.getRequest(), methodName);
                beforQuotationSaveMethods(ss.getRequest(), methodName);
                beforPurchaseOrderSaveMethods(ss.getRequest(), methodName);
                beforBillingPurchaseOrderSaveMethods(ss.getRequest(), methodName);
                beforePurchaseOrderDeleteMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                    valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accVendorPaymentController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforPaymentSaveMethods(ss.getRequest(), methodName);
                beforBillingPaymentSaveMethods(ss.getRequest(), methodName);
                beforContraPaymentSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        } else if (className.equals("accVendorPaymentControllerNew")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforPaymentSaveMethods(ss.getRequest(), methodName);
                beforBillingPaymentSaveMethods(ss.getRequest(), methodName);
                beforContraPaymentSaveMethods(ss.getRequest(), methodName);
                beforePaymentDeleteMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accDebitNoteController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforDebitNoteSaveMethods(ss.getRequest(), methodName);
                beforBillingDebitNoteSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accSalesOrderController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforSalesOrderSaveMethods(ss.getRequest(), methodName);
                beforBillingSalesOrderSaveMethods(ss.getRequest(), methodName);
                beforQuotationSaveMethods(ss.getRequest(), methodName);
                beforeSalesOrderDeleteMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accCreditNoteController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforCreditNoteSaveMethods(ss.getRequest(), methodName);
                beforBillingCreditNoteSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accReceiptController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforReceiptSaveMethods(ss.getRequest(), methodName);
                beforBillingReceiptSaveMethods(ss.getRequest(), methodName);
                beforContraReceiptSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        } else if (className.equals("accReceiptControllerNew")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforReceiptSaveMethods(ss.getRequest(), methodName);
                beforBillingReceiptSaveMethods(ss.getRequest(), methodName);
                beforContraReceiptSaveMethods(ss.getRequest(), methodName);
                beforeReceivePaymentDeleteMethods(ss.getRequest(), methodName);
                
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accJournalEntryController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforJournalEntrySaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accSalesReturnControllerCMN")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforSalesReturnSaveMethods(ss.getRequest(), methodName);
                beforPurchaseReturnSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accContractController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforContractSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
       }else if (className.equals("GoodsTransferController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforeInterStoreDeleteMethods(ss.getRequest(), methodName);
                beforeInterLocationDeleteMethods(ss.getRequest(), methodName);
                beforeStockRequestSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accInvoiceControllerCMN")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforeInvoiceDeleteMethods(ss.getRequest(), methodName);
                 beforeSalesReturnDeleteMethods(ss.getRequest(), methodName);
                /**
                 * Calling beforInvoiceSaveMethods when class name is
                 * accInvoiceControllerCMN as saveInvoice method from
                 * accInvoiceController is moved to accInvoiceControllerCMN the
                 * controller. ERM-736
                 */
                if (methodName.equals("saveInvoice")) {
                    beforInvoiceSaveMethods(ss.getRequest(), methodName);
                }
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("accGoodsReceiptControllerCMN")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforeGoodsReceiptDeleteMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                valueReturn = new ModelAndView("jsonView", "model", jobj.toString());
            
            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        }else if (className.equals("StockAdjustmentController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());

            JSONObject jobj = new JSONObject();
            String msg = "";
            try {
                beforeStockAdjustmentSaveMethods(ss.getRequest(), methodName);
            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    jobj.put("success", issuccess);
                    jobj.put("msg", msg);
                } catch (JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            valueReturn = new ModelAndView("jsonView", "model", jobj.toString());

            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        } else if (className.equals("CycleCountController") || className.equals("ApprovalController") || className.equals("accProductControllerCMN")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());
            JSONObject jobj = new JSONObject();
            String msg = "";
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        } else /**
             * Function will call after savesequenceformat format method from accCompanyPreferencesController.
             */
        if (className.equals("accCompanyPreferencesController")) {
            Method md = cls.getMethod("getMethodNameResolver");
            MethodNameResolver mnr = (MethodNameResolver) md.invoke(obj);
            String methodName = mnr.getHandlerMethodName(ss.getRequest());
            JSONObject jobj = new JSONObject();
            String msg = "";
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
            if (issuccess) {
                valueReturn = mi.proceed();
                afterReturningAOPMethods(ss.getRequest(), className, methodName, valueReturn);
            }
        } else {
            valueReturn = mi.proceed();
        }

        return valueReturn;
    }
   
 /*
  * @Description: when multi group companies is activated at company level then respective transactions is made.
  * @param: JSONObject paramJObj
  * @Mandatory fields: Destination Company and destination module
  */   
    
    private JSONObject processGroupCompanyTransactions(HttpServletRequest request, String className, String methodName, Object valueReturn) throws com.krawler.utils.json.base.JSONException, SessionExpiredException, ServiceException {
        JSONObject returnJobj = new JSONObject();
        StringBuilder msgString = new StringBuilder();
        try {

            if (GroupCompanyProcessMapping.ControllerNameSet.contains(className) && GroupCompanyProcessMapping.MethodSetForAccountPayable.contains(methodName)) {
                boolean isDeleteOperation = false;
                JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request);
                ModelAndView mv = (ModelAndView) valueReturn;
                Map map = mv.getModel();
                String model = (String) map.get(Constants.model);
                JSONObject jobj = new JSONObject(model);
                int moduleid = 0;
                StringBuilder urlString = new StringBuilder();
                
                //generate multiple PO option
                if (methodName.equals(GroupCompanyProcessMapping.GENERATE_PO_From_Multiple_SO)) {
                    methodName = GroupCompanyProcessMapping.SAVE_PURCHASE_ORDER;
                } else if (methodName.equals(GroupCompanyProcessMapping.GENERATE_GRN_From_Multiple_DO)) {//generate multiple GRN option
                    methodName = GroupCompanyProcessMapping.SAVE_GOODSRECEIPT_ORDER;
                }
                
                if (methodName.equals(GroupCompanyProcessMapping.SAVE_PURCHASE_ORDER)) {
                    moduleid = Constants.Acc_Purchase_Order_ModuleId;
                    urlString.append("convertpotoso");
                    paramJObj.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, jobj.optString(Constants.billid));//billid of 
                } else if ((methodName.equals(GroupCompanyProcessMapping.SAVE_PURCHASE_RETURN))) {
                    moduleid = Constants.Acc_Purchase_Return_ModuleId;
                    urlString.append("convertpurchasereturntosalesreturn");
                    paramJObj.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, jobj.optString(Constants.billid));//billid of 
                }else if ((methodName.equals(GroupCompanyProcessMapping.SAVE_GOODSRECEIPT_ORDER))) {
                    moduleid = Constants.Acc_Goods_Receipt_ModuleId;
                    urlString.append("convertgrntodo");
                    paramJObj.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, jobj.optString(Constants.billid));//billid of 
                } else if (methodName.equals(GroupCompanyProcessMapping.SAVE_PURCHASE_INVOICE)) {
                    moduleid = Constants.Acc_Vendor_Invoice_ModuleId;
                    urlString.append("convertpitosi");
                    paramJObj.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, jobj.optString("invoiceid"));//billid of 
                } else if (methodName.equals(GroupCompanyProcessMapping.DELETE_PURCHASEORDER_PERMANENT)) {
                    isDeleteOperation = true;
                    moduleid = Constants.Acc_Purchase_Order_ModuleId;
                    urlString.append("deletesalesorder");
                    paramJObj.put(Constants.deletepermanentflag, "true");
                } else if (methodName.equals(GroupCompanyProcessMapping.DELETE_PURCHASEORDER_TEMPORARY)) {
                    isDeleteOperation = true;
                    moduleid = Constants.Acc_Purchase_Order_ModuleId;
                    urlString.append("deletesalesorder");
                    paramJObj.put(Constants.deletepermanentflag, false);
                }else if (methodName.equals(GroupCompanyProcessMapping.SAVE_MAKE_PAYMENT)) {
                    moduleid = Constants.Acc_Make_Payment_ModuleId;
                    urlString.append("convertvendorpaymenttoreceivepayment");
                    paramJObj.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, jobj.optString("paymentid"));//billid of 
                } else if (methodName.equals(GroupCompanyProcessMapping.DELETE_PURCHASEINVOICE)) {
                    isDeleteOperation = true;
                    moduleid = Constants.Acc_Vendor_Invoice_ModuleId;
                    urlString.append("deleteinvoiceanddo");
                    paramJObj.put(Constants.deletepermanentflag, "true");
                } else if (methodName.equals(GroupCompanyProcessMapping.DELETE_PURCHASERETURN_PERMANENT)) {
                    isDeleteOperation = true;
                    moduleid = Constants.Acc_Purchase_Return_ModuleId;
                    urlString.append("deletesalesreturn");
                    paramJObj.put(Constants.deletepermanentflag, true);
                } else if (methodName.equals(GroupCompanyProcessMapping.DELETE_PURCHASERETURN_TEMPORARY)) {
                    isDeleteOperation = true;
                    moduleid = Constants.Acc_Purchase_Return_ModuleId;
                    urlString.append("deletesalesreturn");
                    paramJObj.put(Constants.deletepermanentflag, false);
                } else if (methodName.equals(GroupCompanyProcessMapping.DELETE_GOODSRECEIPTORDER_PERMANENT)) {
                    isDeleteOperation = true;
                    moduleid = Constants.Acc_Goods_Receipt_ModuleId;
                    urlString.append("deletedo");
                    paramJObj.put(Constants.deletepermanentflag, true);
                }else if (methodName.equals(GroupCompanyProcessMapping.DELETE_GOODSRECEIPTORDER_TEMPORARY)) {
                    isDeleteOperation = true;
                    moduleid = Constants.Acc_Goods_Receipt_ModuleId;
                    urlString.append("deletedo");
                    paramJObj.put(Constants.deletepermanentflag, false);
                } else if (methodName.equals(GroupCompanyProcessMapping.DELETE_MAKEPAYMENT_PERMANENT)) {
                    isDeleteOperation = true;
                    moduleid = Constants.Acc_Make_Payment_ModuleId;
                    urlString.append("deletereceivepayment");
                    paramJObj.put(Constants.deletepermanentflag, true);
                }else if (methodName.equals(GroupCompanyProcessMapping.DELETE_MAKEPAYMENT_TEMPORARY)) {
                    isDeleteOperation = true;
                    moduleid = Constants.Acc_Make_Payment_ModuleId;
                    urlString.append("deletereceivepayment");
                    paramJObj.put(Constants.deletepermanentflag,false);
                }
                
                String accURL = URLUtil.buildRestURL(Constants.ACCOUNTING_URL);
                String endpoint = accURL + "groupcompany/" + urlString.toString();
                System.out.println(endpoint);

                paramJObj.put(Constants.moduleid, moduleid);
                String sourceSubdomain = paramJObj.optString(Constants.RES_CDOMAIN);
                paramJObj.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourceSubdomain);
                paramJObj.put(GroupCompanyProcessMapping.SOURCE_MODULE, paramJObj.optString(Constants.moduleid));

                HashMap<String, Object> fieldrequestParams = new HashMap();

                if (!StringUtil.isNullOrEmpty(sourceSubdomain) && !StringUtil.isNullOrEmpty(paramJObj.optString(Constants.moduleid, null))) {
                    fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourceSubdomain);
                    fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, paramJObj.optString(Constants.moduleid));
                    KwlReturnObject multiCompanyreturnObj = accGroupCompanyDAO.fetchMultiCompanyDetails(fieldrequestParams);
                    List<GroupCompanyProcessMapping> multiCompanyObj = multiCompanyreturnObj.getEntityList();

                    // get MultiCompany Object on the basis of source companyid
                    boolean isSourceCompanyIdFlag = false;
                    if (multiCompanyObj.size() <= 0) {
                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANYID, paramJObj.optString(Constants.companyKey, null));
                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, paramJObj.optString(Constants.moduleid));
                        multiCompanyreturnObj = accGroupCompanyDAO.fetchMultiCompanyDetails(fieldrequestParams);
                        multiCompanyObj = multiCompanyreturnObj.getEntityList();
                        isSourceCompanyIdFlag = true;
                    }

                    if (multiCompanyObj.size() > 0) {
                        for (GroupCompanyProcessMapping multSetUpObj : multiCompanyObj) {
                            String destinationSubdomain = multSetUpObj.getDestinationCompany();
                            String destinationModule = multSetUpObj.getDestinationModule();
                            String sourceModule = multSetUpObj.getSourceModule();
                            String sourceCompanyid = multSetUpObj.getSourceCompanyId();
                            String existingSourceSubdomain = multSetUpObj.getSourceCompany();
                            String destinationCompanyid = multSetUpObj.getDestinationCompanyId();
                            paramJObj.put(Constants.companyKey, destinationCompanyid);

                            //updating subdomain in all the tables if companymapping occurs on the basis of companyid
                            if (isSourceCompanyIdFlag) {
                                fieldrequestParams = new HashMap<String, Object>();
                                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN_UPDATE, sourceSubdomain);
                                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, existingSourceSubdomain);
                                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationSubdomain);
                                KwlReturnObject subdomainResult = accGroupCompanyDAO.updateSubdomain(fieldrequestParams);
                            }

                            if (!StringUtil.isNullOrEmpty(destinationModule) && (!StringUtil.isNullOrEmpty(destinationSubdomain))) {
                                paramJObj.put(Constants.RES_CDOMAIN, destinationSubdomain);
                                paramJObj.put(Constants.moduleid, destinationModule);
                                paramJObj.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationSubdomain);
                                paramJObj.put(GroupCompanyProcessMapping.DESTINATION_MODULE, destinationModule);//as we are storing moduleids in String
                                paramJObj.put(Constants.isdefaultHeaderMap, true);
                                paramJObj.put(Constants.isMultiGroupCompanyFlag, true);

                                //Fetching linked document transactionid to perform delete operation 
                                if (isDeleteOperation) {
                                    JSONArray salesorderArray = new JSONArray(paramJObj.getString(Constants.RES_data));
                                    for (int i = 0; i < salesorderArray.length(); i++) {
                                        JSONObject salesJson = salesorderArray.getJSONObject(i);
                                        String billno="";
                                        fieldrequestParams = new HashMap<String, Object>();
                                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, salesJson.optString(Constants.billid));
                                        paramJObj.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, salesJson.optString(Constants.billid));
                                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, sourceModule);
                                        fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_MODULE, destinationModule);
                                        KwlReturnObject multiTransreturnObj = accGroupCompanyDAO.fetchTransactionMappingDetails(fieldrequestParams);
                                        List<GroupCompanyTransactionMapping> multiTransactionObj = multiTransreturnObj.getEntityList();

                                        for (GroupCompanyTransactionMapping multTransactionSetUpObj : multiTransactionObj) {
                                            String destinationTransactionId = multTransactionSetUpObj.getDestinationTransactionid();
                                            if (!StringUtil.isNullOrEmpty(destinationTransactionId)) {
                                                paramJObj.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, destinationTransactionId);
                                                if (!StringUtil.isNullOrEmpty(salesJson.optString("billno"))) {
                                                    billno = salesJson.optString("billno");
                                                }
                                                if (sourceModule.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))&& destinationModule.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                                    paramJObj =createReceivePaymentDeleteJSON(paramJObj, destinationTransactionId, destinationCompanyid, billno);
                                                } else {
                                                    paramJObj = createDeleteJSON(paramJObj, destinationTransactionId, destinationCompanyid, billno);
                                                }
                                                
                                                postData(endpoint, paramJObj.toString(), "POST");
                                                returnJobj.put(Constants.RES_success, true);
                                            } else {
                                                returnJobj.put(Constants.RES_success, false);
                                                msgString.append(messageSource.getMessage("acc.common.erp50", null, RequestContextUtils.getLocale(request)));
                                                returnJobj.put(Constants.RES_msg, msgString.toString());
                                            }
                                        }//end of for (GroupCompanyProcessMapping multSetUpObj : multiCompanyObj)
                                    }
                                } else {
                                    if (!StringUtil.isNullOrEmpty(paramJObj.optString(Constants.moduleid, null))) {
                                        JSONObject resObj = new JSONObject();
                                        System.out.println(endpoint);
                                        resObj=postData(endpoint, paramJObj.toString(), "POST");
                                        returnJobj.put(Constants.RES_success, true);
//                                        //If transaction fails then delete the transaction else 
//                                        if (resObj.has(Constants.RES_success) && resObj.optBoolean(Constants.RES_success) == false) {
//                                            if ((methodName.equals(GroupCompanyProcessMapping.SAVE_PURCHASE_ORDER))) {
//                                                moduleid = Constants.Acc_Purchase_Order_ModuleId;
//                                                urlString=new StringBuilder();
//                                                urlString.append("deletepurchaseorder");
//                                                String urlEndPoint = accURL + "groupcompany/" + urlString.toString();
//                                                paramJObj = createDeleteJSON(paramJObj, jobj.optString(Constants.billid), sourceCompanyid,jobj.optString(Constants.billno));
//                                                String dataString=paramJObj.optString(Constants.data,"[{}]");
//                                                JSONArray jsonarrayData=new JSONArray(dataString);
//                                                paramJObj.put(Constants.data,jsonarrayData);
//                                                 paramJObj.put(Constants.isMultiGroupCompanyFlag, true);
//                                                 paramJObj.put(Constants.companyKey, sourceCompanyid);
//                                                postData(urlEndPoint, paramJObj.toString(), "POST");
//                                            } else if ((methodName.equals(GroupCompanyProcessMapping.SAVE_PURCHASE_RETURN))) {
//                                                moduleid = Constants.Acc_Purchase_Return_ModuleId;
//                                                urlString=new StringBuilder();
//                                                urlString.append("deletepurchasereturn");
//                                                String urlEndPoint = accURL + "groupcompany/" + urlString.toString();
//                                                paramJObj = createDeleteJSON(paramJObj, jobj.optString(Constants.billid), sourceCompanyid,jobj.optString(Constants.billno));
//                                                 paramJObj.put(Constants.isMultiGroupCompanyFlag, true);
//                                                 paramJObj.put(Constants.companyKey, sourceCompanyid);
//                                                postData(urlEndPoint, paramJObj.toString(), "POST");
//                                            } else if (methodName.equals(GroupCompanyProcessMapping.SAVE_PURCHASE_INVOICE)) {
//                                                moduleid = Constants.Acc_Vendor_Invoice_ModuleId;
//                                                urlString = new StringBuilder();
//                                                urlString.append("deletevendorinvoiceandgrn");
//                                                String urlEndPoint = accURL + "groupcompany/" + urlString.toString();
//                                                paramJObj.put("responsekey",jobj);
//                                                paramJObj.put(GroupCompanyProcessMapping.SOURCE_COMPANYID,sourceCompanyid);
//                                                paramJObj.put(Constants.isMultiGroupCompanyFlag, true);
//                                                paramJObj.put(Constants.companyKey, sourceCompanyid);
//                                                postData(urlEndPoint, paramJObj.toString(), "POST");
//                                            }
//                                            else if (methodName.equals(GroupCompanyProcessMapping.SAVE_MAKE_PAYMENT)) {
//                                                moduleid = Constants.Acc_Make_Payment_ModuleId;
//                                                urlString=new StringBuilder();
//                                                urlString.append("deletemakepayment");
//                                                String urlEndPoint = accURL + "groupcompany/" + urlString.toString();
//                                                paramJObj.put(Constants.isMultiGroupCompanyFlag, true);
//                                                paramJObj.put(Constants.companyKey, sourceCompanyid);
//                                                paramJObj = createMakePaymentDeleteJSON(paramJObj, jobj.optString("paymentid"), sourceCompanyid,null);
//                                                postData(urlEndPoint, paramJObj.toString(), "POST");
//                                            }
//                                        } else {
//                                            returnJobj.put(Constants.RES_success, true);
//                                        }
                                    }
                                }
                            } else {
                                returnJobj.put(Constants.RES_success, false);
                                msgString.append(messageSource.getMessage("acc.common.erp50", null, RequestContextUtils.getLocale(request)));
                                returnJobj.put(Constants.RES_msg, msgString.toString());
                            }
                        }//end of for (GroupCompanyProcessMapping multSetUpObj : multiCompanyObj)
                    }
                }
            }//end of multicompanies flag
        } catch (com.krawler.utils.json.base.JSONException ex) {
            returnJobj.put(Constants.RES_success, false);
            msgString.append(messageSource.getMessage("acc.common.erp46", null, RequestContextUtils.getLocale(request)));
            returnJobj.put(Constants.RES_msg, msgString.toString());
        } catch (SessionExpiredException ex) {
            returnJobj.put(Constants.RES_success, false);
            msgString.append(messageSource.getMessage("acc.common.erp46", null, RequestContextUtils.getLocale(request)));
            returnJobj.put(Constants.RES_msg, msgString.toString());
        } catch (ServiceException ex) {
            returnJobj.put(Constants.RES_success, false);
            msgString.append(messageSource.getMessage("acc.common.erp46", null, RequestContextUtils.getLocale(request)));
            returnJobj.put(Constants.RES_msg, msgString.toString());
        }
        return returnJobj;
    }
  
 /*
  * @Description:Common method to create json for delete operation as json format is same for Sales Invoice, Sales Return and Sales Order.
  * @param: JSONObject paramJObj
  * @Mandatory fields:destinationTransactionId & destinationCompanyid
  */   
    
    private JSONObject createDeleteJSON(JSONObject paramJObj, String billid, String companyid, String billno) throws ServiceException, com.krawler.utils.json.base.JSONException {
        JSONObject deleteSOJson = paramJObj;
        JSONArray deletejArray = new JSONArray();
        JSONObject deleteJson = new JSONObject();
        deleteJson.put(Constants.billid, billid);
        if (!StringUtil.isNullOrEmpty(billno)) {
            deleteJson.put("billno", billno);
        }
        deletejArray.put(deleteJson);
        deleteSOJson.put(Constants.data, deletejArray.toString());
        deleteSOJson.put(Constants.companyKey, companyid);
        deleteSOJson.put(Constants.isdefaultHeaderMap, true);
        deleteSOJson.put(Constants.isMultiGroupCompanyFlag, true);

        return deleteSOJson;
    }
    
    /*
  * @Description:Common method to create json for delete operation as json format is same for Sales Invoice, Sales Return and Sales Order.
  * @param: JSONObject paramJObj
  * @Mandatory fields:destinationTransactionId & destinationCompanyid
  */   
    
    private JSONObject createMakePaymentDeleteJSON(JSONObject paramJObj, String billid, String companyid, String billno) throws ServiceException, com.krawler.utils.json.base.JSONException {
        JSONObject deleteSOJson = paramJObj;
        JSONArray deletejArray = new JSONArray();
        JSONObject deleteJson = new JSONObject();
        deleteJson.put(Constants.billid, billid);

        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), billid);
        Payment payment = (Payment) objItr.getEntityList().get(0);
        if (payment != null) {
            deleteJson.put("journalentryid", payment.getJournalEntry().getID());
            deleteJson.put("entryno", payment.getJournalEntry().getEntryNumber());
            if (!StringUtil.isNullOrEmpty(billno)) {
                deleteJson.put("billno", billno);
            } else {
                deleteJson.put("billno", payment.getPaymentNumber());
            }
        }
        if (paramJObj.has("creationdate")) {
            deleteJson.put("billdate", paramJObj.optString("creationdate"));
        }
        
        deletejArray.put(deleteJson);
        deleteSOJson.put(Constants.data, deletejArray.toString());
        deleteSOJson.put(Constants.companyKey, companyid);
        deleteSOJson.put(Constants.isdefaultHeaderMap, true);
        deleteSOJson.put(Constants.isMultiGroupCompanyFlag, true);
        return deleteSOJson;
    } 
    
    /*
     * @Description:Common method to create json for delete operation as json
     * format is same for Sales Invoice, Sales Return and Sales Order. @param:
     * JSONObject paramJObj @Mandatory fields:destinationTransactionId &
     * destinationCompanyid
     */
    private JSONObject createReceivePaymentDeleteJSON(JSONObject paramJObj, String billid, String companyid, String billno) throws ServiceException, com.krawler.utils.json.base.JSONException {
        JSONObject deleteSOJson = paramJObj;
        JSONArray deletejArray = new JSONArray();
        JSONObject deleteJson = new JSONObject();
        deleteJson.put(Constants.billid, billid);

        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), billid);
        Receipt rp = (Receipt) objItr.getEntityList().get(0);
        if (rp != null) {
            deleteJson.put("journalentryid", rp.getJournalEntry().getID());
            deleteJson.put("entryno", rp.getJournalEntry().getEntryNumber());
            if (!StringUtil.isNullOrEmpty(billno)) {
                deleteJson.put("billno", billno);
            } else {
                deleteJson.put("billno", rp.getReceiptNumber());
            }
        }
        if (paramJObj.has("creationdate")&& paramJObj.get("creationdate")!=null) {
            deleteJson.put("billdate", paramJObj.optString("creationdate"));
        }

        deletejArray.put(deleteJson);
        deleteSOJson.put(Constants.data, deletejArray.toString());
        deleteSOJson.put(Constants.companyKey, companyid);
        deleteSOJson.put(Constants.isdefaultHeaderMap, true);
        deleteSOJson.put(Constants.isMultiGroupCompanyFlag, true);
        return deleteSOJson;
    }
    

     private void beforInvoiceSaveMethods(HttpServletRequest request,  String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
            if (methodName.equals("saveInvoice") || methodName.equals("saveOpeningBalanceInvoice")) {
                boolean isLinkedTransaction = StringUtil.isNullOrEmpty(request.getParameter(Constants.IS_LINKED_TRANSACTION)) ? false : Boolean.parseBoolean(request.getParameter(Constants.IS_LINKED_TRANSACTION));
                String invoiceid =request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(invoiceid) && !isLinkedTransaction){
                    KwlReturnObject result = accCreditNoteDao.getCNFromInvoice(invoiceid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Selected record(s) is currently used in the Credit Note(s). So it can not be edited.");
                    }
                    boolean includeTempDeleted = false;
                     result = accCreditNoteDao.getCNFromInvoiceOtherwise(invoiceid, companyid,includeTempDeleted);   //linked invoice cannot be edited
                     list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Selected record(s) is currently used in the Credit Note(s). So it can not be edited.");
                    }
                    result = accInvoiceDAOobj.getSRFromInvoice(invoiceid, companyid);   //linked invoice cannot be edited
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException(messageSource.getMessage("acc.assetdo.cannotedit", null, RequestContextUtils.getLocale(request)));
                    }
                    HashMap<String, Object> receiptMap = new HashMap<String, Object>();

                    receiptMap.put("invoiceid", invoiceid);
                    receiptMap.put("companyid", companyid);
                    
                    result = accReceiptDao.getReceiptFromInvoice(receiptMap);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Payment against the selected Invoice(s) has been partially/fully received. So, it cannot be edited.");
                    }
                }
                if(methodName.equals("saveInvoice")){
                    try {
                        DateFormat df = authHandler.getDateOnlyFormat();
                        Date billdate = df.parse(request.getParameter("billdate"));
                        boolean isExicseOpeningbalance = false;
                        if (!StringUtil.isNullOrEmpty(request.getParameter("isExicseOpeningbalance"))) {
                            isExicseOpeningbalance = Boolean.parseBoolean(request.getParameter("isExicseOpeningbalance"));
                        }

                        if (!isExicseOpeningbalance) {
                            CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
                        }
                        if (!StringUtil.isNullOrEmpty(invoiceid)) {
                            KwlReturnObject invoiceObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                            Invoice invoice = (Invoice) invoiceObj.getEntityList().get(0);

                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                            requestParams.put("moduleID", Constants.Acc_Invoice_ModuleId);

//                            CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(invoice.getJournalEntry().getEntryDate())));
                            CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(invoice.getCreationDate())));
                        }
                        /**
                         * SDP-14735
                         */
                        if (methodName.equals("saveInvoice")) {
                            String countryID = sessionHandlerImpl.getCountryId(request);
                            if (!StringUtil.isNullOrEmpty(countryID) && Integer.parseInt(countryID) == Constants.indian_country_id) {
                                JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request);
                                if (!paramJObj.optBoolean("isConsignment", false)) {
                                    saveSalesInvoiceRequestDataAndSendEmail(paramJObj, true);
                                }//
                            }
                        }
                    } catch (ParseException ex) {
                        Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }else if( methodName.equals("updateInvoice")){
                String invoiceid =request.getParameter("invoiceid");
                 DateFormat df = authHandler.getDateFormatter(request);
                if (!StringUtil.isNullOrEmpty(invoiceid)) {
                            KwlReturnObject invoiceObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                            Invoice invoice = (Invoice) invoiceObj.getEntityList().get(0);

                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                            requestParams.put("moduleID", Constants.Acc_Invoice_ModuleId);
                try {
//                    CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(invoice.getJournalEntry().getEntryDate())));
                    CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(invoice.getCreationDate())));
                } catch (ParseException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
                        }
            }
     }
     private void beforBillingInvoiceSaveMethods(HttpServletRequest request,  String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
            if (methodName.equals("saveBillingInvoice") ) {
                String invoiceid =request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(invoiceid)){
                    KwlReturnObject result = accCreditNoteDao.getBillingCreditNoteDet(invoiceid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Selected record(s) is currently used in the Credit Note(s). So it can not be edited.");
                    }
                    result = accReceiptDao.getBReceiptFromBInvoice(invoiceid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Payment against the selected Invoice(s) has been partially/fully received. So, it cannot be edited.");
                    }
                }
                try {
                    DateFormat df = authHandler.getDateOnlyFormat();
                    Date billdate = df.parse(request.getParameter("billdate"));
                    CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
                } catch (ParseException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    }
     
    private void beforDeliveryOrderSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveDeliveryOrder")) {
            String doid = request.getParameter("doid");  //handle the edit case of DO if its used in Sales return
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(doid)) {
                KwlReturnObject result = accInvoiceDAOobj.getDODetailsFromSR(doid, companyid);
                List list = result.getEntityList();
                if (!list.isEmpty()) {
                    throw new AccountingException(messageSource.getMessage("acc.assetdo.cannotedit", null, RequestContextUtils.getLocale(request)));
                }
                 KwlReturnObject doObj = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);
                DeliveryOrder deliveryOrder = (DeliveryOrder) doObj.getEntityList().get(0);
                if (deliveryOrder.isIsconsignment()) {
                    KwlReturnObject result1 = accInvoiceDAOobj.getSerialNoUsedinConsignmentInvoiceFromDO(doid, companyid);
                    List list1 = result1.getEntityList();
                    if (!list1.isEmpty()) {
                        throw new AccountingException("Selected record(s) is currently used in the Some Transaction(s). So it can not be edited.");
                    }
                    KwlReturnObject result2 = accInvoiceDAOobj.getbatchNoUsedinConsignmentInvoiceFromDO(doid, companyid);
                    List list2 = result2.getEntityList();
                    if (!list2.isEmpty()) {
                        throw new AccountingException("Selected record(s) is currently used in the Some Transaction(s). So it can not be edited.");
                    }
                }
            }
            
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("billdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);

                if (!StringUtil.isNullOrEmpty(doid)) {
                    KwlReturnObject doObj = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);
                    DeliveryOrder deliveryOrder = (DeliveryOrder) doObj.getEntityList().get(0);
                    
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                    requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                    requestParams.put("moduleID", Constants.Acc_Delivery_Order_ModuleId);
                    
                    CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(deliveryOrder.getOrderDate())));
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void beforSalesReturnSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveSalesReturn")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("billdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
                
                /* ERP-40427:checkActiveDateRange function should be called only when transaction is not of opening type for checking active date
                 * range and which is aleready called above. Only for edit case
                 */
                String billid = request.getParameter("srid");
                if (!StringUtil.isNullOrEmpty(billid)) {
                    KwlReturnObject poObj = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), billid);
                    SalesReturn srObj = (SalesReturn) poObj.getEntityList().get(0);
                    if (srObj != null) {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_Sales_Return_ModuleId);
                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(srObj.getOrderDate())));
                    }
                }
                
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforeStockAdjustmentSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("requestStockAdjustment")) {
            try {
                DateFormat df = authHandler.getOnlyDateFormat(request);
                Date billdate = df.parse(request.getParameter("businessdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void beforeStockRequestSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("addStockOrderRequest") || methodName.equals("addIssueNoteRequest")) {
            try {
                DateFormat df = authHandler.getOnlyDateFormat(request);
                Date billdate = df.parse(request.getParameter("businessdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else if(methodName.equals("addInterStoreTransferRequest")|| methodName.equals("addInterLocationTransfer")){
             try {
                DateFormat df = authHandler.getOnlyDateFormat(request);
                Date billdate = df.parse(request.getParameter("trans date"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void beforContractSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveContract")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("startdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void beforDeliveryOrderDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("deleteDeliveryOrders") || methodName.equals("deleteDeliveryOrdersPermanent")) {
            try {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                        String doid = StringUtil.DecodeText(jobj.optString("billid"));

                        KwlReturnObject doObj = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), doid);
                        DeliveryOrder deliveryOrder = (DeliveryOrder) doObj.getEntityList().get(0);

                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_Delivery_Order_ModuleId);

                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, deliveryOrder.getOrderDate());
                    }
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforeInvoiceDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("deleteInvoice") || methodName.equals("deleteInvoicePermanent")) {
            try {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                        String invoiceId = StringUtil.DecodeText(jobj.optString("billid"));

                        KwlReturnObject invoiceObj = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceId);
                        Invoice invoice = (Invoice) invoiceObj.getEntityList().get(0);

                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_Invoice_ModuleId);

//                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, invoice.getJournalEntry().getEntryDate());
                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, invoice.getCreationDate());
                    }
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }
    
    /*ERP-40427:Before Delete checking the active period days*/
       private void beforeSalesOrderDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("deleteSalesOrders") || methodName.equals("deleteSalesOrdersPermanent")) {
            try {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                        String soId = StringUtil.DecodeText(jobj.optString("billid"));

                        KwlReturnObject grObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soId);
                        SalesOrder srObj = (SalesOrder) grObj.getEntityList().get(0);
                        if (srObj != null) {
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                            requestParams.put("moduleID", Constants.Acc_Sales_Order_ModuleId);

                            CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, srObj.getOrderDate());
                        }
                    }
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    } 
       
    /*ERP-40427:Before Delete checking the active period days*/
       private void beforeReceivePaymentDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
           if (methodName.equals("deleteReceiptForEdit") || methodName.equals("deleteReceiptMerged")) {
               try {
                   JSONArray jArr = new JSONArray(request.getParameter("data"));
                   for (int i = 0; i < jArr.length(); i++) {
                       JSONObject jobj = jArr.getJSONObject(i);
                       if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                           String soId = StringUtil.DecodeText(jobj.optString("billid"));

                           KwlReturnObject returnObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), soId);
                           Receipt rObj = (Receipt) returnObj.getEntityList().get(0);
                           if (rObj != null) {
                               HashMap<String, Object> requestParams = new HashMap<String, Object>();
                               requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                               requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                               requestParams.put("moduleID", Constants.Acc_Receive_Payment_ModuleId);

                               CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, rObj.getCreationDate());
                           }
                       }
                   }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    } 
       /*Before Delete checking the active period days*/
       private void beforeSalesReturnDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("deleteSalesReturn") || methodName.equals("deleteSalesReturnPermanent")) {
            try {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                        String invoiceId = StringUtil.DecodeText(jobj.optString("billid"));

                        KwlReturnObject grObj = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), invoiceId);
                        SalesReturn srObj = (SalesReturn) grObj.getEntityList().get(0);
                        if (srObj != null) {
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                            requestParams.put("moduleID", Constants.Acc_Sales_Return_ModuleId);
                            CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, srObj.getOrderDate());
                        }
                    }
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
             /*Before Delete checking the active period days*/
       private void beforePaymentDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("deletePaymentMerged") || methodName.equals("deletePaymentForEdit")) {
            try {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                        String billid = StringUtil.DecodeText(jobj.optString("billid"));

                        KwlReturnObject grObj = accountingHandlerDAOobj.getObject(Payment.class.getName(), billid);
                        Payment mpObj = (Payment) grObj.getEntityList().get(0);
                        if (mpObj != null) {
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                            requestParams.put("moduleID", Constants.Acc_Make_Payment_ModuleId);
                            CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, mpObj.getCreationDate());
                        }
                    }
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }  
       
    private void beforeGoodsReceiptDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("deleteGoodsReceipt") || methodName.equals("deleteGoodsReceiptPermanent")) {
            try {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                        String invoiceId = StringUtil.DecodeText(jobj.optString("billid"));

                        KwlReturnObject grObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceId);
                        GoodsReceipt gr = (GoodsReceipt) grObj.getEntityList().get(0);

                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_Vendor_Invoice_ModuleId);

//                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, gr.getJournalEntry().getEntryDate());
                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, gr.getCreationDate());
                    }
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforeInterStoreDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("deleteInterStoreTransferRequest")) {
            try {
                String reqIds = request.getParameter("requestIds"); // comma separated

                String[] istIdArray = null;
                if (!StringUtil.isNullOrEmpty(reqIds)) {
                    istIdArray = reqIds.split(",");
                    for (String istId : istIdArray) {
//                        String doid = URLDecoder.decode(istId, StaticValues.ENCODING);

                        KwlReturnObject istObj = accountingHandlerDAOobj.getObject(InterStoreTransferRequest.class.getName(), istId);
                        InterStoreTransferRequest ist = (InterStoreTransferRequest) istObj.getEntityList().get(0);

                        HashMap<String, Object> requestParams = new HashMap<>();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_InterStore_ModuleId);

                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, ist.getBusinessDate());
                    }
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforeInterLocationDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("deleteInterLocationTransferRequest")) {
            try {
                String reqIds = request.getParameter("requestIds"); // comma separated

                String[] istIdArray = null;
                if (!StringUtil.isNullOrEmpty(reqIds)) {
                    istIdArray = reqIds.split(",");
                    for (String istId : istIdArray) {
//                        String doid = StringUtil.DecodeText(istId);

                        KwlReturnObject istObj = accountingHandlerDAOobj.getObject(InterStoreTransferRequest.class.getName(), istId);
                        InterStoreTransferRequest ist = (InterStoreTransferRequest) istObj.getEntityList().get(0);

                        HashMap<String, Object> requestParams = new HashMap<>();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_InterLocation_ModuleId);

                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, ist.getBusinessDate());
                    }
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforePurchaseOrderDeleteMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("deletePurchaseOrders") || methodName.equals("deletePurchaseOrdersPermanent") ) {
             try {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                        String doid = StringUtil.DecodeText(jobj.optString("billid"));

                        KwlReturnObject poObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), doid);
                        PurchaseOrder purchaseOrder = (PurchaseOrder) poObj.getEntityList().get(0);

                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_Purchase_Order_ModuleId);

                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, purchaseOrder.getOrderDate());
                    }
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    }

    private void beforGoodsReceiptSaveMethods(HttpServletRequest request,  String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
            if (methodName.equals("saveGoodsReceipt") || methodName.equals("saveOpeningBalanceGoodsReceipt")) {
                String grid =request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(grid)){
                    KwlReturnObject result = accDebitNoteDao.getDNDetailsFromGReceipt(grid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException(messageSource.getMessage("acc.consignmentpurchaseinvoice.cannotedit", null, RequestContextUtils.getLocale(request)));
                    }
                     result = accGoodsReceiptobj.getGRNDetailsFromPR(grid, companyid);//handle the edit case of DO if its used in Sales return
                     list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException(messageSource.getMessage("acc.consignmentgro.cannotedit", null, RequestContextUtils.getLocale(request)));
                    }
                     result = accDebitNoteDao.getDNDetailsFromGReceiptOtherwise(grid, companyid);
                     list = result.getEntityList();                                       //linked GR cannot be edited
                    if (!list.isEmpty()) {
                        throw new AccountingException(messageSource.getMessage("acc.consignmentpurchaseinvoice.cannotedit", null, RequestContextUtils.getLocale(request)));
                    }
                    result = accVendorPaymentDao.getPaymentsFromGReceipt(grid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Payment against the selected Vendor Invoice(s) has been partially/fully made. So, it cannot be edited.");
                    }
                }
                try {
                    if (methodName.equals("saveGoodsReceipt")) {
                        DateFormat df = authHandler.getDateOnlyFormat();
                        Date billdate = df.parse(request.getParameter("billdate"));
                        // Excise Opening Balance check from Vendor Master Form ERP-27108
                        boolean isExicseOpeningbalance = false;
                        if (!StringUtil.isNullOrEmpty(request.getParameter("isExicseOpeningbalance"))) {
                            isExicseOpeningbalance = Boolean.parseBoolean(request.getParameter("isExicseOpeningbalance"));
                        }

                        if (!isExicseOpeningbalance) {
                            CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
                        }
                        if (!StringUtil.isNullOrEmpty(grid)) {
                            KwlReturnObject grObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
                            GoodsReceipt gr = (GoodsReceipt) grObj.getEntityList().get(0);

                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                            requestParams.put("moduleID", Constants.Acc_Vendor_Invoice_ModuleId);

//                            CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(gr.getJournalEntry().getEntryDate())));
                            CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(gr.getCreationDate())));
                        }
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    }
    private void beforBillingGoodsReceiptSaveMethods(HttpServletRequest request,  String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
            if (methodName.equals("saveBillingGoodsReceipt") ) {
                String grid =request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(grid)){
                    KwlReturnObject result = accDebitNoteDao.getBDNDetailsFromGReceipt(grid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException(messageSource.getMessage("acc.consignmentpurchaseinvoice.cannotedit", null, RequestContextUtils.getLocale(request)));
                    }
                    result = accVendorPaymentDao.getBillingPaymentsFromGReceipt(grid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        throw new AccountingException("Payment against the selected Vendor Invoice(s) has been partially/fully made. So, it can not be edited.");
                    }
                }
                try {
                    DateFormat df = authHandler.getDateOnlyFormat();
                    Date billdate = df.parse(request.getParameter("billdate"));
                    CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
                } catch (ParseException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }            
    }
    private void beforGoodsReceiptOrderSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveGoodsReceiptOrder")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("billdate"));
                String doid = request.getParameter("doid");
                String moduleid = !StringUtil.isNullOrEmpty(request.getParameter("moduleid"))? request.getParameter("moduleid") : "";
                String companyid = sessionHandlerImpl.getCompanyid(request);
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                boolean isnegativestockforlocwar = false;
                ExtraCompanyPreferences extraCompanyPreferences = null;
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                isnegativestockforlocwar = extraCompanyPreferences.isIsnegativestockforlocwar();
                    if (!StringUtil.isNullOrEmpty(doid)) {
                        
                        KwlReturnObject resultPR = accGoodsReceiptobj.getGROFromPR(doid, companyid);
                        List listpr = resultPR.getEntityList();
                        if (!listpr.isEmpty()) {
                            throw new AccountingException(messageSource.getMessage("acc.consignmentgro.cannotedit", null, RequestContextUtils.getLocale(request)));
                        }
                        KwlReturnObject result = accGoodsReceiptobj.getSerialNoUsedinDOFromGRO(doid, companyid);
                        List list = result.getEntityList();
                        if (!list.isEmpty()) {
                            throw new AccountingException( "Item details of selected record(s) are currently used in Delivery order. So it can not be edited");
                        }
                        if(!isnegativestockforlocwar && !moduleid.equals(Integer.toString(Constants.Acc_Goods_Receipt_ModuleId))){
                            KwlReturnObject result1 = accGoodsReceiptobj.getbatchUsedinDOFromGRO(doid, companyid);
                            List list1 = result1.getEntityList();
                            if (!list1.isEmpty()) {
                                throw new AccountingException( "Item details of selected record(s) are currently used in Delivery order. So it can not be edited");
                            }
                        }
                    }
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforPurchaseReturnSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("savePurchaseReturn")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("billdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforRequisitionSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveRequisition")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("billdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforQuotationSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveQuotation")) {
            try {
                String msg="";
                String qid =request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(qid)) {
                    KwlReturnObject result = accSalesOrderDAOobj.getSOforQT(qid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                         msg = messageSource.getMessage("acc.cq.cannoteditquotation", null, RequestContextUtils.getLocale(request));
                        throw new AccountingException(msg);
                         
                    }
                    result = accSalesOrderDAOobj.getQTforinvoice(qid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                         msg = messageSource.getMessage("acc.cq.cannoteditquotation", null, RequestContextUtils.getLocale(request));
                        throw new AccountingException(msg);
                    }
                }
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("billdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforPurchaseOrderSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("savePurchaseOrder")||methodName.equals("updateLinkedPurchaseOrder")) {
            String msg = "";
            if(methodName.equals("updateLinkedPurchaseOrder"))
            {
                request.setAttribute("isEdit", true);
                request.setAttribute("copyInv", false);
            }
            try {
                String poid = request.getParameter("invoiceid");
                String companyid = sessionHandlerImpl.getCompanyid(request);
                boolean isOpeningBalanceOrder = StringUtil.isNullOrEmpty(request.getParameter("isOpeningBalanceOrder")) ? false : Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder"));
                if (!StringUtil.isNullOrEmpty(poid)) {
                    KwlReturnObject result = accPurchaseOrderDAOobj.getSOforPO(poid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        msg = messageSource.getMessage("acc.cq.cannoteditquotation", null, RequestContextUtils.getLocale(request));
                        throw new AccountingException(msg);
                    }
                }
                DateFormat df = authHandler.getDateOnlyFormat();
                if (!isOpeningBalanceOrder) {
                    Date billdate = df.parse(request.getParameter("billdate"));
                    request.setAttribute(Constants.isFromPO, true);
                    CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
                }

                /*
                 checkActiveDateRange function should be called only when transaction is not of opening type 
                 for checking active date range and which is aleready called above.
                 */
                if (!StringUtil.isNullOrEmpty(poid)) {
                    KwlReturnObject poObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid);
                    PurchaseOrder purchaseOrder = (PurchaseOrder) poObj.getEntityList().get(0);

                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                    requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                    requestParams.put("moduleID", Constants.Acc_Purchase_Order_ModuleId);

                    CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(purchaseOrder.getOrderDate())));
                }
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforBillingPurchaseOrderSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveBillingPurchaseOrder")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("billdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforPaymentSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("savePayment") || methodName.equals("saveBulkPayment")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
                
                /*ERP-40427: checkActiveDateRange function should be called only when transaction is not of opening type for checking active date
                 * range and which is already called above.Only for Edit and 
                 */
                String mpid = request.getParameter(Constants.billid);
                if (!StringUtil.isNullOrEmpty(mpid)) {
                    KwlReturnObject poObj = accountingHandlerDAOobj.getObject(Payment.class.getName(), mpid);
                    Payment paymentObj = (Payment) poObj.getEntityList().get(0);
                    if (paymentObj != null) {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_Make_Payment_ModuleId);
                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(paymentObj.getCreationDate())));
                    }
                }
                
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforBillingPaymentSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveBillingPayment")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforContraPaymentSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveContraPayment")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforDebitNoteSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveDebitNote")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforBillingDebitNoteSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveBillingDebitNote")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforSalesOrderSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveSalesOrder")||methodName.equals("updateLinkedSalesOrder")) {
            try {
                if (methodName.equals("updateLinkedSalesOrder")) {
                    request.setAttribute("isEdit", true);
                    request.setAttribute("copyInv", false);
                }
                String msg = "";
                String soid = request.getParameter("invoiceid");
                boolean isOpeningBalanceOrder=StringUtil.isNullOrEmpty(request.getParameter("isOpeningBalanceOrder"))?false:Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder"));
                String companyid = sessionHandlerImpl.getCompanyid(request);
                if (!StringUtil.isNullOrEmpty(soid)) {
                    KwlReturnObject result = accSalesOrderDAOobj.getPOforSO(soid, companyid);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        msg = messageSource.getMessage("acc.so.cannoteditsalesorder", null, RequestContextUtils.getLocale(request));
                        throw new AccountingException(msg);
                    }
                }
                if(!isOpeningBalanceOrder){
                    DateFormat df = authHandler.getDateOnlyFormat();
                    Date billdate = df.parse(request.getParameter("billdate"));
                    request.setAttribute(Constants.isFromSO,true );
                    CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
                }
                 
                
                /* ERP-40427:checkActiveDateRange function should be called only when transaction is not of opening type for checking active date
                 * range and which is aleready called above.
                 */
                DateFormat df = authHandler.getDateOnlyFormat();
                if (!StringUtil.isNullOrEmpty(soid)) {
                    KwlReturnObject poObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
                    SalesOrder soObj = (SalesOrder) poObj.getEntityList().get(0);
                    if (soObj != null) {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_Sales_Order_ModuleId);
                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, new Date(df.format(soObj.getOrderDate())));
                    }
                }
                
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforBillingSalesOrderSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveBillingSalesOrder")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("billdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforCreditNoteSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveCreditNote")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforBillingCreditNoteSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveBillingCreditNote")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforReceiptSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveReceipt")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
                
                                
                /*
                 * ERP-39693:checkActiveDateRange function should be called only
                 * when transaction is not of opening type for checking active
                 * date range and which is aleready called above. Only for edit
                 * case
                 */
                String billid = request.getParameter(Constants.billid);
                if (!StringUtil.isNullOrEmpty(billid)) {
                    KwlReturnObject poObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), billid);
                    Receipt rObj = (Receipt) poObj.getEntityList().get(0);
                    if (rObj != null) {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("userID", sessionHandlerImpl.getUserid(request));
                        requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("moduleID", Constants.Acc_Receive_Payment_ModuleId);
                        CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, rObj.getCreationDate());
                    }
                }
                
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforBillingReceiptSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveBillingReceipt")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforContraReceiptSaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveContraReceipt")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("creationdate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void beforJournalEntrySaveMethods(HttpServletRequest request, String methodName) throws SessionExpiredException, AccountingException, com.krawler.utils.json.base.JSONException, ServiceException {
        if (methodName.equals("saveJournalEntry")) {
            try {
                DateFormat df = authHandler.getDateOnlyFormat();
                Date billdate = df.parse(request.getParameter("entrydate"));
                CompanyPreferencesCMN.checkActiveDateRange(accCompanyPreferencesObj, request, billdate);
            } catch (ParseException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void afterrReturningSaveInventoryConsumptionMethod(String className, String methodName, Object valueReturn) {
        //<editor-fold defaultstate="collapsed" desc="Method used to check Inventory Valuation Type,For PERPETUAL_VALUATION_METHOD perform valuation in MRP after saveInventoryConsumption method">
        if (className.equals("MasterServiceImpl") && methodName.equals("saveInventoryConsumption")) {
            try {
                JSONObject jobj = (JSONObject) valueReturn;
                if (!StringUtil.isNullOrEmpty(jobj.optString("companyid", null))) {
                    String companyid = jobj.optString("companyid");

                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), companyid);
                    if (preferences != null && preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                        performValuationInMRP(companyid, false, className, methodName, valueReturn, preferences.isUpdateStockAdjustmentEntries());
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //</editor-fold>
    }
    private void afterReturningAOPMethods(HttpServletRequest request, String className, String methodName, Object valueReturn) {
        try {
            if (methodName.equals("saveInvoice") || methodName.equals("saveBillingInvoice")) {
                ModelAndView mv = (ModelAndView) valueReturn;
                Map map = mv.getModel();
                String model = (String) map.get("model");
                JSONObject jobj = new JSONObject(model);
                
                boolean isExicseOpeningbalance = false;
                if (!StringUtil.isNullOrEmpty(request.getParameter("isExicseOpeningbalance"))) {
                    isExicseOpeningbalance = Boolean.parseBoolean(request.getParameter("isExicseOpeningbalance"));
                }
// Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
                if (isExicseOpeningbalance) {
                   String invoiceid=jobj.getString("invoiceid");
                   String companyid = sessionHandlerImpl.getCompanyid(request);
                   Map<String, String> passInvobj=new HashMap<String,String>();
                   passInvobj.put("invoiceid",invoiceid);
                   passInvobj.put("companyid",companyid);
                   deleteJEPostofInvoice(passInvobj);
                }
                /**
                 *SDP-14735
                 */
                if (methodName.equals("saveInvoice")) {
                    String countryID = sessionHandlerImpl.getCountryId(request);
                    if (!StringUtil.isNullOrEmpty(countryID) && Integer.parseInt(countryID) == Constants.indian_country_id) {
                        JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request);
                        String invoiceid = jobj.optString("invid","");
                        if (!StringUtil.isNullOrEmpty(invoiceid) && !paramJObj.optBoolean("isConsignment", false)) {
                            paramJObj.put("invoiceid", invoiceid);
                            saveSalesInvoiceRequestDataAndSendEmail(paramJObj, false);
                        }
                    }
                }
            } 
            boolean isAutoCreateDO = false ;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isAutoCreateDO"))) {
                isAutoCreateDO=Boolean.parseBoolean(request.getParameter("isAutoCreateDO"));
            }
            boolean isExpenseInv  = false ;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isExpenseInv"))) {
                isExpenseInv =Boolean.parseBoolean(request.getParameter("isExpenseInv"));
            }
            
            if (methodName.equals("deleteGoodsReceipt")|| methodName.equals("deleteGoodsReceiptPermanent")) {
                ModelAndView mv = (ModelAndView) valueReturn;
                Map map = mv.getModel();
                String model = (String) map.get("model");
                JSONObject jobj = new JSONObject(model);
                if (jobj.has("isExpenseInv") && !StringUtil.isNullOrEmpty(jobj.optString("isExpenseInv"))) {
                    isExpenseInv = Boolean.parseBoolean(jobj.optString("isExpenseInv"));
                }
            }
            
            if (className.equals("accJournalEntryController") &&  methodName.equals("saveJournalEntry")) {
                ModelAndView mv = (ModelAndView) valueReturn;
                Map map = mv.getModel();
                String model = (String) map.get("model");
                JSONObject jobj = new JSONObject(model);
                JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request);
                String jeId = jobj.optString("id", "");
                if (!StringUtil.isNullOrEmpty(jeId)) {
                    paramJObj.put("jeid", jeId);
                    saveJournalEntryRequestSendEmailOnEmptyData(paramJObj);
                }
            }
            
            /**
             * Function will call after savesequenceformat format method from accCompanyPreferencesController.
             */
            if ((className.equals("accCompanyPreferencesController") && methodName.equals("saveSequenceFormat"))) {
                if (valueReturn!=null) {
                    updateExistingtransactionmatchedWithsequenceFormat(request,valueReturn);
                }
            }
            if ((className.equals("accGoodsReceiptController") && (methodName.equals("saveGoodsReceiptOrder") || methodName.equals("deleteGoodsReceiptOrdersPermanent")))
                    || (className.equals("AccWorkOrderControllerCMN") && methodName.equals("updateInventoryForFinishedGood"))
                    || (className.equals("accSalesReturnControllerCMN") && methodName.equals("saveSalesReturn"))
                    || (className.equals("accSalesReturnControllerCMN") && methodName.equals("savePurchaseReturn"))
                    || (className.equals("accGoodsReceiptControllerCMN") && methodName.equals("deletePurchaseReturnPermanent"))
                    || (className.equals("accGoodsReceiptControllerCMN") && methodName.equals("deletePurchaseReturn"))
                    || (className.equals("accGoodsReceiptControllerCMN") && (methodName.equals("deleteGoodsReceipt")||methodName.equals("deleteGoodsReceiptPermanent")) && isExpenseInv)
                    || (className.equals("accInvoiceController") && (methodName.equals("saveDeliveryOrder") ||  methodName.equals("saveShippingDeliveryOrder") || methodName.equals("deleteDeliveryOrdersPermanent")))
                    || (className.equals("accInvoiceControllerCMN") && (methodName.equals("deleteInvoicePermanent") || methodName.equals("deleteSalesReturnPermanent") || methodName.equals("deleteSalesReturn")))
                    || (className.equals("StockAdjustmentController") && methodName.equals("requestStockAdjustment"))
                    || (className.equals("accInvoiceController") && isAutoCreateDO && methodName.equals("saveInvoice"))
                    || (className.equals("accInvoiceControllerCMN") && isAutoCreateDO && methodName.equals("saveInvoice"))
                    || (className.equals("accGoodsReceiptController") && (isAutoCreateDO||isExpenseInv) && methodName.equals("saveGoodsReceipt"))
                    || (className.equals("accGoodsReceiptController") && methodName.equals("deleteGoodsReceiptOrders"))
                    || (className.equals("accInvoiceController") && methodName.equals("deleteDeliveryOrders"))
                    || (className.equals("accInvoiceController") && methodName.equals("saveBulkDOFromSO"))
                    || (className.equals("StockAdjustmentController") && methodName.equals("deletSA"))
                    || (className.equals("CycleCountController") && methodName.equals("addCycleCountRequest"))
                    || (className.equals("accProductControllerCMN") && methodName.equals("buildProductAssembly"))
                    || (className.equals("GoodsTransferController") && methodName.equals("addIssueNoteRequest"))
                    || (className.equals("GoodsTransferController") && methodName.equals("issueStockOrderRequest")) //ERP-39060 adding calls to issue Stock request and accept Return Request 
                    || (className.equals("GoodsTransferController") && methodName.equals("acceptReturnStockRequest"))
                    || (className.equals("GoodsTransferController") && methodName.equals("collectStockOrderRequest"))
                    || (className.equals("accGoodsReceiptController") && methodName.equals("approveGoodsReceiptOrder"))
                    || (className.equals("accGoodsReceiptController") && methodName.equals("approvegr"))
                    || (className.equals("accInvoiceController") && methodName.equals("approveDeliveryOrder"))
                    || (className.equals("accInvoiceController") && methodName.equals("approveInvoice"))
                    || (className.equals("GoodsTransferController") && (methodName.equals("addInterLocationTransfer") || methodName.equals("addInterStoreTransferRequest") || methodName.equals("acceptInterStoreTransferRequest") || methodName.equals("acceptISTReturnRequest")))
                    || (className.equals("ApprovalController") && (methodName.equals("saveStockRepair") || methodName.equals("saveInspectionData1") ))
                    || (className.equals("GoodsTransferController") && methodName.equals("importInterStoreTransferRequest"))) {
                String companyid = sessionHandlerImpl.getCompanyid(request);
                
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), companyid);
                if (preferences != null) {
                    if (preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD && (className.equals("ApprovalController") && (methodName.equals("saveStockRepair") || methodName.equals("saveInspectionData1")))) {
                        String productids = getProductIDs(className, methodName, request, isAutoCreateDO, valueReturn).toString();
                        if (!StringUtil.isNullOrEmpty(productids)) {
                            performValuation(companyid, false, className, methodName, request, isAutoCreateDO, valueReturn, preferences.isUpdateStockAdjustmentEntries());
                        }
                    } else if (className.equals("ApprovalController") && (methodName.equals("saveStockRepair") || methodName.equals("saveInspectionData1"))) {
                        String productids = getProductIDs(className, methodName, request, isAutoCreateDO, valueReturn).toString();
                        if (!StringUtil.isNullOrEmpty(productids)) {
                            performValuation(companyid, true, className, methodName, request, isAutoCreateDO, valueReturn, preferences.isUpdateStockAdjustmentEntries());
                        }
                    } else if (preferences != null && preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                        performValuation(companyid, false, className, methodName, request, isAutoCreateDO, valueReturn, preferences.isUpdateStockAdjustmentEntries());
                    } else if (preferences != null && preferences.getInventoryValuationType() != Constants.PERPETUAL_VALUATION_METHOD) {
                        boolean updateTransactionAmount = true;
                        performValuation(companyid, updateTransactionAmount, className, methodName, request, isAutoCreateDO, valueReturn, preferences.isUpdateStockAdjustmentEntries());
                    }
                }
            }

            if (GroupCompanyProcessMapping.ControllerNameSet.contains(className) && GroupCompanyProcessMapping.MethodSetForAccountPayable.contains(methodName)) {
                KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
                ExtraCompanyPreferences extraCompanyObj = (ExtraCompanyPreferences) companyResult.getEntityList().get(0);
                if (extraCompanyObj.isActivateGroupCompaniesFlag()) {
                    JSONObject returnJobj = processGroupCompanyTransactions(request, className, methodName, valueReturn);
                    if (returnJobj.has(Constants.RES_success) && !returnJobj.optBoolean(Constants.RES_success)) {
                        valueReturn = new ModelAndView("jsonView", "model", returnJobj.toString());
                    }
                }//end of multicompanies flag
            }
        } catch (Exception ex) {
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private JSONObject postData(String endpoint, String data, String type) throws com.krawler.utils.json.base.JSONException {
        StringBuilder resp = new StringBuilder();
        try {
            URL url = new URL(endpoint);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod(type);
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            if (!type.equals("GET")) {
                OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());

                wr.write(data);
                wr.flush();
            }
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);

            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        }     
        System.out.println("ERP post response: " + resp);
        return new JSONObject(resp.toString());
    }
    
    
    public void sendMail(HttpServletRequest request, int mode, String billid) {
        java.io.OutputStream os = null;
        HashMap<String, Object> requestmap = null;
        {
            ByteArrayOutputStream baos = null;

            boolean advanceFlag=false;
            boolean otherwiseFlag=false;
            double advanceAmount = 0;
     
            try {    
                String username = sessionHandlerImpl.getUserName(request);
                String companyid = sessionHandlerImpl.getCompanyid(request);
                String baseUrl = URLUtil.getPageURL(request, loginpageFull);
                if (!StringUtil.isNullOrEmpty(request.getParameter("advanceAmount"))) {
                    advanceAmount = Double.parseDouble(request.getParameter("advanceAmount"));
                }

                if (!StringUtil.isNullOrEmpty(request.getParameter("otherwise"))) {
                    otherwiseFlag = Boolean.parseBoolean(request.getParameter("otherwise"));
                }

                if (!StringUtil.isNullOrEmpty(requestmap.get("advanceFlag").toString())) {
                    advanceFlag = Boolean.parseBoolean(requestmap.get("advanceFlag").toString());
                }
                
                boolean iscontraentryflag = Boolean.parseBoolean(request.getParameter("contraentryflag"));
                Locale loc =RequestContextUtils.getLocale(request);
                requestmap = new HashMap<String, Object>();
                requestmap.put("loc", loc);
                requestmap.put("baseUrl", baseUrl);
                requestmap.put("advanceFlag", advanceFlag);
                requestmap.put("otherwiseFlag", otherwiseFlag);
                requestmap.put("advanceAmount", advanceAmount);
                requestmap.put("iscontraentryflag", iscontraentryflag);
                requestmap.put("username", username);
                requestmap.put(Constants.companyKey, AccountingManager.getCompanyidFromRequest(request));
                requestmap.put(Constants.globalCurrencyKey,  AccountingManager.getCompanyidFromRequest(request));
                requestmap.put(Constants.df, authHandler.getDateOnlyFormat());
                
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
                Company company = preferences.getCompany();
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                String currencyid = currency.getCurrencyID();
                DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);    //This format do not include timezone
                String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                boolean isSend = false;
                String htmlMsg = "";
                String plainMsg = "";
                String subject = "";
                String[] emails = {};
                double amount = 0;
                Date invDate = new Date();
                String fromID = StringUtil.isNullOrEmpty(company.getEmailID())?authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID()):company.getEmailID();
                switch (mode) {
                    case StaticValues.AUTONUM_INVOICE:
                        Invoice inv = (Invoice) kwlCommonTablesDAOObj.getClassObject(Invoice.class.getName(), billid);
                        Customer customer = (Customer) kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(), inv.getCustomerEntry().getAccount().getID());
                        if (customer == null) {
                            return;
                        }
//                        invDate = inv.getJournalEntry().getEntryDate();
                        invDate = inv.getCreationDate();
                        ArrayList<String> em = new ArrayList<String>();

                        HashMap<String, Object> addressParams = new HashMap<String, Object>();
                        addressParams.put("companyid", companyid);
                        addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                        addressParams.put("isBillingAddress", true);    //true to get billing address
                        addressParams.put("customerid", customer.getID());
                        CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);

                        em.add(customerAddressDetails!=null?customerAddressDetails.getEmailID():"");
                        emails = em.toArray(emails);
                        isSend = preferences.isEmailInvoice();
                        subject = AccountingMsgs.invoiceSubject;
                        htmlMsg = String.format(AccountingMsgs.invoiceHtmlMsg, customer.getName(), inv.getInvoiceNumber(), fromID, fromID);
                        plainMsg = String.format(AccountingMsgs.invoicePlainMsg, customer.getName(), inv.getInvoiceNumber(), fromID, fromID);
                        double disc = Double.parseDouble(request.getParameter("discount"));
                        requestmap.put("disc", disc);
                        amount = Double.parseDouble(request.getParameter("subTotal"));
                        if ("true".equals(request.getParameter("perdiscount"))) {
                            amount = amount - amount * disc / 100;
                        } else {
                            amount = amount - disc;
                        }
                        break;

                    case StaticValues.AUTONUM_BILLINGINVOICE:
                        BillingInvoice billinginv = (BillingInvoice) kwlCommonTablesDAOObj.getClassObject(BillingInvoice.class.getName(), billid);
                        Customer billingCustomer = (Customer) kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(), billinginv.getCustomerEntry().getAccount().getID());
                        if (billingCustomer == null) {
                            return;
                        }
                        invDate = billinginv.getJournalEntry().getEntryDate();
                        ArrayList<String> billingem = new ArrayList<String>();
//                        billingem.add(billingCustomer.getEmail());
                        emails = billingem.toArray(emails);
                        isSend = preferences.isEmailInvoice();
                        subject = AccountingMsgs.invoiceSubject;
                        htmlMsg = String.format(AccountingMsgs.invoiceHtmlMsg, billingCustomer.getName(), billinginv.getBillingInvoiceNumber(), fromID, fromID);
                        plainMsg = String.format(AccountingMsgs.invoicePlainMsg, billingCustomer.getName(), billinginv.getBillingInvoiceNumber(), fromID, fromID);
                        double billingDisc = Double.parseDouble(request.getParameter("discount"));
                        requestmap.put("disc", billingDisc);
                        amount = Double.parseDouble(request.getParameter("subTotal"));
                        if ("true".equals(request.getParameter("perdiscount"))) {
                            amount = amount - amount * billingDisc / 100;
                        } else {
                            amount = amount - billingDisc;
                        }
                        break;
                }
                String dateStr = "";
                try {
                    DateFormat df = new SimpleDateFormat("yyyyMMdd");
                    dateStr = df.format(invDate);
                } catch(Exception ex) {
                	Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
                String companyId=sessionHandlerImpl.getCompanyid(request); 
                String userId=sessionHandlerImpl.getUserid(request);
                String baseCurrency=sessionHandlerImpl.getCurrencyID(request);
                baos = CreatePDFObj.createPdf(requestmap, currencyid, billid, formatter, mode, amount, logoPath, null, null, null,false,companyId,userId,baseCurrency);
                File destDir = new File(storageHandlerImpl.GetProfileImgStorePath(), "Invoice"+dateStr+".pdf");
                FileOutputStream oss = new FileOutputStream(destDir);
                baos.writeTo(oss);
                try {
                    if (emails.length > 0 && isSend) {
                        Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                        SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromID, new String[]{destDir.getAbsolutePath()}, smtpConfigMap);
                    }
                } catch (MessagingException e) {
                }
            } catch (DocumentException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ServiceException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    os.close();
                    baos.close();
                } catch (IOException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    /**
     * get sequenceformat id and moduleid.
     * @param valueReturn
     * @return 
     */
    private synchronized JSONObject getTransactionIds(Object valueReturn) {
        
        JSONObject jObj = new JSONObject();
        ModelAndView mv = (ModelAndView) valueReturn;
        Map map = mv.getModel();
        String model = (String) map.get("model");
        try {
            jObj = new JSONObject(model);
        } catch (Exception ex) {
        }
        return jObj;

    }
    private synchronized StringBuffer getProductIDs(String className, String methodName, HttpServletRequest request, boolean isAutoCreateDO, Object valueReturn) {
        StringBuffer productIdBuilder = new StringBuffer();
        ExtraCompanyPreferences extraCompanyPreferences = null;
        KwlReturnObject extraprefresult = null;
        try {
            extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
        } catch (ServiceException ex) {
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        }
      
        boolean isExpenseInv = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isExpenseInv"))) {
            isExpenseInv = Boolean.parseBoolean(request.getParameter("isExpenseInv"));
        }
        
        if (methodName.equals("deleteGoodsReceipt")|| methodName.equals("deleteGoodsReceiptPermanent")) {
             try {
                ModelAndView mv = (ModelAndView) valueReturn;
                Map map = mv.getModel();
                String model = (String) map.get("model");
                JSONObject jobj = new JSONObject(model);
                if (jobj.has("isExpenseInv") && !StringUtil.isNullOrEmpty(jobj.optString("isExpenseInv"))) {
                    isExpenseInv = Boolean.parseBoolean(jobj.optString("isExpenseInv"));
                }
             }catch (com.krawler.utils.json.base.JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
        extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        if (extraCompanyPreferences!=null && extraCompanyPreferences.isJobWorkOutFlow()&& ((className.equals("accGoodsReceiptController") && methodName.equals("saveGoodsReceiptOrder")) 
                || (className.equals("accGoodsReceiptController") && isAutoCreateDO && methodName.equals("saveGoodsReceipt")))) {
            ModelAndView mv = (ModelAndView) valueReturn;
            Map map = mv.getModel();
            String model = (String) map.get("model");
            try {
                JSONObject jobj = new JSONObject(model);
                if (!StringUtil.isNullOrEmpty(jobj.optString("productIds", null))) {
                    /**
                     * If Transaction created by linking Job Work order
                     */
                    String productids = jobj.optString("productIds");
                    productIdBuilder.append(productids);
                } else {
                    /**
                     * Normal Product transaction
                     */
                    String detail = request.getParameter("detail");
                    if (!StringUtil.isNullOrEmpty(detail)) {
                        JSONArray details = new JSONArray(detail);
                        if (details != null && details.length() > 0) {
                            for (int i = 0; i < details.length(); i++) {
                                JSONObject detailObject = details.getJSONObject(i);
                                if (!StringUtil.isNullOrEmpty(detailObject.optString(Constants.productid, null))) {
                                    if (productIdBuilder.indexOf(detailObject.getString(Constants.productid)) == -1) {
                                        productIdBuilder.append(detailObject.getString(Constants.productid)).append(",");
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else if ((className.equals("accGoodsReceiptController") && methodName.equals("saveGoodsReceiptOrder"))
                || (className.equals("accSalesReturnControllerCMN") && methodName.equals("saveSalesReturn"))
                || (className.equals("accSalesReturnControllerCMN") && methodName.equals("savePurchaseReturn"))
                || (className.equals("accInvoiceController") && isAutoCreateDO && methodName.equals("saveInvoice"))
                || (className.equals("accGoodsReceiptControllerCMN") && isExpenseInv && (methodName.equals("deleteGoodsReceipt") || methodName.equals("saveShippingDeliveryOrder") ||methodName.equals("deleteGoodsReceiptPermanent")))
                || (className.equals("accInvoiceControllerCMN") && isAutoCreateDO && methodName.equals("saveInvoice"))
                || (className.equals("accGoodsReceiptController") && (isAutoCreateDO || isExpenseInv) && methodName.equals("saveGoodsReceipt"))
                || (className.equals("accInvoiceController") && methodName.equals("saveDeliveryOrder"))) {
            
            if(isExpenseInv){
                ModelAndView mv = (ModelAndView) valueReturn;
                Map map = mv.getModel();
                String model = (String) map.get("model");
                try {
                    JSONObject jobj = new JSONObject(model);
                    if (!StringUtil.isNullOrEmpty(jobj.optString("productIds", null))) {
                        String productids = jobj.optString("productIds");
                        productIdBuilder.append(productids);
                    }
                } catch (com.krawler.utils.json.base.JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{  String detail = request.getParameter("detail");
            if (!StringUtil.isNullOrEmpty(detail)) {
                try {
                    JSONArray details = new JSONArray(detail);
                    if (details != null && details.length() > 0) {
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject detailObject = details.getJSONObject(i);
                            if (!StringUtil.isNullOrEmpty(detailObject.optString(Constants.productid, null))) {
                                if (productIdBuilder.indexOf(detailObject.getString(Constants.productid)) == -1) {
                                    productIdBuilder.append(detailObject.getString(Constants.productid)).append(",");
                                }
                            }
                        }
                    }
                } catch (com.krawler.utils.json.base.JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                    }
        } else if ((className.equals("accGoodsReceiptController") && (methodName.equals("deleteGoodsReceiptOrders") || methodName.equals("deleteGoodsReceiptOrdersPermanent")))
                || (className.equals("AccWorkOrderControllerCMN") && (methodName.equals("updateInventoryForFinishedGood")))
                || (className.equals("accGoodsReceiptControllerCMN") && (methodName.equals("deletePurchaseReturn") || methodName.equals("deletePurchaseReturnPermanent")))
                || (className.equals("accInvoiceController") && (methodName.equals("deleteDeliveryOrdersPermanent") || methodName.equals("deleteDeliveryOrders") || methodName.equals("saveShippingDeliveryOrder")))
                || (className.equals("StockAdjustmentController") && methodName.equals("deletSA"))
                || (className.equals("accGoodsReceiptController") && methodName.equals("approveGoodsReceiptOrder"))
                || (className.equals("accGoodsReceiptController") && methodName.equals("approvegr"))
                || (className.equals("accInvoiceController") && methodName.equals("approveDeliveryOrder"))
                || (className.equals("accInvoiceController") && methodName.equals("approveInvoice"))
                || (className.equals("accInvoiceController") && methodName.equals("saveBulkDOFromSO"))
                || (className.equals("accInvoiceControllerCMN") && (methodName.equals("deleteSalesReturnPermanent") || methodName.equals("deleteSalesReturn")))
                || (className.equals("ApprovalController") && (methodName.equals("saveStockRepair") || methodName.equals("saveInspectionData1")))
                || (className.equals("GoodsTransferController") && (methodName.equals("collectStockOrderRequest") || methodName.equals("acceptInterStoreTransferRequest"))) 
                || (className.equals("GoodsTransferController") && (methodName.equals("issueStockOrderRequest") || methodName.equals("acceptReturnStockRequest") || methodName.equals("acceptISTReturnRequest")))) {  //ERP-39060 adding calls to Stock request transactons
            ModelAndView mv = (ModelAndView) valueReturn;
            Map map = mv.getModel();
            String model = (String) map.get("model");
            try {
                JSONObject jobj = new JSONObject(model);
                if (!StringUtil.isNullOrEmpty(jobj.optString("productIds", null))) {
                    String productids = jobj.optString("productIds");
                    productIdBuilder.append(productids);
                }
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if ((className.equals("StockAdjustmentController") && methodName.equals("requestStockAdjustment"))
                || (className.equals("GoodsTransferController") && methodName.equals("addIssueNoteRequest"))) {
            String detail = request.getParameter("jsondata");
            if (!StringUtil.isNullOrEmpty(detail)) {
                try {
                    JSONArray details = new JSONArray(detail);
                    if (details != null && details.length() > 0) {
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject detailObject = details.getJSONObject(i);
                            if (!StringUtil.isNullOrEmpty(detailObject.optString(Constants.productid, null))) {
                                if (productIdBuilder.indexOf(detailObject.getString(Constants.productid)) == -1) {
                                    productIdBuilder.append(detailObject.getString(Constants.productid)).append(",");
                                }
                            }
                        }
                    }
                } catch (com.krawler.utils.json.base.JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if ((className.equals("CycleCountController") && methodName.equals("addCycleCountRequest"))) {
            String detail = request.getParameter("jsondata");
            if (!StringUtil.isNullOrEmpty(detail)) {
                try {
                    JSONArray details = new JSONArray(detail);
                    if (details != null && details.length() > 0) {
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject detailObject = details.getJSONObject(i);
                            if (!StringUtil.isNullOrEmpty(detailObject.optString("id", null))) {
                                if (productIdBuilder.indexOf(detailObject.getString("id")) == -1) {
                                    productIdBuilder.append(detailObject.getString("id")).append(",");
                                }
                            }
                        }
                    }
                } catch (com.krawler.utils.json.base.JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (className.equals("GoodsTransferController") && (methodName.equals("addInterLocationTransfer") || methodName.equals("addInterStoreTransferRequest"))) {
            String records = request.getParameter("str");
            if (!StringUtil.isNullOrEmpty(records)) {
                try {
                    JSONArray details = new JSONArray(records);
                    if (details != null && details.length() > 0) {
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject detailObject = details.getJSONObject(i);
                            if (!StringUtil.isNullOrEmpty(detailObject.optString("itemid", null))) {
                                if (productIdBuilder.indexOf(detailObject.getString("itemid")) == -1) {
                                    productIdBuilder.append(detailObject.getString("itemid")).append(",");
                                }
                            }
                        }
                    }
                } catch (com.krawler.utils.json.base.JSONException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                }
        if (productIdBuilder.length() > 0 && ((productIdBuilder.lastIndexOf(",") + 1) == productIdBuilder.length())) {
            productIdBuilder = new StringBuffer(productIdBuilder.substring(0, productIdBuilder.length() - 1));
        }
        return productIdBuilder;
    }

    private synchronized void performValuationInMRP(String companyid, boolean updateTransactionAmount, String className, String methodName, Object valueReturn, boolean updateStockAdjustmentPrice) {
        //<editor-fold defaultstate="collapsed" desc="Method used to perform valuation in thread after Task Complition in MRP which affect inventory for company">
        try {
            String basecurrency = "", productids = "";
            JSONObject jobj = (JSONObject) valueReturn;
            Map<String, Object> reqMap = new HashMap<>();

            if (!StringUtil.isNullOrEmpty(jobj.optString("currencyid", null))) {
                basecurrency = jobj.optString("currencyid");
                reqMap.put(Constants.currencyKey, basecurrency);
            }
            if (!StringUtil.isNullOrEmpty(jobj.optString("productIds", null))) {
                productids = jobj.optString("productIds");
                reqMap.put("productIds", productids);
            }

            reqMap.put(Constants.companyKey, companyid);
            reqMap.put("updateTransactionAmount", updateTransactionAmount);
            reqMap.put("updateStockAdjustmentPrice", updateStockAdjustmentPrice);
            inventoryValuationProcess.add(reqMap);
            Thread t = new Thread(inventoryValuationProcess);
            System.out.println("Thread Execution StarteD for company = " + companyid);
            t.start();
            System.out.println("Thread Execution EndeD for company = " + companyid);
        } catch (Exception ex) {
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>
    }
    
    private synchronized void performValuation(String companyid, boolean updateTransactionAmount, String className, String methodName, HttpServletRequest request, boolean isAutoCreateDO, Object valueReturn, boolean updateStockAdjustmentPrice) {
        //<editor-fold defaultstate="collapsed" desc="Method used to perform valuation in thread after add/edit/delete of any transaction which affect inventory for company">
        try {
            Map<String, Object> reqMap = new HashMap<>();
            String basecurrency = sessionHandlerImpl.getCurrencyID(request); //ERM-890 for landed cost cases
            reqMap.put(Constants.companyKey, companyid);
            reqMap.put("updateTransactionAmount", updateTransactionAmount);
            reqMap.put(Constants.currencyKey, basecurrency);
            reqMap.put("updateStockAdjustmentPrice", updateStockAdjustmentPrice);
            String productids = getProductIDs(className, methodName, request, isAutoCreateDO, valueReturn).toString();
            if (!StringUtil.isNullOrEmpty(productids)) {
                reqMap.put("productIds", productids);
            }
            inventoryValuationProcess.add(reqMap);
            Thread t = new Thread(inventoryValuationProcess);
            System.out.println("Thread Execution StarteD for company = " +companyid);
            t.start();
            System.out.println("Thread Execution EndeD for company = " +companyid);
        } catch (Exception ex) {
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>
    }
    /**
     * 
     * @param request
     * @param valuereturn 
     * This thread will called after savesequence format method.
     * Thread used to update the transactions whose entry number is matched with sequence format pattern. 
     */
    public synchronized void updateExistingtransactionmatchedWithsequenceFormat(HttpServletRequest request, Object valuereturn) {
        try {
            JSONObject jObj = getTransactionIds(valuereturn);
            jObj.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            HashMap requestParams = new HashMap();
            String prefix = request.getParameter("prefix");
                String suffix = request.getParameter("suffix");
                String dateFormatinPrefix = request.getParameter("selecteddateformat");
                String selecteddateformatafterprefix = request.getParameter("selecteddateformatafterprefix");
                int numberofdigit = Integer.parseInt(request.getParameter("numberofdigit"));
                int startfrom = 1;
                if (!StringUtil.isNullOrEmpty(request.getParameter("startfrom"))) {
                    startfrom = Integer.parseInt(request.getParameter("startfrom"));
                }
                boolean showleadingzero = StringUtil.getBoolean(request.getParameter("showleadingzero"));
                boolean isdefaultformat = StringUtil.getBoolean(request.getParameter("isdefaultformat"));
                boolean isshowdateinprefix = StringUtil.getBoolean(request.getParameter("showdateinprefix"));
                boolean showdateafterprefix = StringUtil.getBoolean(request.getParameter("showdateafterprefix"));
                boolean isChecked = StringUtil.getBoolean(request.getParameter("isChecked"));
                boolean showdateaftersuffix = StringUtil.getBoolean(request.getParameter("showdateaftersuffix"));
                String selectedsuffixdateformat = request.getParameter("selectedsuffixdateformat");

                requestParams.put("prefix", prefix);
                requestParams.put("suffix", suffix);
                requestParams.put("numberofdigit", numberofdigit);
                requestParams.put("startfrom", startfrom);
                requestParams.put("showleadingzero", showleadingzero);
                requestParams.put("isdefaultformat", isdefaultformat);
                requestParams.put("isshowdateinprefix", isshowdateinprefix);
                requestParams.put("dateFormatinPrefix", dateFormatinPrefix);
                requestParams.put("isshowdateafterprefix", showdateafterprefix);
                requestParams.put("dateFormatAfterPrefix", selecteddateformatafterprefix);
                requestParams.put("showdateaftersuffix", showdateaftersuffix);
                requestParams.put("selectedsuffixdateformat", selectedsuffixdateformat);
                requestParams.put("moduleid", jObj.optInt(Constants.moduleid));
                requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                String format = "";
                for (int i = 0; i < numberofdigit; i++) {
                    format += "0";
                }
                /*
                 * If user select Date after prefix then this date format will
                 * be part of Prefix. Becuse we cannot add this date format in
                 * the middle of Prefix & Number digit. Because
                 * prefix+number+suffix becomes NAME. And at the time data
                 * retrival we get Name & then append prefix/suffix to it.
                 */
                prefix = prefix + selecteddateformatafterprefix;    //SDP-3810 

                format = prefix + format + suffix;
                requestParams.put("name", format);
            updateExistingTransactionsWithMatchedSequenceformate.add(requestParams);
            updateExistingTransactionsWithMatchedSequenceformate.add(jObj);
            Thread t = new Thread(updateExistingTransactionsWithMatchedSequenceformate);
            t.start();
        } catch (Exception ex) {
            Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
// Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
    private void deleteJEPostofInvoice(Map<String, String> passInvobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            issuccess = true;
            String invoiceid = passInvobj.get("invoiceid");
            String companyid = passInvobj.get("companyid");
            if (!StringUtil.isNullOrEmpty(invoiceid) && !StringUtil.isNullOrEmpty(companyid)) {
                accInvoiceDAOobj.permanentDeleteJournalEntryFromInvoice(invoiceid, companyid);
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
            } catch (JSONException ex) {
                Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * SDP-14735
     * @param passInvobj 
     */
    private void saveSalesInvoiceRequestDataAndSendEmail(JSONObject paramJObj, boolean isBeforeSave) {
        try {
            String CustomerVendorTypeId = "";
            String GSTINRegistrationTypeId = "";
            
            if (!isBeforeSave && !StringUtil.isNullOrEmpty(paramJObj.optString("invoiceid", ""))) {
                List list = accInvoiceDAOobj.getGSTDocumentHistory(paramJObj);
                if (list != null && !list.isEmpty()) {
                    for (Object string : list) {
                        Object[] data = (Object[]) string;
                        CustomerVendorTypeId = data!=null && data[1] !=null ? (String) data[1] : "";
                        GSTINRegistrationTypeId = data!=null && data[2] !=null ? (String) data[2] : "";
                    }
                }
            } else if (isBeforeSave) {
                CustomerVendorTypeId = paramJObj.optString("CustomerVendorTypeId", "");
                GSTINRegistrationTypeId = paramJObj.optString("GSTINRegistrationTypeId", "");
            }
            if(StringUtil.isNullOrEmpty(CustomerVendorTypeId) || StringUtil.isNullOrEmpty(GSTINRegistrationTypeId)){
                accInvoiceDAOobj.saveSalesInvoiceRequestData(paramJObj, isBeforeSave);
                Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), paramJObj.optString("companyid", ""));
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                String htmlMsg ="GST History empty for Sales Invoice document <br> <b>Subdomain :</b>"+ company.getSubDomain();
                htmlMsg += " <br><b>Invoice Number</b>: "+paramJObj.optString("number", "");
                DateFormat df = authHandler.getDateOnlyFormat();    
                htmlMsg += " <br><b>Invoice Date</b>: "+df.parse(paramJObj.optString(Constants.BillDate));
                htmlMsg += " <br><b>URL</b>: " + paramJObj.optString(Constants.PAGE_URL);
                String plainMsg ="";
                String fromID = Constants.ADMIN_EMAILID;
                String subject = "GST History empty for Sales Invoice document";
                SendMailHandler.postMail(new String[]{"rahul.bhawar@krawlernetworks.com","suhas.chaware@krawlernetworks.com","sagar.ahire@deskera.com","swapnil.khandre@krawlernetworks.com"}, subject, htmlMsg, plainMsg, fromID, smtpConfigMap);
            }
        } catch (Exception ex) {
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private void saveJournalEntryRequestSendEmailOnEmptyData(JSONObject paramJObj) {
        try {
            String currencyId = "";
            currencyId = paramJObj.optString("currencyid", "");
            if (StringUtil.isNullOrEmpty(currencyId)) {
                Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), paramJObj.optString("companyid", ""));
                JournalEntry je = (JournalEntry) kwlCommonTablesDAOObj.getClassObject(JournalEntry.class.getName(), paramJObj.optString("jeid", ""));
                
                if (je !=null && je.getCurrency() == null) {
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    String htmlMsg = "<br>Currency is not present for Journal Entry document <br> <br><b>Subdomain : </b>" + company.getSubDomain();
                    htmlMsg += " <br><br><b>Journal Entry Number</b> : " + je.getEntryNumber();
                    DateFormat df = authHandler.getDateOnlyFormat();
                    htmlMsg += " <br><br><b>Journal Entry Date</b>: " + je.getEntryDate();
                    htmlMsg += " <br><br><b>URL</b>: " + paramJObj.optString(Constants.PAGE_URL);
                    htmlMsg += " <br><br>";
                    String plainMsg = "";
                    String fromID = Constants.ADMIN_EMAILID;
                    String subject = "Currency is not present for Journal Entry document";
//                    SendMailHandler.postMail(new String[]{"shrinath.shinde@krawlernetworks.com"}, subject, htmlMsg, plainMsg, fromID, smtpConfigMap);
                    SendMailHandler.postMail(new String[]{"shrinath.shinde@krawlernetworks.com", "sagar.ahire@deskera.com", "swapnil.khandre@krawlernetworks.com"}, subject, htmlMsg, plainMsg, fromID, smtpConfigMap);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
