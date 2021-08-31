/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.asset;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.LandingCostCategory;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.LandingCostAllocationType;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.AssetDepreciationDetail;
import com.krawler.hql.accounting.AssetDetails;
import com.krawler.hql.accounting.AssetDetailsCustomData;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.FixedAssetOpeningMappingWithAssetDetail;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.LccManualWiseProductAmount;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.context.MessageSource;

/**
 *
 * @author swapnil.khandre
 */
public class AccAssetServiceImpl implements AccAssetService {

    private accProductDAO accProductObj;

    private fieldDataManager fieldDataManagercntrl;

    private AccountingHandlerDAO accountingHandlerDAOobj;

    private AccProductModuleService accProductModuleService;

    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    private accGoodsReceiptDAO accGoodsReceiptDAOobj;

    private AccCommonTablesDAO accCommonTablesDAO;

    private accCurrencyDAO currencyDAO;

    private accAccountDAO accAccountDAOobj;

    private exportMPXDAOImpl exportDaoObj;

    private MessageSource messageSource;

    private accMasterItemsDAO accMasterItemsDAOobj;

    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setFieldDataManagercntrl(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setAccGoodsReceiptDAOobj(accGoodsReceiptDAO accGoodsReceiptDAOobj) {
        this.accGoodsReceiptDAOobj = accGoodsReceiptDAOobj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setCurrencyDAO(accCurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    @Override
    public JSONObject getAssetDetails(HttpServletRequest request, boolean isexport) throws SessionExpiredException, ParseException, AccountingException {
        JSONArray jArr = new JSONArray();
        JSONObject finalJSONObject = new JSONObject();
        String companyId = sessionHandlerImpl.getCompanyid(request);
        boolean isDepreciationReport = request.getParameter("isDepreciationReport") != null ? Boolean.parseBoolean(request.getParameter("isDepreciationReport")) : false;
        boolean isDisposedAssetReport = request.getParameter("isDisposedAssetReport") != null ? Boolean.parseBoolean(request.getParameter("isDisposedAssetReport")) : false;
        boolean isFixedAssetDetailReport = request.getParameter("isFixedAssetDetailReport") != null ? Boolean.parseBoolean(request.getParameter("isFixedAssetDetailReport")) : false;
        int depreciationCalculationType = request.getParameter("depreciationCalculationType") != null ? Integer.parseInt(request.getParameter("depreciationCalculationType")) : 0;
        boolean isCreateSchedule = request.getParameter("isCreateSchedule") != null ? Boolean.parseBoolean(request.getParameter("isCreateSchedule")) : false;
        int type = request.getParameter("type") != null ? Integer.parseInt(request.getParameter("type")) : 0;//0 = All
        boolean excludeSoldAssets = false;

        if (!StringUtil.isNullOrEmpty(request.getParameter("excludeSoldAssets"))) {
            excludeSoldAssets = Boolean.parseBoolean(request.getParameter("excludeSoldAssets"));
        }

        DateFormat df = authHandler.getDateOnlyFormat(request);
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        int count = 0;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(request);
        requestParams.put("companyId", companyId);
        requestParams.put("invrecord", true);

        if (excludeSoldAssets) {
            requestParams.put("excludeSoldAsset", excludeSoldAssets);
        }

        try {
            JSONObject groupTotal = new JSONObject();
            JSONObject grandTotal = new JSONObject();
            JSONObject subtotal = null;
            String countryId = sessionHandlerImpl.getCountryId(request);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject excap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) excap.getEntityList().get(0);
            String currencyCode = "";
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency curr = (KWLCurrency) curresult.getEntityList().get(0);
            if (curr != null) {
                currencyCode = curr.getCurrencyCode();//Base currency code
            }
            if (!isexport) {
                if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start))) {
                    requestParams.put(Constants.start, request.getParameter(Constants.start));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
                    requestParams.put(Constants.limit, request.getParameter(Constants.limit));
                }
            }
            if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                requestParams.put("ss", request.getParameter("ss"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("assetGroupIds"))) {
                requestParams.put("assetGroupIds", request.getParameterValues("assetGroupIds"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isMachineMapped"))) {
                requestParams.put("isMachineMapped", request.getParameterValues("isMachineMapped"));
            }
            if (isDisposedAssetReport) {
                requestParams.put("isDisposedAssetReport", isDisposedAssetReport);
            }
            if (isFixedAssetDetailReport) {
                requestParams.put("isFixedAssetDetailReport", isFixedAssetDetailReport);
            }
            if (isCreateSchedule) {
                requestParams.put("isCreateSchedule", isCreateSchedule);
            }
            requestParams.put("type", type);
            Date startdt = null;
            Date enddt = null;
            if (request.getParameter("stdate") != null) {
                startdt = df.parse(request.getParameter("stdate"));
            }
            if (request.getParameter("enddate") != null) {
                enddt = df.parse(request.getParameter("enddate"));
            }
            if ((isFixedAssetDetailReport || isDisposedAssetReport) && request.getParameter("stdate") != null && request.getParameter("enddate") != null) {
                requestParams.put(Constants.REQ_startdate, startdt);
                requestParams.put(Constants.REQ_enddate, enddt);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
                requestParams.put("searchJson", request.getParameter("searchJson"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("filterConjuctionCriteria"))) {
                requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
            }
            String year = "1970";
            int endMonth = 11;
            int currentyear = 0;
            if (startdt != null) {
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startdt);
                currentyear = startCal.get(Calendar.YEAR);
                year = String.valueOf(startCal.get(Calendar.YEAR));
            }
            KwlReturnObject result = accProductObj.getAssetDetails(requestParams);
            List<AssetDetails> list = result.getEntityList();
            count = result.getRecordTotalCount();

            String documentIds = "";
            for (AssetDetails ad : list) {
                documentIds += "'" + ad.getId() + "',";
            }
            Map<String, List<Object[]>> baMap = new HashMap<>();
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
            boolean isEdit = (StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            String moduleID = request.getParameter("moduleid");
            Map<String, Object> batchSerialReqMap = new HashMap<>();
            batchSerialReqMap.put(Constants.companyKey, companyId);
            batchSerialReqMap.put(Constants.df, df);
            batchSerialReqMap.put("linkingFlag", linkingFlag);
            batchSerialReqMap.put("isEdit", isEdit);
            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()) {  //check if company level option is on then only we will check productt level
                batchSerialReqMap.put("documentIds", StringUtil.isNullOrEmpty(documentIds) ? "" : documentIds.substring(0, documentIds.length() - 1));
                baMap = getBatchDetailsMap(batchSerialReqMap);
            }
            /*
             Below logic is used get Disposal Invoice No and Disposal Invoice date of all assets and stored it in Map
             */
            Map<String, Object> resultMap = new HashMap<>();
            if ((isFixedAssetDetailReport || isDisposedAssetReport) && !StringUtil.isNullOrEmpty(documentIds)) {
                Map<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("documentIds", documentIds.substring(0, documentIds.length() - 1));
                paramsMap.put("companyId", companyId);
                resultMap = accProductModuleService.getDisposalInvoiceDetailsFromAssetDetailID(paramsMap);
            }

            for (AssetDetails ad : list) {
                if (isDisposedAssetReport) {
                    if (groupTotal.has(ad.getProduct().getID())) {
                        subtotal = groupTotal.getJSONObject(ad.getProduct().getID());
                    } else {
                        if (subtotal != null) {
                            jArr.put(subtotal);
                        }
                        subtotal = new JSONObject();
                    }
                    subtotal.put("assetGroup", ad.getProduct().getName());
                    subtotal.put("groupinfo", ad.getProduct().getName()+"("+ad.getProduct().getProductid()+")");
                    subtotal.put("assetId", isexport?"Total":"<b>Total</b>");
                    subtotal.put("issummaryvalue", true);
                }

                JSONObject jobj = new JSONObject();
                Product product = null;
                jobj.put("assetdetailId", ad.getId());
                jobj.put("assetGroup", ad.getProduct().getName());
                jobj.put("assetDepreciationMethod", ad.getProduct().getDepreciationMethod());
                jobj.put("assetGroupId", ad.getProduct().getID());
                jobj.put("assetId", ad.getAssetId());
                jobj.put("description", ad.getAssetDescription());
                jobj.put("groupinfo",ad.getProduct().getName()+"("+ad.getProduct().getProductid()+")");
                Date installationDate = ad.getInstallationDate();
                Calendar cal = Calendar.getInstance();
                cal.setTime(installationDate);
                int installationyear = cal.get(Calendar.YEAR);
                int depreciationMethod = ad.getProduct().getDepreciationMethod();
                jobj.put("installationDate", df.format(ad.getInstallationDate()));
                jobj.put("purchaseDate", df.format(ad.getPurchaseDate()));
                jobj.put("assetValue", ad.getCost());
                jobj.put("department", ad.getDepartment() != null ? ad.getDepartment().getName() : "");
                if (isDisposedAssetReport) {
                    if (subtotal.has("assetValue")) {
                        subtotal.put("assetValue", subtotal.getDouble("assetValue") + ad.getCost());
                    } else {
                        subtotal.put("assetValue", ad.getCost());
                    }
                    if (grandTotal.has("assetValue")) {
                        grandTotal.put("assetValue", grandTotal.getDouble("assetValue") + ad.getCost());
                    } else {
                        grandTotal.put("assetValue", ad.getCost());
                    }
                }
                jobj.put("salvageRate", ad.getSalvageRate());
                jobj.put("salvageValue", ad.getSalvageValue());
                jobj.put("openingDepreciation", ad.getOpeningDepreciation());
                String invoiceid = "", invoiceDetailID = "";
                double assetgstcost=0d;  // variable used for ITC flow (India GST).
                // Put Purchase Invoice Number and Vendor Name
                if (ad.isCreatedFromOpeningForm()) {  //If Assets are created from Opening Form
                    jobj.put("vendorname", "Opening Asset");  // There is no Vendor in Opening Asset creation form
                    HashMap<String, Object> requestMap = new HashMap<String, Object>();
                    requestMap.put("companyId", sessionHandlerImpl.getCompanyid(request));
                    requestMap.put("assetDetails", ad.getId());
                    KwlReturnObject assetDetailsResult = accProductObj.getAssetDetailsMappedWithOPeningDocument(requestMap);
                    List<FixedAssetOpeningMappingWithAssetDetail> openlist = assetDetailsResult.getEntityList();
                    String docno = "";
                    Date creationDate = new Date();
                    for (FixedAssetOpeningMappingWithAssetDetail openingMappingWithAssetDetail : openlist) {
                        docno = openingMappingWithAssetDetail.getAssetOpening().getDocumentNumber();
                        creationDate = openingMappingWithAssetDetail.getAssetOpening().getCreationDate();
                    }
                    jobj.put("purchaseinvno", docno);  // opening Document No
                    jobj.put("purchaseinvdate", df.format(creationDate));
                } else {
                    HashMap<String, Object> requestMap = new HashMap<String, Object>();
                    requestMap.put("companyId", sessionHandlerImpl.getCompanyid(request));
                    requestMap.put("assetDetailId", ad.getId());
                    List normallist = accProductObj.getPurchaseInvoiceNoAndVendorOfAssetPurchaseInvoice(requestMap);
                    Iterator normalit = normallist.iterator();
                    String docno = "";
                    Vendor vendor = null;
                    JournalEntry je = null;
                    Tax taxObj = null;
                    if (normallist.size() <= 0) {//If Asset GR is linked in Asset PI
                        String assetDetailId = "";
                        requestMap.put("assetid", ad.getAssetId());
                        KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                        List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                        for (AssetDetails assetDetailObj : assetDetaillist) {
                            assetDetailId += assetDetailObj.getId() + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(assetDetailId)) {
                            assetDetailId = assetDetailId.substring(0, assetDetailId.length() - 1);
                            requestMap.put("assetDetailId", assetDetailId);
                            normallist = accProductObj.getPIDetails_AssetGRLinkedInAssetPI(requestMap);
                            normalit = normallist.iterator();
                        }
                    }
                    while (normalit.hasNext()) {
                        Object obj[] = (Object[]) normalit.next();
                        docno = (String) obj[0];
                        String vendorId = (String) obj[1];
                        String jeid = (String) obj[2];
                        String taxID = (String) obj[3]; //get global level tax
                        if (StringUtil.isNullOrEmpty(taxID)) {
                            taxID = (String) obj[4];   //get line level tax
                        }
                        invoiceid = (String) obj[5];
                        invoiceDetailID = (String) obj[6];
                        if (isFixedAssetDetailReport && !StringUtil.isNullOrEmpty(countryId) && Integer.parseInt(countryId) == Constants.indian_country_id) {
                            /**
                             * India Country ITC flow.
                             */
                            int itctype = (int) obj[7];
                            if (itctype == Constants.GST_ITCTYPE_BLOCKED) {
                                /**
                                 * If ITC type is Blocked ITC in that case gst
                                 * amount should get added in cost of asset.
                                 */
                                double rowtermamount = (double) obj[8];
                                double taxableamt = (double) obj[9];
                                double assetcost = ad.getCost();
                                assetgstcost = rowtermamount * assetcost / taxableamt;
                                assetgstcost = authHandler.round(assetgstcost, companyId);
                                jobj.put("assetValue", ad.getCost() + assetgstcost);
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(vendorId)) {
                            vendor = (Vendor) kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(), vendorId);
                        }
                        if (!StringUtil.isNullOrEmpty(jeid)) {
                            je = (JournalEntry) kwlCommonTablesDAOObj.getClassObject(JournalEntry.class.getName(), jeid);
                        }
                        if (!StringUtil.isNullOrEmpty(taxID)) {
                            taxObj = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxID);
                        }
                    }
                    if (vendor == null) {//Case when no document is linked
                        List vlist = accProductObj.getVendorForAssetDetail(requestMap);
                        Iterator vitr = vlist.iterator();
                        if (vitr.hasNext()) {
                            String vendorId = (String) vitr.next();
                            if (!StringUtil.isNullOrEmpty(vendorId)) {
                                vendor = (Vendor) kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(), vendorId);
                            }
                        }
                    }
                    jobj.put("purchaseinvno", docno);  //Purchase Invoice No
                    jobj.put("vendorname", vendor != null ? vendor.getName() : "");  //Vendor Name
                    jobj.put("purchaseinvdate", je != null ? df.format(je.getEntryDate()) : ""); // Purchase Invoice Date
                    /*
                     Code to get GST code used in Asset Purchase Invoice
                     */
                    jobj.put("gstcode", taxObj != null ? taxObj.getTaxCode() : "");
                }
                double totalLandingCost = 0.0;
                if (!StringUtil.isNullOrEmpty(countryId) && Integer.parseInt(countryId) == Constants.indian_country_id) {
                    if (!StringUtil.isNullOrEmpty(invoiceDetailID) && !StringUtil.isNullOrEmpty(invoiceid) && extraCompanyPreferences.isActivelandingcostofitem()) {
                        Set<LandingCostCategory> lccSet = ad.getProduct().getLccategoryid();
                        String invoiceAssetDetailId = "";
                        HashMap<String, Object> requestMap = new HashMap<>();
                        requestMap.put("assetid", ad.getAssetId());
                        requestMap.put("companyId", companyId);
                        KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                        List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                        for (AssetDetails assetDetailObj : assetDetaillist) {
                            invoiceAssetDetailId = assetDetailObj.getId();
                        }
                        if (!StringUtil.isNullOrEmpty(invoiceAssetDetailId)) {
                            totalLandingCost = getTotalLandedCost(lccSet, invoiceid, globalParams, invoiceDetailID, invoiceAssetDetailId, companyId, jobj);
                        }
                        if (!isDisposedAssetReport) {
                            jobj.put("assetValue", ad.getCost()+assetgstcost + totalLandingCost);
                            jobj.put("assetvaluewithoutlandedcost", ad.getCost()+assetgstcost);
                        }
                    }
                }
                /*
                 Add Disposal Invoice related details
                 */
                if ((isFixedAssetDetailReport || isDisposedAssetReport) && (resultMap != null && resultMap.size() > 0)) {
                    if (resultMap.containsKey(ad.getId())) {
                        List data = (List) resultMap.get(ad.getId());
                        jobj.put("disposalinvoiceno", data.get(1));
                        jobj.put("disposalinvoicedate", data.get(2) != null ? df.format(data.get(2)) : "");
                        jobj.put("disposaljeid", data.get(3) != null ? data.get(3) : "");
                        jobj.put("disposaljeno", data.get(4) != null ? data.get(4) : "");
                    } else {
                        jobj.put("disposalinvoiceno", "");
                        jobj.put("disposalinvoicedate", "");
                        jobj.put("disposaljeid", "");
                        jobj.put("disposaljeno", "");
                    }
                }
                // Put Disposal JE related info in Disposal Assets Report
                if (isDisposedAssetReport) {
                    if (!(ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO)) {
                        jobj.put("disposaljeid", ad.getDisposalJE() != null ? ad.getDisposalJE().getID() : "");
                        jobj.put("disposaljeno", ad.getDisposalJE() != null ? ad.getDisposalJE().getEntryNumber() : "");
                    }

                    double salesproceed = 0d;
                    if (!ad.isIsDisposed() && ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI) {
                        salesproceed = ad.getSellAmount();
                    }
                    jobj.put("salesproceed", salesproceed);

                    if (subtotal.has("salesproceed")) {
                        subtotal.put("salesproceed", subtotal.getDouble("salesproceed") + salesproceed);
                    } else {
                        subtotal.put("salesproceed", salesproceed);
                    }
                    if (grandTotal.has("salesproceed")) {
                        grandTotal.put("salesproceed", grandTotal.getDouble("salesproceed") + salesproceed);
                    } else {
                        grandTotal.put("salesproceed", salesproceed);
                    }
                }

                Date disposalDate = ad.getDisposalDate();
                if (isFixedAssetDetailReport) {
                    double cost_openingbalance = 0, additions = 0, openingdep = 0;
                    double depdisposal = 0, yearval = 0;
                    if (disposalDate != null && disposalDate.before(startdt)) {
                        double ZERO_AMOUNT = 0;
                        jobj.put("cost_openingbalance", authHandler.round(ZERO_AMOUNT, companyId));
                        jobj.put("additions", authHandler.round(ZERO_AMOUNT, companyId));
                        jobj.put("disposal", authHandler.round(ZERO_AMOUNT, companyId));
                        jobj.put("costclosingbal", authHandler.round(ZERO_AMOUNT, companyId));

                        jobj.put("openingdep", authHandler.round(ZERO_AMOUNT, companyId));
                        jobj.put("yearval", ZERO_AMOUNT);
                        jobj.put("depdisposal", authHandler.round(ZERO_AMOUNT, companyId));
                        jobj.put("depclosingbal", authHandler.round(ZERO_AMOUNT, companyId));

                        jobj.put("netbookvalue", authHandler.round(ZERO_AMOUNT, companyId));
                    } else {
                        if (installationDate.before(startdt)) {
                            cost_openingbalance = ad.getCost();
                        } else if ((installationDate.after(startdt) || installationDate.equals(startdt)) && (installationDate.before(enddt) || installationDate.equals(enddt))) {
                            additions = ad.getCost();
                        }
                        jobj.put("cost_openingbalance", authHandler.round(cost_openingbalance, companyId));
                        jobj.put("additions", authHandler.round(additions, companyId));

                        String backyears = "";    // Variable to calculate previous years Depreciation
                        int tempYear = installationyear;    // Variable used to comapare the creation year with current year
//                        if (installationyear <= currentyear) {
                        while (installationyear < currentyear) {
                            backyears += installationyear + ",";
                            installationyear++;
                        }
//                        }

                        int stmonth = 0, endmonth = 0;
                        double accopeningbal = 0;
                        stmonth = installationDate.getMonth();
                        if (startdt.getMonth() != 0) {
                            endmonth = startdt.getMonth() - 1;
                            backyears += currentyear + ",";
                        } else {
                            endmonth = 11;
                        }
                        if (!StringUtil.isNullOrEmpty(backyears)) {   // if there is no previous year to calculate Depreciation then no need to calculate Deprecaition
                            JSONArray curfinalJArr = new JSONArray();
                            HashMap<String, Object> fieldrequestParams1 = new HashMap();
                            fieldrequestParams1.put("startMonth", stmonth);
                            fieldrequestParams1.put("endMonth", endmonth);
                            fieldrequestParams1.put("years", backyears);
                            fieldrequestParams1.put("companyid", sessionHandlerImpl.getCompanyid(request));
                            fieldrequestParams1.put("depreciationCalculationType", depreciationCalculationType);
                            //                        fieldrequestParams1.put("isDepreciationDetailReport", true);
                            fieldrequestParams1.put("isFixedAssetDetailReport", true);
                            if (depreciationCalculationType == 0) { //SDP-7503- Reason- For last period JSON objet was not put in JSON array due to rounding off  problem.
                                fieldrequestParams1.put("isOpening", true);
                            }
                            //                        fieldrequestParams1.put("finanDate", finanDate);

                            if (depreciationMethod == 1) {
                                getAssetStraightLineDepreciation(fieldrequestParams1, ad, curfinalJArr, extraCompanyPreferences);
                            } else {
                                getDoubleDeclineDepreciation(fieldrequestParams1, ad, curfinalJArr, extraCompanyPreferences);
                            }

                            for (int i = 0; i < curfinalJArr.length(); i++) {
                                JSONObject newjobj = curfinalJArr.getJSONObject(i);
                                if (newjobj.has("firstperiodamtInBase")) {
                                    accopeningbal += newjobj.getDouble("firstperiodamtInBase");
                                }
                            }
                            openingdep = accopeningbal;
                        }
                        jobj.put("openingdep", authHandler.round(openingdep, companyId));

                        JSONArray finalJArr = new JSONArray();
                        int startMonth = 0;
                        String calculatedYear = "";
                        Calendar endCalendar = Calendar.getInstance();
                        endCalendar.setTime(enddt);
                        int endYear = endCalendar.get(Calendar.YEAR);
                        int startYear = currentyear;
                        while (startYear <= endYear) {
                            calculatedYear += startYear + ",";
                            startYear++;
                        }
                        if (depreciationCalculationType == 0) { // if yearly Depreciatiion Method is selected
                            calculatedYear = year;
                        } else {
                            startMonth = startdt.getMonth();
                            endMonth = enddt.getMonth();
                        }

                        HashMap<String, Object> fieldrequestParams = new HashMap();
                        fieldrequestParams.put("startMonth", startMonth);
                        fieldrequestParams.put("endMonth", endMonth);
                        fieldrequestParams.put("years", calculatedYear);
                        fieldrequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                        fieldrequestParams.put("depreciationCalculationType", depreciationCalculationType);
                        //                    fieldrequestParams.put("finanDate", finanDate);
                        //                    fieldrequestParams.put("isDepreciationDetailReport", true);
                        fieldrequestParams.put("isFixedAssetDetailReport", true);

                        if (depreciationMethod == 1) { // if selected method is Yearly Straight Line Depreciation Method
                            getAssetStraightLineDepreciation(fieldrequestParams, ad, finalJArr, extraCompanyPreferences);
                        } else if (depreciationMethod == 2) { // IF Depreciation Method is double declined
                            getDoubleDeclineDepreciation(fieldrequestParams, ad, finalJArr, extraCompanyPreferences);
                        }
                        for (int i = 0; i < finalJArr.length(); i++) {
                            JSONObject newjobj = finalJArr.getJSONObject(i);
                            if (depreciationCalculationType == 0) {    // if yearly Depreciatiion Method is selected
                                if (newjobj.has("fromyear")) {
                                    yearval = newjobj.getDouble("firstperiodamtInBase");
                                }
                            } else {      // if Depreciation Method is either Monthly or on the Actual Date
                                if (newjobj.has("frommonth")) {
                                    yearval += newjobj.getDouble("firstperiodamtInBase");
                                }
                            }
                        }
                        jobj.put("yearval", yearval);

                        double accdepreciation = openingdep + yearval;
                        double disposal = 0;
                        if (disposalDate != null && (disposalDate.after(startdt) || disposalDate.equals(startdt)) && (disposalDate.before(enddt) || disposalDate.equals(enddt))) {
//                            if (ad.isIsDisposed()) {
//                                disposal = ad.getDisposalProfitLoss();
//                            } else if(ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO){
//                                disposal = ad.getCost() - accdepreciation;
//                            }
                            if (ad.isIsDisposed() || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO) {
                                disposal = ad.getCost();//If disposed asset then display asset cost in Cost-Disposal
                                depdisposal = accdepreciation;//If disposed asset then Depreciation-Disposal is addition of Depreciation-Opening Balance & Depreciation-Current Dep
                            }
                        }
                        jobj.put("disposal", authHandler.round(disposal, companyId));

                        double costclosingbal = cost_openingbalance + additions - disposal;
                        jobj.put("costclosingbal", authHandler.round(costclosingbal, companyId));

                        jobj.put("depdisposal", authHandler.round(depdisposal, companyId));
                        double depclosingbal = accdepreciation - depdisposal;
                        jobj.put("depclosingbal", authHandler.round(depclosingbal, companyId));

                        double netbookvalue = authHandler.round(costclosingbal, companyId) - authHandler.round(depclosingbal, companyId);
                        jobj.put("netbookvalue", authHandler.round(netbookvalue, companyId));
                    }
                    jobj.put("normalopeningdepreciation", authHandler.round(ad.getOpeningDepreciation(), companyId));
                }

                if (!isDepreciationReport) {
                    if (ad.getProduct().getDepreciationMethod() == 1) {
                        jobj.put("assetdepreciationschedule", "Straight Line Depreciation");
                    } else if (ad.getProduct().getDepreciationMethod() == 2) {
                        jobj.put("assetdepreciationschedule", "Double Decline Depreciation");
                    } else {
                        jobj.put("assetdepreciationschedule", "Non Depreciable");
                    }
                    String status = "";
                    if (ad.isPurchaseReturn() == true) {
                        status = "Returned";
                    }
                    /*
                     getAssetSoldFlag =  1 means asset has been sold from CI, &&&  2 means asset has been sold from DO
                     */
                    if (ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO) {
                        status = "Disposed";
                    } else if (ad.isIsDisposed()) {
                        status = "Manually Disposed";
                    }
                    HashMap<String, Object> assetParams = new HashMap<String, Object>();
                    assetParams.put("assetDetailsId", ad.getId());
                    assetParams.put(Constants.companyKey, companyId);
                    assetParams.put("assetDetails", true);
                    KwlReturnObject assResult = accProductObj.getAssetDepreciationDetail(assetParams);
                    List<AssetDepreciationDetail> assList = assResult.getEntityList();

                    if (assList.size() > 0 || ad.getOpeningDepreciation() > 0) {
                        jobj.put("isdepreciationposted", "Yes");
                    } else {
                        jobj.put("isdepreciationposted", "No");
                    }
                    jobj.put("status", status);
                    if (ad.getDisposalDate() != null) {
                        jobj.put("disposalDate", df.format(ad.getDisposalDate()));
                    }
                    if (!StringUtil.isNullOrEmpty(ad.getProduct().getID())) {
                        KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), ad.getProduct().getID());
                        product = (Product) prodresult.getEntityList().get(0);
                        isBatchForProduct = product.isIsBatchForProduct();
                        isSerialForProduct = product.isIsSerialForProduct();
                        isLocationForProduct = product.isIslocationforproduct();
                        isWarehouseForProduct = product.isIswarehouseforproduct();
                        isRowForProduct = product.isIsrowforproduct();
                        isRackForProduct = product.isIsrackforproduct();
                        isBinForProduct = product.isIsbinforproduct();
                    }

                    // calculate asset depreciation cost
                    double assetDepreciatedCost = 0d;
                    double assetNetBookValue = 0d;
                    if (ad.isIsDisposed() || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO) {
                        assetNetBookValue = 0;
                    } else {
                        for (AssetDepreciationDetail depreciationDetail : assList) {
                            assetDepreciatedCost += depreciationDetail.getPeriodAmount();
                        }
                        assetNetBookValue = ad.getCost()+assetgstcost - assetDepreciatedCost + totalLandingCost;
                    }

                    jobj.put("assetLife", authHandler.formattingDecimalForAmount(ad.getAssetLife(), companyId));
                    jobj.put("remainingLife", authHandler.formattingDecimalForAmount(ad.getRemainingLife(), companyId));
                    jobj.put("currencycode", currencyCode);
                    jobj.put("assetNetBookValue", assetNetBookValue);
                    jobj.put("accumulateddepreciationcost", assetDepreciatedCost);
                    if (isDisposedAssetReport) {
                        if (subtotal.has("assetNetBookValue")) {
                            subtotal.put("assetNetBookValue", subtotal.getDouble("assetNetBookValue") + assetNetBookValue);
                        } else {
                            subtotal.put("assetNetBookValue", assetNetBookValue);
                        }
                        if (grandTotal.has("assetNetBookValue")) {
                            grandTotal.put("assetNetBookValue", grandTotal.getDouble("assetNetBookValue") + assetNetBookValue);
                        } else {
                            grandTotal.put("assetNetBookValue", assetNetBookValue);
                        }

                        if (subtotal.has("accumulateddepreciationcost")) {
                            subtotal.put("accumulateddepreciationcost", subtotal.getDouble("accumulateddepreciationcost") + assetDepreciatedCost);
                        } else {
                            subtotal.put("accumulateddepreciationcost", assetDepreciatedCost);
                        }
                        if (grandTotal.has("accumulateddepreciationcost")) {
                            grandTotal.put("accumulateddepreciationcost", grandTotal.getDouble("accumulateddepreciationcost") + assetDepreciatedCost);
                        } else {
                            grandTotal.put("accumulateddepreciationcost", assetDepreciatedCost);
                        }
                    }
                    jobj.put("assetUser", (ad.getAssetUser() != null) ? ad.getAssetUser().getFirstName() : "N/A");
                    jobj.put("isAssetSold", (ad.getAssetSoldFlag() != 0) ? true : false);
                    jobj.put("isDepreciable", (ad.getProduct().getDepreciationMethod() != 3) ? true : false);
                    jobj.put("isLeased", isexport ? (ad.isLinkedToLeaseSO() == true ? "Yes" : "No") : ad.isLinkedToLeaseSO());//isExport is used for value of isLeased are shown in csv and pdf are Yes or No 
                    jobj.put("location", (ad.getLocation() != null ? ad.getLocation().getName() : "N/A"));

                    //if batch serial option is on then show the batch detail and seial no   
                    if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()) {  //check if company level option is on then only we will check productt level
                        if (isBatchForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {
                            jobj.put("isLocationForProduct", isLocationForProduct);
                            jobj.put("isWarehouseForProduct", isWarehouseForProduct);

                            KwlReturnObject kmsg = null;
                            List batchserialdetails = new ArrayList();
                            if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && product.isIsSerialForProduct()) {
                                kmsg = accCommonTablesDAO.getOnlySerialDetails(ad.getId(), linkingFlag, moduleID, false, isEdit);
                                batchserialdetails = kmsg.getEntityList();
                            } else {
                                if (!product.isIsSerialForProduct() && baMap.containsKey(ad.getId())) {
                                    batchserialdetails = baMap.get(ad.getId());
                                } else {
                                    kmsg = accCommonTablesDAO.getBatchSerialDetails(ad.getId(), !product.isIsSerialForProduct(), linkingFlag, moduleID, false, isEdit, "");
                                    batchserialdetails = kmsg.getEntityList();
                                }
                            }
                            double ActbatchQty = 1;
                            double batchQty = 0;
                            Iterator iter = batchserialdetails.iterator();
                            while (iter.hasNext()) {
                                Object[] objArr = (Object[]) iter.next();
                                jobj.put("batchid", objArr[0] != null ? (String) objArr[0] : "N/A");
                                jobj.put("batch", objArr[1] != null ? (String) objArr[1] : "N/A");
                                jobj.put("batchname", objArr[1] != null ? (String) objArr[1] : "N/A");
                                if (objArr[2] != null) {
                                    String locationId = objArr[2].toString();
                                    if (!StringUtil.isNullOrEmpty(locationId)) {
                                        KwlReturnObject loct = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), locationId);
                                        InventoryLocation location = (InventoryLocation) loct.getEntityList().get(0);
                                        jobj.put("location", !StringUtil.isNullOrEmpty(location.getName()) ? location.getName() : "N/A");
                                    } else {
                                        jobj.put("location", "N/A");
                                    }
                                }
                                if (objArr[3] != null) {
                                    String warehouseId = objArr[3].toString();
                                    if (!StringUtil.isNullOrEmpty(warehouseId)) {
                                        KwlReturnObject war = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouseId);
                                        InventoryWarehouse warehouse = (InventoryWarehouse) war.getEntityList().get(0);
                                        jobj.put("warehouse", !StringUtil.isNullOrEmpty(warehouse.getName()) ? warehouse.getName() : "N/A");
                                    } else {
                                        jobj.put("warehouse", "N/A");
                                    }
                                }
                                if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct()) && product.isIsSerialForProduct()) {
                                    ActbatchQty = objArr[6] != null ? Double.parseDouble(objArr[6].toString()) : 0;
                                    if (batchQty == 0) {
                                        batchQty = objArr[6] != null ? Double.parseDouble(objArr[6].toString()) : 0;
                                    }
                                    if (batchQty == ActbatchQty) {
                                        jobj.put("isreadyonly", false);
                                    } else {
                                        jobj.put("isreadyonly", true);
                                    }
                                } else {
                                    jobj.put("isreadyonly", false);
                                }
                                if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && product.isIsSerialForProduct()) {
                                    jobj.put("mfgdate", "N/A");
                                    jobj.put("expdate", "N/A");
                                } else {
                                    jobj.put("mfgdate", objArr[4] != null ? df.format(objArr[4]) : "N/A");
                                    jobj.put("expdate", objArr[5] != null ? df.format(objArr[5]) : "N/A");
                                }

                                jobj.put("quantity", objArr[6] != null ? objArr[6] : "N/A");

                                if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct()) && !product.isIsSerialForProduct()) {
                                    jobj.put("quantity", objArr[11] != null ? objArr[11] : "N/A");
                                }
                                jobj.put("serialnoid", objArr[7] != null ? (String) objArr[7] : "N/A");
                                jobj.put("serialno", objArr[8] != null ? (String) objArr[8] : "N/A");
                                jobj.put("expstart", (objArr[9] != null && !objArr[9].toString().equalsIgnoreCase("")) ? df.format(objArr[9]) : "N/A");
                                jobj.put("expend", (objArr[10] != null && !objArr[10].toString().equalsIgnoreCase("")) ? df.format(objArr[10]) : "N/A");

                                String row = objArr[15] != null ? objArr[15].toString() : "";
                                if (!StringUtil.isNullOrEmpty(row)) {
                                    KwlReturnObject kwlStoreMaster = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), row);
                                    StoreMaster storeMasterRow = (StoreMaster) kwlStoreMaster.getEntityList().get(0);
                                    jobj.put("row", storeMasterRow.getName() != null ? storeMasterRow.getName() : "N/A");
                                } else {
                                    jobj.put("row", "N/A");
                                }

                                String rack = objArr[16] != null ? objArr[16].toString() : "";
                                if (!StringUtil.isNullOrEmpty(rack)) {
                                    KwlReturnObject kwlStoreMaster = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), rack);
                                    StoreMaster storeMasterRack = (StoreMaster) kwlStoreMaster.getEntityList().get(0);
                                    jobj.put("rack", storeMasterRack.getName() != null ? storeMasterRack.getName() : "N/A");
                                } else {
                                    jobj.put("rack", "N/A");
                                }

                                String bin = objArr[17] != null ? objArr[17].toString() : "";
                                if (!StringUtil.isNullOrEmpty(bin)) {
                                    KwlReturnObject kwlStoreMaster = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), bin);
                                    StoreMaster storeMasterBin = (StoreMaster) kwlStoreMaster.getEntityList().get(0);
                                    jobj.put("bin", storeMasterBin.getName() != null ? storeMasterBin.getName() : "N/A");
                                } else {
                                    jobj.put("bin", "N/A");
                                }

                                batchQty--;
                            }
                        } else {
                            jobj.put("isLocationForProduct", isLocationForProduct);
                            jobj.put("isWarehouseForProduct", isWarehouseForProduct);
                            jobj.put("serialno", "N/A");
                            jobj.put("batch", "N/A");
                            jobj.put("batchname", "N/A");
                            jobj.put("expstart", "N/A");
                            jobj.put("expend", "N/A");
                            jobj.put("warehouse", "N/A");
                            jobj.put("mfgdate", "N/A");
                            jobj.put("expdate", "N/A");
                            jobj.put("row", "N/A");
                            jobj.put("rack", "N/A");
                            jobj.put("bin", "N/A");
                        }
                    } else {
                        jobj.put("isLocationForProduct", isLocationForProduct);
                        jobj.put("isWarehouseForProduct", isWarehouseForProduct);
                        jobj.put("serialno", "N/A");
                        jobj.put("batch", "N/A");
                        jobj.put("batchname", "N/A");
                        jobj.put("expstart", "N/A");
                        jobj.put("expend", "N/A");
                        jobj.put("warehouse", "N/A");
                        jobj.put("mfgdate", "N/A");
                        jobj.put("expdate", "N/A");
                        jobj.put("row", "N/A");
                        jobj.put("rack", "N/A");
                        jobj.put("bin", "N/A");
                    }

                    HashMap<String, Object> fieldrequestParams1 = new HashMap();
                    fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams1.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_FixedAssets_Details_ModuleId));

                    HashMap<String, String> customFieldMap1 = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap1 = new HashMap<String, String>();
                    HashMap<String, String> replaceFieldMap11 = new HashMap<String, String>();
                    HashMap<String, Integer> fieldMap1 = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap11, customFieldMap1, customDateFieldMap1);

                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    AssetDetailsCustomData jeDetailCustom = (AssetDetailsCustomData) ad.getAssetDetailsCustomData();
                    replaceFieldMap11 = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMap1, replaceFieldMap11, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyId);
                        params.put("getCustomFieldArray", true);
                        params.put("isExport", isexport);
                        params.put("isForReport", true);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap1, customDateFieldMap1, jobj, params);
                    }
                }
                jArr.put(jobj);
                if (isDisposedAssetReport) {
                    groupTotal.put(ad.getProduct().getID(), subtotal);
                }
            }
            if (isDisposedAssetReport) {
                if (jArr.length() > 0 && subtotal != null) {
                    jArr.put(subtotal);
                }

                grandTotal.put("issummaryvalue", true);
                grandTotal.put("assetId", isexport?"Grand Total":"<b>Grand Total</b>");
                grandTotal.put("assetGroup", "");
                if (jArr.length() > 0) {
                    jArr.put(grandTotal);
                }
            }
            finalJSONObject.put(Constants.RES_data, jArr);
            finalJSONObject.put(Constants.RES_TOTALCOUNT, count);
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return finalJSONObject;
    }

    private double getTotalLandedCost(Set<LandingCostCategory> lccSet, String invoiceid, HashMap<String, Object> globalParams, String invoiceDetailID, String invoiceAssetDetailId, String companyId, JSONObject jobj) throws ServiceException, JSONException {
        HashMap<String, Double> allcactionMthdData = new HashMap<String, Double>();
        double totalLandingCost = 0.0;
        for (LandingCostCategory lcc : lccSet) {
            /*
             * ------------------ Landed Invoice List(Landing Cost Category ) ------------------
             */
            String landingCostOfCategory = lcc.getId();
            String purchaseinvoiceid = invoiceid;
            KwlReturnObject kwlLCObj = accGoodsReceiptDAOobj.getLandedInviceList(purchaseinvoiceid, landingCostOfCategory);  // Get List of Landed Cost Category  Expense Invoice .
            List<String> expanseGRList = kwlLCObj.getEntityList();
            KwlReturnObject custresult = null;
            for (String grIDObj : expanseGRList) {
                double expanseCharge = 0.0D;
                if (!StringUtil.isNullOrEmpty(grIDObj)) {
                    // Check allocation type Total eligable Total Cost & Total Item .
                    custresult = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grIDObj);
                    GoodsReceipt expanseGRObj = (GoodsReceipt) custresult.getEntityList().get(0);
                    double goodsrecmanualProductAmount = 0.0;
                    /*Get Product Cost Manual Entered  and allocation type is Manual */
                    if (lcc.getLcallocationid() == 3 || lcc.getLcallocationid() == 4) {
                        KwlReturnObject kwlreturn = accGoodsReceiptDAOobj.getManualProductCostLCC(expanseGRObj.getID(), invoiceDetailID, invoiceAssetDetailId);
                        List itemList = kwlreturn.getEntityList();
                        Iterator itemItr = itemList.iterator();
                        while (itemItr.hasNext()) {
                            LccManualWiseProductAmount lccManualWiseProductAmount = (LccManualWiseProductAmount) itemItr.next();
                            goodsrecmanualProductAmount = lccManualWiseProductAmount.isCustomDutyAllocationType() ? lccManualWiseProductAmount.getTaxablevalueforigst() : lccManualWiseProductAmount.getAmount();
                            KWLCurrency currencytemp = (KWLCurrency) expanseGRObj.getCurrency();
                            String currencyIdtemp = currencytemp.getCurrencyID();
                            Date billDateTemp = expanseGRObj.getCreationDate() != null ? expanseGRObj.getCreationDate() : expanseGRObj.getFormdate();
                            KwlReturnObject ruternBRExpan = currencyDAO.getCurrencyToBaseAmount(globalParams, goodsrecmanualProductAmount, currencyIdtemp, billDateTemp, expanseGRObj.getJournalEntry().getExternalCurrencyRate());
                            goodsrecmanualProductAmount = authHandler.roundUnitPrice((Double) ruternBRExpan.getEntityList().get(0), companyId);
                        }
                    }
                    allcactionMthdData.put("totLandedCost", 0.0);
                    allcactionMthdData.put("noEligiableItem", 0.0);
                    allcactionMthdData.put("lineItemQty", 0.0);
                    allcactionMthdData.put("valueOfItem", 0.0);
                    allcactionMthdData.put("eligiableItemCost", 0.0);
                    allcactionMthdData.put("eligiableItemWgt", 0.0);
                    allcactionMthdData.put("itemWght", 0.0);
                    allcactionMthdData.put("manualProductAmount", goodsrecmanualProductAmount);
                    /* Calculate Item wise Landed cost category */
                    expanseCharge = LandingCostAllocationType.getTotalLanddedCost(expanseGRObj.getLandingCostCategory().getLcallocationid(), allcactionMthdData);
                    totalLandingCost += authHandler.roundUnitPrice(expanseCharge, companyId);
                    //Same landing cost of category have multipul Expense Purchase invoice then combine expanse Charges  
                    if (jobj.has(landingCostOfCategory)) {
                        expanseCharge += jobj.getDouble(landingCostOfCategory);
                        jobj.put(landingCostOfCategory, authHandler.roundUnitPrice(expanseCharge, companyId));
                    } else {
                        jobj.put(landingCostOfCategory, authHandler.roundUnitPrice(expanseCharge, companyId));
                    }
                }
            }
        }
        return totalLandingCost;
    }

    public Map<String, List<Object[]>> getBatchDetailsMap(Map<String, Object> requestParams) {
        Map<String, List<Object[]>> baMap = new HashMap<>();
        try {
            boolean linkingFlag = false;
            if (requestParams.containsKey("linkingFlag")) {
                linkingFlag = Boolean.parseBoolean(requestParams.get("linkingFlag").toString());
            }
            boolean isEdit = false;
            if (requestParams.containsKey("isEdit")) {
                isEdit = Boolean.parseBoolean(requestParams.get("isEdit").toString());
            }
            String moduleID = "";
            if (requestParams.containsKey("moduleID")) {
                moduleID = requestParams.get("moduleID").toString();
            }
            String documentIds = "";
            if (requestParams.containsKey("documentIds")) {
                documentIds = requestParams.get("documentIds").toString();
            }
            KwlReturnObject kmsg = accCommonTablesDAO.getBatchSerialDetails("", true, linkingFlag, moduleID, false, isEdit, documentIds);
            List<Object[]> batchserialdetails = kmsg.getEntityList();
            for (Object[] objects : batchserialdetails) {
                if (objects.length >= 20 && objects[20] != null) {  // chek wheather result having the documentid or not
                    if (baMap.containsKey(objects[20].toString())) {
                        List<Object[]> details = baMap.get(objects[20].toString());
                        details.add(objects);
                        baMap.put(objects[20].toString(), details);
                    } else {
                        List<Object[]> details = new ArrayList<>();
                        details.add(objects);
                        baMap.put(objects[20].toString(), details);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return baMap;
    }

    @Override
    public JSONObject getAssetSummeryReportDetails(JSONObject jobject) throws JSONException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            int count = 0;
            JSONObject commData = new JSONObject();

            HashMap<String, Object> requestParams = productHandler.getProductRequestMapfromJson(jobject);

            String[] assetGroupIds = null;
            if (jobject.has("assetGroupIds") && !StringUtil.isNullOrEmpty(jobject.getString("assetGroupIds")) && !jobject.getString("assetGroupIds").equals("All")) {
                assetGroupIds = jobject.getString("assetGroupIds").split(",");
                requestParams.put("ids", assetGroupIds);
            }
            requestParams.put("isFixedAssetSummaryReport", true);
            boolean isFirstTimeLoad = (jobject.has("isFirstTimeLoad") && jobject.get("isFirstTimeLoad") != null) ? Boolean.parseBoolean(jobject.getString("isFirstTimeLoad")) : false;
            if (!isFirstTimeLoad) {
                KwlReturnObject result = accProductObj.getProducts(requestParams);
                List list = result.getEntityList();
                count = result.getRecordTotalCount();//dataJArr.length();
                JSONArray DataJArr = accProductModuleService.getProductsJson(jobject, list);

                dataJArr = getAssetSummeryReportDetailsJSON(DataJArr, jobject);
            }
            commData.put("coldata", dataJArr);
            commData.put("totalCount", count);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            jobj.put("valid", true);
            jobj.put("data", commData);
        } catch (SessionExpiredException | JSONException | AccountingException | ParseException ex) {
            Logger.getLogger(AccAssetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        return jobj;
    }

    public JSONArray getAssetSummeryReportDetailsJSON(JSONArray DataJArr, JSONObject jobject) throws ServiceException, SessionExpiredException, AccountingException {
        JSONArray dataJArr = new JSONArray();
        try {
            String companyId = (jobject.has(Constants.companyKey) && !StringUtil.isNullOrEmpty(jobject.getString(Constants.companyKey))) ? jobject.getString(Constants.companyKey) : "";
            KwlReturnObject excap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) excap.getEntityList().get(0);
            String countryId = extraCompanyPreferences.getCompany().getCountry().getID();
            int depreciationCalculationType = (jobject.has("depreciationCalculationType") && !StringUtil.isNullOrEmpty(jobject.getString("depreciationCalculationType"))) ? Integer.parseInt(jobject.getString("depreciationCalculationType")) : 0;
            Date startdt = null, enddt = null;
            DateFormat df = (jobject.has("df") && !StringUtil.isNullOrEmpty(jobject.getString("df"))) ? (DateFormat) jobject.get("df") : null;
            String year = "1970";
            HashMap<String, Object> globalParams = AccountingManager.getGlobalParams(jobject);
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put("companyId", companyId);
            requestParams.put("invrecord", true);
            if (jobject.has("searchJson") && !StringUtil.isNullOrEmpty(jobject.getString("searchJson"))) {
                requestParams.put("searchJson", jobject.getString("searchJson"));
            }
            if (jobject.has("filterConjuctionCriteria") && !StringUtil.isNullOrEmpty(jobject.getString("filterConjuctionCriteria"))) {
                requestParams.put("filterConjuctionCriteria", jobject.getString("filterConjuctionCriteria"));
            }
            if ((jobject.has("startdate") && !StringUtil.isNullOrEmpty(jobject.getString("startdate"))) && (jobject.has("enddate") && !StringUtil.isNullOrEmpty(jobject.getString("enddate")))) {
                startdt = df.parse(jobject.getString("startdate"));
//                requestParams.put(Constants.REQ_startdate, startdt);
                enddt = df.parse(jobject.getString("enddate"));
//                requestParams.put(Constants.REQ_enddate, enddt);
            }

            Date finanDate = null;
            int endMonth = 11;
            int currentyear = 0;
            if (startdt != null) {
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startdt);
                currentyear = startCal.get(Calendar.YEAR);
                year = String.valueOf(startCal.get(Calendar.YEAR));
            }

            //Iteration on asset groups
            for (int ctr = 0; ctr < DataJArr.length(); ctr++) {
                JSONObject jobj = DataJArr.getJSONObject(ctr);
                JSONObject jSONObject = new JSONObject();

                jSONObject.put("assetgroupname", jobj.get("productname"));
                jSONObject.put("assetgroupid", jobj.get("pid"));

                double cost_assetValue = 0, cost_additions = 0, cost_disposal = 0, cost_closingbalance = 0;
                double depreciation_openingbalance = 0, depreciation_current = 0, depreciation_disposals = 0, depreciation_closingbalance = 0;
                double nbv = 0;

                String[] assetgroupid = {jobj.getString("productid")};
                requestParams.put("assetGroupIds", assetgroupid);

                KwlReturnObject result = accProductObj.getAssetDetails(requestParams);
                List<AssetDetails> list = result.getEntityList();
                //Iteration on Assets under an Asset Group
                for (AssetDetails ad : list) {
                    int depreciationMethod = ad.getProduct().getDepreciationMethod();
                    if (depreciationMethod == 3) { // if Asset Group is Non Depreciable
                        continue;
                    }

                    Date creationDate = ad.getInstallationDate();
                    Date disposalDate = ad.getDisposalDate();
                    double totalLandingCost = 0.0;
                    String invoiceid = "", invoiceDetailID = "";
                    if (!StringUtil.isNullOrEmpty(countryId) && Integer.parseInt(countryId) == Constants.indian_country_id) {
                        if (extraCompanyPreferences.isActivelandingcostofitem()) {
                            HashMap<String, Object> requestMap = new HashMap<>();
                            requestMap.put("companyId", companyId);
                            requestMap.put("assetDetailId", ad.getId());
                            List normallist = accProductObj.getPurchaseInvoiceNoAndVendorOfAssetPurchaseInvoice(requestMap);
                            Iterator normalit = normallist.iterator();
                            if (normallist.size() <= 0) {//If Asset GR is linked in Asset PI
                                String assetDetailId = "";
                                requestMap.put("assetid", ad.getAssetId());
                                KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                                List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                                for (AssetDetails assetDetailObj : assetDetaillist) {
                                    assetDetailId += assetDetailObj.getId() + ",";
                                }
                                if (!StringUtil.isNullOrEmpty(assetDetailId)) {
                                    assetDetailId = assetDetailId.substring(0, assetDetailId.length() - 1);
                                    requestMap.put("assetDetailId", assetDetailId);
                                    normallist = accProductObj.getPIDetails_AssetGRLinkedInAssetPI(requestMap);
                                    normalit = normallist.iterator();
                                }
                            }
                            while (normalit.hasNext()) {
                                Object obj[] = (Object[]) normalit.next();
                                invoiceid = (String) obj[5];
                                invoiceDetailID = (String) obj[6];
                            }
                            if (!StringUtil.isNullOrEmpty(invoiceDetailID) && !StringUtil.isNullOrEmpty(invoiceid)) {
                                Set<LandingCostCategory> lccSet = ad.getProduct().getLccategoryid();
                                String invoiceAssetDetailId = "";
                                requestMap = new HashMap<>();
                                requestMap.put("assetid", ad.getAssetId());
                                requestMap.put("companyId", companyId);
                                KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                                List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                                for (AssetDetails assetDetailObj : assetDetaillist) {
                                    invoiceAssetDetailId = assetDetailObj.getId();
                                }
                                if (!StringUtil.isNullOrEmpty(invoiceAssetDetailId)) {
                                    totalLandingCost = getTotalLandedCost(lccSet, invoiceid, globalParams, invoiceDetailID, invoiceAssetDetailId, companyId, jSONObject);
                                }
                                jSONObject.put("assetValue", ad.getCost() + totalLandingCost);
                                jSONObject.put("assetvaluewithoutlandedcost", ad.getCost());
                            } else {
                                jSONObject.put("assetvaluewithoutlandedcost", ad.getCost());
                            }
                        }
                    }
                    if (disposalDate != null && disposalDate.before(startdt)) {
                        //If disposed before start date, no need to add its value
                    } else {
                        if (creationDate.before(startdt)) {
                            cost_assetValue += authHandler.round(ad.getCost() + totalLandingCost, companyId);
                        } else if ((creationDate.after(startdt) || creationDate.equals(startdt)) && (creationDate.before(enddt) || creationDate.equals(enddt))) {
                            cost_additions += authHandler.round(ad.getCost() + totalLandingCost, companyId);
                        }

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(creationDate);
                        int creationyear = cal.get(Calendar.YEAR);

                        String backyears = "";// Variable to calculate previous years Depreciation
                        if (creationyear <= currentyear) {
                            while (creationyear < currentyear) {
                                backyears += creationyear + ",";
                                creationyear++;
                            }
                        }

                        int stmonth = 0, endmonth = 0;
                        double accopeningbal = 0;
                        stmonth = creationDate.getMonth();
                        if (startdt.getMonth() != 0) {
                            endmonth = startdt.getMonth() - 1;
                            backyears += currentyear + ",";
                        } else {
                            endmonth = 11;
                        }
                        if (!StringUtil.isNullOrEmpty(backyears)) {   // if there is no previous year to calculate Depreciation then no need to calculate Deprecaition
                            JSONArray curfinalJArr = new JSONArray();
                            HashMap<String, Object> fieldrequestParams1 = new HashMap();
                            fieldrequestParams1.put("startMonth", stmonth);
                            fieldrequestParams1.put("endMonth", endmonth);
                            fieldrequestParams1.put("years", backyears);
                            fieldrequestParams1.put("companyid", companyId);
                            fieldrequestParams1.put("depreciationCalculationType", depreciationCalculationType);
                            //                        fieldrequestParams1.put("isDepreciationDetailReport", true);
                            fieldrequestParams1.put("isFixedAssetSummaryReport", true);
                            fieldrequestParams1.put("finanDate", finanDate);
                            if (depreciationCalculationType == 0) {//SDP-7503- Reason- For last period JSON objet was not put in JSON array due to rounding off  problem.
                                fieldrequestParams1.put("isOpening", true);
                            }

                            if (depreciationMethod == 1) {
                                getAssetStraightLineDepreciation(fieldrequestParams1, ad, curfinalJArr, extraCompanyPreferences);
                            } else {
                                getDoubleDeclineDepreciation(fieldrequestParams1, ad, curfinalJArr, extraCompanyPreferences);
                            }

                            for (int i = 0; i < curfinalJArr.length(); i++) {
                                JSONObject newjobj = curfinalJArr.getJSONObject(i);
                                if (newjobj.has("firstperiodamtInBase")) {
                                    accopeningbal += newjobj.getDouble("firstperiodamtInBase");
                                }
                            }
                            if (depreciationCalculationType == 0) { // if yearly Depreciatiion Method is selected
                                depreciation_openingbalance += authHandler.round(accopeningbal, 2);
                            } else {
                                depreciation_openingbalance += accopeningbal;
                            }
                        }

                        double yearval = 0d;
                        JSONArray finalJArr = new JSONArray();
                        int startMonth = 0;
                        String calculatedYear = "";
                        Calendar endCalendar = Calendar.getInstance();
                        endCalendar.setTime(enddt);
                        int endYear = endCalendar.get(Calendar.YEAR);
                        int startYear = currentyear;
                        while (startYear <= endYear) {
                            calculatedYear += startYear + ",";
                            startYear++;
                        }
                        if (depreciationCalculationType == 0) { // if yearly Depreciatiion Method is selected
                            calculatedYear = year;
                        } else {
                            startMonth = startdt.getMonth();
                            endMonth = enddt.getMonth();
                        }
                        HashMap<String, Object> fieldrequestParams = new HashMap();
                        fieldrequestParams.put("startMonth", startMonth);
                        fieldrequestParams.put("endMonth", endMonth);
                        fieldrequestParams.put("years", calculatedYear);
                        fieldrequestParams.put("companyid", companyId);
                        fieldrequestParams.put("depreciationCalculationType", depreciationCalculationType);
                        //                    fieldrequestParams.put("finanDate", finanDate);
                        //                    fieldrequestParams.put("isDepreciationDetailReport", true);
                        fieldrequestParams.put("isFixedAssetSummaryReport", true);

                        if (depreciationMethod == 1) { // if selected method is Straight Line Depreciation Method
                            getAssetStraightLineDepreciation(fieldrequestParams, ad, finalJArr, extraCompanyPreferences);
                        } else if (depreciationMethod == 2) { // IF Depreciation Method is double declined
                            getDoubleDeclineDepreciation(fieldrequestParams, ad, finalJArr, extraCompanyPreferences);
                        } else { // if Asset Group is not Deprecible
                            continue;
                        }
                        for (int i = 0; i < finalJArr.length(); i++) {
                            JSONObject newjobj = finalJArr.getJSONObject(i);
                            if (depreciationCalculationType == 0) {    // if yearly Depreciatiion Method is selected
                                if (newjobj.has("fromyear")) {
                                    yearval += authHandler.round(newjobj.getDouble("firstperiodamtInBase"), companyId);
                                }
                            } else {      // if Depreciation Method is either Monthly or on the Actual Date
                                if (newjobj.has("frommonth")) {
                                    yearval += newjobj.getDouble("firstperiodamtInBase");
                                }
                            }
                        }
                        depreciation_current += yearval;

                        double accdepreciation = accopeningbal + yearval;
                        if (disposalDate != null && (disposalDate.after(startdt) || disposalDate.equals(startdt)) && (disposalDate.before(enddt) || disposalDate.equals(enddt))) {
//                            if(ad.isIsDisposed()){
//                                cost_disposal += authHandler.round(ad.getDisposalProfitLoss(), 2);
//                            }else if(ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO){
//                                cost_disposal += ad.getCost() - accdepreciation;
//                            }
                            if (ad.isIsDisposed() || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO) {
                                cost_disposal += ad.getCost();//If disposed asset then display asset cost in Cost-Disposal
                                depreciation_disposals += accdepreciation;//If disposed asset then Depreciation-Disposal is addition of Depreciation-Opening Balance & Depreciation-Current Dep
                            }
                        }
                    }
                }

                jSONObject.put("cost_openingbalance", cost_assetValue);
                jSONObject.put("cost_addtions", cost_additions);
                jSONObject.put("cost_disposals", cost_disposal);
                cost_closingbalance = cost_assetValue + cost_additions - cost_disposal;
                jSONObject.put("cost_closingbalance", authHandler.round(cost_closingbalance, companyId));

                jSONObject.put("depreciation_openingbalance", authHandler.round(depreciation_openingbalance, companyId));
                jSONObject.put("depreciation_current", authHandler.round(depreciation_current, companyId));
                jSONObject.put("depreciation_disposals", authHandler.round(depreciation_disposals, companyId));
                depreciation_closingbalance = depreciation_openingbalance + depreciation_current - depreciation_disposals;
                jSONObject.put("depreciation_closingbalance", authHandler.round(depreciation_closingbalance, companyId));

                nbv = authHandler.round(cost_closingbalance, companyId) - authHandler.round(depreciation_closingbalance, companyId);
                jSONObject.put("nbv", authHandler.round(nbv, companyId));

                dataJArr.put(jSONObject);
            }

        } catch (JSONException | ParseException | ServiceException ex) {
            Logger.getLogger(AccAssetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataJArr;
    }

    public JSONObject getAssetSummeryReportGridInfo(JSONObject requestParams) throws JSONException, ServiceException {
        int colWidth = 150;
        int colWidth_180 = 180;
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        Locale requestcontextutilsobj = null;
        String companyid = requestParams.optString("companyid", null);
        KwlReturnObject excap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) excap.getEntityList().get(0);
        if (requestParams.has("locale")) {
            requestcontextutilsobj = (Locale) requestParams.get("locale");
        }
        String countryId = "";
        if (extraCompanyPreferences != null) {
            countryId = extraCompanyPreferences.getCompany().getCountry().getID();
        }
        String StoreRec = "assetgroupname,assetgroupid,cost_openingbalance,cost_addtions,cost_disposals,cost_closingbalance,depreciation_openingbalance,depreciation_current,depreciation_disposals,depreciation_closingbalance,nbv";

        String[] recArr = StoreRec.split(",");
        for (String rec : recArr) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", rec);
            jarrRecords.put(jobjTemp);
        }

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header1", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "assetgroupname");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header2", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "assetgroupid");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header3", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "cost_openingbalance");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        if (!StringUtil.isNullOrEmpty(countryId) && Integer.parseInt(countryId) == Constants.indian_country_id) {
            if (extraCompanyPreferences.isActivelandingcostofitem()) {
                KwlReturnObject landingCostOfRetObj = accMasterItemsDAOobj.getMasterItemFromLandingCostCategory(null, companyid);
                List list = landingCostOfRetObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    LandingCostCategory lcc = (LandingCostCategory) itr.next();
                    jobjTemp = new JSONObject();
                    jobjTemp.put("name", lcc.getId());
                    jarrRecords.put(jobjTemp);
                    jobjTemp = new JSONObject();
                    jobjTemp.put("dataIndex", lcc.getId());
                    jobjTemp.put("header", lcc.getLccName() + " ( " + LandingCostAllocationType.getByValue(lcc.getLcallocationid()) + " )");
                    jobjTemp.put("width", "150");
                    jobjTemp.put("pdfwidth", "75");
                    jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                    jarrColumns.put(jobjTemp);
                }
                jobjTemp = new JSONObject();
                jobjTemp.put("name", "assetvaluewithoutlandedcost");
                jarrRecords.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("dataIndex", "assetvaluewithoutlandedcost");
                jobjTemp.put("header", "Asset Value Without Landed Cost");
                jobjTemp.put("width", "150");
                jobjTemp.put("pdfwidth", "75");
                jobjTemp.put("renderer", "WtfGlobal.withoutRateCurrencyDeletedSymbol");
                jarrColumns.put(jobjTemp);
            }
        }

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header4", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "cost_addtions");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth + 35);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header5", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "cost_disposals");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth + 30);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header6", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "cost_closingbalance");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth + 10);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header7", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "depreciation_openingbalance");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth_180);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header8", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "depreciation_current");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header9", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "depreciation_disposals");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header10", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "depreciation_closingbalance");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth_180);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.fixedassetsummeryreport.header11", null, requestcontextutilsobj));
        jobjTemp.put("dataIndex", "nbv");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jMeta.put("totalProperty", "totalCount");
        jMeta.put("root", "coldata");
        jMeta.put("fields", jarrRecords);
        commData.put("columns", jarrColumns);
        commData.put("metadata", jMeta);
        return commData;
    }

    @Override
    public JSONObject exportAssetSummary(JSONObject paramJobj) throws ServiceException {

        JSONObject jobj = new JSONObject();
        JSONObject objData = new JSONObject();
        JSONObject objData1 = new JSONObject();
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        try {
            boolean isExport = true;
            paramJobj.put("isExport", isExport);
            objData = getAssetSummeryReportDetails(paramJobj);
            objData1 = objData.getJSONObject("data");
            jobj.put("data", objData1.getJSONArray("coldata"));
            request = (HttpServletRequest) paramJobj.get("request");
            response = (HttpServletResponse) paramJobj.get("response");
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            Logger.getLogger(AccAssetServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    @Override
    public JSONArray getAssetDepreciation(Map<String, Object> request) throws SessionExpiredException, AccountingException, JSONException {
        JSONArray finalJArr = new JSONArray();
        try {
            boolean excludeSoldAssets = false;
            boolean isGenerateAssetDepreciation = false;
            String companyId = request.get("companyid") != null ? (String) request.get("companyid") : "";
            if (request.get("excludeSoldAssets") != null) {
                excludeSoldAssets = (Boolean) request.get("excludeSoldAssets");
            }
            if (request.containsKey("isGenerateAssetDepreciation") && request.get("isGenerateAssetDepreciation") != null) {
                isGenerateAssetDepreciation = (Boolean) request.get("isGenerateAssetDepreciation");
            }
            KwlReturnObject excap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) excap.getEntityList().get(0);
            List<AssetDetails> assetdetailList = new ArrayList();
            String assetdetailIds = request.get("assetdetailIds") != null ? (String) request.get("assetdetailIds") : "All";
            if (assetdetailIds.indexOf("All") >= 0) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyId", companyId);
                requestParams.put("invrecord", true);

                if (excludeSoldAssets) {
                    requestParams.put("excludeSoldAsset", excludeSoldAssets);
                }
                if (request.get("assetGroupIds") != null && !StringUtil.isNullOrEmpty(request.get("assetGroupIds").toString())) {
                    String[] assetGroupIds = (String[]) request.get("assetGroupIds");
                    requestParams.put("assetGroupIds", assetGroupIds);
                }

                KwlReturnObject result = accProductObj.getAssetDetails(requestParams);
                assetdetailList = result.getEntityList();
            } else {
                List<String> assetdetailIdsList = Arrays.asList(assetdetailIds.split("\\s*,\\s*"));
                for (int i = 0; i < assetdetailIdsList.size(); i++) {
                    String assetdetailId = assetdetailIdsList.get(i);
                    KwlReturnObject accresult = accountingHandlerDAOobj.getObject(AssetDetails.class.getName(), assetdetailId);
                    AssetDetails ad = (AssetDetails) accresult.getEntityList().get(0);
                    assetdetailList.add(ad);
                }
            }
            if (isGenerateAssetDepreciation) {
                request.put("isGenerateAssetDepreciation", true);
            }
            for (AssetDetails ad : assetdetailList) {
                int depreciationMethod = ad.getProduct().getDepreciationMethod();
                if (depreciationMethod == 1) {
                    getAssetStraightLineDepreciation(request, ad, finalJArr, extraCompanyPreferences);
                } else if (depreciationMethod == 2) {
                    getDoubleDeclineDepreciation(request, ad, finalJArr, extraCompanyPreferences);
                } else if (depreciationMethod == 4) { // WDV- Written Down Value
                    getDoubleDeclineDepreciation(request, ad, finalJArr, extraCompanyPreferences);
                } else {
                    continue;
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccAssetServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return finalJArr;
    }

    @Override
    public void getAssetStraightLineDepreciation(Map<String, Object> request, AssetDetails ad, JSONArray finalJArr, ExtraCompanyPreferences extraCompanyPreferences) throws SessionExpiredException, AccountingException {
        try {
            Date creationDate = ad.getInstallationDate();
            int postOption = request.get("postOption") != null ? (Integer) request.get("postOption") : 0;
            boolean isUnpost = request.get("isUnpost") != null ? (Boolean) request.get("isUnpost") : false;
            boolean isDepreciationDetailReport = request.get("isDepreciationDetailReport") != null ? (Boolean) request.get("isDepreciationDetailReport") : false;
            boolean isFixedAssetSummaryReport = request.get("isFixedAssetSummaryReport") != null ? (Boolean) request.get("isFixedAssetSummaryReport") : false;
            boolean isFixedAssetDetailReport = request.get("isFixedAssetDetailReport") != null ? (Boolean) request.get("isFixedAssetDetailReport") : false;
            boolean isMonthsWiseDetails = request.get("isMonthsWiseDetails") != null ? (Boolean) request.get("isMonthsWiseDetails") : false;
            boolean isGenerateAssetDepreciation = false;
            Date startdate = request.get(Constants.REQ_startdate) != null ? (Date) request.get(Constants.REQ_startdate) : null;
            boolean isForDisposeAsset = request.get("isForDisposeAsset") != null ? (Boolean) request.get("isForDisposeAsset") : false;
            int depreciationCalculationType = request.get("depreciationCalculationType") != null ? (Integer) request.get("depreciationCalculationType") : 0;
            int startMonth = request.get("startMonth") != null ? (Integer) request.get("startMonth") : 0;
            int endMonth = request.get("endMonth") != null ? (Integer) request.get("endMonth") : 11;
            String years = request.get("years") != null ? (String) request.get("years") : "";
            String companyid = request.get("companyid") != null ? (String) request.get("companyid") : "";
            DateFormat sdf = authHandler.getGlobalDateFormat();
            List<String> yearList = Arrays.asList(years.split("\\s*,\\s*"));
            Collections.sort(yearList);
            Calendar startcal = Calendar.getInstance();
            Calendar endcal = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();
            double totalLandingCost = 0;
            String countryId = "";
            if (extraCompanyPreferences != null) {
                countryId = extraCompanyPreferences.getCompany().getCountry().getID();
            }
            if (!StringUtil.isNullOrEmpty(countryId) && Integer.parseInt(countryId) == Constants.indian_country_id) {
                if (extraCompanyPreferences.isActivelandingcostofitem()) {
                    HashMap<String, Object> requestMap = new HashMap<>();
                    requestMap.put("companyId", companyid);
                    requestMap.put("assetDetailId", ad.getId());
                    List normallist = accProductObj.getPurchaseInvoiceNoAndVendorOfAssetPurchaseInvoice(requestMap);
                    Iterator normalit = normallist.iterator();
                    if (normallist.size() <= 0) {//If Asset GR is linked in Asset PI
                        String assetDetailId = "";
                        requestMap.put("assetid", ad.getAssetId());
                        KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                        List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                        for (AssetDetails assetDetailObj : assetDetaillist) {
                            assetDetailId += assetDetailObj.getId() + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(assetDetailId)) {
                            assetDetailId = assetDetailId.substring(0, assetDetailId.length() - 1);
                            requestMap.put("assetDetailId", assetDetailId);
                            normallist = accProductObj.getPIDetails_AssetGRLinkedInAssetPI(requestMap);
                            normalit = normallist.iterator();
                        }
                    }
                    String invoiceid = "", invoiceDetailID = "";
                    while (normalit.hasNext()) {
                        Object obj[] = (Object[]) normalit.next();
                        invoiceid = (String) obj[5];
                        invoiceDetailID = (String) obj[6];
                    }
                    if (!StringUtil.isNullOrEmpty(invoiceDetailID) && !StringUtil.isNullOrEmpty(invoiceid)) {
                        Set<LandingCostCategory> lccSet = ad.getProduct().getLccategoryid();
                        String invoiceAssetDetailId = "";
                        requestMap = new HashMap<>();
                        requestMap.put("assetid", ad.getAssetId());
                        requestMap.put("companyId", companyid);
                        KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                        List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                        for (AssetDetails assetDetailObj : assetDetaillist) {
                            invoiceAssetDetailId = assetDetailObj.getId();
                        }
                        HashMap<String, Object> globalParams = new HashMap<String, Object>();
                        requestMap.put(Constants.companyKey, companyid);
                        requestMap.put(Constants.globalCurrencyKey, extraCompanyPreferences.getCompany().getCurrency().getCurrencyID());
                        requestMap.put(Constants.df, authHandler.getDateOnlyFormat());
                        if (!StringUtil.isNullOrEmpty(invoiceAssetDetailId)) {
                            totalLandingCost = getTotalLandedCost(lccSet, invoiceid, globalParams, invoiceDetailID, invoiceAssetDetailId, companyid, new JSONObject());
                        }
                    }
                }
            }
            double openingbalance = ad.getCost() + totalLandingCost;
            double balance = openingbalance;
            double currentAssetVal = 0;
            String backyears = "";
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(creationDate);
            int currentyear = Integer.parseInt(yearList.get(0));
            int creationyear1 = cal1.get(Calendar.YEAR);
            boolean isrepeat = request.get("isrepeat") != null ? (Boolean) request.get("isrepeat") : false;
            Date finanDate = request.get("finanDate") != null ? (Date) request.get("finanDate") : null;
            int financialStartMonth = request.get("financialStartMonth") != null ? Integer.parseInt(request.get("financialStartMonth").toString()) : 0;
            if (request.containsKey("isGenerateAssetDepreciation") && request.get("isGenerateAssetDepreciation") != null) {
                isGenerateAssetDepreciation = (Boolean) request.get("isGenerateAssetDepreciation");
            }
            if (!isrepeat && creationyear1 < currentyear) {
                request.put("actualStartMonth", startMonth);
                request.put("actualEndMonth", endMonth);
                request.put("actualYears", years);
                while (creationyear1 < currentyear) {
                    backyears += creationyear1 + ",";
                    creationyear1++;
                }
                if (depreciationCalculationType != 0) {
                    if (finanDate != null) {  // in case of the Depreciation Details report according to financial year we need to add current year
                        if (finanDate.getMonth() != 0) {
                            backyears += creationyear1;
                        }
                    } else if (startMonth != 0 && isDepreciationDetailReport && depreciationCalculationType != 0) {//Do not put extra year for month wise and date wise.SDP-12870
                        backyears = backyears;
                    } else if (startMonth != 0) {
                        backyears += creationyear1;
                    }
                }
                JSONArray curfinalJArr = new JSONArray();
                request.put("startMonth", creationDate.getMonth());
                request.put("endMonth", finanDate != null ? finanDate.getMonth() - 1 : (startMonth != 0 ? startMonth - 1 : endMonth));
                request.put("years", backyears);
                request.put("isrepeat", true);
                getAssetStraightLineDepreciation(request, ad, curfinalJArr, extraCompanyPreferences);
                for (int i = 0; i < curfinalJArr.length(); i++) {
                    JSONObject newjobj = curfinalJArr.getJSONObject(i);
                    if (newjobj.has("firstperiodamtInBase")) {
                        currentAssetVal += newjobj.getDouble("firstperiodamtInBase");
                    }
                }
                request.remove("isrepeat");
                balance = ad.getCost() + totalLandingCost - currentAssetVal;
                startMonth = (int) request.get("actualStartMonth");
                endMonth = (int) request.get("actualEndMonth");
                years = (String) request.get("actualYears");
            } else {
                request.put("startMonth", request.get("actualStartMonth") != null ? request.get("actualStartMonth") : request.get("startMonth"));
                request.put("endMonth", request.get("actualEndMonth") != null ? request.get("actualEndMonth") : request.get("endMonth"));
                request.put("years", request.get("actualYears") != null ? request.get("actualYears") : request.get("years"));
            }
            double life = ad.getAssetLife();
            double salvage = ad.getSalvageValue();
            double periodDepreciation = calMonthwiseDepreciation(openingbalance, salvage, life * 12);
            boolean isOpening = request.get("isOpening") != null ? (Boolean) request.get("isOpening") : false;
            if (!isOpening && (depreciationCalculationType != 0)) { //If we post periodic depreciation and setting is yearwise depreciation then don't round off here.SDP-7503
                periodDepreciation = authHandler.round(periodDepreciation, companyid);
            }
            double accDepreciation = currentAssetVal;
            cal.setTime(creationDate);
            double firstPeriodAmt = periodDepreciation;
            int tempStartMonth = startdate != null ? startdate.getMonth() : startMonth;
            int tempEndMonth = endMonth;
            boolean IsRepeat = (request.containsKey("isrepeat") && request.get("isrepeat") != null) ? Boolean.parseBoolean(request.get("isrepeat").toString()) : false;
            if (depreciationCalculationType == 0) { //Year Bases
                periodDepreciation = periodDepreciation * 12;
                if (!isOpening) {
                    periodDepreciation = authHandler.round(periodDepreciation, 2);
                }
                firstPeriodAmt = periodDepreciation;
                for (int yeardiff = 0, k = 0; k < yearList.size(); k++, yeardiff++) {  // Varialbe  k is used to take year from year List And Variable j is used to set the year in date of depreciation period
                    int year = Integer.parseInt(yearList.get(k));
                    int creationyear = cal.get(Calendar.YEAR);
                    yeardiff = year - creationyear;
                    if (yeardiff < 0) { // if selected year is less than creation year then there is no depreciation to show
                        continue;
                    }
                    int period = yeardiff + 1;
                    accDepreciation += periodDepreciation;
                    balance -= periodDepreciation;
                    if (balance - salvage < -0.01 || period > life) { //ERP-31503
                        break;
                    }
                    if ((balance - periodDepreciation - salvage < 0) && (period == life)) {
                        periodDepreciation += balance - salvage;
                        accDepreciation += balance - salvage;
                        balance = salvage;
                    }
                    JSONObject finalObj = new JSONObject();
                    startcal.setTime(creationDate);
                    endcal.setTime(creationDate);
                    startcal.add(Calendar.YEAR, yeardiff);
                    endcal.add(Calendar.YEAR, yeardiff);
                    startcal.set(Calendar.MONTH, 0);
                    endcal.set(Calendar.MONTH, 0);
                    startcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                    endcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                    startcal.set(Calendar.HOUR, 0);
                    startcal.set(Calendar.MINUTE, 0);
                    startcal.set(Calendar.SECOND, 0);
                    endcal.set(Calendar.HOUR, 0);
                    endcal.set(Calendar.MINUTE, 0);
                    endcal.set(Calendar.SECOND, 0);

                    HashMap<String, Object> filters = new HashMap<String, Object>();
                    filters.put("period", period);
                    filters.put("assetDetailsId", ad.getId());
                    filters.put("companyid", companyid);
                    filters.put("assetDetails", true);

                    KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                    Iterator itrcust = dresult.getEntityList().iterator();

                    if (itrcust.hasNext()) {
                        AssetDepreciationDetail dd = (AssetDepreciationDetail) itrcust.next();
                        if (dd.getJournalEntry() == null || isForDisposeAsset) {
                            accDepreciation -= periodDepreciation;//substract the period depreciation because it is round off to get previous actual accumulated depreciation.
                            balance += periodDepreciation;//Add the period depreciation because it is round off to get previous actual balance
                            firstPeriodAmt = dd.getPeriodAmount();
                            accDepreciation += dd.getPeriodAmount();//Add  the actual period depreciation because it is not round off to get exact value saved in DB
                            balance -= dd.getPeriodAmount();//substract  the actual period depreciation because it is not round off to get exact value saved in DB
                        } else {
                            accDepreciation = authHandler.round(accDepreciation, companyid);//Round off the value for periodic depreciation
                            balance = authHandler.round(balance, companyid);//Round off the value for periodic depreciation
                            firstPeriodAmt = dd.getPeriodAmount();
                        }
                        finalObj.put("perioddepreciation", dd.getPeriodAmount());
                        finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                        finalObj.put("netbookvalue", dd.getNetBookValue());
                        finalObj.put("assetId", dd.getAssetDetails().getAssetId());
                        double netbookvalueInBase = dd.getNetBookValue();
                        double accdepreciationInBase = dd.getAccumulatedAmount();
                        finalObj.put("netbookvalueInBase", netbookvalueInBase);
                        finalObj.put("accdepreciationInBase", accdepreciationInBase);
                        finalObj.put("isje", true);
                        finalObj.put("status", "Posted");
                        finalObj.put("depdetailid", dd.getID());
                        finalObj.put("jeid", dd.getJournalEntry() != null ? dd.getJournalEntry().getID() : "");
                        finalObj.put("jeno", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryNumber() : "");
                        finalObj.put("jedate", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryDate() : "");
                        finalObj.put("disposed", false);
                        if (ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO) {
                            finalObj.put("disposed", true);
                        }
                    } else {
                        if ((isUnpost || ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO || isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && !isrepeat) {
                            continue;
                        } else {
                            finalObj.put("perioddepreciation", periodDepreciation);
                            double accdepreciationInBase = accDepreciation;
                            finalObj.put("accdepreciation", accDepreciation);
                            finalObj.put("netbookvalue", balance);
                            finalObj.put("assetId", ad.getAssetId());
                            double netbookvalueInBase = balance;
                            finalObj.put("netbookvalueInBase", netbookvalueInBase);
                            finalObj.put("accdepreciationInBase", accdepreciationInBase);
                            finalObj.put("isje", false);
                            finalObj.put("status", "Not Posted");
                            finalObj.put("depdetailid", "");
                        }
                    }
                    finalObj.put("period", period);
                    finalObj.put("firstperiodamt", firstPeriodAmt);
                    double firstperiodamtInBase = firstPeriodAmt;
                    finalObj.put("firstperiodamtInBase", firstperiodamtInBase);
                    finalObj.put("fromyear", startcal.get(Calendar.YEAR));
                    finalObj.put("toyear", endcal.get(Calendar.YEAR));
                    finalObj.put("assetDetailsId", ad.getId());
                    finalObj.put("assetGroupId", ad.getProduct().getID());

                    boolean differentPostOption = false;
                    if ((postOption == 1 && ad.getPostOption() == 2) || (postOption == 2 && ad.getPostOption() == 1)) { // if Client has already posted Depreciation with monthly but now going to post with Yearly
                        differentPostOption = true;
                        if (postOption == 1 && ad.getPostOption() == 2) {
                            finalObj.put("status", "Posted By Monthly Method");
                        } else {
                            finalObj.put("status", "Posted By Yearly Method");
                        }
                    }
                    finalObj.put("differentPostOption", differentPostOption);

                    Calendar calCurrentYear = Calendar.getInstance();
                    calCurrentYear.setTime(new Date());
                    finalObj.put("isFuture", startcal.get(Calendar.YEAR) > (calCurrentYear.get(Calendar.YEAR)) ? true : false);
                    finalObj.put("isQuantityAvailableForDepreciation", !(ad.isPurchaseReturn() || (ad.getAssetSoldFlag() == 2)));
                    if (!(ad.isPurchaseReturn())) {
                        finalJArr.put(finalObj);
                    }

                }
            } else if (depreciationCalculationType == 1) { //Actual Date
                double tempStoreperiodDepreciation = 0;
                boolean nextYear = false; // Variable used to compare the current year either start or end 
                for (int yeardiff = 0, k = 0; k < yearList.size(); k++, yeardiff++) {  // Varialbe  k is used to take year from year List And Variable j is used to set the year in date of depreciation period
                    int year = Integer.parseInt(yearList.get(k));   //take the current year from the yearlist
                    int creationyear = cal.get(Calendar.YEAR);  // take creation year from the installation date
                    yeardiff = year - creationyear;  // calculate the difference between selectd year and the creation year
                    if (yeardiff < 0) { // if selected year is less than creation year then there is no depreciation to show
                        continue;
                    }
                    if (creationyear == year) {  //if creation year same as that of selected year 
                        if (startMonth != endMonth && yearList.size() > 1) {
                            if ((startMonth < creationDate.getMonth() && Integer.parseInt(yearList.get(0)) == year) || (startMonth > creationDate.getMonth() && Integer.parseInt(yearList.get(1)) == year)) {
                                startMonth = creationDate.getMonth();
                            }
                        }
                    } else if (year < creationyear) {  // if selected year is less than the creation year then we can start directly from the creation month
                        startMonth = creationDate.getMonth();
                    } else {
                        if (finanDate != null) {  // Finan date comes only when we select the depreciation based on the financial year 
                            if (nextYear) {   // if current year comes as second year then start from Jan
                                startMonth = 0;
                            } else {  // if current year comes as first then start from finacial year month
                                startMonth = finanDate.getMonth();
                            }
                        } else if (isrepeat) {  // use in normal case to generate Depreciation in Generate Asset Depreciation Report
                            startMonth = 0;
                        }
                    }
                    nextYear = true; // consider current year as next year 
                    int endmonth = 12;
                    if ((isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && yeardiff > 0 && yearList.size() != 1) {
                        startMonth = 0;
                    }
                    if (isMonthsWiseDetails) {
                        if (yeardiff > 0 && yearList.size() != 1) {
                            startMonth = 0;
                        }
                        if (!IsRepeat && k == 0) {
                            startMonth = tempStartMonth;
                        }
                        
                        if (!isDepreciationDetailReport) {
                            if (yearList.size() > 1 && k == yearList.size() - 1 && financialStartMonth != 0) {
                                endmonth = tempStartMonth;
                            }
                        }
                    }
                    if (isGenerateAssetDepreciation) {
                        if (!IsRepeat) {
                            endmonth = tempEndMonth + 1;
                        }
                    }
                    /**
                     * get all depreciation date from o month and upto 11th
                     * month and then we are filtering this dtata according to
                     * date filetr in AccproductServiceImpl.java.
                     */
                    if (isDepreciationDetailReport) {
                        startMonth = 0;
                        endMonth = endmonth;//ERP-38234
                    }
                    for (int i = startMonth; i < endmonth; i++) {
                        if (i == endMonth + 1 && Integer.parseInt(yearList.get(yearList.size() - 1)) == year) {
                            break;
                        } else if (i < cal.get(Calendar.MONTH) && year == cal.get(Calendar.YEAR)) {
                            continue;
                        }
                        int period = (12 * yeardiff) + i + 1;
                        if (cal.get(Calendar.MONTH) == i && year == cal.get(Calendar.YEAR)) {
                            tempStoreperiodDepreciation = periodDepreciation;
                            int noofdaysinmonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                            double periodDepreciationPerDay = periodDepreciation / noofdaysinmonth;
                            LocalDate localCreationDate = new LocalDate(creationDate);
                            DateTime date = localCreationDate.toDateTime(LocalTime.MIDNIGHT);
                            DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                            int days = (Days.daysBetween(new DateTime(creationDate), new DateTime(lastDateOfMonth)).getDays()) + 1; // get difference in days
                            periodDepreciation = periodDepreciationPerDay * days;
                        }
                        accDepreciation += periodDepreciation;
                        balance -= periodDepreciation;
                        if (period > ((life * 12) + cal.get(Calendar.MONTH))) {
                            if (balance < 0 && ((cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period) == Math.ceil(life * 12))) {
                                double remainingDep = balance;
                                accDepreciation -= periodDepreciation;
                                balance += periodDepreciation;
                                periodDepreciation = (periodDepreciation + remainingDep);
                                accDepreciation += periodDepreciation;
                                balance -= periodDepreciation;
                            } else {
                                break;
                            }
                        }
                        if ((balance - periodDepreciation - salvage < 0) && (period == ((life * 12) + cal.get(Calendar.MONTH)))) {
                            periodDepreciation += balance - salvage;
                            /*
                             if opening depreciation then don't round of the period depreciation value.
                             changes done in : SDP-9734
                             */
                            if (!isOpening) {
                                periodDepreciation = authHandler.round(periodDepreciation, companyid);
                            }
                            accDepreciation += balance - salvage;
                            balance = salvage;
                        }
                        firstPeriodAmt = periodDepreciation;
                        JSONObject finalObj = new JSONObject();
                        startcal.setTime(creationDate);
                        endcal.setTime(creationDate);
                        startcal.add(Calendar.YEAR, yeardiff);
                        endcal.add(Calendar.YEAR, yeardiff);
                        startcal.set(Calendar.MONTH, i);
                        endcal.set(Calendar.MONTH, i + 1);
                        startcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        endcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        startcal.set(Calendar.HOUR, 0);
                        startcal.set(Calendar.MINUTE, 0);
                        startcal.set(Calendar.SECOND, 0);
                        endcal.set(Calendar.HOUR, 0);
                        endcal.set(Calendar.MINUTE, 0);
                        endcal.set(Calendar.SECOND, 0);

                        HashMap<String, Object> filters = new HashMap<String, Object>();
                        filters.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        filters.put("assetDetailsId", ad.getId());
                        filters.put("companyid", companyid);
                        filters.put("assetDetails", true);

                        KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                        Iterator itrcust = dresult.getEntityList().iterator();

                        if (itrcust.hasNext()) {
                            AssetDepreciationDetail dd = (AssetDepreciationDetail) itrcust.next();
                            if (dd.getJournalEntry() == null || isForDisposeAsset) {
                                accDepreciation -= periodDepreciation;//substract the period depreciation because it is round off to get previous actual accumulated depreciation.
                                balance += periodDepreciation;//Add the period depreciation because it is round off to get previous actual balance
                                firstPeriodAmt = dd.getPeriodAmount();
                                accDepreciation += dd.getPeriodAmount();//Add  the actual period depreciation because it is not round off to get exact value saved in DB
                                balance -= dd.getPeriodAmount();//substract  the actual period depreciation because it is not round off to get exact value saved in DB
                            } else {
                                accDepreciation = authHandler.round(accDepreciation, companyid);//Round off the value for periodic depreciation
                                balance = authHandler.round(balance, companyid);//Round off the value for periodic depreciation
                            }
                            finalObj.put("perioddepreciation", dd.getPeriodAmount());
                            finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                            finalObj.put("netbookvalue", dd.getNetBookValue());
                            finalObj.put("assetId", dd.getAssetDetails().getAssetId());
                            double netbookvalueInBase = dd.getNetBookValue();
                            double accdepreciationInBase = dd.getAccumulatedAmount();
                            finalObj.put("netbookvalueInBase", netbookvalueInBase);
                            finalObj.put("accdepreciationInBase", accdepreciationInBase);
                            finalObj.put("isje", true);
                            finalObj.put("status", "Posted");
                            finalObj.put("depdetailid", dd.getID());
                            finalObj.put("jeid", dd.getJournalEntry() != null ? dd.getJournalEntry().getID() : "");
                            finalObj.put("jeno", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryNumber() : "");
                            finalObj.put("jedate", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryDate() : "");
                            finalObj.put("disposed", false);
                            if (ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO) {
                                finalObj.put("disposed", true);
                            }
                        } else {
                            if ((isUnpost || ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO || isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && !isrepeat) {
                                continue;
                            } else {
                                finalObj.put("perioddepreciation", periodDepreciation);
                                double accdepreciationInBase = accDepreciation;
                                finalObj.put("accdepreciation", accDepreciation);
                                finalObj.put("netbookvalue", balance);
                                finalObj.put("assetId", ad.getAssetId());
                                double netbookvalueInBase = balance;
                                finalObj.put("netbookvalueInBase", netbookvalueInBase);
                                finalObj.put("accdepreciationInBase", accdepreciationInBase);
                                finalObj.put("isje", false);
                                finalObj.put("status", "Not Posted");
                                finalObj.put("depdetailid", "");
                            }
                        }
                        finalObj.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        finalObj.put("firstperiodamt", firstPeriodAmt);
                        double firstperiodamtInBase = firstPeriodAmt;
                        finalObj.put("firstperiodamtInBase", firstperiodamtInBase);
                        finalObj.put("frommonth", sdf.format(startcal.getTime()));
                        finalObj.put("tomonth", sdf.format(endcal.getTime()));
                        finalObj.put("assetDetailsId", ad.getId());
                        finalObj.put("assetGroupId", ad.getProduct().getID());
                        finalObj.put("differentPostOption", false);
                        finalObj.put("year", year);

                        LocalDate localStartDate = new LocalDate(startcal.getTime());
                        DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                        DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();

                        finalObj.put("isFuture", new DateTime(firstDateOfMonth).toDate().after(new Date()) ? true : false);
                        finalObj.put("isQuantityAvailableForDepreciation", !(ad.isPurchaseReturn() || (ad.getAssetSoldFlag() == 2)));
                        if (!(ad.isPurchaseReturn())) {
                            finalJArr.put(finalObj);
                        }
                        if (cal.get(Calendar.MONTH) == i && year == cal.get(Calendar.YEAR)) {
                            periodDepreciation = tempStoreperiodDepreciation;
                        }
                    }
                }
            } else { //Actual Month       
                boolean nextYear = false; // Variable used to compare the current year either start or end 
//                System.out.println("Year, startMonth, endMonth, endmonth,i, period, periodDepreciation, accDepreciation, balance,years");
                for (int yeardiff = 0, k = 0; k < yearList.size(); k++, yeardiff++) {  // Varialbe  k is used to take year from year List And Variable j is used to set the year in date of depreciation period
                    int year = Integer.parseInt(yearList.get(k));   //take the current year from the yearlist
                    int creationyear = cal.get(Calendar.YEAR);  // take creation year from the installation date
                    yeardiff = year - creationyear;  // calculate the difference between selectd year and the creation year
                    if (yeardiff < 0) { // if selected year is less than creation year then there is no depreciation to show
                        continue;
                    }
                    if (creationyear == year) {  //if creation year same as that of selected year 
                        if (startMonth != endMonth && yearList.size() > 1) {
                            if ((startMonth < creationDate.getMonth() && Integer.parseInt(yearList.get(0)) == year) || (startMonth > creationDate.getMonth() && Integer.parseInt(yearList.get(1)) == year)) {
                                startMonth = creationDate.getMonth();
                            }
                        }
                    } else if (year < creationyear) {  // if selected year is less than the creation year then we can start directly from the creation month
                        startMonth = creationDate.getMonth();
                    } else {
                        if (finanDate != null) {  // Finan date comes only when we select the depreciation based on the financial year 
                            if (nextYear) {   // if current year comes as second year then start from Jan
                                startMonth = 0;
                            } else {  // if current year comes as first then start from finacial year month
                                startMonth = finanDate.getMonth();
                            }
                        } else if (isrepeat) {  // use in normal case to generate Depreciation in Generate Asset Depreciation Report
                            startMonth = 0;
                        }
                    }
                    nextYear = true; // consider current year as next year 
                    int endmonth = 12;
                    if ((isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && yeardiff > 0 && yearList.size() != 1) {
                        startMonth = 0;
                    }
                    if (isMonthsWiseDetails) {
                        if (yeardiff > 0 && yearList.size() != 1) {
                            startMonth = 0;
                        }
                        if (!IsRepeat && k == 0) {
                            startMonth = tempStartMonth;
                        }
                        if (!isDepreciationDetailReport) {
                            if (yearList.size() > 1 && k == yearList.size() - 1 && financialStartMonth != 0) {
                                endmonth = tempStartMonth;
                            }
                        }
                    }
                    if (isGenerateAssetDepreciation) {
                        if (!IsRepeat) {
                            endmonth = tempEndMonth + 1;
                        }
                    }
                    /**
                     * get all depreciation date from o month and upto 11th
                     * month and then we are filtering this dtata according to
                     * date filetr in AccproductServiceImpl.java.
                     */
                    if (isDepreciationDetailReport) {
                        startMonth = 0;
                        endMonth = endmonth;//ERP-38234
                    }
                    for (int i = startMonth; i < endmonth; i++) {
                        if (i == endMonth + 1 && Integer.parseInt(yearList.get(yearList.size() - 1)) == year) {
                            break;
                        } else if (i < cal.get(Calendar.MONTH) && year == cal.get(Calendar.YEAR)) {
                            continue;
                        }
                        int period = (12 * yeardiff) + i + 1;
                        accDepreciation += periodDepreciation;
                        balance -= periodDepreciation;
//                        System.out.println(year +", "+ startMonth +", "+endMonth +", "+endmonth +", "+i+", "+ period +", "+ periodDepreciation +", "+ accDepreciation +", "+ balance+", "+years);
                        if (period > ((life * 12) + cal.get(Calendar.MONTH))) {
                            if (balance < 0 && ((cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period) == Math.ceil(life * 12))) {
                                double remainingDep = balance;
                                accDepreciation -= periodDepreciation;
                                balance += periodDepreciation;
                                periodDepreciation = (periodDepreciation + remainingDep);
                                accDepreciation += periodDepreciation;
                                balance -= periodDepreciation;
                            } else {
                                break;
                            }
                        }
                        if ((balance - periodDepreciation - salvage < 0) && (period == ((life * 12) + cal.get(Calendar.MONTH)))) {
                            periodDepreciation += balance - salvage;
                            /*
                             if opening depreciation then don't round of the period depreciation value.
                             changes done in : SDP-9734
                             */
                            if (!isOpening) {
                                periodDepreciation = authHandler.round(periodDepreciation, companyid);
                            }
                            accDepreciation += balance - salvage;
                            balance = salvage;
//                            System.out.println(year +", "+ startMonth +", "+endMonth +", "+endmonth +", "+i+", "+ period +", "+ periodDepreciation +", "+ accDepreciation +", "+ balance+", "+years);
                        }
                        firstPeriodAmt = periodDepreciation;
                        JSONObject finalObj = new JSONObject();
                        startcal.setTime(creationDate);
                        endcal.setTime(creationDate);
                        startcal.add(Calendar.YEAR, yeardiff);
                        endcal.add(Calendar.YEAR, yeardiff);
                        startcal.set(Calendar.MONTH, i);
                        endcal.set(Calendar.MONTH, i + 1);
                        startcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        endcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        startcal.set(Calendar.HOUR, 0);
                        startcal.set(Calendar.MINUTE, 0);
                        startcal.set(Calendar.SECOND, 0);
                        endcal.set(Calendar.HOUR, 0);
                        endcal.set(Calendar.MINUTE, 0);
                        endcal.set(Calendar.SECOND, 0);

                        HashMap<String, Object> filters = new HashMap<String, Object>();
                        filters.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        filters.put("assetDetailsId", ad.getId());
                        filters.put("companyid", companyid);
                        filters.put("assetDetails", true);

                        KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                        Iterator itrcust = dresult.getEntityList().iterator();
                        if (itrcust.hasNext()) {
                            AssetDepreciationDetail dd = (AssetDepreciationDetail) itrcust.next();
                            if (dd.getJournalEntry() == null || isForDisposeAsset) {
                                accDepreciation -= periodDepreciation;//substract the period depreciation because it is round off to get previous actual accumulated depreciation.
                                balance += periodDepreciation;//Add the period depreciation because it is round off to get previous actual balance
                                firstPeriodAmt = dd.getPeriodAmount();
                                accDepreciation += dd.getPeriodAmount();//Add  the actual period depreciation because it is not round off to get exact value saved in DB
                                balance -= dd.getPeriodAmount();//substract  the actual period depreciation because it is not round off to get exact value saved in DB
                            } else {
                                accDepreciation = authHandler.round(accDepreciation, companyid);//Round off the value for periodic depreciation
                                balance = authHandler.round(balance, companyid);//Round off the value for periodic depreciation
                            }
                            finalObj.put("perioddepreciation", dd.getPeriodAmount());
                            finalObj.put("accdepreciation", accDepreciation);
                            finalObj.put("netbookvalue", balance);
                            finalObj.put("assetId", dd.getAssetDetails().getAssetId());
                            double netbookvalueInBase = balance;
                            double accdepreciationInBase = accDepreciation;
                            finalObj.put("netbookvalueInBase", netbookvalueInBase);
                            finalObj.put("accdepreciationInBase", accdepreciationInBase);
                            finalObj.put("isje", true);
                            finalObj.put("status", "Posted");
                            finalObj.put("jeid", dd.getJournalEntry() != null ? dd.getJournalEntry().getID() : "");
                            finalObj.put("jeno", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryNumber() : "");
                            finalObj.put("jedate", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryDate() : "");
                            finalObj.put("depdetailid", dd.getID());
                            finalObj.put("disposed", false);
                            if (ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO) {
                                finalObj.put("disposed", true);
                            }
                        } else {
                            if ((isUnpost || ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO || isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && !isrepeat) {
                                continue;
                            } else {
                                finalObj.put("perioddepreciation", periodDepreciation);
                                double accdepreciationInBase = accDepreciation;
                                finalObj.put("accdepreciation", accDepreciation);
                                finalObj.put("netbookvalue", balance);
                                finalObj.put("assetId", ad.getAssetId());
                                double netbookvalueInBase = balance;
                                finalObj.put("netbookvalueInBase", netbookvalueInBase);
                                finalObj.put("accdepreciationInBase", accdepreciationInBase);
                                finalObj.put("isje", false);
                                finalObj.put("status", "Not Posted");
                                finalObj.put("depdetailid", "");
                            }
                        }
                        finalObj.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        finalObj.put("firstperiodamt", firstPeriodAmt);
                        double firstperiodamtInBase = firstPeriodAmt;
                        finalObj.put("firstperiodamtInBase", firstperiodamtInBase);
                        finalObj.put("frommonth", sdf.format(startcal.getTime()));
                        finalObj.put("tomonth", sdf.format(endcal.getTime()));
                        finalObj.put("assetDetailsId", ad.getId());
                        finalObj.put("assetGroupId", ad.getProduct().getID());
                        finalObj.put("year", year);

                        boolean differentPostOption = false;
                        if ((postOption == 1 && ad.getPostOption() == 2) || (postOption == 2 && ad.getPostOption() == 1)) { // if Client has already posted Depreciation with Yearly but now going to post with Monthly
                            differentPostOption = true;
                            if (postOption == 1 && ad.getPostOption() == 2) {
                                finalObj.put("status", "Posted By Monthly Method");
                            } else {
                                finalObj.put("status", "Posted By Yearly Method");
                            }
                        }
                        finalObj.put("differentPostOption", differentPostOption);

                        LocalDate localStartDate = new LocalDate(startcal.getTime());
                        DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                        DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();

                        finalObj.put("isFuture", new DateTime(firstDateOfMonth).toDate().after(new Date()) ? true : false);
                        finalObj.put("isQuantityAvailableForDepreciation", !(ad.isPurchaseReturn() || (ad.getAssetSoldFlag() == 2)));
                        if (!(ad.isPurchaseReturn())) {
                            finalJArr.put(finalObj);
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccAssetServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccAssetServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public void getDoubleDeclineDepreciation(Map<String, Object> request, AssetDetails ad, JSONArray finalJArr, ExtraCompanyPreferences extraCompanyPreferences) throws SessionExpiredException, AccountingException {
        try {
            Date creationDate = ad.getInstallationDate();
            int postOption = request.get("postOption") != null ? (Integer) request.get("postOption") : 0;
            boolean isUnpost = request.get("isUnpost") != null ? (Boolean) request.get("isUnpost") : false;
            boolean isDepreciationDetailReport = request.get("isDepreciationDetailReport") != null ? (Boolean) request.get("isDepreciationDetailReport") : false;
            boolean isFixedAssetSummaryReport = request.get("isFixedAssetSummaryReport") != null ? (Boolean) request.get("isFixedAssetSummaryReport") : false;
            boolean isFixedAssetDetailReport = request.get("isFixedAssetDetailReport") != null ? (Boolean) request.get("isFixedAssetDetailReport") : false;
            boolean isMonthsWiseDetails = request.get("isMonthsWiseDetails") != null ? (Boolean) request.get("isMonthsWiseDetails") : false;
            boolean isGenerateAssetDepreciation = request.get("isGenerateAssetDepreciation") != null ? (Boolean) request.get("isGenerateAssetDepreciation") : false;
            Date startdate = request.get(Constants.REQ_startdate) != null ? (Date) request.get(Constants.REQ_startdate) : null;
            boolean isForDisposeAsset = request.get("isForDisposeAsset") != null ? (Boolean) request.get("isForDisposeAsset") : false;
            int depreciationCalculationType = request.get("depreciationCalculationType") != null ? (Integer) request.get("depreciationCalculationType") : 0;
            int startMonth = request.get("startMonth") != null ? (Integer) request.get("startMonth") : 0;
            int endMonth = request.get("endMonth") != null ? (Integer) request.get("endMonth") : 11;
            String years = request.get("years") != null ? (String) request.get("years") : "";
            String companyid = request.get("companyid") != null ? (String) request.get("companyid") : "";
            List<String> yearList = Arrays.asList(years.split("\\s*,\\s*"));
            Collections.sort(yearList);
            Calendar startcal = Calendar.getInstance();
            Calendar endcal = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();
            DateFormat sdf = authHandler.getGlobalDateFormat();
            double totalLandingCost = 0;
            String countryId = "";
            if (extraCompanyPreferences != null) {
                countryId = extraCompanyPreferences.getCompany().getCountry().getID();
            }
            if (!StringUtil.isNullOrEmpty(countryId) && Integer.parseInt(countryId) == Constants.indian_country_id) {
                if (extraCompanyPreferences.isActivelandingcostofitem()) {
                    HashMap<String, Object> requestMap = new HashMap<>();
                    requestMap.put("companyId", companyid);
                    requestMap.put("assetDetailId", ad.getId());
                    List normallist = accProductObj.getPurchaseInvoiceNoAndVendorOfAssetPurchaseInvoice(requestMap);
                    Iterator normalit = normallist.iterator();
                    if (normallist.size() <= 0) {//If Asset GR is linked in Asset PI
                        String assetDetailId = "";
                        requestMap.put("assetid", ad.getAssetId());
                        KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                        List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                        for (AssetDetails assetDetailObj : assetDetaillist) {
                            assetDetailId += assetDetailObj.getId() + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(assetDetailId)) {
                            assetDetailId = assetDetailId.substring(0, assetDetailId.length() - 1);
                            requestMap.put("assetDetailId", assetDetailId);
                            normallist = accProductObj.getPIDetails_AssetGRLinkedInAssetPI(requestMap);
                            normalit = normallist.iterator();
                        }
                    }
                    String invoiceid = "", invoiceDetailID = "";
                    while (normalit.hasNext()) {
                        Object obj[] = (Object[]) normalit.next();
                        invoiceid = (String) obj[5];
                        invoiceDetailID = (String) obj[6];
                    }
                    if (!StringUtil.isNullOrEmpty(invoiceDetailID) && !StringUtil.isNullOrEmpty(invoiceid)) {
                        Set<LandingCostCategory> lccSet = ad.getProduct().getLccategoryid();
                        String invoiceAssetDetailId = "";
                        requestMap = new HashMap<>();
                        requestMap.put("assetid", ad.getAssetId());
                        requestMap.put("companyId", companyid);
                        KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                        List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                        for (AssetDetails assetDetailObj : assetDetaillist) {
                            invoiceAssetDetailId = assetDetailObj.getId();
                        }
                        HashMap<String, Object> globalParams = new HashMap<String, Object>();
                        requestMap.put(Constants.companyKey, companyid);
                        requestMap.put(Constants.globalCurrencyKey, extraCompanyPreferences.getCompany().getCurrency().getCurrencyID());
                        requestMap.put(Constants.df, authHandler.getDateOnlyFormat());
                        if (!StringUtil.isNullOrEmpty(invoiceAssetDetailId)) {
                            totalLandingCost = getTotalLandedCost(lccSet, invoiceid, globalParams, invoiceDetailID, invoiceAssetDetailId, companyid, new JSONObject());
                        }
                    }
                }
            }
            double openingbalance = ad.getCost() + totalLandingCost;
            double balance = openingbalance;
            double currentAssetVal = 0;
            String backyears = "";
            int type = ad.getProduct().getDepreciationMethod();
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(creationDate);
            int currentyear = Integer.parseInt(yearList.get(0));
            int creationyear1 = cal1.get(Calendar.YEAR);
            boolean isrepeat = request.get("isrepeat") != null ? (Boolean) request.get("isrepeat") : false;
            Date finanDate = request.get("finanDate") != null ? (Date) request.get("finanDate") : null;
            int financialStartMonth = request.get("financialStartMonth") != null ? Integer.parseInt(request.get("financialStartMonth").toString()) : 0;
            if (!isrepeat && creationyear1 < currentyear) {
                request.put("actualStartMonth", startMonth);
                request.put("actualEndMonth", endMonth);
                request.put("actualYears", years);
                while (creationyear1 < currentyear) {
                    backyears += creationyear1 + ",";
                    creationyear1++;
                }
                if (depreciationCalculationType != 0) {
                    if (finanDate != null) {  // in case of the Depreciation Details report according to financial year we need to add current year
                        if (finanDate.getMonth() != 0) {
                            backyears += creationyear1;
                        }
                    } else if (startMonth != 0) {
                        backyears += creationyear1;
                    }
                }

                JSONArray curfinalJArr = new JSONArray();
                request.put("endMonth", finanDate != null ? finanDate.getMonth() - 1 : (startMonth != 0 ? startMonth - 1 : endMonth));
                request.put("endMonth", 11);
                request.put("years", backyears);
                request.put("isrepeat", true);
                getDoubleDeclineDepreciation(request, ad, curfinalJArr, extraCompanyPreferences);
                for (int i = 0; i < curfinalJArr.length(); i++) {
                    JSONObject newjobj = curfinalJArr.getJSONObject(i);
                    if (newjobj.has("firstperiodamtInBase")) {
                        currentAssetVal += newjobj.getDouble("firstperiodamtInBase");
                    }
                }
                balance = ad.getCost() + totalLandingCost - currentAssetVal;
                request.remove("isrepeat");
                startMonth = (int) request.get("actualStartMonth");
                endMonth = (int) request.get("actualEndMonth");
                years = (String) request.get("actualYears");
            } else {
                request.put("startMonth", request.get("actualStartMonth") != null ? request.get("actualStartMonth") : request.get("startMonth"));
                request.put("endMonth", request.get("actualEndMonth") != null ? request.get("actualEndMonth") : request.get("endMonth"));
                request.put("years", request.get("actualYears") != null ? request.get("actualYears") : request.get("years"));
            }

            double life = ad.getAssetLife();
            double salvage = ad.getSalvageValue();
            double depreciationPercent = 0;
            if (type == 4) { // wdv
                depreciationPercent = calulateWDVRate(openingbalance, salvage, life);
            } else {
                depreciationPercent = calDoubleDepreciationPercent(openingbalance, life * 12);
            }
            double depreciationPercentValue = depreciationPercent;
            double accDepreciation = currentAssetVal;
            cal.setTime(creationDate);
            double firstPeriodAmt = 0;
            boolean isOpening = request.get("isOpening") != null ? (Boolean) request.get("isOpening") : false;
            int tempStartMonth = startdate != null ? startdate.getMonth() : startMonth;
            int tempEndMonth = endMonth;
            boolean IsRepeat = (request.containsKey("isrepeat") && request.get("isrepeat") != null) ? Boolean.parseBoolean(request.get("isrepeat").toString()) : false;
            if (depreciationCalculationType == 0) { //Year Bases
                for (int yeardiff = 0, k = 0; k < yearList.size(); k++, yeardiff++) {  // Varialbe  k is used to take year from year List And Variable j is used to set the year in date of depreciation period
                    int year = Integer.parseInt(yearList.get(k));
                    int creationyear = cal.get(Calendar.YEAR);
                    yeardiff = year - creationyear;
                    if (yeardiff < 0) {  //if selected year is less than the cretion year then there will be no depreciation to show
                        continue;
                    }
                    int period = yeardiff + 1;
                    double periodDepreciation = getFormatedNumber(balance * depreciationPercent / 100 * 12);
                    if (!isOpening) {
                        periodDepreciation = authHandler.round(periodDepreciation, companyid);
                    }
                    accDepreciation += periodDepreciation;
                    balance -= periodDepreciation;
                    if (period > life) {
                        break;
                    }

                    if (balance < salvage) {
                        periodDepreciation += balance - salvage;
                        accDepreciation += balance - salvage;
                        balance = salvage;
                        depreciationPercentValue = (periodDepreciation / (balance + periodDepreciation)) * 100;
                    }
                    firstPeriodAmt = periodDepreciation;
                    JSONObject finalObj = new JSONObject();

                    startcal.setTime(creationDate);
                    endcal.setTime(creationDate);
                    startcal.add(Calendar.YEAR, yeardiff);
                    endcal.add(Calendar.YEAR, yeardiff);
                    startcal.set(Calendar.MONTH, 0);
                    endcal.set(Calendar.MONTH, 0);
                    startcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                    endcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                    startcal.set(Calendar.HOUR, 0);
                    startcal.set(Calendar.MINUTE, 0);
                    startcal.set(Calendar.SECOND, 0);
                    endcal.set(Calendar.HOUR, 0);
                    endcal.set(Calendar.MINUTE, 0);
                    endcal.set(Calendar.SECOND, 0);
                    HashMap<String, Object> filters = new HashMap<String, Object>();
                    filters.put("period", period);
                    filters.put("assetDetailsId", ad.getId());
                    filters.put("companyid", companyid);
                    filters.put("assetDetails", true);

                    KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                    Iterator itrcust = dresult.getEntityList().iterator();
                    if (itrcust.hasNext()) {
                        AssetDepreciationDetail dd = (AssetDepreciationDetail) itrcust.next();
                        if (dd.getJournalEntry() == null || isForDisposeAsset) {
                            accDepreciation -= periodDepreciation;//substract the period depreciation because it is round off to get previous actual accumulated depreciation.
                            balance += periodDepreciation;//Add the period depreciation because it is round off to get previous actual balance
                            firstPeriodAmt = dd.getPeriodAmount();
                            accDepreciation += dd.getPeriodAmount();//Add  the actual period depreciation because it is not round off to get exact value saved in DB
                            balance -= dd.getPeriodAmount();//substract  the actual period depreciation because it is not round off to get exact value saved in DB
                        } else {
                            accDepreciation = authHandler.round(accDepreciation, companyid);
                            balance = authHandler.round(balance, companyid);
                        }
                        finalObj.put("perioddepreciation", dd.getPeriodAmount());
                        finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                        finalObj.put("netbookvalue", dd.getNetBookValue());
                        finalObj.put("assetId", dd.getAssetDetails().getAssetId());
                        double netbookvalueInBase = dd.getNetBookValue();
                        double accdepreciationInBase = dd.getAccumulatedAmount();
                        finalObj.put("netbookvalueInBase", netbookvalueInBase);
                        finalObj.put("accdepreciationInBase", accdepreciationInBase);
                        finalObj.put("isje", true);
                        finalObj.put("status", "Posted");
                        finalObj.put("jeid", dd.getJournalEntry() != null ? dd.getJournalEntry().getID() : "");
                        finalObj.put("jeno", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryNumber() : "");
                        finalObj.put("jedate", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryDate() : "");
                        finalObj.put("depdetailid", dd.getID());
                        finalObj.put("disposed", false);
                        if (ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO) {
                            finalObj.put("disposed", true);
                        }
                    } else {
                        if ((isUnpost || ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO || isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && !isrepeat) {
                            continue;
                        } else {
                            finalObj.put("perioddepreciation", periodDepreciation);
                            finalObj.put("accdepreciation", accDepreciation);
                            finalObj.put("netbookvalue", balance);
                            finalObj.put("assetId", ad.getAssetId());
                            double netbookvalueInBase = balance;
                            double accdepreciationInBase = accDepreciation;
                            finalObj.put("netbookvalueInBase", netbookvalueInBase);
                            finalObj.put("accdepreciationInBase", accdepreciationInBase);
                            finalObj.put("isje", false);
                            finalObj.put("status", "Not Posted");
                            finalObj.put("depdetailid", "");
                        }
                    }
                    boolean differentPostOption = false;
                    if ((postOption == 1 && ad.getPostOption() == 2) || (postOption == 2 && ad.getPostOption() == 1)) { // if Client has already posted Depreciation with monthly but now going to post with Yearly
                        differentPostOption = true;
                        if (postOption == 1 && ad.getPostOption() == 2) {
                            finalObj.put("status", "Posted By Monthly Method");
                        } else {
                            finalObj.put("status", "Posted By Yearly Method");
                        }
                    }
                    finalObj.put("differentPostOption", differentPostOption);
                    finalObj.put("period", period);
                    finalObj.put("firstperiodamt", firstPeriodAmt);
                    double firstperiodamtInBase = firstPeriodAmt;
                    finalObj.put("firstperiodamtInBase", firstperiodamtInBase);
                    finalObj.put("fromyear", startcal.get(Calendar.YEAR));
                    finalObj.put("toyear", endcal.get(Calendar.YEAR));
                    String depreciationPercentString = getFormatedNumber(depreciationPercentValue) + "%";
                    finalObj.put("depreciatedPercent", depreciationPercentString);
                    finalObj.put("assetDetailsId", ad.getId());
                    finalObj.put("assetGroupId", ad.getProduct().getID());
                    Calendar calCurrentYear = Calendar.getInstance();
                    calCurrentYear.setTime(new Date());
                    finalObj.put("isFuture", startcal.get(Calendar.YEAR) > calCurrentYear.get(Calendar.YEAR) ? true : false);
                    finalObj.put("isQuantityAvailableForDepreciation", !(ad.isPurchaseReturn() || (ad.getAssetSoldFlag() == 2)));
                    if (!(ad.isPurchaseReturn())) {
                        finalJArr.put(finalObj);
                    }
                }
            } else if (depreciationCalculationType == 1) { //Actual Date
                double tempStoreperiodDepreciation = 0;
                boolean nextYear = false; // Variable used to compare the current year either start or end 
                for (int yeardiff = 0, k = 0; k < yearList.size(); k++, yeardiff++) {  // Varialbe  k is used to take year from year List And Variable j is used to set the year in date of depreciation period
                    int year = Integer.parseInt(yearList.get(k));   //take the current year from the yearlist
                    int creationyear = cal.get(Calendar.YEAR);  // take creation year from the installation date
                    yeardiff = year - creationyear;  // calculate the difference between selectd year and the creation year
                    if (yeardiff < 0) { // if selected year is less than creation year then there is no depreciation to show
                        continue;
                    }
                    if (creationyear == year) {  //if creation year same as that of selected year 
                        if (startMonth != endMonth && yearList.size() > 1) {
                            if ((startMonth < creationDate.getMonth() && Integer.parseInt(yearList.get(0)) == year) || (startMonth > creationDate.getMonth() && Integer.parseInt(yearList.get(1)) == year)) {
                                startMonth = creationDate.getMonth();
                            }
                        }
                    } else if (year < creationyear) {  // if selected year is less than the creation year then we can start directly from the creation month
                        startMonth = creationDate.getMonth();
                    } else {
                        if (finanDate != null) {  // Finan date comes only when we select the depreciation based on the financial year 
                            if (nextYear) {   // if current year comes as second year then start from Jan
                                startMonth = 0;
                            } else {  // if current year comes as first then start from finacial year month
                                startMonth = finanDate.getMonth();
                            }
                        } else if (isrepeat) {  // use in normal case to generate Depreciation in Generate Asset Depreciation Report
                            startMonth = 0;
                        }
                    }
                    nextYear = true; // consider current year as next year 
                    int endmonth = 12;
                    if ((isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && yeardiff > 0 && yearList.size() != 1) {
                        startMonth = 0;
                    }
                    if (isMonthsWiseDetails) {
                        if (yeardiff > 0 && yearList.size() != 1) {
                            startMonth = 0;
                        }
                        if (!IsRepeat && k == 0) {
                            startMonth = tempStartMonth;
                        }
                        if (yearList.size() > 1 && k == yearList.size() - 1 && financialStartMonth != 0) {
                            endmonth = tempStartMonth;
                        }
                    }
                    if (isGenerateAssetDepreciation) {
                        if (!IsRepeat) {
                            endmonth = tempEndMonth + 1;
                        }
                    }
                    for (int i = startMonth; i < endmonth; i++) {
                        if (i == endMonth + 1 && Integer.parseInt(yearList.get(yearList.size() - 1)) == year) {
                            break;
                        } else if (i < cal.get(Calendar.MONTH) && year == cal.get(Calendar.YEAR)) {
                            continue;
                        }
                        int period = (12 * yeardiff) + i + 1;
                        double periodDepreciation = getFormatedNumber(balance * depreciationPercent / 100);
                        if (!isOpening) {
                            periodDepreciation = authHandler.round(periodDepreciation, companyid);
                        }
                        if (cal.get(Calendar.MONTH) == i && year == cal.get(Calendar.YEAR)) {
                            tempStoreperiodDepreciation = periodDepreciation;
                            int noofdaysinmonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                            double periodDepreciationPerDay = periodDepreciation / noofdaysinmonth;
                            LocalDate localCreationDate = new LocalDate(creationDate);
                            DateTime date = localCreationDate.toDateTimeAtStartOfDay();
                            DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                            LocalDate localCreationDate1 = new LocalDate(lastDateOfMonth);
                            DateTime date1 = localCreationDate1.toDateTimeAtMidnight();
                            int days = Days.daysBetween(date, date1).getDays(); // get difference in days
                            periodDepreciation = periodDepreciationPerDay * (days + 1);
                        }
                        accDepreciation += periodDepreciation;
                        balance -= periodDepreciation;
                        if (period > ((life * 12) + cal.get(Calendar.MONTH))) {
                            break;
                        }

                        if (balance < salvage) {
                            periodDepreciation += balance - salvage;
                            accDepreciation += balance - salvage;
                            balance = salvage;
                            depreciationPercentValue = (periodDepreciation / (balance + periodDepreciation)) * 100;
                        }
                        firstPeriodAmt = periodDepreciation;
                        JSONObject finalObj = new JSONObject();
                        startcal.setTime(creationDate);
                        endcal.setTime(creationDate);
                        startcal.add(Calendar.YEAR, yeardiff);
                        endcal.add(Calendar.YEAR, yeardiff);
                        startcal.set(Calendar.MONTH, i);
                        endcal.set(Calendar.MONTH, i + 1);
                        startcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        endcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        startcal.set(Calendar.HOUR, 0);
                        startcal.set(Calendar.MINUTE, 0);
                        startcal.set(Calendar.SECOND, 0);
                        endcal.set(Calendar.HOUR, 0);
                        endcal.set(Calendar.MINUTE, 0);
                        endcal.set(Calendar.SECOND, 0);
                        HashMap<String, Object> filters = new HashMap<String, Object>();
                        filters.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        filters.put("assetDetailsId", ad.getId());
                        filters.put("companyid", companyid);
                        filters.put("assetDetails", true);

                        KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                        Iterator itrcust = dresult.getEntityList().iterator();
                        if (itrcust.hasNext()) {
                            AssetDepreciationDetail dd = (AssetDepreciationDetail) itrcust.next();
                            if (dd.getJournalEntry() == null || isForDisposeAsset) {
                                accDepreciation -= periodDepreciation;//substract the period depreciation because it is round off to get previous actual accumulated depreciation.
                                balance += periodDepreciation;//Add the period depreciation because it is round off to get previous actual balance
                                firstPeriodAmt = dd.getPeriodAmount();
                                accDepreciation += dd.getPeriodAmount();//Add  the actual period depreciation because it is not round off to get exact value saved in DB
                                balance -= dd.getPeriodAmount();//substract  the actual period depreciation because it is not round off to get exact value saved in DB
                            } else {
                                accDepreciation = authHandler.round(accDepreciation, companyid);
                                balance = authHandler.round(balance, companyid);
                            }
                            finalObj.put("perioddepreciation", dd.getPeriodAmount());
                            finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                            finalObj.put("netbookvalue", dd.getNetBookValue());
                            finalObj.put("assetId", dd.getAssetDetails().getAssetId());
                            double netbookvalueInBase = dd.getNetBookValue();
                            double accdepreciationInBase = dd.getAccumulatedAmount();
                            finalObj.put("netbookvalueInBase", netbookvalueInBase);
                            finalObj.put("accdepreciationInBase", accdepreciationInBase);
                            finalObj.put("isje", true);
                            finalObj.put("status", "Posted");
                            finalObj.put("depdetailid", dd.getID());
                            finalObj.put("jeid", dd.getJournalEntry() != null ? dd.getJournalEntry().getID() : "");
                            finalObj.put("jeno", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryNumber() : "");
                            finalObj.put("jedate", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryDate() : "");
                            finalObj.put("disposed", false);
                            if (ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO) {
                                finalObj.put("disposed", true);
                            }
                        } else {
                            if ((isUnpost || ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO || isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && !isrepeat) {
                                continue;
                            } else {
                                finalObj.put("perioddepreciation", periodDepreciation);
                                finalObj.put("accdepreciation", accDepreciation);
                                finalObj.put("netbookvalue", balance);
                                finalObj.put("assetId", ad.getAssetId());
                                double netbookvalueInBase = balance;
                                double accdepreciationInBase = accDepreciation;
                                finalObj.put("netbookvalueInBase", netbookvalueInBase);
                                finalObj.put("accdepreciationInBase", accdepreciationInBase);
                                finalObj.put("isje", false);
                                finalObj.put("status", "Not Posted");
                                finalObj.put("depdetailid", "");
                            }
                        }
                        finalObj.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        finalObj.put("firstperiodamt", firstPeriodAmt);
                        double firstperiodamtInBase = firstPeriodAmt;
                        finalObj.put("firstperiodamtInBase", firstperiodamtInBase);
                        finalObj.put("frommonth", sdf.format(startcal.getTime()));
                        finalObj.put("tomonth", sdf.format(endcal.getTime()));
                        String depreciationPercentString = getFormatedNumber(depreciationPercentValue) + "%";
                        finalObj.put("assetDetailId", ad.getId());
                        finalObj.put("assetGroupId", ad.getProduct().getID());
                        finalObj.put("depreciatedPercent", depreciationPercentString);
                        finalObj.put("assetDetailsId", ad.getId());
                        finalObj.put("assetGroupId", ad.getProduct().getID());
                        finalObj.put("differentPostOption", false);

                        LocalDate localStartDate = new LocalDate(startcal.getTime());
                        DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                        DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();

                        finalObj.put("isFuture", new DateTime(firstDateOfMonth).toDate().after(new Date()) ? true : false);
                        finalObj.put("isQuantityAvailableForDepreciation", !(ad.isPurchaseReturn() || (ad.getAssetSoldFlag() == 2)));
                        if (!(ad.isPurchaseReturn() || (ad.getAssetSoldFlag() == 2))) {
                            finalJArr.put(finalObj);
                        }
                        if (cal.get(Calendar.MONTH) == i && year == cal.get(Calendar.YEAR)) {
                            periodDepreciation = tempStoreperiodDepreciation;
                        }
                    }
                }
            } else if (depreciationCalculationType == 3) { //Actual Date (No. of Days)
                double tempStoreperiodDepreciation = 0;
                boolean nextYear = false; // Variable used to compare the current year either start or end 
                depreciationPercent = depreciationPercent * 12;
//                depreciationPercent = authHandler.round(depreciationPercent, 2);

                for (int yeardiff = 0, k = 0; k < yearList.size(); k++, yeardiff++) {  // Varialbe  k is used to take year from year List And Variable j is used to set the year in date of depreciation period
                    int year = Integer.parseInt(yearList.get(k));   //take the current year from the yearlist
                    int creationyear = cal.get(Calendar.YEAR);  // take creation year from the installation date
                    yeardiff = year - creationyear;  // calculate the difference between selectd year and the creation year
                    if (yeardiff < 0) { // if selected year is less than creation year then there is no depreciation to show
                        continue;
                    }
                    if (creationyear == year) {  //if creation year same as that of selected year 
                        if (startMonth != endMonth && yearList.size() > 1) {
                            if ((startMonth < creationDate.getMonth() && Integer.parseInt(yearList.get(0)) == year) || (startMonth > creationDate.getMonth() && Integer.parseInt(yearList.get(1)) == year)) {
                                startMonth = creationDate.getMonth();
                            }
                        }
                    } else if (year < creationyear) {  // if selected year is less than the creation year then we can start directly from the creation month
                        startMonth = creationDate.getMonth();
                    } else {
                        if (finanDate != null) {  // Finan date comes only when we select the depreciation based on the financial year 
                            if (nextYear) {   // if current year comes as second year then start from Jan
                                startMonth = 0;
                            } else {  // if current year comes as first then start from finacial year month
                                startMonth = finanDate.getMonth();
                            }
                        } else if (isrepeat) {  // use in normal case to generate Depreciation in Generate Asset Depreciation Report
                            startMonth = 0;
                        }
                    }
                    nextYear = true; // consider current year as next year 
                    int endmonth = 12;
                    if ((isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && yeardiff > 0 && yearList.size() != 1) {
                        startMonth = 0;
                    }
                    if (isMonthsWiseDetails) {
                        if (yeardiff > 0 && yearList.size() != 1) {
                            startMonth = 0;
                        }
                        if (!IsRepeat && k == 0) {
                            startMonth = tempStartMonth;
                        }
                        if (yearList.size() > 1 && k == yearList.size() - 1 && financialStartMonth != 0) {
                            endmonth = tempStartMonth;
                        }
                    }
                    if (isGenerateAssetDepreciation) {
                        if (!IsRepeat) {
                            endmonth = tempEndMonth + 1;
                        }
                    }

                    double periodDepreciation = getFormatedNumber(balance * depreciationPercent / 100);
//                    periodDepreciation /= 365;
                    periodDepreciation /= cal.getActualMaximum(Calendar.DAY_OF_YEAR);

                    if (!isOpening) {
                        periodDepreciation = authHandler.round(periodDepreciation, companyid);
                    }

                    for (int i = startMonth; i < endmonth; i++) {
                        if (i == endMonth + 1 && Integer.parseInt(yearList.get(yearList.size() - 1)) == year) {
                            break;
                        } else if (i < cal.get(Calendar.MONTH) && year == cal.get(Calendar.YEAR)) {
                            continue;
                        }
                        int period = (12 * yeardiff) + i + 1;

                        tempStoreperiodDepreciation = periodDepreciation;
                        double periodDepreciationPerDay = periodDepreciation;
                        if (cal.get(Calendar.MONTH) == i && year == cal.get(Calendar.YEAR)) {
                            LocalDate localCreationDate = new LocalDate(creationDate);
                            DateTime date = localCreationDate.toDateTimeAtStartOfDay();
                            DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                            LocalDate localCreationDate1 = new LocalDate(lastDateOfMonth);
                            DateTime date1 = localCreationDate1.toDateTimeAtMidnight();
                            int days = Days.daysBetween(date, date1).getDays(); // get difference in days
                            periodDepreciation = periodDepreciationPerDay * (days + 1);
                        } else {
                            Date startdateTemp = new Date(year, i, 01);
                            LocalDate localCreationDate = new LocalDate(startdateTemp);
                            DateTime date = localCreationDate.toDateTimeAtStartOfDay();
                            DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                            LocalDate localCreationDate1 = new LocalDate(lastDateOfMonth);
                            DateTime date1 = localCreationDate1.toDateTimeAtMidnight();
                            int days = Days.daysBetween(date, date1).getDays(); // get difference in days
                            periodDepreciation = periodDepreciationPerDay * (days + 1);
                        }
                        accDepreciation += periodDepreciation;
                        balance -= periodDepreciation;
                        if (period > ((life * 12) + cal.get(Calendar.MONTH))) {
                            break;
                        }

                        if (balance < salvage) {
                            periodDepreciation += balance - salvage;
                            accDepreciation += balance - salvage;
                            balance = salvage;
                            depreciationPercentValue = (periodDepreciation / (balance + periodDepreciation)) * 100;
                        }
                        firstPeriodAmt = periodDepreciation;
                        JSONObject finalObj = new JSONObject();
                        startcal.setTime(creationDate);
                        endcal.setTime(creationDate);
                        startcal.add(Calendar.YEAR, yeardiff);
                        endcal.add(Calendar.YEAR, yeardiff);
                        startcal.set(Calendar.MONTH, i);
                        endcal.set(Calendar.MONTH, i + 1);
                        startcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        endcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        startcal.set(Calendar.HOUR, 0);
                        startcal.set(Calendar.MINUTE, 0);
                        startcal.set(Calendar.SECOND, 0);
                        endcal.set(Calendar.HOUR, 0);
                        endcal.set(Calendar.MINUTE, 0);
                        endcal.set(Calendar.SECOND, 0);
                        HashMap<String, Object> filters = new HashMap<String, Object>();
                        filters.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        filters.put("assetDetailsId", ad.getId());
                        filters.put("companyid", companyid);
                        filters.put("assetDetails", true);

                        KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                        Iterator itrcust = dresult.getEntityList().iterator();
                        if (itrcust.hasNext()) {
                            AssetDepreciationDetail dd = (AssetDepreciationDetail) itrcust.next();
                            if (dd.getJournalEntry() == null || isForDisposeAsset) {
                                accDepreciation -= periodDepreciation;//substract the period depreciation because it is round off to get previous actual accumulated depreciation.
                                balance += periodDepreciation;//Add the period depreciation because it is round off to get previous actual balance
                                firstPeriodAmt = dd.getPeriodAmount();
                                accDepreciation += dd.getPeriodAmount();//Add  the actual period depreciation because it is not round off to get exact value saved in DB
                                balance -= dd.getPeriodAmount();//substract  the actual period depreciation because it is not round off to get exact value saved in DB
                            } else {
                                accDepreciation = authHandler.round(accDepreciation, companyid);
                                balance = authHandler.round(balance, companyid);
                            }
                            finalObj.put("perioddepreciation", dd.getPeriodAmount());
                            finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                            finalObj.put("netbookvalue", dd.getNetBookValue());
                            finalObj.put("assetId", dd.getAssetDetails().getAssetId());
                            double netbookvalueInBase = dd.getNetBookValue();
                            double accdepreciationInBase = dd.getAccumulatedAmount();
                            finalObj.put("netbookvalueInBase", netbookvalueInBase);
                            finalObj.put("accdepreciationInBase", accdepreciationInBase);
                            finalObj.put("isje", true);
                            finalObj.put("status", "Posted");
                            finalObj.put("depdetailid", dd.getID());
                            finalObj.put("jeid", dd.getJournalEntry() != null ? dd.getJournalEntry().getID() : "");
                            finalObj.put("jeno", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryNumber() : "");
                            finalObj.put("jedate", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryDate() : "");
                            finalObj.put("disposed", false);
                            if (ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO) {
                                finalObj.put("disposed", true);
                            }
                        } else {
                            if ((isUnpost || ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO || isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && !isrepeat) {
                                continue;
                            } else {
                                finalObj.put("perioddepreciation", periodDepreciation);
                                finalObj.put("accdepreciation", accDepreciation);
                                finalObj.put("netbookvalue", balance);
                                finalObj.put("assetId", ad.getAssetId());
                                double netbookvalueInBase = balance;
                                double accdepreciationInBase = accDepreciation;
                                finalObj.put("netbookvalueInBase", netbookvalueInBase);
                                finalObj.put("accdepreciationInBase", accdepreciationInBase);
                                finalObj.put("isje", false);
                                finalObj.put("status", "Not Posted");
                                finalObj.put("depdetailid", "");
                            }
                        }
                        finalObj.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        finalObj.put("firstperiodamt", firstPeriodAmt);
                        double firstperiodamtInBase = firstPeriodAmt;
                        finalObj.put("firstperiodamtInBase", firstperiodamtInBase);
                        finalObj.put("frommonth", sdf.format(startcal.getTime()));
                        finalObj.put("tomonth", sdf.format(endcal.getTime()));
                        String depreciationPercentString = getFormatedNumber(depreciationPercentValue) + "%";
                        finalObj.put("assetDetailId", ad.getId());
                        finalObj.put("assetGroupId", ad.getProduct().getID());
                        finalObj.put("depreciatedPercent", depreciationPercentString);
                        finalObj.put("assetDetailsId", ad.getId());
                        finalObj.put("assetGroupId", ad.getProduct().getID());
                        finalObj.put("differentPostOption", false);

                        LocalDate localStartDate = new LocalDate(startcal.getTime());
                        DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                        DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();

                        finalObj.put("isFuture", new DateTime(firstDateOfMonth).toDate().after(new Date()) ? true : false);
                        finalObj.put("isQuantityAvailableForDepreciation", !(ad.isPurchaseReturn() || (ad.getAssetSoldFlag() == 2)));
                        if (!(ad.isPurchaseReturn())) {
                            finalJArr.put(finalObj);
                        }
//                        if (cal.get(Calendar.MONTH) == i && year == cal.get(Calendar.YEAR)) {
                        periodDepreciation = tempStoreperiodDepreciation;
//                        }
                    }
                }
            } else { //Actual Month       
                boolean nextYear = false; // Variable used to compare the current year either start or end 
                for (int yeardiff = 0, k = 0; k < yearList.size(); k++, yeardiff++) {  // Varialbe  k is used to take year from year List And Variable j is used to set the year in date of depreciation period
                    int year = Integer.parseInt(yearList.get(k));   //take the current year from the yearlist
                    int creationyear = cal.get(Calendar.YEAR);  // take creation year from the installation date
                    yeardiff = year - creationyear;  // calculate the difference between selectd year and the creation year
                    if (yeardiff < 0) { // if selected year is less than creation year then there is no depreciation to show
                        continue;
                    }
                    if (creationyear == year) {  //if creation year same as that of selected year 
                        if (startMonth != endMonth && yearList.size() > 1) {
                            if ((startMonth < creationDate.getMonth() && Integer.parseInt(yearList.get(0)) == year) || (startMonth > creationDate.getMonth() && Integer.parseInt(yearList.get(1)) == year)) {
                                startMonth = creationDate.getMonth();
                            }
                        }
                    } else if (year < creationyear) {  // if selected year is less than the creation year then we can start directly from the creation month
                        startMonth = creationDate.getMonth();
                    } else {
                        if (finanDate != null) {  // Finan date comes only when we select the depreciation based on the financial year 
                            if (nextYear) {   // if current year comes as second year then start from Jan
                                startMonth = 0;
                            } else {  // if current year comes as first then start from finacial year month
                                startMonth = finanDate.getMonth();
                            }
                        } else if (isrepeat) {  // use in normal case to generate Depreciation in Generate Asset Depreciation Report
                            startMonth = 0;
                        }
                    }
                    nextYear = true; // consider current year as next year 
                    int endmonth = 12;
                    if ((isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && yeardiff > 0 && yearList.size() != 1) {
                        startMonth = 0;
                    }
                    if (isMonthsWiseDetails) {
                        if (yeardiff > 0 && yearList.size() != 1) {
                            startMonth = 0;
                        }
                        if (!IsRepeat && k == 0) {
                            startMonth = tempStartMonth;
                        }
                        if (yearList.size() > 1 && k == yearList.size() - 1 && financialStartMonth != 0) {
                            endmonth = tempStartMonth;
                        }
                    }
                    if (isGenerateAssetDepreciation) {
                        if (!IsRepeat) {
                            endmonth = tempEndMonth + 1;
                        }
                    }
                    for (int i = startMonth; i < endmonth; i++) {
                        if (i == endMonth + 1 && Integer.parseInt(yearList.get(yearList.size() - 1)) == year) {
                            break;
                        } else if (i < cal.get(Calendar.MONTH) && year == cal.get(Calendar.YEAR)) {
                            continue;
                        }
                        int period = (12 * yeardiff) + i + 1;
                        double periodDepreciation = getFormatedNumber(balance * depreciationPercent / 100);
                        if (!isOpening) {
                            periodDepreciation = authHandler.round(periodDepreciation, companyid);
                        }
                        accDepreciation += periodDepreciation;
                        balance -= periodDepreciation;
                        if (period > ((life * 12) + cal.get(Calendar.MONTH))) {
                            break;
                        }

                        if (balance < salvage) {
                            periodDepreciation += balance - salvage;
                            accDepreciation += balance - salvage;
                            balance = salvage;
                            depreciationPercentValue = (periodDepreciation / (balance + periodDepreciation)) * 100;
                        }
                        firstPeriodAmt = periodDepreciation;
                        JSONObject finalObj = new JSONObject();

                        startcal.setTime(creationDate);
                        endcal.setTime(creationDate);
                        startcal.add(Calendar.YEAR, yeardiff);
                        endcal.add(Calendar.YEAR, yeardiff);
                        startcal.set(Calendar.MONTH, i);
                        endcal.set(Calendar.MONTH, i + 1);
                        startcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        endcal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
                        startcal.set(Calendar.HOUR, 0);
                        startcal.set(Calendar.MINUTE, 0);
                        startcal.set(Calendar.SECOND, 0);
                        endcal.set(Calendar.HOUR, 0);
                        endcal.set(Calendar.MINUTE, 0);
                        endcal.set(Calendar.SECOND, 0);
                        HashMap<String, Object> filters = new HashMap<String, Object>();
                        filters.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        filters.put("assetDetailsId", ad.getId());
                        filters.put("companyid", companyid);
                        filters.put("assetDetails", true);

                        KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                        Iterator itrcust = dresult.getEntityList().iterator();
                        if (itrcust.hasNext()) {
                            AssetDepreciationDetail dd = (AssetDepreciationDetail) itrcust.next();
                            if (dd.getJournalEntry() == null) {
                                firstPeriodAmt = dd.getPeriodAmount();
                            }
                            finalObj.put("perioddepreciation", dd.getPeriodAmount());
                            finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                            finalObj.put("netbookvalue", dd.getNetBookValue());
                            finalObj.put("assetId", dd.getAssetDetails().getAssetId());
                            double netbookvalueInBase = dd.getNetBookValue();
                            double accdepreciationInBase = dd.getAccumulatedAmount();
                            finalObj.put("netbookvalueInBase", netbookvalueInBase);
                            finalObj.put("accdepreciationInBase", accdepreciationInBase);
                            finalObj.put("isje", true);
                            finalObj.put("status", "Posted");
                            finalObj.put("jeid", dd.getJournalEntry() != null ? dd.getJournalEntry().getID() : "");
                            finalObj.put("jeno", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryNumber() : "");
                            finalObj.put("jedate", dd.getJournalEntry() != null ? dd.getJournalEntry().getEntryDate() : "");
                            finalObj.put("depdetailid", dd.getID());
                            finalObj.put("disposed", false);
                            if (ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO) {
                                finalObj.put("disposed", true);
                            }
                        } else {
                            if ((isUnpost || ad.isIsDisposed() || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_CI || ad.getAssetSoldFlag() == Constants.ASSET_SOLD_FROM_DO || isDepreciationDetailReport || isFixedAssetSummaryReport || isFixedAssetDetailReport) && !isrepeat) {
                                continue;
                            } else {
                                finalObj.put("perioddepreciation", periodDepreciation);
                                finalObj.put("accdepreciation", accDepreciation);
                                finalObj.put("netbookvalue", balance);
                                finalObj.put("assetId", ad.getAssetId());
                                double netbookvalueInBase = balance;
                                double accdepreciationInBase = accDepreciation;
                                finalObj.put("netbookvalueInBase", netbookvalueInBase);
                                finalObj.put("accdepreciationInBase", accdepreciationInBase);
                                finalObj.put("isje", false);
                                finalObj.put("status", "Not Posted");
                                finalObj.put("depdetailid", "");
                            }
                        }
                        finalObj.put("period", cal.get(Calendar.MONTH) != 0 ? period - cal.get(Calendar.MONTH) : period);
                        finalObj.put("firstperiodamt", firstPeriodAmt);
                        double firstperiodamtInBase = firstPeriodAmt;
                        finalObj.put("firstperiodamtInBase", firstperiodamtInBase);
                        finalObj.put("frommonth", sdf.format(startcal.getTime()));
                        finalObj.put("tomonth", sdf.format(endcal.getTime()));
                        String depreciationPercentString = getFormatedNumber(depreciationPercentValue) + "%";
                        finalObj.put("depreciatedPercent", depreciationPercentString);
                        finalObj.put("assetDetailsId", ad.getId());
                        finalObj.put("assetGroupId", ad.getProduct().getID());

                        boolean differentPostOption = false;
                        if ((postOption == 1 && ad.getPostOption() == 2) || (postOption == 2 && ad.getPostOption() == 1)) { // if Client has already posted Depreciation with Yearly but now going to post with Monthly
                            differentPostOption = true;
                            if (postOption == 1 && ad.getPostOption() == 2) {
                                finalObj.put("status", "Posted By Monthly Method");
                            } else {
                                finalObj.put("status", "Posted By Yearly Method");
                            }
                        }
                        finalObj.put("differentPostOption", differentPostOption);

                        LocalDate localStartDate = new LocalDate(startcal.getTime());
                        DateTime date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                        DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();

                        finalObj.put("isFuture", new DateTime(firstDateOfMonth).toDate().after(new Date()) ? true : false);
                        finalObj.put("isQuantityAvailableForDepreciation", !(ad.isPurchaseReturn() || (ad.getAssetSoldFlag() == 2)));
                        if (!(ad.isPurchaseReturn())) {
                            finalJArr.put(finalObj);
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccAssetServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccAssetServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public double calMonthwiseDepreciation(double openingbalance, double salvage, double month) throws ServiceException {
        double amount;
        try {
            amount = (openingbalance - salvage) / month;
        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calMonthwiseDepreciation : " + ne.getMessage(), ne);
        }
        return amount;
    }

    /**
     * Description : Method to calculate the Depreciation Rate
     *
     * @param <cost> Cost of Asset
     * @param <salvage> Scrap Value
     * @param <life> Asset Life
     * @return : doubleDepreciationPercent
     */
    private double calulateWDVRate(double cost, double salvage, double life) throws ServiceException {
        double doubleDepreciationPercent = 0d;
        try {
            double percent = (1 - Math.pow((salvage / cost), (1 / life))) * 100;
            doubleDepreciationPercent = percent / 12;
        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calulateWDVRate : " + ne.getMessage(), ne);
        }
        return doubleDepreciationPercent;
    }

    public double calDoubleDepreciationPercent(double openingbalance, double month) throws ServiceException {
        double doubleDepreciationPercent = 0d;
        try {
            double oneMonthDepriciationPercent = ((openingbalance / month) / openingbalance) * 100;
            doubleDepreciationPercent = oneMonthDepriciationPercent * 2;
            doubleDepreciationPercent = getFormatedNumber(doubleDepreciationPercent);

        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calMonthwiseDepreciation : " + ne.getMessage(), ne);
        }
        return doubleDepreciationPercent;
    }

    public double getFormatedNumber(double number) {
        NumberFormat nf = new DecimalFormat("0.00");
        String formatedStringValue = nf.format(number);
        double formatedValue = Double.parseDouble(formatedStringValue);
        return formatedValue;
    }
    public JSONArray getAssetDepreciationDetails(HttpServletRequest request, boolean isexport) throws SessionExpiredException, AccountingException, ParseException {
        JSONArray jArr = new JSONArray();
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        String companyId = sessionHandlerImpl.getCompanyid(request);
        boolean excludeSoldAssets = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        Date startdt = null;
        Date enddt = null;
        Date endDate = null;
        boolean isDepreciationDetailReport = request.getParameter("isDepreciationDetailReport") != null ? Boolean.parseBoolean(request.getParameter("isDepreciationDetailReport")) : false;
        String year = "1970";//request.getParameter("year") != null ? request.getParameter("year") : "1970";
        int depreciationCalculationType = request.getParameter("depreciationCalculationType") != null ? Integer.parseInt(request.getParameter("depreciationCalculationType")) : 0;
        if (!StringUtil.isNullOrEmpty(request.getParameter("excludeSoldAssets"))) {
            excludeSoldAssets = Boolean.parseBoolean(request.getParameter("excludeSoldAssets"));
        }
        JSONObject groupTotal = new JSONObject();
        JSONObject grandTotal = new JSONObject();
        JSONObject jobCount = new JSONObject();
        JSONObject subtotal = null;
        int postedUnposted = request.getParameter("postedUnposted") != null ? Integer.parseInt(request.getParameter("postedUnposted")) : 0;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyId", companyId);
        requestParams.put("invrecord", true);
        if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
        }
        if (isDepreciationDetailReport) {
            requestParams.put("isDepreciationDetailReport", isDepreciationDetailReport);
        }
        if (isDepreciationDetailReport && request.getParameter("stdate") != null && request.getParameter("enddate") != null) {
            startdt = df.parse(request.getParameter("stdate"));
            requestParams.put(Constants.REQ_startdate, startdt);
            enddt = df.parse(request.getParameter("enddate"));
            requestParams.put(Constants.REQ_enddate, enddt);
        }
        if (request.getParameter("dir") != null && !StringUtil.isNullOrEmpty(request.getParameter("dir"))
                && request.getParameter("sort") != null && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
            requestParams.put("dir", request.getParameter("dir"));
            requestParams.put("sort", request.getParameter("sort"));
        }
        if (excludeSoldAssets) {
            requestParams.put("excludeSoldAsset", excludeSoldAssets);
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("assetGroupIds"))) {
            requestParams.put("assetGroupIds", request.getParameterValues("assetGroupIds"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("start"))) {
            requestParams.put("start", request.getParameter("start"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
            requestParams.put("limit", request.getParameter("limit"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("searchJson"))) {
            requestParams.put("searchJson", request.getParameter("searchJson"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("filterConjuctionCriteria"))) {
            requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
        }
        requestParams.put("isexport", isexport);

        try {
            Date finanDate = null;
            int endMonth = 11;
            int currentyear = 0;
            if (startdt != null) {
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startdt);
                currentyear = startCal.get(Calendar.YEAR);
                year = String.valueOf(startCal.get(Calendar.YEAR));
            }
            KwlReturnObject kresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
            CompanyAccountPreferences preferences = null;
            if (kresult.getEntityList().size() > 0) {
                preferences = (CompanyAccountPreferences) kresult.getEntityList().get(0);
            }
            KwlReturnObject extraresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extra = null;
            if (extraresult.getEntityList().size() > 0) {
                extra = (ExtraCompanyPreferences) extraresult.getEntityList().get(0);
            }
            if (extra != null && preferences != null) {
                if (extra.getAssetDepreciationCalculationBasedOn() == Constants.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE) {
                    finanDate = preferences.getBookBeginningFrom();
                } else {
                    finanDate = preferences.getFirstFinancialYearFrom() != null ? preferences.getFirstFinancialYearFrom() : preferences.getFinancialYearFrom();
                }
            }

            String finanDate1 = authHandler.getDateFormatter(request).format(finanDate);
            finanDate = authHandler.getDateOnlyFormat().parse(finanDate1);

            KwlReturnObject result = accProductObj.getAssetDetails(requestParams);
            List<AssetDetails> list = result.getEntityList();
            int count = result.getRecordTotalCount();

            String documentIds = "";
            for (AssetDetails ad : list) {
                documentIds += "'" + ad.getId() + "',";
            }
            Map<String, List<Object[]>> baMap = new HashMap<>();
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
            boolean isEdit = (StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            String moduleID = request.getParameter("moduleid");
            Map<String, Object> batchSerialReqMap = new HashMap<>();
            batchSerialReqMap.put(Constants.companyKey, companyId);
            batchSerialReqMap.put(Constants.df, authHandler.getDateFormatter(request));
            batchSerialReqMap.put("linkingFlag", linkingFlag);
            batchSerialReqMap.put("isEdit", isEdit);
            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()) {  //check if company level option is on then only we will check productt level
                batchSerialReqMap.put("documentIds", StringUtil.isNullOrEmpty(documentIds) ? "" : documentIds.substring(0, documentIds.length() - 1));
                baMap = getBatchDetailsMap(batchSerialReqMap);
            }

            for (AssetDetails ad : list) {
                int depreciationMethod = ad.getProduct().getDepreciationMethod();
                if (depreciationMethod == 3) { // if Asset Group is not Deprecible
                    continue;
                }
                double openingdep = 0;
                Date creationDate = ad.getInstallationDate();
                Date disposalDate = ad.getDisposalDate();
                Product product = null;

                // Declare End Date of the selected Year to avoid the future Assets
//                Date endDate = new Date(finanDate.getTime());
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(enddt);
                endCal.set(Calendar.YEAR, currentyear + 1);
                endCal.add(Calendar.DATE, -1);
                String endDateStr = authHandler.getDateOnlyFormat().format(endCal.getTime());
                endDate = authHandler.getDateOnlyFormat().parse(endDateStr);

                //Condition to avoid future Assets
                if (creationDate.after(endDate)) {
                    continue;
                }
                if (groupTotal.has(ad.getProduct().getID())) {
                    subtotal = groupTotal.getJSONObject(ad.getProduct().getID());
                } else {
                    if (subtotal != null) {
                        jArr.put(subtotal);
                    }
                    subtotal = new JSONObject();
                }
                double totalLandingCost = 0;
                String countryId = "";
                if (extra != null) {
                    countryId = extra.getCompany().getCountry().getID();
                }
                JSONObject jobj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(countryId) && Integer.parseInt(countryId) == Constants.indian_country_id) {
                    if (extra.isActivelandingcostofitem()) {
                        HashMap<String, Object> requestMap = new HashMap<>();
                        requestMap.put("companyId", companyId);
                        requestMap.put("assetDetailId", ad.getId());
                        List normallist = accProductObj.getPurchaseInvoiceNoAndVendorOfAssetPurchaseInvoice(requestMap);
                        Iterator normalit = normallist.iterator();
                        if (normallist.size() <= 0) {//If Asset GR is linked in Asset PI
                            String assetDetailId = "";
                            requestMap.put("assetid", ad.getAssetId());
                            KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                            List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                            for (AssetDetails assetDetailObj : assetDetaillist) {
                                assetDetailId += assetDetailObj.getId() + ",";
                            }
                            if (!StringUtil.isNullOrEmpty(assetDetailId)) {
                                assetDetailId = assetDetailId.substring(0, assetDetailId.length() - 1);
                                requestMap.put("assetDetailId", assetDetailId);
                                normallist = accProductObj.getPIDetails_AssetGRLinkedInAssetPI(requestMap);
                                normalit = normallist.iterator();
                            }
                        }
                        String invoiceid = "", invoiceDetailID = "";
                        while (normalit.hasNext()) {
                            Object obj[] = (Object[]) normalit.next();
                            invoiceid = (String) obj[5];
                            invoiceDetailID = (String) obj[6];
                        }
                        if (!StringUtil.isNullOrEmpty(invoiceDetailID) && !StringUtil.isNullOrEmpty(invoiceid)) {
                            Set<LandingCostCategory> lccSet = ad.getProduct().getLccategoryid();
                            String invoiceAssetDetailId = "";
                            requestMap = new HashMap<>();
                            requestMap.put("assetid", ad.getAssetId());
                            requestMap.put("companyId", companyId);
                            KwlReturnObject assetDetailRes = accProductObj.getAssetDetailFromAssetID(requestMap);
                            List<AssetDetails> assetDetaillist = assetDetailRes.getEntityList();
                            for (AssetDetails assetDetailObj : assetDetaillist) {
                                invoiceAssetDetailId = assetDetailObj.getId();
                            }
                            HashMap<String, Object> globalParams = new HashMap<String, Object>();
                            requestMap.put(Constants.companyKey, companyId);
                            requestMap.put(Constants.globalCurrencyKey, extra.getCompany().getCurrency().getCurrencyID());
                            requestMap.put(Constants.df, authHandler.getDateOnlyFormat());
                            if (!StringUtil.isNullOrEmpty(invoiceAssetDetailId)) {
                                totalLandingCost = getTotalLandedCost(lccSet, invoiceid, globalParams, invoiceDetailID, invoiceAssetDetailId, companyId, jobj);
                            }
                        }
                        jobj.put("assetvaluewithoutlandedcost", ad.getCost());
                    }
                }
                double yearval = 0d;
                double openingDepreciation = (postedUnposted == 2 ? 0d : ad.getOpeningDepreciation());
                subtotal.put("assetGroup", ad.getProduct().getName());
                subtotal.put("groupinfo", ad.getProduct().getName()+"("+ad.getProduct().getProductid()+")");
                subtotal.put("assetId", "Total");
                subtotal.put("issummaryvalue", true);
                jobj.put("assetdetailId", ad.getId());
                jobj.put("assetGroup", ad.getProduct().getName());
                jobj.put("assetdescription", ad.getAssetDescription());
                jobj.put("assetDepreciationMethod", ad.getProduct().getDepreciationMethod());
                jobj.put("depreciationRate", ad.getProduct().getDepreciationRate() + " %");
                jobj.put("assetGroupId", ad.getProduct().getID());
                jobj.put("acccode", ad.getProduct().getDepreciationGLAccount() != null ? (!StringUtil.isNullOrEmpty(ad.getProduct().getDepreciationGLAccount().getAcccode()) ? ad.getProduct().getDepreciationGLAccount().getAcccode() : ad.getProduct().getDepreciationGLAccount().getAccountName()) : "");
                jobj.put("assetId", ad.getAssetId());
                jobj.put("groupinfo",ad.getProduct().getName()+"("+ad.getProduct().getProductid()+")");
                jobj.put("assetcost", ad.getCost()+totalLandingCost);
                jobj.put("installationDate", authHandler.getDateOnlyFormat(request).format(ad.getInstallationDate()));
                jobj.put("purchaseDate", authHandler.getDateOnlyFormat(request).format(ad.getPurchaseDate()));
                jobj.put("openingDepreciation", openingDepreciation);
                if (subtotal.has("openingDepreciation")) {
                    subtotal.put("openingDepreciation", subtotal.getDouble("openingDepreciation") + openingDepreciation);
                } else {
                    subtotal.put("openingDepreciation", openingDepreciation);
                }
                if (grandTotal.has("openingDepreciation")) {
                    grandTotal.put("openingDepreciation", grandTotal.getDouble("openingDepreciation") + openingDepreciation);
                } else {
                    grandTotal.put("openingDepreciation", openingDepreciation);
                }
                /**
                 * Added total and grand total for asset cost.
                 */
                if (subtotal.has("assetcost")) {
                    subtotal.put("assetcost", subtotal.getDouble("assetcost") + ad.getCost()+totalLandingCost);
                } else {
                    subtotal.put("assetcost", ad.getCost()+totalLandingCost);
                }
                if (grandTotal.has("assetcost")) {
                    grandTotal.put("assetcost", grandTotal.getDouble("assetcost") + ad.getCost()+totalLandingCost);
                } else {
                    grandTotal.put("assetcost", ad.getCost()+totalLandingCost);
                }
                jobj.put("salvageRate", ad.getSalvageRate());
                jobj.put("salvageValue", ad.getSalvageValue());
                if (ad.getProduct().getDepreciationMethod() == 1) {
                    jobj.put("assetdepreciationschedule", "Straight Line Depreciation");
                } else if (ad.getProduct().getDepreciationMethod() == 2) {
                    jobj.put("assetdepreciationschedule", "Double Decline Depreciation");
                } else {
                    jobj.put("assetdepreciationschedule", "Non Depreciable");
                }
                if (ad.isCreatedFromOpeningForm()) {  //If Assets are created from Opening Form
                    jobj.put("vendorname", "Opening Asset");  // There is no Vendor in Opening Asset creation form
                    HashMap<String, Object> requestMap = new HashMap<String, Object>();
                    requestMap.put("companyId", companyId);
                    requestMap.put("assetDetails", ad.getId());
                    KwlReturnObject assetDetailsResult = accProductObj.getAssetDetailsMappedWithOPeningDocument(requestMap);
                    List<FixedAssetOpeningMappingWithAssetDetail> openlist = assetDetailsResult.getEntityList();
                    String docno = "";
                    for (FixedAssetOpeningMappingWithAssetDetail openingMappingWithAssetDetail : openlist) {
                        docno = openingMappingWithAssetDetail.getAssetOpening().getDocumentNumber();
                    }
                    jobj.put("purchaseinvno", docno);  // opening Document No
                } else {
                    HashMap<String, Object> requestMap = new HashMap<String, Object>();
                    Tax taxObj = null;
                    requestMap.put("companyId", companyId);
                    requestMap.put("assetDetailId", ad.getId());
                    List normallist = accProductObj.getPurchaseInvoiceNoAndVendorOfAssetPurchaseInvoice(requestMap);
                    Iterator normalit = normallist.iterator();
                    String docno = "";
                    Vendor vendor = null;
                    while (normalit.hasNext()) {
                        Object obj[] = (Object[]) normalit.next();
                        docno = (String) obj[0];
                        String vendorId = (String) obj[1];
                        String taxID = (String) obj[3]; //get global level tax
                        if (StringUtil.isNullOrEmpty(taxID)) {
                            taxID = (String) obj[4];   //get line level tax
                        }
                        if (!StringUtil.isNullOrEmpty(vendorId)) {
                            vendor = (Vendor) kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(), vendorId);
                        }
                        if (!StringUtil.isNullOrEmpty(taxID)) {
                            taxObj = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxID);
                        }
                    }
                    jobj.put("purchaseinvno", docno);  //Purchase Invoice No
                    jobj.put("vendorname", vendor != null ? vendor.getName() : "");  //Vendor Name
                    jobj.put("gstcode", taxObj != null ? taxObj.getTaxCode() : "");
                }

                // Show Asset Status in Depreciation Details report
                String status = "";
                if (ad.isPurchaseReturn() == true) {
                    status = "Returned";
                }
                /*
                 * getAssetSoldFlag = 1 means asset has been sold from CI, &&& 2
                 * means asset has been sold from DO
                 */
                if (ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO) {
                    status = "Disposed";
                } else if (ad.isIsDisposed()) {
                    status = "Manually Disposed";
                }
                jobj.put("status", status);

                // calculate asset depreciation cost
                Calendar cal = Calendar.getInstance();
                double currentAssetVal = 0;
                cal.setTime(creationDate);
                int creationyear = cal.get(Calendar.YEAR);
                JSONArray finalJArr = new JSONArray();
                int startMonth = 0;
                String calculatedYear = "";
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(enddt);
                int endYear = endCalendar.get(Calendar.YEAR);
                int startYear = currentyear;
                while (startYear <= endYear) {
                    calculatedYear += startYear + ",";
                    startYear++;
                }
                if (depreciationCalculationType == 0) { // if yearly Depreciatiion Method is selected
                    calculatedYear = year;
                } else {
//                    if (startdt.getMonth() != 0) {    // if Financial year start month is other than Jan
//                        calculatedYear = Integer.parseInt(year) + "," + (Integer.parseInt(year) + 1);
                    startMonth = startdt.getMonth();
                    endMonth = enddt.getMonth();
//                    } else { // if Financial year month is Jan
//                        startMonth = 0;
//                        endMonth = 11;
//                        calculatedYear = Integer.parseInt(year) + "";
//                    }
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put("startMonth", startMonth);
                fieldrequestParams.put("endMonth", endMonth);
                fieldrequestParams.put("years", calculatedYear);
                fieldrequestParams.put("companyid", companyId);
                fieldrequestParams.put("depreciationCalculationType", depreciationCalculationType);
//                fieldrequestParams.put("finanDate", finanDate);
                fieldrequestParams.put("financialStartMonth", finanDate.getMonth());
                fieldrequestParams.put(Constants.REQ_startdate, startdt);
                fieldrequestParams.put("isMonthsWiseDetails", true);
                fieldrequestParams.put("isDepreciationDetailReport", isDepreciationDetailReport);

                if (depreciationMethod == 1) { // if selected method is Yearly Straight Line Depreciation Method
                    getAssetStraightLineDepreciation(fieldrequestParams, ad, finalJArr, extra);
                } else if (depreciationMethod == 2) { // IF Depreciation Method is double declined
                    getDoubleDeclineDepreciation(fieldrequestParams, ad, finalJArr, extra);
                } else { // if Asset Group is not Deprecible
                    continue;
                }
                for (int i = 0; i < finalJArr.length(); i++) {
                    JSONObject newjobj = finalJArr.getJSONObject(i);
                    if (depreciationCalculationType == 0) {    // if yearly Depreciatiion Method is selected
                        if (newjobj.has("fromyear")) {
                            boolean isje = newjobj.getBoolean("isje");
                            if (postedUnposted == 1) {    // posted
                                if (isje) {
                                    yearval = newjobj.getDouble("firstperiodamtInBase");
                                }
                            } else if (postedUnposted == 2) { // Unposted
                                if (!isje) {
                                    yearval = newjobj.getDouble("firstperiodamtInBase");
                                }
                            } else {  // All
                                yearval = newjobj.getDouble("firstperiodamtInBase");
                            }
                        }
                    } else {      // if Depreciation Method is either Monthly or on the Actual Date
                        int monthval = -1;
                        boolean isje = newjobj.getBoolean("isje");
                        if (newjobj.has("frommonth")) {
                            Date frommonth = new Date((String) newjobj.get("frommonth"));
                            monthval = frommonth.getMonth();
                            if (postedUnposted == 1) {    // Posted
                                if (isje) {
                                    yearval += newjobj.getDouble("firstperiodamtInBase");
                                    jobj.put("month" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                    if (subtotal.has("month" + monthval)) {
                                        subtotal.put("month" + monthval, subtotal.getDouble("month" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
                                    } else {
                                        subtotal.put("month" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                    }
                                    if (grandTotal.has("month" + monthval)) {
                                        grandTotal.put("month" + monthval, grandTotal.getDouble("month" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
                                    } else {
                                        grandTotal.put("month" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                    }
                                }
                            } else if (postedUnposted == 2) { // Unposted
                                if (!isje) {
                                    yearval += newjobj.getDouble("firstperiodamtInBase");
                                    jobj.put("month" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                    if (subtotal.has("month" + monthval)) {
                                        subtotal.put("month" + monthval, subtotal.getDouble("month" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
                                    } else {
                                        subtotal.put("month" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                    }
                                    if (grandTotal.has("month" + monthval)) {
                                        grandTotal.put("month" + monthval, grandTotal.getDouble("month" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
                                    } else {
                                        grandTotal.put("month" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                    }
                                }
                            } else if (isDepreciationDetailReport && depreciationCalculationType != 0) {
                                /**
                                 * Getting All depreciation data from 1st month
                                 * to last month is and filtered in case of
                                 * asset depreciation detail report and filtered
                                 * according to given date filter.SDP-12870
                                 */
                                int Depreciationyear = newjobj.getInt("year");
                                String DepreciationStrDate = newjobj.optString("frommonth");
                                Date DepreciationDate = authHandler.getDateOnlyFormat().parse(DepreciationStrDate);
                                if (depreciationCalculationType == 2 || depreciationCalculationType == 1 ) {
                                    Calendar startdatecal = Calendar.getInstance();
                                    startdatecal.setTime(startdt);
                                    int firstDayOfMonth = startdatecal.getActualMinimum(Calendar.DATE);
                                    startdatecal.set(Calendar.DATE, firstDayOfMonth);
                                    String strStartDate = authHandler.getDateOnlyFormat().format(startdatecal.getTime());
                                    startdt = authHandler.getDateOnlyFormat().parse(strStartDate);
                                    Calendar enddatecal = Calendar.getInstance();
                                    enddatecal.setTime(enddt);
                                    int lastDayOfMonth = enddatecal.getActualMaximum(Calendar.DATE);
                                    enddatecal.set(Calendar.DATE, lastDayOfMonth);
                                    String strEndDate = authHandler.getDateOnlyFormat().format(enddatecal.getTime());
                                    enddt = authHandler.getDateOnlyFormat().parse(strEndDate);
                                }
                                if ((DepreciationDate.equals(startdt) || DepreciationDate.after(startdt)) && (DepreciationDate.equals(enddt) || DepreciationDate.before(enddt))) {
//                                    System.out.println(" startdt :" + startdt + " DepreciationDate : " + DepreciationStrDate + " enddt : " + enddt);
                                    yearval += newjobj.getDouble("firstperiodamtInBase");
                                    jobj.put("month" + Depreciationyear + "_" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                    getDepreciationJsonFormonthwise(Depreciationyear, newjobj, monthval, subtotal, grandTotal);
                                }
                            } else {  //All
                                yearval += newjobj.getDouble("firstperiodamtInBase");
                                jobj.put("month" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                if (subtotal.has("month" + monthval)) {
                                    subtotal.put("month" + monthval, subtotal.getDouble("month" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
                                } else {
                                    subtotal.put("month" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                }
                                if (grandTotal.has("month" + monthval)) {
                                    grandTotal.put("month" + monthval, grandTotal.getDouble("month" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
                                } else {
                                    grandTotal.put("month" + monthval, newjobj.getDouble("firstperiodamtInBase"));
                                }
                            }
                        }
                    }
                }

                jobj.put("yearval", yearval);
                if (subtotal.has("yearval")) {
                    subtotal.put("yearval", subtotal.getDouble("yearval") + yearval);
                } else {
                    subtotal.put("yearval", yearval);
                }
                if (grandTotal.has("yearval")) {
                    grandTotal.put("yearval", grandTotal.getDouble("yearval") + yearval);
                } else {
                    grandTotal.put("yearval", yearval);
                }

                if (finanDate.getMonth() != 0) {
                    endMonth = finanDate.getMonth() - 1;
                } else {
                    endMonth = 11;
                }

                String backyears = "";    // Variable to calculate previous years Depreciation
                int tempYear = creationyear;    // Variable used to comapare the creation year with current year
                if (creationyear <= currentyear) {
                    while (creationyear < currentyear) {
                        backyears += creationyear + ",";
                        creationyear++;
                    }
                    if (depreciationCalculationType != 0) { // if yearly Depreciatiion Method is not selected
                        if (finanDate.getMonth() != 0 && ((finanDate.getMonth() > creationDate.getMonth() && (tempYear == currentyear)) || (currentyear != tempYear))) {
                            backyears += creationyear;
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(backyears)) {   // if there is no previous year to calculate Depreciation then no need to calculate Deprecaition
                    JSONArray curfinalJArr = new JSONArray();
                    HashMap<String, Object> fieldrequestParams1 = new HashMap();
                    fieldrequestParams1.put("startMonth", creationDate.getMonth());
                    fieldrequestParams1.put("endMonth", endMonth);
                    fieldrequestParams1.put("years", backyears);
                    fieldrequestParams1.put("companyid", companyId);
                    fieldrequestParams1.put("depreciationCalculationType", depreciationCalculationType);
                    fieldrequestParams1.put("finanDate", finanDate);
                    fieldrequestParams1.put("financialStartMonth", finanDate.getMonth());

                    if (depreciationMethod == 1) {  //If Depreciation year selected is yearly Deprecaition
                        getAssetStraightLineDepreciation(fieldrequestParams1, ad, curfinalJArr, extra);
                    } else { //if Depreciation method selected is either monthly or on Actual Date
                        getDoubleDeclineDepreciation(fieldrequestParams1, ad, curfinalJArr, extra);
                    }

                    for (int i = 0; i < curfinalJArr.length(); i++) {
                        JSONObject newjobj = curfinalJArr.getJSONObject(i);
                        if (newjobj.has("firstperiodamtInBase")) {
                            boolean isje = newjobj.getBoolean("isje");
                            if (postedUnposted == 1) {  // Posted
                                if (isje) {
                                    currentAssetVal += newjobj.getDouble("firstperiodamtInBase");
                                }
                            } else if (postedUnposted == 2) { // Unposted
                                if (!isje) {
                                    currentAssetVal += newjobj.getDouble("firstperiodamtInBase");
                                }
                            } else { // All
                                currentAssetVal += newjobj.getDouble("firstperiodamtInBase");
                            }
                        }
                    }
                    if (postedUnposted == 1) {  // Posted
                        currentAssetVal += openingDepreciation;
                    }
//                    openingDep = currentAssetVal;
                    currentAssetVal = ad.getCost() - currentAssetVal+totalLandingCost;
                } else {
                    currentAssetVal = ad.getCost()+totalLandingCost;
                }

                int stmonth = 0, endmonth = 11;
                double accdepreciation = 0;
                stmonth = creationDate.getMonth();
                if (!StringUtil.isNullOrEmpty(backyears)) {   // if there is no previous year to calculate Depreciation then no need to calculate Deprecaition
                    JSONArray curfinalJArr = new JSONArray();
                    HashMap<String, Object> fieldrequestParams1 = new HashMap();
                    fieldrequestParams1.put("startMonth", stmonth);
                    fieldrequestParams1.put("endMonth", endmonth);
                    fieldrequestParams1.put("years", backyears);
                    fieldrequestParams1.put("companyid", companyId);
                    fieldrequestParams1.put("depreciationCalculationType", depreciationCalculationType);
                    fieldrequestParams1.put("isDepreciationDetailReport", true);
                    fieldrequestParams1.put("financialStartMonth", finanDate.getMonth());

                    if (depreciationMethod == 1) {
                        getAssetStraightLineDepreciation(fieldrequestParams1, ad, curfinalJArr, extra);
                    } else {
                        getDoubleDeclineDepreciation(fieldrequestParams1, ad, curfinalJArr, extra);
                    }

                    for (int i = 0; i < curfinalJArr.length(); i++) {
                        JSONObject newjobj = curfinalJArr.getJSONObject(i);
                        if (newjobj.has("firstperiodamtInBase")) {
                            boolean isje = newjobj.getBoolean("isje");
                            if (postedUnposted == 1) {  // Posted
                                if (isje) {
                                    accdepreciation += newjobj.getDouble("firstperiodamtInBase");
                                }
                            } else if (postedUnposted == 2) { // Unposted
                                if (!isje) {
                                    accdepreciation += newjobj.getDouble("firstperiodamtInBase");
                                }
                            } else { // All
                                accdepreciation += newjobj.getDouble("firstperiodamtInBase");
                            }
                        }
                    }
                    openingdep = accdepreciation;
                }

                HashMap<String, Object> assetParams = new HashMap<String, Object>();
                assetParams.put("assetDetailsId", ad.getId());
                assetParams.put(Constants.companyKey, companyId);
                assetParams.put("assetDetails", true);
                KwlReturnObject assResult = accProductObj.getAssetDepreciationDetail(assetParams);
                List<AssetDepreciationDetail> assList = assResult.getEntityList();

                // calculate asset depreciation cost
                double assetDepreciatedCost = 0d;
                double assetNetBookValue = 0d;
                if (ad.isIsDisposed() || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI || ad.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO) {
                    assetNetBookValue = 0;
                } else {
                    for (AssetDepreciationDetail depreciationDetail : assList) {
                        assetDepreciatedCost += depreciationDetail.getPeriodAmount();
                    }
                    assetNetBookValue = ad.getCost() - assetDepreciatedCost+totalLandingCost;
                }

                if (assetNetBookValue < 0) {
                    assetNetBookValue = 0;
                }
                jobj.put("assetLife", ad.getAssetLife());
                jobj.put("currentAssetValue", currentAssetVal);
                if (subtotal.has("currentAssetValue")) {
                    subtotal.put("currentAssetValue", subtotal.getDouble("currentAssetValue") + currentAssetVal);
                } else {
                    subtotal.put("currentAssetValue", currentAssetVal);
                }
                if (grandTotal.has("currentAssetValue")) {
                    grandTotal.put("currentAssetValue", grandTotal.getDouble("currentAssetValue") + currentAssetVal);
                } else {
                    grandTotal.put("currentAssetValue", currentAssetVal);
                }
                jobj.put("assetNetBookValue", assetNetBookValue);
                if (subtotal.has("assetNetBookValue")) {
                    subtotal.put("assetNetBookValue", subtotal.getDouble("assetNetBookValue") + assetNetBookValue);
                } else {
                    subtotal.put("assetNetBookValue", assetNetBookValue);
                }
                if (grandTotal.has("assetNetBookValue")) {
                    grandTotal.put("assetNetBookValue", grandTotal.getDouble("assetNetBookValue") + assetNetBookValue);
                } else {
                    grandTotal.put("assetNetBookValue", assetNetBookValue);
                }
                jobj.put("assetUser", (ad.getAssetUser() != null) ? ad.getAssetUser().getFirstName() : "N/A");
                jobj.put("isAssetSold", (ad.getAssetSoldFlag() != 0) ? true : false);
                jobj.put("isDepreciable", (ad.getProduct().getDepreciationMethod() != 3) ? true : false);
                jobj.put("isLeased", isexport ? (ad.isLinkedToLeaseSO() == true ? "Yes" : "No") : ad.isLinkedToLeaseSO());//isExport is used for value of isLeased are shown in csv and pdf are Yes or No 
                jobj.put("location", (ad.getLocation() != null ? ad.getLocation().getName() : "N/A"));

                // Add Warehouse, Location, Batch And Serial Details
                if (!StringUtil.isNullOrEmpty(ad.getProduct().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), ad.getProduct().getID());
                    product = (Product) prodresult.getEntityList().get(0);
                    isBatchForProduct = product.isIsBatchForProduct();
                    isSerialForProduct = product.isIsSerialForProduct();
                    isLocationForProduct = product.isIslocationforproduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();
                    isRowForProduct = product.isIsrowforproduct();
                    isRackForProduct = product.isIsrackforproduct();
                    isBinForProduct = product.isIsbinforproduct();
                }

                if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()) {  //check if company level option is on then only we will check productt level
                    if (isBatchForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {
                        KwlReturnObject kmsg = null;
                        List batchserialdetails = new ArrayList();
                        if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && product.isIsSerialForProduct()) {
                            kmsg = accCommonTablesDAO.getOnlySerialDetails(ad.getId(), linkingFlag, moduleID, false, isEdit);
                            batchserialdetails = kmsg.getEntityList();
                        } else {
                            if (!product.isIsSerialForProduct() && baMap.containsKey(ad.getId())) {
                                batchserialdetails = baMap.get(ad.getId());
                            } else {
                                kmsg = accCommonTablesDAO.getBatchSerialDetails(ad.getId(), !product.isIsSerialForProduct(), linkingFlag, moduleID, false, isEdit, "");
                                batchserialdetails = kmsg.getEntityList();
                            }
                        }
                        double ActbatchQty = 1;
                        double batchQty = 0;
                        Iterator iter = batchserialdetails.iterator();
                        while (iter.hasNext()) {
                            Object[] objArr = (Object[]) iter.next();
                            jobj.put("batchid", objArr[0] != null ? (String) objArr[0] : "N/A");
                            jobj.put("batch", objArr[1] != null ? (String) objArr[1] : "N/A");
                            jobj.put("batchname", objArr[1] != null ? (String) objArr[1] : "N/A");
                            if (objArr[2] != null) {
                                String locationId = objArr[2].toString();
                                if (!StringUtil.isNullOrEmpty(locationId)) {
                                    KwlReturnObject loct = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), locationId);
                                    InventoryLocation location = (InventoryLocation) loct.getEntityList().get(0);
                                    jobj.put("location", !StringUtil.isNullOrEmpty(location.getName()) ? location.getName() : "N/A");
                                } else {
                                    jobj.put("location", "N/A");
                                }
                            }
                            if (objArr[3] != null) {
                                String warehouseId = objArr[3].toString();
                                if (!StringUtil.isNullOrEmpty(warehouseId)) {
                                    KwlReturnObject war = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouseId);
                                    InventoryWarehouse warehouse = (InventoryWarehouse) war.getEntityList().get(0);
                                    jobj.put("warehouse", !StringUtil.isNullOrEmpty(warehouse.getName()) ? warehouse.getName() : "N/A");
                                } else {
                                    jobj.put("warehouse", "N/A");
                                }
                            }
                            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct()) && product.isIsSerialForProduct()) {
                                ActbatchQty = objArr[6] != null ? Double.parseDouble(objArr[6].toString()) : 0;
                                if (batchQty == 0) {
                                    batchQty = objArr[6] != null ? Double.parseDouble(objArr[6].toString()) : 0;
                                }
                                if (batchQty == ActbatchQty) {
                                    jobj.put("isreadyonly", false);
                                } else {
                                    jobj.put("isreadyonly", true);
                                }
                            } else {
                                jobj.put("isreadyonly", false);
                            }
                            if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && product.isIsSerialForProduct()) {
                                jobj.put("mfgdate", "N/A");
                                jobj.put("expdate", "N/A");
                            } else {
                                jobj.put("mfgdate", objArr[4] != null ? authHandler.getDateFormatter(request).format(objArr[4]) : "N/A");
                                jobj.put("expdate", objArr[5] != null ? authHandler.getDateFormatter(request).format(objArr[5]) : "N/A");
                            }

                            jobj.put("quantity", objArr[6] != null ? objArr[6] : "N/A");

                            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct()) && !product.isIsSerialForProduct()) {
                                jobj.put("quantity", objArr[11] != null ? objArr[11] : "N/A");
                            }
                            jobj.put("serialnoid", objArr[7] != null ? (String) objArr[7] : "N/A");
                            jobj.put("serialno", objArr[8] != null ? (String) objArr[8] : "N/A");
                            jobj.put("expstart", (objArr[9] != null && !objArr[9].toString().equalsIgnoreCase("")) ? authHandler.getDateFormatter(request).format(objArr[9]) : "N/A");
                            jobj.put("expend", (objArr[10] != null && !objArr[10].toString().equalsIgnoreCase("")) ? authHandler.getDateFormatter(request).format(objArr[10]) : "N/A");
                            batchQty--;
                        }
                    } else {
                        jobj.put("serialno", "N/A");
                        jobj.put("batch", "N/A");
                        jobj.put("batchname", "N/A");
                        jobj.put("expstart", "N/A");
                        jobj.put("expend", "N/A");
                        jobj.put("warehouse", "N/A");
                        jobj.put("mfgdate", "N/A");
                        jobj.put("expdate", "N/A");
                    }
                } else {
                    jobj.put("serialno", "N/A");
                    jobj.put("batch", "N/A");
                    jobj.put("batchname", "N/A");
                    jobj.put("expstart", "N/A");
                    jobj.put("expend", "N/A");
                    jobj.put("warehouse", "N/A");
                    jobj.put("mfgdate", "N/A");
                    jobj.put("expdate", "N/A");
                }

                HashMap<String, Object> fieldrequestParams1 = new HashMap();
                fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyId, Constants.Acc_FixedAssets_Details_ModuleId));

                HashMap<String, String> customFieldMap1 = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap1 = new HashMap<String, String>();
                HashMap<String, String> replaceFieldMap11 = new HashMap<String, String>();
                HashMap<String, Integer> fieldMap1 = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap11, customFieldMap1, customDateFieldMap1);

                Map<String, Object> variableMap = new HashMap<String, Object>();
                AssetDetailsCustomData jeDetailCustom = (AssetDetailsCustomData) ad.getAssetDetailsCustomData();
                replaceFieldMap11 = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                    AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMap1, replaceFieldMap11, variableMap);
                    JSONObject params = new JSONObject();
                    params.put("companyid", companyId);
                    params.put("getCustomFieldArray", true);
                    params.put("isExport", true);
                    params.put("isForReport", true);
                    fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap1, customDateFieldMap1, jobj, params);
                }

                jArr.put(jobj);
                groupTotal.put(ad.getProduct().getID(), subtotal);
            }
            if (jArr.length() > 0 && subtotal != null) {
                jArr.put(subtotal);
            }
            grandTotal.put("issummaryvalue", true);
            grandTotal.put("assetId", "Grand Total");
            grandTotal.put("assetGroup", "");
            if (jArr.length() > 0) {
                jArr.put(grandTotal);
            }
            jobCount.put("count", count);
            jArr.put(jobCount);
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jArr;
    }
    public void getDepreciationJsonFormonthwise(int Depreciationyear, JSONObject newjobj, int monthval, JSONObject subtotal, JSONObject grandTotal) throws ServiceException, JSONException {
        if (subtotal.has("month" + Depreciationyear + "_" + monthval)) {
            subtotal.put("month" + Depreciationyear + "_" + monthval, subtotal.getDouble("month" + Depreciationyear + "_" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
        } else {
            subtotal.put("month" + Depreciationyear + "_" + monthval, newjobj.getDouble("firstperiodamtInBase"));
        }
        if (grandTotal.has("month" + Depreciationyear + "_" + monthval)) {
            grandTotal.put("month" + Depreciationyear + "_" + monthval, grandTotal.getDouble("month" + Depreciationyear + "_" + monthval) + newjobj.getDouble("firstperiodamtInBase"));
        } else {
            grandTotal.put("month" + Depreciationyear + "_" + monthval, newjobj.getDouble("firstperiodamtInBase"));
        }
    }
}
