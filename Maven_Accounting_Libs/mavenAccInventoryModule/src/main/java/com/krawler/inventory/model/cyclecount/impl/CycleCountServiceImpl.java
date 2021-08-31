/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount.impl;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.inventory.model.approval.sa.SAApprovalService;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApprovalService;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.cyclecount.*;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.ist.InterStoreTransferService;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.stockout.StockAdjustmentService;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
import com.krawler.inventory.model.stockrequest.StockRequestService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignLineItemProp;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author Vipin Gupta
 */
public class CycleCountServiceImpl implements CycleCountService {

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private CycleCountDAO cycleCountDAO;
    private AccountingHandlerDAO accountingHandlerDAO;
    private StockService stockService;
    private StockMovementService stockMovementService;
    private SAApprovalService saApprovalService;
    private StockTransferApprovalService stApprovalService;
    private ConsignmentService consignmentService;
    private StoreService storeService;
    private LocationService locationService;
    private StockAdjustmentService saService;
    private StockRequestService srService;
    private InterStoreTransferService istService;
    private VelocityEngine velocityEngine;
    private static final DateFormat yyyyMMdd_HIPHON = new SimpleDateFormat("yyyy-MM-dd");
    private accJournalEntryDAO accJournalEntryobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private HibernateTransactionManager txnManager;
    private static final Logger lgr = Logger.getLogger(CycleCountServiceImpl.class.getName());
    private auditTrailDAO auditTrailObj;
    private SeqService seqService;
    private fieldDataManager fieldDataManagercntrl;
    private accAccountDAO accAccountDAOobj;

