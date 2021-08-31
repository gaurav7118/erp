/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.mailnotifier;

import com.krawler.utils.json.base.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AccMailNotifyThread implements Runnable{
    private JSONObject requestParam;
    private AccMailNotifyService accMailNotifyServiceobj;
    public AccMailNotifyThread(){
     
    }
    public AccMailNotifyThread(JSONObject requestParam){
        this.requestParam=requestParam;
    }
    public AccMailNotifyService getAccMailNotifyServiceobj() {
        return accMailNotifyServiceobj;
    }
 
    public void setAccMailNotifyServiceobj(AccMailNotifyService accMailNotifyServiceobj) {
        this.accMailNotifyServiceobj = accMailNotifyServiceobj;
    }
    
     @Override
    public void run() {
        try {
            accMailNotifyServiceobj.sendInvoicesonMail(requestParam);
        } catch (Exception ex) {
            Logger.getLogger(AccMailNotifyThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
}
