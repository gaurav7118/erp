/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.activation;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.activation.InventoryActivationService;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public class RunnableActivateInventory implements Runnable {

    private JSONObject setupObject;
    private InventoryActivationService activateInventoryService;
    private User user;
    private Map auditParams;

    public RunnableActivateInventory(JSONObject setupObject, InventoryActivationService activateInventoryService, User user, Map auditParams) {
        this.setupObject = setupObject;
        this.activateInventoryService = activateInventoryService;
        this.user = user;
        this.auditParams = auditParams;
    }

    @Override
    public void run() {
        try {
            activateInventory();
        } catch (ServiceException ex) {
            Logger.getLogger(RunnableActivateInventory.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            activateInventoryService.removeRunningStatus(user.getCompany());
        }
    }

    public void activateInventory() throws ServiceException {
        JSONObject companyLevel = setupObject.optJSONObject("companyLevel");
        JSONObject productLevel = setupObject.optJSONObject("productLevel");
        Company company = user.getCompany();

        
        
        activateInventoryService.maintainWarehouseLocationHirarchy(company);
        
        boolean activateBatch = companyLevel.optBoolean("activateBatch"); 
        boolean activateSerial = companyLevel.optBoolean("activateSerial");
        activateInventoryService.activateLocationWarehouseInCompany(user, activateBatch, activateSerial);



        Store store = activateInventoryService.getDefaultStore(company);
        InventoryWarehouse iw = activateInventoryService.getERPWarehouse(store);
        InventoryLocation il = activateInventoryService.getERPLocation(store.getDefaultLocation());


        String plMsg = "Do not activated for existing products";
        String activateForOldProducts = productLevel.optString("activateType");
        if (!"NONE".equals(activateForOldProducts)) {
            List<Product> productList = new ArrayList();
            if ("ALL".equals(activateForOldProducts)) {
                productList = activateInventoryService.getInvactivatedInventoryProducts(user.getCompany(), null, null);
                plMsg = "Activated for all existing Product";
            }
            if ("SELECTED".equals(activateForOldProducts)) {
                String productIds = productLevel.optString("productIds");// 
                String[] products = productIds.split(",");
                List<String> productIdList = Arrays.asList(products);
                productList = activateInventoryService.getSelectedProducts(productIdList);
                plMsg = "Activated for Selected Products ";
            }
            String productCodes = "";
            Date oldDate = new Date();
            for (Product product : productList) {
                activateInventoryService.activateLocationWarehouseInProduct(product, iw, il);
                if(StringUtil.isNullOrEmpty(productCodes)){
                    productCodes += ", ";
                }
                productCodes += product.getProductid();
            }
            Date newDate = new Date();
            double seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
            System.out.println("Tag for " + productList.size() + " Products : " + seconds);
            
            if ("SELECTED".equals(activateForOldProducts)) {
                plMsg += " - "+productCodes;
            }
        }

        Date oldDate = new Date();



        activateInventoryService.removeStockDataFromInventoryTables(company);
        Date newDate = new Date();
        double seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Remove from SM : " + seconds);
        oldDate = new Date();

        Map<String, Store> storeMap = activateInventoryService.getAllStores(company);
        Map<String, Location> locationMap = activateInventoryService.getAllLocations(company);

        activateInventoryService.insertSMForOpeningTransactions(company, storeMap, locationMap);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Insert SM for OPENING: " + seconds);
        oldDate = new Date();

        activateInventoryService.insertSMForGRNTransactions(company, storeMap, locationMap);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Insert SM for GRN : " + seconds);
        oldDate = new Date();

        activateInventoryService.insertSMForDOTransactions(company, storeMap, locationMap);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Insert SM for DO : " + seconds);
        oldDate = new Date();

        activateInventoryService.insertSMForPRTransactions(company, storeMap, locationMap);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Insert SM for PR : " + seconds);
        oldDate = new Date();

        activateInventoryService.insertSMForSRTransactions(company, storeMap, locationMap);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Insert SM for SR : " + seconds);
        oldDate = new Date();

        activateInventoryService.insertSMForPBTransactions(company, storeMap, locationMap);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Insert SM for PB : " + seconds);
        oldDate = new Date();

        activateInventoryService.insertSMForPBDTransactions(company, storeMap, locationMap);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Insert SM for PBD : " + seconds);
        oldDate = new Date();

        activateInventoryService.insertStockFromSM(company, storeMap, locationMap);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("insertStockFromSM : " + seconds);
        oldDate = new Date();

        activateInventoryService.updateInventoryTab(company, true);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 60000.0;
        System.out.println("activate inventory SM : " + seconds);


        activateInventoryService.sendActivationMail(user);

        String clMsg = "Company Level - Warehouse, Location "+(activateBatch? ", Batch":"")+(activateBatch? ", Serial":"");
        String msg = "User "+auditParams.get("userFullName")+" activated inventory module. "+clMsg+" and Product Level - "+plMsg;
        activateInventoryService.addAuditlog(auditParams, msg);
        
    }

}