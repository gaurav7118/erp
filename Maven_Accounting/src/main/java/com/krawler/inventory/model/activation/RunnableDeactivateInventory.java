/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.activation;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.inventory.model.store.Store;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class RunnableDeactivateInventory implements Runnable {

    private InventoryActivationService activateInventoryService;
    private User user;
    private Map auditParams;

    public RunnableDeactivateInventory(InventoryActivationService activateInventoryService, User user, Map auditParams) {
        this.activateInventoryService = activateInventoryService;
        this.user = user;
        this.auditParams = auditParams;
    }

    @Override
    public void run() {
        try {
            deactivateInventory();
        } catch (ServiceException ex) {
            Logger.getLogger(RunnableDeactivateInventory.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            activateInventoryService.removeRunningStatus(user.getCompany());
        }
    }

    public void deactivateInventory() throws ServiceException {
        Company company = user.getCompany();
        
        Store qaStore = activateInventoryService.getQAStore(company);
        Store repairStore = activateInventoryService.getRepairStore(company);
        
        Date oldDate = new Date();
        activateInventoryService.completeInTransitSRRequests(user,qaStore,repairStore);
        Date newDate = new Date();
        double seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Complete SR IN-Transit : " + seconds);
        oldDate = new Date();

        activateInventoryService.completeInTransitISTRequests(user,qaStore,repairStore);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Complete IST IN-Transit : " + seconds);
        oldDate = new Date();

        activateInventoryService.completeINTransitSARequests(user,qaStore,repairStore);
        
        seconds = (newDate.getTime() - oldDate.getTime()) / 1000.0;
        System.out.println("Complete SA IN-Transit : " + seconds);
        oldDate = new Date();

        activateInventoryService.updateInventoryTab(company, false);
        newDate = new Date();
        seconds = (newDate.getTime() - oldDate.getTime()) / 60000.0;
        System.out.println("deactivate inventory SM : " + seconds);

        activateInventoryService.sendDeactivationMail(user);

        String msg = "User "+auditParams.get("userFullName")+" deactivated inventory module.";
        activateInventoryService.addAuditlog(auditParams, msg);
        
    }

}