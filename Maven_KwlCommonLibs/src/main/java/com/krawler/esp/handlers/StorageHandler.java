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
package com.krawler.esp.handlers;

import com.krawler.esp.utils.ConfigReader;

public class StorageHandler {

    public static String GetProfileImgStorePath() {
        return ConfigReader.getinstance().get("ProfileImagePathBase");
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

    public static String GetSOAPServerUrl() {
        return ConfigReader.getinstance().get("soap_server_url");
    }

    public static String GetAuditTrailIndexPath() {
        return ConfigReader.getinstance().get("audittrailindex");
    }

    public static String GetRemoteAPIKey() {
        return ConfigReader.getinstance().get("remoteapikey");
    }

    public static String GetDocIndexPath() {
        return ConfigReader.getinstance().get("audittraildocindex");
    }

    public static String GetDocStorePath() {
        return ConfigReader.getinstance().get("DocStorePath0");
    }
    
    public static String GetSharedDocStorePath() {
        return ConfigReader.getinstance().get("SharedDocStorePath0");
    }

    public static String GetSummaryContext(String defaultContext) {
        return defaultContext;
    }

    public static String GetSummaryLength(String defaultLength) {
        return defaultLength;
    }

    public static String getAccURL() {
        return ConfigReader.getinstance().get("accURL");
    }

    public static String getStandalone() {
        return ConfigReader.getinstance().get("standalone");
    }

    public static String GetFileSeparator() {
        return System.getProperty("file.separator");
    }

    public static String GetPathSeparator() {
        return System.getProperty("path.separator");
    }

    public static String GetLineSeparator() {
        return System.getProperty("line.separator");
    }
}
