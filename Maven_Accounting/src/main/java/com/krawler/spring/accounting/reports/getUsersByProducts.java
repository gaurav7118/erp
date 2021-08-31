/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.google.visualization.datasource.Capabilities;
import com.google.visualization.datasource.DataTableGenerator;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author krawler
 */
public class getUsersByProducts  implements DataTableGenerator {
    private accInvoiceDAO accInvoiceDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
     
    @Override
    public DataTable generateDataTable(Query query, HttpServletRequest request) throws DataSourceException {
        DataTable dataTable = new DataTable();
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("startdate", request.getParameter("startdate"));
            requestParams.put("enddate", request.getParameter("enddate"));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("nondeleted","true");   
            int type = Integer.parseInt(request.getParameter("type"));
            boolean isProduct = StringUtil.isNullOrEmpty(request.getParameter("isProduct")) ? false : Boolean.parseBoolean((String)request.getParameter("isProduct"));
            boolean isCustomer=Boolean.parseBoolean((String)request.getParameter("isCustomer"));  
            if(isProduct) {
                List<ColumnDescription> columnDescriptions = new ArrayList<ColumnDescription>();
                columnDescriptions.add(new ColumnDescription("Product", ValueType.TEXT, "Product"));
                if(type==0) {
                    columnDescriptions.add(new ColumnDescription("Quantity Sold", ValueType.NUMBER, "Quantity Sold"));
                } else {
                    columnDescriptions.add(new ColumnDescription("Amount", ValueType.NUMBER, "Amount"));                    
                }
                dataTable.addColumns(columnDescriptions);
                
                requestParams.put("accid", request.getParameter("accid"));
                requestParams.put("isTopCustomers", request.getParameter("isTopCustomers"));
                requestParams.put("isProduct", request.getParameter("isProduct"));
                KwlReturnObject result;
                if(type==0) {
                    result = accInvoiceDAOobj.getProductsByUsers(requestParams);  
                } else {
                    result = accInvoiceDAOobj.getProductRevenueByUsers(requestParams);
                }
                Iterator itr1 = result.getEntityList().iterator();
                while (itr1.hasNext()) {
                    Object[] oj = (Object[]) itr1.next();
                    String productid = oj[0].toString();
                    String productName = "";
                    KwlReturnObject product = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                    if (product != null && product.getEntityList() != null && product.getEntityList().get(0) != null) {
                        List<Product> prd = product.getEntityList();
                        productName = prd.get(0).getName();
                    }  
                    if(type==0) {
                        int count = Double.valueOf(oj[1].toString()).intValue(); 
                        dataTable.addRowFromValues(productName , count);
                    } else {
                        double amount = Double.parseDouble(oj[2].toString());
                        dataTable.addRowFromValues(productName , amount);
                    }
                }
            } else {
                requestParams.put("productid", request.getParameter("productid"));
                requestParams.put("isTopCustomers", request.getParameter("isTopCustomers"));
                requestParams.put("isCustomer", request.getParameter("isCustomer"));
                
                List<ColumnDescription> columnDescriptions = new ArrayList<ColumnDescription>();
                columnDescriptions.add(new ColumnDescription("Customer Name", ValueType.TEXT, "Customer Name"));
                if(type==0) {
                    columnDescriptions.add(new ColumnDescription("Quantity Sold", ValueType.NUMBER, "Quantity Sold"));
                }else {
                    columnDescriptions.add(new ColumnDescription("Amount", ValueType.NUMBER, "Amount"));
                }
                
                dataTable.addColumns(columnDescriptions);
        //            boolean isTopCustomers=Boolean.parseBoolean((String)request.getParameter("isTopCustomers"));            
                KwlReturnObject result;
                if(type==0) {
                    result = accInvoiceDAOobj.getUsersByProducts(requestParams);  
                } else {
                    result = accInvoiceDAOobj.getUsersByProductRevenue(requestParams);
                }        
                String topUsers = "";
                if(result.getEntityList().size()> 0){
                    Iterator itr1 = result.getEntityList().iterator();
                    while (itr1.hasNext()) {
                        Object[] oj = (Object[]) itr1.next();
                        String custid = oj[0].toString();

                        String name="",id="";
                        if (isCustomer) {
                            String customerName = "",customerId="";
                            KwlReturnObject customer = accountingHandlerDAOobj.getObject(Customer.class.getName(), custid);
                            if (customer != null && customer.getEntityList() != null && customer.getEntityList().get(0) != null) {
                                List<Customer> cl = customer.getEntityList();
                                customerName = cl.get(0).getName();
                                customerId=cl.get(0).getID();
                            }
                            name=customerName;
                            id=customerId;
                        }

                        if (!isCustomer) {
                            String vendorName = "",vendorId="";
                            KwlReturnObject vendor = accountingHandlerDAOobj.getObject(Vendor.class.getName(), custid);
                            if (vendor != null && vendor.getEntityList() != null && vendor.getEntityList().get(0) != null) {
                                List<Vendor> vl = vendor.getEntityList();
                                vendorName = vl.get(0).getName();
                                vendorId = vl.get(0).getID();
                            }
                            name=vendorName;
                            id=vendorId;
                        }
                        if(type==0) {
                            int count = Double.valueOf(oj[1].toString()).intValue(); 
                            dataTable.addRowFromValues(name , count);
                        } else {
                            double amount = Double.parseDouble(oj[2].toString());
                            dataTable.addRowFromValues(name , amount);
                        }
//                        dataTable.addRowFromValues(name , count);
                        topUsers +="'"+id+"',";

                    }
                }

                // get Others
                if(!StringUtil.isNullOrEmpty(topUsers)) {
                    topUsers = topUsers.substring(0, topUsers.length()-1);
                    ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
                    if(isCustomer) {
                        filter_names.add("NOTINtest.customer");
                    } else {
                        filter_names.add("NOTINtest.vendor");
                    }
                    filter_params.add(topUsers);
                    requestParams.put("filter_names", filter_names);
                    requestParams.put("filter_params", filter_params);
                    result = accInvoiceDAOobj.getUsersByProducts(requestParams);            
                    if(result.getEntityList().size()> 0){
                        Iterator itr1 = result.getEntityList().iterator();
                        int count =0;
                        while (itr1.hasNext()) {
                            Object[] oj = (Object[]) itr1.next();
                            count += Integer.parseInt(oj[1].toString());
                        }
                        dataTable.addRowFromValues("Others" , count);
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(getUsersByProducts.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (SessionExpiredException ex) {
            Logger.getLogger(getUsersByProducts.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return dataTable;
        }
    }

    @Override
    public Capabilities getCapabilities() {
        return Capabilities.NONE; 
    }
    
}
