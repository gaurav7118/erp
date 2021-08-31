/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.Docs;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class SyncDataUsingThread implements Runnable {

    private AccountingHandlerDAO accountingHandlerDAO;
    private accProductDAO accProductObj;
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private APICallHandlerService apiCallHandlerService;
    private HashMap<String, Object> requestParamsMap;
    private JSONObject inputParamJobj;
//    private List processList = new ArrayList();
//    boolean isWorking = false;

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

//    public void addParams(JSONObject json) {
//        processList.add(json);
//    }

    /**
     * @param requestParamsMap the requestParamsMap to set
     */
    public void setRequestParamsMap(HashMap<String, Object> requestParamsMap) {
        this.requestParamsMap = requestParamsMap;
    }

    /**
     * @param inputParamJobj the inputParamJobj to set
     */
    public void setInputParamJobj(JSONObject inputParamJobj) {
        this.inputParamJobj = inputParamJobj;
    }

    public void run() {
        JSONObject jobj = new JSONObject();
        Logger.getLogger(SyncDataUsingThread.class.getName()).log(Level.INFO, null, "PROCESS START- "+ new java.util.Date());
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean isAllSync = false;
        try {
            String companyid = inputParamJobj.getString("companyid");
            KwlReturnObject extraPref = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject result = accProductObj.getProductTypes(requestParams);
            List list = result.getEntityList();
            String cdomain = inputParamJobj.getString("cdomain");
            String userId = inputParamJobj.getString("userId");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", userId);
            userData.put("companyid", companyid);
            userData.put("cdomain", StringUtil.isNullOrEmpty(cdomain) ? "" : cdomain);
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/product";
            JSONArray DataJArr;
            JSONArray typeDataJsonArray = productHandler.getProductTypesJson(list);
            list.clear();
            jobj.put("typedata", typeDataJsonArray);
            typeDataJsonArray = null;
 
            int start = 0;
            int batchLimit = Constants.Customer_Sync_Batch_Count;
            int customerCount = 0;
            if(requestParamsMap.containsKey("isAllSync") && !StringUtil.isNullOrEmpty(requestParamsMap.get("isAllSync").toString())){
                isAllSync = Boolean.parseBoolean(requestParamsMap.get("isAllSync").toString());
            }
            
            if(isAllSync){
                customerCount = accProductObj.getSyncableProductsCount(requestParamsMap); 
            }else{
                if(requestParamsMap.get("ids") != null){
                    String ids[] = (String[]) requestParamsMap.get("ids");
                    customerCount = ids.length;
                }
            }
        
            int noOfCustomerBatch = customerCount / batchLimit;
            
            if(noOfCustomerBatch == 0){
               batchLimit =  customerCount;  
               noOfCustomerBatch = 1; // To execute first iteration if batch is zero.
            }
            
            if (extrareferences != null) {
                jobj.put(IndiaComplianceConstants.ISLINE_LEVELTERM_FLAG, extrareferences.getLineLevelTermFlag());
                jobj.put(IndiaComplianceConstants.ISEXCISEAPPLICABLE, extrareferences.isExciseApplicable());
                jobj.put(IndiaComplianceConstants.ENABLEVATCST, extrareferences.isEnableVatCst());
                if (extrareferences.getLineLevelTermFlag() == 1) {
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("salesOrPurchaseFlag", "true");
                    hashMap.put(Constants.companyKey, companyid);
                    jobj.put(IndiaComplianceConstants.COMPANY_LINELEVEL_TERMS, accProductObj.getCompanyTermsJsonArray(hashMap));
                }
            }
           
            for (int batchCount = 0; batchCount < noOfCustomerBatch; batchCount++) {
                requestParamsMap.put(Constants.start, start);
                requestParamsMap.put(Constants.limit, batchLimit);
                list = accProductObj.getSyncableProductList(requestParamsMap);
                if(list.size()>0) {
                    Logger.getLogger(SyncDataUsingThread.class.getName()).log(Level.INFO, null, "Product JSON Start - "+ new java.util.Date());
                    DataJArr = productHandler.getProductsJsonSync(inputParamJobj, list, accProductObj, accAccountDAOobj, accountingHandlerDAO, accCurrencyDAOobj, false);
                    jobj.put("productdata", DataJArr);
                    Logger.getLogger(SyncDataUsingThread.class.getName()).log(Level.INFO, null, "Product JSON End - "+ new java.util.Date());
                    /*
                    * To move file from Accounting Store to Shared Folder Store once product is shared with other Deskera applications.
                    */
                    moveFilesFromAccountingToSharedLocation(DataJArr);
                    DataJArr = null;
                    userData.put("data", jobj);
                    Logger.getLogger(SyncDataUsingThread.class.getName()).log(Level.INFO, null, "RestAPI Start  - "+ new java.util.Date());                    
                    apiCallHandlerService.restPostMethod(crmURL, userData.toString());
                    Logger.getLogger(SyncDataUsingThread.class.getName()).log(Level.INFO, null, "RestAPI End - "+ new java.util.Date());                    
                    start = (batchLimit * (batchCount + 1)) + 1;
                    list.clear();
                    jobj.remove("productdata");
                    userData.remove("data");
                }
            }
            txnManager.commit(status);

        } catch (Exception Ex) {
           txnManager.rollback(status);
            Logger.getLogger(SyncDataUsingThread.class.getName()).log(Level.SEVERE, null, Ex);
            Logger.getLogger(SyncDataUsingThread.class.getName()).log(Level.INFO, null, "PROCESS END in Exception block: "+ new java.util.Date());            
        }finally{
            Logger.getLogger(SyncDataUsingThread.class.getName()).log(Level.INFO, null, "PROCESS END in Finally block: " + new java.util.Date());
        }
    }
    /*
     * Below function used to move the Shared files from Accounting Specific
     * folder to Shared Folder
     */

    public void moveFilesFromAccountingToSharedLocation(JSONArray DataJArr) {
        for (int k = 0; k < DataJArr.length(); k++) {
            try {
                JSONObject job = DataJArr.getJSONObject(k);
                JSONArray jsarr = job.getJSONArray("shareddocs");
                for (int j = 0; j < jsarr.length(); j++) {
                    JSONObject jsobj = jsarr.getJSONObject(j);
                    String documentid = jsobj.getString("docid");
                    KwlReturnObject curreslt = accountingHandlerDAO.getObject(Docs.class.getName(), documentid);
                    Docs document = (Docs) curreslt.getEntityList().get(0);
                    String sourceFolder = StorageHandler.GetDocStorePath();
                    String targetFolder = StorageHandler.GetSharedDocStorePath();
                    File destinationFolder = new File(targetFolder);
                    if (!destinationFolder.exists()) {  //Create Target folder if it is not exist 
                        destinationFolder.mkdirs();
                    }
                    String ext = "";
                    if (document.getDocname().indexOf('.') != -1) {
                        ext = document.getDocname().substring(document.getDocname().indexOf('.'));
                    }
                    String sourcePath = sourceFolder + documentid + ext;
                    boolean check = new File(sourcePath).exists();    //Check source file is available in ERP folder or not
                    if (!check) {
                        continue;       //Skip if source file is already moved.
                    }
                    Path source = FileSystems.getDefault().getPath(sourcePath);
                    String targetPath = targetFolder + documentid + ext;
                    Path target = FileSystems.getDefault().getPath(targetPath);
                    try {
                        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);    //Available from Java 7
                    } catch (NoSuchFileException nfe) {
                        Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, nfe);
                    } catch (IOException e) {
                        Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}
