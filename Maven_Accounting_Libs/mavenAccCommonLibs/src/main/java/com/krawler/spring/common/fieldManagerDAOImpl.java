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
package com.krawler.spring.common;

import com.krawler.common.admin.AmendingPrice;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.GstDocumentHistory;
import com.krawler.common.admin.GstTaxClassHistory;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;

/**
 *
 * @author krawler
 */
public class fieldManagerDAOImpl extends BaseDAO implements fieldManagerDAO {

 @Override
 public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from FieldParams ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = hql.indexOf("(");

                if (ind > -1) {
                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    value.remove(index);
                }

            }

            /*When any custom field is created for Product & also available in CQ at line level 
            
             then fetching it from FieldParams*/
            if (requestParams.containsKey("isCallFromCRM")) {
               value=new ArrayList();
                value.add((String) requestParams.get("companyId"));
                hql += " where moduleid=30 and relatedmoduleid like '%22%' and companyid=?";
            }

            int moduleId = 0;
            if (requestParams.containsKey("moduleid")) {
                moduleId = requestParams.get("moduleid") != null ? Integer.parseInt(requestParams.get("moduleid").toString()) : 0;
            }
            
            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("isActivated") && (Integer) requestParams.get("isActivated") != null) {
                int activatedFlag = (Integer) requestParams.get("isActivated");
                hql += " and isactivated = " + activatedFlag;
            }
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
            if (requestParams.containsKey("parentid")) {
                hql += " and parentid = '" + requestParams.get("parentid") + "'";
            }
            if (requestParams.containsKey("checkForParent")) {
                hql += " and parentid is not null ";
            }
            if (moduleId != 0) {
                value.add(moduleId);
                hql += " and moduleid = ? ";
            }
            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }

            list = executeQuery(hql, value.toArray());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

  @Override
    public String getParamsValue(String fieldid, String fieldValue) throws ServiceException {
        String[] ids = fieldValue.split(",");
        String values = "";
        for (int i = 0; i < ids.length; i++) {
            List list = null;
            ArrayList params = new ArrayList();
            params.add(fieldid);
            params.add(ids[i]);
            String hql = "select value from FieldComboData where fieldid=? and id= ?";
            list = executeQuery(hql, params.toArray());
            if (list.size() > 0) {
                values += list.get(0).toString() + ",";
            }
        }
        if(!StringUtil.isNullOrEmpty(values)) {
            values = values.substring(0, values.length() - 1);
        }
        return values;
    }
    
    
     @Override
    public String getFieldComboDtaValueUsingId(String fieldcombodataid, String companyid) throws ServiceException {
        String[] ids = fieldcombodataid.split(",");
        String values = "";
        for (int i = 0; i < ids.length; i++) {
            List list = null;
            ArrayList params = new ArrayList();
            params.add(ids[i]);
            params.add(companyid);
            String hql = "select fieldcombodata.value from fieldcombodata inner join fieldparams on fieldparams.id = fieldcombodata.fieldid  where  fieldcombodata.id= ? and fieldparams.companyid=?";
            list = executeSQLQuery(hql, params.toArray());
            if (list.size() > 0) {
                values += list.get(0).toString() + ",";
            }
        }
        if(!StringUtil.isNullOrEmpty(values)) {
            values = values.substring(0, values.length() - 1);
        }
        return values;
    }
    
 @Override
    public String getIdsUsingParamsValue(String fieldid, String fieldValue) throws ServiceException {
        String[] value = fieldValue.split(",");
        String ids = "";
        for (int i = 0; i < value.length; i++) {
            List list = null;
            ArrayList params = new ArrayList();
            params.add(fieldid);
            params.add(value[i]);
            String hql = "select id from FieldComboData where fieldid=? and value= ?";
            list = executeQuery(hql, params.toArray());
            if (list.size() == 0 && !StringUtil.isNullOrEmpty(value[i])) {
                /*
                If value not present in ERP then insert such values
                */
                String uuid = UUID.randomUUID().toString();
                String query = "insert into fieldcombodata (id,value,fieldid) values(?,?,?)";
                executeSQLUpdate(query, new Object[]{uuid, value[i], fieldid});
                ids += uuid + ",";
            } else if (list.size() > 0) {
                ids += list.get(0).toString() + ",";
            }
        }
        if(!StringUtil.isNullOrEmpty(ids)) {
            ids = ids.substring(0, ids.length() - 1);
        }
        return ids;
    }
    /**
     * While getting field combo data IF value not present then don't add missing value.
     * @param fieldid
     * @param fieldValue
     * @return
     * @throws ServiceException 
     */
 @Override
    public String getIdsUsingParamsValueWithoutInsert(String fieldid, String fieldValue) throws ServiceException {
        String[] value = fieldValue.split(",");
        String ids = "";
        for (int i = 0; i < value.length; i++) {
            List list = null;
            ArrayList params = new ArrayList();
            params.add(fieldid);
            params.add(value[i]);
            String hql = "select id from FieldComboData where fieldid=? and value= ?";
            list = executeQuery(hql, params.toArray());
            if (list.size() > 0) {
                ids += list.get(0).toString() + ",";
            }
        }
        if(!StringUtil.isNullOrEmpty(ids)) {
            ids = ids.substring(0, ids.length() - 1);
        }
        return ids;
    }
 
 @Override 
    public int getColumnFromFieldParams(String fieldLabel, String companyId, int module,int customColumn) throws ServiceException {
        String moduleId = "";
        int column = 0;
        List list = null;
        ArrayList params = new ArrayList();
        params.add(fieldLabel);
        params.add(module);
        params.add(companyId);
        params.add(customColumn);
        String hql = "select colnum from FieldParams where fieldlabel=? and moduleid= ? and company.companyID= ? and customcolumn=?";
        list = executeQuery(hql, params.toArray());
        if (list.size() > 0) {
            moduleId += list.get(0).toString();
        }
        if (!StringUtil.isNullOrEmpty(moduleId)) {
            column = Integer.parseInt(moduleId);
        }
        return column;
    }
    /**
     * Function to get Field and its respective column number
     * @param params
     * @return
     * @throws ServiceException 
     */
    public List getFieldwiseColumnFieldParams(JSONObject params) throws ServiceException {
        String moduleId = "";
        int column = 0;
        List list = null;
        ArrayList param = new ArrayList();
        param.add(params.optInt("moduleid"));
        param.add(params.optString("companyId"));
        param.add(params.optInt("customColumn"));
        StringBuilder condition = new StringBuilder();
        condition.append(" and fieldlabel in (");
        condition.append(params.optString("fieldlables"));
        condition.append(")");
        String hql = "select concat(fieldlabel,':',colnum) from FieldParams where moduleid= ? and company.companyID= ? and customcolumn=? " + condition;
        list = executeQuery(hql, param.toArray());
        return list;
    }

