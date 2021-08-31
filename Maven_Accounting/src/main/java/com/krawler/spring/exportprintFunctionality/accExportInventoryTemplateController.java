

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.exportprintFunctionality;

import com.krawler.common.admin.*;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.consignment.Consignment;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentDAO;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentService;
import com.krawler.inventory.model.approval.sa.SAApproval;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.inspection.*;
import com.krawler.inventory.model.ist.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.inventory.model.stockout.StockAdjustmentCustomData;
import com.krawler.inventory.model.stockrequest.RequestStatus;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.assemblyQA.AssemblyProductApprovalDetails;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.customDesign.InventoryCustomDesignerConstants;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.NewsAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class accExportInventoryTemplateController extends MultiActionController{
      
    private VelocityEngine velocityEngine;
    private CustomDesignDAO customDesignDAOObj;
    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private StoreService storeService;
    private LocationService locationService;
    private StockService stockService;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccInvoiceServiceDAO accInvoiceServiceDAOObj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private ConsignmentDAO consignmentDAO;
    private TemplateService templateService;
    private accInvoiceCMN accInvoiceCMNObj;
    private accProductDAO accProductObj;

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setKwlCommonTablesDAOObj(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setAccGoodsReceiptServiceDAOObj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj) {
        this.accGoodsReceiptServiceDAOObj = accGoodsReceiptServiceDAOObj;
    }

    

    public void setAccInvoiceServiceDAOObj(AccInvoiceServiceDAO accInvoiceServiceDAOObj) {
        this.accInvoiceServiceDAOObj = accInvoiceServiceDAOObj;
    }
    
    
    public StoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public accAccountDAO getAccAccountDAOobj() {
        return accAccountDAOobj;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public AccCommonTablesDAO getAccCommonTablesDAO() {
        return accCommonTablesDAO;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public AccountingHandlerDAO getAccountingHandlerDAOobj() {
        return accountingHandlerDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }
    
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
    
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }
        
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setConsignmentDAO(ConsignmentDAO consignmentDAO) {
        this.consignmentDAO = consignmentDAO;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setAccInvoiceCMNObj(accInvoiceCMN accInvoiceCMNObj) {
        this.accInvoiceCMNObj = accInvoiceCMNObj;
    }
    
    public ModelAndView exportSingleStockRequestIssue(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView-empty";
        try {
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String billID = request.getParameter("recordids");
            String invoicePostText="";
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            int moduleid = Integer.parseInt(request.getParameter(Constants.moduleid));
            String transactionno = request.getParameter("transactiono");
            AccCustomData  accCustomData = null;
            HashMap<String, JSONArray> itemDataAgainstInvoice = new HashMap<String, JSONArray>();
            JSONArray lineItemsArr =new JSONArray();
            if (moduleid == Constants.Acc_Stock_Request_ModuleId) {
                lineItemsArr = getStockRequestDetailsItemJSON(request, companyid, billID, transactionno,moduleid);
            } else if (moduleid == Constants.Inventory_ModuleId) {
                lineItemsArr = getStockIssueDetailsItemJSON(request, companyid, billID,transactionno,moduleid);
            }
            itemDataAgainstInvoice.put(billID, lineItemsArr);
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataAgainstInvoice, accCustomData, customDesignDAOObj,accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj,
                    velocityEngine, invoicePostText, otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }  
    
      public JSONArray getStockRequestDetailsItemJSON(HttpServletRequest request, String companyid, String billid,String transactionno,int moduleid) {
          JSONArray jArr = new JSONArray();
          StockRequest stockRequest = null;
          PdfTemplateConfig config = null;
          StockRequestDetail row = null;
          KwlReturnObject idresult = null;
          KwlReturnObject bAmt = null;
          java.util.Date entryDate = null;
          double totalQuantity = 0;
          Iterator itr=null;
          String uomname="",fromStoreName="",toStoreName="",orderStatus="",createdby="";
          JSONObject summaryData = new JSONObject();
        try {
            int quantitydigitafterdecimal=2,amountdigitafterdecimal=2,unitpricedigitafterdecimal=2;
            boolean isgstincluded=false; //flag to check includedgst
          
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Integer.parseInt(request.getParameter("moduleid")));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            int rowcnt = 0;
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
                KwlReturnObject srresult = accountingHandlerDAOobj.getObject(StockRequest.class.getName(), billid);
                stockRequest = (StockRequest) srresult.getEntityList().get(0);
                
                if(stockRequest.getToStore()!=null &&stockRequest.getFromStore()!=null){//From Store and To Store Calculation
                    Store fromStore = storeService.getStoreById(stockRequest.getFromStore().getId());
                    Store toStore = storeService.getStoreById( stockRequest.getToStore().getId());
                    fromStoreName=fromStore.getFullName();
                    toStoreName = toStore.getFullName();
                    CommonFunctions.getTotalAddressofStore(fromStore, true, summaryData);//FromStoreAddress
                    CommonFunctions.getTotalAddressofStore(toStore, false, summaryData);//ToStoreAddress
                }
                
                if (stockRequest.getRequestedBy() != null) {//MoD Value
                    createdby = stockRequest.getRequestedBy().getFullName();
                }
                Map<String, Object> requestParams= new HashMap<String, Object>();
                requestParams.put("transactionno",transactionno);
                requestParams.put("companyid",companyid);
                requestParams.put(Constants.moduleid,moduleid);
                KwlReturnObject productresult= storeService.getInventoryProductDetails(requestParams);
                List productlist = productresult.getEntityList();
                
                //getting Recommended Retail Price
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
                KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
                HashMap<String, Object> requestParamsPOS = new HashMap<String, Object>();
                requestParamsPOS.put("isSyncToPOS", true);
                requestParamsPOS.put("companyID", sessionHandlerImpl.getCompanyid(request));
                requestParamsPOS.put("currencyID", currency.getCurrencyID());
                KwlReturnObject result = accMasterItemsDAOobj.getPOSProductsPrice(requestParamsPOS);
                List<PricingBandMasterDetail> list = result.getEntityList();

                    for(int i=0;i<productlist.size();i++){
                        rowcnt++;
                        JSONObject obj = new JSONObject();
                        Object[] srobject = (Object[]) productlist.get(i);
                        String itemId = srobject[0] != null ? srobject[0].toString() : "";
                        String uomId = srobject[1] != null ? srobject[1].toString() : "";
                        String costCenterId = srobject[2] != null ? srobject[2].toString() : "";
                        double quantity = Double.parseDouble(srobject[4] != null ? srobject[4].toString() : "0");
                        String projectNumber = srobject[5] != null ? srobject[5].toString() : "";
                        String remark = srobject[6] != null ? srobject[6].toString() : "";
                        String stockIssueId = srobject[7] != null ? srobject[7].toString() : "";
                        StockRequest stockRequestObj = null;
                        String costCenterID = "", costCenterName = "";
                        
                        KwlReturnObject inventoryresult = accountingHandlerDAOobj.getObject(StockRequest.class.getName(), stockIssueId);
                        stockRequestObj = (StockRequest) inventoryresult.getEntityList().get(0);
                        /**
                         * while print from Stock Request and Stock Issue.
                         * Deleted and Rejected records not shown.
                         */
                        if (stockRequestObj.getStatus().equals(RequestStatus.DELETED) || stockRequestObj.getStatus().equals(RequestStatus.REJECTED)) {
                            rowcnt--;
                            continue;
                        }
                        KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Product.class.getName(), itemId);
                        Product product = (Product) jeresult.getEntityList().get(0);
                        if (!StringUtil.isNullOrEmpty(uomId)) {
                            jeresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), uomId);
                            UnitOfMeasure uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                            uomname = uom.getNameEmptyforNA();
                        }
                        if (!StringUtil.isNullOrEmpty(costCenterId)) {
                            jeresult = accountingHandlerDAOobj.getObject(CostCenter.class.getName(), costCenterId);
                            CostCenter costCenter = (CostCenter) jeresult.getEntityList().get(0);
                            costCenterID = costCenter.getCcid();
                            costCenterName = costCenter.getName();
                        }
                        //                InventoryWarehouse warehouse = product.getWarehouse();
                        for (PricingBandMasterDetail pricingBandMasterDetailObj : list) {//Retail Price of Product
                            if (pricingBandMasterDetailObj.getProduct().equals(product.getID())) {
                                obj.put(CustomDesignerConstants.DO_RRP, pricingBandMasterDetailObj.getSalesPrice());//RRP
                            }
                        }
                        
                        totalQuantity += quantity;
                        obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                        obj.put(CustomDesignerConstants.IN_ProductCode, product.getProductid().replaceAll("\n", "<br>"));  
                        obj.put(CustomDesignerConstants.ProductName, product.getName().replaceAll("\n", "<br>"));  //product name
                        obj.put(CustomDesignerConstants.Packaging, product.getPackaging() != null ? product.getPackaging().toString() : "");  //packaging
                        obj.put(CustomDesignerConstants.Location, product.getLocation() != null ? product.getLocation().getName() : "");  //location
                        obj.put(CustomDesignerConstants.UOM, uomname);
                        obj.put(CustomDesignerConstants.OrderQuantity, quantity);
                        obj.put(CustomDesignerConstants.Quantity, quantity);
                        obj.put(CustomDesignerConstants.CostCenter, costCenterID); 
                        obj.put(CustomDesignerConstants.CostCenterName, costCenterName); 
                        obj.put(CustomDesignerConstants.ProjectNo, projectNumber);
                        obj.put(CustomDesignerConstants.SR_Remark, remark);
                        obj.put(CustomDesignerConstants.AdditionalDescription, product.getAdditionalDesc() != null ? product.getAdditionalDesc().replaceAll("\n", "<br>") : "");  //product Additional Description
                        obj.put(CustomDesignerConstants.ProductDescription, product.getDescription() != null ? product.getDescription().replaceAll("\n", "<br>") : "");  //product Description
                        obj.put(CustomDesignerConstants.ProductBarcode, product.getBarcode() != null ? product.getBarcode().replaceAll("\n", "<br>") : "");  //product Bar Code
                        obj.put("isGstIncluded", isgstincluded);
                        jArr.put(obj);
                    }
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.FromStore,fromStoreName);
            summaryData.put(CustomDesignerConstants.ToStore, toStoreName);
            summaryData.put(CustomDesignerConstants.OrderStatus, orderStatus);
            summaryData.put(CustomDesignerConstants.Createdby,createdby);
            
            jArr.put(summaryData);
        } catch (Exception ex) {
             Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    } 
      
