/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class AccContractManagementDAOImpl extends BaseDAO implements AccContractManagementDAO {

    private MessageSource messageSource;

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    @Override
    public KwlReturnObject saveMasterContract(Map<String, Object> requestParam) {
        KwlReturnObject result;
        try {
            DateFormat df = (DateFormat) requestParam.get(Constants.df);
            String mastercontractid = "";
            if (requestParam.containsKey("mastercontractid") && requestParam.get("mastercontractid")!=null) {
                mastercontractid = (String) requestParam.get("mastercontractid");
            }

            MRPContract contract = null;
            if(!StringUtil.isNullOrEmpty(mastercontractid)){
                contract = (MRPContract) get(MRPContract.class, mastercontractid);
            }else{
                contract = new MRPContract();
            }

            if (requestParam.containsKey("contractid")) {
                contract.setContractid((String) requestParam.get("contractid"));
            }
            if (requestParam.containsKey("contractname")) {
                contract.setContractname((String) requestParam.get("contractname"));
            }
            if (requestParam.containsKey("creationdate")) {
                Date creationdate = df.parse((String) requestParam.get("creationdate"));
                contract.setCreationdate(creationdate);
            }
            if (requestParam.containsKey("customer")) {
                contract.setCustomer((Customer) get(Customer.class, (String) requestParam.get("customer")));
            }
            if (requestParam.containsKey("sellertype")) {
                contract.setSellertype((MasterItem) get(MasterItem.class, (String) requestParam.get("sellertype")));
            }
            if (requestParam.containsKey("contractstartdate")) {
                Date contractstartdate = df.parse((String) requestParam.get("contractstartdate"));
                contract.setContractstartdate(contractstartdate);
            }
            if (requestParam.containsKey("contractenddate")) {
                Date contractenddate = df.parse((String) requestParam.get("contractenddate"));
                contract.setContractenddate(contractenddate);
            }
            if (requestParam.containsKey("contractterm")) {
                contract.setContractterm((String) requestParam.get("contractterm"));
            }
            if (requestParam.containsKey("contractstatus")) {
                contract.setContractstatus((MasterItem) get(MasterItem.class, (String) requestParam.get("contractstatus")));
            }
//            if (requestParam.containsKey("parentcontractid")) {
//                contract.setParentcontractid((MRPContract) get(MRPContract.class, (String) requestParam.get("parentcontractid")));
//            }
            if (requestParam.containsKey("parentcontractname")) {
                contract.setParentcontractname((String) requestParam.get("parentcontractname"));
            }
            if (requestParam.containsKey("companyid")) {
                contract.setCompany((Company) get(Company.class, (String) requestParam.get("companyid")));
            }
            if (requestParam.containsKey("deleteflag")) {
                contract.setDeleteflag((Boolean) requestParam.get("deleteflag"));
            }
            if (requestParam.containsKey("paymentmethodname")) {
                contract.setPaymentmethodname((PaymentMethod) get(PaymentMethod.class, (String) requestParam.get("paymentmethodname")));
            }
            if (requestParam.containsKey("accountname")) {
                contract.setAccountname((String) requestParam.get("accountname"));
            }
            if (requestParam.containsKey("detailtype")) {
                contract.setDetailstype((String) requestParam.get("detailtype"));
            }
            if (requestParam.containsKey("autopopulate")) {
                contract.setAutopopulate((String) requestParam.get("autopopulate"));
            }
            if (requestParam.containsKey("showincscp")) {
                contract.setShownincsorcp((String) requestParam.get("showincscp"));
            }
            if (requestParam.containsKey("bankname")) {
                contract.setBankname((String) requestParam.get("bankname"));
            }
            if (requestParam.containsKey("bankaccountnumber")) {
                contract.setBankaccountnumber((String) requestParam.get("bankaccountnumber"));
            }
            if (requestParam.containsKey("bankaddress")) {
                contract.setBankaddress((String) requestParam.get("bankaddress"));
            }
            if (requestParam.containsKey("paymenttermname")) {
                contract.setPaymenttermname((Term) get(Term.class, (String) requestParam.get("paymenttermname")));
            }
            if (requestParam.containsKey("paymenttermdate")) {
                Date paymenttermdate = df.parse((String) requestParam.get("paymenttermdate"));
                contract.setPaymenttermdate(paymenttermdate);
            }
            if (requestParam.containsKey("contractorTeeName")) {
                contract.setContractorteename((String) requestParam.get("contractorTeeName"));
            }
            if (requestParam.containsKey("PANNumber")) {
                contract.setPannumber((String) requestParam.get("PANNumber"));
            }
            if (requestParam.containsKey("TANNumber")) {
                contract.setTannumber((String) requestParam.get("TANNumber"));
            }
            if (requestParam.containsKey("dateOfAggrement")) {
                Date dateOfAggrement = df.parse((String) requestParam.get("dateOfAggrement"));
                contract.setDateofaggrement(dateOfAggrement);
            }
            if (requestParam.containsKey("countryAggrement")) {
                contract.setCountryaggrement((MasterItem) get(MasterItem.class, (String) requestParam.get("countryAggrement")));
            }
            if (requestParam.containsKey("stateAggrement")) {
                contract.setStateaggrement((String) requestParam.get("stateAggrement"));
            }
            if (requestParam.containsKey("previousContractId")) {
                contract.setPreviouscontractid((String) requestParam.get("previousContractId"));
            }
            if (requestParam.containsKey("remarks")) {
                contract.setDocumentrequiredremarks((String) requestParam.get("remarks"));
            }
            if (requestParam.containsKey("savedFilesMappingId")) {
                contract.setActualattachment((String) requestParam.get("savedFilesMappingId"));
            }
            if (requestParam.containsKey(Constants.SEQFORMAT)) {
                contract.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) requestParam.get(Constants.SEQFORMAT)));
            }
            if (requestParam.containsKey(Constants.SEQNUMBER)) {
                contract.setSeqnumber(Integer.parseInt(requestParam.get(Constants.SEQNUMBER).toString()));
            }
            if (requestParam.containsKey(Constants.DATEPREFIX) && requestParam.get(Constants.DATEPREFIX) != null) {
                contract.setDatepreffixvalue((String) requestParam.get(Constants.DATEPREFIX));
            }
            if (requestParam.containsKey(Constants.DATEAFTERPREFIX) && requestParam.get(Constants.DATEAFTERPREFIX) != null) {
                contract.setDateAfterPreffixValue((String) requestParam.get(Constants.DATEAFTERPREFIX));
            }
            if (requestParam.containsKey(Constants.DATESUFFIX) && requestParam.get(Constants.DATESUFFIX) != null) {
                contract.setDatesuffixvalue((String) requestParam.get(Constants.DATESUFFIX));
            }
            if (requestParam.containsKey("autogen")) {
                boolean isautogenerated = Boolean.parseBoolean(requestParam.get("autogen").toString());
                contract.setAutogen(isautogenerated);
            }
            if (requestParam.containsKey("accmrpcontractcustomdataref") && requestParam.get("accmrpcontractcustomdataref") != null) {
                MRPContractCustomData mrpContractCustomData = null;
                mrpContractCustomData = (MRPContractCustomData) get(MRPContractCustomData.class, (String) requestParam.get("accmrpcontractcustomdataref"));
                contract.setAccMRPContractCustomData(mrpContractCustomData);
            }

            saveOrUpdate(contract);

            List resultlist = new ArrayList();
            resultlist.add(contract);
            result = new KwlReturnObject(true, null, null, resultlist, resultlist.size());
        } catch (ServiceException | ParseException ex) {
            result = new KwlReturnObject(false, "SaveContract:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    @Override
    public KwlReturnObject saveContractMapping(Map<String, Object> requestParam) {
        KwlReturnObject result;
        try {
            MRPContractMapping contractmapping = new MRPContractMapping();

            if (requestParam.containsKey("mrpContractID")) {
                contractmapping.setMrpcontract((MRPContract) get(MRPContract.class, (String) requestParam.get("mrpContractID")));
            }
            if (requestParam.containsKey("parentcontractid")) {
                contractmapping.setParentcontractid((String) requestParam.get("parentcontractid"));
            }
            
            saveOrUpdate(contractmapping);

            List resultlist = new ArrayList();
            resultlist.add(contractmapping);
            result = new KwlReturnObject(true, null, null, resultlist, resultlist.size());
        } catch (ServiceException ex) {
            result = new KwlReturnObject(false, "saveContractMapping:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    @Override
    public KwlReturnObject saveContractAddressDetails(Map<String, Object> requestParam) {
        KwlReturnObject result;
        try {
            MRPAddressDetails addressdetails = new MRPAddressDetails();

            if (requestParam.containsKey("mrpContractID") && requestParam.get("mrpContractID") != null) {
                addressdetails.setMrpcontract((MRPContract) get(MRPContract.class, (String) requestParam.get("mrpContractID")));
                addressdetails.setIsbilling(true);
            }

            if (requestParam.containsKey("mrpContractDetailsID") && requestParam.get("mrpContractDetailsID") != null) {
                addressdetails.setMrpcontractdetails((MRPContractDetails) get(MRPContractDetails.class, (String) requestParam.get("mrpContractDetailsID")));
                addressdetails.setIsbilling(false);
            }

            if (requestParam.containsKey("addresscombo")) {
                addressdetails.setAddresscombo((String) requestParam.get("addresscombo"));
            }

            if (requestParam.containsKey("aliasname")) {
                addressdetails.setAliasname((String) requestParam.get("aliasname"));
            }

            if (requestParam.containsKey("address")) {
                addressdetails.setAddress((String) requestParam.get("address"));
            }

            if (requestParam.containsKey("county")) {
                addressdetails.setCounty((String) requestParam.get("county"));
            }
            
            if (requestParam.containsKey("city")) {
                addressdetails.setCity((String) requestParam.get("city"));
            }

            if (requestParam.containsKey("state")) {
                addressdetails.setState((String) requestParam.get("state"));
            }

            if (requestParam.containsKey("country")) {
                addressdetails.setCountry((String) requestParam.get("country"));
            }

            if (requestParam.containsKey("postal")) {
                addressdetails.setPostalcode((String) requestParam.get("postal"));
            }

            if (requestParam.containsKey("phone")) {
                addressdetails.setPhone((String) requestParam.get("phone"));
            }

            if (requestParam.containsKey("mobile")) {
                addressdetails.setMobilenumber((String) requestParam.get("mobile"));
            }

            if (requestParam.containsKey("fax")) {
                addressdetails.setFax((String) requestParam.get("fax"));
            }

            if (requestParam.containsKey("email")) {
                addressdetails.setEmailid((String) requestParam.get("email"));
            }

            if (requestParam.containsKey("recipientname")) {
                addressdetails.setRecipientname((String) requestParam.get("recipientname"));
            }

            if (requestParam.containsKey("contactperson")) {
                addressdetails.setContactperson((String) requestParam.get("contactperson"));
            }

            if (requestParam.containsKey("contactpersonnumber")) {
                addressdetails.setContactpersonnumber((String) requestParam.get("contactpersonnumber"));
            }

            if (requestParam.containsKey("contactpersondesignation")) {
                addressdetails.setContactpersondesignation((String) requestParam.get("contactpersondesignation"));
            }

            if (requestParam.containsKey("website")) {
                addressdetails.setWebsite((String) requestParam.get("website"));
            }

            if (requestParam.containsKey("route")) {
                addressdetails.setRoute((String) requestParam.get("route"));
            }

            saveOrUpdate(addressdetails);

            List resultlist = new ArrayList();
            resultlist.add(addressdetails);
            result = new KwlReturnObject(true, null, null, resultlist, resultlist.size());
        } catch (ServiceException ex) {
            result = new KwlReturnObject(false, "SaveContractAddressDetails:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    @Override
    public KwlReturnObject getMasterContracts(Map<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            DateFormat df = (DateFormat) request.get(Constants.df);
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String ss = (String) request.get(Constants.ss);

            ArrayList params = new ArrayList();
            String orderQuery = " order by con.creationdate desc, con.contractid desc";
            params.add((String) request.get(Constants.companyKey));
            String condition = " where con.deleteflag=false and con.company.companyID=? ";

            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i < 2; i++) {
                    params.add(ss + "%");
                }
                condition += " and ( con.contractid like ? or con.customer.name like ? )"; //or con.memo like ? 
            }
            String costCenterId = (String) request.get(CCConstants.REQ_costCenterId);
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and con.costcenter.ID=?";
            }
            String customerId = (String) request.get(CCConstants.REQ_customerId);
            if (!StringUtil.isNullOrEmpty(customerId)) {
                params.add(customerId);
                condition += " and con.customer.ID=?";
            }
            String startDate = (String) request.get(Constants.REQ_startdate);
            String endDate = (String) request.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (con.creationdate >=? and con.creationdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if (request.containsKey("billId") && request.get("billId") != null) {
                String billid = (String) request.get("billId");
                if (!StringUtil.isNullOrEmpty(billid)) {
                    if (billid.contains(",")) {
                        String contractIds = AccountingManager.getFilterInString(billid);
                        condition += " and con.id in " + contractIds;
                    } else {
                        condition += " and con.id=? ";
                        params.add(billid);
                    }
                }
            }

            String query = "from MRPContract con" + condition;
            query += orderQuery;
            list = executeQuery(query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (ParseException | ServiceException | NumberFormatException ex) {
            throw ServiceException.FAILURE("AccContractManagementDAOImpl.getMasterContracts:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getMRPContractDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from MRPContractDetails ";
        return buildNExecuteQuery(query, requestParams);
    }
    
    public KwlReturnObject getMasterContractDetails(Map<String, Object> requestParams) throws ServiceException {
        
        String condition = "", joinCondition = "" , moduleId = "";
        String start = "";
        String limit = "";
        List list = new ArrayList();
        try {

            ArrayList params = new ArrayList();
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);

            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and (contract.creationdate >=? and contract.creationdate <=?)";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            if (requestParams.containsKey("moduleid")) {
                moduleId = (String) requestParams.get("moduleid");
            }

            String ss = (requestParams.containsKey("ss") && requestParams.get("ss") != null) ? (String) requestParams.get("ss") : "";
            if (!StringUtil.isNullOrEmpty(ss)) {
                String[] searchcol = new String[]{"contract.contractname", "contract.contractid"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            /*
             Advance Search Component
             */
            String appendCase = "and";
            String mySearchFilterString = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().trim().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            String searchDefaultFieldSQL = "";
            if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                Searchjson = requestParams.get("searchJson").toString();
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);
                    if (defaultSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Default Form fields
                         */
                        ArrayList tableArray = new ArrayList();
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, moduleId, tableArray, filterConjuctionCriteria);
                        joinCondition += " LEFT JOIN contract.rows crd";
                        joinCondition += " LEFT JOIN contract.accMRPContractCustomData cd ";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("mrpcontractRef.rows", "crd");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("mrpcontractRef", "contract");
                    }
                    if (customSearchFieldArray.length() > 0) {
                        /*
                         Advance Search For Custom fields
                         */
                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, false).get(Constants.myResult));
                        if (mySearchFilterString.contains("c.MRPContractCustomData")) {
                            mySearchFilterString = mySearchFilterString.replaceAll("c.MRPContractCustomData", "cd");
                        }
                        StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }

            String selQuery = "select distinct contract from  MRPContract contract " + joinCondition + " where contract.company.companyID= ? " + condition + mySearchFilterString;

            if (requestParams.containsKey(Constants.start) && requestParams.get(Constants.start) != null && requestParams.containsKey(Constants.limit) && requestParams.get(Constants.limit) != null) {
                start = (String) requestParams.get(Constants.start);
                limit = (String) requestParams.get(Constants.limit);
            }

            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(selQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            } else {
                list = executeQuery(selQuery, params.toArray());
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getContractMasterDetails : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getAddressDetails(Map<String, Object> requestParams) throws ServiceException {
        String condition = "";
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            if(requestParams.containsKey("mrpContractID") && requestParams.get("mrpContractID")!=null){
                params.add((String) requestParams.get("mrpContractID"));
                condition += " where mad.mrpcontract.ID = ? ";
            }else if(requestParams.containsKey("mrpContractDetailsID") && requestParams.get("mrpContractDetailsID")!=null){
                params.add((String) requestParams.get("mrpContractDetailsID"));
                condition += " where mad.mrpcontractdetails.ID = ? ";
            }
            
            String selQuery = "select mad from MRPAddressDetails mad " + condition;
            list = executeQuery(selQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getContractMasterDetails : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveMRPDocuments(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            if (dataMap.containsKey("MRPContractDocuments")) {
                MRPContractDocuments document = (MRPContractDocuments) dataMap.get("MRPContractDocuments");
                saveOrUpdate(document);
                list.add(document);
            }
            if (dataMap.containsKey("MRPContractDocumentsMapping")) {
                MRPContractDocumentsMapping documentMap = (MRPContractDocumentsMapping) dataMap.get("MRPContractDocumentsMapping");
                saveOrUpdate(documentMap);
                list.add(documentMap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveinvoiceDocuments : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public int saveFileMapping(Map<String, Object> filemap) throws ServiceException {
        int count = 0;
        String columns = "";
        String values = "";
        String query = "";
        String id = !StringUtil.isNullOrEmpty(filemap.get("id").toString()) ? filemap.get("id").toString() : "";
        String companyid = !StringUtil.isNullOrEmpty(filemap.get("companyid").toString()) ? filemap.get("companyid").toString() : "";
        String documentid = !StringUtil.isNullOrEmpty(filemap.get("documentid").toString()) ? filemap.get("documentid").toString() : "";
        try {
            if (!StringUtil.isNullOrEmpty(id)) {
                columns += "id";
                values += "'" + id + "'";
            }
            if (!StringUtil.isNullOrEmpty(companyid)) {
                columns += ",company";
                values += ",'" + companyid + "'";
            }
            if (!StringUtil.isNullOrEmpty(documentid)) {
                columns += ",documentid";
                values += ",'" + documentid + "'";
            }
            if (!columns.equals("")) {
                columns = "(" + columns + ")";
            }
            if (!values.equals("")) {
                values = "(" + values + ")";
            }
            query = "insert into invoicedoccompmaptemporary " + columns + " values " + values;
            count = executeSQLUpdate(query);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("saveFileMapping : " + ex.getMessage(), ex);
        }
        return count;
    }

    @Override
    public KwlReturnObject getTemporarySavedFiles(Map<String, Object> requestParams) throws ServiceException {
        List ll = null;
        String fileid = "";
        int dl = 0;
        try {
            if (requestParams.containsKey("fileid") && requestParams.get("fileid") != null) {
                fileid = requestParams.get("fileid").toString();
            }
            String query = "select docid,docname,doctypeid from invoicedocuments where id in (" + fileid + ")";
            ll = executeSQLQuery(query);
            dl = ll.size();
        } catch (HibernateException ex) {
            throw ServiceException.FAILURE("getTemporarySavedFiles", ex);
        }
        return new KwlReturnObject(true, "", "", ll, dl);
    }

    @Override
    public KwlReturnObject getMappedFilesResult(Map<String, Object> requestParams) throws ServiceException {
        List list = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);

            if (requestParams.containsKey("id") && requestParams.get("id") != null) {
                condition += " and id = ? ";
                params.add((String) requestParams.get("id"));
            }

            String selQuery = "select id, documentid from invoicedoccompmaptemporary where company = ?" + condition;
            list = executeSQLQuery(selQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMappedFilesResult : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject SaveUpdateObject(Object object) throws ServiceException {
        List list = new ArrayList();
        try {
            if (object != null) {
                saveOrUpdate(object);
                list.add(object);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("UpdateObject : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public void deleteTemporaryMappedFiles(String savedFilesMappingId, String companyid) throws ServiceException {
        int deletedRecords = 0;
        List params = new ArrayList();
        try {
            if (!StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(savedFilesMappingId)) {
                params.add(savedFilesMappingId);
                params.add(companyid);
                String query = "delete from invoicedoccompmaptemporary where id = ? and company = ? ";
                deletedRecords = executeSQLUpdate(query, params.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteTemporaryMappedFiles", ex);
        }
    }

    @Override
    public KwlReturnObject getDocumentIdFromMappingId(String mappingId, String companyId) throws ServiceException {
        List list = new ArrayList();
        try {
            String returnString = "";
            String query = "select documentid from invoicedoccompmaptemporary where id = ? and company = ?";
            list = executeSQLQuery(query, new Object[]{mappingId, companyId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccContractManagementDAOImpl.getDocumentIdFromMappingId:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveContractDetails(Map<String, Object> requestParam) {
        List resultlist = new ArrayList();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            MRPContractDetails contractdetails =null;

            if (requestParam.containsKey("contractdetailsid")) {
                String contractdetailsid = (String) requestParam.get("contractdetailsid");
                contractdetails = (MRPContractDetails) get(MRPContractDetails.class, contractdetailsid);
            } else {
                contractdetails = new MRPContractDetails();
            }

            if (requestParam.containsKey("mrpContractID") && requestParam.get("mrpContractID") != null) {
                contractdetails.setMrpcontract((MRPContract) get(MRPContract.class, (String) requestParam.get("mrpContractID")));
            }
            if (requestParam.containsKey("productid")) {
                contractdetails.setProduct((Product) get(Product.class, (String) requestParam.get("productid")));
            }
            //Contract Details data start
            if (requestParam.containsKey("quantity") && requestParam.get("quantity") != null) {
                contractdetails.setQuantity(Double.parseDouble((String) requestParam.get("quantity")));
            }
            if (requestParam.containsKey("desc")) {
                contractdetails.setDescription((String) requestParam.get("desc"));
            }
            if (requestParam.containsKey("uomid")) {
                contractdetails.setUom((UnitOfMeasure) get(UnitOfMeasure.class, (String) requestParam.get("uomid")));
            }
            if (requestParam.containsKey("baseuomrate") && requestParam.get("baseuomrate") != null) {
                contractdetails.setBaseuomrate(Double.parseDouble((String) requestParam.get("baseuomrate")));
            }
            if (requestParam.containsKey("baseuomquantity") && !StringUtil.isNullOrEmpty(requestParam.get("baseuomquantity").toString())) {
                contractdetails.setBaseuomquantity(Double.parseDouble((String) requestParam.get("baseuomquantity")));
            }
            if (requestParam.containsKey("rate")) {
                contractdetails.setRate(Double.parseDouble((String) requestParam.get("rate")));
            }
            if (requestParam.containsKey("discamount")) {
                contractdetails.setTotalamount(Double.parseDouble((String) requestParam.get("discamount")));
            }
            if (requestParam.containsKey("companyid")) {
                contractdetails.setCompany((Company) get(Company.class, (String) requestParam.get("companyid")));
            }
            //Contract Details data end

            //Shipment Contract data start
            if (requestParam.containsKey("deliverymode")) {
                contractdetails.setDeliverymode((MasterItem) get(MasterItem.class, (String) requestParam.get("deliverymode")));
            }
            if (requestParam.containsKey("totalnoofunit") && requestParam.get("totalnoofunit") != null) {
                contractdetails.setTotalnoofunit(Integer.parseInt((String) requestParam.get("totalnoofunit")));
            }
            if (requestParam.containsKey("totalquantity") && requestParam.get("totalquantity") != null) {
                contractdetails.setTotalquantity(Integer.parseInt((String) requestParam.get("totalquantity")));
            }
            if (requestParam.containsKey("shippingperiodfrom")) {
                Date shippingperiodfrom = df.parse((String) requestParam.get("shippingperiodfrom"));
                contractdetails.setShippingperiodfrom(shippingperiodfrom);
            }
            if (requestParam.containsKey("shippingperiodto")) {
                Date shippingperiodto = df.parse((String) requestParam.get("shippingperiodto"));
                contractdetails.setShippingperiodto(shippingperiodto);
            }
            if (requestParam.containsKey("partialshipmentallowed") && requestParam.get("partialshipmentallowed") != null) {
                contractdetails.setPartialshipmentallowed(Integer.parseInt((String) requestParam.get("partialshipmentallowed")));
            }
            if (requestParam.containsKey("shipmentstatus")) {
                contractdetails.setShipmentstatus((MasterItem) get(MasterItem.class, (String) requestParam.get("shipmentstatus")));
            }
            if (requestParam.containsKey("shippingagent")) {
                contractdetails.setShippingagent((String) requestParam.get("shippingagent"));
            }
            if (requestParam.containsKey("loadingportcountry")) {
                contractdetails.setLoadingportcountry((String) requestParam.get("loadingportcountry"));
            }
            if (requestParam.containsKey("loadingport")) {
                contractdetails.setLoadingport((String) requestParam.get("loadingport"));
            }
            if (requestParam.containsKey("transshipmentallowed") && requestParam.get("transshipmentallowed") != null) {
                contractdetails.setTransshipmentallowed(Integer.parseInt((String) requestParam.get("transshipmentallowed")));
            }
            if (requestParam.containsKey("dischargeportcountry")) {
                contractdetails.setDischargeportcountry((String) requestParam.get("dischargeportcountry"));
            }
            if (requestParam.containsKey("dischargeport")) {
                contractdetails.setDischargeport((String) requestParam.get("dischargeport"));
            }
            if (requestParam.containsKey("finaldestination")) {
                contractdetails.setFinaldestination((String) requestParam.get("finaldestination"));
            }
            if (requestParam.containsKey("postalcode")) {
                contractdetails.setPostalcode((String) requestParam.get("postalcode"));
            }
            if (requestParam.containsKey("budgetfreightcost")) {
                contractdetails.setBudgetfreightcost((String) requestParam.get("budgetfreightcost"));
            }
            if (requestParam.containsKey("shipmentcontractremarks")) {
                contractdetails.setShipmentcontractremarks((String) requestParam.get("shipmentcontractremarks"));
            }
            //Shipment Contract data end

            //Packaging Contract data start
            if (requestParam.containsKey("unitweightvalue")) {
                contractdetails.setUnitweightvalue((String) requestParam.get("unitweightvalue"));
            }

            if (requestParam.containsKey("unitweight")) {
                contractdetails.setUnitweight((String) requestParam.get("unitweight"));
            }

            if (requestParam.containsKey("packagingtype")) {
                contractdetails.setPackagingtype((String) requestParam.get("packagingtype"));
            }

            if (requestParam.containsKey("certificaterequirement") && requestParam.get("certificaterequirement") != null) {
                contractdetails.setCertificaterequirement(Integer.parseInt((String) requestParam.get("certificaterequirement")));
            }

            if (requestParam.containsKey("certificate")) {
                contractdetails.setCertificate((String) requestParam.get("certificate"));
            }

            if (requestParam.containsKey("shippingmarksdetails")) {
                contractdetails.setShippingmarksdetails((String) requestParam.get("shippingmarksdetails"));
            }

            if (requestParam.containsKey("shipmentmode")) {
                contractdetails.setShipmentmode((String) requestParam.get("shipmentmode"));
            }

            if (requestParam.containsKey("percontainerload")) {
                contractdetails.setPercontainerload((String) requestParam.get("percontainerload"));
            }

            if (requestParam.containsKey("palletmaterial")) {
                contractdetails.setPalletmaterial((String) requestParam.get("palletmaterial"));
            }

            if (requestParam.containsKey("packagingprofiletype")) {
                contractdetails.setPackagingprofiletype((MasterItem) get(MasterItem.class, (String) requestParam.get("packagingprofiletype")));
            }

            if (requestParam.containsKey("marking")) {
                contractdetails.setMarking((String) requestParam.get("marking"));
            }

            if (requestParam.containsKey("drumorbagdetails")) {
                contractdetails.setDrumorbagdetails((String) requestParam.get("drumorbagdetails"));
            }

            if (requestParam.containsKey("drumorbagsize")) {
                contractdetails.setDrumorbagsize((String) requestParam.get("drumorbagsize"));
            }

            if (requestParam.containsKey("numberoflayers")) {
                contractdetails.setNumberoflayers((String) requestParam.get("numberoflayers"));
            }

            if (requestParam.containsKey("heatingpad")) {
                contractdetails.setHeatingpad((String) requestParam.get("heatingpad"));
            }

            if (requestParam.containsKey("palletloadcontainer")) {
                contractdetails.setPalletloadcontainer((String) requestParam.get("palletloadcontainer"));
            }

            if (requestParam.containsKey("accmrpcontractdetailscustomdataref")) {
                MRPContractDetailsCustomData mRPContractDetailsCustomData = (MRPContractDetailsCustomData) get(MRPContractDetailsCustomData.class, (String) requestParam.get("accmrpcontractdetailscustomdataref"));
                contractdetails.setAccMRPContractDetailsCustomData(mRPContractDetailsCustomData);
            }
            //Packaging Contract data end

            saveOrUpdate(contractdetails);
            resultlist.add(contractdetails);
        } catch (ServiceException | SessionExpiredException | ParseException ex) {
            Logger.getLogger(AccContractManagementDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, resultlist, resultlist.size());
    }

    @Override
    public KwlReturnObject isMasterContractIDAlreadyPresent(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);

            if (requestParams.containsKey("contractid")) {
                condition = " and contractid = ? ";
                params.add(requestParams.get("contractid"));
            }

            String selQuery = "from MRPContract where company.companyID= ? " + condition;
            list = executeQuery(selQuery, params.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("Contract ID already present : " + ex.getMessage(), ex);
        }

        return new KwlReturnObject(true, null, null, list, list.size());

    }
    
    @Override
    public KwlReturnObject deleteMasterContracts(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String updateHQL = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("companyid")) {
                params.add(requestParams.get("companyid"));
            }
            if (requestParams.containsKey("id")) {
                params.add(requestParams.get("id"));
            }
            updateHQL = "update MRPContract m set deleteflag ='T' where m.company.companyID = ? and ID=?";
            numRows = executeUpdate(updateHQL, params.toArray());
    
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMasterContracts : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "MasterContracts(s) has been deleted successfully.", null, null, numRows);
    }
   
//    public KwlReturnObject deleteMasterContractsPermanently(Map<String, Object> dataMap) throws ServiceException {
//
//        ArrayList params1 = new ArrayList();
//        String delHQL1 = "", delHQL2 = "",delHQL3="",delHQL4="";
//        String id = (String) dataMap.get("id");
//        
//        int numRows1 = 0;
//        try {
//            
//            if (dataMap.containsKey("companyid")) {
//                params1.add(dataMap.get("companyid"));
//}
//            if(dataMap.containsKey("id")){
//                params1.add(dataMap.get("id"));
//
//            }
//            delHQL1 = "delete from MRPContractDocumentsMapping m where m.company.companyID = ? and m.mrpContractID.ID=?";
//            numRows1 = executeUpdate(delHQL1, params1.toArray());
//
//            delHQL2 = "delete from MRPAddressDetails m where m.mrpcontract.ID=?";
//            numRows1 = numRows1 + executeUpdate(delHQL2, id);
//
//            delHQL3 = "delete from  MRPContractDetails m where m.company.companyID = ? and m.mrpcontract.ID=?";
//            numRows1 = executeUpdate(delHQL3, params1.toArray());
//
//            
//            delHQL4 = "delete from  MRPContract m where m.company.companyID = ? and ID = ?";
//            numRows1 = numRows1 + executeUpdate(delHQL4, params1.toArray());
//
//        } catch (Exception ex) {
//            throw ServiceException.FAILURE("deleteMasterContractsPermanently : " + ex.getMessage(), ex);
//        }
//
//        return new KwlReturnObject(true, "MasterContracts(s) has been deleted successfully.", null, null, numRows1);
//
//    }
    
     /* Function to be used for: getting MRP Contract linked in Sales Order  */
    @Override
    public KwlReturnObject getMRPContractLinkedinSO(Map<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
          
            String start = (String) request.get(Constants.start);
            String limit = (String) request.get(Constants.limit);
            String conditionSQL = "";
            ArrayList params = new ArrayList();
            params.add((String) request.get(Constants.companyKey));
              if (request.containsKey("soid") && request.get("soid") != null) {
                String soid = (String) request.get("soid");
                params.add(soid);
                conditionSQL += "and salesorder.id= ?";
            }
              
            String mysqlQuery = "select  DISTINCT mrpcontract.id,2, 'false' as withoutinventory from mrpcontract "
                    + "inner join mrpcontractdetails on mrpcontractdetails.mrpcontract = mrpcontract.id  "
                    + "inner join sodetails on sodetails.mrpcontractdetailid = mrpcontractdetails.id "
                    + "inner join salesorder on sodetails.salesorder = salesorder.id where mrpcontract.company=? " + conditionSQL;

    
            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeSQLQueryPaging( mysqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMRPContractLinkedinSO:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }  
    public KwlReturnObject getParentContractID(Map<String, Object> requestParams) throws ServiceException {
        List list = Collections.EMPTY_LIST;
        List paramList = new ArrayList();
        try {
            String hql = "";
            String conditionHql = "";
            
            if (requestParams.containsKey("contractid")) {
                conditionHql += " contractMapping.mrpcontract.ID= ? ";
                paramList.add((String)requestParams.get("contractid"));
            }
            hql += " SELECT contractMapping.parentcontractid from MRPContractMapping contractMapping where " + conditionHql;
           list= executeQuery(hql, paramList.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getParentContractID", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getMasterContractRows(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            String hql = "";
            String conditionHql = "";
            if(requestParams.containsKey("mrpcontractid")){
                params.add((String) requestParams.get("mrpcontractid"));
                conditionHql += " where mcd.mrpcontract.ID = ? ";
            }
            
            hql = " from MRPContractDetails mcd " + conditionHql;
            list= executeQuery(hql, params.toArray());
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("getMasterContractRows" + ex.getMessage(), ex); 
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteMasterContractMapping(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String hql = "", msg = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("mrpContractID")) {
                params.add(requestParams.get("mrpContractID"));
            }
            hql = " delete from MRPContractMapping m where m.mrpcontract.ID = ? ";
            numRows = executeUpdate(hql, params.toArray());
            
            msg = "Master Contract mapping has been deleted successfully.";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMasterContractMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }
    
    @Override
    public KwlReturnObject deleteMasterContractDocumentMapping(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String hql = "", msg = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("mrpContractID")) {
                params.add(requestParams.get("mrpContractID"));
            }
            hql = " delete from InvoiceDocumentCompMap m where m.invoiceID = ? ";
            numRows = executeUpdate(hql, params.toArray());
            
            msg = "Master Contract document mapping has been deleted successfully.";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMasterContractMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }
    
    @Override
    public KwlReturnObject deleteBillingAddressDetails(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String hql = "", msg = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("mrpContractID")) {
                params.add(requestParams.get("mrpContractID"));
            }
            hql = " delete from MRPAddressDetails m where m.mrpcontract.ID = ? ";
            numRows = executeUpdate(hql, params.toArray());
            
            msg = "Billing Contract has been deleted successfully.";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteBillingAddressDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }
    
    @Override
    public KwlReturnObject deleteShippingAddressDetails(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String hql = "", msg = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("mrpContractDetailsID")) {
                params.add(requestParams.get("mrpContractDetailsID"));
            }
            hql = " delete from MRPAddressDetails m where m.mrpcontractdetails.ID = ? ";
            numRows = executeUpdate(hql, params.toArray());
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteShippingAddressDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }
    
    @Override
    public KwlReturnObject deleteMasterContractDetails(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String hql = "", msg = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("mrpContractID")) {
                params.add(requestParams.get("mrpContractID"));
            }
            hql = " delete from MRPContractDetails m where m.mrpcontract.ID = ? ";
            numRows = executeUpdate(hql, params.toArray());
            
            msg = "Master Contract details has been deleted successfully.";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMasterContractDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }
    
    @Override
    public KwlReturnObject deleteMasterContract(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String hql = "", msg = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("mrpContractID")) {
                params.add(requestParams.get("mrpContractID"));
            }
            hql = " delete from MRPContract m where m.ID = ? ";
            numRows = executeUpdate(hql, params.toArray());
            
            msg = "Master Contract has been deleted successfully.";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMasterContractDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }
    
    @Override
    public KwlReturnObject getMasterContractAttachments(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            String hql = "";
            String conditionHql = "";
            if(requestParams.containsKey("mrpContractID")){
                params.add((String) requestParams.get("mrpContractID"));
                conditionHql += " where m.invoiceID = ? ";
            }
            
            hql = " from InvoiceDocumentCompMap m " + conditionHql;
            list= executeQuery(hql, params.toArray());
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("getMasterContractAttachments" + ex.getMessage(), ex); 
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteMasterContractCustomData(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String hql = "", msg = "",companyId = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("companyid")) {
                companyId = requestParams.get("companyid").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("mrpContractID")) {
                params.add(requestParams.get("mrpContractID"));
            }
            hql = "delete from MRPContractCustomData m where company.companyID=? and m.MRPContract.ID=?";
            numRows = executeUpdate(hql, params.toArray());

            msg = "Master Contract Custom Data has been deleted successfully.";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMasterContractCustomDataDetails : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }

    @Override
    public KwlReturnObject deleteMasterContractDetailsCustomData(Map<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        ArrayList params1 = new ArrayList();
        String ids = "", msg = "", companyId = "", sql = "";
        int numRows = 0;
        try {
            if (requestParams.containsKey("companyid")) {
                companyId = requestParams.get("companyid").toString();
                params.add(companyId);
            }
            if (requestParams.containsKey("mrpContractID")) {
                params.add(requestParams.get("mrpContractID"));
            }
            sql = "select id from mrpcontractdetails where company=? and mrpcontract=?";
            List id = executeSQLQuery(sql, params.toArray());
            Iterator itr= id.iterator();
            while (itr.hasNext()) {
                String cdid = itr.next().toString();
                ids += "'" + cdid + "',";
            }
            if (!StringUtil.isNullOrEmpty(ids)) {
                ids = ids.substring(0, ids.length() - 1);
            }

            sql = "delete from mrpcontractdetailscustomdata where contractDetailsId in(" + ids + ")";
            numRows = executeSQLUpdate(sql);

            msg = "Master Contract Details Custom Data has been deleted successfully.";
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteMasterContractDetailsCustomData : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, msg, null, null, numRows);
    }
}
