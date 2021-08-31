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
package com.krawler.spring.storageHandler;

import com.krawler.esp.utils.ConfigReader;

/**
 *
 * @author Karthik
 */
public class storageHandlerImpl {

    public static String GetProfileImgStorePath() {
        return ConfigReader.getinstance().get("ProfileImagePathBase");
    }

    public static String GetSOAPServerUrl() {
        return ConfigReader.getinstance().get("soap_server_url");
    }

    public static String GetAuditTrailIndexPath() {
        return ConfigReader.getinstance().get("audittrailindex");
    }

    public static String GetDocIndexPath() {
        return ConfigReader.getinstance().get("audittraildocindex");
    }

    public static String GetDocStorePath() {
        return ConfigReader.getinstance().get("DocStorePath0");
    }

    public static String GetSummaryContext(String defaultContext) {
        return defaultContext;
    }

    public static String GetSummaryLength(String defaultLength) {
        return defaultLength;
    }

    public static String getDefaultTimeZoneID() {
        return ConfigReader.getinstance().get("defaulttimezone");
    }

    public static String getDefaultDateFormatID() {
        return ConfigReader.getinstance().get("defaultdateformat");
    }

    public static String getDefaultCurrencyID() {
        return ConfigReader.getinstance().get("defaultcurrency");
    }

    public static String GetRemoteAPIKey() {
        return ConfigReader.getinstance().get("remoteapikey");
    }

    public static String GetBCHLCompanyId() {
        return ConfigReader.getinstance().get("BCHL_CompanyId");
    }

    public static String GetVRnetCompanyId() {
        return ConfigReader.getinstance().get("VRnet_CompanyId");
    }

    public static String GetLSHCompanyId() {
        return ConfigReader.getinstance().get("LSH_CompanyId");
    }

    public static String GetVHQCompanyId() {
        return ConfigReader.getinstance().get("VHQ_CompanyId");
    }

    public static String GetLowercaseCompanyId() {
        return ConfigReader.getinstance().get("Lowercase_CompanyId");
    }

    public static String GetSATSCompanyId() {
        return ConfigReader.getinstance().get("SATS_CompanyId");
    }

    public static Object VHQPOSTMYCompanyId() {
        return ConfigReader.getinstance().get("VHQPOSTMY_CompanyId");
    }
    
    public static Object VHQPOSTSubdomains() {
        return ConfigReader.getinstance().get("VHQPOST_Subdomains");
    }
    
    public static Object SMSCompanyIds() {
        return ConfigReader.getinstance().get("SMS_CompanyIds");
    }
        
    public static String GetinventoryURL() {
        return ConfigReader.getinstance().get("inventoryURL");
    }

    public static String GetGROApprovalListStorePath() {
        return ConfigReader.getinstance().get("GoodsReceiptOrderApprovalListStorePath");
    }

    public static Object SBICompanyId() {
        return ConfigReader.getinstance().get("SBI_CompanyId");
    }
}
