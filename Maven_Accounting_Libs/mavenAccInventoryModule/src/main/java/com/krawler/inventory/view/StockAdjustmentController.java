/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.*;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.NegativeInventoryException;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.inventory.model.configuration.InventoryConfig;
import com.krawler.inventory.model.configuration.InventoryConfigService;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockout.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Vipin Gupta
 */
public class StockAdjustmentController extends MultiActionController {

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static final Logger lgr = Logger.getLogger(StockAdjustmentController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private StockAdjustmentService stockAdjustmentService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private StoreService storeService;
    private SeqService seqService;
    private LocationService locationService;
    private InventoryConfigService invConfigService;
    private ConsignmentService consignmentService;
    private exportMPXDAOImpl exportDAO;
    private auditTrailDAO auditTrailObj;
    private StockService stockService;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accJournalEntryDAO accJournalEntryobj;
    private fieldDataManager fieldDataManagercntrl;
    private accAccountDAO accAccountDAOobj;
    private MessageSource messageSource;

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setInvConfigService(InventoryConfigService invConfigService) {
        this.invConfigService = invConfigService;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

    public void setStockAdjustmentService(StockAdjustmentService stockAdjustmentService) {
        this.stockAdjustmentService = stockAdjustmentService;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setConsignmentService(ConsignmentService consignmentService) {
        this.consignmentService = consignmentService;
    }

    public void setExportDAO(exportMPXDAOImpl exportDAO) {
        this.exportDAO = exportDAO;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public ModelAndView requestStockAdjustment(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "", billid = "";
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String seqNo = request.getParameter("sequenceNo");
            String qApproval = request.getParameter("qApproval");
            InventoryConfig config = invConfigService.getConfigByCompany(company);
            if ("true".equals(qApproval) && !config.isEnableStockoutApprovalFlow()) {
                msg = "StockoutApprovalFlow Is Not set";
            } else {
                ExtraCompanyPreferences extraCompanyPreferences = null;
                KwlReturnObject extraprefresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
                extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                KwlReturnObject capresult = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
                Date bussinessDate = df.parse(request.getParameter("businessdate"));
                boolean sendForQAApproval = false;
                boolean isSkuforCompany=extraCompanyPreferences.isSKUFieldParm();
//            String longBD = request.getParameter("longbusinessdate");
//            long bussinessDateInLong = Long.parseLong(longBD);
//            Date bussinessDate = new Date();
//            bussinessDate.setTime(bussinessDateInLong);

                String fromStoreId = request.getParameter("fromstore");
                String memo = (request.getParameter("memo")) != null ? request.getParameter("memo") : "";
                boolean isDraft = Boolean.parseBoolean(request.getParameter("isdraft"));
                boolean isJobWorkInReciever = Boolean.parseBoolean(request.getParameter("isJobWorkInReciever"));
//                String challanno = (request.getParameter("challanno"));
                String jobworkorderno = (request.getParameter("jobworkorderno"));
                String customer = (request.getParameter("customer"));
                Store fromStore = storeService.getStoreById(fromStoreId);
                String records = request.getParameter("jsondata");
                String seqFormatId = request.getParameter("seqFormatId");
                String documentNumber = request.getParameter("documentNumber");
                String adjustmentReason = request.getParameter("adjustmentReason");
                JSONArray jArr = new JSONArray(records);
                SeqFormat seqFormat = null;
                SeqFormat  assetSeqFormat =null;
                try {
                    assetSeqFormat = isSkuforCompany ? seqService.getDefaultSeqFormat(company, ModuleConst.Asset_Module) : null;
                } catch (Exception ex) {
                }
                String customfield = request.getParameter("customfield");
                if (!"NA".equals(seqFormatId)) {
                    if (!StringUtil.isNullOrEmpty(seqFormatId)) {
                        seqFormat = seqService.getSeqFormat(seqFormatId);
                    } else {
                        seqFormat = seqService.getDefaultSeqFormat(company, ModuleConst.STOCK_ADJUSTMENT);
                    }
                }
                boolean allowNegativeInventory = false;

                if (!StringUtil.isNullOrEmpty(request.getParameter("allowNegativeInventory"))) {
                    allowNegativeInventory = Boolean.parseBoolean(request.getParameter("allowNegativeInventory"));
                }

                synchronized (this) {
                     boolean seqExist=false;
                    if ("NA".equals(seqFormatId)) {
                        seqNo = documentNumber;
                         seqExist = seqService.isExistingSeqNumber(seqNo, company, ModuleConst.STOCK_ADJUSTMENT);
                        if (seqExist) {
                            throw new InventoryException("Sequence number already exist, please enter other one.");
                        }
                    } else {
                         seqExist = false;
                        do {
                            seqNo = seqService.getNextFormatedSeqNumber(seqFormat);
                            seqExist = seqService.isExistingSeqNumber(seqNo, company, ModuleConst.STOCK_ADJUSTMENT);
                            if (seqExist) {
                                seqService.updateSeqNumber(seqFormat);
                            }
                        } while (seqExist);
                    }
                    txnManager.commit(status);
                    seqNo = seqFormat != null ? seqService.getNextFormatedSeqNumber(seqFormat) : documentNumber;
                    if (!seqExist) {
                        status = null;
                        for (int i = 0; i < jArr.length(); i++) {
                            status = txnManager.getTransaction(def);
                            JSONObject jObj = jArr.optJSONObject(i);
                            String productId = jObj.optString("productid");
                            String uomId = jObj.optString("uomid");
                            String remark = jObj.optString("remark", "");
                            String throughFile="NA";
                            String adjustmentType = jObj.optString("adjustmentType");
                            String costCenterId = jObj.optString("costcenter");
                            double aQty = jObj.optDouble("quantity", 0);
                            double quantity = authHandler.roundQuantity((aQty), companyId);
                            double amount = jObj.optDouble("purchaseprice", 0);
                            String reason = jObj.optString("reason");
                            String linelevelcustomdata = jObj.optJSONArray(Constants.LineLevelCustomData)!=null?jObj.optJSONArray(Constants.LineLevelCustomData).toString():"";

                            jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                            Product product = (Product) jeresult.getEntityList().get(0);

                            jeresult = accountingHandlerDAO.getObject(UnitOfMeasure.class.getName(), uomId);
                            UnitOfMeasure uom = (UnitOfMeasure) jeresult.getEntityList().get(0);

                            jeresult = accountingHandlerDAO.getObject(CostCenter.class.getName(), costCenterId);
                            CostCenter costCenter = (CostCenter) jeresult.getEntityList().get(0);

                            KwlReturnObject kwlObj = accountingHandlerDAO.getObject(MasterItem.class.getName(), adjustmentReason);
                            MasterItem masterItem = (MasterItem) kwlObj.getEntityList().get(0);

                            StockAdjustment stockAdjustment = new StockAdjustment(product, fromStore, uom, quantity, amount, bussinessDate);
                            stockAdjustment.setTransactionNo(seqNo);
                            stockAdjustment.setRemark(remark);
                            stockAdjustment.setThroughFile(throughFile);
                            stockAdjustment.setCostCenter(costCenter);
                            stockAdjustment.setAdjustmentType(!StringUtil.isNullOrEmpty(adjustmentType)?adjustmentType:null);
                            stockAdjustment.setReason(reason);
                            stockAdjustment.setIsJobWorkIn(isJobWorkInReciever);
//                            stockAdjustment.setChallanno(challanno);
                            stockAdjustment.setFinalQuantity(0);
                            stockAdjustment.setMemo(memo);
                            stockAdjustment.setStockAdjustmentReason(adjustmentReason);

                            // Create Journal Entry Number for wastage case
                            if (masterItem != null && masterItem.getDefaultMasterItem() != null && masterItem.getDefaultMasterItem().getID().equalsIgnoreCase(Constants.WASTAGE_ID)) {
                                String oldjeid = "";
                                String jeentryNumber = "";
                                String jeIntegerPart = "";
                                String jeDatePrefix = "";
                                String jeDateAfterPrefix = "";
                                String jeDateSuffix = "";
                                String jeSeqFormatId = "";
                                boolean jeautogenflag = false;
                                if (StringUtil.isNullOrEmpty(oldjeid)) {
                                    synchronized (this) {
                                        Map<String, Object> JEFormatParams = new HashMap<>();
                                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                        JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                        JEFormatParams.put("companyid", companyId);
                                        JEFormatParams.put("isdefaultFormat", true);

                                        kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, bussinessDate);
                                        jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                        jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                        jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                        jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                                        jeSeqFormatId = format.getID();
                                        jeautogenflag = true;
                                    }
                                }

                                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                                jeDataMap.put("entrynumber", jeentryNumber);
                                jeDataMap.put("autogenerated", jeautogenflag);
                                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                                jeDataMap.put("entrydate", bussinessDate);
                                jeDataMap.put("companyid", companyId);
                                jeDataMap.put("memo", "Stock Adjustment JE for " + product.getName());
                                jeDataMap.put("createdby", sessionHandlerImpl.getUserid(request));
                                jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));

                                jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
                                JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

                                stockAdjustment.setJournalEntry(journalEntry);
                                String jeid = journalEntry.getID();
                                jeDataMap.put("jeid", jeid);

                                HashSet jeDetails = new HashSet();
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size() + 1);
                                jedjson.put("companyid", companyId);
                                jedjson.put("amount", authHandler.round((quantity * amount), companyId));
                                jedjson.put("accountid", product.getWastageAccount().getID());
                                jedjson.put("debit", true);
                                jedjson.put("jeid", jeid);
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);

                                if (product.getProducttype().getID().equalsIgnoreCase(Producttype.INVENTORY_PART)) {
                                    jedjson = new JSONObject();
                                    jedjson.put("srno", jeDetails.size() + 1);
                                    jedjson.put("companyid", companyId);
                                    jedjson.put("amount", authHandler.round((quantity * amount), companyId));
                                    jedjson.put("accountid", product.getPurchaseAccount().getID());
                                    jedjson.put("debit", false);
                                    jedjson.put("jeid", jeid);
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jeDetails.add(jed);
                                } else if (product.getProducttype().getID().equalsIgnoreCase(Producttype.ASSEMBLY)) {
                                    jedjson = new JSONObject();
                                    jedjson.put("srno", jeDetails.size() + 1);
                                    jedjson.put("companyid", companyId);
                                    jedjson.put("amount", authHandler.round((quantity * amount), companyId));
                                    jedjson.put("accountid", product.getSalesAccount().getID());
                                    jedjson.put("debit", false);
                                    jedjson.put("jeid", jeid);
                                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                    jeDetails.add(jed);
                                }
                            }
                            
                            double totalFinalQuantity = 0;
                            JournalEntry inventoryJE = null;
                            if (product.getInventoryAccount()!= null && !StringUtil.isNullOrEmpty(adjustmentType) && (adjustmentType.equalsIgnoreCase("Stock Sales") || adjustmentType.equalsIgnoreCase("Stock Out") || adjustmentType.equalsIgnoreCase("Stock In")) && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                                // Create Journal Entry Number for MRP module
                                String oldjeid = "";
                                String jeentryNumber = "";
                                String jeIntegerPart = "";
                                String jeDatePrefix = "";
                                String jeDateAfterPrefix = "";
                                String jeDateSuffix = "";
                                String jeSeqFormatId = "";
                                boolean jeautogenflag = false;
                                if (StringUtil.isNullOrEmpty(oldjeid)) {
                                    synchronized (this) {
                                        Map<String, Object> JEFormatParams = new HashMap<>();
                                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                                        JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                                        JEFormatParams.put("companyid", companyId);
                                        JEFormatParams.put("isdefaultFormat", true);
                                        kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, bussinessDate);
                                        jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                                        jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                        jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                                        jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                                        jeSeqFormatId = format.getID();
                                        jeautogenflag = true;
                                    }
                                }
                                Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                                jeDataMap.put("entrynumber", jeentryNumber);
                                jeDataMap.put("autogenerated", jeautogenflag);
                                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                                jeDataMap.put("entrydate", bussinessDate);
                                jeDataMap.put("companyid", companyId);
                                jeDataMap.put("memo", "Stock Adjustment JE for " + product.getName());
                                jeDataMap.put("createdby", sessionHandlerImpl.getUserid(request));
                                jeDataMap.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                                jeDataMap.put("transactionModuleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                                jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
                                inventoryJE = (JournalEntry) jeresult.getEntityList().get(0);
                                stockAdjustment.setInventoryJE(inventoryJE);
                                HashSet jeDetails = new HashSet();
                                JSONObject jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size() + 1);
                                jedjson.put("companyid", companyId);
                                if (adjustmentType.equalsIgnoreCase("Stock Sales") || adjustmentType.equalsIgnoreCase("Stock Out")) { // Downward Direction-Stock OUT
                                    jedjson.put("amount", authHandler.round(((quantity * amount) * (-1)), companyId));
                                } else {
                                    jedjson.put("amount", authHandler.round(quantity * amount, companyId));
                                }
                                if (adjustmentType.equalsIgnoreCase("Stock Sales") || adjustmentType.equalsIgnoreCase("Stock Out")) {// Upward Direction-Stock IN
                                    jedjson.put("debit", false);
                                } else {
                                    jedjson.put("debit", true);
                                }
                                jedjson.put("accountid", product.getInventoryAccount().getID());
                                jedjson.put("jeid", inventoryJE.getID());
                                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);
                                jedjson = new JSONObject();
                                jedjson.put("srno", jeDetails.size() + 1);
                                jedjson.put("companyid", companyId);
                                if (adjustmentType.equalsIgnoreCase("Stock Sales") || adjustmentType.equalsIgnoreCase("Stock Out")) { // Downward Direction-Stock OUT
                                    jedjson.put("amount", authHandler.round(((quantity * amount) * (-1)), companyId));
                                } else {
                                    jedjson.put("amount", authHandler.round(quantity * amount, companyId));
                                }
                                jedjson.put("accountid", product.getStockAdjustmentAccount().getID());
                                if (adjustmentType.equalsIgnoreCase("Stock Sales") || adjustmentType.equalsIgnoreCase("Stock Out")) { // Downward Direction-Stock OUT
                                    jedjson.put("debit", true);
                                } else {
                                    jedjson.put("debit", false);
                                }
                                jedjson.put("jeid", inventoryJE.getID());
                                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                                jeDetails.add(jed);
                                inventoryJE.setDetails(jeDetails);
                                accJournalEntryobj.saveJournalEntryDetailsSet(jeDetails);
                            }
                            Set<StockAdjustmentDetail> adjustmentDetailSet = new HashSet<StockAdjustmentDetail>();
                            JSONArray stockDetails = jObj.optJSONArray("stockDetails");
                            for (int x = 0; x < stockDetails.length(); x++) {
                                JSONObject detailObj = stockDetails.optJSONObject(x);
                                String locationId = detailObj.optString("locationId");
                                String rowId = detailObj.optString("rowId");
                                String rackId = detailObj.optString("rackId");
                                String binId = detailObj.optString("binId");
                                String batchName = detailObj.optString("batchName");
                                batchName=(batchName!=null&&!(batchName.equals("null")))?batchName:"";
                                String serialNames = detailObj.optString("serialNames");
                                String skuFields = detailObj.optString("skuFields");
                                String approvalSerials = detailObj.optString("approvalSerials");
                                double qty = authHandler.roundQuantity((detailObj.optDouble("quantity")), companyId);
                                String mfgdate = detailObj.optString("mfgdate");
                                String expdate = detailObj.optString("expdate");
                                String warrantyexpfromdate = detailObj.optString("warrantyexpfromdate");
                                String warrantyexptodate = detailObj.optString("warrantyexptodate");

                                Location location = locationService.getLocation(locationId);
                                StoreMaster row = null;
                                if (product.isIsrowforproduct()) {
                                    row = storeService.getStoreMaster(rowId);
                                }
                                StoreMaster rack = null;
                                if (product.isIsrackforproduct()) {
                                    rack = storeService.getStoreMaster(rackId);
                                }
                                StoreMaster bin = null;
                                if (product.isIsbinforproduct()) {
                                    bin = storeService.getStoreMaster(binId);
                                }
                                if (StringUtil.isNullOrEmpty(warrantyexpfromdate) || warrantyexpfromdate.split(",").length == 0) {
                                    warrantyexpfromdate = expdate;
                                }
                                if (StringUtil.isNullOrEmpty(warrantyexptodate) || warrantyexptodate.split(",").length == 0) {
                                    warrantyexptodate = expdate;
                                }

                                if (quantity > 0 && (product.isIsSerialForProduct() || product.isIsBatchForProduct()) 
                                        && (!StringUtil.isNullOrEmpty(expdate) && expdate.split(",").length > 0) 
                                        && (!StringUtil.isNullOrEmpty(warrantyexpfromdate)) && !StringUtil.isNullOrEmpty(warrantyexptodate)
                                        || (!StringUtil.isNullOrEmpty(serialNames) && adjustmentType.equalsIgnoreCase("Stock In"))&&product.isIsSKUForProduct()) {
                                    Map<String, Object> tempTablMap = new HashMap<String, Object>();
                                    tempTablMap.put("serials", serialNames);
                                    tempTablMap.put("mfgdate", mfgdate);
                                    tempTablMap.put("expdate", expdate);
                                    tempTablMap.put("warrantyexpfromdate", warrantyexpfromdate);
                                    tempTablMap.put("warrantyexptodate", warrantyexptodate);
                                    tempTablMap.put("skufields", skuFields);
                                    
                                    String removeStr=skuFields.replace(",", "");
                                    
                                    if (product.isIsSKUForProduct() && (StringUtil.isNullOrEmpty(skuFields)) && adjustmentType.equalsIgnoreCase("Stock In")) {
                                        if (assetSeqFormat != null) {
                                            String[] srArr = serialNames.split(",");
                                            String skuStr = "";
                                            for (int sr = 0; sr < srArr.length; sr++) {
                                                String asetSeqNo = seqService.getNextFormatedSeqNumber(assetSeqFormat);
                                                if (StringUtil.isNullOrEmpty(skuStr)) {
                                                    skuStr = asetSeqNo;
                                                } else {
                                                    skuStr = skuStr + "," + asetSeqNo;
                                                }
                                                seqService.updateSeqNumber(assetSeqFormat);
                                            }
                                            tempTablMap.put("skufields", skuStr);
                                        }else{
                                             throw new InventoryException(InventoryException.Type.NULL, "Please set sequence format for asset.");
                                        }
                                    }

                                    stockAdjustmentService.saveSADetailInTemporaryTable(product, fromStore, location, batchName, tempTablMap);
//                                stockAdjustmentService.saveSADetailInTemporaryTable(product, fromStore, location, batchName, serialNames, mfgdate, expdate, warrantyexpfromdate, warrantyexptodate, skuFields);
                                } 
                                String finalSerialNames = serialNames;
                                double finalQuantity = qty;
                                if (product.isIsSerialForProduct()) {
                                    if (!StringUtil.isNullOrEmpty(approvalSerials)) {
                                        String[] adjSerialArr = serialNames.split(",");
                                        String[] approvalSerialArr = approvalSerials.split(",");
                                        Set<String> adjSerialSet = new HashSet<String>(Arrays.asList(adjSerialArr));
                                        Set<String> approvalSerialSet = new HashSet<String>(Arrays.asList(approvalSerialArr));
                                        adjSerialSet.removeAll(approvalSerialSet);
                                        finalSerialNames = "";
                                        for (String serial : adjSerialSet) {
                                            if (StringUtil.isNullOrEmpty(finalSerialNames)) {
                                                finalSerialNames = serial;
                                            } else {
                                                finalSerialNames += "," + serial;
                                            }
                                        }
                                        finalQuantity = adjSerialSet.size();

                                    }
                                    totalFinalQuantity += finalQuantity;
                                } else {
                                    finalQuantity = 0;
                                    totalFinalQuantity = 0;
                                    finalSerialNames = null;
                                }


                                StockAdjustmentDetail sad = new StockAdjustmentDetail();
                                sad.setStockAdjustment(stockAdjustment);
                                sad.setLocation(location);
                                sad.setRow(row);
                                sad.setRack(rack);
                                sad.setBin(bin);
                                /*
                                *   Saving Job work Order No in Stock Adjustment Details
                                */
                                if (!StringUtil.isNullOrEmpty(jobworkorderno)) { 
                                    KwlReturnObject soresult = accountingHandlerDAO.getObject(SalesOrder.class.getName(), jobworkorderno);
                                    SalesOrder so = (SalesOrder) soresult.getEntityList().get(0);
                                    sad.setJobworkorder(so);
                                }
                                sad.setBatchName(batchName);
                                sad.setSerialNames(serialNames);
                                sad.setFinalSerialNames(finalSerialNames);
                                sad.setQuantity(qty);
                                sad.setFinalQuantity(finalQuantity);
                                adjustmentDetailSet.add(sad);

                            }
                            stockAdjustment.setFinalQuantity(totalFinalQuantity);
                            stockAdjustment.setStockAdjustmentDetail(adjustmentDetailSet);
                            if (!StringUtil.isNullOrEmpty(qApproval)) {
                                sendForQAApproval = Boolean.parseBoolean(qApproval);
                            }


                            if (sendForQAApproval) {
                                if ((StringUtil.isNullOrEmpty(extraCompanyPreferences.getInspectionStore()) && StringUtil.isNullOrEmpty(extraCompanyPreferences.getRepairStore()))) {
                                    throw new InventoryException(InventoryException.Type.NULL, "QA Store and Repair Store are not set in Company Preferences.");
                                } else if ((StringUtil.isNullOrEmpty(extraCompanyPreferences.getInspectionStore()))) {
                                    throw new InventoryException(InventoryException.Type.NULL, "QA Store is not set in Company Preferences.");
                                } else if (StringUtil.isNullOrEmpty(extraCompanyPreferences.getRepairStore())) {
                                    throw new InventoryException(InventoryException.Type.NULL, "Repair Store is not set in Company Preferences.");
                                }
                            }
                            HashMap<String,Object> requestparams = new HashMap<>();
                            requestparams.put("locale", RequestContextUtils.getLocale(request));
                            requestparams.put(Constants.LineLevelCustomData, linelevelcustomdata);
                            requestparams.put("customer",customer);
                            stockAdjustmentService.requestStockAdjustment(user, stockAdjustment, allowNegativeInventory, sendForQAApproval,customfield,requestparams);
                            if (inventoryJE != null) {
                                inventoryJE.setTransactionId(stockAdjustment.getId());
                            }
                            // code for send mail notification when item qty goes below than reorder level.

                            DocumentEmailSettings documentEmailSettings = null;
                            KwlReturnObject documentEmailresult = accountingHandlerDAO.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
                            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
                            if (documentEmailSettings.isQtyBelowReorderLevelMail()) {
                                for (StockAdjustmentDetail sadetail : stockAdjustment.getStockAdjustmentDetail()) {
                                    double availableQtyinStore = stockService.getProductQuantityInStore(stockAdjustment.getProduct(), stockAdjustment.getStore());
                                    if (availableQtyinStore < stockAdjustment.getProduct().getReorderLevel() && stockAdjustment.getQuantity() < 0) {
                                        HashMap<String, String> data = new HashMap<String, String>();
                                        data.put("productName", stockAdjustment.getProduct().getProductName());
                                        data.put("storeId", stockAdjustment.getStore().getId());
                                        data.put("availableQty", Double.toString(availableQtyinStore));
                                        accountingHandlerDAO.sendReorderLevelEmails(user.getUserID(), null, TransactionModule.STOCK_ADJUSTMENT.toString(), data);
                                    }
                                }
                            }
                            billid = stockAdjustment.getId();
                            if (!StringUtil.isNullOrEmpty(auditMessage)) {
                                auditMessage += ", ";
                            }
                            
                            String StUom = stockAdjustment.getUom() != null ? stockAdjustment.getUom().getNameEmptyforNA() : "";
                            auditMessage += "(Product :" + product.getProductid() + ", Quantity :" + (adjustmentType.equalsIgnoreCase("Stock IN") ? quantity : -quantity) + " " + StUom + ", AdjustmentType : " + adjustmentType + " " + ((sendForQAApproval && adjustmentType.equalsIgnoreCase("Stock IN")) ? " and send for QA Approval" : "") + ")";
                            txnManager.commit(status);
                        }
                    }
                    status = null;
                    status = txnManager.getTransaction(def);
                    if (!"NA".equals(seqFormatId)) {
                        seqService.updateSeqNumber(seqFormat);
                    }
                }

                issuccess = true;
                msg = messageSource.getMessage("acc.accPref.autoSA", null, RequestContextUtils.getLocale(request))+": " + seqNo + " "+messageSource.getMessage("acc.bankReconcile.successfully", null, RequestContextUtils.getLocale(request));
                jobj.put("StockAdjustmentNo", seqNo);
                jobj.put("billid", billid);

                auditMessage = "User " + user.getFullName() + " has created Stock Adjustment: " + seqNo + " for Store: " + fromStore.getAbbreviation() + ", " + auditMessage;
                auditTrailObj.insertAuditLog(AuditAction.STOCK_ADJUSTMENT_ADDED, auditMessage, request, "0");
                if (status != null) {
                    txnManager.commit(status);
                }


                TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
                try {
                    KwlReturnObject retObj = consignmentService.assignStockToPendingConsignmentRequests(request);
                    txnManager.commit(statusforBlockSOQty);
                } catch (Exception ex) {
                    txnManager.rollback(statusforBlockSOQty);
                    Logger.getLogger(StockAdjustmentController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (SessionExpiredException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
        } catch (NegativeInventoryException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            if (ex.getType() == NegativeInventoryException.Type.BLOCK) {
                jobj.put("currentInventoryLevel", "block");
            } else if (ex.getType() == NegativeInventoryException.Type.WARN) {
                jobj.put("currentInventoryLevel", "warn");
            }
            msg = ex.getMessage();
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            if(StringUtil.isNullOrEmpty(msg)){
                msg="Error Occurred while processing";
            }
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView approveStockAdjustment(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Save");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {

            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            boolean allowNegativeInventory = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("allowNegativeInventory"))) {
                allowNegativeInventory = Boolean.parseBoolean(request.getParameter("allowNegativeInventory"));
            }

            String records = request.getParameter("jsondata");
            JSONArray jArr = new JSONArray(records);

            for (int i = 0; i < jArr.length(); i++) {

                JSONObject jObj = jArr.optJSONObject(i);
                String reqId = jObj.optString("reqId");
                double quantity = jObj.optDouble("quantity", 0);
                double perUnitPrice = jObj.optDouble("purchaseprice", 0);

                StockAdjustment stockAdjustment = stockAdjustmentService.getStockAdjustmentById(reqId);
                Product product = stockAdjustment.getProduct();
                Set<StockAdjustmentDetail> adjustmentDetailSet = new HashSet<StockAdjustmentDetail>();
                JSONArray stockDetails = jObj.optJSONArray("stockDetails");
                for (int x = 0; x < stockDetails.length(); x++) {
                    JSONObject detailObj = stockDetails.optJSONObject(x);
                    String locationId = detailObj.optString("locationId");
                    String rowId = detailObj.optString("rowId");
                    String rackId = detailObj.optString("rackId");
                    String binId = detailObj.optString("binId");
                    String batchName = detailObj.optString("batchName");
                    String serialNames = detailObj.optString("serialNames");
                    double qty = detailObj.optDouble("quantity");

                    Location location = locationService.getLocation(locationId);
                    StoreMaster row = null;
                    if (product.isIsrowforproduct()) {
                        row = storeService.getStoreMaster(rowId);
                    }
                    StoreMaster rack = null;
                    if (product.isIsrackforproduct()) {
                        rack = storeService.getStoreMaster(rackId);
                    }
                    StoreMaster bin = null;
                    if (product.isIsbinforproduct()) {
                        bin = storeService.getStoreMaster(binId);
                    }
                    StockAdjustmentDetail sad = new StockAdjustmentDetail();
                    sad.setStockAdjustment(stockAdjustment);
                    sad.setLocation(location);
                    sad.setRow(row);
                    sad.setRack(rack);
                    sad.setBin(bin);
                    sad.setBatchName(batchName);
                    sad.setSerialNames(serialNames);
                    sad.setQuantity(qty);
                    adjustmentDetailSet.add(sad);
                }
                stockAdjustment.setStockAdjustmentDetail(adjustmentDetailSet);
                stockAdjustment.setQuantity(quantity);
                stockAdjustment.setPricePerUnit(perUnitPrice);
                stockAdjustmentService.approveStockAdjustment(user, stockAdjustment, allowNegativeInventory);
            }

            issuccess = true;
            msg = "Stock Adjustment  has been approved successfully";
            txnManager.commit(status);

            TransactionStatus statusforBlockSOQty = txnManager.getTransaction(def);
            try {
                KwlReturnObject retObj = consignmentService.assignStockToPendingConsignmentRequests(request);
                txnManager.commit(statusforBlockSOQty);
            } catch (Exception ex) {
                txnManager.rollback(statusforBlockSOQty);
                Logger.getLogger(StockAdjustmentController.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (NegativeInventoryException ex) {
            txnManager.rollback(status);
            if (ex.getType() == NegativeInventoryException.Type.BLOCK) {
                jobj.put("currentInventoryLevel", "block");
            } else if (ex.getType() == NegativeInventoryException.Type.WARN) {
                jobj.put("currentInventoryLevel", "warn");
            }
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

//    public ModelAndView rejectStockAdjustment(HttpServletRequest request, HttpServletResponse response) throws JSONException {
//        JSONObject jobj = new JSONObject();
//        String msg = "";
//        boolean issuccess = false;
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("SA_Tx_Save");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
//        try {
//
//            String companyId = sessionHandlerImpl.getCompanyid(request);
//            String userId = sessionHandlerImpl.getUserid(request);
//
//            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
//            Company company = (Company) jeresult.getEntityList().get(0);
//
//            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
//            User user = (User) jeresult.getEntityList().get(0);
//
//            String[] stockAdjustmentIds = {};
//            if (!StringUtil.isNullOrEmpty(request.getParameter("records"))) {
//                stockAdjustmentIds = request.getParameter("records").split(",");
//            }
//
//            for (int i = 0; i < stockAdjustmentIds.length; i++) {
//
//                StockAdjustment stockAdjustment = stockAdjustmentService.getStockAdjustmentById(stockAdjustmentIds[i]);
//                stockAdjustmentService.rejectStockAdjustment(user, stockAdjustment);
//            }
//
//            issuccess = true;
//            msg = "Stock Adjustment  has been rejected successfully";
//            txnManager.commit(status);
//
//        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            lgr.log(Level.SEVERE, msg, ex);
//        } catch (InventoryException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//        } catch (Exception ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            lgr.log(Level.SEVERE, msg, ex);
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                lgr.log(Level.SEVERE, msg, ex);
//            }
//        }
//        return new ModelAndView(successView, "model", jobj.toString());
//    }
    public ModelAndView getStockAdjustmentList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Get");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;        
        try {
            JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
            DateFormat userDateFormat=null;
            if(paramJobj.has(Constants.userdateformat)){
                userDateFormat=new SimpleDateFormat(String.valueOf(paramJobj.get(Constants.userdateformat)));
            }
            String type=(!StringUtil.isNullOrEmpty(request.getParameter("type"))) ? request.getParameter("type") : "";
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("adjustmentReason", (!StringUtil.isNullOrEmpty(request.getParameter("adjustmentReason"))) ? request.getParameter("adjustmentReason") : "");
            requestParams.put("stockAdjustmentID", (!StringUtil.isNullOrEmpty(request.getParameter("stockAdjustmentID"))) ? request.getParameter("stockAdjustmentID") : "");
            requestParams.put("type", type);
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            JSONArray exportJson = new JSONArray();

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);
            
            requestParams.put("companyid", user.getCompany().getCompanyID());
            requestParams.put("grID", "31");
            Map<String,String> reasonMap=accountingHandlerDAO.getMasterItemByCompanyID(requestParams);
            Date fromDate = request.getParameter("frmDate") != null ? df.parse(request.getParameter("frmDate")) : null;
            Date toDate = request.getParameter("toDate") != null ? df.parse(request.getParameter("toDate")) : null;

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if(!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) ){
            paging = new Paging(start, limit);
            }

            String searchString = request.getParameter("ss");

            String storeId = request.getParameter("storeid");
            String adjustmentType = request.getParameter("adjustmentType");
            String exportFileName = request.getParameter("filename"); // for Export
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(exportFileName)) {
                isExport = true;
                paging = null;
            }
            Store store = null;
            Set<Store> storeSet = new HashSet();
            Set<AdjustmentStatus> adjStatus = new HashSet();
            adjStatus.add(AdjustmentStatus.COMPLETED);
            adjStatus.add(AdjustmentStatus.REQUESTED);
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            } else {
                List storeListByManager = storeService.getStoresByStoreManagers(user, true, null, null, null);
                List storeListByExecutive = storeService.getStoresByStoreExecutives(user, true, null, null, null);
                storeSet.addAll(storeListByManager);
                storeSet.addAll(storeListByExecutive);
            }

            boolean summaryFlag = Boolean.parseBoolean(request.getParameter("summaryFlag"));
            boolean isJobWorkInReciever = Boolean.parseBoolean(request.getParameter("isJobWorkInReciever"));
            if (isJobWorkInReciever) {
                requestParams.put("isJobWorkInReciever", true);
            } else {
                requestParams.put("isJobWorkInReciever", false);
            }
            List<StockAdjustment> stockAdjList = null;
            if (summaryFlag && storeSet != null && !storeSet.isEmpty()) {
                stockAdjList = stockAdjustmentService.getStockAdjustmentList(user.getCompany(), storeSet, null, adjStatus, adjustmentType, fromDate, toDate, searchString, paging, requestParams);
            } else if (storeSet != null && !storeSet.isEmpty()) {
                stockAdjList = stockAdjustmentService.getStockAdjustmentSummary(user.getCompany(), storeSet, null, AdjustmentStatus.COMPLETED, fromDate, toDate, searchString, paging);
            }
            JSONArray exportJArray = new JSONArray();
            if (isExport && type.equals("1")) {
                if (stockAdjList != null) {
                   
                    for (StockAdjustment sa : stockAdjList) {
                        if (sa != null) {
                            String saNo=sa.getTransactionNo();
                            String id=sa.getId();
                            String store_id=sa.getId();
                            String storeAbbr=sa.getStore().getFullName();
                            String transactionModule = sa.getTransactionModule() != null ? sa.getTransactionModule().getString() : "";
                            String storeDesc = sa.getStore().getDescription();
                            String productId = sa.getProduct().getID();
                            String productCode = sa.getProduct().getProductid();
                            String productName = sa.getProduct().getName();
                            String productDescription = sa.getProduct().getDescription();
                            Double quantity = sa.getQuantity();
                            String uomId = sa.getUom() != null ? sa.getUom().getID() : null;
                            String memo = sa.getMemo();
                            String ccnumber = sa.getCyclecount() != null ? sa.getCyclecount().getTransactionNo() : "";
                            String uomName = sa.getUom() != null ? sa.getUom().getNameEmptyforNA() : null;
                            String createdBy = sa.getCreator() != null ? sa.getCreator().getFullName() : null;
                            String date = sa.getCreator() != null ? df.format(sa.getBusinessDate()) : null; //created on date
                            String createdon = sa.getCreatedOn() != null ? authHandler.getUTCToUserLocalDateFormatter_NEW(request, sa.getCreatedOn()) : null;
                            String adjustmenttype = sa.getAdjustmentType();
                            String remark = sa.getRemark();
                            String throughFile=sa.getThroughFile();
                            Double cost = sa.getPricePerUnit();
                            String costcenter = sa.getCostCenter() != null ? sa.getCostCenter().getCcid() : null;
                            String markouttype = "stockout"; //static for now
                            String stype = "system";  //static for now
                            boolean isBatchForProduct = sa.getProduct() != null ? sa.getProduct().isIsBatchForProduct() : false;
                            boolean isSerialForProduct = sa.getProduct() != null ? sa.getProduct().isIsSerialForProduct() : false;
                            boolean isRowForProduct = sa.getProduct() != null ? sa.getProduct().isIsrowforproduct() : false;
                            boolean isRackForProduct = sa.getProduct() != null ? sa.getProduct().isIsrackforproduct() : false;
                            boolean isBinForProduct = sa.getProduct() != null ? sa.getProduct().isIsbinforproduct() : false;
                            String packaging = sa.getPackaging() != null ? sa.getPackaging().toString() : "";
                            String stockAdjustmentReason = sa.getStockAdjustmentReason(); //this adjustment reason is header level 
                            stockAdjustmentReason = reasonMap.get(stockAdjustmentReason);        
                            JournalEntry inventoryJE = sa.getInventoryJE();
                            String inventoryjeid = (inventoryJE != null ? inventoryJE.getID() : "");
                            String inventoryentryno = (inventoryJE != null ? inventoryJE.getEntryNumber() : "");
                            String reason = sa.getReason();
                            reason = reasonMap.get(reason);//this adjustment reason is line level   
                            HashMap<String, String> customFieldMap = new HashMap<>();
                            HashMap<String, String> customDateFieldMap = new HashMap<>();
                            HashMap<String, Object> fieldRequestParams = new HashMap();
                            HashMap<String, String> replaceFieldMap = new HashMap<>();
                            Map<String, Object> variableMap = new HashMap<>();
                            KwlReturnObject custumObjresult = null;
                            JSONObject params = new JSONObject();
                            if (summaryFlag) {
                                /*
                                 Global & Line level Custom data
                                 */
                                
                               

                                fieldRequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                                fieldRequestParams.put(Constants.filter_values, Arrays.asList(user.getCompany().getCompanyID(), Constants.Inventory_Stock_Adjustment_ModuleId));
                                custumObjresult = accountingHandlerDAO.getObject(StockAdjustmentCustomData.class.getName(), sa.getId());
                                replaceFieldMap = new HashMap<>();
                                if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                                    StockAdjustmentCustomData stockDetailCustom = (StockAdjustmentCustomData) custumObjresult.getEntityList().get(0);
                                    if (stockDetailCustom != null) {
                                        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldRequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                                        AccountingManager.setCustomColumnValues(stockDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                        
                                        params.put(Constants.isExport, isExport);
                                        params.put(Constants.userdf, userDateFormat);
                                        
                                    }
                                }
                            }

                            for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {
                                if (sad != null) {
                                    JSONObject detailJSONObject = new JSONObject();
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, detailJSONObject, params);
                                    detailJSONObject.put("locationName", sad.getLocation() != null ? sad.getLocation().getName() : "");
                                    detailJSONObject.put("serialNames", sad.getSerialNames() != null ? sad.getSerialNames().replace(",", ", ") : "");
                                    detailJSONObject.put("batchName", sad.getBatchName() != null ? sad.getBatchName() : "");
                                    detailJSONObject.put("quantityL", authHandler.formattedQuantity(sad.getFinalQuantity(), companyId));
                                    if(paramJobj.has("header")){
                                        String header = paramJobj.getString("header");
                                        if (!(header.contains("quantityL"))){
                                            detailJSONObject.put("quantity", authHandler.formattedQuantity(quantity,companyId));
                                        }
                                    }
                                    detailJSONObject.put("amount", sa.getPricePerUnit() * sad.getQuantity());
                                    detailJSONObject.put("id", id);
                                    detailJSONObject.put("store_id", store_id);
                                    detailJSONObject.put("storeAbbr", storeAbbr);
                                    detailJSONObject.put("transactionModule", transactionModule);
                                    detailJSONObject.put("storeDesc", storeDesc);
                                    detailJSONObject.put("productId", productId);
                                    detailJSONObject.put("productCode", productCode);
                                    detailJSONObject.put("productName", productName);
                                    detailJSONObject.put("productDescription", productDescription);
                                    detailJSONObject.put("uomId", uomId);
                                    detailJSONObject.put("memo", memo);
                                    detailJSONObject.put("ccnumber", ccnumber);
                                    detailJSONObject.put("uomName", uomName);
                                    detailJSONObject.put("createdBy", createdBy);
                                    detailJSONObject.put("date", date); //created on date
                                    detailJSONObject.put("createdon", createdon);
                                    detailJSONObject.put("adjustmentType", adjustmenttype);
                                    detailJSONObject.put("remark", remark);
                                    detailJSONObject.put("throughFile", throughFile);
                                    detailJSONObject.put("cost", cost);
                                    detailJSONObject.put("costcenter", costcenter);
                                    detailJSONObject.put("seqNumber", saNo);
                                    detailJSONObject.put("markouttype", markouttype); //static for now
                                    detailJSONObject.put("type", stype);  //static for now
                                    detailJSONObject.put("isBatchForProduct", isBatchForProduct);
                                    detailJSONObject.put("isSerialForProduct", isSerialForProduct);
                                    detailJSONObject.put("isRowForProduct", isRowForProduct);
                                    detailJSONObject.put("isRackForProduct", isRackForProduct);
                                    detailJSONObject.put("isBinForProduct", isBinForProduct);
                                    detailJSONObject.put("packaging", packaging);
                                    detailJSONObject.put("adjustmentreason", stockAdjustmentReason);
                                    detailJSONObject.put("inventoryjeid", inventoryjeid);
                                    detailJSONObject.put("inventoryentryno", inventoryentryno);
                                    detailJSONObject.put("reason", reason); //this adjustment reason is line level 
                                    /*
                                     *   Adding Job work order no in JSOn to show in List
                                     */
                                    String jobWorkOrderId = "";
                                    String jobWorkOrderCode = "";
                                    String customerid = "";
                                    String customername = "";
                                    jobWorkOrderCode = sad.getJobworkorder() != null ? sad.getJobworkorder().getSalesOrderNumber() : "";
                                    jobWorkOrderId = sad.getJobworkorder() != null ? sad.getJobworkorder().getID() : "";
                                    customername = (sad.getJobworkorder() != null && sad.getJobworkorder().getCustomer() != null) ? sad.getJobworkorder().getCustomer().getName() : "";
                                    customerid = (sad.getJobworkorder() != null && sad.getJobworkorder().getCustomer() != null) ? sad.getJobworkorder().getCustomer().getID() : "";
                                    detailJSONObject.put("jobworkorderno", jobWorkOrderCode);
                                    detailJSONObject.put("jobworkorderid", jobWorkOrderId);
                                    detailJSONObject.put("customerid", customerid);
                                    detailJSONObject.put("customername", customername);
                                    exportJArray.put(detailJSONObject);
                                    detailJSONObject = new JSONObject();
                                }
                            }
                        }
                    }
                }
            } else {
                if (stockAdjList != null) {
                    for (StockAdjustment sa : stockAdjList) {
                        JSONObject jObj = new JSONObject();
                        JSONArray stockDetails = new JSONArray();
                        jObj.put("id", sa.getId());
                        jObj.put("store_id", sa.getStore().getId());
                        jObj.put("storeAbbr", sa.getStore().getFullName());
                        jObj.put("transactionModule", sa.getTransactionModule() != null ? sa.getTransactionModule().getString() : "");
                        jObj.put("storeDesc", sa.getStore().getDescription());
                        jObj.put("productId", sa.getProduct().getID());
                        jObj.put("productCode", sa.getProduct().getProductid());
                        jObj.put("productName", sa.getProduct().getName());
                        jObj.put("productDescription", sa.getProduct().getDescription());
//                    jObj.put("quantity", sa.getQuantity());
                        jObj.put("quantity", summaryFlag ? authHandler.formattedQuantity(sa.getFinalQuantity(), companyId) : authHandler.formattedQuantity(sa.getQuantity(), companyId));
                        jObj.put("uomId", sa.getUom() != null ? sa.getUom().getID() : null);
                        jObj.put("memo", sa.getMemo());
                        jObj.put("ccnumber", sa.getCyclecount() != null ? sa.getCyclecount().getTransactionNo() : "");
                        jObj.put("uomName", sa.getUom() != null ? sa.getUom().getNameEmptyforNA() : null);
                        jObj.put("createdBy", sa.getCreator() != null ? sa.getCreator().getFullName() : null);
                        jObj.put("date", sa.getCreator() != null ? df.format(sa.getBusinessDate()) : null);
                        //Created on date is in UTC so applying user timezone diff. on it.
                        jObj.put("createdon", sa.getCreatedOn() != null ? authHandler.getUTCToUserLocalDateFormatter_NEW(request, sa.getCreatedOn()) : null);
                        jObj.put("adjustmentType", sa.getAdjustmentType());
                        jObj.put("remark", sa.getRemark());
                        jObj.put("throughFile", sa.getThroughFile());
                        jObj.put("cost", sa.getPricePerUnit());
                        jObj.put("costcenter", sa.getCostCenter() != null ? sa.getCostCenter().getCcid() : null);
                        jObj.put("seqNumber", sa.getTransactionNo());
                        jObj.put("markouttype", "stockout"); //static for now
                        jObj.put("type", "system");  //static for now
//                jObj.put("amount", sa.getPricePerUnit() * sa.getFinalQuantity());
                        jObj.put("deleted", sa.isIsdeleted());
                        jObj.put("isBatchForProduct", sa.getProduct() != null ? sa.getProduct().isIsBatchForProduct() : "");
                        jObj.put("isSerialForProduct", sa.getProduct() != null ? sa.getProduct().isIsSerialForProduct() : "");
                        jObj.put("isRowForProduct", sa.getProduct() != null ? sa.getProduct().isIsrowforproduct() : "");
                        jObj.put("isRackForProduct", sa.getProduct() != null ? sa.getProduct().isIsrackforproduct() : "");
                        jObj.put("isBinForProduct", sa.getProduct() != null ? sa.getProduct().isIsbinforproduct() : "");
                        jObj.put("packaging", sa.getPackaging() != null ? sa.getPackaging().toString() : "");

                        String stockAdjustmentReason = sa.getStockAdjustmentReason(); //this adjustment reason is header level 

//                    if (!StringUtil.isNullOrEmpty(stockAdjustmentReason)) {
//                         jObj.put("adjustmentreasonid", stockAdjustmentReason);
//                        jeresult = accountingHandlerDAO.getObject(MasterItem.class.getName(), stockAdjustmentReason);
//                        MasterItem adjustmentReason = (MasterItem) jeresult.getEntityList().get(0);
//                        if (adjustmentReason != null) {
//                            stockAdjustmentReason = adjustmentReason.getValue();
//                        }
//                    }
                     if (!StringUtil.isNullOrEmpty(stockAdjustmentReason)) {
                         jObj.put("adjustmentreasonid", stockAdjustmentReason);
                         stockAdjustmentReason=reasonMap.get(stockAdjustmentReason);
                         jObj.put("adjustmentreason", stockAdjustmentReason);
                     }                     
                        JournalEntry inventoryJE = sa.getInventoryJE();
                        jObj.put("inventoryjeid", (inventoryJE != null ? inventoryJE.getID() : ""));
                        jObj.put("inventoryentryno", (inventoryJE != null ? inventoryJE.getEntryNumber() : ""));
                        String reason = sa.getReason();
//                    if (!StringUtil.isNullOrEmpty(reason)) {
//                        jObj.put("reasonid", reason); //this adjustment reason is line level 
//                        jeresult = accountingHandlerDAO.getObject(MasterItem.class.getName(), reason);
//                        MasterItem adjustmentReason = (MasterItem) jeresult.getEntityList().get(0);
//                        if (adjustmentReason != null) {
//                            reason = adjustmentReason.getValue();
//                        }
//                    }
                    reason=reasonMap.get(reason);
                        jObj.put("reason", reason); //this adjustment reason is line level 
                        String jobWorkOrderId = "";
                        String jobWorkOrderCode = "";
                        String customerid = "";
                        String customername = "";
                        exportJson.put(jObj);
                        JSONObject exportJSONObject = new JSONObject();
                        int srNo=1;
                        if (sa != null) {
                        String expLongTime="";
                        boolean isView=paramJobj.optBoolean("isview",false);
                        if(isExport || isView){
                            for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {
                                if (sad != null) {
                                    exportJSONObject.put("srno", srNo);
                                    JSONObject srObject = new JSONObject();
                                    srObject.put("id", sad.getId());
                                    srObject.put("locationName", (sad.getLocation() != null) ? sad.getLocation().getName() : "");
                                    exportJSONObject.put("locationName", (sad.getLocation() != null) ? sad.getLocation().getName() : "");
                                    srObject.put("locationId", (sad.getLocation() != null) ? sad.getLocation().getId() : "");
                                    srObject.put("rowName", (sad.getRow() != null) ? sad.getRow().getName() : "");
                                    srObject.put("rowId", (sad.getRow() != null) ? sad.getRow().getId() : "");
                                    srObject.put("rackName", (sad.getRack() != null) ? sad.getRack().getName() : "");
                                    srObject.put("rackId", (sad.getRack() != null) ? sad.getRack().getId() : "");
                                    srObject.put("binName", (sad.getBin() != null) ? sad.getBin().getName() : "");
                                    srObject.put("binId", (sad.getBin() != null) ? sad.getBin().getId() : "");
                                    srObject.put("quantity", authHandler.formattedQuantity(sad.getFinalQuantity(), companyId));
                                    exportJSONObject.put("quantityL", authHandler.formattedQuantity(sad.getFinalQuantity(), companyId));
                                    srObject.put("serialNames", (sad.getSerialNames() != null) ? sad.getSerialNames().replace(",", ", ") : "");
                                    exportJSONObject.put("serialNames", (sad.getSerialNames() != null) ? sad.getSerialNames().replace(",", ", ") : "");
//                                srObject.put("quantity", sad.getQuantity());
//                            srObject.put("serialNames", (sad.getFinalSerialNames() != null) ? sad.getFinalSerialNames().replace(",", ", ") : "");
//                                srObject.put("serialNames", (sad.getSerialNames() != null) ? sad.getSerialNames().replace(",", ", ") : "");
                                    srObject.put("serialNames", (sad.getFinalSerialNames() != null) ? sad.getFinalSerialNames().replace(",", ", ") : "");
                                    exportJSONObject.put("serialNames", (sad.getFinalSerialNames() != null) ? sad.getFinalSerialNames().replace(",", ", ") : "");
                                    srObject.put("batchName", (sad.getBatchName() != null) ? sad.getBatchName() : "");
                                    exportJSONObject.put("batchName", (sad.getBatchName() != null) ? sad.getBatchName() : "");

                                    NewProductBatch productBatch = stockService.getERPProductBatch(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName());
                                    if (productBatch != null && !sa.getProduct().isIsSerialForProduct()) {
                                        srObject.put("expLongTime", (productBatch.getExpdate() != null) ? productBatch.getExpdate() : "");
                                    } else if (productBatch != null && sa.getProduct().isIsSerialForProduct()) {
                                        String expDate = "";
                                        String expDates = "";
                                        String[] serialArray = sad.getFinalSerialNames().split(",");
                                        for (String serialName : serialArray) {
                                            NewBatchSerial batchSerial = stockService.getERPBatchSerial(sa.getProduct(), productBatch, serialName);
                                            if (batchSerial != null) {
                                                expDate = (batchSerial.getExptodate() != null ? batchSerial.getExptodate().toString() : "");
                                                expDates += expDate + ",";
                                            } else {
                                                NewBatchSerial serialForSku = stockService.getSerialDataBySerialName(sa.getProduct(), serialName);
                                                if (serialForSku != null) {
                                                    expDate = (serialForSku.getExptodate() != null ? serialForSku.getExptodate().toString() : "");
                                                    expDates += expDate + ",";
                                    }
                                            }
                                        }
                                        srObject.put("expLongTime", (!StringUtil.isNullOrEmpty(expDates) && expDates.endsWith(",")) ? (expDates.substring(0, expDates.length() - 1)) : "");
                                    }

                                jobWorkOrderCode = sad.getJobworkorder() != null ? sad.getJobworkorder().getSalesOrderNumber():"";
                                jobWorkOrderId = sad.getJobworkorder() != null ? sad.getJobworkorder().getID():"";
                                    customername = (sad.getJobworkorder() != null && sad.getJobworkorder().getCustomer() != null) ? sad.getJobworkorder().getCustomer().getName() : "";
                                    customerid = (sad.getJobworkorder() != null && sad.getJobworkorder().getCustomer() != null) ? sad.getJobworkorder().getCustomer().getID() : "";
                                    stockDetails.put(srObject);
                                }
                                exportJson.put(exportJSONObject);
                                exportJSONObject = new JSONObject();
                                srNo++;
                            }
                        }
                        }
                        /*
                         *   Adding Job work order no in JSOn to show in List
                         */
                        jObj.put("jobworkorderno", jobWorkOrderCode);
                        jObj.put("jobworkorderid", jobWorkOrderId);
                        jObj.put("customerid", customerid);
                        jObj.put("customername", customername);
                        jObj.put("stockDetails", stockDetails);
                        if (summaryFlag && "2".equals(type)) {
                            jObj.put("amount", stockAdjustmentService.getTotalAmountOFSABySequenceNo(user.getCompany(), sa.getTransactionNo()));
                        } else {
                            jObj.put("amount", sa.getPricePerUnit() * sa.getQuantity());
                        }

                        if (summaryFlag) {
                            /*
                             Global & Line level Custom data
                             */
                            HashMap<String, String> customFieldMap = new HashMap<>();
                            HashMap<String, String> customDateFieldMap = new HashMap<>();
                            HashMap<String, Object> fieldRequestParams = new HashMap();
                            HashMap<String, String> replaceFieldMap = new HashMap<>();
                            Map<String, Object> variableMap = new HashMap<>();
                            KwlReturnObject custumObjresult = null;

                            fieldRequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                            fieldRequestParams.put(Constants.filter_values, Arrays.asList(user.getCompany().getCompanyID(), Constants.Inventory_Stock_Adjustment_ModuleId));
                            custumObjresult = accountingHandlerDAO.getObject(StockAdjustmentCustomData.class.getName(), sa.getId());
                            replaceFieldMap = new HashMap<>();
                            if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                                StockAdjustmentCustomData stockDetailCustom = (StockAdjustmentCustomData) custumObjresult.getEntityList().get(0);
                                if (stockDetailCustom != null) {
                                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldRequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                                    AccountingManager.setCustomColumnValues(stockDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                    JSONObject params = new JSONObject();
                                    params.put(Constants.isExport, isExport);
                                    params.put(Constants.userdf, userDateFormat);
                                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                                }
                            }
                        }
                       jArray.put(jObj);
                    }
                }
            }
            if (isExport) {
                if (type.equals("1")) {
                    jobj.put("data", exportJArray);
                } else {
                    jobj.put("data", jArray);
                }
                exportDAO.processRequest(request, response, jobj);
            }

            issuccess = true;
            msg = "Stock Adjustment report has been fetched successfully";
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
    public ModelAndView getStockAdjustmentRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Get");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("transactionno", (!StringUtil.isNullOrEmpty(request.getParameter("transactionno"))) ? request.getParameter("transactionno") : "");
            if (!StringUtil.isNullOrEmpty(request.getParameter("type")) && request.getParameter("type").equals("1")) {
                requestParams.put("transactionID", (!StringUtil.isNullOrEmpty(request.getParameter("transactionID"))) ? request.getParameter("transactionID") : "");
            }
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            requestParams.put("company", company);


            List<StockAdjustment> stockAdjList = null;

            stockAdjList = stockAdjustmentService.getStockAdjustmentRows(requestParams);

            if (stockAdjList != null) {
                for (StockAdjustment sa : stockAdjList) {
                    JSONObject jObj = new JSONObject();
                    JSONArray stockDetails = new JSONArray();
                    jObj.put("id", sa.getId());
                    jObj.put("productid", sa.getProduct().getID());
                    jObj.put("productCode", sa.getProduct().getProductid());
                    jObj.put("productName", sa.getProduct().getName());
                    jObj.put("productDescription", sa.getProduct().getDescription());
//                    jObj.put("quantity", sa.getQuantity());
                    jObj.put("quantity", sa.getQuantity());
                    jObj.put("uomId", sa.getUom() != null ? sa.getUom().getID() : null);

                    jObj.put("uomName", sa.getUom() != null ? sa.getUom().getNameEmptyforNA() : null);

                    //Created on date is in UTC so applying user timezone diff. on it.

                    jObj.put("adjustmentType", sa.getAdjustmentType());
                    jObj.put("remark", sa.getRemark());
                    jObj.put("cost", sa.getPricePerUnit());
                    jObj.put("costcenter", sa.getCostCenter() != null ? sa.getCostCenter().getCcid() : null);
                    jObj.put("seqNumber", sa.getTransactionNo());
                    jObj.put("markouttype", "stockout"); //static for now
                    jObj.put("type", "system");  //static for now
//                jObj.put("amount", sa.getPricePerUnit() * sa.getFinalQuantity());
                    jObj.put("isBatchForProduct", sa.getProduct() != null ? sa.getProduct().isIsBatchForProduct() : "");
                    jObj.put("isSerialForProduct", sa.getProduct() != null ? sa.getProduct().isIsSerialForProduct() : "");
                    jObj.put("isRowForProduct", sa.getProduct() != null ? sa.getProduct().isIsrowforproduct() : "");
                    jObj.put("isRackForProduct", sa.getProduct() != null ? sa.getProduct().isIsrackforproduct() : "");
                    jObj.put("isBinForProduct", sa.getProduct() != null ? sa.getProduct().isIsbinforproduct() : "");
                    jObj.put("packaging", sa.getPackaging() != null ? sa.getPackaging().toString() : "");
                   for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {        //stock details 
                            if (sad != null) {
                                JSONObject srObject = new JSONObject();
                                srObject.put("id", sad.getId());
                                srObject.put("locationName", (sad.getLocation() != null) ? sad.getLocation().getName() : "");
                                srObject.put("locationId", (sad.getLocation() != null) ? sad.getLocation().getId() : "");
                                srObject.put("rowName", (sad.getRow() != null) ? sad.getRow().getName() : "");
                                srObject.put("rowId", (sad.getRow() != null) ? sad.getRow().getId() : "");
                                srObject.put("rackName", (sad.getRack() != null) ? sad.getRack().getName() : "");
                                srObject.put("rackId", (sad.getRack() != null) ? sad.getRack().getId() : "");
                                srObject.put("binName", (sad.getBin() != null) ? sad.getBin().getName() : "");
                                srObject.put("binId", (sad.getBin() != null) ? sad.getBin().getId() : "");
                                srObject.put("quantity", authHandler.formattedQuantity(sad.getQuantity(),companyId));
                                

                                srObject.put("serialNames", (sad.getFinalSerialNames() != null) ? sad.getFinalSerialNames().replace(",", ", ") : "");
                                srObject.put("batchName", (sad.getBatchName() != null) ? sad.getBatchName() : "");
                                
                                NewProductBatch productBatch = stockService.getERPProductBatch(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName());
                                if (productBatch != null && !sa.getProduct().isIsSerialForProduct()) {
                                    srObject.put("expLongTime", (productBatch.getExpdate() != null) ? productBatch.getExpdate() : "");
                                } else if (productBatch != null && sa.getProduct().isIsSerialForProduct()) {
                                    String expDate = "";
                                    String expDates = "";
                                    String[] serialArray = sad.getFinalSerialNames().split(",");
                                    for (String serialName : serialArray) {
                                        NewBatchSerial batchSerial = stockService.getERPBatchSerial(sa.getProduct(), productBatch, serialName);
                                        if (batchSerial != null) {

                                            expDate = (batchSerial.getExptodate() != null ? batchSerial.getExptodate().toString() : "");
                                            expDates += expDate + ",";
                                        } else {
                                            NewBatchSerial serialForSku = stockService.getSerialDataBySerialName(sa.getProduct(), serialName);
                                            if (serialForSku != null) {

                                                expDate = (serialForSku.getExptodate() != null ? serialForSku.getExptodate().toString() : "");
                                                expDates += expDate + ",";

                                            }
                                        }
                                    }
                                     srObject.put("expLongTime", (!StringUtil.isNullOrEmpty(expDates) && expDates.endsWith(",")) ? (expDates.substring(0, expDates.length() - 1)) : "");
                                }
                              
                                stockDetails.put(srObject);
                            }

                        }
                   jObj.put("stockDetails", stockDetails);
                    String reason = sa.getReason();
                    if (!StringUtil.isNullOrEmpty(reason)) {
                        jObj.put("reasonid", reason); //this adjustment reason is line level 
                        jeresult = accountingHandlerDAO.getObject(MasterItem.class.getName(), reason);
                        MasterItem adjustmentReason = (MasterItem) jeresult.getEntityList().get(0);
                        if (adjustmentReason != null) {
                            reason = adjustmentReason.getValue();
                        }
                    }
                    jObj.put("reason", reason); //this adjustment reason is line level 

                    jObj.put("amount", sa.getPricePerUnit() * (sa.getQuantity()));

                    /*
                    Line level custom data
                    */
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<>();
                    HashMap<String, String> customDateFieldMap = new HashMap<>();
                    Map<String, Object> variableMap = new HashMap<>();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Inventory_Stock_Adjustment_ModuleId, 1));
                    HashMap<String, String> replaceFieldMap = new HashMap<>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    StockAdjustmentCustomData stockAdjustmentCustomData = (StockAdjustmentCustomData) sa.getStockAdjustmentLineLevelCustomData();
                    AccountingManager.setCustomColumnValues(stockAdjustmentCustomData, FieldMap, replaceFieldMap, variableMap);
                    if (stockAdjustmentCustomData != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isForReport, true);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                    }
                    jArray.put(jObj);
                }
            }



            issuccess = true;
            msg = "Stock Adjustment report has been fetched successfully";
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getPendingStockAdjustmentList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Get");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            Date fromDate = df.parse(request.getParameter("frmDate"));
            Date toDate = df.parse(request.getParameter("toDate"));

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");

            String storeId = request.getParameter("storeid");

            Store store = null;
            Set<Store> storeSet = new HashSet<Store>();
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            }
            Set<AdjustmentStatus> adjStatus = new HashSet();
            adjStatus.add(AdjustmentStatus.REQUESTED);
            List<StockAdjustment> stockAdjList = stockAdjustmentService.getStockAdjustmentList(company, storeSet, null, adjStatus, null, fromDate, toDate, searchString, paging, requestParams);

            for (StockAdjustment stockAdjustment : stockAdjList) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", stockAdjustment.getId());
                jObj.put("store_id", (stockAdjustment.getStore() != null) ? stockAdjustment.getStore().getId() : "");
                jObj.put("storeAbbr", (stockAdjustment.getStore() != null) ? stockAdjustment.getStore().getAbbreviation() : "");
                jObj.put("storeDesc", (stockAdjustment.getStore() != null) ? stockAdjustment.getStore().getDescription() : "");
                jObj.put("productId", (stockAdjustment.getProduct() != null) ? stockAdjustment.getProduct().getID() : "");
                jObj.put("productCode", (stockAdjustment.getProduct() != null) ? stockAdjustment.getProduct().getProductid() : "");
                jObj.put("productName", (stockAdjustment.getProduct() != null) ? stockAdjustment.getProduct().getName() : "");
                jObj.put("quantity", stockAdjustment.getQuantity());
                jObj.put("uomId", (stockAdjustment.getUom() != null) ? stockAdjustment.getUom().getID() : "");
                jObj.put("uomName", (stockAdjustment.getUom() != null) ? stockAdjustment.getUom().getNameEmptyforNA() : "");
                jObj.put("createdBy", (stockAdjustment.getCreator() != null) ? stockAdjustment.getCreator().getFullName() : "");
                jObj.put("date", stockAdjustment.getCreator() != null ? authHandler.getUserDateFormatterWithoutTimeZone(request).format(stockAdjustment.getBusinessDate()) : null);
                jObj.put("reason", stockAdjustment.getReason());
                jObj.put("remark", stockAdjustment.getRemark());
                jObj.put("cost", stockAdjustment.getPricePerUnit());
                jObj.put("costcenter", (stockAdjustment.getCostCenter() != null) ? stockAdjustment.getCostCenter().getName() : "");
                jObj.put("seqNumber", stockAdjustment.getTransactionNo());
                jObj.put("markouttype", "stockout"); //static for now
                jObj.put("type", "system");  //static for now
                jObj.put("amount", stockAdjustment.getPricePerUnit());
                jObj.put("status", "rejected");
