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

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class Log {

    Logger mLogger;

    Log(Logger logger) {
        if (logger == null) {
            throw new IllegalStateException("logger cannot be null");
        }
        mLogger = logger;
    }

    public boolean isDebugEnabled() {
        return mLogger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return mLogger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return mLogger.isEnabledFor(Priority.WARN);
    }

    public boolean isErrorEnabled() {
        return mLogger.isEnabledFor(Priority.ERROR);
    }

    public boolean isFatalEnabled() {
        return mLogger.isEnabledFor(Priority.FATAL);
    }

    public void debug(Object o) {
        mLogger.debug(o);
    }

    public void debug(Object o, Throwable t) {
        mLogger.debug(o, t);
    }

    public void debug(String format, Object... objects) {
        if (isDebugEnabled()) {
            mLogger.debug(String.format(format, objects));
        }
    }

    public void debug(String format, Object o, Throwable t) {
        if (isDebugEnabled()) {
            mLogger.debug(String.format(format, o), t);
        }
    }

    public void debug(String format, Object o1, Object o2, Throwable t) {
        if (isDebugEnabled()) {
            mLogger.debug(String.format(format, o1, o2), t);
        }
    }

    public void debug(String format, Object o1, Object o2, Object o3,
            Throwable t) {
        if (isDebugEnabled()) {
            mLogger.debug(String.format(format, o1, o2, o3), t);
        }
    }

    public void info(Object o) {
        mLogger.info(o);
    }

    public void info(Object o, Throwable t) {
        mLogger.info(o, t);
    }

    public void info(String format, Object... objects) {
        if (isInfoEnabled()) {
            mLogger.info(String.format(format, objects));
        }
    }

    public void info(String format, Object o, Throwable t) {
        if (isInfoEnabled()) {
            mLogger.info(String.format(format, o), t);
        }
    }

    public void info(String format, Object o1, Object o2, Throwable t) {
        if (isInfoEnabled()) {
            mLogger.info(String.format(format, o1, o2), t);
        }
    }

    public void info(String format, Object o1, Object o2, Object o3, Throwable t) {
        if (isInfoEnabled()) {
            mLogger.info(String.format(format, o1, o2, o3), t);
        }
    }

    public void warn(Object o) {
        mLogger.warn(o);
    }

    public void warn(Object o, Throwable t) {
        mLogger.warn(o, t);
    }

    public void warn(String format, Object... objects) {
        if (isWarnEnabled()) {
            mLogger.warn(String.format(format, objects));
        }
    }

    public void warn(String format, Object o, Throwable t) {
        if (isWarnEnabled()) {
            mLogger.warn(String.format(format, o), t);
        }
    }

    public void warn(String format, Object o1, Object o2, Throwable t) {
        if (isWarnEnabled()) {
            mLogger.warn(String.format(format, o1, o2), t);
        }
    }

    public void warn(String format, Object o1, Object o2, Object o3, Throwable t) {
        if (isWarnEnabled()) {
            mLogger.warn(String.format(format, o1, o2, o3), t);
        }
    }

    public void error(Object o) {
        mLogger.error(o);
    }

    public void error(Object o, Throwable t) {
        mLogger.error(o, t);
    }

    public void error(String format, Object... objects) {
        if (isErrorEnabled()) {
            mLogger.error(String.format(format, objects));
        }
    }

    public void error(String format, Object o, Throwable t) {
        if (isErrorEnabled()) {
            mLogger.error(String.format(format, o), t);
        }
    }

    public void error(String format, Object o1, Object o2, Throwable t) {
        if (isErrorEnabled()) {
            mLogger.error(String.format(format, o1, o2), t);
        }
    }

    public void error(String format, Object o1, Object o2, Object o3,
            Throwable t) {
        if (isErrorEnabled()) {
            mLogger.error(String.format(format, o1, o2, o3), t);
        }
    }

    public void fatal(Object o) {
        mLogger.fatal(o);
    }

    public void fatal(Object o, Throwable t) {
        mLogger.fatal(o, t);
    }

    public void fatal(String format, Object... objects) {
        if (isFatalEnabled()) {
            mLogger.fatal(String.format(format, objects));
        }
    }

    public void fatal(String format, Object o, Throwable t) {
        if (isFatalEnabled()) {
            mLogger.fatal(String.format(format, o), t);
        }
    }

    public void fatal(String format, Object o1, Object o2, Throwable t) {
        if (isFatalEnabled()) {
            mLogger.fatal(String.format(format, o1, o2), t);
        }
    }

    public void fatal(String format, Object o1, Object o2, Object o3,
            Throwable t) {
        if (isFatalEnabled()) {
            mLogger.fatal(String.format(format, o1, o2, o3), t);
        }
    }
}