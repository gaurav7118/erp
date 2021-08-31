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
package com.krawler.common.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.PropertyConfigurator;

/**
 * @author schemers
 *
 */
public class KrawlerLog {

    /**
     * "ip" key for context. IP of request
     */
    public static final String C_IP = "ip";
    /**
     * "id" key for context. Id of the target account
     */
    public static final String C_ID = "id";
    /**
     * "name" key for context. Id of the target account
     */
    public static final String C_NAME = "name";
    /**
     * "aid" key for context. Id in the auth token. Only present if target id is
     * different then auth token id.
     */
    public static final String C_AID = "aid";
    /**
     * "aname" key for context. name in the auth token. Only present if target
     * id is different then auth token id.
     */
    public static final String C_ANAME = "aname";
    /**
     * "cid" is the connection id of a server that is monotonically increasing -
     * useful for tracking individual connections.
     */
    public static final String C_CONNECTIONID = "cid";
    /**
     * "mid" key for context. Id of requested mailbox. Only present if request
     * is dealing with a mailbox.
     */
    private static final String C_MID = "mid";
    /**
     * "ua" key for context. The name of the client application.
     */
    public static final String C_USER_AGENT = "ua";
    /**
     * the "krawler.misc" logger. For all events that don't have a
     * specific-catagory.
     */
    public static final Log misc = LogFactory.getLog("krawler.misc");
    /**
     * the "krawler.index" logger. For indexing-related events.
     */
    public static final Log index = LogFactory.getLog("krawler.index");
    /**
     * the "krawler.redolog" logger. For redolog-releated events.
     */
    public static final Log redolog = LogFactory.getLog("krawler.redolog");
    /**
     * the "krawler.lmtp" logger. For LMTP-related events.
     */
    public static final Log lmtp = LogFactory.getLog("krawler.lmtp");
    /**
     * the "krawler.imap" logger. For IMAP-related events.
     */
    public static final Log imap = LogFactory.getLog("krawler.imap");
    /**
     * the "krawler.imap" logger. For POP-related events.
     */
    public static final Log pop = LogFactory.getLog("krawler.pop");
    /**
     * the "krawler.mailbox" logger. For mailbox-related events.
     */
    public static final Log mailbox = LogFactory.getLog("krawler.mailbox");
    /**
     * the "krawler.calendar" logger. For calendar-related events.
     */
    public static final Log calendar = LogFactory.getLog("krawler.calendar");
    /**
     * the "krawler.calendar" logger. For calendar-related events.
     */
    public static final Log im = LogFactory.getLog("krawler.im");
    /**
     * the "krawler.account" logger. For account-related events.
     */
    public static final Log account = LogFactory.getLog("krawler.account");
    /**
     * the "krawler.security" logger. For security-related events
     */
    public static final Log security = LogFactory.getLog("krawler.security");
    /**
     * the "krawler.soap" logger. For soap-related events
     */
    public static final Log soap = LogFactory.getLog("krawler.soap");
    /**
     * the "krawler.test" logger. For testing-related events
     */
    public static final Log test = LogFactory.getLog("krawler.test");
    /**
     * the "krawler.sqltrace" logger. For tracing SQL statements sent to the
     * database
     */
    public static final Log sqltrace = LogFactory.getLog("krawler.sqltrace");
    /**
     * the "krawler.dbconn" logger. For tracing database connections
     */
    public static final Log dbconn = LogFactory.getLog("krawler.dbconn");
    /**
     * the "krawler.perf" logger. For logging performance statistics
     */
    public static final Log perf = LogFactory.getLog("krawler.perf");
    /**
     * the "krawler.cache" logger. For tracing object cache activity
     */
    public static final Log cache = LogFactory.getLog("krawler.cache");
    /**
     * the "krawler.filter" logger. For filter-related logs.
     */
    public static final Log filter = LogFactory.getLog("krawler.filter");
    /**
     * the "krawler.session" logger. For session- and notification-related logs.
     */
    public static final Log session = LogFactory.getLog("krawler.session");
    /**
     * the "krawler.backup" logger. For backup/restore-related logs.
     */
    public static final Log backup = LogFactory.getLog("krawler.backup");
    /**
     * the "krawler.system" logger. For startup/shutdown and other related logs.
     */
    public static final Log system = LogFactory.getLog("krawler.system");
    /**
     * the "krawler.sync" logger. For sync client interface logs.
     */
    public static final Log sync = LogFactory.getLog("krawler.sync");
    /**
     * the "krawler.synctrace" logger. For sync client interface logs.
     */
    public static final Log synctrace = LogFactory.getLog("krawler.synctrace");
    /**
     * the "krawler.syncstate" logger. For sync client interface logs.
     */
    public static final Log syncstate = LogFactory.getLog("krawler.syncstate");
    /**
     * the "krawler.wbxml" logger. For wbxml client interface logs.
     */
    public static final Log wbxml = LogFactory.getLog("krawler.wbxml");
    /**
     * the "krawler.extensions" logger. For logging extension loading related
     * info.
     */
    public static final Log extensions = LogFactory.getLog("krawler.extensions");
    /**
     * the "krawler.zimlet" logger. For logging zimlet related info.
     */
    public static final Log zimlet = LogFactory.getLog("krawler.zimlet");
    /**
     * the "krawler.wiki" logger. For wiki and document sharing.
     */
    public static final Log wiki = LogFactory.getLog("krawler.wiki");
    /**
     * the "krawler.op" logger. Logs server operations
     */
    public static final Log op = LogFactory.getLog("krawler.op");
    /**
     * the "krawler.dav" logger. Logs dav operations
     */
    public static final Log dav = LogFactory.getLog("krawler.dav");
    /**
     * the "krawler.io" logger. Logs file IO operations.
     */
    public static final Log io = LogFactory.getLog("krawler.io");
    /**
     * remote management.
     */
    public static final Log rmgmt = LogFactory.getLog("krawler.rmgmt");
    private static final ThreadLocal<Map<String, String>> sContextMap = new ThreadLocal<Map<String, String>>();
    private static final ThreadLocal<String> sContextString = new ThreadLocal<String>();
    private static final Set<String> CONTEXT_KEY_ORDER = new LinkedHashSet<String>();

