/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.FieldConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.FileUploadHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountControllerCMN;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationController;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.creditnote.accCreditNoteController;
import com.krawler.spring.accounting.creditnote.accCreditNoteService;
import com.krawler.spring.accounting.creditnote.accCreditNoteServiceCMN;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteController;
import com.krawler.spring.accounting.debitnote.accDebitNoteService;
import static com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceHandler.getTermDetails;
import com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.*;
import com.krawler.spring.accounting.jasperreports.FinanceDetails;
import com.krawler.spring.accounting.jasperreports.FinanceDetailsSubReport;
import com.krawler.spring.accounting.jasperreports.InventoryMovementDetails;
import com.krawler.spring.accounting.jasperreports.OnlyDatePojo;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentController;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.accounting.vendorpayment.service.AccVendorPaymentModuleService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.AccExportOtherReportsServiceImpl;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accOtherReportsController extends MultiActionController{
      private exportMPXDAOImpl exportDaoObj;
      private accJournalEntryDAO accJournalEntryobj;
      private accCurrencyDAO accCurrencyDAOobj;
      private AccountingHandlerDAO accountingHandlerDAOobj;
      private accAccountDAO accAccountDAOobj;
      private accGoodsReceiptDAO accGoodsReceiptDAOObj;
      private accGoodsReceiptCMN accGoodsReceiptCommon;
      private accInvoiceDAO accInvoiceDAOobj;
      private accInvoiceCMN accInvoiceCommon;
      private accReceiptDAO accReceiptDao;
      AccReportsService accReportsService;
      private accVendorPaymentDAO accVendorPaymentobj;
      private AccInvoiceServiceDAO accInvoiceServiceDAO;
      private accProductDAO accProductObj;
      private accCreditNoteService accCreditNoteService;
      private accCreditNoteServiceCMN accCreditNoteServiceCMN;
      private accDebitNoteService accDebitNoteService;
      private fieldDataManager fieldDataManagercntrl;
      private accTaxDAO accTaxObj;
      private accBankReconciliationDAO accBankReconciliationObj;
      private AccFinancialReportsService accFinancialReportsService;
      private MessageSource messageSource;
      private AccCommonTablesDAO accCommonTablesDAO;
      private AccOtherReportsService accOtherReportsService;
      private AccVendorPaymentModuleService accVendorPaymentModuleServiceObj; 
      private kwlCommonTablesDAO kwlCommonTablesDAOObj;
      private ImportHandler importHandler;
  
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    /**
     * @param accVendorPaymentModuleServiceObj the
     * accVendorPaymentModuleServiceObj to set
     */
    public void setAccVendorPaymentModuleServiceObj(AccVendorPaymentModuleService accVendorPaymentModuleServiceObj) {
        this.accVendorPaymentModuleServiceObj = accVendorPaymentModuleServiceObj;
    }

    public void setAccOtherReportsService(AccOtherReportsService accOtherReportsService) {
        this.accOtherReportsService = accOtherReportsService;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
     public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
      public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
      public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
      public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
      public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
       public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
     public void setAccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOObj){
        this.accGoodsReceiptDAOObj = accGoodsReceiptDAOObj;
    }
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }   

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }   
    public void setaccReceiptDAO(accReceiptDAO accReceiptDao) {
		this.accReceiptDao = accReceiptDao;
	}
   
    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }
    
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    
    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }
    
    public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }
    
    public void setaccCreditNoteServiceCMN(accCreditNoteServiceCMN accCreditNoteServiceCMN) {
        this.accCreditNoteServiceCMN = accCreditNoteServiceCMN;
    }
    
    public void setaccDebitNoteService(accDebitNoteService accDebitNoteService) {
        this.accDebitNoteService = accDebitNoteService;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    
    public void setAccFinancialReportsService(AccFinancialReportsService accFinancialReportsService) {
        this.accFinancialReportsService = accFinancialReportsService;
    }
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    
    public ModelAndView getJournalEntryForFinance(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr=new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try{
            HashMap<String, Object> requestParams = accReportsController.getJournalEntryMap(request);
             KwlReturnObject result = null;
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid")!=null)?request.getParameter("gcurrencyid"):sessionHandlerImpl.getCurrencyID(request);            
            String accountid=request.getParameter("accountid")!=null? request.getParameter("accountid"):"";
            String currencyid=request.getParameter("currencyid")!=null? request.getParameter("currencyid"):"";
            String companyid = "";
            if(consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("currencyid", currencyid); 
                requestParams.put("accountid", accountid);
                requestParams.put("deleted", true);
                    result = accJournalEntryobj.getJournalEntryForFinanceReport(requestParams);
                
                jobj = getJournalEntryJsonMergedForFinanceReport(requestParams,request, result.getEntityList(), DataJArr);
            }
            
            int cnt = consolidateFlag?DataJArr.length():result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            if(consolidateFlag) {
                String start = request.getParameter(Constants.start);
                String limit = request.getParameter(Constants.limit);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            
            jobj.put("data", pagedJson);
            //jobj.put("count", cnt);
           // exportDaoObj.processRequest(request, response, jobj);
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     public JSONObject getJournalEntryJsonMergedForFinanceReport(HashMap<String, Object> requestParams, HttpServletRequest request, List<JournalEntry> list, JSONArray jArr) throws ServiceException {
        JSONObject jobj = new JSONObject();
            try {
          
            KwlReturnObject result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) requestParams.get("gcurrencyid"));
            KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
            //KwlReturnObject jedresult = accJournalEntryobj.getJournalEntryDetailsForReportForFinanceReport(requestParams);
            String creditAccname="";
            String debitAccname="";
            double creditAmount=0.0;
            double debitAmount=0.0;
            double debitCurrencyAmount=0.0;
            double creditCurrencyAmount=0.0;
            boolean creditFlag=false;
            boolean debitFlag=false;
            
            
            DateFormat df = (DateFormat) requestParams.get("df");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn,"customfield"));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId,0,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            for(JournalEntry entry : list) {
               // String jeId = entry.getID();
                String currencyid=entry.getCurrency()==null?currency.getCurrencyID(): entry.getCurrency().getCurrencyID();
                creditAmount=0.0;
                debitAmount=0.0;
                debitCurrencyAmount=0.0;
                creditCurrencyAmount=0.0;
                creditAccname="";
                debitAccname="";
                requestParams.put("debit","debit");      //For Dedit Type Jedetails from je 
                requestParams.put("jeId", entry.getID());
                result = accJournalEntryobj.getJournalEntryDetailsForFinanceReport(requestParams);
                List<JournalEntryDetail> debitList=result.getEntityList();
                for(JournalEntryDetail entryDetail:debitList){
                    debitFlag=true;
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, entryDetail.getAmount(), currencyid, entryDetail.getJournalEntry().getEntryDate(),entryDetail.getJournalEntry().getExternalCurrencyRate());                        
                    //KwlReturnObject currencyAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, entryDetail.getAmount(), currencyid, entryDetail.getJournalEntry().getEntryDate(),entryDetail.getJournalEntry().getExternalCurrencyRate());                        
                    String accname = StringUtil.isNullOrEmpty(entryDetail.getAccount().getAcccode())?entryDetail.getAccount().getName():"["+entryDetail.getAccount().getAcccode()+"] "+entryDetail.getAccount().getName();
                    debitAmount  = debitAmount + (Double)bAmt.getEntityList().get(0);
                    debitCurrencyAmount=debitCurrencyAmount+entryDetail.getAmount();
                    if(debitAccname.equals("")){
                        debitAccname=accname;
                    }else{
                        debitAccname=debitAccname+","+accname;
                    }
                }
                
                requestParams.put("debit","credit");    //For Credit Type Jedetails from je
                result = accJournalEntryobj.getJournalEntryDetailsForFinanceReport(requestParams);
                List<JournalEntryDetail> creditList=result.getEntityList();
                for(JournalEntryDetail entryDetail:creditList){
                    creditFlag=true;
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, entryDetail.getAmount(), currencyid, entryDetail.getJournalEntry().getEntryDate(),entryDetail.getJournalEntry().getExternalCurrencyRate());                        
                    String accname = StringUtil.isNullOrEmpty(entryDetail.getAccount().getAcccode())?entryDetail.getAccount().getName():"["+entryDetail.getAccount().getAcccode()+"] "+entryDetail.getAccount().getName();
                     creditAmount  = creditAmount + (Double)bAmt.getEntityList().get(0);
                     creditCurrencyAmount=creditCurrencyAmount+entryDetail.getAmount();
                     if(creditAccname.equals("")){
                        creditAccname=accname;
                    }else{
                        creditAccname=creditAccname+","+accname;
                    }
                     
                }     
                
                if(debitFlag==true || creditFlag==true){
                        JSONObject obj = new JSONObject();
                        obj.put("entrydate", df.format(entry.getEntryDate()));        
                        obj.put("entryno", entry.getEntryNumber());
                        obj.put("currencyid",entry.getCurrency()==null?currency.getCurrencyID(): entry.getCurrency().getCurrencyID());
                        obj.put("currencysymbol",entry.getCurrency()==null?currency.getSymbol(): entry.getCurrency().getSymbol());
                        obj.put("currencyname",entry.getCurrency()==null?currency.getName(): entry.getCurrency().getName());
//                        if(debitFlag==true){
//                            obj.put("amount", debitCurrencyAmount);
//                            obj.put("amountinbase", debitAmount);
//                        }else{
                            obj.put("amount", creditCurrencyAmount);
                            obj.put("amountinbase", creditAmount);
                        //}
                        obj.put("accountname",creditAccname);
                        obj.put("remitto",debitAccname);
                        obj.put("journalentryid",entry.getID());
                        obj.put("referid", UUID.randomUUID().toString());
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
                            Detailfilter_names.add("companyid");
                            Detailfilter_params.add(entry.getCompany().getCompanyID());
                            Detailfilter_names.add("journalentryId");
                            Detailfilter_params.add(entry.getID());
                            Detailfilter_names.add("moduleId");
                            Detailfilter_params.add(Constants.Acc_GENERAL_LEDGER_ModuleId+"");
                            invDetailRequestParams.put("filter_names", Detailfilter_names);
                            invDetailRequestParams.put("filter_params", Detailfilter_params);
                            KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomData(invDetailRequestParams);
                            if(idcustresult.getEntityList().size()>0) {
                                AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                                AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap,variableMap);
                                DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                Date dateFromDB=null;
                                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                    String coldata = varEntry.getValue().toString();
                                    if(customFieldMap.containsKey(varEntry.getKey())){
                                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                        // FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), coldata);
                                        if(fieldComboData != null){
                                            obj.put(varEntry.getKey(), fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                        }
                                    }else if(customDateFieldMap.containsKey(varEntry.getKey())){
                                        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        dateFromDB=defaultDateFormat.parse(coldata);
                                        coldata=sdf.format(dateFromDB);
                                        obj.put(varEntry.getKey(), coldata);
                                    }else{
                                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                                obj.put(varEntry.getKey(), coldata);
                                                }
                                        }
                                }
                            }    
                        jArr.put(obj);
                }
                creditFlag=false;
                debitFlag=false;
            }
//            jobj.put("data", jArr);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("getJournalEntryJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }
     public ModelAndView deleteCustomizeReportColumn(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            int reportId = 0;
            String id="";
            String companyId = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                reportId = Integer.parseInt(request.getParameter("reportId"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("id"))) {
                id = request.getParameter("id");
            }
            accountingHandlerDAOobj.deleteCustomizeReportColumn(id,companyId,reportId);
            issuccess = true;
         } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
        
             
    public ModelAndView getCustomizeReportViewMappingField(HttpServletRequest request, HttpServletResponse response) {

        boolean issuccess = false;
        JSONObject jobj = new JSONObject();
        JSONObject newJobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        try {
            int reportId=0;
            int moduleid=0;
             String companyId = sessionHandlerImpl.getCompanyid(request);
              if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                reportId=Integer.parseInt(request.getParameter("reportId"));
            }
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", companyId);
            hashMap.put("reportId", reportId);
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleid"))) {
                moduleid = Integer.parseInt(request.getParameter("moduleid"));
                hashMap.put("moduleId", moduleid);
            }
             KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
             List customizeReportList = customizeReportResult.getEntityList();
             int count=customizeReportResult.getRecordTotalCount();
             for(int i=0;i<count;i++){
                 CustomizeReportMapping customizeReportMapping = (CustomizeReportMapping) customizeReportList.get(i);
                 newJobj = new JSONObject();
                 newJobj.put("id",customizeReportMapping.getId());
                 newJobj.put("fieldDataIndex",customizeReportMapping.getDataIndex());
                 newJobj.put("headerName",customizeReportMapping.getDataHeader());
                 jArr.put(newJobj);
             }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
     
