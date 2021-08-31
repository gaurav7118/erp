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
package com.krawler.common.admin;

public class AuditAction {

    public final static String LOG_IN_SUCCESS = "1";
    public final static String LOG_IN_FAILED = "2";
    public final static String LOGOUT_SUCCESS = "3";
    public final static String LOGOUT_FAILED = "4";
    public final static String COMPANY_CREATION = "5";
    public final static String COMPANY_UPDATION = "6";
    public final static String COMPANY_ACCOUNT_PREFERENCES_UPDATE = "7";
    public final static String COMPANY_DELETION = "8";
    public final static String ACCOUNT_CREATED = "9";
    public final static String ACCOUNT_UPDATED = "10";
    public final static String ACCOUNT_DELETED = "11";
    public final static String INVENTORY_OPENED = "12";
    public final static String PRODUCT_CREATION = "13";
    public final static String PRODUCT_DELETION = "14";
    public final static String PRODUCT_UPDATION = "15";
    public final static String UNIT_OF_MEASURE_CREATED = "16";
    public final static String UNIT_OF_MEASURE_UPDATED = "17";
    public final static String UNIT_OF_MEASURE_DELETED = "18";
    public final static String PRICE_CHANGED = "19";
    public final static String BANK_NAME_ADDED = "20";
    public final static String CUSTOMER_ADDED = "21";
    public final static String CUSTOMER_DELETED = "22";
    public final static String CUSTOMER_UPDATED = "23";
    public final static String VENDOR_ADDED = "24";
    public final static String VENDOR_DELETED = "25";
    public final static String VENDOR_UPDATED = "26";
    public final static String INVOICE_CREATED = "27";
    public final static String INVOICE_DELETED = "28";
    public final static String INVOICE_UPDATED = "29";
    public final static String CREDIT_NOTE_CREATED = "30";
    public final static String CREDIT_NOTE_DELETED = "31";
    public final static String CREDIT_NOTE_MODIFIED = "32";
    public final static String RECEIPT_ADDED = "33";
    public final static String RECEIPT_DELETED = "34";
    public final static String RECEIPT_MODIFIED = "35";
    public final static String JOURNAL_ENTRY_MADE = "36";
    public final static String USER_CREATED = "37";
    public final static String USER_MODIFIED = "38";
    public final static String USER_DELETED = "39";
    public final static String PERMISSIONS_MODIFIED = "40";
    public final static String PASSWORD_RESET = "41";
    public final static String PASSWORD_CHANGED = "42";
    public final static String PROFILE_CHANGED = "43";
    public final static String PAYMENT_METHOD_ADDED = "44";
    public final static String PAYMENT_METHOD_CHANGED = "45";
    public final static String PAYMENT_METHOD_DELETED = "46";
    public final static String PRODUCT_CATEGORY_ADDED = "47";
    public final static String PRODUCT_CATEGORY_CHANGED = "48";
    public final static String PRODUCT_CATEGORY_DELETED = "49";
    public final static String CREDIT_TERM_ADDED = "50";
    public final static String CREDIT_TERM_CHANGED = "51";
    public final static String CREDIT_TERM_DELETED = "52";
    public final static String TAX_DETAIL_CREATED = "53";
    public final static String TAX_DETAIL_UPDATED = "54";
    public final static String TAX_DETAIL_DELETED = "55";
    public final static String PRODUCT_TYPE_CREATED = "56";
    public final static String PRODUCT_TYPE_UPDATED = "57";
    public final static String PRODUCT_TYPE_DELETED = "58";
    public final static String FIXED_ASSET_LOCATION_REMOVED = "59";
    public final static String FIXED_ASSET_LOCATION_ADDED = "60";
    public final static String FIXED_ASSET_LOCATION_CHANGED = "61";
    public final static String PURCHASE_REQUISITION_CREATED = "62";
    public final static String PURCHASE_REQUISITION_UPDATED = "63";
    public final static String PURCHASE_REQUISITION_DELETED = "64";
    public final static String PURCHASE_REQUISITION_APPROVED = "65";
    public final static String PURCHASE_REQUISITION_REJECTED = "66";
    public final static String RFQ_CREATED = "67";
    public final static String RFQ_UPDATED = "68";
    public final static String RFQ_DELETED = "69";
    public final static String NOTE_TYPE_CREATED = "70";
    public final static String NOTE_TYPE_UPDATED = "71";
    public final static String NOTE_TYPE_DELETED = "72";
    public final static String Vendor_Quotation = "73";
    public final static String Customer_Quotation = "74";
    public final static String MAKE_PAYMENT = "75";
    public final static String JOURNAL_ENTRY_DELETED = "76";
    public final static String SALES_ORDER = "77";
    public final static String DELIVERY_ORDER = "78";
    public final static String SALES_RETURN = "79";
    public final static String DABIT_NOTE = "80";
    public final static String GOODS_RECEIPT_CREATED = "81";
    public final static String GOODS_RECEIPT_DELETED = "82";
    public final static String CASH_PURCHASE_RECEIPT_CREATED = "83";
    public final static String ACCOUNT_TYPE_CREATED = "84";
    public final static String ACCOUNT_TYPE_DELETED = "85";
    public final static String PURCHASE_ORDER = "86";
    public final static String PURCHASE_RETURN = "87";
    public final static String CUSTOMER_CATEGORY_CHANGED = "88";
    public final static String VENDOR_CATEGORY_CHANGED = "89";
    public final static String DABIT_NOTE_CREATED = "90";
    public final static String DABIT_NOTE_MODIFIED = "91";
    public final static String OPENING_BALANCE_CREATED = "92";
    public final static String OPENING_BALANCE_UPDATED = "93";
    public final static String OPENING_BALANCE_DELETED = "94";
    public final static String VENDORINVOICEAPPROVED = "95";
    public final static String CUSTOMERINVOICEAPPROVED = "96";
    public final static String PURCHASEORDERAPPROVED = "97";
    public final static String MONTHLYBUDGET = "98";
    public final static String MONTHLYFORECAST = "99";
    public final static String LINKEDPAYMENT = "100";
    public final static String LINKEDRECEIPT = "101";
    public final static String SENT_EMAIL = "102";
    public final static String USER_PROFILE = "103";
    public final static String APPROVAL_RULE = "104";
    public final static String MASTER_GROUP = "105";
    public final static String SALES_TERM_ADDED = "106";
    public final static String INVOICE_EXCLUDE = "107";
    public final static String PRODUCT_PRICE_CREATED = "108";
    public final static String LOCATION_CHANGED = "109";
    public final static String WAREHOUSE_CHANGED = "110";
    public final static String GROUP_UPDATED="112";
    public final static String CUSTOMTEMPLATE_DELETED="111";
    public final static String TEMPLATE_DELETED="113";
    public final static String ACTIVE_DATE_RANCE="114";
    public final static String BANK_RECONCILIATION_ADDED="115"; 
    public final static String CURRENCY_EXCHANGE_ADDED="116";
    public final static String ACCOUNT_REVALUATION_UPDATED="117";
    public final static String VENDOR_QUOTATION_DELETE = "118";
    public final static String GROUP_CREATED = "119";
    public final static String CUSTOMER_QUOTATION_UPDATED = "120";
    public final static String CUSTOMER_QUOTATION_ADDED = "121";
    public final static String COST_CENTER_ADDED="122";
    public final static String COST_CENTER_DELETED="123";   
    public final static String SALES_COMMISSION_ADDED="124";
    public final static String PURCHASE_TERM_ADDED = "125";
    public final static String PURCHASE_TERM_DELETED = "126";
    public final static String SALES_TERM_DELETED = "127";
    public final static String SEQUENCE_FORMATE_ADDED="128";
    public final static String SEQUENCE_FORMATE_DELETED="129";
    public final static String CASH_SALES_CREATED="130";
    public final static String CASH_SALES_DELETED="131";
    public final static String CASH_SALES_UPDATED="132";
    public final static String CASH_PURCHASE_CREATED="133";
    public final static String CASH_PURCHASE_DELETED="134";
    public final static String CASH_PURCHASE_UPDATED="135";
    public final static String CUSTOMIZE_AGED_ADDED="136";
    public final static String CUSTOMIZE_AGED_UPDATED="137";
    public final static String CUSTOMIZE_AGED_DELETED="138";
    public final static String EMAIL_NOTIFICATION_ADDED="139";
    public final static String EMAIL_NOTIFICATION_UPDATED="140";
    public final static String EMAIL_NOTIFICATION_DELETED="141";
    public final static String DEBIT_NOTE_DELETED = "142";
    public final static String GROUP_DELETED = "143";        
    public final static String CASH_PURCHASE_APPROVED = "144"; 
    public final static String CASH_SALES_APPROVED = "145"; 
    public final static String AMEND_PRICE_REMOVED = "146"; 
    public final static String TEMPLATE_UPLOAD="147";
    public final static String TEMPLATE_CUSTOMIZE="148"; 
    public final static String DIMENTION_ADDED="149"; 
    public final static String DIMENTION_UPDATED="150"; 
    public final static String CUSTOM_FIELD_ADDED = "151";
    public final static String CUSTOM_FIELD_UPDATED = "152";
    public final static String CUSTOM_COLUMN_ADDED = "153";
    public final static String CUSTOM_COLUMN_UPDATED = "154";
    public final static String DEPARTMENT_ADDED = "155";
    public final static String DEPARTMENT_UPDATED = "156";  
    public final static String TEMPLATE_CREATED="157";
    public final static String BANK_RECONCILIATION_DELETED="158"; 
    public final static String ASSET_MAINTENANCE_SCHEDULE_ADDED="159";
    public final static String ASSET_MAINTENANCE_SCHEDULE_UPDATED="160";
    public final static String ASSET_MAINTENANCE_SCHEDULE_DELETED="161";
    public final static String ASSET_MAINTENANCE_WORK_ORDER_ADDED="162";
    public final static String ASSET_MAINTENANCE_WORK_ORDER_UPDATED="163";
    public final static String ASSET_MAINTENANCE_WORK_ORDER_DELETED="164";
    public final static String PRODUCT_BUILD_ASSEMBLY_ADDED="165";
    public final static String PRODUCT_BUILD_ASSEMBLY_UPDATED="166";
    public final static String PRODUCT_BUILD_ASSEMBLY_DELETION="167";
    public final static String DELIVERY_PLANNER_ADDED = "168";
    public final static String DELIVERY_PLANNER_UPDATED = "169";
    public final static String Packing_Do_List = "170";
    public final static String PRICING_BAND_CHANGED = "171";
    public final static String PRICE_LIST_VOLUME_DISCOUNT_CHANGED = "172";
    public final static String SERIAL_CHANGED = "173";
    public final static String STOCK_REQUEST_ADDED = "174";
    public final static String STOCK_REQUEST_ISSUED = "175";
    public final static String STOCK_REQUEST_COLLECTED = "176";
    public final static String ISSUE_NOTE_ADDED = "177";
    public final static String INTER_STORE_REQUEST_ADDED = "178";
    public final static String INTER_STORE_REQUEST_ACCEPTED = "179";
    public final static String INTER_STORE_REQUEST_REJECTED = "180";
    public final static String INTER_LOCATION_REQUEST_REJECTED = "181";
    public final static String STOCK_ADJUSTMENT_ADDED = "182";
    public final static String QA_INSPECTION = "183";
    public final static String SEQ_FORMAT_INVENTORY = "184";
    public final static String PRODUCT_THRESHOLD = "185";
    public final static String STORE_MASTER = "186";
    public final static String LOCATION_MASTER = "187";
    public final static String INSPECTION_TEMPLATE = "188";
    public final static String CHEQUE_LAYOUT = "189";
    public final static String RFQ_DELETED_PERMANENT = "190";
    public final static String STOCK_AUTOASSIGNED = "191";
    public final static String INVENTORY_CONFIG = "192";
    public final static String UNLINK_SO_FROM_PO = "193";
    public final static String UNLINK_PO_FROM_SO = "194";
    public final static String Allow_To_Post_Manual_JE = "195";
    public final static String UNLINK_SO_FROM_SI = "196";
    public final static String UNLINK_DO_FROM_SI = "197";
    public final static String UNLINK_CQ_FROM_SI = "198";
    public final static String UNLINK_PO_FROM_PI = "199";
    public final static String UNLINK_GR_FROM_PI = "200";
    public final static String UNLINK_VQ_FROM_PI = "201";
    public final static String UNLINK_PREQ_FROM_VQ = "202";
    public final static String UNLINK_VQ_FROM_CQ = "203";
    public final static String UNLINK_VQ_FROM_PO = "204";
    public final static String UNLINK_CQ_FROM_SO = "205";
    public final static String UNLINK_PO_FROM_GR = "206";
    public final static String UNLINK_PI_FROM_GR = "207";
    public final static String UNLINK_SO_FROM_DO = "208";
    public final static String UNLINK_SI_FROM_DO = "209";
    public final static String UNLINK_GR_FROM_PR = "210";
    public final static String UNLINK_PI_FROM_PR = "210";
    public final static String UNLINK_DO_FROM_SR = "211";
    public final static String UNLINK_SI_FROM_SR = "212";
    public final static String Written_Off_Sales_Invoice = "213";
    public final static String Written_Off_Sales_Invoice_Reverse = "214";
    public final static String IMPORT_MASTER = "215";
    public final static String REPEATED_INVOICE_DELETE = "216";
    public final static String Cron_Success = "217";
    public final static String CUSTOMER_SALESPERSON_CHANGED = "218";
    public final static String VENDOR_AGENT_CHANGED = "219";
    public final static String Written_Off_Receipt = "220";
    public final static String Written_Off_Receipt_Reverse = "221";
    public final static String ADD_RECURRING_MAIL_DETAIL = "222";
    public final static String UPDATE_RECURRING_MAIL_DETAIL = "223";
    public final static String REPEATED_JE_DELETE = "224";
    public final static String REPEATED_MP_DELETE = "225";
    public final static String REPEATED_SO_DELETE = "225";
    public final static String PURCHASE_ORDER_BLOCKED_UNBLOCKED = "226";
    public final static String SALES_ORDER_BLOCKED_UNBLOCKED = "226";
    public final static String UNLINK_RFQ_FROM_VQ = "227";
    public final static String PRICE_DELETED = "228";
    public final static String CC_CALENDAR_UPDATED = "229"; // cycle count
    public final static String CC_ADDED = "230"; //cycle count
    public final static String CREDIT_NOTE_APPROVED = "231";
    public final static String CREDIT_NOTE_REJECTED = "232";
    public final static String DEBIT_NOTE_APPROVED = "233";
    public final static String DEBIT_NOTE_REJECTED = "234";
    public final static String Labour_MANAGEMENT = "235";
    public final static String SAVE_ACCOUNTINGPERIOD = "236";
    public final static String SAVE_TAXPERIOD = "237";
    public final static String DELETE_ACCOUNTINGPERIOD = "238";
    public final static String DELETE_TAXPERIOD = "239";
    public final static String PRODUCT_BRAND_DISCOUNT_CHANGED = "240";
    public final static String FIXED_ASSET_DISPOSEASSET = "241";
    public final static String FIXED_ASSET_REVERTDISPOSEDASSET = "242";
    public final static String WORKCENTRE_MANAGEMENT = "243";
    public final static String CUSTOMER_ACTIVATE_DEACTIVATE = "244";
    public final static String VENDOR_ACTIVATE_DEACTIVATE = "245";
    public final static String CUSTOMER_DEFAULTWAREHOUSE = "246";
    public final static String PRODUCT_IMAGEUPLOAD = "247";
    public final static String ACCOUNT_ACTIVATE_DEACTIVATE = "248";
    public final static String SALESPERSON_ACTIVATE_DEACTIVATE = "249";
    public final static String ACTIVATE_DEACTIVATE_CUSTOM_FIELD_DIMENSION = "250";
    public final static String SALESPERSON_SALESCOMISSIONSCEHMA = "251";
    public final static String  ORDERING_CUSTOM_FIELDS_DIMENSION = "252";
    public final static String FIXED_ASSET_DELETEDISPOSEDASSET = "253";
    public final static String ACTIVE_DAYS_PERIOD = "254";
    public final static String SETROLE_PERMISSION = "255";
    public final static String UOMSCHEMA_ADDEDIT = "256";
    public final static String UOMSCHEMA_DELETE = "257";
    public final static String CONFIGURE_UOMSCHEMA = "258";
    public final static String CUSTOMTEMPLATE_ADDEDIT = "259";
    public final static String CUSTOMTEMPLATE_COPY="260";
    public final static String DELETE_CONSIGNMENTAPPROVALRULE="261";
    public final static String PERMISSIONS_TO_VIEW_RECORDS = "262";
    public final static String POSTED_DEPRECIATION = "263";
    public final static String UNPOSTED_DEPRECIATION = "264";
    public final static String PRODUCT_UNBUILD_ASSEMBLY_ADDED="301";
    public final static String PRODUCT_UNBUILD_ASSEMBLY_UPDATED="302";
    public final static String PURCHASE_ORDER_CLOSED_MANUALLY = "303";
    public final static String SALES_ORDER_CLOSED_MANUALLY = "304";
    public final static String ADD_MANAGE_ELIGIBILITY_RULES = "305";
    public final static String UPDATE_MANAGE_ELIGIBILITY_RULES = "306";
    public final static String DELETE_MANAGE_ELIGIBILITY_RULES = "307";
    public final static String UPDATE_DISBURSEMENT = "308";
    public final static String ADD_DISBURSEMENT = "309";
    public final static String DELETE_DISBURSEMENT = "310";
    public final static String ADD_ROUTING_TEMPLATE = "311";
    public final static String EDIT_ROUTING_TEMPLATE = "312";
    public final static String DELETE_ROUTING_TEMPLATE = "313";
    public final static String ASSET_CREATION = "314";
    public final static String ASSET_DELETION = "315";
    public final static String ASSET_UPDATION = "316";
    public final static String WORK_ORDER_CREATE = "317";
    public final static String WORK_ORDER_EDIT = "318";
    public final static String WORK_ORDER_DELETE = "319";
    public final static String FORECAST_TEMPLATE = "320";
    public final static String MAKE_PAYMENT_REJECTED = "321";
    public final static String MAKE_PAYMENT_APPROVED = "322";
    public final static String WORK_ORDER_STARTED = "323";
    public final static String WORK_ORDER_CLOSED = "324";
    public final static String PRODUCT_INDUSRTYCODE_CHANGED = "325";
    public final static String ADD_RECURRING_SO_ENTRY = "326";
    public final static String ADD_RECURRING_SALES_INVOICE_ENTRY = "327";
    public final static String ADD_RECURRING_PURCHASE_INVOICE_ENTRY = "328";
    public final static String ADD_RECURRING_JOURNAL_ENTRY_ENTRY = "329";
    public final static String ADD_RECURRING_MAKE_PAYMENT_ENTRY = "330";
    public final static String MACHINE_MASTER_CREATED = "331";
    public final static String MACHINE_MASTER_UPDATED = "332";
    public final static String MACHINE_MASTER_DELETED = "333";
    public final static String YEAR_END_CLOSING = "334";
    public final static String REVERSAL_OF_YEAR_END_CLOSING = "335";
    public final static String GIRO_GENERATION_FOR_UOBBank = "336";
    public final static String LABOUR_CREATED= "337";
    public final static String LABOUR_UPDATED= "338";
    public final static String LABOUR_DELETED= "339";
    public final static String SALESCOMMISIONSCHEMA_ADDED = "340";
    public final static String SALESCOMMISIONSCHEMA_EDITED = "341";
    public final static String SALESCOMMISIONSCHEMA_DELETED = "342";
    public final static String STOCK_ADJUSTMENT_DELETED="343";
    public final static String RECEIVE_PAYMENT_REJECTED = "344";
    public final static String RECEIVE_PAYMENT_APPROVED = "345";
    public final static String PRODUCT_IMAGE_DELETE = "346";
    public final static String USER_CHECKIN = "347";
    public final static String USER_CHECKOUT = "348";
    public final static String INCIDENT_ADDED = "349";
    public final static String INCIDENT_UPDATED = "350";
    public final static String INCIDENT_DELETED = "351";    
    public final static String RECEIVE_PAYMENT = "352";
    public final static String SECURYGATEENTRYCREATED = "353";
    public final static String ROUNDING_OFF_JE_CREATED = "354";
    public final static String ROUNDING_OFF_JE_DELETED = "355";
    public final static String ROUNDING_OFF_JE_UPDATED = "356";
    public final static String DISCOUNT_MASTER_DELETED = "357";
    public final static String DISCOUNT_MASTER_UPDATED = "358";
    public final static String DISCOUNT_MASTER_ASSIGNED = "359";
    public final static String GST = "360";
    public final static String GROUP_COMPANY_CREATED = "361";
    public final static String GROUP_COMPANY_UPDATE = "362";
    public final static String GST_RULE_DELETE = "363";
    public final static String RCM_URD_JE_UPDATED = "364"; // When RCM PI from Unregistered cross The daily limit then update All Tax JE details for that bill date
    public final static String RCM_URD_JE_DELETED = "365"; // When RCM PI from Unregistered not cross The daily limit then delete All Tax JE details for that bill date
    public final static String GST_TERM_DELETE = "366";
    public final static String POS_STORE_ADD = "367";
    public final static String POS_STORE_EDIT = "368";
    public final static String GST_DETAIL_ADD_UPDATE = "369";// Audit Trial Entry For Add Entity GST Details In Company Preferences
    public final static String GST_DETAIL_DELETE = "370";// Audit Trial Entry For Delete Entity GST Details In Company Preferences 
    public final static String GST_GENERATION_03 = "371"; // Audit Trial Entry For Malaysia GST Form Genration
    public final static String MRP_JOBWORKOUT_ADDED = "372";
    public final static String MRP_JOBWORKOUT_UPDATED = "373";
    public final static String MRP_JOBWORKOUT_DELETED = "374";
    /**
     * ERP-40117 
     * Password policy feature for company is added. In case of
     * adding/editing password policy for company for inserting audit log
     * constants are added.
     */
    public final static String PASSWORD_POLICY_ADDED = "375";
    public final static String PASSWORD_POLICY_UPDATED = "376";
    /* POJO fields*/
    private String ID;
    private String actionName;
    private AuditGroup auditGroup;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public AuditGroup getAuditGroup() {
        return auditGroup;
    }

    public void setAuditGroup(AuditGroup auditGroup) {
        this.auditGroup = auditGroup;
    }
}
