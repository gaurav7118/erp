/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.gst;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.*;
import com.krawler.spring.accounting.handler.*;
import com.krawler.spring.accounting.invoice.*;
import com.krawler.spring.accounting.journalentry.*;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.reports.AccFinancialReportsService;
import com.krawler.spring.accounting.reports.AccReportsService;
import com.krawler.spring.accounting.tax.TaxConstants;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import static com.krawler.spring.authHandler.authHandler.getDateOnlyFormatPattern;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import com.sun.jersey.core.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.w3c.dom.*;

/**
 *
 * @author krawler
 */
public class AccGstServiceImpl extends BaseDAO implements AccGstService {

    private AccGstDAO accGstDAO;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private AccountingDashboardDAO accountingDashboardDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accJournalEntryDAO accJournalEntryDAOobj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private accInvoiceCMN accInvoiceCommon;
    private accVendorPaymentDAO accVendorPaymentDAOobj;
    private accTaxDAO accTaxDAOobj;
    private AccReportsService accReportsService;
    private accReceiptDAO accReceiptDAOobj;
    private accAccountDAO accAccountDAOobj;
    private AccFinancialReportsService accFinancialReportsService;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private exportMPXDAOImpl exportDAOObj;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }   

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
   

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setAccGstDAO(AccGstDAO accGstDAO) {
        this.accGstDAO = accGstDAO;
    }

    public void setAccInvoiceDAO(accInvoiceDAO invoiceDAO) {
        this.accInvoiceDAOobj = invoiceDAO;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setAccountingDashboardDAOObj(AccountingDashboardDAO accountingDashboardDAOObj) {
        this.accountingDashboardDAOObj = accountingDashboardDAOObj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryDAOobj = accJournalEntryobj;
    }
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }
    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentDao) {
        this.accVendorPaymentDAOobj = accVendorPaymentDao;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxDAOobj = accTaxObj;
    }
    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDao) {
        this.accReceiptDAOobj = accReceiptDao;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setAccFinancialReportsService(AccFinancialReportsService accFinancialReportsService) {
        this.accFinancialReportsService = accFinancialReportsService;
    }
    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDAOObj) {
        this.exportDAOObj = exportDAOObj;
    }
        
    @Override
    public JSONObject getGSTFormGenerationHistoryConfig(Map<String, Object> requestParams) {
        JSONObject jSONObject = new JSONObject();
        try {
            JSONObject jobjTemp = new JSONObject();
            
            boolean isMultiEntity  = false;
            if(requestParams.containsKey(Constants.isMultiEntity) && requestParams.get(Constants.isMultiEntity) !=null){
                isMultiEntity = Boolean.parseBoolean((String) requestParams.get(Constants.isMultiEntity));
            }

            JSONArray records = new JSONArray();
            JSONArray jarrColumns = new JSONArray();
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "id");
            records.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "personname");
            records.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "userid");
            records.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "generationdate");
            jobjTemp.put("type", "date");
            records.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "startdate");
            jobjTemp.put("type", "date");
            records.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "enddate");
            jobjTemp.put("type", "date");
            records.put(jobjTemp);
            /*
             * Entity column is added If Multi Entity is activated
             */
            if (isMultiEntity) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", "entity");
                records.put(jobjTemp);
            }
            
            if (isMultiEntity) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", "entityid");
                records.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "filename");
            records.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Name");
            jobjTemp.put("dataIndex", "personname");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Generation Date");
            jobjTemp.put("align", "center");
            jobjTemp.put("dataIndex", "generationdate");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("renderer", "WtfGlobal.onlyDateDeletedRenderer");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Start Date");
            jobjTemp.put("dataIndex", "startdate");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("renderer", "WtfGlobal.onlyDateDeletedRenderer");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "End Date");
            jobjTemp.put("align", "center");
            jobjTemp.put("dataIndex", "enddate");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("renderer", "WtfGlobal.onlyDateDeletedRenderer");
            jarrColumns.put(jobjTemp);
            /*
            * Entity column is added If Multi Entity is activated
            */
            if (isMultiEntity) {
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "Entity");
                jobjTemp.put("dataIndex", "entity");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }
//            

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Download Submission File");
            jobjTemp.put("align", "center");
            jobjTemp.put("dataIndex", "filename");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
//            
            jSONObject.put("columns", jarrColumns);
            jSONObject.put("records", records);
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONObject;
    }

    @Override
    public JSONObject getGSTFormGenerationHistoryData(Map<String, Object> requestMap) {
        JSONObject jSONObject = new JSONObject();
        try {
            DateFormat df = (DateFormat) requestMap.get(Constants.df);
            KwlReturnObject result = accGstDAO.getGstFormGenerationHistory(requestMap);
            List<GstFormGenerationHistory> list = result.getEntityList();
            int count=0;
            count=result.getRecordTotalCount();
            JSONArray dataArray = new JSONArray();
            if (list != null && !list.isEmpty()) {
                for (GstFormGenerationHistory gstFormGenerationHistory : list) {
                    JSONObject json = new JSONObject();
                    json.put("id", gstFormGenerationHistory.getID());
                    json.put("personname", StringUtil.getFullName(gstFormGenerationHistory.getUser()));
                    json.put("userid", gstFormGenerationHistory.getUser() != null ? gstFormGenerationHistory.getUser().getUserID() : "");
                    json.put("generationdate", gstFormGenerationHistory.getGenerationDate() != null ? df.format(gstFormGenerationHistory.getGenerationDate()) : "");
                    json.put("startdate", gstFormGenerationHistory.getStartDate() != null ? df.format(gstFormGenerationHistory.getStartDate()) : "");
                    json.put("enddate", gstFormGenerationHistory.getEndDate() != null ? df.format(gstFormGenerationHistory.getEndDate()) : "");
                    json.put("entity", gstFormGenerationHistory.getEntityMapping() != null ? gstFormGenerationHistory.getEntityMapping().getMultiEntity().getValue() : "");
                    json.put("entityid", gstFormGenerationHistory.getEntityMapping() != null ? gstFormGenerationHistory.getEntityMapping().getMultiEntity().getId() : "");
                    json.put("filename", gstFormGenerationHistory.getFileName() != null ? gstFormGenerationHistory.getFileName() : "");
                    dataArray.put(json);
                }
            }
            jSONObject.put("data", dataArray);
            jSONObject.put("count", count);
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONObject;
    }

    @Override
    public void exportAndSaveGSTFormGenerationHistory(ByteArrayOutputStream outputStream, Map<String, Object> requestMap) {
        try {
            Date generationDate = new Date();
            DateFormat df = (DateFormat) requestMap.get(Constants.df);
            String userid = (requestMap.containsKey(Constants.userid) && requestMap.get(Constants.userid) != null) ? requestMap.get(Constants.userid).toString() : "";
            String destinationDirectory = storageHandlerImpl.GetDocStorePath();
            SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
            String fileName = "Submission_" + (sdfTemp.format(generationDate)).toString() + ".pdf";
            File directory = new File(destinationDirectory + Constants.GST_SUBMISSIONFILE_STORAGE_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(destinationDirectory + Constants.GST_SUBMISSIONFILE_STORAGE_PATH + File.separator + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(outputStream.toByteArray());
            fos.close();
            requestMap.put("fileName", fileName);
            requestMap.put("generationDate", generationDate);
            accGstDAO.saveGstFormGenerationHistory(requestMap);
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
    }

    
    @Override
    public JSONObject getValidDateRangeForFileGeneration(Map<String,Object> requestParams) throws ServiceException {
        JSONObject returnObj = new JSONObject();
        try {
            
            String companyId = (String) requestParams.get(Constants.companyid);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String multiEntityId = (String) (requestParams.containsKey(Constants.multiEntityId) ? requestParams.get(Constants.multiEntityId) : "");
            String multiEntityValue = (String) (requestParams.containsKey(Constants.multiEntityValue) ? requestParams.get(Constants.multiEntityValue) : "");
            
            Map map = new HashMap();
            map.put("companyid", companyId);
            map.put("searchForMaxStartDate", true);
            if (!StringUtil.isNullOrEmpty(multiEntityId)) {
                map.put(Constants.multiEntityId, multiEntityId);
            }
            if (!StringUtil.isNullOrEmpty(multiEntityValue)) {
                map.put(Constants.multiEntityValue, multiEntityValue);
            }
            KwlReturnObject result = accGstDAO.getGstFormGenerationHistory(map);
            List list = result.getEntityList();

//            result = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
//            ExtraCompanyPreferences extraCom = (ExtraCompanyPreferences) result.getEntityList().get(0);
//            int submissionCriteria = extraCom.getGstSubmissionPeriod();

            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("id", companyId);
            Object[] preferencesArray = (Object[]) kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"gstSubmissionPeriod", "gstEffectiveDate", "isMultiEntity"}, paramsMap);
            int submissionCriteria = (int) preferencesArray[0];
            boolean isMultiEntity = (boolean) preferencesArray[2];
            if (!StringUtil.isNullOrEmpty(multiEntityValue) && isMultiEntity) {
                KwlReturnObject entityMappingObj = accGstDAO.getEntityDetails(map);//To get MultiEntityMapping Object from multiEntityValue(Master Item value)
                MultiEntityMapping entityMapping = (MultiEntityMapping) entityMappingObj.getEntityList().get(0);
                submissionCriteria = entityMapping.getGstSubmissionPeriod();
            }
            if (list != null && !list.isEmpty()) {
                GstFormGenerationHistory history = (GstFormGenerationHistory) list.get(0);
                Date starDate = history.getStartDate();
                Date endDate = history.getEndDate();
                returnObj = getDateRange(starDate, endDate, submissionCriteria, df);

            } else {
//                result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
//                Company company = (Company) result.getEntityList().get(0);
//
//                result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
//                CompanyAccountPreferences com = (CompanyAccountPreferences) result.getEntityList().get(0);
//
//                Date gstActivationDate = extraCom.getGstEffectiveDate();
//                
//                Date bookBeginDate = com.getBookBeginningFrom();
                
                paramsMap = new HashMap<>();
                paramsMap.put("ID", companyId);
                
                Date gstActivationDate = (Date) preferencesArray[1];
                Date bookBeginDate = (Date) kwlCommonTablesDAOObj.getRequestedObjectFields(CompanyAccountPreferences.class, new String[]{"bookBeginningFrom"}, paramsMap);
                Date date = new Date();
                if (gstActivationDate != null) {
                    if (bookBeginDate.after(gstActivationDate)) {
                        date = bookBeginDate;
                    } else {
                        date = gstActivationDate;
                    }
                } else {
                    date = bookBeginDate;
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.DATE, 1);
                date = cal.getTime();
                returnObj.put("startDate", df.format(date));
                if (submissionCriteria == Constants.GST_Monthly_Submission) {
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                } else {
                    cal.add(Calendar.MONTH, 2);
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                }
                date = cal.getTime();
                returnObj.put("endDate", df.format(date));

            }
        } catch (com.krawler.utils.json.base.JSONException | ServiceException | JSONException ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccGstServiceImpl.getValidDateRangeForFileGeneration:" + ex.getMessage(), ex);
        }
        return returnObj;
    }
/**
 * Function to Return Valid date period for export tap file
 * @param requestParams
 * @return
 * @throws ServiceException 
 */
    @Override
    public JSONObject getValidDateRangeForTAPFileGeneration(Map<String, Object> requestParams) throws ServiceException {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        boolean isInvalidDate = false;
        boolean success = true;
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String language=requestParams.get(Constants.language)!=null?(String)requestParams.get(Constants.language):"";
            Date startdate = null;
            Date enddate = null;
            Calendar cal = Calendar.getInstance();
            Date date = new Date();
            String companyId = "";
            String multiEntityValue = "";
            if (requestParams.containsKey("startdate") && requestParams.get("startdate") != null) {
                startdate = df.parse(requestParams.get("startdate").toString());
            }
            if (requestParams.containsKey("enddate") && requestParams.get("enddate") != null) {
                enddate = df.parse(requestParams.get("enddate").toString());
            }
            if (requestParams.containsKey(Constants.companyid) && requestParams.get(Constants.companyid) != null) {
                companyId = (String) requestParams.get(Constants.companyid);
            }
            if (requestParams.containsKey(Constants.multiEntityValue) && requestParams.get(Constants.multiEntityValue) != null) {
                multiEntityValue = (String)requestParams.get(Constants.multiEntityValue);
            }

            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("id", companyId);
            Object[] result = (Object[]) kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"gstSubmissionPeriod", "gstEffectiveDate", "isMultiEntity"}, paramsMap);
            Date bookBeginningdate = (Date) kwlCommonTablesDAOObj.getRequestedObjectFields(CompanyAccountPreferences.class, new String[]{"bookBeginningFrom"}, paramsMap);
            int submissionCriteria = result[0] != null ? (int) result[0] : 0; //submission Criteria 0 for Monthly and 1 for Quarterly
            Date GSTActivationDate = (Date) result[1]; // GST Activation date

            boolean isMultiEntity = result[2] != null ? (boolean) result[2] : false;
            if (!StringUtil.isNullOrEmpty(multiEntityValue) && isMultiEntity) {
                /**
                 * To get MultiEntityMapping Object from multiEntityValue(Master
                 * Item value)
                 */
                KwlReturnObject entityMappingObj = accGstDAO.getEntityDetails(requestParams);
                MultiEntityMapping entityMapping = (MultiEntityMapping) entityMappingObj.getEntityList().get(0);
                submissionCriteria = entityMapping.getGstSubmissionPeriod();
            }       
             /**
              * Check end date before GstActivationDate if true then throw exception 
              */       
            if (GSTActivationDate != null && enddate.before(GSTActivationDate)) {
                isInvalidDate = true;
                throw ServiceException.FAILURE(messageSource.getMessage("acc.gst.tap.daterange.alert", null, Locale.forLanguageTag(language)), "", false);
            }
            cal.setTime(enddate); //set end date to calender 
            if (submissionCriteria == Constants.GST_Monthly_Submission) { //if monthly
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
                startdate = cal.getTime(); // 1st day of month
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                enddate = cal.getTime(); // last day of month
            } else {// submissionCriteria == 1 (Quarterly)
                if (GSTActivationDate != null) {
                    if (bookBeginningdate.after(GSTActivationDate)) {
                        date = bookBeginningdate;
                    } else {
                        date = GSTActivationDate;
                    }
                } else {
                    date = bookBeginningdate;
                }

                cal.setTime(date);//set compare date to calender
                cal.set(cal.DAY_OF_MONTH, cal.getActualMinimum(cal.DAY_OF_MONTH));
                while (enddate.after(date)) {
                    cal.add(cal.MONTH, 2);
                    cal.set(cal.DAY_OF_MONTH, cal.getActualMaximum(cal.DAY_OF_MONTH));
                    cal.add(cal.DATE, 1);
                    if (enddate.after(cal.getTime()) || enddate.equals(cal.getTime())) { // check endate afer next quarter 
                        date = cal.getTime();
                    } else {
                        break;
                    }
                }
                cal.setTime(date);
                cal.set(cal.DAY_OF_MONTH, cal.getActualMinimum(cal.DAY_OF_MONTH));
                startdate = cal.getTime(); //start date

                cal.add(cal.MONTH, 2); //start date+ 2 months = 1 Quarter
                cal.set(cal.DAY_OF_MONTH, cal.getActualMaximum(cal.DAY_OF_MONTH));
                enddate = cal.getTime(); //end date
            }

            returnObj.put("startdate", df.format(startdate));
            returnObj.put("enddate", df.format(enddate));
            returnObj.put("submissionCriteria", submissionCriteria);

        } catch (Exception ex) {
            msg = ex.getMessage();
            success = false;
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                returnObj.put("msg", msg);
                returnObj.put("success", success);
                returnObj.put("isinvaliddate", isInvalidDate);
            } catch (Exception ex) {
            }
        }
        return returnObj;
    }
    
    public JSONObject getDateRange(Date startDate, Date endDate, int submissionCriteria, DateFormat df) {
        JSONObject jObj = new JSONObject();
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        try {
            cal.setTime(endDate);
            cal.add(Calendar.DATE, 1);
            date = cal.getTime();
            jObj.put("startDate", df.format(date));
            cal.setTime(date);
            if (submissionCriteria == Constants.GST_Monthly_Submission) {
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            } else if (submissionCriteria == Constants.GST_Quarterly_Submission) {
                cal.add(Calendar.MONTH, 2);
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            date = cal.getTime();
            jObj.put("endDate", df.format(date));
        } catch (com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jObj;
    }

    @Override
    public boolean checkForClaimableSalesInvoices(HashMap<String, Object> requestParams) {
        boolean invoiceExists = false;
        try {
            KwlReturnObject result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
            List list = result.getEntityList();
            if (list != null && !list.isEmpty()) {
                invoiceExists = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return invoiceExists;
    }

    @Override
    public boolean checkForClaimablePurchaseInvoices(HashMap<String, Object> requestParams) {
        boolean invoiceExists = false;
        try {
            KwlReturnObject result = accGoodsReceiptobj.getGoodsReceiptsMerged(requestParams);
            List list = result.getEntityList();
            if (list != null && !list.isEmpty()) {
                invoiceExists = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return invoiceExists;
    }

    @Override
    public boolean checkForUnInvoicedDOs(Map<String, Object> requestMap) {
        boolean isDOPresent = false;
        try {
            HashMap<String, Object> map = new HashMap();
            DateFormat df = (DateFormat) requestMap.get(Constants.df);
            map.put("isForTaxApplication", true);
            map.put("isTaxAppliedDOs", false);
            map.put(Constants.companyKey, requestMap.get(Constants.companyKey));
            map.put(Constants.df, requestMap.get(Constants.df));
            if (requestMap.containsKey("enddate") && requestMap.get("enddate") != null) {
                map.put("taxApplicableCalculationDate", df.parse(requestMap.get("enddate").toString()));
            }
            KwlReturnObject result = accInvoiceDAOobj.getDeliveryOrdersMerged(map);
            List list = result.getEntityList();
            if (list != null && !list.isEmpty()) {
                isDOPresent = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return isDOPresent;
    }
    
    /**
     *
     * @param requestParams
     * @return List
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject saveEntityMapping(Map<String, Object> requestParams) throws ServiceException{
        KwlReturnObject result = null;
        try{
            JSONObject dataJobj = new JSONObject(requestParams.get("data").toString());
            JSONArray dataArr = dataJobj.getJSONArray("data");
            JSONObject jObj = dataArr.getJSONObject(0);

            if (!StringUtil.isNullOrEmpty(jObj.optString("id"))) {
                requestParams.put("id", jObj.getString("id"));
            }
            if (!StringUtil.isNullOrEmpty(jObj.optString("multiEntity"))) {
                requestParams.put("multiEntity", jObj.getString("multiEntity"));
            }
            if (!StringUtil.isNullOrEmpty(jObj.optString("multiEntityId"))) {
                requestParams.put("multiEntityId", jObj.getString("multiEntityId"));
            }
            if (!StringUtil.isNullOrEmpty(jObj.optString("multiEntitygstno"))) {
                requestParams.put("multiEntitygstno", jObj.getString("multiEntitygstno"));
            }
            if (!StringUtil.isNullOrEmpty(jObj.optString("multiEntitytaxNumber"))) {
                requestParams.put("multiEntitytaxNumber", jObj.getString("multiEntitytaxNumber"));
            }
            if (!StringUtil.isNullOrEmpty(jObj.optString("multiEntitycompanybrn"))) {
                requestParams.put("multiEntitycompanybrn", jObj.getString("multiEntitycompanybrn"));
            }
            requestParams.put("industryCodeId", jObj.getString("industryCodeId"));
            requestParams.put("gstSubmissionPeriod", jObj.optInt("gstSubmissionPeriod"));
            result  = accGstDAO.saveEntityMapping(requestParams);
            
        }catch(com.krawler.utils.json.base.JSONException | ServiceException ex){
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
            throw ServiceException.FAILURE("AccGstServiceImpl.saveEntityMapping:" + ex.getMessage(), ex);
        }
        
        return new KwlReturnObject(true, null, null, result.getEntityList(), result.getRecordTotalCount());
    }
    
    /**
     *
     * @param requestParams
     * @return :KwlReturnObject of Multi Entity Details
     * @throws ServiceException
     */
    @Override
    public JSONObject getEntityDetails(Map<String, Object> requestParams) throws ServiceException{
        JSONObject jSONObject = new  JSONObject();
        JSONArray dataJArr = new JSONArray();
        String start = "", limit = "";
        try{
            if(requestParams.containsKey(Constants.start)){
                start = (String) requestParams.get(Constants.start);
            }
            if(requestParams.containsKey(Constants.limit)){
                limit = (String) requestParams.get(Constants.limit);
            }
            KwlReturnObject result = accGstDAO.getEntityDetails(requestParams);
            List<MultiEntityMapping> list = result.getEntityList();
            
            for(MultiEntityMapping entityMapping : list){
                JSONObject jobj = new JSONObject();
                jobj.put("id", entityMapping.getId());
                jobj.put("multiEntityid", entityMapping.getMultiEntity().getId());
                jobj.put("multiEntity", entityMapping.getMultiEntity().getValue());
                jobj.put("multiEntitygstno", entityMapping.getGstNumber());
                jobj.put("multiEntitytaxNumber", entityMapping.getTaxNumber());
                jobj.put("multiEntitycompanybrn", entityMapping.getCompanyBRN());
                jobj.put("industryCodeValue", entityMapping.getIndustryCode()!=null?entityMapping.getIndustryCode().getValue():"");
                jobj.put("industryCodeId", entityMapping.getIndustryCode()!=null?entityMapping.getIndustryCode().getID():"");
                jobj.put("gstSubmissionPeriod", entityMapping.getGstSubmissionPeriod());
                dataJArr.put(jobj);
            }
            JSONArray pagedJson = dataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jSONObject.put("data", pagedJson);
            jSONObject.put("count", dataJArr.length());
            jSONObject.put("success", result.isSuccessFlag());
        }catch(Exception ex){
            throw ServiceException.FAILURE("AccGstServiceImpl.saveEntityMapping:" + ex.getMessage(), ex);
        }
        return jSONObject;
    }
    
    /**
     *
     * @param requestParams
     * @return :JSON Object of Multi Entity Details
     * @throws ServiceException
     */
    @Override
    public JSONObject getMultiEntityForCombo(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jObj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        List<FieldComboData> list = null;
        try {
            list = accGstDAO.getMultiEntityForCombo(requestParams);

            for (FieldComboData data : list) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", data.getId());
                jsonObject.put("name", data.getValue());
                jsonObject.put("fieldid",data.getFieldid());
                dataJArr.put(jsonObject);
            }
            jObj.put("data", dataJArr);
            jObj.put("count", dataJArr.length());

        } catch (ServiceException | com.krawler.utils.json.base.JSONException ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jObj;
    }
    
    /**
     *
     * @param requestParams
     * @throws ServiceException
     * 
     */
    @Override
    public KwlReturnObject deleteEntityMapping(Map<String, Object> requestParams) throws ServiceException{
        String entityName = ""; 
        try{
            JSONArray jSONArray = new JSONArray((String) requestParams.get("data"));
            for(int i=0;i<jSONArray.length();i++){
                JSONObject jObj = jSONArray.getJSONObject(i);
                String entityMapId = jObj.optString("id");
                entityName = jObj.optString("multiEntity");
                requestParams.put("id", entityMapId);
                accGstDAO.deleteEntityMapping(requestParams);
            }
        }catch(com.krawler.utils.json.base.JSONException | ServiceException ex){
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, entityName, null, null, 1);
    }
    
    /**
     *
     * @param requestParams
     * 
     */
    @Override
    public JSONObject getLatestDateOfFileGeneration(Map<String, Object> requestMap) {
        JSONObject jSONObject = new JSONObject();
        try {
            DateFormat df = (DateFormat) requestMap.get(Constants.df);
            KwlReturnObject result = accGstDAO.getGstFormGenerationHistory(requestMap);
            List<GstFormGenerationHistory> list = result.getEntityList();
            GstFormGenerationHistory gstFormGenerationHistory = null;
            if (list != null && !list.isEmpty()) {
                gstFormGenerationHistory = list.get(0);
                jSONObject.put("id", gstFormGenerationHistory.getID());
                jSONObject.put("generationdate", gstFormGenerationHistory.getGenerationDate() != null ? df.format(gstFormGenerationHistory.getGenerationDate()) : "");
                jSONObject.put("startdate", gstFormGenerationHistory.getStartDate() != null ? df.format(gstFormGenerationHistory.getStartDate()) : "");
                jSONObject.put("enddate", gstFormGenerationHistory.getEndDate() != null ? df.format(gstFormGenerationHistory.getEndDate()) : "");
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONObject;
    }
    
    /**
     * This function returns entity industry code mapping for GST form 5 export
     * @param requestParams
     * @return :Map<EntityName,MSIC_Code>
     * @throws ServiceException
     */
    @Override
    public Map getEntityMSICCode(Map<String, Object> requestParams){
        HashMap<String, String> obj = new HashMap();
        try {
           
            KwlReturnObject result = accGstDAO.getEntityDetails(requestParams);
            List<MultiEntityMapping> list = result.getEntityList();

            for (MultiEntityMapping entityMapping : list) {
                // < Entity Value , Industry code > 
                obj.put(entityMapping.getMultiEntity().getValue(), entityMapping.getIndustryCode() != null ? entityMapping.getIndustryCode().getValue() : "");
            }

        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return obj;
    }

    /*
     * Method for finding pending transactions in selected period.
     */ 
    @Override
    public JSONObject checkForPendingTransactions(HashMap<String, Object> requestParams) {
        JSONObject returnObj = new JSONObject();
        try {
           
            KwlReturnObject result = accountingDashboardDAOObj.getPendingApprovalDetails(requestParams);
            List list = result.getEntityList();
            int count = list.size();
            returnObj.put("isTransactionExists", count>0);
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return returnObj;
    }
    
    @Override
    public StringBuilder generateTXTGAFV1(JSONObject paramsJObj) throws ServiceException {
        StringBuilder report = new StringBuilder();
        try {
            String companyid = paramsJObj.optString(Constants.companyKey);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date endDate = authHandler.getDateOnlyFormat().parse(paramsJObj.optString("enddate"));
            Date startDate = authHandler.getDateOnlyFormat().parse(paramsJObj.optString("stdate"));
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) result.getEntityList().get(0);
            String companyname = company.getCompanyName() != null ? company.getCompanyName() : "";

            result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(paramsJObj);

            String searchJson = paramsJObj.optString(Constants.Acc_Search_Json);
            String filterConjuctionCriteria = paramsJObj.optString(Constants.Filter_Criteria);

            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                requestParams.put(Constants.Acc_Search_Json, searchJson);
                requestParams.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                accFinancialReportsService.getAdvanceSearchModuleFieldParams(requestParams);
            }
            boolean isAdvanceSearch = requestParams.containsKey(Constants.isAdvanceSearch) ? (Boolean) requestParams.get(Constants.isAdvanceSearch) : false;
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                if (extrapref != null && extrapref.isIsMultiEntity() && !StringUtil.isNullOrEmpty(searchJson)) {
                    companyname = exportDAOObj.getEntityDimensionNameforExport(searchJson, company);
                }
            }

            String companyBRN = companyAccountPreferences.getCompanyUEN();
            String companyGSTNo = companyAccountPreferences.getGstNumber();
            if (!StringUtil.isNullOrEmpty(paramsJObj.optString(Constants.multiEntityValue))) {
                Map entityParamsMap = new HashMap<>();
                JSONObject entityObj = null;
                entityParamsMap.put(Constants.companyid, companyid);
                entityParamsMap.put(Constants.multiEntityValue, paramsJObj.optString(Constants.multiEntityValue));
                entityObj = getEntityDetails(entityParamsMap);
                if (entityObj.getJSONArray(Constants.data).length() > 0) {
                    JSONObject entityDetailsObj = entityObj.getJSONArray(Constants.data).getJSONObject(0);
                    companyBRN = entityDetailsObj.optString("multiEntitycompanybrn");
                    companyGSTNo = entityDetailsObj.optString("multiEntitygstno");
                }
            }
            int totalPCount = 0, totalSCount = 0, totalLCount = 0;
            double totalPurchaseAmount = 0, purchaseTaxAmount = 0, totalSalesAmount = 0, salesTaxAmount = 0;
            report.append("C|" + companyname + "|" + companyBRN + "|" + companyGSTNo + "|" + sdf.format(startDate) + "|" + sdf.format(endDate) + "|" + sdf.format(new Date()) + "|" + Constants.DeskeraERPVersion + "|" + Constants.GAFFileVersion_GAFv1 + "|" + "\r\n");

            String cashAccount = companyAccountPreferences.getCashAccount().getID();
            requestParams.put("cashaccountid", cashAccount);
            requestParams.put("endDate", endDate);
            requestParams.put("startDate", startDate);
            requestParams.put("issales", false);
            requestParams.put("isSalesTax", false);
            requestParams.put(Constants.companyKey, companyid);
            result = accTaxDAOobj.getCalculatedTax((Map) requestParams);
            List list = result.getEntityList();
            JSONArray purchasejArr = getCalculatedPurchaseTaxforIAFfile(requestParams, list);

            for (int i = 0; i < purchasejArr.length(); i++) {
                JSONArray grArray = purchasejArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < grArray.length(); j++) {
                    double grAmt = grArray.getJSONObject(j).optDouble("gramtexcludinggst");//Value of purchase excluding GST
                    double grTaxAmt = grArray.getJSONObject(j).optDouble("grtaxamount");

                    report.append("P|" + grArray.getJSONObject(j).optString("grname") + "|" + grArray.getJSONObject(j).optString("gruen") + "|" + sdf.format(new Date(grArray.getJSONObject(j).getLong("grdate"))) + "|" + grArray.getJSONObject(j).optString("grno") + "|" + grArray.getJSONObject(j).optString(Constants.importExportDeclarationNo) + "|" + grArray.getJSONObject(j).optString("grlineno") + "|" + grArray.getJSONObject(j).optString("grproduct") + "|" + authHandler.formattedAmount(grAmt, companyid) + "|" + authHandler.formattedAmount(grTaxAmt, companyid) + "|" + grArray.getJSONObject(j).optString("grtaxcode") + "|");

                    String fcycode = "XXX";
                    double gstfcy = 0.0;
                    double purchasefcyexcludinggst = 0.0;
                    /*
                     *Displayed Foreign Currency Code, Purchase Foreign Currency Amount & 
                     *Purchase Currency GST Amount only if transaction in foreign currency.
                     */
                    if (!Constants.MYR_CURRENCY_CODE.equalsIgnoreCase(grArray.getJSONObject(j).optString("fcycode"))) {
                        fcycode = grArray.getJSONObject(j).optString("fcycode");
                        gstfcy = grArray.getJSONObject(j).optDouble("gstfcy");
                        purchasefcyexcludinggst = grArray.getJSONObject(j).optDouble("purchasefcyexcludinggst");//Value of purchase excluding GST
                    }
                    report.append(fcycode + "|" + authHandler.formattedAmount(purchasefcyexcludinggst, companyid) + "|" + authHandler.formattedAmount(gstfcy, companyid) + "|" + "\r\n");
                    totalPurchaseAmount += grArray.getJSONObject(j).optDouble("gramtexcludinggst");
                    purchaseTaxAmount += grArray.getJSONObject(j).optDouble("grtaxamount");
                }
                totalPCount += grArray.length();
            }

            //Supplies
            requestParams.put("issales", true);
            requestParams.put("isSalesTax", true);
            result = accTaxDAOobj.getCalculatedTax((Map) requestParams);
            list = result.getEntityList();
            JSONArray salesjArr = getCalculatedSalesTaxforIAFfile(requestParams, list);

            for (int i = 0; i < salesjArr.length(); i++) {
                JSONArray invArray = salesjArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < invArray.length(); j++) {
                    double invTaxAmt = invArray.getJSONObject(j).getDouble("invtaxamount");
                    double invAmt = invArray.getJSONObject(j).getDouble("invamtexcludinggst");//Value of supply excluding GST
                    report.append("S|" + invArray.getJSONObject(j).optString("invname", "") + "|" + invArray.getJSONObject(j).optString("invuen", "") + "|" + sdf.format(new Date(invArray.getJSONObject(j).getLong("invdate"))) + "|" + invArray.getJSONObject(j).getString("invno") + "|" + invArray.getJSONObject(j).getString("invlineno") + "|" + invArray.getJSONObject(j).getString("invproduct") + "|" + authHandler.formattedAmount(invAmt, companyid) + "|" + authHandler.formattedAmount(invTaxAmt, companyid) + "|" + invArray.getJSONObject(j).getString("invtaxcode") + "|" + invArray.getJSONObject(j).optString(Constants.SHIPPING_COUNTRY) + "|");

                    String fcycode = "XXX";
                    double gstfcy = 0.0;
                    double salesfcyexcludinggst = 0.0;
                    /*
                     *Displayed Foreign Currency Code, Supply Foreign Currency Amount & 
                     *Supply Currency GST Amount only if transaction in foreign currency.
                     */
                    if (!Constants.MYR_CURRENCY_CODE.equalsIgnoreCase(invArray.getJSONObject(j).optString("fcycode"))) {
                        fcycode = invArray.getJSONObject(j).optString("fcycode");
                        gstfcy = invArray.getJSONObject(j).getDouble("gstfcy");
                        salesfcyexcludinggst = invArray.getJSONObject(j).getDouble("salesfcyexcludinggst");//Value of supply excluding GST
                    }
                    report.append(fcycode + "|" + authHandler.formattedAmount(salesfcyexcludinggst, companyid) + "|" + authHandler.formattedAmount(gstfcy, companyid) + "|" + "\r\n");
                    totalSalesAmount += invArray.getJSONObject(j).getDouble("invamtexcludinggst");
                    salesTaxAmount += invArray.getJSONObject(j).getDouble("invtaxamount");
                }
                totalSCount += invArray.length();
            }

            double credit = 0, debit = 0, totalOpeningBalance = 0;
            if (isAdvanceSearch) {
                requestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                requestParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.journalEntrySearchJson));
            }
            JSONArray JEjArr = getGLdataforIAFfileNEW(paramsJObj, requestParams);//<-- This method is use less for calculation of GL DATA For IAF File

            HashMap<String, JSONArray> accountwiseJEDetails = new HashMap<>();
            for (int i = 0; i < JEjArr.length(); i++) {
                JSONArray jeArray = JEjArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < jeArray.length(); j++) {
                    JSONArray accountJEDetailsArr = new JSONArray();
                    String accountName = jeArray.getJSONObject(j).getString("jeaccountname");
                    if (accountwiseJEDetails.containsKey(accountName)) {
                        accountJEDetailsArr = accountwiseJEDetails.get(accountName);
                        accountJEDetailsArr.put(jeArray.getJSONObject(j));
                        accountwiseJEDetails.put(accountName, accountJEDetailsArr);
                    } else {
                        accountJEDetailsArr.put(jeArray.getJSONObject(j));
                        accountwiseJEDetails.put(accountName, accountJEDetailsArr);
                    }
                }
                totalLCount += jeArray.length();
            }

            for (Map.Entry<String, JSONArray> entry : accountwiseJEDetails.entrySet()) {
                JSONArray jeArray = entry.getValue();
                double subtotal = 0.0;
                for (int j = 0; j < jeArray.length(); j++) {
                    if (j == 0) {
                        report.append("L|" + sdf.format(new Date(jeArray.getJSONObject(j).getLong("creationdate"))) + "|" + jeArray.getJSONObject(j).optString("jeaccountid", "") + "|" + jeArray.getJSONObject(j).getString("jeaccountname") + "|OPENING BALANCE|||||0.00|0.00|" + authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("openingbalnace"), companyid) + "|" + "\r\n");
                        totalLCount += 1;
                        totalOpeningBalance += jeArray.getJSONObject(j).getDouble("openingbalnace");
                        subtotal += jeArray.getJSONObject(j).getDouble("openingbalnace");
                    }
                    subtotal += (-jeArray.getJSONObject(j).getDouble("credit")) + jeArray.getJSONObject(j).getDouble("debit");
                    report.append("L|" + sdf.format(new Date(jeArray.getJSONObject(j).getLong("jedate"))) + "|" + jeArray.getJSONObject(j).optString("jeaccountid", "") + "|" + jeArray.getJSONObject(j).getString("jeaccountname") + "|" + jeArray.getJSONObject(j).optString("jedesc", "") + "|" + companyname + "|" + jeArray.getJSONObject(j).getString("jeid") + "|" + jeArray.getJSONObject(j).getString("sourcedocid") + "|" + jeArray.getJSONObject(j).getString("sourcetype") + "|" + authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("debit"), companyid) + "|" + authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("credit"), companyid) + "|" + authHandler.formattedAmount(subtotal, companyid) + "|" + "\r\n");

                    debit += (jeArray.getJSONObject(j).getDouble("debit"));
                    credit += (jeArray.getJSONObject(j).getDouble("credit"));
//                    total += -(jeArray.getJSONObject(j).getDouble("credit")) + jeArray.getJSONObject(j).getDouble("debit");
                }
            }
            report.append("F|" + totalPCount + "|" + authHandler.formattedAmount(totalPurchaseAmount, companyid) + "|" + authHandler.formattedAmount(purchaseTaxAmount, companyid) + "|" + totalSCount + "|" + authHandler.formattedAmount(totalSalesAmount, companyid) + "|" + authHandler.formattedAmount(salesTaxAmount, companyid) + "|" + totalLCount + "|" + authHandler.formattedAmount(debit, companyid) + "|" + authHandler.formattedAmount(credit, companyid) + "|" + authHandler.formattedAmount(debit - credit + totalOpeningBalance, companyid) + "|" + "\r\n");
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl:generateGAFV1" + ex.getMessage(), ex);
        }
        return report;
    }

    @Override
    public StringBuilder generateTXTGAFV2(JSONObject paramsJObj) throws ServiceException {
        StringBuilder report = new StringBuilder();
        try {
            String companyid = paramsJObj.optString(Constants.companyKey);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date endDate = authHandler.getDateOnlyFormat().parse(paramsJObj.optString("enddate"));
            Date startDate = authHandler.getDateOnlyFormat().parse(paramsJObj.optString("stdate"));
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) result.getEntityList().get(0);
            String companyname = company.getCompanyName() != null ? company.getCompanyName() : "";
            result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(paramsJObj);
            String searchJson = paramsJObj.optString(Constants.Acc_Search_Json);
            String filterConjuctionCriteria = paramsJObj.optString(Constants.Filter_Criteria);
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                requestParams.put(Constants.Acc_Search_Json, searchJson);
                requestParams.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                accFinancialReportsService.getAdvanceSearchModuleFieldParams(requestParams);
            }
            boolean isAdvanceSearch = requestParams.containsKey(Constants.isAdvanceSearch) ? (Boolean) requestParams.get(Constants.isAdvanceSearch) : false;
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                if (extrapref != null && extrapref.isIsMultiEntity() && !StringUtil.isNullOrEmpty(searchJson)) {
                    companyname = exportDAOObj.getEntityDimensionNameforExport(searchJson, company);
                }
            }

            String companyBRN = companyAccountPreferences.getCompanyUEN();
            String companyGSTNo = companyAccountPreferences.getGstNumber();
            if (!StringUtil.isNullOrEmpty(paramsJObj.optString(Constants.multiEntityValue))) {
                Map entityParamsMap = new HashMap<>();
                JSONObject entityObj = null;
                entityParamsMap.put(Constants.companyid, companyid);
                entityParamsMap.put(Constants.multiEntityValue, paramsJObj.optString(Constants.multiEntityValue));
                entityObj = getEntityDetails(entityParamsMap);
                if (entityObj.getJSONArray(Constants.data).length() > 0) {
                    JSONObject entityDetailsObj = entityObj.getJSONArray(Constants.data).getJSONObject(0);
                    companyBRN = entityDetailsObj.optString("multiEntitycompanybrn");
                    companyGSTNo = entityDetailsObj.optString("multiEntitygstno");
                }
            }
            int totalPCount = 0, totalSCount = 0, totalLCount = 0;
            double totalPurchaseAmount = 0, purchaseTaxAmount = 0, totalSalesAmount = 0, salesTaxAmount = 0;
            report.append("C|C2_CompanyName|C3_CompanyBRN|C4_CompanyGSTNo|C5_PeriodStart|C6_PeriodEnd|C7_GAFCreationDate|C8_SoftwareVersion|C9_GAFVersion|" + "\r\n");
            report.append("C|" + companyname + "|" + companyBRN + "|" + companyGSTNo + "|" + sdf.format(startDate) + "|" + sdf.format(endDate) + "|" + sdf.format(new Date()) + "|" + Constants.DeskeraERPVersion + "|" + Constants.GAFFileVersion_GAFv2 + "|" + "\r\n");
            String cashAccount = companyAccountPreferences.getCashAccount().getID();
            requestParams.put("cashaccountid", cashAccount);
            requestParams.put("endDate", endDate);
            requestParams.put("startDate", startDate);
            requestParams.put("issales", false);
            requestParams.put("isSalesTax", false);
            requestParams.put(Constants.companyKey, companyid);
            result = accTaxDAOobj.getCalculatedTax((Map) requestParams);
            List list = result.getEntityList();
            JSONArray purchasejArr = getCalculatedPurchaseTaxforIAFfile(requestParams, list);

            report.append("P|P2_SupplierName|P3_SupplierBRN|P4_SupplierGSTNo|P5_InvoiceDate|P6_PostingDate|P7_InvoiceNo|P8_ImportK1No|P9_LineNo|P10_ProductDescription|P11_PValueMYR|P12_PGSTValueMYR|P13_TaxCode|P14_FCYCode|P15_PValueFCY|P16_PGSTValueFCY|" + "\r\n");
            for (int i = 0; i < purchasejArr.length(); i++) {
                JSONArray grArray = purchasejArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < grArray.length(); j++) {
                    double grAmt = grArray.getJSONObject(j).optDouble("gramtexcludinggst");//Value of purchase excluding GST
                    double grTaxAmt = grArray.getJSONObject(j).optDouble("grtaxamount");

                    report.append("P|" + grArray.getJSONObject(j).optString("grname") + "|" + grArray.getJSONObject(j).optString("gruen") + "|" + grArray.getJSONObject(j).optString("suppliergstno") + "|" + sdf.format(new Date(grArray.getJSONObject(j).getLong("grdate"))) + "|" + sdf.format(new Date(grArray.getJSONObject(j).getLong("postingdate"))) + "|" + grArray.getJSONObject(j).optString("grno") + "|" + grArray.getJSONObject(j).optString(Constants.importExportDeclarationNo) + "|" + grArray.getJSONObject(j).optString("grlineno") + "|" + grArray.getJSONObject(j).optString("grproduct") + "|" + authHandler.formattedAmount(grAmt, companyid) + "|" + authHandler.formattedAmount(grTaxAmt, companyid) + "|" + grArray.getJSONObject(j).optString("grtaxcode") + "|");

                    String fcycode = "";
                    double gstfcy = 0.0;
                    double purchasefcyexcludinggst = 0.0;
                    /*
                     *Displayed Foreign Currency Code, Purchase Foreign Currency Amount & 
                     *Purchase Currency GST Amount only if transaction in foreign currency.
                     */
                    if (!Constants.MYR_CURRENCY_CODE.equalsIgnoreCase(grArray.getJSONObject(j).optString("fcycode"))) {
                        fcycode = grArray.getJSONObject(j).optString("fcycode");
                        gstfcy = grArray.getJSONObject(j).optDouble("gstfcy");

                        purchasefcyexcludinggst = grArray.getJSONObject(j).optDouble("purchasefcyexcludinggst");//Value of purchase excluding GST
                    }
                    report.append(fcycode + "|" + authHandler.formattedAmount(purchasefcyexcludinggst, companyid) + "|" + authHandler.formattedAmount(gstfcy, companyid) + "|" + "\r\n");
                    totalPurchaseAmount += grArray.getJSONObject(j).optDouble("gramtexcludinggst");//Value of purchase excluding GST
                    purchaseTaxAmount += grArray.getJSONObject(j).optDouble("grtaxamount");
                }
                totalPCount += grArray.length();
            }

            //Supplies
            requestParams.put("issales", true);
            requestParams.put("isSalesTax", true);
            result = accTaxDAOobj.getCalculatedTax((Map) requestParams);
            list = result.getEntityList();
            JSONArray salesjArr = getCalculatedSalesTaxforIAFfile(requestParams, list);
            report.append("S|S2_CustomerName|S3_CustomerBRN|S4_CustomerGSTNo|S5_InvoiceDate|S6_InvoiceNo|S7_ExportK2No|S8_LineNo|S9_ProductDescription|S10_SValueMYR|S11_SGSTValueMYR|S12_TaxCode|S13_Country|S14_FCYCode|S15_SValueFCY|S16_SGSTValueFCY|" + "\r\n");
            for (int i = 0; i < salesjArr.length(); i++) {
                JSONArray invArray = salesjArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < invArray.length(); j++) {
                    double invTaxAmt = invArray.getJSONObject(j).getDouble("invtaxamount");
                    double invAmt = invArray.getJSONObject(j).getDouble("invamtexcludinggst");//Value of supply excluding GST

                    report.append("S|" + invArray.getJSONObject(j).optString("invname") + "|" + invArray.getJSONObject(j).optString("invuen") + "|" + invArray.getJSONObject(j).optString("customergstno") + "|" + sdf.format(new Date(invArray.getJSONObject(j).getLong("invdate"))) + "|" + invArray.getJSONObject(j).getString("invno") + "|" + invArray.getJSONObject(j).optString(Constants.importExportDeclarationNo) + "|" + invArray.getJSONObject(j).getString("invlineno") + "|" + invArray.getJSONObject(j).getString("invproduct") + "|" + authHandler.formattedAmount(invAmt, companyid) + "|" + authHandler.formattedAmount(invTaxAmt, companyid) + "|" + invArray.getJSONObject(j).getString("invtaxcode") + "|" + invArray.getJSONObject(j).optString(Constants.SHIPPING_COUNTRY) + "|");

                    String fcycode = "";
                    double gstfcy = 0.0;
                    double salesfcyexcludinggst = 0.0;
                    /*
                     *Displayed Foreign Currency Code, Supply Foreign Currency Amount & 
                     *Supply Currency GST Amount only if transaction in foreign currency.
                     */
                    if (!Constants.MYR_CURRENCY_CODE.equalsIgnoreCase(invArray.getJSONObject(j).optString("fcycode"))) {
                        fcycode = invArray.getJSONObject(j).optString("fcycode");
                        gstfcy = invArray.getJSONObject(j).getDouble("gstfcy");
                        salesfcyexcludinggst = invArray.getJSONObject(j).getDouble("salesfcyexcludinggst");//Value of supply excluding GST
                    }
                    report.append(fcycode + "|" + authHandler.formattedAmount(salesfcyexcludinggst, companyid) + "|" + authHandler.formattedAmount(gstfcy, companyid) + "|" + "\r\n");
                    totalSalesAmount += invArray.getJSONObject(j).getDouble("invamtexcludinggst");//Value of supply excluding GST
                    salesTaxAmount += invArray.getJSONObject(j).getDouble("invtaxamount");
                }
                totalSCount += invArray.length();
            }

            double credit = 0, debit = 0, totalOpeningBalance = 0;
            if (isAdvanceSearch) {
                requestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                requestParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.journalEntrySearchJson));
            }
            JSONArray JEjArr = getGLdataforIAFfileNEW(paramsJObj, requestParams);
            HashMap<String, JSONArray> accountwiseJEDetails = new HashMap<>();
            for (int i = 0; i < JEjArr.length(); i++) {
                JSONArray jeArray = JEjArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < jeArray.length(); j++) {
                    JSONArray accountJEDetailsArr = new JSONArray();
                    String accountName = jeArray.getJSONObject(j).getString("jeaccountname");
                    if (accountwiseJEDetails.containsKey(accountName)) {
                        accountJEDetailsArr = accountwiseJEDetails.get(accountName);
                        accountJEDetailsArr.put(jeArray.getJSONObject(j));
                        accountwiseJEDetails.put(accountName, accountJEDetailsArr);
                    } else {
                        accountJEDetailsArr.put(jeArray.getJSONObject(j));
                        accountwiseJEDetails.put(accountName, accountJEDetailsArr);
                    }
                }
                totalLCount += jeArray.length();
            }
            report.append("L|L2_TransactionDate|L3_AccountID|L4_AccountType|L5_AccountName|L6_TransactionDescription|L7_EntityName|L8_TransactionID|L9_SourceDocID|L10_SourceType|L11_Debit|L12_Credit|L13_RunningBalance|" + "\r\n");

            for (Map.Entry<String, JSONArray> entry : accountwiseJEDetails.entrySet()) {
                JSONArray jeArray = entry.getValue();
                double subtotal = 0.0;
                for (int j = 0; j < jeArray.length(); j++) {
                    if (j == 0) {
                        report.append("L|" + sdf.format(new Date(jeArray.getJSONObject(j).getLong("creationdate"))) + "|" + jeArray.getJSONObject(j).optString("jeaccountid", "") + "|" + jeArray.getJSONObject(j).getString("jeaccountname") + "|OPENING BALANCE|||||0.00|0.00|" + authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("openingbalnace"), companyid) + "|" + "\r\n");
                        totalLCount += 1;
                        subtotal = jeArray.getJSONObject(j).getDouble("openingbalnace");
                        totalOpeningBalance += jeArray.getJSONObject(j).getDouble("openingbalnace");
                    }
                    subtotal += (-jeArray.getJSONObject(j).getDouble("credit")) + jeArray.getJSONObject(j).getDouble("debit");
                    report.append("L|" + sdf.format(new Date(jeArray.getJSONObject(j).getLong("jedate"))) + "|" + jeArray.getJSONObject(j).optString("jeaccountid") + "|" + jeArray.getJSONObject(j).optString("accounttype") + "|" + jeArray.getJSONObject(j).getString("jeaccountname") + "|" + jeArray.getJSONObject(j).optString("jedesc") + "|" + companyname + "|" + jeArray.getJSONObject(j).getString("jeid") + "|" + jeArray.getJSONObject(j).getString("sourcedocid") + "|" + jeArray.getJSONObject(j).getString("sourcetype") + "|" + authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("debit"), companyid) + "|" + authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("credit"), companyid) + "|" + authHandler.formattedAmount(subtotal, companyid) + "|" + "\r\n");

                    debit += (jeArray.getJSONObject(j).getDouble("debit"));
                    credit += (jeArray.getJSONObject(j).getDouble("credit"));