public ModelAndView exportJournalEntryFinance(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr=new JSONArray();
        String view = "jsonView_ex";
        ModelAndView mav = null;
        try{
            HashMap<String, Object> requestParams = accReportsController.getJournalEntryMap(request);    
            requestParams.put("exportQuery", true);
            if(request.getParameter("groupid") != null && Boolean.parseBoolean(request.getParameter("groupid")))
            	requestParams.put("groupid", true);
            KwlReturnObject result = null;
            boolean consolidateFlag = request.getParameter("consolidateFlag")!=null?Boolean.parseBoolean(request.getParameter("consolidateFlag")):false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids")!=null)?request.getParameter("companyids").split(","):sessionHandlerImpl.getCompanyid(request).split(",");
            String currencyid = request.getParameter("currencyid")!=null?request.getParameter("currencyid"):sessionHandlerImpl.getCurrencyID(request); 
            String companyid = "";
            if(consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", currencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", currencyid);  
                requestParams.put("currencyid", currencyid);  
                
                result = accJournalEntryobj.getJournalEntryForFinanceReport(requestParams);
                
                jobj = getJournalEntryJsonMergedForFinanceReport(requestParams,request,result.getEntityList(), DataJArr);
            }  
            jobj.put("data", DataJArr);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            if (StringUtil.equal(fileType, "pdf")){
                view = "jsonView-empty";
                exportFinanceDetailsJasper(request,response);
            }
            else
            {
            exportDaoObj.processRequest(request, response, jobj);
            mav = new ModelAndView(view, "model", jobj.toString());
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return mav;
    }

    public void exportFinanceDetailsJasper(HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> financeDetailsMap = new HashMap<String, Object>();
        String mainReport = "";
        String subReport = "";
        FinanceDetails financeDetails = new FinanceDetails();
        ArrayList<FinanceDetails> financeDetailsList = new ArrayList<FinanceDetails>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int templateflag=Integer.parseInt(request.getParameter("templateflag"));
            if(templateflag==Constants.sats_templateflag){
                mainReport="/SatsFinanceDetails.jrxml";
            }else{
                mainReport="/FinanceDetails.jrxml";
                subReport="/FinanceDetailsSubReport.jrxml";
            }
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            financeDetails.setName(company.getCompanyName());
            financeDetails.setEmail(company.getEmailID() != null ? company.getEmailID() : "");
            financeDetails.setFax(company.getFaxNumber() != null ? company.getFaxNumber() : "");
            financeDetails.setPhone(company.getPhoneNumber() != null ? company.getPhoneNumber() : "");
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            String endDate = df.format(authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate")));
            Calendar c = Calendar.getInstance();
            c.setTime(df.parse(endDate)); // number of days to substract
//            c.add(Calendar.DATE, -1);
            endDate = df.format(c.getTime());
            String startDate = df.format(authHandler.getDateOnlyFormat(request).parse(request.getParameter("startdate")));
            String date = "From Date : " + startDate + ",To Date : " + endDate;
            financeDetails.setDateRange(date);
            financeDetailsMap = getFinanceDetailsJasper(request, response);
            String currency = financeDetailsMap.get("currency").toString();
            financeDetailsMap.remove("currency");
            if (currency != "") {
                financeDetails.setCurrencyinword(currency);
            }
            financeDetailsList.add(financeDetails);
            financeDetailsMap.put("format", "pdf");
            String fileName = StorageHandler.GetDocStorePath() + companyid + "_template" + FileUploadHandler.getCompanyImageExt();
            String fileName2 = StorageHandler.GetDocStorePath()+ companyid + "_onlyLogo" + FileUploadHandler.getCompanyImageExt();
            if(templateflag==Constants.sms_templateflag || templateflag==Constants.smsholding_templateflag){
                financeDetailsMap.put("imagePath", fileName2);
            }else{
                financeDetailsMap.put("imagePath", fileName);
            }
            financeDetailsMap.put("datasource", new JRBeanCollectionDataSource(financeDetailsList));
            
            InputStream inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + mainReport);
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            if(templateflag != Constants.sats_templateflag){
                InputStream inputStreamSubReport = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + subReport);
                JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
                JasperReport jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);
                financeDetailsMap.put("FinanceDetailsSubReport", jasperReportSubReport);
            }

            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(financeDetailsList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, financeDetailsMap, beanColDataSource);
            response.setHeader("Content-Disposition", "attachment;filename=" + "FinanceDetails_v1.pdf");
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
            
        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Map<String, Object> getFinanceDetailsJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException {


        ArrayList<FinanceDetailsSubReport> financeDetailsSubReportList = new ArrayList< FinanceDetailsSubReport>();
        HashMap<String, FinanceDetailsSubReport> fdSubReportMap = new HashMap<String, FinanceDetailsSubReport>();
        Map<String, Object> financeDetailsMap = new HashMap<String, Object>();
        KWLCurrency currency = null;

        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            if (gcurrencyid != null) {
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
                currency = (KWLCurrency) curresult.getEntityList().get(0);
            }
            ModelAndView result = getJournalEntryForFinance(request, response);
            String model = (String) result.getModel().get("model");
            JSONObject objJSONObj = new JSONObject(model);
            JSONArray objJSONArray = objJSONObj.getJSONArray("data");

            for (int i = 0; i < objJSONArray.length(); i++) {
                JSONObject obj = objJSONArray.getJSONObject(i);
                FinanceDetailsSubReport financeDetailsSubReport = new FinanceDetailsSubReport();
                financeDetailsSubReport.setAccountname(obj.getString("accountname") != null ? obj.getString("accountname") : "");
                financeDetailsSubReport.setEntryno(obj.getString("entryno") != null ? obj.getString("entryno") : "");
                financeDetailsSubReport.setEntrydate(obj.getString("entrydate") != null ? dateFormat.format(authHandler.getDateOnlyFormat(request).parse(obj.getString("entrydate"))) : "");
                financeDetailsSubReport.setRemmitto(obj.getString("remitto") != null ? obj.getString("remitto") : "");
                financeDetailsSubReport.setCurrency(obj.getString("currencysymbol") != null ? obj.getString("currencysymbol") : "");
                financeDetailsSubReport.setCurrencyword(obj.getString("currencyname") != null ? obj.getString("currencyname") : "");
                financeDetailsSubReport.setBasecurrency(currency.getCurrencyCode() != null ? currency.getCurrencyCode() : "");
                if (obj.getString("amount") != null) {
                    Double amount = Double.parseDouble(obj.getString("amount"));
                    financeDetailsSubReport.setAmount(amount);
                }
                if (obj.getString("amountinbase") != null) {
                    Double amountinbase = Double.parseDouble(obj.getString("amountinbase"));
                    financeDetailsSubReport.setCcy(amountinbase);
                }
                String Custom_Project = "";
                if (!StringUtil.isNullOrEmpty(obj.optString("Custom_Project"))) {
                    Custom_Project = obj.getString("Custom_Project");
                    financeDetailsSubReport.setProject(Custom_Project != null ? Custom_Project : "");
                }
                String Custom_Due_Date = "";
                if (!StringUtil.isNullOrEmpty(obj.optString("Custom_Due_Date"))) {
                    Custom_Due_Date = dateFormat.format(authHandler.getDateOnlyFormat(request).parse(obj.getString("Custom_Due_Date")));
                    financeDetailsSubReport.setDuedate(Custom_Due_Date != null ? Custom_Due_Date : "");
                }
                financeDetailsSubReportList.add(financeDetailsSubReport);
                fdSubReportMap.put(gcurrencyid, financeDetailsSubReport);
            }
            financeDetailsMap.put("FinanceDetailsSubReportData", new JRBeanCollectionDataSource(financeDetailsSubReportList));
            financeDetailsMap.put("currency", currency.getCurrencyCode() != null ? currency.getCurrencyCode() : "");

        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getFinanceDetailsJasper : " + ex.getMessage(), ex);
        }
        return financeDetailsMap;
    }
     public ModelAndView getCustomerLedger(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            jobj = getCustomerLedger(request);
            JSONArray jArr = jobj.getJSONArray("data");
            JSONArray pagedJson = jArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getCustomerLedger(HttpServletRequest request) throws ServiceException, SessionExpiredException {

        JSONObject jobj = new JSONObject();
        try {

            JSONArray jArr = new JSONArray();
            JSONObject jobjTemp = new JSONObject();
            String companyid = sessionHandlerImpl.getCompanyid(request);
             boolean invoiceAmountDueFilter=true;
                if(request.getParameter("invoiceAmountDueFilter")!=null)
                {
                    invoiceAmountDueFilter=Boolean.parseBoolean(request.getParameter("invoiceAmountDueFilter"));
                }
            boolean isPostDatedCheque =false;
            if(request.getParameter("isPostDatedCheque")!=null){
                isPostDatedCheque=Boolean.parseBoolean(request.getParameter("isPostDatedCheque"));
            }
            boolean isLedgerReport =false;
            if(request.getParameter("ledgerReport")!=null){
                isLedgerReport=Boolean.parseBoolean(request.getParameter("ledgerReport"));
            }
            Date startDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String customerIds = request.getParameter("accountid");
            String withoutinventory = request.getParameter("withoutinventory");
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);          
//            requestParams.put("customerIds", customerIds);
            requestParams.put("withoutinventory", withoutinventory);
            requestParams.put("startDate", startDate);
            requestParams.put("endDate", endDate);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isLedgerReport", isLedgerReport);
            requestParams.put("showDishonouredPayment", true);
            String custIDSet[]=customerIds.split(",");
            for(int c=0;c<custIDSet.length;c++){
                requestParams.put("customerIds", custIDSet[c]);
            KwlReturnObject customerLedger = accInvoiceDAOobj.getCustomerLedgerReport(requestParams);
            List customerLedgerList = customerLedger.getEntityList();
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);

            double balanceAmtInBase = 0;
            double balanceAmount = 0;
            double totalCreditAmount = 0;
            double totalDebitAmountAmount = 0;
            String prevCustomer = "";
            String accountid="",accountname="",accCode="",accCodeName=""; 
            String currencyid="";
            String invoiceCustomerName="";
            KWLCurrency currency = null;
            Account account = null;
            
            for(int i=0; i<customerLedgerList.size(); i++){
                account = null;
                accountname="";
                Object[] tempCustomerLedger = (Object[]) customerLedgerList.get(i);
                String type = (String) tempCustomerLedger[0];
                String invoiceId = (String) tempCustomerLedger[1]; // if(type=='invoice') ==> invoice.id, if(type=='receipt') ==> receipt.id, if(type=='creditnote') ==> creditnote.id
                String invoiceNumber = (String) tempCustomerLedger[2]; // if(type=='invoice') ==> invoice.invoicenumber, if(type=='receipt') ==> receipt.receiptnumber, if(type=='creditnote') ==> creditnote.cnnumber
                String jeId = tempCustomerLedger[3]==null ? "" : (String) tempCustomerLedger[3]; // journalentry.id
                String memo = StringUtil.isNullOrEmpty((String)tempCustomerLedger[4]) ? "" : (String) tempCustomerLedger[4]; // if(type=='invoice') ==> invoice.memo, if(type=='receipt') ==> receipt.memo, if(type=='creditnote') ==> creditnote.memo
                currencyid = StringUtil.isNullOrEmpty((String) tempCustomerLedger[5]) ? sessionHandlerImpl.getCurrencyID(request) : (String) tempCustomerLedger[5]; // if(type=='invoice') ==> invoice.currency, if(type=='receipt') ==> receipt.currency, if(type=='creditnote') ==> creditnote.currency
                double amount = (Double) tempCustomerLedger[6]; // if(type=='invoice') ==> jedetail.amount, if(type=='receipt') ==> receiptdetails.amount, if(type=='creditnote') ==> jedetail.amount
                String jeEntryNumber = tempCustomerLedger[7]==null ? "" : (String) tempCustomerLedger[7];
                Date jeEntryDate = (Date) tempCustomerLedger[8];
                double jeEntryExternalCurrencyRate = (Double) tempCustomerLedger[9];
                String invoiceCustomerId = (String) tempCustomerLedger[10];
                invoiceCustomerName = (String) tempCustomerLedger[11];
                String withoutInventaryFlag = (String) tempCustomerLedger[13];
                double cndnAmountDue = (Double) tempCustomerLedger[14];
                String tranAccountId = (String) tempCustomerLedger[15];
                int openingtransactionFlag = Integer.parseInt(tempCustomerLedger[16].toString());
                /*
                 * Check For is lease Sales Invoice 
                 */
                boolean isLeaseFixedAsset = (tempCustomerLedger.length > 20) ? (!StringUtil.isNullOrEmpty(tempCustomerLedger[20].toString()) ? Integer.parseInt(tempCustomerLedger[20].toString()) == 1 : false): false;
                boolean isOpeningBalanceTransaction = false;
                
                if (openingtransactionFlag == 1) {
                    isOpeningBalanceTransaction = true;
                }
                
                boolean isConversionRateFromCurrencyToBase = false;
                int conversionRateFromCurrencyToBaseFlag = Integer.parseInt(tempCustomerLedger[17].toString());
                if (conversionRateFromCurrencyToBaseFlag == 1) {
                    isConversionRateFromCurrencyToBase = true;
                }
               
                if(!prevCustomer.equals(invoiceCustomerId)){
                    prevCustomer = invoiceCustomerId;
                    balanceAmtInBase = 0;
                    balanceAmount = 0;
                }
              
                if(!StringUtil.isNullOrEmpty(currencyid)){
                    KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
                    currency = (KWLCurrency) curresult1.getEntityList().get(0);
                }
                if(!StringUtil.isNullOrEmpty(tranAccountId)){
                    KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(Account.class.getName(), tranAccountId);
                    account = (Account) curresult1.getEntityList().get(0);
                    accountname=account!=null?account.getName():"";
                }
                  Customer customer = null;
                if(!StringUtil.isNullOrEmpty(invoiceCustomerId)){
                    KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(Customer.class.getName(), invoiceCustomerId);
                    customer = (Customer) curresult1.getEntityList().get(0);
                    accountid=customer.getAccount().getID();
//                    accountname=customer.getAccount().getName();
                    accCode=customer.getAccount().getAcccode();
                }
                
                jobjTemp = new JSONObject();
                String emptyChar="";
                 if (!(type.equalsIgnoreCase("Cash Sale") || type.equalsIgnoreCase("Vendor Invoice") || type.equalsIgnoreCase("Payment Received") || type.equalsIgnoreCase("Credit Note") || type.equalsIgnoreCase(Constants.DISHONOURED_MAKE_PAYMENT))) {
                    emptyChar="d"; 
                    jobjTemp.put("c_type", type);
                    jobjTemp.put("invoiceId", invoiceId);
                    jobjTemp.put("billid", invoiceId);
                    jobjTemp.put("noteid", invoiceId);
                    jobjTemp.put("referid", UUID.randomUUID().toString());
                    jobjTemp.put("c_transactionID", invoiceNumber);
                    jobjTemp.put("isLeaseFixedAsset", isLeaseFixedAsset);
                    jobjTemp.put("c_journalentryid", jeId);
                    jobjTemp.put("c_transactionDetails", memo);
                    jobjTemp.put("currencyid", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getCurrencyID() : currencyid);
                    jobjTemp.put("currencysymbol", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getSymbol() : currency.getSymbol());
                    jobjTemp.put("c_entryno", jeEntryNumber);
                    jobjTemp.put("c_date", df.format(jeEntryDate));
//                    jobjTemp.put("jeEntryExternalCurrencyRate", jeEntryExternalCurrencyRate);
                    jobjTemp.put("c_accountname", accountname);
                    jobjTemp.put("c_acccode", accCode);
                    jobjTemp.put("accCode", accCode);
                    jobjTemp.put("accCodeName", invoiceCustomerName);
                    jobjTemp.put(emptyChar + "_date", "");
                    jobjTemp.put(emptyChar + "_accountname", "");
                    jobjTemp.put(emptyChar + "_acccode", "");
                    jobjTemp.put(emptyChar + "_entryno", "");
                    jobjTemp.put(emptyChar + "_journalentryid", "");
                    jobjTemp.put(emptyChar + "_amount", "");
                    jobjTemp.put(emptyChar + "_type", "");
                    jobjTemp.put(emptyChar + "_amountAccountCurrency", "");
                    jobjTemp.put(emptyChar + "_transactionID", "");
                    jobjTemp.put(emptyChar + "_transactionDetails", "");
//                    jobjTemp.put("amountDue", "");
                }else{
                    emptyChar="c";   
                    jobjTemp.put("d_type", type);
                    jobjTemp.put("invoiceId", invoiceId);
                    jobjTemp.put("billid", invoiceId);
                    jobjTemp.put("noteid", invoiceId);
                    jobjTemp.put("referid", UUID.randomUUID().toString());
                    jobjTemp.put("d_transactionID", invoiceNumber);
                    jobjTemp.put("d_journalentryid", jeId);
                    jobjTemp.put("isLeaseFixedAsset", isLeaseFixedAsset);
                    jobjTemp.put("d_transactionDetails", memo);
                    jobjTemp.put("currencyid", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getCurrencyID() : currencyid);
                    jobjTemp.put("currencysymbol", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getSymbol() : currency.getSymbol());
                    jobjTemp.put("d_entryno", jeEntryNumber);
                    jobjTemp.put("d_date", df.format(jeEntryDate));
//                    jobjTemp.put("jeEntryExternalCurrencyRate", jeEntryExternalCurrencyRate);
                    jobjTemp.put("d_accountname", accountname);
                    jobjTemp.put("d_acccode", accCode);
                    jobjTemp.put("accCode", accCode);
                    jobjTemp.put("accCodeName", invoiceCustomerName);
                    jobjTemp.put(emptyChar + "_date", "");
                    jobjTemp.put(emptyChar + "_accountname", "");
                    jobjTemp.put(emptyChar + "_acccode", "");
                    jobjTemp.put(emptyChar + "_entryno", "");
                    jobjTemp.put(emptyChar + "_journalentryid", "");
                    jobjTemp.put(emptyChar + "_amount", "");
                    jobjTemp.put(emptyChar + "_type", "");
                    jobjTemp.put(emptyChar + "_amountAccountCurrency", "");
                    jobjTemp.put(emptyChar + "_transactionID", "");
                    jobjTemp.put(emptyChar + "_transactionDetails", "");
                }
                KwlReturnObject bAmt = null;
                if (isOpeningBalanceTransaction && isConversionRateFromCurrencyToBase) {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, jeEntryDate, jeEntryExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, currencyid, jeEntryDate, jeEntryExternalCurrencyRate);
                }

                double amountinbase = (Double) bAmt.getEntityList().get(0);
                amountinbase = authHandler.round(amountinbase, companyid);
                  if(type.equalsIgnoreCase("Cash Sale")){
                    jobjTemp.put("c_amount", "");
                    jobjTemp.put("d_amount", amountinbase);
                    totalDebitAmountAmount=totalDebitAmountAmount+amountinbase;
//                     balanceAmtInBase = balanceAmtInBase - amountinbase;
//                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);
//                     balanceAmount = balanceAmount - amount; 
                    jobjTemp.put("isCash", true);
                    jobjTemp.put("c_amountAccountCurrency", "");
                    jobjTemp.put("d_amountAccountCurrency",  (amount!=0)?authHandler.round(amount, companyid):"");
//                    jobjTemp.put("balanceAmount",  (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                }else if(type.equalsIgnoreCase("Vendor Invoice")){
                    balanceAmtInBase = balanceAmtInBase - amountinbase;
                    jobjTemp.put("c_amount", "");
                    jobjTemp.put("d_amount", amountinbase);
                    totalDebitAmountAmount=totalDebitAmountAmount+amountinbase;
//                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);

                    balanceAmount = balanceAmount - amount;
                    jobjTemp.put("c_amountAccountCurrency", "");
                    jobjTemp.put("d_amountAccountCurrency", (amount!=0)?authHandler.round(amount, companyid):"");
//                    jobjTemp.put("balanceAmount", (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                }else if(type.equalsIgnoreCase("Payment Received")){
                    balanceAmtInBase = balanceAmtInBase - amountinbase;
                    jobjTemp.put("c_amount", "");
                    jobjTemp.put("d_amount", amountinbase);
                    totalDebitAmountAmount=totalDebitAmountAmount+amountinbase;
//                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);

                    balanceAmount = balanceAmount - amount;
                    jobjTemp.put("c_amountAccountCurrency", "");
                    jobjTemp.put("d_amountAccountCurrency", (amount!=0)?authHandler.round(amount, companyid):"");
//                    jobjTemp.put("balanceAmount", (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                }else if(type.equalsIgnoreCase("Credit Note")){
                    balanceAmtInBase = balanceAmtInBase - amountinbase;
                    jobjTemp.put("c_amount", "");
                    jobjTemp.put("d_amount", amountinbase);
                    totalDebitAmountAmount=totalDebitAmountAmount+amountinbase;
//                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);

                    balanceAmount = balanceAmount - amount;
                    jobjTemp.put("c_amountAccountCurrency", "");
                    jobjTemp.put("d_amountAccountCurrency", (amount!=0)?authHandler.round(amount, companyid):"");
//                    jobjTemp.put("balanceAmount", (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                }else {  //if((type.equalsIgnoreCase("Payment Made")) || (type.equalsIgnoreCase("Debit Note")))
                    balanceAmtInBase = balanceAmtInBase + amountinbase;
                    jobjTemp.put("c_amount", amountinbase);
                    jobjTemp.put("d_amount", "");
                    totalCreditAmount=totalCreditAmount+amountinbase;
//                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);

                    balanceAmount = balanceAmount + amount;
                    jobjTemp.put("c_amountAccountCurrency", (amount!=0)?authHandler.round(amount, companyid):"");
                    jobjTemp.put("d_amountAccountCurrency", "");
//                    jobjTemp.put("balanceAmount", (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                }
                jArr.put(jobjTemp);
                
              }
                 String entryChar = "", emptyChar = "";
                 if (balanceAmtInBase != 0) {
                    if (balanceAmtInBase < 0) {
                        entryChar = "c";
                        emptyChar = "d";
                    } else {
                        entryChar = "d";
                        emptyChar = "c";
                    }
                    JSONObject objlast = new JSONObject();
                    objlast.put(entryChar + "_date", request.getParameter("enddate"));
                    objlast.put(entryChar + "_accountname", "Balance c/f");
                    objlast.put(entryChar + "_acccode", "");
                    objlast.put(entryChar + "_journalentryid", "");
                    objlast.put(entryChar + "_amount", (Double) Math.abs(balanceAmtInBase));
                    objlast.put(entryChar + "_transactionID", "");
                    objlast.put(entryChar + "_transactionDetails", "");
                    objlast.put(entryChar + "_checkno", "");
                    objlast.put(entryChar + "_transactionDetailsBankBook", "");
                    objlast.put(entryChar + "_amountAccountCurrency","");
                    objlast.put(emptyChar + "_amountAccountCurrency", "");
                    objlast.put(emptyChar + "_date", "");
                    objlast.put(emptyChar + "_accountname", "");
                    objlast.put(emptyChar + "_journalentryid", "");
                    objlast.put(emptyChar + "_amount", "");
                    objlast.put(emptyChar + "_transactionID", "");
                    objlast.put(emptyChar + "_transactionDetails", "");
                    objlast.put(emptyChar + "_checkno", "");
                    objlast.put(emptyChar + "_transactionDetailsBankBook", "");
                    objlast.put("accountid", accountid);
    //                accountname = StringUtil.isNullOrEmpty(account.getName())? "" : account.getName();
                    objlast.put("accountname", invoiceCustomerName);
                    objlast.put("currencysymbol", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getSymbol() : currency.getSymbol());
    //                String acccode = StringUtil.isNullOrEmpty(account.getAcccode())? "" : account.getAcccode();
                    objlast.put("accCode",accCode);
                    objlast.put("currencyid", baseCurrency.getCurrencyID());

                    //this is for displaying on the group header
//                    if (!accCode.equals(""))
//                        objlast.put("accCodeName",accCode + " - " + accountname);
//                    else
                        objlast.put("accCodeName", invoiceCustomerName);

                    objlast.put("balanceAmount", authHandler.round(Math.abs(balanceAmtInBase), companyid));                
                    objlast.put("balanceAmountAccountCurrency", authHandler.round(Math.abs(balanceAmount), companyid));
                    jArr.put(objlast);
               

                    if (request.getParameter("filetype") != null) {
//                        if (request.getParameter("filetype").equals("print")) {
                             if (balanceAmtInBase < 0) {
                                entryChar = "c";
                                emptyChar = "d";
                                totalCreditAmount=totalCreditAmount+balanceAmtInBase;
                            } else {
                                entryChar = "d";
                                emptyChar = "c";
                                totalDebitAmountAmount=totalDebitAmountAmount+balanceAmtInBase;
                            }
                            JSONObject total1 = new JSONObject();
                            total1.put(entryChar + "_date", request.getParameter("enddate"));
                            total1.put(entryChar + "_accountname", "Total");
                            total1.put(entryChar + "_journalentryid", "");
                            total1.put(entryChar + "_amount", authHandler.round(Math.abs(totalCreditAmount), companyid));
                            total1.put(emptyChar + "_date", request.getParameter("enddate"));
                            total1.put(emptyChar + "_accountname", "Total");
                            total1.put(emptyChar + "_acccode", "");
                            total1.put(emptyChar + "_journalentryid", "");
                            total1.put(entryChar + "_transactionID", "");
                            total1.put(emptyChar + "_transactionID", "");
                            total1.put(entryChar + "_transactionDetails", "");
                            total1.put(emptyChar + "_transactionDetails", "");
                            total1.put(emptyChar + "_amount", authHandler.round(Math.abs(totalDebitAmountAmount), companyid));
                            total1.put("accountid", accountid);

                            // to separate account code & names into 2 different columns
//                            accountname = StringUtil.isNullOrEmpty(account.getName())? "" : account.getName();
                            total1.put("accountname", invoiceCustomerName);
                            total1.put("currencysymbol", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getSymbol() : currency.getSymbol());
                            total1.put("currencyid", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getCurrencyID() : currencyid);
                            	total1.put("accCodeName", invoiceCustomerName);
                            
                            jArr.put(total1);
//                        }
                    }
                }
            
            jobj.put("data", jArr);
            }
        } catch (ParseException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountsExceedingBudget : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountsExceedingBudget : " + ex.getMessage(), ex);
        }

        return jobj;
    }
    
    

    public ModelAndView getVendorLedger(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            jobj = getVendorLedger(request);
            JSONArray jArr = jobj.getJSONArray("data");
            JSONArray pagedJson = jArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getVendorLedger(HttpServletRequest request) throws ServiceException, SessionExpiredException {

        JSONObject jobj = new JSONObject();
        try {

            JSONArray jArr = new JSONArray();
            JSONObject jobjTemp = new JSONObject();
            String companyid = sessionHandlerImpl.getCompanyid(request);
           
            boolean isPostDatedCheque =false;
            if(request.getParameter("isPostDatedCheque")!=null){
                isPostDatedCheque=Boolean.parseBoolean(request.getParameter("isPostDatedCheque"));
            }
            boolean isLedgerReport =false;
            if(request.getParameter("ledgerReport")!=null){
                isLedgerReport=Boolean.parseBoolean(request.getParameter("ledgerReport"));
            }
            Date startDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("enddate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String vendorIds = request.getParameter("accountid");
            String withoutinventory = request.getParameter("withoutinventory");
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
           
            requestParams.put("withoutinventory", withoutinventory);
            requestParams.put("startDate", startDate);
            requestParams.put("endDate", endDate);
//            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isLedgerReport", isLedgerReport);
             String vendIDSet[]=vendorIds.split(",");
             requestParams.put("showDishonouredPayment",true);
            for(int v=0;v<vendIDSet.length;v++){
                requestParams.put("vendorIds", vendIDSet[v]);
                KwlReturnObject vendorLedger = accInvoiceDAOobj.getVendorLedgerReport(requestParams);
                List vendorLedgerList = vendorLedger.getEntityList();
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);

                double balanceAmtInBase = 0;
                double balanceAmount = 0;
                double totalCreditAmount = 0;
                double totalDebitAmountAmount = 0;
                String prevVendor = "";
                String accountid="",accountname="",accCode="",accCodeName="";
                KWLCurrency currency = null;
                Account account = null;
                String currencyid ="";
                String invoiceVendorName="";

                for(int i=0; i<vendorLedgerList.size(); i++){

                    Object[] tempVendorLedger = (Object[]) vendorLedgerList.get(i);
                    String type = (String) tempVendorLedger[0];
                    String invoiceId = (String) tempVendorLedger[1]; // if(type=='goodsreceipt') ==> goodsreceipt.id, if(type=='payment') ==> payment.id, if(type=='debitnote') ==> debitnote.id
                    String invoiceNumber = (String) tempVendorLedger[2]; // if(type=='goodsreceipt') ==> goodsreceipt.grnumber, if(type=='payment') ==> payment.paymentnumber, if(type=='debitnote') ==> debitnote.dnnumber
                    String jeId = tempVendorLedger[3]==null ? "" : (String) tempVendorLedger[3]; // journalentry.id
                    String memo = StringUtil.isNullOrEmpty((String)tempVendorLedger[4]) ? "" : (String) tempVendorLedger[4]; // if(type=='goodsreceipt') ==> goodsreceipt.memo, if(type=='payment') ==> payment.memo, if(type=='debitnote') ==> debitnote.memo
                    currencyid = StringUtil.isNullOrEmpty((String) tempVendorLedger[5]) ? sessionHandlerImpl.getCurrencyID(request) : (String) tempVendorLedger[5]; // if(type=='goodsreceipt') ==> goodsreceipt.currency, if(type=='payment') ==> payment.currency, if(type=='debitnote') ==> debitnote.currency
                    double amount = (Double) tempVendorLedger[6]; // if(type=='goodsreceipt') ==> jedetail.amount, if(type=='payment') ==> paymentdetails.amount, if(type=='debitnote') ==> jedetail.amount
                    String jeEntryNumber = tempVendorLedger[7]==null ? "" : (String) tempVendorLedger[7];
                    Date jeEntryDate = (Date) tempVendorLedger[8];
                    double jeEntryExternalCurrencyRate = (Double) tempVendorLedger[9];
                    String invoiceVendorId = (String) tempVendorLedger[10];
                    invoiceVendorName = (String) tempVendorLedger[11];
                    String withoutInventaryFlag = (String) tempVendorLedger[13];
                    double cndnAmountDue = (Double) tempVendorLedger[14];
                    String tranAccountID = (String) tempVendorLedger[15];
                    int openingtransactionFlag = Integer.parseInt(tempVendorLedger[16].toString());
                    boolean isOpeningBalanceTransaction = false;
                    if(openingtransactionFlag == 1){
                        isOpeningBalanceTransaction = true;
                    }
                    
                    boolean isConversionRateFromCurrencyToBase = false;
                    int conversionRateFromCurrencyToBaseFlag = Integer.parseInt(tempVendorLedger[17].toString());
                    if(conversionRateFromCurrencyToBaseFlag == 1){
                        isConversionRateFromCurrencyToBase = true;
                    }

    //                if(!prevVendor.equals(invoiceVendorId)){
    //                    prevVendor = invoiceVendorId;
    //                    balanceAmtInBase = 0;
    //                    balanceAmount = 0;
    //                }


                    if(!StringUtil.isNullOrEmpty(currencyid)){
                        KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
                        currency = (KWLCurrency) curresult1.getEntityList().get(0);
                    }
                    if(!StringUtil.isNullOrEmpty(tranAccountID)){
                        KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(Account.class.getName(), tranAccountID);
                        account = (Account) curresult1.getEntityList().get(0);
                        accountname=account!=null?account.getName():"";
                    }
                    Vendor vendor = null;
                    if(!StringUtil.isNullOrEmpty(invoiceVendorId)){
                        KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(Vendor.class.getName(), invoiceVendorId);
                        vendor = (Vendor) curresult1.getEntityList().get(0);
                        accountid=vendor.getAccount().getID();                        
                        accCode=vendor.getAccount().getAcccode();
                    }

                    jobjTemp = new JSONObject();

                      String emptyChar="";

                    if(type.equalsIgnoreCase("Cash Purchase") ||type.equalsIgnoreCase("Vendor Invoice")||type.equalsIgnoreCase("Payment Received")||type.equalsIgnoreCase("Credit Note")||type.equalsIgnoreCase(Constants.DISHONOURED_MAKE_PAYMENT)){
                        emptyChar="d";
                        jobjTemp.put("c_type", type);
                        jobjTemp.put("invoiceId", invoiceId);
                        jobjTemp.put("billid", invoiceId);
                        jobjTemp.put("noteid", invoiceId);
                        jobjTemp.put("referid", UUID.randomUUID().toString());
                        jobjTemp.put("c_transactionID", invoiceNumber);
                        jobjTemp.put("c_journalentryid", jeId);
                        jobjTemp.put("c_transactionDetails", memo);
                        jobjTemp.put("currencyid", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getCurrencyID() : currencyid);
                        jobjTemp.put("currencysymbol", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getSymbol() : currency.getSymbol());
                        jobjTemp.put("c_entryno", jeEntryNumber);
                        jobjTemp.put("c_date", df.format(jeEntryDate));
    //                    jobjTemp.put("jeEntryExternalCurrencyRate", jeEntryExternalCurrencyRate);
                        jobjTemp.put("c_accountname", accountname);
                        jobjTemp.put("c_acccode", accCode);
                        jobjTemp.put("accCode", accCode);
                        jobjTemp.put("accCodeName", invoiceVendorName);
                        jobjTemp.put(emptyChar + "_date", "");
                        jobjTemp.put(emptyChar + "_accountname", "");
                        jobjTemp.put(emptyChar + "_acccode", "");
                        jobjTemp.put(emptyChar + "_entryno", "");
                        jobjTemp.put(emptyChar + "_journalentryid", "");
                        jobjTemp.put(emptyChar + "_amount", "");
                        jobjTemp.put(emptyChar + "_type", "");
                        jobjTemp.put(emptyChar + "_amountAccountCurrency", "");
                        jobjTemp.put(emptyChar + "_transactionID", "");
                        jobjTemp.put(emptyChar + "_transactionDetails", "");
    //                    jobjTemp.put("amountDue", "");
                    }else{
                        emptyChar="c";
                        jobjTemp.put("d_type", type);
                        jobjTemp.put("invoiceId", invoiceId);
                        jobjTemp.put("billid", invoiceId);
                        jobjTemp.put("noteid", invoiceId);
                        jobjTemp.put("referid", UUID.randomUUID().toString());
                        jobjTemp.put("d_transactionID", invoiceNumber);
                        jobjTemp.put("d_journalentryid", jeId);
                        jobjTemp.put("d_transactionDetails", memo);
                        jobjTemp.put("currencyid", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getCurrencyID() : currencyid);
                        jobjTemp.put("currencysymbol", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getSymbol() : currency.getSymbol());
                        jobjTemp.put("d_entryno", jeEntryNumber);
                        jobjTemp.put("d_date", df.format(jeEntryDate));
    //                    jobjTemp.put("jeEntryExternalCurrencyRate", jeEntryExternalCurrencyRate);
                        jobjTemp.put("d_accountname", accountname);
                        jobjTemp.put("d_acccode", accCode);
                        jobjTemp.put("accCode", accCode);
                        jobjTemp.put("accCodeName", invoiceVendorName);
                        jobjTemp.put(emptyChar + "_date", "");
                        jobjTemp.put(emptyChar + "_accountname", "");
                        jobjTemp.put(emptyChar + "_acccode", "");
                        jobjTemp.put(emptyChar + "_entryno", "");
                        jobjTemp.put(emptyChar + "_journalentryid", "");
                        jobjTemp.put(emptyChar + "_amount", "");
                        jobjTemp.put(emptyChar + "_type", "");
                        jobjTemp.put(emptyChar + "_amountAccountCurrency", "");
                        jobjTemp.put(emptyChar + "_transactionID", "");
                        jobjTemp.put(emptyChar + "_transactionDetails", "");
                    }
                    KwlReturnObject bAmt = null;
                    if (isOpeningBalanceTransaction && isConversionRateFromCurrencyToBase) {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, jeEntryDate, jeEntryExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, currencyid, jeEntryDate, jeEntryExternalCurrencyRate);
                    }
                    
                    double amountinbase = (Double) bAmt.getEntityList().get(0);
                    amountinbase = authHandler.round(amountinbase, companyid);

                    if(type.equalsIgnoreCase("Cash Purchase")){
                        jobjTemp.put("d_amount", "");
//                        balanceAmtInBase = balanceAmtInBase - amountinbase;
                        jobjTemp.put("c_amount", amountinbase);
                        totalCreditAmount=totalCreditAmount+amountinbase;
    //                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);
//                        balanceAmount = balanceAmount - amount;
                        jobjTemp.put("d_amountAccountCurrency", "");
                        jobjTemp.put("isCash", true);
                        jobjTemp.put("c_amountAccountCurrency",  (amount!=0)?authHandler.round(amount, companyid):"");
    //                    jobjTemp.put("balanceAmount",  (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                    }else if(type.equalsIgnoreCase("Vendor Invoice")){
                        balanceAmtInBase = balanceAmtInBase - amountinbase;
                        jobjTemp.put("d_amount", "");
                        jobjTemp.put("c_amount", amountinbase);
                        totalCreditAmount=totalCreditAmount+amountinbase;
    //                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);

                        balanceAmount = balanceAmount - amount;
                        jobjTemp.put("d_amountAccountCurrency", "");
                        jobjTemp.put("c_amountAccountCurrency", (amount!=0)?authHandler.round(amount, companyid):"");
    //                    jobjTemp.put("balanceAmount", (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                    }else if(type.equalsIgnoreCase("Payment Received") || type.equalsIgnoreCase(Constants.DISHONOURED_MAKE_PAYMENT)){
                        balanceAmtInBase = balanceAmtInBase - amountinbase;
                        jobjTemp.put("d_amount", "");
                        jobjTemp.put("c_amount", amountinbase);
                        totalCreditAmount=totalCreditAmount+amountinbase;
    //                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);

                        balanceAmount = balanceAmount - amount;
                        jobjTemp.put("d_amountAccountCurrency", "");
                        jobjTemp.put("c_amountAccountCurrency", (amount!=0)?authHandler.round(amount, companyid):"");
    //                    jobjTemp.put("balanceAmount", (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                    }else if(type.equalsIgnoreCase("Credit Note")){
                        balanceAmtInBase = balanceAmtInBase - amountinbase;
                        jobjTemp.put("d_amount", "");
                        jobjTemp.put("c_amount", amountinbase);
                        totalCreditAmount=totalCreditAmount+amountinbase;
    //                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);

                        balanceAmount = balanceAmount - amount;
                        jobjTemp.put("d_amountAccountCurrency", "");
                        jobjTemp.put("c_amountAccountCurrency", (amount!=0)?authHandler.round(amount, companyid):"");
    //                    jobjTemp.put("balanceAmount", (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                    }else {  //if((type.equalsIgnoreCase("Payment Made")) || (type.equalsIgnoreCase("Debit Note")))
                        balanceAmtInBase = balanceAmtInBase + amountinbase;
                        jobjTemp.put("d_amount", amountinbase);
                        jobjTemp.put("c_amount", "");
                        totalDebitAmountAmount=totalDebitAmountAmount+amountinbase;
    //                    jobjTemp.put("balanceAmountInBase", balanceAmtInBase);

                        balanceAmount = balanceAmount + amount;
                        jobjTemp.put("d_amountAccountCurrency", (amount!=0)?authHandler.round(amount, companyid):"");
                        jobjTemp.put("c_amountAccountCurrency", "");
    //                    jobjTemp.put("balanceAmount", (balanceAmount!=0)?authHandler.round(balanceAmount, 2):"");
                    }
                    jArr.put(jobjTemp);
            }
                String entryChar = "", emptyChar = "";
                    if (balanceAmtInBase != 0) {
                        if (balanceAmtInBase > 0) {
                            entryChar = "c";
                            emptyChar = "d";
                        } else {
                            entryChar = "d";
                            emptyChar = "c";
                        }
                        JSONObject objlast = new JSONObject();
                        objlast.put(entryChar + "_date", request.getParameter("enddate"));
                        objlast.put(entryChar + "_accountname", "Balance c/f");
                        objlast.put(entryChar + "_acccode", "");
                        objlast.put(entryChar + "_journalentryid", "");
                        objlast.put(entryChar + "_amount", (Double) Math.abs(balanceAmtInBase));
                        objlast.put(entryChar + "_transactionID", "");
                        objlast.put(entryChar + "_transactionDetails", "");
                        objlast.put(entryChar + "_checkno", "");
                        objlast.put(entryChar + "_transactionDetailsBankBook", "");
                        objlast.put(entryChar + "_amountAccountCurrency","");
                        objlast.put(emptyChar + "_amountAccountCurrency", "");
                        objlast.put(emptyChar + "_date", "");
                        objlast.put(emptyChar + "_accountname", "");
                        objlast.put(emptyChar + "_journalentryid", "");
                        objlast.put(emptyChar + "_amount", "");
                        objlast.put(emptyChar + "_transactionID", "");
                        objlast.put(emptyChar + "_transactionDetails", "");
                        objlast.put(emptyChar + "_checkno", "");
                        objlast.put(emptyChar + "_transactionDetailsBankBook", "");
                        objlast.put("accountid", accountid);
        //                accountname = StringUtil.isNullOrEmpty(account.getName())? "" : account.getName();
                        objlast.put("accountname", invoiceVendorName);
                        objlast.put("currencysymbol", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getSymbol() : currency.getSymbol());
        //                String acccode = StringUtil.isNullOrEmpty(account.getAcccode())? "" : account.getAcccode();
                        objlast.put("accCode",accCode);
                        objlast.put("currencyid", baseCurrency.getCurrencyID());

                        //this is for displaying on the group header
    //                    if (!accCode.equals(""))
    //                        objlast.put("accCodeName",accCode + " - " + invoiceVendorName);
    //                    else
                            objlast.put("accCodeName", invoiceVendorName);

                        objlast.put("balanceAmount", authHandler.round(Math.abs(balanceAmtInBase), companyid));                
                        objlast.put("balanceAmountAccountCurrency", authHandler.round(Math.abs(balanceAmount), companyid));
                        jArr.put(objlast);


                }
                 if (request.getParameter("filetype") != null) {
//                        if (request.getParameter("filetype").equals("print")) {
                             if (balanceAmtInBase < 0) {
                                entryChar = "c";
                                emptyChar = "d";
                                totalDebitAmountAmount=totalDebitAmountAmount+balanceAmtInBase;
                            } else {
                                entryChar = "d";
                                emptyChar = "c";
                                totalCreditAmount=totalCreditAmount+balanceAmtInBase;
                            }
                            JSONObject total1 = new JSONObject();
                            total1.put(entryChar + "_date", request.getParameter("enddate"));
                            total1.put(entryChar + "_accountname", "Total");
                            total1.put(entryChar + "_journalentryid", "");
                            total1.put(entryChar + "_amount", authHandler.round(Math.abs(totalCreditAmount), companyid));
                            total1.put(emptyChar + "_date", request.getParameter("enddate"));
                            total1.put(emptyChar + "_accountname", "Total");
                            total1.put(emptyChar + "_acccode", "");
                            total1.put(emptyChar + "_journalentryid", "");
                            total1.put(entryChar + "_transactionID", "");
                            total1.put(emptyChar + "_transactionID", "");
                            total1.put(entryChar + "_transactionDetails", "");
                            total1.put(emptyChar + "_transactionDetails", "");
                            total1.put(emptyChar + "_amount", authHandler.round(Math.abs(totalDebitAmountAmount), companyid));
                            total1.put("accountid", accountid);

                            total1.put("accountname", invoiceVendorName);
                            total1.put("currencysymbol", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getSymbol() : currency.getSymbol());
                            total1.put("currencyid", StringUtil.isNullOrEmpty(currencyid) ? baseCurrency.getCurrencyID() : currencyid);

                            	total1.put("accCodeName", invoiceVendorName);
                            
                            jArr.put(total1);
//                        }
                    }
                    jobj.put("data", jArr);
            }
        } catch (ParseException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountsExceedingBudget : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountsExceedingBudget : " + ex.getMessage(), ex);
        }

        return jobj;
    }
     public ModelAndView exportCustomerLedger(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            jobj = getCustomerLedger(request);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView exportVendorLedger(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try{
            jobj = getVendorLedger(request);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView getRCNReportData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getRCNReportData(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getRCNReportData(HttpServletRequest request) throws ServiceException, SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        double total_amount_usd = 0;
        double total_inteedton = 0;
        double total_price = 0;
        double total_bankcharges = 0;
        double total_bankint = 0;
        double total_amount = 0;
        double total_tons = 0;
        double total_amount_usd1 = 0;
        double total_freight_usd = 0;
        double total_insurchg = 0;
        double total_usdbankcharge = 0;
        double total_bankint_usd = 0;
        double total_claimscn = 0;
        double total_claims = 0;
        double nett = 0;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String accid = request.getParameter("accname");
        String refAccId = "";
        if (accid.equals("")) {
            accid = "All";
        }
        try {
            JSONArray jArr = new JSONArray();
            JSONArray DataJArr = new JSONArray();

            HashMap<String, Object> requestParams = accReportsController.getJournalEntryMap(request);
            if (request.getParameter("groupid") != null && Boolean.parseBoolean(request.getParameter("groupid"))) {
                requestParams.put("groupid", true);
            }

            KwlReturnObject result = null;

            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);

            String companyid = "";
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }

            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }

            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("partyjournal", CCConstants.isPartyJE);
                result = accJournalEntryobj.getJournalEntry(requestParams);
                JSONObject jobj1 = accReportsService.getJournalEntryJsonMerged(requestParams, result.getEntityList(), DataJArr);

                for (int i = 0; i < DataJArr.length(); i++) {
                    JSONObject getObj = DataJArr.getJSONObject(i);
                    if (getObj.has("partlyJeEntryWithCnDn")) {
                        if ((Integer) getObj.get("partlyJeEntryWithCnDn") == 1) {
                            // code to put data from the Party Journal entry     
                            JSONArray jedetails = getObj.getJSONArray("jeDetails");
                            for (int count = 0; count < jedetails.length(); count++) {
                                JSONObject test1 = jedetails.getJSONObject(count);
                                if (test1.getString("debit").equals("Credit")) {
                                    refAccId = test1.getString("accountid");

                                }
                            }
                            if ((accid.equals(refAccId) && !accid.equals("All")) || accid.equals("All")) {
                                String bank = null;
                                String ref = null;
                                String supplier = null;
                                String lctrno = getObj.getString("entryno");
                                String ttdate = df.format(authHandler.getDateOnlyFormat(request).parse(getObj.get("entrydate").toString()));
                                double amount_euro = 0;
                                double amount_usd = 0;
                                double inteedton = 0;
                                double price = 0;
                                for (int j = 0; j < jedetails.length(); j++) {
                                    JSONObject test = jedetails.getJSONObject(j);
                                    if (test.getString("debit").equals("Credit")) {
                                        bank = test.getString("accountname");
                                        refAccId = test.getString("accountid");
                                        amount_usd = test.getDouble("c_amount");
                                        total_amount_usd = total_amount_usd + amount_usd;
                                    } else {
                                        supplier = test.getString("accountname");
                                    }
                                }
                                amount_euro = authHandler.round(0.78 * amount_usd, companyid);

                                JSONArray fields = getFieldParams(request, getObj.getString("journalentryid"), "24");
                                for (int k = 0; k < fields.length(); k++) {
                                    JSONObject customfield = fields.getJSONObject(k);
                                    String fieldlabel = customfield.getString("fieldlabel");
                                    if (fieldlabel.equals("Ref")) {
                                        if (customfield.has("fieldData")) {
                                            ref = customfield.getString("fieldData");
                                        }
                                    } else if (fieldlabel.equals("INT'ED TON")) {
                                        if (customfield.has("fieldData")) {
                                            inteedton = (Double.parseDouble(customfield.get("fieldData").toString()));
                                            total_inteedton = total_inteedton + inteedton;
                                        }
                                    } else if (fieldlabel.equals("PRICE")) {
                                        if (customfield.has("fieldData")) {
                                            price = (Double.parseDouble(customfield.get("fieldData").toString()));;
                                            total_price = total_price + price;
                                        }
                                    }
                                }

                                // Code to show the data from Payment against GL code
                                double bankcharges = 0;
                                double bankint = 0;
                                double amount = 0;
                                String paidon = null;
                                JSONObject payment = getPayment(request, lctrno);
                                if (payment != null) {
                                    bankcharges = (Double.parseDouble(payment.get("bankCharges").toString()));
                                    total_bankcharges = total_bankcharges + bankcharges;
                                    bankint = (Double.parseDouble(payment.get("bankInterest").toString()));
                                    total_bankint = total_bankint + bankint;
                                    amount = (Double.parseDouble(payment.get("amountinbase").toString()));
                                    total_amount = total_amount + amount;
                                    paidon = df.format(authHandler.getDateOnlyFormat(request).parse(payment.get("billdate").toString()));
                                }

                                // code to put the data from the Sales Invoice(Customer Invoice)
                                String date = null;
                                String invoiceno = null;
                                String customer = null;
                                String fcls = null;
                                double tons = 0;
                                double price1 = 0;
                                double less2per = 0;
                                double freight = 0;
                                double calcamt = 0;
                                double claimscn = 0;
                                double amount_usd1 = 0;
                                double freight_usd = 0;
                                double insurchg = 0;
                                double usdbankcharge = 0;
                                String paidon1 = null;
                                double bankint_usd = 0;
                                String shortageplusor = null;
                                double claims = 0;

                                JSONObject invoice = getInvoice(request, lctrno);
                                if (invoice != null) {
                                    date = df.format(authHandler.getDateOnlyFormat(request).parse(invoice.get("date").toString()));
                                    invoiceno = invoice.get("billno").toString();
                                    customer = invoice.get("customername").toString();
                                    paidon1 = df.format(authHandler.getDateOnlyFormat(request).parse(invoice.get("date").toString()));
                                    amount_usd1 = (Double.parseDouble(invoice.get("amountinbase").toString()));
                                    total_amount_usd1 = total_amount_usd1 + amount_usd1;
                                    price1 = (Double.parseDouble(invoice.get("Custom_PRICE").toString()));
                                    less2per = (Double.parseDouble(invoice.get("Custom_Less 2 percent").toString()));
                                    calcamt = (Double.parseDouble(invoice.get("Custom_Calc Amt").toString()));
                                    insurchg = (Double.parseDouble(invoice.get("Custom_INSUR CHG").toString()));
                                    total_insurchg = total_insurchg + insurchg;
                                    usdbankcharge = (Double.parseDouble(invoice.get("Custom_USD BANK CHRGS").toString()));
                                    total_usdbankcharge = total_usdbankcharge + usdbankcharge;
                                    bankint_usd = (Double.parseDouble(invoice.get("Custom_BANK INT USD").toString()));
                                    total_bankint_usd = total_bankint_usd + bankint_usd;
                                    shortageplusor = invoice.get("Custom_SHORTAGE PLUS OR").toString();
                                    claims = (Double.parseDouble(invoice.get("Custom_CLAIMS").toString()));
                                    total_claims = total_claims + claims;
                                    fcls = invoice.get("Custom_fcls").toString();
                                    tons = (Double.parseDouble(invoice.get("Custom_TONS").toString()));
                                    total_tons = total_tons + tons;
                                    freight = (Double.parseDouble(invoice.get("Custom_Freight").toString()));
                                    claimscn = (Double.parseDouble(invoice.get("Custom_Claims CN").toString()));
                                    total_claimscn = total_claimscn + claimscn;
                                    freight_usd = (Double.parseDouble(invoice.get("Custom_FREIGHT USD").toString()));
                                    total_freight_usd = total_freight_usd + freight_usd;
                                }

                                // After calculating each term put final data in JSON Object
                                JSONObject putObj = new JSONObject();
                                putObj.put("bank", bank);
                                putObj.put("ref", ref);
                                putObj.put("supplier", supplier);
                                putObj.put("lctrno", lctrno);
                                putObj.put("ttdate", ttdate);
                                putObj.put("amount_euro", amount_euro);
                                putObj.put("amount_usd", amount_usd);
                                putObj.put("inteedton", inteedton);
                                putObj.put("price", price);
                                putObj.put("bankcharges", bankcharges);
                                putObj.put("bankint", bankint);
                                putObj.put("amount", amount);
                                putObj.put("paidon", paidon);
                                putObj.put("date", date);
                                putObj.put("invoiceno1", invoiceno);
                                putObj.put("customer", customer);
                                putObj.put("fcls", fcls);
                                putObj.put("tons", tons);
                                putObj.put("price1", price1);
                                putObj.put("less2per", less2per);
                                putObj.put("freight", freight);
                                putObj.put("calcamt", calcamt);
                                putObj.put("claimscn", claimscn);
                                putObj.put("amount_usd1", amount_usd1);
                                putObj.put("freight_usd", freight_usd);
                                putObj.put("insurchg", insurchg);
                                putObj.put("usdbankcharge", usdbankcharge);
                                putObj.put("paidon1", paidon1);
                                putObj.put("bankint_usd", bankint_usd);
                                putObj.put("shortageplusor", shortageplusor);
                                putObj.put("claims", claims);

                                // Put Complete JSON Object which we used to display the values in JSONArray
                                jArr.put(putObj);
                            }
                        }
                    }
                }
            }
                // Just Add Bank row below the Report
                JSONObject blank = new JSONObject();
                blank.put("bank", "");
                blank.put("ref", "");
                blank.put("supplier", "");
                blank.put("lctrno", "");
                blank.put("ttdate", "");
                blank.put("amount_euro", "");
                blank.put("amount_usd", "");
                blank.put("inteedton", "");
                blank.put("price", "");
                blank.put("bankcharges", "");
                blank.put("bankint", "");
                blank.put("amount", "");
                blank.put("paidon", "");
                blank.put("date", "");
                blank.put("invoiceno1", "");
                blank.put("customer", "");
                blank.put("fcls", "");
                blank.put("tons", "");
                blank.put("price1", "");
                blank.put("less2per", "");
                blank.put("freight", "");
                blank.put("calcamt", "");
                blank.put("claimscn", "");
                blank.put("amount_usd1", "");
                blank.put("freight_usd", "");
                blank.put("insurchg", "");
                blank.put("usdbankcharge", "");
                blank.put("paidon1", "");
                blank.put("bankint_usd", "");
                blank.put("shortageplusor", "");
                blank.put("claims", "");
                jArr.put(blank);

                // Add totals for respective columns
                JSONObject total = new JSONObject();
                total.put("bank", "Total");
                total.put("ref", "");
                total.put("supplier", "");
                total.put("lctrno", "");
                total.put("ttdate", "");
                total.put("amount_euro", "");
                total.put("amount_usd", total_amount_usd);
                total.put("inteedton", total_inteedton);
                total.put("price", total_price);
                total.put("bankcharges", total_bankcharges);
                total.put("bankint", total_bankint);
                total.put("amount", total_amount);
                total.put("paidon", "");
                total.put("date", "");
                total.put("invoiceno1", "");
                total.put("customer", "");
                total.put("fcls", "");
                total.put("tons", total_tons);
                total.put("price1", "");
                total.put("less2per", "");
                total.put("freight", "");
                total.put("calcamt", "");
                total.put("claimscn", total_claimscn);
                total.put("amount_usd1", total_amount_usd1);
                total.put("freight_usd", total_freight_usd);
                total.put("insurchg", total_insurchg);
                total.put("usdbankcharge", total_usdbankcharge);
                total.put("paidon1", "");
                total.put("bankint_usd", total_bankint_usd);
                total.put("shortageplusor", "");
                total.put("claims", "");
                total.put("format", "total");
                jArr.put(total);
                jArr.put(blank);
                //calculate the nett
                nett = total_amount_usd + total_bankcharges + total_bankint + total_amount + total_insurchg + total_usdbankcharge + total_bankint_usd + total_claimscn;

                //put Summary data into the JSONArray 
                JSONObject summary1 = new JSONObject();
                summary1.put("bank", "Remittance(Total of AMOUNT USD)");
                summary1.put("ref", "");
                summary1.put("supplier", "");
                summary1.put("lctrno", total_amount_usd);
                summary1.put("ttdate", "");
                summary1.put("amount_euro", "");
                summary1.put("amount_usd", "");
                summary1.put("inteedton", "");
                summary1.put("price", "");
                summary1.put("bankcharges", "");
                summary1.put("bankint", "");
                summary1.put("amount", "");
                summary1.put("paidon", "");
                summary1.put("date", "");
                summary1.put("invoiceno1", "");
                summary1.put("customer", "");
                summary1.put("fcls", "");
                summary1.put("tons", "");
                summary1.put("price1", "");
                summary1.put("less2per", "");
                summary1.put("freight", "");
                summary1.put("calcamt", "");
                summary1.put("claimscn", "");
                summary1.put("amount_usd1", "");
                summary1.put("freight_usd", "");
                summary1.put("insurchg", "");
                summary1.put("usdbankcharge", "");
                summary1.put("paidon1", "");
                summary1.put("bankint_usd", "");
                summary1.put("shortageplusor", "");
                summary1.put("claims", "");
                summary1.put("format", "title");
                jArr.put(summary1);

                JSONObject summary2 = new JSONObject();
                summary2.put("bank", "Inv Fin B/c(total of BANK CHARGES)");
                summary2.put("ref", "");
                summary2.put("supplier", "");
                summary2.put("lctrno", total_bankcharges);
                summary2.put("ttdate", "");
                summary2.put("amount_euro", "");
                summary2.put("amount_usd", "");
                summary2.put("inteedton", "");
                summary2.put("price", "");
                summary2.put("bankcharges", "");
                summary2.put("bankint", "");
                summary2.put("amount", "");
                summary2.put("paidon", "");
                summary2.put("date", "");
                summary2.put("invoiceno1", "");
                summary2.put("customer", "");
                summary2.put("fcls", "");
                summary2.put("tons", "");
                summary2.put("price1", "");
                summary2.put("less2per", "");
                summary2.put("freight", "");
                summary2.put("calcamt", "");
                summary2.put("claimscn", "");
                summary2.put("amount_usd1", "");
                summary2.put("freight_usd", "");
                summary2.put("insurchg", "");
                summary2.put("usdbankcharge", "");
                summary2.put("paidon1", "");
                summary2.put("bankint_usd", "");
                summary2.put("shortageplusor", "");
                summary2.put("claims", "");
                summary2.put("format", "title");
                jArr.put(summary2);

                JSONObject summary3 = new JSONObject();
                summary3.put("bank", "Inv Fin Int(total of AMOUNT From Payment against GL Code)");
                summary3.put("ref", "");
                summary3.put("supplier", "");
                summary3.put("lctrno", total_amount);
                summary3.put("ttdate", "");
                summary3.put("amount_euro", "");
                summary3.put("amount_usd", "");
                summary3.put("inteedton", "");
                summary3.put("price", "");
                summary3.put("bankcharges", "");
                summary3.put("bankint", "");
                summary3.put("amount", "");
                summary3.put("paidon", "");
                summary3.put("date", "");
                summary3.put("invoiceno1", "");
                summary3.put("customer", "");
                summary3.put("fcls", "");
                summary3.put("tons", "");
                summary3.put("price1", "");
                summary3.put("less2per", "");
                summary3.put("freight", "");
                summary3.put("calcamt", "");
                summary3.put("claimscn", "");
                summary3.put("amount_usd1", "");
                summary3.put("freight_usd", "");
                summary3.put("insurchg", "");
                summary3.put("usdbankcharge", "");
                summary3.put("paidon1", "");
                summary3.put("bankint_usd", "");
                summary3.put("shortageplusor", "");
                summary3.put("claims", "");
                summary3.put("format", "title");
                jArr.put(summary3);

                JSONObject summary4 = new JSONObject();
                summary4.put("bank", "Freight(total of FREIGHT)");
                summary4.put("ref", "");
                summary4.put("supplier", "");
                summary4.put("lctrno", total_freight_usd);
                summary4.put("ttdate", "");
                summary4.put("amount_euro", "");
                summary4.put("amount_usd", "");
                summary4.put("inteedton", "");
                summary4.put("price", "");
                summary4.put("bankcharges", "");
                summary4.put("bankint", "");
                summary4.put("amount", "");
                summary4.put("paidon", "");
                summary4.put("date", "");
                summary4.put("invoiceno1", "");
                summary4.put("customer", "");
                summary4.put("fcls", "");
                summary4.put("tons", "");
                summary4.put("price1", "");
                summary4.put("less2per", "");
                summary4.put("freight", "");
                summary4.put("calcamt", "");
                summary4.put("claimscn", "");
                summary4.put("amount_usd1", "");
                summary4.put("freight_usd", "");
                summary4.put("insurchg", "");
                summary4.put("usdbankcharge", "");
                summary4.put("paidon1", "");
                summary4.put("bankint_usd", "");
                summary4.put("shortageplusor", "");
                summary4.put("claims", "");
                summary4.put("format", "title");
                jArr.put(summary4);

                JSONObject summary5 = new JSONObject();
                summary5.put("bank", "Ins(total of INSUR CHG)");
                summary5.put("ref", "");
                summary5.put("supplier", "");
                summary5.put("lctrno", total_insurchg);
                summary5.put("ttdate", "");
                summary5.put("amount_euro", "");
                summary5.put("amount_usd", "");
                summary5.put("inteedton", "");
                summary5.put("price", "");
                summary5.put("bankcharges", "");
                summary5.put("bankint", "");
                summary5.put("amount", "");
                summary5.put("paidon", "");
                summary5.put("date", "");
                summary5.put("invoiceno1", "");
                summary5.put("customer", "");
                summary5.put("fcls", "");
                summary5.put("tons", "");
                summary5.put("price1", "");
                summary5.put("less2per", "");
                summary5.put("freight", "");
                summary5.put("calcamt", "");
                summary5.put("claimscn", "");
                summary5.put("amount_usd1", "");
                summary5.put("freight_usd", "");
                summary5.put("insurchg", "");
                summary5.put("usdbankcharge", "");
                summary5.put("paidon1", "");
                summary5.put("bankint_usd", "");
                summary5.put("shortageplusor", "");
                summary5.put("claims", "");
                summary5.put("format", "title");
                jArr.put(summary5);

                JSONObject summary6 = new JSONObject();
                summary6.put("bank", "Expt B/c(total Of USD BANK CHRGS)");
                summary6.put("ref", "");
                summary6.put("supplier", "");
                summary6.put("lctrno", total_usdbankcharge);
                summary6.put("ttdate", "");
                summary6.put("amount_euro", "");
                summary6.put("amount_usd", "");
                summary6.put("inteedton", "");
                summary6.put("price", "");
                summary6.put("bankcharges", "");
                summary6.put("bankint", "");
                summary6.put("amount", "");
                summary6.put("paidon", "");
                summary6.put("date", "");
                summary6.put("invoiceno1", "");
                summary6.put("customer", "");
                summary6.put("fcls", "");
                summary6.put("tons", "");
                summary6.put("price1", "");
                summary6.put("less2per", "");
                summary6.put("freight", "");
                summary6.put("calcamt", "");
                summary6.put("claimscn", "");
                summary6.put("amount_usd1", "");
                summary6.put("freight_usd", "");
                summary6.put("insurchg", "");
                summary6.put("usdbankcharge", "");
                summary6.put("paidon1", "");
                summary6.put("bankint_usd", "");
                summary6.put("shortageplusor", "");
                summary6.put("claims", "");
                summary6.put("format", "title");
                jArr.put(summary6);

                JSONObject summary7 = new JSONObject();
                summary7.put("bank", "Expt Bills Int(Total of BANK INT USD)");
                summary7.put("ref", "");
                summary7.put("supplier", "");
                summary7.put("lctrno", total_bankint_usd);
                summary7.put("ttdate", "");
                summary7.put("amount_euro", "");
                summary7.put("amount_usd", "");
                summary7.put("inteedton", "");
                summary7.put("price", "");
                summary7.put("bankcharges", "");
                summary7.put("bankint", "");
                summary7.put("amount", "");
                summary7.put("paidon", "");
                summary7.put("date", "");
                summary7.put("invoiceno1", "");
                summary7.put("customer", "");
                summary7.put("fcls", "");
                summary7.put("tons", "");
                summary7.put("price1", "");
                summary7.put("less2per", "");
                summary7.put("freight", "");
                summary7.put("calcamt", "");
                summary7.put("claimscn", "");
                summary7.put("amount_usd1", "");
                summary7.put("freight_usd", "");
                summary7.put("insurchg", "");
                summary7.put("usdbankcharge", "");
                summary7.put("paidon1", "");
                summary7.put("bankint_usd", "");
                summary7.put("shortageplusor", "");
                summary7.put("claims", "");
                summary7.put("format", "title");
                jArr.put(summary7);

                JSONObject summary8 = new JSONObject();
                summary8.put("bank", "Claims (Total of Claims CN)");
                summary8.put("ref", "");
                summary8.put("supplier", "");
                summary8.put("lctrno", total_claimscn);
                summary8.put("ttdate", "");
                summary8.put("amount_euro", "");
                summary8.put("amount_usd", "");
                summary8.put("inteedton", "");
                summary8.put("price", "");
                summary8.put("bankcharges", "");
                summary8.put("bankint", "");
                summary8.put("amount", "");
                summary8.put("paidon", "");
                summary8.put("date", "");
                summary8.put("invoiceno1", "");
                summary8.put("customer", "");
                summary8.put("fcls", "");
                summary8.put("tons", "");
                summary8.put("price1", "");
                summary8.put("less2per", "");
                summary8.put("freight", "");
                summary8.put("calcamt", "");
                summary8.put("claimscn", "");
                summary8.put("amount_usd1", "");
                summary8.put("freight_usd", "");
                summary8.put("insurchg", "");
                summary8.put("usdbankcharge", "");
                summary8.put("paidon1", "");
                summary8.put("bankint_usd", "");
                summary8.put("shortageplusor", "");
                summary8.put("claims", "");
                summary8.put("format", "title");
                jArr.put(summary8);

                JSONObject summary9 = new JSONObject();
                summary9.put("bank", "NETT");
                summary9.put("ref", "");
                summary9.put("supplier", "");
                summary9.put("lctrno", nett);
                summary9.put("ttdate", "");
                summary9.put("amount_euro", "");
                summary9.put("amount_usd", "");
                summary9.put("inteedton", "");
                summary9.put("price", "");
                summary9.put("bankcharges", "");
                summary9.put("bankint", "");
                summary9.put("amount", "");
                summary9.put("paidon", "");
                summary9.put("date", "");
                summary9.put("invoiceno1", "");
                summary9.put("customer", "");
                summary9.put("fcls", "");
                summary9.put("tons", "");
                summary9.put("price1", "");
                summary9.put("less2per", "");
                summary9.put("freight", "");
                summary9.put("calcamt", "");
                summary9.put("claimscn", "");
                summary9.put("amount_usd1", "");
                summary9.put("freight_usd", "");
                summary9.put("insurchg", "");
                summary9.put("usdbankcharge", "");
                summary9.put("paidon1", "");
                summary9.put("bankint_usd", "");
                summary9.put("shortageplusor", "");
                summary9.put("claims", "");
                summary9.put("format", "title");
                jArr.put(summary9);

                jobj.put("data", jArr);
            }  catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("getRCNReportData : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public JSONArray getFieldParams(HttpServletRequest request, String jeid, String moduleId) throws ServiceException {
        KwlReturnObject result = null;
        JSONArray jresult = new JSONArray();
        int lineitem = StringUtil.isNullOrEmpty(request.getParameter(Constants.customcolumn)) ? 0 : Integer.parseInt(request.getParameter(Constants.customcolumn));
        String module = moduleId;
        String jeId = jeid;
        String[] moduleidarray = request.getParameterValues(Constants.moduleidarray);
        boolean isForProductCustomFieldHistoryCombo = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isForProductCustomFieldHistoryCombo"))) {
            isForProductCustomFieldHistoryCombo = Boolean.parseBoolean(request.getParameter("isForProductCustomFieldHistoryCombo"));
        }
        String commaSepratedModuleids = "";
        if (moduleidarray != null) {
            for (int i = 0; i < moduleidarray.length; i++) {
                if (!StringUtil.isNullOrEmpty(moduleidarray[i])) {
                    commaSepratedModuleids += moduleidarray[i] + ",";
                }
            }
            if (moduleidarray.length > 1) {
                commaSepratedModuleids = commaSepratedModuleids.substring(0, commaSepratedModuleids.length() - 1);
            }
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            Date currentDate = authHandler.getDateOnlyFormat(request).parse(authHandler.getDateOnlyFormat(request).format(new Date()));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Integer colcount = 1;
            if (StringUtil.isNullOrEmpty(commaSepratedModuleids) && StringUtil.isNullOrEmpty(module)) {
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.customcolumn));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, lineitem));
            } else if (StringUtil.isNullOrEmpty(commaSepratedModuleids)) {
                Integer moduleid = Integer.parseInt(module);
                if (moduleid > 99) {  //Added module >100 for Report like ledger, Balance sheet etc
                    if (moduleid == 101 || moduleid == 100 || moduleid == 102) {
                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid));
                        requestParams.put(Constants.filter_values, Arrays.asList(companyid));
                    } else {
                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.customcolumn));
                        requestParams.put(Constants.filter_values, Arrays.asList(companyid, lineitem));
                    }
                } else {
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, lineitem));
                }
            } else {
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.INmoduleid, Constants.customcolumn));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, commaSepratedModuleids, lineitem));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("iscustomfield"))) {
                requestParams.put("customfield", 1);
            }
            Integer moduleidint = 0;
            if (!StringUtil.isNullOrEmpty(module)) {
                moduleidint = Integer.parseInt(module);
            }
            requestParams.put("checkForParent", true);//check if parent-child relation exist.
            result = accAccountDAOobj.getFieldParams(requestParams);
            requestParams.remove("checkForParent");
            List checkForParentList = result.getEntityList();
            if (checkForParentList != null && checkForParentList.size() == 0) {
                requestParams.put("order_by", Arrays.asList("fieldlabel"));
                requestParams.put("order_type", Arrays.asList("asc"));
            }
            if (moduleidint == 100 || moduleidint == 101 || moduleidint == 102) {//Used this query to club the same name dimension/Custom fields
                result = accAccountDAOobj.getFieldParamsUsingSql(requestParams);
            } else {
                result = accAccountDAOobj.getFieldParams(requestParams);
            }
            List lst = result.getEntityList();
            colcount = lst.size();
            AccJECustomData accBillInvCustomData = null;
            KwlReturnObject custumObjresult = null;
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                FieldParams tmpcontyp = null;
                if (moduleidint == 100 || moduleidint == 101 || moduleidint == 102) {
                    Object[] temp = (Object[]) ite.next();
                    KwlReturnObject fieldParamObj = null;
                    try {
                        fieldParamObj = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), temp[1].toString());
                        tmpcontyp = (FieldParams) fieldParamObj.getEntityList().get(0);
                    } catch (ServiceException ex) {
                        Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    tmpcontyp = (FieldParams) ite.next();
                }
                if (isForProductCustomFieldHistoryCombo && !(tmpcontyp.getFieldtype() == 1 || tmpcontyp.getFieldtype() == 2)) {
                    continue;
                }
                JSONObject jobj = new JSONObject();
                jobj.put("fieldname", tmpcontyp.getFieldname());
                if (!StringUtil.isNullOrEmpty(jeId)) {
                    Integer moduleid = Integer.parseInt(module);
                    try {
                        switch (moduleid) {
                            case 24:
                                custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                                break;
                            default:
                                custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                        }
                    } catch (Exception e) {
                    }
                    accBillInvCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                    if (accBillInvCustomData != null) {
                        String coldata = accBillInvCustomData.getCol(tmpcontyp.getColnum());
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            jobj.put("fieldData", coldata);
                        }
                    }
                }
                jobj.put("isessential", tmpcontyp.getIsessential());
                jobj.put("maxlength", tmpcontyp.getMaxlength());
                jobj.put("validationtype", tmpcontyp.getValidationtype());
                jobj.put("fieldid", tmpcontyp.getId());
                jobj.put("moduleid", tmpcontyp.getModuleid());
                jobj.put("fieldtype", tmpcontyp.getFieldtype());
                jobj.put("iseditable", tmpcontyp.getIseditable());
                jobj.put("comboid", tmpcontyp.getComboid());
                jobj.put("comboname", tmpcontyp.getComboname());
                jobj.put("moduleflag", tmpcontyp.getModuleflag());
                jobj.put("parentid", tmpcontyp.getParentid());
                jobj.put("fieldlabel", tmpcontyp.getFieldlabel());

                jresult.put(jobj);
            }
        } catch (ParseException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jresult;
    }

    public JSONObject getPayment(HttpServletRequest request, String ttno) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = accVendorPaymentController.getPaymentMap(request);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            boolean contraentryflag = request.getParameter("contraentryflag") != null;
            boolean isAdvancePayment = request.getParameter("advancePayment") != null;
            boolean isAdvanceToCustomer = request.getParameter("advanceToCustomer") != null;
            boolean isPostDatedCheque = request.getParameter("isPostDatedCheque") != null;
            boolean isDishonouredCheque = request.getParameter("isDishonouredCheque") != null;
            boolean isGlcode = request.getParameter("isGlcode") != null;
            String billid = request.getParameter("billid") != null ? request.getParameter("billid") : "";
            boolean onlyOpeningBalanceTransactionsFlag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyOpeningBalanceTransactionsFlag"))) {
                onlyOpeningBalanceTransactionsFlag = Boolean.parseBoolean(request.getParameter("onlyOpeningBalanceTransactionsFlag"));
            }
            requestParams.put("contraentryflag", contraentryflag);
            requestParams.put("isadvancepayment", isAdvancePayment);
            requestParams.put("isadvancetocustomer", isAdvanceToCustomer);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isDishonouredCheque", isDishonouredCheque);
            requestParams.put("isGlcode", isGlcode);
            requestParams.put("billid", billid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordType"))) {
                requestParams.put("receipttype", request.getParameter("recordType"));
            }
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
            requestParams.put(Constants.start, "");
            requestParams.put(Constants.limit, "");
            KwlReturnObject result = null;
            KwlReturnObject openingBalanceReceiptsResult = null;
            KwlReturnObject billingResult = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                tempList.clear();
                if (onlyOpeningBalanceTransactionsFlag) {
                    // getting opening balance receipts
                    openingBalanceReceiptsResult = accVendorPaymentobj.getAllOpeningBalancePayments(requestParams);
                    JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//                    tempList = accVendorPaymentobj.getOpeningBalanceReceiptJsonForReport(request, openingBalanceReceiptsResult.getEntityList(), tempList);
                    tempList = accVendorPaymentModuleServiceObj.getOpeningBalanceReceiptJsonForReport(paramJobj, openingBalanceReceiptsResult.getEntityList(), tempList);
                } else {
                    result = accVendorPaymentobj.getPayments(requestParams);
                    tempList = accVendorPaymentModuleServiceObj.getPaymentsJson(requestParams, result.getEntityList(), tempList);
                    billingResult = accVendorPaymentobj.getBillingPayments(requestParams);
                    tempList = accVendorPaymentModuleServiceObj.getBillingPaymentsJson(requestParams, billingResult.getEntityList(), tempList);
                }
                list.addAll(tempList);
            }
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                JSONObject jSONObject = (JSONObject) iterator.next();
                String receipttype = jSONObject.getString("receipttype");
                if (receipttype.equals("9")) {
                    JSONArray fields = getFieldParams(request, jSONObject.getString("journalentryid"), "14");
                    for (int k = 0; k < fields.length(); k++) {
                        JSONObject customfield = fields.getJSONObject(k);
                        String fieldlabel = customfield.getString("fieldlabel");
                        if (fieldlabel.equals("TT Number")) {
                            if (customfield.has("fieldData")) {
                                String chkttno = customfield.getString("fieldData");
                                if (chkttno.equals(ttno)) {
                                    return jSONObject;
                                }
                            }
                        }
                    }
                }
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (ServiceException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public JSONObject getInvoice(HttpServletRequest request, String ttno) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        try {
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean isAged = request.getParameter("isAged") != null ? Boolean.parseBoolean(request.getParameter("isAged")) : false;
            boolean custInvFlagForSalesPerson = request.getParameter("salesPersonFilterFlag") != null ? Boolean.parseBoolean(request.getParameter("salesPersonFilterFlag")) : false;
            boolean isForTemplate = false;
            boolean onlyOutstanding = false;
            boolean report = false;
            int totalCount = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isForTemplate"))) {
                isForTemplate = Boolean.parseBoolean(request.getParameter("isForTemplate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("onlyOutsatnding"))) {
                onlyOutstanding = Boolean.parseBoolean(request.getParameter("onlyOutsatnding"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("report"))) {
                report = Boolean.parseBoolean(request.getParameter("report"));
            }
            boolean eliminateflag = consolidateFlag;
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                request.setAttribute("onlyOutstanding", onlyOutstanding);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("consolidateFlag", consolidateFlag);
                requestParams.put("isForTemplate", isForTemplate);
                requestParams.put("datefilter", request.getParameter("datefilter"));
                requestParams.put("custVendorID", request.getParameter("custVendorID"));
                if (custInvFlagForSalesPerson) {
                    requestParams.put("custInvFlagForSalesPerson", custInvFlagForSalesPerson);
                }
                KwlReturnObject result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                totalCount = result != null ? result.getRecordTotalCount() : 0;
                List list = result.getEntityList();
                DataJArr = accInvoiceServiceDAO.getInvoiceJsonMerged(request, list, DataJArr);
            }

            for (int l = 0; l < DataJArr.length(); l++) {
                JSONObject jSONObject = DataJArr.getJSONObject(l);
                JSONArray fields = getFieldParams(request, jSONObject.getString("journalentryid"), "2");
                for (int k = 0; k < fields.length(); k++) {
                    JSONObject customfield = fields.getJSONObject(k);
                    String fieldlabel = customfield.getString("fieldlabel");
                    if (fieldlabel.equals("TT Number")) {
                        if (customfield.has("fieldData")) {
                            String chkttno = customfield.getString("fieldData");
                            if (chkttno.equals(ttno)) {
                                return jSONObject;
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
     
   public ModelAndView exportRCNReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr=new JSONArray();
        String view = "jsonView_ex";
        ModelAndView mav = null;
        try{
                jobj = getRCNReportData(request);
                exportDaoObj.processRequest(request, response, jobj);
                mav = new ModelAndView(view, "model", jobj.toString());

        } catch (SessionExpiredException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mav;
    }
    public JSONArray getCustomCombodata(HttpServletRequest request) {
        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        JSONArray Jarry = new JSONArray();
        String fieldid = request.getParameter(FieldConstants.Crm_fieldid);
        String flag = request.getParameter(FieldConstants.Crm_flag);
        String parentid = "";
        if (!StringUtil.isNullOrEmpty(request.getParameter("parentid"))) {
            parentid = request.getParameter("parentid");
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            Integer colcount = 1;
            requestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_fieldid, FieldConstants.Crm_deleteflag));
            requestParams.put(Constants.filter_values, Arrays.asList(fieldid, 0));
            if (!StringUtil.isNullOrEmpty(request.getParameter("parentid"))) {
                requestParams.put("parentid", parentid);
            }
            ArrayList order_by = new ArrayList();
            ArrayList order_type = new ArrayList();
            order_by.add("itemsequence");
            order_type.add("asc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);
            result = accAccountDAOobj.getCustomCombodata(requestParams);
            List lst = result.getEntityList();
            colcount = lst.size();
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                FieldComboData tmpcontyp = (FieldComboData) row[0];
                JSONObject jobjTemp = new JSONObject();
                jobjTemp.put(FieldConstants.Crm_id, tmpcontyp.getId());
                jobjTemp.put(FieldConstants.Crm_name, tmpcontyp.getValue());
                FieldComboData parentItem = (FieldComboData) row[3];
                if (parentItem != null) {
                    jobjTemp.put("parentid", parentItem.getId());
                    jobjTemp.put("parentname", parentItem.getValue());
                }
                jobjTemp.put("level", row[1]);
                jobjTemp.put("leaf", row[2]);
                Jarry.put(jobjTemp);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Jarry;
    }
                         
    public ModelAndView getCustomColumnSummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray detailObjArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            detailObjArr = getCustomColumnSummaryReport(request);
            int count = detailObjArr.length();
            JSONArray pagedJson = detailObjArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getCustomColumnSummaryReport(HttpServletRequest request) {

        JSONArray detailObjArr = new JSONArray();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        boolean isSummaryReport = true;
        try {

            DataJArr = getCustomCombodata(request); //for geting Custom Combo Data
            String Searchjson = "{}";
            if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.Acc_Search_Json))) {
                Searchjson = request.getParameter(Constants.Acc_Search_Json);
            }
            JSONObject jobjSearch = new JSONObject(Searchjson);
            int count = jobjSearch.getJSONArray(Constants.root).length();


            for (int i = 0; i < DataJArr.length() && count > 0; i++) {
                JSONObject obj = new JSONObject();
                JSONObject jobj1 = jobjSearch.getJSONArray(Constants.root).getJSONObject(0);
                JSONObject jobjTemp = DataJArr.getJSONObject(i);

                jobj1.put("combosearch", jobjTemp.get("name"));
                jobj1.put("searchText", jobjTemp.get("id"));
                jobj1.put("search", jobjTemp.get("id"));
                JSONArray jArray = new JSONArray();
                JSONObject jSONObject = new JSONObject();
                jArray.put(jobj1);
                jSONObject.put("root", jArray);
                resultList = getCustomColumnDetailReport(request, isSummaryReport, jSONObject);
                //DataJArr=resultList.isEmpty()?null:(JSONArray)resultList.get(0);
//                   jobj.put("data", DataJArr); 
                obj.put("header", jobjTemp.get("name"));
                obj.put("id", jobjTemp.get("id"));
                obj.put("searchText", jobjTemp.get("id"));

                obj.put("amount", resultList.isEmpty() ? 0 : (Double) resultList.get(1));
                detailObjArr.put(obj);

            }
        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return detailObjArr;
    }

    public ModelAndView ExportCustomColumnSummaryReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray detailObjArr = new JSONArray();
        JSONArray customComboArr = new JSONArray();
        String view = "jsonView_ex";
        boolean issuccess = false;
        String msg = "";
        try {

            detailObjArr = getCustomColumnSummaryReport(request);
            jobj.put("data", detailObjArr);
            jobj.put("count", detailObjArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
            
    public ModelAndView getCustomColumnDetailReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        boolean isSummaryReport = false;
        boolean issuccess = false;
        String msg = "";
        try {
            resultList = getCustomColumnDetailReport(request, isSummaryReport, jobj1);
            DataJArr = (JSONArray) resultList.get(0);
            
            int count = DataJArr.length();
            JSONArray pagedJson = DataJArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView ExportCustomColumnDetailReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        boolean isSummaryReport = false;
        String view = "jsonView";
        String msg = "";
        try {
            resultList = getCustomColumnDetailReport(request, isSummaryReport, jobj1);
            DataJArr = (JSONArray) resultList.get(0);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);

        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {

                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public List getCustomColumnDetailReport(HttpServletRequest request, boolean isSummaryReport, JSONObject SerachObject) {

        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        try {
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);

            boolean isForTemplate = false;
            boolean onlyOutstanding = false;
            DateFormat df = authHandler.getDateOnlyFormat(request);
            int totalCount = 0;
            boolean eliminateflag = consolidateFlag;
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            double totalAmount = 0.0;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                request.setAttribute("onlyOutstanding", onlyOutstanding);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("consolidateFlag", consolidateFlag);
                requestParams.put("isForTemplate", isForTemplate);
                requestParams.put("datefilter", request.getParameter("datefilter"));
                requestParams.put("custVendorID", request.getParameter("custVendorID"));
                double APAmount = 0.0, ARAmount = 0.0, GLAmount = 0.0;

                if (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) {
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), request.getParameter("billid"));
                    Invoice inv = (Invoice) result.getEntityList().get(0);
                    requestParams.put("isFixedAsset", inv.isFixedAssetInvoice());
                }
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
                KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

                requestParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                String Searchjson = "";
                boolean retainModuleId=true;
                if (!isSummaryReport && (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null)) {
                    Searchjson = requestParams.get("searchJson").toString();
                    Searchjson = accJournalEntryobj.advanceserachJsornEdit(Searchjson, companyid, (Integer) requestParams.get(Constants.moduleid),retainModuleId);
                    requestParams.put(Constants.Acc_Search_Json, Searchjson);
                } else if (isSummaryReport) {
                    Searchjson = SerachObject.toString();
                    Searchjson = accJournalEntryobj.advanceserachJsornEdit(Searchjson, companyid, (Integer) requestParams.get(Constants.moduleid),retainModuleId);
                    requestParams.put(Constants.Acc_Search_Json, Searchjson);
                }
                boolean searchFlag = true;
                if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null && requestParams.get("searchJson") != "") {
                    searchFlag = false;
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray array=serachJobj.has(Constants.root) ? serachJobj.getJSONArray(Constants.root) : new JSONArray();
                    JSONArray array1=new JSONArray();
                    for(int i=0 ; i<array.length() ; i++){
                        JSONObject Jobj = array.get(i)!=null ? new JSONObject(array.get(i).toString()) : new JSONObject();
                        if(Jobj.has("moduleid") && Constants.Acc_Vendor_Invoice_ModuleId==Integer.parseInt(Jobj.getString("moduleid"))){
                            searchFlag = true;
                            array1.put(Jobj);
//                            break;
                        }
                    }
                    if (searchFlag) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("root", array1);
                        requestParams.put(Constants.Acc_Search_Json, jSONObject.toString());
                    }
                }
                KwlReturnObject result=null;
                List list = null;
                Iterator itr = null;
                if(searchFlag){
                    result = accGoodsReceiptDAOObj.getGoodsReceiptsMerged(requestParams);
                    list = result.getEntityList();
                    itr = list.iterator();
                    List ll = null;
                    boolean belongsTo1099 = false;
                    HashMap<String, Object> requestParam = AccountingManager.getGlobalParams(request);
                    while (itr.hasNext()) {

                        Object[] oj = (Object[]) itr.next();
                        String invid = oj[0].toString();
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                        GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);

                        JournalEntry je = null;
                        JournalEntryDetail d = null;
                        if (gReceipt.isNormalInvoice()) {
                            je = gReceipt.getJournalEntry();
                            d = gReceipt.getVendorEntry();
                        }

                        double invoiceOriginalAmt = 0d;
                        double externalCurrencyRate = 0d;
                        boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();
                        Date creationDate = null;

                        String currencyid = (currency!= null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
                        Account account = null;
                        creationDate = gReceipt.getCreationDate();
                        if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                            KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), gReceipt.getVendor().getAccount().getID());
                            account = (Account) accObjItr.getEntityList().get(0);
                            externalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
                            invoiceOriginalAmt = gReceipt.getOriginalOpeningBalanceAmount();
                        } else {
                            account = d.getAccount();
                            externalCurrencyRate = je.getExternalCurrencyRate();
//                            creationDate = je.getEntryDate();
                            invoiceOriginalAmt = d.getAmount();
                        }
                        double amountdue = 0, deductDiscount = 0;
                        if (gReceipt.isIsExpenseType()) {
                            ll = accGoodsReceiptCommon.getExpGRAmountDue(requestParam, gReceipt);
                            amountdue = (Double) ll.get(1);
                            belongsTo1099 = (Boolean) ll.get(3);
                        } else {
                            if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                                ll = new ArrayList();
                                ll.add(gReceipt.getOriginalOpeningBalanceAmount());
                                ll.add(gReceipt.getOpeningBalanceAmountDue());
                                ll.add("");
                                ll.add(false);
                                ll.add(0.0);
                            } else {
                                if (Constants.InvoiceAmountDueFlag) {
                                    ll = accGoodsReceiptCommon.getInvoiceDiscountAmountInfo(requestParam, gReceipt);
                                } else {
                                    ll = accGoodsReceiptCommon.getGRAmountDue(requestParam, gReceipt);
                                }
                            }

                            amountdue = (Double) ll.get(1);
                            belongsTo1099 = (Boolean) ll.get(3);
                            deductDiscount = (Double) ll.get(4);
                        }

                        com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                        obj.put(GoodsReceiptCMNConstants.BILLID, gReceipt.getID());
                        obj.put(GoodsReceiptCMNConstants.BILLNO, gReceipt.getGoodsReceiptNumber());
                        obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                        obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (currency!= null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                        obj.put("currencyCode", (currency!= null ? currency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                        obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (currency!= null ? currency.getName() : gReceipt.getCurrency().getName()));                    
                        obj.put(GoodsReceiptCMNConstants.ENTRYNO, je != null ? je.getEntryNumber() : "");
                        obj.put(GoodsReceiptCMNConstants.DATE, df.format(creationDate));
                        obj.put(GoodsReceiptCMNConstants.MEMO, gReceipt.getMemo());
                        obj.put("group", "Accounts Payable (AP)");
                        obj.put(GoodsReceiptCMNConstants.DELETED, gReceipt.isDeleted());
                        obj.put("description", gReceipt.getMemo());
                        obj.put("customer_vendor",(gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getName()));
                        //for getting total invoice amount [PS]
                        KwlReturnObject bAmt = null;
                        if (gReceipt.isIsExpenseType()) {
                            obj.put(GoodsReceiptCMNConstants.AMOUNT, invoiceOriginalAmt);//for expense invoice                        
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParam, invoiceOriginalAmt, currencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParam, invoiceOriginalAmt, currencyid, creationDate, je.getExternalCurrencyRate());
                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, bAmt.getEntityList().get(0));
                        } else {
                            obj.put(GoodsReceiptCMNConstants.AMOUNT, invoiceOriginalAmt); //actual invoice amount
                            if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParam, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParam, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                            }
                            double amountinbase = (Double) bAmt.getEntityList().get(0);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round(amountinbase, companyid));
    //                            totalAmount=totalAmount+authHandler.round(amountinbase, 2);
                            APAmount = APAmount + authHandler.round(amountinbase, companyid);
                        }
                        DataJArr.put(obj);

                    }
                }

                //Aged Receivable 
                requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
                requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                requestParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                Searchjson = "";

                if (!isSummaryReport && (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null)) {
                    Searchjson = requestParams.get("searchJson").toString();
                    Searchjson = accJournalEntryobj.advanceserachJsornEdit(Searchjson, companyid, (Integer) requestParams.get(Constants.moduleid),retainModuleId);
                    requestParams.put(Constants.Acc_Search_Json, Searchjson);
                } else if (isSummaryReport) {
                    Searchjson = SerachObject.toString();
                    Searchjson = accJournalEntryobj.advanceserachJsornEdit(Searchjson, companyid, (Integer) requestParams.get(Constants.moduleid),retainModuleId);
                    requestParams.put(Constants.Acc_Search_Json, Searchjson);
                }
                searchFlag = true;
                if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null && requestParams.get("searchJson") != "") {
                    searchFlag = false;
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray array=serachJobj.has(Constants.root) ? serachJobj.getJSONArray(Constants.root) : new JSONArray();
                    JSONArray array1=new JSONArray();
                    for(int i=0 ; i<array.length() ; i++){
                        JSONObject Jobj = array.get(i)!=null ? new JSONObject(array.get(i).toString()) : new JSONObject();
                        if(Jobj.has("moduleid") && Constants.Acc_Invoice_ModuleId==Integer.parseInt(Jobj.getString("moduleid"))){
                            searchFlag = true;
                            array1.put(Jobj);
//                            break;
                        }
                    }
                       if (searchFlag) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("root", array1);
                        requestParams.put(Constants.Acc_Search_Json, jSONObject.toString());
                    }
                }
                if(searchFlag){
                    result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                    totalCount = result != null ? result.getRecordTotalCount() : 0;
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {

                        //Invoice invoice = (Invoice) itr.next();

                        Object[] oj = (Object[]) itr.next();
                        String invid = oj[0].toString();
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                        Invoice invoice = (Invoice) objItr.getEntityList().get(0);

                        Date invoiceCreationDate = null;
                        Double externalCurrencyRate = 0d;
                        boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                        Double invoiceOriginalAmount = 0d;
                        if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                            ExchangeRateDetails erd = invoice.getExchangeRateDetail();
                            externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                            invoiceOriginalAmount = invoice.getOriginalOpeningBalanceAmount();
                        }

                        JournalEntry je = null;
                        if (invoice.isNormalInvoice()) {
                            je = invoice.getJournalEntry();
//                            invoiceCreationDate = je.getEntryDate();
                            externalCurrencyRate = je.getExternalCurrencyRate();
//                        } else if (invoice.isIsOpeningBalenceInvoice()) {
                        }
                        invoiceCreationDate = invoice.getCreationDate();

                        JournalEntryDetail d = null;
                        if (invoice.isNormalInvoice()) {
                            d = invoice.getCustomerEntry();
                            invoiceOriginalAmount = d.getAmount();
                        }

    //                    Account account = null;
    //                    if(invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()){
    //                        account = invoice.getCustomer().getAccount();
    ////                        KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), invoice.getCustomer().getID());
    ////                        account = (Account) accObjItr.getEntityList().get(0);
    //                    }else{
    //                        account = d.getAccount();
    //                    }

                        String currencyid = (currency!= null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
                        JSONObject obj = new JSONObject();
                        obj.put("billid", invoice.getID());
                        obj.put("billno", invoice.getInvoiceNumber());
                        obj.put("group", "Accounts Receivable (AR)");
                        obj.put("currencyid", currencyid);
                        obj.put("currencysymbol", (currency!= null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                        obj.put("currencycode", (currency!= null ? currency.getCurrencyCode() : invoice.getCurrency().getCurrencyCode()));
                        obj.put("currencyname", (currency!= null ? currency.getName() : invoice.getCurrency().getName()));
                        obj.put("description", invoice.getMemo());
                        obj.put("customer_vendor",(invoice.getCustomer() == null ? "" : invoice.getCustomer().getName()));
                        //                obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,je.getEntryDate()));
                        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, invoiceCreationDate, 0);            
                        obj.put("entryno", (je != null ? je.getEntryNumber() : ""));
                        obj.put("date", df.format(invoiceCreationDate));
                        obj.put("amount", invoiceOriginalAmount);   //actual invoice amount
                        if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, invoiceOriginalAmount, currencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceOriginalAmount, currencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                        double amountinbase = (Double) bAmt.getEntityList().get(0);
                        obj.put("amountinbase", authHandler.round(amountinbase, companyid));
                        ARAmount = ARAmount + authHandler.round(amountinbase, companyid);
    //                    obj.put("amountinbase",invoice.getOpeningBalanceInvoiceCustomData().getCol(cnt));
                        DataJArr.put(obj);
                    }
                }



                //Journal Entry
                requestParams = accReportsController.getJournalEntryMap(request);
                requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                requestParams.put(Constants.Journal_Entry_Type, Constants.Normal_Journal_Entry);
                requestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                Searchjson = "";
                if (!isSummaryReport && (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null)) {
                    Searchjson = requestParams.get("searchJson").toString();
                    Searchjson = accJournalEntryobj.advanceserachJsornEdit(Searchjson, companyid, (Integer) requestParams.get(Constants.moduleid),retainModuleId);
                    requestParams.put(Constants.Acc_Search_Json, Searchjson);
                } else if (isSummaryReport) {
                    Searchjson = SerachObject.toString();
                    Searchjson = accJournalEntryobj.advanceserachJsornEdit(Searchjson, companyid, (Integer) requestParams.get(Constants.moduleid),retainModuleId);
                    requestParams.put(Constants.Acc_Search_Json, Searchjson);
                }
                requestParams.put("pendingFlag", false);
                requestParams.put("start", null);
                searchFlag = true;
                if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null && requestParams.get("searchJson") != "") {
                    searchFlag = false;
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray array=serachJobj.has(Constants.root) ? serachJobj.getJSONArray(Constants.root) : new JSONArray();
                    JSONArray array1=new JSONArray();
                    for(int i=0 ; i<array.length() ; i++){
                        JSONObject Jobj = array.get(i)!=null ? new JSONObject(array.get(i).toString()) : new JSONObject();
                        if(Jobj.has("moduleid") && Constants.Acc_GENERAL_LEDGER_ModuleId==Integer.parseInt(Jobj.getString("moduleid"))){
                            searchFlag = true;
                            array1.put(Jobj);
//                            break;
                        }
                    }
                    if (searchFlag) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("root", array1);
                        requestParams.put(Constants.Acc_Search_Json, jSONObject.toString());
                    }
                }
                if(searchFlag){
                    result = accJournalEntryobj.getJournalEntry(requestParams);
                    list = result.getEntityList();
                    itr = list.iterator();
                    String creditAccname = "";
                    double creditAmount = 0.0;
                    double creditCurrencyAmount = 0.0;
                    while (itr.hasNext()) {
                        JournalEntry entry = (JournalEntry) itr.next();
                        // String jeId = entry.getID();
                        String currencyid = entry.getCurrency() == null ? currency.getCurrencyID() : entry.getCurrency().getCurrencyID();

                        creditAmount = 0.0;
                        creditCurrencyAmount = 0.0;
                        requestParams.put("debit", "credit");    //For Credit Type Jedetails from je
                        requestParams.put("jeId", entry.getID());
                        result = accJournalEntryobj.getJournalEntryDetailsForFinanceReport(requestParams);
                        List<JournalEntryDetail> creditList = result.getEntityList();
                        for (JournalEntryDetail entryDetail : creditList) {

                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, entryDetail.getAmount(), currencyid, entryDetail.getJournalEntry().getEntryDate(), entryDetail.getJournalEntry().getExternalCurrencyRate());
                            String accname = StringUtil.isNullOrEmpty(entryDetail.getAccount().getAcccode()) ? entryDetail.getAccount().getName() : "[" + entryDetail.getAccount().getAcccode() + "] " + entryDetail.getAccount().getName();
                            creditAmount = creditAmount + (Double) bAmt.getEntityList().get(0);
                            creditCurrencyAmount = creditCurrencyAmount + entryDetail.getAmount();
                            if (creditAccname.equals("")) {
                                creditAccname = accname;
                            } else {
                                creditAccname = creditAccname + "," + accname;
                            }

                        }

                        JSONObject obj = new JSONObject();
                        obj.put("date", df.format(entry.getEntryDate()));
                        obj.put("entryno", entry.getEntryNumber());
                        obj.put("currencyid", currency!= null ? currency.getCurrencyID() : entry.getCurrency().getCurrencyID());
                        obj.put("currencysymbol", currency!= null ? currency.getSymbol() : entry.getCurrency().getSymbol());
                        obj.put("currencyname", currency!= null ? currency.getName() : entry.getCurrency().getName());
                        obj.put("amount", creditCurrencyAmount);
                        obj.put("amountinbase", authHandler.round(creditAmount, companyid));
                        obj.put("billno", "");
                        obj.put("group", "GL");
                        obj.put("journalentryid", entry.getID());
                        obj.put("description", entry.getMemo());
                        obj.put("customer_vendor","");
                        GLAmount = GLAmount + authHandler.round(creditAmount, companyid);

                        DataJArr.put(obj);
                    }
                }
                totalAmount = ARAmount - (APAmount + GLAmount);  //Formula AR -(AP+GL)
                JSONObject obj = new JSONObject();
                obj.put("date", "");//df.format(new Date())        
                obj.put("entryno", "Total Amount");
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("currencyname", currency.getName());
                obj.put("amount", authHandler.round(totalAmount, companyid));
                obj.put("amountinbase", authHandler.round(totalAmount, companyid));
                obj.put("billno", "");
                obj.put("group", "Total(AR-(AP+GL))");


                DataJArr.put(obj);
                JSONArray pagedJson = DataJArr;
                if (consolidateFlag) {
                    String start = request.getParameter("start");
                    String limit = request.getParameter("limit");
                    if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                        try {
                            pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                        } catch (NumberFormatException e) {
                            pagedJson = StringUtil.getPagedJSON(pagedJson, 0, 15);//ERP-32676 - Number Format Exception
                            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, e);
                        }                        
                    }
                }

            }
            resultList.add(DataJArr);
            resultList.add(authHandler.round(totalAmount, companyid));//Used in summary report to show project / country wise amount.
            totalAmount = 0.0;
        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resultList;

    }
    /**
     *
     * @param request
     * @param response
     * @return
     * @Description : Export Custom Column Line Detail Report
     */
    public ModelAndView ExportCustomColumnDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        boolean isSummaryReport = false;
        String view = "jsonView_ex";
        String msg = "";
        try {
            jobj1 = getCustomColumnDetailsJson(request, true);
            DataJArr = (JSONArray) jobj1.optJSONArray("data");
            request.setAttribute("isExport", true);
            String type = request.getParameter("type") == null ? " " : request.getParameter("type");
            if (type.equals("detailedXls")) {
                DataJArr = formatDataWithLineDetails(DataJArr);
            }
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     *
     * @param request
     * @param response
     * @return
     * @Description : Report Custom Column Line Detail
     */
    public ModelAndView getCustomColumnDetails(HttpServletRequest request, HttpServletResponse response) {

        boolean issuccess = false;
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        List ckeckList = new ArrayList();
        List checkTotal = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            jobj = getCustomColumnDetailsJson(request, false);
        } catch (Exception ex) {

            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    /**
     *
     * @param request
     * @param export
     * @return
     * @throws AccountingException
     * @throws SessionExpiredException
     * @throws JSONException
     * @Description : Return Array for Custom Column Line Detail report
     */

    public JSONObject getCustomColumnDetailsJson(HttpServletRequest request, boolean export) throws AccountingException, SessionExpiredException, JSONException {
        JSONObject jobj1 = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jMeta = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        JSONObject commData = new JSONObject();
        JSONArray pagedJson = new JSONArray();
        JSONObject jResObj = new JSONObject();
        Locale locale = RequestContextUtils.getLocale(request);
        try {
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean isForTemplate = false;
            boolean onlyOutstanding = false;
            DateFormat df = authHandler.getDateOnlyFormat(request);
            int totalCount = 0;
            boolean eliminateflag = consolidateFlag;
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            requestParams.put("dateformat", authHandler.getDateOnlyFormat(request));
            double totalAmount = 0.0;
            String companyid = "";
            String browsertz = sessionHandlerImpl.getBrowserTZ(request);
            /*
             Configuration check
             */
            boolean includingTax = false;
            boolean includingDiscount = false;
            boolean lineLevelAmount = false;
            if ((requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null)) {
                String serachJson = requestParams.get("searchJson").toString();
                if (!StringUtil.isNullOrEmpty(serachJson)) {
                    serachJson = StringUtil.DecodeText(serachJson);
                    JSONObject jobjSearch = new JSONObject(serachJson);
                    int count = jobjSearch.getJSONArray(Constants.root).length();
                    for (int i = 0; i < count; i++) {
                        JSONObject searchob = jobjSearch.getJSONArray(Constants.root).getJSONObject(i);
                        int levelSearch = Integer.parseInt(searchob.optString("transactionSearch", "1"));
                        includingTax = Boolean.parseBoolean(searchob.optString("includingTax", "false"));
                        includingDiscount = Boolean.parseBoolean(searchob.optString("includingDiscount", "false"));
                        if (levelSearch == 2) {
                            lineLevelAmount = true;
                        }
                        break;
                    }
                }
            }
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                request.setAttribute("onlyOutstanding", onlyOutstanding);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("consolidateFlag", consolidateFlag);
                requestParams.put("isForTemplate", isForTemplate);
                requestParams.put("datefilter", request.getParameter("datefilter"));
                requestParams.put("custVendorID", request.getParameter("custVendorID"));
                double APAmount = 0.0, ARAmount = 0.0, GLAmount = 0.0;

                if (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) {
                    KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), request.getParameter("billid"));
                    Invoice inv = (Invoice) result.getEntityList().get(0);
                    requestParams.put("isFixedAsset", inv.isFixedAssetInvoice());
                }
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), AccountingManager.getGlobalCurrencyidFromRequest(request));
                KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
                requestParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                String Searchjson = "";
                if ((requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null)) {
                    Searchjson = requestParams.get("searchJson").toString();
                    boolean retainModuleId = true;
                    Searchjson = accJournalEntryobj.advanceserachJsornEdit(Searchjson, companyid, (Integer) requestParams.get(Constants.moduleid), retainModuleId);
                    requestParams.put(Constants.Acc_Search_Json, Searchjson);
                }
                boolean searchFlag = false;
                if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null && requestParams.get("searchJson") != "") {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray array = serachJobj.has(Constants.root) ? serachJobj.getJSONArray(Constants.root) : new JSONArray();
                    JSONArray array1 = new JSONArray();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject Jobj = array.get(i) != null ? new JSONObject(array.get(i).toString()) : new JSONObject();
                        if (Jobj.has("moduleid") && Constants.Acc_Vendor_Invoice_ModuleId == Integer.parseInt(Jobj.getString("moduleid"))) {
                            searchFlag = true;
                            array1.put(Jobj);
                        }
                    }
                    if (searchFlag) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("root", array1);
                        requestParams.put(Constants.Acc_Search_Json, jSONObject.toString());
                    }
                }
                HashMap lineMap = new HashMap(requestParams);
                KwlReturnObject result = null;
                List list = null;
                Iterator itr = null;
                if (searchFlag) {
                    result = accGoodsReceiptDAOObj.getGoodsReceiptsMerged(requestParams);
                    list = result.getEntityList();
                    itr = list.iterator();
                    List ll = null;
                    boolean belongsTo1099 = false;
                    HashMap<String, Object> requestParam = AccountingManager.getGlobalParams(request);
                    while (itr.hasNext()) {
                        Object[] oj = (Object[]) itr.next();
                        String invid = oj[0].toString();
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                        GoodsReceipt gReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                        JournalEntry je = null;
                        JournalEntryDetail d = null;
                        if (gReceipt.isNormalInvoice()) {
                            je = gReceipt.getJournalEntry();
                            d = gReceipt.getVendorEntry();
                        }
                        double invoiceOriginalAmt = 0d;
                        double externalCurrencyRate = 0d;
                        double taxAmount = 0;
                        double discountAmount = 0;
                        double rowAmount = 0;
                        double amountWithoutTax = 0;
                        boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();
                        Date creationDate = null;
                        String currencyid = (currency != null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
                        Account account = null;
                        creationDate = gReceipt.getCreationDate();
                        if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                            KwlReturnObject accObjItr = accountingHandlerDAOobj.getObject(Account.class.getName(), gReceipt.getVendor().getAccount().getID());
                            account = (Account) accObjItr.getEntityList().get(0);
                            externalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
                            invoiceOriginalAmt = gReceipt.getOriginalOpeningBalanceAmount();
                        } else {
                            account = d.getAccount();
                            externalCurrencyRate = je.getExternalCurrencyRate();
//                            creationDate = je.getEntryDate();
                            invoiceOriginalAmt = d.getAmount();
                        }
                        /*
                         get invoice details 
                         */
                        Map map = new HashMap();
                        map.put("companyId", companyid);
                        map.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                        lineMap.put("Id", gReceipt.getID());
                        lineMap.put("lineLevelAmount", lineLevelAmount);
                        com.krawler.utils.json.base.JSONObject obj = new com.krawler.utils.json.base.JSONObject();
                        JSONArray details = new JSONArray();
                        boolean globalTax = false;
                        if (!gReceipt.isIsExpenseType()) {
                            KwlReturnObject grdresult = accGoodsReceiptDAOObj.getGoodsReceiptDetailsUsingAdvanceSearch(lineMap);
                            List<GoodsReceiptDetail> goodsReceiptDetailList = grdresult.getEntityList();
                            if (goodsReceiptDetailList != null && !goodsReceiptDetailList.isEmpty()) {
                                for (GoodsReceiptDetail goodsReceiptDetail : goodsReceiptDetailList) {
                                    map.put("grDetails", goodsReceiptDetail);
                                    map.put("browsertz", browsertz);
                                    obj = new JSONObject();
                                    accReportsService.putLineCustomDetails(obj, map);
                                    obj.put("productId", goodsReceiptDetail.getInventory().getProduct().getProductid());
                                    obj.put("productName", goodsReceiptDetail.getInventory().getProduct().getProductName());
                                    obj.put("description", goodsReceiptDetail.getDescription());
                                    obj.put("quantity", goodsReceiptDetail.getInventory().getActquantity());
                                    obj.put("rate", goodsReceiptDetail.getRate());
                                    obj.put("unitname", goodsReceiptDetail.getInventory().getUom() != null ? goodsReceiptDetail.getInventory().getUom().getNameEmptyforNA() : "");

                                    /*
                                     Calculate Amount 
                                     */
                                    double quantity = goodsReceiptDetail.getInventory().getQuantity();
                                    rowAmount = rowAmount + (goodsReceiptDetail.getRate() * quantity);
                                    double tempTotal = (goodsReceiptDetail.getRate() * quantity);
                                    if (goodsReceiptDetail.getDiscount() != null) {
                                        if (goodsReceiptDetail.getDiscount().isInPercent()) {
                                            discountAmount = discountAmount + (rowAmount * goodsReceiptDetail.getDiscount().getDiscount() / 100);
                                            tempTotal = tempTotal - (rowAmount * goodsReceiptDetail.getDiscount().getDiscount() / 100);
                                            obj.put("discountamt", (rowAmount * goodsReceiptDetail.getDiscount().getDiscount() / 100));
                                        } else {
                                            discountAmount = discountAmount + goodsReceiptDetail.getDiscount().getDiscount();
                                            tempTotal = tempTotal - goodsReceiptDetail.getDiscount().getDiscount();
                                            obj.put("discountamt", goodsReceiptDetail.getDiscount().getDiscount());
                                        }
                                    }
                                    taxAmount = taxAmount + goodsReceiptDetail.getRowTaxAmount() + goodsReceiptDetail.getRowTermTaxAmount();
                                    tempTotal = tempTotal + goodsReceiptDetail.getRowTaxAmount() + goodsReceiptDetail.getRowTermTaxAmount();
                                    obj.put("taxamt", (goodsReceiptDetail.getRowTaxAmount()+goodsReceiptDetail.getRowTermTaxAmount()));
                                    obj.put("amount", tempTotal);
                                    obj.put("detailType", "ProductDetails");
                                    details.put(obj);
                                }
                            }
                            amountWithoutTax = rowAmount - discountAmount;
                            double taxPercent = 0;
                            if (gReceipt.getTax() != null) {
                                globalTax = true;
//                                KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), gReceipt.getJournalEntry().getEntryDate(), gReceipt.getTax().getID());
                                KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), gReceipt.getCreationDate(), gReceipt.getTax().getID());
                                taxPercent = (Double) taxresult.getEntityList().get(0);
                                taxAmount = (taxPercent == 0 ? 0 : authHandler.round((amountWithoutTax * taxPercent / 100), companyid));
                            }
                        } else {
                            lineMap.put("isExpense", true);
                            KwlReturnObject grdresult = accGoodsReceiptDAOObj.getGoodsReceiptDetailsUsingAdvanceSearch(lineMap);
                            lineMap.remove("isExpense");
                            List<ExpenseGRDetail> goodsReceiptDetailList = grdresult.getEntityList();
                            if (goodsReceiptDetailList != null && !goodsReceiptDetailList.isEmpty()) {
                                for (ExpenseGRDetail goodsReceiptDetail : goodsReceiptDetailList) {
                                    map.put("expenseDetails", goodsReceiptDetail);
                                    map.put("browsertz", browsertz);
                                    obj = new JSONObject();
                                    accReportsService.putLineCustomDetails(obj, map);
                                    obj.put("accountname", goodsReceiptDetail.getAccount().getName());
                                    obj.put("description", goodsReceiptDetail.getDescription());
                                    obj.put("amount", goodsReceiptDetail.getRate());
                                    /*
                                     Calculate Amount 
                                     */
                                    rowAmount = rowAmount + goodsReceiptDetail.getRate();
                                    double tempTotal = goodsReceiptDetail.getRate();
                                    if (goodsReceiptDetail.getDiscount() != null) {
                                        if (goodsReceiptDetail.getDiscount().isInPercent()) {
                                            discountAmount = discountAmount + (rowAmount * goodsReceiptDetail.getDiscount().getDiscount() / 100);
                                            tempTotal = tempTotal - (rowAmount * goodsReceiptDetail.getDiscount().getDiscount() / 100);
                                            obj.put("discountamt", (rowAmount * goodsReceiptDetail.getDiscount().getDiscount() / 100));
                                        } else {
                                            discountAmount = discountAmount + goodsReceiptDetail.getDiscount().getDiscount();
                                            tempTotal = tempTotal - goodsReceiptDetail.getDiscount().getDiscount();
                                            obj.put("discountamt", goodsReceiptDetail.getDiscount().getDiscount());
                                        }
                                    }

                                    taxAmount = taxAmount + goodsReceiptDetail.getRowTaxAmount();
                                    tempTotal = tempTotal + goodsReceiptDetail.getRowTaxAmount();
                                    obj.put("taxamt", goodsReceiptDetail.getRowTaxAmount());
                                    obj.put("totalamt", tempTotal);
                                    obj.put("detailType", "expenseDetails");
                                    details.put(obj);
                                }
                            }
                            /*
                             global Tax
                             */
                            amountWithoutTax = rowAmount - discountAmount;
                            double taxPercent = 0;
                            if (gReceipt.getTax() != null) {
                                globalTax = true;
//                                KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), gReceipt.getJournalEntry().getEntryDate(), gReceipt.getTax().getID());
                                KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), gReceipt.getCreationDate(), gReceipt.getTax().getID());
                                taxPercent = (Double) taxresult.getEntityList().get(0);
                                taxAmount = (taxPercent == 0 ? 0 : authHandler.round((amountWithoutTax * taxPercent / 100), companyid));
                            }
                        }

                        /*
                         get Term amount
                         */
                        double termAmount = CommonFunctions.getTotalTermsAmount(getTermDetails(gReceipt.getID(), accGoodsReceiptDAOObj));
                        /*
                         Change amount as per configuration
                         */

                        if (lineLevelAmount) {
                            invoiceOriginalAmt = rowAmount;
                            if (includingDiscount) {
                                invoiceOriginalAmt = invoiceOriginalAmt - discountAmount;
                            }
                            if (includingTax) {
                                invoiceOriginalAmt = invoiceOriginalAmt + taxAmount;
                            }
                        } else {
                            invoiceOriginalAmt = rowAmount;
                            if (includingDiscount) {
                                invoiceOriginalAmt = invoiceOriginalAmt - discountAmount + termAmount;
                            }
                            if (includingTax) {
                                invoiceOriginalAmt = invoiceOriginalAmt + taxAmount;
                            }
                        }

                        obj = new JSONObject();
                        obj.put("details", details);
                        obj.put(GoodsReceiptCMNConstants.BILLID, gReceipt.getID());
                        obj.put(GoodsReceiptCMNConstants.BILLNO, gReceipt.getGoodsReceiptNumber());
                        obj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyid);
                        obj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
                        obj.put("currencyCode", (gReceipt.getCurrency() == null ? currency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
                        obj.put(GoodsReceiptCMNConstants.CURRENCYNAME, (gReceipt.getCurrency() == null ? currency.getName() : gReceipt.getCurrency().getName()));
                        obj.put(GoodsReceiptCMNConstants.ENTRYNO, je != null ? je.getEntryNumber() : "");
                        obj.put(GoodsReceiptCMNConstants.DATE, df.format(creationDate));
                        obj.put(GoodsReceiptCMNConstants.MEMO, gReceipt.getMemo());
                        obj.put("group", "Accounts Payable (AP)");
                        obj.put(GoodsReceiptCMNConstants.DELETED, gReceipt.isDeleted());
                        obj.put("description", gReceipt.getMemo());
                        obj.put("customer_vendor", (gReceipt.getVendor() == null ? "" : gReceipt.getVendor().getName()));
                        //for getting total invoice amount [PS]
                        KwlReturnObject bAmt = null;
                        if (gReceipt.isIsExpenseType()) {
                            obj.put("isExpense", true);
                            obj.put(GoodsReceiptCMNConstants.AMOUNT, invoiceOriginalAmt);//for expense invoice                        
//                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParam, invoiceOriginalAmt, currencyid, je.getEntryDate(), je.getExternalCurrencyRate());
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParam, invoiceOriginalAmt, currencyid, creationDate, je.getExternalCurrencyRate());
                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, bAmt.getEntityList().get(0));
                            double amountinbase = (Double) bAmt.getEntityList().get(0);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round(amountinbase, companyid));
                            APAmount = APAmount + authHandler.round(amountinbase, companyid);
                        } else {
                            obj.put(GoodsReceiptCMNConstants.AMOUNT, invoiceOriginalAmt); //actual invoice amount
                            if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParam, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                            } else {
                                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParam, invoiceOriginalAmt, currencyid, creationDate, externalCurrencyRate);
                            }
                            double amountinbase = (Double) bAmt.getEntityList().get(0);
                            obj.put(GoodsReceiptCMNConstants.AMOUNTINBASE, authHandler.round(amountinbase, companyid));
                            //                            totalAmount=totalAmount+authHandler.round(amountinbase, 2);
                            APAmount = APAmount + authHandler.round(amountinbase, companyid);
                        }

                        /*
                         Add Global Custom data for document
                         */
                        Map globalMap = new HashMap();
                        globalMap.put("moduleId", Constants.Acc_Vendor_Invoice_ModuleId);
                        globalMap.put("companyId", companyid);
                        globalMap.put("jeId", gReceipt.getJournalEntry().getID());
                        accReportsService.putGlobalCustomDetails(obj, globalMap);

                        DataJArr.put(obj);
                    }
                }
                
                //Debit Notes
                requestParams = accDebitNoteController.gettDebitNoteMap(request);
                requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                requestParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("lineLevelAmount", lineLevelAmount);
                requestParams.put("includingTax", includingTax);
                requestParams.put("currency", currency);
                requestParams.put("APAmount", APAmount);
                requestParams.put("isCustomDetailLineReport",true);
                jResObj = accReportsService.getDNdataForCustomLineDetailReport(requestParams, DataJArr);
                if (jResObj.has("DataJArr") && !jResObj.get("DataJArr").equals("[]")) {
                    DataJArr = (JSONArray) jResObj.get("DataJArr");
                    APAmount = (Double) jResObj.get("APAmount");
                }

                //Aged Receivable 
                requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
                requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                requestParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                Searchjson = "";
                if ((requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null)) {
                    Searchjson = requestParams.get("searchJson").toString();
                    boolean retainModuleId = true;
                    Searchjson = accJournalEntryobj.advanceserachJsornEdit(Searchjson, companyid, (Integer) requestParams.get(Constants.moduleid), retainModuleId);
                    requestParams.put(Constants.Acc_Search_Json, Searchjson);
                }
                searchFlag = false;
                if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null && requestParams.get("searchJson") != "") {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray array = serachJobj.has(Constants.root) ? serachJobj.getJSONArray(Constants.root) : new JSONArray();
                    JSONArray array1 = new JSONArray();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject Jobj = array.get(i) != null ? new JSONObject(array.get(i).toString()) : new JSONObject();
                        if (Jobj.has("moduleid") && Constants.Acc_Invoice_ModuleId == Integer.parseInt(Jobj.getString("moduleid"))) {
                            searchFlag = true;
                            array1.put(Jobj);
                        }
                    }
                    if (searchFlag) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("root", array1);
                        requestParams.put(Constants.Acc_Search_Json, jSONObject.toString());
                    }
                }
                lineMap = new HashMap(requestParams);
                if (searchFlag) {
                    result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                    totalCount = result != null ? result.getRecordTotalCount() : 0;
                    list = result.getEntityList();
                    itr = list.iterator();
                    while (itr.hasNext()) {
                        Object[] oj = (Object[]) itr.next();
                        String invid = oj[0].toString();
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                        Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                        Date invoiceCreationDate = null;
                        Double externalCurrencyRate = 0d;
                        boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                        Double invoiceOriginalAmount = 0d;
                        double taxAmount = 0;
                        double discountAmount = 0;
                        double rowAmount = 0;
                        double amountWithoutTax = 0;
                        if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                            ExchangeRateDetails erd = invoice.getExchangeRateDetail();
                            externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                            invoiceOriginalAmount = invoice.getOriginalOpeningBalanceAmount();
                        }
                        JournalEntry je = null;
                        if (invoice.isNormalInvoice()) {
                            je = invoice.getJournalEntry();
//                            invoiceCreationDate = je.getEntryDate();
                            externalCurrencyRate = je.getExternalCurrencyRate();
//                        } else if (invoice.isIsOpeningBalenceInvoice()) {
                        }
                        invoiceCreationDate = invoice.getCreationDate();
                        JournalEntryDetail d = null;
                        if (invoice.isNormalInvoice()) {
                            d = invoice.getCustomerEntry();
                            invoiceOriginalAmount = d.getAmount();
                        }
                        JSONObject obj = new JSONObject();
                        /*
                         get invoice details 
                         */
                        Map map = new HashMap();
                        map.put("companyId", companyid);
                        map.put("moduleid", Constants.Acc_Invoice_ModuleId);
                        lineMap.put("Id", invoice.getID());
                        lineMap.put("lineLevelAmount", lineLevelAmount);
                        KwlReturnObject grdresult = accInvoiceDAOobj.getInvoiceDetailsUsingAdvanceSearch(lineMap);
                        List<InvoiceDetail> invoiceDetailList = grdresult.getEntityList();
                        JSONArray details = new JSONArray();
                        if (invoiceDetailList != null && !invoiceDetailList.isEmpty()) {
                            for (InvoiceDetail invoiceDetail : invoiceDetailList) {
                                map.put("InvoiceDetail", invoiceDetail);
                                map.put("browsertz", browsertz);
                                obj = new JSONObject();
                                accReportsService.putLineCustomDetails(obj, map);
                                obj.put("productId", invoiceDetail.getInventory().getProduct().getProductid());
                                obj.put("productName", invoiceDetail.getInventory().getProduct().getProductName());
                                obj.put("description", invoiceDetail.getDescription());
                                obj.put("quantity", invoiceDetail.getInventory().getActquantity());
                                obj.put("rate", invoiceDetail.getRate());
                                obj.put("unitname", invoiceDetail.getInventory().getUom() != null ? invoiceDetail.getInventory().getUom().getNameEmptyforNA() : "");
                                /*
                                 Calculate Amount 
                                 */
                                double quantity = invoiceDetail.getInventory().getQuantity();
                                rowAmount = rowAmount + (invoiceDetail.getRate() * quantity);
                                double tempTotal = (invoiceDetail.getRate() * quantity);
                                if (invoiceDetail.getDiscount() != null) {
                                    if (invoiceDetail.getDiscount().isInPercent()) {
                                        discountAmount = discountAmount + (rowAmount * invoiceDetail.getDiscount().getDiscount() / 100);
                                        tempTotal = tempTotal - (rowAmount * invoiceDetail.getDiscount().getDiscount() / 100);
                                        obj.put("discountamt", (rowAmount * invoiceDetail.getDiscount().getDiscount() / 100));
                                    } else {
                                        discountAmount = discountAmount + invoiceDetail.getDiscount().getDiscount();
                                        tempTotal = tempTotal - invoiceDetail.getDiscount().getDiscount();
                                        obj.put("discountamt", invoiceDetail.getDiscount().getDiscount());
                                    }
                                }

                                taxAmount = taxAmount + invoiceDetail.getRowTaxAmount() + invoiceDetail.getRowTermTaxAmount();
                                tempTotal = tempTotal + invoiceDetail.getRowTaxAmount() + invoiceDetail.getRowTermTaxAmount();
                                obj.put("taxamt", (invoiceDetail.getRowTaxAmount()+invoiceDetail.getRowTermTaxAmount()));
                                obj.put("amount", tempTotal);
                                obj.put("detailType", "ProductDetails");
                                details.put(obj);

                            }
                        }
                        /*
                         Global tax
                         */
                        amountWithoutTax = rowAmount - discountAmount;
                        double taxPercent = 0;
                        boolean globalTax = false;
                        if (invoice.getTax() != null) {
                            globalTax = true;
//                            KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                            KwlReturnObject taxresult = accTaxObj.getTaxPercent(sessionHandlerImpl.getCompanyid(request), invoice.getCreationDate(), invoice.getTax().getID());
                            taxPercent = (Double) taxresult.getEntityList().get(0);
                            taxAmount = (taxPercent == 0 ? 0 : authHandler.round((amountWithoutTax * taxPercent / 100), companyid));
                        }

                        /*
                         get Term amount
                         */
                        double termAmount = CommonFunctions.getTotalTermsAmount(accInvoiceServiceDAO.getTermDetails(invoice.getID()));
                        /*
                         Change amount as per configuration
                         */

                        if (lineLevelAmount) {
                            invoiceOriginalAmount = rowAmount;
                            if (includingDiscount) {
                                invoiceOriginalAmount = invoiceOriginalAmount - discountAmount;
                            }
                            if (includingTax) {
                                invoiceOriginalAmount = invoiceOriginalAmount + taxAmount;
                            }
                        } else {
                            invoiceOriginalAmount = rowAmount;
                            if (includingDiscount) {
                                invoiceOriginalAmount = invoiceOriginalAmount - discountAmount + termAmount;
                            }
                            if (includingTax) {
                                invoiceOriginalAmount = invoiceOriginalAmount + taxAmount;
                            }
                        }

                        obj = new JSONObject();
                        obj.put("details", details);
                        String currencyid = (currency != null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
                        obj.put("billid", invoice.getID());
                        obj.put("billno", invoice.getInvoiceNumber());
                        obj.put("group", "Accounts Receivable (AR)");
                        obj.put("currencyid", currencyid);
                        obj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                        obj.put("currencycode", (invoice.getCurrency() == null ? currency.getCurrencyCode() : invoice.getCurrency().getCurrencyCode()));
                        obj.put("currencyname", (invoice.getCurrency() == null ? currency.getName() : invoice.getCurrency().getName()));
                        obj.put("description", invoice.getMemo());
                        obj.put("customer_vendor", (invoice.getCustomer() == null ? "" : invoice.getCustomer().getName()));
                        //                obj.put("oldcurrencyrate", CompanyHandler.getBaseToCurrencyAmount(session,request,1.0,currencyid,je.getEntryDate()));
                        KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, invoiceCreationDate, 0);
                        obj.put("entryno", (je != null ? je.getEntryNumber() : ""));
                        obj.put("date", df.format(invoiceCreationDate));
                        obj.put("amount", invoiceOriginalAmount);   //actual invoice amount
                        if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, invoiceOriginalAmount, currencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoiceOriginalAmount, currencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                        double amountinbase = (Double) bAmt.getEntityList().get(0);
                        obj.put("amountinbase", authHandler.round(amountinbase, companyid));
                        ARAmount = ARAmount + authHandler.round(amountinbase, companyid);

                        /*
                         Add Global Custom data for document
                         */
                        Map globalMap = new HashMap();
                        globalMap.put("moduleId", Constants.Acc_Invoice_ModuleId);
                        globalMap.put("companyId", companyid);
                        globalMap.put("jeId", invoice.getJournalEntry().getID());
                        accReportsService.putGlobalCustomDetails(obj, globalMap);
                        DataJArr.put(obj);
                    }
                }
                
                //Credit Notes
                requestParams = accCreditNoteController.getCreditNoteMap(request);
                requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                requestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("lineLevelAmount", lineLevelAmount);
                requestParams.put("includingTax", includingTax);
                requestParams.put("currency", currency);
                requestParams.put("ARAmount", ARAmount);
                requestParams.put("isCustomDetailLineReport",true);
                jResObj = accReportsService.getCNdataForCustomLineDetailReport(requestParams, DataJArr);
                if (jResObj.has("DataJArr") && !jResObj.get("DataJArr").equals("[]")) {
                    DataJArr = (JSONArray) jResObj.get("DataJArr");
                    ARAmount = (Double) jResObj.get("ARAmount");
                }

                //Journal Entry
                requestParams = accReportsController.getJournalEntryMap(request);
                requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                requestParams.put(Constants.Journal_Entry_Type, Constants.Normal_Journal_Entry);
                requestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                Searchjson = "";
                if ((requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null)) {
                    Searchjson = requestParams.get("searchJson").toString();
                    boolean retainModuleId = true;
                    Searchjson = accJournalEntryobj.advanceserachJsornEdit(Searchjson, companyid, (Integer) requestParams.get(Constants.moduleid), retainModuleId);
                    requestParams.put(Constants.Acc_Search_Json, Searchjson);
                }
                requestParams.put("pendingFlag", false);
                requestParams.put("start", null);
                searchFlag = false;
                if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null && requestParams.get("searchJson") != "") {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray array = serachJobj.has(Constants.root) ? serachJobj.getJSONArray(Constants.root) : new JSONArray();
                    JSONArray array1 = new JSONArray();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject Jobj = array.get(i) != null ? new JSONObject(array.get(i).toString()) : new JSONObject();
                        if (Jobj.has("moduleid") && Constants.Acc_GENERAL_LEDGER_ModuleId == Integer.parseInt(Jobj.getString("moduleid"))) {
                            searchFlag = true;
                            array1.put(Jobj);
                        }
                    }
                    if (searchFlag) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("root", array1);
                        requestParams.put(Constants.Acc_Search_Json, jSONObject.toString());
                    }
                }
                lineMap = new HashMap(requestParams);
                if (searchFlag) {
                    result = accJournalEntryobj.getJournalEntry(requestParams);
                    list = result.getEntityList();
                    itr = list.iterator();
                    String creditAccname = "";
                    double creditAmount = 0.0;
                    double creditCurrencyAmount = 0.0;
                    while (itr.hasNext()) {
                        JournalEntry entry = (JournalEntry) itr.next();
                        String currencyid = entry.getCurrency() == null ? currency.getCurrencyID() : entry.getCurrency().getCurrencyID();
                        Map map = new HashMap();
                        map.put("companyId", companyid);
                        map.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        creditAmount = 0.0;
                        creditCurrencyAmount = 0.0;
                        lineMap.put("debit", "credit");    //For Credit Type Jedetails from je
                        lineMap.put("jeId", entry.getID());
                        lineMap.put("lineLevelAmount", lineLevelAmount);
                        JSONObject obj = new JSONObject();
                        result = accJournalEntryobj.getJournalEntryDetailsForFinanceReport(lineMap);
                        JSONArray details = new JSONArray();
                        List<JournalEntryDetail> creditList = result.getEntityList();
                        for (JournalEntryDetail entryDetail : creditList) {
                            map.put("jeDetails", entryDetail);
                            map.put("browsertz", browsertz);
                            obj = new JSONObject();
                            accReportsService.putLineCustomDetails(obj, map);
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, entryDetail.getAmount(), currencyid, entryDetail.getJournalEntry().getEntryDate(), entryDetail.getJournalEntry().getExternalCurrencyRate());
                            String accname = StringUtil.isNullOrEmpty(entryDetail.getAccount().getAcccode()) ? entryDetail.getAccount().getName() : "[" + entryDetail.getAccount().getAcccode() + "] " + entryDetail.getAccount().getName();
                            creditAmount = creditAmount + (Double) bAmt.getEntityList().get(0);
                            creditCurrencyAmount = creditCurrencyAmount + entryDetail.getAmount();
                            if (creditAccname.equals("")) {
                                creditAccname = accname;
                            } else {
                                creditAccname = creditAccname + "," + accname;
                            }
                            obj.put("accountname", accname);
                            obj.put("description", entryDetail.getDescription());
                            obj.put("amount", entryDetail.getAmount());
                            obj.put("detailType", "JEDetails");
                            details.put(obj);

                        }

                        obj = new JSONObject();
                        obj.put("details", details);
                        obj.put("date", df.format(entry.getEntryDate()));
                        obj.put("entryno", entry.getEntryNumber());
                        obj.put("currencyid", entry.getCurrency() == null ? currency.getCurrencyID() : entry.getCurrency().getCurrencyID());
                        obj.put("currencysymbol", entry.getCurrency() == null ? currency.getSymbol() : entry.getCurrency().getSymbol());
                        obj.put("currencyname", entry.getCurrency() == null ? currency.getName() : entry.getCurrency().getName());
                        obj.put("amount", creditCurrencyAmount);
                        obj.put("amountinbase", authHandler.round(creditAmount, companyid));
                        obj.put("billno", "");
                        obj.put("group", "GL");
                        obj.put("journalentryid", entry.getID());
                        obj.put("description", entry.getMemo());
                        obj.put("customer_vendor", "");
                        GLAmount = GLAmount + authHandler.round(creditAmount, companyid);

                        /*
                         Add Global Custom data for document
                         */
                        Map globalMap = new HashMap();
                        globalMap.put("moduleId", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        globalMap.put("companyId", companyid);
                        globalMap.put("jeId", entry.getID());
                        accReportsService.putGlobalCustomDetails(obj, globalMap);
                        DataJArr.put(obj);
                    }
                }
                totalAmount = ARAmount - (APAmount + GLAmount);  //Formula AR -(AP+GL)
                JSONObject obj = new JSONObject();
                obj.put("date", "");//df.format(new Date())        
                obj.put("entryno", "Total Amount");
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("currencyname", currency.getName());
                obj.put("amount", authHandler.round(totalAmount, companyid));
                obj.put("amountinbase", authHandler.round(totalAmount, companyid));
                obj.put("billno", "");
                obj.put("group", "Total(AR-(AP+GL))");
                DataJArr.put(obj);
                pagedJson = DataJArr;
