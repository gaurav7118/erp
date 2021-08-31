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
package com.krawler.spring.accounting.invoice;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public class accDeliveryPlannerImpl extends BaseDAO implements accDeliveryPlannerDAO {

    @Override
    public KwlReturnObject saveOrUpdatePushToDeliveryPlanner(HashMap<String, Object> deliveryPlannerParams) throws ServiceException {
        List list = new ArrayList();
        try {
            DeliveryPlanner deliveryPlanner;
            String deliveryPlannerID = (String) deliveryPlannerParams.get("deliveryPlannerID");
            if (!StringUtil.isNullOrEmpty(deliveryPlannerID)) {
                deliveryPlanner = (DeliveryPlanner) get(DeliveryPlanner.class, deliveryPlannerID);
            } else {
                deliveryPlanner = new DeliveryPlanner();
            }

            if (deliveryPlannerParams.containsKey("pushTime") && deliveryPlannerParams.get("pushTime") != null) {
                deliveryPlanner.setPushTime((Date) deliveryPlannerParams.get("pushTime"));
            }

            if (deliveryPlannerParams.containsKey("docID") && deliveryPlannerParams.get("docID") != null) {
                int moduleid = (deliveryPlannerParams.containsKey(Constants.moduleid) && deliveryPlannerParams.get(Constants.moduleid) != null) ? (Integer) deliveryPlannerParams.get(Constants.moduleid) : 0;
                
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    deliveryPlanner.setDocumentNo((String) deliveryPlannerParams.get("docID"));
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    Invoice inv = (Invoice) get(Invoice.class, (String) deliveryPlannerParams.get("docID"));
                    deliveryPlanner.setReferenceNumber(inv);
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    DeliveryOrder deliveryOrder = (DeliveryOrder) get(DeliveryOrder.class, (String) deliveryPlannerParams.get("docID"));
                    deliveryPlanner.setDeliveryOrder(deliveryOrder);
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    SalesReturn salesReturn = (SalesReturn) get(SalesReturn.class, (String) deliveryPlannerParams.get("docID"));
                    deliveryPlanner.setSalesReturn(salesReturn);
                }
            }

            if (deliveryPlannerParams.containsKey("deliveryDate") && deliveryPlannerParams.get("deliveryDate") != null) {
                deliveryPlanner.setDeliveryDate((Date) deliveryPlannerParams.get("deliveryDate"));
            }

            if (deliveryPlannerParams.containsKey("deliveryTime") && deliveryPlannerParams.get("deliveryTime") != null) {
                deliveryPlanner.setDeliveryTime((String) deliveryPlannerParams.get("deliveryTime"));
            }

            if (deliveryPlannerParams.containsKey("remarksBySales") && deliveryPlannerParams.get("remarksBySales") != null) {
                deliveryPlanner.setRemarksBySales((String) deliveryPlannerParams.get("remarksBySales"));
            }

            if (deliveryPlannerParams.containsKey("fromUser") && deliveryPlannerParams.get("fromUser") != null) {
                User from = (User) get(User.class, (String) deliveryPlannerParams.get("fromUser"));
                deliveryPlanner.setFromUser(from);
            }

            if (deliveryPlannerParams.containsKey("companyID") && deliveryPlannerParams.get("companyID") != null) {
                Company company = (Company) get(Company.class, (String) deliveryPlannerParams.get("companyID"));
                deliveryPlanner.setCompany(company);
            }

            if (deliveryPlannerParams.containsKey("deliveryLocation") && deliveryPlannerParams.get("deliveryLocation") != null) {
                deliveryPlanner.setDeliveryLocation((String) deliveryPlannerParams.get("deliveryLocation"));
            }

            if (deliveryPlannerParams.containsKey("remarksByPlanner") && deliveryPlannerParams.get("remarksByPlanner") != null) {
                deliveryPlanner.setRemarksByPlanner((String) deliveryPlannerParams.get("remarksByPlanner"));
            }

            if (deliveryPlannerParams.containsKey("vehicleNo") && deliveryPlannerParams.get("vehicleNo") != null) {
                MasterItem vehicleNo = (MasterItem) get(MasterItem.class, (String) deliveryPlannerParams.get("vehicleNo"));
                deliveryPlanner.setVehicleNumber(vehicleNo);
            }

            if (deliveryPlannerParams.containsKey("driver") && deliveryPlannerParams.get("driver") != null) {
                MasterItem driver = (MasterItem) get(MasterItem.class, (String) deliveryPlannerParams.get("driver"));
                deliveryPlanner.setDriver(driver);
            }

            if (deliveryPlannerParams.containsKey("tripNo") && deliveryPlannerParams.get("tripNo") != null) {
                MasterItem tripNo = (MasterItem) get(MasterItem.class, (String) deliveryPlannerParams.get("tripNo"));
                deliveryPlanner.setTripNumber(tripNo);
            }

            if (deliveryPlannerParams.containsKey("tripDesc") && deliveryPlannerParams.get("tripDesc") != null) {
                deliveryPlanner.setTripDescription((String) deliveryPlannerParams.get("tripDesc"));
            }

            if (deliveryPlannerParams.containsKey("invoiceOccurance") && deliveryPlannerParams.get("invoiceOccurance") != null) {
                deliveryPlanner.setInvoiceOccurance((Integer) deliveryPlannerParams.get("invoiceOccurance"));
            }

            if (deliveryPlannerParams.containsKey("printedBy") && deliveryPlannerParams.get("printedBy") != null) {
                deliveryPlanner.setPrintedBy((String) deliveryPlannerParams.get("printedBy"));
            }
            
            if (deliveryPlannerParams.containsKey(Constants.moduleid) && deliveryPlannerParams.get(Constants.moduleid) != null) {
                deliveryPlanner.setModule((Integer) deliveryPlannerParams.get(Constants.moduleid));
            }
            
            if (deliveryPlannerParams.containsKey("deliveryOrder") && deliveryPlannerParams.get("deliveryOrder") != null) {
                deliveryPlanner.setDeliveryOrder((DeliveryOrder) deliveryPlannerParams.get("deliveryOrder"));
            }
            
            if (deliveryPlannerParams.containsKey("invoice") && deliveryPlannerParams.get("invoice") != null) {
                deliveryPlanner.setReferenceNumber((Invoice) deliveryPlannerParams.get("invoice"));
            }

            saveOrUpdate(deliveryPlanner);

            list.add(deliveryPlanner);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getDeliveryPlanner(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            
            boolean isFromDeliveryPlannerReport = Boolean.parseBoolean(requestParams.get("isFromDeliveryPlannerReport").toString());
            int moduleid = (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null) ? (Integer) requestParams.get(Constants.moduleid) : 0;
            String billid = (requestParams.containsKey("billid") && requestParams.get("billid") != null) ? (String) requestParams.get("billid") : "";

            ArrayList params = new ArrayList();
            String condition = "";
            params.add((String) requestParams.get("companyID"));
            
            if (moduleid != 0) {
                if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    condition += " and dplan.module in (" + Constants.Acc_Invoice_ModuleId + "," + Constants.Acc_Delivery_Order_ModuleId + ") " ;
                } else {
                    params.add(moduleid);
                    condition += " and dplan.module = ? ";
                }
            }
            
            if (!StringUtil.isNullOrEmpty(billid)) {
                if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    params.add(billid);
                    condition += " and dplan.referenceNumber.ID = ? ";
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    params.add(billid);
                    condition += " and dplan.deliveryOrder.ID = ? ";
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    params.add(billid);
                    condition += " and dplan.salesReturn.ID = ? ";
                }
            }

            String searchString = (String) requestParams.get("ss");
            Map SearchStringMap = null;
            if (!StringUtil.isNullOrEmpty(searchString)) {
                String[] searchcol = null;
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    searchcol = new String[]{"po.purchaseOrderNumber", "dplan.vehicleNumber.value", "dplan.driver.value"};
                    SearchStringMap = StringUtil.insertParamSearchStringMap(params, searchString, 3);
                    StringUtil.insertParamSearchString(SearchStringMap);
                } else if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    searchcol = new String[]{"dplan.referenceNumber.invoiceNumber", "dplan.deliveryOrder.deliveryOrderNumber", "dplan.vehicleNumber.value", "dplan.driver.value"};
                    SearchStringMap = StringUtil.insertParamSearchStringMap(params, searchString, 4);
                    StringUtil.insertParamSearchString(SearchStringMap);
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    searchcol = new String[]{"dplan.salesReturn.salesReturnNumber", "dplan.vehicleNumber.value", "dplan.driver.value"};
                    SearchStringMap = StringUtil.insertParamSearchStringMap(params, searchString, 3);
                    StringUtil.insertParamSearchString(SearchStringMap);
                }
                
                String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                condition += searchQuery;
            }

            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
                if (isFromDeliveryPlannerReport) {
                    condition += " and dplan.pushTime >= ? and dplan.pushTime <= ? ";
                } else {
                    condition += " and (DATE_FORMAT(convert_tz(dplan.pushTime,'+00:00',dplan.referenceNumber.createdby.timeZone.difference),'%m-%d-%Y') >= DATE_FORMAT(?,'%m-%d-%Y') and DATE_FORMAT(convert_tz(dplan.pushTime,'+00:00',dplan.referenceNumber.createdby.timeZone.difference),'%m-%d-%Y') <= DATE_FORMAT(?,'%m-%d-%Y')) ";
                }
            }

            if (requestParams.containsKey("order_by")) {
                condition += " order by " + requestParams.get("order_by");
            }
            
            String query = "" ;
            if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                query = " from DeliveryPlanner dplan, PurchaseOrder po left join dplan.vehicleNumber left join dplan.driver  where po.id = dplan.documentNo and dplan.company.companyID = ? " + condition;
            } else if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Sales_Return_ModuleId) {
                query = " from DeliveryPlanner dplan left join dplan.referenceNumber left join dplan.vehicleNumber left join dplan.driver  where dplan.company.companyID = ? " + condition;
            }
            
            returnList = executeQuery( query, params.toArray());
            totalCount = returnList.size();

            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                returnList = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    public KwlReturnObject deleteDeliveryPlannerPermanently(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        String idString="";
        try {           
            ArrayList params = new ArrayList();
            idString=requestParams.get("id").toString();
            params.add((String) requestParams.get("companyID"));
            String query = " delete from deliveryplanner where company=? and id in"+"("+idString+")";
            totalCount = executeSQLUpdate( query, params.toArray());
            
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Delivery Planner has been deleted successfully.", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject saveOrUpdateDeliveryPlannerAnnouncement(HashMap<String, Object> announcementParams) throws ServiceException {
        List list = new ArrayList();
        try {
            DeliveryPlannerAnnouncement deliveryPlannerAnnouncement;
            String announcementID = (String) announcementParams.get("announcementID");
            if (!StringUtil.isNullOrEmpty(announcementID)) {
                deliveryPlannerAnnouncement = (DeliveryPlannerAnnouncement) get(DeliveryPlannerAnnouncement.class, announcementID);
            } else {
                deliveryPlannerAnnouncement = new DeliveryPlannerAnnouncement();
            }

            if (announcementParams.containsKey("announcementTime") && announcementParams.get("announcementTime") != null) {
                deliveryPlannerAnnouncement.setAnnouncementTime((Date) announcementParams.get("announcementTime"));
            }

            if (announcementParams.containsKey("announcementMsg") && announcementParams.get("announcementMsg") != null) {
                deliveryPlannerAnnouncement.setAnnouncementMsg((String) announcementParams.get("announcementMsg"));
            }

            if (announcementParams.containsKey("companyID") && announcementParams.get("companyID") != null) {
                Company company = (Company) get(Company.class, (String) announcementParams.get("companyID"));
                deliveryPlannerAnnouncement.setCompany(company);
            }

            saveOrUpdate(deliveryPlannerAnnouncement);

            list.add(deliveryPlannerAnnouncement);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getDliveryPlannerAnnouncement(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");

            ArrayList params = new ArrayList();
            String condition = "";
            String orderBy = "order by announcementTime desc";
            params.add((String) requestParams.get("companyID"));

            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
                condition += " and announcementTime >= ? and announcementTime <= ? ";
            }

            String query = " from DeliveryPlannerAnnouncement where company.companyID = ? " + condition + orderBy;

            returnList = executeQuery( query, params.toArray());
            totalCount = returnList.size();

            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                returnList = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getVehicleDliverySummaryReport(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            boolean isVehicleDeliverySummaryReport = (Boolean) requestParams.get("isVehicleDeliverySummaryReport");
            boolean isDriverDeliverySummaryReport = (Boolean) requestParams.get("isDriverDeliverySummaryReport");
            int moduleid = (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null) ? (Integer) requestParams.get(Constants.moduleid) : 0;

            String conditionSQL = "";
            ArrayList params = new ArrayList();
            params.add((String) requestParams.get("companyID"));

            Date startDate = (Date) requestParams.get(Constants.REQ_startdate);
            Date endDate = (Date) requestParams.get(Constants.REQ_enddate);
            if (startDate != null && endDate != null) {
                params.add(startDate);
                params.add(endDate);
                conditionSQL += " and ( dp.deliverydate >=? and dp.deliverydate <=?) ";
            }
            
            if (moduleid != 0) {
                params.add(moduleid);
                conditionSQL += " and dp.module = ? ";
            }

            String sqlQuery = "";

            if (isVehicleDeliverySummaryReport) {
                sqlQuery =
                        "select mi.value as 'Vehicle Number', count(distinct(dp.tripnumber)) as 'No. of Trip', count(distinct(dp.referencenumber)) as 'No. Of DO/PO', mi.id, count(distinct(dp.documentno)) as 'No. Of PO' from masteritem mi "
                        + " left join deliveryplanner dp on mi.id = dp.vehiclenumber "
                        + " where mi.masterGroup = 25 and mi.company = ? " + conditionSQL
                        + " group by dp.vehiclenumber order by mi.value ";
            } else if (isDriverDeliverySummaryReport) {
                sqlQuery =
                        "select mi.value as 'Driver', count(distinct(dp.tripnumber)) as 'No. of Trip', count(distinct(dp.referencenumber)) as 'No. Of DO/PO', mi.id, count(distinct(dp.documentno)) as 'No. Of PO' from masteritem mi "
                        + " left join deliveryplanner dp on mi.id = dp.driver "
                        + " where mi.masterGroup = 26 and mi.company = ? " + conditionSQL
                        + " group by dp.driver order by mi.value ";
            }

            returnList = executeSQLQuery( sqlQuery, params.toArray());
            totalCount = returnList.size();

            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                returnList = executeSQLQueryPaging( sqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getIndividualVehicleDliveryReport(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            boolean isIndividualVehicleDeliveryReport = (Boolean) requestParams.get("isIndividualVehicleDeliveryReport");
            boolean isIndividualDriverDeliveryReport = (Boolean) requestParams.get("isIndividualDriverDeliveryReport");
            int moduleid = (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null) ? (Integer) requestParams.get(Constants.moduleid) : 0;

            String conditionSQL = "";
            ArrayList params = new ArrayList();
            params.add((String) requestParams.get("companyID"));

            String vehicleNo = (String) requestParams.get("vehicleNo");
            if (!StringUtil.isNullOrEmpty(vehicleNo)) {
                params.add(vehicleNo);
                conditionSQL = " and mi.id = ? ";
            }

            Date startDate = (Date) requestParams.get(Constants.REQ_startdate);
            Date endDate = (Date) requestParams.get(Constants.REQ_enddate);
            if (startDate != null && endDate != null) {
                params.add(startDate);
                params.add(endDate);
                conditionSQL += " and ( dp.deliverydate >=? and dp.deliverydate <=?) ";
            }
            
            if (moduleid != 0) {
                params.add(moduleid);
                conditionSQL += " and dp.module = ? ";
            }

            String sqlQuery = "";

            if (isIndividualVehicleDeliveryReport) {
                sqlQuery =
                        "select dp.deliverydate, DAYNAME(dp.deliverydate), count(distinct(dp.tripnumber)) as 'No. of Trip', count(distinct(dp.referencenumber)) as 'No. Of DO/PO', mi.id,mi.value, count(distinct(dp.documentno)) as 'No. Of PO' from masteritem mi "
                        + " left join deliveryplanner dp on mi.id = dp.vehiclenumber "
                        + " where masterGroup = 25 and mi.company = ? " + conditionSQL
                        + " group by DATE_FORMAT(dp.deliverydate,'%m-%d-%Y'), dp.vehiclenumber order by dp.deliverydate ";
            } else if (isIndividualDriverDeliveryReport) {
                sqlQuery =
                        "select dp.deliverydate, DAYNAME(dp.deliverydate), count(distinct(dp.tripnumber)) as 'No. of Trip', count(distinct(dp.referencenumber)) as 'No. Of DO/PO', mi.id,mi.value, count(distinct(dp.documentno)) as 'No. Of PO' from masteritem mi "
                        + " left join deliveryplanner dp on mi.id = dp.driver "
                        + " where masterGroup = 26 and mi.company = ? " + conditionSQL
                        + " group by DATE_FORMAT(dp.deliverydate,'%m-%d-%Y'), dp.driver order by dp.deliverydate ";
            }

            returnList = executeSQLQuery( sqlQuery, params.toArray());
            totalCount = returnList.size();

            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                returnList = executeSQLQueryPaging( sqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getIndividualVehicleDOPOReport(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            boolean isIndividualVehicleDOPOReport = (Boolean) requestParams.get("isIndividualVehicleDOPOReport");
            boolean isIndividualDriverDOPOReport = (Boolean) requestParams.get("isIndividualDriverDOPOReport");
            int moduleid = (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null) ? (Integer) requestParams.get(Constants.moduleid) : 0;

            String columnName = "";
            String joinSQL = "";
            String conditionSQL = "";
            ArrayList params = new ArrayList();
            params.add((String) requestParams.get("companyID"));
            
            if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                columnName = " po.ponumber ";
                joinSQL += " left join purchaseorder po on po.id = dp.documentno ";
            } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                columnName = " inv.invoicenumber ";
                joinSQL += " left join invoice inv on inv.id = dp.referencenumber ";
            }

            String vehicleNo = (String) requestParams.get("vehicleNo");
            if (!StringUtil.isNullOrEmpty(vehicleNo)) {
                params.add(vehicleNo);
                conditionSQL = " and mi2.id = ? ";
            }

            String startDate = (String) requestParams.get("startDate");
            if (!StringUtil.isNullOrEmpty(startDate)) {
                params.add(df.parse(startDate));
                conditionSQL += " and dp.deliverydate = ? ";
            }
            
            if (moduleid != 0) {
                params.add(moduleid);
                conditionSQL += " and dp.module = ? ";
            }

            String sqlQuery = "";

            if (isIndividualVehicleDOPOReport) {
                sqlQuery = "select " + columnName + " as 'DO/PO', mi1.value as 'Trip No.', dp.tripdescription,mi2.value from deliveryplanner dp "
                        + " left join masteritem mi1 on mi1.id = dp.tripnumber "
                        + " left join masteritem mi2 on mi2.id = dp.vehiclenumber " + joinSQL
                        + " where dp.company = ? " + conditionSQL;
            } else if (isIndividualDriverDOPOReport) {
                sqlQuery = "select " + columnName + " as 'DO/PO', mi1.value as 'Trip No.', dp.tripdescription,mi2.value from deliveryplanner dp "
                        + " left join masteritem mi1 on mi1.id = dp.tripnumber "
                        + " left join masteritem mi2 on mi2.id = dp.driver " + joinSQL
                        + " where dp.company = ? " + conditionSQL;
            }

            returnList = executeSQLQuery( sqlQuery, params.toArray());
            totalCount = returnList.size();

            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                returnList = executeSQLQueryPaging( sqlQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getNoOfTripsAndDOPOofVehicleForDay(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            boolean isIndividualVehicleDOPOReport = (Boolean) requestParams.get("isIndividualVehicleDOPOReport");
            boolean isIndividualDriverDOPOReport = (Boolean) requestParams.get("isIndividualDriverDOPOReport");
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            int moduleid = (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null) ? (Integer) requestParams.get(Constants.moduleid) : 0;

            String conditionSQL = "";
            ArrayList params = new ArrayList();
            params.add((String) requestParams.get("companyID"));

            String vehicleNo = (String) requestParams.get("vehicleNo");
            if (!StringUtil.isNullOrEmpty(vehicleNo)) {
                params.add(vehicleNo);
                if (isIndividualVehicleDOPOReport) {
                    conditionSQL = " and dp.vehiclenumber = ? ";
                } else if (isIndividualDriverDOPOReport) {
                    conditionSQL = " and dp.driver = ? ";
                }
            }

            String startDate = (String) requestParams.get("startDate");
            if (!StringUtil.isNullOrEmpty(startDate)) {
                params.add(df.parse(startDate));
                conditionSQL += " and dp.deliverydate = ? ";
            }
            
            if (moduleid != 0) {
                params.add(moduleid);
                conditionSQL += " and dp.module = ? ";
            }

            String sqlQuery = "select count(distinct(dp.tripnumber)) as 'No. of Trips', count(distinct(dp.referencenumber)), count(distinct(dp.documentno)) from deliveryplanner dp "
                    + " where dp.company = ? " + conditionSQL;

            returnList = executeSQLQuery( sqlQuery, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
    
    @Override
    public KwlReturnObject getCountOfInvoicesInDeliveryPlanner(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String conditionSQL = "";
            String columnName = "";
            ArrayList params = new ArrayList();
            params.add((String) requestParams.get("companyID"));

            String docID = "";
            if (requestParams.containsKey("docID") && requestParams.get("docID") != null) {
                docID = (String) requestParams.get("docID");
            }
            int moduleid = (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null) ? (Integer) requestParams.get(Constants.moduleid) : 0;
            
            conditionSQL += " and dp.module = ? ";
            params.add(moduleid);
            
            if (!StringUtil.isNullOrEmpty(docID)) {
                params.add(docID);
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    conditionSQL += " and dp.documentno = ? ";
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    conditionSQL += " and dp.referencenumber = ? ";
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    conditionSQL += " and dp.deliveryorder = ? ";
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    conditionSQL += " and dp.salesreturn = ? ";
                }
            }
            
            if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                columnName = "dp.documentno";
            } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                columnName = "dp.referencenumber";
            } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                columnName = "dp.deliveryorder";
            } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                columnName = "dp.salesreturn";
            }

            String sqlQuery = " select count(" + columnName + ") from deliveryplanner dp "
                    + " where dp.company = ? " + conditionSQL;

            returnList = executeSQLQuery( sqlQuery, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
    
    @Override
    public KwlReturnObject getDriversTrackingReport(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            
            ArrayList params = new ArrayList();
            String condition = "";
            params.add((String) requestParams.get("companyID"));
            
            String searchString = (String) requestParams.get("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                String[] searchcol = new String[]{"do.deliveryOrderNumber", "do.customer.name", "do.driver.value"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, searchString, 3);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                condition += searchQuery;
            }

            String query = " from DeliveryOrder do inner join do.driver where do.company.companyID = ? " + condition;

            returnList = executeQuery( query, params.toArray());
            totalCount = returnList.size();
            
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                returnList = executeQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
}
