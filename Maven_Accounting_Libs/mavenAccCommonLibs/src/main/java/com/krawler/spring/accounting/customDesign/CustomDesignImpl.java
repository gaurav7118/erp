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
package com.krawler.spring.accounting.customDesign;

import com.krawler.common.admin.CustomerAddressDetails;
import com.krawler.common.admin.VendorAddressDetails;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.admin.Company;
import com.sun.corba.se.impl.orbutil.closure.Constant;

/**
 *
 * @author krawler
 */
public class CustomDesignImpl extends BaseDAO implements CustomDesignDAO {

    public KwlReturnObject getDesignTemplateList(String companyid, int moduleid, String ss, String start, String limit,String isActive, String countryid, String stateid, String sort,String dir) throws ServiceException {
        List ll = new ArrayList();
        int count=0;
        ArrayList paramslist = new ArrayList();
        try {            
            String conditionSql = "";
            int country = 0;
            paramslist.add(companyid);
            paramslist.add(Constants.defaultTemplateCompanyid);
            if ( !(moduleid == 0) ) {
                paramslist.add(moduleid);
            }
            
            if (!StringUtil.isNullOrEmpty(ss))
            {
                String[] searchcol = new String[]{"customdesigntemplate.templatename"};
                Map map = StringUtil.insertParamSearchStringMap(paramslist, ss, 1);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and",searchcol);
                conditionSql =searchQuery;
            }
            if (!StringUtil.isNullOrEmpty(isActive) && !(isActive.equalsIgnoreCase("all"))) {
                conditionSql += " and customdesigntemplate.isdefault=" + isActive;
            }
            if(!StringUtil.isNullOrEmpty(countryid)){
                country = Integer.parseInt(countryid);
                conditionSql += " and ( customdesigntemplate.countryid is null or customdesigntemplate.countryid = '' or customdesigntemplate.countryid=" + countryid +" ) ";
            }
            if(country == Constants.indian_country_id ){
                if(!StringUtil.isNullOrEmpty(stateid)){
                    conditionSql += " and ( customdesigntemplate.stateid is null or customdesigntemplate.stateid = '' or customdesigntemplate.stateid=" + stateid+" ) ";
                } else{
                    conditionSql += " and ( customdesigntemplate.stateid is null or customdesigntemplate.stateid = '' ) ";
                }
            }
            String query = "select id, templatename, concat(users.fname,' ',users.lname), DATE(customdesigntemplate.createdon), isdefault, moduleid, templatesubtype, isnewdesign, isdefaulttemplate, DATE(customdesigntemplate.updatedon) from customdesigntemplate "
                    + " inner join users on users.userid=customdesigntemplate.createdby where (customdesigntemplate.company=? or customdesigntemplate.company=?) " ;
            if ( !(moduleid == 0) ) {
                query += " and moduleid = ? ";
            }
            query += conditionSql;
            if (!StringUtil.isNullOrEmpty(sort) && !StringUtil.isNullOrEmpty(dir)) {
                query += " order by customdesigntemplate." + sort + " " + dir;
            } else {
                query += " order by customdesigntemplate.templatename";
            }
            ll = executeSQLQuery(query, paramslist.toArray());
            count=ll.size();
            if(!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)){
                ll = executeSQLQueryPaging(query, paramslist.toArray(),new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getDesignTemplateList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, count);
    }

    public KwlReturnObject getDesignTemplate(String templateid) throws ServiceException {
        List ll = new ArrayList();
        try {
            String query = "select html,json, replace(html, 'border-color: rgb(181, 184, 200); border-style: solid; border-width: 1px;', '') as 'formathtml', "
                    + "sqlquery , pagelayoutproperty , replace(pagefooterhtml, 'border-color: rgb(181, 184, 200); border-style: solid; border-width: 1px;', '') as 'pagefooterhtml',"
                    + " pagefooterjson , pagefootersqlquery , templatesubtype , replace(pageheaderhtml, 'border-color: rgb(181, 184, 200); border-style: solid; border-width: 1px;', '') as 'pageheaderhtml',"
                    + " pageheaderjson , pageheadersqlquery , footerheader, isnewdesign, isdefaulttemplate , templatename from customdesigntemplate where id =?";
            ll = executeSQLQuery(query, new Object[]{templateid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getDesignTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
    public KwlReturnObject getNewDesignTemplate(String companyid, String templateid, boolean isPreview) throws ServiceException {
        List ll = new ArrayList();
        try {
            if(isPreview){
                String query = "select html, json, sqlquery , pagelayoutproperty , pagefooterhtml ,"
                        + " pagefooterjson , pagefootersqlquery , pageheaderhtml,"
                        + " pageheaderjson , pageheadersqlquery , footerheader from previewcustomdesigntemplate where company = ?";
                ll = executeSQLQuery(query, new Object[]{companyid});
            } else{
                String query = "select html, json, sqlquery , pagelayoutproperty , pagefooterhtml ,"
                        + " pagefooterjson , pagefootersqlquery , templatesubtype , pageheaderhtml,"
                        + " pageheaderjson , pageheadersqlquery , footerheader, isnewdesign, isdefaulttemplate from customdesigntemplate where company in (?,'1') and id =?";
                ll = executeSQLQuery(query, new Object[]{companyid, templateid});
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getDesignTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

    public KwlReturnObject getModuleDefaultTemplate(String companyid, int moduleid) throws ServiceException {
        List ll = new ArrayList();
        try {
            String query = "select html,json, replace(replace(html, 'border-color: rgb(181, 184, 200); border-style: solid; border-width: 1px;', ''),'border-color:#B5B8C8;border-style:solid;border-width:1px;','') as 'formathtml', sqlquery from customdesigntemplate where company=? and moduleid =? and isdefault=1";
            ll = executeSQLQuery(query, new Object[]{companyid, moduleid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getModuleDefaultTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
     public KwlReturnObject getCompanyDetails(String companyid) throws ServiceException {
        List ll = new ArrayList();
        try {
            String query = "select quantitydigitafterdecimal,amountdigitafterdecimal,unitpricedigitafterdecimal,uomconversionratedigitafterdecimal from compaccpreferences where id=? ";
            ll = executeSQLQuery(query, new Object[]{companyid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getCompanyDetails", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

//    public KwlReturnObject resetModuleDefaultTemplate(String companyid, int moduleid) throws ServiceException {
//        List ll = new ArrayList();
//        try {
//            String query = "update customdesigntemplate set isdefault = 0 where company=? and moduleid =? and isdefault=1";
//            executeSQLUpdate(query, new Object[]{companyid,moduleid});
//         } catch (Exception e) {
//            throw ServiceException.FAILURE("CustomDesignImpl.resetModuleDefaultTemplate", e);
//        }
//        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
//    }
 @Override    
    public KwlReturnObject getDefaultHeaders(String moduleid, String companyid) throws ServiceException {
        List ll = new ArrayList();
        try {
            /**
             * To get default as well as country specific entries from default headers
             */
            String countryid = "";
            String conditionQuery = "";
            Company company = (Company) get(Company.class, companyid);
            if (company != null) {
                countryid = company.getCountry().getID();
            }
            if (!StringUtil.isNullOrEmpty(countryid)) {
                conditionQuery = " AND (countryid = 0 OR countryid = " + countryid + ")";
            } else {
                conditionQuery = " AND countryid = 0";
            }
            String query = "SELECT id, defaultHeader, dbcolumnname,reftablename, reftablefk, reftabledatacolumn, dummyvalue, xtype, allowincustomtemplate "
                    + "FROM default_header WHERE module = ? AND islineitem = ? AND allowindocumentdesigner = ? " + conditionQuery;
            ll = executeSQLQuery(query, new Object[]{moduleid, 'F', 'T'});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getDefaultHeaders", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
@Override 
    public KwlReturnObject getDummyValue(String fieldIDS) throws ServiceException {
        List ll = new ArrayList();
        try {
            String query = "select defaultHeader, dummyvalue,xtype from default_header where id in (" + fieldIDS + ")";
            ll = executeSQLQuery(query);
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getDummyValue", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

 @Override    
    public KwlReturnObject saveDesignTemplate(String companyid, String userid, String templateid, String templatename, int moduleid, String json, String html, String sqlquery, String pagelayoutproperty, String bandID) throws ServiceException {
        List ll = new ArrayList();
        try {
            // update entry
            String query;
            if (!StringUtil.isNullOrEmpty(templateid)) {
                if (bandID.equals("2")) {
                    query = "update customdesigntemplate set pagefooterhtml=? , pagefooterjson=?, pagefootersqlquery=? where id = ?";
                    executeSQLUpdate(query, new Object[]{html, json, sqlquery, templateid});
                } else if (bandID.equals("3")) {
                    query = "update customdesigntemplate set pageheaderhtml=? , pageheaderjson=?, pageheadersqlquery=? where id = ?";
                    executeSQLUpdate(query, new Object[]{html, json, sqlquery, templateid});
                } else {
                    query = "update customdesigntemplate set html=?, json=?, sqlquery=?, pagelayoutproperty=? where id = ?";
                    executeSQLUpdate(query, new Object[]{html, json, sqlquery, pagelayoutproperty, templateid});
                }
            } else {
                templateid = java.util.UUID.randomUUID().toString();
                query = "insert into customdesigntemplate(id,templatename,moduleid,createdby,createdon,company,isdefault,html,json,sqlquery,pagelayoutproperty) values(?,?,?,?,NOW(),?,?,?,?,?,?)";
                executeSQLUpdate(query, new Object[]{templateid, templatename, moduleid, userid, companyid, 0, html, json, sqlquery, pagelayoutproperty});
                }
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.saveDesignTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
 @Override 
    //Save As
    public KwlReturnObject saveAsDesignTemplate(String companyid,String userid,String templateid,String templatename,int moduleid,String json,String html,String sqlquery,String pagelayoutproperty,String pagefooterhtml,String pagefooterjson,String pagefootersqlquery,String pageheaderhtml,String pageheaderjson,String pageheadersqlquery,int footerheader,String templatesubtype)throws ServiceException {
        List ll = new ArrayList();
        try {
            // update entry
            String query="";
                templateid = java.util.UUID.randomUUID().toString();
            query = "insert into customdesigntemplate(id,templatename,moduleid,createdby,createdon,company,isdefault,html,json,sqlquery,pagelayoutproperty,pagefooterjson,pagefooterhtml,pagefootersqlquery,pageheaderjson,pageheaderhtml,pageheadersqlquery,footerheader,templatesubtype) values(?,?,?,?,NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            executeSQLUpdate(query, new Object[]{templateid, templatename, moduleid, userid, companyid, 0, html, json, sqlquery, pagelayoutproperty,pagefooterjson,pagefooterhtml,pagefootersqlquery,pageheaderjson,pageheaderhtml,pageheadersqlquery,footerheader,templatesubtype});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.saveDesignTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

//    public KwlReturnObject checkIsDefaultTemplateSet(String companyid, int moduleid, String templateid) throws ServiceException {
//        List ll = new ArrayList();
//        try {
//            String query = "select * from customdesigntemplate where moduleid=? and company=? and id !=? and isdefault=1";
//            ll = executeSQLQuery(query, new Object[]{moduleid, companyid, templateid});
//         } catch (Exception e) {
//            throw ServiceException.FAILURE("CustomDesignImpl.checkIsDefaultTemplateSet", e);
//        }
//        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
//    }
 @Override 
    public KwlReturnObject createTemplate(String companyid, String userid, int moduleid, String templatename, String templatesubtype) throws ServiceException {
        List ll = new ArrayList();
        try {
            // insert new entry
                String id = java.util.UUID.randomUUID().toString();
                String query = "insert into customdesigntemplate(id, templatename, moduleid, createdby, createdon, updatedon, company, isdefault, templatesubtype) values (?, ?, ?, ?, NOW(), NOW(), ?, ?, ?)";
                executeSQLUpdate(query, new Object[]{id, templatename, moduleid, userid, companyid, 0, templatesubtype});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.createTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
 @Override 
    public KwlReturnObject copyTemplate(String companyid, String userid, int moduleid, String templatename, String templatesubtype,String html,String json,String pagelayoutproperty,String pagefooterhtml,String pagefooterJSON,String pageheaderhtml,String pageheaderJSON,String sqlquery,String pagefootersqlquery,String pageheadersqlquery,String footerheader,String isnewdesign) throws ServiceException {
        List ll = new ArrayList();
        try {
            // insert new entry
                String id = java.util.UUID.randomUUID().toString();
                String query = "insert into customdesigntemplate(id, templatename, moduleid, createdby, createdon, updatedon, company, isdefault, templatesubtype, html, json, pagelayoutproperty, pagefooterhtml, pagefooterjson, pageheaderhtml, pageheaderjson, sqlquery, pagefootersqlquery, pageheadersqlquery, footerheader, isnewdesign, isdefaulttemplate) values (?, ?, ?, ?, NOW(), NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                executeSQLUpdate(query, new Object[]{id, templatename, moduleid, userid, companyid, 1, templatesubtype, html, json, pagelayoutproperty, pagefooterhtml, pagefooterJSON, pageheaderhtml, pageheaderJSON, sqlquery,pagefootersqlquery,pageheadersqlquery, footerheader, isnewdesign, 0 });
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.createTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

 @Override 
    public boolean isDuplicateTemplate(String companyid, int moduleid, String templatename) {
        List ll = new ArrayList();
        boolean isDuplicate = false;
        try {
            String query = "";
            query = "select * from customdesigntemplate where templatename=? and moduleid=? and company=? ";
            ll = executeSQLQuery(query, new Object[]{templatename, moduleid, companyid});
            if (!ll.isEmpty()) {
                isDuplicate = true;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(CustomDesignImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isDuplicate;
    }
    @Override
    public KwlReturnObject getCustomerDetails(String customerid) throws ServiceException {
       List ll = new ArrayList();
       List addressll = new ArrayList();
       List masterItem = new ArrayList();
       List returnll = new ArrayList();
       CustomerAddressDetails addressBillingDetails = null;
       CustomerAddressDetails addressShippingDetails = null;
       String termName = "";
       String customerGSTIN = "";
       try {
           String query = "", addressQuery="",masterDetailsQuery ="";
           query = "select c.acccode,c.name,c.aliasname,c.contactno,c.uennumber,c.income,c.gstregistrationnumber,ct.termdays,c.creditlimit,cu.currencycode,cu.name as curname,cu.symbol,ct.termname,c.gstin as customerGSTIN"
                   + " from customer c, currency cu, creditterm ct where c.currency=cu.currencyid and c.creditterm = ct.termid and c.id = ?";
                      
           ll = executeSQLQuery(query, new Object[]{customerid});
           Iterator ite = ll.iterator();
           while(ite.hasNext()){
               Object[] obj = (Object[]) ite.next();
               returnll.add(0, obj[0]);
               returnll.add(1, obj[1]);
               returnll.add(2, obj[2]);
               returnll.add(3, obj[3]);
               returnll.add(4, obj[4]);
               returnll.add(5, obj[5]);
               returnll.add(6, obj[6]);
               returnll.add(7, obj[7]);
               returnll.add(8, obj[8]);
               returnll.add(9, obj[9]);
               returnll.add(10, obj[10]);
               returnll.add(11, obj[11]);
               
               termName = String.valueOf(obj[12]);
               customerGSTIN = obj[13] == null ? "" : String.valueOf(obj[13]);
           }
//           addressQuery = "select id from customeraddressdetails where isdefaultaddress='T' AND customerid='"+customerid+"'";
//           addressll = executeSQLQuery(addressQuery);
//           if(!addressll.isEmpty()){
//               addressBillingDetails = (CustomerAddressDetails) get(CustomerAddressDetails.class, addressll.get(0).toString());
//               addressShippingDetails = (CustomerAddressDetails) get(CustomerAddressDetails.class, addressll.get(1).toString());
//           }
           addressQuery = "FROM CustomerAddressDetails WHERE isDefaultAddress=TRUE AND customerID='" + customerid + "'";
           addressll = executeQuery(addressQuery);
           if (!addressll.isEmpty()) {
               for (Object obj : addressll) {
                   CustomerAddressDetails customerAddressDetails = (CustomerAddressDetails) obj;
                   if (customerAddressDetails.isIsBillingAddress()) {
                       addressBillingDetails = customerAddressDetails;
                   } else {
                       addressShippingDetails = customerAddressDetails;
                   }
               }
           }
           returnll.add(12, addressBillingDetails != null? addressBillingDetails.getAddress() : "");
           returnll.add(13, addressBillingDetails != null? addressBillingDetails.getCity() : "");
           returnll.add(14, addressBillingDetails != null? addressBillingDetails.getState() : "");
           returnll.add(15, addressBillingDetails != null? addressBillingDetails.getCountry() : "");
           returnll.add(16, addressBillingDetails != null? addressBillingDetails.getPostalCode() : "");
           returnll.add(17, addressBillingDetails != null? addressBillingDetails.getPhone() : "");
           returnll.add(18, addressBillingDetails != null? addressBillingDetails.getMobileNumber() : "");
           returnll.add(19, addressBillingDetails != null? addressBillingDetails.getFax() : "");
           returnll.add(20, addressBillingDetails != null? addressBillingDetails.getEmailID() : "");
           returnll.add(21, addressBillingDetails != null? addressBillingDetails.getContactPerson() : "");
           returnll.add(22, addressBillingDetails != null? addressBillingDetails.getContactPersonNumber() : "");
           returnll.add(23, addressShippingDetails != null? addressShippingDetails.getAddress() : "");
           returnll.add(24, addressShippingDetails != null? addressShippingDetails.getCity() : "");
           returnll.add(25, addressShippingDetails != null? addressShippingDetails.getState() : "");
           returnll.add(26, addressShippingDetails != null? addressShippingDetails.getCountry() : "");
           returnll.add(27, addressShippingDetails != null? addressShippingDetails.getPostalCode() : "");
           returnll.add(28, addressShippingDetails != null? addressShippingDetails.getPhone() : "");
           returnll.add(29, addressShippingDetails != null? addressShippingDetails.getMobileNumber() : "");
           returnll.add(30, addressShippingDetails != null? addressShippingDetails.getFax() : "");
           returnll.add(31, addressShippingDetails != null? addressShippingDetails.getEmailID() : "");
           returnll.add(32, addressShippingDetails != null? addressShippingDetails.getContactPerson() : "");
           returnll.add(33, addressShippingDetails != null? addressShippingDetails.getContactPersonNumber() : "");
           
           masterDetailsQuery = "SELECT mi.value,mi.designation,mi.emailid,mi.code from masteritem mi left join customer c on mi.id=c.salespersonmap where c.id='"+customerid+"'";
           masterItem = executeSQLQuery(masterDetailsQuery);
           if (!masterItem.isEmpty()) {
               Iterator ite1 = masterItem.iterator();
               while (ite1.hasNext()) {
                   Object[] obj = (Object[]) ite1.next();
                   returnll.add(34, obj[0] != null ? obj[0] : "");
                   returnll.add(35, obj[1] != null ? obj[1] : "");
                   returnll.add(36, obj[2] != null ? obj[2] : "");
                   returnll.add(37, obj[3] != null ? obj[3] : "");
               }

           } else {
               returnll.add(34, "");
               returnll.add(35, "");
               returnll.add(36, "");
               returnll.add(37, "");
           }
           returnll.add(38, termName);
           returnll.add(39, addressBillingDetails != null? addressBillingDetails.getCounty() : "");
           returnll.add(40, addressShippingDetails != null? addressShippingDetails.getCounty() : "");
           returnll.add(41, customerGSTIN);
           ll.set(0, returnll);
//           ll = executeSQLQuery(query, new Object[]{customerid,customerid,customerid});
       } catch (Exception e) {
           throw ServiceException.FAILURE("CustomDesignImpl.getSQLNativeQueryResult", e);
       }
        
       return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

    @Override
    public KwlReturnObject getVendorDetails(String customerid) throws ServiceException {
       List ll = new ArrayList();
       List addressll = new ArrayList();
       List returnll = new ArrayList();
       List masterItem = new ArrayList();
       VendorAddressDetails addressBillingDetails = null;
       VendorAddressDetails addressShippingDetails = null;
       String termName = "";
       String vendorGSTIN = "";
       try {
           String query = "", addressQuery="", masterDetailsQuery = "";
           query = "select v.acccode,v.name,v.aliasname,v.contactno,v.uennumber,v.gstregistrationnumber,ct.termdays,v.debitlimit,cu.currencycode,cu.name as curname,cu.symbol,ct.termname,v.gstin as vendorGSTIN"
                   + " from vendor v, currency cu, creditterm ct  where v.currency=cu.currencyid and v.debitTerm = ct.termid and v.id = ?";
           
           ll = executeSQLQuery(query, new Object[]{customerid});
           Iterator ite = ll.iterator();
           while(ite.hasNext()){
               Object[] obj = (Object[]) ite.next();
               returnll.add(0, obj[0]); //Customer Code
               returnll.add(1, obj[1]); //Customer Name
               returnll.add(2, obj[2]); //Customer Alias Name
               returnll.add(3, obj[3]); //
               returnll.add(4, obj[4]);
               returnll.add(5, obj[5]);
               returnll.add(6, obj[6]);
               returnll.add(7, obj[7]);
               returnll.add(8, obj[8]);
               returnll.add(9, obj[9]);
               returnll.add(10, obj[10]);
               
               termName = String.valueOf(obj[11]);
               vendorGSTIN = obj[12] == null ? "" : String.valueOf(obj[12]);
           }
//           addressQuery = "select id from vendoraddressdetails where isdefaultaddress='T' AND vendorid='"+customerid+"'";   //SDP-9421
//           addressll = executeSQLQuery(addressQuery);
//           if(!addressll.isEmpty()){
//               addressBillingDetails = (VendorAddressDetails) get(VendorAddressDetails.class, addressll.get(0).toString());
//               addressShippingDetails = (VendorAddressDetails) get(VendorAddressDetails.class, addressll.get(1).toString());
//           } 
           addressQuery = "FROM VendorAddressDetails WHERE isDefaultAddress=TRUE AND vendorID='" + customerid + "'";   //SDP-9421
           addressll = executeQuery(addressQuery);
           if (!addressll.isEmpty()) {
               for (Object obj : addressll) {
                   VendorAddressDetails vendorAddressDetails = (VendorAddressDetails) obj;
                   if (vendorAddressDetails.isIsBillingAddress()) {
                       addressBillingDetails = vendorAddressDetails;
                   } else {
                       addressShippingDetails = vendorAddressDetails;
                   }
               }
           }
           returnll.add(11, addressBillingDetails != null? addressBillingDetails.getAddress() : "");
           returnll.add(12, addressBillingDetails != null? addressBillingDetails.getCity() : "");
           returnll.add(13, addressBillingDetails != null? addressBillingDetails.getState() : "");
           returnll.add(14, addressBillingDetails != null? addressBillingDetails.getCountry() : "");
           returnll.add(15, addressBillingDetails != null? addressBillingDetails.getPostalCode() : "");
           returnll.add(16, addressBillingDetails != null? addressBillingDetails.getPhone() : "");
           returnll.add(17, addressBillingDetails != null? addressBillingDetails.getMobileNumber() : "");
           returnll.add(18, addressBillingDetails != null? addressBillingDetails.getFax() : "");
           returnll.add(19, addressBillingDetails != null? addressBillingDetails.getEmailID() : "");
           returnll.add(20, addressBillingDetails != null? addressBillingDetails.getContactPerson() : "");
           returnll.add(21, addressBillingDetails != null? addressBillingDetails.getContactPersonNumber() : "");
           returnll.add(22, addressShippingDetails != null? addressShippingDetails.getAddress() : "");
           returnll.add(23, addressShippingDetails != null? addressShippingDetails.getCity() : "");
           returnll.add(24, addressShippingDetails != null? addressShippingDetails.getState() : "");
           returnll.add(25, addressShippingDetails != null? addressShippingDetails.getCountry() : "");
           returnll.add(26, addressShippingDetails != null? addressShippingDetails.getPostalCode() : "");
           returnll.add(27, addressShippingDetails != null? addressShippingDetails.getPhone() : "");
           returnll.add(28, addressShippingDetails != null? addressShippingDetails.getMobileNumber() : "");
           returnll.add(29, addressShippingDetails != null? addressShippingDetails.getFax() : "");
           returnll.add(30, addressShippingDetails != null? addressShippingDetails.getEmailID() : "");
           returnll.add(31, addressShippingDetails != null? addressShippingDetails.getContactPerson() : "");
           returnll.add(32, addressShippingDetails != null? addressShippingDetails.getContactPersonNumber() : "");
           
           masterDetailsQuery = "SELECT mi.value,mi.designation,mi.emailid,mi.code from masteritem mi left join vendor v on mi.id=v.agent where v.id='"+customerid+"'";
           masterItem = executeSQLQuery(masterDetailsQuery);
           if (!masterItem.isEmpty()) {
               Iterator ite1 = masterItem.iterator();
               while (ite1.hasNext()) {
                   Object[] obj = (Object[]) ite1.next();
                   returnll.add(33, obj[0] != null ? obj[0] : "");
                   returnll.add(34, obj[1] != null ? obj[1] : "");
                   returnll.add(35, obj[2] != null ? obj[2] : "");
                   returnll.add(36, obj[3] != null ? obj[3] : "");
               }

           }else{
               returnll.add(33, "");
               returnll.add(34, "");
               returnll.add(35, "");
               returnll.add(36, "");
           }
           returnll.add(37, termName);
           returnll.add(38, ""); // added blank as indexing should be same for both customer and Vendor
           returnll.add(39, addressBillingDetails != null? addressBillingDetails.getCounty() : "");
           returnll.add(40, addressShippingDetails != null? addressShippingDetails.getCounty() : "");
           returnll.add(41, vendorGSTIN);
           ll.set(0, returnll);
           
//           ll = executeSQLQuery(query, new Object[]{customerid,customerid,customerid});
       } catch (Exception e) {
           throw ServiceException.FAILURE("CustomDesignImpl.getSQLNativeQueryResult", e);
       }
        
       return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

    
    @Override
    public KwlReturnObject getSQLNativeQueryResult(String recordId, String sqlquery) throws ServiceException {
        List ll = new ArrayList();
        try {
            ll = executeSQLQuery(sqlquery, new Object[]{recordId});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getSQLNativeQueryResult", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

    @Override
    public KwlReturnObject getCustomLineFields(String companyid, int moduleid) throws ServiceException {
        List ll = new ArrayList();
        try {
            String query = "select id, fieldtype, fieldlabel, colnum from fieldparams where companyid=? and moduleid=? and customcolumn=1";
            ll = executeSQLQuery(query, new Object[]{companyid, moduleid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getCustomLineFields", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
    
 @Override  
    public KwlReturnObject getProductCustomLineFields(String companyid, int moduleid) throws ServiceException {
        List ll = new ArrayList();
        try {
            String query = "select id, fieldtype, fieldlabel, colnum, relatedmoduleid from fieldparams where companyid=? and moduleid=? and customcolumn=0";
            ll = executeSQLQuery(query, new Object[]{companyid, moduleid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getCustomLineFields", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

    @Override
    public KwlReturnObject getGlobalCustomFields(String companyid, int moduleid) throws ServiceException {
        List ll = new ArrayList();
        try {
            String query = "select id, fieldlabel, fieldtype, colnum from fieldparams where companyid=? and moduleid=? and customcolumn=0 and allowindocumentdesigner=1";
            ll = executeSQLQuery(query, new Object[]{companyid, moduleid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getGlobalCustomFields", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

    @Override 
    public KwlReturnObject deleteCustomTemplate(String templateid, int moduleid, String companyid) throws ServiceException {

        String delQuery = "delete from customdesigntemplate where company= ? and moduleid = ? and id IN (" + templateid + ")";
        int numRows = executeSQLUpdate(delQuery, new Object[]{companyid, moduleid});
        return new KwlReturnObject(true, "Template has been deleted successfully.", null, null, numRows);
    }

    @Override 
    public KwlReturnObject getActiveDesignTemplateList(String companyid, int moduleid,String templatesubtype,String countryid,String stateid) throws ServiceException {
        List ll = new ArrayList();
        try {
            int country = 0;
            String query = "select id,templatename,concat(users.fname,' ',users.lname), DATE(customdesigntemplate.createdon), isdefault  , moduleid, templatesubtype from customdesigntemplate "
                    + " inner join users on users.userid=customdesigntemplate.createdby where (customdesigntemplate.company=? or customdesigntemplate.company='1') and moduleid=? and isdefault=? ";
            String condition = "";
            if(!StringUtil.isNullOrEmpty(countryid)){
                country = Integer.parseInt(countryid);
                condition += " and ( customdesigntemplate.countryid is null or customdesigntemplate.countryid = '' or customdesigntemplate.countryid=" + countryid +" ) ";
            }
            if(country == Constants.indian_country_id){
                if(!StringUtil.isNullOrEmpty(stateid)){
                    condition += " and ( customdesigntemplate.stateid is null or customdesigntemplate.stateid = '' or customdesigntemplate.stateid=" + stateid+" ) ";
                } else{
                    condition += " and ( customdesigntemplate.stateid is null or customdesigntemplate.stateid = '' ) ";
                }
            }
            query += condition;
            String orderBy = " order by customdesigntemplate.templatename";
            if (!StringUtil.isNullOrEmpty(templatesubtype)) {
                query += "and templatesubtype in ("+templatesubtype+")";
                query += orderBy;
                ll = executeSQLQuery(query, new Object[]{companyid, moduleid, 1});
            } else {
                query += orderBy;
                ll = executeSQLQuery(query, new Object[]{companyid, moduleid, 1});
            }
            
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getDesignTemplateList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
@Override 
    public KwlReturnObject saveActiveModeTemplate(String templateid, int moduleid, int isactive) throws ServiceException {
        List ll = new ArrayList();
        try {
            // update entry
            String query;
            if (!StringUtil.isNullOrEmpty(templateid)) {
                query = "update customdesigntemplate set isdefault=? where id = ?";
                executeSQLUpdate(query, new Object[]{isactive, templateid});
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.saveActiveModeTemplate", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

@Override 
    public KwlReturnObject getAllDesignTemplateList(String companyid, int moduleid, String templateid) throws ServiceException {
        List ll = new ArrayList();
        try {
            if (!StringUtil.isNullOrEmpty(templateid)) {
                String query = "select templatename from customdesigntemplate where company=?  and moduleid=? and id IN (" + templateid + ")  ";
                ll = executeSQLQuery(query, new Object[]{companyid, moduleid});
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getAllDesignTemplateList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
@Override     
//Summary Terms Fields-Invoice Terms    
    public KwlReturnObject getSummaryTerms(HashMap<String, Object> hm) throws ServiceException {
        List ll = new ArrayList();
        try {
            String companyId = (String) hm.get("companyid");
            boolean salesOrPurchaseFlag = false;
            if (hm.containsKey("salesOrPurchaseFlag")) {
                salesOrPurchaseFlag = (Boolean) hm.get("salesOrPurchaseFlag");
            }

            if (!StringUtil.isNullOrEmpty(companyId)) {
                String query = "select id,term from invoicetermssales where company=? and deleted=0 and salesorpurchase=?";
                ll = executeSQLQuery(query, new Object[]{companyId, salesOrPurchaseFlag});
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getAllDesignTemplateList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }  
    @Override
    public KwlReturnObject getTemplates(JSONObject jobj) throws ServiceException {
        List ll = new ArrayList();
        try {
            ArrayList<Object> objects =  new ArrayList<Object>();
            String companyid = "";
            if (jobj.has("companyid")) {
                companyid = jobj.optString("companyid","");
            }
            objects.add(companyid);
            String query = "select html,json, replace(html, 'border-color: rgb(181, 184, 200); border-style: solid; border-width: 1px;', '') as 'formathtml', "
                    + "sqlquery , pagelayoutproperty , replace(pagefooterhtml, 'border-color: rgb(181, 184, 200); border-style: solid; border-width: 1px;', '') as 'pagefooterhtml',"
                    + " pagefooterjson , pagefootersqlquery , templatesubtype , replace(pageheaderhtml, 'border-color: rgb(181, 184, 200); border-style: solid; border-width: 1px;', '') as 'pageheaderhtml',"
                    + " pageheaderjson , pageheadersqlquery , footerheader, isnewdesign, isdefaulttemplate , templatename from customdesigntemplate where customdesigntemplate.company = ? ";
            String templateName = "";
            if (jobj.has("templatename")) {
                templateName = jobj.optString("templatename","");
                query += " and customdesigntemplate.templatename = ? ";
                objects.add(templateName);
            }
            String moduleid = "";
            if (jobj.has("moduleid")) {
                moduleid = jobj.optString("moduleid","");
                query += " and customdesigntemplate.moduleid = ? ";
                objects.add(moduleid);
            }
            
            ll = executeSQLQuery(query, objects.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getAllDesignTemplateList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
    @Override
    public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
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
            int moduleId = 0;
            if(requestParams.containsKey("moduleid")){
                moduleId = requestParams.get("moduleid")!=null ? Integer.parseInt(requestParams.get("moduleid").toString()) : 0;
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }
    
            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("isActivated") && (Integer) requestParams.get("isActivated") != null) {
                int activatedFlag=(Integer) requestParams.get("isActivated");
                hql += " and isactivated = "+activatedFlag;
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
            if (moduleId!=0) {
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
        return new KwlReturnObject(true, "", null, list, (list != null) ? list.size() : 0);
    }
    
    @Override
    public KwlReturnObject getCompanyPreferences(String companyid) throws ServiceException {
        List ll = new ArrayList();
        try {
//            String query = "select c.companyname, cp.gstnumber, cp.companyuen, ca1.address as billadd, ca2.address as shipadd from company c, compaccpreferences cp, companyaddressdetails ca1,companyaddressdetails ca2 where c.companyid=? and cp.id=? AND c.companyid=cp.id and ca1.company=? and ca2.company=? and ca1.isbillingaddress='T' and ca1.isdefaultaddress='T' and ca2.isbillingaddress='F' and ca2.isdefaultaddress='T'; ";
            String query = "select c.companyname, cp.gstnumber, cp.companyuen from company c, compaccpreferences cp where c.companyid=? and cp.id=? and c.companyid=cp.id";
            ll = executeSQLQuery(query, new Object[]{companyid,companyid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getCompanyDetails", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }

    @Override
    public KwlReturnObject getCompanyAddress(String companyid, String isBillingAddress, String isDefaultAddress) throws ServiceException {
         List ll = new ArrayList();
        try {
            String query = "select address, city, state, country, postalcode, phone, mobilenumber, fax, emailid, contactperson, contactpersonnumber from companyaddressdetails where company=? and isbillingaddress=? and isdefaultaddress=?";
            ll = executeSQLQuery(query, new Object[]{companyid,isBillingAddress,isDefaultAddress});
        } catch (Exception e) {
            throw ServiceException.FAILURE("CustomDesignImpl.getCompanyDetails", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
        
    @Override
    public KwlReturnObject getComboFieldParams(String companyid, int moduleid) throws ServiceException{
        List ll = new ArrayList();
        try{
            String query = "select id, fieldlabel, fieldname from FieldParams where moduleid=? and fieldtype=? and customcolumn=? and companyid=?";
            ll = executeQuery(query, new Object[]{moduleid, 4, 1, companyid}); // 4 is for Combo fieldtype, 1 is for Line level custom fields
        } catch(Exception e){
            throw ServiceException.FAILURE("CustomDesignImpl.getGroupingFields", e);
        }
        
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
}