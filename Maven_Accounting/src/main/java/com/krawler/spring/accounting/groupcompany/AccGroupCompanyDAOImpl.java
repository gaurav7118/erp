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
package com.krawler.spring.accounting.groupcompany;

import com.krawler.hql.accounting.GroupCompanyTransactionMapping;
import com.krawler.hql.accounting.GroupCompanyProcessMapping;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class AccGroupCompanyDAOImpl extends BaseDAO implements AccGroupCompanyDAO {

    private MessageSource messageSource;

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    @Override
    public KwlReturnObject fetchMultiCompanyDetails(Map<String, Object> requestParams) throws ServiceException {
        List param = new ArrayList();
        StringBuilder conditionbuildString = new StringBuilder();

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) != null) {
            conditionbuildString.append(" sourceCompany=? ");
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_MODULE) && requestParams.get(GroupCompanyProcessMapping.SOURCE_MODULE) != null) {
            if (conditionbuildString.length() > 0) {
                conditionbuildString.append(" and sourceModule=? ");
            } else {
                conditionbuildString.append(" sourceModule=?");
            }

            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_MODULE));
        }
        if (requestParams.containsKey(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) != null) {
            if (conditionbuildString.length() > 0) {
                conditionbuildString.append(" and destinationCompany=? ");
            } else {
                conditionbuildString.append(" destinationCompany=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_COMPANYID) && requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANYID) != null) {
            if (conditionbuildString.length() > 0) {
                conditionbuildString.append(" and sourceCompanyId=? ");
            } else {
                conditionbuildString.append(" sourceCompanyId=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANYID));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.DESTINATION_MODULE) && requestParams.get(GroupCompanyProcessMapping.DESTINATION_MODULE) != null) {
            if (conditionbuildString.length() > 0) {
                conditionbuildString.append(" and destinationModule=? ");
            } else {
                conditionbuildString.append(" destinationModule=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.DESTINATION_MODULE));
        }

        String query = " From GroupCompanyProcessMapping where " + conditionbuildString.toString();
        List list = executeQuery(query, param.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject fetchCustomerVendorDetails(Map<String, Object> requestParams) throws ServiceException {
        List param = new ArrayList();
        StringBuilder conditionString = new StringBuilder();

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) != null) {
            conditionString.append(" where sourceCompany=? ");
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN));
        }
        if (requestParams.containsKey(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and destinationCompany=? ");
            } else {
                conditionString.append(" where destinationCompany=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_MASTERCODE) && requestParams.get(GroupCompanyProcessMapping.SOURCE_MASTERCODE) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and sourceMasterCode=? ");
            } else {
                conditionString.append(" where sourceMasterCode=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_MASTERCODE));
        }

        String query = " From GroupCompanyCustomerVendorMapping " + conditionString.toString();
        List list = executeQuery(query, param.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject fetchTaxMappingDetails(Map<String, Object> requestParams) throws ServiceException {
        List param = new ArrayList();
        StringBuilder conditionString = new StringBuilder();
        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) != null) {
            conditionString.append(" sourceCompany=? ");
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and destinationCompany=? ");
            } else {
                conditionString.append("  destinationCompany=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_TAX_CODE) && requestParams.get(GroupCompanyProcessMapping.SOURCE_TAX_CODE) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and sourceTaxCode=? ");
            } else {
                conditionString.append("  sourceTaxCode=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_TAX_CODE));
        }

        String query = " From GroupCompanyTaxMapping where " + conditionString.toString();
        List list = executeQuery(query, param.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject fetchTermMappingDetails(Map<String, Object> requestParams) throws ServiceException {
        List param = new ArrayList();
        StringBuilder conditionString = new StringBuilder();

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) != null) {
            conditionString.append(" sourceCompany=? ");
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and destinationCompany=? ");
            } else {
                conditionString.append(" destinationCompany=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_TERM_NAME) && requestParams.get(GroupCompanyProcessMapping.SOURCE_TERM_NAME) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and sourceTermName=? ");
            } else {
                conditionString.append(" sourceTermName=?");
            }

            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_TERM_NAME));
        }

        String query = " From GroupCompanyTermMapping where " + conditionString.toString();
        List list = executeQuery(query, param.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public JSONObject saveDocumentTransactionsid(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject returnjObj = new JSONObject();
        boolean isSuccess = false;
        StringBuilder msgBuildString = new StringBuilder();

        try {
            GroupCompanyTransactionMapping multiGroupTranObj = new GroupCompanyTransactionMapping();
            // Getting register details
            if (paramJobj.has(GroupCompanyProcessMapping.SOURCE_MODULE) && paramJobj.get(GroupCompanyProcessMapping.SOURCE_MODULE) != null) {
                multiGroupTranObj.setSourceModule(paramJobj.optString(GroupCompanyProcessMapping.SOURCE_MODULE));
            }

            if (paramJobj.has(GroupCompanyProcessMapping.DESTINATION_MODULE) && paramJobj.get(GroupCompanyProcessMapping.DESTINATION_MODULE) != null) {
                multiGroupTranObj.setDestinationModule(paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE));
            }
            if (paramJobj.has(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID) && paramJobj.get(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID) != null) {
                multiGroupTranObj.setSourceTransactionid((String) paramJobj.get(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
            }

            if (paramJobj.has(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID) && paramJobj.get(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID) != null) {
                multiGroupTranObj.setDestinationTransactionid(paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID));
            }
            if (paramJobj.has("id") && paramJobj.get("id") != null) {
                if (paramJobj.has("id") && paramJobj.get("id") != null) {
                    multiGroupTranObj.setID(paramJobj.getString("id"));
                }
                saveOrUpdate(multiGroupTranObj);
            } else {
                save(multiGroupTranObj);
            }

            List resultlist = new ArrayList();
            resultlist.add(multiGroupTranObj);
            msgBuildString.append(messageSource.getMessage("acc.common.erp41", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            isSuccess = true;

        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnjObj.put(Constants.RES_success, isSuccess);
            returnjObj.put(Constants.RES_MESSAGE, msgBuildString.toString());
        }
        return returnjObj;
    }

    @Override
    public KwlReturnObject fetchTransactionMappingDetails(Map<String, Object> requestParams) throws ServiceException {
        List param = new ArrayList();
        StringBuilder conditionString = new StringBuilder();

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_MODULE) && requestParams.get(GroupCompanyProcessMapping.SOURCE_MODULE) != null) {
            conditionString.append(" sourceModule=? ");
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_MODULE));
        }
        if (requestParams.containsKey(GroupCompanyProcessMapping.DESTINATION_MODULE) && requestParams.get(GroupCompanyProcessMapping.DESTINATION_MODULE) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and destinationModule=? ");
            } else {
                conditionString.append(" destinationModule=?");
            }

            param.add((String) requestParams.get(GroupCompanyProcessMapping.DESTINATION_MODULE));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID) && requestParams.get(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and sourceTransactionid=? ");
            } else {
                conditionString.append(" sourceTransactionid=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
        }

        String query = " From GroupCompanyTransactionMapping where " + conditionString.toString();
        List list = executeQuery(query, param.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public JSONObject deleteTransactionMappingRecord(Map<String, Object> requestParams) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        int deletedRecords = 0;
        List params = new ArrayList();
        boolean isSuccess = false;
        StringBuilder conditionString = new StringBuilder();
        try {
            if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_MODULE) && requestParams.get(GroupCompanyProcessMapping.SOURCE_MODULE) != null) {
                conditionString.append(" sourcemodule=? ");
                params.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_MODULE));
            }
            if (requestParams.containsKey(GroupCompanyProcessMapping.DESTINATION_MODULE) && requestParams.get(GroupCompanyProcessMapping.DESTINATION_MODULE) != null) {
                if (conditionString.length() > 0) {
                    conditionString.append(" and destinationmodule=? ");
                } else {
                    conditionString.append(" destinationmodule=?");
                }
                params.add((String) requestParams.get(GroupCompanyProcessMapping.DESTINATION_MODULE));
            }

            if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID) && requestParams.get(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID) != null) {
                if (conditionString.length() > 0) {
                    conditionString.append(" and sourcetransactionid=? ");
                } else {
                    conditionString.append(" sourcetransactionid=?");
                }
                params.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
            }

            if (requestParams.containsKey(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID) && requestParams.get(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID) != null) {
                if (conditionString.length() > 0) {
                    conditionString.append(" and destinationtransactionid=? ");
                } else {
                    conditionString.append(" destinationtransactionid=?");
                }
                params.add((String) requestParams.get(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID));
            }

            String query = "delete from groupcompany_transactionmapping where " + conditionString.toString();
            deletedRecords = executeSQLUpdate(query, params.toArray());
            isSuccess = true;

        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnJobj.put("deletedrecords", deletedRecords);
            returnJobj.put(Constants.RES_success, isSuccess);
        }
        return returnJobj;
    }

    @Override
    public KwlReturnObject updateSubdomain(Map<String, Object> requestParams) throws ServiceException {
        List param = new ArrayList();
        StringBuilder conditionString = new StringBuilder();
        StringBuilder updateString = new StringBuilder();

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN_UPDATE) && requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN_UPDATE) != null) {
            updateString.append(" set sourceCompany=? ");
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN_UPDATE));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) != null) {
            conditionString.append(" where sourceCompany=? ");
            param.add((String) requestParams.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN));
        }

        if (requestParams.containsKey(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) && requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and destinationCompany=? ");
            } else {
                conditionString.append(" where destinationCompany=?");
            }
            param.add((String) requestParams.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN));
        }

        String groupCompanyCusVendQuery = "update GroupCompanyCustomerVendorMapping " + updateString.toString() + " where " + conditionString.toString();
        String groupCompanyProcessQuery = "update GroupCompanyProcessMapping " + updateString.toString() + " where " + conditionString.toString();
        String groupCompanyTaxQuery = "update GroupCompanyTaxMapping " + updateString.toString() + " where " + conditionString.toString();
        String groupCompanTermVendQuery = "update GroupCompanyTermMapping " + updateString.toString() + " where " + conditionString.toString();
        int numRows = executeUpdate(groupCompanyCusVendQuery, param.toArray());
        numRows += executeUpdate(groupCompanyProcessQuery, param.toArray());
        numRows += executeUpdate(groupCompanyTaxQuery, param.toArray());
        numRows += executeUpdate(groupCompanTermVendQuery, param.toArray());
        return new KwlReturnObject(true, "Subdomains have been updated successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject fetchSubdomains(JSONObject paramJobj) throws ServiceException {
        List param = new ArrayList();
        List list = new ArrayList();
        try {
            StringBuilder conditionString = new StringBuilder();
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("cdomainValue", null))) {
                conditionString.append("where subdomain NOT IN (?)");
                param.add(paramJobj.optString("cdomainValue"));
                String parentcompanyid = getParentCompany(paramJobj);
                if (!StringUtil.isNullOrEmpty(parentcompanyid)) {
                    conditionString.append(" and parentCompany=?");
                    param.add(parentcompanyid);
                }
            }
            String query = "from GroupCompanySubdomainMapping " + conditionString.toString();
            list = executeQuery(query, param.toArray());
        } catch (JSONException ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
@Override
    public JSONObject deleteExistingRecordsofTable(JSONObject paramJobj, Map<String, Object> requestParams) throws ServiceException, JSONException {
        JSONObject returnObj = new JSONObject();
        StringBuilder conditionString = new StringBuilder();
        ArrayList params = new ArrayList();
        boolean isSuccess=true;
        List returnList = new ArrayList();
        int deletedRecords =0;
        String tablename = "";
        try{

        if (requestParams.containsKey("isTaxMapping") && requestParams.get("isTaxMapping") != null) {
            tablename = "groupcompany_taxmapping";
        }

        if (requestParams.containsKey("isVendCustMapping") && requestParams.get("isVendCustMapping") != null) {
            tablename = "groupcompany_customervendormapping";
        }

        if (requestParams.containsKey("isInvoiceTermsMapping") && requestParams.get("isInvoiceTermsMapping") != null) {
            tablename = "groupcompany_termmapping";
        }

        if (requestParams.containsKey("isModuleMapping") && requestParams.get("isModuleMapping") != null) {
            tablename = "groupcompany_processmapping";
        }

        if (paramJobj.has(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) && paramJobj.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and " + tablename + ".sourcecompany=? ");
            } else {
                conditionString.append(tablename + ".sourcecompany=? ");
            }
            params.add(paramJobj.get(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN));
        }

        if (paramJobj.has(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) && paramJobj.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) != null) {
            if (conditionString.length() > 0) {
                conditionString.append(" and "  + tablename + ".destinationcompany=? ");
            } else {
                conditionString.append(tablename + ".destinationcompany=? ");
            }
            params.add(paramJobj.get(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN));
        }

        String sqlquery = "select count(*) from " + tablename + " where " + conditionString.toString();
        returnList = executeSQLQuery(sqlquery, params.toArray());

            if (!returnList.isEmpty() && returnList.size() > 0) {
            String query = "delete from " + tablename + " where " + conditionString.toString();
            deletedRecords = executeSQLUpdate(query, params.toArray());
            
        }
    } catch (JSONException ex) {
        isSuccess = false;
        Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
        returnObj.put(Constants.RES_success, isSuccess);
        returnObj.put(Constants.RES_TOTALCOUNT, deletedRecords);
    }
    return returnObj;
    }

