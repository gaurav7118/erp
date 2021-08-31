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
package com.krawler.spring.accounting.uom;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.DefaultUnitOfMeasure;
import com.krawler.hql.accounting.UOMNature;
import com.krawler.hql.accounting.UOMschemaType;
import com.krawler.hql.accounting.UOMSchema;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class accUomImpl extends BaseDAO implements accUomDAO {

    public KwlReturnObject getUnitOfMeasure(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String uomname = "";
        String query = "from UnitOfMeasure ";

        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }

        if (filterParams.containsKey("uomname")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "name=?";
            uomname = filterParams.get("uomname").toString();
            params.add(filterParams.get("uomname"));
        }
        
        if (filterParams.containsKey("doNotShowNAUomName") && Boolean.parseBoolean(filterParams.get("doNotShowNAUomName").toString()) && !uomname.equals("N/A")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "name!='N/A'";
        }

        query += condition;
//        query="from UnitOfMeasure where company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    @Override
    public KwlReturnObject getDisplayUnitOfMeasure(JSONObject obj) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from UOMSchema ";
        if (obj.has("uomschematypeid")) {
            condition += " where uomschematype.ID = ?  and(uomnature = 0 or uomnature = 1)";
            params.add(obj.optString("uomschematypeid"));
        }
        query += condition;
        returnList = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    @Override
    public JSONArray getDisplayUOM(List<UOMSchema> list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            for (UOMSchema us : list) {
                JSONObject obj = new JSONObject();
                if (us.getPurchaseuom() != null && us.getPurchaseuom().getID() != us.getBaseuom().getID()) {
                    obj.put("uomid", us.getPurchaseuom().getID());
                    obj.put("uomname", us.getPurchaseuom().getNameEmptyforNA());
                    obj.put("precision", us.getPurchaseuom().getAllowedPrecision());
                    jArr.put(obj);
                } else if (us.getSalesuom() != null && us.getSalesuom().getID() != us.getBaseuom().getID()) {
                    String checkexist = jArr.toString();
                    if (!checkexist.contains(us.getSalesuom().getID())) {
                        obj.put("uomid", us.getSalesuom().getID());
                        obj.put("uomname", us.getSalesuom().getNameEmptyforNA());
                        obj.put("precision", us.getSalesuom().getAllowedPrecision());
                        jArr.put(obj);
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accUomImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }    
    public KwlReturnObject getUOMType(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String uomname = "";
        String query = "from UOMschemaType ";

        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }

        if (filterParams.containsKey("uomname")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "name=?";
            uomname = filterParams.get("uomname").toString();
            params.add(filterParams.get("uomname"));
        }
        if (filterParams.containsKey("stockuomid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "stockuom.ID=?";
            params.add(filterParams.get("stockuomid"));
        }
        
        if (filterParams.containsKey("doNotShowNAUomName") && Boolean.parseBoolean(filterParams.get("doNotShowNAUomName").toString()) && !uomname.equals("N/A")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "name!='N/A'";
        }
        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    public KwlReturnObject getUOMSchema(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from UOMSchema ";

        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }

        if (filterParams.containsKey("uomnature")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "uomnature=?";
            params.add(filterParams.get("uomnature"));
        }
        if (filterParams.containsKey("uomschematypeid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "uomschematype.ID=?";
            params.add(filterParams.get("uomschematypeid"));
        }
        if (filterParams.containsKey("stockuomid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "baseuom.ID=?";
            params.add(filterParams.get("stockuomid"));
        }
        if (filterParams.containsKey("purchaseuom")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "purchaseuom.ID=?";
            params.add(filterParams.get("purchaseuom"));
        }
        if (filterParams.containsKey("salesuom")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "salesuom.ID=?";
            params.add(filterParams.get("salesuom"));
        }
        if (filterParams.containsKey("orderuom")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "orderuom.ID=?";
            params.add(filterParams.get("orderuom"));
        }
        if (filterParams.containsKey("transferuom")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "transferuom.ID=?";
            params.add(filterParams.get("transferuom"));
        }

        query += condition;
//        query="from UnitOfMeasure where company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject deleteUoM(String uomid, String companyid) throws ServiceException {
        String delQuery = "delete from UnitOfMeasure u where u.ID=? and u.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{uomid, companyid});
        return new KwlReturnObject(true, "UoM has been deleted successfully.", null, null, numRows);
    }
    
    /*
     * Search uom which is used for any product.
     */
     public KwlReturnObject searchUoM(String uomid, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        ArrayList params = new ArrayList();
        String searchQuery = "";
        String searchQueryOfProd = "";
        String searchQueryOfInv = "";
        String searchQueryOfCNaginstVenGST = "";
        String searchQueryOfDNaginstCustGST = "";
        String vendorQuotationDetails = "";
        String quotationDetails = "";
        String purchaseRequisitionDetail = "";
        String requestForQuotationDetail = "";
        String uomSchema = "";
        String uomSchemaType = "";
        String poDetails = "";
        String soDetails = "";
        String packaging = "";
        
        
        searchQueryOfProd = "select count(id),'Product' as module from  product where company=? and unitOfMeasure= ? Group By module ";
        params.add(companyid);
        params.add(uomid);
        
        searchQueryOfInv = "select count(id),'Inventory' as module  from  inventory where company=? and uom=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        
        searchQueryOfCNaginstVenGST = "select count(id),'Credit Note' as module from  cndetailsgst where company=? and uom= ? Group By module ";
        params.add(companyid);
        params.add(uomid);
        
        searchQueryOfDNaginstCustGST = "select count(id),'Debit Note' as module from  dndetailsgst where company=? and uom=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        
        vendorQuotationDetails = "select count(id),'Vendor Quotation' as module from  vendorquotationdetails where company=? and uom=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        
        quotationDetails = "select count(id),'Customer Quotation' as module from  quotationdetails where company=? and uom=? or reportinguomexcise=? or reportinguomvat=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        params.add(uomid);
        params.add(uomid);
        
        purchaseRequisitionDetail = "select count(id),'Purchase Requisition' as module from  purchaserequisitiondetail where company=? and uom=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        
        requestForQuotationDetail = "select count(id),'Request For Quotation' as module from  requestforquotationdetail where company=? and uom=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        
        uomSchema = "select count(id),'UOM Schema' as module from  uomschema where company=? and purchaseuom=? or salesuom=? or orderuom=? or transferuom=? or baseuom=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        params.add(uomid);
        params.add(uomid);
        params.add(uomid);
        params.add(uomid);
        
        uomSchemaType = "select count(id),'UOM Schema Master' as module from  uomschematype where company=? and stockuom=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        
        poDetails = "select count(id),'Purchase Order' as module from  podetails where company=? and uom=? or reportinguomexcise=? or reportinguomvat=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        params.add(uomid);
        params.add(uomid);
        
        soDetails = "select count(id),'Sales Order' as module from  sodetails where company=? and uom=? or reportinguomexcise=? or reportinguomvat=? Group By module ";
        params.add(companyid);
        params.add(uomid);
        params.add(uomid);
        params.add(uomid);
        
         packaging = "select count(id),'Packaging' as module from  in_packaging where company=? and casinguom=? or inneruom=? or stockuom=? Group By module ";
         params.add(companyid);
         params.add(uomid);
         params.add(uomid);
         params.add(uomid);
        
        searchQuery =  searchQueryOfProd + " UNION " +searchQueryOfInv + " UNION " +searchQueryOfCNaginstVenGST + " UNION "+searchQueryOfDNaginstCustGST
                       + " UNION " +vendorQuotationDetails + " UNION " +quotationDetails + " UNION " +purchaseRequisitionDetail + " UNION " +requestForQuotationDetail
                       + " UNION " +uomSchema + " UNION " +uomSchemaType + " UNION " +poDetails + " UNION " +soDetails + " UNION " + packaging; 
        
//        list = executeSQLQuery(searchQuery, new Object[]{companyid, uomid});
        list = executeSQLQuery(searchQuery, params.toArray());
        if (list != null && list.size() > 0) {
            count = list.size();
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject addUoM(HashMap<String, Object> uomMap) throws ServiceException {
        List list = new ArrayList();
        try {
            UnitOfMeasure uom = new UnitOfMeasure();
            uom = buildUoM(uom, uomMap);
            save(uom);
            list.add(uom);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addUoM : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "UoM has been added successfully", null, list, list.size());
    }
    public KwlReturnObject addUOMSchemaType(HashMap<String, Object> uomMap) throws ServiceException {
        List list = new ArrayList();
        try {
            UOMschemaType uomType = null;
            
            if (uomMap.containsKey("rowid") && !StringUtil.isNullOrEmpty((String) uomMap.get("rowid"))) {
                uomType = (UOMschemaType) get(UOMschemaType.class, (String) uomMap.get("rowid"));
            }else{
                uomType=new UOMschemaType();
            }
            if (uomMap.containsKey("schemaName")) {
                uomType.setName((String) uomMap.get("schemaName"));
            }        
            if (uomMap.containsKey("stockuomid")) {
                UnitOfMeasure uom = uomMap.get("stockuomid") == null ? null : (UnitOfMeasure) get(UnitOfMeasure.class, (String) uomMap.get("stockuomid"));
                uomType.setStockuom(uom);
            }
            if (uomMap.containsKey("companyid")) {
                Company company = uomMap.get("companyid") == null ? null : (Company) get(Company.class, (String) uomMap.get("companyid"));
                uomType.setCompany(company);
            }
            saveOrUpdate(uomType);
            list.add(uomType);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addUoM : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "UoM has been added successfully", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject searchUoMSchemaType(String schemaName, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String searchQuery = "from UOMschemaType u where u.name in ('"+ schemaName + "') and u.company.companyID=?";
        list = executeQuery(searchQuery, new Object[]{companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public int searchUoMTypeUsedinProduct(String uomschematype, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        if (!StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(uomschematype)) {
            String searchQuery = "select count(p.id) from  Product p where p.company.companyID= ? and p.uomSchemaType.name in ('"+ uomschematype + "') ";
            list = executeQuery(searchQuery, new Object[]{companyid});
            Long longCount = list != null && list.get(0) != null ? (Long) list.get(0): 0;  
            count= longCount.intValue();
            
            searchQuery = "select count(id) from UOMSchema u where u.uomschematype.name in ('"+ uomschematype + "') and u.company.companyID=?";
            list = executeQuery(searchQuery, new Object[]{companyid});
             longCount = list != null && list.get(0) != null ? (Long) list.get(0): 0;
            count = count + longCount.intValue();
        }
        return count;
    }

    public KwlReturnObject addUOMSchema(HashMap<String, Object> uomMap) throws ServiceException {
        List list = new ArrayList();
        try {
            UOMSchema uomSchema = null;
            
            if (uomMap.containsKey("rowid") && !StringUtil.isNullOrEmpty((String) uomMap.get("rowid"))) {
                uomSchema = (UOMSchema) get(UOMSchema.class, (String) uomMap.get("rowid"));
            }
            if(uomSchema == null){
                uomSchema=new UOMSchema();
            }
            if (uomMap.containsKey("purchaseuom")) {
                UnitOfMeasure uom = uomMap.get("purchaseuom") == null ? null : (UnitOfMeasure) get(UnitOfMeasure.class, (String) uomMap.get("purchaseuom"));
                uomSchema.setPurchaseuom(uom);
            }        
            if (uomMap.containsKey("salesuom")) {
                UnitOfMeasure uom = uomMap.get("salesuom") == null ? null : (UnitOfMeasure) get(UnitOfMeasure.class, (String) uomMap.get("salesuom"));
                uomSchema.setSalesuom(uom);
            }        
            if (uomMap.containsKey("orderuom")) {
                UnitOfMeasure uom = uomMap.get("orderuom") == null ? null : (UnitOfMeasure) get(UnitOfMeasure.class, (String) uomMap.get("orderuom"));
                uomSchema.setOrderuom(uom);
            }        
            if (uomMap.containsKey("transferuom")) {
                UnitOfMeasure uom = uomMap.get("transferuom") == null ? null : (UnitOfMeasure) get(UnitOfMeasure.class, (String) uomMap.get("transferuom"));
                uomSchema.setTransferuom(uom);
            }        
            if (uomMap.containsKey("baseuom")) {
                UnitOfMeasure uom = uomMap.get("baseuom") == null ? null : (UnitOfMeasure) get(UnitOfMeasure.class, (String) uomMap.get("baseuom"));
                uomSchema.setBaseuom(uom);
            }  
            if (uomMap.containsKey("baseuomrate")) {
                uomSchema.setBaseuomrate((Double) uomMap.get("baseuomrate"));
            }
            if (uomMap.containsKey("rateperuom")) {
                uomSchema.setRateperuom((Double) uomMap.get("rateperuom"));
            }
            if (uomMap.containsKey("uomnature")) {
                if (uomMap.get("uomnature").equals(UOMNature.Purchase)) {
                    uomSchema.setUomnature(UOMNature.Purchase);
                } else if (uomMap.get("uomnature").equals(UOMNature.Sales)) {
                    uomSchema.setUomnature(UOMNature.Sales);
                } else if (uomMap.get("uomnature").equals(UOMNature.Stock)) {
                    uomSchema.setUomnature(UOMNature.Stock);
                } else if (uomMap.get("uomnature").equals(UOMNature.Transfer)) {
                    uomSchema.setUomnature(UOMNature.Transfer);
                }
            }
            if (uomMap.containsKey("uomschematypeid")) {
                 UOMschemaType uomt = uomMap.get("uomschematypeid") == null ? null : (UOMschemaType) get(UOMschemaType.class, (String) uomMap.get("uomschematypeid"));
                uomSchema.setUomschematype(uomt);
            }
            if (uomMap.containsKey("companyid")) {
                Company company = uomMap.get("companyid") == null ? null : (Company) get(Company.class, (String) uomMap.get("companyid"));
                uomSchema.setCompany(company);
            }
            saveOrUpdate(uomSchema);
            list.add(uomSchema);
        } catch (Exception e) {
            throw ServiceException.FAILURE("UoM schema : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "UoM schema has been added successfully", null, list, list.size());
    }
     public KwlReturnObject deleteUOMSchemaForSchemaType(String uomTypeId, String companyid) throws ServiceException {
            String delQuery ="";
            if (!StringUtil.isNullOrEmpty(uomTypeId)) {
                uomTypeId = AccountingManager.getFilterInString(uomTypeId);
                delQuery = "delete from UOMSchema u where u.uomschematype in ("+ uomTypeId + ") and u.company.companyID=?";
            } 
//        String delQuery = "delete from UOMschemaType u where u.ID=? and u.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{companyid});
        return new KwlReturnObject(true, "UOM Schema has been deleted successfully.", null, null, numRows);
    }    
     public KwlReturnObject getProductLinkedWithUOMType(String uomTypeIds,String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Product p where p.uomSchemaType.ID=? and p.company.companyID=?";// and pod.purchaseOrder.deleted=false";
        list = executeQuery( q, new Object[]{uomTypeIds, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     public KwlReturnObject deleteUOMSchemaType(String uomTypeId, String companyid) throws ServiceException {
            String delQuery ="";
            if (!StringUtil.isNullOrEmpty(uomTypeId)) {
                uomTypeId = AccountingManager.getFilterInString(uomTypeId);
                delQuery = "delete from UOMschemaType u where u.ID in ("+ uomTypeId + ") and u.company.companyID=?";
            } 
//        String delQuery = "delete from UOMschemaType u where u.ID=? and u.company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{companyid});
        return new KwlReturnObject(true, "UOM Schema Type has been deleted successfully.", null, null, numRows);
    }
    public KwlReturnObject updateUoM(HashMap<String, Object> uomMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String uomid = (String) uomMap.get("uomid");
            UnitOfMeasure uom = (UnitOfMeasure) get(UnitOfMeasure.class, uomid);
            if (uom != null) {
                uom = buildUoM(uom, uomMap);
                saveOrUpdate(uom);
            }
            list.add(uom);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateUoM : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "UoM has been updated successfully", null, list, list.size());
    }

    public UnitOfMeasure buildUoM(UnitOfMeasure uom, HashMap<String, Object> uomMap) throws ServiceException {
        try {
            if (uomMap.containsKey("uomname")) {
                uom.setName((String) uomMap.get("uomname"));
            }
            if (uomMap.containsKey("uomtype")) {
                uom.setType((String) uomMap.get("uomtype"));
            }
            if (uomMap.containsKey("precision")) {
                uom.setAllowedPrecision((Integer) uomMap.get("precision"));
            }
            if (uomMap.containsKey("inventoryReferId")) {
                uom.setInventoryReferId((String) uomMap.get("inventoryReferId"));
            }
            if (uomMap.containsKey("companyid")) {
                Company company = uomMap.get("companyid") == null ? null : (Company) get(Company.class, (String) uomMap.get("companyid"));
                uom.setCompany(company);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildUoM : " + e.getMessage(), e);
        }
        return uom;
    }
/**
 * On company creation Unit and pcs UOM copy for all company.
 * On company setup currently INDIA company copy UOM data.
 * @param companyid
 * @param uomMap
 * @throws ServiceException 
 */
    @Override
//    @Transactional(propagation = Propagation.REQUIRED)
    public void copyUOM(String companyid, HashMap<String, Object> uomMap) throws ServiceException {
        try {
            String query = "from DefaultUnitOfMeasure";
            String UOMCondition = "";
            if((uomMap.containsKey("country") && uomMap.get("country")!=null) && !StringUtil.isNullOrEmpty(uomMap.get("country").toString())){
                String countryid = (String)uomMap.get("country");
                UOMCondition = " where country='"+ countryid + "'";
                /*
                 * Current this code is executed only INDIA company.
                 */
            }else{
                UOMCondition = " where country is null ";
            }
            List list = executeQuery( query + UOMCondition);
            Iterator iter = list.iterator();
            Company company = (Company) get(Company.class, companyid);
            while (iter.hasNext()) {
                DefaultUnitOfMeasure defaultUOM = (DefaultUnitOfMeasure) iter.next();
                UnitOfMeasure uom = new UnitOfMeasure();
                if (!isUOMPresnet(companyid, defaultUOM.getID())) {
                    uom.setCompany(company);
                    uom.setName(defaultUOM.getName());
                    uom.setType(defaultUOM.getType());
                    uom.setDefaultunitofmeasure(defaultUOM);
                    save(uom);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyUOM : " + ex.getMessage(), ex);
        }

    }

    public boolean isUOMPresnet(String companyid, String DefaultUOMId) throws ServiceException {
        try {
            String query = "from UnitOfMeasure";
            String UOMCondition = "";
            UOMCondition = " where defaultunitofmeasure = '"+DefaultUOMId+"' and company='"+companyid+"'";
            List list = executeQuery( query + UOMCondition);
            if(list.size()>0){
                return true;
            }else{
                return false;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Check Duplicate UOM : " + ex.getMessage(), ex);
        }

    }
    
    @Override
    public KwlReturnObject getUOMschemaTypeByName(String uomSchemaTypeName, String companyID) throws ServiceException {
        KwlReturnObject result = null;
        if (!StringUtil.isNullOrEmpty(companyID) && !StringUtil.isNullOrEmpty(uomSchemaTypeName)) {
            ArrayList params = new ArrayList();
            params.add(uomSchemaTypeName);
            params.add(companyID);
            String query = "from UOMschemaType where name=? and company.companyID=?";
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        }
        return result;
    }
    
    @Override
    public KwlReturnObject getUnitOfMeasureOfProductUOMSchema(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList subQueryParams = new ArrayList();
        if (filterParams.containsKey("isSalesModule")) {
            String subQuerycondition = "";
            String uomtype = Boolean.parseBoolean(filterParams.get("isSalesModule").toString()) ? "salesuom" : "purchaseuom";
            String query = "select " + uomtype + " from uomschema where " + uomtype + " is not null and uomschematype in ";
            String subQuery = "SELECT uomschematype from product where company = ? and uomschematype is not null";
            subQueryParams.add(filterParams.get(Constants.companyKey));

            if (filterParams.containsKey("productid")) {
                subQuerycondition += " and id = ? ";
                subQueryParams.add(filterParams.get("productid"));
}

            subQuery += subQuerycondition;
            query += " ( " + subQuery + " ) ";

            returnList = executeSQLQuery(query, subQueryParams.toArray());
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    /*
     * Delete Packaging entry while deleting product 
     * Delete the entry in packaging that only use in packaging not in any of transaction.
     * @param deletePackingMap
     * @throws ServiceException
     */
    @Override
    public int deletePackaging(Map<String, String> deletePackingMap) throws ServiceException {
        ArrayList params = new ArrayList();
        String companyId = "";
        String packingId = "";
        String hqlquery = "";
        String condition = "";
        int numRows = 0;
        try {
            if (deletePackingMap.containsKey("companyid") && deletePackingMap.get("companyid") != null) {
                companyId = deletePackingMap.get("companyid");
                condition += " WHERE company.companyID=? ";
                params.add(companyId);
                if (deletePackingMap.containsKey("packingId") && deletePackingMap.get("packingId") != null) {
                    packingId = deletePackingMap.get("packingId");
                    condition += " AND id=? ";
                    params.add(packingId);
                }
                if (deletePackingMap.containsKey("stockuom") && deletePackingMap.get("stockuom") != null) {
                    packingId = deletePackingMap.get("stockuom");
                    condition += " AND stockUoM.ID=? ";
                    params.add(packingId);
                }
                hqlquery = "DELETE FROM Packaging " + condition;
                numRows = executeUpdate(hqlquery, params.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Delete Packging error : " + ex.getMessage(), ex);
        }
        return numRows;
    }
    
    @Override
    public boolean isValidDisplayUOM(String uomSchemaTypeId, String displayUOMId) throws ServiceException{
        boolean result = false;
        if (!StringUtil.isNullOrEmpty(uomSchemaTypeId) && !StringUtil.isNullOrEmpty(displayUOMId)) {
            ArrayList params = new ArrayList();
            params.add(uomSchemaTypeId);
            params.add(displayUOMId);
            params.add(displayUOMId);
            String query = "from UOMSchema us LEFT JOIN us.purchaseuom LEFT JOIN us.salesuom where us.uomschematype.ID=? and ((us.purchaseuom.ID=? AND us.purchaseuom.ID<>us.baseuom.ID) OR (us.salesuom.ID=? AND us.salesuom.ID<>us.baseuom.ID))";
            List list = executeQuery( query, params.toArray());
            result = list.size()>0;
        }
        return result;
    }
}