    static {
        CONTEXT_KEY_ORDER.add(C_NAME);
        CONTEXT_KEY_ORDER.add(C_ANAME);
        CONTEXT_KEY_ORDER.add(C_MID);
        CONTEXT_KEY_ORDER.add(C_IP);
    }

    static String getContextString() {
        return sContextString.get();
    }

    /**
     * Adds a key/value pair to the current thread's logging context. If
     * <tt>key</tt> is null, does nothing. If <tt>value</tt> is null, removes
     * the context entry.
     */
    public static void addToContext(String key, String value) {
        if (key == null) {
            return;
        }

        Map<String, String> contextMap = sContextMap.get();
        boolean contextChanged = false;

        if (StringUtil.isNullOrEmpty(value)) {
            // Remove
            if (contextMap != null) {
                String oldValue = contextMap.remove(key);
                if (oldValue != null) {
                    contextChanged = true;
                }
            }
        } else {
            // Add
            if (contextMap == null) {
                contextMap = new HashMap<String, String>();
                sContextMap.set(contextMap);
            }
            String oldValue = contextMap.put(key, value);
            if (!StringUtil.equal(oldValue, value)) {
                contextChanged = true;
            }
        }
        if (contextChanged) {
            updateContextString();
        }
    }

    /**
     * Updates the context string with the latest data in {@link #sContextMap}.
     */
    private static void updateContextString() {
        Map<String, String> contextMap = sContextMap.get();
        if (contextMap == null || contextMap.size() == 0) {
            sContextString.set(null);
            return;
        }

        StringBuffer sb = new StringBuffer();

        // Append ordered keys first
        for (String key : CONTEXT_KEY_ORDER) {
            String value = contextMap.get(key);
            if (value != null) {
                encodeArg(sb, key, value);
            }
        }

        // Append the rest
        for (String key : contextMap.keySet()) {
            if (!CONTEXT_KEY_ORDER.contains(key)) {
                String value = contextMap.get(key);
                if (key != null && value != null) {
                    encodeArg(sb, key, value);
                }
            }
        }

        sContextString.set(sb.toString());
    }

    /**
     * Adds a <tt>MailItem</tt> id to the current thread's logging context.
     */
    public static void addItemToContext(int itemId) {
        addToContext("item", Integer.toString(itemId));
    }

    /**
     * Removes a key/value pair from the current thread's logging context.
     */
    public static void removeFromContext(String key, String value) {
        if (key != null) {
            addToContext(key, null);
        }
    }

    /**
     * Removes a <tt>MailItem</tt> id from the current thread's logging context.
     */
    public static void removeItemFromContext(int itemId) {
        removeFromContext("item", Integer.toString(itemId));
    }

    /**
     * Adds account name to the current thread's logging context.
     */
    public static void addAccountNameToContext(String accountName) {
        KrawlerLog.addToContext(C_NAME, accountName);
    }

