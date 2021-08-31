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
package com.krawler.hql.accounting;

/**
 *
 * @author krawler-user
 */
public interface AccountingMsgs {

    String invoiceHtmlMsg = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
            + "a:link, a:visited, a:active {\n"
            + " 	color: #03C;"
            + "}\n"
            + "body {\n"
            + "	font-family: Arial, Helvetica, sans-serif;"
            + "	color: #000;"
            + "	font-size: 13px;"
            + "}\n"
            + "</style><body>"
            + "<p>Hi %s,</p>"
            + "<p></p>"
            + "<p>Thanks for using our services.</p>"
            + "<p>Please find the attached Invoice document (Invoice Number: %s).</p>"
            + "<p></p>"
            + "<p>For any clarification, feel free to email us at <a href='mailto:%s'>%s</a>.</p>"
            + "<p></p>"
            + "<p>Assuring you of our best services at all times.</p>"
            + "<p></p>"
            + "<p>Thanks</p>"
            + "<p>Invoice Team<br>"
            + "Krawler Information Services</p>";
    ;
    String invoicePlainMsg = "Hi %s,\n\n"
            + "Thanks for using our services.\n"
            + "Please find the attached Invoice document (Invoice Number: %s).\n"
            + "For any clarification, feel free to email us at <a href='mailto:%s'>%s</a>.\n\n"
            + "Assuring you of our best services at all times.\n\n"
            + "Thanks\n\n"
            + "Invoice Team\n"
            + "Krawler Information Services";
    String invoiceSubject = "Invoice Information";
}
