/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.pos;

import com.krawler.common.dao.BaseDAO;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.util.Constants;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class AccPOSInterfaceDAOImpl  extends BaseDAO implements AccPOSInterfaceDAO{
    
    private MessageSource messageSource;

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    @Override
    public JSONObject saveCompanywiseCurrencyDenomination(JSONObject paramJobj) {
        boolean isSuccess = false;
        JSONObject queryresultJson = new JSONObject();

        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String denominations = paramJobj.optString("denominations", "[{}]");
            if (!StringUtil.isNullOrEmpty(denominations)) {
                JSONArray jArray = new JSONArray(denominations);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jObj = jArray.getJSONObject(i);
                    int currencynotes = jObj.optInt(Constants.currencynotes, 0);
                    String currencyid = jObj.optString(Constants.currencyKey, Constants.globalCurrencyKey);
                    String locationid = jObj.optString("storeid");//On pos side there are stores and on ERP there are locations 
                    String new_id = UUID.randomUUID().toString();
                    String addCashdetailsQuery = "insert into cashdenomination (id,currencydenomination,company,currency,locationid) values(?,?,?,?,?)";
                    int row = executeSQLUpdate(addCashdetailsQuery, new Object[]{new_id, currencynotes, companyid, currencyid, locationid});
                    isSuccess = true;
                }
                queryresultJson.put(Constants.RES_success, isSuccess);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return queryresultJson;
    }
    
@Override
    public JSONObject deleteCurrencyDenominations(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        int deletedRecords = 0;
        List params = new ArrayList();
        boolean isSuccess = false;
        StringBuilder conditionbuildString = new StringBuilder();
        try {
            String condition = "";
            
            String companyid = paramJobj.getString(Constants.companyKey);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.companyKey, null))) {
                conditionbuildString.append(" where company=?");
                params.add(paramJobj.optString(Constants.companyKey));
            }
            
            String denominations = paramJobj.optString("denominations", "[{}]");
            if (!StringUtil.isNullOrEmpty(denominations)) {
                JSONArray jArray = new JSONArray(denominations);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jObj = jArray.getJSONObject(i);

                    //Currency Notes
                    if (jObj.has(Constants.currencynotes) && jObj.get(Constants.currencynotes) != null) {
                        int cashdenomination = jObj.optInt(Constants.currencynotes, 0);
                        if (conditionbuildString.length() > 0) {
                            conditionbuildString.append(" and currencydenomination=?");
                        } else {
                            conditionbuildString.append(" where currencydenomination=?");
                        }
                        params.add(cashdenomination);
                    }

                    //Currency
                    if (jObj.has(Constants.currencyKey) && jObj.get(Constants.currencyKey) != null) {
                        String currencyid = jObj.getString(Constants.currencyKey);
                        if (conditionbuildString.length() > 0) {
                            conditionbuildString.append(" and currency=?");
                        } else {
                            conditionbuildString.append(" where currency=?");
                        }
                        params.add(currencyid);
                    }

                    //Stores   
                    if (jObj.has("storeid") && jObj.get("storeid") != null) {
                        String locationid = jObj.getString("storeid");
                        if (conditionbuildString.length() > 0) {
                            conditionbuildString.append(" and locationid=?");
                        } else {
                            conditionbuildString.append(" where locationid=?");
                        }
                        params.add(locationid);
                    }

                    if (jObj.has("id") && jObj.get("id") != null) {
                        if (conditionbuildString.length() > 0) {
                            conditionbuildString.append(" and id=?");
                        } else {
                            conditionbuildString.append(" where id=?");
                        }
                        params.add((String) jObj.get("id"));
                    }
                    condition=conditionbuildString.toString();

                    String query = "delete from cashdenomination " + condition;
                    deletedRecords = executeSQLUpdate(query, params.toArray());
                    isSuccess = true;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnJobj.put("deletedrecords", deletedRecords);
            returnJobj.put(Constants.RES_success, isSuccess);
        }
        return returnJobj;
    }
    
    @Override
    public List getCompanywiseCurrencyDenomination(JSONObject paramJobj) throws ServiceException, JSONException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        StringBuilder conditionbuildString = new StringBuilder();
        String companyid = paramJobj.optString(Constants.companyKey);
        String currencyid = paramJobj.optString(Constants.currencyKey);
        
        if (!StringUtil.isNullOrEmpty(companyid)) {
            conditionbuildString.append(" where company=?");
            params.add(companyid);
        }

        if (!StringUtil.isNullOrEmpty(currencyid)) {
            if (conditionbuildString.length() > 0) {
                conditionbuildString.append(" and currency=?");
            } else {
                conditionbuildString.append(" where currency=?");
            }
            params.add(currencyid);
        }
        
        condition = conditionbuildString.toString();
        try {
            String cdQuery = "select id,currencydenomination,currency from cashdenomination " + condition;
            returnList = executeSQLQuery(cdQuery, params.toArray());

        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnList;
    }
    
    @Override
    public KwlReturnObject getRegisterDetails(JSONObject paramJobj) throws ServiceException, JSONException {
        ArrayList params = new ArrayList();
        List list = null;
        String condition = "";
        int count = 0;
        int start = 0;
        int limit = 0;
        StringBuilder conditionbuildString = new StringBuilder();
        String companyid = paramJobj.optString(Constants.companyKey);
//        String transactionDate = paramJobj.optString("transactiondate");
        String locationid = paramJobj.optString("storeid");
        params.add(companyid);

        if (paramJobj.has("transactiondate") && paramJobj.get("transactiondate") != null) {
            conditionbuildString.append(" and transactionDate =?");
            params.add(paramJobj.get("transactiondate"));
        }

        if (!StringUtil.isNullOrEmpty(locationid)) {
            conditionbuildString.append(" and locationid=?");
            params.add(locationid);
        }

        if (paramJobj.has("isopen") && paramJobj.get("isopen") != null) {
            conditionbuildString.append(" and isopen=?");
            params.add(paramJobj.get("isopen"));
        }

        if (paramJobj.has("startdate") && paramJobj.get("startdate") != null) {
            conditionbuildString.append(" and transactionDate >=? ");
            params.add(paramJobj.get("startdate"));
        }

        if (paramJobj.has("enddate") && paramJobj.get("enddate") != null) {
            conditionbuildString.append(" and transactionDate <=? ");
//            Condition = " and inv.journalEntry.entryDate >= ? and inv.journalEntry.entryDate <= ?";
            params.add(paramJobj.get("enddate"));
        }

        if (paramJobj.has("start") && paramJobj.get("start") != null) {
            start = (Integer) paramJobj.get("start");
        }
        if (paramJobj.has("limit") && paramJobj.get("limit") != null) {
            limit = (Integer) paramJobj.get("limit");
        }

//        if (paramJobj.has("userid") && paramJobj.get("userid")!=null) {
//            User userid = (User) get(User.class, (String) paramJobj.get("userid"));
//            conditionbuildString.append(" and userid.userID=?");
//            params.add(paramJobj.get("userid"));
//        }
        condition = conditionbuildString.toString();

        try {
            // Getting register details
            String getRegisterQuery = "from CompanyRegister where company.companyID=? " + condition;
            list = executeQuery(getRegisterQuery, params.toArray());
            count = list.size();
            if (paramJobj.has("start") && paramJobj.get("start") != null && paramJobj.has("limit") && paramJobj.get("limit") != null) {
                list = executeQueryPaging(getRegisterQuery, params.toArray(), new Integer[]{start, limit});
            }
        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, count);
    }

 @Override  
    public JSONObject openandCloseRegister(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject returnjObj = new JSONObject();
        boolean isSuccess = false;
        StringBuilder msgBuildString = new StringBuilder();

        try {
            CompanyRegister companyRegObj = new CompanyRegister();
            // Getting register details
            String companyid = paramJobj.optString(Constants.companyKey);
            int isopen = 0;
            if (paramJobj.has("isopen") && paramJobj.get("isopen") != null) {
                isopen = (Integer) paramJobj.get("isopen");
            }
            
            KwlReturnObject returnObj = getRegisterDetails(paramJobj);
            if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().get(0) != null) {
                if (isopen == 0) {
                    JSONObject response = StringUtil.getErrorResponse("acc.common.erp40", paramJobj, "Register is already opened.", messageSource);
                    msgBuildString.append(messageSource.getMessage("acc.common.erp40", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
                } else {
                    JSONObject response = StringUtil.getErrorResponse("acc.common.erp43", paramJobj, "Register is already closed.", messageSource);
                    msgBuildString.append(messageSource.getMessage("acc.common.erp43", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
                }
            } else {
                if (paramJobj.has("isopen") && paramJobj.get("isopen") != null) {
                    companyRegObj.setIsopen(paramJobj.optInt("isopen", 0));
                }
                Company company = (Company) get(Company.class, companyid);
                companyRegObj.setCompany(company);

                if (paramJobj.has("transactiondate") && paramJobj.get("transactiondate") != null) {
                    companyRegObj.setTransactionDate((Date) paramJobj.get("transactiondate"));
                }
                if (paramJobj.has("transactiondateinlong") && paramJobj.get("transactiondateinlong") != null) {
                    companyRegObj.setTransactionDateinLong(paramJobj.optLong("transactiondateinlong"));
                }
                 if (paramJobj.has("userid") && paramJobj.get("userid")!=null) {
                    User userid = (User) get(User.class, (String) paramJobj.get("userid"));
                    companyRegObj.setUserid(userid);
                }

                if (paramJobj.has("storeid") && paramJobj.get("storeid") != null) {
                    String locationid = paramJobj.optString("storeid");
                    companyRegObj.setLocationid(locationid);
                }
                if (paramJobj.has("openingamount") && paramJobj.get("openingamount") != null) {
                    companyRegObj.setOpeningamount(paramJobj.optDouble("openingamount", 0.0));
                }
                if (paramJobj.has("previousclosedbalance") && paramJobj.get("previousclosedbalance") != null) {
                    companyRegObj.setPreviousclosedbalance(paramJobj.optDouble("previousclosedbalance", 0.0));
                }
                if (paramJobj.has("finalopeningamount") && paramJobj.get("finalopeningamount") != null) {
                    companyRegObj.setFinalopeningamount(paramJobj.optDouble("finalopeningamount", 0.0));
                }
                if (paramJobj.has("cashdetails") && paramJobj.get("cashdetails") != null) {
                    companyRegObj.setCurrencydenominationsjson(paramJobj.optString("cashdetails", ""));
                }

                if (paramJobj.has("addedamount") && paramJobj.get("addedamount") != null) {
                    companyRegObj.setAddedamount(paramJobj.optDouble("addedamount", 0.0));
                }
                if (paramJobj.has("byCash") && paramJobj.get("byCash") != null) {
                    companyRegObj.setByCash(paramJobj.optDouble("byCash", 0.0));
                }
                if (paramJobj.has("byCheque") && paramJobj.get("byCheque") != null) {
                    companyRegObj.setByCheque(paramJobj.optDouble("byCheque", 0.0));
                }
                if (paramJobj.has("byCard") && paramJobj.get("byCard") != null) {
                    companyRegObj.setByCard(paramJobj.optDouble("byCard", 0.0));
                }
                if (paramJobj.has("byGiftCard") && paramJobj.get("byGiftCard") != null) {
                    companyRegObj.setByGiftCard(paramJobj.optDouble("byGiftCard", 0.0));
                }
                if (paramJobj.has("variance") && paramJobj.get("variance") != null) {
                    companyRegObj.setVariance(paramJobj.optDouble("variance", 0.0));
                }
                if (paramJobj.has("closingamount") && paramJobj.get("closingamount") != null) {
                    companyRegObj.setClosingamount(paramJobj.optDouble("closingamount", 0.0));
                }
                if (paramJobj.has("depositedamount") && paramJobj.get("depositedamount") != null) {
                    companyRegObj.setDepositedamount(paramJobj.optDouble("depositedamount", 0.0));
                }
                if (paramJobj.has("finalamount") && paramJobj.get("finalamount") != null) {
                    companyRegObj.setFinalamount(paramJobj.optDouble("finalamount", 0.0));
                }
                if (paramJobj.has("cashoutamount") && paramJobj.get("cashoutamount") != null) {
                    companyRegObj.setCashoutamount(paramJobj.optDouble("cashoutamount", 0.0));
                }
                
                if (paramJobj.has("id") && paramJobj.get("id") != null) {
                    if (paramJobj.has("id") && paramJobj.get("id") != null) {
                        companyRegObj.setID(paramJobj.getString("id"));
                    }
                    saveOrUpdate(companyRegObj);
                } else {
                    save(companyRegObj);
                }
                          
                List resultlist = new ArrayList();
                resultlist.add(companyRegObj);
                msgBuildString.append(messageSource.getMessage("acc.common.erp41", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                isSuccess = true;
            }

        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnjObj.put(Constants.RES_success, isSuccess);
            returnjObj.put(Constants.RES_MESSAGE, msgBuildString.toString());
        }
        return returnjObj;
    }
 
    @Override
    public JSONObject savePOSCompanyWizardSettings(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject returnjObj = new JSONObject();
        boolean isSuccess = false;
        boolean isEdit = false;
        StringBuilder msgBuildString = new StringBuilder();
        int executeRow=0;
        String new_id = UUID.randomUUID().toString();
        try {
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.Acc_id, null))) {
                new_id = paramJobj.optString(Constants.Acc_id);
                isEdit=true;
            }
            String walkinCustomer = paramJobj.optString(POSERPMapping.WalkinCustomer);
            String cashOutAccountId = paramJobj.optString("cashOutAccountId");
            String paymentMethodId = paramJobj.optString("paymentMethodId");
            String companyid = paramJobj.optString(Constants.companyKey);
            String useridKey = paramJobj.optString(Constants.useridKey);
            String invoiceSeq = paramJobj.optString(POSERPMapping.INVOICE_SEQUENCEFORMAT);
            String doSeq = paramJobj.optString(POSERPMapping.DO_SEQUENCEFORMAT);
            String srSeq = paramJobj.optString(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT);
            String cnSeq = paramJobj.optString(POSERPMapping.CN_SEQUENCEFORMAT);
            String mpSeq = paramJobj.optString(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT);
            String rpSeq = paramJobj.optString(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT);
            String soSeq = paramJobj.optString(POSERPMapping.SALESORDER_SEQUENCEFORMAT);
            String depositAccountId = paramJobj.optString(POSERPMapping.DEPOSIT_ACCOUNT_ID);
            boolean allowcloseregistermultipletimesFlag = Boolean.parseBoolean(paramJobj.optString("allowcloseregistermultipletimesFlag"));
            long createdon = paramJobj.optLong(POSERPMapping.CREATED_ON);
            long updatedon = paramJobj.optLong(POSERPMapping.UPDATED_ON);
            char allowFlag = 'F';
            if (allowcloseregistermultipletimesFlag) {
                allowFlag = 'T';
            }
            String retailStoreId = paramJobj.optString("storeid");
            String addCashdetailsQuery = "insert into erp_pos_mapping (id,walkincustomer,cashoutaccount,paymentmethod,iscloseregistermultipletimes,storeid,invoicesequenceformat,dosequenceformat,srsequenceformat,cnsequenceformat,makepaymentsequenceformat,receiveapaymentsequenceformat,userid,company,createdon,updatedon,salesordersequenceformat,depositaccount) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            if (isEdit) {
                addCashdetailsQuery = "update erp_pos_mapping set walkincustomer=?,cashoutaccount=?,paymentmethod=?,iscloseregistermultipletimes=?,storeid=?,invoicesequenceformat=?,dosequenceformat=?,srsequenceformat=?,cnsequenceformat=?,makepaymentsequenceformat=?,receiveapaymentsequenceformat=?,userid=?,company=?,updatedon=?,salesordersequenceformat=?,depositaccount=? where id=?";
                executeRow = executeSQLUpdate(addCashdetailsQuery, new Object[]{walkinCustomer, cashOutAccountId, paymentMethodId, allowFlag, retailStoreId,invoiceSeq,doSeq,srSeq,cnSeq,mpSeq,rpSeq,useridKey,companyid,updatedon,soSeq,depositAccountId,new_id});
            } else {
                executeRow = executeSQLUpdate(addCashdetailsQuery, new Object[]{new_id, walkinCustomer, cashOutAccountId, paymentMethodId, allowFlag, retailStoreId,invoiceSeq,doSeq,srSeq,cnSeq,mpSeq,rpSeq,useridKey, companyid,createdon,updatedon,soSeq,depositAccountId});
            }
            if (executeRow > 0) {
                msgBuildString.append(messageSource.getMessage("acc.rem.pos.storeaftersavemsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                isSuccess = true;
            }
            
        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnjObj.put(Constants.RES_success, isSuccess);
            returnjObj.put(Constants.isEdit, isEdit);
            returnjObj.put(Constants.RES_MESSAGE, msgBuildString.toString());
            returnjObj.put(Constants.Acc_id, new_id);
        }
        return returnjObj;
    } 
    
    
    @Override
    public KwlReturnObject getPOSConfigDetails(JSONObject paramJobj) throws ServiceException, JSONException {
        ArrayList params = new ArrayList();
        List<Object[]> list = null;
        String id="";
        StringBuilder conditionbuildString = new StringBuilder();
        String companyid = paramJobj.optString(Constants.companyKey);
        String locationid = paramJobj.optString(POSERPMapping.StoreId);
        params.add(companyid);
        
        //Not checking walkincustomer,cashaccount,paymentmethod while saving
        if (!paramJobj.optBoolean(POSERPMapping.IS_SAVE)) {
            if (paramJobj.has(POSERPMapping.WalkinCustomer) && paramJobj.get(POSERPMapping.WalkinCustomer) != null) {
                conditionbuildString.append(" and walkincustomer =?");
                params.add(paramJobj.get(POSERPMapping.WalkinCustomer));
            }

            if (paramJobj.has(POSERPMapping.CASHOUT_ACCOUNT_ID) && paramJobj.get(POSERPMapping.CASHOUT_ACCOUNT_ID) != null) {
                conditionbuildString.append(" and cashoutaccount=?");
                params.add(paramJobj.get(POSERPMapping.CASHOUT_ACCOUNT_ID));
            }
            if (paramJobj.has(POSERPMapping.DEPOSIT_ACCOUNT_ID) && paramJobj.get(POSERPMapping.DEPOSIT_ACCOUNT_ID) != null) {
                conditionbuildString.append(" and depositaccount=?");
                params.add(paramJobj.get(POSERPMapping.DEPOSIT_ACCOUNT_ID));
            }

            if (paramJobj.has(POSERPMapping.PAYMENT_METHOD_ID) && paramJobj.get(POSERPMapping.PAYMENT_METHOD_ID) != null) {
                conditionbuildString.append(" and paymentmethod=?");
                params.add(paramJobj.get(POSERPMapping.PAYMENT_METHOD_ID));
            }
            
            if (paramJobj.has(POSERPMapping.CN_SEQUENCEFORMAT) && paramJobj.get(POSERPMapping.CN_SEQUENCEFORMAT) != null) {
                conditionbuildString.append(" and cnsequenceformat=?");
                params.add(paramJobj.get(POSERPMapping.CN_SEQUENCEFORMAT));
            }
            if (paramJobj.has(POSERPMapping.INVOICE_SEQUENCEFORMAT) && paramJobj.get(POSERPMapping.INVOICE_SEQUENCEFORMAT) != null) {
                conditionbuildString.append(" and invoicesequenceformat=?");
                params.add(paramJobj.get(POSERPMapping.INVOICE_SEQUENCEFORMAT));
            }
            if (paramJobj.has(POSERPMapping.DO_SEQUENCEFORMAT) && paramJobj.get(POSERPMapping.DO_SEQUENCEFORMAT) != null) {
                conditionbuildString.append(" and dosequenceformat=?");
                params.add(paramJobj.get(POSERPMapping.DO_SEQUENCEFORMAT));
            }
            if (paramJobj.has(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT) && paramJobj.get(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT) != null) {
                conditionbuildString.append(" and srsequenceformat=?");
                params.add(paramJobj.get(POSERPMapping.SALESRETRUN_SEQUENCEFORMAT));
            }
            if (paramJobj.has(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT) && paramJobj.get(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT) != null) {
                conditionbuildString.append(" and makepaymentsequenceformat=?");
                params.add(paramJobj.get(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT));
            }
            if (paramJobj.has(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT) && paramJobj.get(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT) != null) {
                conditionbuildString.append(" and receiveapaymentsequenceformat=?");
                params.add(paramJobj.get(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT));
            }
            if (paramJobj.has(POSERPMapping.SALESORDER_SEQUENCEFORMAT) && paramJobj.get(POSERPMapping.SALESORDER_SEQUENCEFORMAT) != null) {
                conditionbuildString.append(" and salesordersequenceformat=?");
                params.add(paramJobj.get(POSERPMapping.SALESORDER_SEQUENCEFORMAT));
            }
        }

        if (!StringUtil.isNullOrEmpty(locationid)) {
            conditionbuildString.append(" and storeid=?");
            params.add(locationid);
        }


//        if (paramJobj.has("userid") && paramJobj.get("userid") != null) {
//            User userid = (User) get(User.class, (String) paramJobj.get("userid"));
//            conditionbuildString.append(" and userid=?");
//            params.add((String) paramJobj.get("userid"));
//        }

        try {
            String detailQuery = "select id,walkincustomer,cashoutaccount,paymentmethod,storeid,iscloseregistermultipletimes,dosequenceformat,invoicesequenceformat,srsequenceformat,cnsequenceformat,makepaymentsequenceformat,receiveapaymentsequenceformat,salesordersequenceformat,depositaccount from erp_pos_mapping where company=? " + conditionbuildString.toString();
            list = executeSQLQuery(detailQuery, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
     @Override
    public JSONObject saveCashOutDetails(HashMap<String, Object> reqParams) throws ServiceException, JSONException {
        JSONObject returnjObj = new JSONObject();
        boolean isSuccess = false;
        CashOut csObj = new CashOut();

        try {
            if (reqParams.containsKey(Constants.companyKey) && reqParams.get(Constants.companyKey) != null) {
                Company company = (Company) get(Company.class, (String) reqParams.get(Constants.companyKey));
                csObj.setCompany(company);
            }

            if (reqParams.containsKey("transactiondate") && reqParams.get("transactiondate") != null) {
                csObj.setTransactionDate((Date) reqParams.get("transactiondate"));
            }
            if (reqParams.containsKey("transactiondateinlong") && reqParams.get("transactiondateinlong") != null) {
                csObj.setTransactionDateinLong((Long) reqParams.get("transactiondateinlong"));
            }

            if (reqParams.containsKey("amount") && reqParams.get("amount") != null) {
                csObj.setAmount((Double) reqParams.get("amount"));
            }

            if (reqParams.containsKey("reason") && reqParams.get("reason") != null) {
                MasterItem msObj = (MasterItem) get(MasterItem.class, (String) reqParams.get("reason"));
                csObj.setReason(msObj);
            }

            if (reqParams.containsKey("location") && reqParams.get("location") != null) {
                csObj.setStoreid((String) reqParams.get("location"));
            }
            
            if (reqParams.containsKey("isdeposit") && reqParams.get("isdeposit") != null) {
                csObj.setIsdeposit((Boolean) reqParams.get("isdeposit"));
            }

            if (reqParams.containsKey(Constants.useridKey) && reqParams.get(Constants.useridKey) != null) {
                User userObj = (User) get(User.class, (String) reqParams.get(Constants.useridKey));
                csObj.setUserid(userObj);
            }

            if (reqParams.containsKey("id") && reqParams.get("id") != null) {
                csObj.setID((String) reqParams.get("id"));
                saveOrUpdate(csObj);
            } else {
                save(csObj);
            }
            List resultlist = new ArrayList();
            resultlist.add(csObj);
            isSuccess = true;

        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnjObj.put(Constants.RES_success, isSuccess);
        }
        return returnjObj;
    } 
    
    @Override
    public JSONObject savePaymentMethodType(HashMap<String, Object> reqParams) throws ServiceException, JSONException {
        JSONObject returnjObj = new JSONObject();
        boolean isSuccess = false;
        PaymentDetailPos payDetailObj = new PaymentDetailPos();

        try {
            if (reqParams.containsKey(Constants.companyKey) && reqParams.get(Constants.companyKey) != null) {
                Company company = (Company) get(Company.class, (String) reqParams.get(Constants.companyKey));
                payDetailObj.setCompany(company);
            }

            if (reqParams.containsKey("transactiondateinlong") && reqParams.get("transactiondateinlong") != null) {
                payDetailObj.setTransactionDateinLong((Long) reqParams.get("transactiondateinlong"));
            }
            
            if (reqParams.containsKey("transactiondate") && reqParams.get("transactiondate") != null) {
                payDetailObj.setTransactionDate((Date) reqParams.get("transactiondate"));
            }

            if (reqParams.containsKey("amount") && reqParams.get("amount") != null) {
                payDetailObj.setAmount((Double) reqParams.get("amount"));
            }

            if (reqParams.containsKey("location") && reqParams.get("location") != null) {
                payDetailObj.setLocationid((String) reqParams.get("location"));
            }

            if (reqParams.containsKey(Constants.useridKey) && reqParams.get(Constants.useridKey) != null) {
                User userObj = (User) get(User.class, (String) reqParams.get(Constants.useridKey));
                payDetailObj.setUserid(userObj);
            }
            if (reqParams.containsKey(PaymentDetailPos.Payment_Method__Type) && reqParams.get(PaymentDetailPos.Payment_Method__Type) != null) {
                int paymentmethodtype = (Integer) reqParams.get(PaymentDetailPos.Payment_Method__Type);
                payDetailObj.setPaymenttype(paymentmethodtype);
            }
            if (reqParams.containsKey(PaymentDetailPos.Payment_Method__Name) && reqParams.get(PaymentDetailPos.Payment_Method__Name) != null) {
                String paymentMethodName = (String) reqParams.get(PaymentDetailPos.Payment_Method__Name);
                payDetailObj.setPaymentmethodname(paymentMethodName);
            }
            if (reqParams.containsKey(PaymentDetailPos.RECEIPT_ID) && reqParams.get(PaymentDetailPos.RECEIPT_ID) != null) {
                String receiptId = (String) reqParams.get(PaymentDetailPos.RECEIPT_ID);
                payDetailObj.setReceiptid(receiptId);
            }
            if (reqParams.containsKey(PaymentDetailPos.INVOICE_ID) && reqParams.get(PaymentDetailPos.INVOICE_ID) != null) {
                String invoiceId = (String) reqParams.get(PaymentDetailPos.INVOICE_ID);
                payDetailObj.setInvoiceid(invoiceId);
            }

            if (reqParams.containsKey("id") && reqParams.get("id") != null) {
                payDetailObj.setID((String) reqParams.get("id"));
                saveOrUpdate(payDetailObj);
            } else {
                save(payDetailObj);
            }
            List resultlist = new ArrayList();
            resultlist.add(payDetailObj);
            isSuccess = true;

        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnjObj.put(Constants.RES_success, isSuccess);
        }
        return returnjObj;
    }
     
    @Override  //when gro is not linked to po in parent subdomain then it fetches source grn address
    public JSONObject getInvoiceDetailsid(String invoiceid, String companyid, String productid) throws ServiceException {
        JSONObject returnJobj = new JSONObject();
        List params = new ArrayList();
        int totalCount = 0;
        String selectedBillIds = invoiceid;
        selectedBillIds = AccountingManager.getFilterInString(selectedBillIds);
        try {
            if (!StringUtil.isNullOrEmpty(productid) && !StringUtil.isNullOrEmpty(selectedBillIds)) {
                params.add(companyid);
                params.add(productid);
                String sqlQuery = "select invd.id,inven.product from invoicedetails invd inner join inventory inven on inven.id=invd.id where invd.company=? and  inven.product=? and invd.invoice in " + selectedBillIds;
                List<Object[]> list = executeSQLQuery(sqlQuery, params.toArray());

                if (!list.isEmpty() && list.size() > 0) {
                    totalCount = list.size();
                    returnJobj.put(Constants.RES_TOTALCOUNT, totalCount);
                    for (Object[] row : list) {
                        if (row.length > 0 && row[0] != null) {
                            returnJobj.put(Constants.Acc_id, (String) row[0]);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnJobj;
    }
    
   @Override
    public KwlReturnObject getPOSCashOutDetails(JSONObject paramJobj) throws ServiceException, JSONException {
        ArrayList params = new ArrayList();
        List<Object[]> list = null;
        StringBuilder conditionbuildString = new StringBuilder();
        int count = 0;
        int start = 0;
        int limit = 0;
        boolean isForReport=false;
        String companyid = paramJobj.optString(Constants.companyKey);
        String locationid = paramJobj.optString(POSERPMapping.StoreId);
        params.add(companyid);

       if (paramJobj.has("reason") && paramJobj.get("reason") != null) {
           conditionbuildString.append(" and reason=?");
           params.add(paramJobj.get("reason"));
       }
       if (paramJobj.has(Constants.isForReport) && paramJobj.get(Constants.isForReport) != null) {
           isForReport = paramJobj.optBoolean(Constants.isForReport);
       }
       
       if (paramJobj.has("transactiondate") && paramJobj.get("transactiondate") != null) {
           conditionbuildString.append(" and transactiondate =?");
           params.add(paramJobj.get("transactiondate"));
       }
       
        if (!StringUtil.isNullOrEmpty(locationid)) {
            conditionbuildString.append(" and storeid=?");
            params.add(locationid);
        }

       if (paramJobj.has("startdate") && paramJobj.get("startdate") != null) {
           conditionbuildString.append("and transactionDate >=? ");
           params.add(paramJobj.get("startdate"));
       }

       if (paramJobj.has("enddate") && paramJobj.get("enddate") != null) {
           conditionbuildString.append("and transactionDate <=? ");
//            Condition = " and inv.journalEntry.entryDate >= ? and inv.journalEntry.entryDate <= ?";
           params.add(paramJobj.get("enddate"));
       }

       if (paramJobj.has("start") && paramJobj.get("start") != null) {
           start = (Integer) paramJobj.get("start");
       }
       if (paramJobj.has("limit") && paramJobj.get("limit") != null) {
           limit = (Integer) paramJobj.get("limit");
       }

       if (paramJobj.has("isdeposit") && paramJobj.get("isdeposit") != null) {
           boolean isdepositflag=(Boolean) paramJobj.get("isdeposit");
           if(isdepositflag){
            conditionbuildString.append(" and isdeposit='T'");
           }else{
            conditionbuildString.append(" and isdeposit='F'");
           }
       }

        try {
            String detailQuery = "select sum(amount),max(transactiondateinlong) from cashouttransaction where company=? " + conditionbuildString.toString();
            list = executeSQLQuery(detailQuery, params.toArray());
            count=list.size();
            //for report
            if (paramJobj.has("start") && paramJobj.get("start") != null && paramJobj.has("limit") && paramJobj.get("limit") != null && isForReport) {
                detailQuery = "from CashOut where company.companyID=? " + conditionbuildString.toString();
                list = executeQueryPaging(detailQuery, params.toArray(), new Integer[]{start, limit});
            }
            
        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, count);
    } 
   
    @Override
    public KwlReturnObject getPreviousClosedBalanceDetails(JSONObject paramJobj) throws ServiceException, JSONException {
        ArrayList params = new ArrayList();
        List<Object[]> list = null;
        StringBuilder conditionbuildString = new StringBuilder();
        String companyid = paramJobj.optString(Constants.companyKey);
        String locationid = paramJobj.optString(POSERPMapping.StoreId);
//        if (paramJobj.has("transactiondateinlong") && paramJobj.get("transactiondateinlong") != null) {
//            conditionbuildString.append(" and transactiondateinlong <= ?");
//            params.add(paramJobj.get("transactiondateinlong"));
//        }

//        if (!StringUtil.isNullOrEmpty(locationid)) {
//            conditionbuildString.append(" and locationid=?");
//            params.add(locationid);
//        }

        params.add(companyid);
        params.add(companyid);
        params.add(locationid);
        params.add(paramJobj.get("transactiondateinlong"));
        params.add(locationid);
        
//        if (paramJobj.has("userid") && paramJobj.get("userid") != null) {
//            conditionbuildString.append(" and userid=?");
//            params.add((String) paramJobj.get("userid"));
//        }
        
        try {
            String detailQuery = "select max(transactiondateinlong),max(openingamount) from companyregister where company=? " + conditionbuildString.toString()+" and isopen=1";
            String detailQuery2 = "select max(cr.transactiondateinlong) from companyregister cr where cr.company=? and cr.locationid=? and cr.transactiondateinlong <= ? and cr.isopen=1";
            
            String detailQuery1 = "select companyreg.closingamount,companyreg.variance from companyregister companyreg where companyreg.company=? and companyreg.transactiondateinlong =("+detailQuery2+") and  companyreg.locationid=? and companyreg.isopen=1";
            list = executeSQLQuery(detailQuery1, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
       
    @Override
    public KwlReturnObject getPOSPaymentMethodDetails(JSONObject paramJobj) throws ServiceException, JSONException {
        ArrayList params = new ArrayList();
        List<Object[]> list = null;
        StringBuilder conditionbuildString = new StringBuilder();
        String companyid = paramJobj.optString(Constants.companyKey);
        String locationid = paramJobj.optString(POSERPMapping.StoreId);
        params.add(companyid);

            if (paramJobj.has(PaymentDetailPos.INVOICE_ID) && paramJobj.get(PaymentDetailPos.INVOICE_ID) != null) {
                conditionbuildString.append(" and invoiceid=?");
                params.add(paramJobj.get(PaymentDetailPos.INVOICE_ID));
            }
            if (paramJobj.has(PaymentDetailPos.RECEIPT_ID) && paramJobj.get(PaymentDetailPos.RECEIPT_ID) != null) {
                conditionbuildString.append(" and receiptid=?");
                params.add(paramJobj.get(PaymentDetailPos.RECEIPT_ID));
            }
                
            if (paramJobj.has("transactiondate") && paramJobj.get("transactiondate") != null) {
                conditionbuildString.append(" and transactiondate =?");
                params.add(paramJobj.get("transactiondate"));
            }

            if (paramJobj.has("transactiondateinlong") && paramJobj.get("transactiondateinlong") != null) {
                conditionbuildString.append(" and transactiondateinlong =?");
                params.add(paramJobj.get("transactiondateinlong"));
            }
            
                if (paramJobj.has(PaymentDetailPos.Payment_Method__Type) && paramJobj.get(PaymentDetailPos.Payment_Method__Type) != null) {
                    conditionbuildString.append(" and paymenttype=?");
                    params.add(paramJobj.get(PaymentDetailPos.Payment_Method__Type));
                }

                if (!StringUtil.isNullOrEmpty(locationid)) {
                    conditionbuildString.append(" and locationid=?");
                    params.add(locationid);
                }

//                if (paramJobj.has("userid") && paramJobj.get("userid") != null) {
//                    conditionbuildString.append(" and userid=?");
//                    params.add((String) paramJobj.get("userid"));
//                }

        try {
            String detailQuery = "select id,locationid,invoiceid,receiptid,paymentmethodname,transactiondate,paymenttype,userid,amount,transactiondateinlong from pospaydetail where company=? " + conditionbuildString.toString();
            //get only total amount flag
            if (paramJobj.optBoolean(PaymentDetailPos.IS_SUMMATION_FLAG)) {
                detailQuery = "select sum(amount),max(transactiondateinlong) from pospaydetail where company=? " + conditionbuildString.toString();
            }
            list = executeSQLQuery(detailQuery, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
   //While deleting entry from receive payment deleting entry from table 
    public JSONObject deletePaymentMethodEntry(JSONObject requestParamsJson) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        int deletedRecords = 0;
        List params = new ArrayList();
        boolean isSuccess = false;
        StringBuilder conditionbuildString = new StringBuilder();
        try {
            String condition = "";

            String companyid = requestParamsJson.getString(Constants.companyKey);
            if (!StringUtil.isNullOrEmpty(requestParamsJson.optString(Constants.companyKey, null))) {
                conditionbuildString.append(" where company=?");
                params.add(companyid);
            }
            if (!StringUtil.isNullOrEmpty(requestParamsJson.optString(Constants.useridKey, null))) {
                if (conditionbuildString.length() > 0) {
                    conditionbuildString.append(" and userid=?");
                } else {
                    conditionbuildString.append(" where userid=?");
                }
                params.add(requestParamsJson.optString(Constants.useridKey));
            }

            //Storeid check
            if (requestParamsJson.has(POSERPMapping.StoreId) && requestParamsJson.get(POSERPMapping.StoreId) != null) {
                String storeid = requestParamsJson.getString(POSERPMapping.StoreId);
                if (conditionbuildString.length() > 0) {
                    conditionbuildString.append(" and locationid=?");
                } else {
                    conditionbuildString.append(" where locationid=?");
                }
                params.add(storeid);
            }

            //Invoiceid
            if (requestParamsJson.has("invoiceid") && requestParamsJson.get("invoiceid") != null) {
                String invoiceid = requestParamsJson.getString("invoiceid");
                if (conditionbuildString.length() > 0) {
                    conditionbuildString.append(" and invoiceid=?");
                } else {
                    conditionbuildString.append(" where invoiceid=?");
                }
                params.add(invoiceid);
            }

            if (requestParamsJson.has("receiptid") && requestParamsJson.get("receiptid") != null) {
                String selectedBillIds = (String) requestParamsJson.get("receiptid");
                selectedBillIds = AccountingManager.getFilterInString(selectedBillIds);
                if (conditionbuildString.length() > 0) {
                    conditionbuildString.append(" and receiptid in " + selectedBillIds);
                } else {
                    conditionbuildString.append(" where receiptid in " + selectedBillIds);
                }
            }
            condition = conditionbuildString.toString();

            String query = "delete from pospaydetail " + condition;
            deletedRecords = executeSQLUpdate(query, params.toArray());
            isSuccess = true;

        } catch (Exception ex) {
            Logger.getLogger(AccPOSInterfaceDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnJobj.put("deletedrecords", deletedRecords);
            returnJobj.put(Constants.RES_success, isSuccess);
        }
        return returnJobj;
    } 
}