    /**
     * Adds ip to the current thread's logging context.
     */
    public static void addIpToContext(String ipAddress) {
        KrawlerLog.addToContext(C_IP, ipAddress);
    }

    /**
     * Adds connection id to the current thread's logging context.
     */
    public static void addConnectionIdToContext(String connectionId) {
        KrawlerLog.addToContext(C_CONNECTIONID, connectionId);
    }

    /**
     * Adds mailbox id to the current thread's logging context.
     */
    public static void addMboxToContext(int mboxId) {
        addToContext(C_MID, Integer.toString(mboxId));
    }

    /**
     * Clears the current thread's logging context.
     *
     */
    public static void clearContext() {
        Map<String, String> contextMap = sContextMap.get();
        if (contextMap != null) {
            contextMap.clear();
        }
        sContextString.remove();
    }

    /**
     * Setup log4j for our command line tools.
     *
     * If System.getProperty(krawler.log4j.level) is set then log at that level.
     * Else log at the specified defaultLevel.
     */
    public static void toolSetupLog4j(String defaultLevel, String logFile,
            boolean showThreads) {
        String level = System.getProperty("krawler.log4j.level");
        if (level == null) {
            level = defaultLevel;
        }
        Properties p = new Properties();
        p.put("log4j.rootLogger", level + ",A1");
        if (logFile != null) {
            p.put("log4j.appender.A1", "org.apache.log4j.FileAppender");
            p.put("log4j.appender.A1.File", logFile);
            p.put("log4j.appender.A1.Append", "false");
        } else {
            p.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        }
        p.put("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
        if (showThreads) {
            p.put("log4j.appender.A1.layout.ConversionPattern",
                    "[%t] [%x] %p: %m%n");
        } else {
            p.put("log4j.appender.A1.layout.ConversionPattern",
                    "[%x] %p: %m%n");
        }
        PropertyConfigurator.configure(p);
    }

    /**
     * Setup log4j for command line tool using specified log4j.properties file.
     * If file doesn't exist
     * System.getProperty(krawler.home)/conf/log4j.properties file will be used.
     *
     * @param defaultLevel
     * @param propsFile full path to log4j.properties file
     */
    public static void toolSetupLog4j(String defaultLevel, String propsFile) {
        if (propsFile != null && new File(propsFile).exists()) {
            PropertyConfigurator.configure(propsFile);
        } else {
            toolSetupLog4j(defaultLevel, null, false);
        }
    }

    private static void encodeArg(StringBuffer sb, String name, String value) {
        if (value == null) {
            value = "";
        }
        if (value.indexOf(';') != -1) {
            value = value.replaceAll(";", ";;");
        }
        // replace returns ref to original string if char to replace doesn't
        // exist
        value = value.replace('\r', ' ');
        value = value.replace('\n', ' ');
        sb.append(name);
        sb.append("=");
        sb.append(value);
        sb.append(';');
    }

    /**
     * Take an array of Strings [ "name1", "value1", "name2", "value2", ...] and
     * format them for logging purposes.
     *
     * @param strings
     * @return
     */
    public static String encodeAttrs(String[] args) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args.length; i += 2) {
            if (i > 0) {
                sb.append(' ');
            }
            encodeArg(sb, args[i], args[i + 1]);
        }
        return sb.toString();
    }

    /**
     * Take an array of Strings [ "name1", "value1", "name2", "value2", ...] and
     * format them for logging purposes into: name1=value1; name2=value;
     * semicolons are escaped with two semicolons (value a;b is encoded as a;;b)
     *
     * @param strings
     * @return
     */
    public static String encodeAttrs(String[] args, Map extraArgs) {
        StringBuffer sb = new StringBuffer();
        boolean needSpace = false;
        for (int i = 0; i < args.length; i += 2) {
            if (needSpace) {
                sb.append(' ');
            } else {
                needSpace = true;
            }
            encodeArg(sb, args[i], args[i + 1]);
        }
        if (extraArgs != null) {
            for (Iterator it = extraArgs.entrySet().iterator(); it.hasNext();) {
                if (needSpace) {
                    sb.append(' ');
                } else {
                    needSpace = true;
                }
                Map.Entry entry = (Entry) it.next();
                String name = (String) entry.getKey();
                Object v = entry.getValue();
                if (v == null) {
                    encodeArg(sb, name, "");
                } else if (v instanceof String) {
                    encodeArg(sb, name, (String) v);
                } else if (v instanceof String[]) {
                    String values[] = (String[]) v;
                    for (int i = 0; i < values.length; i++) {
                        encodeArg(sb, name, values[i]);
                    }
                }
            }
        }
        return sb.toString();
    }
}