//                if (consolidateFlag) {
                    String start = request.getParameter("start");
                    String limit = request.getParameter("limit");
                    if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                        pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                    }
//                }
            }
            Map columnModelParams = new HashMap();
            columnModelParams.put("companyId", companyid);
            columnModelParams.put("locale", locale);
            columnModelParams.put("reportId", request.getParameter("reportId"));
            createColumnModelForDetails(columnModelParams, jarrColumns, jarrRecords);
            commData.put("success", true);
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", DataJArr.length());
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            jobj1.put("valid", true);
            if (export) {
                jobj1.put("data", DataJArr);
            } else {
                jobj1.put("data", commData);
            }

        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj1;
    }

    /**
     *
     * @param map
     * @param jarrColumns
     * @param jarrRecords
     * @throws JSONException
     * @throws ServiceException
     * @Decsription : Create column model for Report
     */
    public void createColumnModelForDetails(Map map, JSONArray jarrColumns, JSONArray jarrRecords) throws JSONException, ServiceException {
        String companyId = "";
        String reportId = "";
        if (map.containsKey("companyId")) {
            companyId = map.get("companyId").toString();
        }
        if (map.containsKey("reportId")) {
            reportId = map.get("reportId").toString();
        }
        Locale locale = null;
        if(map.containsKey("locale")){
        locale = (Locale) map.get("locale");
        }
        JSONObject jobjTemp = new JSONObject();
        // Column Model
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "entryno");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "group");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "billno");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "date");
        jobjTemp.put("type", "date");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "amountinbase");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "description");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "customer_vendor");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "Company");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "details");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", GoodsReceiptCMNConstants.BILLNO);
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", GoodsReceiptCMNConstants.CURRENCYID);
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", GoodsReceiptCMNConstants.CURRENCYSYMBOL);
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "isExpense");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "isCreditNote");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "isDebitNote");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.saleByItem.gridInvoice", null, locale));
        jobjTemp.put("dataIndex", "billno");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.rem.18.1", null, locale));
        jobjTemp.put("dataIndex", "group");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.accPref.autoJE", null, locale));
        jobjTemp.put("dataIndex", "entryno");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("sortable", true);
        jobjTemp.put("summaryRenderer", "function(){\n"
                + "            return '<div class=\"grid-summary-common\">'+WtfGlobal.getLocaleText(\"acc.common.total\")+'</div>'\n"
                + "        }");
        jarrColumns.put(jobjTemp);
