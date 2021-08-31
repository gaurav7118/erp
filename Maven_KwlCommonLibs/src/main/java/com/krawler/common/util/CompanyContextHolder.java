/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.common.util;

import java.util.logging.Logger;


public class CompanyContextHolder {
    private static Logger _logger = Logger.getLogger(CompanyContextHolder.class.getName());
    private static final ThreadLocal<String> contextHolder = new InheritableThreadLocal<String>();
    private static final ThreadLocal<String> contextCompanyIDHolder = new InheritableThreadLocal<String>();
    private static final ThreadLocal<String> contextUserIDHolder = new InheritableThreadLocal<String>();

    public static void setCompanySubdomain(String subdomain) {

        if (contextHolder.get() != null && !contextHolder.get().equals(subdomain)) {
            _logger.warning("Resetting subdomain from " + contextHolder.get() + " to " + subdomain);
        }
        contextHolder.set(subdomain);
    }

    public static void setCompanyID(String companyId) {

        if (contextCompanyIDHolder.get() != null && !contextCompanyIDHolder.get().equals(companyId)) {
            _logger.warning("Resetting companyid from " + contextCompanyIDHolder.get() + " to " + companyId);
        }
        contextCompanyIDHolder.set(companyId);
    }

    public static void setUserID(String userId) {

        if (contextUserIDHolder.get() != null && !contextUserIDHolder.get().equals(userId)) {
            _logger.warning("Resetting companyid from " + contextUserIDHolder.get() + " to " + userId);
        }
        contextUserIDHolder.set(userId);
    }

    public static String getCompanySubdomain() {

        if (contextHolder.get() == null) {
            // default 
        }

        return (String) contextHolder.get();
    }

    public static String getCompanyID() {
        if (contextCompanyIDHolder.get() == null) {
            // default
        }
        return (String) contextCompanyIDHolder.get();
    }

    public static String getUserID() {
        if (contextUserIDHolder.get() == null) {
            // default
        }
        return (String) contextUserIDHolder.get();
    }

    public static void clearCompanySubdomain() {
        contextHolder.remove();
        contextCompanyIDHolder.remove();
        contextUserIDHolder.remove();
    }
}
