/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.documentdetails;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.krawler.utils.json.JSONArray;
import com.krawler.utils.json.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class accDetailsImpl extends BaseDAO implements accDetailsDAO {

    @Override
    public KwlReturnObject addComments(HashMap<String, Object> requestParam) throws ServiceException {
        List list = new ArrayList();
        int dl = 0;
        Comment comment = new Comment();
        try {

            if (requestParam.get("User") != null) {
                comment.setUser((User) requestParam.get("User"));
            }
            if (requestParam.get("moduleId") != null) {
                comment.setModuleId((String) requestParam.get("moduleId"));
            }
            if (requestParam.get("refid") != null) {
                comment.setRecordId((String) requestParam.get("refid"));
            }
            if (requestParam.get("id") != null) {
                comment.setId((String) requestParam.get("id"));
            }
            if (requestParam.get("comment") != null) {
                comment.setComment((String) requestParam.get("comment"));
            }
            if (requestParam.get("mapid") != null) {
                comment.setRelatedto((String) requestParam.get("mapid"));
            }
            if (requestParam.get("Company") != null) {
                comment.setCompany((Company) requestParam.get("Company"));
            }
            if (requestParam.get("postedon") != null) {
                comment.setPostedon((Date) requestParam.get("postedon"));
            }

            save(comment);

            list.add(comment);
        } catch (Exception e) {
            throw ServiceException.FAILURE("accDetailsImpl.addComments : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject editComments(HashMap<String, Object> requestParam) throws ServiceException {
        List list = new ArrayList();
        int dl = 0;
        try {
            Comment comment = new Comment();
            if (requestParam.get("User") != null) {
                comment.setUser((User) requestParam.get("User"));
            }
            if (requestParam.get("moduleId") != null) {
                comment.setModuleId((String) requestParam.get("moduleId"));
            }
            if (requestParam.get("refid") != null) {
                comment.setRecordId((String) requestParam.get("refid"));
            }
            if (requestParam.get("id") != null) {
                comment.setId((String) requestParam.get("id"));
            }
            if (requestParam.get("comment") != null) {
                comment.setComment((String) requestParam.get("comment"));
            }
            if (requestParam.get("mapid") != null) {
                comment.setRelatedto((String) requestParam.get("mapid"));
            }
            if (requestParam.get("Company") != null) {
                comment.setCompany((Company) requestParam.get("Company"));
            }
            if (requestParam.get("postedon") != null) {
                comment.setPostedon((Date) requestParam.get("postedon"));
            }
            if (requestParam.get("updatedon") != null) {
                comment.setPostedon((Date) requestParam.get("updatedon"));
            }

//            comment.setUpdatedon(new Date());
//            comment.setUpdatedon(new Date());

            merge(comment);
            list.add(comment);

            // insertCommentUserMapping(comment, companyid, userid, cid);

        } catch (Exception e) {
            throw ServiceException.FAILURE("accDetailsImpl.addComments : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getComments(HashMap requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String recid = "", moduleid = "";
            if (requestParams != null) {
                recid = (String) requestParams.get("recid");
                moduleid = (String) requestParams.get("module");
            }
            String Hql = " FROM Comment c  where c.recordId=? and c.moduleId=? order by c.postedon desc";
            ll = executeQuery( Hql, new Object[]{recid, moduleid});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("crmCommentDAOImpl.getComments : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    @Override
    public KwlReturnObject deleteComments(String id) throws ServiceException {

        boolean successflag = false;
        Comment comment = (Comment) get(Comment.class, id);
        try {
            if (comment != null) {
                delete(comment);
                successflag = true;
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("accDetailsImpl.deleteComments : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Comment has been deleted successfully.", null, null, 1);
    }

    @Override
    public KwlReturnObject getDocuments(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        String recid = "", moduleid = "";
        try {
            if (requestParams != null) {
                recid = (String) requestParams.get("recid");
                moduleid = (String) requestParams.get("module");
            }
            String Hql = "FROM com.krawler.common.admin.Docs dm where dm.recordId=? and dm.moduleId=? and dm.deleteflag!=1 order by uploadedon desc";
            ll = executeQuery( Hql, new Object[]{recid, moduleid});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("documentDAOImpl.getDocuments : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    @Override
    public KwlReturnObject saveDocuments(HashMap<String, Object> requestParam) throws ServiceException {
        List list = new ArrayList();
        try {
            Docs docs = new Docs();
            if (requestParam.get("DocumentId") != null) {
                docs.setId((String) requestParam.get("DocumentId"));
            }
            if (requestParam.get("DocumentNm") != null) {
                docs.setDocname((String) requestParam.get("DocumentNm"));
            }
            if (requestParam.get("DocumentType") != null) {
                docs.setDoctype("");
            }
            if (requestParam.get("DocumentSize") != null) {
                docs.setDocsize("" + requestParam.get("DocumentSize"));
            }
            if (requestParam.get("User") != null) {
                docs.setUser((User) requestParam.get("User"));
            }
            if (requestParam.get("recordId") != null) {
                docs.setRecordId((String) requestParam.get("recordId"));
            }
            if (requestParam.get("moduleId") != null) {
                docs.setModuleId((String) requestParam.get("moduleId"));
            }
            if (requestParam.get("Company") != null) {
                docs.setCompany((Company) requestParam.get("Company"));
            }
            docs.setUploadedon(new Date());


            save(docs);
            list.add(docs);
        } catch (Exception e) {
            throw ServiceException.FAILURE("documentDAOImpl.saveDocuments : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteDocument(String docsId) throws ServiceException {
        boolean successflag = false;
        Docs document = (Docs) get(Docs.class, docsId);
        try {
            if (document != null) {
                document.setDeleteflag(1);
                merge(document);
                successflag = true;
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("accDetailsImpl.deleteComments : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Document has been deleted successfully.", null, null, 1);
    }

    @Override
    public KwlReturnObject downloadDocument(String id) throws ServiceException {

        List ll = null;
        int dl = 0;
        try {
            ll = executeQuery( "FROM com.krawler.common.admin.Docs AS doc where doc.id =?", new Object[]{id});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
    
    public KwlReturnObject getQuotationDocumentList(HashMap<String, Object> requestParams) throws ServiceException {
        int start = 0;
        int limit = 20;
        int dl = 0;
        List ll = new ArrayList();
        Object[] params = null;
        try {
            String tagSearch = requestParams.containsKey("tag") ? requestParams.get("tag").toString() : "";
            String quickSearch = requestParams.containsKey("ss") ? requestParams.get("ss").toString() : "";
            if (requestParams.containsKey("tagSearch") && requestParams.get("tagSearch") != null) {
                tagSearch = quickSearch;
                quickSearch = "";
            }

            String quotationid = "";
            if (requestParams.containsKey("quotationid") && !StringUtil.isNullOrEmpty("quotationid")) {
                quotationid = requestParams.get("quotationid").toString();
            }

            if (requestParams.containsKey("start") && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
            }
            String modulequery="";
            int moduleid=Integer.parseInt(requestParams.get("moduleid").toString());
            switch (moduleid) {
                case Constants.Acc_Invoice_ModuleId:
                    modulequery="select inv.product.ID from InvoiceDetail ind inner join ind.inventory inv where ind.invoice.ID=?";
                    break;
                case Constants.Acc_Vendor_Invoice_ModuleId:
                    modulequery="select inv.product.ID from GoodsReceiptDetail grd inner join grd.inventory inv where grd.goodsReceipt.ID=?";
                    break;
                case Constants.Acc_Customer_Quotation_ModuleId:
                    modulequery="select product.ID from QuotationDetail where quotation.ID=?";
                    break;
                case Constants.Acc_Vendor_Quotation_ModuleId:
                    modulequery="select product.ID from VendorQuotationDetail where vendorquotation.ID=?";
                    break;
                case Constants.Acc_Sales_Order_ModuleId:
                    modulequery="select product.ID from SalesOrderDetail where salesOrder.ID=?";
                    break;
                case Constants.Acc_Purchase_Order_ModuleId:
                    modulequery="select product.ID from PurchaseOrderDetail where purchaseOrder.ID=?";
                    break;
                case Constants.Acc_Delivery_Order_ModuleId:
                    modulequery="select product.ID from DeliveryOrderDetail where deliveryOrder.ID=?";
                    break;
                case Constants.Acc_Goods_Receipt_ModuleId:
                    modulequery="select product.ID from GoodsReceiptOrderDetails where grOrder.ID=?";
                    break;
                case Constants.Acc_Sales_Return_ModuleId:
                    modulequery="select product.ID from SalesReturnDetail where salesReturn.ID=?";
                    break;
                case Constants.Acc_Purchase_Return_ModuleId:
                    modulequery="select product.ID from PurchaseReturnDetail where purchaseReturn.ID=?";
                    break;
                case Constants.Acc_Purchase_Requisition_ModuleId: 
                    modulequery="select product.ID from PurchaseRequisitionDetail where purchaserequisition.ID=?";
                    break;
            }
            String companyid = requestParams.get("companyid").toString();
            String Hql = "select c from com.krawler.common.admin.Docs c where c.company.companyID=? and c.deleteflag=0 and (c.recordId=? OR c.recordId in("+modulequery+")) ";
            params = new Object[]{companyid, quotationid, quotationid};

            if (!StringUtil.isNullOrEmpty(tagSearch)) {
                tagSearch = tagSearch.replaceAll("'", "");
                Hql += " and c.tags like '%" + tagSearch + "%' ";
            }
            if (!StringUtil.isNullOrEmpty(quickSearch)) {
                Hql += " and c.docname like '" + quickSearch + "%' ";
            }
            String selectInQuery = "";

            selectInQuery = Hql;
            ll = executeQuery(selectInQuery, params);

            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("documentDAOImpl.getQuotationDocumentList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
    
    /**
     * description : delete shared documnets between Accounting & other Apps.
     * @param Docs ID
     * @return boolean
     */
    public boolean deleteSharedDocuments(String docid) {
        boolean successflag = false;
        Docs document = (Docs) get(Docs.class, docid);
        try {
            if (document != null) {
                document.setDeleteflag(1);
                merge(document);
                successflag = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accDetailsImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(accDetailsImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return successflag;
    }
    /**
     * description : Get documents attached to transactions.
     * @param dataMap
     * @return
     * @throws ServiceException 
     * @return KwlReturnObject
     */
    @Override
    public KwlReturnObject getTransactionDocuments(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String start = (String) dataMap.get(Constants.start);
            String limit = (String) dataMap.get(Constants.limit);
            String quickSearch = dataMap.containsKey("ss") ? dataMap.get("ss").toString() : "";
            ArrayList params = new ArrayList();

            params.add((String) dataMap.get(Constants.companyKey));

            String conditionSQL = " where invoicedoccompmap.company=?";

            String transactionId = (String) dataMap.get("quotationid");
            if (!StringUtil.isNullOrEmpty(transactionId)) {
                params.add(transactionId);
                conditionSQL += " and invoicedoccompmap.invoiceid=?";
            }
            if (!StringUtil.isNullOrEmpty(quickSearch)) {
                conditionSQL += " and invoicedocuments.docname like '" + quickSearch + "%' ";
            }
            String mysqlQuery = "select invoicedocuments.docid as docid,invoicedocuments.docname  as docname,invoicedocuments.doctypeid as doctypeid "
                    + "from invoicedoccompmap inner join invoicedocuments on invoicedoccompmap.documentid=invoicedocuments.id " + conditionSQL;

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
}
