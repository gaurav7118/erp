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
package com.krawler.spring.accounting.account;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author krawler
 */
public class accVendorCustomerProductDAOImpl extends BaseDAO implements accVendorCustomerProductDAO{
    
//for saving in vendorproductmapping-Neeraj D
    @Override
    public KwlReturnObject saveVendorProductMapping(String vendorid, String productmapping, String jsonString) throws ServiceException {
        List list = new ArrayList();
        try {
            VendorProductMapping products = new VendorProductMapping();
            if (vendorid != null) {
                products.setVendor((Vendor) get(Vendor.class, vendorid));
            }
            if (productmapping != null) {
                products.setProducts((Product) get(Product.class, productmapping));
            }
             if(!StringUtil.isNullOrEmpty(jsonString)){ //ERP-30963 Set Custom data Json in column
                products.setJsonstring(jsonString);
            } else{
                 products.setJsonstring(null);
            }
            save(products);
            list.add(products);
        } catch (Exception e) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.saveVendorProductMapping", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
//for retrieving from vendorproductmapping-Neeraj D
    /**
     * jsonstringFilter -> value to filter data on jsonstring column
     * @param vendorid
     * @param ss
     * @param jsonstringFilter
     * @return
     * @throws ServiceException 
     */
     @Override
    public KwlReturnObject getProductsByVendor(String vendorid, String ss, String jsonstringFilter) throws ServiceException {
        String vendorproductid = vendorid;
        ArrayList params = new ArrayList();
        params.add(vendorproductid);
        String conditionss = "";
        List returnList = new ArrayList();
//       String query = "select vendorproducts from Vendorproductmapping where vendorid =  ? ";
        if (StringUtil.isNullOrEmpty(ss) == false) {
            params.add(("%" + ss + "%").toString());
            conditionss += "and p.vendor.name like ?";
        }
        if (!StringUtil.isNullOrEmpty(jsonstringFilter)) {
            params.add("%" + jsonstringFilter + "%");
            conditionss += "and p.jsonstring like ?";
        }

        String query = "from VendorProductMapping p where p.vendor.ID=?" + conditionss;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
//getting all the products except the vendor mapped products
    @Override
    public KwlReturnObject getProductsVendorabsence(HashMap<String, Object> requestParams,List listvendorproducts) throws ServiceException {
        int dl = 0;
        List ll = null;
        try {
            ArrayList params = new ArrayList();
            StringBuilder tempstring = new StringBuilder();
            String conditionSQL = "";
            Iterator itr = listvendorproducts.iterator();
             while (itr.hasNext()) {
                    VendorProductMapping VendorProductObj = (VendorProductMapping) itr.next();
                    String producc = VendorProductObj.getProducts().getID();
                String productid = producc;
                if (itr.hasNext()) {
                    tempstring.append("'" + productid + "',");
                } else {
                    tempstring.append("'" + producc + "'");
                }
            }
              conditionSQL +=" and p.ID not in ("+tempstring+") ";
            boolean isFixedAsset = false;
            if (requestParams.containsKey("isFixedAsset")) {
                isFixedAsset = (Boolean) requestParams.get("isFixedAsset");
            }
            params.add(isFixedAsset);
            String companyid = requestParams.get("companyid").toString();
             params.add(companyid);
             
            Company company = (Company) get(Company.class, companyid);
            String Hql = "from Product p where p.asset=? and p.company.companyID = ? "+conditionSQL+" order by p.producttype, p.name ";
            ll = executeQuery( Hql,params.toArray());
            dl = ll.size();

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("organizationChartDAOImpl.getUnmappedUsers", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
    
    //deleting a vendor mapped records on the basis of vendor id-Neeraj D
         @Override
    public KwlReturnObject deleteVendorProductMapped(String vendorid, String productid) throws ServiceException {
        int numRows = 0;
        StringBuilder conditionString = new StringBuilder();
        String vendProdid = vendorid;
        if (!StringUtil.isNullOrEmpty(vendorid)) {
            conditionString.append(" pm.vendor.ID=? ");

        }
        if (!StringUtil.isNullOrEmpty(productid)) {
            conditionString.append(" pm.products.ID=? ");
            vendProdid = productid;
        }
        String delQuery = "delete from VendorProductMapping pm where " + conditionString.toString();
        numRows += executeUpdate(delQuery, new Object[]{vendProdid});
        return new KwlReturnObject(true, "Vendor Mapped Product has been deleted successfully.", null, null, numRows);
    }
         
    //deleting a customer mapped records on the basis of customer id-Neeraj D  
         @Override
    public KwlReturnObject deleteCustomerProductMapped(String customerid) throws ServiceException {
        int numRows = 0;
        String delQuery = "delete from CustomerProductMapping pm where pm.customer.ID=?";
        numRows += executeUpdate( delQuery, new Object[]{customerid});
        return new KwlReturnObject(true, "Customer's Mapped Product has been deleted successfully.", null, null, numRows);
    }
    
        
     //for saving in customerproductmapping table-Neeraj D
         @Override
       public KwlReturnObject saveCustomerProductMapping(String customerid, String productmapping, String jsonString) throws ServiceException {
        List list = new ArrayList();
        try {
            CustomerProductMapping products = new CustomerProductMapping();
            if (customerid != null) {
                products.setCustomer((Customer) get(Customer.class, customerid));
            }
            if (productmapping != null) {
                products.setProducts((Product) get(Product.class, productmapping));
            }
            if(!StringUtil.isNullOrEmpty(jsonString)){
                products.setJsonstring(jsonString);
            } else{
                 products.setJsonstring(null);
            }
            save(products);
            list.add(products);
        } catch (Exception e) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.saveCustomerProductMapping", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
//for retrieving from customerproductmapping-Neeraj D
    /**
     * jsonstringFilter -> value to filter data on jsonstring column
     * @param customerid
     * @param ss
     * @param jsonstringFilter
     * @return
     * @throws ServiceException 
     */
     @Override
    public KwlReturnObject getProductsByCustomer(String customerid, String ss, String jsonstringFilter) throws ServiceException {
        String customerproductid = customerid;
        ArrayList params = new ArrayList();
        params.add(customerproductid);
        String conditionss = "";
        List returnList = new ArrayList();
//       String query = "select vendorproducts from Vendorproductmapping where vendorid =  ? ";
        if (StringUtil.isNullOrEmpty(ss) == false) {
            params.add(("%"+ss +"%").toString());
            conditionss += "and p.customer.name like ?";
        }
      
        if (!StringUtil.isNullOrEmpty(jsonstringFilter)) {
            params.add("%" + jsonstringFilter + "%");
            conditionss += "and p.jsonstring like ?";
        }
        
        String query = "from CustomerProductMapping p where p.customer.ID=?" + conditionss;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
     
     
     //getting all the products except the vendor mapped products
    @Override
    public KwlReturnObject getProductsCustomerabsence(HashMap<String, Object> requestParams,List listcustomerproducts) throws ServiceException {
        int dl = 0;
        List ll = null;
        try {
            ArrayList params = new ArrayList();
            StringBuilder tempstring = new StringBuilder();
            String conditionSQL = "";
            Iterator itr = listcustomerproducts.iterator();
             while (itr.hasNext()) {
                    CustomerProductMapping CustomerProductObj = (CustomerProductMapping) itr.next();
                    String producc = CustomerProductObj.getProducts().getID();
                String productid = producc;
                if (itr.hasNext()) {
                    tempstring.append("'" + productid + "',");
                } else {
                    tempstring.append("'" + producc + "'");
                }
            }
              conditionSQL +=" and p.ID not in ("+tempstring+") ";
            boolean isFixedAsset = false;
            if (requestParams.containsKey("isFixedAsset")) {
                isFixedAsset = (Boolean) requestParams.get("isFixedAsset");
            }
            params.add(isFixedAsset);
            String companyid = requestParams.get("companyid").toString();
             params.add(companyid);
             
            Company company = (Company) get(Company.class, companyid);
            String Hql = "from Product p where p.asset=? and p.company.companyID = ? "+conditionSQL+" order by p.producttype, p.name ";
            ll = executeQuery( Hql,params.toArray());
            dl = ll.size();

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("organizationChartDAOImpl.getUnmappedUsers", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    @Override
    public KwlReturnObject getProductByProductCode(String companyId, String productCode) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(productCode);
            params.add(companyId);
            String query = "from Product p where p.productid=? and p.company.companyID=? ";
            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorCustomerProductDAOImpl.getProductByProductCode", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
     @Override
    public KwlReturnObject getProductByNameorID(String companyid, String value, String masterGroupID, String fetchColumn, String conditionColumn) throws ServiceException {
            List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(value);
            String query = "SELECT "+fetchColumn +" from Product p where  p.company.companyID=? and "+conditionColumn+"=? ";
            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorCustomerProductDAOImpl.getProductByNameorID", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
     @Override
    public KwlReturnObject getCustomerCategoryIDs(String customerid) throws ServiceException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();
        params.add(customerid);

        String query = "from CustomerCategoryMapping where customerID.ID =  ? ";
        List list = executeQuery( query, params.toArray());

        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }

    @Override
    public KwlReturnObject deleteCustomer(String customerId, String companyId) throws ServiceException {
        int numrows=0;
        try{
           String delQuery = "delete from Customer c where c.ID=? and c.company.companyID=?";
           numrows = executeUpdate( delQuery, new Object[]{customerId,companyId});           
        }catch(Exception ex){
           throw ServiceException.FAILURE("accVendorCustomerProductDAOImpl.deleteCustomer", ex); 
        }
        return new KwlReturnObject(true, "", null, null, numrows); 
    }
    @Override
    public KwlReturnObject deleteVendor(String vendorId, String companyId) throws ServiceException {
        int numrows=0;
        try{
           String delQuery = "delete from Vendor v where v.ID=? and v.company.companyID=?";
           numrows = executeUpdate( delQuery, new Object[]{vendorId,companyId});           
        }catch(Exception ex){
           throw ServiceException.FAILURE("accVendorCustomerProductDAOImpl.deleteVendor", ex); 
        }
        return new KwlReturnObject(true, "", null, null, numrows); 
    }
    @Override
    public KwlReturnObject getProductsByVendorFunction(String companyId, String ss,int start,int limit) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(companyId);
        String conditionss = "";
        int totalCount=0;
        List returnList = new ArrayList();
        
        if (StringUtil.isNullOrEmpty(ss) == false) {
            params.add(("%" + ss + "%").toString());
            conditionss += "and p.vendor.name like ?";
        }
        String query = "from VendorProductMapping p where p.vendor.company.companyID=?"+ conditionss +" order by p.vendor.name,p.products.name" ;
        returnList = executeQuery( query, params.toArray());
        if(returnList.size()>0){
            totalCount=returnList.size();
        }
        returnList = executeQueryPaging( query, params.toArray(), new Integer[]{start, limit});
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getProductsByCustomerfunction(String customerid, String ss,int start,int limit) throws ServiceException {
        String customerproductid = customerid;
        ArrayList params = new ArrayList();
        params.add(customerproductid);
        String conditionss = "";
        int totalCount=0;
        List returnList = new ArrayList();
        if (StringUtil.isNullOrEmpty(ss) == false) {
            params.add(("%" + ss + "%").toString());
            conditionss += "and p.customer.name like ?";
        }
        String query = "from CustomerProductMapping p where p.customer.company.companyID=?" + conditionss + "order by p.customer.name,p.products.name";
        returnList = executeQuery( query, params.toArray());
        if(returnList.size()>0){
            totalCount=returnList.size();
        }
        returnList = executeQueryPaging( query, params.toArray(), new Integer[]{start, limit});
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
}