@Override    
    //get DefaultHeaders-for Rest Services
    public KwlReturnObject getDefaultHeaders(HashMap<String, Object> requestParams) {
        List list = null;
        try {
            List<Integer> indexList = new ArrayList<Integer>();
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from DefaultHeader ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = hql.indexOf("(");

                while (ind > -1) {
                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    indexList.add(index);

                    ind = hql.indexOf("(", ind + 1);
                }
                Collections.reverse(indexList);
                for (Integer ctr : indexList) {
                    value.remove(ctr.intValue());
                }
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            list = executeQuery(hql, value.toArray());


        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public String getFieldParamsId(HashMap<String, Object> requestParams) throws ServiceException {

        String id = "";
        List list = null;
        ArrayList params = new ArrayList();
        if (requestParams.get("filedname") != null) {
            params.add(requestParams.get("filedname"));
        }
        if (requestParams.get("moduleid") != null) {
            params.add(requestParams.get("moduleid"));
        }
        if (requestParams.get("companyid") != null) {
            params.add(requestParams.get("companyid"));
        }

        String hql = "select id from FieldParams where fieldname=? and moduleid=? and companyid=?";
        list = executeQuery(hql, params.toArray());

        if (list.size() > 0) {
            id = list.get(0).toString();
        }
        return id;
    }
    /**
     * Desc : Function Return User GRP and FCD Mapping
     *
     * @param map
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getUsersGroupMappedToFCD(Map<String, Object> map) throws ServiceException {
        String masterfieldid = (String) map.get("masterFieldId");
        String value = (String) map.get("value");
        String fcdid = getIdsUsingParamsValue(masterfieldid, value);
        String query = "From UserGroupFieldComboMapping where fieldComboData.id=?";
        List list = executeQuery(query, new Object[]{fcdid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     * @Desc : Append Condition to fetch user specific records
     * @param reqparams = userid,companyid, jointable to whom join need to apply
     * and moduleid of master
     * @return
     * @throws ServiceException
     */
    public String appendUsersCondition(Map<String, Object> reqparams) throws ServiceException {
        List params = new ArrayList();
        if (reqparams.containsKey("userid") && reqparams.get("userid") != null) {
            params.add((String) reqparams.get("userid"));
        }
        if (reqparams.containsKey("companyid") && reqparams.get("companyid") != null) {
            params.add((String) reqparams.get("companyid"));
        }
        String jointable = "";
        if (reqparams.containsKey("jointable") && reqparams.get("jointable") != null) {
            jointable = (String) reqparams.get("jointable");
        }
        int moduleid = 30;
        if (reqparams.containsKey("moduleid") && reqparams.get("moduleid") != null) {
            moduleid = (int) reqparams.get("moduleid");

        }
        params.add(moduleid);
        String query = "select colnum,fieldcombodata from usergroupfieldcombomapping ufcd inner join usersgroup ug on ug.id=ufcd.usersgroup inner join  usersgroupmapping ugm on "
                + "ugm.usersgroup = ug.id where ugm.user=? and ug.company=? and ufcd.moduleid=?";
        List list = executeSQLQuery(query, params.toArray());
        String usercondition = "";
        for (Iterator it = list.iterator(); it.hasNext();) {
            Object[] object = (Object[]) it.next();
            short colnum = (short) object[0];
            String Value = (String) object[1];
            usercondition += " " + jointable + ".col" + colnum + "='" + Value + "' or";
        }
        if (usercondition.length() > 0) {
            usercondition = usercondition.substring(0, usercondition.length() - 2);
        }
        return usercondition;
    }
    
 @Override
    public boolean isKnockOffAdvancedSearch(String Searchjson, String companyId) throws ServiceException {
        boolean isAdvanceSearch = false;
        try {
            if (!StringUtil.isNullOrEmpty(Searchjson) && !StringUtil.isNullOrEmpty(companyId)) {
                JSONObject jobjSearch = new JSONObject(Searchjson);
                int count = jobjSearch.getJSONArray(Constants.root).length();
                for (int i = 0; i < count; i++) {
                    KwlReturnObject result = null;
                    JSONObject jobj1 = jobjSearch.getJSONArray(Constants.root).getJSONObject(i);
                    HashMap<String, Object> requestParams = new HashMap<>();
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyId, StringUtil.DecodeText(jobj1.optString("columnheader"))));
                    result = getFieldParams(requestParams);
                    List<FieldParams> fieldsList = result.getEntityList();
                    for (FieldParams fieldParams : fieldsList) {
                        if (fieldParams.isIsForKnockOff()) {
                            isAdvanceSearch = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("fieldDataManager.isMultiDimesionalAdvancedSearch" + ex.getMessage(), ex);
        }
        return isAdvanceSearch;
    }
    /**
     *
     * @param nObject
     * @return
     * @Desc : Get Address- Dimension Mapping
     * @throws ServiceException
     */
    public KwlReturnObject getAddressDimensionMapping(JSONObject nObject) throws ServiceException {
        List params = new ArrayList();
        String company = nObject.optString("companyid");
        params.add(company);
        String query = " from AddressFieldDimensionMapping where company.companyID = ? ";
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public String getStateForEntity(JSONObject reqParams) throws ServiceException {
        List params = new ArrayList();
        String stateValue = "";
        String companyid = reqParams.optString("companyid");
        String entity = reqParams.optString("entityid");
        params.add(entity);
        int colnum = getColumnFromFieldParams(Constants.STATE, companyid, Constants.GSTModule, 0);
        String query = " select fcd.value from fieldcombodata fcd inner join multientitydimesioncustomdata cd on fcd.id=cd.col" + colnum + " where fcdId=?";
        List list = executeSQLQuery(query, params.toArray());
        if (!list.isEmpty() && list.get(0) != null) {
            stateValue = (String) list.get(0);
        }
        return stateValue;
    }
    
    @Override
    public KwlReturnObject getFieldComboDescAndValue(JSONObject reqParams) {
        List list = new ArrayList();
        try {
            StringBuilder query = new StringBuilder();
            List params = new ArrayList();
            query.append("select fcd.itemdescription,fcd.value from fieldcombodata fcd inner join fieldparams fp on fp.id = fcd.fieldid ");
            if (reqParams.has(Constants.companyKey)) {
                query.append(" where fp.companyid =  ? ");
                params.add(reqParams.get(Constants.companyKey));
            }
            if (reqParams.has(Constants.moduleid)) {
                if (query.indexOf("where") == -1) {
                    query.append(" where");
                } else {
                    query.append(" and ");
                }
                query.append("fp.moduleid = ? ");
                params.add(reqParams.get(Constants.moduleid));
            }
            if (reqParams.has(Constants.fieldlabel)) {
                if (query.indexOf("where") == -1) {
                    query.append(" where");
                } else {
                    query.append(" and ");
                }
                query.append("fp.fieldlabel like '%" + reqParams.get(Constants.fieldlabel) + "%' ");
            }
            list = executeSQLQuery(query.toString(), params.toArray());
        } catch (Exception ex) {
            org.apache.log4j.Logger.getLogger(fieldDataManagerDAOImpl.class.getName()).log(org.apache.log4j.Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * Function to save GST document history
     *
     * @param reqMap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject saveGstDocumentHistory(Map<String, Object> reqMap) throws ServiceException {
        List list = new ArrayList();
        try {
            GstDocumentHistory gstDocumentHistory = null;
            if (reqMap.containsKey("gstdochistoryid") && reqMap.get("gstdochistoryid") != null) {
                gstDocumentHistory = (GstDocumentHistory) get(GstDocumentHistory.class, (String) reqMap.get("gstdochistoryid"));
            } else {
                gstDocumentHistory = new GstDocumentHistory();
            }
            if (reqMap.containsKey("custventype") && reqMap.get("custventype") != null) {
                gstDocumentHistory.setCustvenTypeId((String) reqMap.get("custventype"));
            }
            if (reqMap.containsKey("gstrtype") && reqMap.get("gstrtype") != null) {
                gstDocumentHistory.setGstrType((String) reqMap.get("gstrtype"));
            }
             if (reqMap.containsKey("gstin") && reqMap.get("gstin") != null) {
                gstDocumentHistory.setGstin((String) reqMap.get("gstin"));
            }
            if (reqMap.containsKey("docid") && reqMap.get("docid") != null) {
                gstDocumentHistory.setRefDocId((String) reqMap.get("docid"));
            }
            if (reqMap.containsKey("moduleid") && reqMap.get("moduleid") != null) {
                gstDocumentHistory.setModuleId((int) reqMap.get("moduleid"));
            }
            saveOrUpdate(gstDocumentHistory);

            list.add(gstDocumentHistory);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    /**
     * Function to save Tax class history at document level
     *
     * @param reqMap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject saveGstTaxClassHistory(Map<String, Object> reqMap) throws ServiceException {
        List list = new ArrayList();
        try {
            GstTaxClassHistory gstTaxClassHistory = null;
            if (reqMap.containsKey("taxclasshistoryid") && reqMap.get("taxclasshistoryid") != null) {
                gstTaxClassHistory = (GstTaxClassHistory) get(GstTaxClassHistory.class, (String) reqMap.get("taxclasshistoryid"));
                if (gstTaxClassHistory == null) {
                    gstTaxClassHistory = new GstTaxClassHistory();
                }
            } else {
                gstTaxClassHistory = new GstTaxClassHistory();
            }
            if (reqMap.containsKey("taxclass") && reqMap.get("taxclass") != null) {
                gstTaxClassHistory.setProductTaxClass((String) reqMap.get("taxclass"));
            }
            if (reqMap.containsKey("detaildocid") && reqMap.get("detaildocid") != null) {
                gstTaxClassHistory.setRefDocId((String) reqMap.get("detaildocid"));
            }
            if (reqMap.containsKey("moduleid") && reqMap.get("moduleid") != null) {
                gstTaxClassHistory.setModuleId((int) reqMap.get("moduleid"));
            }
            saveOrUpdate(gstTaxClassHistory);

            list.add(gstTaxClassHistory);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    /**
     * Function to get GST fields history for documents
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getGSTDocumentHistory(JSONObject reqParams) throws ServiceException, JSONException {
        String refdocid = reqParams.optString("refdocid");
        /**
         * Changes in query to get GST details master items default ids
         */
        String query = " select gdh.id,gdh.custventypeid,gdh.gstrtype,gdh.gstin,mi1.defaultmasteritem as GSTINRegTypeDefaultMstrID, mi2.defaultmasteritem as CustVenTypeDefaultMstrID  "
                + " from gstdocumenthistory gdh inner join masteritem mi1 on mi1.id=gdh.gstrtype  "
                + " inner join masteritem mi2 on mi2.id=gdh.custventypeid  where refdocid=?";
        List list = executeSQLQuery(query.toString(), new Object[]{refdocid});
        return list;
    }

    /**
     * Function to get GST tax class history for documents
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getGSTTaxClassHistory(JSONObject reqParams) throws ServiceException, JSONException {
        String refdocid = reqParams.optString("refdocid");
        String query = "select id,producttaxclass from gsttaxclasshistory where refdocid=?";
        List list = executeSQLQuery(query.toString(), new Object[]{refdocid});
        return list;
    }

    /**
     * Function to delete GST tax class history for documents
     *
     * @param docrefid
     * @throws ServiceException
     */
    public void deleteGstTaxClassDetails(String docrefid) throws ServiceException {
        if (!StringUtil.isNullOrEmpty(docrefid)) {
            String delQuery = " delete from gsttaxclasshistory where refdocid IN (" + docrefid + ")";
            executeSQLUpdate(delQuery);
        }
    }

    /**
     * * Function to delete GST fields history for documents
     *
     * @param docrefid
     * @throws ServiceException
     */
    public void deleteGstDocHistoryDetails(String docrefid) throws ServiceException {
        if (!StringUtil.isNullOrEmpty(docrefid)) {
            String delQuery = " delete from gstdocumenthistory where refdocid=?";
            executeSQLUpdate(delQuery, new Object[]{docrefid});
        }
    }
    
    @Override
    public JSONObject GetUserAmendingPrice(HashMap<String, Object> requestParams) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        AmendingPrice AmendingPriceObj = (AmendingPrice) get(AmendingPrice.class, (String) requestParams.get("userid"));
        if (AmendingPriceObj != null) {
            try {
                jobj.put("CInvoice", AmendingPriceObj.isCInvoice());
                jobj.put("VInvoice", AmendingPriceObj.isVInvoice());
                jobj.put("SalesOrder", AmendingPriceObj.isSalesOrder());
                jobj.put("PurchaseOrder", AmendingPriceObj.isPurchaseOrder());
                jobj.put("VendorQuotation", AmendingPriceObj.isVendorQuotation());
                jobj.put("CustomerQuotation", AmendingPriceObj.isCustomerQuotation());
                jobj.put("BlockAmendingPrice", AmendingPriceObj.isBlockAmendingPrice());
            } catch (JSONException ex) {
                throw ServiceException.FAILURE("GetUserAmendingPrice.GetUserAmendingPrice:" + ex, ex);
            }
        }
        return jobj;
    }
    
    public List getFieldComboValue(int colnum, String productid) throws ServiceException, JSONException {
        List list = new ArrayList();
        list.add(productid);
        String query = " select fcd.value,fcd.id from fieldcombodata fcd inner join accproductcustomdata pcd on fcd.id=pcd.col" + colnum + " where pcd.productId=?";
        List returnList = executeSQLQuery(query, list.toArray());
        return returnList;
    }
    
    @Override
    public List getTaxPercentageUsingDefaultTerm(int colnum, String defaulttermid, String companyid, String productid, String entityID, Date applyDate) throws ServiceException, JSONException {
        List list = new ArrayList();
        String sqlQuery = "select entr.id,entr.percentage,entr.applieddate from company c \n"
                + "inner join product p on p.company = c.companyid \n"
                + "inner join accproductcustomdata acp on p.id = acp.productId \n"
                + "inner join prodcategorygstmapping pcgm on acp.col" + colnum + " = pcgm.prodcategory \n"
                + "inner join entitybasedlineleveltermsrate entr on pcgm.entitytermrate = entr.id\n"
                + "inner join linelevelterms lt on lt.id = entr.linelevelterms and lt.company = c.companyid\n"
                + "where lt.defaultterms = ? and c.companyid = ? and p.id = ? and entr.entity = ? and entr.applieddate = \n"
                + "(select max(eltr1.applieddate) from  entitybasedlineleveltermsrate eltr1 inner join prodcategorygstmapping pcgm1 on  pcgm1.entitytermrate = eltr1.id where eltr1.linelevelterms = lt.id and eltr1.entity=entr.entity and eltr1.applieddate<=? and pcgm.prodcategory = pcgm1.prodcategory)";
        List params = new ArrayList();
        params.add(defaulttermid);
        params.add(companyid);
        params.add(productid);
        params.add(entityID);
        params.add(applyDate);
        list = executeSQLQuery(sqlQuery, params.toArray());
        return list;
    }
    
    @Override
    public boolean isTaxActivated(String companyId, String taxId) throws ServiceException {
        boolean isActivated = false;
        try {
            String query = "select activated from Tax where company.companyID = ? and id = ?";
            ArrayList params = new ArrayList();
            params.add(companyId);
            params.add(taxId);
            List list = executeQuery(query, params.toArray());
            if (!list.isEmpty() && list.size() > 0) {
                isActivated = (boolean) list.get(0);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("fieldManagerDAOImpl.isTaxActivated", ex);
        }
        return isActivated;
    }
    /**
     * Function to get column no and Field combo data id for any dimension
     * value. Here this is written for Entity in Indian GST.
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    public List getEntityDataForRequestedModule(JSONObject reqParams) throws ServiceException {
        ArrayList qParams = new ArrayList();
        qParams.add(reqParams.optString("companyid"));
        StringBuilder builder = new StringBuilder();
        if (reqParams.has("moduleids")) {
            builder.append(" and fp.moduleid in (");
            String typeArr[] = reqParams.optString("moduleids").split(",");
            for (String type : typeArr) {
                if (builder.indexOf("?") == -1) {
                    builder.append("?");
                    qParams.add(type);
                } else {
                    builder.append(",?");
                    qParams.add(type);
                }
            }
            builder.append(") ");
        }
        if (reqParams.has("fieldlabel")) {
            builder.append(" and fp.fieldlabel=? ");
            qParams.add(reqParams.optString("fieldlabel"));
        }
        if (reqParams.has("fcdvalue")) {
            builder.append(" and fcd.value=? ");
            qParams.add(reqParams.optString("fcdvalue"));
        }

        String query = " select fp.moduleid,fp.id as fieldparamsid,fp.colnum,fcd.id as fcdid from fieldparams fp "
                + " inner join fieldcombodata fcd on fcd.fieldid=fp.id where fp.companyid=? " + builder;
        List list = executeSQLQuery(query, qParams.toArray());
        return list;
    }

    /**
     * Function to get column number for any no of custom field and dimension at
     * one time
     * @param fieldData
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    public List getColnumForDimensionCollectively(Map<String, Integer> fieldData, JSONObject reqParams) throws ServiceException {
        StringBuilder query = new StringBuilder();
        List params = new ArrayList();
        String companyid = reqParams.optString("companyid");
        for (Map.Entry<String, Integer> entry : fieldData.entrySet()) {
            String fieldlabel = entry.getKey();
            Integer moduleid = entry.getValue();
            fieldlabel = fieldlabel.replace("" + moduleid, "");
            query.append(" select colnum,'" + fieldlabel + "','" + moduleid + "' from fieldparams where fieldname=? and moduleid= ? and companyid=? ");
            query.append(" UNION ALL");
            params.add("Custom_" + fieldlabel);
            params.add(moduleid);
            params.add(companyid);
        }
        String finalQuery = query.toString();
        finalQuery = finalQuery.substring(0, finalQuery.length() - 10);
        List list = executeSQLQuery(finalQuery.toString(), params.toArray());
        return list;
    }
}
