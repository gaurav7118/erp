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
package com.krawler.accounting.utils;

/**
 *
 * @author krawler
 */
public interface KWLErrorMsgs {

    String msgTempPassword = "Hi %s,\n\nHere is your temporary password: %s\n\nPlease change your password when you login next time. Please keep your password safe to prevent unauthorized access.\n\n\nYou can login at:\n%s\n\n\nSee you on Deskera Accounting!\n\n - The Deskera Accounting Team\n";
    String msgMailPassword = "<html><head><title>Deskera Accounting - Your Account Password</title><style type='text/css'>"
            + "a:link, a:visited, a:active {\n"
            + " 	color: #03C;"
            + "}\n"
            + "body {\n"
            + "	font-family: Arial, Helvetica, sans-serif;"
            + "	color: #000;"
            + "	font-size: 13px;"
            + "}\n"
            + "</style><body>"
            + "	<div>"
            + "		<p>Hi <strong>%s</strong>,</p>"
            + "		<p>Here is your temporary password: <strong>%s</strong></p><p>Please change your password when you login next time. Please keep your password safe to prevent unauthorized access.</p><p>You can login at: <a href='%s'>%s</a></p><br/><p>See you on Deskera Accounting!</p><p> - The Deskera Accounting Team</p>"
            + "	</div></body></html>";
    String msgWelcomeDeskera = "[Deskera] Welcome to Deskera Accounting";
    String msgMailInvite = "Hi %s,\n\n%s has created an account for you at Deskera Accounting.\n\nDeskera Accounting is an Account Management Tool which you'll love using.\n\nUsername: %s \nPassword: %s\n\nYou can log in at:\n%s\n\n\nSee you on Deskera Accounting\n\n - %s and The Deskera Acconting Team";
    String msgWelcomeSigningAddress = "Hi %s,\n\nWelcome to Deskera Accounting and thanks for signing up!\n\n\nBookmark your login page - %s\n\nThis is the address where you'll log in to your account for now on\n\n - The Deskera Accounting Team\n";
    String msgNewDeskeraAccount = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
            + "a:link, a:visited, a:active {\n"
            + " 	color: #03C;"
            + "}\n"
            + "body {\n"
            + "	font-family: Arial, Helvetica, sans-serif;"
            + "	color: #000;"
            + "	font-size: 13px;"
            + "}\n"
            + "</style><body>"
            + "	<div>"
            + "		<p>Hi <strong>%s</strong>,</p>"
            + "		<p>Welcome to Deskera Accounting and thanks for signing up!</p>"
            + "		<p>Access your Deskera account at: <a href='%s'>%s</a>"
            + "		<p>See you on Deskera Accounting!</p><p> - The Deskera Accounting Team</p>"
            + "	</div></body></html>";
    String msgMailInviteUsernamePassword = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
            + "a:link, a:visited, a:active {\n"
            + " 	color: #03C;"
            + "}\n"
            + "body {\n"
            + "	font-family: Arial, Helvetica, sans-serif;"
            + "	color: #000;"
            + "	font-size: 13px;"
            + "}\n"
            + "</style><body>"
            + "	<div>"
            + "		<p>Hi <strong>%s</strong>,</p>"
            + "		<p>%s has created an account for you at %s.</p>"
            + "             <p>Deskera Accounting is an Account Management Tool which you'll love using.</p>"
            + "		<p>Username: <strong>%s</strong> </p>"
            + "               <p>Password: <strong>%s</strong></p>"
            + "		<p>You can log in to Deskera Acconting at: <a href=%s>%s</a>.</p>"
            + "		<br/><p>See you on Deskera Accounting!</p><p> - %s and The Deskera Accounting Team</p>"
            + "	</div></body></html>";
    String msgMailChangePassword = "<html><head><title>Deskera Accounting - Change Password</title><style type='text/css'>"
            + "a:link, a:visited, a:active {\n"
            + " 	color: #03C;" + "}\n"
            + "body {\n"
            + "	font-family: Arial, Helvetica, sans-serif;"
            + "	color: #000;"
            + "	font-size: 13px;"
            + "}\n"
            + "</style><body>"
            + "	<div>"
            + "		<p>Hi <strong>%s</strong>,</p>"
            + "		<p>%s has changed your password to: %s<br/></p>"
            + "		<p>Please change it when you log in next time.</p>You can log in at:<br><a href='%s'>%s</a>"
            + "	</div></body></html>";
}