//
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.field.TransactionDate", null, locale));
        jobjTemp.put("dataIndex", "date");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("sortable", true);
        jobjTemp.put("renderer", "WtfGlobal.onlyDateDeletedRenderer");
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.bankReconcile.import.grid.Amount", null, locale));
        jobjTemp.put("dataIndex", "amountinbase");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jobjTemp.put("summaryType", "sum");
        jobjTemp.put("renderer", "WtfGlobal.currencyDeletedRenderer");
        jobjTemp.put("summaryRenderer", "function(value, m, rec) {\n"
                + "                if (value != 0) {\n"
                + "                    var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)\n"
                + "                    return retVal;\n"
                + "                } else {\n"
                + "                    return '';\n"
                + "                }\n"
                + "            }");
        jarrColumns.put(jobjTemp);
//            
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header2", null, locale));
        jobjTemp.put("dataIndex", "description");
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.salescomission.Customer/VendorName", null, locale));
        jobjTemp.put("dataIndex", "customer_vendor");
//        jobjTemp.put("hidden", true);
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);
//            
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.repeatedJE.Gridcol2", null, locale));
        jobjTemp.put("dataIndex", "Company");
        jobjTemp.put("hidden", true);
        jobjTemp.put("width", 150);
        jobjTemp.put("pdfwidth", 150);
        jarrColumns.put(jobjTemp);

        HashMap hashMap = new HashMap();
        hashMap.put("companyId", companyId);
        hashMap.put("reportId", reportId);
        KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
        List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
        List arrayList = new ArrayList();
        for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
            String column = "Custom_" + customizeReportMapping.getDataIndex();
            if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", column);
                jarrRecords.put(jobjTemp);
                jobjTemp = new JSONObject();
                jobjTemp.put("header", customizeReportMapping.getDataHeader());
                jobjTemp.put("dataIndex", column);
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("custom", "true");
                jarrColumns.put(jobjTemp);
                arrayList.add(customizeReportMapping.getDataIndex());
            }
        }
    }

    /**
     *
     * @param dataArray
     * @return
     * @Description : Format JSON for Detail Export
     */
    public JSONArray formatDataWithLineDetails(JSONArray dataArray) {
        JSONObject jobj = new JSONObject();
        JSONArray finalArray = new JSONArray();
        try {
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject obj = dataArray.getJSONObject(i);
                finalArray.put(obj);
                if (obj.has("details")) {
                    JSONArray jArray = (JSONArray) obj.optJSONArray("details");
                    int prCount = 0;
                    int exDetailCount = 0;
                    int jeCount = 0;
                    for (int j = 0; j < jArray.length(); j++) {
                        JSONObject row = jArray.getJSONObject(j);
                        String type = row.optString("detailType");
                        if (type.equalsIgnoreCase("productDetails")) {
                            row.put("srno", prCount + 1);
                            row.put("prdescription", row.opt("description"));
                            row.remove("description");
                            row.put("prdiscountamt", row.opt("discountamt"));
                            row.put("prtaxamt", row.opt("taxamt"));
                            row.put("pramount", row.opt("amount"));
                            prCount++;
                            finalArray.put(row);
                        } else if (type.equalsIgnoreCase("expenseDetails")) {
                            row.put("srnoforexaccount", exDetailCount + 1);
                            row.put("exaccountname", row.opt("accountname"));
                            row.put("exdescription", row.opt("description"));
                            row.remove("description");
                            row.put("examount", row.opt("amount"));
                            row.put("exdiscountamt", row.opt("discountamt"));
                            row.put("extaxamt", row.opt("taxamt"));
                            row.put("extotalamt", row.opt("totalamt"));
                            finalArray.put(row);
                            exDetailCount++;
                        } else {
                            row.put("srnoforcraccount", jeCount + 1);
                            row.put("jeaccountname", row.opt("accountname"));
                            row.put("jedescription", row.opt("description"));
                            row.remove("description");
                            row.put("jeamount", row.opt("amount"));
                            finalArray.put(row);
                            jeCount++;
                        }
                    }
                }
            }
            jobj.put("data", finalArray);
        } catch (Exception ex) {
            Logger.getLogger(accCreditNoteController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalArray;
    }
    
    public ModelAndView getCostOfManufacturing(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        JSONObject returnObject = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);

            /*
             * Get common request parameters
             */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            paramJobj.put("df", df);
            
            /*
             * Get Cost CategoryExpenses Masteritems
             */
            Map<String,Object>dataMap=new HashMap();
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("customfieldname"))) {
                /**
                 * If Custom field selected from filter.
                 * Get Module wise Custom Values for selected field.
                 */
                paramJobj.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                List<FieldComboData> productFieldComboDatas = accOtherReportsService.getFieldComboData(paramJobj);
                paramJobj.put("moduleid", Constants.Account_Statement_ModuleId);
                List<FieldComboData> accountFieldComboDatas = accOtherReportsService.getFieldComboData(paramJobj);
                dataMap.put("productFieldComboDatas", productFieldComboDatas);
                dataMap.put("accountFieldComboDatas", accountFieldComboDatas);

                /**
                 * Create List for selected cost category values (Custom Field) for product mudule.
                 */
                List<String> fcdids = new ArrayList();
                fcdids = accOtherReportsService.getFcdIdForField(dataMap, paramJobj);
                dataMap.put("productsfcdids", fcdids);
                
                /**
                 * Get Column number for selected custom field for product and account module.
                 */
                paramJobj.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                int productcolnum = accOtherReportsService.getCustomColumnNoForModuleField(paramJobj);
                paramJobj.put("moduleid", Constants.Account_Statement_ModuleId);
                int accountcolnum = accOtherReportsService.getCustomColumnNoForModuleField(paramJobj);
                dataMap.put("productcolnum", productcolnum);
                dataMap.put("accountcolnum", accountcolnum);
                
            }else{
                /**
                 * If custom field not selected then return empty data.
                 */
                paramJobj.put("returnEmptyData", true);
                
            }
            
            /**
             * Get Data for report.
             */
            jobjTemp = accOtherReportsService.getCostOfManufacturingData(paramJobj, dataMap);
            