//                    total += -(jeArray.getJSONObject(j).getDouble("credit")) + jeArray.getJSONObject(j).getDouble("debit");
                }
            }
            report.append("F|F2_CountPRecord|F3_SumPValueMYR|F4_SumPGSTValueMYR|F5_CountSRecord|F6_SumSValueMYR|F7_SumSGSTValueMYR|F8_CountLRecord|F9_SumLDebit|F10_SumLCredit|F11_SumLCloseBalance|" + "\r\n");
            report.append("F|" + totalPCount + "|" + authHandler.formattedAmount(totalPurchaseAmount, companyid) + "|" + authHandler.formattedAmount(purchaseTaxAmount, companyid) + "|" + totalSCount + "|" + authHandler.formattedAmount(totalSalesAmount, companyid) + "|" + authHandler.formattedAmount(salesTaxAmount, companyid) + "|" + totalLCount + "|" + authHandler.formattedAmount(debit, companyid) + "|" + authHandler.formattedAmount(credit, companyid) + "|" + authHandler.formattedAmount(debit - credit + totalOpeningBalance, companyid) + "|" + "\r\n");
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl:generateGAFV2" + ex.getMessage(), ex);
        }
        return report;
    }

    @Override
    public Document generateXMLGAFV1(JSONObject paramsJObj) throws ServiceException {
        Document doc = null;
        try {
            String companyid = paramsJObj.optString(Constants.companyKey);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date endDate = authHandler.getDateOnlyFormat().parse(paramsJObj.optString("enddate"));
            Date startDate = authHandler.getDateOnlyFormat().parse(paramsJObj.optString("stdate"));
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) result.getEntityList().get(0);
            String companyName = company.getCompanyName();
            result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(paramsJObj);
            String searchJson = paramsJObj.optString(Constants.Acc_Search_Json);
            String filterConjuctionCriteria = paramsJObj.optString(Constants.Filter_Criteria);
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                requestParams.put(Constants.Acc_Search_Json, searchJson);
                requestParams.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                accFinancialReportsService.getAdvanceSearchModuleFieldParams(requestParams);
            }

            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                if (extrapref != null && extrapref.isIsMultiEntity() && !StringUtil.isNullOrEmpty(searchJson)) {
                    companyName = exportDAOObj.getEntityDimensionNameforExport(searchJson, company);
                }
            }
            String businessRN = companyAccountPreferences.getCompanyUEN();
            String gstNumber = companyAccountPreferences.getGstNumber();
            if (!StringUtil.isNullOrEmpty(paramsJObj.optString(Constants.multiEntityValue))) {
                Map entityParamsMap = new HashMap<>();
                JSONObject entityObj = null;
                entityParamsMap.put(Constants.companyid, companyid);
                entityParamsMap.put(Constants.multiEntityValue, paramsJObj.optString(Constants.multiEntityValue));
                entityObj = getEntityDetails(entityParamsMap);
                if (entityObj.getJSONArray(Constants.data).length() > 0) {
                    JSONObject entityDetailsObj = entityObj.getJSONArray(Constants.data).getJSONObject(0);
                    businessRN = entityDetailsObj.optString("multiEntitycompanybrn");
                    gstNumber = entityDetailsObj.optString("multiEntitygstno");
                }
            }

            int totalPCount = 0, totalSCount = 0, totalLCount = 0;
            double totalPurchaseAmount = 0, purchaseTaxAmount = 0, totalSalesAmount = 0, salesTaxAmount = 0;
            String gafCreationDate = sdf.format(new Date());
            String gafVersion = companyAccountPreferences.getIafVersion();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.newDocument();
            Element gstRootElement = doc.createElement("GSTAuditFile");
            doc.appendChild(gstRootElement);

            Element companiesRootElement = doc.createElement("Companies");
            gstRootElement.appendChild(companiesRootElement);
            Element companyRootElement = doc.createElement("Company");
            companiesRootElement.appendChild(companyRootElement);

            companyRootElement.appendChild(getElements(doc, "BusinessName", companyName));
            companyRootElement.appendChild(getElements(doc, "BusinessRN", businessRN));
            companyRootElement.appendChild(getElements(doc, "GSTNumber", gstNumber));
            companyRootElement.appendChild(getElements(doc, "PeriodStart", sdf.format(startDate)));
            companyRootElement.appendChild(getElements(doc, "PeriodEnd", sdf.format(endDate)));
            companyRootElement.appendChild(getElements(doc, "GAFCreationDate", gafCreationDate));
            companyRootElement.appendChild(getElements(doc, "ProductVersion", Constants.DeskeraERPVersion));
            companyRootElement.appendChild(getElements(doc, "GAFVersion", Constants.GAFFileVersion_GAFv1));

            String cashAccount = companyAccountPreferences.getCashAccount().getID();
            requestParams.put("cashaccountid", cashAccount);
            requestParams.put("endDate", endDate);
            requestParams.put("startDate", startDate);
            requestParams.put("issales", false);
            requestParams.put("isSalesTax", false);
            requestParams.put(Constants.companyKey, companyid);
            result = accTaxDAOobj.getCalculatedTax((Map) requestParams);
            List list = result.getEntityList();
            JSONArray purchasejArr = getCalculatedPurchaseTaxforIAFfile(requestParams, list);

            //purchases
            Element purchasesRootElement = doc.createElement("Purchases");
            gstRootElement.appendChild(purchasesRootElement);
            for (int i = 0; i < purchasejArr.length(); i++) {
                JSONArray grArray = purchasejArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < grArray.length(); j++) {
                    double grAmt = grArray.getJSONObject(j).optDouble("gramtexcludinggst");//Value of purchase excluding GST
                    double grTaxAmt = grArray.getJSONObject(j).optDouble("grtaxamount");

                    String fcycode = "XXX";
                    double gstfcy = 0.0;
                    double purchasefcyexcludinggst = 0.0;
                    /*
                     *Displayed Foreign Currency Code, Purchase Foreign Currency Amount & 
                     *Purchase Currency GST Amount only if transaction in foreign currency.
                     */
                    if (!Constants.MYR_CURRENCY_CODE.equalsIgnoreCase(grArray.getJSONObject(j).optString("fcycode"))) {
                        fcycode = grArray.getJSONObject(j).optString("fcycode");
                        purchasefcyexcludinggst = grArray.getJSONObject(j).optDouble("purchasefcyexcludinggst");//Value of purchase excluding GST
                        gstfcy = grArray.getJSONObject(j).optDouble("gstfcy");
                    }

                    Element purchaseSubRootElement = doc.createElement("Purchase");
                    purchasesRootElement.appendChild(purchaseSubRootElement);

                    purchaseSubRootElement.appendChild(getElements(doc, "SupplierName", grArray.getJSONObject(j).optString("grname")));
                    purchaseSubRootElement.appendChild(getElements(doc, "SupplierBRN", grArray.getJSONObject(j).optString("gruen")));
                    purchaseSubRootElement.appendChild(getElements(doc, "InvoiceDate", sdf.format(new Date(new Long(grArray.getJSONObject(j).getString("grdate"))))));
                    purchaseSubRootElement.appendChild(getElements(doc, "InvoiceNumber", grArray.getJSONObject(j).optString("grno")));
                    purchaseSubRootElement.appendChild(getElements(doc, "ImportDeclarationNo", grArray.getJSONObject(j).optString(Constants.importExportDeclarationNo)));
                    purchaseSubRootElement.appendChild(getElements(doc, "LineNumber", grArray.getJSONObject(j).optString("grlineno")));
                    purchaseSubRootElement.appendChild(getElements(doc, "ProductDescription", grArray.getJSONObject(j).optString("grproduct")));
                    purchaseSubRootElement.appendChild(getElements(doc, "PurchaseValueMYR", authHandler.formattedAmount(grAmt, companyid)));
                    purchaseSubRootElement.appendChild(getElements(doc, "GSTValueMYR", authHandler.formattedAmount(grTaxAmt, companyid)));
                    purchaseSubRootElement.appendChild(getElements(doc, "TaxCode", grArray.getJSONObject(j).optString("grtaxcode")));
                    purchaseSubRootElement.appendChild(getElements(doc, "FCYCode", fcycode));
                    purchaseSubRootElement.appendChild(getElements(doc, "PurchaseFCY", authHandler.formattedAmount(purchasefcyexcludinggst, companyid)));
                    purchaseSubRootElement.appendChild(getElements(doc, "GSTFCY", authHandler.formattedAmount(gstfcy, companyid)));

                    totalPurchaseAmount += grArray.getJSONObject(j).optDouble("gramtexcludinggst");
                    purchaseTaxAmount += grArray.getJSONObject(j).optDouble("grtaxamount");
                }
                totalPCount += grArray.length();
            }

            //Supplies
            Element suppliesRootElement = doc.createElement("Supplies");
            gstRootElement.appendChild(suppliesRootElement);

            requestParams.put("issales", true);
            requestParams.put("isSalesTax", true);
            result = accTaxDAOobj.getCalculatedTax((Map) requestParams);
            list = result.getEntityList();
            JSONArray salesjArr = getCalculatedSalesTaxforIAFfile(requestParams, list);
            for (int i = 0; i < salesjArr.length(); i++) {
                JSONArray invArray = salesjArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < invArray.length(); j++) {
                    double invAmt = invArray.getJSONObject(j).getDouble("invamtexcludinggst");//Value of supply excluding GST
                    double invTaxAmt = invArray.getJSONObject(j).getDouble("invtaxamount");

                    String fcycode = "XXX";
                    double gstfcy = 0.0;
                    double salesfcyexcludinggst = 0.0;
                    /*
                     *Displayed Foreign Currency Code, Supply Foreign Currency Amount & 
                     *Supply Currency GST Amount only if transaction in foreign currency.
                     */
                    if (!Constants.MYR_CURRENCY_CODE.equalsIgnoreCase(invArray.getJSONObject(j).optString("fcycode"))) {
                        fcycode = invArray.getJSONObject(j).optString("fcycode");
                        gstfcy = invArray.getJSONObject(j).getDouble("gstfcy");
                        salesfcyexcludinggst = invArray.getJSONObject(j).getDouble("salesfcyexcludinggst");//Value of supply excluding GST
                    }

                    Element suppliseSubRootElement = doc.createElement("Supply");
                    suppliesRootElement.appendChild(suppliseSubRootElement);

                    suppliseSubRootElement.appendChild(getElements(doc, "CustomerName", invArray.getJSONObject(j).optString("invname", "")));
                    suppliseSubRootElement.appendChild(getElements(doc, "CustomerBRN", invArray.getJSONObject(j).optString("invuen", "")));
                    suppliseSubRootElement.appendChild(getElements(doc, "InvoiceDate", sdf.format(new Date(new Long(invArray.getJSONObject(j).getString("invdate"))))));
                    suppliseSubRootElement.appendChild(getElements(doc, "InvoiceNumber", invArray.getJSONObject(j).getString("invno")));
                    suppliseSubRootElement.appendChild(getElements(doc, "LineNumber", invArray.getJSONObject(j).getString("invlineno")));
                    suppliseSubRootElement.appendChild(getElements(doc, "ProductDescription", invArray.getJSONObject(j).getString("invproduct")));
                    suppliseSubRootElement.appendChild(getElements(doc, "SupplyValueMYR", authHandler.formattedAmount(invAmt, companyid)));
                    suppliseSubRootElement.appendChild(getElements(doc, "GSTValueMYR", authHandler.formattedAmount(invTaxAmt, companyid)));
                    suppliseSubRootElement.appendChild(getElements(doc, "TaxCode", invArray.getJSONObject(j).getString("invtaxcode")));
                    suppliseSubRootElement.appendChild(getElements(doc, "Country", invArray.getJSONObject(j).optString(Constants.SHIPPING_COUNTRY)));
                    suppliseSubRootElement.appendChild(getElements(doc, "FCYCode", fcycode));
                    suppliseSubRootElement.appendChild(getElements(doc, "SupplyFCY", authHandler.formattedAmount(salesfcyexcludinggst, companyid)));
                    suppliseSubRootElement.appendChild(getElements(doc, "GSTFCY", authHandler.formattedAmount(gstfcy, companyid)));

                    totalSalesAmount += invArray.getJSONObject(j).getDouble("invamtexcludinggst");
                    salesTaxAmount += invArray.getJSONObject(j).getDouble("invtaxamount");
                }
                totalSCount += invArray.length();
            }

            //Ledger
            double credit = 0, debit = 0, totalOpeningBalance = 0;
            JSONArray JEjArr = getGLdataforIAFfileNEW(paramsJObj, requestParams);

            HashMap<String, JSONArray> accountwiseJEDetails = new HashMap<>();
            for (int i = 0; i < JEjArr.length(); i++) {
                JSONArray jeArray = JEjArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < jeArray.length(); j++) {
                    JSONArray accountJEDetailsArr = new JSONArray();
                    String accountName = jeArray.getJSONObject(j).getString("jeaccountname");
                    if (accountwiseJEDetails.containsKey(accountName)) {
                        accountJEDetailsArr = accountwiseJEDetails.get(accountName);
                        accountJEDetailsArr.put(jeArray.getJSONObject(j));
                        accountwiseJEDetails.put(accountName, accountJEDetailsArr);
                    } else {
                        accountJEDetailsArr.put(jeArray.getJSONObject(j));
                        accountwiseJEDetails.put(accountName, accountJEDetailsArr);
                    }
                }
                totalLCount += jeArray.length();
            }
            Element ledgerRootElement = doc.createElement("Ledger");
            gstRootElement.appendChild(ledgerRootElement);
            for (Map.Entry<String, JSONArray> entry : accountwiseJEDetails.entrySet()) {
                JSONArray jeArray = entry.getValue();
                double subtotal = 0.0;
                for (int j = 0; j < jeArray.length(); j++) {
                    if (j == 0) {
                        Element ledgerSubRootElement = doc.createElement("LedgerEntry");
                        ledgerRootElement.appendChild(ledgerSubRootElement);
                        ledgerSubRootElement.appendChild(getElements(doc, "TransactionDate", sdf.format(new Date(new Long(jeArray.getJSONObject(j).getString("creationdate"))))));
                        ledgerSubRootElement.appendChild(getElements(doc, "AccountID", jeArray.getJSONObject(j).optString("jeaccountid", "")));
                        ledgerSubRootElement.appendChild(getElements(doc, "AccountName", jeArray.getJSONObject(j).getString("jeaccountname")));
                        ledgerSubRootElement.appendChild(getElements(doc, "TransactionDescription", "OPENING BALANCE"));
                        ledgerSubRootElement.appendChild(getElements(doc, "Name", ""));
                        ledgerSubRootElement.appendChild(getElements(doc, "TransactionID", ""));
                        ledgerSubRootElement.appendChild(getElements(doc, "SourceDocumentID", ""));
                        ledgerSubRootElement.appendChild(getElements(doc, "SourceType", ""));
                        ledgerSubRootElement.appendChild(getElements(doc, "Debit", "0.00"));
                        ledgerSubRootElement.appendChild(getElements(doc, "Credit", "0.00"));
                        ledgerSubRootElement.appendChild(getElements(doc, "Balance", authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("openingbalnace"), companyid)));

                        totalLCount += 1;
                        subtotal = jeArray.getJSONObject(j).getDouble("openingbalnace");
                        totalOpeningBalance += jeArray.getJSONObject(j).getDouble("openingbalnace");
                    }
                    subtotal += (-jeArray.getJSONObject(j).getDouble("credit")) + jeArray.getJSONObject(j).getDouble("debit");

                    Element ledgerSubRootElement = doc.createElement("LedgerEntry");
                    ledgerRootElement.appendChild(ledgerSubRootElement);
                    ledgerSubRootElement.appendChild(getElements(doc, "TransactionDate", sdf.format(new Date(new Long(jeArray.getJSONObject(j).getString("jedate"))))));
                    ledgerSubRootElement.appendChild(getElements(doc, "AccountID", jeArray.getJSONObject(j).optString("jeaccountid", "")));
                    ledgerSubRootElement.appendChild(getElements(doc, "AccountName", jeArray.getJSONObject(j).getString("jeaccountname")));
                    ledgerSubRootElement.appendChild(getElements(doc, "TransactionDescription", jeArray.getJSONObject(j).optString("jedesc", "")));
                    ledgerSubRootElement.appendChild(getElements(doc, "Name", companyName));
                    ledgerSubRootElement.appendChild(getElements(doc, "TransactionID", jeArray.getJSONObject(j).getString("jeid")));
                    ledgerSubRootElement.appendChild(getElements(doc, "SourceDocumentID", jeArray.getJSONObject(j).getString("sourcedocid")));
                    ledgerSubRootElement.appendChild(getElements(doc, "SourceType", jeArray.getJSONObject(j).getString("sourcetype")));
                    ledgerSubRootElement.appendChild(getElements(doc, "Debit", authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("debit"), companyid)));
                    ledgerSubRootElement.appendChild(getElements(doc, "Credit", authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("credit"), companyid)));
                    ledgerSubRootElement.appendChild(getElements(doc, "Balance", authHandler.formattedAmount(subtotal, companyid)));

                    debit += (jeArray.getJSONObject(j).getDouble("debit"));
                    credit += (jeArray.getJSONObject(j).getDouble("credit"));
