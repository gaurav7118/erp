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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class LogFactory {

    private static Map<Class, Log> sLogsByClass = new HashMap<Class, Log>();
    private static Map<String, Log> sLogsByName = new HashMap<String, Log>();

    public static Log getLog(Class clazz) {
        Log log = null;

        synchronized (sLogsByClass) {
            log = sLogsByClass.get(clazz);
            if (log == null) {
                Logger log4jLogger = Logger.getLogger(clazz);
                log = new Log(log4jLogger);
                sLogsByClass.put(clazz, log);
            }
        }

        return log;
    }

    public static Log getLog(String name) {
        Log log = null;

        synchronized (sLogsByName) {
            log = sLogsByName.get(name);
            if (log == null) {
                Logger log4jLogger = Logger.getLogger(name);
                log = new Log(log4jLogger);
                sLogsByName.put(name, log);
            }
        }

        return log;
    }
}
