/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.accounting.handler;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.JSONVIEW;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.MODEL;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.MSG;
import static com.krawler.spring.accounting.goodsreceipt.GoodsReceiptCMNConstants.SUCCESS;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptController;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author k3
 */
public class AccLinkDataController extends MultiActionController {
    private LinkInformationHandler linkInformationHandlerObj;
    private exportMPXDAOImpl exportDaoObj;

    public void setLinkInformationHandlerObj(LinkInformationHandler linkInformationHandlerObj) {
        this.linkInformationHandlerObj = linkInformationHandlerObj;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public ModelAndView getMonthWiseLinkingReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String buildHtml="";
        JSONArray DataJArr = new JSONArray();
        try {
            
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String documentFilter = (String) request.getParameter("ss");
            if (!StringUtil.isNullOrEmpty(documentFilter)) {
                buildHtml = getLinkedDocument(request,DataJArr);
            } else {
                DateFormat df = authHandler.getDateOnlyFormat(request);
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.REQ_startdate, df.parse(request.getParameter(Constants.REQ_startdate)));
                requestParams.put(Constants.REQ_enddate, df.parse(request.getParameter(Constants.REQ_enddate)));
                buildHtml = linkInformationHandlerObj.getPurchaseSideLinkingHTML(companyid, requestParams,DataJArr);

            }          
            jobj.put("html", buildHtml);
            
            issuccess = true;
        } catch (Exception ex) {
            msg = "accGoodsReceiptController.getMonthWiseGoodsReceiptsDue : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public ModelAndView exportMonthWiseLinkingPurchaseReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        boolean isSummaryReport = false;
        String view = "jsonView_ex";
        String msg = "";
        String buildHtml = "";
        try {
            /*
             Put Request params into Map
             */
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String documentFilter = (String) request.getParameter("ss");
            if (!StringUtil.isNullOrEmpty(documentFilter)) {
                buildHtml = getLinkedDocument(request, DataJArr);
            } else {
                DateFormat df = authHandler.getDateOnlyFormat(request);
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.REQ_startdate, df.parse(request.getParameter(Constants.REQ_startdate)));
                requestParams.put(Constants.REQ_enddate, df.parse(request.getParameter(Constants.REQ_enddate)));
                buildHtml = linkInformationHandlerObj.getPurchaseSideLinkingHTML(companyid, requestParams, DataJArr);

            }
            request.setAttribute("isExport", true);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getMonthWiseLinkingSalesReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String buildHtml="";
        JSONArray DataJArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String documentFilter = (String)request.getParameter("ss");
            if (!StringUtil.isNullOrEmpty(documentFilter)){
                buildHtml = getLinkedDocument(request,DataJArr);
            } else {
                DateFormat df = authHandler.getDateOnlyFormat(request);
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.REQ_startdate, df.parse(request.getParameter(Constants.REQ_startdate)));
                requestParams.put(Constants.REQ_enddate, df.parse(request.getParameter(Constants.REQ_enddate)));
                buildHtml = linkInformationHandlerObj.getSalesSideLinkingHTML(companyid, requestParams,DataJArr);

            }

            jobj.put("html", buildHtml);
            