    public void setCycleCountDAO(CycleCountDAO cycleCountDAO) {
        this.cycleCountDAO = cycleCountDAO;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setSaService(StockAdjustmentService saService) {
        this.saService = saService;
    }

    public void setConsignmentService(ConsignmentService consignmentService) {
        this.consignmentService = consignmentService;
    }

    public void setSaApprovalService(SAApprovalService saApprovalService) {
        this.saApprovalService = saApprovalService;
    }

    public void setStApprovalService(StockTransferApprovalService stApprovalService) {
        this.stApprovalService = stApprovalService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setIstService(InterStoreTransferService istService) {
        this.istService = istService;
    }

    public void setSrService(StockRequestService srService) {
        this.srService = srService;
    }

    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }
    
    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }
    
    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }
       public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    @Override
    public CycleCountCalendar getCycleCountCalendar(Company company, Date date) throws ServiceException {
        try {
            date = df.parse(df.format(date));
        } catch (ParseException ex) {
        }
        CycleCountCalendar ccl = cycleCountDAO.getCycleCountCalendar(company, date);
        if (ccl == null) {
            ccl = getDefaultCalendar(company, date);
        }
        return ccl;
    }

    @Override
    public List<CycleCountCalendar> getCycleCountCalendarForMonth(Company company, Date date) throws ServiceException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int first = cal.getActualMinimum(Calendar.DATE);
        int last = cal.getActualMaximum(Calendar.DATE);
        cal.set(Calendar.DATE, first);
        String fdate, tdate;
        Date fromDate = cal.getTime();
        DateFormat datef = null;
        try {
            datef = authHandler.getDateOnlyFormat();
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CycleCountServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fdate = datef.format(fromDate);
            fromDate = datef.parse(fdate);
        } catch (ParseException ex) {
            fromDate = cal.getTime();
        }
        cal.set(Calendar.DATE, last);
        Date toDate = cal.getTime();
        try {
            tdate = datef.format(toDate);
            toDate = datef.parse(tdate);
        } catch (ParseException ex) {
            toDate = cal.getTime();
        }
        List list = cycleCountDAO.getCycleCountCalendarForMonth(company, fromDate, toDate);
        return list;
    }

    @Override
    public List<CycleCountCalendar> getCycleCountCalendarForDate(Company company, Date date) throws ServiceException {
        List list = cycleCountDAO.getCycleCountCalendarForMonth(company, date, date);
        return list;
    }

    @Override
    public List<CycleCountCalendar> getDefaultCalendarForMonth(Company company, Date date) throws ServiceException {
        Calendar scal = Calendar.getInstance();
        Calendar ecal = Calendar.getInstance();
        try {
            date = df.parse(df.format(date));
        } catch (ParseException ex) {
        }
        scal.setTime(date);
        ecal.setTime(date);

        scal.set(Calendar.DATE, scal.getActualMinimum(Calendar.DATE));
        ecal.set(Calendar.DATE, ecal.getActualMaximum(Calendar.DATE));

        List<CycleCountCalendar> calendarList = new ArrayList<>();

//        Frequency frequency = (Frequency) cycleCountDAO.getObject(Frequency.class, Frequency.DAILY);
//        Set<Frequency> fSet = new HashSet<>();
//        fSet.add(frequency);

        while (scal.before(ecal) || scal.equals(ecal)) {
            CycleCountCalendar ccCalendar = new CycleCountCalendar();
            ccCalendar.setCompany(company);
            ccCalendar.setDate(scal.getTime());
            calendarList.add(ccCalendar);
            scal.add(Calendar.DATE, 1);
        }
        return calendarList;
    }

    @Override
    public void addOrUpdateCCCalendar(CycleCountCalendar cycleCountCalendar) throws ServiceException {
        if (cycleCountCalendar == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Cycle count calendar cannot be null");
        }
        if (cycleCountCalendar.getCompany() == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Company for cycle count calendar cannot be null");
        }
        if (cycleCountCalendar.getDate() == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Count date for cycle count calendar cannot be null");
        }
        cycleCountDAO.saveOrUpdate(cycleCountCalendar);

    }

    @Override
    public Map<Integer, Frequency> getAllFrequencyMap() throws ServiceException {
        Map<Integer, Frequency> fMap = new HashMap<>();
        List<Frequency> frequencyList = cycleCountDAO.getAllFrequencies();
        for (Frequency frequency : frequencyList) {
            fMap.put(frequency.getId(), frequency);
        }
        return fMap;
    }

    @Override
    public CycleCountCalendar getDefaultCalendar(Company company, Date date) throws ServiceException {
        try {
            date = df.parse(df.format(date));
        } catch (ParseException ex) {
            // Logger.getLogger(CycleCountServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        Frequency frequency = (Frequency) cycleCountDAO.getObject(Frequency.class, Frequency.DAILY);
        Set<Frequency> fSet = new HashSet<>();
        fSet.add(frequency);
        return new CycleCountCalendar(company, date, fSet);

    }

    @Override
    public List<Object[]> getCycleCountProducts(Company company, Date date) throws ServiceException {
        return cycleCountDAO.getCCCalendarProducts(company, date);
    }

    @Override
    public List<Object[]> getCycleCountExtraProducts(Company company, Date date) throws ServiceException {
        return cycleCountDAO.getCCCalendarExtraProducts(company, date);
    }

    @Override
    public List<Object[]> getCycleCountDraftExtraProducts(Company company, Date date) throws ServiceException {
        return cycleCountDAO.getCycleCountDraftExtraProducts(company, date);
    }

    @Override
    public List<CycleCount> getCycleCountReport(Store store, Date businessDate, String searchString, Paging paging) throws ServiceException {
        if (store == null) {
            throw new IllegalArgumentException("Store is required");
        }
        if (businessDate == null) {
            throw new IllegalArgumentException("Bussiness Date is required");
        }
        return cycleCountDAO.getCycleCountReport(store, businessDate, searchString, paging);
    }

    @Override
    public List<CycleCount> getCycleCountReport(Company company, Store store, Date fromDate, Date toDate, String searchString, Paging paging,Map<String, Object> requestParams) throws ServiceException, JSONException, ParseException {
        if (company == null) {
            throw new IllegalArgumentException("company is required");
        }
        if (fromDate == null) {
            throw new IllegalArgumentException("From Date is required");
        }
        if (toDate == null) {
            throw new IllegalArgumentException("To Date is required");
        }
        return cycleCountDAO.getCycleCountReport(company, store, fromDate, toDate, searchString, paging,requestParams);
    }

    @Override
    public void addCycleCountRequest(User user, CycleCount cc) throws ServiceException {
        if (cc == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Cycle Count Request is null");
        }
        if (cc.getCycleCountDetails().isEmpty()) {
            throw new InventoryException(InventoryException.Type.NULL, "Cycle Count Details is null");
        }
        if (cc.getStatus() == CycleCountStatus.DONE && StringUtil.isNullOrEmpty(cc.getTransactionNo())) {
            throw new InventoryException(InventoryException.Type.NULL, "Document no is null");
        }
        cc.setCreatedBy(user);
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date newdate=new Date();
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date newcreatedate=authHandler.getDateWithTimeFormat().parse(sdf.format(newdate));
            cc.setCreatedOn(newcreatedate);
        } catch (ParseException | SessionExpiredException ex) {
            cc.setCreatedOn(new Date());
        }
        cycleCountDAO.saveOrUpdate(cc);

    }

    @Override
    public void addStockAdjustmentForCycleCount(User user, CycleCount cc, List<CycleCountAdjustment> ccaList, String stockAdjustmentNo, Store qaStore, Store repairStore, List<MovedSerialMailDetail> movedSerialmailDetails, ExtraCompanyPreferences ecp, Map<String, Object> jeDataMap, Map<String, String> serialSkuMap) throws ServiceException {
        if (cc == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Cycle Count Request is null");
        }
        if (cc.getCycleCountDetails().isEmpty()) {
            throw new InventoryException(InventoryException.Type.NULL, "Cycle Count Details is null");
        }
        if (StringUtil.isNullOrEmpty(stockAdjustmentNo)) {
            throw new InventoryException(InventoryException.Type.NULL, "Stock Adjustment number is required");
        }
        String companyId = ecp.getCompany().getCompanyID();
        String currencyID = (String) jeDataMap.get(Constants.globalCurrencyKey);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cycleCountDAO.getObject(CompanyAccountPreferences.class, companyId);
        if (cc.getStatus() != CycleCountStatus.DRAFT) {

            Set<Store> qaRepairStoreSet = new HashSet();
            qaRepairStoreSet.add(qaStore);
            qaRepairStoreSet.add(repairStore);

            Product product = cc.getProduct();

            Date date = new Date();
            try {
                date = yyyyMMdd_HIPHON.parse(yyyyMMdd_HIPHON.format(date));
            } catch (ParseException ex) {
                Logger.getLogger(CycleCountServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            double pricePerUnit = stockService.getProductPurchasePrice(product, cc.getBusinessDate());


            StockAdjustment saOut = new StockAdjustment(product, cc.getStore(), product.getUnitOfMeasure(), 0, pricePerUnit, cc.getBusinessDate());
            saOut.setTransactionModule(TransactionModule.CYCLE_COUNT);
            saOut.setMemo("Cycle Count Adjustment");
            saOut.setAdjustmentType("Stock Out");
            saOut.setRemark("Cycle count done");
            saOut.setTransactionNo(stockAdjustmentNo);
            saOut.setCyclecount(cc);
            JournalEntry inventoryJEOut = null;


            StockAdjustment saIn = new StockAdjustment(product, cc.getStore(), product.getUnitOfMeasure(), 0, pricePerUnit, cc.getBusinessDate());
            saIn.setTransactionModule(TransactionModule.CYCLE_COUNT);
            saIn.setPackaging(cc.getPackaging());
            saIn.setMemo("Cycle Count Adjustment");
            saIn.setAdjustmentType("Stock IN");
            saIn.setRemark("Cycle count done");
            saIn.setTransactionNo(stockAdjustmentNo);
            saIn.setCyclecount(cc);
            JournalEntry inventoryJEIn = null;



            Map<Store, Set<Stock>> otherStoreDuplicateSerialStock = new HashMap();
            Map<Stock, String> otherStoreDuplicateSerial = new HashMap();
            for (CycleCountAdjustment cca : ccaList) {
                Location location = cca.getLocation();
                StoreMaster row = cca.getRow();
                StoreMaster rack = cca.getRack();
                StoreMaster bin = cca.getBin();
                if (product.isIsrowforproduct() && row == null) {
                    throw new InventoryException(InventoryException.Type.NULL, "Row is empty for product " + product.getProductid());
                }
                if (product.isIsrackforproduct() && rack == null) {
                    throw new InventoryException(InventoryException.Type.NULL, "Rack is empty for product " + product.getProductid());
                }
                if (product.isIsbinforproduct() && bin == null) {
                    throw new InventoryException(InventoryException.Type.NULL, "Bin is empty for product " + product.getProductid());
                }
                String batchName = cca.getBatchName();
                double addedQtyVariance = cca.getAddedQuantityVariance();
                if (addedQtyVariance > 0) {
                    String serialVariance = cca.getAddedSerialVariance();
                    // code for updating sku field
                    if (product.isIsSerialForProduct() && product.isIsSKUForProduct()) {
                        String skusVariance = "";
                        for (String serialName : serialVariance.split(",")) {
                            String key = product.getID() + batchName + serialName;
                            skusVariance += ((serialSkuMap.containsKey(key) && serialSkuMap.get(key) != null) ? serialSkuMap.get(key) : "") + ",";
                        }
                        if (skusVariance.contains(",")) {
                            skusVariance = skusVariance.substring(0, skusVariance.lastIndexOf(","));
                        }

                        Map<String, Object> tempTablMap = new HashMap<String, Object>();
                        tempTablMap.put("serials", serialVariance);
                        tempTablMap.put("skufields", skusVariance);
                        try {
                            saService.saveSADetailInTemporaryTable(product, cc.getStore(), location, batchName, tempTablMap);
                        } catch (ParseException ex) {
                            Logger.getLogger(CycleCountServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }
                    //

                    StockAdjustmentDetail sad = new StockAdjustmentDetail();
                    sad.setStockAdjustment(saIn);
                    sad.setLocation(location);
                    sad.setRow(row);
                    sad.setRack(rack);
                    sad.setBin(bin);
                    sad.setBatchName(batchName);
                    sad.setQuantity(addedQtyVariance);
                    sad.setFinalQuantity(sad.getQuantity());
                    sad.setSerialNames(serialVariance);
                    sad.setFinalSerialNames(sad.getSerialNames());

                    saIn.setQuantity(saIn.getQuantity() + addedQtyVariance); // positive quantity
                    saIn.setFinalQuantity(saIn.getQuantity());

                    saIn.getStockAdjustmentDetail().add(sad);

                    if (product.getInventoryAccount() != null && ecp != null && (ecp.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {// If MRP is activated then post JE for Stock In
                        try {
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
                                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, date);
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
                            jeDataMap.put("entrydate", cc.getBusinessDate());
                            jeDataMap.put("companyid", companyId);
                            jeDataMap.put("memo", "Stock Adjustment JE for " + product.getName());
                            jeDataMap.put("createdby", user.getUserID());
                            jeDataMap.put("currencyid", currencyID);
                            jeDataMap.put("transactionModuleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
                            inventoryJEIn = (JournalEntry) jeresult.getEntityList().get(0);
                            saIn.setInventoryJE(inventoryJEIn);

                            HashSet jeDetails = new HashSet();
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyId);
                            jedjson.put("amount", authHandler.round((addedQtyVariance * pricePerUnit), companyId));
                            jedjson.put("debit", true);
                            jedjson.put("accountid", product.getInventoryAccount().getID());
                            jedjson.put("jeid", inventoryJEIn.getID());
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);

                            jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyId);
                            jedjson.put("amount", authHandler.round((addedQtyVariance * pricePerUnit), companyId));
                            jedjson.put("accountid", product.getStockAdjustmentAccount().getID());
                            jedjson.put("debit", false);
                            jedjson.put("jeid", inventoryJEIn.getID());
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                            inventoryJEIn.setDetails(jeDetails);
                            accJournalEntryobj.saveJournalEntryDetailsSet(jeDetails);
                        } catch (Exception ex) {
                            Logger.getLogger(CycleCountServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
                        }
                    }
                    if (product.isIsSerialForProduct()) {
                        String serials = sad.getSerialNames();
                        String[] serialArr = serials.split(",");
                        for (String serialName : serialArr) {
                            if (StringUtil.isNullOrEmpty(serialName)) {
                                continue;
                            }
                            List<Stock> stockList = stockService.getStockDetailBySerialInOtherStore(product, cc.getStore(), batchName, serialName);
                            for (Stock stock : stockList) {
                                boolean isForApproval = false;
                                if (qaRepairStoreSet.contains(stock.getStore())) {
                                    List<Object[]> list = stockService.getStockForPendingApprovalSerial(stock.getProduct().getID(), stock.getBatchName(), serialName);
                                    for (Object[] obj : list) {
                                        isForApproval = true;
                                        String approvalDetailId = obj[0] != null ? (String) obj[0] : null;
                                        BigInteger transactionModule = obj[1] != null ? (BigInteger) obj[1] : null;
                                        if (transactionModule.intValue() == 0) {
                                            approvePendingQARepairForSerial(user, approvalDetailId, TransactionModule.STOCK_REQUEST, false, qaStore, repairStore);
                                        } else if (transactionModule.intValue() == 2) {
                                            approvePendingQARepairForSerial(user, approvalDetailId, TransactionModule.INTER_STORE_TRANSFER, false, qaStore, repairStore);
                                        } else if (transactionModule.intValue() == 3) {
                                            approvePendingQARepairForSerial(user, approvalDetailId, TransactionModule.STOCK_ADJUSTMENT, false, qaStore, repairStore);
                                        } else if (transactionModule.intValue() == 7) {
                                            approvePendingQARepairForSerial(user, approvalDetailId, TransactionModule.ERP_Consignment_DO, false, qaStore, repairStore);
                                        }
                                    }
                                    list = stockService.getStockForPendingRepairSerial(stock.getProduct().getID(), stock.getBatchName(), serialName);
                                    for (Object[] obj : list) {
                                        isForApproval = true;
                                        String approvalDetailId = obj[0] != null ? (String) obj[0] : null;
                                        BigInteger transactionModule = obj[1] != null ? (BigInteger) obj[1] : null;
                                        if (transactionModule.intValue() == 0) {
                                            approvePendingQARepairForSerial(user, approvalDetailId, TransactionModule.STOCK_REQUEST, true, qaStore, repairStore);
                                        } else if (transactionModule.intValue() == 2) {
                                            approvePendingQARepairForSerial(user, approvalDetailId, TransactionModule.INTER_STORE_TRANSFER, true, qaStore, repairStore);
                                        } else if (transactionModule.intValue() == 3) {
                                            approvePendingQARepairForSerial(user, approvalDetailId, TransactionModule.STOCK_ADJUSTMENT, true, qaStore, repairStore);
                                        } else if (transactionModule.intValue() == 7) {
                                            approvePendingQARepairForSerial(user, approvalDetailId, TransactionModule.ERP_Consignment_DO, true, qaStore, repairStore);
                                        }
                                    }
                                }
                                if (!isForApproval) {
                                    if (otherStoreDuplicateSerialStock.containsKey(stock.getStore())) {
                                        otherStoreDuplicateSerialStock.get(stock.getStore()).add(stock);
                                    } else {
                                        Set<Stock> serialStock = new HashSet<>();
                                        serialStock.add(stock);
                                        otherStoreDuplicateSerialStock.put(stock.getStore(), serialStock);
                                    }
                                    if (otherStoreDuplicateSerial.containsKey(stock)) {
                                        String addedSerials = otherStoreDuplicateSerial.get(stock);
                                        otherStoreDuplicateSerial.put(stock, addedSerials + "," + serialName);
                                    } else {
                                        otherStoreDuplicateSerial.put(stock, serialName);
                                    }
                                } else {
                                    List<Stock> sameStockList = stockService.getStockDetailBySerial(product, batchName, serialName);
                                    for (Stock internalStock : sameStockList) {
                                        if (otherStoreDuplicateSerialStock.containsKey(internalStock.getStore())) {
                                            otherStoreDuplicateSerialStock.get(internalStock.getStore()).add(internalStock);
                                        } else {
                                            Set<Stock> serialStock = new HashSet<>();
                                            serialStock.add(internalStock);
                                            otherStoreDuplicateSerialStock.put(internalStock.getStore(), serialStock);
                                        }
                                        if (otherStoreDuplicateSerial.containsKey(internalStock)) {
                                            String addedSerials = otherStoreDuplicateSerial.get(internalStock);
                                            otherStoreDuplicateSerial.put(internalStock, addedSerials + "," + serialName);
                                        } else {
                                            otherStoreDuplicateSerial.put(internalStock, serialName);
                                        }
                                    }
                                }

                                MovedSerialMailDetail msmd = new MovedSerialMailDetail(product, stock.getStore(), stock.getLocation(), stock.getRow(), stock.getRack(), stock.getBin(), stock.getBatchName(), serialName);
                                movedSerialmailDetails.add(msmd);
                            }

                        }
                    }
                }
                double removedQtyVariance = cca.getRemovedQuantityVariance();
                if (removedQtyVariance > 0) {
                    StockAdjustmentDetail sad = new StockAdjustmentDetail();
                    sad.setStockAdjustment(saOut);
                    sad.setLocation(location);
                    sad.setRow(row);
                    sad.setRack(rack);
                    sad.setBin(bin);
                    sad.setBatchName(batchName);
                    sad.setQuantity(removedQtyVariance);
                    sad.setFinalQuantity(sad.getQuantity());
                    sad.setSerialNames(cca.getRemovedSerialVariance());
                    sad.setFinalSerialNames(sad.getSerialNames());

                    saOut.setQuantity(saOut.getQuantity() - removedQtyVariance); // negative quantity
                    saOut.setFinalQuantity(saOut.getQuantity());

                    saOut.getStockAdjustmentDetail().add(sad);
                    if (product.getInventoryAccount() != null && ecp != null && (ecp.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) { // If MRP is activated then post JE for Stock Out
                        try {
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
                                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, date);
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
                            jeDataMap.put("entrydate", cc.getBusinessDate());
                            jeDataMap.put("companyid", companyId);
                            jeDataMap.put("memo", "Stock Adjustment JE for " + product.getName());
                            jeDataMap.put("createdby", user.getUserID());
                            jeDataMap.put("currencyid", currencyID);
                            jeDataMap.put("transactionModuleid", Constants.Inventory_Stock_Adjustment_ModuleId);
                            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap); // Create Journal entry without JEdetails
                            inventoryJEOut = (JournalEntry) jeresult.getEntityList().get(0);
                            saOut.setInventoryJE(inventoryJEOut);
                            HashSet jeDetails = new HashSet();
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyId);
                            jedjson.put("amount", authHandler.round(((removedQtyVariance * pricePerUnit) * (-1)), companyId));
                            jedjson.put("debit", false);
                            jedjson.put("accountid", product.getInventoryAccount().getID());
                            jedjson.put("jeid", inventoryJEOut.getID());
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);

                            jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyId);
                            jedjson.put("amount", authHandler.round(((removedQtyVariance * pricePerUnit) * (-1)), companyId));
                            jedjson.put("accountid", product.getStockAdjustmentAccount().getID());
                            jedjson.put("debit", true);
                            jedjson.put("jeid", inventoryJEOut.getID());
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                            inventoryJEOut.setDetails(jeDetails);
                            accJournalEntryobj.saveJournalEntryDetailsSet(jeDetails);
                        } catch (Exception ex) {
                            Logger.getLogger(CycleCountServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
                        }
                    }
                }
            }
            if (saOut.getFinalQuantity() != 0) {
                saService.addStockAdjustmentWithStockMovement(user, saOut, false, "cycle count done", "cycle count done");
                if (inventoryJEOut != null) {
                    inventoryJEOut.setTransactionId(saOut.getId());
                }
            }

            if (!otherStoreDuplicateSerialStock.isEmpty()) {
                for (Entry<Store, Set<Stock>> entry : otherStoreDuplicateSerialStock.entrySet()) {
                    Store store = entry.getKey();
                    Set<Stock> detailList = entry.getValue();
                    StockAdjustment saDuplicateOut = new StockAdjustment(product, store, product.getUnitOfMeasure(), 0, pricePerUnit, cc.getBusinessDate());
                    saDuplicateOut.setTransactionModule(TransactionModule.CYCLE_COUNT);
                    saDuplicateOut.setMemo("Cycle Count Adjustment");
                    saDuplicateOut.setAdjustmentType("Stock Out");
                    saDuplicateOut.setRemark("Removing duplicate serial for cycle count");
                    saDuplicateOut.setTransactionNo(stockAdjustmentNo);
                    for (Stock stock : detailList) {
                        String serials = otherStoreDuplicateSerial.get(stock);
                        int qty = serials.split(",").length;
                        StockAdjustmentDetail sad = new StockAdjustmentDetail();
                        sad.setStockAdjustment(saDuplicateOut);
                        sad.setLocation(stock.getLocation());
                        sad.setRow(stock.getRow());
                        sad.setRack(stock.getRack());
                        sad.setBin(stock.getBin());
                        sad.setBatchName(stock.getBatchName());
                        sad.setQuantity(qty);
                        sad.setFinalQuantity(sad.getQuantity());
                        sad.setSerialNames(otherStoreDuplicateSerial.get(stock));
                        sad.setFinalSerialNames(sad.getSerialNames());

                        saDuplicateOut.setQuantity(saDuplicateOut.getQuantity() - qty); // negative quantity
                        saDuplicateOut.setFinalQuantity(saDuplicateOut.getQuantity());

                        saDuplicateOut.getStockAdjustmentDetail().add(sad);
                    }
                    saService.addStockAdjustmentWithStockMovement(user, saDuplicateOut, false, "cycle count done for duplicate serials in other store", "cycle count done for duplicate serials in other store");
                }

            }
            if (saIn.getFinalQuantity() != 0) {
                saService.addStockAdjustmentWithStockMovement(user, saIn, false, "cycle count done", "cycle count done");
                if (inventoryJEIn != null) {
                    inventoryJEIn.setTransactionId(saIn.getId());
                }
            }
        }
    }

    private void completeIntransitTransaction(CycleCount cycleCount) throws ServiceException {
        Product product = cycleCount.getProduct();
        Store store = cycleCount.getStore();
        User user = cycleCount.getCreatedBy();
        if (product.isIsSerialForProduct()) {
            Map<StockRequestDetail, String> inTransitSRSerials = new HashMap();
            Map<ISTDetail, String> inTransitISTSerials = new HashMap();
            for (CycleCountDetail ccd : cycleCount.getCycleCountDetails()) {

                String batchName = ccd.getBatchName();
                String serials = ccd.getAddedSerialVariance();
                String[] serialArr = serials.split(",");
                for (String serialName : serialArr) {
                    if (StringUtil.isNullOrEmpty(serialName)) {
                        continue;
                    }
                    StockRequestDetail srdissue = srService.getIssuedSrDetailsforSerial(product, batchName, serialName);

                    if (srdissue != null) {
                        String s = "";
                        if (inTransitSRSerials.containsKey(srdissue)) {
                            s = inTransitSRSerials.get(srdissue);
                            s += "," + serialName;
                        } else {
                            s = serialName;
                        }
                        inTransitSRSerials.put(srdissue, s);
                    } else {
                        ISTDetail istintransit = istService.getIntransitIstDetailForSerial(product, batchName, serialName);
                        if (istintransit != null) {
                            String s = "";
                            if (inTransitISTSerials.containsKey(istintransit)) {
                                s = inTransitISTSerials.get(istintransit);
                                s += "," + serialName;
                            } else {
                                s = serialName;
                            }
                            inTransitISTSerials.put(istintransit, s);
                        }
                    }
                }
            }
            acceptSrIntransitRequest(user, store, inTransitSRSerials);
            acceptIstIntransitRequest(user, store, inTransitISTSerials);

            for (CycleCountDetail ccd : cycleCount.getCycleCountDetails()) {
                String batchName = ccd.getBatchName();
                String serials = ccd.getAddedSerialVariance();
                String[] serialArr = serials.split(",");
                for (String serialName : serialArr) {
                    if (StringUtil.isNullOrEmpty(serialName)) {
                        continue;
                    }
                    StockRequest srreturn = srService.getReturnSrforSerial(product, batchName, serialName);

                    if (srreturn != null) {
                        srService.acceptReturnStockRequest(user, srreturn, "cycle count done");
                    } else {
                        InterStoreTransferRequest istreturn = istService.getReturnTransactionforSerial(product, batchName, serialName);
                        if (istreturn != null) {
                            istService.acceptISTReturnRequest(user, istreturn, "cycle count done");
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Double> getProductSystemQty(List<String> products, Store store) throws ServiceException {
        if (products != null && !products.isEmpty()) {
            return cycleCountDAO.getProductsSystemQty(products, store);
        } else {
            return new HashMap();
        }
    }

    @Override
    public void removeCycleCountForProduct(Product product, Store store, Date businessDate, boolean isDraft) throws ServiceException {
        if (product == null) {
            throw new IllegalArgumentException("product can not be null");
        }
        if (store == null) {
            throw new IllegalArgumentException("store can not be null");
        }
        if (businessDate == null) {
            throw new IllegalArgumentException("bussiness date can not be null");
        }
        cycleCountDAO.removeCycleCountForProduct(product, store, businessDate, isDraft);
    }

    @Override
    public void removeCycleCountForProduct(Product product, Store store, Date businessDate) throws ServiceException {
        removeCycleCountForProduct(product, store, businessDate, false);
    }

    @Override
    public void removeAllCycleCountDraft(Store store, Date businessDate) throws ServiceException {
        if (store == null) {
            throw new IllegalArgumentException("store can not be null");
        }
        if (businessDate == null) {
            throw new IllegalArgumentException("bussiness date can not be null");
        }
        cycleCountDAO.removeCycleCountForProduct(null, store, businessDate, true);
    }

    @Override
    public List<Object[]> getAllCycleCountStatusReport(String companyId, Date businessDate, Paging paging) throws ServiceException {
        if (StringUtil.isNullOrEmpty(companyId)) {
            throw new IllegalArgumentException("companyId can not be null");
        }
        if (businessDate == null) {
            throw new IllegalArgumentException("bussiness date can not be null");
        }
        return cycleCountDAO.getAllCycleCountStatusReport(companyId, businessDate, paging);
    }

    @Override
    public List<Object[]> getCycleCountStatusReport(String userId, Date businessDate, Paging paging) throws ServiceException {
        if (StringUtil.isNullOrEmpty(userId)) {
            throw new IllegalArgumentException("userId can not be null");
        }
        if (businessDate == null) {
            throw new IllegalArgumentException("bussiness date can not be null");
        }
        return cycleCountDAO.getCycleCountStatusReport(userId, businessDate, paging);
    }

    @Override
    public boolean isCycleCountDone(String storeId, Date businessDate) throws ServiceException {
        if (StringUtil.isNullOrEmpty(storeId)) {
            throw new IllegalArgumentException("storeid can not be null");
        }
        if (businessDate == null) {
            throw new IllegalArgumentException("bussiness date can not be null");
        }
        return cycleCountDAO.isCycleCountDone(storeId, businessDate);
    }

    @Override
    public boolean isCycleCountDoneForProduct(String storeId, Date businessDate, String productId) throws ServiceException {
        if (StringUtil.isNullOrEmpty(storeId)) {
            throw new IllegalArgumentException("storeid can not be null");
        }
        if (businessDate == null) {
            throw new IllegalArgumentException("bussiness date can not be null");
        }
        if (StringUtil.isNullOrEmpty(productId)) {
            throw new IllegalArgumentException("productId can not be null");
        }
        return cycleCountDAO.isCycleCountDoneForProduct(storeId, businessDate, productId);
    }

    @Override
    public Date getLastCycleCountDate(String storeId) throws ServiceException {
        if (StringUtil.isNullOrEmpty(storeId)) {
            throw new IllegalArgumentException("store is required");
        }
        return cycleCountDAO.getLastCycleCountDate(storeId);
    }

    private void approvePendingQARepairForSerial(User user, String approvalDetailId, TransactionModule transactionModule, boolean fromRepair, Store qaStore, Store repairStore) throws ServiceException {
        String msg = "Cycle Count Done";
        switch (transactionModule) {
            case STOCK_REQUEST:
                StockTransferDetailApproval srda = (StockTransferDetailApproval) cycleCountDAO.getObject(StockTransferDetailApproval.class, approvalDetailId);
                if (fromRepair) {
                    srda.setReason(msg);
                } else {
                    srda.setRemark(msg);
                }
                stApprovalService.approveStockTransferDetail(user, srda, null, fromRepair, 1);
                if (fromRepair) {
                    stApprovalService.createStockMovementForRepairingIntr(user.getCompany(), Arrays.asList(srda), repairStore);
                } else {
                    stApprovalService.createStockMovementForQAApprovalIntr(user.getCompany(), Arrays.asList(srda), qaStore, repairStore);
                }
                break;
            case INTER_STORE_TRANSFER:
                StockTransferDetailApproval istda = (StockTransferDetailApproval) cycleCountDAO.getObject(StockTransferDetailApproval.class, approvalDetailId);
                if (fromRepair) {
                    istda.setReason(msg);
                } else {
                    istda.setRemark(msg);
                }
                stApprovalService.approveStockTransferDetail(user, istda, null, fromRepair, 1);
                if (fromRepair) {
                    stApprovalService.createStockMovementForRepairingIntr(user.getCompany(), Arrays.asList(istda), repairStore);
                } else {
                    stApprovalService.createStockMovementForQAApprovalIntr(user.getCompany(), Arrays.asList(istda), qaStore, repairStore);
                }

                break;
            case STOCK_ADJUSTMENT:
                SADetailApproval sada = (SADetailApproval) cycleCountDAO.getObject(SADetailApproval.class, approvalDetailId);
                if (fromRepair) {
                    sada.setReason(msg);
                } else {
                    sada.setRemark(msg);
                }
                saApprovalService.approveStockAdjustmentDetail(user, sada, null, 1, fromRepair);
                if (fromRepair) {
                    saApprovalService.createStockMovementForRepairing(user.getCompany(), Arrays.asList(sada), repairStore);
                } else {
                    saApprovalService.createStockMovementForQAApproval(user.getCompany(), Arrays.asList(sada), qaStore, repairStore);
                }
                break;
            case ERP_Consignment_DO:
                ConsignmentApprovalDetails cad = (ConsignmentApprovalDetails) cycleCountDAO.getObject(ConsignmentApprovalDetails.class, approvalDetailId);
                if (fromRepair) {
                    cad.setReason(msg);
                } else {
                    cad.setRemark(msg);
                }
                consignmentService.approveConsignmentDetail(user, cad, null, user.getCompany(), 1, fromRepair, null, null); // done stock movement in this method
                break;
        }
    }

    @Override
    public List<CycleCountAdjustment> getCycleCountCurrentAdjustment(CycleCount cycleCount) throws ServiceException {

        completeIntransitTransaction(cycleCount);

        Product product = cycleCount.getProduct();
        Store store = cycleCount.getStore();
        Set<Store> storeSet = new HashSet<>();
        storeSet.add(store);
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTime(cycleCount.getBusinessDate());
        fromCal.add(Calendar.DATE, 1);

        Map<String, Object[]> afterCountingDateInStockMap = stockService.getDateWiseStockDetailListForProduct(product, storeSet, null, fromCal.getTime(), null, false, null, null);
        modifyTotalAddedStockAfterCounting(cycleCount, product, store, afterCountingDateInStockMap);

        Map<String, Object[]> afterCountingDateOutStockMap = stockService.getDateWiseStockDetailListForProduct(product, storeSet, null, fromCal.getTime(), null, true, null, null);
        removeOutStockAfterCounting(product, afterCountingDateInStockMap, afterCountingDateOutStockMap);

        List<CycleCountAdjustment> cycleCountAdjustments = getCycleCountAdjustment(cycleCount, afterCountingDateInStockMap);

        return cycleCountAdjustments;

    }

    private void removeOutStockAfterCounting(Product product, Map<String, Object[]> afterCountingDateInStockMap, Map<String, Object[]> afterCountingDateOutStockMap) {
        for (Entry<String, Object[]> inStockEntry : afterCountingDateInStockMap.entrySet()) {
            String key = inStockEntry.getKey();
            Object[] inStock = inStockEntry.getValue();
            String inSerials = inStock[7] != null ? (String) inStock[7] : null;
            double inQty = inStock[8] != null ? (Double) inStock[8] : 0;
            if (afterCountingDateOutStockMap.containsKey(key)) {
                Object[] outStock = afterCountingDateOutStockMap.get(key);
                double outQty = outStock[8] != null ? (Double) outStock[8] : 0;
                if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(inSerials)) {
                    String[] serials = inSerials.split(",");
                    outQty = 0;
                    String outSerials = (String) outStock[7];
                    String serialNames = "";
                    for (String serial : serials) {
                        if (!outSerials.contains(serial)) {
                            if (!StringUtil.isNullOrEmpty(serialNames)) {
                                serialNames += ",";
                            }
                            serialNames += serial;
                        } else {
                            if (outSerials.contains(serial + ",")) {
                                outSerials = outSerials.replaceFirst(serial + ",", "");
                                outQty++;
                            } else if (outSerials.contains("," + serial)) {
                                outSerials = outSerials.replaceFirst("," + serial, "");
                                outQty++;
                            } else {
                                outSerials = outSerials.replaceFirst(serial, "");
                                outQty++;
                            }
                        }
                    }
                    List<String> serialList = Arrays.asList(serialNames.split(","));
                    Set<String> serialSet = new HashSet(serialList);
                    int duplicateSerialCount = serialList.size() - serialSet.size();
                    if (duplicateSerialCount > 0) {
                        serialNames = "";
                        for (String s : serialSet) {
                            if (!StringUtil.isNullOrEmpty(serialNames)) {
                                serialNames += ",";
                            }
                            serialNames += s;
                        }
                        outQty += duplicateSerialCount;
                    }
                    inStock[7] = serialNames;
                } else {
                    inStock[7] = null;
                }
                inStock[8] = (inQty - outQty);

            }
        }
    }

    private void modifyTotalAddedStockAfterCounting(CycleCount cycleCount, Product product, Store store, Map<String, Object[]> afterCountingDateInStockMap) throws InventoryException {
        for (CycleCountDetail ccd : cycleCount.getCycleCountDetails()) {
            Location location = ccd.getLocation();
            StoreMaster row = ccd.getRow();
            StoreMaster rack = ccd.getRack();
            StoreMaster bin = ccd.getBin();
            if (product.isIsrowforproduct() && row == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Row is empty for product " + product.getProductid());
            }
            if (product.isIsrackforproduct() && rack == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Rack is empty for product " + product.getProductid());
            }
            if (product.isIsbinforproduct() && bin == null) {
                throw new InventoryException(InventoryException.Type.NULL, "Bin is empty for product " + product.getProductid());
            }
            String batchName = ccd.getBatchName();

            String key = product.getID() + store.getAbbreviation() + location.getName()
                    + (row != null ? row.getName() : "")
                    + (rack != null ? rack.getName() : "")
                    + (bin != null ? bin.getName() : "")
                    + batchName;

            Object[] objs = afterCountingDateInStockMap.containsKey(key) ? afterCountingDateInStockMap.get(key) : null;
            String serials = ccd.getActualSerials();
            double qty = ccd.getActualQuantity();
            if (objs != null) {
                String serialnames = objs[7] != null ? (String) objs[7] : null;
                double quantity = objs[8] != null ? (Double) objs[8] : 0;
                objs[8] = qty + quantity;
                if (product.isIsSerialForProduct()) {
                    if (!StringUtil.isNullOrEmpty(serialnames)) {
                        objs[7] = serialnames + "," + serials;
                    } else {
                        objs[7] = serials;
                    }
                }
            } else {
                Object[] newobjs = new Object[16];
                newobjs[0] = product.getID();
                newobjs[1] = store.getAbbreviation();
                newobjs[2] = location.getName();
                newobjs[3] = row != null ? row.getName() : null;
                newobjs[4] = rack != null ? rack.getName() : null;
                newobjs[5] = bin != null ? bin.getName() : null;
                newobjs[6] = batchName;
                newobjs[7] = serials;
                newobjs[8] = qty;
                newobjs[9] = store.getDescription();
//                newobjs[10] = null ? (String) newobjs[10] : null; // uom
                newobjs[11] = store.getId();
                newobjs[12] = location.getId();
                newobjs[13] = row != null ? row.getId() : null;
                newobjs[14] = rack != null ? rack.getId() : null;
                newobjs[15] = bin != null ? bin.getId() : null;

                afterCountingDateInStockMap.put(key, newobjs);
            }
        }
    }

    private List<CycleCountAdjustment> getCycleCountAdjustment(CycleCount cycleCount, Map<String, Object[]> afterCountingDateInStockMap) throws ServiceException {
        Product product = cycleCount.getProduct();
        Store store = cycleCount.getStore();
        Set<Store> storeSet = new HashSet<>();
        storeSet.add(store);
        List<Stock> currentSystemStockList = stockService.getBatchSerialListByStoreProductLocation(product, storeSet, null);
        List<CycleCountAdjustment> ccaList = new ArrayList<>();
        for (Stock systemStock : currentSystemStockList) {
            CycleCountAdjustment cca = new CycleCountAdjustment();
            Location location = systemStock.getLocation();
            StoreMaster row = systemStock.getRow();
            StoreMaster rack = systemStock.getRack();
            StoreMaster bin = systemStock.getBin();
            String batchName = systemStock.getBatchName();

            cca.setCycleCount(cycleCount);
            cca.setLocation(location);
            cca.setRow(row);
            cca.setRack(rack);
            cca.setBin(bin);
            cca.setBatchName(batchName);
            cca.setSystemQuantity(systemStock.getQuantity());
            cca.setSystemSerials(systemStock.getSerialNames());

            String key = product.getID() + store.getAbbreviation() + location.getName()
                    + (row != null ? row.getName() : "")
                    + (rack != null ? rack.getName() : "")
                    + (bin != null ? bin.getName() : "")
                    + batchName;
            if (afterCountingDateInStockMap.containsKey(key) && afterCountingDateInStockMap.get(key) != null) {
                Object objs[] = afterCountingDateInStockMap.get(key);
                cca.setActualQuantity((Double) objs[8]);
                cca.setActualSerials((String) objs[7]);
            }
            ccaList.add(cca);
            afterCountingDateInStockMap.remove(key);
        }
        for (Entry<String, Object[]> entry : afterCountingDateInStockMap.entrySet()) {
            CycleCountAdjustment cca = new CycleCountAdjustment();
            Object[] objs = entry.getValue();
            cca.setCycleCount(cycleCount);
            cca.setLocation(locationService.getLocation((String) objs[12]));
            if (product.isIsrowforproduct()) {
                cca.setRow(storeService.getStoreMaster((String) objs[13]));
            }
            if (product.isIsrackforproduct()) {
                cca.setRack(storeService.getStoreMaster((String) objs[14]));
            }
            if (product.isIsbinforproduct()) {
                cca.setBin(storeService.getStoreMaster((String) objs[15]));
            }
            cca.setBatchName((String) objs[6]);
            cca.setActualQuantity((Double) objs[8]);
            cca.setActualSerials((String) objs[7]);
            ccaList.add(cca);
        }
        return ccaList;
    }

    @Override
    public void sendMovedSerialsMail(User user, List<MovedSerialMailDetail> movedSerialmailDetails, Store cycleCountStore, String cycleCountTransactionNo) {
        if (!movedSerialmailDetails.isEmpty()) {
            Map<Store, List<MovedSerialMailDetail>> movedSerialDetailMap = new HashMap<>();
            for (MovedSerialMailDetail movedSerialMailDetail : movedSerialmailDetails) {
                Store store = movedSerialMailDetail.getStore();
                if (movedSerialDetailMap.containsKey(store)) {
                    List<MovedSerialMailDetail> list = movedSerialDetailMap.get(store);
                    list.add(movedSerialMailDetail);
                } else {
                    List<MovedSerialMailDetail> msList = new ArrayList<>();
                    msList.add(movedSerialMailDetail);
                    movedSerialDetailMap.put(store, msList);
                }
            }
            for (Entry<Store, List<MovedSerialMailDetail>> entry : movedSerialDetailMap.entrySet()) {
                sendMovedSerialsMail(user, entry.getValue(), cycleCountStore, entry.getKey(), cycleCountTransactionNo);
            }

        }
    }

    private void sendMovedSerialsMail(User user, List<MovedSerialMailDetail> movedSerialDetails, Store cycleCountStore, Store movingFromStore, String cycleCountTransactionNo) {
        try {
            Set<String> emailIds = new HashSet();
            for (User recipient : movingFromStore.getStoreManagerSet()) {
                emailIds.add(recipient.getEmailID());
            }
            for (User recipient : movingFromStore.getStoreExecutiveSet()) {
                emailIds.add(recipient.getEmailID());
            }

            if (emailIds.size() > 0 && movedSerialDetails != null && !movedSerialDetails.isEmpty()) {

                String[] recipients = (String[]) emailIds.toArray(new String[emailIds.size()]);

                Company company = user.getCompany();
                String subdomain = company.getSubDomain();
                String url = URLUtil.getDomainURL(subdomain, true);

                String subject = "Moved serial notification by cycle count";
                String htmlText = "<p> Hi,<p>"
                        + "<p>Following item stock is moved from <b>" + movingFromStore.getAbbreviation() + "</b> to <b>" + cycleCountStore.getAbbreviation() + "</b> store during Cycle Count <b>" + cycleCountTransactionNo + "</b>.</p>";
                String plainMsg = "Hi,"
                        + "\nFollowing item stock is moved from " + movingFromStore.getAbbreviation() + " to " + cycleCountStore.getAbbreviation() + " store during Cycle Count " + cycleCountTransactionNo + ".\n";

                CompanyAccountPreferences cap = (CompanyAccountPreferences) cycleCountDAO.getObject(CompanyAccountPreferences.class, company.getCompanyID());

                List<List<String>> finalData = new ArrayList();

                List<String> headerItems = new ArrayList();
                headerItems.add("No.");
                headerItems.add("Product Code");
                headerItems.add("Product Name");
                headerItems.add("Location");
                if (cap.isIsrowcompulsory()) {
                    headerItems.add("Row");
                }
                if (cap.isIsrackcompulsory()) {
                    headerItems.add("Rack");
                }
                if (cap.isIsbincompulsory()) {
                    headerItems.add("Bin");
                }
                if (cap.isIsBatchCompulsory()) {
                    headerItems.add("Batch");
                }
                headerItems.add("Serial");

                int sno = 1;

                for (MovedSerialMailDetail msmd : movedSerialDetails) {
                    List data = new ArrayList();
                    data.add(String.valueOf(sno));
                    data.add(msmd.getProduct().getProductid());
                    data.add(msmd.getProduct().getProductName());
                    data.add(msmd.getLocation().getName());
                    if (msmd.getProduct().isIsrowforproduct()) {
                        data.add(msmd.getRow().getName());
                    } else if (cap.isIsrowcompulsory()) {
                        data.add("");
                    }
                    if (msmd.getProduct().isIsrackforproduct()) {
                        data.add(msmd.getRack().getName());
                    } else if (cap.isIsrackcompulsory()) {
                        data.add("");
                    }
                    if (msmd.getProduct().isIsbinforproduct()) {
                        data.add(msmd.getBin().getName());
                    } else if (cap.isIsbincompulsory()) {
                        data.add("");
                    }
                    if (msmd.getProduct().isIsBatchForProduct()) {
                        data.add(msmd.getBatchName());
                    } else if (cap.isIsBatchCompulsory()) {
                        data.add("");
                    }
                    data.add(msmd.getSerialNames());

                    finalData.add(data);
                    sno++;
                }

                List headerList = new ArrayList();
                for (String header : headerItems) {
                    CustomDesignLineItemProp headerProp = new CustomDesignLineItemProp();
                    headerProp.setAlign("left");
                    headerProp.setData(header);
                    headerProp.setWidth("50px");
                    headerList.add(headerProp);

                    plainMsg += "\t" + header;

                }
                List finalProductList = new ArrayList();
                for (List<String> rowData : finalData) {
                    List<CustomDesignLineItemProp> prodList = new ArrayList();
                    plainMsg += "\n";
                    for (String colData : rowData) {
                        CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                        prop.setAlign("left");
                        prop.setData(colData);
                        prodList.add(prop);

                        plainMsg += "\t" + colData;
                    }
                    finalProductList.add(prodList);

                }
                String top = "10px", left = "10px", tablewidth = CustomDesignHandler.pageWidth;
                StringWriter writer = new StringWriter();
                VelocityContext context = new VelocityContext();
                context.put("tableHeader", headerList);
                context.put("prodList", finalProductList);
                context.put("top", top);
                context.put("left", left);
                context.put("width", tablewidth);
                velocityEngine.mergeTemplate("duemailitems.vm", "UTF-8", context, writer);
                String tablehtml = writer.toString();
                htmlText = htmlText.concat(tablehtml);

                htmlText += "<br><br><p>This is an auto-generated email from " + url + ". Please do not reply.</p>";
                plainMsg += "\n\nThis is an auto-generated email from " + url + ". Please do not reply.";

                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                SendMailHandler.postMail(recipients, subject, htmlText, plainMsg, user.getEmailID(), smtpConfigMap);


            }


        } catch (MessagingException ex) {
            Logger.getLogger(CycleCountServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CycleCountServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List<Object[]> getCycleCountDraftList(String userId, String searchString, Paging paging,ExtraCompanyPreferences ecp) throws ServiceException {
        return cycleCountDAO.getCycleCountDraftList(userId, searchString, paging,ecp.getInspectionStore(),ecp.getRepairStore());
    }

    @Override
    public List<CycleCount> getCycleCountDraftList(String storeId, Date businessDate) throws ServiceException {
        if (StringUtil.isNullOrEmpty(storeId)) {
            throw new IllegalArgumentException("Storeid is required");
        }
        if (businessDate == null) {
            throw new IllegalArgumentException("Business Date is required");
        }
        return cycleCountDAO.getCycleCountDraftItemList(storeId, businessDate);
    }

    private void acceptSrIntransitRequest(User user, Store ccStore, Map<StockRequestDetail, String> inTransitSRSerials) throws ServiceException {
        Set<StockRequest> srAcceptList = new HashSet();
        for (Entry<StockRequestDetail, String> entry : inTransitSRSerials.entrySet()) {
            StockRequestDetail srd = entry.getKey();
            String serials = entry.getValue();
            StockRequest sr = srd.getStockRequest();
            srAcceptList.add(sr);
            if (sr.getFromStore() == ccStore || sr.getToStore() == ccStore) {
                srd.setDeliveredSerialNames(serials);
                if (sr.getToStore() == ccStore) {//counting store is issued store, it means counted serials are not accepted
                    serials = srd.getReturnSerialNames();
                    srd.setDeliveredSerialNames(serials);
                }
                if (!StringUtil.isNullOrEmpty(serials)) {
                    srd.setDeliveredQuantity(serials.split(",").length);
                    srd.setDeliveredLocation(sr.getFromStore().getDefaultLocation());
                    srd.setDeliveredRow(srd.getIssuedRow());
                    srd.setDeliveredRack(srd.getIssuedRack());
                    srd.setDeliveredBin(srd.getIssuedBin());
                    sr.setDeliveredQty(sr.getDeliveredQty() + srd.getDeliveredQuantity());
                }
            }
        }
        for (StockRequest sr : srAcceptList) {
            double acceptedQty = sr.getDeliveredQty();
            if (acceptedQty != 0) {
                Packaging p = sr.getPackaging();
                UnitOfMeasure srUom = sr.getUom();
                double f = p.getStockUomQtyFactor(srUom);
                acceptedQty = acceptedQty / f;
            }
            sr.setDeliveredQty(acceptedQty);
            srService.collectStockOrderRequest(user, sr, "cycle count done");
        }
    }

    private void acceptIstIntransitRequest(User user, Store ccStore, Map<ISTDetail, String> inTransitISTSerials) throws ServiceException {
        Set<InterStoreTransferRequest> istAcceptList = new HashSet();
        for (Entry<ISTDetail, String> entry : inTransitISTSerials.entrySet()) {
            ISTDetail istd = entry.getKey();
            String serials = entry.getValue();
            InterStoreTransferRequest ist = istd.getIstRequest();
            istAcceptList.add(ist);
            if (ist.getFromStore() == ccStore || ist.getToStore() == ccStore) {
                istd.setDeliveredSerialNames(serials);
                if (ist.getFromStore() == ccStore) {//counting store is issued store, it means counting serials are not accepted
                    serials = istd.getReturnSerialNames();
                    istd.setDeliveredSerialNames(serials);
                }
                if (!StringUtil.isNullOrEmpty(serials)) {
                    istd.setDeliveredQuantity(serials.split(",").length);
                    istd.setDeliveredRow(istd.getIssuedRow());
                    istd.setDeliveredRack(istd.getIssuedRack());
                    istd.setDeliveredBin(istd.getIssuedBin());
                    istd.setDeliveredLocation(ist.getToStore().getDefaultLocation());
                    ist.setAcceptedQty(ist.getAcceptedQty() + istd.getDeliveredQuantity());
                }
            }
        }
        for (InterStoreTransferRequest ist : istAcceptList) {
            double acceptedQty = ist.getAcceptedQty();
            if (acceptedQty != 0) {
                Packaging p = ist.getPackaging();
                UnitOfMeasure srUom = ist.getUom();
                double f = p.getStockUomQtyFactor(srUom);
                acceptedQty = acceptedQty / f;
            }
            ist.setAcceptedQty(acceptedQty);
            istService.acceptInterStoreTransferRequest(user, ist, "cycle count done");
        }
    }
   
  @Override  
    public void updateCCProductJArray(List<Object[]> ccProducts, Map<Product, Double> dateWiseStockMap, Map<String, CycleCount> ccDraftMap, JSONArray jArray, Map<Product, Double> currentProductStockMap) throws JSONException {
        for (Object[] p : ccProducts) {
            Product product = p[0] != null ? (Product) p[0] : null;
            String casingUomName = p[1] != null ? (String) p[1] : "-";
            String innerUomName = p[2] != null ? (String) p[2] : "-";
            String looseUomName = p[3] != null ? (String) p[3] : "-";
            double casingUomValue = p[4] != null ? (Double) p[4] : 1;
            double innerUomValue = p[5] != null ? (Double) p[5] : 1;
            double looseUomValue = p[6] != null ? (Double) p[6] : 1;

            String id = product != null ? product.getID() : null;
            String productCode = product != null ? product.getProductid() : null;
            String productName = product != null ? product.getProductName() : null;
            boolean batchForProduct = product != null ? product.isIsBatchForProduct() : false;
            boolean serialForProduct = product != null ? product.isIsSerialForProduct() : false;
            boolean rowForProduct = product != null ? product.isIsrowforproduct() : false;
            boolean rackForProduct = product != null ? product.isIsrackforproduct() : false;
            boolean binForProduct = product != null ? product.isIsbinforproduct() : false;
            boolean skuForProduct = product != null ? product.isIsSKUForProduct() : false;

            JSONObject jObj = new JSONObject();

            jObj.put("id", id);
            jObj.put("code", productCode);
            jObj.put("name", productName);
            jObj.put("isRowForProduct", rowForProduct);
            jObj.put("isRackForProduct", rackForProduct);
            jObj.put("isBinForProduct", binForProduct);
            jObj.put("isBatchForProduct", batchForProduct);
            jObj.put("isSerialForProduct", serialForProduct);
            jObj.put("isSkuForProduct", skuForProduct);
            jObj.put("casinguom", casingUomName);
            jObj.put("inneruom", innerUomName);
            jObj.put("looseuom", looseUomName);
            jObj.put("casinguomval", casingUomValue);
            jObj.put("inneruomval", innerUomValue);
            jObj.put("looseuomval", looseUomValue);
            String packaging = Packaging.packagingPreview(casingUomName, casingUomValue, innerUomName, innerUomValue, looseUomName, looseUomValue);
            jObj.put("packaging", packaging);
            double qty = dateWiseStockMap.containsKey(product) ? dateWiseStockMap.get(product) : 0;
            jObj.put("sysqty", qty);
            jObj.put("casinguomcnt", "");
            jObj.put("inneruomcnt", "");
            jObj.put("looseuomcnt", "");
            jObj.put("extraItem", false);
            jObj.put("currentsysqty", currentProductStockMap.containsKey(product) ? currentProductStockMap.get(product) : 0);
            if (ccDraftMap.containsKey(id)) {
                CycleCount draft = ccDraftMap.get(id);
                JSONObject draftObject = new JSONObject();
                draftObject.put("casinguomcnt", draft.getCasingUomCount());
                draftObject.put("inneruomcnt", draft.getInnerUomCount());
                draftObject.put("looseuomcnt", draft.getStockUomCount());
                double detailQty = 0;
                JSONArray stockDetails = new JSONArray();
                for (CycleCountDetail draftDetail : draft.getCycleCountDetails()) {
                    JSONObject ccdObj = new JSONObject();
                    ccdObj.put("locationId", draftDetail.getLocation().getId());
                    ccdObj.put("locationName", draftDetail.getLocation().getName());
                    StoreMaster row = draftDetail.getRow();
                    if (row != null) {
                        ccdObj.put("rowId", row.getId());
                        ccdObj.put("rowName", row.getName());
                    }
                    StoreMaster rack = draftDetail.getRack();
                    if (rack != null) {
                        ccdObj.put("rackId", rack.getId());
                        ccdObj.put("rackName", rack.getName());
                    }
                    StoreMaster bin = draftDetail.getBin();
                    if (bin != null) {
                        ccdObj.put("binId", bin.getId());
                        ccdObj.put("binName", bin.getName());
                    }
                    ccdObj.put("batchName", draftDetail.getBatchName());
                    ccdObj.put("actualQty", draftDetail.getActualQuantity());
                    ccdObj.put("systemQty", draftDetail.getSystemQuantity());
                    ccdObj.put("actualSerials", draftDetail.getActualSerials());
                    ccdObj.put("systemSerials", draftDetail.getSystemSerials());
                    if (serialForProduct && skuForProduct) {
                        ccdObj.put("actualSerialsSku", draftDetail.getActualSerialsSku());
                        ccdObj.put("systemSerialsSku", draftDetail.getSystemSerialsSku());
                    }

                    stockDetails.put(ccdObj);

                    detailQty += draftDetail.getActualQuantity();
                }
                draftObject.put("stockDetailQuantity", detailQty);
                draftObject.put("stockDetails", stockDetails);
                jObj.put("draftDetail", draftObject);
                jObj.put("extraItem", draft.isExtraItem());
                jObj.put("reason", draft.getRemark());
            }
            jArray.put(jObj);
        }
    }
  
    public boolean addCycleCountRequest(JSONObject paramJobj, User user, String ccTransactionNo) throws Exception {
        boolean issuccess = false;
        String storeId = paramJobj.optString("storeid",null);
        boolean isDraft = "true".equals(paramJobj.optString("isDraft")) ? true : false;
        Date businessDate = yyyyMMdd_HIPHON.parse(paramJobj.optString("countingdate"));
//        Date today = yyyyMMdd_HIPHON.parse(paramJobj.optString("currentDate"));
        String records = paramJobj.optString("jsondata");
        Store store = storeService.getStoreById(storeId);
        JSONArray jArr = new JSONArray(records);
        Map<String, Object> globalParams = AccountingManager.getGlobalParamsJson(paramJobj);
        KwlReturnObject jeresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), user.getCompany().getCompanyID());
        ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) jeresult.getEntityList().get(0);
        Store qaStore = storeService.getStoreById(ecp.getInspectionStore());
        Store repairStore = storeService.getStoreById(ecp.getRepairStore());
        List<MovedSerialMailDetail> movedSerialmailDetails = new ArrayList<>();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jObj = jArr.optJSONObject(i);
            String itemId = jObj.optString("id");
            double casingCount = jObj.optDouble("cc", 0);
            double innerCount = jObj.optDouble("ic", 0);
            double stockCount = jObj.optDouble("lc", 0);
            double systemQty = jObj.optDouble("sq", 0);
            String reason = jObj.optString("rs");
            boolean isExtraItem = jObj.optBoolean("extraItem", false);
            jeresult = accountingHandlerDAO.getObject(Product.class.getName(), itemId);
            Product product = (Product) jeresult.getEntityList().get(0);
            CycleCount cyclecount = new CycleCount(product, store, businessDate, product.getPackaging(), casingCount, innerCount, stockCount);
            cyclecount.setTransactionNo(ccTransactionNo);
            cyclecount.setSystemQty(systemQty);
            cyclecount.setRemark(reason);
            if (isDraft) {
                cyclecount.setStatus(CycleCountStatus.DRAFT);
            } else { // if not a draft then remove last
                removeCycleCountForProduct(product, store, businessDate);
            }
            cyclecount.setExtraItem(isExtraItem);
            JSONArray stockDetails = jObj.optJSONArray("stockDetails");
            Map<String, String> serialSku = new HashMap();
            for (int x = 0; x < stockDetails.length(); x++) {
                JSONObject detailObj = stockDetails.optJSONObject(x);
                String locationId = detailObj.optString("locationId");
                String rowId = detailObj.optString("rowId");
                String rackId = detailObj.optString("rackId");
                String binId = detailObj.optString("binId");
                String batchName = detailObj.optString("batchName","");
                double actualQty = detailObj.optDouble("actualQty", 0);
                double sysQty = detailObj.optDouble("systemQty", 0);
                String actualSerials = detailObj.optString("actualSerials");
                String sysSerials = detailObj.optString("systemSerials");
                String actualSerialSku = detailObj.optString("actualSerialsSku");
                String systemSerialSku = detailObj.optString("systemSerialsSku");
                
                if(product.isIsSerialForProduct() && product.isIsSKUForProduct()){
                    String[] serialArray = actualSerials.split(",");
                    String[] serialSkuArray = actualSerialSku.split(",");
                    for(int c=0; c<serialArray.length; c++){
                        serialSku.put(product.getID()+batchName+serialArray[c], serialSkuArray[c]);
                    }
                }
                
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
                CycleCountDetail ccd = new CycleCountDetail();
                ccd.setCycleCount(cyclecount);
                ccd.setLocation(location);
                ccd.setRow(row);
                ccd.setRack(rack);
                ccd.setBin(bin);
                ccd.setBatchName(batchName);
                ccd.setActualQuantity(actualQty);
                ccd.setSystemQuantity(sysQty);
                ccd.setActualSerials(actualSerials);
                ccd.setSystemSerials(sysSerials);
                ccd.setActualSerialsSku(actualSerialSku);
                ccd.setSystemSerialsSku(systemSerialSku);
                cyclecount.getCycleCountDetails().add(ccd);
            }
             addCycleCountRequest(user, cyclecount);
            String customfield = jObj.optString(Constants.customfield, null);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                HashMap<String, Object> SOMap = new HashMap<String, Object>();
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "CycleCount");
                customrequestParams.put("moduleprimarykey", "Ccid");
                customrequestParams.put("modulerecid", cyclecount.getId());
                customrequestParams.put("moduleid", Constants.Acc_CycleCount_ModuleId);
                customrequestParams.put("companyid", cyclecount.getCompany().getCompanyID());
                customrequestParams.put("customdataclasspath", "com.krawler.inventory.model.cyclecount.CycleCountCustomData");
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    CycleCountCustomData cycleCountCustomData = (CycleCountCustomData) cycleCountDAO.getObject(CycleCountCustomData.class, cyclecount.getId());
                    cyclecount.setCycleCountCustomData(cycleCountCustomData);
                    addCycleCountRequest(user, cyclecount);
                }
            }
            if (!isDraft) { 
                List<CycleCountAdjustment> ccAdjustmentList = getCycleCountCurrentAdjustment(cyclecount);
               addStockAdjustmentForCycleCount(user, cyclecount, ccAdjustmentList, ccTransactionNo, qaStore, repairStore, movedSerialmailDetails, ecp, globalParams, serialSku);
                
            }
            issuccess = true;
        }
        if(issuccess){
            sendMovedSerialsMail(user, movedSerialmailDetails, store, ccTransactionNo);
        }
        return issuccess;
    }
  
  @Override
    public JSONObject addCycleCountRequest(JSONObject paramJobj) throws JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        JSONObject dataObj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SOR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String userId = paramJobj.optString(Constants.useridKey,null);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String storeId = paramJobj.optString("storeid", null);

            String ccTransactionNo = paramJobj.optString("seqno", null);
            boolean sequenceDone = "true".equals(paramJobj.optString("sequenceDone")) ? true : false;
            boolean isDraft = "true".equals(paramJobj.optString("isDraft")) ? true : false;
            String seqFormatId = paramJobj.optString("seqFormatId", null);
            Date businessDate = yyyyMMdd_HIPHON.parse(paramJobj.optString("countingdate"));
            Store store = storeService.getStoreById(storeId);

            if (!sequenceDone) {
                removeAllCycleCountDraft(store, businessDate);
            }
            if (isDraft || sequenceDone) {
                if (isDraft) {
                    ccTransactionNo = null;
                }

                issuccess = addCycleCountRequest(paramJobj, user, ccTransactionNo);
            } else {
                boolean isccDone = isCycleCountDone(storeId, businessDate);
                if (isccDone) {
                    throw new InventoryException("Cycle Count is already done for given or future bussiness date.");
                }
                synchronized (this) {
                    SeqFormat ccSeqFormat = null;

                    boolean seqExist = false;
                    if ("NA".equals(seqFormatId)) {
                        seqExist = seqService.isExistingSeqNumber(ccTransactionNo, user.getCompany(), ModuleConst.CYCLE_COUNT);
                        if (seqExist) {
                            throw new InventoryException("Document number [" + ccTransactionNo + "] is already exist, please enter other one.");
                        }
                    } else {
                        seqExist = false;
                        if (!StringUtil.isNullOrEmpty(seqFormatId)) {
                            ccSeqFormat = seqService.getSeqFormat(seqFormatId);
                        } else {
                            ccSeqFormat = seqService.getDefaultSeqFormat(user.getCompany(), ModuleConst.CYCLE_COUNT);
                        }
                        do {
                            ccTransactionNo = seqService.getNextFormatedSeqNumber(ccSeqFormat);
                            seqExist = seqService.isExistingSeqNumber(ccTransactionNo, user.getCompany(), ModuleConst.CYCLE_COUNT);
                            if (seqExist) {
                                seqService.updateSeqNumber(ccSeqFormat);
                            }
                        } while (seqExist);
                    }

                    issuccess = addCycleCountRequest(paramJobj, user, ccTransactionNo);

                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

                    String auditMessage = "User " + user.getFullName() + " has done cycle count " + ccTransactionNo + " for store: " + store.getFullName() + ", business date: " + yyyyMMdd_HIPHON.format(businessDate);
                    auditTrailObj.insertAuditLog(AuditAction.CC_ADDED, auditMessage, auditRequestParams, "0");

                    if (!isDraft && !sequenceDone && ccSeqFormat != null && !"NA".equals(seqFormatId)) {
                        seqService.updateSeqNumber(ccSeqFormat);
                    }
                }
            }
            dataObj.put("transactionNo", ccTransactionNo);
            if (!issuccess) {
                txnManager.rollback(status);
            } else {
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true && isDraft) {
                    msg = "Cycle Count detail is saved as draft successfully.";
                } else {
                    msg = "Cycle count request  : " + ccTransactionNo + " has been added successfully";
                }
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
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put(Constants.RES_data, dataObj);
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    jobj.put(Constants.RES_TOTALCOUNT, dataObj.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return jobj;
    }
    
  @Override
 public JSONArray getCycleCountReport(JSONObject paramJobj,HashMap<String,Object> requestParams) {

      String msg = "";
      JSONArray jArray = new JSONArray();
      Paging paging = null;
        try {
            String companyId = paramJobj.getString(Constants.companyKey);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);
            String fDate = paramJobj.optString("fromDate", null);
            String tDate = paramJobj.optString("toDate", null);
            String storeId = paramJobj.optString("storeId", null);
            String searchString = paramJobj.optString("ss",null);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_CycleCount_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            
            if (requestParams.containsKey("pagingObject") && requestParams.get("pagingObject") != null) {
                paging = (Paging) requestParams.get("pagingObject");
            }
            
            Store store = null;
            Date fromDate, toDate;
            try {
                fromDate = yyyyMMdd_HIPHON.parse(fDate);
                toDate = yyyyMMdd_HIPHON.parse(tDate);
            } catch (ParseException ex) {
                fromDate = null;
                toDate = null;
            }
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
            }
            List<CycleCount> cycleCountList = getCycleCountReport(company, store, fromDate, toDate, searchString, paging,requestParams);

            for (CycleCount cc : cycleCountList) {
                JSONObject jObj = new JSONObject();
                JSONArray stockDetails = new JSONArray();
                Product product = cc.getProduct();
                jObj.put("id", cc.getId());
                jObj.put("isBatchForProduct", product.isIsBatchForProduct());
                jObj.put("isSerialForProduct", product.isIsSerialForProduct());
                jObj.put("isRowForProduct", product.isIsrowforproduct());
                jObj.put("isRackForProduct", product.isIsrackforproduct());
                jObj.put("isBinForProduct", product.isIsbinforproduct());
                jObj.put("transactionNo", cc.getTransactionNo());
                jObj.put("createdBy", cc.getCreatedBy().getFullName());
                jObj.put("itemCode", product.getProductid());
                jObj.put("itemName", product.getName());
                jObj.put("itemDesc", product.getDescription());
                jObj.put("actualQty", authHandler.formattedQuantity(cc.getActualQty(), companyId));
                jObj.put("systemQty", authHandler.formattedQuantity(cc.getSystemQty(),companyId));
                jObj.put("businessDate", yyyyMMdd_HIPHON.format(cc.getBusinessDate()));
                jObj.put("storeId", cc.getStore().getId());
                jObj.put("storeCode", cc.getStore().getAbbreviation());
                jObj.put("storeDesc", cc.getStore().getDescription());
                Packaging packaging = cc.getPackaging();
                jObj.put("packaging", packaging.toString());
                jObj.put("casingUom", packaging.getCasingUoM() == null ? "-" : packaging.getCasingUoM().getNameEmptyforNA());
                jObj.put("innerUom", packaging.getInnerUoM() == null ? "-" : packaging.getInnerUoM().getNameEmptyforNA());
                jObj.put("looseUom", packaging.getStockUoM() == null ? "-" : packaging.getStockUoM().getNameEmptyforNA());
                jObj.put("casingUomCnt", cc.getCasingUomCount());
                jObj.put("innerUomCnt", cc.getInnerUomCount());
                jObj.put("looseUomCnt", cc.getStockUomCount());
                jObj.put("countedOn", authHandler.getUTCToUserLocalDateFormatter_NEWJson(paramJobj, cc.getCreatedOn()));
                jObj.put("reason", cc.getRemark());
                double qtyVariance = cc.getQtyVariance();
                jObj.put("variance", authHandler.formattedQuantity(cc.getQtyVariance(),companyId));

                if (cc.getSystemQty() != 0) {
                    jObj.put("variancePer", StringUtil.roundDoubleTo((Math.abs(qtyVariance) / cc.getSystemQty()) * 100, 2) + "%");
                } else {
                    jObj.put("variancePer", "100%");
                }

                if (cc != null) {
                    for (CycleCountDetail ccd : cc.getCycleCountDetails()) {
                        if (ccd != null) {
                            JSONObject srObject = new JSONObject();
                            srObject.put("id", ccd.getId());
                            srObject.put("locationName", (ccd.getLocation() != null) ? ccd.getLocation().getName() : "");
                            srObject.put("batchName", (ccd.getBatchName() != null) ? ccd.getBatchName() : "");
                            srObject.put("rowName", (ccd.getRow() != null) ? ccd.getRow().getName() : "");
                            srObject.put("rackName", (ccd.getRack() != null) ? ccd.getRack().getName() : "");
                            srObject.put("binName", (ccd.getBin() != null) ? ccd.getBin().getName() : "");
                            srObject.put("systemQty", authHandler.formattedQuantity(ccd.getSystemQuantity(),companyId));
                            srObject.put("actualQty", authHandler.formattedQuantity(ccd.getActualQuantity(), companyId));
                            srObject.put("variance", authHandler.formattedQuantity(ccd.getQtyVariance(),companyId) );
                            srObject.put("systemSerials", (ccd.getSystemSerials() != null) ? ccd.getSystemSerials().replace(",", ", ") : "");
                            srObject.put("actualSerials", (ccd.getActualSerials() != null) ? ccd.getActualSerials().replace(",", ", ") : "");
                            stockDetails.put(srObject);
                        }
                    }
                }
                jObj.put("stockDetails", stockDetails);
                KwlReturnObject custumObjresult = null;
                custumObjresult = accountingHandlerDAO.getObject(CycleCountCustomData.class.getName(), cc.getId());
                Map<String, Object> variableMap = new HashMap<String, Object>();
                replaceFieldMap = new HashMap<String, String>();
                if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                    CycleCountCustomData jeDetailCustom = (CycleCountCustomData) custumObjresult.getEntityList().get(0);
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyId);
                        params.put("isExport", true);
//                params.put(Constants.userdf, map.get(Constants.userdf));
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jObj, params);
                    }
                }
                jArray.put(jObj);
            }
        } catch (InventoryException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } 
        return jArray;
    }
  
  
}
