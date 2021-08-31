/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.fileuploaddownlaod.impl;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.InventoryModules;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.fileuploaddownlaod.InvDocumentDAO;
import com.krawler.inventory.model.fileuploaddownlaod.InventoryDocumentCompMap;
import com.krawler.inventory.model.fileuploaddownlaod.InventoryDocuments;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author krawler
 */
public class InvDocumentDAOImpl extends BaseDAO implements InvDocumentDAO{
    
    @Override
    public KwlReturnObject saveInventoryDocuments(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            if (dataMap.containsKey("InventoryDocument")) {
                InventoryDocuments document = (InventoryDocuments) dataMap.get("InventoryDocument");
                saveOrUpdate(document);
                list.add(document);
            }
            if (dataMap.containsKey("InventoryDocumentMapping")) {
                InventoryDocumentCompMap documentCompMap = (InventoryDocumentCompMap) dataMap.get("InventoryDocumentMapping");
                saveOrUpdate(documentCompMap);
                list.add(documentCompMap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveinvoiceDocuments : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

     @Override
    public KwlReturnObject getInventoryDocuments(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String start = (String) dataMap.get(Constants.start);
            String limit = (String) dataMap.get(Constants.limit);

            ArrayList params = new ArrayList();

            String conditionSQL = " where in_documentcompmap.company=?";
            params.add((String) dataMap.get(Constants.companyKey));

            String moduleWiseId = (String) dataMap.get("modulewiseid");
            if (!StringUtil.isNullOrEmpty(moduleWiseId)) {
                conditionSQL += " and in_documentcompmap.modulewiseid=?";
                params.add(moduleWiseId);
            }
            
            InventoryModules module = InventoryModules.valueOf((String) dataMap.get("modulename"));
            if (!StringUtil.isNullOrEmpty(moduleWiseId)) {
                conditionSQL += " and in_documentcompmap.module=?";
                params.add(module.ordinal());
            }

            String mysqlQuery = "select in_documents.docname  as docname,in_documents.doctypeid as doctypeid,in_documents.docid as docid "
                    + "from in_documentcompmap inner join in_documents on in_documentcompmap.documentid=in_documents.id " + conditionSQL;

            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getinvoiceDocuments:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject deleteInventoryDocument(String documentID) throws ServiceException {
        List list = null;
        int numRows = 0;

        String query = "from InventoryDocuments invdoc where invdoc.docID=?";
        list = executeQuery( query, new Object[]{documentID});

        if (!list.isEmpty()) {
            query = "delete from InventoryDocumentCompMap invdocmap where invdocmap.document=?";
            numRows = executeUpdate( query, new Object[]{list.get(0)});
            delete(list.get(0));
            return new KwlReturnObject(true, "Document has been deleted successfully.", null, null, numRows);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
}
