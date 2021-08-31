/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.esp.handlers;

import com.mchange.v2.c3p0.ConnectionCustomizer;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.util.Constants;
/**
 *
 * @author krawler
 */
public class ConnectionPoolLogger  implements ConnectionCustomizer{

//    private static final Logger logger = Logger
//            .getLogger(ConnectionPoolLogger.class);
    private int activeConnections = 0;
    private int acquiredConnections = 0;
    private int maxPoolSize = 50;
    public void onAcquire(Connection c, String pdsIdt) {
//        Logger.getLogger(ConnectionPoolLogger.class.getName()).log(Level.INFO, "onAcquire: Connection acquired from database : " + c
//                + " [" + pdsIdt + "]");
        acquiredConnections++;
        
//        if(!Constants.connectionThreads.containsKey(pdsIdt)) {
//            Constants.connectionThreads.put(pdsIdt, printStack());
//        }
        if(acquiredConnections > maxPoolSize) {
            Logger.getLogger(ConnectionPoolLogger.class.getName()).log(Level.INFO, "onAcquire: Total Open Connections in Pool : " + acquiredConnections);
        }
    }

    public void onDestroy(Connection c, String pdsIdt) {
//        Logger.getLogger(ConnectionPoolLogger.class.getName()).log(Level.INFO, "onDestroy: Connection closed with database : " + c + " ["
//                + pdsIdt + "]");
        acquiredConnections--;
//        if(Constants.connectionThreads.containsKey(pdsIdt)) {
//            Constants.connectionThreads.remove(pdsIdt);
//        }
        Logger.getLogger(ConnectionPoolLogger.class.getName()).log(Level.INFO, "onDestroy: Total Open Connections in Pool : " + acquiredConnections);
        
    }

    public void onCheckOut(Connection c, String pdsIdt) {
//        Logger.getLogger(ConnectionPoolLogger.class.getName()).log(Level.INFO, "onCheckOut: Connection from pool provide to application : "
//                + c + " [" + pdsIdt + "]");
        activeConnections++;
        if(!Constants.connectionThreads.containsKey(pdsIdt)) {
            Constants.connectionThreads.put(pdsIdt, printStack());
        }

        if(activeConnections > maxPoolSize) {
            Logger.getLogger(ConnectionPoolLogger.class.getName()).log(Level.INFO, "onCheckOut: Total Active Connections in Pool : " + activeConnections);
        }
    }

    public void onCheckIn(Connection c, String pdsIdt) {
//        Logger.getLogger(ConnectionPoolLogger.class.getName()).log(Level.INFO, "onCheckIn: Connection returned to pool from application : "
//                + c + " [" + pdsIdt + "]");
        activeConnections--;
        if(Constants.connectionThreads.containsKey(pdsIdt)) {
            Constants.connectionThreads.remove(pdsIdt);
        }
        if(activeConnections > maxPoolSize) {
            Logger.getLogger(ConnectionPoolLogger.class.getName()).log(Level.INFO, "onCheckIn: Total Active Connections in Pool : " + activeConnections);
        }
    }
    
    public String printStack() {
        StringBuilder stackstring = new StringBuilder();
        stackstring.append("================================== Thread ID "+Thread.currentThread().getId()+"\n");
        for (int cnt=0; cnt< Thread.currentThread().getStackTrace().length; cnt++) {
            stackstring.append(Thread.currentThread().getStackTrace()[cnt].toString());
        }
        return stackstring.toString();
    }
}