//            List<MasterItem> listMasterItems = accOtherReportsService.getCostCategoryMasterItems(paramJobj);
            
            /*
             * Get Grid configuration Meta Data and Column/Field Information
             */
            jobj = accOtherReportsService.getCostOfManufacturingColumnModel(paramJobj,dataMap);
            
            
            /*
             * Get Cost of Manufacturing Data
             */

            JSONObject dataObj = jobjTemp.getJSONObject("data");
            dataObj.put("columns", jobj.getJSONArray("columns"));
            dataObj.put("success", true);
            dataObj.put("metaData", jobj.getJSONObject("metadata"));
            returnObject.put("data", dataObj);
            returnObject.put("valid", true);
            issuccess = true;
        } catch (SessionExpiredException | JSONException | ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnObject.put("success", issuccess);
                returnObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, returnObject.toString());
    }

    public ModelAndView exportCostOfManufacturing(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobject = new JSONObject();
        JSONObject jsonobject = new JSONObject();
        String view = "jsonView_ex";
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);

            /*
             * Get common request parameters
             */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("locale", RequestContextUtils.getLocale(request));
            paramJobj.put("df", df);
            
            /*
             * Get Cost CategoryExpenses Masteritems
             */
            List<MasterItem> listMasterItems = accOtherReportsService.getCostCategoryMasterItems(paramJobj);
            
            /*
             * Get Cost of Manufacturing Data
             */
            Map<String, Object> dataMap = new HashMap();
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("customfieldname"))) {
                /**
                 * If Custom field selected from filter. Get Module wise Custom
                 * Values for selected field.
                 */
                paramJobj.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                List<FieldComboData> productFieldComboDatas = accOtherReportsService.getFieldComboData(paramJobj);
                paramJobj.put("moduleid", Constants.Account_Statement_ModuleId);
                List<FieldComboData> accountFieldComboDatas = accOtherReportsService.getFieldComboData(paramJobj);
                dataMap.put("productFieldComboDatas", productFieldComboDatas);
                dataMap.put("accountFieldComboDatas", accountFieldComboDatas);

                /**
                 * Create List for selected cost category values (Custom Field)
                 * for product mudule.
                 */
                List<String> fcdids = new ArrayList();
                fcdids = accOtherReportsService.getFcdIdForField(dataMap, paramJobj);
                dataMap.put("productsfcdids", fcdids);

                /**
                 * Get Column number for selected custom field for product and
                 * account module.
                 */
                paramJobj.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                int productcolnum = accOtherReportsService.getCustomColumnNoForModuleField(paramJobj);
                paramJobj.put("moduleid", Constants.Account_Statement_ModuleId);
                int accountcolnum = accOtherReportsService.getCustomColumnNoForModuleField(paramJobj);
                dataMap.put("productcolnum", productcolnum);
                dataMap.put("accountcolnum", accountcolnum);

            } else {
                /**
                 * If custom field not selected then return empty data.
                 */
                paramJobj.put("returnEmptyData", true);

            }

            
            jobject = accOtherReportsService.getCostOfManufacturingData(paramJobj,dataMap);
            jsonobject = jobject.getJSONObject("data");
            jobj.put("data",jsonobject.getJSONArray("coldata"));
            
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
                exportDaoObj.processRequest(request, response, jobj);
            } else {
                exportDaoObj.processRequest(request, response, jobj);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public void exportInventoryMovementReportJasper(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> stockLocatioMap = new HashMap<String, Object>();
	String view = "inventoryMovementDeatils";        
        ArrayList<InventoryMovementDetails> detailsList = new ArrayList<InventoryMovementDetails>();
        ArrayList<OnlyDatePojo> OnlyDatePojolist = new ArrayList<OnlyDatePojo>();
         List<JasperPrint> jasperPrintList = new ArrayList<JasperPrint>();
        try {
            SimpleDateFormat df1=new SimpleDateFormat("dd-MM-yyyy");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();   
            SimpleDateFormat df=new SimpleDateFormat("MMMM yyyy");
            final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
            Date startDate = dtf.parseLocalDate(request.getParameter("stdate")).toDate();
            Date endDate = dtf.parseLocalDate(request.getParameter("enddate")).toDate();
            LocalDate localStartDate = new LocalDate(startDate);
            LocalDate localEndDate = new LocalDate(endDate);

            startDate = localStartDate.toDateTimeAtCurrentTime().dayOfMonth().withMinimumValue().toDate();
            endDate = localEndDate.toDateTimeAtCurrentTime().dayOfMonth().withMaximumValue().toDate();
            startDate.setHours(00);
            startDate.setMinutes(00);
            startDate.setSeconds(00);

            endDate.setHours(00);
            endDate.setMinutes(00);
            endDate.setSeconds(00);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startDay = cal.get(Calendar.DATE);
            int startMonth = cal.get(Calendar.MONTH + 1);
            int startYear = cal.get(Calendar.YEAR);

            cal.setTime(endDate);
            int endDay = cal.get(Calendar.DATE);
            int endMonth = cal.get(Calendar.MONTH + 1);
            int endYear = cal.get(Calendar.YEAR);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM DD, yyyy HH:mm:ss a");

            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put(Constants.REQ_startdate,startDate);
            requestParams.put(Constants.REQ_enddate, endDate);
            requestParams.put(Constants.df, sdf);
            requestParams.put("companyid", companyid);
            String ss="";
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
            if(request.getParameter("ss")!=null)
                ss=request.getParameter("ss");
            if(!ss.equals("") && !ss.equals("undefined"))
                requestParams.put("ss", request.getParameter("ss"));    
            requestParams.put("locationid", request.getParameter("locationid"));
            requestParams.put("type", request.getParameter("type"));
            KwlReturnObject result =accProductObj.getAssemblyProducts(requestParams);
            List<ProductBuild> list = result.getEntityList();           
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                try {
                    InventoryMovementDetails details = new InventoryMovementDetails();
                    ProductBuild prodBuild =(ProductBuild)itr.next();
                    JSONObject obj = new JSONObject();
                    details.setProductID(prodBuild.getProduct().getProductid());
                    details.setProductName(prodBuild.getProduct().getName());
                    details.setQuantity(prodBuild.getQuantity());
                    details.setBuildDate(df1.format(prodBuild.getEntryDate()));
                    details.setCost(prodBuild.getProductcost());
                    details.setBuildRefNo(prodBuild.getRefno());
                    details.setMemo(prodBuild.getMemo());
                    details.setDescription(prodBuild.getDescription());
                    details.setMonthYear(df.format(prodBuild.getEntryDate()));
                    details.setCurrency(baseCurrency.getCurrencyCode());
                    detailsList.add(details);
                } catch (Exception ex) {
                    throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
                }
            }
             OnlyDatePojo datePojo=new OnlyDatePojo();
            datePojo.setDate(df1.format(new Date()));
            OnlyDatePojolist.add(datePojo);
	    stockLocatioMap.put("format", "pdf");
            JasperPrint jasperPrint = null;
            JasperReport jasperReport = null;
            JasperReport jasperReportSubReport = null;
            InputStream inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/InventoryMovementDetails.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            jasperReport = JasperCompileManager.compileReport(jasperDesign);

            InputStream inputStreamSubReport = null;
           
             inputStreamSubReport = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/InventoryMovementDetails_subreport.jrxml");
 
            JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
            jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);

            stockLocatioMap.put("SubReport", jasperReportSubReport);
            JRBeanCollectionDataSource sub = new JRBeanCollectionDataSource(detailsList);
             stockLocatioMap.put("InventoryMovementReportData", sub);
             stockLocatioMap.put("CompanyName", company.getCompanyName());
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(OnlyDatePojolist);
            jasperPrint = JasperFillManager.fillReport(jasperReport, stockLocatioMap, beanColDataSource);
            jasperPrintList.add(jasperPrint);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            response.setHeader("Content-Disposition", "attachment;filename=" + "InventoryMovementDetails.pdf");
            exp.exportReport();

	   }catch (Exception ex) {
            Logger.getLogger(AccExportOtherReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
//        return jasperPrintList;
    }
    
    public void exportCreditNoteRegister(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> registerMap = new HashMap<String, Object>();
        HashMap<String, Object> subReportMap = null;
        HashMap<String, String> subReportTableMap = null;
        ArrayList<HashMap<String, Object>> subReportsList = new ArrayList<HashMap<String, Object>>();

        /*
         * Code for Header part which includes the standard header information.
         */

        String startDate = "", endDate = "";
        JSONArray DataJArr = new JSONArray();
        try {
            String companyname = "", companyaddress = "", companyphone = "", companyfax = "", companyemail = "";
            String startPeriod = "", endPeriod = "", rangeType = "", currencyname = "", currencycode = "";
            int mode = Integer.parseInt(request.getParameter("mode"));
            boolean isCreditNoteRegister = false;
            if (mode == 27) {
                isCreditNoteRegister = true;
            }
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);
//            DateFormat df2 = authHandler.getUserDateFormatter(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) compaccresult.getEntityList().get(0);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            companyname = company.getCompanyName() != null ? company.getCompanyName() : "";
            companyaddress = AccountingAddressManager.getCompanyDefaultBillingAddress(companyid, accountingHandlerDAOobj);
            companyemail = company.getEmailID() != null ? company.getEmailID() : "";
            companyfax = company.getFaxNumber() != null ? company.getFaxNumber() : "";
            companyphone = company.getPhoneNumber() != null ? company.getPhoneNumber() : "";
            currencyname = company.getCurrency().getName();
            currencycode = accCommonTablesDAO.getCustomCurrencyCode(company.getCurrency().getCurrencyCode(), companyid);

            //Calculate Accounting Period
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate")) && !StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                startDate = df.format(authHandler.getGlobalDateFormat().parse(request.getParameter("startdate")));
                endDate = df.format(authHandler.getGlobalDateFormat().parse(request.getParameter("enddate")));    //ERP-8442
            }
            startPeriod = df.format(companyAccountPreferences.getFinancialYearFrom());
            Calendar c1 = Calendar.getInstance();
            c1.setTime(df.parse(startPeriod));
            c1.add(Calendar.YEAR, 1); // number of years to add
            c1.add(Calendar.DATE, -1);
            endPeriod = df.format(c1.getTime());
            request.setAttribute(Constants.isExport, true);

            HashMap dataHashMap = null;
            if (isCreditNoteRegister) {
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                dataHashMap = accCreditNoteService.getCreditNoteCommonCode(paramJobj);
            } else {
                dataHashMap = accDebitNoteService.getDebitNoteCommonCode(request, response);
            }
            DataJArr = (JSONArray) dataHashMap.get("data");

            double taxamount = 0;
            double totalamount = 0;

            /*
             * Code for Credit Note Information.
             */

            for (int i = 0; i < DataJArr.length(); i++) {
                subReportMap = new HashMap<String, Object>();
                ArrayList<HashMap<String, String>> accountTableList = new ArrayList<HashMap<String, String>>(); // stores account info
                ArrayList<HashMap<String, String>> invoiceTableList = new ArrayList<HashMap<String, String>>(); // stores invoice info
                ArrayList<HashMap<String, String>> returnsTableList = new ArrayList<HashMap<String, String>>(); // stores product info
                JSONObject jobj = DataJArr.getJSONObject(i);
                String billid = jobj.optString("billid", "");
                subReportMap.put("isCN", isCreditNoteRegister ? "true" : "false");
                subReportMap.put("isCustomer", jobj.optString("iscustomer", "true"));
                subReportMap.put("isReturn", jobj.optString("isReturnNote", "false"));
                subReportMap.put("number", jobj.optString("noteno", ""));
                subReportMap.put("date", jobj.optString("dateinuserformat", ""));
                subReportMap.put("personCode", jobj.optString("personcode", ""));
                subReportMap.put("personName", jobj.optString("personname", ""));
                subReportMap.put("paymentterm", jobj.optString("paymentterm", ""));
                subReportMap.put("currency",accCommonTablesDAO.getCustomCurrencyCode(jobj.optString("currencycode", ""), companyid));
                subReportMap.put("totalamount",accCommonTablesDAO.getCustomCurrencySymbol(jobj.optString("currencysymbol", "") , companyid) + " " + authHandler.formattedCommaSeparatedAmount(jobj.optDouble("amount", 0), companyid));
                double exchangerate = jobj.optDouble("externalcurrencyrate", 1);
                if (exchangerate == 1 || exchangerate == 0) {
                    subReportMap.put("remark", jobj.optString("memo", ""));
                } else {
                    String exchStmt = "1 " + currencyname + " = " + authHandler.formattedCommaSeparatedAmount(exchangerate, companyid) + " " + jobj.optString("currencyname", "");
                    subReportMap.put("remark", jobj.optString("memo", "") + "\nExchange Rate : " + exchStmt);
                }

                totalamount += jobj.optDouble("paidamountinbase", 0);
                taxamount += jobj.optDouble("taxamountinbase", 0);

                /*
                 * Code for Credit Note Details Information.
                 */

                boolean isReturnNote = jobj.optBoolean("isReturnNote", false);
                boolean isOpeningBalanceTransaction = jobj.optBoolean("isOpeningBalanceTransaction", false);
                if (isOpeningBalanceTransaction) {
                    subReportTableMap = new HashMap<String, String>();
                    subReportTableMap.put("srno", "");
                    subReportTableMap.put("description", "Opening Balance for " + jobj.optString("personname", ""));
                    subReportTableMap.put("qty", "");
                    subReportTableMap.put("rate", "");
                    subReportTableMap.put("amount", authHandler.formattedCommaSeparatedAmount(jobj.optDouble("amount", 0), companyid));
                    accountTableList.add(subReportTableMap);
                } else if (isReturnNote) {     
                    /*
                     * Code for Sales Return with Credit Note
                     */
                    int accountCount = 1;
                    int returnsCount = 1;
                    JSONArray jArray = null;
                    if (isCreditNoteRegister) {
                        JSONObject temp = accCreditNoteServiceCMN.getCreditNoteRow(request, billid.split(","));
                        jArray = (JSONArray) temp.get("data");
                    } else {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
                        requestParams.put("bills", billid.split(","));
                        requestParams.put("df", authHandler.getDateFormatter(request));
                        requestParams.put(Constants.userdf, df);
                        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                        jArray = accDebitNoteService.getDebitNoteRowsJson(requestParams);
                    }
                    for (int j = 0; j < jArray.length(); j++) {
                        JSONObject row = jArray.getJSONObject(j);
                        if (row.optBoolean("isaccountdetails", false)) {

                            /*
                             * Stores Account Information against which used in
                             * Sales Return
                             */

                            subReportTableMap = new HashMap<String, String>();
                            subReportTableMap.put("srno", accountCount + ".");
                            subReportTableMap.put("description", row.optString("accountname", ""));
                            subReportTableMap.put("qty", authHandler.formattedCommaSeparatedAmount((row.optDouble("totalamountforaccount", 0)), companyid));
                            subReportTableMap.put("rate", authHandler.formattedCommaSeparatedAmount(row.optDouble("taxamount", 0), companyid));
                            subReportTableMap.put("amount", authHandler.formattedCommaSeparatedAmount((row.optDouble("totalamountforaccount", 0) + row.optDouble("taxamount", 0)), companyid));
                            accountTableList.add(subReportTableMap);
                            accountCount++;
                        }

                    }
                    if (isCreditNoteRegister) {
                        KwlReturnObject creditNoteResult = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), billid);
                        CreditNote cn = (CreditNote) creditNoteResult.getEntityList().get(0);
                        SalesReturn sr = cn.getSalesReturn();
                        if (sr != null) {
                            Iterator itr = sr.getRows().iterator();
                            while (itr.hasNext()) {
                                SalesReturnDetail srd = (SalesReturnDetail) itr.next();

                                /*
                                 * Stores Product Information which is return in
                                 * Sales Return
                                 */

                                subReportTableMap = new HashMap<String, String>();
                                String description = srd.getDescription();
                                double rate = srd.getRate();
                                double quantity = srd.getReturnQuantity();
                                String uom = srd.getUom() == null ? "" : " " + srd.getUom().getNameEmptyforNA();
                                double amount = rate * quantity;
                                double rowtaxamount = srd.getRowTaxAmount();
                                amount += rowtaxamount;
                                subReportTableMap.put("srno", returnsCount + ".");
                                subReportTableMap.put("description", description);
                                subReportTableMap.put("qty", authHandler.formattedQuantity(quantity, companyid) + uom);
                                subReportTableMap.put("rate", authHandler.formattedCommaSeparatedAmount(rate, companyid));
                                subReportTableMap.put("amount", authHandler.formattedCommaSeparatedAmount(amount, companyid));
                                returnsTableList.add(subReportTableMap);
                                returnsCount++;
                            }
                        }
                    } else {
                        KwlReturnObject debitNoteResult = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), billid);
                        DebitNote cn = (DebitNote) debitNoteResult.getEntityList().get(0);
                        PurchaseReturn pr = cn.getPurchaseReturn();
                        if (pr != null) {
                            Iterator itr = pr.getRows().iterator();
                            while (itr.hasNext()) {
                                PurchaseReturnDetail prd = (PurchaseReturnDetail) itr.next();

                                /*
                                 * Stores Product Information which is return in
                                 * Sales Return
                                 */

                                subReportTableMap = new HashMap<String, String>();
                                String description = prd.getDescription();
                                double rate = prd.getRate();
                                double quantity = prd.getReturnQuantity();
                                String uom = prd.getUom() == null ? "" : " " + prd.getUom().getNameEmptyforNA();
                                double amount = rate * quantity;
                                double rowtaxamount = prd.getRowTaxAmount();
                                amount += rowtaxamount;
                                subReportTableMap.put("srno", returnsCount + ".");
                                subReportTableMap.put("description", description);
                                subReportTableMap.put("qty", authHandler.formattedQuantity(quantity, companyid) + uom);
                                subReportTableMap.put("rate", authHandler.formattedCommaSeparatedAmount(rate, companyid));
                                subReportTableMap.put("amount", authHandler.formattedCommaSeparatedAmount(amount, companyid));
                                returnsTableList.add(subReportTableMap);
                                returnsCount++;
                            }
                        }
                    }
                } else {    
                    /*
                     * Code for Credit Note Details in Other cases
                     */
                    JSONArray jArray = null;
                    if (isCreditNoteRegister) {
                        JSONObject temp = accCreditNoteServiceCMN.getCreditNoteRow(request, billid.split(","));
                        jArray = (JSONArray) temp.get("data");
                    } else {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
                        requestParams.put("bills", billid.split(","));
                        requestParams.put("df", authHandler.getDateFormatter(request));
                        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put(Constants.isExport, true);
                        jArray = accDebitNoteService.getDebitNoteRowsJson(requestParams);
                    }
                    int accountCount = 1;
                    int invoiceCount = 1;
                    for (int j = 0; j < jArray.length(); j++) {
                        JSONObject row = jArray.getJSONObject(j);
                        if (row.optBoolean("isaccountdetails", false)) {

                            /*
                             * Stores Account Information which used in Credit
                             * Note
                             */

                            subReportTableMap = new HashMap<String, String>();
                            String desc = row.optString("accountname", "")
                                    + ((row.optString("description", "-").equals("-")||row.optString("description", "-").equals(""))? "" : "<br>Description : " + row.optString("description", ""))
                                    + ((row.optString("reason", "-").equals("-")||row.optString("reason", "-").equals("")) ? "" : "<br>Reason : " + row.optString("reason", ""));
                            subReportTableMap.put("srno", accountCount + ".");
                            subReportTableMap.put("description", desc);
                            subReportTableMap.put("qty", authHandler.formattedCommaSeparatedAmount((row.optDouble("totalamountforaccount", 0)), companyid));
                            subReportTableMap.put("rate", authHandler.formattedCommaSeparatedAmount(row.optDouble("taxamount", 0), companyid));
                            subReportTableMap.put("amount", authHandler.formattedCommaSeparatedAmount((row.optDouble("totalamountforaccount", 0) + row.optDouble("taxamount", 0)), companyid));
                            accountTableList.add(subReportTableMap);
                            accountCount++;
                        } else {

                            /*
                             * Stores Invoice Information against which used in
                             * Credit Note against Invoice
                             */

                            subReportTableMap = new HashMap<String, String>();
                            subReportMap.put("isInvoice", "true");
                            subReportTableMap.put("srno", invoiceCount + ".");
                            String desc = row.optString("transectionno", "")
                                    + "<br>Invoice Amount : " + accCommonTablesDAO.getCustomCurrencyCode(row.optString("currencysymbol", "") , companyid)+ " " + authHandler.formattedCommaSeparatedAmount(row.optDouble("invamount", 0), companyid)
                                    + " - Amount Due : " + accCommonTablesDAO.getCustomCurrencySymbol(row.optString("currencysymbol", "") , companyid)+ " " + authHandler.formattedCommaSeparatedAmount(row.optDouble("invamountdue", 0), companyid)
                                    + (row.optDouble("exchangeratefortransaction", 1) == 1 ? "" : "<br>Exchange Rate : 1 " + row.optString("currencyname", "") + " = " + authHandler.formattedCommaSeparatedAmount(row.optDouble("exchangeratefortransaction", 1), companyid) + " " + jobj.optString("currencyname", ""));
                            subReportTableMap.put("description", desc);
                            subReportTableMap.put("qty", "");
                            subReportTableMap.put("rate", "");
                            subReportTableMap.put("amount", authHandler.formattedCommaSeparatedAmount(row.optDouble("paidAmountinTransactionCurrency", 0), companyid));
                            invoiceTableList.add(subReportTableMap);
                            invoiceCount++;
                        }
                    }
                }
                subReportMap.put("accountTable", accountTableList);
                subReportMap.put("invoiceTable", invoiceTableList);
                subReportMap.put("returnsTable", returnsTableList);
                subReportsList.add(subReportMap);
            }

            /*
             * Putted all the info in the Map
             */
            registerMap.put("reportName", (isCreditNoteRegister ? "Credit " : "Debit ") + "Note Register - (Detail)");
            registerMap.put("companyname", companyname);
            registerMap.put("companyaddress", companyaddress);
            registerMap.put("companyphone", companyphone);
            registerMap.put("companyfax", companyfax);
            registerMap.put("companyemail", companyemail);
            registerMap.put("currency", currencyname);
            registerMap.put("currencycode", currencycode);
            registerMap.put("rangeType", "Custom");
            registerMap.put("fromDate", startDate);
            registerMap.put("toDate", endDate);
            registerMap.put("startPeriod", startPeriod);
            registerMap.put("endPeriod", endPeriod);
            registerMap.put("basicamount", authHandler.formattedCommaSeparatedAmount((totalamount - taxamount), companyid));
            registerMap.put("taxamount", authHandler.formattedCommaSeparatedAmount(taxamount, companyid));
            registerMap.put("totalamount", authHandler.formattedCommaSeparatedAmount(totalamount, companyid));
            registerMap.put("subReportData", new JRBeanCollectionDataSource(subReportsList));
            String filename = isCreditNoteRegister ? "CreditNoteRegister_v1.pdf" : "DebitNoteRegister_v1.pdf";
            response.setHeader("Content-Disposition", "attachment;filename=\"" + filename+"\"");

            /*
             * Putted Single Date Entry for Export
             */

            HashMap<String, String> map = new HashMap<String, String>();
            ArrayList<HashMap<String, String>> salesInvoiceList = new ArrayList<HashMap<String, String>>();
            map.put("date", df.format(new Date()));
            salesInvoiceList.add(map);

            /*
             * Load and Compile SubReport
             */

            InputStream inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/CreditNoteRegister.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            /*
             * Load and Compile MainReport
             */

            InputStream inputStreamSubReport = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/CreditNoteRegisterSubReport.jrxml");
            JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
            JasperReport jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);
            registerMap.put("subReport", jasperReportSubReport);

            /*
             * Export the Template
             */

            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(salesInvoiceList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, registerMap, beanColDataSource);
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception e) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public ModelAndView getMatchedReconciliationData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
         String msg = "";
        boolean issuccess = false;
        try {
            jobj = getMatchedReconciliationData(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getMatchedReconciliationData(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String transcurname = "";
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            boolean isConcileReport = StringUtil.isNullOrEmpty(request.getParameter("isConcileReport")) ? false : Boolean.parseBoolean(request.getParameter("isConcileReport"));
            String accountid = request.getParameter("accountid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(request);
            String entryChar = "d", emptyChar = "c";

            //Used GlobalDateFormat to avoid change in time as per time zone. ERP-8474
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();

            double minAmount = StringUtil.isNullOrEmpty(request.getParameter("minamount")) ? 0 : Double.parseDouble(request.getParameter("minamount"));
            double maxAmount = StringUtil.isNullOrEmpty(request.getParameter("maxamount")) ? 0 : Double.parseDouble(request.getParameter("maxamount"));
            String searchPayee = StringUtil.isNullOrEmpty(request.getParameter("payee")) ? "" : request.getParameter("payee");
            String reference = "";
            String chequeNumber = "";
            String descrip = "";
            Date startDate = null;
            Date endDate = null;
            boolean isBankImport = false;
            /**
             * Getting JSON Object which is set in case when we import records
             * from Bank i.e through rest service and setting the values in
             * respective variables as we need to fetch the records i.e MP and
             * RP matching with selected Accountid, payee, startdate, enddate
             * and amount.In case of Import from Bank startdate, enddate are the
             * bank transaction date and maxamount and minamount is bank
             * transaction amount. ERP-41871
             */
            if ((request.getAttribute("isBankImport") != null) && (request.getAttribute("isBankImport") instanceof JSONObject)) {
                isBankImport = true;
                JSONObject importedRecordsJson = (JSONObject) request.getAttribute("isBankImport");
                searchPayee = importedRecordsJson.optString("payee", "");
                try {
                    startDate = formatter.parse(importedRecordsJson.optString("date"));     //to confirm the default value
                    endDate = formatter.parse(importedRecordsJson.optString("date"));
                } catch (Exception ex) {
                    Logger.getLogger(accOtherReportsController.class.getName() + ":getMatchedReconciliationData").log(Level.SEVERE, null, ex);
                }
                reference = importedRecordsJson.optString("reference", "");
                chequeNumber = importedRecordsJson.optString("chequenumber", "");
                descrip = importedRecordsJson.optString("desc", "");
                maxAmount = importedRecordsJson.optDouble("amount", 0.00);
                minAmount = importedRecordsJson.optDouble("amount", 0.00);
            }
            /**
             * if we import records using CSV then the start date and end date
             * are passed in request hence getting startDate and endDate from
             * request. ERP-41871
             */
            if (!isBankImport) {
                startDate = formatter.parse(request.getParameter("stdate"));
                endDate = formatter.parse(request.getParameter("enddate"));
            }
            
            KwlReturnObject lresult = accBankReconciliationObj.getMachingRecordsForReconciliation(companyid, accountid, startDate, endDate, minAmount, maxAmount);
            List list = lresult.getEntityList();
            String transCurrSymbol = "";    //Transaction Currency Symbol.

            Iterator itr = list.iterator();
            JSONArray jArrL = new JSONArray();

            KwlReturnObject accountResult1 = accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Account", accountid);
            Account account1 = (Account) accountResult1.getEntityList().get(0);
            String accountcurrencyid = account1.getCurrency() == null ? currency.getCurrencyID() : account1.getCurrency().getCurrencyID();
            String accountCurrSymbol = account1.getCurrency() == null ? currency.getSymbol() : account1.getCurrency().getSymbol();

            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JournalEntryDetail jed = (JournalEntryDetail) row[1];
                boolean exchangeGainLoss = jed.getJournalEntry().getIsReval() == 2 ? true : false;    //ERP-14744
                if (exchangeGainLoss) {
                    continue;
                }
                JournalEntry entry = (JournalEntry) row[0];
                BankReconciliation br = null;
                BankReconciliationDetail brd = null;
                String brdID = "";
                int transactionModuleId = entry.getTransactionModuleid();
                if (isConcileReport) {
                    br = (BankReconciliation) row[2];
                    brd = (BankReconciliationDetail) row[3];
                    brdID = brd.getID();
                }
                boolean withoutinventory = true;
                currencyid = (jed.getJournalEntry().getCurrency() == null ? currency.getCurrencyID() : jed.getJournalEntry().getCurrency().getCurrencyID());
                transCurrSymbol = (jed.getJournalEntry().getCurrency() == null ? currency.getSymbol() : jed.getJournalEntry().getCurrency().getSymbol());
                transcurname = (jed.getJournalEntry().getCurrency() == null ? currency.getName() : jed.getJournalEntry().getCurrency().getName());
                KwlReturnObject amountresult = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, jed.getAmount(), currencyid, jed.getJournalEntry().getEntryDate(), jed.getJournalEntry().getExternalCurrencyRate());
                double jedAmountInBase = (Double) amountresult.getEntityList().get(0);
                amountresult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, jedAmountInBase, accountcurrencyid, jed.getJournalEntry().getEntryDate(), 0);
                double amountInAccountCurrency = (Double) amountresult.getEntityList().get(0);

                Set details = entry.getDetails();
                Iterator iter = details.iterator();
                String accountName = "";
                String accountCode = "";
                while (iter.hasNext()) {
                    JournalEntryDetail d = (JournalEntryDetail) iter.next();
                    if (d.isDebit() == jed.isDebit()) {
                        continue;
                    }
                    accountName += d.getAccount().getName() + ", ";
                    accountCode += d.getAccount().getAcccode() + ", ";
                }
                accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));
                if (StringUtil.isNullOrEmpty(accountName)) {
                    accountName = accountCode.substring(0, Math.max(0, accountCode.length() - 2));
                }


                JSONObject obj = new JSONObject();
                if (isConcileReport && br != null) {
                    obj.put(entryChar + "_reconciledate", authHandler.getDateOnlyFormat(request).format(br.getClearanceDate()));
                    obj.put(entryChar + "_reconciledateinuserdf", userdf.format(br.getClearanceDate()));
                    obj.put("lastreconciledate", authHandler.getDateOnlyFormat(request).format(br.getClearanceDate())); //ERP-12881
                }
                obj.put("isdebit", jed.isDebit());
                obj.put("id", brdID);
                obj.put(entryChar + "_date", authHandler.getDateOnlyFormat(request).format(entry.getEntryDate()));
                obj.put(entryChar + "_dateinuserdf", userdf.format(entry.getEntryDate()));
                obj.put(entryChar + "_accountname", accountName);
                obj.put(entryChar + "_entryno", entry.getEntryNumber());
                obj.put(entryChar + "_journalentryid", entry.getID());
                obj.put(entryChar + "_amountintransactioncurrency", authHandler.round(jed.getAmount(), companyid));
                double baseamt = jedAmountInBase;
                obj.put(entryChar + "_amount", authHandler.round(baseamt, companyid));//this is amount in base crrency 
                if (currencyid.equals(accountcurrencyid)) {
                    obj.put(entryChar + "_amountinacc", authHandler.round(jed.getAmount(), companyid));//this is amount in Account crrency for PDF
                } else {
                    double amtinAccCurr = amountInAccountCurrency;   //Amount in Account Currency
                    if (jed.getJournalEntry().getTypeValue() == Constants.FundTransfer_Journal_Entry && jed.getExchangeRateForTransaction() != 0 && jed.getExchangeRateForTransaction() != -1) {
                        amtinAccCurr = jed.getAmount() / jed.getExchangeRateForTransaction();
                    }
                    obj.put(entryChar + "_amountinacc", authHandler.round(amtinAccCurr, companyid));//this is amount in Account crrency for PDF
                }

                obj.put(entryChar + "_transCurrSymbol", transCurrSymbol);
                obj.put("accountcurrencysymbol", accountCurrSymbol);//Account currency syambol
                obj.put(emptyChar + "_reconciledate", "");
                obj.put(emptyChar + "_reconciledateinuserdf", "");
                obj.put(emptyChar + "_date", "");
                obj.put(emptyChar + "_dateinuserdf", "");
                obj.put(emptyChar + "_accountname", "");
                obj.put(emptyChar + "_entryno", "");
                obj.put(emptyChar + "_journalentryid", "");
                obj.put(emptyChar + "_amountintransactioncurrency", "");
                obj.put(emptyChar + "_amount", "");
                obj.put(emptyChar + "_amountinacc", "");
                if (transactionModuleId == Constants.Acc_Receive_Payment_ModuleId && entry.getTransactionId() != null) {
                    KwlReturnObject resultReceipt = accountingHandlerDAOobj.getObject(Receipt.class.getName(), entry.getTransactionId());
                    Receipt receipt = (Receipt) resultReceipt.getEntityList().get(0);
                    if (receipt != null) {
                        obj.put("transactionID", (receipt).getReceiptNumber());
                        if ((receipt).getPayDetail() != null && ((receipt).getPayDetail().getCheque() != null)) {
                            obj.put("chequeno", (receipt).getPayDetail().getCheque().getChequeNo());
                            obj.put("chequedate", receipt.getPayDetail().getCheque().getDueDate() != null ? authHandler.getDateOnlyFormatter(request).format(receipt.getPayDetail().getCheque().getDueDate()) : "");
                            obj.put("chequedateinuserdf", receipt.getPayDetail().getCheque().getDueDate() != null ? userdf.format(receipt.getPayDetail().getCheque().getDueDate()) : "");
                            String description = (receipt).getPayDetail().getCheque().getDescription();
                            obj.put("description", description != null ? description : "");
                            /**
                             * Passing JE date in response for validating date
                             * on JS Side ERM-655.
                             */
                            obj.put("jeDate", entry.getEntryDate() != null ? authHandler.getDateOnlyFormat().format(entry.getEntryDate()) : "");
                            obj.put("jeDateinuserdf", entry.getEntryDate() != null ? userdf.format(entry.getEntryDate()) : "");

                            Vendor vendor = null;
                            if (receipt.getVendor() != null) {
                                KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                                vendor = (Vendor) custResult.getEntityList().get(0);
                            }
                            Customer customer = receipt.getCustomer();
                            String vName = "";
                           // try {
                                if (customer != null) {
                                    vName = StringUtil.DecodeText(customer.getName());
                                } else if (vendor != null) {
                                    vName = StringUtil.DecodeText(vendor.getName());
                                } else {
                                    KwlReturnObject result = accReceiptDao.getReceiptCustomerNames(receipt.getCompany().getCompanyID(), receipt.getID());
                                    List cNameList = result.getEntityList();
                                    Iterator cNamesItr = cNameList.iterator();
                                    String customerNames = "";
                                    try {
                                        while (cNamesItr.hasNext()) {
                                            String tempName = URLEncoder.encode((String) cNamesItr.next(), "UTF-8");
                                            customerNames += tempName;
                                            customerNames += ",";
                                        }
                                        customerNames = customerNames.substring(0, Math.max(0, customerNames.length() - 1));
                                    } catch (Exception e) {
                                        throw ServiceException.FAILURE("getReconciliationData : " + e.getMessage(), e);
                                    }
                                    vName =StringUtil.DecodeText(customerNames);
                                }
                           /* } catch (UnsupportedEncodingException ex) {
                                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.WARNING, ex.getMessage());
                            }*/
                            obj.put(entryChar + "_accountname", vName);
                        }
                        obj.put("billid", (receipt).getID());
                        obj.put("isOpeningTransaction", false);
                        obj.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                        obj.put("paidto", receipt.getReceivedFrom() == null ? "" : (receipt.getReceivedFrom().getValue()) == null ? "" : receipt.getReceivedFrom().getValue());
                        obj.put("type", Constants.PAYMENT_RECEIVED);
                        obj.put("currencysymbol", receipt.getCurrency() != null ? receipt.getCurrency().getSymbol() : currency.getSymbol());
                        withoutinventory = false;
                    }
                } else if (transactionModuleId == Constants.Acc_Make_Payment_ModuleId && entry.getTransactionId() != null) {
                    KwlReturnObject resultPayment = accountingHandlerDAOobj.getObject(Payment.class.getName(), entry.getTransactionId());
                    Payment payment = (Payment) resultPayment.getEntityList().get(0);
                    if (payment != null) {
                        obj.put("transactionID", payment.getPaymentNumber());
                        if (payment.getPayDetail() != null && (payment.getPayDetail().getCheque() != null)) {
                            obj.put("chequeno", payment.getPayDetail().getCheque().getChequeNo());
                            obj.put("chequedate", payment.getPayDetail().getCheque().getDueDate() != null ? authHandler.getDateOnlyFormat(request).format(payment.getPayDetail().getCheque().getDueDate()) : "");
                            obj.put("chequedateinuserdf", payment.getPayDetail().getCheque().getDueDate() != null ? userdf.format(payment.getPayDetail().getCheque().getDueDate()) : "");
                            String description = payment.getPayDetail().getCheque().getDescription();
                            obj.put("description", description != null ? description : "");
                            /**
                             * Passing JE date in response for validating date
                             * on JS Side ERM-655.
                             */
                            obj.put("jeDate", entry.getEntryDate() != null ? authHandler.getDateOnlyFormat().format(entry.getEntryDate()) : "");
                            obj.put("jeDateinuserdf", entry.getEntryDate() != null ? userdf.format(entry.getEntryDate()) : "");

                            Customer customer = null;
                            if (payment.getCustomer() != null) {
                                KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                                customer = (Customer) custResult.getEntityList().get(0);
                            }
                            Vendor vendor = payment.getVendor();
                            String vName = "";
                            //try {
                                if (vendor != null) {
                                    vName = StringUtil.DecodeText(vendor.getName());
                                } else if (customer != null) {
                                    vName = StringUtil.DecodeText(customer.getName());
                                } else {
                                    KwlReturnObject result = accVendorPaymentobj.getPaymentVendorNames(companyid, payment.getID());
                                    List vNameList = result.getEntityList();
                                    Iterator vNamesItr = vNameList.iterator();
                                    String vendorNames = "";
                                    try {
                                        while (vNamesItr.hasNext()) {
                                            String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
                                            vendorNames += tempName;
                                            vendorNames += ",";
                                        }
                                        vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
                                    } catch (Exception e) {
                                        throw ServiceException.FAILURE("getReconciliationData : " + e.getMessage(), e);
                                    }
                                    vName = StringUtil.DecodeText(vendorNames);
                                }
                             /*}catch (UnsupportedEncodingException ex) {
                                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.WARNING, ex.getMessage());
                            }*/
                            obj.put(entryChar + "_accountname", vName);
                        }
                        obj.put("billid", payment.getID());
                        obj.put("isOpeningTransaction", false);
                        obj.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                        obj.put("type", Constants.PAYMENT_MADE);
                        obj.put("paidto", payment.getPaidTo() == null ? "" : payment.getPaidTo().getValue() == null ? "" : payment.getPaidTo().getValue());
                        obj.put("currencysymbol", payment.getCurrency() != null ? payment.getCurrency().getSymbol() : currency.getSymbol());
                        withoutinventory = false;
                    }
                } else if (transactionModuleId == Constants.Acc_Cash_Sales_ModuleId) {
                    KwlReturnObject resultPayment = accountingHandlerDAOobj.getObject(Invoice.class.getName(), entry.getTransactionId());
                    Invoice invoice = (Invoice) resultPayment.getEntityList().get(0);
                    if (invoice != null) {
                        obj.put("transactionID", invoice.getInvoiceNumber());
                        if (invoice.getPayDetail() != null && (invoice.getPayDetail().getCheque() != null)) {
                            obj.put("chequeno", invoice.getPayDetail().getCheque().getChequeNo());
                            obj.put("chequedate", invoice.getPayDetail().getCheque().getDueDate() != null ? authHandler.getDateOnlyFormat(request).format(invoice.getPayDetail().getCheque().getDueDate()) : "");
                            obj.put("chequedateinuserdf", invoice.getPayDetail().getCheque().getDueDate() != null ? userdf.format(invoice.getPayDetail().getCheque().getDueDate()) : "");
                            String description = invoice.getPayDetail().getCheque().getDescription();
                            obj.put("description", description != null ? description : "");
                            obj.put(entryChar + "_accountname", invoice.getCustomer().getName() != null ? invoice.getCustomer().getName() : "");
                        }
                        
                        /**
                         * Passing JE date in response for validating date on JS
                         * Side ERM-655.
                         */
                        obj.put("jeDate", entry.getEntryDate() != null ? authHandler.getDateOnlyFormat().format(entry.getEntryDate()) : "");
                        obj.put("jeDateinuserdf", entry.getEntryDate() != null ? userdf.format(entry.getEntryDate()) : ""); 
                        obj.put("currencysymbol", invoice.getCurrency() != null ? invoice.getCurrency().getSymbol() : currency.getSymbol());
                        obj.put("billid", invoice.getID());
                        obj.put("isOpeningTransaction", false);
                        obj.put("moduleid", Constants.Acc_Cash_Sales_ModuleId);
                        obj.put("paidto", "");
                        obj.put("type", "Cash Sale");
                    }
                    withoutinventory = false;

                } else if (transactionModuleId == Constants.Acc_Cash_Purchase_ModuleId) {
                    KwlReturnObject resultPayment = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), entry.getTransactionId());
                    GoodsReceipt invoice = (GoodsReceipt) resultPayment.getEntityList().get(0);
                    if (invoice != null) {
                        obj.put("transactionID", invoice.getGoodsReceiptNumber());
                        if (invoice.getPayDetail() != null && (invoice.getPayDetail().getCheque() != null)) {
                            obj.put("chequeno", invoice.getPayDetail().getCheque().getChequeNo());
                            obj.put("chequedate", invoice.getPayDetail().getCheque().getDueDate() != null ? authHandler.getDateOnlyFormat(request).format(invoice.getPayDetail().getCheque().getDueDate()) : "");
                            obj.put("chequedateinuserdf", invoice.getPayDetail().getCheque().getDueDate() != null ? userdf.format(invoice.getPayDetail().getCheque().getDueDate()) : "");
                            String description = invoice.getPayDetail().getCheque().getDescription();
                            obj.put("description", description != null ? description : "");
                            obj.put(entryChar + "_accountname", invoice.getVendor().getName() != null ? invoice.getVendor().getName() : "");
                        }

                        /**
                         * Passing JE date in response for validating date on JS
                         * Side ERM-655.
                         */
                        obj.put("jeDate", entry.getEntryDate() != null ? authHandler.getDateOnlyFormat().format(entry.getEntryDate()) : "");
                        obj.put("jeDateinuserdf", entry.getEntryDate() != null ? userdf.format(entry.getEntryDate()) : ""); 
                        obj.put("currencysymbol", invoice.getCurrency() != null ? invoice.getCurrency().getSymbol() : currency.getSymbol());
                        obj.put("billid", invoice.getID());
                        obj.put("isOpeningTransaction", false);
                        obj.put("moduleid", Constants.Acc_Cash_Purchase_ModuleId);
                        obj.put("paidto", "");
                        obj.put("type", Constants.CASH_PURCHASE);
                    }
                    withoutinventory = false;

                } else {
                    obj.put("chequeno", jed.getJournalEntry().getCheque() != null ? jed.getJournalEntry().getCheque().getChequeNo() : "");
                    obj.put("description", jed.getJournalEntry().getCheque() != null ? jed.getJournalEntry().getCheque().getDescription() : "");
                    obj.put("chequedate", jed.getJournalEntry().getCheque() != null ? (jed.getJournalEntry().getCheque().getDueDate() != null ? authHandler.getDateOnlyFormatter(request).format(jed.getJournalEntry().getCheque().getDueDate()) : "") : "");
                    obj.put("chequedateinuserdf", jed.getJournalEntry().getCheque() != null ? (jed.getJournalEntry().getCheque().getDueDate() != null ? userdf.format(jed.getJournalEntry().getCheque().getDueDate()) : "") : "");
                    obj.put("transactionID", "");
                    obj.put("currencysymbol", (jed.getJournalEntry().getCurrency() == null ? currency.getSymbol() : jed.getJournalEntry().getCurrency().getSymbol()));
                    /**
                     * Passing JE date in response for validating date on JS
                     * Side ERM-655.
                     */
                    obj.put("jeDate", jed.getJournalEntry().getEntryDate() != null ? authHandler.getDateOnlyFormat().format(jed.getJournalEntry().getEntryDate()) : "");
                    obj.put("jeDateinuserdf", jed.getJournalEntry().getEntryDate() != null ? userdf.format(jed.getJournalEntry().getEntryDate()) : "");
                }
                obj.put("withoutinventory", withoutinventory);

                if (obj.getString("d_accountname").contains(searchPayee)) { // Adding the record iff matched with searchpayee.
                    jArrL.put(obj);
                }
            }

            KwlReturnObject paymentResult = accBankReconciliationObj.getMachingOpeningBalancesForReconciliation(companyid, accountid, startDate, endDate, minAmount, maxAmount, false);
            List paymentList = paymentResult.getEntityList();
            Iterator paymentItr = paymentList.iterator();
            while (paymentItr.hasNext()) {
                Payment payment = null;
                payment = (Payment) paymentItr.next();
                currencyid = (payment.getCurrency() == null ? currency.getCurrencyID() : payment.getCurrency().getCurrencyID());
                transCurrSymbol = (payment.getCurrency() == null ? currency.getSymbol() : payment.getCurrency().getSymbol());
                transcurname = (payment.getCurrency() == null ? currency.getName() : payment.getCurrency().getName());
                double jedAmountInBase = payment.getOriginalOpeningBalanceBaseAmount();

                KwlReturnObject amountresult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, jedAmountInBase, accountcurrencyid, payment.getCreationDate(), 0);
                double amountInAccountCurrency = (Double) amountresult.getEntityList().get(0);
                String accountName = payment.getPayDetail() == null ? "" : payment.getPayDetail().getPaymentMethod() == null ? "" : payment.getPayDetail().getPaymentMethod().getAccount().getAccountName();

                JSONObject obj = new JSONObject();
                obj.put("isdebit", false);
                obj.put(entryChar + "_date", authHandler.getDateOnlyFormat(request).format(payment.getCreationDate()));
                obj.put(entryChar + "_dateinuserdf", userdf.format(payment.getCreationDate()));
                obj.put(entryChar + "_accountname", accountName);
                obj.put(entryChar + "_entryno", "");
                obj.put(entryChar + "_journalentryid", "");
                obj.put(entryChar + "_amountintransactioncurrency", authHandler.round(payment.getDepositAmount(), companyid));
                double baseamt = jedAmountInBase;
                obj.put(entryChar + "_amount", authHandler.round(baseamt, companyid));//this is amount in base crrency 
                if (currencyid.equals(accountcurrencyid)) {
                    obj.put(entryChar + "_amountinacc", authHandler.round(payment.getDepositAmount(), companyid));//this is amount in Account crrency for PDF
                } else {
                    double amtinAccCurr = amountInAccountCurrency;   //Amount in Account Currency
                    obj.put(entryChar + "_amountinacc", authHandler.round(amtinAccCurr, companyid));//this is amount in Account crrency for PDF
                }

                obj.put(entryChar + "_transCurrSymbol", transCurrSymbol);
                obj.put("accountcurrencysymbol", accountCurrSymbol);//Account currency syambol
                obj.put(emptyChar + "_reconciledate", "");
                obj.put(emptyChar + "_reconciledateinuserdf", "");
                obj.put(emptyChar + "_date", "");
                obj.put(emptyChar + "_dateinuserdf", "");
                obj.put(emptyChar + "_accountname", "");
                obj.put(emptyChar + "_entryno", "");
                obj.put(emptyChar + "_journalentryid", "");
                obj.put(emptyChar + "_amountintransactioncurrency", "");
                obj.put(emptyChar + "_amount", "");
                obj.put(emptyChar + "_amountinacc", "");

                if (payment != null) {
                    obj.put("transactionID", payment.getPaymentNumber());
                    if (payment.getPayDetail() != null && (payment.getPayDetail().getCheque() != null)) {
                        obj.put("chequeno", payment.getPayDetail().getCheque().getChequeNo());
                        obj.put("chequedate", payment.getPayDetail().getCheque().getDueDate() != null ? authHandler.getDateOnlyFormat(request).format(payment.getPayDetail().getCheque().getDueDate()) : "");
                        obj.put("chequedateinuserdf", payment.getPayDetail().getCheque().getDueDate() != null ? userdf.format(payment.getPayDetail().getCheque().getDueDate()) : "");
                        String description = payment.getPayDetail().getCheque().getDescription();
                        obj.put("description", description != null ? description : "");
                        Customer customer = null;
                        if (payment.getCustomer() != null) {
                            KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                            customer = (Customer) custResult.getEntityList().get(0);
                        }
                        Vendor vendor = payment.getVendor();
                        String vName = "";
                       // try {
                            if (vendor != null) {
                                vName = StringUtil.DecodeText(vendor.getName());
                            } else if (customer != null) {
                                vName = StringUtil.DecodeText(customer.getName());
                            } else {
                                KwlReturnObject result = accVendorPaymentobj.getPaymentVendorNames(companyid, payment.getID());
                                List vNameList = result.getEntityList();
                                Iterator vNamesItr = vNameList.iterator();
                                String vendorNames = "";
                                try {
                                    while (vNamesItr.hasNext()) {
                                        String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
                                        vendorNames += tempName;
                                        vendorNames += ",";
                                    }
                                    vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
                                } catch (Exception e) {
                                    throw ServiceException.FAILURE("getReconciliationData : " + e.getMessage(), e);
                                }
                                vName = StringUtil.DecodeText(vendorNames);
                            }
                       /* } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(accReportsController.class.getName()).log(Level.WARNING, ex.getMessage());
                        }*/
                        obj.put(entryChar + "_accountname", "Opening Balance for : " + vName);
                    }
                    obj.put("billid", payment.getID());
                    obj.put("isOpeningTransaction", true);
                    obj.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                    obj.put("type", Constants.PAYMENT_MADE);
                    obj.put("paidto", payment.getPaidTo() == null ? "" : payment.getPaidTo().getValue() == null ? "" : payment.getPaidTo().getValue());
                    obj.put("currencysymbol", payment.getCurrency() != null ? payment.getCurrency().getSymbol() : currency.getSymbol());
                }
                if (obj.getString("d_accountname").contains(searchPayee)) { // Adding the record iff matched with searchpayee.
                    jArrL.put(obj);
                }

            }

            KwlReturnObject receiptResult = accBankReconciliationObj.getMachingOpeningBalancesForReconciliation(companyid, accountid, startDate, endDate, minAmount, maxAmount, true);
            List<Receipt> receiptList = receiptResult.getEntityList();
            Iterator receiptItr = receiptList.iterator();
            while (receiptItr.hasNext()) {
                Receipt receipt = null;

                receipt = (Receipt) receiptItr.next();
                currencyid = (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID());
                transCurrSymbol = (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol());
                transcurname = (receipt.getCurrency() == null ? currency.getName() : receipt.getCurrency().getName());
                double jedAmountInBase = receipt.getDepositamountinbase();
                KwlReturnObject amountresult = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, jedAmountInBase, accountcurrencyid, receipt.getCreationDate(), 0);
                double amountInAccountCurrency = (Double) amountresult.getEntityList().get(0);
                String accountName = receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod() == null ? "" : receipt.getPayDetail().getPaymentMethod().getAccount().getAccountName();

                JSONObject obj = new JSONObject();
                obj.put("isdebit", true);
                obj.put(entryChar + "_date", authHandler.getDateOnlyFormat(request).format(receipt.getCreationDate()));
                obj.put(entryChar + "_dateinuserdf", userdf.format(receipt.getCreationDate()));
                obj.put(entryChar + "_accountname", accountName);
                obj.put(entryChar + "_entryno", "");
                obj.put(entryChar + "_journalentryid", "");
                obj.put(entryChar + "_amountintransactioncurrency", authHandler.round(receipt.getDepositAmount(), companyid));
                double baseamt = jedAmountInBase;
                obj.put(entryChar + "_amount", authHandler.round(baseamt, companyid));//this is amount in base crrency 
                if (currencyid.equals(accountcurrencyid)) {
                    obj.put(entryChar + "_amountinacc", authHandler.round(receipt.getDepositAmount(), companyid));//this is amount in Account crrency for PDF
                } else {
                    double amtinAccCurr = amountInAccountCurrency;   //Amount in Account Currency
                    obj.put(entryChar + "_amountinacc", authHandler.round(amtinAccCurr, companyid));//this is amount in Account crrency for PDF
                }

                obj.put(entryChar + "_transCurrSymbol", transCurrSymbol);
                obj.put("accountcurrencysymbol", accountCurrSymbol);//Account currency syambol
                obj.put(emptyChar + "_reconciledate", "");
                obj.put(emptyChar + "_reconciledateinuserdf", "");
                obj.put(emptyChar + "_date", "");
                obj.put(emptyChar + "_dateinuserdf", "");
                obj.put(emptyChar + "_accountname", "");
                obj.put(emptyChar + "_entryno", "");
                obj.put(emptyChar + "_journalentryid", "");
                obj.put(emptyChar + "_amountintransactioncurrency", "");
                obj.put(emptyChar + "_amount", "");
                obj.put(emptyChar + "_amountinacc", "");

                if (receipt != null) {
                    obj.put("transactionID", (receipt).getReceiptNumber());
                    if ((receipt).getPayDetail() != null && ((receipt).getPayDetail().getCheque() != null)) {
                        obj.put("chequeno", (receipt).getPayDetail().getCheque().getChequeNo());
                        obj.put("chequedate", receipt.getPayDetail().getCheque().getDueDate() != null ? authHandler.getDateOnlyFormatter(request).format(receipt.getPayDetail().getCheque().getDueDate()) : "");
                        obj.put("chequedateinuserdf", receipt.getPayDetail().getCheque().getDueDate() != null ? userdf.format(receipt.getPayDetail().getCheque().getDueDate()) : "");
                        String description = (receipt).getPayDetail().getCheque().getDescription();
                        obj.put("description", description != null ? description : "");

                        Vendor vendor = null;
                        if (receipt.getVendor() != null) {
                            KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                            vendor = (Vendor) custResult.getEntityList().get(0);
                        }
                        Customer customer = receipt.getCustomer();
                        String vName = "";
                       // try {
                            if (customer != null) {
                                vName = StringUtil.DecodeText(customer.getName());
                            } else if (vendor != null) {
                                vName =StringUtil.DecodeText(vendor.getName());
                            } else {
                                KwlReturnObject result = accReceiptDao.getReceiptCustomerNames(receipt.getCompany().getCompanyID(), receipt.getID());
                                List cNameList = result.getEntityList();
                                Iterator cNamesItr = cNameList.iterator();
                                String customerNames = "";
                                try {
                                    while (cNamesItr.hasNext()) {
                                        String tempName = URLEncoder.encode((String) cNamesItr.next(), "UTF-8");
                                        customerNames += tempName;
                                        customerNames += ",";
                                    }
                                    customerNames = customerNames.substring(0, Math.max(0, customerNames.length() - 1));
                                } catch (Exception e) {
                                    throw ServiceException.FAILURE("getReconciliationData : " + e.getMessage(), e);
                                }
                                vName = StringUtil.DecodeText(customerNames);
                            }
                        /*} catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(accReportsController.class.getName()).log(Level.WARNING, ex.getMessage());
                        }*/
                        obj.put(entryChar + "_accountname", "Opening Balance for : " + vName);
                    }
                    obj.put("billid", (receipt).getID());
                    obj.put("isOpeningTransaction", true);
                    obj.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                    obj.put("paidto", receipt.getReceivedFrom() == null ? "" : (receipt.getReceivedFrom().getValue()) == null ? "" : receipt.getReceivedFrom().getValue());
                    obj.put("type", Constants.PAYMENT_RECEIVED);
                    obj.put("currencysymbol", receipt.getCurrency() != null ? receipt.getCurrency().getSymbol() : currency.getSymbol());
                }
                if (obj.getString("d_accountname").contains(searchPayee)) { // Adding the record iff matched with searchpayee.
                    jArrL.put(obj);
                }
            }
            double balanceC_F = 0;  // Balance c/f
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("startdate", formatter.format(startDate));
            paramJobj.put("enddate", formatter.format(endDate));
            JSONObject bankBookObj = accReportsService.getLedger(paramJobj);
            JSONArray bankBookDataArray = bankBookObj.getJSONArray(Constants.RES_data);
            for (int i = 0; i < bankBookDataArray.length(); i++) {
                JSONObject bankBookData = bankBookDataArray.getJSONObject(i);
                if ((bankBookData.optString("d_accountname", "")).equals("Balance c/f") || (bankBookData.optString("c_accountname", "")).equals("Balance c/f")) {
                    balanceC_F = bankBookData.getString("balanceAmountAccountCurrency").equals("") ? 0 : Double.parseDouble(bankBookData.getString("balanceAmountAccountCurrency"));    //Future use, if need
                } 
            }
            jobj.put("data", jArrL);
            jobj.put("bankdatainacc", balanceC_F);
            jobj.put("currencysymbol", accountCurrSymbol);
        } catch (ParseException | JSONException ex) {
            throw ServiceException.FAILURE("getReconciliationData : " + ex.getMessage(), ex);
        }
        return jobj;
    }    
    
    public ModelAndView getWidgetReportList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        JSONArray jarr = new JSONArray();
        try {
            jarr = getWidgetReportList(request);
            if (jarr.length() > 0) {
                jobj.put("data", jarr);
                jobj.put("count", jarr.getJSONObject(0).get("count"));
                issuccess = true;
            } else {
                jobj.put("data", jarr);
                jobj.put("count", jarr.length());
                msg = "No report are added in to widget view.";
                issuccess = false;
            }
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getWidgetReportList(HttpServletRequest request) throws ServiceException, ParseException, JSONException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        KwlReturnObject result;
        int check = 0;
        String id = "";
        String name = "";
        String desc = "";
        String methodName = "";
        String groupedunder = "";
        String widgetURL = "";
        String helpText = "";
        String companyId = sessionHandlerImpl.getCompanyid(request);
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", companyId);
            result = accInvoiceDAOobj.getReportsForWidgets(requestParams);

            Iterator iter = result.getEntityList().iterator();
            while (iter.hasNext()) {
                Object[] oj = (Object[]) iter.next();
                String productid = oj[0].toString();
                KwlReturnObject reportM = accountingHandlerDAOobj.getObject(ReportMaster.class.getName(), productid);
                List<ReportMaster> prd = reportM.getEntityList();
                id = prd.get(0).getID();
                name = prd.get(0).getName();
                desc = prd.get(0).getDescription();
                methodName = prd.get(0).getMethodName();
                groupedunder = prd.get(0).getGroupedUnder();
                widgetURL = prd.get(0).getWidgetURL();  
                helpText = prd.get(0).getHelpText(); 
                JSONObject obj = new JSONObject();
                if (check == 0) {
                    obj.put("count", result.getRecordTotalCount());
                    check = 1;
                }
                obj.put("id", id);
                obj.put("name", name);
                obj.put("description", desc);
                obj.put("methodName", methodName);
                obj.put("groupedunder", groupedunder);
                obj.put("widgeturl", widgetURL);
                obj.put("helptext", helpText);
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("accOtherReportsController.getWidgetReportList : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accOtherReportsController.getWidgetReportList : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public ModelAndView getYearlyTradingAndProfitLoss(HttpServletRequest request, HttpServletResponse response) throws ServiceException, ParseException, JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String view = "jsonView";
        boolean issuccess = false;
        try {
            //flag for chart
            String chartType = "";
            if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.CHART_TYPE))) {
                chartType = request.getParameter(Constants.CHART_TYPE);
            }
            if (request.getParameter("singleGrid") != null && Boolean.parseBoolean(request.getParameter("singleGrid").toString())) {
                request.setAttribute("isForTradingAndProfitLoss", true);
                request.setAttribute("isMonthlyOrYearlyPNL", true);// this flag is sending to to do montly and yearly specific changes in common used function.
                JSONObject fobj1 = accFinancialReportsService.getYearlyTradingProfitLossJasperExport(request, true);
                JSONObject fobj = new JSONObject();
                JSONArray jArrL = fobj1.getJSONArray("refleft");
                JSONArray jArrR = fobj1.getJSONArray("refright");
                JSONArray array = fobj1.getJSONArray("months");
                JSONObject monthArrayObject = new JSONObject();
                monthArrayObject.put("months", array);
                jArrL.put(monthArrayObject);
                jArrR.put(monthArrayObject);
                fobj.put("left", jArrL);
                fobj.put("right", jArrR);
                jobj.put("data", fobj);
                jobj.put("monthCount", array.length());
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//                jobj = accFinancialReportsService.getNewMonthlyMYOBtradingreport(request, jobj, false);
                
                if (StringUtil.equalIgnoreCase(chartType, Constants.LINE_CHART) && paramJobj.optBoolean("isMonthlyOrYearlyPNL", false)) {
                    jobj = accFinancialReportsService.getMonthlyYearlyTradingProfitAndLossChartJSON(paramJobj, jobj);
                } else {
                    jobj = accFinancialReportsService.getNewMonthlyMYOBtradingreport(paramJobj, jobj, false);
                }
                
                boolean isWidgetRequest = request.getParameter("isWidgetRequest") != null ? Boolean.parseBoolean(request.getParameter("isWidgetRequest")) : false;
                if (isWidgetRequest) {
                    view = "jsonView_ex";
                    JSONObject commData = new JSONObject();
                    JSONObject jMeta = new JSONObject();
                    Map<String, Object> requestParamsForCM = new HashMap<>();
                    requestParamsForCM.put("headers", array);
                    Map<String, JSONArray> returnMap = accInvoiceServiceDAO.getColumnModuleForMonthlyTradingAndProfitLossReport(requestParamsForCM);
                    JSONArray jarrRecords = returnMap.get("record");
                    JSONArray jarrColumns = returnMap.get("columns");
                    commData.put("success", true);
                    jMeta.put("totalProperty", "totalCount");
                    jMeta.put("root", "coldata");
                    jMeta.put("fields", jarrRecords); //Record Array
                    JSONArray temp = null;
                    if(jobj.getJSONObject("data")!=null){
                        temp = jobj.getJSONObject("data").getJSONArray("left");
                    }
                    commData.put("coldata", temp.put(monthArrayObject));  //Actual data
                    commData.put("columns", jarrColumns); //Column Module Array
                    commData.put("totalCount", jobj.length());
                    commData.put("metaData", jMeta);

                    jobj.put("valid", true);
                    jobj.put("data", commData);
                }
                
            } 
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    public ModelAndView getSalesCommissionReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            params.put("df", df);
            params.put("locale", RequestContextUtils.getLocale(request));
            /*
             Below functions are used to return commssion data
             */
            int reporttype=Integer.parseInt(request.getParameter("ReportType"));
            switch(reporttype){
                /**
                 * Margin
                 */
                case 1 : jobj = accOtherReportsService.getSalesCommission(params);
                         break;
                /**
                 * Amount
                 */    
                case 2 : jobj = accOtherReportsService.getAmountSalesCommission(params);
                         break;
                /**
                 * Payment Term
                 */   
                case 3 : jobj = accOtherReportsService.getPaymentTermCommission(params);
                         break;
                /**
                 * Brand
                 */    
                case 4 : jobj = accOtherReportsService.getBrandCommission(params);
                         break;
            }
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView getProfitabilityReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to return commssio data
             */
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            params.put("df", df);
            params.put("locale", RequestContextUtils.getLocale(request));
            jobj = accOtherReportsService.getSalesCommission(params);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    public ModelAndView ExportSalesCommission(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        String view = "jsonView_ex";
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            params.put("df", df);
            params.put("locale", RequestContextUtils.getLocale(request));
            params.put("isExport", true);
            int reporttype=Integer.parseInt(request.getParameter("ReportType"));
            switch(reporttype){
                /**
                 * Margin
                 */
                case 1 : jobj = accOtherReportsService.getSalesCommission(params);
                         break;
                /**
                 * Amount
                 */    
                case 2 : jobj = accOtherReportsService.getAmountSalesCommission(params);
                         break;
                /**
                 * Payment Term
                 */   
                case 3 : jobj = accOtherReportsService.getPaymentTermCommission(params);
                         break;
                /**
                 * Brand
                 */    
                case 4 : jobj = accOtherReportsService.getBrandCommission(params);
                         break;
            }
            DataJArr = (JSONArray) jobj.optJSONArray("data");
            request.setAttribute("isExport", true);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * importBankReconciliationFromBank method is called when we import from
     * bank in bank reconciliation screen.It is calls the rest method to get
     * data from Bank and iterates the list and finds the matching MP or RP with
     * it from the system.ERP-41871.
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView importBankReconciliationFromBank(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            JSONArray dataJArr = new JSONArray();
            JSONArray matchedRecordsReturnJarr = new JSONArray();
            boolean issuccess = true;
//            List<BankTransactionDetails> list = getBankAccountTransactionsFromBank(requestJobj);            //call shubham function
            List<BankTransactionDetails> list = new ArrayList<>();
            BankTransactionDetails arr[] = new BankTransactionDetails[4];
            arr[0] = new BankTransactionDetails(new Date(), 2000, "V1", "descrip1", "CH00001", "CH00001");
            arr[1] = new BankTransactionDetails(new Date(), 1000, "V2", "descrip2", "CH00002", "CH00002");
            arr[2] = new BankTransactionDetails(new Date(), 1000, "C1", "descrip3", "RP00001", "RP00001");
            arr[3] = new BankTransactionDetails(new Date(), 1000, "C1", "descrip3", "RP00002", "RP00002");
            list.addAll(Arrays.asList(arr));
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                BankTransactionDetails bankReconciliationRest = (BankTransactionDetails) itr.next();
                DateFormat df = authHandler.getDateOnlyFormat();
                JSONObject obj = new JSONObject();
                obj.put("date", df.format(bankReconciliationRest.getDate()));
                obj.put("amount", bankReconciliationRest.getAmount());
                obj.put("payee", bankReconciliationRest.getPayee());
                obj.put("desc", bankReconciliationRest.getDescription());
                obj.put("reference", bankReconciliationRest.getReference());
                obj.put("chequenumber", bankReconciliationRest.getChequeNumber());
                request.setAttribute("isBankImport", obj);
                JSONObject matchedRecordsJobj = getMatchedReconciliationData(request);
                JSONArray matchedRecordsArr = matchedRecordsJobj.optJSONArray("data");
                /**
                 * if only one matched record is found then we need to auto add
                 * that record in final grid in Import from Bank, Bank
                 * reconciliation screen if there are more then one match then
                 * we need to only add that record in left grid on bank
                 * reconciliation screen hence created 2 JSONArrays
                 * (matchedRecordsReturnJarr : for matching records and dataJArr
                 * : for unmatched records). ERP-41871
                 */
                if (matchedRecordsArr.length() == 1) {
                    JSONObject matchedRecordJobj = matchedRecordsArr.optJSONObject(0);
                    Iterator itr1 = obj.keys();
                    while (itr1.hasNext()) {
                        String key = (String) itr1.next();
                        matchedRecordJobj.put(key, obj.opt(key));
                    }
                    matchedRecordsReturnJarr.put(matchedRecordJobj);
                } else {
                    dataJArr.put(obj);
                }
            }
            JSONObject matchedRecordJobj = new JSONObject();
            matchedRecordJobj.put("data", matchedRecordsReturnJarr);
            jobj.put("data", dataJArr);
            jobj.put("matchedRecord", matchedRecordJobj);
            jobj.put("success", issuccess);
            jobj.put("msg", "success");
            jobj.put("totalrecords", list.size());
            jobj.put("successrecords", list.size());
            jobj.put("failedrecords", 0);
            jobj.put("Module", Constants.Bank_Reconciliation_ModuleId);
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException jex) {
                Logger.getLogger(accBankReconciliationController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
