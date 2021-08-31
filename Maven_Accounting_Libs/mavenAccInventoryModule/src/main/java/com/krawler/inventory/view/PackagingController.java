/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UOMSchema;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.packaging.PackagingDAO;
import com.krawler.inventory.model.packaging.PackagingService;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class PackagingController extends MultiActionController {

    private static final Logger lgr = Logger.getLogger(ApprovalController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private PackagingService packagingService;
    private accProductDAO accProductObj;

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public accProductDAO getAccProductObj() {
        return accProductObj;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public PackagingService getPackagingService() {
        return packagingService;
    }

    public void setPackagingService(PackagingService packagingService) {
        this.packagingService = packagingService;
    }

    public ModelAndView getPackagingList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            Product product = null;
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String productId = request.getParameter("productId");
            if (!StringUtil.isNullOrEmpty(productId)) {
                KwlReturnObject rtObj = accProductObj.getProductByID(productId, companyId);
                product = ((Product) rtObj.getEntityList().get(0));

                if (product != null) {
                    Packaging packaging = product.getPackaging();

                    if (packaging != null) {
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", packaging.getId());
                        double orderToStockUOMFactor = 1;
                        double transferToStockUOMFactor = 1;
                        String packag = "";
                        orderToStockUOMFactor = packaging.getStockUomQtyFactor(product.getOrderingUOM());
                        transferToStockUOMFactor = packaging.getStockUomQtyFactor(product.getTransferUOM());
                        packag = packaging.toString();
                        jObj.put("name", packag);
                        jArray.put(jObj);
                    }
                    issuccess = true;
                    msg = "Packaging  List  has been fetched successfully";
                }
//                if (product != null) {
//                    List<Packaging> pkg = packagingService.getPackagingList(product.getID());
//                    for (Packaging packaging : pkg) {
//                        JSONObject jObj = new JSONObject();
//                        jObj.put("id", packaging.getId());
//                        double orderToStockUOMFactor = 1;
//                        double transferToStockUOMFactor = 1;
//                        String packag = "";
//                        if (packaging != null) {
//                            orderToStockUOMFactor = packaging.getStockUomQtyFactor(product.getOrderingUOM());
//                            transferToStockUOMFactor = packaging.getStockUomQtyFactor(product.getTransferUOM());
//                            packag = packaging.toString();
//                        }
//                        jObj.put("name", packag);
//                        jArray.put(jObj);
//                    }
//                    issuccess = true;
//                    msg = "Packaging  List  has been fetched successfully";
//                }

            }
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getPackagingUOMList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String packagingId = request.getParameter("packagingId");
            Map<String, String> uommap = new HashMap<String, String>();
            Packaging pkg = packagingService.getPackaging(packagingId);
            if (pkg != null) {
                UnitOfMeasure stockUom = pkg.getStockUoM();
                if (stockUom != null && pkg.getStockUomValue() != 0) {
                    if (!uommap.containsKey(stockUom.getID())) {
                        uommap.put(stockUom.getID(), stockUom.getNameEmptyforNA());
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", stockUom.getID());
                        jObj.put("name", stockUom.getNameEmptyforNA());
                        jObj.put("factor", pkg.getStockUomQtyFactor(stockUom));
                        jArray.put(jObj);
                    }
                }
                UnitOfMeasure innerUom = pkg.getInnerUoM();
                if (innerUom != null && pkg.getInnerUomValue() != 0) {
                    if (!uommap.containsKey(innerUom.getID())) {
                        uommap.put(innerUom.getID(), innerUom.getNameEmptyforNA());
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", innerUom.getID());
                        jObj.put("name", innerUom.getNameEmptyforNA());
                        jObj.put("factor", pkg.getStockUomQtyFactor(innerUom));
                        jArray.put(jObj);
                    }
                }
                UnitOfMeasure casingUom = pkg.getCasingUoM();
                if (casingUom != null && pkg.getCasingUomValue() != 0) {
                    if (!uommap.containsKey(casingUom.getID())) {
                        uommap.put(casingUom.getID(), casingUom.getNameEmptyforNA());
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", casingUom.getID());
                        jObj.put("name", casingUom.getNameEmptyforNA());
                        jObj.put("factor", pkg.getStockUomQtyFactor(casingUom));
                        jArray.put(jObj);
                    }
                }

            }
            issuccess = true;
            msg = "Stock Adjustment Approval List  has been fetched successfully";

            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getUOMSchemaList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject obj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("STR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);

            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            String productId = request.getParameter("productId");
            String currentuomid = request.getParameter("currentuomid");
            String uomnature = request.getParameter("uomnature");

            if (!StringUtil.isNullOrEmpty(productId)) {

                KwlReturnObject rtObj = accProductObj.getProductByID(productId, companyId);
                if (rtObj.getEntityList().size() > 0) {
                    Product product = ((Product) rtObj.getEntityList().get(0));
                    if (product != null && product.getUomSchemaType() != null) {
                        String uomschematypeid = product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "";
                        requestParams.put("uomschematypeid", uomschematypeid);
                        requestParams.put("companyid", companyId);
                        requestParams.put("currentuomid", currentuomid);
                        requestParams.put("carryin", false);
                        requestParams.put("uomnature", uomnature);
                        KwlReturnObject res = packagingService.getProductBaseUOMRate(requestParams);
                        List list = res.getEntityList();
                        Iterator itr = list.iterator();
                        if (itr.hasNext()) {
                            UOMSchema row = (UOMSchema) itr.next();
                            if (row == null) {
                                obj.put("baseuomrate", 1);
                                obj.put("rateperuom", 0);
                            } else {
                                obj.put("baseuomrate", row.getBaseuomrate());
                                obj.put("rateperuom", row.getRateperuom());
                            }

                            jArray.put(obj);
                        } else {
                            obj.put("baseuomrate", 1);
                            obj.put("rateperuom", 0);

                            jArray.put(obj);
                        }
                    } else {
                        obj.put("baseuomrate", 1);
                        obj.put("rateperuom", 0);

                        jArray.put(obj);
                    }

                    issuccess = true;
                    msg = "Uom  List  has been fetched successfully";
                }
            }
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }
}