/*Stock Issue*/
    public JSONArray getStockIssueDetailsItemJSON(HttpServletRequest request, String companyid, String billid, String transactionno,int moduleid) {
        JSONArray jArr = new JSONArray();
        StockRequest stockRequest = null;
        PdfTemplateConfig config = null;
        KwlReturnObject idresult = null;
        double totalQuantity = 0;
        Iterator itr = null;
        JSONObject summaryData = new JSONObject();
        String uomname = "", fromStoreName = "", toStoreName = "", orderStatus = "", createdby = "";
        try {
            boolean isgstincluded = false; //flag to check includedgst
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Integer.parseInt(request.getParameter("moduleid")));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            int rowcnt = 0;
            KwlReturnObject srresult = accountingHandlerDAOobj.getObject(StockRequest.class.getName(), billid);
            stockRequest = (StockRequest) srresult.getEntityList().get(0);

            if (stockRequest.getToStore() != null && stockRequest.getFromStore() != null) {//From Store and To Store Calculation
                Store fromStore = storeService.getStoreById(stockRequest.getFromStore().getId());
                Store toStore = storeService.getStoreById(stockRequest.getToStore().getId());
                fromStoreName = fromStore.getFullName();
                toStoreName = toStore.getFullName();
                CommonFunctions.getTotalAddressofStore(fromStore,true,summaryData);//FromStoreAddress
                CommonFunctions.getTotalAddressofStore(toStore,false,summaryData);//ToStoreAddress
            }

            if (stockRequest.getRequestedBy() != null) {//MoD Value
                createdby = stockRequest.getRequestedBy().getFullName();
            }
            orderStatus = "Collected";
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("transactionno", transactionno);
            requestParams.put("companyid", companyid);
            requestParams.put(Constants.moduleid,moduleid);
            KwlReturnObject productresult = storeService.getInventoryProductDetails(requestParams);
            List productlist = productresult.getEntityList();
            
            //getting Recommended Retail Price
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, Object> requestParamsPOS = new HashMap<String, Object>();
            requestParamsPOS.put("isSyncToPOS", true);
            requestParamsPOS.put("companyID", sessionHandlerImpl.getCompanyid(request));
            requestParamsPOS.put("currencyID", currency.getCurrencyID());
            KwlReturnObject result = accMasterItemsDAOobj.getPOSProductsPrice(requestParamsPOS);
            List<PricingBandMasterDetail> list = result.getEntityList();

            for (int i = 0; i < productlist.size(); i++) {
                rowcnt++;
                String snos = "", bnos = "", isslnames = "", collectlname = "";
                JSONObject obj = new JSONObject();
                Object[] srobject = (Object[]) productlist.get(i);
                String itemId = srobject[0] != null ? srobject[0].toString() : "";
                String uomId = srobject[1] != null ? srobject[1].toString() : "";
                String costCenterId = srobject[2] != null ? srobject[2].toString() : "";
                double quantity = Double.parseDouble(srobject[4] != null ? srobject[4].toString() : "0");
                String projectNumber = srobject[5] != null ? srobject[5].toString() : "";
                String remark = srobject[6] != null ? srobject[6].toString() : "";
                String stockIssueId = srobject[7] != null ? srobject[7].toString() : "";
                StockRequest stockRequestObj = null;
                String costCenterID = "", costCenterName = "";

                KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Product.class.getName(), itemId);
                Product product = (Product) jeresult.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(uomId)) {
                    jeresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), uomId);
                    UnitOfMeasure uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                    uomname = uom.getNameEmptyforNA();
                }
                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    jeresult = accountingHandlerDAOobj.getObject(CostCenter.class.getName(), costCenterId);
                    CostCenter costCenter = (CostCenter) jeresult.getEntityList().get(0);
                    costCenterID = costCenter.getCcid();
                    costCenterName = costCenter.getName();
                }

                //Inventory Batch Details
                HashMap<String, String> issuedlocationnamesmap = new HashMap<String, String>();
                HashMap<String, String> collectlocationnamesmap = new HashMap<String, String>();
                HashMap<String, String> batchnamesmap = new HashMap<String, String>();
                HashMap<String, String> serialnumbersmap = new HashMap<String, String>();
                Set<StockRequestDetail> requestDetailSet = new HashSet<StockRequestDetail>();
                String IssueLocation = "", CollectLocation = "", Batchnos = "", Serialnos = "";
                
                KwlReturnObject inventoryresult = accountingHandlerDAOobj.getObject(StockRequest.class.getName(), stockIssueId);
                stockRequestObj = (StockRequest) inventoryresult.getEntityList().get(0);
                /**
                 * while print from Stock Request and Stock Issue.
                 * Deleted and Rejected records not shown. 
                 */
                if(stockRequestObj.getStatus().equals(RequestStatus.DELETED) || stockRequestObj.getStatus().equals(RequestStatus.REJECTED) ){
                    rowcnt--;
                    continue;
                }
                int pos = 1;
                if(stockRequestObj.getStockRequestDetails() != null){
                    requestDetailSet = stockRequestObj.getStockRequestDetails();
                    for (StockRequestDetail requestDetailvalue : requestDetailSet) {
                        bnos = requestDetailvalue.getBatchName();
                        batchnamesmap.put("batch"+pos, bnos);
                        isslnames = requestDetailvalue.getIssuedLocation().getName();
                        issuedlocationnamesmap.put("issuedlocation"+pos, isslnames);
                        collectlname = requestDetailvalue.getDeliveredLocation().getName();
                        collectlocationnamesmap.put("collectlocation"+pos, collectlname);
                        snos = requestDetailvalue.getDeliveredSerialNames() != null ? requestDetailvalue.getDeliveredSerialNames() : "";
                        serialnumbersmap.put("serial"+pos, snos);
                        pos++;
                    }
                    for (PricingBandMasterDetail pricingBandMasterDetailObj : list) {//Retail Price of Product
                        if (pricingBandMasterDetailObj.getProduct().equals(product.getID())) {
                            obj.put(CustomDesignerConstants.DO_RRP, pricingBandMasterDetailObj.getSalesPrice());//RRP
                        }
                    }
                    // set issued location
                    for(int cnt = 1; cnt <= issuedlocationnamesmap.size(); cnt++){
                        String lno = "";
                        lno = issuedlocationnamesmap.get("issuedlocation"+cnt);
                        if(!StringUtil.isNullOrEmpty(lno)){
                            IssueLocation += lno.concat("!##");
                        }
                    }
                    // set collect location
                    for(int cnt = 1; cnt <= collectlocationnamesmap.size(); cnt++){
                        String lno = "";
                        lno = collectlocationnamesmap.get("collectlocation"+cnt);
                        if(!StringUtil.isNullOrEmpty(lno)){
                            CollectLocation += lno.concat("!##");
                        }
                    }
                    // set serial no
                    for(int cnt = 1; cnt <= serialnumbersmap.size(); cnt++){
                        String sn = "";
                        sn = serialnumbersmap.get("serial"+cnt);
                        if(!StringUtil.isNullOrEmpty(sn)){
                            Serialnos += sn.concat("!##");
                        }
                    }
                    // set batch no
                    for(int cnt = 1; cnt <= batchnamesmap.size(); cnt++){
                        String bat = "";
                        bat = batchnamesmap.get("batch"+cnt);
                        if(!StringUtil.isNullOrEmpty(bat)){
                            Batchnos += bat.concat("!##");
                        }
                    }
                    //remove extra <hr> tag from ending of location, serial no and batch no
                    if (!StringUtil.isNullOrEmpty(IssueLocation)) {
                        IssueLocation = IssueLocation.substring(0, IssueLocation.length() - 3);
                    }

                    if (!StringUtil.isNullOrEmpty(CollectLocation)) {
                        CollectLocation = CollectLocation.substring(0, CollectLocation.length() - 3);
                    }

                    if (!StringUtil.isNullOrEmpty(Serialnos)) {
                        Serialnos = Serialnos.substring(0, Serialnos.length() - 3);
                    }
                    if (!StringUtil.isNullOrEmpty(Batchnos.toString())) {
                        Batchnos = Batchnos.substring(0, Batchnos.length() - 3);
                    }
                }
                totalQuantity += quantity;
                obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                obj.put(CustomDesignerConstants.IN_ProductCode, product.getProductid().replaceAll("\n", "<br>"));
                obj.put(CustomDesignerConstants.ProductName, product.getName().replaceAll("\n", "<br>"));  //product name
                obj.put(CustomDesignerConstants.ProductBarcode, product.getBarcode() == null ? "" : product.getBarcode());//Product Bar Code
                obj.put(CustomDesignerConstants.Packaging, product.getPackaging() != null ? product.getPackaging().toString() : "");  //packaging
                obj.put(CustomDesignerConstants.UOM, uomname);
                obj.put(CustomDesignerConstants.Quantity, quantity);
                obj.put(CustomDesignerConstants.ISSUED_QUANTITY, stockRequestObj.getIssuedQty());
                obj.put(CustomDesignerConstants.DELIVERED_QUANTITY, stockRequestObj.getDeliveredQty());
                obj.put(CustomDesignerConstants.CostCenter, costCenterID);
                obj.put(CustomDesignerConstants.CostCenterName, costCenterName);
                obj.put(CustomDesignerConstants.ProjectNo, projectNumber);
                obj.put(CustomDesignerConstants.SR_Remark, remark);
                obj.put(CustomDesignerConstants.CollectLocation, CollectLocation);
                obj.put(CustomDesignerConstants.Location, IssueLocation);
                obj.put(CustomDesignerConstants.SerialNumber, Serialnos);
                obj.put(CustomDesignerConstants.BatchNumber, Batchnos);
                obj.put(CustomDesignerConstants.AdditionalDescription, product.getAdditionalDesc() != null ? product.getAdditionalDesc().replaceAll("\n", "<br>") : "");  //product Additional Description
                obj.put(CustomDesignerConstants.ProductDescription, product.getDescription() != null ? product.getDescription().replaceAll("\n", "<br>") : "");  //product Description
                obj.put(CustomDesignerConstants.ProductBarcode, product.getBarcode() != null ? product.getBarcode().replaceAll("\n", "<br>") : "");  //product Bar Code
                obj.put("isGstIncluded", isgstincluded);
                jArr.put(obj);
            }
          
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.FromStore, fromStoreName);
            summaryData.put(CustomDesignerConstants.ToStore, toStoreName);
            summaryData.put(CustomDesignerConstants.OrderStatus, orderStatus);
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);//Total Quantity

            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public ModelAndView exportSingleStockAdjustment(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView-empty";
        try {
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String invoicePostText = "", recordids = "", transactionnos = "";
            AccCustomData accCustomData = null;
            HashMap<String, JSONArray> itemDataAgainstInvoice = new HashMap<String, JSONArray>();
            JSONArray lineItemsArr = new JSONArray();

            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> billIDs = CustomDesignHandler.getSelectedBillIDs(recordids);//getting array of id's
            
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            int moduleid = Integer.parseInt(request.getParameter(Constants.moduleid));
            if (!StringUtil.isNullOrEmpty(request.getParameter("transactiono"))) {
                transactionnos = request.getParameter("transactiono");
            }
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            /*
             * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
             */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);
            
            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

            //For product custom field
            fieldrequestParams.clear();
            HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
            
            ArrayList<String> transactionNos = CustomDesignHandler.getSelectedBillIDs(transactionnos);//getting array of transaction no's
            
            for (int count = 0; count < billIDs.size(); count++) {
                lineItemsArr = getStockAdjustmentDetailsItemJSON(request, companyid, billIDs.get(count), transactionNos.get(count),moduleid, FieldMap, DimensionFieldMap, LineLevelCustomFieldMap, ProductLevelCustomFieldMap);
                itemDataAgainstInvoice.put(billIDs.get(count), lineItemsArr);
            }
            
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataAgainstInvoice, accCustomData, customDesignDAOObj,accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj,
                    velocityEngine, invoicePostText, otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }  

    /*
     * Stock Adjustment
     */
    public JSONArray getStockAdjustmentDetailsItemJSON(HttpServletRequest request, String companyid, String billid, String transactionno,int moduleid, HashMap<String, Integer> FieldMap, HashMap<String, Integer> DimensionFieldMap, HashMap<String, Integer> LineLevelCustomFieldMap, HashMap<String, Integer> ProductLevelCustomFieldMap) {
        JSONArray jArr = new JSONArray();
        StockAdjustment stockAdj = null;
        PdfTemplateConfig config = null;
        StockRequestDetail srd = null;
        String uomname = "", globallevelcustomfields = "", globalleveldimensions = "", fromStoreName = "",fromStoreCode = "",fromStoreDesc = "", fromStoreAddress = "", orderStatus = "", createdby = "", reasonname = "";
        double totalIn = 0, totalOut = 0, totalInQty = 0, totalOutQty = 0;
        try {
            JSONObject requestObj = StringUtil.convertRequestToJsonObject(request);
            boolean isgstincluded = false; //flag to check includedgst
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Integer.parseInt(request.getParameter("moduleid")));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            int quantitydigitafterdecimal = 2;
            int rowcnt = 0;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("transactionno", transactionno);
            requestParams.put("companyid", companyid);
            requestParams.put(Constants.moduleid,moduleid);
            KwlReturnObject productresult = storeService.getInventoryProductDetails(requestParams);
            List productlist = productresult.getEntityList();
            
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                
            }
            //load company preferences and extra company preferences objects
            KwlReturnObject pref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preference = (CompanyAccountPreferences) pref.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            
            String customerId = "", customerCode = "", customerName = "", jwInOrderNo = "", jwBatchName = "", customerGstinNumber = "";
            for (int i = 0; i < productlist.size(); i++) {
                rowcnt++;
                String snos = "", bnos = "",locationname = "",Location = "", Batchnos = "", Serialnos = "", subQties = "", batchexpDates = "",serialexpDates = "", assets = "", asset = "";
                String batchMfgDts = "", serialMfgDts = "";
                double subqty = 0.0;
                Date batchexpDate, batchMfgDate;
                Date serialexpDate, serialMfgDate;
                JSONObject obj = new JSONObject();
                Object[] srobject = (Object[]) productlist.get(i);
                String itemId = srobject[0] != null ? srobject[0].toString() : "";
                String uomId = srobject[1] != null ? srobject[1].toString() : "";
                String costCenterId = srobject[2] != null ? srobject[2].toString() : "";
                double quantity = Double.parseDouble(srobject[4] != null ? srobject[4].toString() : "0");
                String reason = srobject[5] != null ? srobject[5].toString() : "";
                String remark = srobject[6] != null ? srobject[6].toString() : "";
                double perunitprice = Double.parseDouble(srobject[7] != null ? srobject[7].toString() : "0");
                String adjustmenttype = srobject[8] != null ? srobject[8].toString() : "";
                String stockAdjustmentId = srobject[9] != null ? srobject[9].toString() : "";
                String costCenterID = "", costCenterName = "";

                if(adjustmenttype.equals("Stock IN")){
                    totalIn += (perunitprice * quantity);
                    totalInQty += quantity;
                } else if(adjustmenttype.equals("Stock Out") || adjustmenttype.equals("Stock Sales")){
                    quantity = Math.abs(quantity);         // get absolute value for removing - (minus) symbol
                    totalOut += (perunitprice * quantity);
                    totalOutQty += quantity;
                }
                
                KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Product.class.getName(), itemId);
                Product product = (Product) jeresult.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(uomId)) {
                    jeresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), uomId);
                    UnitOfMeasure uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                    uomname = uom.getNameEmptyforNA();
                }
                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    jeresult = accountingHandlerDAOobj.getObject(CostCenter.class.getName(), costCenterId);
                    CostCenter costCenter = (CostCenter) jeresult.getEntityList().get(0);
                    costCenterID = costCenter.getCcid();
                    costCenterName = costCenter.getName();
                }

                if (!StringUtil.isNullOrEmpty(reason)) {
                    jeresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), reason);
                    MasterItem masterreason = (MasterItem) jeresult.getEntityList().get(0);
                    reasonname = masterreason.getValue();
                }

                //Inventory Batch Details
                Set<StockAdjustmentDetail> adjustmentDetailSet = new HashSet<StockAdjustmentDetail>();

                KwlReturnObject srresult = accountingHandlerDAOobj.getObject(StockAdjustment.class.getName(), stockAdjustmentId);
                stockAdj = (StockAdjustment) srresult.getEntityList().get(0);

                if (stockAdj.getStore() != null) {//From Store and To Store Calculation
                    Store fromStore = storeService.getStoreById(stockAdj.getStore().getId());
                    fromStoreName = fromStore.getFullName();
                    fromStoreCode = fromStore.getAbbreviation();
                    fromStoreDesc = fromStore.getDescription();
                    fromStoreAddress = fromStore.getAddress();
                }

                if (stockAdj.getCreator() != null) {//MoD Value
                    createdby = stockAdj.getCreator().getFullName();
                }
                
                if (stockAdj.getStockAdjustmentDetail() != null) {
                    HashMap<String, String> locationnames = new HashMap<String, String>();
                    HashMap<String, String> batchnames = new HashMap<String, String>();
                    HashMap<String, String> serialnumbers = new HashMap<String, String>();
                    HashMap<String, String> subQuantities = new HashMap<String, String>();
                    HashMap<String, String> batchexpiryDates = new HashMap<String, String>();
                    HashMap<String, String> serialexpiryDates = new HashMap<String, String>();
                    HashMap<String, String> batchMfgDates = new HashMap<String, String>();
                    HashMap<String, String> serialMfgDates = new HashMap<String, String>();
                    HashMap<String, String> assetMap = new HashMap<String, String>();
                    int pos = 1;
                    adjustmentDetailSet = stockAdj.getStockAdjustmentDetail();
                    for (StockAdjustmentDetail adjustmentDetailvalue : adjustmentDetailSet) {
                        subqty =  adjustmentDetailvalue.getFinalQuantity();
                        subQuantities.put("subQty"+pos,String.valueOf(subqty));
                        bnos = adjustmentDetailvalue.getBatchName();
                        batchnames.put("batch"+pos, bnos);
                        locationname = adjustmentDetailvalue.getLocation().getName();
                        locationnames.put("location"+pos, locationname);
                        snos = adjustmentDetailvalue.getSerialNames();
                        serialnumbers.put("serial" + pos, snos);
                        NewProductBatch productBatch = stockService.getERPProductBatch(stockAdj.getProduct(), stockAdj.getStore(), adjustmentDetailvalue.getLocation(), adjustmentDetailvalue.getRow(), adjustmentDetailvalue.getRack(), adjustmentDetailvalue.getBin(), adjustmentDetailvalue.getBatchName());
                        if(productBatch != null) {
                            batchexpDate = productBatch.getExpdate();
                            batchexpiryDates.put("batchexpdate" + pos, batchexpDate != null ? df.format(batchexpDate): "");
                            batchMfgDate = productBatch.getMfgdate();
                            batchMfgDates.put("batchmfgdate" + pos, batchMfgDate != null ? df.format(batchMfgDate): "");
                            if (!StringUtil.isNullOrEmpty(snos)) {
                                String[] snosArr = snos.split(",");
                                String asst = "";
                                for (String srl : snosArr) {
                                    NewBatchSerial productSerial = stockService.getERPBatchSerial(stockAdj.getProduct(), productBatch, srl);
                                    if (productSerial != null) {
                                        serialexpDate = productSerial.getExptodate();
                                        serialexpiryDates.put("serialexpdate" + pos, serialexpDate != null ? df.format(serialexpDate): "");
                                        serialMfgDate = productSerial.getExpfromdate();
                                        serialMfgDates.put("serialmfgdate" + pos, serialMfgDate != null ? df.format(serialMfgDate): "");

                                        asst += productSerial.getSkufield()!= null ? productSerial.getSkufield() :"";
                                    }
                                }
                                assetMap.put("asset"+pos, asst);
                                
                            }
                        }
                        adjustmentDetailvalue.getSerialNames();
                        adjustmentDetailvalue.getFinalSerialNames();
                        /**
                         * Get customer name, sales order no from linked sales order
                         * Get challan number
                         */
                        if(adjustmentDetailvalue.getJobworkorder() != null){
                            if(adjustmentDetailvalue.getJobworkorder().getCustomer() != null){
                                customerName = adjustmentDetailvalue.getJobworkorder().getCustomer().getName() != null ? adjustmentDetailvalue.getJobworkorder().getCustomer().getName() : "";
                                customerId = adjustmentDetailvalue.getJobworkorder().getCustomer().getID() != null ? adjustmentDetailvalue.getJobworkorder().getCustomer().getID() : "";
                                customerCode = adjustmentDetailvalue.getJobworkorder().getCustomer().getAcccode()!= null ? adjustmentDetailvalue.getJobworkorder().getCustomer().getAcccode(): "";
                                customerGstinNumber = adjustmentDetailvalue.getJobworkorder().getCustomer().getGSTIN() != null ? adjustmentDetailvalue.getJobworkorder().getCustomer().getGSTIN() : "";
                            }
                            jwInOrderNo = adjustmentDetailvalue.getJobworkorder().getSalesOrderNumber() != null ? adjustmentDetailvalue.getJobworkorder().getSalesOrderNumber() : "";
                        }
                        jwBatchName = adjustmentDetailvalue.getBatchName() != null ? adjustmentDetailvalue.getBatchName() : "";
                        
                        pos++;
                    }
                    // set location
                    for(int cnt = 1; cnt <= locationnames.size(); cnt++){
                        String lno = "";
                        lno = locationnames.get("location"+cnt);
                        if(!StringUtil.isNullOrEmpty(lno)){
                            Location += lno.concat("!##");
                        }
                    }
                    // set serial no
                    for(int cnt = 1; cnt <= serialnumbers.size(); cnt++){
                        String sn = "";
                        sn = serialnumbers.get("serial"+cnt);
                        if(!StringUtil.isNullOrEmpty(sn)){
                            Serialnos += sn.concat("!##");
                        }
                    }
                    // set batch no
                    for(int cnt = 1; cnt <= batchnames.size(); cnt++){
                        String bat = "";
                        bat = batchnames.get("batch"+cnt);
                        if(!StringUtil.isNullOrEmpty(bat)){
                            Batchnos += bat.concat("!##");
                        }
                    }
                    // set Batch sub Quantity
                    for(int cnt = 1; cnt <= subQuantities.size(); cnt++){
                        String qty = "";
                        qty = subQuantities.get("subQty"+cnt)+ " " +uomname;
                        if(!StringUtil.isNullOrEmpty(qty)){
                            subQties += qty.concat("!##");
                        }
                    }
                    // set Batch Expiry Date
                    for(int cnt = 1; cnt <= batchexpiryDates.size(); cnt++){
                        String date = "";
                        date = batchexpiryDates.get("batchexpdate"+cnt);
                        if(!StringUtil.isNullOrEmpty(date)){
                            batchexpDates += date.concat("!##");
                        }
                    }
                    // set serial Expiry Date
                    for(int cnt = 1; cnt <= serialexpiryDates.size(); cnt++){
                        String date = "";
                        date = serialexpiryDates.get("serialexpdate"+cnt);
                        if(!StringUtil.isNullOrEmpty(date)){
                            serialexpDates += date.concat("!##");
                        }
                    }
                    // set Batch Mfg Date
                    for(int cnt = 1; cnt <= batchMfgDates.size(); cnt++){
                        String date = "";
                        date = batchMfgDates.get("batchmfgdate"+cnt);
                        if(!StringUtil.isNullOrEmpty(date)){
                            batchMfgDts += date.concat("!##");
                        }
                    }
                    // set serial Expiry Date
                    for(int cnt = 1; cnt <= serialMfgDates.size(); cnt++){
                        String date = "";
                        date = serialMfgDates.get("serialmfgdate"+cnt);
                        if(!StringUtil.isNullOrEmpty(date)){
                            serialMfgDts += date.concat("!##");
                        }
                    }
                    // set Assets
                    for(int cnt = 1; cnt <= assetMap.size(); cnt++){
                        String assetStr = "";
                        assetStr = assetMap.get("asset"+cnt);
                        if(!StringUtil.isNullOrEmpty(assetStr)){
                            assets += assetStr.concat("!##");
                        }
                    }
                    //remove extra <hr> tag from ending of location, serial no and batch no
                    if (!StringUtil.isNullOrEmpty(Location)) {
                        Location = Location.substring(0, Location.length() - 3);//remove extra <hr> tag
                    }

                    if (!StringUtil.isNullOrEmpty(Serialnos)) {
                        Serialnos = Serialnos.substring(0, Serialnos.length() - 3);//remove extra <hr> tag
                    }
                    if (!StringUtil.isNullOrEmpty(Batchnos)) {
                        Batchnos = Batchnos.substring(0, Batchnos.length() - 3);//remove extra <hr> tag
                    }
                    if (!StringUtil.isNullOrEmpty(subQties)) {
                        subQties = subQties.substring(0, subQties.length() - 3);//remove extra <hr> tag
                    }
                    if (!StringUtil.isNullOrEmpty(batchexpDates)) {
                        batchexpDates = batchexpDates.substring(0, batchexpDates.length() - 3);//remove extra <hr> tag
                    }
                    if (!StringUtil.isNullOrEmpty(serialexpDates)) {
                        serialexpDates = serialexpDates.substring(0, serialexpDates.length() - 3);//remove extra <hr> tag
                    }
                    if (!StringUtil.isNullOrEmpty(batchMfgDts)) {
                        batchMfgDts = batchMfgDts.substring(0, batchMfgDts.length() - 3);//remove extra comma
                    }
                    if (!StringUtil.isNullOrEmpty(serialMfgDts)) {
                        serialMfgDts = serialMfgDts.substring(0, serialMfgDts.length() - 3);//remove extra comma
                    }
                    if (!StringUtil.isNullOrEmpty(assets)) {
                        assets = assets.substring(0, assets.length() - 3);//remove extra <hr> tag
                    }
                }    //adjustment end case  

                obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                obj.put(CustomDesignerConstants.IN_ProductCode, product.getProductid().replaceAll("\n", "<br>"));
                obj.put(CustomDesignerConstants.ProductName, product.getName().replaceAll("\n", "<br>"));  //product name
                obj.put(CustomDesignerConstants.ProductDescription, product.getDescription().replaceAll("\n", "<br>"));  //product name
                obj.put(CustomDesignerConstants.ProductBarcode, product.getBarcode() != null ? product.getBarcode().replaceAll("\n", "<br>") : "");  //product Bar Code
                obj.put(CustomDesignerConstants.UOM, uomname);
                obj.put(CustomDesignerConstants.CostCenter, costCenterID);
                obj.put(CustomDesignerConstants.CostCenterName, costCenterName);
                obj.put(CustomDesignerConstants.Quantity, quantity);
                obj.put(CustomDesignerConstants.AdjustmentType, ("Stock In").equalsIgnoreCase(adjustmenttype) ? "IN" : (("Stock Out").equalsIgnoreCase(adjustmenttype) ? "OUT" : "SALES") );
                obj.put(CustomDesignerConstants.SR_Reason, reasonname);
                obj.put(CustomDesignerConstants.SR_Batch_SubQty, subQties);
                obj.put(CustomDesignerConstants.SR_Batch_Exp_Date, batchexpDates);
                obj.put(CustomDesignerConstants.SR_Serial_Exp_Date, serialexpDates);
                obj.put(CustomDesignerConstants.ManufacturingDate_Batch, batchMfgDts);
                obj.put(CustomDesignerConstants.ManufacturingDate_Serial, serialMfgDts);
                obj.put(CustomDesignerConstants.SR_Batch_SubQty_UOM, uomname);
                obj.put(CustomDesignerConstants.SR_Asset, assets);
                obj.put(CustomDesignerConstants.SR_Remark, remark);
                obj.put(CustomDesignerConstants.PerUnitPrice, perunitprice);
                obj.put(CustomDesignerConstants.Location, Location);
                obj.put(CustomDesignerConstants.SerialNumber, Serialnos);
                obj.put(CustomDesignerConstants.BatchNumber, Batchnos);
                obj.put("isGstIncluded", isgstincluded);
                /*
                 * get custom line data
                 */
                Map<String, Object> variableMap = new HashMap<>();
                
                StockAdjustmentCustomData stockDetailCustom = stockAdj.getStockAdjustmentLineLevelCustomData();
                if (stockDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, stockDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                /*Set All Line level Dimension & All LIne level Custom Field Values*/   
                obj=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap,variableMap,obj,false);//for dimensions
                obj=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap,variableMap,obj,true);//for customfields
                jArr.put(obj);
            }
            /*
             * Other fields Calculation
             */
            JSONObject summaryData = new JSONObject();
            summaryData.put("summarydata", true);
            /*All Global Section Custom Field and DImensions*/
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, moduleid);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", billid);
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.FromStore, fromStoreName);
            summaryData.put(CustomDesignerConstants.FromStoreDesc, fromStoreDesc);
            summaryData.put(CustomDesignerConstants.FromStoreCode, fromStoreCode);
            summaryData.put(CustomDesignerConstants.FromStoreAddress, fromStoreAddress);
            summaryData.put(CustomDesignerConstants.OrderStatus, orderStatus);
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            summaryData.put(CustomDesignerConstants.TOTALIN, totalIn);
            summaryData.put(CustomDesignerConstants.TOTALOUT, totalOut);
            summaryData.put(CustomDesignerConstants.TOTAL_IN_QTY, totalInQty);
            summaryData.put(CustomDesignerConstants.TOTAL_OUT_QTY, totalOutQty);
            
            /**
             * Get Customer address details
             */
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("customerid", customerId);
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData, preference, extraCompanyPreferences);
            //Put details in json
            summaryData.put(CustomDesignerConstants.CUSTOMER_NAME, customerName);
            summaryData.put(CustomDesignerConstants.CustomerVendor_AccCode, customerCode);
            summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, customerGstinNumber);
            summaryData.put(CustomDesignerConstants.JOB_WORK_IN_ORDER_NO, jwInOrderNo);
            summaryData.put(CustomDesignerConstants.JOB_WORK_CHALLAN_NO, jwBatchName);
            
            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    public ModelAndView exportSingleStockRepair(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView-empty";
        try {
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String invoicePostText = "", recordids = "", transactionnos = "",transactionmodules="";
            AccCustomData accCustomData = null;
            HashMap<String, JSONArray> itemDataAgainstInvoice = new HashMap<String, JSONArray>();
            JSONArray lineItemsArr = new JSONArray();

            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> billIDs = CustomDesignHandler.getSelectedBillIDs(recordids);//getting array of id's
            
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            int moduleid = Integer.parseInt(request.getParameter(Constants.moduleid));
            if (!StringUtil.isNullOrEmpty(request.getParameter("transactiono"))) {
                transactionnos = request.getParameter("transactiono");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("transactionmodules"))) {
                transactionmodules = request.getParameter("transactionmodules");
            }
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            /*
             * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
             */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);
            
            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

            //For product custom field
            fieldrequestParams.clear();
            HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
            
            ArrayList<String> transactionNos = CustomDesignHandler.getSelectedBillIDs(transactionnos);//getting array of transaction no's
            ArrayList<String> transactionmodule = CustomDesignHandler.getSelectedBillIDs(transactionmodules);//getting array of transaction no's
            for(int count = 0; count < billIDs.size(); count++) {
                lineItemsArr = getStockRepairDetailsItemJSON(request, companyid, billIDs.get(count), transactionNos.get(count),moduleid, FieldMap, DimensionFieldMap, LineLevelCustomFieldMap, ProductLevelCustomFieldMap, transactionmodule.get(count));
                itemDataAgainstInvoice.put(billIDs.get(count), lineItemsArr);
            }
            
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataAgainstInvoice, accCustomData, customDesignDAOObj,accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj,
                    velocityEngine, invoicePostText, otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public JSONArray getStockRepairDetailsItemJSON(HttpServletRequest request, String companyid, String billid, String transactionno,int moduleid, HashMap<String, Integer> FieldMap, HashMap<String, Integer> DimensionFieldMap, HashMap<String, Integer> LineLevelCustomFieldMap, HashMap<String, Integer> ProductLevelCustomFieldMap, String transactionmodule) {
        JSONArray jArr = new JSONArray();
        String uomname = "", fromStoreAbb = "" ,fromStoreName = "", createdby = "", toStoreName="",toStoreAbb = "";
        double totalQuantity = 0;
        
        try{
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter
            KwlReturnObject extraresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraresult.getEntityList().get(0);
            
            if(extraCompanyPreferences != null && !StringUtil.isNullOrEmpty(extraCompanyPreferences.getRepairStore())){
                Store fromStore = storeService.getStoreById(extraCompanyPreferences.getRepairStore());
                toStoreName = fromStore.getFullName();
                toStoreAbb = fromStore.getAbbreviation();
            }
            
            String date = "";
            if(!StringUtil.isNullOrEmpty(transactionmodule)){
                if (Constants.INVENTORY_CONSIGNMENT_MODULE.equals(transactionmodule)) {
                    Map<String, Object> requestMap = new HashMap<String,Object>();
                    requestMap.put(Constants.companyKey,companyid);
                    requestMap.put("transactionno",transactionno);
                    KwlReturnObject dataresult = consignmentDAO.getConsignmentObjectByTransactionNumber(requestMap);
                    List<Consignment> conslist = dataresult.getEntityList();
                    
                    int rowcnt = 0;
                    for(Consignment consignment :conslist ){
                        double quantity=0;
                        JSONObject obj = new JSONObject();
                        String snos = "", bnos = "",locationname = "",Location = "", Batchnos = "", Serialnos = "";
                        String costCenterID = "", costCenterName = "";
                        
                        if(consignment.getCostcenter()!= null && !StringUtil.isNullOrEmpty(consignment.getCostcenter().getName())){
                            costCenterID = consignment.getCostcenter().getCcid();
                            costCenterName = consignment.getCostcenter().getName();
                        }
                        if(consignment.getUom()!= null && !StringUtil.isNullOrEmpty(consignment.getUom().getNameEmptyforNA())){
                            uomname = consignment.getUom().getNameEmptyforNA();
                        }
                        String reason = "";
                        String remark = "";

                        if (consignment.getStore() != null) {//From Store and To Store Calculation
                            Store toStore = storeService.getStoreById(consignment.getStore().getId());
                            fromStoreName = toStore.getFullName();
                            fromStoreAbb = toStore.getAbbreviation();
                        }

                        if (consignment.getConsignmentApprovalDetails() != null) {
                            HashMap<String, String> locationnames = new HashMap<String, String>();
                            HashMap<String, String> batchnames = new HashMap<String, String>();
                            HashMap<String, String> serialnumbers = new HashMap<String, String>();
                            int pos = 1;
                            Set<ConsignmentApprovalDetails> consignmentDetailSet = consignment.getConsignmentApprovalDetails();
                            for (ConsignmentApprovalDetails consignmentDetailvalue : consignmentDetailSet) {
                                if (consignmentDetailvalue.getRetQty() > 0 && consignmentDetailvalue.getApprovalStatus()!= null &&  consignmentDetailvalue.getApprovalStatus().equals(ApprovalStatus.REJECTED)
                                        || consignmentDetailvalue.getApprovalStatus().equals(ApprovalStatus.REPAIRDONE)
                                        || consignmentDetailvalue.getApprovalStatus().equals(ApprovalStatus.REPAIRREJECT)
                                        || consignmentDetailvalue.getApprovalStatus().equals(ApprovalStatus.RETURNTOREPAIR)) {
                                    
                                    
                                    if(consignmentDetailvalue.getModifiedOn() != null){
                                        date = df.format(consignmentDetailvalue.getModifiedOn());
                                    }
                                    quantity = consignmentDetailvalue.getRetQty();
                                    totalQuantity += quantity;
                                    if (consignmentDetailvalue.getInspector() != null) {
                                        createdby = consignmentDetailvalue.getInspector().getFullName();
                                    }
                                    if (consignmentDetailvalue.getRemark() != null) {
                                        remark = consignmentDetailvalue.getRemark();
                                    }
                                    if (consignmentDetailvalue.getReason() != null) {
                                        reason = consignmentDetailvalue.getReason();
                                    }
                                    if (consignmentDetailvalue.getBatchName() != null) {
                                        bnos = consignmentDetailvalue.getBatchName();
                                    }
                                    if (bnos != null) {
                                        batchnames.put("batch" + pos, bnos);
                                    }
                                    if (consignmentDetailvalue.getLocation() != null) {
                                        locationname = consignmentDetailvalue.getLocation().getName();
                                        if (!StringUtil.isNullOrEmpty(locationname)) {
                                            locationnames.put("location" + pos, locationname);
                                        }
                                    }
                                    if(consignmentDetailvalue.getSerialName() != null){
                                        snos = consignmentDetailvalue.getSerialName();
                                    }
                                    if (snos != null) {
                                        serialnumbers.put("serial" + pos, snos);
                                    }
                                    pos++;
                                }
                            }
                            // set location
                            for (int cnt = 1; cnt <= locationnames.size(); cnt++) {
                                String lno = "";
                                lno = locationnames.get("location" + cnt);
                                if(!StringUtil.isNullOrEmpty(lno)){
                                    Location += lno.concat("!##");
                                }
                            }
                            // set serial no
                            for (int cnt = 1; cnt <= serialnumbers.size(); cnt++) {
                                String sn = "";
                                sn = serialnumbers.get("serial" + cnt);
                                if(!StringUtil.isNullOrEmpty(sn)){
                                    Serialnos += sn.concat("!##");
                                }
                            }
                            // set batch no
                            for (int cnt = 1; cnt <= batchnames.size(); cnt++) {
                                String bat = "";
                                bat = batchnames.get("batch" + cnt);
                                if(!StringUtil.isNullOrEmpty(bat)){
                                    Batchnos += bat.concat("!##");
                                }
                            }
                            //remove extra <hr> tag from ending of location, serial no and batch no
                            if (!StringUtil.isNullOrEmpty(Location.toString())) {
                                Location = Location.substring(0, Location.length() - 3);//remove extra <hr> tag
                            }

                            if (!StringUtil.isNullOrEmpty(Serialnos.toString())) {
                                Serialnos = Serialnos.substring(0, Serialnos.length() - 3);//remove extra <hr> tag
                            }
                            if (!StringUtil.isNullOrEmpty(Batchnos.toString())) {
                                Batchnos = Batchnos.substring(0, Batchnos.length() - 3);//remove extra <hr> tag
                            }
                        }   
                        if(quantity > 0){
                            rowcnt++;
                            obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                            String productname = "", productid = "",proddesc = "";
                            if(consignment.getProduct() != null && !StringUtil.isNullOrEmpty(consignment.getProduct().getProductid())){
                                productid = consignment.getProduct().getProductid();
                            }
                            if(consignment.getProduct() != null && !StringUtil.isNullOrEmpty(consignment.getProduct().getName())){
                                productname = consignment.getProduct().getName();
                            }
                            if(consignment.getProduct() != null && !StringUtil.isNullOrEmpty(consignment.getProduct().getDescription())){
                                proddesc = consignment.getProduct().getDescription();
                            }
                            obj.put(CustomDesignerConstants.IN_ProductCode, productid.replaceAll("\n", "<br>"));
                            obj.put(CustomDesignerConstants.ProductName, productname.replaceAll("\n", "<br>"));  //product name
                            obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>")); 
                            obj.put(CustomDesignerConstants.UOM, uomname);
                            obj.put(CustomDesignerConstants.CostCenter, costCenterID);
                            obj.put(CustomDesignerConstants.CostCenterName, costCenterName);
                            obj.put(CustomDesignerConstants.Quantity, quantity);
                            obj.put(CustomDesignerConstants.Location, Location);
                            obj.put(CustomDesignerConstants.SerialNumber, Serialnos);
                            obj.put(CustomDesignerConstants.BatchNumber, Batchnos);
                            obj.put(CustomDesignerConstants.SR_Remark, remark);
                            obj.put(CustomDesignerConstants.SR_Reason, reason);
                            jArr.put(obj);
                        }
                    }
                    
                } else if (Constants.INVENTORY_BUILD_ASSEMBLY_MODULE.equals(transactionmodule)) {
                    
                    Map<String, Object> requestMap = new HashMap<String,Object>();
                    requestMap.put(Constants.companyKey,companyid);
                    requestMap.put("transactionno",transactionno);
                    KwlReturnObject dataresult = consignmentDAO.getBuildAssemblyObjectByTransactionNumber(requestMap);
                    
                    List<AssemblyProductApprovalDetails> apblist = dataresult.getEntityList();
                    
                    int rowcnt = 0;
                    for(AssemblyProductApprovalDetails apb :apblist ){
                        
                        JSONObject obj = new JSONObject();
                        String snos = "", bnos = "",locationname = "",Location = "", Batchnos = "", Serialnos = "";
                        rowcnt++;
                        double quantity = apb.getQuantity();
                        totalQuantity += quantity;
//                        if(pb.getCostcenter()!= null && !StringUtil.isNullOrEmpty(consignment.getCostcenter().getName())){
//                            costCenterName = consignment.getCostcenter().getName();
//                        }
//                        if(consignment.getUom()!= null && !StringUtil.isNullOrEmpty(consignment.getUom().getName())){
//                            uomname = consignment.getUom().getName();
//                        }
                        String reason = "";
                        String remark = "";

//                        if (consignment.getStore() != null) {//From Store and To Store Calculation
//                            Store fromStore = storeService.getStoreById(consignment.getStore().getId());
//                            fromStoreName = fromStore.getFullName();
//                            toStoreName = fromStore.getFullName();
//                        }

//                        if (consignment.getConsignmentApprovalDetails() != null) {
//                            HashMap<String, String> locationnames = new HashMap<String, String>();
//                            HashMap<String, String> batchnames = new HashMap<String, String>();
//                            HashMap<String, String> serialnumbers = new HashMap<String, String>();
//                            int pos = 1;
//                            Set<ConsignmentApprovalDetails> consignmentDetailSet = consignment.getConsignmentApprovalDetails();
//                            for (ConsignmentApprovalDetails consignmentDetailvalue : consignmentDetailSet) {
//                                createdby = consignmentDetailvalue.getInspector().getFullName();
//                                remark = consignmentDetailvalue.getRemark();
//                                reason = consignmentDetailvalue.getReason();
//                                bnos = consignmentDetailvalue.getBatchName();
//                                if(bnos != null){
//                                    batchnames.put("batch" + pos, bnos);
//                                }
//                                if(consignmentDetailvalue.getLocation() != null){
//                                    locationname = consignmentDetailvalue.getLocation().getName();
//                                    if(!StringUtil.isNullOrEmpty(locationname)){
//                                        locationnames.put("location" + pos, locationname);
//                                    }
//                                }
//                                snos = consignmentDetailvalue.getSerialName();
//                                if(snos != null){
//                                    serialnumbers.put("serial" + pos, snos);
//                                }
//                                pos++;
//                            }
//                            // set location
//                            for (int cnt = 1; cnt <= locationnames.size(); cnt++) {
//                                String lno = "";
//                                lno = locationnames.get("location" + cnt);
//                                Location += lno.concat("<hr>");
//                            }
//                            // set serial no
//                            for (int cnt = 1; cnt <= serialnumbers.size(); cnt++) {
//                                String sn = "";
//                                sn = serialnumbers.get("serial" + cnt);
//                                Serialnos += sn.concat("<hr>");
//                            }
//                            // set batch no
//                            for (int cnt = 1; cnt <= batchnames.size(); cnt++) {
//                                String bat = "";
//                                bat = batchnames.get("batch" + cnt);
//                                Batchnos += bat.concat("<hr>");
//                            }
//                            //remove extra <hr> tag from ending of location, serial no and batch no
//                            if (!StringUtil.isNullOrEmpty(Location.toString())) {
//                                Location = Location.substring(0, Location.length() - 4);//remove extra <hr> tag
//                            }
//
//                            if (!StringUtil.isNullOrEmpty(Serialnos.toString())) {
//                                Serialnos = Serialnos.substring(0, Serialnos.length() - 4);//remove extra <hr> tag
//                            }
//                            if (!StringUtil.isNullOrEmpty(Batchnos.toString())) {
//                                Batchnos = Batchnos.substring(0, Batchnos.length() - 4);//remove extra <hr> tag
//                            }
//                        }   

//                        obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
//                        obj.put(CustomDesignerConstants.IN_ProductCode, apb.getPrBuild().getProduct().getProductid().replaceAll("\n", "<br>"));
//                        obj.put(CustomDesignerConstants.ProductName, apb.getPrBuild().getProduct().getName().replaceAll("\n", "<br>"));  //product name
//                        obj.put(CustomDesignerConstants.ProductDescription, apb.getPrBuild().getProduct().getDescription().replaceAll("\n", "<br>"));  //product name
//                        obj.put(CustomDesignerConstants.UOM, uomname);
//                        obj.put(CustomDesignerConstants.CostCenter, costCenterName);
//                        obj.put(CustomDesignerConstants.Quantity, quantity);
//                        obj.put(CustomDesignerConstants.Location, Location);
//                        obj.put(CustomDesignerConstants.SerialNumber, Serialnos);
//                        obj.put(CustomDesignerConstants.BatchNumber, Batchnos);
//                        obj.put(CustomDesignerConstants.SR_Remark, remark);
//                        obj.put(CustomDesignerConstants.SR_Reason, reason);
//                        jArr.put(obj);
                    }
                    
                    
                } else if (Constants.INVENTORY_SA_DETAIL_MODULE.equals(transactionmodule)) {
                    
                    Map<String, Object> requestMap = new HashMap<String,Object>();
                    requestMap.put(Constants.companyKey,companyid);
                    requestMap.put("transactionno",transactionno);
                    KwlReturnObject dataresult = consignmentDAO.getSAApprovedObjectByTransactionNumber(requestMap);
                    List<SAApproval> saapplist = dataresult.getEntityList();
                    
                    int rowcnt = 0;
                    for(SAApproval saapp :saapplist ){
                        
                        JSONObject obj = new JSONObject();
                        double quantity = 0;
                        String snos = "", bnos = "",locationname = "",Location = "", Batchnos = "", Serialnos = "";
                        String costCenterID = "", costCenterName = "";
                        
                        if(saapp.getStockAdjustment()!= null &&  saapp.getStockAdjustment().getCostCenter() != null && !StringUtil.isNullOrEmpty(saapp.getStockAdjustment().getCostCenter().getName())){
                            costCenterID = saapp.getStockAdjustment().getCostCenter().getCcid();
                            costCenterName = saapp.getStockAdjustment().getCostCenter().getName();
                        }
                        if(saapp.getStockAdjustment()!= null && saapp.getStockAdjustment().getUom()!= null && !StringUtil.isNullOrEmpty(saapp.getStockAdjustment().getUom().getNameEmptyforNA())){
                            uomname = saapp.getStockAdjustment().getUom().getNameEmptyforNA();
                        }
                        String reason = "";
                        String remark = "";

                        if (saapp.getStockAdjustment()!= null && saapp.getStockAdjustment().getStore() != null) {//From Store and To Store Calculation
                            Store toStore = storeService.getStoreById(saapp.getStockAdjustment().getStore().getId());
                            fromStoreName = toStore.getFullName();
                            fromStoreAbb= toStore.getAbbreviation();
                        }

                        if (saapp.getSADetailApprovalSet() != null) {
                            HashMap<String, String> locationnames = new HashMap<String, String>();
                            HashMap<String, String> batchnames = new HashMap<String, String>();
                            HashMap<String, String> serialnumbers = new HashMap<String, String>();
                            int pos = 1;
                            
                            
                            Set<SADetailApproval> saApprovalDetailSet = saapp.getSADetailApprovalSet();
                            for (SADetailApproval saApprovalDetailvalue : saApprovalDetailSet) {
                                if(saApprovalDetailvalue.getRetQty() > 0 && saApprovalDetailvalue.getApprovalStatus()!= null && saApprovalDetailvalue.getApprovalStatus().equals(ApprovalStatus.REJECTED)
                                        || saApprovalDetailvalue.getApprovalStatus().equals(ApprovalStatus.REPAIRDONE)
                                        || saApprovalDetailvalue.getApprovalStatus().equals(ApprovalStatus.REPAIRREJECT)
                                        || saApprovalDetailvalue.getApprovalStatus().equals(ApprovalStatus.RETURNTOREPAIR)){
                                    
                                    if(saApprovalDetailvalue.getModifiedOn() != null){
                                        date = df.format(saApprovalDetailvalue.getModifiedOn());
                                    }
                                    
                                    quantity = saApprovalDetailvalue.getRetQty();
                                    totalQuantity += quantity;
                                    
                                    if(saApprovalDetailvalue.getInspector() != null){
                                        createdby = saApprovalDetailvalue.getInspector().getFullName();
                                    }
                                    if(saApprovalDetailvalue.getRemark() != null){
                                        remark = saApprovalDetailvalue.getRemark();
                                    }
                                    if(saApprovalDetailvalue.getReason() != null){
                                        reason = saApprovalDetailvalue.getReason();
                                    }
                                    if(saApprovalDetailvalue.getStockAdjustmentDetail() != null && saApprovalDetailvalue.getStockAdjustmentDetail().getBatchName()!=null){
                                        bnos = saApprovalDetailvalue.getStockAdjustmentDetail().getBatchName();
                                    }
                                    if(bnos != null){
                                        batchnames.put("batch" + pos, bnos);
                                    }
                                    if(saApprovalDetailvalue.getStockAdjustmentDetail().getLocation() != null){
                                        locationname = saApprovalDetailvalue.getStockAdjustmentDetail().getLocation().getName();
                                        if(!StringUtil.isNullOrEmpty(locationname)){
                                            locationnames.put("location" + pos, locationname);
                                        }
                                    }
                                    snos = saApprovalDetailvalue.getSerialName();
                                    if(snos != null){
                                        serialnumbers.put("serial" + pos, snos);
                                    }
                                    pos++;
                                }
                            }
                            // set location
                            for (int cnt = 1; cnt <= locationnames.size(); cnt++) {
                                String lno = "";
                                lno = locationnames.get("location" + cnt);
                                if(!StringUtil.isNullOrEmpty(lno)){
                                    Location += lno.concat("!##");
                                }
                            }
                            // set serial no
                            for (int cnt = 1; cnt <= serialnumbers.size(); cnt++) {
                                String sn = "";
                                sn = serialnumbers.get("serial" + cnt);
                                if(!StringUtil.isNullOrEmpty(sn)){
                                    Serialnos += sn.concat("!##");
                                }
                            }
                            // set batch no
                            for (int cnt = 1; cnt <= batchnames.size(); cnt++) {
                                String bat = "";
                                bat = batchnames.get("batch" + cnt);
                                if(!StringUtil.isNullOrEmpty(bat)){
                                    Batchnos += bat.concat("!##");
                                }
                            }
                            //remove extra <hr> tag from ending of location, serial no and batch no
                           if (!StringUtil.isNullOrEmpty(Location)) {
                                Location = Location.substring(0, Location.length() - 3);//remove extra <hr> tag
                            }

                            if (!StringUtil.isNullOrEmpty(Serialnos)) {
                                Serialnos = Serialnos.substring(0, Serialnos.length() - 3);//remove extra <hr> tag
                            }
                            if (!StringUtil.isNullOrEmpty(Batchnos)) {
                                Batchnos = Batchnos.substring(0, Batchnos.length() - 3);//remove extra <hr> tag
                            }
                        }   
                        if(quantity > 0){
                            rowcnt++;
                            obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                            String productname = "", productid = "",proddesc = "";
                            if(saapp.getStockAdjustment() != null ){
                                if(saapp.getStockAdjustment().getProduct() != null && !StringUtil.isNullOrEmpty(saapp.getStockAdjustment().getProduct().getProductid())){
                                    productid = saapp.getStockAdjustment().getProduct().getProductid();
                                }
                                if(saapp.getStockAdjustment().getProduct() != null && !StringUtil.isNullOrEmpty(saapp.getStockAdjustment().getProduct().getName())){
                                    productname = saapp.getStockAdjustment().getProduct().getName();
                                }
                                if(saapp.getStockAdjustment().getProduct() != null && !StringUtil.isNullOrEmpty(saapp.getStockAdjustment().getProduct().getDescription())){
                                    proddesc = saapp.getStockAdjustment().getProduct().getDescription();
                                }
                            }
                            obj.put(CustomDesignerConstants.IN_ProductCode, productid.replaceAll("\n", "<br>"));
                            obj.put(CustomDesignerConstants.ProductName, productname.replaceAll("\n", "<br>"));  //product name
                            obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>")); 
                            obj.put(CustomDesignerConstants.UOM, uomname);
                            obj.put(CustomDesignerConstants.CostCenter, costCenterID);
                            obj.put(CustomDesignerConstants.CostCenterName, costCenterName);
                            obj.put(CustomDesignerConstants.Quantity, quantity);
                            obj.put(CustomDesignerConstants.Location, Location);
                            obj.put(CustomDesignerConstants.SerialNumber, Serialnos);
                            obj.put(CustomDesignerConstants.BatchNumber, Batchnos);
                            obj.put(CustomDesignerConstants.SR_Remark, remark);
                            obj.put(CustomDesignerConstants.SR_Reason, reason);
                            jArr.put(obj);
                        }
                    }
                    
                } else if (Constants.INVENTORY_STORE_STOCK_TRANSFER_MODULE.equals(transactionmodule) || Constants.INVENTORY_INTER_STOCK_TRANSFER_MODULE.equals(transactionmodule)) {
                    Map<String, Object> requestMap = new HashMap<String,Object>();
                    requestMap.put(Constants.companyKey,companyid);
                    requestMap.put("transactionno",transactionno);
                    KwlReturnObject dataresult = consignmentDAO.getStoreOrInterStockObjectByTransactionNumber(requestMap);
                    List<InterStoreTransferRequest> interapplist = dataresult.getEntityList();
                    int rowcnt = 0;
                    for(InterStoreTransferRequest interTransferapp :interapplist ){
                        
                        JSONObject obj = new JSONObject();
                        String snos = "", bnos = "",locationname = "",Location = "", Batchnos = "", Serialnos = "";
                        String costCenterID = "", costCenterName = "";
                        
                        if(interTransferapp.getCostCenter() != null && !StringUtil.isNullOrEmpty(interTransferapp.getCostCenter().getName())){
                            costCenterID = interTransferapp.getCostCenter().getCcid();
                            costCenterName = interTransferapp.getCostCenter().getName();
                        }
                        if(interTransferapp.getUom()!= null && !StringUtil.isNullOrEmpty(interTransferapp.getUom().getNameEmptyforNA())){
                            uomname = interTransferapp.getUom().getNameEmptyforNA();
                        }
                        String reason = "";
                        String remark = "";
                        
                        

                        if (interTransferapp.getFromStore()!= null) {//From Store and To Store Calculation
                            Store toStore = storeService.getStoreById(interTransferapp.getFromStore().getId());
                            if(toStore != null){
                                fromStoreName = toStore.getFullName();
                                fromStoreAbb = toStore.getAbbreviation();
                            }
                        }
                        requestMap.clear();
                        requestMap.put("transactionid",interTransferapp.getId());
                        dataresult = consignmentDAO.getStockTransfer(requestMap);
                        List<StockTransferApproval> stockapplist = dataresult.getEntityList();
                        
                        double quantity = 0;
                        if(stockapplist.size() > 0){
                            
                            if (interTransferapp.getIstDetails() != null) {
                                HashMap<String, String> locationnames = new HashMap<String, String>();
                                HashMap<String, String> batchnames = new HashMap<String, String>();
                                HashMap<String, String> serialnumbers = new HashMap<String, String>();
                                int pos = 1;
                                Set<ISTDetail> interTransferDetailSet = interTransferapp.getIstDetails();
                                for (ISTDetail istDetailvalue : interTransferDetailSet) {
                                    requestMap.clear();
                                    requestMap.put("transactionid",istDetailvalue.getId());
                                    dataresult = consignmentDAO.getStockTransferDetail(requestMap);
                                    List<StockTransferDetailApproval> stockdetail = dataresult.getEntityList();
                                    
                                    if(stockdetail.size() > 0){
                                        
                                        StockTransferDetailApproval stockdetailapp =  stockdetail.get(0);
                                        if(stockdetailapp.getRetQty() > 0 && stockdetailapp.getApprovalStatus()!=null && stockdetailapp.getApprovalStatus().equals(ApprovalStatus.REJECTED)
                                        || stockdetailapp.getApprovalStatus().equals(ApprovalStatus.REPAIRDONE)
                                        || stockdetailapp.getApprovalStatus().equals(ApprovalStatus.REPAIRREJECT)
                                            || stockdetailapp.getApprovalStatus().equals(ApprovalStatus.RETURNTOREPAIR)){
                                       
                                        
                                        if (stockdetailapp.getModifiedOn() != null) {
                                            date = df.format(stockdetailapp.getModifiedOn());
                                        }
                                            
                                        quantity = stockdetailapp.getRetQty();
                                        totalQuantity += quantity;
                                        if(stockdetailapp.getInspector() !=null){
                                            createdby = stockdetailapp.getInspector().getFullName();
                                        }
                                        
                                        if(stockdetailapp.getRemark() !=null){
                                            remark = stockdetailapp.getRemark();
                                        }
                                        if(stockdetailapp.getReason() !=null){
                                            reason = stockdetailapp.getReason();
                                        }
                                        if(istDetailvalue.getBatchName() !=null){
                                            bnos = istDetailvalue.getBatchName();
                                        }
                                        if(bnos != null){
                                            batchnames.put("batch" + pos, bnos);
                                        }
                                        if(istDetailvalue.getDeliveredLocation() != null){
                                            locationname = istDetailvalue.getDeliveredLocation().getName();
                                            if(!StringUtil.isNullOrEmpty(locationname)){
                                                locationnames.put("location" + pos, locationname);
                                            }
                                        }
                                        snos = stockdetailapp.getSerialName();
                                        if(snos != null){
                                            serialnumbers.put("serial" + pos, snos);
                                        }
                                        pos++;
                                    }
                                    }
                                }
                                // set location
                                for (int cnt = 1; cnt <= locationnames.size(); cnt++) {
                                    String lno = "";
                                    lno = locationnames.get("location" + cnt);
                                    if(!StringUtil.isNullOrEmpty(lno)){
                                        Location += lno.concat("!##");
                                    }
                                }
                                // set serial no
                                for (int cnt = 1; cnt <= serialnumbers.size(); cnt++) {
                                    String sn = "";
                                    sn = serialnumbers.get("serial" + cnt);
                                    if(!StringUtil.isNullOrEmpty(sn)){
                                        Serialnos += sn.concat("!##");
                                    }
                                }
                                // set batch no
                                for (int cnt = 1; cnt <= batchnames.size(); cnt++) {
                                    String bat = "";
                                    bat = batchnames.get("batch" + cnt);
                                    if(!StringUtil.isNullOrEmpty(bat)){
                                        Batchnos += bat.concat("!##");
                                    }
                                }
                                //remove extra <hr> tag from ending of location, serial no and batch no
                                if (!StringUtil.isNullOrEmpty(Location.toString())) {
                                    Location = Location.substring(0, Location.length() - 3);//remove extra <hr> tag
                                }

                                if (!StringUtil.isNullOrEmpty(Serialnos.toString())) {
                                    Serialnos = Serialnos.substring(0, Serialnos.length() - 3);//remove extra <hr> tag
                                }
                                if (!StringUtil.isNullOrEmpty(Batchnos.toString())) {
                                    Batchnos = Batchnos.substring(0, Batchnos.length() - 3);//remove extra <hr> tag
                                }
                            }   
                        }

                        if(quantity > 0){
                            rowcnt++;
                            obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                            String productname = "", productid = "",proddesc = "";
                            if(interTransferapp.getProduct() != null && !StringUtil.isNullOrEmpty(interTransferapp.getProduct().getProductid())){
                                productid = interTransferapp.getProduct().getProductid();
                            }
                            if(interTransferapp.getProduct() != null && !StringUtil.isNullOrEmpty(interTransferapp.getProduct().getName())){
                                productname = interTransferapp.getProduct().getName();
                            }
                            if(interTransferapp.getProduct() != null && !StringUtil.isNullOrEmpty(interTransferapp.getProduct().getDescription())){
                                proddesc = interTransferapp.getProduct().getDescription();
                            }
                            obj.put(CustomDesignerConstants.IN_ProductCode, productid.replaceAll("\n", "<br>"));
                            obj.put(CustomDesignerConstants.ProductName, productname.replaceAll("\n", "<br>"));  //product name
                            obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>")); 
                            obj.put(CustomDesignerConstants.UOM, uomname);
                            obj.put(CustomDesignerConstants.CostCenter, costCenterID);
                            obj.put(CustomDesignerConstants.CostCenterName, costCenterName);
                            obj.put(CustomDesignerConstants.Quantity, quantity);
                            obj.put(CustomDesignerConstants.Location, Location);
                            obj.put(CustomDesignerConstants.SerialNumber, Serialnos);
                            obj.put(CustomDesignerConstants.BatchNumber, Batchnos);
                            obj.put(CustomDesignerConstants.SR_Remark, remark);
                            obj.put(CustomDesignerConstants.SR_Reason, reason);
                            jArr.put(obj);
                        }
                    }
                }
                JSONObject summaryData = new JSONObject();
                summaryData.put("summarydata", true);

                summaryData.put(CustomDesignerConstants.FromStore, fromStoreName);
                summaryData.put(CustomDesignerConstants.ToStore, toStoreName);
                summaryData.put(InventoryCustomDesignerConstants.FROMSTORENAME, fromStoreAbb);
                summaryData.put(InventoryCustomDesignerConstants.TOSTORENAME, toStoreAbb);
                summaryData.put(CustomDesignerConstants.Createdby, createdby);
                summaryData.put(InventoryCustomDesignerConstants.INVENTORYTRANSACTIONNUMBER, transactionno);
                summaryData.put(InventoryCustomDesignerConstants.INVENTORYDATE, date);
                summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);

                jArr.put(summaryData);
                
            }
            
        }catch(Exception ex){
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
     public ModelAndView exportSingleInterStoreTransfer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView-empty";
        try {
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String invoicePostText = "";
            AccCustomData accCustomData = null;
            HashMap<String, JSONArray> itemDataAgainstInvoice = new HashMap<String, JSONArray>();
            JSONArray lineItemsArr = new JSONArray();

            String billID = request.getParameter("recordids");
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            int moduleid = Integer.parseInt(request.getParameter(Constants.moduleid));
            String transactionno = request.getParameter("transactiono");
            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            /*
             * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
             */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);
            
            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

            //For product custom field
            fieldrequestParams.clear();
            HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);
            
            lineItemsArr = getInterStoreTransferDetailsItemJSON(request, companyid, billID, transactionno,moduleid, FieldMap, DimensionFieldMap, LineLevelCustomFieldMap, ProductLevelCustomFieldMap);
            itemDataAgainstInvoice.put(billID, lineItemsArr);
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataAgainstInvoice, accCustomData, customDesignDAOObj,accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj,
                    velocityEngine, invoicePostText, otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }  
    
    /*
     * Inter Store Transfer
     */
    public JSONArray getInterStoreTransferDetailsItemJSON(HttpServletRequest request, String companyid, String billid, String transactionno, int moduleid, HashMap<String, Integer> FieldMap, HashMap<String, Integer> DimensionFieldMap, HashMap<String, Integer> LineLevelCustomFieldMap, HashMap<String, Integer> ProductLevelCustomFieldMap) {
        JSONArray jArr = new JSONArray();
        InterStoreTransferRequest interstoretransfer = null;
        InterStoreTransferRequest interStoreTrans = null;
        PdfTemplateConfig config = null;
        StockRequestDetail srd = null;
        String uomname = "", globallevelcustomfields = "", globalleveldimensions = "", orderStatus = "", createdby = "",packagingName="";
        String fromStoreName = "",fromStoreCode = "",fromStoreDesc = "", fromStoreAddress = "", toStoreName = "",toStoreCode = "",toStoreDesc = "",toStoreAddress = "";
        try {
            JSONObject requestObj = StringUtil.convertRequestToJsonObject(request);
            boolean isgstincluded = false; //flag to check includedgst
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Integer.parseInt(request.getParameter("moduleid")));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            int rowcnt = 0;
            KwlReturnObject srresult = accountingHandlerDAOobj.getObject(InterStoreTransferRequest.class.getName(), billid);
            interstoretransfer = (InterStoreTransferRequest) srresult.getEntityList().get(0);

            if (interstoretransfer.getToStore() != null && interstoretransfer.getFromStore() != null) {//From Store and To Store Calculation
                Store fromStore = storeService.getStoreById(interstoretransfer.getFromStore().getId());
                Store toStore = storeService.getStoreById(interstoretransfer.getToStore().getId());
                fromStoreName = fromStore.getFullName();
                fromStoreDesc = fromStore.getDescription();
                fromStoreCode = fromStore.getAbbreviation();
                fromStoreAddress = fromStore.getAddress();
                toStoreName = toStore.getFullName();
                toStoreDesc = toStore.getDescription();
                toStoreCode = toStore.getAbbreviation();
                toStoreAddress = toStore.getAddress();
            }

            if (interstoretransfer.getCreatedBy() != null) {//MoD Value
                createdby = interstoretransfer.getCreatedBy().getFullName();
            }

            if (interstoretransfer.getStatus() != null) {
                orderStatus = interstoretransfer.getStatus().name();
            }

            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("transactionno", transactionno);
            requestParams.put("companyid", companyid);
            requestParams.put(Constants.moduleid, moduleid);
            KwlReturnObject productresult = storeService.getInventoryProductDetails(requestParams);
            List productlist = productresult.getEntityList();

            for (int i = 0; i < productlist.size(); i++) {
                rowcnt++;
                String Serialnos = "", Location = "", Batchnos = "", mfgDts = "", expDts = "";
                HashMap<String, String> batchnames = new HashMap<String, String>();
                HashMap<String, String> expiryDates = new HashMap<String, String>();
                HashMap<String, String> mfgDates = new HashMap<String, String>();
                HashMap<String, String> serialnumbers = new HashMap<String, String>();
                HashMap<String, String> locationnames = new HashMap<String, String>();
                JSONObject obj = new JSONObject();
                Object[] srobject = (Object[]) productlist.get(i);
                String itemId = srobject[0] != null ? srobject[0].toString() : "";
                String uomId = srobject[1] != null ? srobject[1].toString() : "";
                String costCenterId = srobject[2] != null ? srobject[2].toString() : "";
                String packagingId = srobject[3] != null ? srobject[3].toString() : "";
                double orderedquantity = Double.parseDouble(srobject[4] != null ? srobject[4].toString() : "0");
                double acceptedquantity = Double.parseDouble(srobject[5] != null ? srobject[5].toString() : "0");
                String remark = srobject[6] != null ? srobject[6].toString() : "";
                String interStoreTransferId = srobject[7] != null ? srobject[7].toString() : "";
                String costCenterID = "", costCenterName = "";
                
                KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Product.class.getName(), itemId);
                Product product = (Product) jeresult.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(uomId)) {
                    jeresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), uomId);
                    UnitOfMeasure uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                    uomname = uom.getNameEmptyforNA();
                }
                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    jeresult = accountingHandlerDAOobj.getObject(CostCenter.class.getName(), costCenterId);
                    CostCenter costCenter = (CostCenter) jeresult.getEntityList().get(0);
                    costCenterID = costCenter.getCcid();
                    costCenterName = costCenter.getName();
                }
                
                if (!StringUtil.isNullOrEmpty(packagingId)) {
                    jeresult = accountingHandlerDAOobj.getObject(Packaging.class.getName(), packagingId);
                    Packaging packaging = (Packaging) jeresult.getEntityList().get(0);
                    packagingName = packaging.toString();
                }

                //Inventory Batch Details
                KwlReturnObject interStorTransResult = accountingHandlerDAOobj.getObject(InterStoreTransferRequest.class.getName(), interStoreTransferId);
                interStoreTrans = (InterStoreTransferRequest) interStorTransResult.getEntityList().get(0);
                Set<ISTDetail> istdSet = new HashSet<ISTDetail>();

                if (interStoreTrans.getIstDetails() != null) {
                    istdSet = interStoreTrans.getIstDetails();
                    Date batchExpDate, batchMfgDate, serialExpDate, serialMfgDate;
                    int pos = 1;
                    String snos = "", bnos = "", locationname = "";
                    for (ISTDetail interstoreDetailvalue : istdSet) {
                        bnos = interstoreDetailvalue.getBatchName();
                        batchnames.put("batch" + pos, bnos);
                        locationname = interstoreDetailvalue.getIssuedLocation().getName();
                        locationnames.put("location" + pos, locationname);
                        snos = interstoreDetailvalue.getIssuedSerialNames();
                        serialnumbers.put("serial" + pos, snos);
                        /**
                         * get manufacturing and expiry date
                         */
                        NewProductBatch productBatch = stockService.getERPProductBatch(interStoreTrans.getProduct(), interStoreTrans.getFromStore(), interstoreDetailvalue.getIssuedLocation(), interstoreDetailvalue.getIssuedRow(), interstoreDetailvalue.getIssuedRack(), interstoreDetailvalue.getIssuedBin(), interstoreDetailvalue.getBatchName());
                        if(productBatch != null) {
                            if (!StringUtil.isNullOrEmpty(snos)) {
                                String[] snosArr = snos.split(",");
                                for (String srl : snosArr) {
                                    /**
                                     * get serial manufacturing and expiry date
                                     */
                                    NewBatchSerial productSerial = stockService.getERPBatchSerial(interStoreTrans.getProduct(), productBatch, srl);
                                    if (productSerial != null) {
                                        serialExpDate = productSerial.getExptodate();
                                        serialMfgDate = productSerial.getExpfromdate();
                                        expiryDates.put("expdate" + pos, serialExpDate != null ? df.format(serialExpDate): "");
                                        mfgDates.put("mfgdate" + pos, serialMfgDate != null ? df.format(serialMfgDate): "");
                                    }
                                }
                            } else{
                                /**
                                 * get batch manufacturing and expiry date
                                 */
                                batchExpDate = productBatch.getExpdate();
                                batchMfgDate = productBatch.getMfgdate();
                                expiryDates.put("expdate" + pos, batchExpDate != null ? df.format(batchExpDate): "");
                                mfgDates.put("mfgdate" + pos, batchMfgDate != null ? df.format(batchMfgDate): "");
                            }
                        }
                        pos++;
                    }
                    // set location names
                    for(int cnt = 1; cnt <= locationnames.size(); cnt++){
                        String loc = "";
                        loc = locationnames.get("location"+cnt);
                        if(!StringUtil.isNullOrEmpty(loc)){
                            Location += loc.concat("!##");
                        }
                    }
                    // set serial numbers
                    for(int cnt = 1; cnt <= serialnumbers.size(); cnt++){
                        String ser = "";
                        ser = serialnumbers.get("serial"+cnt);
                        if(!StringUtil.isNullOrEmpty(ser)){
                            Serialnos += ser.concat("!##");
                        }
                    }
                    // set batch names
                    for(int cnt = 1; cnt <= batchnames.size(); cnt++){
                        String batch = "";
                        batch = batchnames.get("batch"+cnt);
                        if(!StringUtil.isNullOrEmpty(batch)){
                            Batchnos += batch.concat("!##");
                        }
                    }
                    // set Expiry Date
                    for(int cnt = 1; cnt <= expiryDates.size(); cnt++){
                        String date = "";
                        date = expiryDates.get("expdate"+cnt);
                        if(!StringUtil.isNullOrEmpty(date)){
                            expDts += date.concat("!##");
                        }
                    }
                    // set Mfg Date
                    for(int cnt = 1; cnt <= mfgDates.size(); cnt++){
                        String date = "";
                        date = mfgDates.get("mfgdate"+cnt);
                        if(!StringUtil.isNullOrEmpty(date)){
                            mfgDts += date.concat("!##");
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(Location.toString())) {
                        Location = Location.substring(0, Location.length() - 3);
                    }

                    if (!StringUtil.isNullOrEmpty(Serialnos.toString())) {
                        Serialnos = Serialnos.substring(0, Serialnos.length() -3);
                    }
                    if (!StringUtil.isNullOrEmpty(Batchnos.toString())) {
                        Batchnos = Batchnos.substring(0, Batchnos.length() - 3);
                    }
                    //remove extra comma from expiry date value
                    if (!StringUtil.isNullOrEmpty(expDts.toString())) {
                        expDts = expDts.substring(0, expDts.length() - 3);
                    }
                    //remove extra comma from manufacturing date value
                    if (!StringUtil.isNullOrEmpty(mfgDts.toString())) {
                        mfgDts = mfgDts.substring(0, mfgDts.length() - 3);
                    }
                    
                }
                //get initial purchase price of product
                KwlReturnObject purchase = accProductObj.getProductPrice(product.getID(), true, null, "", "");
                
                String proddesc = StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription();
                proddesc = StringUtil.DecodeText(proddesc);
                
                obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                obj.put(CustomDesignerConstants.IN_ProductCode, product.getProductid().replaceAll("\n", "<br>"));
                obj.put(CustomDesignerConstants.ProductName, product.getName().replaceAll("\n", "<br>"));  //product name
                obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));  //product Description
                obj.put(CustomDesignerConstants.ProductBarcode, product.getBarcode() != null ? product.getBarcode().replaceAll("\n", "<br>") : "");  //product Bar Code
                obj.put(CustomDesignerConstants.HSCode, (product.getHSCode() != null) ? product.getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                obj.put(CustomDesignerConstants.Packaging, packagingName.replaceAll("\n", "<br>"));  //packaging
                obj.put(CustomDesignerConstants.UOM, uomname);
                obj.put(CustomDesignerConstants.CostCenter, costCenterID);
                obj.put(CustomDesignerConstants.CostCenterName, costCenterName);
                obj.put(CustomDesignerConstants.Quantity, orderedquantity);
                obj.put(CustomDesignerConstants.SR_Remark, remark);
                obj.put(CustomDesignerConstants.Location, Location);
                obj.put(CustomDesignerConstants.BatchNumber, Batchnos);
                obj.put(CustomDesignerConstants.SerialNumber, Serialnos);
                obj.put(CustomDesignerConstants.BatchNumberExp, expDts);
                obj.put(CustomDesignerConstants.ManufacturingDate, mfgDts);
                obj.put(CustomDesignerConstants.PerUnitPrice, purchase.getEntityList().get(0) != null ? purchase.getEntityList().get(0) : 0);
                obj.put("isGstIncluded", isgstincluded);
                /*
                 * get custom line data
                 */
                Map<String, Object> variableMap = new HashMap<>();
                
                InterStoreTransferCustomData stockDetailCustom = interStoreTrans.getISTLineLevelCustomData();
                if (stockDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, stockDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                /*Set All Line level Dimension & All LIne level Custom Field Values*/   
                obj=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap,variableMap,obj,false);//for dimensions
                obj=CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap,variableMap,obj,true);//for customfields
                jArr.put(obj);
            }
            /*
             * Other Fields Calculation
             */
            JSONObject summaryData = new JSONObject();
            summaryData.put("summarydata", true);
            /*All Global Section Custom Field and DImensions*/
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, moduleid);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", billid);
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.FromStore, fromStoreName);
            summaryData.put(CustomDesignerConstants.FromStoreDesc, fromStoreDesc);
            summaryData.put(CustomDesignerConstants.FromStoreCode, fromStoreCode);
            summaryData.put(CustomDesignerConstants.FromStoreAddress, fromStoreAddress);
            summaryData.put(CustomDesignerConstants.ToStore, toStoreName);
            summaryData.put(CustomDesignerConstants.ToStoreDesc, toStoreDesc);
            summaryData.put(CustomDesignerConstants.ToStoreCode, toStoreCode);
            summaryData.put(CustomDesignerConstants.ToStoreAddress, toStoreAddress);
            summaryData.put(CustomDesignerConstants.OrderStatus, orderStatus);
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            
            KwlReturnObject pref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preference = (CompanyAccountPreferences) pref.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            
            /**
             * Get and put vendor name, purchase order no from purchase order
             */
            String vendorId = "", vendorCode = "", vendorName = "", jwOutOrderNo = "", vendorGstinNumber = "";
            if(!StringUtil.isNullOrEmpty(interstoretransfer.getPurchaseOrderDetail())){
                KwlReturnObject podResult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), interstoretransfer.getPurchaseOrderDetail());
                PurchaseOrderDetail purchaseOrderDetail = (PurchaseOrderDetail) podResult.getEntityList().get(0);
                if(purchaseOrderDetail.getPurchaseOrder() != null){
                    if(purchaseOrderDetail.getPurchaseOrder().getVendor() != null){
                        vendorName = purchaseOrderDetail.getPurchaseOrder().getVendor().getName() != null ? purchaseOrderDetail.getPurchaseOrder().getVendor().getName() : "";
                        vendorId = purchaseOrderDetail.getPurchaseOrder().getVendor().getID() != null ? purchaseOrderDetail.getPurchaseOrder().getVendor().getID() : "";
                        vendorCode = purchaseOrderDetail.getPurchaseOrder().getVendor().getAcccode() != null ? purchaseOrderDetail.getPurchaseOrder().getVendor().getAcccode() : "";
                        vendorGstinNumber = purchaseOrderDetail.getPurchaseOrder().getVendor().getGSTIN() != null ? purchaseOrderDetail.getPurchaseOrder().getVendor().getGSTIN() : "";
                    }
                    jwOutOrderNo = purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber() != null ? purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber() : "";
                }
            }
            /**
             * Get Vendor address details
             */
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("vendorid", vendorId);
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData, preference, extraCompanyPreferences);
            
            summaryData.put(CustomDesignerConstants.VENDOR_NAME, vendorName);
            summaryData.put(CustomDesignerConstants.CustomerVendor_AccCode, vendorCode);
            summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, vendorGstinNumber);
            summaryData.put(CustomDesignerConstants.JOB_WORK_OUT_ORDER_NO, jwOutOrderNo);
            //get and put challan number
            summaryData.put(CustomDesignerConstants.JOB_WORK_CHALLAN_NO, interstoretransfer.getChallanNumber() != null ? interstoretransfer.getChallanNumber().getChallanNumber() : "");

            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    /*Store Location Transfer*/
    public ModelAndView exportSingleInterLocationStockTransfer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView-empty";
        try {
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String invoicePostText = "";
            AccCustomData accCustomData = null;
            HashMap<String, JSONArray> itemDataAgainstInvoice = new HashMap<String, JSONArray>();
            JSONArray lineItemsArr = new JSONArray();

            String billID = request.getParameter("recordids");
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            int moduleid = Integer.parseInt(request.getParameter(Constants.moduleid));
            String transactionno = request.getParameter("transactiono");

            lineItemsArr = getInterLocationStockTransferDetailsItemJSON(request, companyid, billID, transactionno, moduleid);
            itemDataAgainstInvoice.put(billID, lineItemsArr);
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataAgainstInvoice, accCustomData, customDesignDAOObj,accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj,
                    velocityEngine, invoicePostText, otherconfigrequestParams,accInvoiceServiceDAOObj,accGoodsReceiptServiceDAOObj);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    /*
     * Inter Location Transfer
     */
    public JSONArray getInterLocationStockTransferDetailsItemJSON(HttpServletRequest request, String companyid, String billid, String transactionno, int moduleid) {
        JSONArray jArr = new JSONArray();
        InterStoreTransferRequest interstoretransfer = null;
        PdfTemplateConfig config = null;
        StockRequestDetail srd = null;
        String uomname = "", costCenterName = "",costCenterID="", fromStoreName = "",fromLocName = "",fromLocCode = "", fromStoreCode = "", fromStoreDesc = "", toLocName = "",toLocCode = "",toStoreName = "",toStoreCode = "",toStoreDesc = "", orderStatus = "", createdby = "", packagingName = "";
        boolean locFlag =false;
        try {
            boolean isgstincluded = false; //flag to check includedgst
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Integer.parseInt(request.getParameter("moduleid")));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            int rowcnt = 0;
            KwlReturnObject srresult = accountingHandlerDAOobj.getObject(InterStoreTransferRequest.class.getName(), billid);
            interstoretransfer = (InterStoreTransferRequest) srresult.getEntityList().get(0);

            if (interstoretransfer.getToStore() != null && interstoretransfer.getFromStore() != null) {//From Store and To Store Calculation
                Store fromStore = storeService.getStoreById(interstoretransfer.getFromStore().getId());
                Store toStore = storeService.getStoreById(interstoretransfer.getToStore().getId());
                fromStoreName = fromStore.getFullName();
                fromStoreCode = fromStore.getAbbreviation();
                fromStoreDesc = fromStore.getDescription();
                toStoreName = toStore.getFullName();
                toStoreCode = toStore.getAbbreviation();
                toStoreDesc = toStore.getDescription();
            }

            if (interstoretransfer.getCreatedBy() != null) {//MoD Value
                createdby = interstoretransfer.getCreatedBy().getFullName();
            }

            if (interstoretransfer.getStatus() != null) {
                orderStatus = interstoretransfer.getStatus().name();
            }

            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("transactionno", transactionno);
            requestParams.put("companyid", companyid);
            requestParams.put(Constants.moduleid, moduleid);
            KwlReturnObject productresult = storeService.getInventoryProductDetails(requestParams);
            List productlist = productresult.getEntityList();

            for (int i = 0; i < productlist.size(); i++) {
                rowcnt++;
                String Serialnos = "", Location = "", Batchnos = "";
                Set<String> batchnames = new HashSet<String>();
                Set<String> serialnumbers = new HashSet<String>();
                Set<String> locationnames = new HashSet<String>();
                JSONObject obj = new JSONObject();
                Object[] srobject = (Object[]) productlist.get(i);
                String itemId = srobject[0] != null ? srobject[0].toString() : "";
                String uomId = srobject[1] != null ? srobject[1].toString() : "";
                String costCenterId = srobject[2] != null ? srobject[2].toString() : "";
                String packagingId = srobject[3] != null ? srobject[3].toString() : "";
                double orderedquantity = Double.parseDouble(srobject[4] != null ? srobject[4].toString() : "0");
                double acceptedquantity = Double.parseDouble(srobject[5] != null ? srobject[5].toString() : "0");
                String remark = srobject[6] != null ? srobject[6].toString() : "";
                String interStoreTransferId = srobject[7] != null ? srobject[7].toString() : "";

                KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Product.class.getName(), itemId);
                Product product = (Product) jeresult.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(uomId)) {
                    jeresult = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), uomId);
                    UnitOfMeasure uom = (UnitOfMeasure) jeresult.getEntityList().get(0);
                    uomname = uom.getNameEmptyforNA();
                }
                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    jeresult = accountingHandlerDAOobj.getObject(CostCenter.class.getName(), costCenterId);
                    CostCenter costCenter = (CostCenter) jeresult.getEntityList().get(0);
                    costCenterId = costCenter.getCcid();
                    costCenterName = costCenter.getName();
                }

                if (!StringUtil.isNullOrEmpty(packagingId)) {
                    jeresult = accountingHandlerDAOobj.getObject(Packaging.class.getName(), packagingId);
                    Packaging packaging = (Packaging) jeresult.getEntityList().get(0);
                    packagingName = packaging.toString();
                }

                //Inventory Batch Details
                KwlReturnObject interStorTransResult = accountingHandlerDAOobj.getObject(InterStoreTransferRequest.class.getName(), interStoreTransferId);
                InterStoreTransferRequest interStoreTrans = (InterStoreTransferRequest) interStorTransResult.getEntityList().get(0);
                Set<ISTDetail> istdSet = new HashSet<ISTDetail>();

                if (interStoreTrans.getIstDetails() != null) {
                    istdSet = interStoreTrans.getIstDetails();
                    Iterator ite = istdSet.iterator();
                    for (ISTDetail interstoreDetailvalue : istdSet) {
                        String snos = "", bnos = "", locationname = "";
                        bnos += interstoreDetailvalue.getBatchName();
                        batchnames.add(bnos);
                        locationname = interstoreDetailvalue.getIssuedLocation().getName();
                        locationnames.add(locationname);
                        if (!locFlag) {
                            fromLocName = interstoreDetailvalue.getIssuedLocation() != null? interstoreDetailvalue.getIssuedLocation().getName() : "";
                            toLocName = interstoreDetailvalue.getDeliveredLocation() != null? interstoreDetailvalue.getDeliveredLocation().getName() : "";
                        }
                        snos = interstoreDetailvalue.getIssuedSerialNames();
                        serialnumbers.add(snos);
                    }
                    for (String str : locationnames) {
                        String bno = "";
                        bno = str;
                        if(!StringUtil.isNullOrEmpty(bno)){
                            Location += bno.concat("!##");
                        }
                    }

                    for (String str : serialnumbers) {
                        String lno = "";
                        lno = str;
                        if(!StringUtil.isNullOrEmpty(lno)){
                            Serialnos += lno.concat("!##");
                        }
                    }

                    for (String str : batchnames) {
                        String lno = "";
                        lno = str;
                        if(!StringUtil.isNullOrEmpty(lno)){
                            Batchnos += lno.concat("!##");
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(Location)) {
                        Location = Location.substring(0, Location.length() - 3);
                    }

                    if (!StringUtil.isNullOrEmpty(Serialnos)) {
                        Serialnos = Serialnos.substring(0, Serialnos.length() - 3);
                    }
                    if (!StringUtil.isNullOrEmpty(Batchnos)) {
                        Batchnos = Batchnos.substring(0, Batchnos.length() - 3);
                    }
                }

                obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                obj.put(CustomDesignerConstants.IN_ProductCode, product.getProductid().replaceAll("\n", "<br>"));
                obj.put(CustomDesignerConstants.ProductName, product.getName().replaceAll("\n", "<br>"));  //product name
                obj.put(CustomDesignerConstants.ProductDescription, product.getDescription().replaceAll("\n", "<br>"));  //product name
                obj.put(CustomDesignerConstants.ProductBarcode, product.getBarcode().replaceAll("\n", "<br>"));  //product name
                obj.put(CustomDesignerConstants.HSCode, (product.getHSCode() != null) ? product.getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                obj.put(CustomDesignerConstants.Packaging, packagingName.replaceAll("\n", "<br>"));  //packaging
                obj.put(CustomDesignerConstants.UOM, uomname);
                obj.put(CustomDesignerConstants.CostCenter, costCenterID);
                obj.put(CustomDesignerConstants.CostCenterName, costCenterName);
                obj.put(CustomDesignerConstants.Quantity, orderedquantity);
                obj.put(CustomDesignerConstants.SR_Remark, remark);
                obj.put(CustomDesignerConstants.CollectLocation, Location);
                obj.put(CustomDesignerConstants.BatchNumber, Batchnos);
                obj.put(CustomDesignerConstants.SerialNumber, Serialnos);
                obj.put("isGstIncluded", isgstincluded);
                jArr.put(obj);
            }
            /*
             * Other Fields Calculation
             */
            JSONObject summaryData = new JSONObject();
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.FromStore, fromStoreName);
            summaryData.put(CustomDesignerConstants.FromStoreCode, fromStoreCode);
            summaryData.put(CustomDesignerConstants.FromStoreDesc, fromStoreDesc);
            summaryData.put(CustomDesignerConstants.ToStore, toStoreName);
            summaryData.put(CustomDesignerConstants.ToStoreCode, toStoreCode);
            summaryData.put(CustomDesignerConstants.ToStoreDesc, toStoreDesc);
            summaryData.put(CustomDesignerConstants.ToLocName, toLocName);
            summaryData.put(CustomDesignerConstants.FromLocName, fromLocName);
            summaryData.put(CustomDesignerConstants.OrderStatus, orderStatus);
            summaryData.put(CustomDesignerConstants.Createdby, createdby);

            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    /**
     * Export single QA Approval template
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView exportSingleQAApproval(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView-empty";
        try {
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String invoicePostText = "", recordids = "", transactionnos = "", transactionmodules = "", moduletype = "";
            AccCustomData accCustomData = null;
            HashMap<String, JSONArray> itemDataAgainstInvoice = new HashMap<String, JSONArray>();
            JSONArray lineItemsArr = new JSONArray();

            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> billIDs = CustomDesignHandler.getSelectedBillIDs(recordids);//getting array of id's

            String companyid = AccountingManager.getCompanyidFromRequest(request);
            int moduleid = Integer.parseInt(request.getParameter(Constants.moduleid));
            if (!StringUtil.isNullOrEmpty(request.getParameter("transactiono"))) {
                transactionnos = request.getParameter("transactiono");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("transactionmodules"))) {
                transactionmodules = request.getParameter("transactionmodules");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduletype"))) {
                moduletype = request.getParameter("moduletype");
            }
            
            if(moduleid == Constants.Acc_QA_APPROVAL_MODULE_ID){
                switch(moduletype){
                    case "deliveryorder" :
                        moduleid = Constants.Acc_Delivery_Order_ModuleId;
                        break;
                }
            }
            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            /*
             * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
             */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

            //For product custom field
            fieldrequestParams.clear();
            HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);

            for (int count = 0; count < billIDs.size(); count++) {
                lineItemsArr = getQAApprovalDetailsItemJSON(request, companyid, billIDs.get(count), transactionnos, moduleid, FieldMap, DimensionFieldMap, LineLevelCustomFieldMap, ProductLevelCustomFieldMap, transactionmodules, moduletype);
                itemDataAgainstInvoice.put(billIDs.get(count), lineItemsArr);
            }

            moduleid = Constants.Acc_QA_APPROVAL_MODULE_ID;
            
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            ExportRecordHandler.exportSingleGeneric(request, response, itemDataAgainstInvoice, accCustomData, customDesignDAOObj, accCommonTablesDAO, accAccountDAOobj, accountingHandlerDAOobj,
                    velocityEngine, invoicePostText, otherconfigrequestParams, accInvoiceServiceDAOObj, accGoodsReceiptServiceDAOObj);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    /**
     * Get QA Approval details JSON
     * @param request
     * @param companyid
     * @param billid
     * @param transactionno
     * @param moduleid
     * @param FieldMap
     * @param DimensionFieldMap
     * @param LineLevelCustomFieldMap
     * @param ProductLevelCustomFieldMap
     * @param transactionmodule
     * @param moduletype
     * @return 
     */
    public JSONArray getQAApprovalDetailsItemJSON(HttpServletRequest request, String companyid, String billid, String transactionno, int moduleid, HashMap<String, Integer> FieldMap, HashMap<String, Integer> DimensionFieldMap, HashMap<String, Integer> LineLevelCustomFieldMap, HashMap<String, Integer> ProductLevelCustomFieldMap, String transactionmodule, String moduletype) {
        JSONArray jArr = new JSONArray();

        try {
            JSONObject requestObj = StringUtil.convertRequestToJsonObject(request);
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(request);//User Date Formatter

            if (!StringUtil.isNullOrEmpty(moduletype) && !StringUtil.isNullOrEmpty(transactionmodule)) {
                JSONObject summaryData = new JSONObject();
                summaryData.put("summarydata", true);
                
                JSONObject jsonObj = new JSONObject();
                String searchString = request.getParameter("ss");
                String start = request.getParameter("start");
                String limit = request.getParameter("limit");
                jsonObj.put("ss", searchString);
                jsonObj.put("start", start);
                jsonObj.put("limit", limit);
                
                if (Constants.ACC_DELIVERY_ORDER_MODULENAME.equals(moduletype)) {
                    String doId = "", doDetailId = "";
                    DeliveryOrderDetail doDetail = null;
                    if (Constants.Acc_StockAdjustment_modulename.equals(transactionmodule)) {
                        KwlReturnObject stockAdjResult = accountingHandlerDAOobj.getObject(StockAdjustment.class.getName(), billid);
                        StockAdjustment stockAdj = (StockAdjustment) stockAdjResult.getEntityList().get(0);

                        if (stockAdj != null) {
                            if (stockAdj.getApprovedDODQCISTMapping() != null) {
                                doDetailId = stockAdj.getApprovedDODQCISTMapping().getDodetailID();

                                KwlReturnObject doDetailsResult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), doDetailId);
                                doDetail = (DeliveryOrderDetail) doDetailsResult.getEntityList().get(0);

                                doId = doDetail.getDeliveryOrder().getID();
                                
                                if(!StringUtil.isNullOrEmpty(doDetail.getInvlocid())){
                                    KwlReturnObject locationResult = accountingHandlerDAOobj.getObject(Location.class.getName(), doDetail.getInvlocid());
                                    Location locDetail = (Location) locationResult.getEntityList().get(0);
                                    summaryData.put(CustomDesignerConstants.Location, locDetail.getName());
                                } else{
                                    summaryData.put(CustomDesignerConstants.Location, "");
                                }
                                if(!StringUtil.isNullOrEmpty(doDetail.getInvstoreid())){
                                    KwlReturnObject storeResult = accountingHandlerDAOobj.getObject(Store.class.getName(), doDetail.getInvstoreid());
                                    Store storeDetail = (Store) storeResult.getEntityList().get(0);
                                    summaryData.put(CustomDesignerConstants.Warehouse, storeDetail.getFullName());
                                } else{
                                    summaryData.put(CustomDesignerConstants.Warehouse, "");
                                }
                                summaryData.put(CustomDesignerConstants.IN_ProductCode, doDetail.getProduct().getProductid());
                                summaryData.put(CustomDesignerConstants.ProductName, doDetail.getProduct().getProductName());
                                summaryData.put(CustomDesignerConstants.DO_QUANTITY, doDetail.getDeliveredQuantity());
                                summaryData.put(CustomDesignerConstants.Poreferencenumber, doDetail.getDeliveryOrder().getCustomerPORefNo());

                                summaryData.put(CustomDesignerConstants.INSPECTION_REF_NO, stockAdj.getTransactionNo());
                                summaryData.put(CustomDesignerConstants.INSPECTION_DESCRIPTION, doDetail.getProduct().getProductName());
                                summaryData.put(CustomDesignerConstants.INSPECTOR, stockAdj.getCreator().getFullName());
                                summaryData.put(CustomDesignerConstants.CustomDesignRemarks_fieldTypeId, stockAdj.getRemark());

                                Set<StockAdjustmentDetail> stockAdjDetails = stockAdj.getStockAdjustmentDetail();
                                for (StockAdjustmentDetail sad : stockAdjDetails) {
                                    summaryData.put(CustomDesignerConstants.Quantity, sad.getQuantity());

                                    if (stockAdj.getProduct().isIsBatchForProduct()) {
                                        summaryData.put(CustomDesignerConstants.BatchNumber, sad.getBatchName());
                                    } else {
                                        summaryData.put(CustomDesignerConstants.BatchNumber, "");
                                    }
                                    if (stockAdj.getProduct().isIsSerialForProduct()) {
                                        summaryData.put(CustomDesignerConstants.SerialNumber, sad.getSerialNames());
                                    } else {
                                        summaryData.put(CustomDesignerConstants.SerialNumber, "");
                                    }

                                    InspectionForm insForm = sad.getInspectionForm();
                                    if(insForm != null){
                                        JSONObject insFormDetailsJobj = getInspectionFormDetails(jArr, summaryData, insForm, df);
                                        if(insFormDetailsJobj.has("summaryData")){
                                            summaryData = insFormDetailsJobj.getJSONObject("summaryData");
                                        }
                                        if(insFormDetailsJobj.has("jArr")){
                                            jArr = insFormDetailsJobj.getJSONArray("jArr");
                                        }
                                    } else{
                                        summaryData.put(CustomDesignerConstants.INSPECTION_DATE, df.format(stockAdj.getModifiedOn()));
                                        summaryData.put(CustomDesignerConstants.MODEL_NAME, doDetail.getProduct().getProductid());
                                        summaryData.put(CustomDesignerConstants.INSPECTION_CUSTOMER_NAME, "");
                                        summaryData.put(CustomDesignerConstants.DEPARTMENT, "");
                                        summaryData.put(CustomDesignerConstants.CONSIGNMENT_RETURN_NO, stockAdj.getTransactionNo());
                                        
                                        InspectionTemplate insTemplate = stockAdj.getProduct().getInspectionTemplate();
                                        
                                        jArr = getInspectionTemplateDetails(jArr, insTemplate, jsonObj);
                                    }
                                }
                                summaryData.put(CustomDesignerConstants.INSPECTION_STATUS, "APPROVED");
                            }
                        }
                    } else if (Constants.Acc_InterStoreTransfer_modulename.equals(transactionmodule)) {
                        KwlReturnObject istResult = accountingHandlerDAOobj.getObject(InterStoreTransferRequest.class.getName(), billid);
                        InterStoreTransferRequest istRequest = (InterStoreTransferRequest) istResult.getEntityList().get(0);

                        if (istRequest != null) {
                            JSONObject paramsJobj = new JSONObject();
                            paramsJobj.put("istRequestId", billid);
                            KwlReturnObject dodqdcIstResult = stockService.getDODQCISTMapping(paramsJobj);
                            
                            if (dodqdcIstResult.getEntityList().size() > 0) {
                                DODQCISTMapping DODQCISTMappingResult = (DODQCISTMapping) dodqdcIstResult.getEntityList().get(0);
                                doDetailId = DODQCISTMappingResult.getDodetailID();

                                JSONObject returnJobj = getDOQAApprovalISTDetails(jArr, summaryData, istRequest, doDetailId, df, jsonObj);
                                if (returnJobj.has("summaryData")) {
                                    summaryData = returnJobj.getJSONObject("summaryData");
                                }
                                if (returnJobj.has("jArr")) {
                                    jArr = returnJobj.getJSONArray("jArr");
                                }
                                if (returnJobj.has("doId")) {
                                    doId = returnJobj.optString("doId", "");
                                }
                                if (returnJobj.has("doDetail")) {
                                    doDetail = (DeliveryOrderDetail) returnJobj.get("doDetail");
                                }
                                summaryData.put(CustomDesignerConstants.INSPECTION_STATUS, "PENDING");
                            }
                        }
                    } else if (Constants.ACC_REPAIR_IST_MODULENAME.equals(transactionmodule)) {
                        KwlReturnObject istResult = accountingHandlerDAOobj.getObject(InterStoreTransferRequest.class.getName(), billid);
                        InterStoreTransferRequest istRequest = (InterStoreTransferRequest) istResult.getEntityList().get(0);

                        if (istRequest != null) {
                            JSONObject paramsJobj = new JSONObject();
                            paramsJobj.put("istRequestId", billid);
                            KwlReturnObject rejectedIstResult = stockService.getRejectedDODQCISTMapping(paramsJobj);
                            
                            if (rejectedIstResult.getEntityList().size() > 0) {
                                RejectedDODQCISTMapping rejectedDODQCISTMapping = (RejectedDODQCISTMapping) rejectedIstResult.getEntityList().get(0);
                                doDetailId = rejectedDODQCISTMapping.getDodqcistmapping().getDodetailID();

                                JSONObject returnJobj = getDOQAApprovalISTDetails(jArr, summaryData, istRequest, doDetailId, df, jsonObj);
                                if (returnJobj.has("summaryData")) {
                                    summaryData = returnJobj.getJSONObject("summaryData");
                                }
                                if (returnJobj.has("jArr")) {
                                    jArr = returnJobj.getJSONArray("jArr");
                                }
                                if (returnJobj.has("doId")) {
                                    doId = returnJobj.optString("doId", "");
                                }
                                if (returnJobj.has("doDetail")) {
                                    doDetail = (DeliveryOrderDetail) returnJobj.get("doDetail");
                                }
                                summaryData.put(CustomDesignerConstants.INSPECTION_STATUS, "REJECTED");
                            }
                        }
                    } else if(Constants.ACC_PACKED_IST_MODULENAME.equals(transactionmodule)){
                        KwlReturnObject istResult = accountingHandlerDAOobj.getObject(InterStoreTransferRequest.class.getName(), billid);
                        InterStoreTransferRequest istRequest = (InterStoreTransferRequest) istResult.getEntityList().get(0);

                        if (istRequest != null) {
                            JSONObject paramsJobj = new JSONObject();
                            paramsJobj.put("istRequestId", billid);
                            KwlReturnObject dodqcistmappingResult = stockService.getDODetailISTMapping(paramsJobj);
                            
                            if (dodqcistmappingResult.getEntityList().size() > 0) {
                                DODQCISTMapping DODQCISTMap = (DODQCISTMapping) dodqcistmappingResult.getEntityList().get(0);
                                doDetailId = DODQCISTMap.getDodetailID();

                                JSONObject returnJobj = getDOQAApprovalISTDetails(jArr, summaryData, istRequest, doDetailId, df, jsonObj);
                                if (returnJobj.has("summaryData")) {
                                    summaryData = returnJobj.getJSONObject("summaryData");
                                }
                                if (returnJobj.has("jArr")) {
                                    jArr = returnJobj.getJSONArray("jArr");
                                }
                                if (returnJobj.has("doId")) {
                                    doId = returnJobj.optString("doId", "");
                                }
                                if (returnJobj.has("doDetail")) {
                                    doDetail = (DeliveryOrderDetail) returnJobj.get("doDetail");
                                }
                                summaryData.put(CustomDesignerConstants.INSPECTION_STATUS, "APPROVED");
                            }
                        }
                    }
                    
                    HashMap<String, Object> paramMap = new HashMap();
                    paramMap.put(Constants.fieldMap, FieldMap);
                    paramMap.put(Constants.replaceFieldMap, null);
                    paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
                    paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
                    paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
                    requestObj.put("moduleid", moduleid);
                    
                    // Get all DO fields
                    JSONArray doFieldsJArr = accInvoiceCMNObj.getDODetailsItemJSON(requestObj, doId, paramMap);
                    
                    JSONObject doSummaryDataObj = doFieldsJArr.getJSONObject(1);
                    Iterator ite = doSummaryDataObj.keys();
                    while (ite.hasNext()){
                        String key = (String) ite.next();
                        summaryData.put(key, doSummaryDataObj.getString(key));
                    }
                    /*
                    * All Global Section Custom Field and DImensions
                    */
                    HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                    HashMap<String, Object> extraparams = new HashMap<String, Object>();
                    df = authHandler.getUserDateFormatter(request);//User Date Formatter
                    extraparams.put(Constants.companyid, companyid);
                    extraparams.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                    extraparams.put(Constants.customcolumn, 0);
                    extraparams.put(Constants.customfield, 1);
                    extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                    extraparams.put("billid", doId);
                    returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);

                    if (returnvalues.containsKey("summaryData")) {
                        summaryData = (JSONObject) returnvalues.get("summaryData");
                    }
                    /*
                     * get custom line data
                     */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    DeliveryOrderDetailCustomData jeDetailCustom = (DeliveryOrderDetailCustomData) doDetail.getDeliveryOrderDetailCustomData();
                    if (jeDetailCustom != null) {
                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, summaryData, kwlCommonTablesDAOObj, variableMap);
                    }
                }
                jArr.put(summaryData);
            }
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    /**
     * Get DO QA Approval global and line level details
     * @param jArr
     * @param summaryData
     * @param istRequest
     * @param doDetailId
     * @param df
     * @param jsonObj
     * @return
     * @throws JSONException 
     */
    public JSONObject getDOQAApprovalISTDetails(JSONArray jArr, JSONObject summaryData, InterStoreTransferRequest istRequest, String doDetailId, DateFormat df, JSONObject jsonObj) throws JSONException{
        JSONObject returnJobj = new JSONObject();
        try {
            DeliveryOrderDetail doDetail = null;
            KwlReturnObject doDetailsResult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), doDetailId);
            doDetail = (DeliveryOrderDetail) doDetailsResult.getEntityList().get(0);

            String doId = doDetail.getDeliveryOrder().getID();

            if(!StringUtil.isNullOrEmpty(doDetail.getInvlocid())){
                KwlReturnObject locationResult = accountingHandlerDAOobj.getObject(Location.class.getName(), doDetail.getInvlocid());
                Location locDetail = (Location) locationResult.getEntityList().get(0);
                summaryData.put(CustomDesignerConstants.Location, locDetail.getName());
            } else{
                summaryData.put(CustomDesignerConstants.Location, "");
            }
            if(!StringUtil.isNullOrEmpty(doDetail.getInvstoreid())){
                KwlReturnObject storeResult = accountingHandlerDAOobj.getObject(Store.class.getName(), doDetail.getInvstoreid());
                Store storeDetail = (Store) storeResult.getEntityList().get(0);
                summaryData.put(CustomDesignerConstants.Warehouse, storeDetail.getFullName());
            } else{
                summaryData.put(CustomDesignerConstants.Warehouse, "");
            }
            summaryData.put(CustomDesignerConstants.IN_ProductCode, doDetail.getProduct().getProductid());
            summaryData.put(CustomDesignerConstants.ProductName, doDetail.getProduct().getProductName());
            summaryData.put(CustomDesignerConstants.DO_QUANTITY, doDetail.getDeliveredQuantity());
            summaryData.put(CustomDesignerConstants.Poreferencenumber, doDetail.getDeliveryOrder().getCustomerPORefNo());

            summaryData.put(CustomDesignerConstants.INSPECTION_REF_NO, istRequest.getTransactionNo());
            summaryData.put(CustomDesignerConstants.INSPECTION_DESCRIPTION, doDetail.getProduct().getProductName());
            summaryData.put(CustomDesignerConstants.INSPECTOR, istRequest.getCreatedBy().getFullName());
            summaryData.put(CustomDesignerConstants.CustomDesignRemarks_fieldTypeId, istRequest.getRemark());

            Set<ISTDetail> istDetails = istRequest.getIstDetails();
            for (ISTDetail istDetail : istDetails) {
                summaryData.put(CustomDesignerConstants.Quantity, istDetail.getReturnQuantity());

                if (istRequest.getProduct().isIsBatchForProduct()) {
                    summaryData.put(CustomDesignerConstants.BatchNumber, istDetail.getBatchName());
                } else {
                    summaryData.put(CustomDesignerConstants.BatchNumber, "");
                }
                if (istRequest.getProduct().isIsSerialForProduct()) {
                    summaryData.put(CustomDesignerConstants.SerialNumber, istDetail.getIssuedSerialNames());
                } else {
                    summaryData.put(CustomDesignerConstants.SerialNumber, "");
                }

                InspectionForm insForm = istDetail.getInspectionForm();
                if(insForm != null){
                    JSONObject insFormDetailsJobj = getInspectionFormDetails(jArr, summaryData, insForm, df);
                    if(insFormDetailsJobj.has("summaryData")){
                        summaryData = insFormDetailsJobj.getJSONObject("summaryData");
                    }
                    if(insFormDetailsJobj.has("jArr")){
                        jArr = insFormDetailsJobj.getJSONArray("jArr");
                    }
                } else{
                    summaryData.put(CustomDesignerConstants.INSPECTION_DATE, df.format(istRequest.getModifiedOn()));
                    summaryData.put(CustomDesignerConstants.MODEL_NAME, doDetail.getProduct().getProductid());
                    summaryData.put(CustomDesignerConstants.INSPECTION_CUSTOMER_NAME, "");
                    summaryData.put(CustomDesignerConstants.DEPARTMENT, "");
                    summaryData.put(CustomDesignerConstants.CONSIGNMENT_RETURN_NO, istRequest.getTransactionNo());

                    InspectionTemplate insTemplate = istRequest.getProduct().getInspectionTemplate();

                    jArr = getInspectionTemplateDetails(jArr, insTemplate, jsonObj);
                }
            }
            
            returnJobj.put("doId", doId);
            returnJobj.put("doDetail", doDetail);
            returnJobj.put("summaryData", summaryData);
            returnJobj.put("jArr", jArr);
        } catch (Exception ex) {
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return  returnJobj;
    }
    
    /**
     * Get Inspection Form global and line level details
     * @param jArr
     * @param summaryData
     * @param insForm
     * @param df
     * @return
     * @throws JSONException 
     */
    public JSONObject getInspectionFormDetails(JSONArray jArr, JSONObject summaryData, InspectionForm insForm, DateFormat df) throws JSONException{
        JSONObject returnJobj = new JSONObject();
        try{
            summaryData.put(CustomDesignerConstants.INSPECTION_DATE, df.format(insForm.getInspectionDate()));
            summaryData.put(CustomDesignerConstants.MODEL_NAME, insForm.getModelName());
            summaryData.put(CustomDesignerConstants.INSPECTION_CUSTOMER_NAME, insForm.getCustomerName());
            summaryData.put(CustomDesignerConstants.DEPARTMENT, insForm.getDepartment());
            summaryData.put(CustomDesignerConstants.CONSIGNMENT_RETURN_NO, insForm.getConsignmentReturnNo());

            Set<InspectionFormDetails> insFormDetails = insForm.getRows();

            int srNo = 1;
            for(InspectionFormDetails insFormDetail : insFormDetails){
                JSONObject obj = new JSONObject();

                obj.put(CustomDesignerConstants.SrNo, srNo);
                obj.put(CustomDesignerConstants.INSPECTION_TEMPLATE_AREA, insFormDetail.getInspectionAreaValue());
                obj.put(CustomDesignerConstants.INSPECTION_TEMPLATE_STATUS, insFormDetail.getInspectionStatus());
                obj.put(CustomDesignerConstants.INSPECTION_TEMPLATE_FAULTS, insFormDetail.getFaults());

                if(insFormDetail.getInspectionArea() != null){
                    obj.put(CustomDesignerConstants.INSPECTION_TEMPLATE_SPECIFIED_FAULTS, insFormDetail.getInspectionArea().getFaults());
                } else{
                    obj.put(CustomDesignerConstants.INSPECTION_TEMPLATE_SPECIFIED_FAULTS, "");
                }

                jArr.put(obj);
                srNo++;
            }
            returnJobj.put("summaryData", summaryData);
            returnJobj.put("jArr", jArr);
            
        } catch(Exception e){
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return returnJobj;
    }
    /**
     * Get Inspection template line level(Inspection Area) details
     * @param jArr
     * @param insTemplate
     * @param params
     * @return
     * @throws JSONException 
     */
    public JSONArray getInspectionTemplateDetails(JSONArray jArr, InspectionTemplate insTemplate, JSONObject params) throws JSONException{
        try{
            if(insTemplate != null){
                String searchString = params.optString("ss", "");
                String start = params.optString("start", "");
                String limit = params.optString("limit", "");
                Paging paging = new Paging(start, limit);

                List<InspectionArea> insAreaList = templateService.getInspectionAreaList(insTemplate, searchString, paging);

                int srNo = 1;
                for(InspectionArea insArea : insAreaList){
                    JSONObject obj = new JSONObject();

                    obj.put(CustomDesignerConstants.SrNo, srNo);
                    obj.put(CustomDesignerConstants.INSPECTION_TEMPLATE_AREA, insArea.getName());
                    obj.put(CustomDesignerConstants.INSPECTION_TEMPLATE_STATUS, "");
                    obj.put(CustomDesignerConstants.INSPECTION_TEMPLATE_FAULTS, insArea.getFaults());
                    obj.put(CustomDesignerConstants.INSPECTION_TEMPLATE_SPECIFIED_FAULTS, insArea.getFaults());

                    jArr.put(obj);
                    srNo++;
                }
            }
        } catch(Exception e){
            Logger.getLogger(accExportInventoryTemplateController.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return jArr;
    }
}
