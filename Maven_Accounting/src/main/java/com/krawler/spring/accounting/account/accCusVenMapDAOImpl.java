/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.account;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.CustomerVendorMapping;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.TaxTermsMapping;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author krawler
 */
public class accCusVenMapDAOImpl extends BaseDAO implements accCusVenMapDAO {

    public CustomerVendorMapping checkCustomerMappingExists(String customerid) throws ServiceException {
        CustomerVendorMapping customervendormapping = null;
        String query = "select id,vendoraccountid from customervendormapping where customeraccountid = ?";

        List list = executeSQLQuery( query, new Object[]{customerid});

        Iterator it = list.iterator();
        if (it.hasNext()) {
            Object obj[] = (Object[]) it.next();
            String mappingid = obj[0].toString();
            String venid = obj[1].toString();
            Vendor vendor = (Vendor) get(Vendor.class, venid);
            if(vendor!=null){
                customervendormapping = (CustomerVendorMapping) get(CustomerVendorMapping.class, mappingid);
            }
        }
        return customervendormapping;
    }

    public CustomerVendorMapping checkVendorMappingExists(String vendorid) throws ServiceException {
        CustomerVendorMapping customervendormapping = null;
        String query = "select id,customeraccountid from customervendormapping where vendoraccountid = ?";

        List list = executeSQLQuery( query, new Object[]{vendorid});

        Iterator it = list.iterator();
        if (it.hasNext()) {
            Object obj[] = (Object[]) it.next();
            String mappingid = obj[0].toString();
            String custid = obj[1].toString();
            Customer customer = (Customer) get(Customer.class, custid);
            if(customer!=null){
                customervendormapping = (CustomerVendorMapping) get(CustomerVendorMapping.class, mappingid);
            }
        }
        return customervendormapping;
    }

