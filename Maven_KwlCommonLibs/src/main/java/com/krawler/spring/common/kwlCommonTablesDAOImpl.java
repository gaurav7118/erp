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

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karthik
 */
public class kwlCommonTablesDAOImpl extends BaseDAO implements kwlCommonTablesDAO {

    public KwlReturnObject getObject(String classpath, String id) throws ServiceException {
        List list = new ArrayList();
        try {
            Class cls = Class.forName(classpath);
            Object obj = get(cls, id);
            list.add(obj);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(kwlCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public Object getClassObject(String classpath, String id) throws ServiceException {
        Object obj = null;
        try {
            Class cls = Class.forName(classpath);
            obj = get(cls, id);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(kwlCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return obj;
    }

    public Object getRequestedObjectFields(Class classname, String[] columnNames, Map<String, Object> paramMap) throws ServiceException {
        return executeQueryWithProjection(classname, columnNames, paramMap);
    }
    
    /**
     * This method will populate required information(fetchColumn) from provided table(tableName),
     * company(companyColumn), condition(condtionColumn). 
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject populateMasterInformation(Map<String, String> requestParams) throws ServiceException {
        List data = Collections.emptyList();
        try {
            ArrayList params = new ArrayList();
            StringBuilder hqlQuery = new StringBuilder();
            
            if (requestParams.containsKey("fetchColumn") && !StringUtil.isNullOrEmpty(requestParams.get("fetchColumn"))) {
                hqlQuery.append(" select " + requestParams.get("fetchColumn") + " ");
            }
            
            if (requestParams.containsKey("tableName") && !StringUtil.isNullOrEmpty(requestParams.get("tableName"))) {
                hqlQuery.append(" from " + requestParams.get("tableName") + " ");
            }
            
            if (requestParams.containsKey("companyColumn") && !StringUtil.isNullOrEmpty(requestParams.get("companyColumn"))) {
                hqlQuery.append(" where " + requestParams.get("companyColumn") + "= ? ");
                params.add(requestParams.get(Constants.companyKey));
            }

            if (requestParams.containsKey("condtionColumn") && !StringUtil.isNullOrEmpty(requestParams.get("condtionColumn"))) {
                hqlQuery.append(" and " + requestParams.get("condtionColumn") + "= ? ");
                params.add(requestParams.get("condtionColumnvalue"));
            }
            data = executeQuery(hqlQuery.toString(), params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccountingHandlerDAOImpl.populateMasterInformation:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, data, data.size());
    }
    
    @Override
    public List getRequestedObjectFieldsInCollection(Class c, String[] columnNames, Map<String, Object> paramMap) throws ServiceException {
        return executeCollectionQueryWithProjections(c, columnNames, paramMap);
    }
    
    public KwlReturnObject getAllTimeZones() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from KWLTimeZone order by (sortOrder*1)";
            ll = executeQuery(query);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getAllTimeZones", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAllCurrencies(Map<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        String conditionhql="";
        ArrayList params = new ArrayList();
        boolean isquicksearch=false;
        try {
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String ss = requestParams.get("ss").toString();
                ss = ss.replaceAll("%", "////");		// issue for search
                ss = ss.replaceAll("_", "////");
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"curr.name"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "where", searchcol);
                    conditionhql += searchQuery;
                    isquicksearch=true;
                }
            }
            String query = "from KWLCurrency curr " + conditionhql;
            if (!isquicksearch) {
                ll = executeQuery(query);
            } else {
                ll = executeQuery(query, params.toArray());
            }
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getAllCurrencies", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAllCountries() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from Country order by countryName";
            ll = executeQuery(query);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getAllCountries", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
    
    public KwlReturnObject getAllStates(String countryid) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from State where country = "+ countryid +" order by stateName";
            ll = executeQuery(query);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getAllStates", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getSubdomainListFromCountry(String countryid) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from Company where country.ID = "+ countryid +" and deleted = 0 and subDomain != 'admin' order by subDomain";
            ll = executeQuery(query);
            dl = ll.size();
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("kwlCommonTablesDAOImpl.getSubdomainListFromCountry", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAllDateFormats(Map<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String query = "from KWLDateFormat";
            if (requestParams.containsKey("formatids")) {
                String ids = (String) requestParams.get("formatids");
                query += " where formatID in (" + ids + ")";
            }
            ll = executeQuery(query);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getCompanyInformation", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public DateFormat getUserDateFormatter(String dateFormatId, String userTimeFormatId, String timeZoneDiff) throws ServiceException {
        SimpleDateFormat sdf = null;
        try {
            KWLDateFormat df = (KWLDateFormat) load(KWLDateFormat.class, dateFormatId);
            String dateformat = "";
            if (userTimeFormatId.equals("1")) {
                dateformat = df.getJavaForm().replace('H', 'h');
                if (!dateformat.equals(df.getJavaForm())) {
                    dateformat += " a";
                }
            } else {
                dateformat = df.getJavaForm();
            }
            sdf = new SimpleDateFormat(dateformat);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT" + timeZoneDiff));
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.getUserDateFormatter", e);
        }
        return sdf;
    }

    @Deprecated
    public String currencyRender(String currency, String currencyid) throws SessionExpiredException {
        KWLCurrency cur = (KWLCurrency) load(KWLCurrency.class, currencyid);
        String symbol = cur.getHtmlcode();
        char temp = (char) Integer.parseInt(symbol, 16);
        symbol = Character.toString(temp);
        float v = 0;
        String str = authHandler.getCompleteDFStringForAmount("#,##0.");
        DecimalFormat decimalFormat = new DecimalFormat(str);
        if (currency.equals("")) {
            return symbol;
        }
        v = Float.parseFloat(currency);
        String fmt = decimalFormat.format(v);
        fmt = symbol + fmt;
        return fmt;
    }
    
    public String currencyRender(String currency, String currencyid, String companyid) throws SessionExpiredException {
        KWLCurrency cur = (KWLCurrency) load(KWLCurrency.class, currencyid);
        String symbol = cur.getHtmlcode();
        char temp = (char) Integer.parseInt(symbol, 16);
        symbol = Character.toString(temp);
        float v = 0;
        String str = authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
        DecimalFormat decimalFormat = new DecimalFormat(str);
        if (currency.equals("")) {
            return symbol;
        }
        v = Float.parseFloat(currency);
        String fmt = decimalFormat.format(v);
        fmt = symbol + fmt;
        return fmt;
    }

    public KwlReturnObject getEditHelpComponent(HashMap requestMap) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            ArrayList al = new ArrayList();
            String modname = (String) requestMap.get("modname");
            al.add(modname);
            String query = "select c from EditHelp c where c.modeid= ? order by (c.id * 1)";
            ll = executeQuery(query, al.toArray());
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("kwlCommonTablesDAOImpl.getEditHelpComponent", e);
        }
        return new KwlReturnObject(true, "", "", ll, dl);
    }
    public String getCompanyId(String subdomain) throws ServiceException {
        List l = null;
        String company = "";
        try {
            String query = "select companyID from Company where subDomain=?";
            l = executeQuery(query, subdomain);
            company = l.get(0).toString();
        } catch (Exception e) {
        }
        return company;
    }
    //This method returns Timezone ID associated to specified country
    public String getCountryTimezoneID(String countryid) throws ServiceException {
        List l = null;
        String timezone = "";
        try {
            String query = "SELECT timezoneid FROM Country WHERE ID=?";
            l = executeQuery(query, countryid);
            timezone = l.get(0)!=null?l.get(0).toString():"23"; //Default Mountain Arizona : GMT-7:00 (USA)
        } catch (Exception e) {
            throw ServiceException.FAILURE("kwlCommonTablesDAOImpl.getCountryTimezone", e);
        }
        return timezone;
    }
    
    //This method updates Timezone of a User(s) associated to specified company
    public void updateCompanyUsersTimezone(String companyid) throws ServiceException {
        int update = 0;
        try {
            String query = "UPDATE User SET timeZone = ( SELECT timeZone from Company WHERE companyID = '" + companyid + "') WHERE company.companyID = '"+companyid+"'";
            update = executeUpdate(query);
            if(update > 0){
                System.out.println(update +" - no. of user's timezone updated");
            } else {
                System.out.println("0 - no. of user's timezone updated");
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("kwlCommonTablesDAOImpl.updateCompanyUsersTimezone", e);
        }
    }
    
    @Override
    public void saveObj(Object obj) throws ServiceException {
        save(obj);
    }

    @Override
    public void saveorUpdateObj(Object obj) throws ServiceException {
        saveOrUpdate(obj);
    }

    @Override
    public void evictObject(Object obj) throws ServiceException {
        evictObj(obj);
    }
    
    @Override
    public KwlReturnObject getSampleFileDataList(HashMap<String,Object> map) throws ServiceException {
        
        String id="", moduleName = "", countryid="",query="", fileName="";
        
        ArrayList params=new ArrayList();
        ArrayList params1=new ArrayList();
        
        if (map.containsKey("moduleName") && map.get("moduleName") != null) {
            moduleName = (String) map.get("moduleName");
            params.add(moduleName);
            params1.add(moduleName);
        }
        if (map.containsKey("filename") && map.get("filename") != null) {
            fileName = (String) map.get("filename");
        }

        if (fileName.equals(Constants.sampleAssemblyProductWithoutBOM)) {  //For without BOM File
            if (map.containsKey("id") && map.get("id") != null) {
                id = (String) map.get("id");
                params.add(id);
                query = "select header_json,filedata_json,samplefilename,version,issamplefile from samplefiledata where modulename =? and countryid='0' AND id = ?";
            }

        } else {

            if (map.containsKey("countryid") && map.get("countryid") != null) {
                countryid = (String) map.get("countryid");
                params.add(countryid);
            } else {
                params.add(String.valueOf(0));
            }

            query = "select header_json,filedata_json,samplefilename,version,issamplefile from samplefiledata where modulename =? and countryid=?";            
        }

        List list = executeSQLQuery(query,params.toArray());
        
        if (list.isEmpty()) {// Here we first execute query with specific contryid, which comes in parameter. If data is not found for that  special country then we fetched common sample file with countryid 0
            params1.add(String.valueOf(0));
            list = executeSQLQuery(query, params1.toArray());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getIsCurrenyCodeAndIsActivatedTodate(String companyId) throws ServiceException {
         List list = null;
         String query = "select currencyCode,activateToDateforExchangeRates from ExtraCompanyPreferences where id=?";
         list = executeQuery(query, companyId);
         return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getLandingCostCategoryStore(String companyid) throws ServiceException{
        List list = null;
        String query = "from LandingCostCategory Where company.companyID = ?";
        list = executeQuery(query,new Object[]{companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public List getCommonQueryForTermsInLinking(String tableName,String ids) throws ServiceException{
        List list = null;
        String tableNameQuery = "",query = "", subQuery = "";
        
        String[] idsArray = (String[]) ids.split(",");
        
        String transactionIds = " ( ";
        for (int i = 0; i < idsArray.length; i++) {
            transactionIds += "'" + idsArray[i] + "',";
        }
        transactionIds = transactionIds.substring(0, transactionIds.length() - 1) + ") ";
        
        if(tableName.equals(Constants.invoicetermsmap)){
            tableNameQuery = Constants.invoicetermsmap;
            subQuery = " invoice IN " + transactionIds;
        }else if(tableName.equals(Constants.receipttermsmap)){
            tableNameQuery = Constants.receipttermsmap;
            subQuery = " goodsreceipt IN " + transactionIds;
        }else if(tableName.equals(Constants.vendorquotationtermmap)){
            tableNameQuery = Constants.vendorquotationtermmap;
            subQuery = " vendorquotation IN "+ transactionIds;
        }else if(tableName.equals(Constants.quotationtermmap)){
            tableNameQuery = Constants.quotationtermmap;
            subQuery = " quotation IN "+ transactionIds;
        }else if(tableName.equals(Constants.goodsreceiptordertermmap)){
            tableNameQuery = Constants.goodsreceiptordertermmap;
            subQuery = " goodsreceiptorder IN "+ transactionIds;
        }else if(tableName.equals(Constants.deliveryordertermmap)){
            tableNameQuery = Constants.deliveryordertermmap;
            subQuery = " deliveryorder IN "+ transactionIds;
        }else if(tableName.equals(Constants.purchaseordertermmap)){
            tableNameQuery = Constants.purchaseordertermmap;
            subQuery = " purchaseorder IN "+ transactionIds;
        }else if(tableName.equals(Constants.salesordertermmap)){
            tableNameQuery = Constants.salesordertermmap;
            subQuery = " salesorder IN "+ transactionIds;
        }else if(tableName.equals(Constants.purchasereturntermsmap)){
            tableNameQuery = Constants.purchasereturntermsmap;
            subQuery = " purchasereturn IN "+ transactionIds;
        }else if(tableName.equals(Constants.salesreturntermsmap)){
            tableNameQuery = Constants.salesreturntermsmap;
            subQuery = " salesreturn IN "+ transactionIds;
        }
        
        query = "SELECT term,sum(termamount),termTax,sum(termTaxamount) FROM " + tableNameQuery + " WHERE " + subQuery + " GROUP BY term,termTax ";
        
        list = executeSQLQuery(query);
        return list;
    }
    
    @Override
    public List getPercentAndTaxNameFromTaxid(String taxid,String companyid) throws ServiceException{
        List list = null;
        String query = "SELECT tl.percent,t.name FROM tax t INNER JOIN taxlist tl on tl.tax=t.id WHERE tl.tax=? AND t.company=? ";
        list = executeSQLQuery(query,new Object[]{taxid,companyid});
        return list;
    }
    
    @Override
    public List getSummationOfTermAmtAndTermTaxAmt(String tableName,String transactionId) throws ServiceException{
        List list = null;
        String tableNameQuery = "";
        String query = "", subQuery = "";
        if(tableName.equals(Constants.invoicetermsmap)){
            tableNameQuery = Constants.invoicetermsmap;
            subQuery = " invoice=? ";
        }else if(tableName.equals(Constants.receipttermsmap)){
            tableNameQuery = Constants.receipttermsmap;
            subQuery = " goodsreceipt=? ";
        }else if(tableName.equals(Constants.vendorquotationtermmap)){
            tableNameQuery = Constants.vendorquotationtermmap;
            subQuery = " vendorquotation=? ";
        }else if(tableName.equals(Constants.quotationtermmap)){
            tableNameQuery = Constants.quotationtermmap;
            subQuery = " quotation=? ";
        }else if(tableName.equals(Constants.goodsreceiptordertermmap)){
            tableNameQuery = Constants.goodsreceiptordertermmap;
            subQuery = " goodsreceiptorder=? ";
        }else if(tableName.equals(Constants.deliveryordertermmap)){
            tableNameQuery = Constants.deliveryordertermmap;
            subQuery = " deliveryorder=? ";
        }else if(tableName.equals(Constants.purchaseordertermmap)){
            tableNameQuery = Constants.purchaseordertermmap;
            subQuery = " purchaseorder=? ";
        }else if(tableName.equals(Constants.salesordertermmap)){
            tableNameQuery = Constants.salesordertermmap;
            subQuery = " salesorder=? ";
        }else if(tableName.equals(Constants.purchasereturntermsmap)){
            tableNameQuery = Constants.purchasereturntermsmap;
            subQuery = " purchasereturn=? ";
        }else if(tableName.equals(Constants.salesreturntermsmap)){
            tableNameQuery = Constants.salesreturntermsmap;
            subQuery = " salesreturn=? ";
        }
        query = "select sum(termamount),sum(termamountinbase),sum(termTaxamount),sum(termTaxamountinbase),sum(termamountexcludingtax),sum(termamountexcludingtaxinbase) from " + tableNameQuery + " where " + subQuery;
        list = executeSQLQuery(query,new Object[]{transactionId});
        return list;
    }
    
    @Override
    public Map<String, Object[]> getSummationOfTermAmtAndTermTaxAmtList(String tableName,String transactionIdList) throws ServiceException{
        Map<String, Object[]> map = new HashMap<String, Object[]>();
        List list = null;
        String tableNameQuery = "";
        String query = "", subQuery = "", attribute="";
        if(tableName.equals(Constants.invoicetermsmap)){
            tableNameQuery = Constants.invoicetermsmap;
            subQuery = " invoice in (?) ";
            attribute = " invoice";
        }else if(tableName.equals(Constants.receipttermsmap)){
            tableNameQuery = Constants.receipttermsmap;
            subQuery = " goodsreceipt in (?) ";
            attribute = " goodsreceipt";
        }else if(tableName.equals(Constants.vendorquotationtermmap)){
            tableNameQuery = Constants.vendorquotationtermmap;
            subQuery = " vendorquotation in (?) ";
            attribute = " vendorquotation";
        }else if(tableName.equals(Constants.quotationtermmap)){
            tableNameQuery = Constants.quotationtermmap;
            subQuery = " quotation in (?) ";
            attribute = " quotation";
        }else if(tableName.equals(Constants.goodsreceiptordertermmap)){
            tableNameQuery = Constants.goodsreceiptordertermmap;
            subQuery = " goodsreceiptorder in (?) ";
            attribute = " goodsreceiptorder";
        }else if(tableName.equals(Constants.deliveryordertermmap)){
            tableNameQuery = Constants.deliveryordertermmap;
            subQuery = " deliveryorder in (?) ";
            attribute = " deliveryorder";
        }else if(tableName.equals(Constants.purchaseordertermmap)){
            tableNameQuery = Constants.purchaseordertermmap;
            subQuery = " purchaseorder in (?) ";
            attribute = " purchaseorder";
        }else if(tableName.equals(Constants.salesordertermmap)){
            tableNameQuery = Constants.salesordertermmap;
            subQuery = " salesorder in (?) ";
            attribute = " salesorder";
        }else if(tableName.equals(Constants.purchasereturntermsmap)){
            tableNameQuery = Constants.purchasereturntermsmap;
            subQuery = " purchasereturn in (?) ";
            attribute = " purchasereturn";
        }else if(tableName.equals(Constants.salesreturntermsmap)){
            tableNameQuery = Constants.salesreturntermsmap;
            subQuery = " salesreturn in (?) ";
            attribute = " salesreturn";
        }
        
        query = "select "+attribute+", sum(termamount),sum(termamountinbase),sum(termTaxamount),sum(termTaxamountinbase),sum(termamountexcludingtax),sum(termamountexcludingtaxinbase) from " + tableNameQuery + " where " + subQuery +" group by "+attribute;
        list = executeSQLQuery(query,new Object[]{transactionIdList});
        
        for (Object value : list) {
            Object[] items = (Object[])value;
            map.put((String)items[0], items);
        }
        
        return map;
    }
    
}
