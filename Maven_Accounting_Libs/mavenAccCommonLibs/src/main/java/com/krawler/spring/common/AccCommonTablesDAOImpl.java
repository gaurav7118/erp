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

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.inventory.model.inspection.InspectionArea;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.inspection.InspectionFormDetails;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.springframework.context.MessageSource;
/**
 *
 * @author krawler
 */
public class AccCommonTablesDAOImpl extends BaseDAO implements AccCommonTablesDAO {
    
    private MessageSource messageSource;
     
     public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public KwlReturnObject getCompanyTypes() throws ServiceException {
        List ll = new ArrayList();
        try {
            String query = "from CompanyType";
            ll = executeQuery(query);
        } catch (Exception e) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.getCompanyTypes", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
      public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
         List list =null;
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
      /**
       * 
       * @param requestParams
       * @description To get Specific fields from fieldparams
       */
       public KwlReturnObject getFieldParamsforSpecificFields(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "select id,fieldname,fieldlabel from FieldParams ";
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

      public KwlReturnObject getCustomCombodata(HashMap<String, Object> requestParams)
    {
        KwlReturnObject result = null;
        List list =null;
        try
        {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from FieldComboData ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null)
            {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = hql.indexOf("(");
                if (ind > -1)
                {
                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    value.remove(index);
                }
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null)
            {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }
            String searchText = "";
            if (requestParams.containsKey("searchText")) {
                searchText = requestParams.get("searchText") != null ? requestParams.get("searchText").toString() : "";
            }
            if (!StringUtil.isNullOrEmpty(searchText)) {
                String valArr[] = searchText.split(",");
                String val = "";
                for (int index = 0; index < valArr.length; index++) {
                    val += "'" + valArr[index] + "',";
                }
                if (val.length() > 1) {
                    val = val.substring(0, val.length() - 1);
                }
                if (!StringUtil.isNullOrEmpty(val)) {
                    hql += " and value IN (" + val + ")";
                }
            }
            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null)
            {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
             list = executeQuery(hql, value.toArray());

        } catch (Exception ex)
        {            
            ex.printStackTrace();

        } finally
        {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }
        public KwlReturnObject setPDFTemplate(HashMap<String, Object> pdfTemplateMap) throws ServiceException {
        
            List list = new ArrayList();
        try {
            PdfTemplateConfig config = new PdfTemplateConfig();
            if (pdfTemplateMap.containsKey("ID")) {
                config.setID((String)pdfTemplateMap.get("ID"));
            }
            if (pdfTemplateMap.containsKey("module")) {
                config.setModule((Integer) pdfTemplateMap.get("module"));
            }
            if (pdfTemplateMap.containsKey("pdfheader")) {
                config.setPdfHeader((String)pdfTemplateMap.get("pdfheader"));
            }
            if (pdfTemplateMap.containsKey("pdffooter")) {
                config.setPdfFooter((String)pdfTemplateMap.get("pdffooter"));
            }
            if (pdfTemplateMap.containsKey("pdfpretext")) {
                config.setPdfPreText((String)pdfTemplateMap.get("pdfpretext"));
            }
            if (pdfTemplateMap.containsKey("pdfposttext")) {
                config.setPdfPostText((String)pdfTemplateMap.get("pdfposttext"));
            }
            if (pdfTemplateMap.containsKey("companyid")) {
                Company company = pdfTemplateMap.get("companyid") == null ? null : (Company) get(Company.class, (String) pdfTemplateMap.get("companyid"));
                config.setCompany(company);
            }
            save(config);
            list.add(config);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.setPDFTemplate : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "acc.field.PDFTemplateinformationupdatedsuccessfully", null, list, list.size());
    }
      public KwlReturnObject getPDFTemplateRow(String companyid,int module) throws ServiceException {        
        ArrayList params = new ArrayList();
        List list = new ArrayList();        
        params.add(companyid);
        params.add(module);
        String query = "from PdfTemplateConfig config where config.company.companyID=? and config.module=?";
        list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
     public KwlReturnObject getPDFTemplateConfig(String companyid) throws ServiceException {
        int listSize = 0;
        List list = new ArrayList();
        try {
            String query = "from PdfTemplateConfig config where config.company.companyID=?";
            list = executeQuery(query, new String[]{companyid});
            if (list != null) {
                listSize = list.size();
            }

        } catch (Exception ex) {
            Logger.getLogger(AccCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject saveBatchForProduct(HashMap<String, Object> productbatchMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ProductBatch productBatch = new ProductBatch();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                productBatch = (ProductBatch) get(ProductBatch.class, itemID);
                if (productBatch == null) {
                    productBatch = new ProductBatch();
                    productBatch.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                productBatch.setId(newitemID);
            }

            if (productbatchMap.containsKey("name")) {
                productBatch.setName((String) productbatchMap.get("name"));
            }
            if (productbatchMap.containsKey("product")) {
                productBatch.setProduct((String) productbatchMap.get("product"));
            }
            if (productbatchMap.containsKey("asset")) {
                productBatch.setAsset((String) productbatchMap.get("asset"));
            }
            if (productbatchMap.containsKey("mfgdate")) {
                productBatch.setMfgdate((Date) productbatchMap.get("mfgdate"));
            }
            if (productbatchMap.containsKey("expdate")) {
                productBatch.setExpdate((Date) productbatchMap.get("expdate"));
            }
            
            if (productbatchMap.containsKey("quantity") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("quantity"))) {
                double qty = Double.parseDouble((String) productbatchMap.get("quantity"));
                productBatch.setQuantity(qty);
            }
            if (productbatchMap.containsKey("balance") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("balance"))) {
                double qty = Double.parseDouble((String) productbatchMap.get("balance"));
                productBatch.setBalance(qty);
            }
            if (productbatchMap.containsKey("transactiontype") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("transactiontype"))) {
                int typeNo = Integer.parseInt((String) productbatchMap.get("transactiontype"));
                productBatch.setTransactiontype(typeNo);
            }
            if (productbatchMap.containsKey("location")) {
                InventoryLocation location = new InventoryLocation();
                location = (InventoryLocation) get(InventoryLocation.class, (String) productbatchMap.get("location"));
                if (location != null) {
                    productBatch.setLocation(location);
                }
            }
            if (productbatchMap.containsKey("companyid")) {
                Company company = new Company();
                company = (Company) get(Company.class, (String) productbatchMap.get("companyid"));
                if (company != null) {
                    productBatch.setCompany(company);
                }
            }
            if (productbatchMap.containsKey("warehouse")) {
                InventoryWarehouse warehouse = new InventoryWarehouse();
                warehouse = (InventoryWarehouse) get(InventoryWarehouse.class, (String) productbatchMap.get("warehouse"));
                if (warehouse != null) {
                    productBatch.setWarehouse(warehouse);
                }
            }
            if (productbatchMap.containsKey("isopening")) {
                boolean isopening = (Boolean) productbatchMap.get("isopening");
                productBatch.setIsopening(isopening);
            }
            if (productbatchMap.containsKey("ispurchase")) {
                boolean ispurchase = (Boolean) productbatchMap.get("ispurchase");
                productBatch.setIspurchase(ispurchase);
            }
            saveOrUpdate(productBatch);
            list.add(productBatch);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.updatePDFTemplate : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "PDF Template information updated successfully.", null, list, list.size());
    }
    @Override
    public void saveBatchAmountDue(HashMap<String, Object> productbatchMap) throws ServiceException {
        try {
            NewProductBatch productBatch = new NewProductBatch();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                productBatch = (NewProductBatch) load(NewProductBatch.class, itemID);
                
                if (productbatchMap.containsKey("qty") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("qty"))) {
                    Double itemQty = Double.parseDouble((String) productbatchMap.get("qty"));
                    productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() + itemQty),productBatch.getCompany().getCompanyID()));
                }
                //in partial case update quantity
                if (productbatchMap.containsKey("quantity") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("quantity"))) {
                    Double quantity = Double.parseDouble((String) productbatchMap.get("quantity"));
                    productBatch.setQuantity(authHandler.roundQuantity(productBatch.getQuantity() + quantity, productBatch.getCompany().getCompanyID()));
                }

                if (productbatchMap.containsKey("lockquantity") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("lockquantity"))) {
                    Double lockQty = Double.parseDouble((String) productbatchMap.get("lockquantity"));
                    double newLockquantity=0.0;
                    newLockquantity =authHandler.roundQuantity((productBatch.getLockquantity() + lockQty), productBatch.getCompany().getCompanyID());
                    if ((newLockquantity) >= 0) {
                        productBatch.setLockquantity(newLockquantity);
                    }
                }
                
                if (productbatchMap.containsKey("consignquantity") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("consignquantity"))) {
                    Double consignQty = Double.parseDouble((String) productbatchMap.get("consignquantity"));
                    productBatch.setConsignquantity(authHandler.roundQuantity(productBatch.getConsignquantity() + consignQty, productBatch.getCompany().getCompanyID()));
                }
                if (productbatchMap.containsKey("isForconsignment")) {
                    boolean isForconsignment = (Boolean) productbatchMap.get("isForconsignment");
                    productBatch.setIsForconsignment(isForconsignment);
                }
            }
                 saveOrUpdate(productBatch);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchAmountDue : " + ex.getMessage(), ex);
        }
    }
    public KwlReturnObject updateNewBatchProductIfPresent(HashMap<String, Object> productbatchMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String row = null, rack = null, bin = null, warehouse = null, location = null, batch = null, product = null, company = null;
            NewProductBatch productBatch;
            if (productbatchMap.containsKey("id")  && !StringUtil.isNullOrEmpty((String) productbatchMap.get("id"))) {
                    productBatch = (NewProductBatch) get(NewProductBatch.class, (String) productbatchMap.get("id"));
                    if (productBatch == null) {
                        productBatch = new NewProductBatch();
                        productBatch.setId((String) productbatchMap.get("id"));
                    }
                    if (productbatchMap.containsKey("quantity")) {
                        double qty = (double) productbatchMap.get("quantity");
                        productBatch.setQuantity(qty);
                        productBatch.setQuantitydue(qty);
                    }
                    saveOrUpdate(productBatch);
                    list.add(productBatch);
                }
//            }
//            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.updatePDFTemplate : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "PDF Template information updated successfully.", null, list, list.size());
    }
    
    public KwlReturnObject saveNewBatchForProduct(HashMap<String, Object> productbatchMap) throws ServiceException {
        List list = new ArrayList();
        try {
            NewProductBatch productBatch = new NewProductBatch();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                productBatch = (NewProductBatch) get(NewProductBatch.class, itemID);
                if (productBatch == null) {
                    productBatch = new NewProductBatch();
                    productBatch.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                productBatch.setId(newitemID);
            }

            if (productbatchMap.containsKey("name")) {
                productBatch.setBatchname((String) productbatchMap.get("name"));
            }
            if (productbatchMap.containsKey("product")) {
                productBatch.setProduct((String) productbatchMap.get("product"));
            }
            if (productbatchMap.containsKey("asset")) {
                productBatch.setAsset((String) productbatchMap.get("asset"));
            }
            if (productbatchMap.containsKey("mfgdate")) {
                productBatch.setMfgdate((Date) productbatchMap.get("mfgdate"));
            }
            if (productbatchMap.containsKey("expdate")) {
                productBatch.setExpdate((Date) productbatchMap.get("expdate"));
            }
            
            if (productbatchMap.containsKey("quantity") && !StringUtil.isNullOrEmpty(""+ productbatchMap.get("quantity"))) {
                double qty = authHandler.roundQuantity(Double.parseDouble(""+ productbatchMap.get("quantity")),(String) productbatchMap.get("companyid") );
                productBatch.setQuantity(qty);
                productBatch.setQuantitydue(qty);
            }
             if (productbatchMap.containsKey("qty") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("qty"))) {
                Double itemQty =  authHandler.roundQuantity(Double.parseDouble((String) productbatchMap.get("qty")),(String) productbatchMap.get("companyid"));
                productBatch.setQuantitydue(itemQty);
            }
            if (productbatchMap.containsKey("consignquantity") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("consignquantity"))) {
                double consignquantity=authHandler.roundQuantity(Double.parseDouble((String) productbatchMap.get("consignquantity")),(String) productbatchMap.get("companyid"));;
                productBatch.setConsignquantity(consignquantity);
            }
            if (productbatchMap.containsKey("balance") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("balance"))) {
                double qty = Double.parseDouble((String) productbatchMap.get("balance"));
                productBatch.setBalance(qty);
            }
            if (productbatchMap.containsKey("transactiontype") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("transactiontype"))) {
                int typeNo = Integer.parseInt((String) productbatchMap.get("transactiontype"));
                productBatch.setTransactiontype(typeNo);
            }
            if (productbatchMap.containsKey("location")) {
                InventoryLocation location = new InventoryLocation();
                location = (InventoryLocation) get(InventoryLocation.class, (String) productbatchMap.get("location"));
                if (location != null) {
                    productBatch.setLocation(location);
                }
            }
            if (productbatchMap.containsKey("row") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("row"))) {
                StoreMaster storeMaster = new StoreMaster();
                storeMaster = (StoreMaster) get(StoreMaster.class, (String) productbatchMap.get("row"));
                if (storeMaster != null) {
                    productBatch.setRow(storeMaster);
                }
            }
            if (productbatchMap.containsKey("rack") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("rack"))) {
                StoreMaster storeMaster = new StoreMaster();
                storeMaster = (StoreMaster) get(StoreMaster.class, (String) productbatchMap.get("rack"));
                if (storeMaster != null) {
                    productBatch.setRack(storeMaster);
                }
            }
            if (productbatchMap.containsKey("bin") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("bin"))) {
                StoreMaster storeMaster = new StoreMaster();
                storeMaster = (StoreMaster) get(StoreMaster.class, (String) productbatchMap.get("bin"));
                if (storeMaster != null) {
                    productBatch.setBin(storeMaster);
                }
            }
            if (productbatchMap.containsKey("companyid")) {
                Company company = new Company();
                company = (Company) get(Company.class, (String) productbatchMap.get("companyid"));
                if (company != null) {
                    productBatch.setCompany(company);
                }
            }
            if (productbatchMap.containsKey("warehouse")) {
                InventoryWarehouse warehouse = new InventoryWarehouse();
                warehouse = (InventoryWarehouse) get(InventoryWarehouse.class, (String) productbatchMap.get("warehouse"));
                if (warehouse != null) {
                    productBatch.setWarehouse(warehouse);
                }
            }
            if (productbatchMap.containsKey("isopening")) {
                boolean isopening = (Boolean) productbatchMap.get("isopening");
                productBatch.setIsopening(isopening);
            }
            if (productbatchMap.containsKey("ispurchase")) {
                boolean ispurchase = (Boolean) productbatchMap.get("ispurchase");
                productBatch.setIspurchase(ispurchase);
            }
            if (productbatchMap.containsKey("isConsignment") && productbatchMap.get("isConsignment") != null) {
                productBatch.setIsconsignment((Boolean) productbatchMap.get("isConsignment"));
            }
            if (productbatchMap.containsKey("isForconsignment") && productbatchMap.get("isForconsignment") != null) {
                productBatch.setIsForconsignment((Boolean) productbatchMap.get("isForconsignment"));
            }
            
            saveOrUpdate(productBatch);
            list.add(productBatch);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.updatePDFTemplate : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "PDF Template information updated successfully.", null, list, list.size());
    }
    @Override
    public KwlReturnObject saveSalesPurchaseSerialMapping(HashMap<String, Object> productbatchMap) throws ServiceException {
        List list = new ArrayList();
        try {
            BatchSerialMapping serialMapping = new BatchSerialMapping();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                serialMapping = (BatchSerialMapping) get(BatchSerialMapping.class, itemID);
                if (serialMapping == null) {
                    serialMapping = new BatchSerialMapping();
                    serialMapping.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                serialMapping.setId(newitemID);
            }


            if (productbatchMap.containsKey("purchaseserialid")) {
                BatchSerial batchSerial = new BatchSerial();
                batchSerial = (BatchSerial) get(BatchSerial.class, (String) productbatchMap.get("purchaseserialid"));
                if (batchSerial != null) {
                    serialMapping.setPurchaseSerial(batchSerial);
                }
            }

            if (productbatchMap.containsKey("salesserialid")) {
                BatchSerial batchSerial = new BatchSerial();
                batchSerial = (BatchSerial) get(BatchSerial.class, (String) productbatchMap.get("salesserialid"));
                if (batchSerial != null) {
                    serialMapping.setSalesSerial(batchSerial);
                }
            }

           

            saveOrUpdate(serialMapping);
            list.add(serialMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Batch Mapping updated successfully.", null, list, list.size());
    }
    @Override
    public void saveSerialAmountDue(HashMap<String, Object> productSerialMap) throws ServiceException {
        try {
            NewBatchSerial newBatchSerial = new NewBatchSerial();
            String itemID = (String) productSerialMap.get("id");
            if (productSerialMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                newBatchSerial = (NewBatchSerial) get(NewBatchSerial.class, itemID);
                 if (productSerialMap.containsKey("qty") && !StringUtil.isNullOrEmpty((String) productSerialMap.get("qty"))) {
                    Double itemQty = Double.parseDouble((String) productSerialMap.get("qty"));
                    newBatchSerial.setQuantitydue(newBatchSerial.getQuantitydue() + itemQty);
                }
                if (productSerialMap.containsKey("lockquantity") && !StringUtil.isNullOrEmpty((String) productSerialMap.get("lockquantity"))) {
                    Double lockQty = Double.parseDouble((String) productSerialMap.get("lockquantity"));
                    if(lockQty == -1 ){
                        if(newBatchSerial.getLockquantity() > 0){
                            newBatchSerial.setLockquantity(newBatchSerial.getLockquantity() + lockQty);
                        }
                    }else{
                        newBatchSerial.setLockquantity(newBatchSerial.getLockquantity() + lockQty);
                    }
                }
                if (productSerialMap.containsKey("consignquantity") && !StringUtil.isNullOrEmpty((String) productSerialMap.get("consignquantity"))) {
                    Double consignQty = Double.parseDouble((String) productSerialMap.get("consignquantity"));
                    newBatchSerial.setConsignquantity(newBatchSerial.getConsignquantity() + consignQty);
                }
                if (productSerialMap.containsKey("isForconsignment")) {
                    boolean isForconsignment = (Boolean) productSerialMap.get("isForconsignment");
                    newBatchSerial.setIsForconsignment(isForconsignment);
                }
                if (productSerialMap.containsKey("purchasereturn") && productSerialMap.get("purchasereturn") != null) {
                    boolean purchasereturn =(Boolean) productSerialMap.get("purchasereturn");
                    newBatchSerial.setIspurchasereturn(purchasereturn);
                }
//                if (productSerialMap.containsKey("requestpendingapproval") && productSerialMap.get("requestpendingapproval") != null) {
//                    newBatchSerial.setRequestApprovalStatus((RequestApprovalStatus)productSerialMap.get("requestpendingapproval"));
//                }
//                if (productSerialMap.containsKey("approver") && productSerialMap.get("approver") != null ) {
//                    newBatchSerial.setApprover((User)productSerialMap.get("approver"));
//                }
                if (productSerialMap.containsKey("isQAinspection") && productSerialMap.get("isQAinspection") != null) {
                    boolean isQAinspection =(Boolean) productSerialMap.get("isQAinspection");
                    if(isQAinspection){
                     newBatchSerial.setQaApprovalstatus(QaApprovalStatus.PENDING);
                    }

                }
            }
            saveOrUpdate(newBatchSerial);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveSerialAmountDue : " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public KwlReturnObject saveBatchDocumentMapping(HashMap<String, Object> productbatchMap) throws AccountingException,ServiceException {
        List list = new ArrayList();
        try {
            LocationBatchDocumentMapping locationBatchDocumentMapping = new LocationBatchDocumentMapping();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                locationBatchDocumentMapping = (LocationBatchDocumentMapping) get(LocationBatchDocumentMapping.class, itemID);
                if (locationBatchDocumentMapping == null) {
                    locationBatchDocumentMapping = new LocationBatchDocumentMapping();
                    locationBatchDocumentMapping.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                locationBatchDocumentMapping.setId(newitemID);
            }


            if (productbatchMap.containsKey("batchmapid")) {
                NewProductBatch newProductBatch = new NewProductBatch();
                newProductBatch = (NewProductBatch) get(NewProductBatch.class, (String) productbatchMap.get("batchmapid"));
                if (newProductBatch != null) {
                    locationBatchDocumentMapping.setBatchmapid(newProductBatch);
                } else {
                    throw new AccountingException(" Batch id should not be null or empty");
                }

            }

            if (productbatchMap.containsKey("transactiontype") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("transactiontype"))) {
                int typeNo = Integer.parseInt((String) productbatchMap.get("transactiontype"));
                locationBatchDocumentMapping.setTransactiontype(typeNo);
            }

            if (productbatchMap.containsKey("documentid")) {
                locationBatchDocumentMapping.setDocumentid((String) productbatchMap.get("documentid"));
            }
            if (productbatchMap.containsKey("batchsequence")) {
                locationBatchDocumentMapping.setSelectedsequence((int) productbatchMap.get("batchsequence"));
            }
            if (productbatchMap.containsKey("purchasereturn")) {
                locationBatchDocumentMapping.setIspurchasereturn(true);
            }
              if (productbatchMap.containsKey("isConsignment") && productbatchMap.get("isConsignment") != null) {
                locationBatchDocumentMapping.setIsconsignment((Boolean) productbatchMap.get("isConsignment"));
            }
           
             if (productbatchMap.containsKey("mfgdate")) {
                locationBatchDocumentMapping.setMfgdate((Date) productbatchMap.get("mfgdate"));
            }
            if (productbatchMap.containsKey("expdate")) {
                locationBatchDocumentMapping.setExpdate((Date) productbatchMap.get("expdate"));
            }
             if (productbatchMap.containsKey("quantity") && !StringUtil.isNullOrEmpty(""+ productbatchMap.get("quantity"))) {
                double qty = Double.parseDouble(""+productbatchMap.get("quantity"));
                locationBatchDocumentMapping.setQuantity(qty);
            }
            if (productbatchMap.containsKey("approvedqty") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("approvedqty"))) {
                double approvedqty = Double.parseDouble((String) productbatchMap.get("approvedqty"));
                locationBatchDocumentMapping.setApprovedQuantity(approvedqty);
            }
            if (productbatchMap.containsKey("requestpendingapproval") && productbatchMap.get("requestpendingapproval") != null) {
                locationBatchDocumentMapping.setRequestApprovalStatus((RequestApprovalStatus) productbatchMap.get("requestpendingapproval"));
            }
            if (productbatchMap.containsKey("approver") && productbatchMap.get("approver") != null) {
                locationBatchDocumentMapping.setApproverSet((Set<User>) productbatchMap.get("approver"));
            }
            if (productbatchMap.containsKey("stocktype") && !StringUtil.isNullOrEmpty((String)productbatchMap.get("stocktype")) && (!StringUtil.isNullOrEmpty((String) productbatchMap.get("stocktype")))) {
                locationBatchDocumentMapping.setStockType(Integer.parseInt((String) productbatchMap.get("stocktype")));
            }else{
                locationBatchDocumentMapping.setStockType(1);
            }

            saveOrUpdate(locationBatchDocumentMapping);
            list.add(locationBatchDocumentMapping);
        }  catch (AccountingException ex) {
            throw  ex;
        }catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchDocumentMapping : " + ex.getMessage(), ex);
        } 
        return new KwlReturnObject(true, "Batch Mapping updated successfully.", null, list, list.size());
    }
    public KwlReturnObject getGridConfig(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {
            String companyid=requestParams.get("companyid").toString();
            String userid=requestParams.get("userid").toString();
            String moduleid = requestParams.get("moduleid").toString();
            boolean isdocumentEntryForm=Boolean.parseBoolean(requestParams.get("isdocumentEntryForm").toString());
            
            String hql = "from GridConfig where moduleid=? and user.userID=? and company.companyID=? and isDocumentEntryForm=? ORDER BY updatedOn DESC";
            ll = executeQuery(hql, new Object[]{moduleid, userid,companyid,isdocumentEntryForm});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.getGridConfig : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "002", "", ll, dl);
    }
    
    public GridConfig saveGridConfig(JSONObject jobj) throws ServiceException{
        GridConfig cm = null;
        try {
            cm = (GridConfig) get(GridConfig.class, jobj.getString("cid"));
            if (cm == null) {
                cm = new GridConfig();
                if (jobj.has("cmuuid")) {
                    cm.setCid(jobj.getString("cmuuid"));
                }
                if (jobj.has("moduleid")) {
                    cm.setModuleid(jobj.getString("moduleid"));
                }
                if (jobj.has("userid")) {
                    cm.setUser((User) get(User.class, jobj.getString("userid")));
                }
                if (jobj.has("companyid")) {
                    cm.setCompany((Company) get(Company.class, jobj.getString("companyid")));
                }
//                if (jobj.has("isnewconfigsaved")) {
//                    cm.setIsNewConfigSaved(jobj.optBoolean("isnewconfigsaved"));
//                }
            }
            if (jobj.has("state")) {
                cm.setState(jobj.getString("state"));
            }
            if (jobj.has("rule")) {
                cm.setRules(jobj.getString("rule"));
            }
            if (jobj.has("isdocumentEntryForm")) {
                cm.setIsDocumentEntryForm(jobj.optBoolean("isdocumentEntryForm", false));
            }
            if (jobj.has("updatedon")) {
                cm.setUpdatedOn(System.currentTimeMillis());
            }
//            if (jobj.has("isnewconfigsaved")) {
//                cm.setIsNewConfigSaved(cm.isIsNewConfigSaved() ? cm.isIsNewConfigSaved() : jobj.optBoolean("isnewconfigsaved"));
//            }
            saveOrUpdate(cm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveGridConfig : " + e.getMessage(), e);
        }
        return cm;
    }
    
    @Override
    public KwlReturnObject saveSerialDocumentMapping(HashMap<String, Object> productSerialMap) throws ServiceException {
        List list = new ArrayList();
        try {
            SerialDocumentMapping serialDocumentMapping = new SerialDocumentMapping();
            String itemID = (String) productSerialMap.get("id");
            if (productSerialMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                serialDocumentMapping = (SerialDocumentMapping) get(SerialDocumentMapping.class, itemID);
                if (serialDocumentMapping == null) {
                    serialDocumentMapping = new SerialDocumentMapping();
                    serialDocumentMapping.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                serialDocumentMapping.setId(newitemID);
            }


            if (productSerialMap.containsKey("serialmapid")) {
                NewBatchSerial newBatchSerial = new NewBatchSerial();
                newBatchSerial = (NewBatchSerial) get(NewBatchSerial.class, (String) productSerialMap.get("serialmapid"));
                if (newBatchSerial != null) {
                    serialDocumentMapping.setSerialid(newBatchSerial);
                }
            }

            if (productSerialMap.containsKey("transactiontype") && !StringUtil.isNullOrEmpty((String) productSerialMap.get("transactiontype"))) {
                int typeNo = Integer.parseInt((String) productSerialMap.get("transactiontype"));
                serialDocumentMapping.setTransactiontype(typeNo);
            }
            if (productSerialMap.containsKey("stocktype") && !StringUtil.isNullOrEmpty(productSerialMap.get("stocktype").toString())) {
                int stockType = Integer.parseInt(productSerialMap.get("stocktype").toString());
                serialDocumentMapping.setStockType(stockType);
            }else{
                serialDocumentMapping.setStockType(1);
            }

            if (productSerialMap.containsKey("documentid")) {
                serialDocumentMapping.setDocumentid((String) productSerialMap.get("documentid"));
            }
            if (productSerialMap.containsKey("serialsequence")) {
                serialDocumentMapping.setSelectedsequence((int) productSerialMap.get("serialsequence"));
            }
             if (productSerialMap.containsKey("expfromdate")) {
                serialDocumentMapping.setExpfromdate((Date) productSerialMap.get("expfromdate"));
            }
            if (productSerialMap.containsKey("exptodate")) {
                serialDocumentMapping.setExptodate((Date) productSerialMap.get("exptodate"));
            }
             if (productSerialMap.containsKey("purchasereturn")) {
                serialDocumentMapping.setIspurchasereturn(true);
            }
            if (productSerialMap.containsKey("isConsignment") && productSerialMap.get("isConsignment") != null) {
                serialDocumentMapping.setIsconsignment((Boolean) productSerialMap.get("isConsignment"));
            }
            if (productSerialMap.containsKey("requestpendingapproval") && productSerialMap.get("requestpendingapproval") != null) {
                serialDocumentMapping.setRequestApprovalStatus((RequestApprovalStatus) productSerialMap.get("requestpendingapproval"));
            }
            if (productSerialMap.containsKey("approver") && productSerialMap.get("approver") != null) {
                serialDocumentMapping.setApproverSet((Set<User>) productSerialMap.get("approver"));
            }
            if (productSerialMap.containsKey("reusablecount") && productSerialMap.get("reusablecount") != null) {
                double usedCount = serialDocumentMapping.getReusablecount();
                double currentUsedCount = Double.parseDouble(productSerialMap.get("reusablecount").toString());
                double totalUsedCount = usedCount + currentUsedCount;
                serialDocumentMapping.setReusablecount(totalUsedCount);
            }

            saveOrUpdate(serialDocumentMapping);
            list.add(serialDocumentMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchDocumentMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Batch Mapping updated successfully.", null, list, list.size());
    }
        public KwlReturnObject updateserialcustomdata(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String serialcustomdataref = (String) requestParams.get("serialcustomdataref");
            SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) get(SerialDocumentMapping.class, serialcustomdataref);
            if (requestParams.containsKey("serialcustomdataref")) {
                SerialCustomData serialCustomData = null;
                serialCustomData = (SerialCustomData) get(SerialCustomData.class, (String) requestParams.get("serialcustomdataref"));
                serialDocumentMapping.setSerialCustomData(serialCustomData);
            }
            saveOrUpdate(serialDocumentMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateDeliveryOrderCustomData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    @Override
    public KwlReturnObject saveReturnSerialMapping(HashMap<String, Object> productbatchMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ReturnSerialMapping serialMapping = new ReturnSerialMapping();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                serialMapping = (ReturnSerialMapping) get(ReturnSerialMapping.class, itemID);
                if (serialMapping == null) {
                    serialMapping = new ReturnSerialMapping();
                    serialMapping.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                serialMapping.setId(newitemID);
            }


            if (productbatchMap.containsKey("maptoserialid")) {
                BatchSerial batchSerial = new BatchSerial();
                batchSerial = (BatchSerial) get(BatchSerial.class, (String) productbatchMap.get("maptoserialid"));
                if (batchSerial != null) {
                    serialMapping.setMaptoserial(batchSerial);
                }
            }

            if (productbatchMap.containsKey("mapserialid")) {
                BatchSerial batchSerial = new BatchSerial();
                batchSerial = (BatchSerial) get(BatchSerial.class, (String) productbatchMap.get("mapserialid"));
                if (batchSerial != null) {
                    serialMapping.setMapserial(batchSerial);
                }
            }
               if (productbatchMap.containsKey("returntype")) {
                if (productbatchMap.containsKey("returntype") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("returntype"))) {
                    int typeNo = Integer.parseInt((String) productbatchMap.get("returntype"));
                    serialMapping.setReturntype(typeNo);
                }
            }
            saveOrUpdate(serialMapping);
            list.add(serialMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Batch Mapping updated successfully.", null, list, list.size());
    }
    
     public KwlReturnObject deleteSalesPurchaseSerialMapping(String doserialid) throws ServiceException {
        String delQuery4 = "delete from batchserialmapping where salesSerial=? ";
        int numRows4 = executeSQLUpdate(delQuery4, new Object[]{doserialid});
        return new KwlReturnObject(true, "Delivery Order Serial no. has been deleted successfully.", null, null, numRows4);
    }
     public KwlReturnObject deleteReturnbatchSerialMapping(String doserialid) throws ServiceException {
        String delQuery4 = "delete from returnserialmapping where mapserial=? ";
        int numRows4 = executeSQLUpdate(delQuery4, new Object[]{doserialid});
        return new KwlReturnObject(true, "salesreturn Serial no. mapping has been deleted successfully.", null, null, numRows4);
    }
    
    
    public KwlReturnObject saveBatchMapping(HashMap<String, Object> productbatchMap) throws ServiceException {
        List list = new ArrayList();
        try {
            SalesPurchaseBatchMapping batchMapping = new SalesPurchaseBatchMapping();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                batchMapping = (SalesPurchaseBatchMapping) get(SalesPurchaseBatchMapping.class, itemID);
                if (batchMapping == null) {
                    batchMapping = new SalesPurchaseBatchMapping();
                    batchMapping.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                batchMapping.setId(newitemID);
            }


            if (productbatchMap.containsKey("purchasebatchid")) {
                ProductBatch productBatch = new ProductBatch();
                productBatch = (ProductBatch) get(ProductBatch.class, (String) productbatchMap.get("purchasebatchid"));
                if (productBatch != null) {
                    batchMapping.setPurchaseBatch(productBatch);
                }
            }

            if (productbatchMap.containsKey("salesbatchid")) {
                ProductBatch productBatch = new ProductBatch();
                productBatch = (ProductBatch) get(ProductBatch.class, (String) productbatchMap.get("salesbatchid"));
                if (productBatch != null) {
                    batchMapping.setSalesBatch(productBatch);
                }
            }
        
            if (productbatchMap.containsKey("quantity") && !StringUtil.isNullOrEmpty(productbatchMap.get("quantity").toString())) {
                double qty = Double.parseDouble(productbatchMap.get("quantity").toString());
                batchMapping.setQuantity(qty);
            }
            saveOrUpdate(batchMapping);
            list.add(batchMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Batch Mapping updated successfully.", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject saveReturnBatchMapping(HashMap<String, Object> productbatchMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ReturnBatchMapping batchMapping = new ReturnBatchMapping();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                batchMapping = (ReturnBatchMapping) get(ReturnBatchMapping.class, itemID);
                if (batchMapping == null) {
                    batchMapping = new ReturnBatchMapping();
                    batchMapping.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                batchMapping.setId(newitemID);
            }
            
            
            if (productbatchMap.containsKey("batchtomap")) {
                ProductBatch productBatch = new ProductBatch();
                productBatch = (ProductBatch) get(ProductBatch.class, (String) productbatchMap.get("batchtomap"));
                if (productBatch != null) {
                    batchMapping.setBatchtomap(productBatch);
                }
            }
            
            if (productbatchMap.containsKey("batchmap")) {
                ProductBatch productBatch = new ProductBatch();
                productBatch = (ProductBatch) get(ProductBatch.class, (String) productbatchMap.get("batchmap"));
                if (productBatch != null) {
                    batchMapping.setBatchmap(productBatch);
                }
            }
            if (productbatchMap.containsKey("returntype")) {
                if (productbatchMap.containsKey("returntype") && !StringUtil.isNullOrEmpty((String) productbatchMap.get("returntype"))) {
                    int typeNo = Integer.parseInt((String) productbatchMap.get("returntype"));
                    batchMapping.setReturntype(typeNo);
                }
            }
            
           
        if (productbatchMap.containsKey("quantity") && !StringUtil.isNullOrEmpty(productbatchMap.get("quantity").toString())) {
                double qty = Double.parseDouble(productbatchMap.get("quantity").toString());
                batchMapping.setQuantity(qty);
            }
            saveOrUpdate(batchMapping);
            list.add(batchMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Return Batch Mapping updated successfully.", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject saveSerialForBatch(HashMap<String, Object> productserialMap) throws ServiceException {
        List list = new ArrayList();
        try {
            BatchSerial batchSerial = new BatchSerial();
            String itemID = (String) productserialMap.get("id");
            if (productserialMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                batchSerial = (BatchSerial) get(BatchSerial.class, itemID);
                if (batchSerial == null) {
                    batchSerial = new BatchSerial();
                    batchSerial.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                batchSerial.setId(newitemID);
            }
    
            if (productserialMap.containsKey("name")) {
                batchSerial.setName((String) productserialMap.get("name"));
            }
    
            if (productserialMap.containsKey("expfromdate")) {
                batchSerial.setExpfromdate((Date) productserialMap.get("expfromdate"));
            }
            if (productserialMap.containsKey("exptodate")) {
                batchSerial.setExptodate((Date) productserialMap.get("exptodate"));
            }
            
            if (productserialMap.containsKey("product")) {
                batchSerial.setProduct((String) productserialMap.get("product"));
            }
            if (productserialMap.containsKey("batch")) {
                ProductBatch  productBatch= new ProductBatch();
                productBatch = (ProductBatch) get(ProductBatch.class, (String) productserialMap.get("batch"));
                if (productBatch != null) {
                    batchSerial.setBatch(productBatch);
                }
            }
            if (productserialMap.containsKey("ispurchase")) {
                boolean ispurchase = (Boolean) productserialMap.get("ispurchase");
                batchSerial.setIspurchase(ispurchase);
            }
            if (productserialMap.containsKey("transactiontype") && !StringUtil.isNullOrEmpty((String) productserialMap.get("transactiontype"))) {
                int typeNo = Integer.parseInt((String) productserialMap.get("transactiontype"));
                batchSerial.setTransactiontype(typeNo);
            }
            if (productserialMap.containsKey("companyid")) {
                Company company = new Company();
                company = (Company) get(Company.class, (String) productserialMap.get("companyid"));
                if (company != null) {
                    batchSerial.setCompany(company);
                }
            }
          
            saveOrUpdate(batchSerial);
            list.add(batchSerial);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveSerialForBatch : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Batch Serial updated successfully.", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject saveNewSerialForBatch(HashMap<String, Object> productserialMap) throws ServiceException {
        List list = new ArrayList();
        try {
            NewBatchSerial batchSerial = new NewBatchSerial();
            String itemID = (String) productserialMap.get("id");
            boolean isUnbuildAssembly = productserialMap.get("isUnbuildAssembly")!=null ? (Boolean)productserialMap.get("isUnbuildAssembly") : false;
            if (productserialMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                batchSerial = (NewBatchSerial) get(NewBatchSerial.class, itemID);
                /*We have item id, So we get NewBatchSerial obj. For existing object, we cannot set new primary id.
                  We get here duplicate identifier for existing object exception. As discussed with Malhari P. sir, I am bypassing 
                  * this condition by using Unbuild assembly check.
                */
                if (batchSerial == null && !isUnbuildAssembly) {    
                    batchSerial = new NewBatchSerial();
                    batchSerial.setId(itemID);
                } else if(isUnbuildAssembly){
                    batchSerial.setId(itemID);
                } else {
                    
                    if (batchSerial == null) {
                        String newitemID = UUID.randomUUID().toString();
                        batchSerial.setId(newitemID);
                    }
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                batchSerial.setId(newitemID);
            }
    
            if (productserialMap.containsKey("name")) {
                batchSerial.setSerialname((String) productserialMap.get("name"));
            }
            if (productserialMap.containsKey("skufield")) {
                batchSerial.setSkufield((String) productserialMap.get("skufield"));
            }
    
            if (productserialMap.containsKey("expfromdate")) {
                batchSerial.setExpfromdate((Date) productserialMap.get("expfromdate"));
            }
            if (productserialMap.containsKey("exptodate")) {
                batchSerial.setExptodate((Date) productserialMap.get("exptodate"));
            }
            
            if (productserialMap.containsKey("product")) {
                batchSerial.setProduct((String) productserialMap.get("product"));
            }
            if (productserialMap.containsKey("asset")) {
                batchSerial.setAsset((String) productserialMap.get("asset"));
            }
            if (productserialMap.containsKey("quantity")) {
               double qty = Double.parseDouble((String) productserialMap.get("quantity"));
                batchSerial.setQuantity(qty);
                batchSerial.setQuantitydue(qty);
            }
            if (productserialMap.containsKey("qty")) {
               double qty = Double.parseDouble((String) productserialMap.get("qty"));
//                batchSerial.setQuantity(qty);
                batchSerial.setQuantitydue(qty);
            }
            if (productserialMap.containsKey("batch")) {
                NewProductBatch  productBatch= new NewProductBatch();
                productBatch = (NewProductBatch) get(NewProductBatch.class, (String) productserialMap.get("batch"));
                if (productBatch != null) {
                    batchSerial.setBatch(productBatch);
                }
            }
            if (productserialMap.containsKey("ispurchase")) {
                boolean ispurchase = (Boolean) productserialMap.get("ispurchase");
                batchSerial.setIspurchase(ispurchase);
            }
            if (productserialMap.containsKey("isopening")) {
                boolean ispurchase = (Boolean) productserialMap.get("isopening");
                batchSerial.setIsopening(ispurchase);
            }
            
            if (productserialMap.containsKey("transactiontype") && !StringUtil.isNullOrEmpty((String) productserialMap.get("transactiontype"))) {
                int typeNo = Integer.parseInt((String) productserialMap.get("transactiontype"));
                batchSerial.setTransactiontype(typeNo);
            }
            if (productserialMap.containsKey("companyid")) {
                Company company = new Company();
                company = (Company) get(Company.class, (String) productserialMap.get("companyid"));
                if (company != null) {
                    batchSerial.setCompany(company);
                }
            }
            if (productserialMap.containsKey("isConsignment") && productserialMap.get("isConsignment") != null) {
                batchSerial.setIsconsignment((Boolean) productserialMap.get("isConsignment"));
            }
          if (productserialMap.containsKey("consignquantity") && !StringUtil.isNullOrEmpty((String) productserialMap.get("consignquantity"))) {
                double consignquantity=Double.parseDouble((String)productserialMap.get("consignquantity"));
                batchSerial.setConsignquantity(consignquantity);
            }
            if (productserialMap.containsKey("isForconsignment") && productserialMap.get("isForconsignment") != null) {
                batchSerial.setIsForconsignment((Boolean) productserialMap.get("isForconsignment"));
            }
            if(productserialMap.containsKey("skuvalue")){
                batchSerial.setSkufield((String)productserialMap.get("skuvalue"));
            }
            if (productserialMap.containsKey("purchasereturn") && productserialMap.get("purchasereturn") != null) {
                batchSerial.setIspurchasereturn((Boolean) productserialMap.get("purchasereturn"));
            }
            saveOrUpdate(batchSerial);
            list.add(batchSerial);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveSerialForBatch : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Batch Serial updated successfully.", null, list, list.size());
    }
    
    public KwlReturnObject saveAssembySerialBatchMapping(HashMap<String, Object> productserialMap) throws ServiceException {
        List list = new ArrayList();
        try {
            AssemblySubProductBatchSerialMapping batchMapping = new AssemblySubProductBatchSerialMapping();
            String itemID = (String) productserialMap.get("id");
            if (productserialMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                batchMapping = (AssemblySubProductBatchSerialMapping) get(AssemblySubProductBatchSerialMapping.class, itemID);
                if (batchMapping == null) {
                    batchMapping = new AssemblySubProductBatchSerialMapping();
                    batchMapping.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                batchMapping.setId(newitemID);
            }

            if (productserialMap.containsKey("subproductbatch")) {
                  ProductBatch  productBatch= new ProductBatch();
                productBatch = (ProductBatch) get(ProductBatch.class, (String) productserialMap.get("subproductbatch"));
                if (productBatch != null) {
                    batchMapping.setSubproductbatch(productBatch);
                }
            }
            if (productserialMap.containsKey("mainproductserial")) {
                  BatchSerial  batchSerial= new BatchSerial();
                batchSerial = (BatchSerial) get(BatchSerial.class, (String) productserialMap.get("mainproductserial"));
                if (batchSerial != null) {
                    batchMapping.setMainproductserial(batchSerial);
                }
            }
           
            saveOrUpdate(batchMapping);
            list.add(batchMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveAssembySerialBatchMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Assembly Batch Serial Mapping updated successfully.", null, list, list.size());
    }
   /* public KwlReturnObject saveAssembyProductBatchMapping(HashMap<String, Object> productserialMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ProductBuild productBuild = new ProductBuild();
            String itemID = (String) productserialMap.get("productbuild");
            if (productserialMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                productBuild = (ProductBuild) get(ProductBuild.class, itemID);
                if (productBuild == null) {
                    productBuild = new ProductBuild();
                    productBuild.setID(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                productBuild.setID(newitemID);
            }

          
            if (productserialMap.containsKey("productbatch")) {
                  ProductBatch  productBatch= new ProductBatch();
                 productBatch = (ProductBatch) get(ProductBatch.class, (String) productserialMap.get("productbatch"));
                if (productBatch != null) {
                    productBuild.setBatch(productBatch);
                }
            }
           
            saveOrUpdate(productBuild);
            list.add(productBuild);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveAssembySerialBatchMapping : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Assembly Batch Serial Mapping updated successfully.", null, list, list.size());
    }*/
    public KwlReturnObject getSerialForBatch(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BatchSerial";
        return buildNExecuteQuery(query, requestParams);
    }
    public KwlReturnObject getNewSerialForBatch(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from NewBatchSerial";
        return buildNExecuteQuery(query, requestParams);
    }
    
     public Double getInvQuantity(String docid, String productid,boolean isVenQty) throws ServiceException {
            double batchQuantity = 1;
            String mysql="";
            String conQuery="";
            List paList=new ArrayList();
            if(isVenQty){
             mysql = "select sum(venconsignuomquantity) from inventory where  product=?";
             paList.add(productid);
            }else{
                mysql = "select sum(quantity) from inventory where  product=?";
                paList.add(productid);
            }
            if(!StringUtil.isNullOrEmpty(docid)){
                mysql+=" And id=?";
                paList.add(docid);
            }
            List list = executeSQLQuery(mysql, paList.toArray());
            if (list.size() > 0) {
                batchQuantity = (Double) list.get(0);
            }
            return batchQuantity;
        }
     
    public KwlReturnObject getBatchSerialDetails(String documentid, boolean addlocationmappcheck, boolean linkingFlag, String moduleID, boolean isConsignment, boolean isEdit,String documentIDs) throws ServiceException {
        String addCheck = "";
        String linkCondition = "";
        String ConditionForOr = "";
        String consignmentCondition = "";
        String groupByCheck = " group by newbatchserial.id";
        String orderByCheck = " order by newproductbatch.id";//for fetching batch wise data incase of multiple batch serial 
        boolean isSerialForProduct = !addlocationmappcheck; // As we pass parameters !isSerialForProduct = addlocationmappcheck
        if (isSerialForProduct) {
            /**
             * while fetch data edit/view of DO then sort Batch/Serial according
             * to User Selected Sequence.
             */
            orderByCheck = " order by  IF(locationbatchdocumentmapping.selectedsequence =0 and serialdocumentmapping.selectedsequence =0, newproductbatch.id, locationbatchdocumentmapping.selectedsequence), "
                    + "IF(serialdocumentmapping.selectedsequence =0, null, serialdocumentmapping.selectedsequence) ";
            orderByCheck += ", serialdocumentmapping.stocktype";
        } else {
            orderByCheck = " order by  IF(locationbatchdocumentmapping.selectedsequence =0 , newproductbatch.id, locationbatchdocumentmapping.selectedsequence) ";
        }
        String wastageColumn = "";
        if (addlocationmappcheck) {
            if (!documentIDs.equals("")) {
                addCheck = " locationbatchdocumentmapping.documentid in (" + documentIDs + ") ";
//                groupByCheck = " group by locationbatchdocumentmapping.documentid";
            } else {
                addCheck = " locationbatchdocumentmapping.documentid= '" + documentid + "' ";
            }
            groupByCheck = " GROUP by locationbatchdocumentmapping.documentid,newproductbatch.id,locationbatchdocumentmapping.stocktype ";
        }
        if (linkingFlag) {
            if (!StringUtil.isNullOrEmpty(moduleID)) {
                int moduleid = Integer.parseInt(moduleID);
                if (moduleid == Constants.Acc_Purchase_Return_ModuleId || moduleid == Constants.Acc_Invoice_ModuleId) {
                    linkCondition = " and (newbatchserial.quantitydue>0.0 or newproductbatch.quantitydue>0.0) ";
                }else if(moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId){
                    linkCondition = " and locationbatchdocumentmapping.transactiontype=27 ";
                } else if(isConsignment && moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId){
//                    linkCondition = " and (serialdocumentmapping.consignmentapprovalstatus=1 or serialdocumentmapping.consignmentapprovalstatus is NULL ) and newbatchserial.quantitydue>0.0 ";
                    if (!addlocationmappcheck) {
                    linkCondition  += " and (serialdocumentmapping.consignmentapprovalstatus=1 or serialdocumentmapping.consignmentapprovalstatus is NULL )";
                    }
                }
//                else if(moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId){
//                    linkCondition = " and (newbatchserial.consignmentapprovalstatus=1 or newbatchserial.consignmentapprovalstatus is NULL )";
//                }
            }
        } else if (!StringUtil.isNullOrEmpty(moduleID) && isEdit) {
            int moduleid = Integer.parseInt(moduleID);
            if (moduleid == Constants.Acc_FixedAssets_DeliveryOrder_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=41 ";
            } else if (moduleid == Constants.Acc_FixedAssets_GoodsReceipt_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=40 ";
            }else if (moduleid == Constants.Acc_Delivery_Order_ModuleId ||moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=27 ";
            } else if (moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId && isConsignment) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=29 ";
            } else if (isConsignment && moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                if (!addlocationmappcheck) {
              linkCondition = " and (serialdocumentmapping.consignmentapprovalstatus=1 or serialdocumentmapping.consignmentapprovalstatus is NULL )";
                }
            } else if (moduleid == Constants.MRP_WORK_ORDER_MODULEID){
                /*
                 * Get only block quantity entry from locationbatchdocumentmapping table in Work order EDIT case.
                 */                
                if(addlocationmappcheck) {
                  linkCondition = " and locationbatchdocumentmapping.transactiontype=20 ";
                }
            
            }
        }

        if (isSerialForProduct) {
            wastageColumn = " newbatchserial.wastagequantitytype,newbatchserial.wastagequantity ";
        } else {
            wastageColumn = " newproductbatch.wastagequantitytype,newproductbatch.wastagequantity ";
        }
//        if (isConsignment) {
//            consignmentCondition = " and serialdocumentmapping.isconsignment='T' ";
//        }
//        } else {
//            consignmentCondition = " and serialdocumentmapping.isconsignment='F' ";
//        }
        String query = "";
        if (addlocationmappcheck) {
            query = "select  newproductbatch.id,newproductbatch.batchname,newproductbatch.location,newproductbatch.warehouse,newproductbatch.mfgdate,"
                + " newproductbatch.expdate,newproductbatch.quantity,newbatchserial.id as serialid,newbatchserial.serialname,'','',"
                + " SUM(locationbatchdocumentmapping.quantity) as locationmappqty,newproductbatch.lockquantity,newbatchserial.skufield,'',newproductbatch.row,"
                + " newproductbatch.rack,newproductbatch.bin," + wastageColumn + ", locationbatchdocumentmapping.documentid,locationbatchdocumentmapping.stocktype,"
                + " locationbatchdocumentmapping.stocktype AS srstocktype "
                + " from newproductbatch "
                + " left join newbatchserial on newproductbatch.id=newbatchserial.batch "
                + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                + " where  " + addCheck + linkCondition + groupByCheck + orderByCheck;
        }else{
            query = "select distinct newproductbatch.id,newproductbatch.batchname,newproductbatch.location,newproductbatch.warehouse,newproductbatch.mfgdate,"
                + " newproductbatch.expdate,newproductbatch.quantity,newbatchserial.id as serialid,newbatchserial.serialname,"
                + " serialdocumentmapping.expfromdate,serialdocumentmapping.exptodate,locationbatchdocumentmapping.quantity as locationmappqty,"
                + " newproductbatch.lockquantity,newbatchserial.skufield,serialdocumentmapping.accserialcustomdataref,newproductbatch.row,newproductbatch.rack,"
                + " newproductbatch.bin," + wastageColumn + ",locationbatchdocumentmapping.documentid,locationbatchdocumentmapping.stocktype,"
                + " serialdocumentmapping.stocktype AS srstocktype  from newproductbatch "
                + " left join newbatchserial on newproductbatch.id=newbatchserial.batch "
                + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                + " left join serialdocumentmapping on serialdocumentmapping.serialid=newbatchserial.id "
                + " where  " + addCheck + " serialdocumentmapping.documentid=? AND locationbatchdocumentmapping.stocktype=serialdocumentmapping.stocktype " + ConditionForOr + linkCondition + groupByCheck + consignmentCondition+orderByCheck;
        }
        List list = executeSQLQuery(query, addlocationmappcheck? new Object[]{} : new Object[]{documentid});
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }    
    /**
     *  Return Consignment Batch Details
     * @return
     * @throws ServiceException 
     */ 
    @Override
    public KwlReturnObject getConsignmentBatchSerialDetails(String documentid, boolean addlocationmappcheck, boolean linkingFlag, String moduleID, boolean isConsignment, boolean isEdit,String documentIDs) throws ServiceException {
        String addCheck = "";
        String linkCondition = "";
        String ConditionForOr = "";
        String consignmentCondition = "";
        String groupByCheck = " group by newbatchserial.id";
        String orderByCheck = " order by newproductbatch.id";//for fetching batch wise data incase of multiple batch serial 
        boolean isSerialForProduct = !addlocationmappcheck; // As we pass parameters !isSerialForProduct = addlocationmappcheck
        if (isSerialForProduct) {
            orderByCheck = orderByCheck + ", serialdocumentmapping.stocktype";
        }
        String wastageColumn = "";
        if (addlocationmappcheck) {
            if (!documentIDs.equals("")) {
                addCheck = " locationbatchdocumentmapping.documentid in (" + documentIDs + ") ";
//                groupByCheck = " group by locationbatchdocumentmapping.documentid";
            } else {
                addCheck = " locationbatchdocumentmapping.documentid= '" + documentid + "' ";
            }
            groupByCheck = " GROUP by locationbatchdocumentmapping.documentid,newproductbatch.id,locationbatchdocumentmapping.stocktype ";
        }
        if (linkingFlag) {
            if (!StringUtil.isNullOrEmpty(moduleID)) {
                int moduleid = Integer.parseInt(moduleID);
                if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                    linkCondition = " and (newbatchserial.quantitydue>0.0 or newproductbatch.quantitydue>0.0) ";
                }else if(moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId){
                    linkCondition = " and locationbatchdocumentmapping.transactiontype=27 ";
                } else if(isConsignment && moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId){
//                    linkCondition = " and (serialdocumentmapping.consignmentapprovalstatus=1 or serialdocumentmapping.consignmentapprovalstatus is NULL ) and newbatchserial.quantitydue>0.0 ";
                    linkCondition = " and locationbatchdocumentmapping.isconsignment='T' "; //return Only Consignemt Batch
                    if (!addlocationmappcheck) {
                    linkCondition  += " and (serialdocumentmapping.consignmentapprovalstatus=1 or serialdocumentmapping.consignmentapprovalstatus is NULL )";
                    }
                }
//                else if(moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId){
//                    linkCondition = " and (newbatchserial.consignmentapprovalstatus=1 or newbatchserial.consignmentapprovalstatus is NULL )";
//                }
            }
        } else if (!StringUtil.isNullOrEmpty(moduleID) && isEdit) {
            int moduleid = Integer.parseInt(moduleID);
            if (moduleid == Constants.Acc_FixedAssets_DeliveryOrder_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=41 ";
            } else if (moduleid == Constants.Acc_FixedAssets_GoodsReceipt_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=40 ";
            }else if (moduleid == Constants.Acc_Delivery_Order_ModuleId ||moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=27 ";
            } else if (moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId && isConsignment) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=29 ";
            } else if (isConsignment && moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                if (!addlocationmappcheck) {
              linkCondition = " and (serialdocumentmapping.consignmentapprovalstatus=1 or serialdocumentmapping.consignmentapprovalstatus is NULL )";
                }
            }
        }

        if (isSerialForProduct) {
            wastageColumn = " newbatchserial.wastagequantitytype,newbatchserial.wastagequantity ";
        } else {
            wastageColumn = " newproductbatch.wastagequantitytype,newproductbatch.wastagequantity ";
        }
//        if (isConsignment) {
//            consignmentCondition = " and serialdocumentmapping.isconsignment='T' ";
//        }
//        } else {
//            consignmentCondition = " and serialdocumentmapping.isconsignment='F' ";
//        }
        String query = "";
        if (addlocationmappcheck) {
            query = "select  newproductbatch.id,newproductbatch.batchname,newproductbatch.location,newproductbatch.warehouse,newproductbatch.mfgdate,"
                + " newproductbatch.expdate,newproductbatch.quantity,newbatchserial.id as serialid,newbatchserial.serialname,'','',"
                + " SUM(locationbatchdocumentmapping.quantity) as locationmappqty,newproductbatch.lockquantity,newbatchserial.skufield,'',newproductbatch.row,"
                + " newproductbatch.rack,newproductbatch.bin," + wastageColumn + ", locationbatchdocumentmapping.documentid,locationbatchdocumentmapping.stocktype,"
                + " locationbatchdocumentmapping.stocktype AS srstocktype "
                + " from newproductbatch "
                + " left join newbatchserial on newproductbatch.id=newbatchserial.batch "
                + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                + " where  " + addCheck + linkCondition + groupByCheck + orderByCheck;
        }else{
            query = "select distinct newproductbatch.id,newproductbatch.batchname,newproductbatch.location,newproductbatch.warehouse,newproductbatch.mfgdate,"
                + " newproductbatch.expdate,newproductbatch.quantity,newbatchserial.id as serialid,newbatchserial.serialname,"
                + " serialdocumentmapping.expfromdate,serialdocumentmapping.exptodate,locationbatchdocumentmapping.quantity as locationmappqty,"
                + " newproductbatch.lockquantity,newbatchserial.skufield,serialdocumentmapping.accserialcustomdataref,newproductbatch.row,newproductbatch.rack,"
                + " newproductbatch.bin," + wastageColumn + ",locationbatchdocumentmapping.documentid,locationbatchdocumentmapping.stocktype,"
                + " serialdocumentmapping.stocktype AS srstocktype  from newproductbatch "
                + " left join newbatchserial on newproductbatch.id=newbatchserial.batch "
                + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                + " left join serialdocumentmapping on serialdocumentmapping.serialid=newbatchserial.id "
                + " where  " + addCheck + " serialdocumentmapping.documentid=? AND locationbatchdocumentmapping.stocktype=serialdocumentmapping.stocktype " + ConditionForOr + linkCondition + groupByCheck + consignmentCondition+orderByCheck;
        }
        List list = executeSQLQuery(query, addlocationmappcheck? new Object[]{} : new Object[]{documentid});
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    /**
     * This Function Returns only batch Details.
     * @param documentid
     * @param addlocationmappcheck
     * @param linkingFlag
     * @param moduleID
     * @param isConsignment
     * @param isEdit
     * @param documentIDs
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getBatchDetails(String documentid, boolean linkingFlag, String moduleID, boolean isConsignment, boolean isEdit, String documentIDs) throws ServiceException {
        String addCheck = "";
        String linkCondition = "";
        String groupByCheck = " group by newbatchserial.id";
        String orderByCheck = " order by newproductbatch.id";//for fetching batch wise data incase of multiple batch serial 
        String wastageColumn = "";
        if (!documentIDs.equals("")) {
            addCheck = " locationbatchdocumentmapping.documentid in (" + documentIDs + ") ";
        } else {
            addCheck = " locationbatchdocumentmapping.documentid= '" + documentid + "' ";
        }
        groupByCheck = " GROUP by locationbatchdocumentmapping.documentid,newproductbatch.id,locationbatchdocumentmapping.stocktype ";
        if (linkingFlag) {
            if (!StringUtil.isNullOrEmpty(moduleID)) {
                int moduleid = Integer.parseInt(moduleID);
                if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                    linkCondition = " and (newbatchserial.quantitydue>0.0 or newproductbatch.quantitydue>0.0) ";
                } else if (moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                    linkCondition = " and locationbatchdocumentmapping.transactiontype=27 ";
                }
            }
        } else if (!StringUtil.isNullOrEmpty(moduleID) && isEdit) {
            int moduleid = Integer.parseInt(moduleID);
            if (moduleid == Constants.Acc_FixedAssets_DeliveryOrder_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=41 ";
            } else if (moduleid == Constants.Acc_FixedAssets_GoodsReceipt_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=40 ";
            } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=27 ";
            } else if (moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId && isConsignment) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=29 ";
            }
        }

        wastageColumn = " newproductbatch.wastagequantitytype,newproductbatch.wastagequantity ";
        String query = "select  newproductbatch.id,newproductbatch.batchname,newproductbatch.location,newproductbatch.warehouse,newproductbatch.mfgdate,"
                + " newproductbatch.expdate,newproductbatch.quantity,'','','','',"
                + " SUM(locationbatchdocumentmapping.quantity) as locationmappqty,newproductbatch.lockquantity,'','',newproductbatch.row,"
                + " newproductbatch.rack,newproductbatch.bin," + wastageColumn + ", locationbatchdocumentmapping.documentid,locationbatchdocumentmapping.stocktype,"
                + " locationbatchdocumentmapping.stocktype AS srstocktype "
                + " from newproductbatch "
                //                + " left join newbatchserial on newproductbatch.id=newbatchserial.batch "
                + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                + " where  " + addCheck + linkCondition + groupByCheck + orderByCheck;

        List list = executeSQLQuery(query, new Object[]{});
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }

    public KwlReturnObject getSerialDetails(String documentid, boolean addlocationmappcheck, boolean linkingFlag, String moduleID, boolean isConsignment, boolean isEdit,String documentIDs) throws ServiceException {
        String addCheck = "";
        String linkCondition = "";
        String ConditionForOr = "";
        String consignmentCondition = "";
        String groupByCheck = " group by newbatchserial.id";
        String orderByCheck = " order by newproductbatch.id";//for fetching batch wise data incase of multiple batch serial 
        boolean isSerialForProduct = !addlocationmappcheck; // As we pass parameters !isSerialForProduct = addlocationmappcheck
        if (isSerialForProduct) {
            orderByCheck = orderByCheck + ", serialdocumentmapping.stocktype";
        }
        String wastageColumn = "";
        if (addlocationmappcheck) {
            if (!documentIDs.equals("")) {
                addCheck = " locationbatchdocumentmapping.documentid in (" + documentIDs + ") ";
//                groupByCheck = " group by locationbatchdocumentmapping.documentid";
            } else {
                addCheck = " locationbatchdocumentmapping.documentid= '" + documentid + "' ";
            }
            groupByCheck = " GROUP by locationbatchdocumentmapping.documentid,newproductbatch.id,locationbatchdocumentmapping.stocktype ";
        }
        if (linkingFlag) {
            if (!StringUtil.isNullOrEmpty(moduleID)) {
                int moduleid = Integer.parseInt(moduleID);
                if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                    linkCondition = " and (newbatchserial.quantitydue>0.0 or newproductbatch.quantitydue>0.0) ";
                }else if(moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId){
                    linkCondition = " and locationbatchdocumentmapping.transactiontype=27 ";
                } 
            }
        } else if (!StringUtil.isNullOrEmpty(moduleID) && isEdit) {
            int moduleid = Integer.parseInt(moduleID);
            if (moduleid == Constants.Acc_FixedAssets_DeliveryOrder_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=41 ";
            } else if (moduleid == Constants.Acc_FixedAssets_GoodsReceipt_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=40 ";
            }else if (moduleid == Constants.Acc_Delivery_Order_ModuleId ||moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=27 ";
            } else if (moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId && isConsignment) {
                linkCondition = " and locationbatchdocumentmapping.transactiontype=29 ";
            } 
        }

//        if (isSerialForProduct) {
//            wastageColumn = " newbatchserial.wastagequantitytype,newbatchserial.wastagequantity ";
//        } else {
            wastageColumn = " newproductbatch.wastagequantitytype,newproductbatch.wastagequantity ";
//        }
//        if (isConsignment) {
//            consignmentCondition = " and serialdocumentmapping.isconsignment='T' ";
//        }
//        } else {
//            consignmentCondition = " and serialdocumentmapping.isconsignment='F' ";
//        }
        String query = "";
        if (addlocationmappcheck) {
            query = "select  newproductbatch.id,newproductbatch.batchname,newproductbatch.location,newproductbatch.warehouse,newproductbatch.mfgdate,"
                + " newproductbatch.expdate,newproductbatch.quantity,'','','','',"
                + " SUM(locationbatchdocumentmapping.quantity) as locationmappqty,newproductbatch.lockquantity,'','',newproductbatch.row,"
                + " newproductbatch.rack,newproductbatch.bin," + wastageColumn + ", locationbatchdocumentmapping.documentid,locationbatchdocumentmapping.stocktype,"
                + " locationbatchdocumentmapping.stocktype AS srstocktype "
                + " from newproductbatch "
                + " left join newbatchserial on newproductbatch.id=newbatchserial.batch "
                + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                + " where  " + addCheck + linkCondition + groupByCheck + orderByCheck;
        }
        List list = executeSQLQuery(query, addlocationmappcheck? new Object[]{} : new Object[]{documentid});
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }

    public KwlReturnObject getOnlySerialDetails(String documentid, boolean linkingFlag, String moduleID, boolean isConsignment, boolean isEdit) throws ServiceException {
        String linkCondition = "";
        String consignmentCondition = "";
        List list = null;
        try {
            if (linkingFlag) {
                if (!StringUtil.isNullOrEmpty(moduleID)) {
                    int moduleid = Integer.parseInt(moduleID);
                    if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                        linkCondition = " and newbatchserial.quantitydue>0.0 ";
                }else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                        linkCondition = " and newbatchserial.quantitydue<=0.0 ";
                    }
                }
            }
            if (isConsignment) {
                consignmentCondition = " and serialdocumentmapping.isconsignment='T' ";
            } else {
                consignmentCondition = " and serialdocumentmapping.isconsignment='F' ";
            }
            String query = "select '','','','','','','',newbatchserial.id,newbatchserial.serialname,newbatchserial.expfromdate,newbatchserial.exptodate,newbatchserial.quantity as locationmappqty,newbatchserial.lockquantity,newbatchserial.skufield,serialdocumentmapping.accserialcustomdataref,'','','',newbatchserial.wastagequantitytype,newbatchserial.wastagequantity,'','',serialdocumentmapping.stocktype from newbatchserial "
                    + " left join serialdocumentmapping on serialdocumentmapping.serialid=newbatchserial.id "
                    + " where serialdocumentmapping.documentid=?" + linkCondition + consignmentCondition;
            
            list = executeSQLQuery(query, new Object[]{documentid});
        } catch (Exception e) {
            Logger.getLogger(AccCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    public KwlReturnObject getOnlyInventoryDetails(String documentid ,String moduleID, boolean isEdit) throws ServiceException {
        List list = null;
        try {
            
            String query = "select id,quantity,venconsignuomquantity from inventory "
                    + " where id=?";
            
            list = executeSQLQuery(query, new Object[]{documentid});
        } catch (Exception e) {
            Logger.getLogger(AccCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    
    @Override
    public List getBatchSerialDetailsForRemoteAPI(String documentid, boolean addlocationmappcheck) throws ServiceException {
        String addCheck = "";
        String groupByCheck = " group by newbatchserial.id";
        if (addlocationmappcheck) {
            addCheck = "locationbatchdocumentmapping.documentid= '" + documentid + "' or ";
            groupByCheck = "";
        }
        String query = "select newproductbatch.id,newproductbatch.batchname,newproductbatch.location,newproductbatch.warehouse,newproductbatch.mfgdate,newproductbatch.expdate,newproductbatch.quantity,newbatchserial.id as serialid,newbatchserial.serialname,newbatchserial.expfromdate,newbatchserial.exptodate,locationbatchdocumentmapping.quantity as locationmappqty,newproductbatch.lockquantity from newproductbatch "
                + " left join newbatchserial on newproductbatch.id=newbatchserial.batch "
                + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                + " left join serialdocumentmapping on serialdocumentmapping.serialid=newbatchserial.id "
                + " where  " + addCheck + " serialdocumentmapping.documentid=?  " + groupByCheck;

        List params = new ArrayList();
        params.add(documentid);
        List list = executeSQLQuery(query, params.toArray());
        return list;
    }

    @Override
    public List getOnlySerialDetailsForRemoteAPI(String documentid) throws ServiceException {
        String query = "select '','','','','','','',newbatchserial.id,newbatchserial.serialname,newbatchserial.expfromdate,newbatchserial.exptodate from newbatchserial "
                + " left join serialdocumentmapping on serialdocumentmapping.serialid=newbatchserial.id "
                + " where serialdocumentmapping.documentid=?";
        List params = new ArrayList();
        params.add(documentid);
        List list = executeSQLQuery(query, params.toArray());
        return list;
    }

    public KwlReturnObject getBatchSerialDetailsforProduct(String productId, boolean isSerialForProduct,boolean isEdit, HttpServletRequest request) throws ServiceException, SessionExpiredException {
      String requestWarehouse="", requestLocation="";
        String companyId = sessionHandlerImpl.getCompanyid(request);
        String requestTypeId = request.getParameter("movementtype");
        Company company = (Company) get(Company.class, companyId);

        return getBatchSerialDetailsforProduct(company, productId,isSerialForProduct, requestTypeId, isEdit,requestWarehouse,requestLocation);
    }
    
  @Override
    public KwlReturnObject getBatchSerialDetailsforProduct(String productId, boolean isSerialForProduct, boolean isEdit, JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        Company company = null;
        String requestWarehouse = "", requestLocation = "";
        String requestTypeId = null;
        try {
            String companyId = paramJobj.getString(Constants.companyKey);
            requestTypeId = paramJobj.optString("movementtype", null);
            company = (Company) get(Company.class, companyId);
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
            ex.getStackTrace();
            Logger.getLogger(AccCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return getBatchSerialDetailsforProduct(company, productId, isSerialForProduct, requestTypeId, isEdit, requestWarehouse, requestLocation);
    }

    @Override
    public KwlReturnObject getBatchSerialDetailsforProduct(Company company, String productId,boolean isSerialForProduct, String requestTypeId, boolean isEdit,String requestWarehouse,String requestLocation) throws ServiceException {

        ArrayList params = new ArrayList();

        String warehouseTypeCheck = "";
        String warehouseTypeJoin = "";
        String autoapprovaljoin = "";
        String query="";
        params.add(company.getCompanyID());
        params.add(productId);
                            
        if (!StringUtil.isNullOrEmpty(requestTypeId)) {
            warehouseTypeJoin = " LEFT JOIN inventorywarehouse ON inventorywarehouse.id = newproductbatch.warehouse "
                    + " LEFT JOIN in_store_movementtype ON in_store_movementtype.storeid = inventorywarehouse.id ";
            warehouseTypeCheck = " AND in_store_movementtype.movementtype = ? ";
            params.add(requestTypeId);
        }
        if (!StringUtil.isNullOrEmpty(requestWarehouse) && !StringUtil.isNullOrEmpty(requestLocation)) {
            autoapprovaljoin = "and newproductbatch.warehouse=? and newproductbatch.location=? ";
            params.add(requestWarehouse);
            params.add(requestLocation);
        }
        if (isSerialForProduct) {
            query = " select distinct(newbatchserial.id) as serialid,newproductbatch.id as batchid,newproductbatch.batchname,newproductbatch.mfgdate,newproductbatch.expdate,locationbatchdocumentmapping.transactiontype,locationbatchdocumentmapping.documentid,newproductbatch.quantitydue,newbatchserial.serialname,newbatchserial.quantitydue as batchQuantity,newproductbatch.warehouse,newproductbatch.location,newbatchserial.serialname, serialdocumentmapping.reusablecount from newproductbatch"
                    + " left join newbatchserial on newproductbatch.id=newbatchserial.batch"
                    + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                    + " left join serialdocumentmapping on serialdocumentmapping.serialid=newbatchserial.id "
                    + warehouseTypeJoin
                    + " where newproductbatch.company=? and newbatchserial.quantitydue-newbatchserial.lockquantity>0 and newbatchserial.product=? "
                    + " and (newbatchserial.qaApprovalstatus IN (1,3) OR newbatchserial.qaApprovalstatus IS NULL)" // APPROVED - 1,  DONE - 3;
                    // + "and locationbatchdocumentmapping.transactiontype=28";
                    + warehouseTypeCheck + autoapprovaljoin;
        } else {
             query = " select distinct(newproductbatch.id) as batchid,newproductbatch.id as batchid,newproductbatch.batchname,newproductbatch.mfgdate,newproductbatch.expdate,locationbatchdocumentmapping.transactiontype,locationbatchdocumentmapping.documentid,newproductbatch.quantitydue,'','',newproductbatch.warehouse,newproductbatch.location,'' from newproductbatch"
                    + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                    + warehouseTypeJoin
                    + " where newproductbatch.company=? and newproductbatch.quantitydue-newproductbatch.lockquantity>0 and newproductbatch.product=? and (locationbatchdocumentmapping.transactiontype=28 or locationbatchdocumentmapping.transactiontype is NULL OR locationbatchdocumentmapping.transactiontype=20) "
                    + warehouseTypeCheck + autoapprovaljoin;
        
        }
        List list = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject getPendingConsignmentRequests(Company company) throws ServiceException {

        KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);
        List<String> dataList = new ArrayList();
        List params = new ArrayList();
        boolean activatefromdateToDate = false;
        String condsql="";
        ExtraCompanyPreferences extracompanypreferances = (ExtraCompanyPreferences) get(ExtraCompanyPreferences.class, company.getCompanyID());
        if(extracompanypreferances != null) {
            activatefromdateToDate = extracompanypreferances.isActivatefromdateToDate();
        }
        if (activatefromdateToDate) {
            condsql = " AND so.fromdate is NOT NULL AND so.fromdate >= ? ";
        }
        String qry = " SELECT DISTINCT so.id  FROM salesorder so INNER JOIN  sodetails sod ON sod.salesorder=so.id WHERE so.company= ? "
                + " AND so.freezeflag='F' AND so.isconsignment='T' AND so.lockquantityflag=1  AND sod.lockquantitydue > 0  AND sod.approvedbasequantity > 0 "+condsql;

                //+ " AND so.freeze=false AND so.deleted=false "
//                + " union " //in case of autoapprove added only those so who has approved quantity geater than 0
//                +" SELECT DISTINCT so.id,sod.product as prd,so.fromdate as frmdate  FROM salesorder so INNER JOIN  sodetails sod ON sod.salesorder=so.id WHERE so.company= ? "
//                + " AND so.isconsignment='T' AND so.autoapproveflag=1 AND so.lockquantityflag=0  AND sod.approvedbasequantity > 0 "
//                + " AND so.fromdate is NOT NULL AND so.fromdate >= ? "        
//                + " ORDER BY prd,frmdate ";
            params.add(company.getCompanyID());
            if (activatefromdateToDate) {
                Date todayDate = new Date();
                DateFormat df;
                try {
                    df = authHandler.getDateOnlyFormat();
                    todayDate = df.parse(df.format(todayDate));
                } catch (Exception ex) {
                    Logger.getLogger(AccCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                params.add(todayDate);
            }
//            params.add(company.getCompanyID());
//            params.add(new Date());

        try {
                dataList = executeSQLQuery(qry, params.toArray());

            retObj = new KwlReturnObject(true, null, null, dataList, dataList.size());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.getStackTrace();
            Logger.getLogger(AccCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return retObj;
        }

    }
//update the Lock quantity of So if it is delivered

    public KwlReturnObject updateSOLockQuantity(String sodid, double dquantity, String companyid) throws ServiceException {
        int count = 0;
        ArrayList params = new ArrayList();
        params.add(dquantity);
        params.add(sodid);
        params.add(companyid);
        String query = "update sodetails set lockquantity=(lockquantity-?) where id=? and company=?";
        count = executeSQLUpdate(query, params.toArray());
        return new KwlReturnObject(true, "lockquantity has been updated successfully.", null, null, count);
    }
    
      /**
       * 
       * @param moduleid
       * @param billid
       * @param companyid
       * @description To update print flag for all reports
       */
    public KwlReturnObject updatePrintFlag(Integer moduleid, String billid, String companyid) throws ServiceException {
        int count = 0;
        String tableName = "";
        ArrayList params = new ArrayList();
        try {
            if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.LEASE_INVOICE_MODULEID) {
                tableName = "invoice";
            } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                tableName = "goodsreceipt";
            } else if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                tableName = "purchaseorder";
            } else if (moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Lease_Order_ModuleId || moduleid == Constants.Acc_ConsignmentRequest_ModuleId) {
                tableName = "salesorder";
            } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Lease_DO) {
                tableName = "deliveryorder";
            } else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                tableName = "grorder";
            } else if (moduleid == Constants.Acc_Credit_Note_ModuleId) {
                tableName = "creditnote";
            } else if (moduleid == Constants.Acc_Debit_Note_ModuleId) {
                tableName = "debitnote";
            } else if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                tableName = "payment";
            } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                tableName = "receipt";
            } else if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                tableName = "purchasereturn";
            } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                tableName = "salesreturn";
            } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId || moduleid == Constants.Acc_Lease_Quotation) {
                tableName = "quotation";
            } else if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {
                tableName = "vendorquotation";
            } else if (moduleid == Constants.Acc_Purchase_Requisition_ModuleId) {
                tableName = "purchaserequisition";
            }
            params.add(billid);
            params.add(companyid);
            String query = "UPDATE " + tableName + " SET printedflag=1 WHERE id=? and company=?";
            count = executeSQLUpdate(query, params.toArray());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.getStackTrace();
            Logger.getLogger(AccCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "Print Flag has been updated successfully.", null, null, count);
    }
    
    /**
       * 
       * @param moduleid
       * @param billid
       * @param companyid
       * @description To update send Email flag for reports SI/VI/CQ/PO/SO/MP/RP
       */
    public KwlReturnObject updateSentEmailFlag(Integer moduleid, String billid, String companyid) throws ServiceException {
        int count = 0;
        String tableName = "";
        ArrayList params = new ArrayList();
        try {
            if (moduleid == Constants.Acc_Invoice_ModuleId) {
                tableName = "invoice";
            } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                tableName = "goodsreceipt";
            } else if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                tableName = "purchaseorder";
            } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                tableName = "salesorder";
            } else if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                tableName = "payment";
            } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                tableName = "receipt";
            } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
                tableName = "quotation";
            }
            params.add(billid);
            params.add(companyid);
            String query = "UPDATE " + tableName + " SET emailsentflag=1 WHERE id=? and company=?";
            count = executeSQLUpdate(query, params.toArray());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.getStackTrace();
            Logger.getLogger(AccCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "Send Email Flag has been updated successfully.", null, null, count);
    }

    public KwlReturnObject updateSOLockQuantitydue(String sodid, double dquantity, String companyid) throws ServiceException {
        int count = 0;
        ArrayList params = new ArrayList();
        params.add(dquantity);
        params.add(sodid);
        params.add(companyid);
        String query = "update sodetails set lockquantitydue=(lockquantitydue-?) where id=? and company=?";
        count = executeSQLUpdate(query, params.toArray());
        return new KwlReturnObject(true, "lockquantitydue has been updated successfully.", null, null, count);
    }
    public KwlReturnObject getBatchMappingDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query="";
        if (requestParams.containsKey("moduleid") && (!StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString()) && Integer.parseInt(requestParams.get("moduleid").toString()) == Constants.Acc_Sales_Return_ModuleId)) {
            query = "from ReturnBatchMapping";                          // For Sales return  , entry is in ReturnBatchMapping table 
        } else {
            query = "from SalesPurchaseBatchMapping";
        }
        return buildNExecuteQuery(query, requestParams);
    }
    public KwlReturnObject getSerialMappingDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from BatchSerialMapping";
        return buildNExecuteQuery(query, requestParams);
    }

    public Date getVendorExpDateForSerial(String serialnoid,boolean ispurchase) throws ServiceException {
            Date venExpDate = null;
            String mysql=" select bs.exptodate from newbatchserial bs where bs.id = ? ";
//        if (ispurchase) {
////            mysql = " select exptodate from batchserial where id in(select salesserial from batchserialmapping where purchaseSerial=?) ";
//            mysql = " select bs.exptodate from batchserial bs inner join batchserialmapping bsm on bs.id=bsm.salesserial where bsm.purchaseSerial=? ";
//        } else {
////            mysql = " select exptodate from batchserial where id in(select purchaseSerial  from batchserialmapping where salesserial=?) ";
//            mysql = "  select bs.exptodate from batchserial bs inner join batchserialmapping bsm on bs.id=bsm.purchaseSerial where bsm.salesserial=?";
//        }
            List list = executeSQLQuery(mysql, new Object[]{serialnoid});
            if(list.size()>0){
            venExpDate = (Date)list.get(0);
            }
            return venExpDate;
        }
    public Double getBatchQuantity(String docid, String batchid) throws ServiceException {
        return getBatchQuantity(docid, batchid, 1);
    }

    @Override
    public Double getBatchQuantity(String docid, String batchid, int type) throws ServiceException {
        double batchQuantity = 1;
        String mysql = " select quantity from locationbatchdocumentmapping where documentid=? and batchmapid=? AND stocktype=?";
        List list = executeSQLQuery(mysql, new Object[]{docid, batchid,type});
        if (list.size() > 0) {
            batchQuantity = (Double) list.get(0);
        }
        return batchQuantity;
    }
       public Double getApprovedSerialQty(String docid, String batchid, boolean isEdit) throws ServiceException {
        double serialqty = 0;
        String condsql = "";
        if (isEdit) {
            condsql = "and nb.quantitydue!=0";
        }
        String mysql = "select count(sdm.id) from serialdocumentmapping sdm inner join newbatchserial nb on sdm.serialid=nb.id inner join newproductbatch np on np.id=nb.batch   where  (sdm.consignmentapprovalstatus=1  or sdm.consignmentapprovalstatus is NULL) and sdm.documentid=? and np.id=? "+condsql;
        List list = executeSQLQuery(mysql, new Object[]{docid, batchid});
        if (!list.isEmpty() && list.size() > 0) {
                BigInteger bigInteger = (BigInteger) list.get(0);
                serialqty = bigInteger.doubleValue();
            }
            return serialqty;
        }
        public Double getserialAssignedQty(String docid) throws ServiceException {
            double serialqty=0;
            String mysql = "select count(id) from serialdocumentmapping where documentid=? ";
            List list = executeSQLQuery(mysql, new Object[]{docid});
            if (!list.isEmpty() && list.size() > 0) {
                BigInteger bigInteger = (BigInteger) list.get(0);
                serialqty = bigInteger.doubleValue();
            }
            return serialqty;
        }
        public Double getbatchAssignedQty(String docid) throws ServiceException {
            double serialqty=0;
            String mysql = "select sum(quantity) from locationbatchdocumentmapping where documentid=? ";
            List list = executeSQLQuery(mysql, new Object[]{docid});
            if (!list.isEmpty() && !list.contains(null) && list.size() > 0) {
                 serialqty = (Double) list.get(0);
            }
            return serialqty;
        }
    public String getDefaultLocation(String companyid) throws ServiceException {
            String defaultlocationID = "";
            String mysql = " select id from inventorylocation where company=? and isdefault=true ";
            List list = executeSQLQuery(mysql, new Object[]{companyid});
            if(list.size()>0){
            Object lst = list.get(0);
            defaultlocationID = lst.toString();
            }
            return defaultlocationID;
        }
    public String getDefaultWarehouse(String companyid) throws ServiceException {
            String defaultlocationID = "";
            String mysql = " select id from inventorywarehouse where company=? and isdefault=true ";
            List list = executeSQLQuery(mysql, new Object[]{companyid});
            if(list.size()>0){
            Object lst = list.get(0);
            defaultlocationID = lst.toString();
            }
            return defaultlocationID;
        }
    
    public KwlReturnObject getBatch(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ProductBatch";
        return buildNExecuteQuery(query, requestParams);
    }
    
 
    public String getPurchaseBatchId(String id) throws ServiceException {
            String defaultpurchaseID = "";
            String mysql = " select batchtomap from returnbatchmapping where batchmap=? ";
            List list = executeSQLQuery(mysql, new Object[]{id});
            if(list.size()>0){
            Object lst = list.get(0);
            defaultpurchaseID = lst.toString();
            }
            return defaultpurchaseID;
        }
        public String getPurchaseSerialId(String id) throws ServiceException {
            String defaultpurchaseID = "";
            String mysql = " select maptoserial from returnserialmapping where mapserial=? ";
            List list = executeSQLQuery(mysql, new Object[]{id});
            if(list.size()>0){
            Object lst = list.get(0);
            defaultpurchaseID = lst.toString();
            }
            return defaultpurchaseID;
        }
    @Override
    public KwlReturnObject updatePDFTemplate(HashMap<String, Object> pdfTemplateMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String id = (String) pdfTemplateMap.get("ID");
            PdfTemplateConfig config = (PdfTemplateConfig) get(PdfTemplateConfig.class, id);
            if (pdfTemplateMap.containsKey("pdfheader")) {
                config.setPdfHeader((String)pdfTemplateMap.get("pdfheader"));
            }
            if (pdfTemplateMap.containsKey("pdffooter")) {
                config.setPdfFooter((String)pdfTemplateMap.get("pdffooter"));
            }
            if (pdfTemplateMap.containsKey("pdfpretext")) {
                config.setPdfPreText((String)pdfTemplateMap.get("pdfpretext"));
            }
            if (pdfTemplateMap.containsKey("pdfposttext")) {
                config.setPdfPostText((String)pdfTemplateMap.get("pdfposttext"));
            }
            saveOrUpdate(config);
            list.add(config);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.updatePDFTemplate : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "PDF Template information updated successfully.", null, list, list.size());
    }   
    @Override
     public double getBatchRemainingQuantity(String purchasebatchid,int moduleid, String companyId) throws ServiceException {
        double actualquantity = 0;
        double soldquantity = 0;
        double purchreturnquantity = 0;
        double remainingquantity = 0;
        double salesreturnquantity = 0;
        String salesBatchID ="";
        String mysql = " select quantity from productbatch where id=? ";
        List list = executeSQLQuery(mysql, new Object[]{purchasebatchid});
        if (list.size() > 0 &&  !list.contains(null)) {
            actualquantity = (Double) list.get(0); //Actual Quantity of batch Of GRN(purchased)
        }
        String query = " select sum(quantity),salesBatch from salespurchasebatchmapping where purchasebatch=? ";
        List list1 = executeSQLQuery(query, new Object[]{purchasebatchid});
        Iterator itr = list1.iterator();
        while (itr.hasNext()) {
            Object[] oj = (Object[]) itr.next();
            if (oj[0] != null) {
                soldquantity = Double.valueOf((Double) oj[0]);
            }
            if (oj[1] != null) {
                salesBatchID = oj[1].toString();
            }
        }
    
        String query1 = " select sum(quantity) from returnbatchmapping where batchtomap=? and returntype=2 ";
        List list2 = executeSQLQuery(query1, new Object[]{purchasebatchid});
        if (list2.size() > 0 &&  !list2.contains(null)){
            purchreturnquantity = (Double) list2.get(0); //purchase retuen quantity of Purchase Return(Purchase Return)
        }
        if (moduleid == Constants.Acc_Sales_Return_ModuleId) {  //for sales retun directly check remaining quantity from DO batch
            String query2 = " select sum(quantity) from returnbatchmapping where batchtomap=? and returntype=1 ";
            List list3 = executeSQLQuery(query2, new Object[]{purchasebatchid});
            if (list3.size() > 0 && !list3.contains(null)) {
                salesreturnquantity = (Double) list3.get(0); //sales retuen quantity of Purchase Return(Purchase Return)
            }
        } else if (!StringUtil.isNullOrEmpty(salesBatchID)) {  //for DO and PR we will have to take salesbathid  and with that find out the quantity
            String query2 = " select sum(quantity) from returnbatchmapping where batchtomap=? and returntype=1 ";
            List list3 = executeSQLQuery(query2, new Object[]{salesBatchID});
            if (list3.size() > 0 && !list3.contains(null)) {
                salesreturnquantity = (Double) list3.get(0); //sales retuen quantity of Purchase Return(Purchase Return)
            }
        }
        remainingquantity = (actualquantity - purchreturnquantity) - soldquantity;
        if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
            remainingquantity = remainingquantity - salesreturnquantity;
        } else {
            remainingquantity = remainingquantity + salesreturnquantity;
        }

        
        return authHandler.roundQuantity(remainingquantity, companyId);
    }
    
  
   public boolean isserialusedinDOandPR(String serialId) throws ServiceException {  //to check the serial no of GR used in do or not
        boolean isserialusedinDO = false;
        ArrayList params = new ArrayList();
        params.add(serialId);
        params.add(serialId);
          String query = " select id from batchserialmapping where purchaseSerial= ?  union select id from returnserialmapping where maptoserial=? and returntype=2 ";
         List list = executeSQLQuery(query, params.toArray());
        Iterator itr = list.iterator();
        if (itr.hasNext()) {
            isserialusedinDO = true;
        }

        return isserialusedinDO;
    }
   public boolean isserialusedinDOandSR(String serialId) throws ServiceException {  //to check the serial no of GR used in do or not
        boolean isserialusedinDO = false;
        ArrayList params = new ArrayList();
        params.add(serialId);
          String query = " select id from returnserialmapping where maptoserial=?  ";
         List list = executeSQLQuery(query, params.toArray());
        Iterator itr = list.iterator();
        if (itr.hasNext()) {
            isserialusedinDO = true;
        }

        return isserialusedinDO;
    }
       public KwlReturnObject deleteBatches(String batchid, String companyid) throws ServiceException {
        String delQuery = "delete from NewProductBatch where id=? and company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{batchid, companyid});

        return new KwlReturnObject(true, "Batch details has been deleted successfully.", null, null, numRows);
    }
       public KwlReturnObject deleteSerial(String batchid, String companyid) throws ServiceException {
        String delQuery = "delete from NewBatchSerial where batch.id=? and company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{batchid, companyid});

        return new KwlReturnObject(true, "Batch details has been deleted successfully.", null, null, numRows);
    }
    public String isbatchExsistOrNot(String batchName,String custWarehouse,String productId,String companyid) throws ServiceException {
        String batchId = "";
        String mysql = " select id from newproductbatch where batchname=? and warehouse=? and product=? and company=? and isconsignment='T' ";
        List list = executeSQLQuery(mysql, new Object[]{batchName,custWarehouse,productId,companyid});
        if (list.size() > 0) {
            Object lst = list.get(0);
            batchId = lst.toString();
        }
        return batchId;
    }
    public String isSerialExsistOrNot(String serialName,String custWarehouse, String productId, String companyid) throws ServiceException {
        String serialId = "";
        String mysql = " select id from newbatchserial where serialname=? and product=? and company=? and isconsignment='T' ";
        List list = executeSQLQuery(mysql, new Object[]{serialName, productId, companyid});
        if (list.size() > 0) {
            Object lst = list.get(0);
            serialId = lst.toString();
        }
        return serialId;
    }
    public String getpurchaseBatchIdForSR(String batchid, String batchName) throws ServiceException {
        String batchId = "";
//        String mysql = "select batchmapid  from locationbatchdocumentmapping where documentid in(select documentid from locationbatchdocumentmapping where  batchmapid=?) and transactiontype=27 ";
//        String mysql = "select locationbatchdocumentmapping.batchmapid  from locationbatchdocumentmapping inner join newproductbatch on locationbatchdocumentmapping.batchmapid=newproductbatch.id  where documentid in(select documentid from locationbatchdocumentmapping  where  batchmapid=?) and locationbatchdocumentmapping.transactiontype=27 and newproductbatch.batchname=? ";
        String batchCond = "";
        if (StringUtil.isNullOrEmpty(batchName)) {
            batchCond = " AND (newproductbatch.batchname='' OR newproductbatch.batchname IS NULL) ";
        } else {
            batchCond = " AND newproductbatch.batchname='" + batchName + "' ";
        }
        String mysql = "select locationbatchdocumentmapping.batchmapid from locationbatchdocumentmapping  "
                + " inner join  newproductbatch on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                + " where locationbatchdocumentmapping.batchmapid=? and locationbatchdocumentmapping.transactiontype=27 "
                //                + " and newproductbatch.batchname=? ";
                + batchCond;
        List list = executeSQLQuery(mysql, new Object[]{batchid});
        if (list.size() > 0) {
            Object lst = list.get(list.size() - 1);
            batchId = lst.toString();
        }
        return batchId;
    }
    public String getpurchaseBatchIdForSRByRowId(String docuId, String batchName, String batchid) throws ServiceException {
        String batchId = "";
        String batchCond = "";
        if (StringUtil.isNullOrEmpty(batchName)) {
            batchCond = " AND (newproductbatch.batchname='' OR newproductbatch.batchname IS NULL) ";
        } else {
            batchCond = " AND newproductbatch.batchname='" + batchName + "' ";
        }
        String mysql = "select locationbatchdocumentmapping.batchmapid  from locationbatchdocumentmapping "
                + " INNER JOIN newproductbatch on locationbatchdocumentmapping.batchmapid=newproductbatch.id"
                + " where  locationbatchdocumentmapping.documentid=?  AND locationbatchdocumentmapping.transactiontype=27 AND "
                + " locationbatchdocumentmapping.isconsignment='F'  " + batchCond;
        List list = executeSQLQuery(mysql, new Object[]{docuId});
        if (list.size() > 0) {
            Object lst = list.get(0);
            batchId = lst.toString();
        }
        return batchId;
    }
    public String getpurchaseBatchIdForDo(String batchid,String documentid) throws ServiceException {
        String batchId = "";
         String mysql = "select batchmapid  from locationbatchdocumentmapping where documentid=? and locationbatchdocumentmapping.transactiontype=28 and isconsignment='T' and batchmapid!=? ";
         List list = executeSQLQuery(mysql, new Object[]{documentid,batchid});
        if (list.size() > 0) {
            Object lst = list.get(0);
            batchId = lst.toString();
        }
        return batchId;
    }
    public String getpurchaseBatchIdForNonbatch(String batchid,String batchName,String pid) throws ServiceException {
        String batchId = "";
//        String mysql = "select batchmapid  from locationbatchdocumentmapping where documentid in(select documentid from locationbatchdocumentmapping where  batchmapid=?) and transactiontype=27 and batchmapid is not null";
        String mysql = "select batchmapid  from locationbatchdocumentmapping where  batchmapid=? and transactiontype=27 and batchmapid is not null";
//        String mysql = "SELECT warehouse FROM newproductbatch WHERE id=? AND batchname=? AND product=?";
         List list = executeSQLQuery(mysql, new Object[]{batchid});
        if (list.size() > 0) {
            Object lst = list.get(list.size()-1);
            batchId = lst.toString();
        }
        return batchId;
    }
    public String getpurchaseBatchIdForNonbatchByRowId(String docId,String batchid) throws ServiceException {
        String batchId = "";
        String mysql = "select batchmapid  from locationbatchdocumentmapping where documentid=? and transactiontype=27 and batchmapid is not null AND isconsignment='F' ";
//        String mysql = "SELECT warehouse FROM newproductbatch WHERE id=? AND batchname=? AND product=?";
         List list = executeSQLQuery(mysql, new Object[]{docId});
        if (list.size() > 0) {
            Object lst = list.get(list.size()-1);
            batchId = lst.toString();
        }
        return batchId;
    }
    public String getpurchaseBatchIdForLocationWarehouse(String productid, String location, String warehouse) throws ServiceException {
        String batchId = "", loccondsql = "", warcondsql = "";
        ArrayList params = new ArrayList();
        params.add(productid);
        if (!StringUtil.isNullOrEmpty(location)) {
            loccondsql = " and location=? ";
            params.add(location);
        }
        if (!StringUtil.isNullOrEmpty(warehouse)) {
            warcondsql = " and warehouse=? ";
            params.add(warehouse);
        }
        String mysql = "SELECT id FROM newproductbatch WHERE product=? "+loccondsql+warcondsql;
        List list = executeSQLQuery(mysql, params.toArray());
        if (list.size() > 0) {
            Object lst = list.get(0);
            batchId = lst.toString();
        }
        return batchId;
    }
    
    @Override
    public String getpurchaseBatchIdForLocationWarehouseRowRackBin(String productid,String location,String warehouse,String rowId,String rackId,String binId,String batchName) throws ServiceException{
        String batchId = "", loccondsql = "", warcondsql = "",rowcondsql = "", rackcondsql = "",bincondsql = "",batchcondsql = "";
        ArrayList params = new ArrayList();
        params.add(productid);
        if (!StringUtil.isNullOrEmpty(location)) {
            loccondsql = " and location.id=? ";
            params.add(location);
        }
        if (!StringUtil.isNullOrEmpty(warehouse)) {
            warcondsql = " and warehouse.id=? ";
            params.add(warehouse);
        }
        if (!StringUtil.isNullOrEmpty(rowId)) {
            rowcondsql = " and row.id =? ";
            params.add(rowId);
        }
        if (!StringUtil.isNullOrEmpty(rackId)) {
            rackcondsql = " and rack.id=? ";
            params.add(rackId);
        }
        if (!StringUtil.isNullOrEmpty(binId)) {
            bincondsql = " and bin.id=? ";
            params.add(binId);
        }
        String batchNM= (batchName == null ? "" : batchName) ;
        batchcondsql = " and batchname = ?";
        params.add(batchNM);
        
        String mysql = "SELECT id FROM NewProductBatch WHERE product= ? "+loccondsql+warcondsql+rowcondsql+rackcondsql+bincondsql+batchcondsql;
        List list = executeQuery(mysql, params.toArray());
        if (list.size() > 0) {
            Object lst = list.get(0);
            batchId = lst.toString();
        }
        return batchId;
    }
    
    @Override
    public KwlReturnObject getStockAdjustmentSerialData(String companyId,NewProductBatch newProdBatch,String stockAdjId,boolean isFetchOnlyAvailableSerials) throws ServiceException{
        ArrayList params = new ArrayList();
        List serialList = new ArrayList();
        String batchId=newProdBatch.getId();
        String qryForFetchingSerial = "SELECT finalserialnames from in_sa_detail WHERE stockadjustment=? AND batchname= ? ";
        params.add(stockAdjId);
        params.add(newProdBatch.getBatchname());
        List list = executeSQLQuery(qryForFetchingSerial, params.toArray());
        if (list.size() > 0) {
            String serials = (String)list.get(0);
            if(!StringUtil.isNullOrEmpty(serials)){
                String srl[]=serials.split(",");
                for(String s: srl){
                    ArrayList srlparams = new ArrayList();
                    String qry="SELECT id from newbatchserial WHERE batch=? AND company= ? AND serialname= ? ";
                    srlparams.add(batchId);
                    srlparams.add(newProdBatch.getCompany().getCompanyID());
                    srlparams.add(s);
                    List l1 = executeSQLQuery(qry, srlparams.toArray());
                    if(l1.size() > 0){
                        String srlId=(String)l1.get(0);
                        serialList.add(srlId);
                    }
                }
            }
        }
        return new KwlReturnObject(true, "", "", serialList, serialList.size());
    }
    
    public String getpurchaseSerialIdForSR(String serialid,String serialName) throws ServiceException {
        String serialId = "";
//        String mysql = "select batchmapid  from locationbatchdocumentmapping where documentid in(select documentid from locationbatchdocumentmapping where  batchmapid=?) and transactiontype=27 ";
        String mysql = "select serialdocumentmapping.serialid  from serialdocumentmapping inner join newbatchserial on serialdocumentmapping.serialid=newbatchserial.id  where documentid in(select documentid from serialdocumentmapping   where  serialid=?) and serialdocumentmapping.transactiontype=27 and newbatchserial.serialname=? ";
//        String mysql = "select serialdocumentmapping.serialid  from serialdocumentmapping inner join newbatchserial on serialdocumentmapping.serialid=newbatchserial.id  where serialdocumentmapping.serialid=? and serialdocumentmapping.transactiontype=27 and newbatchserial.serialname=?";
         List list = executeSQLQuery(mysql, new Object[]{serialid,serialName});
        if (list.size() > 0) {
            Object lst = list.get(0);
            serialId = lst.toString();
        }
        return serialId;
    }
    
    @Override
    public String getStoreIdForNonbatchSerialByDODetailId(String companyId,String DODetailId) throws ServiceException {
        String storeId = "";
        String mysql = "SELECT warehouse from newproductbatch where  company=?   AND id=(SELECT batchmapid from locationbatchdocumentmapping   WHERE documentid=? AND transactiontype=27) ";

        List list = executeSQLQuery(mysql, new Object[]{companyId,DODetailId});
        if (list.size() > 0) {
            Object lst = list.get(0);
            storeId = lst.toString();
        }
        return storeId;
    }
    
    public String getLocationIdForNonbatchSerialByDODetailId(String companyId,String DODetailId) throws ServiceException {
        String locationId = "";
        String mysql = "SELECT location from newproductbatch where  company=?   AND id=(SELECT batchmapid from locationbatchdocumentmapping   WHERE documentid=? AND transactiontype=27) ";

        List list = executeSQLQuery(mysql, new Object[]{companyId,DODetailId});
        if (list.size() > 0) {
            Object lst = list.get(0);
            locationId = lst.toString();
        }
        return locationId;
    }
    
     public String getBatchIdForNonbatchSerialByDODetailId(String companyId,String DODetailId) throws ServiceException {
        String batchId = "";
        String mysql = "SELECT id from newproductbatch where  company=?   AND id=(SELECT batchmapid from locationbatchdocumentmapping   WHERE documentid=? AND transactiontype=27) ";

        List list = executeSQLQuery(mysql, new Object[]{companyId,DODetailId});
        if (list.size() > 0) {
            Object lst = list.get(0);
            batchId = lst.toString();
        }
        return batchId;
    }
   
    @Override
    public KwlReturnObject getBatchMapIdQtyByDocumentId(String companyId,String documentId) throws ServiceException {
        KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);
        String mysql = " SELECT batchmapid,quantity from locationbatchdocumentmapping WHERE documentid=? ";

        List list = executeSQLQuery(mysql, documentId);
        
        return new KwlReturnObject(true, "Batchmapid and qty has been fetched successfully.", null, list, list.size());
    }
    
    
     @Override
    public boolean isCroneExecutedForCurrentDay(String croneID, Date executionDate) throws ServiceException {
        boolean isCroneExecutedForCurrentDay = false;
        
        List list = getCroneDetails(croneID, executionDate);
        if (!list.isEmpty()) {
            isCroneExecutedForCurrentDay = true;
        }
        return isCroneExecutedForCurrentDay;
    }
    
    public List getCroneDetails(String croneID, Date executionDate) throws ServiceException {
        String hql = "FROM CroneSchedule where id=? and lastHit=?";
        List list = executeQuery(hql, new Object[]{croneID, executionDate});
        return list;
    }
    
    @Override
    public CroneSchedule saveCroneDetails(String croneID, String croneName, Date executionDate) throws ServiceException {
        CroneSchedule croneSchedule = (CroneSchedule) get(CroneSchedule.class, croneID);
        if (croneSchedule == null) {
            croneSchedule = new CroneSchedule();
            croneSchedule.setId(croneID);
            croneSchedule.setCroneName(croneName);
        }

        croneSchedule.setLastHit(executionDate);

        saveOrUpdate(croneSchedule);

        return croneSchedule;
    }
    
    public KwlReturnObject getCompany(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from Company c ";
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
    
//    public int executeSQLUpdate(String query, Object[] params) throws ServiceException {
//        int noofrecords = 0;
//        noofrecords = executeSQLUpdate(query, params);
//        return noofrecords;
//    }
    
//    public List executeSQLQuery(String query, Object[] params) throws ServiceException {
//        List recordList = new ArrayList();
//        recordList = executeSQLQuery(query, params);
//        return recordList;
//    } 
    
    @Override
    public KwlReturnObject getSerialsReusableCount(String productId,String serialName,String companyId, int transType,boolean viewOnly,String documentid,String batchID) throws ServiceException {
        ArrayList params = new ArrayList();
        String batchQuery="";
        params.add(productId);
        params.add(serialName);
        params.add(companyId);
       if(!StringUtil.isNullOrEmpty(batchID)){
           batchQuery=" and batch=?";
           params.add(batchID);
       }
        params.add(transType);
        String query="";
        if (viewOnly) {
            query = "select reusablecount from SerialDocumentMapping where serialid.id= ? and transactiontype= ? and documentid= '" +documentid+"'";
        } else {
//            query = "select sum(reusablecount) from SerialDocumentMapping where serialid.id= ? and transactiontype= ? ";
            query = "select  sum(reusablecount) from serialdocumentmapping where serialid in(SELECT id FROM newbatchserial WHERE product =? and serialname =?  and company=? "+batchQuery+") and transactiontype=?"; 
        }
        List list = executeSQLQuery(query, params.toArray());
        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }
    
    @Override
    public KwlReturnObject getOnlySerialDetailsForConsignmentLoan(String documentid) throws ServiceException {
        String query = "select '' as batchname, newbatchserial.serialname as serialname,newbatchserial.skufield as assetnm from newbatchserial "
                + " left join serialdocumentmapping on serialdocumentmapping.serialid = newbatchserial.id "
                + " where serialdocumentmapping.isconsignment = 'T' and serialdocumentmapping.documentid = ? ";
        
        List list = executeSQLQuery(query, new Object[]{documentid});
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
//    @Override
    public KwlReturnObject getOnlySerialDetailsForConsignmentLoanReport(String documentid) throws ServiceException {
        String query = "select '' as batchname, newbatchserial.serialname as serialname,newbatchserial.skufield as assetnm,serialdocumentmapping.documentid "
                + " from newbatchserial "
                + "  Inner JOin product p ON p.id=newbatchserial.product "
                + " left join serialdocumentmapping on serialdocumentmapping.serialid = newbatchserial.id "
                + " where serialdocumentmapping.isconsignment = 'T' and serialdocumentmapping.documentid IN (?) ";
    
        List list = executeSQLQuery(query, new Object[]{documentid});
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject getBatchSerialDetailsForConsignmentLoan(String documentid, boolean addlocationmappcheck) throws ServiceException {
        String addCheck = "";
        String ConditionForOr = "";
        String groupByCheck = " group by newbatchserial.id";
        String orderByCheck = " order by newproductbatch.id"; // for fetching batch wise data incase of multiple batch serial 
        if (addlocationmappcheck) {
            addCheck = "( locationbatchdocumentmapping.documentid= '" + documentid + "' or ";
            groupByCheck = "";
            ConditionForOr = ")";
        }
        String query = " select newproductbatch.batchname as batchname, newbatchserial.serialname as serialname,newbatchserial.skufield as assetnm from newproductbatch "
                + " left join newbatchserial on newproductbatch.id = newbatchserial.batch "
                + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid = newproductbatch.id and locationbatchdocumentmapping.transactiontype = 27 "
                + " left join serialdocumentmapping on serialdocumentmapping.serialid = newbatchserial.id and serialdocumentmapping.transactiontype = 27 "
                + " where " + addCheck + " serialdocumentmapping.documentid=?  " + ConditionForOr + groupByCheck + orderByCheck;
        List list = executeSQLQuery(query, new Object[]{documentid});
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    @Override
    public KwlReturnObject getBatchSerialDetailsForLoanReport(String documentid, boolean addlocationmappcheck, String comapny) throws ServiceException {
        List list = null;
        int cnt = 0;
        if (!StringUtil.isNullOrEmpty(documentid)) {
            String query = " SELECT * FROM (SELECT nb.batchname,'' AS serialname,'' AS asset,lcm.documentid AS documentid,nb.company FROM newproductbatch nb "
                    + " INNER JOIN product p ON p.id=nb.product AND p.isBatchForProduct='T' AND p.isSerialForProduct='F'"
                    + " INNER JOIN locationbatchdocumentmapping lcm ON lcm.batchmapid=nb.id "
                    + " WHERE  lcm.transactiontype=27"
                    + " UNION "
                    + " SELECT nb.batchname,sr.serialname AS serialname,sr.skufield AS asset,srm.documentid AS documentid,nb.company FROM newproductbatch nb "
                    + " INNER JOIN newbatchserial sr ON sr.batch=nb.id"
                    + " INNER JOIN product p ON p.id=nb.product AND p.isSerialForProduct='T'"
                    + " INNER JOIN serialdocumentmapping srm ON srm.serialid=sr.id "
                    + " WHERE  srm.transactiontype=27 ) AS tb WHERE tb.documentid IN (" + documentid + ") AND tb.company=? ";

            list = executeSQLQuery(query, new Object[]{comapny});
            cnt = list.size();
        }
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, cnt);
    }
    @Override
    public KwlReturnObject getBatchSerialDetailsForReturnReport(String documentid, boolean addlocationmappcheck, String comapny) throws ServiceException {

        String query = " SELECT * FROM (SELECT nb.batchname,'' AS serialname,'' AS asset,lcm.documentid AS documentid,nb.company,srd.salesreturn FROM newproductbatch nb "
                + " INNER JOIN product p ON p.id=nb.product AND p.isBatchForProduct='T' AND p.isSerialForProduct='F'"
                + " INNER JOIN locationbatchdocumentmapping lcm ON lcm.batchmapid=nb.id "
                + " INNER JOIN srdetails srd ON srd.id=lcm.documentid"
                + " WHERE  lcm.transactiontype=53"
                + " UNION "
                + " SELECT nb.batchname,sr.serialname AS serialname,sr.skufield AS asset,srm.documentid AS documentid,nb.company,srd.salesreturn FROM newproductbatch nb "
                + " INNER JOIN newbatchserial sr ON sr.batch=nb.id"
                + " INNER JOIN product p ON p.id=nb.product AND p.isSerialForProduct='T'"
                + " INNER JOIN serialdocumentmapping srm ON srm.serialid=sr.id "
                + " INNER JOIN srdetails srd ON srd.id=srm.documentid"
                + " WHERE  srm.transactiontype=53 ) AS tb WHERE tb.salesreturn IN (" + documentid + ") AND tb.company=? ";

        List list = executeSQLQuery(query, new Object[]{comapny});

        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    public KwlReturnObject getBatchSerialDetailsforProductAccordingToRequestType(String companyid, String productId, String requestTypeId) throws ServiceException {

        ArrayList params = new ArrayList();

        String warehouseTypeCheck = "";
        String warehouseTypeJoin = "";
        params.add(companyid);
        params.add(productId);

        if (!StringUtil.isNullOrEmpty(requestTypeId)) {
            warehouseTypeJoin = " LEFT JOIN inventorywarehouse ON inventorywarehouse.id = newproductbatch.warehouse "
                    + " LEFT JOIN in_store_movementtype ON in_store_movementtype.storeid = inventorywarehouse.id ";
            warehouseTypeCheck = " AND in_store_movementtype.movementtype = ? ";
            params.add(requestTypeId);
        }

        String query = " select distinct(newbatchserial.id) as serialid,newproductbatch.id as batchid,newproductbatch.batchname,newproductbatch.mfgdate,newproductbatch.expdate,locationbatchdocumentmapping.transactiontype,locationbatchdocumentmapping.documentid,newproductbatch.quantitydue,newbatchserial.serialname,newbatchserial.quantitydue as batchQuantity,newproductbatch.warehouse,newproductbatch.location from newproductbatch"
                + " left join newbatchserial on newproductbatch.id=newbatchserial.batch"
                + " left join locationbatchdocumentmapping on locationbatchdocumentmapping.batchmapid=newproductbatch.id "
                + " left join serialdocumentmapping on serialdocumentmapping.serialid=newbatchserial.id "
                + warehouseTypeJoin
                + " where newproductbatch.company=? and newbatchserial.quantitydue-newbatchserial.lockquantity>0 and newbatchserial.product=? "
                // + "and locationbatchdocumentmapping.transactiontype=28";
                + warehouseTypeCheck;
        List list = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }

    //this function is useful when we return any transction with link to GR and if that GR is linked with PO then we will update the PO status
    public KwlReturnObject updatePurchaseOrderStatus(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            double balanceqty=0;
            String query="";
            boolean add=false;
            boolean update=false;
            if (requestParams.containsKey("add") && requestParams.get("add")!=null &&  requestParams.get("add")!= "") {
               add = (Boolean) requestParams.get("add");
            }
            if (requestParams.containsKey("update") && requestParams.get("update")!=null &&  requestParams.get("update")!= "") {
               update = (Boolean) requestParams.get("update");
            }
            if (add) {
                query = "update PurchaseOrderDetail set balanceqty = balanceqty+? where ID=? and company.companyID=?";
            } else if(update){
                query = "update PurchaseOrderDetail set balanceqty = ? where ID=? and company.companyID=?";
            }else {
                query = "update PurchaseOrderDetail set balanceqty = balanceqty-? where ID=? and company.companyID=?";
            }

            if(requestParams.containsKey("balanceqty") && requestParams.get("balanceqty")!=null && requestParams.get("balanceqty")!=""){
             balanceqty=(Double)requestParams.get("balanceqty");
            }
            String podetailsID = (String) requestParams.get("podetails");
            String companyid = (String) requestParams.get("companyid");
            int numRows = executeUpdate(query, new Object[]{balanceqty,podetailsID, companyid});

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateDeliveryOrderStatus:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    /**
     * To Update Expense Purchase Order Balance Amount
     * @param requestParams (Required : Company ID, Purchase Order ID)
     * @return KwlReturnObject (Optional).
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject updateExpensePurchaseOrderStatus(Map<Object, Object> requestParams) throws ServiceException {
        try {
            double balAmount = 0;
            String query = "";
            boolean add = false;
            boolean update = false;
            if (requestParams.containsKey("add") && requestParams.get("add") != null && requestParams.get("add") != "") {
                add = (Boolean) requestParams.get("add");
            }
            if (requestParams.containsKey("update") && requestParams.get("update") != null && requestParams.get("update") != "") {
                update = (Boolean) requestParams.get("update");
            }
            if (add) {
                query = "update ExpensePODetail set balAmount = balAmount+? where ID=? and company.companyID=?";
            } else if (update) {
                query = "update ExpensePODetail set balAmount = ? where ID=? and company.companyID=?";
            } else {
                query = "update ExpensePODetail set balAmount = balAmount-? where ID=? and company.companyID=?";
            }

            if (requestParams.containsKey("balAmount") && requestParams.get("balAmount") != null && requestParams.get("balAmount") != "") {
                balAmount = (Double) requestParams.get("balAmount");
            }
            String expPODetailsID = (String) requestParams.get("expPODetailsID");
            String companyid = (String) requestParams.get("companyid");
            int numRows = executeUpdate(query, new Object[]{balAmount, expPODetailsID, companyid});

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateExpensePurchaseOrderStatus:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    /**
     * To update secuiry gate entry status while linking gate entry record in Gr
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject updateSecurityGateStatus(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            double balanceqty=0;
            String query="";
            boolean add=false;
            boolean update=false;
            if (requestParams.containsKey("add") && requestParams.get("add")!=null &&  requestParams.get("add")!= "") {
               add = (Boolean) requestParams.get("add");
            }
            if (requestParams.containsKey("update") && requestParams.get("update")!=null &&  requestParams.get("update")!= "") {
               update = (Boolean) requestParams.get("update");
            }
            if (add) {
                query = "update SecurityGateDetails set balanceqty = balanceqty+? where ID=? and company.companyID=?";
            } else if(update){
                query = "update SecurityGateDetails set balanceqty = ? where ID=? and company.companyID=?";
            } else {
                query = "update SecurityGateDetails set balanceqty = balanceqty-? where ID=? and company.companyID=?";
            }

            if(requestParams.containsKey("balanceqty") && requestParams.get("balanceqty")!=null && requestParams.get("balanceqty")!=""){
             balanceqty=(Double)requestParams.get("balanceqty");
            }
            String segDetailsID = (String) requestParams.get("sgedetails");
            String companyid = (String) requestParams.get("companyid");
            int numRows = executeUpdate(query, new Object[]{balanceqty,segDetailsID, companyid});

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.SecurityGateDetails:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    //this function is useful when we return any transction with link to DO and if that DO is linked with SO then we will update the SO status
    public KwlReturnObject updateSalesorderOrderStatus(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            double balanceqty=0;
            String query="";
            boolean add=false;
            boolean update=false;
            if (requestParams.containsKey("add") && requestParams.get("add")!=null &&  requestParams.get("add")!= "") {
               add = (Boolean) requestParams.get("add");
            }
            if (requestParams.containsKey("update") && requestParams.get("update")!=null &&  requestParams.get("update")!= "") {
               update = (Boolean) requestParams.get("update");
            }
            if (add) {
                query = "update SalesOrderDetail set balanceqty = balanceqty+? where ID=? and company.companyID=?";
            } else if(update){
                query = "update SalesOrderDetail set balanceqty = ? where ID=? and company.companyID=?";
            }else {
                query = "update SalesOrderDetail set balanceqty = balanceqty-? where ID=? and company.companyID=?";
            }

            if(requestParams.containsKey("balanceqty") && requestParams.get("balanceqty")!=null && requestParams.get("balanceqty")!=""){
             balanceqty=(Double)requestParams.get("balanceqty");
            }
            String sodetailsID = (String) requestParams.get("sodetails");
            String companyid = (String) requestParams.get("companyid");
            int numRows = executeUpdate(query, new Object[]{balanceqty,sodetailsID, companyid});

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.updateDeliveryOrderStatus:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }
    
        
     public KwlReturnObject setRolePermissions(HashMap<String, Object> requestParams, String[] features, String[] permissions) throws ServiceException {
        List ll = null;
        List<UserPermission> oldPermissions = null;
        String permissionChangeDetailStr = "";

        int dl = 0;
        String Hql = "";
        try {

            String roleId = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            String companyId = requestParams.get("companyid").toString();
            Company company = (Company) get(Company.class, companyId);
            String decription = requestParams.containsKey("desc") && requestParams.get("desc") != null ? requestParams.get("desc").toString() : "";
            String rolename = requestParams.containsKey("rolename") && requestParams.get("rolename") != null ? requestParams.get("rolename").toString() : "";
            boolean isEdit = requestParams.containsKey("isEdit") && requestParams.get("isEdit") != null ? Boolean.parseBoolean(requestParams.get("isEdit").toString()) : false;

            Rolelist addrole = null;
            Role role1 = null;

            if (!isEdit) {
                role1 = new Role();
                role1.setCompany(company);
                role1.setName(rolename);
                save(role1);

                addrole = new Rolelist(role1.getID(), rolename, rolename);
                addrole.setCompany(company);
                addrole.setDescription(decription);
                save(addrole);
            } else {
                addrole = (Rolelist) get(Rolelist.class, roleId);
                if (!roleId.equals("2")&&!roleId.equals("1")) {
//                    role1 = (Role) get(Role.class, roleId); //As id for role is different than role list and role is not used anywhare so no need to update
//                    role1.setCompany(company);
//                    role1.setName(rolename);
                    
                    addrole.setRolename(rolename);
                    addrole.setDisplayrolename(rolename);
                    addrole.setCompany(company);
                    addrole.setDescription(decription);
                    
                }
            }
            if (!(roleId.equals(Rolelist.COMPANY_ADMIN))) {

                for (int i = 0; i < features.length; i++) {

                    ProjectFeature projFeature = (ProjectFeature) load(ProjectFeature.class, features[i]);

                    RolePermission permission = new RolePermission();
                    permission.setRole(addrole);

                    permission.setFeature(projFeature);
                    permission.setPermissionCode(Long.parseLong(permissions[i]));
                    permission.setCompany(company);
                    try {
                        if (isEdit) {
                            saveOrUpdate(permission);
                        } else {
                            save(permission);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, permissionChangeDetailStr, "", ll, dl);
    }
    public KwlReturnObject getRolePermission(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        ArrayList params = null;
        try {
            String companyid = requestParams.containsKey("companyid") && requestParams.get("companyid") != null ? requestParams.get("companyid").toString() : "";
            String roleid = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";

            String Hql = " select feature.featureName, permissionCode, feature.featureID from RolePermission up";
            String condition = "";
            params = new ArrayList();

            if (!StringUtil.isNullOrEmpty(roleid)) {
                condition += (condition.length() == 0 ? " where " : " and ") + "role.roleid=? ";
                params.add(roleid);
            }

            if (!StringUtil.isNullOrEmpty(companyid)) {
                condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=? ";
                params.add(companyid);
            }

            ll = executeQuery(Hql + condition, params.toArray());
            dl = ll.size();

            if (dl == 0 && (roleid.equals(Rolelist.COMPANY_ADMIN))) {
                Hql = " select feature.featureName, permissionCode, feature.featureID from RolePermission up where role.roleid=? ";
                ll = executeQuery(Hql, roleid);
                dl = ll.size();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getUserPermission", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAllUserPermission(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        int count = 0;
        ArrayList params = new ArrayList();
        try {
            String companyId = requestParams.containsKey("companyid") && requestParams.get("companyid") != null ? requestParams.get("companyid").toString() : "";
            String roleid = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            String featureid = requestParams.containsKey("featureid") && requestParams.get("featureid") != null ? requestParams.get("featureid").toString() : "";
            String userid = requestParams.containsKey("userid") && requestParams.get("userid") != null ? requestParams.get("userid").toString() : "";
            String mysqlQuery = "select  permissioncode from userpermission  "
                    + " inner join role_user_mapping on userpermission.roleUserMapping=role_user_mapping.id  "
                    + " inner join users on role_user_mapping.userid=users.userid  "
                    + " where users.roleid=? and users.company=? and userpermission.feature=? and users.userid=? and users.deleteflag=0";
            params.add(roleid);
            params.add(companyId);
            params.add(featureid);
            params.add(userid);
            list = executeSQLQuery(mysqlQuery, params.toArray());
            count = list.size();

        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getUserPermission", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", list, count);
    }

    public KwlReturnObject isUsernameExitornot(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        int count = 0;
        ArrayList params = new ArrayList();
        try {
            String companyId = requestParams.containsKey("companyid") && requestParams.get("companyid") != null ? requestParams.get("companyid").toString() : "";
            String rolename = requestParams.containsKey("rolename") && requestParams.get("rolename") != null ? requestParams.get("rolename").toString() : "";
            String roleid = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";

            String mysqlQuery = "select  rolename from rolelist where rolename= ? and  company=? and roleid != ?  ";
            params.add(rolename);
            params.add(companyId);
            params.add(roleid);
            list = executeSQLQuery(mysqlQuery, params.toArray());
            count = list.size();
            if (count == 0) {
                ArrayList params1 = new ArrayList();
                String mysqlQuery1 = "select  rolename from rolelist where rolename= ? and  company is null and roleid != ?";
                params1.add(rolename);
                params1.add(roleid);
                //params.add(companyId);
                list = executeSQLQuery(mysqlQuery1, params1.toArray());
                count = list.size();
            }

        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getUserPermission", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", list, count);
    }

    public KwlReturnObject deleteRole(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        String Hql = "";
        Rolelist role = null;
        String msg = "";
        Locale locale = null;
        if(requestParams.containsKey("locale")){
            locale = (Locale) requestParams.get("locale");
        }
        try {
            String id = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            if (!StringUtil.isNullOrEmpty(id)) {
                role = (Rolelist) load(Rolelist.class, id);
                Hql = "from RoleUserMapping where roleId=?";
                ll = executeQuery(Hql, role);
                if (ll.size() > 0) {
                    msg = messageSource.getMessage("acc.rolemanagement.deleterole", null, locale);
                } else {
                    String Sql = "delete from rolepermission where role=?";
                    int count = executeSQLUpdate(Sql, new Object[]{id});

                    String Sql2 = "delete from role where id=?";
                    int count2 = executeSQLUpdate(Sql2, new Object[]{id});

                    String Sql1 = "delete from rolelist where roleid=?";
                    int count1 = executeSQLUpdate(Sql1, new Object[]{id});

                    msg = messageSource.getMessage("acc.rolemanagement.Roledeletedsuccessfully", null, locale);
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.deleteRole", e);
        }
        return new KwlReturnObject(true, msg, "", ll, dl);
    }

    public KwlReturnObject getRoleList(String companyid, String ss) throws ServiceException {
        List ll = null;
        int dl = 0;
        String condition = "";
        ArrayList params = new ArrayList();
        try {
            String searchString = ss;
            params.add(companyid);
            if (!StringUtil.isNullOrEmpty(searchString)) {
                condition = " and rolename like ? ";

                params.add("%" + ss + "%");
            }
            String Hql = "from Rolelist where (company is null or company.companyID=? ) " + condition + " order by roleid";
//            String Hql = "from Rolelist where (company is null"+ condition + ") or (company.companyID=?  " + condition + ") order by roleid";
            ll = executeQuery(Hql, params.toArray());
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getRoleList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getUserList(String companyid, String roleid) throws ServiceException {
        List ll = null;
        int dl = 0;
        String condition = "";
        ArrayList params = new ArrayList();
        try {
            params.add(companyid);
            params.add(roleid);
            String Hql = "from User where  company.companyID=? and roleID=? and deleteflag=0 ";
            ll = executeQuery(Hql, params.toArray());
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getRoleList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
    
    @Override
    public KwlReturnObject getTransactionInTemp(String documentno, String companyId,int moduleId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String query = "select documentno from duplicateno where documentno=? and company=? and moduleid=? ";
        list = executeSQLQuery(query, new Object[]{documentno, companyId,moduleId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public int insertTransactionInTemp(String documentno, String companyId,int moduleId) throws ServiceException {
        String query = "insert into duplicateno (documentno,company,moduleid) values(?,?,?)";
        int row=executeSQLUpdate(query, new Object[]{documentno, companyId,moduleId});
        return row;
    }
    
    @Override
    public int deleteTransactionInTemp(String documentno, String companyId,int moduleId ) throws ServiceException {
        String query = "delete from duplicateno where documentno=? and company=?  and moduleid=? ";
        int row=executeSQLUpdate(query, new Object[]{documentno, companyId,moduleId});
        return row;
    }
    @Override
    public KwlReturnObject saveWastageDetailsForBatch(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            NewProductBatch newProductBatch = null;
            if (requestParams.containsKey("batchmapid")) {
                newProductBatch = (NewProductBatch) get(NewProductBatch.class, (String) requestParams.get("batchmapid"));
            }

            if (newProductBatch != null && requestParams.containsKey("wastageQuantityType") && !StringUtil.isNullOrEmpty((String) requestParams.get("wastageQuantityType"))) {
                int wastageQuantityType = Integer.parseInt((String) requestParams.get("wastageQuantityType"));
                newProductBatch.setWastageQuantityType(wastageQuantityType);
            }

            if (newProductBatch != null && requestParams.containsKey("wastageQuantity") && !StringUtil.isNullOrEmpty((String) requestParams.get("wastageQuantity"))) {
                double wastageQuantity = Double.parseDouble((String) requestParams.get("wastageQuantity"));
                newProductBatch.setWastageQuantity(wastageQuantity);
            }

            if (newProductBatch != null) {
                saveOrUpdate(newProductBatch);
            }
            
            list.add(newProductBatch);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveWastageDetailsForBatch : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Wastage Details updated successfully.", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveWastageDetailsForSerial(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            NewBatchSerial newBatchSerial = null;
            if (requestParams.containsKey("serialmapid")) {
                newBatchSerial = (NewBatchSerial) get(NewBatchSerial.class, (String) requestParams.get("serialmapid"));
            }

            if (newBatchSerial != null && requestParams.containsKey("wastageQuantityType") && !StringUtil.isNullOrEmpty((String) requestParams.get("wastageQuantityType"))) {
                int wastageQuantityType = Integer.parseInt((String) requestParams.get("wastageQuantityType"));
                newBatchSerial.setWastageQuantityType(wastageQuantityType);
            }

            if (newBatchSerial != null && requestParams.containsKey("wastageQuantity") && !StringUtil.isNullOrEmpty((String) requestParams.get("wastageQuantity"))) {
                double wastageQuantity = Double.parseDouble((String) requestParams.get("wastageQuantity"));
                newBatchSerial.setWastageQuantity(wastageQuantity);
            }

            if (newBatchSerial != null) {
                saveOrUpdate(newBatchSerial);
            }
            
            list.add(newBatchSerial);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveWastageDetailsForSerial : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Wastage Details updated successfully.", null, list, list.size());
    }
    
    @Override
    public List getActiveUsersOfCompany(String companyID) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            String SELECT_USER_INFO = " from User u where u.company.companyID = ?  and u.deleteflag=?";
            params.add(companyID);
            params.add(0);
            List list = executeQuery(SELECT_USER_INFO, params.toArray());
            return list;
        } catch (Exception ex) {
            Logger.getLogger(AccCommonTablesDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.getActiveUsersOfCompany:" + ex.getMessage(), ex);
        }
    }
    /* This function was written for updating balance quantity of SO while Creating DO linking with SO.
    
     But this was already handled. So it is useless*/

//     @Override
//    public KwlReturnObject updateSOBalQuantity(String sodid, double dquantity, String companyid) throws ServiceException {
//       int count = 0;
//        ArrayList params = new ArrayList();
//        params.add(dquantity);
//        params.add(sodid);
//        params.add(companyid);
//        String query = "update sodetails set balanceqty=(balanceqty-?) where id=? and company=?";
//        count = executeUpdate(query, params.toArray());
//        return new KwlReturnObject(true, "lockquantitydue has been updated successfully.", null, null, count);
//    }
    
    /**
     * Description : Below Method is used to fetch TDS Rates in given Period.
     *
     * @param (HashMap<String, Object> requestParams) used to get request
     * parameters
     * @return List of objects of TDSRate
     * @throws ServiceException
     */
    @Override
    public List getTDSMasterRates(HashMap<String, Object> TDSParams) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = null;
        try {
            String companyid = "", query= "",subQry = " WHERE tr.deleted = 'F' ";//Only Non-Deleted records.
            String ss = (String) TDSParams.get(Constants.ss);
            Date StartDate = null, EndDate = null;
            if (TDSParams.containsKey(Constants.REQ_startdate) && TDSParams.containsKey(Constants.REQ_enddate)) {
                StartDate = (Date) TDSParams.get(Constants.REQ_startdate);
                EndDate = (Date) TDSParams.get(Constants.REQ_enddate);
                subQry += " AND ((fromdate >= ?  AND  fromdate <= ?) OR (todate >=?  AND todate<=?) OR (fromdate <?  AND todate>?)) ";
                params.add(StartDate);
                params.add(EndDate);
                params.add(StartDate);
                params.add(EndDate);
                params.add(StartDate);
                params.add(EndDate);
            }
            if (TDSParams.containsKey("companyid")) {
                companyid = (String) TDSParams.get("companyid");
                subQry += " AND (tr.company IS NULL or tr.company.companyID = ? )";
                params.add(companyid);
            }
            if (!StringUtil.isNullOrEmpty(ss)) {
                subQry += " and tr.natureofpayment in (select ID from DefaultMasterItem dmi WHERE dmi.value like ? OR dmi.code like ? ) ";
                params.add("%" +ss + "%");
                params.add("%" +ss + "%");
            }
            query = "FROM TDSRate tr " + subQry;
            list = executeQuery(query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("kwlCommonTablesDAOImpl.getTDSMasterRates: " + e.getMessage(), e);
        }
        return list;
    }
    @Override
    public List getMasterItemsForNatureOfPayment(HashMap<String, Object> TDSParams) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = null;
        try {
            String companyid = (String) TDSParams.get("companyid");
            String query = "FROM MasterItem WHERE company.companyID=? and masterGroup.ID= ?";
            params.add(companyid);
            params.add(IndiaComplianceConstants.NATUREOFPAYMENT_MASTERGROUP);
            list = executeQuery(query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("kwlCommonTablesDAOImpl.getMasterItemsForNatureOfPayment: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Description : Below Method is used to Save New TDS Rate.
     * @param TDSParams
     * @return List of Saved object of TDSRate
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject AddTDSRate(Map<String, Object> TDSParams) throws ServiceException {
        List list = new ArrayList();
        try {
            
            TDSRate tds_rate = new TDSRate();
            if(TDSParams.containsKey("id")){
                tds_rate = (TDSRate) get(TDSRate.class, (Integer) TDSParams.get("id"));
            }
            if (TDSParams.containsKey("companyid")) {
                Company company = (Company) get(Company.class, (String) TDSParams.get("companyid"));
                if (company != null) {
                    tds_rate.setCompany(company);
                }
            }
            if (TDSParams.containsKey("addnatureofpayment")) {
                tds_rate.setNatureofpayment((String) TDSParams.get("addnatureofpayment"));
            }
            if (TDSParams.containsKey("addresidentialstatus")) {
                tds_rate.setResidentialstatus((String) TDSParams.get("addresidentialstatus"));
            }
            if (TDSParams.containsKey("addbasicexemptionpertransaction")) {
                tds_rate.setBasicexemptionpertransaction((double) TDSParams.get("addbasicexemptionpertransaction"));
            }
            if (TDSParams.containsKey("addbasicexemptionperannum")) {
                tds_rate.setBasicexemptionperannum((double) TDSParams.get("addbasicexemptionperannum"));
            }
            if (TDSParams.containsKey("adddeducteetype")) {
                tds_rate.setDeducteetype((String) TDSParams.get("adddeducteetype"));
             }
            if (TDSParams.containsKey("addtdsrate")) {
                tds_rate.setRate((String) TDSParams.get("addtdsrate"));
            }
            if (TDSParams.containsKey("addtdsfromdate")) {
                tds_rate.setFromdate((Date) TDSParams.get("addtdsfromdate"));
            }
            if (TDSParams.containsKey("addtdstodate")) {
                tds_rate.setTodate((Date) TDSParams.get("addtdstodate"));
            }
            if (TDSParams.containsKey("addtdsrateifpannotavailable")) {
                tds_rate.setTdsrateifpannotavailable((double) TDSParams.get("addtdsrateifpannotavailable"));
            }
            saveOrUpdate(tds_rate);
            list.add(tds_rate);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.AddTDSRate() : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Batch Mapping updated successfully.", null, list, list.size());
    }
    
    @Override
    //To Check Duplicate record of TDS Master Rate in database before adding new.
    public List CheckDuplicateTDSMasterRate(HashMap<String, Object> TDSParams) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = null;
        try {
            String subQry = " WHERE tr.deleted = 'F' ";//Only Non-Deleted records.
            if (TDSParams.containsKey("companyid")) {
                subQry += " AND (tr.company IS NULL or tr.company.companyID = ? )";
                params.add((String) TDSParams.get("companyid"));
            }
            if (TDSParams.containsKey("addnatureofpayment")) {
                subQry += " AND  tr.natureofpayment = ? ";
                params.add((String) TDSParams.get("addnatureofpayment"));
            }
            if (TDSParams.containsKey("addresidentialstatus")) {
                subQry += " AND  tr.residentialstatus = ? ";
                params.add((String) TDSParams.get("addresidentialstatus"));
            }
            if (TDSParams.containsKey("adddeducteetype")) {
                subQry += " AND  tr.deducteetype = ? ";
                params.add((String) TDSParams.get("adddeducteetype"));
            }
            if (TDSParams.containsKey("addtdsfromdate")) {
                subQry += " AND tr.fromdate <= ? AND tr.todate >= ? ";
                params.add((Date) TDSParams.get("addtdstodate"));
                params.add((Date) TDSParams.get("addtdsfromdate"));
            }
            if (TDSParams.containsKey("id")) {
                subQry += " AND  tr.id <> ? ";
                params.add(Integer.parseInt(TDSParams.get("id").toString()));
            }
            String query = "FROM TDSRate tr " + subQry;
            list = executeQuery(query, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("kwlCommonTablesDAOImpl.getTDSMasterRates: " + e.getMessage(), e);
        }
        return list;
    }
    
    public KwlReturnObject getTDSRate(HashMap<String, Object> TDSParams) throws ServiceException {
        double rate = 0.0;
        List l = null;
        String innerwhereclouse = "",outerwhereclouse="";
        ArrayList al = new ArrayList();
        ArrayList alNatureofPayment = new ArrayList();
        ArrayList alDeducteeType = new ArrayList();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (TDSParams.get("natureofPayment")!=null && TDSParams.get("residentialstatus")!=null && TDSParams.get("billdate")!=null&&  TDSParams.get("deducteeType")!= null ) {
            String natureofPayment = TDSParams.get("natureofPayment").toString();
            double amount = (double) TDSParams.get("totalamount");
            String deducteeType = TDSParams.get("deducteeType").toString();
            String deducteeTypeName = "";
            String deducteeTypeDefaultMaster = ""; // To check defaultmaster item check with constant ERP-29007
            String residentialstatus = TDSParams.get("residentialstatus").toString();
            if(!StringUtil.isNullOrEmpty(TDSParams.get("billdate").toString())){
                Date date = sdf.parse(TDSParams.get("billdate").toString());
//            if (!StringUtil.isNullOrEmpty(natureofPayment) && !StringUtil.isNullOrEmpty(residentialstatus) && date != null && !StringUtil.isNullOrEmpty(deducteeType)) {
                alNatureofPayment.add(natureofPayment);
                alNatureofPayment.add("33");
                String queryDefaultItemsNOP = "select mi.defaultmasteritem FROM masteritem mi WHERE mi.id=? AND mi.masterGroup=?";
                l = executeSQLQuery(queryDefaultItemsNOP, alNatureofPayment.toArray());
                if (l.size() > 0) {
                    if (l.get(0) != null) {
                        natureofPayment = l.get(0).toString();
                    }
                }
                al.add(natureofPayment);
                al.add(residentialstatus);
                al.add(natureofPayment);
                al.add(residentialstatus);
                al.add(date);
                al.add(date);
//                // To check defaultmaster item check with constant ERP-29007
//                String queryDefaultItemsDT = "select mi.id,mi.value,mi.defaultmasteritem FROM masteritem mi WHERE mi.id=? AND mi.masterGroup=?";
//                alDeducteeType.add(deducteeType);
//                alDeducteeType.add("34");
//                l = executeSQLQuery(queryDefaultItemsDT, alDeducteeType.toArray());
//                if (l.size() > 0) {
//                    Object [] arrObj=(Object [])l.get(0);
//                    deducteeType =arrObj.length>0?arrObj[0].toString():"";
//                    deducteeTypeName = arrObj.length>1?arrObj[1].toString():"";
//                    deducteeTypeDefaultMaster = arrObj.length>2?(arrObj[2] != null ?arrObj[2].toString():""):"";// To check defaultmaster item check with constant ERP-29007
//                }
//                if (!deducteeType.equals("")) {
//                    if (residentialstatus.equals("1")) {
//                        whereclouse = "  AND tr.deducteetype=? AND fromamount< ? and ((toamount<>-1 and toamount>= ?) OR (toamount=-1)) ";
                    if (!StringUtil.isNullOrEmpty(deducteeType)) {
                        innerwhereclouse = " AND trd.deducteetype=? " ;
                        outerwhereclouse = " AND tr.deducteetype=? " ;
                        al.add(deducteeType);
//                        al.add(amount);
//                        al.add(amount);
                        al.add(deducteeType);
//                        al.add(amount);
//                        al.add(amount);
//                    } else {
//                        // To check defaultmaster item check with constant ERP-29007
//                        if (natureofPayment.equals(Constants.NATURE_OF_PAYMENT_194C) && (deducteeTypeDefaultMaster.equals(Constants.DEDUCTEE_TYPE_INDIVIDUAL) || deducteeTypeDefaultMaster.equals(Constants.DEDUCTEE_TYPE_HUF))) {
//                            whereclouse = "  AND tr.deducteetype=?  ";
//                            al.add("0");
//                            al.add("0");
//                        } else if (natureofPayment.equals(Constants.NATURE_OF_PAYMENT_194C)) {
//                            whereclouse = "  AND tr.deducteetype=?  ";
//                            al.add("1");
//                            al.add("1");
//                        }
//                    }
                    }
                    String query = "select tr.rate,tr.id,tr.basicexemptionperannum FROM TDSRate tr WHERE tr.deleted='F' AND tr.natureofpayment=? AND tr.residentialstatus=? AND tr.fromdate = "
                            + "(select max(trd.fromdate) FROM TDSRate trd WHERE trd.deleted='F' AND trd.natureofpayment=? AND trd.residentialstatus=? AND trd.fromdate<=? AND trd.todate>=? " + innerwhereclouse+")" + outerwhereclouse;
                    l = executeQuery(query, al.toArray());
              }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("kwlCommonTablesDAOImpl.getTDSRate - Parameter Missing", e);
        }
        return new KwlReturnObject(true, "", null, l, l.size());
    }
    public KwlReturnObject getCustomCurrency(String currencysymbol, String companyID) throws ServiceException {
         ArrayList params = new ArrayList();
         List list =null;
         params.add(companyID);
         params.add(currencysymbol);
             String query = "from CustomCurrency where companyid=? and systemcurrencysymbol=?";
             list = executeQuery(query, params.toArray());
         
        return new KwlReturnObject(true, "", "", list, list.size());
    }
     
      public KwlReturnObject getCustomCurrcode(String currencycode,String companyID ) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = null;
        params.add(companyID);
        params.add(currencycode);
        String query = "from CustomCurrency where companyid=? and systemcurrencycode=?";
        list = executeQuery(query, params.toArray());

        return new KwlReturnObject(true, "", "", list, list.size());
    }
      public String getCustomCurrencySymbol(String currencysymbol, String companyid) throws ServiceException {
        String currencySymbol = "";
        try {
            KwlReturnObject result = getCustomCurrency(currencysymbol, companyid);
            List list = result.getEntityList();
            if (list.size() != 0) {
                CustomCurrency customcurrency = (CustomCurrency) list.get(0);
                currencySymbol = customcurrency.getCustomcurrencysymbol();
            }else{
                currencySymbol = currencysymbol;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return currencySymbol;
    }

    public String getCustomCurrencyCode(String currencycode, String companyid) throws ServiceException {
        String currencySymbol = "";
        try {
            KwlReturnObject result = getCustomCurrcode(currencycode, companyid);
            List list = result.getEntityList();
            if (list.size() != 0) {
                CustomCurrency customcurrency = (CustomCurrency) list.get(0);
                currencySymbol = customcurrency.getCustomcurrencycode();
            } else {
                currencySymbol = currencycode;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return currencySymbol;
    }
     @Override
    public void releaseSerialFromOtherSo(NewBatchSerial newbatchseria, String documentId) throws ServiceException {
        String currencySymbol = "";
        try {
            String query = "SELECT sonumber,sdtl.id AS documentid FROM salesorder s "
                    + " INNER JOIN sodetails sdtl ON s.id=sdtl.salesorder "
                    + " INNER JOIN serialdocumentmapping srmp ON srmp.documentid=sdtl.id  "
                    + " WHERE deleteflag='F' AND s.company=? AND freezeflag='F' AND (linkflag=0 OR linkflag=2) AND isopen='T' "
                    + " AND issoclosed='F' AND (s.isconsignment='T' AND leaseormaintenanceso=3) AND isjobworkorder='F' "
                    + " AND currency=6 AND pendingapproval=0 AND approvestatuslevel=11 AND istemplate!=2  "
                    + "  AND srmp.serialid=? ";
            if(!StringUtil.isNullOrEmpty((documentId))){
                query+=" AND srmp.documentid NOT IN('" + documentId + "') ";
            } 

            List list1 = executeSQLQuery(query, new Object[]{newbatchseria.getCompany().getCompanyID(), newbatchseria.getId()});
            Iterator itr = list1.iterator();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String documentid = oj[1] != null ? ("" + oj[1]) : "";
                String delQuerySrMp = "delete from serialdocumentmapping where documentid=? and serialid=? ";
                int numRows1 = executeSQLUpdate(delQuerySrMp, new Object[]{documentid,newbatchseria.getId()});
                
                String delQueryBatchMp = "delete from locationbatchdocumentmapping where documentid=? ";
               // int numRows2 = executeSQLUpdate(delQueryBatchMp, new Object[]{documentid});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
      public void updateBatchExpDate(HashMap<String, Object> BatchParams) throws ServiceException{
           String currencySymbol = "";
        try {
            
            String batchName = BatchParams.containsKey("batchname") ? BatchParams.get("batchname").toString() : "";
            String itemCode = BatchParams.containsKey("itemcode") ? BatchParams.get("itemcode").toString() : "";
            String companyId = BatchParams.containsKey("company") ? BatchParams.get("company").toString() : "";
            Date expDate = BatchParams.containsKey("exptodate") ? ((Date) BatchParams.get("exptodate")) : null;

            String query = " UPDATE newproductbatch nb INNER JOIN product p ON p.id=nb.product AND p.company=nb.company "
                    + " SET nb.expdate=? WHERE p.id=nb.product  AND nb.batchname=? AND p.productid=? AND p.company=?  ";

           int numRows = executeSQLUpdate(query, new Object[]{expDate, batchName,itemCode,companyId});
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
      }
      /**
       * @description Update document transaction id with batch id
       * @param productBatchId
       * @param attachmentids
       * @throws ServiceException 
       */
      public void UpdateDocuments(String productBatchId, String attachmentids) throws ServiceException {
        String attachmentidsarr[] = attachmentids.split(",");
        String ids = "";
        try {
            for (int i = 0; i < attachmentidsarr.length; i++) {
                ids = ids + ",'" + attachmentidsarr[i] + "'";
            }
            ids = ids.substring(1);
            String query = "UPDATE invoicedoccompmap SET invoiceid=? WHERE documentid IN(" + ids + ")";
            executeSQLUpdate(query, new Object[]{productBatchId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
      
    public KwlReturnObject getDistributedOpeningBalance(HashMap<String, Object> requestParams) {
        List list = null;
        try {
            String accountid = "";
            String fieldid = "";
            String comboid = "";
            if (requestParams.containsKey("accountid")) {
                accountid = requestParams.get("accountid").toString();
                String hql = " select db.comboid.id, db.openingbal, db.field.id ";
                if (requestParams.containsKey("fieldid")) {
                    fieldid = requestParams.get("fieldid").toString();
                    hql += " from DistributeBalance db where db.accountid.ID = ? and db.field.id = ? ";
                    list = executeQuery(hql, new Object[]{accountid, fieldid});
                } else if (requestParams.containsKey("accountid") && requestParams.containsKey("comboid")) {
                    comboid = requestParams.get("comboid").toString();
                    hql += " from DistributeBalance db where db.accountid.ID = ? and db.comboid.id = ? ";
                    list = executeQuery(hql, new Object[]{accountid, comboid});
                } else {
                    hql += " , db.id from DistributeBalance db where db.accountid.ID = ? ";
                    list = executeQuery(hql, new Object[]{accountid});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }
    
    public JSONObject totalTDSAssessableAmountForExemptLimit(HashMap<String, Object> requestParams) {
        JSONObject jobj =new JSONObject();
        try {
            List ll = null;
            double totalTDSAssesableAmount=0.0;
            double totalTDSamountdeducted=0.0;
            String sdate = requestParams.get("activeFromDate").toString();
            String edate = requestParams.get("activeToDate").toString();
            String natureofpayment =  requestParams.get("natureofPayment").toString();
            String whereClouseGRD ="";
            String whereClouseERD ="";
            if(requestParams.containsKey("documentid") && !StringUtil.isNullOrEmpty(requestParams.get("documentid").toString())){
                whereClouseGRD = " AND grd.id NOT IN ('"+requestParams.get("documentid").toString()+"') ";
                whereClouseERD = " AND erd.id NOT IN ('"+requestParams.get("documentid").toString()+"') ";
            }
            String vendorid = requestParams.get("vendorID") != null ? requestParams.get("vendorID").toString() : "";
            String delQuery = "SELECT  SUM(grd.tdsassessableamount) as tdsassessableamount,SUM(grd.tdslineamount) as tdspayment  FROM grdetails grd INNER JOIN goodsreceipt gr on grd.goodsreceipt = gr.id "
                    + "INNER JOIN journalentry je on je.id=gr.journalentry "
                    + "WHERE  gr.vendor =? AND je.entrydate<=? AND je.entrydate>=? AND grd.natureofpayment=? AND gr.deleteflag='F' "+ whereClouseGRD
                    +" UNION "
                    +" SELECT  SUM(erd.tdsassessableamount) as tdsassessableamount,SUM(erd.tdslineamount) as tdspayment  FROM expenseggrdetails erd INNER JOIN goodsreceipt gr on erd.goodsreceipt = gr.id "
                    + "INNER JOIN journalentry je on je.id=gr.journalentry "
                    + "WHERE  gr.vendor =? AND je.entrydate<=? AND je.entrydate>=? AND erd.natureofpayment=? AND gr.deleteflag='F' "+ whereClouseERD;
            ll = executeSQLQuery(delQuery, new Object[]{vendorid, edate, sdate,natureofpayment,vendorid, edate, sdate,natureofpayment});
            if (ll.size() > 0) {
                for(Object list: ll){
                    Object[] row = (Object[])list;
                    totalTDSAssesableAmount+=row[0]!=null?(Double)row[0]:0.0;
                    totalTDSamountdeducted+=row[1]!=null?(Double)row[1]:0.0;
                }
            }
            jobj.put("totalTDSAssesableAmount", totalTDSAssesableAmount);
            jobj.put("totalTDSamountdeducted",  totalTDSamountdeducted);
            jobj.put("deductedWithMapping", false);
            String tdsjeMapping = "SELECT tdsjemapping  FROM grdetails grd INNER JOIN goodsreceipt gr on grd.goodsreceipt = gr.id "
                    + "INNER JOIN journalentry je on je.id=gr.journalentry "
                    + "WHERE  gr.vendor =? AND je.entrydate<=? AND je.entrydate>=? AND grd.natureofpayment=? AND grd.tdsjemapping IS NOT NULL  AND gr.deleteflag='F'  "+ whereClouseGRD
                    +" UNION "
                    +" SELECT  tdsjemapping  FROM expenseggrdetails erd INNER JOIN goodsreceipt gr on erd.goodsreceipt = gr.id "
                    + "INNER JOIN journalentry je on je.id=gr.journalentry "
                    + "WHERE  gr.vendor =? AND je.entrydate<=? AND je.entrydate>=? AND erd.natureofpayment=? AND erd.tdsjemapping IS NOT NULL  AND gr.deleteflag='F'  "+ whereClouseERD;
            ll = executeSQLQuery(tdsjeMapping, new Object[]{vendorid, edate, sdate,natureofpayment,vendorid, edate, sdate,natureofpayment});
             if (ll.size() > 0) {
                 for (Object list : ll) {
                     if(list!=null && !StringUtil.isNullOrEmpty(list.toString())){
                        jobj.put("deductedWithMapping", true);
                        break;
                     }
                 }
            }
        } catch (Exception ex) {
            jobj.put("totalTDSAssesableAmount", 0);
            jobj.put("totalTDSamountdeducted",  0);
            jobj.put("deductedWithMapping", false);
            ex.printStackTrace();
        } finally {
            return jobj;
        }
    }
    
   @Override 
    public String getTransactionId(String companyid, String cnnumber,String moduleid) throws ServiceException {
        ArrayList params = new ArrayList();
        String id = "";
        String query="";
        params.add(companyid);
        params.add(cnnumber);
        if(moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Credit_Note_ModuleId))){
           query = "select cn.id from creditnote cn where cn.company=? and cn.cnnumber=?";
        }
        List<String> list = executeSQLQuery(query, params.toArray());

        for (String transactionid : list) {
            id = transactionid;
        }
        return id;
    }
    
    @Override
    public boolean isUnitPriceHiddenForPR(String companyId) throws ServiceException {
        CustomizeReportMapping customizeReportMapping = new CustomizeReportMapping();
        boolean isUnitPriceHiddenInPR = false;
        List<CustomizeReportMapping> customizeMappingContainsList = find("from CustomizeReportMapping where moduleId = " + Constants.Acc_Purchase_Requisition_ModuleId + " and company.companyID = '" + companyId + "' and customizeReportHeader.id= '380c6a5e-eeaf-11e6-9459-4ccc6a2d6a4d'");
        if (customizeMappingContainsList != null && !customizeMappingContainsList.isEmpty() && customizeMappingContainsList.size() > 0) {  //ERP-38157
            customizeReportMapping = customizeMappingContainsList.get(0);
            if (customizeReportMapping != null) {
                isUnitPriceHiddenInPR = customizeReportMapping.isHidden();
}
        }
        return isUnitPriceHiddenInPR;
    }
    /**
     * Save or Update Inspection Form
     * @param params
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject saveOrUpdateInspectionForm(JSONObject params) throws ServiceException {
        List list = new ArrayList();
        try{
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String inspectionDate = params.optString("inspectionDate", "");
            String modelName = params.optString("modelName", "");
            String consignmentReturnNo = params.optString("consignmentReturnNo", "");
            String department = params.optString("department", "");
            String customerName = params.optString("customerName", "");
            String inspectionFormId = params.optString("inspectionFormId", "");
            Date inspDate = df.parse(inspectionDate);
            //Load Inspection Form Object
            InspectionForm inspectionForm = null;
            if(StringUtil.isNullOrEmpty(inspectionFormId)){
                inspectionForm = new InspectionForm();
            } else{
                inspectionForm = (InspectionForm) get(InspectionForm.class, inspectionFormId);
            }
            inspectionForm.setInspectionDate(inspDate);
            inspectionForm.setCustomerName(customerName);
            inspectionForm.setDepartment(department);
            inspectionForm.setConsignmentReturnNo(consignmentReturnNo);
            inspectionForm.setModelName(modelName);
            
            saveOrUpdate(inspectionForm);
            list.add(inspectionForm);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveOrUpdateInspectionForm : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Save Inspection Form.", "", list, list.size());
    }
    /**
     * Delete Inspection Form Details
     * @param InspectionFormId
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteInspectionFormDetails(String InspectionFormId) throws ServiceException {
        String delQuery = "delete from in_inspection_form_details where inspectionformid = ?";
        int numRows = executeSQLUpdate( delQuery, new Object[]{InspectionFormId});

        return new KwlReturnObject(true, "", null, null, numRows);
    }
    /**
     * Save Inspection Form Details
     * @param inspectionFormDetailsMap
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject saveInspectionFormDetails(HashMap<String, Object> inspectionFormDetailsMap) throws ServiceException {
        List list = new ArrayList();
        try {
            InspectionFormDetails inspectionFormDetails = new InspectionFormDetails();
            if (inspectionFormDetailsMap.containsKey("id")) {
                inspectionFormDetails = inspectionFormDetailsMap.get("id") == null ? null : (InspectionFormDetails) get(InspectionFormDetails.class, (String) inspectionFormDetailsMap.get("id"));
            }
            if(inspectionFormDetailsMap.containsKey("inspectionFormId")){
                InspectionForm inspectionForm = inspectionFormDetailsMap.get("inspectionFormId") == null ? null : (InspectionForm) get(InspectionForm.class, (String) inspectionFormDetailsMap.get("inspectionFormId"));
                inspectionFormDetails.setInspectionForm(inspectionForm);
            }
            if(inspectionFormDetailsMap.containsKey("areaId")){
                InspectionArea inspectionArea = inspectionFormDetailsMap.get("areaId") == null ? null : (InspectionArea) get(InspectionArea.class, (String) inspectionFormDetailsMap.get("areaId"));
                inspectionFormDetails.setInspectionArea(inspectionArea);
            }
            if(inspectionFormDetailsMap.containsKey("areaName")){
                inspectionFormDetails.setInspectionAreaValue((String) inspectionFormDetailsMap.get("areaName"));
            }
            if(inspectionFormDetailsMap.containsKey("status")){
                inspectionFormDetails.setInspectionStatus((String) inspectionFormDetailsMap.get("status"));
            }
            if(inspectionFormDetailsMap.containsKey("faults")){
                inspectionFormDetails.setFaults((String) inspectionFormDetailsMap.get("faults"));
            }
            if(inspectionFormDetailsMap.containsKey("passingValue")){
                inspectionFormDetails.setPassingValue((String) inspectionFormDetailsMap.get("passingValue"));
            }
            if(inspectionFormDetailsMap.containsKey("actualValue")){
                inspectionFormDetails.setActualValue((String) inspectionFormDetailsMap.get("actualValue"));
            }
            
            save(inspectionFormDetails);
            list.add(inspectionFormDetails);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveInspectionFormDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Inspection Form Deails has been added successfully", null, list, list.size());
    }
}