    public KwlReturnObject saveUpdateCustomerVendorMapping(JSONObject accjson) throws ServiceException {
        List list = new ArrayList();
        try {
            CustomerVendorMapping customerVendorMapping = null;

            if (accjson.has("id")) {
                customerVendorMapping = (CustomerVendorMapping) get(CustomerVendorMapping.class, (String) accjson.get("id"));
            } else {
                customerVendorMapping = new CustomerVendorMapping();
            }
            if (accjson.has("customeraccountid")) {
                Customer account = (accjson.get("customeraccountid") == null ? null : (Customer) get(Customer.class, (String) accjson.get("customeraccountid")));
                customerVendorMapping.setCustomeraccountid(account);
            }
            if (accjson.has("vendoraccountid")) {
                Vendor account = (accjson.get("vendoraccountid") == null ? null : (Vendor) get(Vendor.class, (String) accjson.get("vendoraccountid")));
                customerVendorMapping.setVendoraccountid(account);
            }
            if (accjson.has("mappingflag")) {
                customerVendorMapping.setMappingflag((Boolean) accjson.get("mappingflag"));
            }

            saveOrUpdate(customerVendorMapping);
            list.add(customerVendorMapping);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveUpdateCustomerVendorMapping : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Customer-Vendor mapping has been done successfully.", null, list, list.size());
    }

    public KwlReturnObject getCustomerVendorMapping(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from CustomerVendorMapping ab ";

        if (filterParams.containsKey("customeraccountid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.customeraccountid.ID=?";
            params.add(filterParams.get("customeraccountid"));
        }
        if (filterParams.containsKey("vendoraccountid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.vendoraccountid.ID=?";
            params.add(filterParams.get("vendoraccountid"));
        }
        condition += (condition.length() == 0 ? " where " : " and ") + "ab.mappingflag=true";
        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    public KwlReturnObject saveTermForTax(HashMap<String, Object> termMap) throws ServiceException {
        List list = new ArrayList();
        try {
            TaxTermsMapping taxTermsMapping = new TaxTermsMapping();
            if (termMap.containsKey("id")) {
                taxTermsMapping = (TaxTermsMapping) get(TaxTermsMapping.class, termMap.get("id").toString());
}

            if (termMap.containsKey("term")) {
                taxTermsMapping.setInvoicetermssales((InvoiceTermsSales) get(InvoiceTermsSales.class, termMap.get("term").toString()));

            }
            if (termMap.containsKey("tax")) {
                taxTermsMapping.setTax((Tax) get(Tax.class, termMap.get("tax").toString()));

            }
            saveOrUpdate(taxTermsMapping);
            list.add(taxTermsMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public List getTerms(String tax) throws ServiceException {

        String query = "select invoicetermssales from taxtermsmapping where tax = ?";

        List list = executeSQLQuery( query, new Object[]{tax});
        return list;
    }
       public List deleteTermForTax(String tax) throws ServiceException {
        String query = "delete from taxtermsmapping where tax = ?";
        int list = executeSQLUpdate( query, new Object[]{tax});
        return null;
}
    public boolean isCustomerUsedInTransactions(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        boolean isused = false;
        List params = new ArrayList();
        String qforDN = " Select 1 from debitnote where customer=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforCN = " Select 1 from creditnote where customer=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforPayment = " Select 1 from payment where customer=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforReceipt = " Select 1 from receipt where customer=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforInvoice = " Select 1 from invoice where customer=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforSO = " Select 1 from salesorder where customer=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforCQ = " Select 1 from quotation where customer=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforDO = " Select 1 from deliveryorder where customer=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforSR = " Select 1 from salesreturn where customer=? and  company=?";
        params.add(accountid);
        params.add(companyid);

        String finalQuery = qforDN + " UNION" + qforCN + " UNION" + qforPayment + " UNION" + qforReceipt + " UNION" + qforInvoice + " UNION" + qforSO + " UNION" + qforCQ + " UNION" + qforDO + " UNION" + qforSR;
        list = executeSQLQuery(finalQuery, params.toArray());
        if (list.size() > 0) {
            isused = true;
        }
        return isused;
    }

    public boolean isVendorUsedInTransactions(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        boolean isused = false;
        List params = new ArrayList();
        String qforDN = " Select 1 from debitnote  where vendor=? and company=?";
        params.add(accountid);
        params.add(companyid);
        String qforPayment = " Select 1 from payment where vendor=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforReceipt = " Select 1 from receipt where vendor=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforInvoice = " Select 1 from goodsreceipt where vendor=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforPO = " Select 1 from purchaseorder where vendor=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforVQ = " Select 1 from vendorquotation where vendor=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforPR = " Select 1 from purchasereturn where vendor=? and  company=?";
        params.add(accountid);
        params.add(companyid);
        String qforCN = " Select 1 from creditnote where vendor=? and  company=?";
        params.add(accountid);
        params.add(companyid);

        String finalQuery = qforDN + " UNION" + qforCN + " UNION" + qforPayment + " UNION" + qforReceipt + " UNION" + qforInvoice + " UNION" + qforPO + " UNION" + qforVQ + " UNION" + qforPR;
        list = executeSQLQuery(finalQuery,params.toArray());
        if (list.size() > 0) {
            isused = true;
        }
        return isused;
    }
    //To Find Out whether TDS Transactions are made using selected vendor.
    public boolean isVendorUsedInTDSTransactions(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        boolean isused = false;
        List params = new ArrayList();
//        String qforDN = " Select 1 from debitnote  where vendor=? and company=?";
//        params.add(accountid);
//        params.add(companyid);
        String qforaDVPayment = " SELECT 1 from tdsdetails INNER JOIN advancedetail on advancedetail.id = tdsdetails.advancedetail  " +
                             " INNER JOIN payment on payment.id=advancedetail.payment " +
                             " WHERE vendor=? and payment.company=? AND tdsdetails.tdsassessableamount>0 ";
        params.add(accountid);
        params.add(companyid);
        String qforPaymentdet = " SELECT 1 from tdsdetails  INNER JOIN paymentdetail on paymentdetail.id = tdsdetails.paymentdetail "+
                             " INNER JOIN payment on payment.id=paymentdetail.payment " +
                             " WHERE vendor=? and payment.company=? AND tdsassessableamount>0 ";
        params.add(accountid);
        params.add(companyid);
        String qforProductInvoice = " SELECT 1 from grdetails INNER JOIN goodsreceipt on grdetails.goodsreceipt = goodsreceipt.id "+
                             " WHERE  vendor=? and goodsreceipt.company=? AND tdsassessableamount>0";
        params.add(accountid);
        params.add(companyid);
        String qforExpenseInvoice = " SELECT 1 from expenseggrdetails INNER JOIN goodsreceipt on expenseggrdetails.goodsreceipt = goodsreceipt.id "+
                             " WHERE  vendor=? and goodsreceipt.company=? AND tdsassessableamount>0";
        params.add(accountid);
        params.add(companyid);
        String finalQuery =  qforaDVPayment + " UNION "+qforPaymentdet + " UNION" + qforProductInvoice + " UNION " + qforExpenseInvoice;
        list = executeSQLQuery(finalQuery, params.toArray());
        if (list.size() > 0) {
            isused = true;
        }
        return isused;
    }
    //To Verify whether Vendor's TDS Interest Payable account is Used in TDS Interest Make Payment Transactionor not.
    public boolean isVendorTDSInterestPayableAccUsedInTrans(String VendorTDSInterestPayableAccount, String companyid) throws ServiceException {
        List list = new ArrayList();
        boolean isused = false;
        List params = new ArrayList();
        //As this account is used only while making TDS Interest Payment i.e. Make Payment Against GL so checking it in "paymentdetailotherwise" only.
        if (!StringUtil.isNullOrEmpty(VendorTDSInterestPayableAccount) && !StringUtil.isNullOrEmpty(companyid)) {
            String qforPI = "SELECT pd.id  FROM paymentdetailotherwise pd INNER JOIN payment p ON pd.payment = p.id where pd.account = ?  and p.company = ? ";
            params.add(VendorTDSInterestPayableAccount);
            params.add(companyid);
            list = executeSQLQuery(qforPI, params.toArray());
            if (list.size() > 0) {
                isused = true;
            }
        }
        return isused;
    }
              
}
