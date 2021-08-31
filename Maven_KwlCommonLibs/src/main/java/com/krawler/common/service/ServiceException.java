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
package com.krawler.common.service;

@SuppressWarnings("serial")
public class ServiceException extends Exception {

    public static final String FAILURE = "service.FAILURE";
    public static final String INVALID_REQUEST = "service.INVALID_REQUEST";
    public static final String UNKNOWN_DOCUMENT = "service.UNKNOWN_DOCUMENT";
    public static final String PARSE_ERROR = "service.PARSE_ERROR";
    public static final String RESOURCE_UNREACHABLE = "service.RESOURCE_UNREACHABLE";
    public static final String TEMPORARILY_UNAVAILABLE = "service.TEMPORARILY_UNAVAILABLE";
    public static final String PERM_DENIED = "service.PERM_DENIED";
    public static final String AUTH_REQUIRED = "service.AUTH_REQUIRED";
    public static final String AUTH_EXPIRED = "service.AUTH_EXPIRED";
    public static final String WRONG_HOST = "service.WRONG_HOST";
    public static final String NON_READONLY_OPERATION_DENIED = "service.NON_READONLY_OPERATION_DENIED";
    public static final String PROXY_ERROR = "service.PROXY_ERROR";
    public static final String TOO_MANY_HOPS = "service.TOO_MANY_HOPS";
    public static final String ALREADY_IN_PROGRESS = "service.ALREADY_IN_PROGRESS";
    public static final String NOT_IN_PROGRESS = "service.NOT_IN_PROGRESS";
    public static final String INTERRUPTED = "service.INTERRUPTED";
    public static final String NO_SPELL_CHECK_URL = "service.NO_SPELL_CHECK_URL";
    protected String mCode;
    protected Argument[] mArgs = null;
    public static final String HOST = "host";
    public static final String URL = "url";
    public static final String MAILBOX_ID = "mboxId";
    public static final String ACCOUNT_ID = "acctId";
    public static final String PROXIED_FROM_ACCT = "proxiedFromAcct"; // exception

    // proxied
    // from
    // remote
    // account
    public static class Argument {

        public static enum Type {

            IID, // mail-item ID or mailbox-id
            ACCTID, // account ID
            STR, // opaque string
            NUM
            // opaque number
        }

        public Argument(String name, String value, Type typ) {
            mName = name;
            mValue = value;
            mType = typ;
        }

        public Argument(String name, long value, Type type) {
            mName = name;
            mValue = Long.toString(value);
            mType = type;
        }
        public String mName;
        public String mValue;
        public Type mType;
    }

    /**
     * Sets the specified argument if it is not already set, updates it if it
     * is.
     *
     * @param name
     * @param value
     */
    public void setArgument(String name, String value, Argument.Type type) {
        if (mArgs == null) {
            mArgs = new Argument[1];
            mArgs[0] = new Argument(name, value, type);
        } else {
            for (Argument arg : mArgs) {
                if ((arg.mName.equals(name)) && (arg.mType == type)) {
                    arg.mValue = value;
                    return;
                }
            }

            // not found -- enlarge array
            Argument[] newArgs = new Argument[mArgs.length + 1];
            for (int i = mArgs.length - 1; i >= 0; i--) {
                newArgs[i] = mArgs[i];
            }

            // add new argument
            newArgs[mArgs.length] = new Argument(name, value, type);
            mArgs = newArgs;
        }
    }
    /**
     * Comment for
     * <code>mReceiver</code>
     *
     * Causes a Sender/Receiver element to show up in a soap fault. It is
     * supposed to let the client know whether it did something wrong or
     * something is wrong on the server.
     *
     * For example, ServiceException.FAILURE sets it to true, meaning something
     * bad happened on the server-side and the client could attempt a retry. The
     * rest are false, as it generally means the client made a bad request.
     *
     */
    public static final boolean RECEIVERS_FAULT = true; // server's fault
    public static final boolean SENDERS_FAULT = false; // client's fault
    private boolean mReceiver;

    protected ServiceException(String message, String code,
            boolean isReceiversFault, Throwable cause, Argument... arguments) {
        super(message, cause);
        mCode = code;
        mReceiver = isReceiversFault;

        mArgs = arguments;
    }

    protected ServiceException(String message, String code,
            boolean isReceiversFault, Argument... arguments) {
        super(message);
        mCode = code;
        mReceiver = isReceiversFault;

        mArgs = arguments;
    }

    public String getCode() {
        return mCode;
    }

    public Argument[] getArgs() {
        return mArgs;
    }

    /**
     * @return See the comment for the mReceiver member
     */
    public boolean isReceiversFault() {
        return mReceiver;
    }

