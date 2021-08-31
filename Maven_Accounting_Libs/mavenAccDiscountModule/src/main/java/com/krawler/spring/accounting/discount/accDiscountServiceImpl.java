/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.discount;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.DiscountMaster;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class accDiscountServiceImpl implements accDiscountService {

    private accDiscountDAO accDiscountDAOObj;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public auditTrailDAO getAuditTrailObj() {
        return auditTrailObj;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public accDiscountDAO getAccDiscountDAOObj() {
        return accDiscountDAOObj;
    }

    public void setAccDiscountDAOObj(accDiscountDAO accDiscountDAOObj) {
        this.accDiscountDAOObj = accDiscountDAOObj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getDiscountMaster(Map requestParam) throws ServiceException {
        JSONObject returnJobj = new JSONObject();
        try {
            KwlReturnObject result = accDiscountDAOObj.getDiscountMaster(requestParam);
            List<DiscountMaster> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr = getDiscountMasterJson(list);
            returnJobj.put(Constants.RES_data, DataJArr);
            returnJobj.put(Constants.RES_count, count);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDiscountServiceImpl.getDiscountMaster:" + ex.getMessage(), ex);
        }
        return returnJobj;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getDiscountsAndTerms(Map requestParam) throws ServiceException {
        JSONObject returnJobj = new JSONObject();
        try {
            KwlReturnObject result = accDiscountDAOObj.getDiscountsAndTermsMasters(requestParam);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr = getDiscountsAndTermsMasterJson(list);
            returnJobj.put(Constants.RES_data, DataJArr);
            returnJobj.put(Constants.RES_count, count);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDiscountServiceImpl.getDiscountsAndTerms:" + ex.getMessage(), ex);
        }
        return returnJobj;
    }

    public JSONArray getDiscountMasterJson(List<DiscountMaster> list) throws ServiceException {
        JSONArray discountMasterJarr = new JSONArray();

        try {
            if (!list.isEmpty()) {
                for (DiscountMaster discountMaster : list) {
                    JSONObject jObj = new JSONObject();
                    jObj.put("discountid", discountMaster.getId());
                    jObj.put("discountname", discountMaster.getName());
                    jObj.put("discountdescription", discountMaster.getDescription());
                    jObj.put("discountvalue", discountMaster.getValue());
                    jObj.put("discountaccount", discountMaster.getAccount() != null ? discountMaster.getAccount() : " ");
                    jObj.put("discounttype", discountMaster.isDiscounttype() ? Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE : Constants.DISCOUNT_MASTER_TYPE_FLAT);
                    discountMasterJarr.put(jObj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDiscountServiceImpl.getDiscountMasterJson:" + ex.getMessage(), ex);
        }
        return discountMasterJarr;
    }
    
    public JSONArray getDiscountsAndTermsMasterJson(List list) throws ServiceException {
        JSONArray discountMasterJarr = new JSONArray();
        try {
            if (!list.isEmpty()) {
                Iterator masterItr = list.iterator();
                while (masterItr.hasNext()) {
                    JSONObject jObj = new JSONObject();
                    Object[] termsAndDiscounts = (Object[]) masterItr.next();
                    jObj.put("termid", termsAndDiscounts[0]);
                    jObj.put("discountid", termsAndDiscounts[1]);
                    jObj.put("applicabledays", termsAndDiscounts[2]);
                    jObj.put("discountname", termsAndDiscounts[3]);
                    jObj.put("discountvalue", termsAndDiscounts[4]);
                    jObj.put("discounttype", (!StringUtil.isNullObject(termsAndDiscounts[5]) && termsAndDiscounts[5].toString().equalsIgnoreCase("T")) ? Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE : Constants.DISCOUNT_MASTER_TYPE_FLAT);
                    jObj.put("discountaccount", termsAndDiscounts[6]);
                    discountMasterJarr.put(jObj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDiscountServiceImpl.getDiscountsAndTermsMasterJson:" + ex.getMessage(), ex);
        }
        return discountMasterJarr;
    }

    /**
     * @desc :
     * @param requestParam
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class})
    public JSONObject saveDiscountMaster(Map requestParam) throws ServiceException, JSONException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        JSONArray jDelArr = new JSONArray();

        if (requestParam.containsKey("data") && requestParam.get("data") != null) {
            jArr = (JSONArray) requestParam.get("data");
        }
        if (requestParam.containsKey("deleteddata") && requestParam.get("deleteddata") != null) {
            jDelArr = (JSONArray) requestParam.get("deleteddata");
        }
        String userName = "";
        if (requestParam.containsKey(Constants.username) && requestParam.get(Constants.username) != null) {
            userName = requestParam.get(Constants.username).toString();
        }
        String userId = "";
        if (requestParam.containsKey("userid") && requestParam.get("userid") != null) {
            userId = requestParam.get("userid").toString();
        }
        Locale locale = (Locale) requestParam.get("locale");
        KwlReturnObject result = null;
        String companyid = !StringUtil.isNullObject(requestParam.get(Constants.companyid)) ? requestParam.get(Constants.companyid).toString() : "";
        try {
            /*
             * variables to store the names of used and duplicate Discount
             */
            String linkedDiscountMaster = "", duplicateDiscountMaster = "";

            for (int i = 0; i < jDelArr.length(); i++) {
                String deleteid = "";
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("discountid"))) {
                    try {
                        deleteid = jobj.getString("discountid");
                        String deletename = StringUtil.DecodeText(jobj.getString("discountname"));
                        Map<String, Object> deleteParams = new HashMap<>();
                        deleteParams.put("discountid", deleteid);
                        deleteParams.put("discountname", deletename);
                        deleteParams.put(Constants.companyid, companyid);
                        result = accDiscountDAOObj.deleteDiscountMaster(deleteParams);
                        if (result.isSuccessFlag()) {
                            /*
                             * making an entry of deleted Discount Master in
                             * auditTrailObj
                             */
                            auditTrailObj.insertAuditLog(AuditAction.DISCOUNT_MASTER_DELETED, "User " + userName + " has deleted Discount Master " + deletename, requestParam, deleteid);
                        } else {
                            /*
                             * check to append comma in between names of linked Discount Master
                             */
                            if (i > 0 && i != jDelArr.length()) {
                                linkedDiscountMaster += " , ";
                            }
                            /*
                             * storing the name of Discount Master which is being
                             * used in other transactions
                             */
                            linkedDiscountMaster += deletename;
                        }
                    } catch (ServiceException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.discountmaster.excp1", null, locale));   //"The Discount Master is or had been used in transaction(s). So, it cannot be deleted.");
                    }
                }
            }

            HashMap<String, Object> dmMap;
            int cntDuplicate = 0;
            KwlReturnObject saveResult = null;
            KwlReturnObject linkResult = null;
            KwlReturnObject unqResult;
            String discountMasterId = "";
            String discountMasterName = "";
            String discountDescription = "";
            String accountId = "";
            String discounttype = "";
            double value = 0.0;
            int modifiedCnt = 0;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                discountMasterId = StringUtil.DecodeText(jobj.optString("discountid"));
                discountMasterName = StringUtil.DecodeText(jobj.optString("discountname"));
                discountDescription = StringUtil.DecodeText(jobj.optString("discountdescription"));
                accountId = jobj.optString("discountaccount");
                discounttype = jobj.optString("discounttype");
                value = jobj.optDouble("discountvalue");
                boolean modified = jobj.optBoolean("modified", false);

                dmMap = new HashMap<String, Object>();
                dmMap.put("id", discountMasterId);
                dmMap.put("name", discountMasterName);
                dmMap.put("description", discountDescription);
                dmMap.put("company", companyid);
                dmMap.put("account", accountId);
                dmMap.put("discounttype", discounttype);
                dmMap.put("value", value);

                unqResult = accDiscountDAOObj.checkUniqueDiscountMaster(dmMap);
                if (unqResult.getRecordTotalCount() > 0) {
                    /*
                     * check to append comma in between names of duplicate Discount Master
                     */
                    if (cntDuplicate > 0) {
                        duplicateDiscountMaster += " , ";
                    }
                    /*
                     * storing the name of Discount Master which is being added
                     * duplicately
                     */
                    duplicateDiscountMaster += discountMasterName;
                    cntDuplicate += 1;
                } else {
                    if (modified) {
                        linkResult = accDiscountDAOObj.getLinkedDiscountMasters(dmMap);
                        if (linkResult != null && linkResult.getEntityList().size() > 0) {
                            /*
                             * check to append comma in between names of linked Discount Master
                             */
                            if (modifiedCnt > 0 && modifiedCnt != jArr.length()) {
                                linkedDiscountMaster += " , ";
                            }
                            modifiedCnt++;
                            linkedDiscountMaster += discountMasterName;
                        } else {                      //to add new record as new records contians modified flag as true
                            saveResult = accDiscountDAOObj.saveDiscountMaster(dmMap);
                            String action = "updated";
                            if (jobj.optBoolean("modified", false)) {
                                action = "added";
                            }
                            auditTrailObj.insertAuditLog(AuditAction.DISCOUNT_MASTER_UPDATED, "User " + userName + " has " + action + " Discount Master " + discountMasterName, requestParam, discountMasterId);
                        }
                    } else {
                        saveResult = accDiscountDAOObj.saveDiscountMaster(dmMap);
                        String action = "updated";
                        if (jobj.optBoolean("modified", false)) {
                            action = "added";
                        }
                        auditTrailObj.insertAuditLog(AuditAction.DISCOUNT_MASTER_UPDATED, "User " + userName + " has " + action + " Discount Master " + discountMasterName, requestParam, discountMasterId);
                    }
                }
                /*
                 * binding the success flag,Discount Master names in the JSONObject
                 */
                returnJobj.put("success", true);
                returnJobj.put("isUsedDiscountMaster", linkedDiscountMaster);
                returnJobj.put("isDuplicateDiscountMaster", duplicateDiscountMaster);
                returnJobj.put("msg", saveResult != null ? saveResult.getMsg() : "");
            }
        } catch (AccountingException aex) {
            returnJobj.put("success", true);
            throw new AccountingException(aex.getMessage());
        } catch (Exception ex) {
            returnJobj.put("success", false);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return returnJobj;
    }

}