//                jObj.put("locationname", stockAdjustment.getLocation() != null ? stockAdjustment.getLocation().getName() : "");
//                jObj.put("locationid", stockAdjustment.getLocation() != null ? stockAdjustment.getLocation().getId() : "");

                jArray.put(jObj);
            }


            issuccess = true;
            msg = "Pending Stock Adjustment report has been fetched successfully";
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getRejectedStockAdjustmentList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Get");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            Date fromDate = df.parse(request.getParameter("frmDate"));
            Date toDate = df.parse(request.getParameter("toDate"));

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");

            String storeId = request.getParameter("storeid");

            Store store = null;
            Set<Store> storeSet = new HashSet<Store>();
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
                storeSet.add(store);
            }
            Set<AdjustmentStatus> adjStatus = new HashSet();
            adjStatus.add(AdjustmentStatus.REJECTED);
            List<StockAdjustment> stockAdjList = stockAdjustmentService.getStockAdjustmentList(company, storeSet, null, adjStatus, null, fromDate, toDate, searchString, paging, requestParams);

            for (StockAdjustment stockAdjustment : stockAdjList) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", stockAdjustment.getId());
                jObj.put("store_id", stockAdjustment.getStore().getId());
                jObj.put("storeAbbr", stockAdjustment.getStore().getAbbreviation());
                jObj.put("storeDesc", stockAdjustment.getStore().getDescription());
                jObj.put("productId", stockAdjustment.getProduct().getID());
                jObj.put("productCode", stockAdjustment.getProduct().getProductid());
                jObj.put("productName", stockAdjustment.getProduct().getName());
                jObj.put("quantity", stockAdjustment.getQuantity());
                jObj.put("uomId", stockAdjustment.getUom().getID());
                jObj.put("uomName", stockAdjustment.getUom().getNameEmptyforNA());
                jObj.put("createdBy", stockAdjustment.getCreator().getFullName());
                jObj.put("date", stockAdjustment.getCreator() != null ? authHandler.getUserDateFormatterWithoutTimeZone(request).format(stockAdjustment.getBusinessDate()) : null);
                jObj.put("reason", stockAdjustment.getReason());
                jObj.put("remark", stockAdjustment.getRemark());
                jObj.put("cost", stockAdjustment.getPricePerUnit());
                jObj.put("costcenter", stockAdjustment.getCostCenter().getName());
                jObj.put("seqNumber", stockAdjustment.getTransactionNo());
                jObj.put("markouttype", "stockout"); //static for now
                jObj.put("type", "system");  //static for now
                jObj.put("amount", stockAdjustment.getPricePerUnit());