            issuccess = true;
        } catch (Exception ex) {
            msg = "accGoodsReceiptController.getMonthWiseGoodsReceiptsDue : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    public ModelAndView exportMonthWiseLinkingSalesReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        try {
            /*
             Put Request params into Map
             */
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String documentFilter = (String) request.getParameter("ss");
            if (!StringUtil.isNullOrEmpty(documentFilter)) {
                getLinkedDocument(request, DataJArr);
            } else {
                DateFormat df = authHandler.getDateOnlyFormat(request);
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.REQ_startdate, df.parse(request.getParameter(Constants.REQ_startdate)));
                requestParams.put(Constants.REQ_enddate, df.parse(request.getParameter(Constants.REQ_enddate)));
                linkInformationHandlerObj.getSalesSideLinkingHTML(companyid, requestParams, DataJArr);

            }
            request.setAttribute("isExport", true);
            jobj.put("data", DataJArr);
            jobj.put("count", DataJArr.length());
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
        
    public void getLinkedRequestParams(HttpServletRequest request, HashMap<String, Object> requestParams) throws SessionExpiredException{
        int requestModuleid = !StringUtil.isNullOrEmpty(request.getParameter("requestModuleid")) ? Integer.parseInt(request.getParameter("requestModuleid")) : -1;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        requestParams.put("companyid", companyid);
        requestParams.put("sourceflag", 0);
        requestParams.put("moduleid", requestModuleid);
        requestParams.put("isAdvanceSearch", true);
    }
    
    public ModelAndView getLinkedSINo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedSINo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billid", obj[0]);
                jobject.put("billno", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedSINo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    
    public ModelAndView getLinkedDONo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedDONo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billid", obj[0]);
                jobject.put("billno", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedDONo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    
    public ModelAndView getLinkedSONo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedSONo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billid", obj[0]);
                jobject.put("billno", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedSONo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    
    public ModelAndView getLinkedCQNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedCQNo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billno", obj[0]);
                jobject.put("billid", obj[2]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedCQNo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    
    public ModelAndView getLinkedVQNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedVQNo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billno", obj[0]);
                jobject.put("billid", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedVQNo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    
    public ModelAndView getLinkedPONo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedPONo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billno", obj[0]);
                jobject.put("billid", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedPONo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    
    public ModelAndView getLinkedGRNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedGRNo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billno", obj[0]);
                jobject.put("billid", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedGRNo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    
    public ModelAndView getLinkedPINo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedPINo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billno", obj[0]);
                jobject.put("billid", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedPINo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    public ModelAndView getLinkedPRNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedPRNo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billno", obj[0]);
                jobject.put("billid", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedPRNo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    /**
     * 
     * @param request 
     * @param response
     * @return = return Link RFQ NO
     */
    public ModelAndView getLinkedRFQNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedRFQNo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billno", obj[0]);
                jobject.put("billid", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedRFQNo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

        public ModelAndView getLinkedDebitNoteNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            requestParams.put("isFromPayment", true);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedDebitNoteNo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billno", obj[0]);
                jobject.put("billid", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedDebitNoteNo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    public ModelAndView getLinkedCreditNoteNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        List<Object[]> list = new ArrayList();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            getLinkedRequestParams(request, requestParams);
            KwlReturnObject kwlq = linkInformationHandlerObj.getLinkedCreditNoteNo(requestParams);
            list = kwlq.getEntityList();
            for (Object[] obj : list) {
                JSONObject jobject = new JSONObject();
                jobject.put("billid", obj[0]);
                jobject.put("billno", obj[1]);
                jarr.put(jobject);
            }
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedCreditNoteNo : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }
    
    public String getLinkedDocument(HttpServletRequest request,JSONArray jSONArray) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
      
        boolean issuccess = false;
        String msg = "";
        String buildHtml="";
        try {
           boolean isCustomer=Boolean.parseBoolean(request.getParameter("isCustomer"));
        
           if(isCustomer){
           buildHtml=  getLinkedDocumentForSalesSide(request,jSONArray); 
           }else{
             buildHtml=getLinkedDocumentForPurchaseSide(request,jSONArray);  
           }
            
            jobj.put("data", jarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedDocument : " + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLinkDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       return buildHtml;
    }
    
     public String getLinkedDocumentForSalesSide(HttpServletRequest request,JSONArray jSONArray) {
       
        String msg = "";
        String buildHtml="";
        try {
           int documentType=Integer.parseInt(request.getParameter("documentType"));
           String documentNo=(String)request.getParameter("ss");
           String companyid = sessionHandlerImpl.getCompanyid(request);
           HashMap<String, Object> requestParams = new HashMap<String, Object>();
           requestParams.put("documentType", documentType);
           requestParams.put("documentNo", documentNo);
           requestParams.put("companyId", companyid);
           
            if (documentType == Constants.Acc_Delivery_Order_ModuleId) {
                buildHtml = linkInformationHandlerObj.getDOLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Sales_Order_ModuleId) {
                buildHtml = linkInformationHandlerObj.getSOLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Invoice_ModuleId) {
                buildHtml = linkInformationHandlerObj.getSILinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Sales_Return_ModuleId) {
                buildHtml = linkInformationHandlerObj.getSRLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Customer_Quotation_ModuleId) {
                buildHtml = linkInformationHandlerObj.getCQLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Credit_Note_ModuleId) {
                buildHtml = linkInformationHandlerObj.getCNLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Receive_Payment_ModuleId) {
                buildHtml = linkInformationHandlerObj.getRPLinkingInfo(requestParams,jSONArray);
            }
            
            
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedDocumentForSalesSide : " + ex.getMessage();
        } 
        return buildHtml;
    }
     
      public String getLinkedDocumentForPurchaseSide(HttpServletRequest request,JSONArray jSONArray) {
    
        String msg = "";
        String buildHtml="";
        try {
           DateFormat df = authHandler.getDateOnlyFormat(request);
           int documentType=Integer.parseInt(request.getParameter("documentType"));
           boolean linkedPredecessor=Boolean.parseBoolean(request.getParameter("linkedPredecessor"));
           String documentNo=(String)request.getParameter("ss");
           String companyid = sessionHandlerImpl.getCompanyid(request);
           HashMap<String, Object> requestParams = new HashMap<String, Object>();
           requestParams.put("documentType", documentType);
           requestParams.put("linkedPredecessor", linkedPredecessor);
           requestParams.put("documentNo", documentNo);
           requestParams.put("companyId", companyid);
          
              
           
            if (documentType == Constants.Acc_Goods_Receipt_ModuleId) {
                buildHtml = linkInformationHandlerObj.getGRLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Purchase_Order_ModuleId) {
                buildHtml = linkInformationHandlerObj.getPOLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Vendor_Invoice_ModuleId) {
                buildHtml = linkInformationHandlerObj.getPILinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Purchase_Return_ModuleId) {
                buildHtml = linkInformationHandlerObj.getPRLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Vendor_Quotation_ModuleId) {
                buildHtml = linkInformationHandlerObj.getVQLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Debit_Note_ModuleId) {
                buildHtml = linkInformationHandlerObj.getDNLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Make_Payment_ModuleId) {
                buildHtml = linkInformationHandlerObj.getMPLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_Purchase_Requisition_ModuleId) {
                buildHtml = linkInformationHandlerObj.getRequisitionLinkingInfo(requestParams,jSONArray);
            } else if (documentType == Constants.Acc_RFQ_ModuleId) {
                buildHtml = linkInformationHandlerObj.getRFQLinkingInfo(requestParams,jSONArray);
            }
            
            
        } catch (Exception ex) {
            msg = "AccLinkDataController.getLinkedDocumentForPurchaseSide : " + ex.getMessage();
        } 
        return buildHtml;
    }
}