    /**
     * generic system failure. most likely a temporary situation.
     */
    public static ServiceException FAILURE(String message, Throwable cause) {
        return new ServiceException("system failure: " + message, FAILURE,
                RECEIVERS_FAULT, cause);
    }

    /**
     * generic system failure. most likely a temporary situation.
     */
    public static ServiceException FAILURE(String message, String customErrorCode, boolean isReceiversFault) {
        return new ServiceException(message,
                customErrorCode, isReceiversFault);
    }
    
    /**
     * The request was somehow invalid (wrong parameter, wrong target, etc)
     */
    public static ServiceException INVALID_REQUEST(String message,
            Throwable cause) {
        return new ServiceException("invalid request: " + message,
                INVALID_REQUEST, SENDERS_FAULT, cause);
    }

    /**
     * User sent an unknown SOAP command (the "document" is the Soap Request)
     */
    public static ServiceException UNKNOWN_DOCUMENT(String message,
            Throwable cause) {
        return new ServiceException("unknown document: " + message,
                UNKNOWN_DOCUMENT, SENDERS_FAULT, cause);
    }

    public static ServiceException PARSE_ERROR(String message, Throwable cause) {
        return new ServiceException("parse error: " + message, PARSE_ERROR,
                SENDERS_FAULT, cause);
    }

    public static ServiceException RESOURCE_UNREACHABLE(String message,
            Throwable cause) {
        return new ServiceException("resource unreachable: " + message,
                RESOURCE_UNREACHABLE, RECEIVERS_FAULT, cause);
    }

    public static ServiceException TEMPORARILY_UNAVAILABLE() {
        return new ServiceException("service temporarily unavailable",
                TEMPORARILY_UNAVAILABLE, RECEIVERS_FAULT);
    }

    public static ServiceException PERM_DENIED(String message) {
        return new ServiceException("permission denied: " + message,
                PERM_DENIED, SENDERS_FAULT);
    }

    public static ServiceException AUTH_EXPIRED() {
        return new ServiceException("auth credentials have expired",
                AUTH_EXPIRED, SENDERS_FAULT);
    }

    public static ServiceException AUTH_REQUIRED() {
        return new ServiceException("no valid authtoken present",
                AUTH_REQUIRED, SENDERS_FAULT);
    }

    public static ServiceException WRONG_HOST(String target, Throwable cause) {
        return new ServiceException("operation sent to wrong host (you want '"
                + target + "')", WRONG_HOST, SENDERS_FAULT, cause,
                new Argument(HOST, target, Argument.Type.STR));
    }

    public static ServiceException NON_READONLY_OPERATION_DENIED() {
        return new ServiceException("non-readonly operation denied",
                NON_READONLY_OPERATION_DENIED, SENDERS_FAULT);
    }

    public static ServiceException PROXY_ERROR(Throwable cause, String url) {
        return new ServiceException(
                "error while proxying request to target server (url="
                + url
                + "): "
                + (cause != null ? cause.getMessage()
                : "unknown reason"), PROXY_ERROR,
                RECEIVERS_FAULT, cause, new Argument(URL, url,
                Argument.Type.STR));
    }

    public static ServiceException TOO_MANY_HOPS() {
        return new ServiceException("mountpoint loop detected", TOO_MANY_HOPS,
                SENDERS_FAULT);
    }

    public static ServiceException ALREADY_IN_PROGRESS(String message) {
        return new ServiceException(message, ALREADY_IN_PROGRESS, SENDERS_FAULT);
    }

    public static ServiceException ALREADY_IN_PROGRESS(String mboxId,
            String action) {
        return new ServiceException("mbox " + mboxId
                + " is already running action " + action, ALREADY_IN_PROGRESS,
                SENDERS_FAULT, new Argument(MAILBOX_ID, mboxId,
                Argument.Type.IID), new Argument("action", action,
                Argument.Type.STR));
    }

    public static ServiceException NOT_IN_PROGRESS(String mboxId, String action) {
        return new ServiceException("mbox " + mboxId
                + " is not currently running action " + action,
                NOT_IN_PROGRESS, SENDERS_FAULT, new Argument(MAILBOX_ID,
                mboxId, Argument.Type.IID), new Argument("action",
                action, Argument.Type.STR));
    }

    public static ServiceException INTERRUPTED(String str) {
        return new ServiceException(
                "The operation has been interrupted " + str != null ? str : "",
                INTERRUPTED, RECEIVERS_FAULT);
    }

    public static ServiceException NO_SPELL_CHECK_URL(String str) {
        return new ServiceException(
                "Spell Checking Not Available " + str != null ? str : "",
                NO_SPELL_CHECK_URL, RECEIVERS_FAULT);
    }
}