//                    totalOpeningBalance += (-jeArray.getJSONObject(j).getDouble("credit")) + jeArray.getJSONObject(j).getDouble("debit");
                }
            }
            Element footerRootElement = doc.createElement("Footer");
            gstRootElement.appendChild(footerRootElement);
            footerRootElement.appendChild(getElements(doc, "TotalPurchaseCount", totalPCount + ""));
            footerRootElement.appendChild(getElements(doc, "TotalPurchaseAmount", authHandler.formattedAmount(totalPurchaseAmount, companyid)));
            footerRootElement.appendChild(getElements(doc, "TotalPurchaseAmountGST", authHandler.formattedAmount(purchaseTaxAmount, companyid)));
            footerRootElement.appendChild(getElements(doc, "TotalSupplyCount", totalSCount + ""));
            footerRootElement.appendChild(getElements(doc, "TotalSupplyAmount", authHandler.formattedAmount(totalSalesAmount, companyid)));
            footerRootElement.appendChild(getElements(doc, "TotalSupplyAmountGST", authHandler.formattedAmount(salesTaxAmount, companyid)));
            footerRootElement.appendChild(getElements(doc, "TotalLedgerCount", totalLCount + ""));
            footerRootElement.appendChild(getElements(doc, "TotalLedgerDebit", authHandler.formattedAmount(debit, companyid)));
            footerRootElement.appendChild(getElements(doc, "TotalLedgerCredit", authHandler.formattedAmount(credit, companyid)));
            footerRootElement.appendChild(getElements(doc, "TotalLedgerBalance", authHandler.formattedAmount(debit - credit + totalOpeningBalance, companyid)));
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl:generateXMLGAFV1" + ex.getMessage(), ex);
        }
        return doc;
    }

    @Override
    public Document generateXMLGAFV2(JSONObject paramsJObj) throws ServiceException {
        Document doc = null;
        try {
            String companyid = paramsJObj.optString(Constants.companyKey);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date endDate = authHandler.getDateOnlyFormat().parse(paramsJObj.optString("enddate"));
            Date startDate = authHandler.getDateOnlyFormat().parse(paramsJObj.optString("stdate"));
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) result.getEntityList().get(0);
            String companyName = company.getCompanyName();
            result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(paramsJObj);
            String searchJson = paramsJObj.optString(Constants.Acc_Search_Json);
            String filterConjuctionCriteria = paramsJObj.optString(Constants.Filter_Criteria);
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                requestParams.put(Constants.Acc_Search_Json, searchJson);
                requestParams.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                accFinancialReportsService.getAdvanceSearchModuleFieldParams(requestParams);
            }

            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                if (extrapref != null && extrapref.isIsMultiEntity() && !StringUtil.isNullOrEmpty(searchJson)) {
                    companyName = exportDAOObj.getEntityDimensionNameforExport(searchJson, company);
                }
            }
            String businessRN = companyAccountPreferences.getCompanyUEN();
            String gstNumber = companyAccountPreferences.getGstNumber();
            if (!StringUtil.isNullOrEmpty(paramsJObj.optString(Constants.multiEntityValue))) {
                Map entityParamsMap = new HashMap<>();
                JSONObject entityObj = null;
                entityParamsMap.put(Constants.companyid, companyid);
                entityParamsMap.put(Constants.multiEntityValue, paramsJObj.optString(Constants.multiEntityValue));
                entityObj = getEntityDetails(entityParamsMap);
                if (entityObj.getJSONArray("data").length() > 0) {
                    JSONObject entityDetailsObj = entityObj.getJSONArray("data").getJSONObject(0);
                    businessRN = entityDetailsObj.optString("multiEntitycompanybrn");
                    gstNumber = entityDetailsObj.optString("multiEntitygstno");
                }
            }

            int totalPCount = 0, totalSCount = 0, totalLCount = 0;
            double totalPurchaseAmount = 0, purchaseTaxAmount = 0, totalSalesAmount = 0, salesTaxAmount = 0;
            String gafCreationDate = sdf.format(new Date());

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.newDocument();
            Element companyRootElement = doc.createElement("Company");
            doc.appendChild(companyRootElement);

            Element CompanyInfoRootElement = doc.createElement("CompanyInfo");
            companyRootElement.appendChild(CompanyInfoRootElement);
            CompanyInfoRootElement.appendChild(getElements(doc, "CompanyName", companyName));
            CompanyInfoRootElement.appendChild(getElements(doc, "CompanyBRN", businessRN));
            CompanyInfoRootElement.appendChild(getElements(doc, "CompanyGSTNo", gstNumber));
            CompanyInfoRootElement.appendChild(getElements(doc, "PeriodStart", sdf.format(startDate)));
            CompanyInfoRootElement.appendChild(getElements(doc, "PeriodEnd", sdf.format(endDate)));
            CompanyInfoRootElement.appendChild(getElements(doc, "GAFCreationDate", gafCreationDate));
            CompanyInfoRootElement.appendChild(getElements(doc, "SoftwareVersion", Constants.DeskeraERPVersion));
            CompanyInfoRootElement.appendChild(getElements(doc, "GAFVersion", Constants.GAFFileVersion_GAFv2));

            String cashAccount = companyAccountPreferences.getCashAccount().getID();
            requestParams.put("cashaccountid", cashAccount);
            requestParams.put("issales", false);
            requestParams.put("isSalesTax", false);
            requestParams.put("endDate", endDate);
            requestParams.put("startDate", startDate);
            requestParams.put(Constants.companyKey, companyid);
            result = accTaxDAOobj.getCalculatedTax((Map) requestParams);
            List list = result.getEntityList();

            //Purchases
            JSONArray purchasejArr = getCalculatedPurchaseTaxforIAFfile(requestParams, list);

            Element purchaseRootElement = doc.createElement("Purchase");
            companyRootElement.appendChild(purchaseRootElement);
            for (int i = 0; i < purchasejArr.length(); i++) {
                JSONArray grArray = purchasejArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < grArray.length(); j++) {
                    double grAmt = grArray.getJSONObject(j).optDouble("gramtexcludinggst");//Value of purchase excluding GST
                    double grTaxAmt = grArray.getJSONObject(j).optDouble("grtaxamount");

                    String fcycode = "XXX";
                    double gstfcy = 0.0;
                    double purchasefcyexcludinggst = 0.0;
                    /*
                     *Displayed Foreign Currency Code, Purchase Foreign Currency Amount & 
                     *Purchase Currency GST Amount only if transaction in foreign currency.
                     */
                    if (!Constants.MYR_CURRENCY_CODE.equalsIgnoreCase(grArray.getJSONObject(j).optString("fcycode"))) {
                        fcycode = grArray.getJSONObject(j).optString("fcycode");
                        purchasefcyexcludinggst = grArray.getJSONObject(j).optDouble("purchasefcyexcludinggst");//Value of purchase excluding GST
                        gstfcy = grArray.getJSONObject(j).optDouble("gstfcy");
                    }

                    Element purchaseLinesRootElement = doc.createElement("PurchaseLines");
                    purchaseRootElement.appendChild(purchaseLinesRootElement);

                    purchaseLinesRootElement.appendChild(getElements(doc, "SupplierName", grArray.getJSONObject(j).optString("grname")));
                    purchaseLinesRootElement.appendChild(getElements(doc, "SupplierBRN", grArray.getJSONObject(j).optString("gruen")));
                    purchaseLinesRootElement.appendChild(getElements(doc, "SupplierGSTNo", grArray.getJSONObject(j).optString("suppliergstno")));
                    purchaseLinesRootElement.appendChild(getElements(doc, "InvoiceDate", sdf.format(new Date(new Long(grArray.getJSONObject(j).getString("grdate"))))));
                    purchaseLinesRootElement.appendChild(getElements(doc, "PostingDate", sdf.format(new Date(new Long(grArray.getJSONObject(j).getString("postingdate"))))));
                    purchaseLinesRootElement.appendChild(getElements(doc, "InvoiceNo", grArray.getJSONObject(j).optString("grno")));
                    purchaseLinesRootElement.appendChild(getElements(doc, "ImportK1Number", grArray.getJSONObject(j).optString(Constants.importExportDeclarationNo)));
                    purchaseLinesRootElement.appendChild(getElements(doc, "LineNo", grArray.getJSONObject(j).optString("grlineno")));
                    purchaseLinesRootElement.appendChild(getElements(doc, "ProductDescription", grArray.getJSONObject(j).optString("grproduct")));
                    purchaseLinesRootElement.appendChild(getElements(doc, "PurchaseValueMYR", authHandler.formattedAmount(grAmt, companyid)));
                    purchaseLinesRootElement.appendChild(getElements(doc, "PurchaseGSTValueMYR", authHandler.formattedAmount(grTaxAmt, companyid)));
                    purchaseLinesRootElement.appendChild(getElements(doc, "TaxCode", grArray.getJSONObject(j).optString("grtaxcode")));
                    purchaseLinesRootElement.appendChild(getElements(doc, "FCYCode", fcycode));
                    purchaseLinesRootElement.appendChild(getElements(doc, "PurchaseFCY", authHandler.formattedAmount(purchasefcyexcludinggst, companyid)));
                    purchaseLinesRootElement.appendChild(getElements(doc, "PurchaseGSTValueFCY", authHandler.formattedAmount(gstfcy, companyid)));
                    totalPurchaseAmount += grArray.getJSONObject(j).optDouble("gramtexcludinggst");
                    purchaseTaxAmount += grArray.getJSONObject(j).optDouble("grtaxamount");
                }
                totalPCount += grArray.length();
            }
            Attr SumPurchaseValueMYR = doc.createAttribute("SumPurchaseValueMYR");
            SumPurchaseValueMYR.setValue(authHandler.formattedAmount(totalPurchaseAmount, companyid));
            purchaseRootElement.setAttributeNode(SumPurchaseValueMYR);

            Attr SumPurchaseGSTValueMYR = doc.createAttribute("SumPurchaseGSTValueMYR");
            SumPurchaseGSTValueMYR.setValue(authHandler.formattedAmount(purchaseTaxAmount, companyid));
            purchaseRootElement.setAttributeNode(SumPurchaseGSTValueMYR);

            Attr CountPurchaseRecord = doc.createAttribute("CountPurchaseRecord");
            CountPurchaseRecord.setValue("" + totalPCount);
            purchaseRootElement.setAttributeNode(CountPurchaseRecord);

            //Supplies
            requestParams.put("issales", true);
            requestParams.put("isSalesTax", true);
            result = accTaxDAOobj.getCalculatedTax((Map) requestParams);
            list = result.getEntityList();
            JSONArray salesjArr = getCalculatedSalesTaxforIAFfile(requestParams, list);

            Element supplyRootElement = doc.createElement("Supply");
            companyRootElement.appendChild(supplyRootElement);

            for (int i = 0; i < salesjArr.length(); i++) {
                JSONArray invArray = salesjArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < invArray.length(); j++) {
                    double invTaxAmt = invArray.getJSONObject(j).getDouble("invtaxamount");
                    double invAmt = invArray.getJSONObject(j).getDouble("invamtexcludinggst");//Value of supply excluding GST

                    String fcycode = "XXX";
                    double gstfcy = 0.0;
                    double salesfcyexcludinggst = 0.0;
                    /*
                     *Displayed Foreign Currency Code, Supply Foreign Currency Amount & 
                     *Supply Currency GST Amount only if transaction in foreign currency.
                     */
                    if (!Constants.MYR_CURRENCY_CODE.equalsIgnoreCase(invArray.getJSONObject(j).optString("fcycode"))) {
                        fcycode = invArray.getJSONObject(j).optString("fcycode");
                        gstfcy = invArray.getJSONObject(j).getDouble("gstfcy");
                        salesfcyexcludinggst = invArray.getJSONObject(j).getDouble("salesfcyexcludinggst");//Value of supply excluding GST
                    }

                    Element supplyLinesRootElement = doc.createElement("SupplyLines");
                    supplyRootElement.appendChild(supplyLinesRootElement);

                    supplyLinesRootElement.appendChild(getElements(doc, "CustomerName", invArray.getJSONObject(j).optString("invname", "")));
                    supplyLinesRootElement.appendChild(getElements(doc, "CustomerBRN", invArray.getJSONObject(j).optString("invuen", "")));
                    supplyLinesRootElement.appendChild(getElements(doc, "CustomerGSTNo", invArray.getJSONObject(j).optString("customergstno", "")));
                    supplyLinesRootElement.appendChild(getElements(doc, "InvoiceDate", sdf.format(new Date(new Long(invArray.getJSONObject(j).getString("invdate"))))));
                    supplyLinesRootElement.appendChild(getElements(doc, "InvoiceNo", invArray.getJSONObject(j).getString("invno")));
                    supplyLinesRootElement.appendChild(getElements(doc, "ExportK2Number", invArray.getJSONObject(j).getString(Constants.importExportDeclarationNo)));
                    supplyLinesRootElement.appendChild(getElements(doc, "LineNumber", invArray.getJSONObject(j).getString("invlineno")));
                    supplyLinesRootElement.appendChild(getElements(doc, "ProductDescription", invArray.getJSONObject(j).getString("invproduct")));
                    supplyLinesRootElement.appendChild(getElements(doc, "SupplyValueMYR", authHandler.formattedAmount(invAmt, companyid)));
                    supplyLinesRootElement.appendChild(getElements(doc, "SupplyGSTValueMYR", authHandler.formattedAmount(invTaxAmt, companyid)));
                    supplyLinesRootElement.appendChild(getElements(doc, "TaxCode", invArray.getJSONObject(j).getString("invtaxcode")));
                    supplyLinesRootElement.appendChild(getElements(doc, "Country", invArray.getJSONObject(j).optString(Constants.SHIPPING_COUNTRY)));
                    supplyLinesRootElement.appendChild(getElements(doc, "FCYCode", fcycode));
                    supplyLinesRootElement.appendChild(getElements(doc, "SupplyValueFCY", authHandler.formattedAmount(salesfcyexcludinggst, companyid)));
                    supplyLinesRootElement.appendChild(getElements(doc, "SupplyGSTValueFCY", authHandler.formattedAmount(gstfcy, companyid)));

                    totalSalesAmount += invArray.getJSONObject(j).getDouble("invamtexcludinggst");
                    salesTaxAmount += invArray.getJSONObject(j).getDouble("invtaxamount");
                }
                totalSCount += invArray.length();
            }
            Attr SumSupplyValueMYR = doc.createAttribute("SumSupplyValueMYR");
            SumSupplyValueMYR.setValue(authHandler.formattedAmount(totalSalesAmount, companyid));
            supplyRootElement.setAttributeNode(SumSupplyValueMYR);

            Attr SumSupplyGSTValueMYR = doc.createAttribute("SumSupplyGSTValueMYR");
            SumSupplyGSTValueMYR.setValue(authHandler.formattedAmount(salesTaxAmount, companyid));
            supplyRootElement.setAttributeNode(SumSupplyGSTValueMYR);

            Attr CountSupplyRecord = doc.createAttribute("CountSupplyRecord");
            CountSupplyRecord.setValue("" + totalSCount);
            supplyRootElement.setAttributeNode(CountSupplyRecord);

            //Ledger
            double credit = 0, debit = 0, totalOpeningBalance = 0;
            JSONArray JEjArr = getGLdataforIAFfileNEW(paramsJObj, requestParams);

            HashMap<String, JSONArray> accountwiseJEDetails = new HashMap<>();
            for (int i = 0; i < JEjArr.length(); i++) {
                JSONArray jeArray = JEjArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < jeArray.length(); j++) {
                    JSONArray accountJEDetailsArr = new JSONArray();
                    String accountName = jeArray.getJSONObject(j).getString("jeaccountname");
                    if (accountwiseJEDetails.containsKey(accountName)) {
                        accountJEDetailsArr = accountwiseJEDetails.get(accountName);
                        accountJEDetailsArr.put(jeArray.getJSONObject(j));
                        accountwiseJEDetails.put(accountName, accountJEDetailsArr);
                    } else {
                        accountJEDetailsArr.put(jeArray.getJSONObject(j));
                        accountwiseJEDetails.put(accountName, accountJEDetailsArr);
                    }
                }
                totalLCount += jeArray.length();
            }
            Element GLDataRootElement = doc.createElement("GLData");
            companyRootElement.appendChild(GLDataRootElement);

            for (Map.Entry<String, JSONArray> entry : accountwiseJEDetails.entrySet()) {
                JSONArray jeArray = entry.getValue();
                double subtotal = 0.0;
                for (int j = 0; j < jeArray.length(); j++) {
                    if (j == 0) {
                        Element GLDataLinesRootElement = doc.createElement("GLDataLines");
                        GLDataRootElement.appendChild(GLDataLinesRootElement);
                        GLDataLinesRootElement.appendChild(getElements(doc, "TransactionDate", sdf.format(new Date(new Long(jeArray.getJSONObject(j).getString("creationdate"))))));
                        GLDataLinesRootElement.appendChild(getElements(doc, "AccountID", jeArray.getJSONObject(j).optString("jeaccountid", "")));
                        GLDataLinesRootElement.appendChild(getElements(doc, "AccountType", jeArray.getJSONObject(j).optString("accounttype", "")));
                        GLDataLinesRootElement.appendChild(getElements(doc, "AccountName", jeArray.getJSONObject(j).getString("jeaccountname")));
                        GLDataLinesRootElement.appendChild(getElements(doc, "TransactionDescription", "OPENING BALANCE"));
                        GLDataLinesRootElement.appendChild(getElements(doc, "EntityName", ""));
                        GLDataLinesRootElement.appendChild(getElements(doc, "TransactionID", "0"));
                        GLDataLinesRootElement.appendChild(getElements(doc, "SourceDocumentID", "0"));
                        GLDataLinesRootElement.appendChild(getElements(doc, "SourceType", "0"));
                        GLDataLinesRootElement.appendChild(getElements(doc, "Debit", "0"));
                        GLDataLinesRootElement.appendChild(getElements(doc, "Credit", "0"));
                        GLDataLinesRootElement.appendChild(getElements(doc, "RunningBalance", authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("openingbalnace"), companyid)));

                        totalLCount += 1;
                        subtotal = jeArray.getJSONObject(j).getDouble("openingbalnace");
                        totalOpeningBalance += jeArray.getJSONObject(j).getDouble("openingbalnace");
                    }
                    subtotal += (-jeArray.getJSONObject(j).getDouble("credit")) + jeArray.getJSONObject(j).getDouble("debit");

                    Element GLDataLinesRootElement = doc.createElement("GLDataLines");
                    GLDataRootElement.appendChild(GLDataLinesRootElement);
                    GLDataLinesRootElement.appendChild(getElements(doc, "TransactionDate", sdf.format(new Date(new Long(jeArray.getJSONObject(j).getString("jedate"))))));
                    GLDataLinesRootElement.appendChild(getElements(doc, "AccountID", jeArray.getJSONObject(j).optString("jeaccountid", "")));
                    GLDataLinesRootElement.appendChild(getElements(doc, "AccountType", jeArray.getJSONObject(j).optString("accounttype", "")));
                    GLDataLinesRootElement.appendChild(getElements(doc, "AccountName", jeArray.getJSONObject(j).getString("jeaccountname")));
                    GLDataLinesRootElement.appendChild(getElements(doc, "TransactionDescription", jeArray.getJSONObject(j).optString("jedesc", "")));
                    GLDataLinesRootElement.appendChild(getElements(doc, "EntityName", companyName));
                    GLDataLinesRootElement.appendChild(getElements(doc, "TransactionID", jeArray.getJSONObject(j).getString("jeid")));
                    GLDataLinesRootElement.appendChild(getElements(doc, "SourceDocumentID", jeArray.getJSONObject(j).getString("sourcedocid")));
                    GLDataLinesRootElement.appendChild(getElements(doc, "SourceType", jeArray.getJSONObject(j).getString("sourcetype")));
                    GLDataLinesRootElement.appendChild(getElements(doc, "Debit", authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("debit"), companyid)));
                    GLDataLinesRootElement.appendChild(getElements(doc, "Credit", authHandler.formattedAmount(jeArray.getJSONObject(j).getDouble("credit"), companyid)));
                    GLDataLinesRootElement.appendChild(getElements(doc, "RunningBalance", authHandler.formattedAmount(subtotal, companyid)));

                    debit += (jeArray.getJSONObject(j).getDouble("debit"));
                    credit += (jeArray.getJSONObject(j).getDouble("credit"));
