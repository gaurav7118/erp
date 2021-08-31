/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.auditTrailModule;


import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author krawler
 */
public class AuditIndex implements Runnable {

    private boolean working = false;
    private List<AuditIndexData> auditIndexQueue = new ArrayList();
    private static final com.krawler.esp.indexer.CreateIndex cIndex = new com.krawler.esp.indexer.CreateIndex();

    protected final Log logger = LogFactory.getLog(getClass());

    public boolean isWorking() {
        return working;
    }
    
    public void addAuditIndexData(AuditIndexData auditIndexData){
        auditIndexQueue.add(auditIndexData);
    }
    public void addAuditIndexData(List<String> indexNames, List<Object> indexValues, String indexPath){
        AuditIndexData auditIndexData = new AuditIndexData(indexNames, indexValues, indexPath);
        addAuditIndexData(auditIndexData);
    }

    @Override
    public synchronized void run() {
        try {
            while(!auditIndexQueue.isEmpty()){
                working = true;
                AuditIndexData auditIndexObj = (AuditIndexData) auditIndexQueue.get(0);
                try {
                    List<String> indexNames = auditIndexObj.getIndexNames();
                    List<Object> indexValues = auditIndexObj.getIndexValues();
                    String indexPath = auditIndexObj.getIndexPath();
                    com.krawler.esp.indexer.KrawlerIndexCreator kwlIndex = new com.krawler.esp.indexer.KrawlerIndexCreator();
                    kwlIndex.setIndexPath(indexPath);
                    cIndex.indexAlert(kwlIndex, (ArrayList)indexValues, (ArrayList)indexNames);
                } catch (Exception ex) {
                    logger.warn("AuditIndexLogEntry: " + ex.getMessage(), ex);
                } finally {
                    auditIndexQueue.remove(auditIndexObj);
                }
            }
            
        } catch (Exception ex) {
            logger.warn("AuditIndexLogEntry: " + ex.getMessage(), ex);
        } finally {
            working = false;
        }
    }
}
