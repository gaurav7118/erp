/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.invoice;

import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AccPaymentThread implements Runnable {

    private JSONObject requestParam;
    public accDeliveryPlannerController accDeliveryPlanner;
    private ArrayList list=new ArrayList();
    private boolean isworking = false;
    
    public AccPaymentThread() {
    }

    public AccPaymentThread(JSONObject requestParam) {
        this.requestParam = requestParam;
    }

    public void setRequestParam(JSONObject requestParam) {
        this.requestParam = requestParam;
    }

    public void setAccDeliveryPlanner(accDeliveryPlannerController accDeliveryPlanner) {
        this.accDeliveryPlanner = accDeliveryPlanner;
    }
    public void add(JSONObject requestParam){
         list.add(requestParam);
     }

    @Override
    public void run() {
        try {
            while (!list.isEmpty() && !isworking) {
                JSONObject reqMap = (JSONObject) list.get(0);
                isworking = true;
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    System.out.println(dateFormat.format(date));
                    System.out.println("Payment Thread Started for subdomain: " + reqMap.optString("subdomain") + ":" + dateFormat.format(date));
                    accDeliveryPlanner.insertPaymentDataFromOneDBToOtherDB(reqMap);
                    date = new Date();
                    System.out.println("Payment Thread Ended for subdomain: " + reqMap.optString("subdomain") + ":" + dateFormat.format(date));
                } catch (Exception ex) {
                    Logger.getLogger(AccPaymentThread.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    isworking = false;
                    list.remove(reqMap);
                }

            }
            System.out.println("###########Payment Thread Ended#############");
        } catch (Exception ex) {
            Logger.getLogger(AccPaymentThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