//                jObj.put("locationname", stockAdjustment.getLocation() != null ? stockAdjustment.getLocation().getName() : "");
//                jObj.put("locationid", stockAdjustment.getLocation() != null ? stockAdjustment.getLocation().getId() : "");

                jArray.put(jObj);
            }


            issuccess = true;
            msg = "Rejected Stock Adjustment report has been fetched successfully";
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getStockAdjustmentDetailBySequenceNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Get");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String sequenceNo = request.getParameter("sequenceNo");

            List<StockAdjustment> saList = null;
            if (!StringUtil.isNullOrEmpty(sequenceNo)) {
                saList = stockAdjustmentService.getStockAdjustmentBySequenceNo(company, sequenceNo);
            }


            if (saList != null && !saList.isEmpty()) {

                for (StockAdjustment sa : saList) {

                    JSONObject jObj = new JSONObject();
                    JSONArray stockDetails = new JSONArray();
                    jObj.put("id", sa.getId());
                    jObj.put("store_id", sa.getStore().getId());
                    jObj.put("storeAbbr", sa.getStore().getFullName());
                    jObj.put("storeDesc", sa.getStore().getDescription());
                    jObj.put("productId", sa.getProduct().getID());
                    jObj.put("productCode", sa.getProduct().getProductid());
                    jObj.put("productName", sa.getProduct().getName());
                    jObj.put("productDesc", sa.getProduct().getDescription());
                    jObj.put("quantity", sa.getQuantity());
                    jObj.put("uomId", sa.getUom() != null ? sa.getUom().getID() : null);
                    jObj.put("uomName", sa.getUom() != null ? sa.getUom().getNameEmptyforNA() : null);
                    jObj.put("createdBy", sa.getCreator() != null ? sa.getCreator().getFullName() : null);
                    jObj.put("date", sa.getCreator() != null ? authHandler.getUserDateFormatterWithoutTimeZone(request).format(sa.getBusinessDate()) : null);
                    jObj.put("adjustmentType", sa.getAdjustmentType());
                    jObj.put("remark", sa.getRemark());
                    jObj.put("cost", sa.getPricePerUnit());
                    jObj.put("costcenter", sa.getCostCenter() != null ? sa.getCostCenter().getCcid() : null);
                    jObj.put("costcenterName", sa.getCostCenter() != null ? sa.getCostCenter().getName() : "");
                    jObj.put("seqNumber", sa.getTransactionNo());
                    jObj.put("markouttype", "stockout"); //static for now
                    jObj.put("type", "system");  //static for now
                    jObj.put("amount", sa.getPricePerUnit() * sa.getQuantity());
                    jObj.put("isBatchForProduct", sa.getProduct() != null ? sa.getProduct().isIsBatchForProduct() : "");
                    jObj.put("isSerialForProduct", sa.getProduct() != null ? sa.getProduct().isIsSerialForProduct() : "");
                    jObj.put("isSKUForProduct", sa.getProduct() != null ? sa.getProduct().isIsSKUForProduct() : "");
                    jObj.put("hscode", sa.getProduct() != null ? sa.getProduct().getHSCode() : "");
                    jObj.put("packaging", sa.getPackaging() != null ? sa.getPackaging().toString() : "");
                    jObj.put("memo", sa.getMemo() != null ? sa.getMemo() : "");

                    String stockAdjustmentReason = sa.getStockAdjustmentReason(); //this adjustment reason is header level 

                    if (!StringUtil.isNullOrEmpty(stockAdjustmentReason)) {
                        jeresult = accountingHandlerDAO.getObject(MasterItem.class.getName(), stockAdjustmentReason);
                        MasterItem adjustmentReason = (MasterItem) jeresult.getEntityList().get(0);
                        if (adjustmentReason != null) {
                            stockAdjustmentReason = adjustmentReason.getValue();
                        }
                    }
                    jObj.put("adjustmentReason", stockAdjustmentReason);

                    String reason = sa.getReason();
                    if (!StringUtil.isNullOrEmpty(reason)) {
                        jeresult = accountingHandlerDAO.getObject(MasterItem.class.getName(), reason);
                        MasterItem adjustmentReason = (MasterItem) jeresult.getEntityList().get(0);
                        if (adjustmentReason != null) {
                            reason = adjustmentReason.getValue();
                        }
                    }
                    jObj.put("reason", reason);

                    if (sa != null) {
                        for (StockAdjustmentDetail sad : sa.getStockAdjustmentDetail()) {
                            if (sad != null) {
                                JSONObject srObject = new JSONObject();
                                srObject.put("id", sad.getId());
                                srObject.put("locationName", (sad.getLocation() != null) ? ((Location) sad.getLocation()).getName() : "");
                                srObject.put("rowName", (sad.getRow() != null) ? sad.getRow().getName() : "");
                                srObject.put("rackName", (sad.getRack() != null) ? sad.getRack().getName() : "");
                                srObject.put("binName", (sad.getBin() != null) ? sad.getBin().getName() : "");
                                srObject.put("quantity", sad.getQuantity());
                                srObject.put("serialNames", (sad.getSerialNames() != null) ? sad.getSerialNames().replace(",", ", ") : "");
                                
                                List skuList = new ArrayList();
                                List serialExpDateList = new ArrayList();
                                if (sa.getProduct().isIsSKUForProduct()) {
                                    String serialStr = !StringUtil.isNullOrEmpty(sad.getSerialNames()) ? sad.getSerialNames() : "";
                                    String[] serialArr = serialStr.split(",");
                                    for (String srl : serialArr) {
                                        NewBatchSerial serialObj = stockService.getSerialDataBySerialName(sa.getProduct(), srl);
                                        if (serialObj != null) {
                                            String sku = ((!StringUtil.isNullOrEmpty(serialObj.getSkufield())) ? serialObj.getSkufield() : "");
                                            String expDate = (serialObj.getExptodate() != null ? serialObj.getExptodate().toString() : "");
                                            if (!StringUtil.isNullOrEmpty(sku)) {
                                                skuList.add(sku);
                                            }
                                            if (!StringUtil.isNullOrEmpty(expDate)) {
                                                serialExpDateList.add(expDate);
                                            }
                                        }
                                    }
                                }
                                srObject.put("skuNames", (skuList != null && skuList.size() > 0) ? StringUtil.join(",", skuList).replace(",", ", ") : "");
                                srObject.put("expiryDates", (serialExpDateList != null && serialExpDateList.size() > 0) ? StringUtil.join(",", serialExpDateList).replace(",", ", ") : "");
                                srObject.put("batchName", (sad.getBatchName() != null) ? sad.getBatchName() : "");
                                NewProductBatch npb = stockService.getERPProductBatch(sa.getProduct(), sa.getStore(), sad.getLocation(), sad.getRow(), sad.getRack(), sad.getBin(), sad.getBatchName());
                                if (npb != null && !sa.getProduct().isIsSerialForProduct()&&sa.getProduct().isIsBatchForProduct()) {
                                    srObject.put("expiryDates", npb.getExpdate());
                                }
                                stockDetails.put(srObject);
                            }

                        }
                    }
                    jObj.put("stockDetails", stockDetails);
                    
                    /*
                     Custom data
                     */
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<>();
                    HashMap<String, String> customDateFieldMap = new HashMap<>();
                    HashMap<String, String> replaceFieldMap = new HashMap<>();
                    HashMap<String, Integer> fieldMap = new HashMap<>();
                    Map<String, Object> variableMap = new HashMap<>();
                    JSONObject params = new JSONObject();
                    params.put("isExport", true);

                    /*
                     Global level Custom data
                     */
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Inventory_Stock_Adjustment_ModuleId, 0));
                    fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    StockAdjustmentCustomData stockAdjustmentGlobalCustomData = (StockAdjustmentCustomData) sa.getStockAdjustmentCustomData();
                    AccountingManager.setCustomColumnValues(stockAdjustmentGlobalCustomData, fieldMap, replaceFieldMap, variableMap);
                    if (stockAdjustmentGlobalCustomData != null) {
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                    }
                    customFieldMap.clear();
                    customDateFieldMap.clear();
                    replaceFieldMap.clear();
                    variableMap.clear();
                    fieldMap.clear();
                    /*
                     Line level Custom data
                     */
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Inventory_Stock_Adjustment_ModuleId, 1));
                    fieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                    StockAdjustmentCustomData stockAdjustmentLineLevelCustomData = (StockAdjustmentCustomData) sa.getStockAdjustmentLineLevelCustomData();
                    AccountingManager.setCustomColumnValues(stockAdjustmentLineLevelCustomData, fieldMap, replaceFieldMap, variableMap);
                    if (stockAdjustmentLineLevelCustomData != null) {
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                    }
                    
                    jArray.put(jObj);
                }
            }

            issuccess = true;
            msg = "Stock Adjustment has been fetched successfully";
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
        public ModelAndView deletSA(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jsonObj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isPermanent = Boolean.parseBoolean(request.getParameter("isPermanent"));        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SA_Tx_Get");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        String auditMessage = "";
        HashMap<String, Object> requestParams = new HashMap<>();
            StringBuffer productIds = new StringBuffer();
            try {
                String saIds = request.getParameter("said");
                JSONArray jarr = new JSONArray(saIds);
                for (int j = 0; j < jarr.length(); j++) {
                    JSONObject obj = (JSONObject) jarr.get(j);
                    String saId = obj.getString("said");
                    StockAdjustment sa = stockAdjustmentService.getStockAdjustmentById(saId);
                    String companyId = sessionHandlerImpl.getCompanyid(request);
                    String userId = sessionHandlerImpl.getUserid(request);
                    KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
                    Company company = (Company) jeresult.getEntityList().get(0);
                    jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
                    User user = (User) jeresult.getEntityList().get(0);

                    /**
                     * Check Whether SA created through JOb work GRN
                     */
                    if (sa != null) {
                        jsonObj.put("said", sa.getId());
                        jsonObj.put("companyid", sa.getCompany().getCompanyID());
                        int retValue = stockAdjustmentService.stockOutCreatedFromOtherTransaction(jsonObj);
                        if (retValue == 1) {
                            msg = messageSource.getMessage("acc.stockoutcreatedfrombackend", null, RequestContextUtils.getLocale(request));
                            throw new InventoryException(msg);
                        } else if (retValue == 3) {
                            msg = messageSource.getMessage("acc.stockoutcreatedfromDO", null, RequestContextUtils.getLocale(request));
                            throw new InventoryException(msg);
                        } else {
                            jsonObj.put("checkQCTransaction", true);
                            retValue = stockAdjustmentService.stockOutCreatedFromOtherTransaction(jsonObj);
                            if (retValue == 2) {
                                throw new InventoryException(messageSource.getMessage("acc.stockoutcreatedfrombackend.for.qaapproval", null, RequestContextUtils.getLocale(request)));
                            }
                        }
                    }
                    jsonObj = stockAdjustmentService.deleteSA(saId, company, user,isPermanent);
                    if (jsonObj.has("productIds") && !StringUtil.isNullOrEmpty(jsonObj.optString("productIds", null))) {
                        productIds = (StringBuffer) jsonObj.get("productIds");
                    }
                    jArray.put(jsonObj);
                    if (jsonObj.has("success") && "true".equalsIgnoreCase(jsonObj.get("success").toString())) {
                        issuccess=true;
                        auditMessage = "User " + user.getFullName() + " has deleted the Stock Adjustment: " + sa.getTransactionNo() + " (for product: " + sa.getProduct().getProductid() + " , Qty: " + sa.getQuantity() + ")";
                        auditTrailObj.insertAuditLog(AuditAction.STOCK_ADJUSTMENT_DELETED, auditMessage, request, "0");
                    }else{
                        issuccess=false;
                        break;
                    }
                }
                if (issuccess) {
                    txnManager.commit(status);
                }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                if (!StringUtil.isNullOrEmpty(msg)) {
                    jsonObj.put("success", issuccess);
                    jsonObj.put("msg", msg);
                    jsonObj.put("productIds", productIds);
                }
            } catch (JSONException ex1) {
                lgr.log(Level.SEVERE, msg, ex1);
            }
        }
        return new ModelAndView(successView, "model", jsonObj.toString());
    }
}