//                    total += (-jeArray.getJSONObject(j).getDouble("credit")) + jeArray.getJSONObject(j).getDouble("debit");
                }
            }
            Attr SumLedgerDebit = doc.createAttribute("SumLedgerDebit");
            SumLedgerDebit.setValue(authHandler.formattedAmount(debit, companyid));
            GLDataRootElement.setAttributeNode(SumLedgerDebit);

            Attr SumLedgerCredit = doc.createAttribute("SumLedgerCredit");
            SumLedgerCredit.setValue(authHandler.formattedAmount(credit, companyid));
            GLDataRootElement.setAttributeNode(SumLedgerCredit);

            Attr CountLedgerRecord = doc.createAttribute("CountLedgerRecord");
            CountLedgerRecord.setValue("" + totalLCount);
            GLDataRootElement.setAttributeNode(CountLedgerRecord);

            Attr SumLedgerClosingBalance = doc.createAttribute("SumLedgerClosingBalance");
            SumLedgerClosingBalance.setValue(authHandler.formattedAmount(debit - credit + totalOpeningBalance, companyid));
            GLDataRootElement.setAttributeNode(SumLedgerClosingBalance);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl:generateXMLGAFV1" + ex.getMessage(), ex);
        }
        return doc;
    }

    private static Node getElements(Document doc, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }

    public JSONArray getCalculatedPurchaseTaxforIAFfile(Map<String, Object> requestParams, List taxList) throws ServiceException, ParseException {
        JSONArray jArr = new JSONArray();
        try {
            JSONArray grjArr;
            JSONObject purchases;
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            String companyCurrencyId = company.getCurrency().getCurrencyID();
            boolean isMalasianCompany = company.getCountry().getID().equalsIgnoreCase("" + Constants.malaysian_country_id);
            boolean isAdvanceSearch = requestParams.containsKey(Constants.isAdvanceSearch) ? (Boolean) requestParams.get(Constants.isAdvanceSearch) : false;

            Iterator itr = taxList.iterator();
            while (itr.hasNext()) {
                grjArr = new JSONArray();
                Object[] row = (Object[]) itr.next();
                Tax taxObj = (Tax) row[0];
                TaxList taxListObj = (TaxList) row[1];
                Map<String, Object> filterParams = new HashMap<>();
                filterParams.put("taxid", taxObj.getID());
                if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
                    filterParams.put("startDate", requestParams.get("startDate"));
                    filterParams.put("endDate", requestParams.get("endDate"));
                }
                if (requestParams.containsKey(Constants.companyKey) && requestParams.containsKey(Constants.companyKey)) {
                    filterParams.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                }
                if (requestParams.containsKey("excludeRetailPurchaseInvoice")) {
                    filterParams.put("excludeRetailPurchaseInvoice", requestParams.get("excludeRetailPurchaseInvoice"));
                }
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.purchaseInvoiceSearchJson));
                    filterParams.put(Constants.Filter_Criteria, requestParams.get(Constants.Filter_Criteria));
                    filterParams.put(Constants.fixedAssetsPurchaseInvoiceSearchJson, requestParams.get(Constants.fixedAssetsPurchaseInvoiceSearchJson));
                }
                KwlReturnObject grResult = accGoodsReceiptobj.getCalculatedGRTax(filterParams);
                List<GoodsReceipt> list = grResult.getEntityList();

                //Cal Tax for GR.
                for (GoodsReceipt temp : list) {
                    String documentCurrencyId = temp.getCurrency().getCurrencyID();
                    double taxPercent = taxListObj.getPercent();
                    double originalRowAmtExcludingGST = 0.0, originalTaxAmt = 0.0;

                    JSONArray invoiceTermsDetailsArray = CommonFunctions.getTermDetails(temp.getID(), accGoodsReceiptobj);
                    double termAmount = CommonFunctions.getTotalTermsAmount(invoiceTermsDetailsArray);
                    double termAmountToBeIncluded = 0.0d;
                    if (termAmount != 0) {
                        /**
                         * Creating set with invoice id and mapped terms
                         * information.
                         */
                        Set<String> termsUsedInInvoice = new HashSet<>();
                        //Creating map to put term and it's corresponding amount information.
                        HashMap<String, Double> termAndAmountMapping = new HashMap<>();
                        for (int i = 0; i < invoiceTermsDetailsArray.length(); i++) {
                            JSONObject jsonObj = invoiceTermsDetailsArray.getJSONObject(i);
                            String termIdInJson = jsonObj.getString("id");
                            double termAmountInJson = jsonObj.optDouble("termamount", 0.0);
                            termAndAmountMapping.put(termIdInJson, termAmountInJson);
                            termsUsedInInvoice.add(termIdInJson);
                        }

                        /**
                         * Finding all terms in system mapped with tax.
                         */
                        List termList = accTaxDAOobj.getTerms(taxObj.getID());
                        String termid = "";
                        Iterator itr1 = termList.iterator();
                        while (itr1.hasNext()) {
                            KwlReturnObject termResult = accountingHandlerDAOobj.getObject(InvoiceTermsSales.class.getName(), itr1.next().toString());
                            InvoiceTermsSales invoiceTermsSales = (InvoiceTermsSales) termResult.getEntityList().get(0);

                            if (invoiceTermsSales != null) {
                                termid = invoiceTermsSales.getId();
                                /**
                                 * If this mapped term is used in invoice.
                                 */
                                if (termsUsedInInvoice.contains(termid)) {
                                    double termAmountToAdd = termAndAmountMapping.get(termid);
                                    termAmountToBeIncluded += termAmountToAdd;
                                }
                            }
                        }
                    }
                    Set<GoodsReceiptDetail> rows = temp.getRows();
                    for (GoodsReceiptDetail grDetail : rows) {
                        double quantity = grDetail.getInventory().getQuantity();
                        double amount = authHandler.round(grDetail.getRate() * quantity, companyid);
                        double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());

                        originalRowAmtExcludingGST = amount - rdisc;//Original amount excluding gst.
                        originalTaxAmt = (originalRowAmtExcludingGST * taxPercent) / 100;//Original tax amount.

                        double rowAmtExGSTInbase = originalRowAmtExcludingGST;
                        double taxAmtInBase = originalTaxAmt;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExcludingGST, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            rowAmtExGSTInbase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalTaxAmt, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            taxAmtInBase = (Double) bAmt.getEntityList().get(0);//Tax amount in base excluding gst.
                        }

                        purchases = new JSONObject();
                        purchases.put("grdate", temp.getCreationDate().getTime());
                        purchases.put("grname", temp.getVendor().getName());
                        purchases.put("grno", temp.getGoodsReceiptNumber());
                        purchases.put("gramt", rowAmtExGSTInbase + taxAmtInBase);
                        purchases.put("gramtexcludinggst", rowAmtExGSTInbase);
                        purchases.put("grtaxamount", taxAmtInBase);
                        purchases.put("grlineno", grDetail.getSrno());
                        purchases.put("grproduct", !StringUtil.isNullOrEmpty(grDetail.getInventory().getProduct().getDescription()) ? grDetail.getInventory().getProduct().getDescription() : grDetail.getInventory().getProduct().getProductName());
                        purchases.put("grtaxcode", temp.getTax().getTaxCode());
                        purchases.put("gruen", (isMalasianCompany) ? temp.getVendor().getCompanyRegistrationNumber() : temp.getVendor().getAltContactNumber());
                        purchases.put("fcycode", temp.getCurrency().getCurrencyCode());
                        purchases.put("purchasefcy", originalRowAmtExcludingGST + originalTaxAmt);
                        purchases.put("purchasefcyexcludinggst", originalRowAmtExcludingGST);
                        purchases.put("gstfcy", originalTaxAmt);
                        purchases.put("permit", grDetail.getPermit() != null ? grDetail.getPermit() : "");
                        purchases.put("suppliergstno", (temp.getVendor() != null && temp.getVendor().getGstRegistrationNumber() != null) ? temp.getVendor().getGstRegistrationNumber() : "");
                        purchases.put("postingdate", temp.getJournalEntry().getEntryDate().getTime());//Posting date same as JE date.
                        purchases.put(Constants.importExportDeclarationNo, temp.getImportDeclarationNo() != null ? temp.getImportDeclarationNo() : "");
                        grjArr.put(purchases);

                    }

                    if (termAmountToBeIncluded != 0) {
                        double termTaxAmount = termAmountToBeIncluded * taxPercent / 100;
                        double termAmtInBase = termAmountToBeIncluded;
                        double termTaxAmtInBase = termTaxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmountToBeIncluded, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            termAmtInBase = (Double) bAmt.getEntityList().get(0);//Term amount in base excluding gst.

                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termTaxAmount, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            termTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Term tax amount in base excluding gst.
                        }
                        purchases = new JSONObject();
                        purchases.put("grdate", temp.getCreationDate().getTime());
                        purchases.put("grname", temp.getVendor().getName());
                        purchases.put("grno", temp.getGoodsReceiptNumber());
                        purchases.put("gramt", termAmtInBase + termTaxAmtInBase);
                        purchases.put("gramtexcludinggst", termAmtInBase);
                        purchases.put("grtaxamount", termTaxAmtInBase);
                        purchases.put("grlineno", 1);
                        purchases.put("grproduct", "");
                        purchases.put("grtaxcode", temp.getTax().getTaxCode());
                        purchases.put("gruen", (isMalasianCompany) ? temp.getVendor().getCompanyRegistrationNumber() : temp.getVendor().getAltContactNumber());
                        purchases.put("fcycode", temp.getCurrency().getCurrencyCode());
                        purchases.put("purchasefcy", termAmountToBeIncluded + termTaxAmount);
                        purchases.put("purchasefcyexcludinggst", termAmountToBeIncluded);
                        purchases.put("gstfcy", termTaxAmount);
                        purchases.put("permit", "");
                        purchases.put("suppliergstno", (temp.getVendor() != null && temp.getVendor().getGstRegistrationNumber() != null) ? temp.getVendor().getGstRegistrationNumber() : "");
                        purchases.put("postingdate", temp.getJournalEntry().getEntryDate().getTime());//Posting date same as JE date.
                        purchases.put(Constants.importExportDeclarationNo, temp.getImportDeclarationNo() != null ? temp.getImportDeclarationNo() : "");
                        grjArr.put(purchases);
                    }

                    Set<ExpenseGRDetail> expenseRows = temp.getExpenserows();
                    for (ExpenseGRDetail expenseGRDetail : expenseRows) {
                        originalRowAmtExcludingGST = expenseGRDetail.getAmount(); //Original amount excluding gst.
                        originalTaxAmt = (originalRowAmtExcludingGST * taxPercent) / 100;//Original tax amount.

                        double salesAmtExGSTInbase = originalRowAmtExcludingGST;
                        double taxAmtInBase = originalTaxAmt;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExcludingGST, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            salesAmtExGSTInbase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.

                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalTaxAmt, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            taxAmtInBase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                        }
                        purchases = new JSONObject();
                        purchases.put("grdate", temp.getCreationDate().getTime());
                        purchases.put("grname", temp.getVendor().getName());
                        purchases.put("grno", temp.getGoodsReceiptNumber());
                        purchases.put("gramt", salesAmtExGSTInbase + taxAmtInBase);
                        purchases.put("gramtexcludinggst", salesAmtExGSTInbase);
                        purchases.put("grtaxamount", taxAmtInBase);
                        purchases.put("grlineno", expenseGRDetail.getSrno());
                        purchases.put("grproduct", expenseGRDetail.getAccount().getAccountName());
                        purchases.put("grtaxcode", temp.getTax().getTaxCode());
                        purchases.put("gruen", (isMalasianCompany) ? temp.getVendor().getCompanyRegistrationNumber() : temp.getVendor().getAltContactNumber());
                        purchases.put("fcycode", temp.getCurrency().getCurrencyCode());
                        purchases.put("purchasefcy", originalRowAmtExcludingGST + originalTaxAmt);
                        purchases.put("purchasefcyexcludinggst", originalRowAmtExcludingGST);
                        purchases.put("gstfcy", originalTaxAmt);
                        purchases.put("permit", "");
                        purchases.put("suppliergstno", (temp.getVendor() != null && temp.getVendor().getGstRegistrationNumber() != null) ? temp.getVendor().getGstRegistrationNumber() : "");
                        purchases.put("postingdate", temp.getJournalEntry().getEntryDate().getTime());//Posting date same as JE date.
                        purchases.put(Constants.importExportDeclarationNo, temp.getImportDeclarationNo() != null ? temp.getImportDeclarationNo() : "");
                        grjArr.put(purchases);
                    }
                }

                //Cal Tax for GR Details.
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.purchaseInvoiceSearchJson));
                    filterParams.put(Constants.fixedAssetsPurchaseInvoiceSearchJson, requestParams.get(Constants.fixedAssetsPurchaseInvoiceSearchJson));
                }
                KwlReturnObject result = accGoodsReceiptobj.getCalculatedGRDtlTax(filterParams);
                List<GoodsReceiptDetail> GRList = result.getEntityList();
                for (GoodsReceiptDetail temp : GRList) {
                    String documentCurrencyId = temp.getGoodsReceipt().getCurrency().getCurrencyID();
                    double originalRowAmtExcludingGST = temp.getRowExcludingGstAmount();
                    double originalRowTaxAmt = temp.getRowTaxAmount();
                    double rowAmtInbase = temp.getRowExcludingGstAmountInBase();
                    double rowTaxAmtInBase = temp.getRowTaxAmountInBase();

                    Map<String, Double> grtermdetails = accGoodsReceiptCommon.getGRDetailsTermAmount(temp);
                    double termAmount = grtermdetails.get("termamount");//term amount in transaction currency.
                    double termTaxAmount = grtermdetails.get("taxamount");//term tax amount in transaction currency.
                    double termAmountInbase = termAmount;
                    double termTaxAmtInbase = termTaxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, documentCurrencyId, temp.getGoodsReceipt().getCreationDate(), temp.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate());
                        termAmountInbase = (Double) bAmt.getEntityList().get(0);

                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termTaxAmount, documentCurrencyId, temp.getGoodsReceipt().getCreationDate(), temp.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate());
                        termTaxAmtInbase = (Double) bAmt.getEntityList().get(0);
                    }
                    purchases = new JSONObject();
                    purchases.put("grdate", temp.getGoodsReceipt().getCreationDate().getTime());
                    purchases.put("grname", temp.getGoodsReceipt().getVendor().getName());
                    purchases.put("grno", temp.getGoodsReceipt().getGoodsReceiptNumber());
                    purchases.put("gramt", rowAmtInbase + rowTaxAmtInBase + termAmountInbase + termTaxAmtInbase);
                    purchases.put("gramtexcludinggst", rowAmtInbase + termAmountInbase);
                    purchases.put("grtaxamount", rowTaxAmtInBase + termTaxAmtInbase);
                    purchases.put("grnamegst", temp.getGoodsReceipt().getVendor().getOther());
                    purchases.put("grlineno", temp.getSrno());
                    purchases.put("grproduct", temp.getInventory().getProduct() != null ? (!StringUtil.isNullOrEmpty(temp.getInventory().getProduct().getDescription()) ? temp.getInventory().getProduct().getDescription() : temp.getInventory().getProduct().getName()) : "");
                    purchases.put("grtaxcode", temp.getTax().getTaxCode());
                    purchases.put("gruen", (isMalasianCompany) ? temp.getGoodsReceipt().getVendor().getCompanyRegistrationNumber() : temp.getGoodsReceipt().getVendor().getAltContactNumber());
                    purchases.put("fcycode", temp.getGoodsReceipt().getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", originalRowAmtExcludingGST + originalRowTaxAmt + termAmount + termTaxAmount);
                    purchases.put("purchasefcyexcludinggst", originalRowAmtExcludingGST + termAmount);
                    purchases.put("gstfcy", originalRowTaxAmt + termTaxAmount);
                    purchases.put("permit", temp.getPermit() != null ? temp.getPermit() : "");
                    purchases.put("suppliergstno", (temp.getGoodsReceipt().getVendor() != null && temp.getGoodsReceipt().getVendor().getGstRegistrationNumber() != null) ? temp.getGoodsReceipt().getVendor().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", temp.getGoodsReceipt().getJournalEntry().getEntryDate().getTime());//Posting date same as JE date.
                    purchases.put(Constants.importExportDeclarationNo, temp.getGoodsReceipt().getImportDeclarationNo() != null ? temp.getGoodsReceipt().getImportDeclarationNo() : "");
                    grjArr.put(purchases);
                }

                //Cal Tax for Expense GR Details[PS]
                result = accGoodsReceiptobj.getCalculatedExpenseGRDtlTax(filterParams);
                List<ExpenseGRDetail> expList = result.getEntityList();
                for (ExpenseGRDetail temp : expList) {
                    String documentCurrencyId = temp.getGoodsReceipt().getCurrency().getCurrencyID();
                    double originalRowAmtIncludingGST = temp.getAmount();
                    double originalRowTaxAmount = temp.getRowTaxAmount();

                    double rowAmtIncludingGSTInbase = originalRowAmtIncludingGST;
                    double rowTaxAmtInBase = originalRowTaxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {//To getamount in companycurrency.
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtIncludingGST, documentCurrencyId, temp.getGoodsReceipt().getCreationDate(), temp.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate());
                        rowAmtIncludingGSTInbase = (Double) bAmt.getEntityList().get(0);//Amount in base including gst.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowTaxAmount, documentCurrencyId, temp.getGoodsReceipt().getCreationDate(), temp.getGoodsReceipt().getJournalEntry().getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Tax amount in base.
                    }
                    purchases = new JSONObject();
                    purchases.put("grdate", temp.getGoodsReceipt().getCreationDate().getTime());
                    purchases.put("grname", temp.getGoodsReceipt().getVendor().getName());
                    purchases.put("grno", temp.getGoodsReceipt().getGoodsReceiptNumber());
                    purchases.put("gramt", rowAmtIncludingGSTInbase);
                    purchases.put("gramtexcludinggst", (rowAmtIncludingGSTInbase - rowTaxAmtInBase));
                    purchases.put("grtaxamount", rowTaxAmtInBase);
                    purchases.put("grnamegst", temp.getGoodsReceipt().getVendor().getOther());
                    purchases.put("grlineno", temp.getSrno());
                    purchases.put("grproduct", temp.getAccount().getName());
                    purchases.put("grtaxcode", temp.getTax().getTaxCode());
                    purchases.put("gruen", (isMalasianCompany) ? temp.getGoodsReceipt().getVendor().getCompanyRegistrationNumber() : temp.getGoodsReceipt().getVendor().getAltContactNumber());
                    purchases.put("fcycode", temp.getGoodsReceipt().getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", originalRowAmtIncludingGST);
                    purchases.put("purchasefcyexcludinggst", originalRowAmtIncludingGST - originalRowTaxAmount);
                    purchases.put("gstfcy", originalRowTaxAmount);
                    purchases.put("permit", "");
                    purchases.put("postingdate", temp.getGoodsReceipt().getJournalEntry().getEntryDate().getTime());//Posting date same as JE date.
                    purchases.put(Constants.importExportDeclarationNo, temp.getGoodsReceipt().getImportDeclarationNo() != null ? temp.getGoodsReceipt().getImportDeclarationNo() : "");
                    grjArr.put(purchases);
                }

                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.debitNoteSearchJson));
                }
                result = accGoodsReceiptobj.getCalculatedDNTax(filterParams);
                List<DebitNote> dnList = result.getEntityList();
                for (DebitNote debitMemo : dnList) {
                    double dnAmount = 0, taxAmount = 0;
                    KwlReturnObject result1 = accJournalEntryDAOobj.getJournalEntryDetail(debitMemo.getJournalEntry().getID(), debitMemo.getJournalEntry().getCompany().getCompanyID());
                    Iterator iterator = result1.getEntityList().iterator();
                    boolean taxflag = false;
                    while (iterator.hasNext()) {
                        JournalEntryDetail jed = (JournalEntryDetail) iterator.next();
                        Account account = jed.getAccount();
                        if (account.getGroup().getID().equals(Group.OTHER_CURRENT_LIABILITIES)) {
                            if (!jed.isDebit() && account.getID().equals(taxObj.getAccount().getID())) {
                                taxAmount = jed.getAmount();
                                taxflag = true;
                            }
                        }
                        if (jed.isDebit()) {
                            dnAmount += jed.getAmount();
                        }
                    }
                    if (taxflag) {
                        String documentCurrencyId = debitMemo.getJournalEntry().getCurrency().getCurrencyID();
                        double dnAmountInBase = dnAmount;
                        double taxAmountInBase = taxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, dnAmount, documentCurrencyId, debitMemo.getCreationDate(), debitMemo.getJournalEntry().getExternalCurrencyRate());
                            dnAmountInBase = (Double) bAmt1.getEntityList().get(0);

                            bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, taxAmount, documentCurrencyId, debitMemo.getCreationDate(), debitMemo.getJournalEntry().getExternalCurrencyRate());
                            taxAmountInBase = (Double) bAmt1.getEntityList().get(0);
                        }
                        purchases = new JSONObject();
                        purchases.put("grdate", debitMemo.getCreationDate().getTime());
                        purchases.put("grname", debitMemo.getVendor().getName());
                        purchases.put("grno", debitMemo.getDebitNoteNumber());
                        purchases.put("gramt", -dnAmountInBase);
                        purchases.put("gramtexcludinggst", -(dnAmountInBase - taxAmountInBase));
                        purchases.put("grtaxamount", -taxAmountInBase);
                        purchases.put("grnamegst", "");
                        purchases.put("grlineno", 1);
                        purchases.put("grproduct", "");
                        purchases.put("grtaxcode", taxObj.getTaxCode());
                        purchases.put("gruen", (isMalasianCompany) ? ((debitMemo.getVendor() != null) ? debitMemo.getVendor().getCompanyRegistrationNumber() : "") : "");
                        purchases.put("fcycode", debitMemo.getJournalEntry().getCurrency().getCurrencyCode());
                        purchases.put("purchasefcy", -dnAmount);
                        purchases.put("purchasefcyexcludinggst", -(dnAmount - taxAmount));
                        purchases.put("gstfcy", -taxAmount);
                        purchases.put("permit", "");
                        purchases.put("suppliergstno", (debitMemo.getVendor() != null && debitMemo.getVendor().getGstRegistrationNumber() != null) ? debitMemo.getVendor().getGstRegistrationNumber() : "");
                        purchases.put("postingdate", debitMemo.getJournalEntry().getEntryDate().getTime());//Posting date same as document date.
                        purchases.put(Constants.importExportDeclarationNo, "");
                        grjArr.put(purchases);
                    }
                }

                /**
                 * Debit Note For Overcharge PI ERM-778.
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.debitNoteSearchJson));
                    filterParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                }
                result = accGoodsReceiptobj.getCalculatedDebitNoteTax(filterParams);
                List<DebitNote> calculateDNTaxList = result.getEntityList();
                for (DebitNote debitMemo : calculateDNTaxList) {

                    double dnAmount = 0, taxAmount = 0, dnAmountExcludingTax = 0;
                    double dnAmountInBase = 0, taxAmountInBase = 0, dnAmountExcludingTaxInBase = 0;

                    KwlReturnObject result1 = accJournalEntryDAOobj.getJournalEntryDetail(debitMemo.getJournalEntry().getID(), debitMemo.getJournalEntry().getCompany().getCompanyID());
                    List<JournalEntryDetail> entryDetails = result1.getEntityList();
                    boolean taxflag = false;
                    for (JournalEntryDetail jed : entryDetails) {
                        Account account = jed.getAccount();
                        if (!jed.isDebit() && account.getID().equals(taxObj.getAccount().getID())) {
                            taxAmount += jed.getAmount();
                            taxflag = true;
                        }
                        if (jed.isDebit()) {
                            dnAmount += jed.getAmount();
                        }
                    }
                    dnAmountExcludingTax = dnAmount - taxAmount;
                    if (taxflag) {
                        String documentCurrencyId = debitMemo.getJournalEntry().getCurrency().getCurrencyID();
                        dnAmountInBase = dnAmount;
                        taxAmountInBase = taxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, dnAmount, documentCurrencyId, debitMemo.getCreationDate(), debitMemo.getJournalEntry().getExternalCurrencyRate());
                            dnAmountInBase = (Double) bAmt1.getEntityList().get(0);
                            bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, taxAmount, documentCurrencyId, debitMemo.getCreationDate(), debitMemo.getJournalEntry().getExternalCurrencyRate());
                            taxAmountInBase = (Double) bAmt1.getEntityList().get(0);
                        }
                        dnAmountExcludingTaxInBase = dnAmountInBase - taxAmountInBase;

                        purchases = new JSONObject();
                        purchases.put("grdate", debitMemo.getCreationDate().getTime());
                        purchases.put("grname", debitMemo.getVendor().getName());
                        purchases.put("grno", debitMemo.getDebitNoteNumber());
                        purchases.put("gramt", authHandler.round(dnAmountInBase, companyid));
                        purchases.put("gramtexcludinggst", dnAmountExcludingTaxInBase);
                        purchases.put("grtaxamount", taxAmountInBase);
                        purchases.put("grnamegst", "");
                        purchases.put("grlineno", 1);
                        purchases.put("grproduct", "");
                        purchases.put("grtaxcode", taxObj.getTaxCode());
                        purchases.put("gruen", (isMalasianCompany) ? ((debitMemo.getVendor() != null) ? debitMemo.getVendor().getCompanyRegistrationNumber() : "") : "");
                        purchases.put("fcycode", debitMemo.getJournalEntry().getCurrency().getCurrencyCode());
                        purchases.put("purchasefcy", dnAmount);
                        purchases.put("purchasefcyexcludinggst", dnAmountExcludingTax);
                        purchases.put("gstfcy", taxAmount);
                        purchases.put("permit", "");
                        purchases.put("suppliergstno", (debitMemo.getVendor() != null && debitMemo.getVendor().getGstRegistrationNumber() != null) ? debitMemo.getVendor().getGstRegistrationNumber() : "");
                        purchases.put("postingdate", debitMemo.getJournalEntry().getEntryDate().getTime());
                        purchases.put(Constants.importExportDeclarationNo, "");
                        grjArr.put(purchases);
                    }
                }

                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.debitNoteSearchJson));
                }
                result = accGoodsReceiptobj.getCalculatedDNTaxGst(filterParams);
                List<DebitNoteAgainstCustomerGst> dnDetailsList = result.getEntityList();
                for (DebitNoteAgainstCustomerGst temp : dnDetailsList) {
                    String documentCurrencyId = temp.getDebitNote().getCurrency().getCurrencyID();
                    double dnAmount = temp.getRate() * temp.getReturnQuantity();
                    double taxAmount = temp.getRowTaxAmount();
                    double discountAmt = temp.getDiscount();
                    dnAmount = dnAmount - discountAmt;
                    double dnAmountInBase = dnAmount;
                    double taxAmountInBase = taxAmount;

                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, dnAmount, documentCurrencyId, temp.getDebitNote().getCreationDate(), temp.getDebitNote().getJournalEntry().getExternalCurrencyRate());
                        dnAmountInBase = (Double) bAmt.getEntityList().get(0);
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, documentCurrencyId, temp.getDebitNote().getCreationDate(), temp.getDebitNote().getJournalEntry().getExternalCurrencyRate());
                        taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    double dnAmountExcludingTax = dnAmount;
                    double dnAmountExcludingTaxInBase = dnAmountInBase;

                    purchases = new JSONObject();
                    purchases.put("grdate", temp.getDebitNote().getCreationDate().getTime());
                    purchases.put("grname", temp.getDebitNote().getVendor().getName());
                    purchases.put("grno", temp.getDebitNote().getDebitNoteNumber());
                    purchases.put("gramt", authHandler.round(dnAmountInBase, companyid));
                    purchases.put("gramtexcludinggst", dnAmountExcludingTaxInBase);
                    purchases.put("grtaxamount", taxAmountInBase);
                    purchases.put("grnamegst", "");
                    purchases.put("grlineno", temp.getSrno());
                    purchases.put("grproduct", temp.getProduct() != null ? (!StringUtil.isNullOrEmpty(temp.getProduct().getDescription()) ? temp.getProduct().getDescription() : temp.getProduct().getProductName()) : "");
                    purchases.put("grtaxcode", taxObj.getTaxCode());
                    purchases.put("gruen", (isMalasianCompany) ? ((temp.getDebitNote().getVendor() != null) ? temp.getDebitNote().getVendor().getCompanyRegistrationNumber() : "") : "");
                    purchases.put("fcycode", temp.getDebitNote().getJournalEntry().getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", dnAmount);
                    purchases.put("purchasefcyexcludinggst", dnAmountExcludingTax);
                    purchases.put("gstfcy", taxAmount);
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", (temp.getDebitNote().getVendor() != null && temp.getDebitNote().getVendor().getGstRegistrationNumber() != null) ? temp.getDebitNote().getVendor().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", temp.getDebitNote().getJournalEntry().getEntryDate().getTime());
                    purchases.put(Constants.importExportDeclarationNo, "");
                    grjArr.put(purchases);
                }

                /**
                 * Make Payment Otherwise Tax Calculation.
                 *
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.makePaymentSearchJson));
                }
                result = accVendorPaymentDAOobj.getCalculatedMakePaymentOtherwiseTax(filterParams);
                List<PaymentDetailOtherwise> pdoList = result.getEntityList();
                for (PaymentDetailOtherwise temp : pdoList) {
                    String documentCurrencyId = temp.getPayment().getCurrency().getCurrencyID();
                    double taxAmount = temp.getTaxamount();
                    double ramount = temp.getAmount();
                    double ramountExGSTInbase = ramount;
                    double taxAmountInbase = taxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, ramount, documentCurrencyId, temp.getPayment().getCreationDate(), temp.getPayment().getJournalEntry().getExternalCurrencyRate());                        //Converting into base [PS]
                        ramountExGSTInbase = (Double) bAmt.getEntityList().get(0);

                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, documentCurrencyId, temp.getPayment().getCreationDate(), temp.getPayment().getJournalEntry().getExternalCurrencyRate());
                        taxAmountInbase = (Double) bAmt.getEntityList().get(0);
                    }
                    purchases = new JSONObject();
                    purchases.put("grdate", temp.getPayment().getCreationDate().getTime());
                    purchases.put("grname", temp.getAccount().getName());
                    purchases.put("grno", temp.getPayment().getPaymentNumber());
                    purchases.put("gramt", (ramountExGSTInbase + taxAmountInbase));
                    purchases.put("gramtexcludinggst", ramountExGSTInbase);
                    purchases.put("grtaxamount", taxAmountInbase);
                    purchases.put("grlineno", temp.getSrNoForRow());
                    purchases.put("grproduct", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    purchases.put("grtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : (temp.getGstapplied() != null ? temp.getGstapplied().getTaxCode() : temp.getAccount().getName()));
                    purchases.put("gruen", (isMalasianCompany) ? ((temp.getPayment().getVendor() != null) ? temp.getPayment().getVendor().getCompanyRegistrationNumber() : "") : "");
                    purchases.put("fcycode", temp.getPayment().getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", ramount + taxAmount);
                    purchases.put("purchasefcyexcludinggst", ramount);
                    purchases.put("gstfcy", taxAmount);
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", (temp.getPayment().getVendor() != null && temp.getPayment().getVendor().getGstRegistrationNumber() != null) ? temp.getPayment().getVendor().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", temp.getPayment().getJournalEntry().getEntryDate().getTime());//Posting date same as document date.
                    purchases.put(Constants.importExportDeclarationNo, "");
                    grjArr.put(purchases);
                }

                /**
                 * Debit Note Otherwise Tax Calculation.
                 *
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.debitNoteSearchJson));
                }
                result = accVendorPaymentDAOobj.getCalculatedDebitNoteOtherwiseTax(filterParams);
                List<DebitNoteTaxEntry> entityList = result.getEntityList();
                for (DebitNoteTaxEntry temp : entityList) {
                    String documentCurrencyId = temp.getDebitNote().getCurrency().getCurrencyID();
                    double taxAmount = 0;
                    double taxAmountInOriginalCurrency = 0;
                    double cnSalesAmt = 0.0;
                    if (!temp.getDebitNote().isIncludingGST()) {
                        cnSalesAmt = (Double) temp.getAmount();
                    } else {
                        cnSalesAmt = (Double) temp.getRateIncludingGst();
                    }
                    double cnSalesAmtInOriginalCurrency = cnSalesAmt;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, cnSalesAmt, temp.getDebitNote().getCurrency().getCurrencyID(), temp.getDebitNote().getCreationDate(), temp.getDebitNote().getJournalEntry().getExternalCurrencyRate());
                        cnSalesAmt = (Double) bAmt.getEntityList().get(0);
                    }

                    if (temp.getTax() != null) {
                        taxAmount = temp.getTaxamount();
                        taxAmountInOriginalCurrency = taxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bTaxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, temp.getDebitNote().getCurrency().getCurrencyID(), temp.getDebitNote().getCreationDate(), temp.getDebitNote().getJournalEntry().getExternalCurrencyRate());
                            taxAmount = (Double) bTaxAmt.getEntityList().get(0);
                        }
                    }
                    if (temp.getAccount().getID().equals(taxObj.getAccount().getID())) {
                        taxAmount += cnSalesAmt;
                        taxAmountInOriginalCurrency += cnSalesAmtInOriginalCurrency;
                        cnSalesAmt = 0;
                        cnSalesAmtInOriginalCurrency = 0;
                    }

                    purchases = new JSONObject();
                    purchases.put("grdate", temp.getDebitNote().getCreationDate().getTime());
                    purchases.put("grname", temp.getAccount().getName());
                    purchases.put("grno", temp.getDebitNote().getDebitNoteNumber());
                    purchases.put("gramt", cnSalesAmt + (taxAmount));
                    purchases.put("gramtexcludinggst", cnSalesAmt);
                    purchases.put("grtaxamount", (taxAmount));
                    purchases.put("grlineno", temp.getSrNoForRow());
                    purchases.put("grproduct", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    purchases.put("grtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : temp.getAccount().getName());
                    purchases.put("gruen", (isMalasianCompany) ? ((temp.getDebitNote().getVendor() != null) ? temp.getDebitNote().getVendor().getCompanyRegistrationNumber() : "") : "");
                    purchases.put("fcycode", temp.getDebitNote().getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", cnSalesAmtInOriginalCurrency + (taxAmountInOriginalCurrency));
                    purchases.put("purchasefcyexcludinggst", cnSalesAmtInOriginalCurrency);
                    purchases.put("gstfcy", (taxAmountInOriginalCurrency));
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", (temp.getDebitNote().getVendor() != null && temp.getDebitNote().getVendor().getGstRegistrationNumber() != null) ? temp.getDebitNote().getVendor().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", temp.getDebitNote().getJournalEntry().getEntryDate().getTime());//Posting date same as document date.
                    purchases.put(Constants.importExportDeclarationNo, "");
                    grjArr.put(purchases);
                }

                /**
                 * Credit Note against Vendor Tax calculation.
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.creditNoteSearchJson));
                }
                result = accInvoiceDAOobj.getCalculatedCreditNoteTax(filterParams);
                List<CreditNote> calculatedCNTaxList = result.getEntityList();
                for (CreditNote creditMemo : calculatedCNTaxList) {
                    String documentCurrencyId = creditMemo.getCurrency().getCurrencyID();
                    double originalCNAmountWithTax = 0, originalCNAmountWithoutTax = 0, originalTaxAmount = 0;
                    double cnAmtWithoutTaxInBase = 0, taxAmountInBase = 0, totalAmtWithTaxInBase = 0;

                    KwlReturnObject result1 = accJournalEntryDAOobj.getJournalEntryDetail(creditMemo.getJournalEntry().getID(), creditMemo.getJournalEntry().getCompany().getCompanyID());
                    List<JournalEntryDetail> entryDetails = result1.getEntityList();
                    boolean taxflag = false;
                    for (JournalEntryDetail jed : entryDetails) {
                        Account account = jed.getAccount();
                        if (jed.isDebit() && account.getID().equals(taxObj.getAccount().getID())) {
                            originalTaxAmount += jed.getAmount();
                            taxflag = true;
                        }
                        if (jed.isDebit()) {
                            originalCNAmountWithTax += jed.getAmount();
                        }
                    }

                    originalCNAmountWithoutTax = originalCNAmountWithTax - originalTaxAmount;
                    if (taxflag) {
                        cnAmtWithoutTaxInBase = originalCNAmountWithoutTax;
                        taxAmountInBase = originalTaxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalCNAmountWithoutTax, documentCurrencyId, creditMemo.getCreationDate(), creditMemo.getJournalEntry().getExternalCurrencyRate());
                            cnAmtWithoutTaxInBase = (Double) bAmt1.getEntityList().get(0);

                            bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalTaxAmount, documentCurrencyId, creditMemo.getCreationDate(), creditMemo.getJournalEntry().getExternalCurrencyRate());
                            taxAmountInBase = (Double) bAmt1.getEntityList().get(0);
                        }
                        totalAmtWithTaxInBase = cnAmtWithoutTaxInBase + taxAmountInBase;

                        purchases = new JSONObject();
                        purchases.put("grdate", creditMemo.getCreationDate().getTime());
                        purchases.put("grname", creditMemo.getVendor().getName());
                        purchases.put("grno", creditMemo.getCreditNoteNumber());
                        purchases.put("gramt", totalAmtWithTaxInBase);
                        purchases.put("gramtexcludinggst", cnAmtWithoutTaxInBase);
                        purchases.put("grtaxamount", taxAmountInBase);
                        purchases.put("grlineno", 1);
                        purchases.put("grproduct", "");
                        purchases.put("grtaxcode", creditMemo.getTax().getTaxCode());
                        purchases.put("gruen", (isMalasianCompany) ? ((creditMemo.getVendor() != null) ? creditMemo.getVendor().getCompanyRegistrationNumber() : "") : "");
                        purchases.put("fcycode", creditMemo.getCurrency().getCurrencyCode());
                        purchases.put("purchasefcy", originalCNAmountWithTax);
                        purchases.put("purchasefcyexcludinggst", originalCNAmountWithoutTax);
                        purchases.put("gstfcy", originalTaxAmount);
                        purchases.put("permit", "");
                        purchases.put("suppliergstno", (creditMemo.getVendor() != null && creditMemo.getVendor().getGstRegistrationNumber() != null) ? creditMemo.getVendor().getGstRegistrationNumber() : "");
                        purchases.put("postingdate", creditMemo.getJournalEntry().getEntryDate().getTime());//Posting date same as document date.
                        purchases.put(Constants.importExportDeclarationNo, "");
                        grjArr.put(purchases);
                    }
                }

                result = accGoodsReceiptobj.getCalculatedCNTaxGst(filterParams);
                List<CreditNoteAgainstVendorGst> list12 = result.getEntityList();
                for (CreditNoteAgainstVendorGst temp : list12) {
                    String documentCurrencyId = temp.getCreditNote().getCurrency().getCurrencyID();
                    double originalCNAmtWithoutTax = (temp.getRate() * temp.getReturnQuantity());
                    double originalTaxAmount = temp.getRowTaxAmount();
                    double cnAmtWithoutTaxInBase = 0, taxAmountInBase = 0;

                    cnAmtWithoutTaxInBase = originalCNAmtWithoutTax;
                    taxAmountInBase = originalTaxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, originalCNAmtWithoutTax, documentCurrencyId, temp.getCreditNote().getCreationDate(), temp.getCreditNote().getJournalEntry().getExternalCurrencyRate());
                        cnAmtWithoutTaxInBase = (Double) bAmt.getEntityList().get(0);

                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, originalTaxAmount, documentCurrencyId, temp.getCreditNote().getCreationDate(), temp.getCreditNote().getJournalEntry().getExternalCurrencyRate());
                        taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }

                    purchases = new JSONObject();
                    purchases.put("grdate", temp.getCreditNote().getCreationDate().getTime());
                    purchases.put("grname", temp.getCreditNote().getVendor().getName());
                    purchases.put("grno", temp.getCreditNote().getCreditNoteNumber());
                    purchases.put("gramt", cnAmtWithoutTaxInBase);
                    purchases.put("gramtexcludinggst", cnAmtWithoutTaxInBase);
                    purchases.put("grtaxamount", taxAmountInBase);
                    purchases.put("grlineno", temp.getSrno());
                    purchases.put("grproduct", temp.getProduct() != null ? (!StringUtil.isNullOrEmpty(temp.getProduct().getDescription()) ? temp.getProduct().getDescription() : temp.getProduct().getName()) : "");
                    purchases.put("grtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : "");
                    purchases.put("gruen", (isMalasianCompany) ? ((temp.getCreditNote().getVendor() != null) ? temp.getCreditNote().getVendor().getCompanyRegistrationNumber() : "") : "");
                    purchases.put("fcycode", temp.getCreditNote().getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", (originalCNAmtWithoutTax + originalTaxAmount));
                    purchases.put("purchasefcyexcludinggst", originalCNAmtWithoutTax);
                    purchases.put("gstfcy", originalTaxAmount);
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", (temp.getCreditNote().getVendor() != null && temp.getCreditNote().getVendor().getGstRegistrationNumber() != null) ? temp.getCreditNote().getVendor().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", temp.getCreditNote().getJournalEntry().getEntryDate().getTime());//Posting date same as document date.
                    purchases.put(Constants.importExportDeclarationNo, "");
                    grjArr.put(purchases);
                }

                /**
                 * Receive Payment For Otherwise With Purchase Tax Calculation.
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.receivePaymentSearchJson));
                }
                result = accReceiptDAOobj.getCalculatedReceivePaymentOtherwiseTax(filterParams);
                List<ReceiptDetailOtherwise> rdoList = result.getEntityList();
                for (ReceiptDetailOtherwise temp : rdoList) {
                    String documentCurrencyId = temp.getReceipt().getCurrency().getCurrencyID();
                    double ramount = temp.getAmount();
                    double taxAmount = temp.getTaxamount();
                    double ramountInBase = ramount;
                    double taxAmountInBase = taxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, ramount, documentCurrencyId, temp.getReceipt().getCreationDate(), temp.getReceipt().getJournalEntry().getExternalCurrencyRate());                        //Converting into base [PS]
                        ramountInBase = (Double) bAmt.getEntityList().get(0);

                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, documentCurrencyId, temp.getReceipt().getCreationDate(), temp.getReceipt().getJournalEntry().getExternalCurrencyRate());
                        taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }

                    purchases = new JSONObject();
                    purchases.put("grdate", temp.getReceipt().getCreationDate().getTime());
                    purchases.put("grname", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    purchases.put("grno", temp.getReceipt().getReceiptNumber());
                    purchases.put("gramt", (ramountInBase + taxAmountInBase));
                    purchases.put("gramtexcludinggst", ramountInBase);
                    purchases.put("grtaxamount", taxAmountInBase);
                    purchases.put("grlineno", temp.getSrNoForRow());
                    purchases.put("grproduct", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    purchases.put("grtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : (temp.getGstapplied() != null ? temp.getGstapplied().getTaxCode() : temp.getAccount().getName()));
                    purchases.put("gruen", (temp.getReceipt().getCustomer() != null && temp.getReceipt().getCustomer().getCompanyRegistrationNumber() != null) ? temp.getReceipt().getCustomer().getCompanyRegistrationNumber() : "");
                    purchases.put("fcycode", temp.getReceipt().getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", (ramount + taxAmount));
                    purchases.put("purchasefcyexcludinggst", ramount);
                    purchases.put("gstfcy", taxAmount);
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", (temp.getReceipt().getCustomer() != null && temp.getReceipt().getCustomer().getGstRegistrationNumber() != null) ? temp.getReceipt().getCustomer().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", temp.getReceipt().getJournalEntry().getEntryDate().getTime());//Posting date same as journal entry date.
                    purchases.put(Constants.importExportDeclarationNo, "");
                    grjArr.put(purchases);
                }

                /**
                 * Advance Receipt With Purchase Tax calculation.
                 */
                result = accReceiptDAOobj.getAdvanceReceiptTax(filterParams);
                List<ReceiptAdvanceDetail> receiptAdvDtlList = result.getEntityList();
                for (ReceiptAdvanceDetail temp : receiptAdvDtlList) {
                    String documentCurrencyId = temp.getReceipt().getCurrency().getCurrencyID();
                    double originalSalesAmountWithTax = temp.getAmount();
                    double originalTaxAmount = temp.getTaxamount();
                    double originalSalesAmountWithoutTax = originalSalesAmountWithTax - originalTaxAmount;
                    double salesAmountWithoutTaxInBase = originalSalesAmountWithoutTax;
                    double taxAmountInBase = originalTaxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, originalSalesAmountWithoutTax, documentCurrencyId, temp.getReceipt().getCreationDate(), temp.getReceipt().getJournalEntry().getExternalCurrencyRate());
                        salesAmountWithoutTaxInBase = (Double) bAmt.getEntityList().get(0);
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, originalTaxAmount, documentCurrencyId, temp.getReceipt().getCreationDate(), temp.getReceipt().getJournalEntry().getExternalCurrencyRate());
                        taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    double totalAmtWithTaxInBase = salesAmountWithoutTaxInBase + taxAmountInBase;
                    purchases = new JSONObject();
                    purchases.put("grdate", temp.getReceipt().getCreationDate().getTime());
                    purchases.put("grname", temp.getReceipt().getCustomer().getName());
                    purchases.put("grno", temp.getReceipt().getReceiptNumber());
                    purchases.put("gramt", totalAmtWithTaxInBase);
                    purchases.put("gramtexcludinggst", salesAmountWithoutTaxInBase);
                    purchases.put("grtaxamount", taxAmountInBase);
                    purchases.put("grlineno", temp.getSrNoForRow());
                    purchases.put("grproduct", !StringUtil.isNullOrEmpty(temp.getDescription()) ? temp.getDescription() : "");
                    purchases.put("grtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : "");
                    purchases.put("gruen", (temp.getReceipt().getCustomer() != null && temp.getReceipt().getCustomer().getCompanyRegistrationNumber() != null) ? temp.getReceipt().getCustomer().getCompanyRegistrationNumber() : "");
                    purchases.put("fcycode", temp.getReceipt().getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", originalSalesAmountWithTax);
                    purchases.put("purchasefcyexcludinggst", originalSalesAmountWithoutTax);
                    purchases.put("gstfcy", originalTaxAmount);
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", (temp.getReceipt().getCustomer() != null && temp.getReceipt().getCustomer().getGstRegistrationNumber() != null) ? temp.getReceipt().getCustomer().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", temp.getReceipt().getJournalEntry().getEntryDate().getTime());//Posting date same as journal entry date.
                    purchases.put(Constants.importExportDeclarationNo, "");
                    grjArr.put(purchases);
                }
                
                /**
                 * Credit Note Otherwise Tax Calculation.
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.creditNoteSearchJson));
                }
                result = accReceiptDAOobj.getCalculatedCreditNoteOtherwiseTax(filterParams);
                List<CreditNoteTaxEntry> cnTaxList = result.getEntityList();
                for (CreditNoteTaxEntry temp : cnTaxList) {
                    String documentCurrencyId = temp.getCreditNote().getCurrency().getCurrencyID();
                    double taxAmount = 0;
                    double taxAmountInOriginalCurrency = 0;
                    double cnSalesAmt = (Double) temp.getAmount();
                    double cnSalesAmtInOriginalCurrency = (Double) temp.getAmount();

                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, cnSalesAmt, documentCurrencyId, temp.getCreditNote().getCreationDate(), temp.getCreditNote().getJournalEntry().getExternalCurrencyRate());
                        cnSalesAmt = (Double) bAmt.getEntityList().get(0);
                    }

                    if (temp.getTax() != null) {
                        taxAmount = temp.getTaxamount();
                        taxAmountInOriginalCurrency = taxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bTaxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, documentCurrencyId, temp.getCreditNote().getCreationDate(), temp.getCreditNote().getJournalEntry().getExternalCurrencyRate());
                            taxAmount = (Double) bTaxAmt.getEntityList().get(0);
                        }
                    }
                    if (temp.getAccount().getID().equals(taxObj.getAccount().getID())) {
                        taxAmount += cnSalesAmt;
                        taxAmountInOriginalCurrency += cnSalesAmtInOriginalCurrency;
                        cnSalesAmt = 0;
                        cnSalesAmtInOriginalCurrency = 0;
                    }

                    purchases = new JSONObject();
                    purchases.put("grdate", temp.getCreditNote().getCreationDate().getTime());
                    purchases.put("grname", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    purchases.put("grno", temp.getCreditNote().getCreditNoteNumber());
                    purchases.put("gramt", (cnSalesAmt + taxAmount));
                    purchases.put("gramtexcludinggst", cnSalesAmt);
                    purchases.put("grtaxamount", taxAmount);
                    purchases.put("grlineno", temp.getSrNoForRow());
                    purchases.put("grproduct", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    purchases.put("grtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : "");
                    purchases.put("gruen", (temp.getCreditNote().getCustomer() != null && temp.getCreditNote().getCustomer().getCompanyRegistrationNumber() != null) ? temp.getCreditNote().getCustomer().getCompanyRegistrationNumber() : "");
                    purchases.put("fcycode", temp.getCreditNote().getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", (cnSalesAmtInOriginalCurrency + taxAmountInOriginalCurrency));
                    purchases.put("purchasefcyexcludinggst", cnSalesAmtInOriginalCurrency);
                    purchases.put("gstfcy", taxAmountInOriginalCurrency);
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", (temp.getCreditNote().getCustomer() != null && temp.getCreditNote().getCustomer().getGstRegistrationNumber() != null) ? temp.getCreditNote().getCustomer().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", temp.getCreditNote().getJournalEntry().getEntryDate().getTime());//Posting date same as journal entry date.
                    purchases.put(Constants.importExportDeclarationNo, "");
                    grjArr.put(purchases);
                }
                
                JSONObject obj = new JSONObject();
                obj.put("details", grjArr);
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl:getCalculatedPurchaseTaxforIAFfile " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getCalculatedSalesTaxforIAFfile(HashMap<String, Object> requestParams, List taxList) throws ServiceException, ParseException {
        JSONArray jArr = new JSONArray();
        try {
            JSONArray invjArr;
            JSONObject sales;
            KwlReturnObject result;
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            boolean isMalasianCompany = company.getCountry().getID().equalsIgnoreCase("137");
            boolean isAdvanceSearch = requestParams.containsKey(Constants.isAdvanceSearch) ? (Boolean) requestParams.get(Constants.isAdvanceSearch) : false;
            String countryName = company.getCountry().getCountryName();
            String companyCurrencyId = company.getCurrency().getCurrencyID();

            Iterator itr = taxList.iterator();
            while (itr.hasNext()) {
                invjArr = new JSONArray();
                Object[] row = (Object[]) itr.next();
                Tax taxObj = (Tax) row[0];
                TaxList taxListObj = (TaxList) row[1];
                Map<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put("taxid", taxObj.getID());
                if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
                    filterParams.put("startDate", requestParams.get("startDate"));
                    filterParams.put("endDate", requestParams.get("endDate"));
                }
                if (requestParams.containsKey(Constants.companyKey) && requestParams.containsKey(Constants.companyKey)) {
                    filterParams.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                }

                /**
                 * Invoice Tax Calculation.
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.invoiceSearchJson));
                    filterParams.put(Constants.Filter_Criteria, requestParams.get(Constants.Filter_Criteria));
                    filterParams.put(Constants.fixedAssetsDisposalInvoiceSearchJson, requestParams.get(Constants.fixedAssetsDisposalInvoiceSearchJson));
                }
                result = accInvoiceDAOobj.getCalculatedInvTax(filterParams);
                List<Invoice> invList = result.getEntityList();
                for (Invoice temp : invList) {
                    String documentCurrencyId = temp.getCurrency().getCurrencyID();
                    double taxPercent = taxListObj.getPercent();
                    String country = "";
                    if (temp.getBillingShippingAddresses() != null) {
                        if (!StringUtil.isNullOrEmpty(temp.getBillingShippingAddresses().getShippingCountry()) && !temp.getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getBillingShippingAddresses().getShippingCountry();
                        } else if (StringUtil.isNullOrEmpty(temp.getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(temp.getBillingShippingAddresses().getBillingCountry()) && !temp.getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getBillingShippingAddresses().getBillingCountry();
                        }
                    }

                    JSONArray invoiceTermsDetailsArray = accInvoiceServiceDAO.getTermDetails(temp.getID());
                    double termAmount = CommonFunctions.getTotalTermsAmount(invoiceTermsDetailsArray);
                    double termAmountToBeIncluded = 0.0d;
                    if (termAmount != 0) {
                        /**
                         * Creating set with invoice id and mapped terms
                         * information.
                         */
                        Set<String> termsUsedInInvoice = new HashSet<>();
                        //Creating map to put term and it's corresponding amount information.
                        HashMap<String, Double> termAndAmountMapping = new HashMap<>();
                        for (int i = 0; i < invoiceTermsDetailsArray.length(); i++) {
                            JSONObject jsonObj = invoiceTermsDetailsArray.getJSONObject(i);
                            String termIdInJson = jsonObj.getString("id");
                            double termAmountInJson = jsonObj.optDouble("termamount", 0.0);
                            termAndAmountMapping.put(termIdInJson, termAmountInJson);
                            termsUsedInInvoice.add(termIdInJson);
                        }

                        /**
                         * Finding all terms in system mapped with tax.
                         */
                        List termList = accTaxDAOobj.getTerms(taxObj.getID());
                        String termid = "";
                        Iterator itr1 = termList.iterator();
                        while (itr1.hasNext()) {
                            KwlReturnObject termResult = accountingHandlerDAOobj.getObject(InvoiceTermsSales.class.getName(), itr1.next().toString());
                            InvoiceTermsSales invoiceTermsSales = (InvoiceTermsSales) termResult.getEntityList().get(0);

                            if (invoiceTermsSales != null) {
                                termid = invoiceTermsSales.getId();
                                /**
                                 * If this mapped term is used in invoice.
                                 */
                                if (termsUsedInInvoice.contains(termid)) {
                                    double termAmountToAdd = termAndAmountMapping.get(termid);
                                    termAmountToBeIncluded += termAmountToAdd;
                                }
                            }
                        }
                    }

                    Set<InvoiceDetail> rows = temp.getRows();
                    for (InvoiceDetail invoiceDetail : rows) {
                        double quantity = invoiceDetail.getInventory().getQuantity();
                        double amount = invoiceDetail.getRate() * quantity;
                        double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                        double originalRowAmtExcludingGST = amount - rdisc;
                        double originalRowTaxAmt = (originalRowAmtExcludingGST * taxPercent) / 100;//Original tax amount.
                        double rowAmtExcludingGSTInbase = originalRowAmtExcludingGST;
                        double rowTaxAmtInBase = originalRowTaxAmt;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExcludingGST, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            rowAmtExcludingGSTInbase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.

                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowTaxAmt, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                        }

                        sales = new JSONObject();
                        sales.put("invdate", temp.getCreationDate().getTime());
                        sales.put("invname", temp.getCustomer().getName());
                        sales.put("invno", temp.getInvoiceNumber());
                        sales.put("invamt", rowAmtExcludingGSTInbase + rowTaxAmtInBase);
                        sales.put("invamtexcludinggst", rowAmtExcludingGSTInbase);
                        sales.put("invtaxamount", rowTaxAmtInBase);
                        sales.put("invnamegst", temp.getCustomer().getOther());
                        sales.put("invlineno", invoiceDetail.getSrno());
                        sales.put("invproduct", !StringUtil.isNullOrEmpty(invoiceDetail.getInventory().getProduct().getDescription()) ? invoiceDetail.getInventory().getProduct().getDescription() : invoiceDetail.getInventory().getProduct().getName());
                        sales.put("invtaxcode", temp.getTax().getTaxCode());
                        sales.put("invuen", (isMalasianCompany) ? temp.getCustomer().getCompanyRegistrationNumber() : temp.getCustomer().getAltContactNumber());
                        sales.put("invcountry", temp.getCustomer().getCountry() != null ? temp.getCustomer().getCountry().getCountryName() : "");
                        sales.put("fcycode", temp.getCurrency().getCurrencyCode());
                        sales.put("salesfcy", originalRowAmtExcludingGST + originalRowTaxAmt);
                        sales.put("salesfcyexcludinggst", originalRowAmtExcludingGST);
                        sales.put("gstfcy", originalRowTaxAmt);
                        sales.put("customergstno", (temp.getCustomer() != null && temp.getCustomer().getGstRegistrationNumber() != null) ? temp.getCustomer().getGstRegistrationNumber() : "");
                        sales.put(Constants.SHIPPING_COUNTRY, country);
                        sales.put(Constants.importExportDeclarationNo, temp.getExportDeclarationNo() != null ? temp.getExportDeclarationNo() : "");
                        invjArr.put(sales);
                    }

                    //Term Amount Entry on New Line.
                    if (termAmountToBeIncluded != 0) {
                        double termAmountPerRow = termAmountToBeIncluded;
                        double termTaxAmountPerRow = (termAmountToBeIncluded * taxPercent / 100);
                        double termAmtInBase = termAmountPerRow;
                        double termTaxAmtInBase = termTaxAmountPerRow;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmountPerRow, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            termAmtInBase = (Double) bAmt.getEntityList().get(0);//Term amount in base excluding gst.

                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termTaxAmountPerRow, documentCurrencyId, temp.getCreationDate(), temp.getJournalEntry().getExternalCurrencyRate());
                            termTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Term tax amount in base excluding gst.
                        }
                        sales = new JSONObject();
                        sales.put("invdate", temp.getCreationDate().getTime());
                        sales.put("invname", temp.getCustomer().getName());
                        sales.put("invno", temp.getInvoiceNumber());
                        sales.put("invamt", termAmtInBase + termTaxAmtInBase);
                        sales.put("invamtexcludinggst", termAmtInBase);
                        sales.put("invtaxamount", termTaxAmtInBase);
                        sales.put("invnamegst", temp.getCustomer().getOther());
                        sales.put("invlineno", 1);
                        sales.put("invproduct", "");
                        sales.put("invtaxcode", temp.getTax().getTaxCode());
                        sales.put("invuen", (isMalasianCompany) ? temp.getCustomer().getCompanyRegistrationNumber() : temp.getCustomer().getAltContactNumber());
                        sales.put("invcountry", temp.getCustomer().getCountry() != null ? temp.getCustomer().getCountry().getCountryName() : "");
                        sales.put("fcycode", temp.getCurrency().getCurrencyCode());
                        sales.put("salesfcy", termAmountPerRow + termTaxAmountPerRow);
                        sales.put("salesfcyexcludinggst", termAmountPerRow);
                        sales.put("gstfcy", termTaxAmountPerRow);
                        sales.put("customergstno", (temp.getCustomer() != null && temp.getCustomer().getGstRegistrationNumber() != null) ? temp.getCustomer().getGstRegistrationNumber() : "");
                        sales.put(Constants.SHIPPING_COUNTRY, country);
                        sales.put(Constants.importExportDeclarationNo, temp.getExportDeclarationNo() != null ? temp.getExportDeclarationNo() : "");
                        invjArr.put(sales);
                    }
                }

                /**
                 * Invoice Detail Tax Calculation.
                 */
                result = accInvoiceDAOobj.getCalculatedInvDtlTax(filterParams);
                List<InvoiceDetail> list = result.getEntityList();
                for (InvoiceDetail temp : list) {
                    String documentCurrencyId = temp.getInvoice().getCurrency().getCurrencyID();
                    double originalRowAmtExcludingGST = temp.getRowExcludingGstAmount();
                    double originalRowTaxAmt = temp.getRowTaxAmount();
                    double rowAmtExcludingGSTInbase = temp.getRowExcludingGstAmountInBase();
                    double rowTaxAmtInBase = temp.getRowTaxAmountInBase();
                    Map<String, Double> invtermdetails = accInvoiceCommon.getInvoiceDetailsTermAmount(temp);
                    double termAmount = invtermdetails.get("termamount");//term amount in transaction currency.
                    double termTaxAmount = invtermdetails.get("taxamount");//term tax amount in transaction currency.
                    double termAmountInbase = termAmount;
                    double termTaxAmtInbase = termTaxAmount;

                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termAmount, documentCurrencyId, temp.getInvoice().getCreationDate(), temp.getInvoice().getJournalEntry().getExternalCurrencyRate());
                        termAmountInbase = (Double) bAmt.getEntityList().get(0);
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, termTaxAmount, documentCurrencyId, temp.getInvoice().getCreationDate(), temp.getInvoice().getJournalEntry().getExternalCurrencyRate());
                        termTaxAmtInbase = (Double) bAmt.getEntityList().get(0);
                    }

                    sales = new JSONObject();
                    sales.put("invdate", temp.getInvoice().getCreationDate().getTime());
                    sales.put("invname", temp.getInvoice().getCustomer().getName());
                    sales.put("invno", temp.getInvoice().getInvoiceNumber());
                    sales.put("invamt", rowAmtExcludingGSTInbase + rowTaxAmtInBase + termAmountInbase + termTaxAmtInbase);
                    sales.put("invamtexcludinggst", rowAmtExcludingGSTInbase + termAmountInbase);
                    sales.put("invtaxamount", rowTaxAmtInBase + termTaxAmtInbase);
                    sales.put("invnamegst", temp.getInvoice().getCustomer().getOther());
                    sales.put("invlineno", temp.getSrno());
                    sales.put("invproduct", temp.getInventory().getProduct() != null ? (!StringUtil.isNullOrEmpty(temp.getInventory().getProduct().getDescription()) ? temp.getInventory().getProduct().getDescription() : temp.getInventory().getProduct().getName()) : "");
                    sales.put("invtaxcode", temp.getTax().getTaxCode());
                    sales.put("invuen", (isMalasianCompany) ? temp.getInvoice().getCustomer().getCompanyRegistrationNumber() : temp.getInvoice().getCustomer().getAltContactNumber());
                    sales.put("invcountry", temp.getInvoice().getCustomer().getCountry() != null ? temp.getInvoice().getCustomer().getCountry().getCountryName() : "");
                    sales.put("fcycode", temp.getInvoice().getCurrency().getCurrencyCode());
                    sales.put("salesfcy", originalRowAmtExcludingGST + originalRowTaxAmt + termAmount + termTaxAmount);
                    sales.put("salesfcyexcludinggst", originalRowAmtExcludingGST + termAmount);
                    sales.put("gstfcy", originalRowTaxAmt + termTaxAmount);
                    sales.put("customergstno", (temp.getInvoice().getCustomer() != null && temp.getInvoice().getCustomer().getGstRegistrationNumber() != null) ? temp.getInvoice().getCustomer().getGstRegistrationNumber() : "");

                    String country = "";
                    if (temp.getInvoice().getBillingShippingAddresses() != null) {
                        if (!StringUtil.isNullOrEmpty(temp.getInvoice().getBillingShippingAddresses().getShippingCountry()) && !temp.getInvoice().getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getInvoice().getBillingShippingAddresses().getShippingCountry();
                        } else if (StringUtil.isNullOrEmpty(temp.getInvoice().getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(temp.getInvoice().getBillingShippingAddresses().getBillingCountry()) && !temp.getInvoice().getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getInvoice().getBillingShippingAddresses().getBillingCountry();
                        }
                    }
                    sales.put(Constants.SHIPPING_COUNTRY, country);
                    sales.put(Constants.importExportDeclarationNo, temp.getInvoice().getExportDeclarationNo() != null ? temp.getInvoice().getExportDeclarationNo() : "");
                    invjArr.put(sales);
                }

                /**
                 * Receive Payment For Otherwise Tax Calculation.
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.receivePaymentSearchJson));
                }
                result = accReceiptDAOobj.getCalculatedReceivePaymentOtherwiseTax(filterParams);
                List<ReceiptDetailOtherwise> rdoList = result.getEntityList();
                for (ReceiptDetailOtherwise temp : rdoList) {
                    String documentCurrencyId = temp.getReceipt().getCurrency().getCurrencyID();
                    double ramount = temp.getAmount();
                    double taxAmount = temp.getTaxamount();
                    double ramountInBase = ramount;
                    double taxAmountInBase = taxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, ramount, documentCurrencyId, temp.getReceipt().getCreationDate(), temp.getReceipt().getJournalEntry().getExternalCurrencyRate());                        //Converting into base [PS]
                        ramountInBase = (Double) bAmt.getEntityList().get(0);

                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, documentCurrencyId, temp.getReceipt().getCreationDate(), temp.getReceipt().getJournalEntry().getExternalCurrencyRate());
                        taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }

                    sales = new JSONObject();
                    sales.put("invdate", temp.getReceipt().getCreationDate().getTime());
                    sales.put("invname", temp.getAccount().getName());
                    sales.put("invno", temp.getReceipt().getReceiptNumber());
                    sales.put("invamt", (ramountInBase + taxAmountInBase));
                    sales.put("invamtexcludinggst", ramountInBase);
                    sales.put("invtaxamount", taxAmountInBase);
                    sales.put("invnamegst", "");
                    sales.put("invlineno", temp.getSrNoForRow());
                    sales.put("invproduct", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    sales.put("invtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : (temp.getGstapplied() != null ? temp.getGstapplied().getTaxCode() : temp.getAccount().getName()));
                    sales.put("invuen", (isMalasianCompany) ? (temp.getReceipt().getCustomer() != null ? temp.getReceipt().getCustomer().getCompanyRegistrationNumber() : "") : "");
                    sales.put("invcountry", "");
                    sales.put("fcycode", temp.getReceipt().getCurrency().getCurrencyCode());
                    sales.put("salesfcy", ramount + taxAmount);//rowamount + (rowamount * rowTaxPercent/100)
                    sales.put("salesfcyexcludinggst", ramount);//rowamount + (rowamount * rowTaxPercent/100)
                    sales.put("gstfcy", taxAmount);
                    sales.put("customergstno", (temp.getReceipt().getCustomer() != null && temp.getReceipt().getCustomer().getGstRegistrationNumber() != null) ? temp.getReceipt().getCustomer().getGstRegistrationNumber() : "");
                    sales.put(Constants.importExportDeclarationNo, "");
                    invjArr.put(sales);
                }
 
                /**
                 * Make Payment Otherwise With Sales Tax Calculation.
                 *
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.makePaymentSearchJson));
                }
                result = accVendorPaymentDAOobj.getCalculatedMakePaymentOtherwiseTax(filterParams);
                List<PaymentDetailOtherwise> pdoList = result.getEntityList();
                for (PaymentDetailOtherwise temp : pdoList) {
                    String documentCurrencyId = temp.getPayment().getCurrency().getCurrencyID();
                    double taxAmount = temp.getTaxamount();
                    double ramount = temp.getAmount();
                    double ramountExGSTInbase = ramount;
                    double taxAmountInbase = taxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, ramount, documentCurrencyId, temp.getPayment().getCreationDate(), temp.getPayment().getJournalEntry().getExternalCurrencyRate());                        //Converting into base [PS]
                        ramountExGSTInbase = (Double) bAmt.getEntityList().get(0);

                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, documentCurrencyId, temp.getPayment().getCreationDate(), temp.getPayment().getJournalEntry().getExternalCurrencyRate());
                        taxAmountInbase = (Double) bAmt.getEntityList().get(0);
                    }
                    sales = new JSONObject();
                    sales.put("invdate", temp.getPayment().getCreationDate().getTime());
                    sales.put("invname", temp.getAccount().getName());
                    sales.put("invno", temp.getPayment().getPaymentNumber());
                    sales.put("invamt", (ramountExGSTInbase + taxAmountInbase));
                    sales.put("invamtexcludinggst", ramountExGSTInbase);
                    sales.put("invtaxamount", taxAmountInbase);
                    sales.put("invnamegst", "");
                    sales.put("invlineno", temp.getSrNoForRow());
                    sales.put("invproduct", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    sales.put("invtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : (temp.getGstapplied() != null ? temp.getGstapplied().getTaxCode() : temp.getAccount().getName()));
                    sales.put("invuen", (isMalasianCompany) ? ((temp.getPayment().getVendor() != null) ? temp.getPayment().getVendor().getCompanyRegistrationNumber() : "") : "");
                    sales.put("invcountry", "");
                    sales.put("fcycode", temp.getPayment().getCurrency().getCurrencyCode());
                    sales.put("salesfcy", ramount + taxAmount);
                    sales.put("salesfcyexcludinggst", ramount);
                    sales.put("gstfcy", taxAmount);
                    sales.put("customergstno", (temp.getPayment().getVendor() != null && temp.getPayment().getVendor().getGstRegistrationNumber() != null) ? temp.getPayment().getVendor().getGstRegistrationNumber() : "");
                    sales.put(Constants.SHIPPING_COUNTRY, "");
                    sales.put(Constants.importExportDeclarationNo, "");
                    invjArr.put(sales);
                }
                
                /**
                 * Credit Note Otherwise Tax Calculation.
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.creditNoteSearchJson));
                }
                result = accReceiptDAOobj.getCalculatedCreditNoteOtherwiseTax(filterParams);
                List<CreditNoteTaxEntry> cnTaxList = result.getEntityList();
                for (CreditNoteTaxEntry temp : cnTaxList) {
                    String documentCurrencyId = temp.getCreditNote().getCurrency().getCurrencyID();
                    double taxAmount = 0;
                    double taxAmountInOriginalCurrency = 0;
                    double cnSalesAmt = (Double) temp.getAmount();
                    double cnSalesAmtInOriginalCurrency = (Double) temp.getAmount();

                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, cnSalesAmt, documentCurrencyId, temp.getCreditNote().getCreationDate(), temp.getCreditNote().getJournalEntry().getExternalCurrencyRate());
                        cnSalesAmt = (Double) bAmt.getEntityList().get(0);
                    }

                    if (temp.getTax() != null) {
                        taxAmount = temp.getTaxamount();
                        taxAmountInOriginalCurrency = taxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bTaxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, documentCurrencyId, temp.getCreditNote().getCreationDate(), temp.getCreditNote().getJournalEntry().getExternalCurrencyRate());
                            taxAmount = (Double) bTaxAmt.getEntityList().get(0);
                        }
                    }
                    if (temp.getAccount().getID().equals(taxObj.getAccount().getID())) {
                        taxAmount += cnSalesAmt;
                        taxAmountInOriginalCurrency += cnSalesAmtInOriginalCurrency;
                        cnSalesAmt = 0;
                        cnSalesAmtInOriginalCurrency = 0;
                    }

                    sales = new JSONObject();
                    sales.put("invdate", temp.getCreditNote().getCreationDate().getTime());
                    sales.put("invname", temp.getAccount().getName());
                    sales.put("invno", temp.getCreditNote().getCreditNoteNumber());
                    sales.put("invamt", cnSalesAmt + (taxAmount));
                    sales.put("invamtexcludinggst", cnSalesAmt);
                    sales.put("invtaxamount", taxAmount);
                    sales.put("invlineno", temp.getSrNoForRow());
                    sales.put("invproduct", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    sales.put("invtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : temp.getAccount().getName());
                    sales.put("invuen", (isMalasianCompany) ? (temp.getCreditNote().getCustomer() != null ? temp.getCreditNote().getCustomer().getCompanyRegistrationNumber() : "") : "");
                    sales.put("invcountry", "");
                    sales.put("fcycode", temp.getCreditNote().getCurrency().getCurrencyCode());
                    sales.put("salesfcy", cnSalesAmtInOriginalCurrency + (taxAmountInOriginalCurrency));
                    sales.put("salesfcyexcludinggst", cnSalesAmtInOriginalCurrency);
                    sales.put("gstfcy", taxAmountInOriginalCurrency);
                    sales.put("customergstno", (temp.getCreditNote().getCustomer() != null && temp.getCreditNote().getCustomer().getGstRegistrationNumber() != null) ? temp.getCreditNote().getCustomer().getGstRegistrationNumber() : "");

                    String country = "";
                    if (temp.getCreditNote().getBillingShippingAddresses() != null) {
                        if (!StringUtil.isNullOrEmpty(temp.getCreditNote().getBillingShippingAddresses().getShippingCountry()) && !temp.getCreditNote().getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getCreditNote().getBillingShippingAddresses().getShippingCountry();
                        } else if (StringUtil.isNullOrEmpty(temp.getCreditNote().getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(temp.getCreditNote().getBillingShippingAddresses().getBillingCountry()) && !temp.getCreditNote().getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getCreditNote().getBillingShippingAddresses().getBillingCountry();
                        }
                    }
                    sales.put(Constants.SHIPPING_COUNTRY, country);
                    sales.put(Constants.importExportDeclarationNo, "");
                    invjArr.put(sales);
                }

                /**
                 * Calculation of Tax For Credit Note.
                 */
                result = accInvoiceDAOobj.getCalculatedCNTax(filterParams);
                List<CreditNote> list11 = result.getEntityList();
                for (CreditNote creditNote : list11) {
                    double cnAmount = 0, taxAmount = 0;
                    KwlReturnObject result1 = accJournalEntryDAOobj.getJournalEntryDetail(creditNote.getJournalEntry().getID(), creditNote.getJournalEntry().getCompany().getCompanyID());
                    boolean taxflag = false;
                    List<JournalEntryDetail> jeDetails = result1.getEntityList();
                    for (JournalEntryDetail jed : jeDetails) {
                        Account account = jed.getAccount();
                        String currLibgrp = "";
                        Group group = accAccountDAOobj.getNewGroupFromOldId(Group.OTHER_CURRENT_LIABILITIES, account.getCompany().getCompanyID());
                        if (group != null) {
                            currLibgrp = group.getID();
                        }
                        if (account.getGroup().getID().equals(currLibgrp)) {
                            if (jed.isDebit() && account.getID().equals(taxObj.getAccount().getID())) {
                                taxAmount = jed.getAmount();
                                taxflag = true;
                            }
                        }
                        if (!jed.isDebit()) {
                            cnAmount += jed.getAmount();
                        }
                    }

                    if (taxflag) {
                        String documentCurrencyId = creditNote.getJournalEntry().getCurrency().getCurrencyID();
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnAmount, documentCurrencyId, creditNote.getCreationDate(), creditNote.getJournalEntry().getExternalCurrencyRate());
                            cnAmount = (Double) bAmt1.getEntityList().get(0);
                            bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, taxAmount, documentCurrencyId, creditNote.getCreationDate(), creditNote.getJournalEntry().getExternalCurrencyRate());
                            taxAmount = (Double) bAmt1.getEntityList().get(0);
                        }

                        sales = new JSONObject();
                        sales.put("invdate", creditNote.getCreationDate().getTime());
                        sales.put("invname", creditNote.getCustomer().getName());
                        sales.put("invno", creditNote.getCreditNoteNumber());
                        sales.put("invamt", -cnAmount);
                        sales.put("invamtexcludinggst", -(cnAmount - taxAmount));
                        sales.put("invtaxamount", -taxAmount);
                        sales.put("invnamegst", "");
                        sales.put("invlineno", 1);
                        sales.put("invproduct", "");
                        sales.put("invtaxcode", taxObj.getTaxCode());
                        sales.put("invuen", (isMalasianCompany) ? (creditNote.getCustomer() != null ? creditNote.getCustomer().getCompanyRegistrationNumber() : "") : "");
                        sales.put("invcountry", "");
                        sales.put("fcycode", creditNote.getJournalEntry().getCurrency().getCurrencyCode());
                        sales.put("salesfcy", -cnAmount);
                        sales.put("salesfcyexcludinggst", -(cnAmount - taxAmount));
                        sales.put("gstfcy", -taxAmount);
                        sales.put("customergstno", (creditNote.getCustomer() != null && creditNote.getCustomer().getGstRegistrationNumber() != null) ? creditNote.getCustomer().getGstRegistrationNumber() : "");

                        String country = "";
                        if (creditNote.getBillingShippingAddresses() != null) {
                            if (!StringUtil.isNullOrEmpty(creditNote.getBillingShippingAddresses().getShippingCountry()) && !creditNote.getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                                country = creditNote.getBillingShippingAddresses().getShippingCountry();
                            } else if (StringUtil.isNullOrEmpty(creditNote.getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(creditNote.getBillingShippingAddresses().getBillingCountry()) && !creditNote.getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                                country = creditNote.getBillingShippingAddresses().getBillingCountry();
                            }
                        }
                        sales.put(Constants.SHIPPING_COUNTRY, country);
                        sales.put(Constants.importExportDeclarationNo, "");
                        invjArr.put(sales);
                    }
                }

                /**
                 * Calculate tax for Credit Note For Overcharge.
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.creditNoteSearchJson));
                }
                result = accInvoiceDAOobj.getCalculatedCreditNoteTax(filterParams);
                List<CreditNote> calculatedCNTaxList = result.getEntityList();
                for (CreditNote creditMemo : calculatedCNTaxList) {
                    double cnAmount = 0, taxAmount = 0;
                    KwlReturnObject result1 = accJournalEntryDAOobj.getJournalEntryDetail(creditMemo.getJournalEntry().getID(), creditMemo.getJournalEntry().getCompany().getCompanyID());
                    List<JournalEntryDetail> jeDetailList = result1.getEntityList();
                    boolean taxflag = false;
                    for (JournalEntryDetail jed : jeDetailList) {
                        Account account = jed.getAccount();
                        if (jed.isDebit() && account.getID().equals(taxObj.getAccount().getID())) {
                            taxAmount += jed.getAmount();
                            taxflag = true;
                        }
                        if (jed.isDebit()) {
                            cnAmount += jed.getAmount();
                        }
                    }
                    double cnAmountExcludingTax = cnAmount - taxAmount;
                    if (taxflag) {
                        String documentCurrencyId = creditMemo.getJournalEntry().getCurrency().getCurrencyID();
                        double cnAmountInBase = cnAmount;
                        double taxAmountInBase = taxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnAmount, documentCurrencyId, creditMemo.getCreationDate(), creditMemo.getJournalEntry().getExternalCurrencyRate());
                            cnAmountInBase = (Double) bAmt1.getEntityList().get(0);

                            bAmt1 = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, taxAmount, documentCurrencyId, creditMemo.getCreationDate(), creditMemo.getJournalEntry().getExternalCurrencyRate());
                            taxAmountInBase = (Double) bAmt1.getEntityList().get(0);
                        }
                        double cnAmountExcludingTaxInBase = cnAmountInBase - taxAmountInBase;

                        sales = new JSONObject();
                        sales.put("invdate", creditMemo.getCreationDate().getTime());
                        sales.put("invname", creditMemo.getCustomer().getName());
                        sales.put("invno", creditMemo.getCreditNoteNumber());
                        sales.put("invamt", cnAmountInBase);
                        sales.put("invamtexcludinggst", cnAmountExcludingTaxInBase);
                        sales.put("invtaxamount", taxAmountInBase);
                        sales.put("invnamegst", "");
                        sales.put("invlineno", 1);
                        sales.put("invproduct", "");
                        sales.put("invtaxcode", taxObj.getTaxCode());
                        sales.put("invuen", (isMalasianCompany) ? (creditMemo.getCustomer() != null ? creditMemo.getCustomer().getCompanyRegistrationNumber() : "") : "");
                        sales.put("invcountry", "");
                        sales.put("fcycode", creditMemo.getJournalEntry().getCurrency().getCurrencyCode());
                        sales.put("salesfcy", cnAmount);
                        sales.put("salesfcyexcludinggst", cnAmountExcludingTax);
                        sales.put("gstfcy", taxAmount);
                        sales.put("customergstno", (creditMemo.getCustomer() != null && creditMemo.getCustomer().getGstRegistrationNumber() != null) ? creditMemo.getCustomer().getGstRegistrationNumber() : "");

                        String country = "";
                        if (creditMemo.getBillingShippingAddresses() != null) {
                            if (!StringUtil.isNullOrEmpty(creditMemo.getBillingShippingAddresses().getShippingCountry()) && !creditMemo.getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                                country = creditMemo.getBillingShippingAddresses().getShippingCountry();
                            } else if (StringUtil.isNullOrEmpty(creditMemo.getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(creditMemo.getBillingShippingAddresses().getBillingCountry()) && !creditMemo.getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                                country = creditMemo.getBillingShippingAddresses().getBillingCountry();
                            }
                        }
                        sales.put(Constants.SHIPPING_COUNTRY, country);
                        sales.put(Constants.importExportDeclarationNo, "");
                        invjArr.put(sales);
                    }
                }

                /**
                 * Calculate tax for Credit Note For Overcharge details.
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.creditNoteSearchJson));
                    filterParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                }
                result = accGoodsReceiptobj.getCalculatedCNTaxGst(filterParams);
                List<CreditNoteAgainstVendorGst> cnGSTDetails = result.getEntityList();
                for (CreditNoteAgainstVendorGst temp : cnGSTDetails) {
                    String documentCurrencyId = temp.getCreditNote().getCurrency().getCurrencyID();
                    double cnAmtExcludingTax = (temp.getRate() * temp.getReturnQuantity());
                    double taxAmount = temp.getRowTaxAmount();
                    cnAmtExcludingTax -= temp.getDiscount();//amount excluding tax & discount.

                    double cnAmountExcludingTaxInBase = cnAmtExcludingTax;
                    double taxAmountInBase = taxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, cnAmtExcludingTax, documentCurrencyId, temp.getCreditNote().getCreationDate(), temp.getCreditNote().getJournalEntry().getExternalCurrencyRate());
                        cnAmountExcludingTaxInBase = (Double) bAmt.getEntityList().get(0);

                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, documentCurrencyId, temp.getCreditNote().getCreationDate(), temp.getCreditNote().getJournalEntry().getExternalCurrencyRate());
                        taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    double cnAmountIncludingTaxInBase = cnAmountExcludingTaxInBase + taxAmountInBase;

                    sales = new JSONObject();
                    sales.put("invdate", temp.getCreditNote().getCreationDate().getTime());
                    sales.put("invname", temp.getCreditNote().getCustomer().getName());
                    sales.put("invno", temp.getCreditNote().getCreditNoteNumber());
                    sales.put("invamt", cnAmountIncludingTaxInBase);
                    sales.put("invamtexcludinggst", cnAmountExcludingTaxInBase);
                    sales.put("invtaxamount", taxAmountInBase);
                    sales.put("invnamegst", "");
                    sales.put("invlineno", temp.getSrno());
                    sales.put("invproduct", temp.getProduct() != null ? (!StringUtil.isNullOrEmpty(temp.getProduct().getDescription()) ? temp.getProduct().getDescription() : temp.getProduct().getName()) : "");
                    sales.put("invtaxcode", taxObj.getTaxCode());
                    sales.put("invuen", (isMalasianCompany) ? (temp.getCreditNote().getCustomer() != null ? temp.getCreditNote().getCustomer().getCompanyRegistrationNumber() : "") : "");
                    sales.put("invcountry", "");
                    sales.put("fcycode", temp.getCreditNote().getJournalEntry().getCurrency().getCurrencyCode());
                    sales.put("salesfcy", cnAmtExcludingTax + taxAmount);
                    sales.put("salesfcyexcludinggst", cnAmtExcludingTax);
                    sales.put("gstfcy", taxAmount);
                    sales.put("customergstno", (temp.getCreditNote().getCustomer() != null && temp.getCreditNote().getCustomer().getGstRegistrationNumber() != null) ? temp.getCreditNote().getCustomer().getGstRegistrationNumber() : "");

                    String country = "";
                    if (temp.getCreditNote().getBillingShippingAddresses() != null) {
                        if (!StringUtil.isNullOrEmpty(temp.getCreditNote().getBillingShippingAddresses().getShippingCountry()) && !temp.getCreditNote().getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getCreditNote().getBillingShippingAddresses().getShippingCountry();
                        } else if (StringUtil.isNullOrEmpty(temp.getCreditNote().getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(temp.getCreditNote().getBillingShippingAddresses().getBillingCountry()) && !temp.getCreditNote().getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getCreditNote().getBillingShippingAddresses().getBillingCountry();
                        }
                    }
                    sales.put(Constants.SHIPPING_COUNTRY, country);
                    sales.put(Constants.importExportDeclarationNo, "");
                    invjArr.put(sales);
                }
 
                if (isAdvanceSearch) {
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.receivePaymentSearchJson));
                    filterParams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                }

                /**
                 * Advance receipt Tax calculation.
                 */
                result = accReceiptDAOobj.getAdvanceReceiptTax(filterParams);
                List<ReceiptAdvanceDetail> receiptAdvDtlList = result.getEntityList();
                for (ReceiptAdvanceDetail temp : receiptAdvDtlList) {
                    String documentCurrencyId = temp.getReceipt().getCurrency().getCurrencyID();
                    double originalSalesAmountWithTax = temp.getAmount();
                    double originalTaxAmount = temp.getTaxamount();
                    double originalSalesAmountWithoutTax = originalSalesAmountWithTax - originalTaxAmount;
                    double salesAmountWithoutTaxInBase = originalSalesAmountWithoutTax;
                    double taxAmountInBase = originalTaxAmount;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, originalSalesAmountWithoutTax, documentCurrencyId, temp.getReceipt().getCreationDate(), temp.getReceipt().getJournalEntry().getExternalCurrencyRate());
                        salesAmountWithoutTaxInBase = (Double) bAmt.getEntityList().get(0);
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, originalTaxAmount, documentCurrencyId, temp.getReceipt().getCreationDate(), temp.getReceipt().getJournalEntry().getExternalCurrencyRate());
                        taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                    }
                    double totalAmtWithTaxInBase = salesAmountWithoutTaxInBase + taxAmountInBase;
                    sales = new JSONObject();
                    sales.put("invdate", temp.getReceipt().getCreationDate().getTime());
                    sales.put("invname", temp.getReceipt().getCustomer().getName());
                    sales.put("invno", temp.getReceipt().getReceiptNumber());
                    sales.put("invamt", totalAmtWithTaxInBase);
                    sales.put("invamtexcludinggst", salesAmountWithoutTaxInBase);
                    sales.put("invtaxamount", taxAmountInBase);
                    sales.put("invnamegst", "");
                    sales.put("invlineno", temp.getSrNoForRow());
                    sales.put("invproduct", !StringUtil.isNullOrEmpty(temp.getDescription()) ? temp.getDescription() : "");
                    sales.put("invtaxcode", temp.getTax().getTaxCode());
                    sales.put("invuen", (temp.getReceipt().getCustomer() != null && temp.getReceipt().getCustomer().getCompanyRegistrationNumber() != null) ? temp.getReceipt().getCustomer().getCompanyRegistrationNumber() : "");
                    sales.put("invcountry", "");
                    sales.put("fcycode", temp.getReceipt().getCurrency().getCurrencyCode());
                    sales.put("salesfcy", originalSalesAmountWithTax);
                    sales.put("salesfcyexcludinggst", originalSalesAmountWithoutTax);//Value of supply excluding GST in Foreign Currency
                    sales.put("gstfcy", originalTaxAmount);//Value of GST on supply in Foreign Currency
                    sales.put("customergstno", (temp.getReceipt().getCustomer() != null && temp.getReceipt().getCustomer().getGstRegistrationNumber() != null) ? temp.getReceipt().getCustomer().getGstRegistrationNumber() : "");
                    sales.put(Constants.importExportDeclarationNo, "");
                    invjArr.put(sales);
                }

                /**
                 * Debit Note against Customer Tax calculation.
                 *
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.debitNoteSearchJson));
                }
                result = accGoodsReceiptobj.getCalculatedDebitNoteTax(filterParams);
                List<DebitNote> calculateDNTaxList = result.getEntityList();
                for (DebitNote debitMemo : calculateDNTaxList) {
                    double originalDNAmountWithTax = 0;
                    double originalDNAmountWithoutTax = 0;
                    double originalTaxAmount = 0;

                    KwlReturnObject result1 = accJournalEntryDAOobj.getJournalEntryDetail(debitMemo.getJournalEntry().getID(), debitMemo.getJournalEntry().getCompany().getCompanyID());
                    boolean taxflag = false;
                    List<JournalEntryDetail> entryDetails =result1.getEntityList();
                    for(JournalEntryDetail jed:entryDetails){
                        Account account = jed.getAccount();
                        if (!jed.isDebit() && account.getID().equals(taxObj.getAccount().getID())) {
                            originalTaxAmount += jed.getAmount();
                            taxflag = true;
                        }
                        if (jed.isDebit()) {
                            originalDNAmountWithTax += jed.getAmount();
                        }
                    }
                    originalDNAmountWithoutTax += originalDNAmountWithTax - originalTaxAmount;

                    if (taxflag) {
                        String documentCurrencyId = debitMemo.getCurrency().getCurrencyID();
                        double dnAmtWithoutTaxInBase = originalDNAmountWithoutTax;
                        double taxAmountInBase = originalTaxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalDNAmountWithoutTax, documentCurrencyId, debitMemo.getCreationDate(), debitMemo.getJournalEntry().getExternalCurrencyRate());
                            dnAmtWithoutTaxInBase = (Double) bAmt.getEntityList().get(0);

                            bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalTaxAmount, documentCurrencyId, debitMemo.getCreationDate(), debitMemo.getJournalEntry().getExternalCurrencyRate());
                            taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                        }
                        double totalAmtWithTaxInBase = dnAmtWithoutTaxInBase + taxAmountInBase;

                        sales = new JSONObject();
                        sales.put("invdate", debitMemo.getCreationDate().getTime());
                        sales.put("invname", debitMemo.getCustomer().getName());
                        sales.put("invno", debitMemo.getDebitNoteNumber());
                        sales.put("invamt", totalAmtWithTaxInBase);
                        sales.put("invamtexcludinggst", dnAmtWithoutTaxInBase);
                        sales.put("invtaxamount", taxAmountInBase);
                        sales.put("invnamegst", "");
                        sales.put("invlineno", 1);
                        sales.put("invproduct", "");
                        sales.put("invtaxcode", debitMemo.getTax().getTaxCode());
                        sales.put("invuen", (debitMemo.getCustomer() != null && debitMemo.getCustomer().getCompanyRegistrationNumber() != null) ? debitMemo.getCustomer().getCompanyRegistrationNumber() : "");
                        sales.put("invcountry", "");
                        sales.put("fcycode", debitMemo.getCurrency().getCurrencyCode());
                        sales.put("salesfcy", originalDNAmountWithTax);
                        sales.put("salesfcyexcludinggst", originalDNAmountWithoutTax);//Value of supply excluding GST in Foreign Currency
                        sales.put("gstfcy", originalTaxAmount);//Value of GST on supply in Foreign Currency
                        sales.put("customergstno", (debitMemo.getCustomer() != null && debitMemo.getCustomer().getGstRegistrationNumber() != null) ? debitMemo.getCustomer().getGstRegistrationNumber() : "");

                        String country = "";
                        if (debitMemo.getBillingShippingAddresses() != null) {
                            if (!StringUtil.isNullOrEmpty(debitMemo.getBillingShippingAddresses().getShippingCountry()) && !debitMemo.getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                                country = debitMemo.getBillingShippingAddresses().getShippingCountry();
                            } else if (StringUtil.isNullOrEmpty(debitMemo.getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(debitMemo.getBillingShippingAddresses().getBillingCountry()) && !debitMemo.getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                                country = debitMemo.getBillingShippingAddresses().getBillingCountry();
                            }
                        }
                        sales.put(Constants.SHIPPING_COUNTRY, country);
                        sales.put(Constants.importExportDeclarationNo, "");
                        invjArr.put(sales);
                    }
                }
                result = accGoodsReceiptobj.getCalculatedDNTaxGst(filterParams);
                List<DebitNoteAgainstCustomerGst> list12 = result.getEntityList();

                for (DebitNoteAgainstCustomerGst temp : list12) {
                    double originalSalesAmount = (temp.getRate() * temp.getReturnQuantity());
                    double originalTaxAmount = temp.getRowTaxAmount();
                    double salesAmountInBase = 0;
                    double taxAmountInBase = 0;
                    double totalAmountwithTax = 0;

                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, originalSalesAmount, temp.getDebitNote().getCurrency().getCurrencyID(), temp.getDebitNote().getCreationDate(), temp.getDebitNote().getJournalEntry().getExternalCurrencyRate());
                    salesAmountInBase = (Double) bAmt.getEntityList().get(0);

                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, originalTaxAmount, temp.getDebitNote().getCurrency().getCurrencyID(), temp.getDebitNote().getCreationDate(), temp.getDebitNote().getJournalEntry().getExternalCurrencyRate());
                    taxAmountInBase = (Double) bAmt.getEntityList().get(0);
                    totalAmountwithTax = salesAmountInBase + taxAmountInBase;

                    sales = new JSONObject();
                    sales.put("invdate", temp.getDebitNote().getCreationDate().getTime());
                    sales.put("invname", temp.getDebitNote().getCustomer().getName());
                    sales.put("invno", temp.getDebitNote().getDebitNoteNumber());
                    sales.put("invamt", totalAmountwithTax);
                    sales.put("invamtexcludinggst", salesAmountInBase);
                    sales.put("invtaxamount", taxAmountInBase);
                    sales.put("invnamegst", "");
                    sales.put("invlineno", temp.getSrno());
                    sales.put("invproduct", temp.getProduct() != null ? (!StringUtil.isNullOrEmpty(temp.getProduct().getDescription()) ? temp.getProduct().getDescription() : temp.getProduct().getName()) : "");
                    sales.put("invtaxcode", temp.getTax().getTaxCode());
                    sales.put("invuen", (temp.getDebitNote().getCustomer() != null && temp.getDebitNote().getCustomer().getCompanyRegistrationNumber() != null) ? temp.getDebitNote().getCustomer().getCompanyRegistrationNumber() : "");
                    sales.put("invcountry", "");
                    sales.put("fcycode", temp.getDebitNote().getCurrency().getCurrencyCode());
                    sales.put("salesfcy", originalSalesAmount + originalTaxAmount);
                    sales.put("salesfcyexcludinggst", originalSalesAmount);//Value of supply excluding GST in Foreign Currency
                    sales.put("gstfcy", originalTaxAmount);//Value of GST on supply in Foreign Currency
                    sales.put("customergstno", (temp.getDebitNote().getCustomer() != null && temp.getDebitNote().getCustomer().getGstRegistrationNumber() != null) ? temp.getDebitNote().getCustomer().getGstRegistrationNumber() : "");

                    String country = "";
                    if (temp.getDebitNote().getBillingShippingAddresses() != null) {
                        if (!StringUtil.isNullOrEmpty(temp.getDebitNote().getBillingShippingAddresses().getShippingCountry()) && !temp.getDebitNote().getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getDebitNote().getBillingShippingAddresses().getShippingCountry();
                        } else if (StringUtil.isNullOrEmpty(temp.getDebitNote().getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(temp.getDebitNote().getBillingShippingAddresses().getBillingCountry()) && !temp.getDebitNote().getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                            country = temp.getDebitNote().getBillingShippingAddresses().getBillingCountry();
                        }
                    }
                    sales.put(Constants.SHIPPING_COUNTRY, country);
                    sales.put(Constants.importExportDeclarationNo, "");
                    invjArr.put(sales);
                }
                
                /**
                 * Debit Note Otherwise Sales Tax Calculation.
                 *
                 */
                if (isAdvanceSearch) {
                    filterParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    filterParams.put(Constants.Acc_Search_Json, requestParams.get(Constants.debitNoteSearchJson));
                }
                result = accVendorPaymentDAOobj.getCalculatedDebitNoteOtherwiseTax(filterParams);
                List<DebitNoteTaxEntry> entityList = result.getEntityList();
                for (DebitNoteTaxEntry temp : entityList) {
                    String documentCurrencyId = temp.getDebitNote().getCurrency().getCurrencyID();
                    double taxAmount = 0;
                    double taxAmountInOriginalCurrency = 0;
                    double cnSalesAmt = 0.0;
                    if (!temp.getDebitNote().isIncludingGST()) {
                        cnSalesAmt = (Double) temp.getAmount();
                    } else {
                        cnSalesAmt = (Double) temp.getRateIncludingGst();
                    }
                    double cnSalesAmtInOriginalCurrency = cnSalesAmt;
                    if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, cnSalesAmt, temp.getDebitNote().getCurrency().getCurrencyID(), temp.getDebitNote().getCreationDate(), temp.getDebitNote().getJournalEntry().getExternalCurrencyRate());
                        cnSalesAmt = (Double) bAmt.getEntityList().get(0);
                    }

                    if (temp.getTax() != null) {
                        taxAmount = temp.getTaxamount();
                        taxAmountInOriginalCurrency = taxAmount;
                        if (!documentCurrencyId.equalsIgnoreCase(companyCurrencyId)) {
                            KwlReturnObject bTaxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount((Map) requestParams, taxAmount, temp.getDebitNote().getCurrency().getCurrencyID(), temp.getDebitNote().getCreationDate(), temp.getDebitNote().getJournalEntry().getExternalCurrencyRate());
                            taxAmount = (Double) bTaxAmt.getEntityList().get(0);
                        }
                    }
                    if (temp.getAccount().getID().equals(taxObj.getAccount().getID())) {
                        taxAmount += cnSalesAmt;
                        taxAmountInOriginalCurrency += cnSalesAmtInOriginalCurrency;
                        cnSalesAmt = 0;
                        cnSalesAmtInOriginalCurrency = 0;
                    }

                    sales = new JSONObject();
                    sales.put("invdate", temp.getDebitNote().getCreationDate().getTime());
                    sales.put("invname", temp.getAccount().getName());
                    sales.put("invno", temp.getDebitNote().getDebitNoteNumber());
                    sales.put("invamt", (cnSalesAmt + taxAmount));
                    sales.put("invamtexcludinggst", cnSalesAmt);
                    sales.put("invtaxamount", taxAmount);
                    sales.put("invnamegst", "");
                    sales.put("invlineno", temp.getSrNoForRow());
                    sales.put("invproduct", temp.getAccount() != null ? temp.getAccount().getAccountName() : "");
                    sales.put("invtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : temp.getAccount().getName());
                    sales.put("invuen", (isMalasianCompany) ? ((temp.getDebitNote().getVendor() != null) ? temp.getDebitNote().getVendor().getCompanyRegistrationNumber() : "") : "");
                    sales.put("invcountry", "");
                    sales.put("fcycode", temp.getDebitNote().getCurrency().getCurrencyCode());
                    sales.put("salesfcy", (cnSalesAmtInOriginalCurrency + taxAmountInOriginalCurrency));//rowamount + (rowamount * rowTaxPercent/100)
                    sales.put("salesfcyexcludinggst", cnSalesAmtInOriginalCurrency);
                    sales.put("gstfcy", taxAmountInOriginalCurrency);
                    sales.put("customergstno", (temp.getDebitNote().getVendor() != null && temp.getDebitNote().getVendor().getGstRegistrationNumber() != null) ? temp.getDebitNote().getVendor().getGstRegistrationNumber() : "");
                    sales.put(Constants.SHIPPING_COUNTRY, "");
                    sales.put(Constants.importExportDeclarationNo, "");
                    invjArr.put(sales);
                }
                
                JSONObject obj = new JSONObject();
                obj.put("details", invjArr);
                jArr.put(obj);
            }
        } catch (com.krawler.utils.json.base.JSONException ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl :getCalculatedSalesTaxforIAFfile " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getGLdataforIAFfileNEW(JSONObject paramsJObj, Map<String, Object> requestParams) throws ServiceException, ParseException {
        JSONArray jArr = new JSONArray();
        try {
            JSONObject purchases;
            JSONArray grjArr = new JSONArray();
            String startDate = null;
            String endDate = null;
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            if (requestParams.containsKey("startDate") && requestParams.containsKey("endDate")) {
                startDate = df.format((Date) requestParams.get("startDate"));
                endDate = df.format((Date) requestParams.get("endDate"));
            }

            requestParams.put(JournalEntryConstants.DELETED, "false");
            requestParams.put(JournalEntryConstants.NONDELETED, "true");
            requestParams.put(Constants.REQ_startdate, startDate);
            requestParams.put(Constants.REQ_enddate, endDate);
            requestParams.put("onlyPendingApprovalJesFlag", true);
            boolean isAdvanceSearch = requestParams.containsKey(Constants.isAdvanceSearch) ? (Boolean) requestParams.get(Constants.isAdvanceSearch) : false;
            String invoiceSearchJson = "";
            String purchaseInvoiceSearchJson = "";
            String creditNoteSearchJson = "";
            String debitNoteSearchJson = "";
            String receivePaymentSearchJson = "";
            String makePaymentSearchJson = "";
            String journalEntrySearchJson = "";

            if (isAdvanceSearch) {
                invoiceSearchJson = (String) requestParams.get(Constants.invoiceSearchJson);
                purchaseInvoiceSearchJson = (String) requestParams.get(Constants.purchaseInvoiceSearchJson);
                creditNoteSearchJson = (String) requestParams.get(Constants.creditNoteSearchJson);
                debitNoteSearchJson = (String) requestParams.get(Constants.debitNoteSearchJson);
                receivePaymentSearchJson = (String) requestParams.get(Constants.receivePaymentSearchJson);
                makePaymentSearchJson = (String) requestParams.get(Constants.makePaymentSearchJson);
                journalEntrySearchJson = (String) requestParams.get(Constants.journalEntrySearchJson);
                requestParams.put(Constants.moduleid, Constants.Acc_GENERAL_LEDGER_ModuleId);
                requestParams.put(Constants.Acc_Search_Json, journalEntrySearchJson);
                requestParams.put(Constants.isGSTAuditFile, true);//Addded isGSTAuditFile flag to handle advance search in GAF export case for Multi Entity flow.
            }

            HashMap<String, Object> jeRequestParams = new HashMap<>(requestParams);
            jeRequestParams.put("isCustomFieldRequired",false);
            Map<String, JSONArray> jeDetails = accReportsService.getJournalEntryDetailsMap(jeRequestParams);
            Set<String> jeIdsSet = jeDetails.keySet();
            Iterator jeIdsSetItr = jeIdsSet.iterator();

            while (jeIdsSetItr.hasNext()) {
                String jeId = jeIdsSetItr.next().toString();

                List<String> jeIdList = new ArrayList();
                jeIdList.add(jeId);
                KwlReturnObject jeResult = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), jeId);
                JournalEntry je = (JournalEntry) jeResult.getEntityList().get(0);
                JSONArray accountsInJE = jeDetails.get(jeId);
                Map<String, JSONObject> sourceValues = new HashMap<>();
                for (int i = 0; i < accountsInJE.length(); i++) {
                    JSONObject obj = accountsInJE.getJSONObject(i);
                    purchases = new JSONObject();
                    KwlReturnObject accountResult = accountingHandlerDAOobj.getObject(Account.class.getName(), obj.optString("accountid", ""));
                    Account account = (Account) accountResult.getEntityList().get(0);

                    String accid = "";
                    KwlReturnObject vendor = accountingHandlerDAOobj.getObject(Vendor.class.getName(), obj.optString("accountid", ""));
                    if (vendor != null && vendor.getEntityList() != null && vendor.getEntityList().get(0) != null) {
                        List<Vendor> vl = vendor.getEntityList();
                        accid = vl.get(0).getAltContactNumber();
                    }

                    KwlReturnObject customer = accountingHandlerDAOobj.getObject(Customer.class.getName(), obj.optString("accountid", ""));
                    if (customer != null && customer.getEntityList() != null && customer.getEntityList().get(0) != null) {
                        List<Customer> cl = customer.getEntityList();
                        accid = cl.get(0).getAltContactNumber();
                    }
                    JSONObject sourceDetails = new JSONObject();
                    if (!sourceValues.containsKey(jeId) && obj.has("sourceDocumentModuleId") && obj.get("sourceDocumentModuleId") != null) {
                        int moduleid = Integer.parseInt(obj.getString("sourceDocumentModuleId"));
                        Map<String, Object> paramsMap = new HashMap<>();
                        if (obj.has("sourceDocumentId") && obj.get("sourceDocumentId") != null) {
                            paramsMap.put("id", obj.getString("sourceDocumentId"));
                            if (Constants.Acc_ConsignmentInvoice_ModuleId == moduleid || Constants.Acc_Invoice_ModuleId == moduleid || Constants.Acc_FixedAssets_DisposalInvoice_ModuleId == moduleid || Constants.LEASE_INVOICE_MODULEID == moduleid) {
                                String sourcedocid = (String) kwlCommonTablesDAOObj.getRequestedObjectFields(Invoice.class, new String[]{"invoiceNumber"}, paramsMap);
                                sourceDetails.put("sourcedocid", sourcedocid);
                                sourceDetails.put("sourceType", "AR");
                                sourceValues.put(jeId, sourceDetails);
                            } else if (Constants.Acc_Consignment_GoodsReceipt_ModuleId == moduleid || Constants.Acc_Vendor_Invoice_ModuleId == moduleid || Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId == moduleid) {
                                String sourcedocid = (String) kwlCommonTablesDAOObj.getRequestedObjectFields(GoodsReceipt.class, new String[]{"goodsReceiptNumber"}, paramsMap);
                                sourceDetails.put("sourcedocid", sourcedocid);
                                sourceDetails.put("sourceType", "AP");
                                sourceValues.put(jeId, sourceDetails);
                            } else if (Constants.Acc_Credit_Note_ModuleId == moduleid) {
                                String sourcedocid = (String) kwlCommonTablesDAOObj.getRequestedObjectFields(CreditNote.class, new String[]{"creditNoteNumber"}, paramsMap);
                                sourceDetails.put("sourcedocid", sourcedocid);
                                sourceDetails.put("sourceType", "AR");
                                sourceValues.put(jeId, sourceDetails);
                            } else if (Constants.Acc_Debit_Note_ModuleId == moduleid) {
                                String sourcedocid = (String) kwlCommonTablesDAOObj.getRequestedObjectFields(DebitNote.class, new String[]{"debitNoteNumber"}, paramsMap);
                                sourceDetails.put("sourcedocid", sourcedocid);
                                sourceDetails.put("sourceType", "AP");
                                sourceValues.put(jeId, sourceDetails);
                            } else if (Constants.Acc_Receive_Payment_ModuleId == moduleid) {
                                String sourcedocid = (String) kwlCommonTablesDAOObj.getRequestedObjectFields(Receipt.class, new String[]{"receiptNumber"}, paramsMap);
                                sourceDetails.put("sourcedocid", sourcedocid);
                                sourceDetails.put("sourceType", "AR");
                                sourceValues.put(jeId, sourceDetails);
                            } else if (Constants.Acc_Make_Payment_ModuleId == moduleid) {
                                String sourcedocid = (String) kwlCommonTablesDAOObj.getRequestedObjectFields(Payment.class, new String[]{"paymentNumber"}, paramsMap);
                                sourceDetails.put("sourcedocid", sourcedocid);
                                sourceDetails.put("sourceType", "AP");
                                sourceValues.put(jeId, sourceDetails);
                            }
                        }
                    }
                    
                    double accountOpeningBalanceInBase = accInvoiceCommon.getOpeningBalanceOfAccountJson(paramsJObj, account, false, null);

                    purchases.put("jedate", je.getEntryDate().getTime());
                    purchases.put("jeaccountid", account.getAcccode() != null ? account.getAcccode() : "");
                    purchases.put("jeaccountname", account.getName() != null ? account.getName() : "");
                    purchases.put("openingbalnace", accountOpeningBalanceInBase);
                    purchases.put("creationdate", account.getCreationDate().getTime());
                    purchases.put("jedesc", je.getMemo() != null ? je.getMemo().replace("|", "").replace("\n", " ") : "");//GAF format break due to "|" & "\n" so removed.
                    purchases.put("jename", obj.optString("accountname", ""));
                    purchases.put("jeid", je.getEntryNumber());
                    purchases.put("sourcedocid", sourceValues.containsKey(jeId)&&sourceValues.get(jeId).has("sourcedocid")?sourceValues.get(jeId).getString("sourcedocid"):"");
                    purchases.put("accid", accid);
                    purchases.put("accounttype", account.getAccounttype() == 1 ? "BS" : "PL");//ERM-315
                    purchases.put("entityname", "");

                    if (obj.has("c_amount")) {
                        purchases.put("credit", obj.optDouble("c_amount", 0.0));
                        purchases.put("debit", "0.00");
                    } else {
                        purchases.put("debit", obj.optDouble("d_amount", 0.0));
                        purchases.put("credit", "0.00");
                    }
                    purchases.put("sourcetype", sourceValues.containsKey(jeId)&&sourceValues.get(jeId).has("sourceType")?sourceValues.get(jeId).getString("sourceType"):"");
                    grjArr.put(purchases);
                }
            }

            JSONObject obj = new JSONObject();
            obj.put("details", grjArr);
            jArr.put(obj);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getGLdataforIAFfileNEW : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    @Override
    public int getGSTGuideVersion(HashMap<String, Object> requestParams) throws ServiceException {
        int gstGuideVersion = Constants.GSTGuideMarch2018_Version;
        try {
            if (!StringUtil.isNullOrEmpty((String) requestParams.get(Constants.Acc_Search_Json))) {
                String searchJson = (String) requestParams.get(Constants.Acc_Search_Json);
                JSONObject jObj = new JSONObject(searchJson);
                if (jObj.getJSONArray(Constants.root).length() == 1) {
                    requestParams.put(Constants.multiEntityValue, jObj.getJSONArray(Constants.root).getJSONObject(0).optString("combosearch"));
                }
            }

            KwlReturnObject returnObj = accGstDAO.getGSTFormGenerationHistoryDetails(requestParams);
            if (returnObj.getEntityList() != null && returnObj.getEntityList().size() > 0) {
                GstFormGenerationHistory gstFormGenerationHistory = (GstFormGenerationHistory) returnObj.getEntityList().get(0);
                Date requstedFileEndDate = (Date) requestParams.get("endDate");
                if (requstedFileEndDate.before(gstFormGenerationHistory.getEndDate()) || requstedFileEndDate.equals(gstFormGenerationHistory.getEndDate())) {
                    gstGuideVersion = gstFormGenerationHistory.getGstGuideVersion();
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl.getGSTGuideVersion : " + ex.getMessage(), ex);
        }
        return gstGuideVersion;
    }
    
    /*
    *Function to delete GST Form 03 Generation History.    
    */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ServiceException.class, AccountingException.class, Exception.class})
    public KwlReturnObject deleteGSTFileGenerationHistory(JSONObject paramsObj) throws ServiceException, Exception {
        Map<String, Object> requestParams = new HashMap();
        Locale locale = null;
        if (paramsObj.has(Constants.locale)) {
            locale = (Locale) paramsObj.get(Constants.locale);
        }
        SimpleDateFormat sdf = new SimpleDateFormat(getDateOnlyFormatPattern());
        requestParams.put(Constants.df, sdf);
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
        if (!StringUtil.isNullOrEmpty(paramsObj.optString(Constants.multiEntityId))) {
            requestParams.put(Constants.multiEntityId, paramsObj.optString(Constants.multiEntityId));
        }
        /*
        * Map requestParams1 used for Audit Trial Entry.
        */
        Map<String, Object> requestParams1 = new HashMap<>();
        requestParams1.put("reqHeader", paramsObj.get("reqHeader") != null ? paramsObj.get("reqHeader") : "");
        requestParams1.put("userid", paramsObj.get("userid") != null ? paramsObj.get("userid") : "");
        requestParams1.put("remoteAddress", paramsObj.get("remoteAddress") != null ? paramsObj.get("remoteAddress") : "");
        /*
        * searchForMaxStartDate is used for selecting latest one entry.
        */
        requestParams.put("searchForMaxStartDate", true);
        requestParams.put(Constants.companyid, paramsObj.optString(Constants.companyid));
        JSONObject jobj = new JSONObject();
        jobj = getLatestDateOfFileGeneration(requestParams);
        Date deleteEntryStartDate = authHandler.getDateOnlyFormat().parse(paramsObj.optString("startdate"));
        Date deleteEntryEndDate = authHandler.getDateOnlyFormat().parse(paramsObj.optString("enddate"));
        Date lastGenerationDate = authHandler.getDateOnlyFormat().parse(jobj.optString("enddate"));
        requestParams.put("id", paramsObj.optString("id"));
        /**
         * If selected gst form 03 generation history is latest one, then delete
         * it. if it is before the latest generated then notify user to select
         * latest one.
         */
        if (!deleteEntryEndDate.before(lastGenerationDate)) {
            accGstDAO.deleteGSTFileGenerationHistory(requestParams);
        } else {
            throw new AccountingException(messageSource.getMessage("acc.gst.genHistorySelectLatest", null, locale));
        }

        String auditMsg = "User " + paramsObj.optString(Constants.userfullname) + " has deleted GST Form 03 generation history[Start Date: " + sdfDate.format(deleteEntryStartDate) + ",End Date: " + sdfDate.format(deleteEntryEndDate) + "].";
        auditTrailObj.insertAuditLog("", auditMsg, requestParams1, "");

        return new KwlReturnObject(true, null, null, null, 1);
    }
    @Override
    public JSONObject getGSTForm5SubmissionData(Map<String, Object> requestMap) {
        JSONObject jSONObject = new JSONObject();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            KwlReturnObject result = accGstDAO.getGSTForm5Details(requestMap);
            List<GstForm5eSubmissionDetails> list = result.getEntityList();
            int count=0;
            count=result.getRecordTotalCount();
            JSONArray dataArray = new JSONArray();
            if (list != null && !list.isEmpty()) {
                for (GstForm5eSubmissionDetails gstForm5SubmissionDetails : list) {
                    JSONObject json = new JSONObject();
                    json.put("id", gstForm5SubmissionDetails.getID());
                    json.put("fromdate", sdf.format(gstForm5SubmissionDetails.getDtPeriodStart()));
                    json.put("todate", sdf.format(gstForm5SubmissionDetails.getDtPeriodEnd()));
                    json.put("status", gstForm5SubmissionDetails.getStatus());  // 0  Authentication Pending | 1  Success | 2  Failure | 3 Aborted By User | 
                    json.put("submissiondate", sdf.format(gstForm5SubmissionDetails.geteSubmissionDate()));
                    if (!StringUtil.isNullObject(gstForm5SubmissionDetails.getMessageCode())) {
                        json.put("messageCode", gstForm5SubmissionDetails.getMessageCode());
                        String generalResponseMessage = messageSource.getMessage(gstForm5SubmissionDetails.getMessageCode(), null, Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));
                        json.put("responsemessages", generalResponseMessage);
                    }
                    if(!StringUtil.isNullOrEmpty(gstForm5SubmissionDetails.getResponse())){
                        JSONObject jobj = new JSONObject(gstForm5SubmissionDetails.getResponse());
                        JSONArray jArr = jobj.getJSONObject("info").getJSONArray("fieldInfoList");
                        String responseDetails ="";
                        for(int i=0; i<jArr.length();i++){
                            responseDetails +="<b>"+ jArr.getJSONObject(i).getString("field")+"</b> : ";
                            responseDetails += jArr.getJSONObject(i).getString("message")+"</br>";
                        }
                        json.put("responsedetails", responseDetails);
                    }
                    dataArray.put(json);
                }
            }
            jSONObject.put("data", dataArray);
            jSONObject.put("count", count);
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONObject;
    }
    @Override
    public JSONObject gstTransactionListingSubmissionDetails(Map<String, Object> requestMap) {
        JSONObject jSONObject = new JSONObject();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            KwlReturnObject result = accGstDAO.getTransactionListingDetails(requestMap);
            List<GSTTransactionListingSubmissionDetails> list = result.getEntityList();
            int count=0;
            count=result.getRecordTotalCount();
            JSONArray dataArray = new JSONArray();
            Calendar cal = Calendar.getInstance();
            if (list != null && !list.isEmpty()) {
                for (GSTTransactionListingSubmissionDetails gstTransactionListingDetails : list) {
                    JSONObject json = new JSONObject();
                    json.put("id", gstTransactionListingDetails.getID());
                    json.put("fromdate", sdf.format(gstTransactionListingDetails.getDtPeriodStart()));
                    json.put("todate", sdf.format(gstTransactionListingDetails.getDtPeriodEnd()));
                    json.put("status", gstTransactionListingDetails.getStatus());
                    json.put("chunknumber", gstTransactionListingDetails.getCurrentChunk()+"/"+gstTransactionListingDetails.getTotalChunk());
                    json.put("identifier",gstTransactionListingDetails.getIdentifier());
                    json.put("groupDateField", sdf.format(gstTransactionListingDetails.getDtPeriodStart())+" to "+sdf.format(gstTransactionListingDetails.getDtPeriodEnd()));
                    if (gstTransactionListingDetails.getChunkResponseDateTime() != null) {
                        json.put("submissiondate", sdf.format(gstTransactionListingDetails.getChunkResponseDateTime()));
                        json.put("submissiondatetime", sdfDateTime.format(gstTransactionListingDetails.getChunkResponseDateTime()));
                        if (gstTransactionListingDetails.getStatus() == 2) { // Failure
                            json.put("resubmitstatus", 0);
                            cal.setTime(gstTransactionListingDetails.getChunkResponseDateTime());
                            cal.add(Calendar.MINUTE, 30);
                            if (cal.getTime().before(new Date()) || cal.getTime().equals(new Date())) {
                                json.put("resubmitstatus", 1);
                            }
                        }
                    } else if(gstTransactionListingDetails.getStatus()==Constants.IRASSubmissionFlag_Pending_For_Authentication){
                        json.put("resubmitstatus", 1);
                    }
                    if(!StringUtil.isNullOrEmpty(gstTransactionListingDetails.getResponsePayload())){
                        JSONObject jobj = new JSONObject(gstTransactionListingDetails.getResponsePayload());
                        if (gstTransactionListingDetails.getStatus() == 2 && jobj.has("info") && jobj.getJSONObject("info").has("messageCode")) {
                            json.put("messageCode", jobj.getJSONObject("info").getString("messageCode"));
                            String generalResponseMessage = messageSource.getMessage(jobj.getJSONObject("info").getString("messageCode"), null, Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));
                            json.put("responsemessages", generalResponseMessage);
                            JSONArray jArr = jobj.getJSONObject("info").getJSONArray("fieldInfoList");
                            String responseDetails = "";
                            for (int i = 0; i < jArr.length(); i++) {
                                responseDetails += "<b>Record No.</b> : ";
                                responseDetails += jArr.getJSONObject(i).getString("recordID") + "</br>";
                                responseDetails += "  <b>" + jArr.getJSONObject(i).getString("field") + "</b> : ";
                                responseDetails += jArr.getJSONObject(i).getString("message") + "</br>";
                            }
                            json.put("responsedetails", responseDetails);
                        }
                    }
                    dataArray.put(json);
                }
            }
            jSONObject.put("data", dataArray);
            jSONObject.put("count", count);
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONObject;
    }
    @Override
    public JSONObject gstTransactionListingSubmissioncheck(Map<String, Object> requestMap) {
        JSONObject jSONObject = new JSONObject();
        try {
            KwlReturnObject result = accGstDAO.getTransactionListingDetails(requestMap);
            List<GSTTransactionListingSubmissionDetails> list = result.getEntityList();
            if (list != null && !list.isEmpty()) {
                jSONObject.put("message", "GST Transaction Listing already submitted for given date filter. Please check <b>GST Transaction Listing Submission History</b> for more details");
                jSONObject.put("flag", 2);
            }else{
                jSONObject.put("flag", 1);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONObject;
    }
    
    @Override
    public void getGoodsReceiptRowsForAuditFile(Map<String, Object> requestParams, List<GoodsReceipt> list, JSONArray dataArr) throws JSONException, ServiceException {
        try {
            JSONObject purchases;
            String companyid = (String) requestParams.get(Constants.companyKey);
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            boolean isSingaporeCompany = company.getCountry().getID().equalsIgnoreCase(Constants.SINGAPOREID);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            if (requestParams.containsKey(Constants.userdf)) {
                sdf = (SimpleDateFormat) requestParams.get(Constants.userdf);
            }

            for (GoodsReceipt goodsReceipt : list) {
                String documentCurrencyId = goodsReceipt.getCurrency().getCurrencyID();
                double taxPercent = (double) (requestParams.containsKey(TaxConstants.PERCENT) ? requestParams.get(TaxConstants.PERCENT) : 0);
                double originalRowAmtExcludingGST = 0.0, originalRowTaxAmt = 0.0;

                Set<GoodsReceiptDetail> rows = goodsReceipt.getRows();
                for (GoodsReceiptDetail grDetail : rows) {
                    double quantity = grDetail.getInventory().getQuantity();
                    double amount = authHandler.round(grDetail.getRate() * quantity, companyid);
                    double rdisc = (grDetail.getDiscount() == null ? 0 : grDetail.getDiscount().getDiscountValue());

                    originalRowAmtExcludingGST = amount - rdisc;//Original amount excluding gst.
                    originalRowTaxAmt = (originalRowAmtExcludingGST * taxPercent) / 100;//Original tax amount.

                    double rowAmtExGSTInbase = originalRowAmtExcludingGST;//Amount In Country Currency.
                    double taxAmtInBase = originalRowTaxAmt;//Tax amount in Country Currency.

                    if (requestParams.containsKey(Constants.gstFlag) && !gcurrencyid.equals(Constants.SGDID)) {
                        //Country is Singapore && Company currency is not SGD
                        if (!goodsReceipt.getCurrency().getCurrencyID().equals(Constants.SGDID)) {
                            //Document currency not SGD
                            if (goodsReceipt.getGstCurrencyRate() != 0) {
                                rowAmtExGSTInbase = originalRowAmtExcludingGST * goodsReceipt.getGstCurrencyRate();
                                taxAmtInBase = originalRowTaxAmt * goodsReceipt.getGstCurrencyRate();
                            } else {
                                KwlReturnObject modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowAmtExcludingGST, documentCurrencyId, Constants.SGDID, goodsReceipt.getCreationDate(), goodsReceipt.getGstCurrencyRate());
                                rowAmtExGSTInbase = (Double) modifiedAmountObj.getEntityList().get(0);
                                modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowTaxAmt, documentCurrencyId, Constants.SGDID, goodsReceipt.getCreationDate(), goodsReceipt.getGstCurrencyRate());
                                taxAmtInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                            }
                        }
                    } else if (!documentCurrencyId.equalsIgnoreCase(gcurrencyid)) {
                        /**
                         * For Malaysia - If document currency is other than
                         * base currency then amount converted in base currency.
                         */
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExcludingGST, documentCurrencyId, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        rowAmtExGSTInbase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowTaxAmt, documentCurrencyId, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        taxAmtInBase = (Double) bAmt.getEntityList().get(0);//Tax amount in base excluding gst.
                    }

                    purchases = new JSONObject();
                    purchases.put("grdate", sdf.format(goodsReceipt.getCreationDate().getTime()));
                    purchases.put("grname", goodsReceipt.getVendor().getName());
                    purchases.put("grno", goodsReceipt.getGoodsReceiptNumber());
                    purchases.put("gramt", rowAmtExGSTInbase + taxAmtInBase);
                    purchases.put("gramtexcludinggst", rowAmtExGSTInbase);
                    purchases.put("grtaxamount", taxAmtInBase);
                    purchases.put("grlineno", grDetail.getSrno());
                    purchases.put("grproduct", !StringUtil.isNullOrEmpty(grDetail.getDescription()) ? grDetail.getDescription() : !StringUtil.isNullOrEmpty(grDetail.getInventory().getProduct().getDescription()) ? grDetail.getInventory().getProduct().getDescription() : grDetail.getInventory().getProduct().getProductName());
                    purchases.put("grtaxcode", goodsReceipt.getTax().getTaxCode());
                    purchases.put("gruen", (isSingaporeCompany) ? goodsReceipt.getVendor().getUENNumber() : goodsReceipt.getVendor().getCompanyRegistrationNumber());
                    purchases.put("fcycode", goodsReceipt.getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", originalRowAmtExcludingGST + originalRowTaxAmt);
                    purchases.put("purchasefcyexcludinggst", originalRowAmtExcludingGST);
                    purchases.put("gstfcy", originalRowTaxAmt);
                    purchases.put("permit", grDetail.getPermit() != null ? grDetail.getPermit() : "");
                    purchases.put("suppliergstno", (goodsReceipt.getVendor() != null && goodsReceipt.getVendor().getGstRegistrationNumber() != null) ? goodsReceipt.getVendor().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", goodsReceipt.getJournalEntry().getEntryDate().getTime());//Posting date same as JE date.
                    purchases.put(Constants.importExportDeclarationNo, goodsReceipt.getImportDeclarationNo() != null ? goodsReceipt.getImportDeclarationNo() : "");

                    purchases.put("gramtexcludingtax", rowAmtExGSTInbase);
                    purchases.put("transactioncurrencycode", goodsReceipt.getCurrency().getCurrencyCode());
                    purchases.put("originalamountincludingtax", originalRowAmtExcludingGST + originalRowTaxAmt);
                    purchases.put("originaltaxamount", originalRowTaxAmt);

                    dataArr.put(purchases);
                }

                Set<ExpenseGRDetail> expenseRows = goodsReceipt.getExpenserows();
                for (ExpenseGRDetail expenseGRDetail : expenseRows) {
                    originalRowAmtExcludingGST = expenseGRDetail.getAmount(); //Original amount excluding gst.
                    originalRowTaxAmt = (originalRowAmtExcludingGST * taxPercent) / 100;//Original tax amount.
                    /**
                     * -----------------Rules For Transaction with Purchase Tax
                     * as per ERM-263--------- If Debit/Credit type = Debit &&
                     * Tax Type = Purchase Tax then Amount Sign = Positive If
                     * Debit/Credit type = Credit && Tax Type = Purchase Tax
                     * then Amount Sign = Negative.
                     */
                    if (!expenseGRDetail.isIsdebit()) {
                        originalRowAmtExcludingGST = -originalRowAmtExcludingGST;
                        originalRowTaxAmt = -originalRowTaxAmt;
                    }
                    double rowAmtExGSTInBase = originalRowAmtExcludingGST;
                    double rowTaxAmtInBase = originalRowTaxAmt;
                    if (requestParams.containsKey(Constants.gstFlag) && !gcurrencyid.equals(Constants.SGDID)) {
                        //Country is Singapore && Company currency is not SGD
                        if (!goodsReceipt.getCurrency().getCurrencyID().equals(Constants.SGDID)) {
                            //Document currency not SGD
                            if (goodsReceipt.getGstCurrencyRate() != 0) {
                                rowAmtExGSTInBase = originalRowAmtExcludingGST * goodsReceipt.getGstCurrencyRate();
                                rowTaxAmtInBase = originalRowTaxAmt * goodsReceipt.getGstCurrencyRate();
                            } else {
                                KwlReturnObject modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowAmtExcludingGST, documentCurrencyId, Constants.SGDID, goodsReceipt.getCreationDate(), goodsReceipt.getGstCurrencyRate());
                                rowAmtExGSTInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                                modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowTaxAmt, documentCurrencyId, Constants.SGDID, goodsReceipt.getCreationDate(), goodsReceipt.getGstCurrencyRate());
                                rowTaxAmtInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                            }
                        }
                    } else if (!documentCurrencyId.equalsIgnoreCase(gcurrencyid)) {
                        /**
                         * For Malaysia - If document currency is other than
                         * base then amount converted in base currency.
                         */
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExcludingGST, documentCurrencyId, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        rowAmtExGSTInBase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.

                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowTaxAmt, documentCurrencyId, goodsReceipt.getCreationDate(), goodsReceipt.getJournalEntry().getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                    }
                    purchases = new JSONObject();
                    purchases.put("grdate", sdf.format(goodsReceipt.getCreationDate().getTime()));
                    purchases.put("grname", goodsReceipt.getVendor().getName());
                    purchases.put("grno", goodsReceipt.getGoodsReceiptNumber());
                    purchases.put("gramt", rowAmtExGSTInBase + rowTaxAmtInBase);
                    purchases.put("gramtexcludinggst", rowAmtExGSTInBase);
                    purchases.put("grtaxamount", rowTaxAmtInBase);
                    purchases.put("grlineno", expenseGRDetail.getSrno());
                    purchases.put("grproduct", !StringUtil.isNullOrEmpty(expenseGRDetail.getDescription()) ? expenseGRDetail.getDescription() : expenseGRDetail.getAccount().getAccountName());
                    purchases.put("grtaxcode", goodsReceipt.getTax().getTaxCode());
                    purchases.put("gruen", (isSingaporeCompany) ? goodsReceipt.getVendor().getUENNumber() : goodsReceipt.getVendor().getCompanyRegistrationNumber());
                    purchases.put("fcycode", goodsReceipt.getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", originalRowAmtExcludingGST + originalRowTaxAmt);
                    purchases.put("purchasefcyexcludinggst", originalRowAmtExcludingGST);
                    purchases.put("gstfcy", originalRowTaxAmt);
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", (goodsReceipt.getVendor() != null && goodsReceipt.getVendor().getGstRegistrationNumber() != null) ? goodsReceipt.getVendor().getGstRegistrationNumber() : "");
                    purchases.put("postingdate", goodsReceipt.getJournalEntry().getEntryDate().getTime());//Posting date same as JE date.
                    purchases.put(Constants.importExportDeclarationNo, goodsReceipt.getImportDeclarationNo() != null ? goodsReceipt.getImportDeclarationNo() : "");

                    purchases.put("gramtexcludingtax", rowAmtExGSTInBase);
                    purchases.put("transactioncurrencycode", goodsReceipt.getCurrency().getCurrencyCode());
                    purchases.put("originalamountincludingtax", originalRowAmtExcludingGST + originalRowTaxAmt);
                    purchases.put("originaltaxamount", originalRowTaxAmt);

                    dataArr.put(purchases);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl.getGoodsReceiptRowsForAuditFile : " + ex.getMessage(), ex);
        }
    }

    @Override
    public void getInvoiceRowsForAuditFile(Map<String, Object> requestParams, List list, JSONArray dataArr) throws JSONException, ServiceException {
        try {
            JSONObject sales;
            String companyid = (String) requestParams.get(Constants.companyKey);
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            String countryName = company.getCountry().getCountryName();
            boolean isSingaporeCompany = company.getCountry().getID().equalsIgnoreCase(Constants.SINGAPOREID);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            if (requestParams.containsKey(Constants.userdf)) {
                sdf = (SimpleDateFormat) requestParams.get(Constants.userdf);
            }
            Iterator ite1 = list.iterator();
            while (ite1.hasNext()) {
                Object[] objects = (Object[]) ite1.next();
                Invoice invoice = (Invoice) objects[0];

                String documentCurrencyId = invoice.getCurrency().getCurrencyID();
                double taxPercent = (Double) (requestParams.containsKey(TaxConstants.PERCENT) ? requestParams.get(TaxConstants.PERCENT) : 0);
                String customerName = invoice.getCustomer().getName();
                String customerUEN = isSingaporeCompany ? invoice.getCustomer().getUENNumber() : invoice.getCustomer().getCompanyRegistrationNumber();
                String country = "";
                if (invoice.getBillingShippingAddresses() != null) {
                    if (!StringUtil.isNullOrEmpty(invoice.getBillingShippingAddresses().getShippingCountry()) && !invoice.getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                        country = invoice.getBillingShippingAddresses().getShippingCountry();
                    } else if (StringUtil.isNullOrEmpty(invoice.getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(invoice.getBillingShippingAddresses().getBillingCountry()) && !invoice.getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                        country = invoice.getBillingShippingAddresses().getBillingCountry();
                    }
                }
                double originalRowAmtExcludingGST = 0.0, originalRowTaxAmt = 0.0;
                Set<InvoiceDetail> invoiceDetails = invoice.getRows();
                for (InvoiceDetail row : invoiceDetails) {

                    double quantity = row.getInventory().getQuantity();
                    double amount = authHandler.round(row.getRate() * quantity, companyid);
                    double rdisc = (row.getDiscount() == null ? 0 : row.getDiscount().getDiscountValue());

                    originalRowAmtExcludingGST = amount - rdisc;//Original amount excluding gst.
                    originalRowTaxAmt = (originalRowAmtExcludingGST * taxPercent) / 100;//Original tax amount.

                    double rowAmtExGSTInbase = originalRowAmtExcludingGST;//Amount In Country Currency.
                    double rowTaxAmtInBase = originalRowTaxAmt;//Tax amount in Country Currency.

                    if (requestParams.containsKey(Constants.gstFlag) && !gcurrencyid.equals(Constants.SGDID)) {
                        //Country is Singapore && Company currency is not SGD
                        if (!invoice.getCurrency().getCurrencyID().equals(Constants.SGDID)) {
                            //Document currency not SGD
                            if (invoice.getGstCurrencyRate() != 0) {
                                rowAmtExGSTInbase = originalRowAmtExcludingGST * invoice.getGstCurrencyRate();
                                rowTaxAmtInBase = originalRowTaxAmt * invoice.getGstCurrencyRate();
                            } else {
                                KwlReturnObject modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowAmtExcludingGST, documentCurrencyId, Constants.SGDID, invoice.getCreationDate(), invoice.getGstCurrencyRate());
                                rowAmtExGSTInbase = (Double) modifiedAmountObj.getEntityList().get(0);
                                modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowTaxAmt, documentCurrencyId, Constants.SGDID, invoice.getCreationDate(), invoice.getGstCurrencyRate());
                                rowTaxAmtInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                            }
                        }
                    } else if (!documentCurrencyId.equalsIgnoreCase(gcurrencyid)) {
                        /**
                         * For Malaysia - If document currency is other than
                         * base currency then amount converted in base currency.
                         */
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExcludingGST, documentCurrencyId, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                        rowAmtExGSTInbase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowTaxAmt, documentCurrencyId, invoice.getCreationDate(), invoice.getJournalEntry().getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Tax amount in base excluding gst.
                    }

                    sales = new JSONObject();
                    sales.put("invdate", sdf.format(invoice.getCreationDate().getTime()));
                    sales.put("invname", customerName);
                    sales.put("invno", invoice.getInvoiceNumber());
                    sales.put("invamt", rowAmtExGSTInbase + rowTaxAmtInBase);
                    sales.put("invamtexcludinggst", rowAmtExGSTInbase);
                    sales.put("invtaxamount", rowTaxAmtInBase);
                    sales.put("invlineno", row.getSrno());
                    sales.put("invproduct", !StringUtil.isNullOrEmpty(row.getDescription()) ? row.getDescription() : (!StringUtil.isNullOrEmpty(row.getInventory().getProduct().getDescription()) ? row.getInventory().getProduct().getDescription() : row.getInventory().getProduct().getName()));
                    sales.put("invtaxcode", invoice.getTax() != null ? invoice.getTax().getTaxCodeWithoutPercentage() : "");
                    sales.put("invuen", customerUEN);
                    sales.put("invcountry", country);
                    sales.put("fcycode", invoice.getCurrency().getCurrencyCode());
                    sales.put("salesfcy", (originalRowAmtExcludingGST + originalRowTaxAmt));
                    sales.put("salesfcyexcludinggst", originalRowAmtExcludingGST);
                    sales.put("gstfcy", originalRowTaxAmt);
                    sales.put("customergstno", (invoice.getCustomer() != null && invoice.getCustomer().getGstRegistrationNumber() != null) ? invoice.getCustomer().getGstRegistrationNumber() : "");

                    sales.put("gramtexcludingtax", rowAmtExGSTInbase);
                    sales.put("transactioncurrencycode", invoice.getCurrency().getCurrencyCode());
                    sales.put("originalamountincludingtax", originalRowAmtExcludingGST + originalRowTaxAmt);
                    sales.put("originaltaxamount", originalRowTaxAmt);
                    dataArr.put(sales);

                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl.getCNOverchargeRowsForAuditFile" + ex.getMessage(), ex);
        }
    }

    @Override
    public void getDNUnderchargeRowsForAuditFile(Map<String, Object> requestParams, List<DebitNote> list, JSONArray dataArr) throws ServiceException {
        try {
            JSONObject sales;
            String companyid = (String) requestParams.get(Constants.companyKey);
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            String countryName = company.getCountry().getCountryName();
            boolean isSingaporeCompany = company.getCountry().getID().equalsIgnoreCase(Constants.SINGAPOREID);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            if (requestParams.containsKey(Constants.userdf)) {
                sdf = (SimpleDateFormat) requestParams.get(Constants.userdf);
            }
            for (DebitNote debitNote : list) {
                String documentCurrencyId = debitNote.getCurrency().getCurrencyID();
                double taxPercent = (double) (requestParams.containsKey(TaxConstants.PERCENT) ? requestParams.get(TaxConstants.PERCENT) : 0);
                String customerName = debitNote.getCustomer().getName();
                String customerUEN = isSingaporeCompany ? debitNote.getCustomer().getUENNumber() : debitNote.getCustomer().getCompanyRegistrationNumber();
                String country = "";
                if (debitNote.getBillingShippingAddresses() != null) {
                    if (!StringUtil.isNullOrEmpty(debitNote.getBillingShippingAddresses().getShippingCountry()) && !debitNote.getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                        country = debitNote.getBillingShippingAddresses().getShippingCountry();
                    } else if (StringUtil.isNullOrEmpty(debitNote.getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(debitNote.getBillingShippingAddresses().getBillingCountry()) && !debitNote.getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                        country = debitNote.getBillingShippingAddresses().getBillingCountry();
                    }
                }

                double originalRowAmtExcludingGST = 0;
                double rowAmtExGSTInBase = 0, rowTaxAmtInBase = 0;
                Set<DebitNoteAgainstCustomerGst> rows = debitNote.getRowsGst();
                for (DebitNoteAgainstCustomerGst temp : rows) {
                    originalRowAmtExcludingGST = temp.getRate() * temp.getReturnQuantity();
                    String discountType = String.valueOf(temp.getDiscountispercent());
                    double discountValue = temp.getDiscount();
                    if (discountType.equalsIgnoreCase(Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE)) {
                        originalRowAmtExcludingGST -= (originalRowAmtExcludingGST * discountValue / 100);//amount excluding discount.
                    } else {
                        originalRowAmtExcludingGST -= discountValue; //flat discount.
                    }

                    double originalRowTaxAmt = (originalRowAmtExcludingGST * taxPercent) / 100;//Original tax amount.
                    rowAmtExGSTInBase = originalRowAmtExcludingGST;
                    rowTaxAmtInBase = originalRowTaxAmt;

                    if (requestParams.containsKey(Constants.gstFlag) && !gcurrencyid.equals(Constants.SGDID)) {
                        //Country is Singapore && Company currency is not SGD
                        if (!debitNote.getCurrency().getCurrencyID().equals(Constants.SGDID)) {
                            //Document currency not SGD
                            if (debitNote.getGstCurrencyRate() != 0) {
                                rowAmtExGSTInBase = originalRowAmtExcludingGST * debitNote.getGstCurrencyRate();
                                rowTaxAmtInBase = originalRowTaxAmt * debitNote.getGstCurrencyRate();
                            } else {
                                KwlReturnObject modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowAmtExcludingGST, documentCurrencyId, Constants.SGDID, debitNote.getCreationDate(), debitNote.getGstCurrencyRate());
                                rowAmtExGSTInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                                modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowTaxAmt, documentCurrencyId, Constants.SGDID, debitNote.getCreationDate(), debitNote.getGstCurrencyRate());
                                rowTaxAmtInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                            }
                        }
                    } else if (!documentCurrencyId.equalsIgnoreCase(gcurrencyid)) {
                        /**
                         * For Malaysia - If document currency is other than
                         * base currency then amount converted in base currency.
                         */
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExcludingGST, documentCurrencyId, debitNote.getCreationDate(), debitNote.getJournalEntry().getExternalCurrencyRate());
                        rowAmtExGSTInBase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowTaxAmt, documentCurrencyId, debitNote.getCreationDate(), debitNote.getJournalEntry().getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Tax amount in base excluding gst.
                    }

                    sales = new JSONObject();
                    sales.put("invdate", sdf.format(debitNote.getCreationDate().getTime()));
                    sales.put("invname", customerName);
                    sales.put("invno", debitNote.getDebitNoteNumber());
                    sales.put("invamt", rowAmtExGSTInBase + rowTaxAmtInBase);
                    sales.put("invamtexcludinggst", rowAmtExGSTInBase);
                    sales.put("invtaxamount", rowTaxAmtInBase);
                    sales.put("invlineno", temp.getSrno());
                    sales.put("invproduct", !StringUtil.isNullOrEmpty(temp.getDescription()) ? temp.getDescription() : (!StringUtil.isNullOrEmpty(temp.getProduct().getDescription()) ? temp.getProduct().getDescription() : temp.getProduct().getName()));
                    sales.put("invtaxcode", debitNote.getTax() != null ? debitNote.getTax().getTaxCodeWithoutPercentage() : "");
                    sales.put("invuen", customerUEN);
                    sales.put("invcountry", country);
                    sales.put("fcycode", debitNote.getCurrency().getCurrencyCode());
                    sales.put("salesfcy", (originalRowAmtExcludingGST + originalRowTaxAmt));
                    sales.put("salesfcyexcludinggst", originalRowAmtExcludingGST);
                    sales.put("gstfcy", originalRowTaxAmt);
                    sales.put("customergstno", (debitNote.getCustomer() != null && debitNote.getCustomer().getGstRegistrationNumber() != null) ? debitNote.getCustomer().getGstRegistrationNumber() : "");

                    sales.put("gramtexcludingtax", rowAmtExGSTInBase);
                    sales.put("transactioncurrencycode", debitNote.getCurrency().getCurrencyCode());
                    sales.put("originalamountincludingtax", originalRowAmtExcludingGST + originalRowTaxAmt);
                    sales.put("originaltaxamount", originalRowTaxAmt);
                    dataArr.put(sales);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl.getDNUnderchargeRowsForAuditFile" + ex.getMessage(), ex);
        }
    }

    @Override
    public void getCNUnderChargeRowsForAuditFile(Map<String, Object> requestParams, List<CreditNote> list, JSONArray dataArr) throws ServiceException {
        try {
            JSONObject purchases;
            String companyid = (String) requestParams.get(Constants.companyKey);
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);

            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            boolean isSingaporeCompany = company.getCountry().getID().equalsIgnoreCase(Constants.SINGAPOREID);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            if (requestParams.containsKey(Constants.userdf)) {
                sdf = (SimpleDateFormat) requestParams.get(Constants.userdf);
            }
            for (CreditNote creditMemo : list) {
                String documentCurrencyId = creditMemo.getCurrency().getCurrencyID();
                double taxPercent = (double) (requestParams.containsKey(TaxConstants.PERCENT) ? requestParams.get(TaxConstants.PERCENT) : 0);

                String supplierName = creditMemo.getVendor().getName();
                String supplierUEN = isSingaporeCompany ? creditMemo.getVendor().getUENNumber() : creditMemo.getVendor().getCompanyRegistrationNumber();
                String supplierGSTNo = creditMemo.getVendor().getGstRegistrationNumber();

                double originalRowAmtExcludingGST = 0;
                double rowAmtExGSTInBase = 0, rowTaxAmtInBase = 0;
                Set<CreditNoteAgainstVendorGst> rows = creditMemo.getRowsGst();
                for (CreditNoteAgainstVendorGst temp : rows) {
                    originalRowAmtExcludingGST = temp.getRate() * temp.getReturnQuantity();
                    String discountType = String.valueOf(temp.getDiscountispercent());
                    double discountValue = temp.getDiscount();
                    if (discountType.equalsIgnoreCase(Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE)) {
                        originalRowAmtExcludingGST -= (originalRowAmtExcludingGST * discountValue / 100);//amount excluding discount.
                    } else {
                        originalRowAmtExcludingGST -= discountValue; //flat discount.
                    }

                    double originalRowTaxAmt = (originalRowAmtExcludingGST * taxPercent) / 100;//Original tax amount.
                    rowAmtExGSTInBase = originalRowAmtExcludingGST;
                    rowTaxAmtInBase = originalRowTaxAmt;

                    if (requestParams.containsKey(Constants.gstFlag) && !gcurrencyid.equals(Constants.SGDID)) {
                        //Country is Singapore && Company currency is not SGD
                        if (!creditMemo.getCurrency().getCurrencyID().equals(Constants.SGDID)) {
                            //Document currency not SGD
                            if (creditMemo.getGstCurrencyRate() != 0) {
                                rowAmtExGSTInBase = originalRowAmtExcludingGST * creditMemo.getGstCurrencyRate();
                                rowTaxAmtInBase = originalRowTaxAmt * creditMemo.getGstCurrencyRate();
                            } else {
                                KwlReturnObject modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowAmtExcludingGST, documentCurrencyId, Constants.SGDID, creditMemo.getCreationDate(), creditMemo.getGstCurrencyRate());
                                rowAmtExGSTInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                                modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowTaxAmt, documentCurrencyId, Constants.SGDID, creditMemo.getCreationDate(), creditMemo.getGstCurrencyRate());
                                rowTaxAmtInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                            }
                        }
                    } else if (!documentCurrencyId.equalsIgnoreCase(gcurrencyid)) {
                        /**
                         * For Malaysia - If document currency is other than
                         * base currency then amount converted in base currency.
                         */
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExcludingGST, documentCurrencyId, creditMemo.getCreationDate(), creditMemo.getJournalEntry().getExternalCurrencyRate());
                        rowAmtExGSTInBase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowTaxAmt, documentCurrencyId, creditMemo.getCreationDate(), creditMemo.getJournalEntry().getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Tax amount in base excluding gst.
                    }

                    purchases = new JSONObject();
                    purchases.put("grdate", sdf.format(creditMemo.getCreationDate().getTime()));
                    purchases.put("grname", supplierName);
                    purchases.put("grno", creditMemo.getCreditNoteNumber());
                    purchases.put("gramt", rowAmtExGSTInBase + rowTaxAmtInBase);
                    purchases.put("gramtexcludinggst", rowAmtExGSTInBase);
                    purchases.put("grtaxamount", rowTaxAmtInBase);
                    purchases.put("grlineno", temp.getSrno());
                    purchases.put("grproduct", !StringUtil.isNullOrEmpty(temp.getDescription()) ? temp.getDescription() : (!StringUtil.isNullOrEmpty(temp.getProduct().getDescription()) ? temp.getProduct().getDescription() : temp.getProduct().getName()));
                    purchases.put("grtaxcode", (temp.getTax() != null) ? temp.getTax().getTaxCode() : "");
                    purchases.put("gruen", supplierUEN);
                    purchases.put("fcycode", creditMemo.getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", (originalRowAmtExcludingGST + originalRowTaxAmt));
                    purchases.put("purchasefcyexcludinggst", originalRowAmtExcludingGST);
                    purchases.put("gstfcy", originalRowTaxAmt);
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", supplierGSTNo);

                    purchases.put("gramtexcludingtax", rowAmtExGSTInBase);
                    purchases.put("transactioncurrencycode", creditMemo.getCurrency().getCurrencyCode());
                    purchases.put("originalamountincludingtax", originalRowAmtExcludingGST + originalRowTaxAmt);
                    purchases.put("originaltaxamount", originalRowTaxAmt);

                    dataArr.put(purchases);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl.getCNUnderChargeRowsForAuditFile" + ex.getMessage(), ex);
        }
    }

    @Override
    public void getDNOverchargeRowsForAuditFile(Map<String, Object> requestParams, List<DebitNote> list, JSONArray dataArr) throws ServiceException {
        try {
            JSONObject purchases;
            String companyid = (String) requestParams.get(Constants.companyKey);
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            boolean isSingaporeCompany = company.getCountry().getID().equalsIgnoreCase(Constants.SINGAPOREID);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            if (requestParams.containsKey(Constants.userdf)) {
                sdf = (SimpleDateFormat) requestParams.get(Constants.userdf);
            }
            for (DebitNote debitMemo : list) {
                String documentCurrencyId = debitMemo.getCurrency().getCurrencyID();
                double taxPercent = (double) (requestParams.containsKey(TaxConstants.PERCENT) ? requestParams.get(TaxConstants.PERCENT) : 0);

                String supplierName = debitMemo.getVendor().getName();
                String supplierUEN = isSingaporeCompany ? debitMemo.getVendor().getUENNumber() : debitMemo.getVendor().getCompanyRegistrationNumber();
                String supplierGSTNo = debitMemo.getVendor().getGstRegistrationNumber();

                double originalRowAmtExGST = 0, originalRowTaxAmt = 0;
                double rowAmtExGSTInBase = 0, rowTaxAmtInBase = 0;

                Set<DebitNoteAgainstCustomerGst> rowGstDetail = debitMemo.getRowsGst();
                for (DebitNoteAgainstCustomerGst row : rowGstDetail) {
                    originalRowAmtExGST = row.getRate() * row.getReturnQuantity();
                    double discountValue = row.getDiscount();
                    String discountType = String.valueOf(row.getDiscountispercent());
                    if (discountType.equalsIgnoreCase(Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE)) {
                        originalRowAmtExGST -= (originalRowAmtExGST * discountValue / 100);// amount excluding discount.ERP-38665
                    } else {
                        originalRowAmtExGST -= discountValue; //flat discount 
                    }
                    originalRowTaxAmt = (originalRowAmtExGST * taxPercent) / 100;//Original tax amount.

                    /**
                     * -----------------Rules For Transaction with Purchase Tax
                     * as per ERM-263--------- If Debit/Credit type = Debit &&
                     * Tax Type = Purchase Tax then Amount Sign = Positive If
                     * Debit/Credit type = Credit && Tax Type = Purchase Tax
                     * then Amount Sign = Negative
                     *
                     * By default type of tax account is credit for DebitNote.
                     */
                    originalRowAmtExGST = -originalRowAmtExGST;
                    originalRowTaxAmt = -originalRowTaxAmt;
                    /**
                     * ***************************************************************************
                     */

                    rowAmtExGSTInBase = originalRowAmtExGST;
                    rowTaxAmtInBase = originalRowTaxAmt;

                    if (requestParams.containsKey(Constants.gstFlag) && !gcurrencyid.equals(Constants.SGDID)) {
                        //Country is Singapore && Company currency is not SGD
                        if (!debitMemo.getCurrency().getCurrencyID().equals(Constants.SGDID)) {
                            //Document currency not SGD
                            if (debitMemo.getGstCurrencyRate() != 0) {
                                rowAmtExGSTInBase = originalRowAmtExGST * debitMemo.getGstCurrencyRate();
                                rowTaxAmtInBase = originalRowTaxAmt * debitMemo.getGstCurrencyRate();
                            } else {
                                KwlReturnObject modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowAmtExGST, documentCurrencyId, Constants.SGDID, debitMemo.getCreationDate(), debitMemo.getGstCurrencyRate());
                                rowAmtExGSTInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                                modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowTaxAmt, documentCurrencyId, Constants.SGDID, debitMemo.getCreationDate(), debitMemo.getGstCurrencyRate());
                                rowTaxAmtInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                            }
                        }
                    } else if (!documentCurrencyId.equalsIgnoreCase(gcurrencyid)) {
                        /**
                         * For Malaysia - If document currency is other than
                         * base currency then amount converted in base currency.
                         */
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExGST, documentCurrencyId, debitMemo.getCreationDate(), debitMemo.getJournalEntry().getExternalCurrencyRate());
                        rowAmtExGSTInBase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowTaxAmt, documentCurrencyId, debitMemo.getCreationDate(), debitMemo.getJournalEntry().getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Tax amount in base excluding gst.
                    }

                    purchases = new JSONObject();
                    purchases.put("grdate", sdf.format(debitMemo.getCreationDate().getTime()));
                    purchases.put("grname", supplierName);
                    purchases.put("grno", debitMemo.getDebitNoteNumber());
                    purchases.put("gramt", rowAmtExGSTInBase + rowTaxAmtInBase);
                    purchases.put("gramtexcludinggst", rowAmtExGSTInBase);
                    purchases.put("grtaxamount", rowTaxAmtInBase);
                    purchases.put("grlineno", row.getSrno());
                    purchases.put("grproduct", !StringUtil.isNullOrEmpty(row.getDescription()) ? row.getDescription() : (!StringUtil.isNullOrEmpty(row.getProduct().getDescription()) ? row.getProduct().getDescription() : row.getProduct().getName()));
                    purchases.put("grtaxcode", (debitMemo.getTax() != null) ? debitMemo.getTax().getTaxCodeWithoutPercentage() : "");
                    purchases.put("gruen", supplierUEN);
                    purchases.put("fcycode", debitMemo.getCurrency().getCurrencyCode());
                    purchases.put("purchasefcy", (originalRowAmtExGST + originalRowTaxAmt));
                    purchases.put("purchasefcyexcludinggst", originalRowAmtExGST);
                    purchases.put("gstfcy", originalRowTaxAmt);
                    purchases.put("permit", "");
                    purchases.put("suppliergstno", supplierGSTNo);

                    purchases.put("gramtexcludingtax", rowAmtExGSTInBase);
                    purchases.put("transactioncurrencycode", debitMemo.getCurrency().getCurrencyCode());
                    purchases.put("originalamountincludingtax", originalRowAmtExGST + originalRowTaxAmt);
                    purchases.put("originaltaxamount", originalRowTaxAmt);
                    dataArr.put(purchases);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl.getDNOverchargeRowsForAuditFile" + ex.getMessage(), ex);
        }

    }

    @Override
    public void getCNOverchargeRowsForAuditFile(Map<String, Object> requestParams, List<CreditNote> list, JSONArray dataArr) throws ServiceException {
        try {
            JSONObject sales;
            String companyid = (String) requestParams.get(Constants.companyKey);
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            String countryName = company.getCountry().getCountryName();
            boolean isSingaporeCompany = company.getCountry().getID().equalsIgnoreCase(Constants.SINGAPOREID);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            if (requestParams.containsKey(Constants.userdf)) {
                sdf = (SimpleDateFormat) requestParams.get(Constants.userdf);
            }
            for (CreditNote creditMemo : list) {
                String documentCurrencyId = creditMemo.getCurrency().getCurrencyID();
                double taxPercent = (double) (requestParams.containsKey(TaxConstants.PERCENT) ? requestParams.get(TaxConstants.PERCENT) : 0);
                String customerName = creditMemo.getCustomer().getName();
                String customerUEN = isSingaporeCompany ? creditMemo.getCustomer().getUENNumber() : creditMemo.getCustomer().getCompanyRegistrationNumber();
                String country = "";
                if (creditMemo.getBillingShippingAddresses() != null) {
                    if (!StringUtil.isNullOrEmpty(creditMemo.getBillingShippingAddresses().getShippingCountry()) && !creditMemo.getBillingShippingAddresses().getShippingCountry().equalsIgnoreCase(countryName)) {
                        country = creditMemo.getBillingShippingAddresses().getShippingCountry();
                    } else if (StringUtil.isNullOrEmpty(creditMemo.getBillingShippingAddresses().getShippingCountry()) && !StringUtil.isNullOrEmpty(creditMemo.getBillingShippingAddresses().getBillingCountry()) && !creditMemo.getBillingShippingAddresses().getBillingCountry().equalsIgnoreCase(countryName)) {
                        country = creditMemo.getBillingShippingAddresses().getBillingCountry();
                    }
                }

                double originalRowAmtExcludingGST = 0;
                double rowAmtExGSTInBase = 0, rowTaxAmtInBase = 0;
                Set<CreditNoteAgainstVendorGst> rows = creditMemo.getRowsGst();
                for (CreditNoteAgainstVendorGst temp : rows) {
                    originalRowAmtExcludingGST = temp.getRate() * temp.getReturnQuantity();
                    String discountType = String.valueOf(temp.getDiscountispercent());
                    double discountValue = temp.getDiscount();
                    if (discountType.equalsIgnoreCase(Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE)) {
                        originalRowAmtExcludingGST -= (originalRowAmtExcludingGST * discountValue / 100);//amount excluding discount.
                    } else {
                        originalRowAmtExcludingGST -= discountValue; //flat discount.
                    }
                    double originalRowTaxAmt = (originalRowAmtExcludingGST * taxPercent) / 100;//Original tax amount.

                    /**
                     * -----------------Rules For Transaction with Purchase Tax
                     * as per ERM-263--------- If Debit/Credit type = Debit &&
                     * Tax Type = Purchase Tax then Amount Sign = Positive If
                     * Debit/Credit type = Credit && Tax Type = Purchase Tax
                     * then Amount Sign = Negative
                     *
                     * By default type of tax account is debit for CreditNote.
                     */
                    originalRowAmtExcludingGST = -originalRowAmtExcludingGST;
                    originalRowTaxAmt = -originalRowTaxAmt;
                    /**
                     * ***************************************************************************
                     */
                    rowAmtExGSTInBase = originalRowAmtExcludingGST;
                    rowTaxAmtInBase = originalRowTaxAmt;

                    if (requestParams.containsKey(Constants.gstFlag) && !gcurrencyid.equals(Constants.SGDID)) {
                        //Country is Singapore && Company currency is not SGD
                        if (!creditMemo.getCurrency().getCurrencyID().equals(Constants.SGDID)) {
                            //Document currency not SGD
                            if (creditMemo.getGstCurrencyRate() != 0) {
                                rowAmtExGSTInBase = originalRowAmtExcludingGST * creditMemo.getGstCurrencyRate();
                                rowTaxAmtInBase = originalRowTaxAmt * creditMemo.getGstCurrencyRate();
                            } else {
                                KwlReturnObject modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowAmtExcludingGST, documentCurrencyId, Constants.SGDID, creditMemo.getCreationDate(), creditMemo.getGstCurrencyRate());
                                rowAmtExGSTInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                                modifiedAmountObj = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, originalRowTaxAmt, documentCurrencyId, Constants.SGDID, creditMemo.getCreationDate(), creditMemo.getGstCurrencyRate());
                                rowTaxAmtInBase = (Double) modifiedAmountObj.getEntityList().get(0);
                            }
                        }
                    } else if (!documentCurrencyId.equalsIgnoreCase(gcurrencyid)) {
                        /**
                         * For Malaysia - If document currency is other than
                         * base currency then amount converted in base currency.
                         */
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowAmtExcludingGST, documentCurrencyId, creditMemo.getCreationDate(), creditMemo.getJournalEntry().getExternalCurrencyRate());
                        rowAmtExGSTInBase = (Double) bAmt.getEntityList().get(0);//Amount in base excluding gst.
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, originalRowTaxAmt, documentCurrencyId, creditMemo.getCreationDate(), creditMemo.getJournalEntry().getExternalCurrencyRate());
                        rowTaxAmtInBase = (Double) bAmt.getEntityList().get(0);//Tax amount in base excluding gst.
                    }

                    sales = new JSONObject();
                    sales.put("invdate", sdf.format(creditMemo.getCreationDate().getTime()));
                    sales.put("invname", customerName);
                    sales.put("invno", creditMemo.getCreditNoteNumber());
                    sales.put("invamt", rowAmtExGSTInBase + rowTaxAmtInBase);
                    sales.put("invamtexcludinggst", rowAmtExGSTInBase);
                    sales.put("invtaxamount", rowTaxAmtInBase);
                    sales.put("invlineno", temp.getSrno());
                    sales.put("invproduct", !StringUtil.isNullOrEmpty(temp.getDescription()) ? temp.getDescription() : (!StringUtil.isNullOrEmpty(temp.getProduct().getDescription()) ? temp.getProduct().getDescription() : temp.getProduct().getName()));
                    sales.put("invtaxcode", creditMemo.getTax() != null ? creditMemo.getTax().getTaxCodeWithoutPercentage() : "");
                    sales.put("invuen", customerUEN);
                    sales.put("invcountry", country);
                    sales.put("fcycode", creditMemo.getCurrency().getCurrencyCode());
                    sales.put("salesfcy", (originalRowAmtExcludingGST + originalRowTaxAmt));
                    sales.put("salesfcyexcludinggst", originalRowAmtExcludingGST);
                    sales.put("gstfcy", originalRowTaxAmt);
                    sales.put("customergstno", (temp.getCreditNote().getCustomer() != null && temp.getCreditNote().getCustomer().getGstRegistrationNumber() != null) ? temp.getCreditNote().getCustomer().getGstRegistrationNumber() : "");

                    sales.put("gramtexcludingtax", rowAmtExGSTInBase);
                    sales.put("transactioncurrencycode", creditMemo.getCurrency().getCurrencyCode());
                    sales.put("originalamountincludingtax", originalRowAmtExcludingGST + originalRowTaxAmt);
                    sales.put("originaltaxamount", originalRowTaxAmt);
                    dataArr.put(sales);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGstServiceImpl.getCNOverchargeRowsForAuditFile" + ex.getMessage(), ex);
        }
    }

    /**
     * Function for export chunk data which is in message part 
     * @request : JSON Object containing chunk id
     * @response : JSON Object contain chunk data & chunk number
     */
    @Override
    public JSONObject gstTransactionListingChunkData(JSONObject jobj) {
        JSONObject jSONObject = new JSONObject();
        try {
            KwlReturnObject result = accountingHandlerDAOobj.getObject(GSTTransactionListingSubmissionDetails.class.getName(), jobj.optString("id"));
            GSTTransactionListingSubmissionDetails gstTL = (GSTTransactionListingSubmissionDetails) result.getEntityList().get(0);
            String requestPayload = gstTL.getRequestPayload();
            JSONObject jobjRequestPayload = new JSONObject(requestPayload);
            JSONObject jobjData= jobjRequestPayload.getJSONObject("data");
            String message = jobjData.getString("message");
            String currentChunk = jobjData.getString("currentChunk");
            String chunkData = Base64.base64Decode(message);
            jSONObject.put("message", chunkData);
            jSONObject.put("chunknumber", currentChunk);
            
        } catch (Exception ex) {
            Logger.getLogger(AccGstServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONObject;
    }
     
}