@Override    
public JSONObject insertRecordsInTable(String mappingDetails, Map<String, Object> requestParams,JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject returnObj = new JSONObject();
        boolean isSuccess = true;
        String sourcecompanyid=paramJobj.optString(GroupCompanyProcessMapping.SOURCE_COMPANYID);
        String sourcecompanysubdomain=paramJobj.optString(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN);
        String destinationcompanysubdomain=paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN);
        String destinationcompanyid=paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_COMPANYID);
        try {
            
            if (requestParams.containsKey("isModuleMapping") && requestParams.get("isModuleMapping") != null) {
                if (!StringUtil.isNullOrEmpty(mappingDetails)) {
                    JSONArray jArray = new JSONArray(mappingDetails);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jObj = jArray.getJSONObject(i);
                        String sourceModule = jObj.optString(GroupCompanyProcessMapping.SOURCE_MODULE);
                        String destinationModule = jObj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE);//On pos side there are stores and on ERP there are locations 
                        String new_id = UUID.randomUUID().toString();
                        String addCashdetailsQuery = "insert into groupcompany_processmapping (id,sourcecompany,sourcecompanyid,sourcemodule,destinationcompany,destinationcompanyid,destinationmodule) values(?,?,?,?,?,?,?)";
                        int row = executeSQLUpdate(addCashdetailsQuery, new Object[]{new_id, sourcecompanysubdomain, sourcecompanyid, sourceModule, destinationcompanysubdomain,destinationcompanyid,destinationModule});
                        isSuccess = true;
                    }
                    returnObj.put(Constants.RES_success, isSuccess);
                }
            }
              if (requestParams.containsKey("isVendCustMapping") && requestParams.get("isVendCustMapping") != null) {
                if (!StringUtil.isNullOrEmpty(mappingDetails)) {
                    JSONArray jArray = new JSONArray(mappingDetails);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jObj = jArray.getJSONObject(i);
                        String sourceMasterCode = jObj.optString(GroupCompanyProcessMapping.SOURCE_MASTERCODE);
                        String sourceMasterId = jObj.optString(GroupCompanyProcessMapping.SOURCE_MASTER_ID);
                        String destinationMasterCode = jObj.optString(GroupCompanyProcessMapping.DESTNATION_MASTERCODE);
                        String destinationMasterId = jObj.optString(GroupCompanyProcessMapping.DESTNATION_MASTER_ID);
                        String isSourceCustomer = jObj.optString(GroupCompanyProcessMapping.IS_SOURCE_CUSTOMER);
                        boolean isSourceFlag=true;
                        if (!StringUtil.isNullOrEmpty(isSourceCustomer)) {
                            isSourceFlag = Boolean.parseBoolean(isSourceCustomer);
                        }
                        String new_id = UUID.randomUUID().toString();
                        String addCashdetailsQuery = "insert into groupcompany_customervendormapping (id,sourcecompany,destinationcompany,sourcemastercode,sourcemasterid,destinationmastercode,destinationmasterid,issourcecustomer) values(?,?,?,?,?,?,?,?)";
                        int row = executeSQLUpdate(addCashdetailsQuery, new Object[]{new_id, sourcecompanysubdomain, destinationcompanysubdomain, sourceMasterCode, sourceMasterId,destinationMasterCode,destinationMasterId,isSourceFlag});
                        isSuccess = true;
                    }
                    returnObj.put(Constants.RES_success, isSuccess);
                }
            }
            if (requestParams.containsKey("isTaxMapping") && requestParams.get("isTaxMapping") != null) {
                if (!StringUtil.isNullOrEmpty(mappingDetails)) {
                    JSONArray jArray = new JSONArray(mappingDetails);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jObj = jArray.getJSONObject(i);
                        String sourceTaxCode = jObj.optString(GroupCompanyProcessMapping.SOURCE_TAX_CODE);
                        String destinationTaxCode = jObj.optString(GroupCompanyProcessMapping.DESTINATION_TAX_CODE);
                        String sourceTaxid = jObj.optString(GroupCompanyProcessMapping.SOURCE_TAX_ID);
                        String destinationTaxid = jObj.optString(GroupCompanyProcessMapping.DESTINATION_TAX_ID);
                        String new_id = UUID.randomUUID().toString();
                        String addCashdetailsQuery = "insert into groupcompany_taxmapping (id,sourcecompany,destinationcompany,sourcetaxcode,sourcetaxid,destinationtaxcode,destinationtaxid) values(?,?,?,?,?,?,?)";
                        int row = executeSQLUpdate(addCashdetailsQuery, new Object[]{new_id, sourcecompanysubdomain, destinationcompanysubdomain, sourceTaxCode, sourceTaxid,destinationTaxCode,destinationTaxid});
                        isSuccess = true;
                    }
                    returnObj.put(Constants.RES_success, isSuccess);
                }
            }

 
            if (requestParams.containsKey("isInvoiceTermsMapping") && requestParams.get("isInvoiceTermsMapping") != null) {
                if (!StringUtil.isNullOrEmpty(mappingDetails)) {
                    JSONArray jArray = new JSONArray(mappingDetails);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jObj = jArray.getJSONObject(i);
                        String sourceTermName = jObj.optString(GroupCompanyProcessMapping.SOURCE_TERM_NAME);
                        String sourceTermId = jObj.optString(GroupCompanyProcessMapping.SOURCE_TERM_ID);
                        String destinationTermName = jObj.optString(GroupCompanyProcessMapping.DESTINATION_TERM_NAME);
                        String destinationTermId = jObj.optString(GroupCompanyProcessMapping.DESTINATION_TERM_ID);
                        String new_id = UUID.randomUUID().toString();
                        String addCashdetailsQuery = "insert into groupcompany_termmapping (id,sourcecompany,destinationcompany,sourcetermname,sourcetermid,destinationtermname,destinationtermid) values(?,?,?,?,?,?,?)";
                        int row = executeSQLUpdate(addCashdetailsQuery, new Object[]{new_id, sourcecompanysubdomain, destinationcompanysubdomain, sourceTermName, sourceTermId,destinationTermName,destinationTermId});
                        isSuccess = true;
                    }
                    returnObj.put(Constants.RES_success, isSuccess);
                }
            }
            
        } catch (JSONException ex) {
            isSuccess = false;
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("Sales receipt number '" + entryNumber + "' already exists.", "erp12{" + entryNumber + "}", false);
            
        } finally {
            returnObj.put(Constants.RES_success, isSuccess);
        }
        return returnObj;
    }

    public String getParentCompany(JSONObject paramJobj) throws ServiceException, JSONException {
        ArrayList params = new ArrayList();
        List<String> returnList = new ArrayList();
        String parentcompany = "";
        try {
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("cdomainValue", null))) {
                params.add(paramJobj.optString("cdomainValue"));
                String sqlquery = "select parentcompany from groupcompany_companylist where subdomain=?";
                returnList = executeSQLQuery(sqlquery, params.toArray());
                if (returnList.size() > 0) {
                    for (String companyid : returnList) {
                        parentcompany = companyid;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parentcompany;
    }
    
@Override    
    public boolean isSubdomainpresent(JSONObject paramJobj) throws ServiceException {
        boolean ispresent = false;
        BigInteger subdomainrows = BigInteger.ZERO;
        List list =new ArrayList<>();
        ArrayList params = new ArrayList();
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("cdomainValue", null))) {
            params.add(paramJobj.optString("cdomainValue"));
            String query = "select count(*) from  groupcompany_companylist where subdomain=?";
            list = executeSQLQuery(query, params.toArray());
            if (!list.isEmpty() && !StringUtil.isNullObject(list)) {
                subdomainrows = (BigInteger) list.get(0);
            }
            if (subdomainrows.intValue() > 0) {
                ispresent = true;
            }
        }
        return ispresent;
    }

    @Override
    public String fetchDetailsid(JSONObject paramJobj, Map<String, Object> requestParams) throws ServiceException {
        List param = new ArrayList();
        StringBuilder conditionString = new StringBuilder();
        String query = null;
        String detailid = "";
        String sourcemoduleid = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_MODULE);
        String destinationmoduleid = paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE);
        try {
            if (sourcemoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Purchase_Order_ModuleId)) && destinationmoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                if (requestParams.containsKey(Constants.productid) && requestParams.get(Constants.productid) != null) {
                    param.add(requestParams.get(Constants.productid));
                    if (conditionString.length() > 0) {
                        conditionString.append(" and ");
                    } else {
                        conditionString.append(" where ");
                    }
                    conditionString.append(" pod.product=? ");
                }

                if (requestParams.containsKey("purchaseorder") && requestParams.get("purchaseorder") != null) {
                    param.add(requestParams.get("purchaseorder"));
                    if (conditionString.length() > 0) {
                        conditionString.append(" and ");
                    } else {
                        conditionString.append(" where ");
                    }
                    conditionString.append(" pod.purchaseorder=? ");
                }
                query = "select id from podetails pod " + conditionString.toString();
            }
            
             if (sourcemoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId)) && destinationmoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                if (requestParams.containsKey(Constants.productid) && requestParams.get(Constants.productid) != null) {
                    param.add(requestParams.get(Constants.productid));
                    if (conditionString.length() > 0) {
                        conditionString.append(" and ");
                    } else {
                        conditionString.append(" where ");
                    }
                    conditionString.append(" sod.product=? ");
                }

                if (requestParams.containsKey("salesorder") && requestParams.get("salesorder") != null) {
                    param.add(requestParams.get("salesorder"));
                    if (conditionString.length() > 0) {
                        conditionString.append(" and ");
                    } else {
                        conditionString.append(" where ");
                    }
                    conditionString.append(" sod.salesorder=? ");
                }
                query = "select id from sodetails pod " + conditionString.toString();
            } 
            
            
            if (sourcemoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId)) && destinationmoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                if (requestParams.containsKey(Constants.productid) && requestParams.get(Constants.productid) != null) {
                    param.add(requestParams.get(Constants.productid));
                    if (conditionString.length() > 0) {
                        conditionString.append(" and ");
                    } else {
                        conditionString.append(" where ");
                    }
                    conditionString.append(" grod.product=? ");
                }

                if (requestParams.containsKey("goodsreceiptorder") && requestParams.get("goodsreceiptorder") != null) {
                    param.add(requestParams.get("goodsreceiptorder"));
                    if (conditionString.length() > 0) {
                        conditionString.append(" and ");
                    } else {
                        conditionString.append(" where ");
                    }
                    conditionString.append(" grod.grorder=? ");
                }
                query = "select id from grodetails grod " + conditionString.toString();
            }
            
            if (!StringUtil.isNullOrEmpty(query)) {
                List list = executeSQLQuery(query, param.toArray());
                if (!list.isEmpty() && !StringUtil.isNullObject(list)) {
                    detailid = (String) list.get(0);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return detailid;
    }

  @Override
    public String fetchlinkedDetailsid(Map<String,Object> valuesParams,String destinationmoduledetailstablename,String destinationmoduletablename,String sourcemoduledetailstablename) throws ServiceException {
        List param = new ArrayList();
        StringBuilder conditionString = new StringBuilder();
        String query = null;
        String detailid = "";
        
        String productid=null;
       try {
        //do not change the order as detailstablename is required
           //It is the destination module details table name
           if (!StringUtil.isNullOrEmpty(destinationmoduledetailstablename)) {
               //It is the source modules details table name
               if (valuesParams.containsKey(sourcemoduledetailstablename) && valuesParams.get(sourcemoduledetailstablename) != null) {
                   param.add((String) valuesParams.get(sourcemoduledetailstablename));
                   if (conditionString.length() > 0) {
                       conditionString.append(" and ");
                   } else {
                       conditionString.append(" where ");
                   }
                   conditionString.append(" " + destinationmoduledetailstablename + "." + sourcemoduledetailstablename + "=? ");
               }

               //it is the destination module tablename
               if (valuesParams.containsKey(destinationmoduletablename) && valuesParams.get(destinationmoduletablename) != null) {
                   param.add((String) valuesParams.get(destinationmoduletablename));
                   if (conditionString.length() > 0) {
                       conditionString.append(" and ");
                   } else {
                       conditionString.append(" where ");
                   }
                   conditionString.append(" " + destinationmoduledetailstablename + "." + destinationmoduletablename + "=? ");
               }

               //productid of destination module
               if (valuesParams.containsKey(Constants.productid) && valuesParams.get(Constants.productid) != null) {
                   productid = (String) valuesParams.get(Constants.productid);
                   param.add(productid);
                   if (conditionString.length() > 0) {
                       conditionString.append(" and ");
                   } else {
                       conditionString.append(" where ");
                   }
                   conditionString.append(" " + destinationmoduledetailstablename + ".product=? ");
               }
               query = "select id from " + destinationmoduledetailstablename + " " + conditionString.toString();

               if (!StringUtil.isNullOrEmpty(query)) {
                   List list = executeSQLQuery(query, param.toArray());
                   if (!list.isEmpty() && !StringUtil.isNullObject(list)) {
                       detailid = (String) list.get(0);
                   }
               }
           }// end of string util
        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return detailid;
    }
     
    @Override
    public Map<String, Object> getDetailsProductsid(JSONObject paramJobj, Map<String, Object> requestParams) throws ServiceException {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        List<String> vendorList = new ArrayList<String>();
        JSONArray returnJArray = new JSONArray();
        Set<String> vendorIdSet = new HashSet<String>();
        HashSet billidsSet = new HashSet<>();
        HashSet sourcePOBillidSet = new HashSet<>();
        StringBuilder conditionString = new StringBuilder();
        String query = null;
        String querywithoutlinking = null;
        String sourcemoduleid = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_MODULE);
        String destinationmoduleid = paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE);
        try {

            if (sourcemoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId)) && destinationmoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                if (requestParams.containsKey("selectedBillIds") && requestParams.get("selectedBillIds") != null) {
                    String selectedBillIds = requestParams.get("selectedBillIds").toString();
                    selectedBillIds = AccountingManager.getFilterInString(selectedBillIds);
                    if (conditionString.length() > 0) {
                        conditionString.append(" and ");
                    } else {
                        conditionString.append(" where ");
                    }
                    conditionString.append(" sod.salesorder IN " + selectedBillIds);
                    query = "select sod.product,sod.salesorder,sod.sourcepodetailsid,sod.id,vpm.vendorid from sodetails sod inner join vendorproductmapping vpm on vpm.vendorproducts=sod.product " + conditionString.toString();
                }
            }
            
           /* ERM-745 -  Case:When DO is linked with SO and then GRN will be generated on the basis of vendor mapped products. This code is used find the relation DO -> SO -> PO detailsid. PO is generated in the same company*/ 
             if (sourcemoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Delivery_Order_ModuleId)) && destinationmoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                if (requestParams.containsKey("selectedBillIds") && requestParams.get("selectedBillIds") != null) {
                    String selectedBillIds = requestParams.get("selectedBillIds").toString();
                    selectedBillIds = AccountingManager.getFilterInString(selectedBillIds);
                    if (conditionString.length() > 0) {
                        conditionString.append(" and ");
                    } else {
                        conditionString.append(" where ");
                    }
                    conditionString.append(" dod.deliveryorder IN " + selectedBillIds);
                    query = "select dod.product,dod.deliveryorder,pod.id,dod.id as dodetailid,vpm.vendorid,pod.purchaseorder from dodetails dod inner join vendorproductmapping vpm on "
                            + " vpm.vendorproducts=dod.product "
                            + " inner join podetails pod on pod.salesorderdetailid=dod.sodetails" + conditionString.toString();
                    querywithoutlinking = "select dod.product,dod.deliveryorder,dod.id as dodetailid,vpm.vendorid from dodetails dod inner join vendorproductmapping vpm on "
                            + " vpm.vendorproducts=dod.product "+ conditionString.toString();
//                            + " inner join podetails pod on pod.salesorderdetailid=dod.sodetails" + conditionString.toString();
                }
            } 
            

            if (!StringUtil.isNullOrEmpty(query)) {
                List<Object[]> list = executeSQLQuery(query, new Object[]{});
                if (!list.isEmpty() && !StringUtil.isNullObject(list)) {
                    for (Object[] row : list) {
//                        if (row[2] != null) {//if sourcepurchaseorderdetailid is not present
                        JSONObject returnJobj = new JSONObject();
                        returnJobj.put(Constants.productid, row[0]);
                        returnJobj.put(Constants.billid, row[1]);
                        billidsSet.add((String) row[1]);
                        returnJobj.put("sourcepodetailid", row[2]);
                        returnJobj.put("detailid", row[3]);
                        returnJobj.put(Constants.vendorid, row[4]);
                        vendorIdSet.add((String) row[4]);
                        vendorList.add((String) row[4]);
                        if (row.length>5  && row[5] != null && !StringUtil.isNullOrEmpty(row[5].toString())) {
                            returnJobj.put("sourcepoid", row[5].toString());
                            sourcePOBillidSet.add(row[5].toString());
                        }
                        returnJArray.put(returnJobj);
                    }
                } else if (!StringUtil.isNullOrEmpty(querywithoutlinking)) {
                    list = executeSQLQuery(querywithoutlinking, new Object[]{});
                    if (!list.isEmpty() && !StringUtil.isNullObject(list)) {
                        for (Object[] row : list) {

                            JSONObject returnJobj = new JSONObject();
                            returnJobj.put(Constants.productid, row[0]);
                            returnJobj.put(Constants.billid, row[1]);
                            billidsSet.add((String) row[1]);
                            returnJobj.put("detailid", row[2]);
                            returnJobj.put(Constants.vendorid, row[3]);
                            vendorIdSet.add((String) row[3]);
                            vendorList.add((String) row[3]);
                            returnJArray.put(returnJobj);
                        }
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnMap.put(Constants.detail, returnJArray.toString());
            returnMap.put("vendorIdSet", vendorIdSet);
            returnMap.put("vendorIdList", vendorList);
            returnMap.put("billidIdSet", billidsSet);
            returnMap.put("sourcePOBillidSet", sourcePOBillidSet);
            
        }
        return returnMap;
    }
   
   @Override
    public Map<String, Object> checkEntryForDeliveryOrderinGRO(String deliveryorderIds,String companyid) throws ServiceException {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        List params = new ArrayList();
        HashSet<String> sourceGROBillidSet = new HashSet<>();
        int totalCount = 0;
        String selectedBillIds = deliveryorderIds;
       selectedBillIds = AccountingManager.getFilterInString(selectedBillIds);
        try {
            params.add(companyid);
//            params.add(deliveryorderid);
//            String sqlQuery = "select grod.id,grod.sourcedeliveryorderdetailsid as deliveryorderid from grodetails grod inner join dodetails dod on grod.sourcedeliveryorderdetailsid =dod.id inner join deliveryorder deliObj on deliObj.id=dod.deliveryorder where deliObj.id=?  ";
            String sqlQuery = "select grod.id,grod.sourcedeliveryorderdetailsid as deliveryorderdetailsid,grod.grorder from grodetails grod inner join dodetails dod on grod.sourcedeliveryorderdetailsid =dod.id where dod.deliveryorder IN " + selectedBillIds+" and grod.company=?";
            List<Object[]> list = executeSQLQuery(sqlQuery, params.toArray());

            if (!list.isEmpty() && !StringUtil.isNullObject(list) && list.size() > 0) {
                totalCount = list.size();
                if (!list.isEmpty() && !StringUtil.isNullObject(list)) {
                    for (Object[] row : list) {
                        if(row.length>2 && row[2]!=null )
                        sourceGROBillidSet.add((String) row[2]);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnMap.put("sourceGROBillidSet", sourceGROBillidSet);

        }
        return returnMap;
    }
   
    @Override
    public Map<String, Object> checkEntryForSalesOrderInPO(String salesorderIds,String companyid) throws ServiceException {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        List params = new ArrayList();
        HashSet<String> sourcePOBillidSet = new HashSet<>();
        int totalCount = 0;
        String selectedBillIds = salesorderIds;
        selectedBillIds = AccountingManager.getFilterInString(selectedBillIds);
        try {
            params.add(companyid);
            String sqlQuery = "select pod.salesorderdetailid,pod.purchaseorder from podetails pod inner join sodetails sod on sod.id=pod.salesorderdetailid inner join purchaseorder po on po.id=pod.purchaseorder where sod.salesorder IN " + selectedBillIds+" and po.company=?";
//            String sqlQuery = "select sod.id,sod.salesorder,sod.sourcepodetailsid,sod.id from podetails grod inner join dodetails dod on grod.sourcedeliveryorderdetailsid =dod.id inner join deliveryorder deliObj on deliObj.id=dod.deliveryorder where deliObj.id IN" + selectedBillIds;
            List<Object[]> list = executeSQLQuery(sqlQuery, params.toArray());

            if (!list.isEmpty() && list.size()>0) {
                for (Object[] row : list) {
                    if (row.length > 1 && row[1] != null) {
                        sourcePOBillidSet.add((String) row[1]);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnMap.put("sourcePOBillidSet", sourcePOBillidSet);

        }
        return returnMap;
    }
   
    
    @Override//when gro is linked to po in parent subdomain then it fetches source po address
    public JSONObject getSourceCompanyPOTransactionid(JSONObject paramJObj, String companyid, String poDetailIds) throws ServiceException {
        JSONObject returnJobj=new JSONObject();
        List params = new ArrayList();
        int totalCount = 0;
        String selectedBillIds = poDetailIds;
        selectedBillIds = AccountingManager.getFilterInString(selectedBillIds);
        try {
            params.add(companyid);
            String sqlQuery = "select pod.salesorderdetailid,sod.id,sod.salesorder,sod.sourcepodetailsid from podetails pod inner join sodetails sod on sod.id=pod.salesorderdetailid inner join salesorder so on so.id=sod.salesorder where sod.id IN " + selectedBillIds + " and so.company=?";
            List<Object[]> list = executeSQLQuery(sqlQuery, params.toArray());

            if (!list.isEmpty() && list.size() > 0) {
                totalCount=list.size();
                returnJobj.put(Constants.RES_TOTALCOUNT,totalCount);
                for (Object[] row : list) {
                    if (row.length > 1 && row[1] != null) {
                        returnJobj.put("salesorderdetailid",(String) row[1]);
                    }
                    if (row.length > 0 && row[0] != null) {
                        returnJobj.put("podsodetailid",(String) row[0]);
                    }
                    
                    if (row.length > 2 && row[2] != null) {
                        returnJobj.put("salesorderid",(String) row[2]);
                    }
                    if (row.length > 3 && row[3] != null) {
                        returnJobj.put("sourcepodetailid",(String) row[3]);
                    }
                    
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return returnJobj;
    }
    
     @Override  //when gro is not linked to po in parent subdomain then it fetches source grn address
    public JSONObject getSOdetailid(JSONObject paramJObj, String companyid, String poDetailIds) throws ServiceException {
        JSONObject returnJobj = new JSONObject();
        List params = new ArrayList();
        int totalCount = 0;
        String selectedBillIds = poDetailIds;
        selectedBillIds = AccountingManager.getFilterInString(selectedBillIds);
        try {
            params.add(companyid);
            String sqlQuery = "select pod.salesorderdetailid,pod.id from podetails pod where pod.id IN " + selectedBillIds + " and pod.company=?";
            List<Object[]> list = executeSQLQuery(sqlQuery, params.toArray());

            if (!list.isEmpty() && list.size() > 0) {
                totalCount = list.size();
                returnJobj.put(Constants.RES_TOTALCOUNT, totalCount);
                for (Object[] row : list) {
                    if (row.length > 0 && row[0] != null) {
                        returnJobj.put("sodetailid", (String) row[0]);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnJobj;
    } 
     
     
     
     @Override
    public JSONObject getSourceCompanyGROTranasctionId(JSONObject paramJObj, String companyid, String poDetailIds) throws ServiceException {
        JSONObject returnJobj = new JSONObject();
        List params = new ArrayList();
        int totalCount = 0;
        String selectedBillIds = poDetailIds;
        selectedBillIds = AccountingManager.getFilterInString(selectedBillIds);
        try {
            params.add(companyid);
            String sqlQuery = "select grod.sourcedeliveryorderdetailsid as deliveryorderdetailsid,dod.deliveryorder,dod.sourcegoodsreceiptorderdetailsid,gro.id from grodetails grod inner join dodetails dod on grod.sourcedeliveryorderdetailsid =dod.id "
                    + " inner join grodetails grd on grd.id=dod.sourcegoodsreceiptorderdetailsid inner join grorder gro on gro.id=grd.grorder "
                    + " where grod.id IN " + selectedBillIds+" and grod.company=? ";
            List<Object[]> list = executeSQLQuery(sqlQuery, params.toArray());

            if (!list.isEmpty() && list.size() > 0) {
                totalCount = list.size();
                returnJobj.put(Constants.RES_TOTALCOUNT, totalCount);
                for (Object[] row : list) {
                    if (row.length > 1 && row[1] != null) {
                        returnJobj.put("deliveryorderid", (String) row[1]);
                    }
                    
                    if (row.length > 0 && row[0] != null) {
                        returnJobj.put("deliveryorderdetailid", (String) row[0]);
                    }

                    if (row.length > 2 && row[2] != null) {
                        returnJobj.put("sourcegrodetailid", (String) row[2]);
//                        sqlQuery = "select grod.id ,gro.id from grodetails grod inner join grorder dod on gro on gro.id=grd.id "
//                    + " inner join grodetails grd on grd.id=dod.sourcegoodsreceiptorderdetailsid inner join grorder gro on gro.id=grd.id "
//                    + " where grod.id IN " + selectedBillIds+" and grod.company=? ";
                        
                    }
                    if (row.length > 3 && row[3] != null) {
                        returnJobj.put("sourcegroid", (String) row[3]);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnJobj;
    }  
     
    
//    @Override
//    public Integer checkEntryForDeliveryOrderinGRO(String deliveryorderid) throws ServiceException {
//        List params = new ArrayList();
//        int totalCount = 0;
//        try {
//            params.add(deliveryorderid);
//            String sqlQuery = "select grod.id,grod.sourcedeliveryorderdetailsid from grodetails grod inner join dodetails dod on grod.sourcedeliveryorderdetailsid =dod.id inner join deliveryorder deliObj on deliObj.id=dod.deliveryorder where deliObj.id=?  ";
//            List<Object[]> list = executeSQLQuery(sqlQuery, params.toArray());
//            
//            if (!list.isEmpty() && !StringUtil.isNullObject(list) && list.size() > 0) {
//                totalCount = list.size();
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return totalCount;
//    }
    
   
//    @Override
//    public KwlReturnObject fetchVendorMappedProducts(JSONObject paramJobj, Map<String, Object> requestParams) throws ServiceException {
//        List param = new ArrayList();
//        List resultList = new ArrayList();
//        StringBuilder conditionString = new StringBuilder();
//        String query = null;
//        String vendorid = "";
//        try {
//            /*
//             * If product is selected
//             */
//            if (requestParams.containsKey("selectedProductIds") && requestParams.get("selectedProductIds") != null) {
//                String selectedProductIds = requestParams.get("selectedProductIds").toString();
//                selectedProductIds = AccountingManager.getFilterInString(selectedProductIds);
//                if (conditionString.length() > 0) {
//                    conditionString.append(" and ");
//                } else {
//                    conditionString.append(" where ");
//                }
//                conditionString.append(" vpm.vendorproducts IN " + selectedProductIds);
//                query = "select vpm.vendor from vendorproductmapping vpm " + conditionString.toString();
//            }
//
//            if (!StringUtil.isNullOrEmpty(query)) {
//                List<Object[]> list = executeSQLQuery(query, param.toArray());
//                if (!list.isEmpty() && !StringUtil.isNullObject(list)) {
//                    if (list != null && !list.isEmpty()) {
//                        for (Object[] result : list) {
//                            vendorid = (String) result[0];
//                            resultList.add(vendorid);
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception ex) {
//            Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return new KwlReturnObject(true, null, null, resultList, resultList.size());
//    }
  
}
