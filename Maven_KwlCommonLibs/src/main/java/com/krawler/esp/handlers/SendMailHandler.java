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

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import java.io.UnsupportedEncodingException;
import javax.mail.*;
import javax.mail.internet.*;

import com.krawler.esp.utils.ConfigReader;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.mail.util.ByteArrayDataSource;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

public class SendMailHandler {
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    private static String getSMTPPath() {
        return ConfigReader.getinstance().get("SMTPPath");
    }

    private static String getSMTPPort() {
        return ConfigReader.getinstance().get("SMTPPort");
    }

    public static void postMail(String recipients[], String subject,
            String htmlMsg, String plainMsg, String from, Map<String,Object> SMTPConfig) throws MessagingException {
        boolean debug = false;

        Session session = getSMTPSession(SMTPConfig);
        session.setDebug(debug);

        // create a message
        MimeMessage msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        
        //get Reply to mapping (from -> reply_to_email_id)
        if (!StringUtil.isNullOrEmpty((String) SMTPConfig.get("replytoemail"))) {
            try {
                JSONObject replyToEmailsMap = new JSONObject((String) SMTPConfig.get("replytoemail"));
                // set the Reply to address (single email id OR comma separated email ids)
                /**
                 * If From mail id is "admin@deskera.com" then Reply-To mail should be "admin@deskera.com".
                 * Reply-To mail id should be same with From mail-id.
                 * SDP-10166
                 */
                if(from.equalsIgnoreCase(Constants.ADMIN_EMAILID)){
                    msg.setReplyTo(InternetAddress.parse(from));
                }else {
//                    msg.setReplyTo(InternetAddress.parse(replyToEmailsMap.getString(addressFrom.getAddress())));
                    msg.setReplyTo(InternetAddress.parse(from));
                }
            } catch (Exception e) {
                System.out.println("SendMailHandler : Error while setting replytoemail :- " + e.getMessage());
            }
        }
        
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            String address = recipients[i].trim().replace(" ", "+");
            addressTo[i] = new InternetAddress(address);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(subject);

        Multipart multipart = new MimeMultipart("alternative");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(plainMsg, "text/plain; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);

        messageBodyPart = new MimeBodyPart();
//        messageBodyPart.setContent(htmlMsg, "text/html");
        messageBodyPart.setContent(htmlMsg, "text/html; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);

        msg.setContent(multipart);
        Transport.send(msg);
    }
    // This is common method used for to send To,CC,BCC mail with attachments or without.
    public static void postMail(String ccMailId,String bccMailIds[],String recipients[], String subject,
            String htmlMsg, String plainMsg, String from,String attachments[], Map<String,Object> SMTPConfig) throws MessagingException {
        boolean debug = false;

        Session session = getSMTPSession(SMTPConfig);
        session.setDebug(debug);

        // create a message
        MimeMessage msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        
        //get Reply to mapping (from -> reply_to_email_id)
        if (!StringUtil.isNullOrEmpty((String) SMTPConfig.get("replytoemail"))) {
            try {
                JSONObject replyToEmailsMap = new JSONObject((String) SMTPConfig.get("replytoemail"));
                // set the Reply to address (single email id OR comma separated email ids)
                /**
                 * If From mail id is "admin@deskera.com" then Reply-To mail should be "admin@deskera.com".
                 * Reply-To mail id should be same with From mail-id.
                 * SDP-10166
                 */
                if(from.equalsIgnoreCase(Constants.ADMIN_EMAILID)){
                    msg.setReplyTo(InternetAddress.parse(from));
                }else {
//                    msg.setReplyTo(InternetAddress.parse(replyToEmailsMap.getString(addressFrom.getAddress())));
                    msg.setReplyTo(InternetAddress.parse(from));
                }
            } catch (Exception e) {
                System.out.println("SendMailHandler : Error while setting replytoemail :- " + e.getMessage());
            }
        }
        
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
//        InternetAddress[] addressCC = new InternetAddress[1];
        InternetAddress[] addressBcc = new InternetAddress[bccMailIds.length];
        if (recipients.length > 0) {
            for (int i = 0; i < recipients.length; i++) {
                String address = recipients[i].trim().replace(" ", "+");
                addressTo[i] = new InternetAddress(address);
            }
            msg.setRecipients(Message.RecipientType.TO, addressTo);
        }
        //Send CC Mail to User
        if (!StringUtil.isNullOrEmpty(ccMailId)) {
            String[] addressCCStr = ccMailId.split(",");
            InternetAddress[] addressCC = new InternetAddress[addressCCStr.length];
            for (int i = 0; i < addressCCStr.length; i++) {
                String address = addressCCStr[i].trim().replace(" ", "+");
                addressCC[i] = new InternetAddress(address);
            }
            msg.setRecipients(Message.RecipientType.CC, addressCC);
        }
        
        //Send Bcc  Mail to Vendors
        if (bccMailIds.length > 0) {

            for (int i = 0; i < bccMailIds.length; i++) {
                String address = bccMailIds[i].trim().replace(" ", "+");
                addressBcc[i] = new InternetAddress(address);
            }
            msg.setRecipients(Message.RecipientType.BCC, addressBcc);

        }
        // Setting the Subject and Content Type
        msg.setSubject(subject);

        Multipart multipart = new MimeMultipart("alternative");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(plainMsg, "text/plain; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);

        messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(htmlMsg, "text/html; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);
        if (attachments.length > 0) {
            for (int i = 0; i < attachments.length; i++) {
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachments[i]);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(source.getName());
                multipart.addBodyPart(messageBodyPart);
            }
        }
        msg.setContent(multipart);
        Transport.send(msg);
    }

    public static void postMail(String recipients[], String subject,
            String htmlMsg, String plainMsg, String fromAddress, String fromName, Map<String,Object> SMTPConfig) throws MessagingException, UnsupportedEncodingException {
        boolean debug = false;

        Session session = getSMTPSession(SMTPConfig);
        session.setDebug(debug);

        // create a message
        MimeMessage msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(fromAddress, fromName);
        msg.setFrom(addressFrom);
        
        //get Reply to mapping (from -> reply_to_email_id)
        if (!StringUtil.isNullOrEmpty((String) SMTPConfig.get("replytoemail"))) {
            try {
                JSONObject replyToEmailsMap = new JSONObject((String) SMTPConfig.get("replytoemail"));
                // set the Reply to address (single email id OR comma separated email ids)
                /**
                 * If From mail id is "admin@deskera.com" then Reply-To mail should be "admin@deskera.com".
                 * Reply-To mail id should be same with From mail-id.
                 * SDP-10166
                 */
                if(fromAddress.equalsIgnoreCase(Constants.ADMIN_EMAILID)){
                    msg.setReplyTo(InternetAddress.parse(fromAddress));
                }else {
//                    msg.setReplyTo(InternetAddress.parse(replyToEmailsMap.getString(addressFrom.getAddress())));
                    msg.setReplyTo(InternetAddress.parse(fromAddress));
                }
            } catch (Exception e) {
                System.out.println("SendMailHandler : Error while setting replytoemail :- " + e.getMessage());
            }
        }
        
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i].trim().replace(" ", "+"));
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(subject);

        Multipart multipart = new MimeMultipart("alternative");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(plainMsg, "text/plain; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);

        messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(htmlMsg, "text/html; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);

        msg.setContent(multipart);
        Transport.send(msg);
    }

    public static void postMail(String recipients[], String subject,
            String htmlMsg, String plainMsg, String from, String attachments[], Map<String,Object> SMTPConfig) throws MessagingException {
        boolean debug = false;

        Session session = getSMTPSession(SMTPConfig);
        session.setDebug(debug);

        // create a message
        MimeMessage msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        
        //get Reply to mapping (from -> reply_to_email_id)
        if (!StringUtil.isNullOrEmpty((String) SMTPConfig.get("replytoemail"))) {
            try {
                JSONObject replyToEmailsMap = new JSONObject((String) SMTPConfig.get("replytoemail"));
                // set the Reply to address (single email id OR comma separated email ids)
                /**
                 * If from mail id is "admin@deskera.com" then Reply-to mail id should be "admin@deskera.com".
                 * Reply-to mail id should be same with From mail-id.
                 * SDP-10166
                 */
                if(from.equalsIgnoreCase(Constants.ADMIN_EMAILID)){
                    msg.setReplyTo(InternetAddress.parse(from));
                }else {
//                    msg.setReplyTo(InternetAddress.parse(replyToEmailsMap.getString(addressFrom.getAddress())));
                    msg.setReplyTo(InternetAddress.parse(from));
                }
            } catch (Exception e) {
                System.out.println("SendMailHandler : Error while setting replytoemail :- " + e.getMessage());
            }
        }

        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            String address = recipients[i].trim().replace(" ", "+");
            addressTo[i] = new InternetAddress(address);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(subject);

        Multipart multipart = new MimeMultipart("alternative");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(plainMsg, "text/plain; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);
        
        messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(htmlMsg, "text/html; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);

        for (int i = 0; i < attachments.length; i++) {
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachments[i]);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(source.getName());
            multipart.addBodyPart(messageBodyPart);
        }

        msg.setContent(multipart);
        Transport.send(msg);
    }
    public static void postMail(String recipients[], String subject,
            String htmlMsg, String plainMsg, String from, String attachments[],String attachmentnames[], Map<String,Object> SMTPConfig) throws MessagingException {
        boolean debug = false;

        Session session = getSMTPSession(SMTPConfig);
        session.setDebug(debug);

        // create a message
        MimeMessage msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        
        //get Reply to mapping (from -> reply_to_email_id)
        if (!StringUtil.isNullOrEmpty((String) SMTPConfig.get("replytoemail"))) {
            try {
                JSONObject replyToEmailsMap = new JSONObject((String) SMTPConfig.get("replytoemail"));
                // set the Reply to address (single email id OR comma separated email ids)
                /**
                 * If From mail id is "admin@deskera.com" then Reply-To mail should be "admin@deskera.com".
                 * Reply-To mail id should be same with From mail-id.
                 * SDP-10166
                 */
                if(from.equalsIgnoreCase(Constants.ADMIN_EMAILID)){
                    msg.setReplyTo(InternetAddress.parse(from));
                }else {
//                    msg.setReplyTo(InternetAddress.parse(replyToEmailsMap.getString(addressFrom.getAddress())));
                    msg.setReplyTo(InternetAddress.parse(from));
                }
            } catch (Exception e) {
                System.out.println("SendMailHandler : Error while setting replytoemail :- " + e.getMessage());
            }
        }

        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            String address = recipients[i].trim().replace(" ", "+");
            addressTo[i] = new InternetAddress(address);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(subject);

        Multipart multipart = new MimeMultipart("alternative");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(plainMsg, "text/plain; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);
        
        messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(htmlMsg, "text/html; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);

        for (int i = 0; i < attachments.length; i++) {
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachments[i]);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachmentnames[i]);
            multipart.addBodyPart(messageBodyPart);
        }

        msg.setContent(multipart);
        Transport.send(msg);
    }
    
    public static void attachPDFToMail(String fileName, String recipients[], String subject,
            String htmlMsg, String plainMsg, String from, ByteArrayInputStream arrayInputStream,int mode, Map<String,Object> SMTPConfig, String ccMailId) throws MessagingException, IOException {
        boolean debug = false;
        
        Session session = getSMTPSession(SMTPConfig);
        session.setDebug(debug);

        // create a message
        MimeMessage msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        
        //get Reply to mapping (from -> reply_to_email_id)
        if (!StringUtil.isNullOrEmpty((String) SMTPConfig.get("replytoemail"))) {
            try {
                JSONObject replyToEmailsMap = new JSONObject((String) SMTPConfig.get("replytoemail"));
                // set the Reply to address (single email id OR comma separated email ids)
                /**
                 * If From mail id is "admin@deskera.com" then Reply-To mail should be "admin@deskera.com".
                 * Reply-To mail id should be same with From mail-id.
                 * SDP-10166
                 */
                if(from.equalsIgnoreCase(Constants.ADMIN_EMAILID)){
                    msg.setReplyTo(InternetAddress.parse(from));
                }else {
//                    msg.setReplyTo(InternetAddress.parse(replyToEmailsMap.getString(addressFrom.getAddress())));
                    msg.setReplyTo(InternetAddress.parse(from));
                }
            } catch (Exception e) {
                System.out.println("SendMailHandler : Error while setting replytoemail :- " + e.getMessage());
            }
        }
        
        //Send CC Mail to User
        if (!StringUtil.isNullOrEmpty(ccMailId)) {
            String[] addressCCStr = ccMailId.split(",");
            InternetAddress[] addressCC = new InternetAddress[addressCCStr.length];
            for (int i = 0; i < addressCCStr.length; i++) {
                String address = addressCCStr[i].trim().replace(" ", "+");
                addressCC[i] = new InternetAddress(address);
            }
            msg.setRecipients(Message.RecipientType.CC, addressCC);
        }
        
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        InternetAddress[] addressBcc = new InternetAddress[recipients.length];
        // Send RFQ mail as Bcc 
        if (mode == 59) {
            if (recipients.length > 0) {
                for (int i = 0; i < recipients.length; i++) {
                    String address = recipients[i].trim().replace(" ", "+");
                    addressBcc[i] = new InternetAddress(address);
                }
                msg.setRecipients(Message.RecipientType.BCC, addressBcc);
            }
        } else {
            for (int i = 0; i < recipients.length; i++) {
                String address = recipients[i].trim().replace(" ", "+");
                addressTo[i] = new InternetAddress(address);
            }
            msg.setRecipients(Message.RecipientType.TO, addressTo);
        }
        // Setting the Subject and Content Type
        msg.setSubject(subject);

        Multipart multipart = new MimeMultipart("alternative");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(plainMsg, "text/plain; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);

        messageBodyPart = new MimeBodyPart();
//        messageBodyPart.setContent(htmlMsg, "text/html");
        messageBodyPart.setContent(htmlMsg, "text/html; charset=UTF-8");
        messageBodyPart.setHeader("Content-Transfer-Encoding", "base64");
        multipart.addBodyPart(messageBodyPart);

        if (arrayInputStream != null && arrayInputStream instanceof ByteArrayInputStream) {
            messageBodyPart = new MimeBodyPart();
            ByteArrayDataSource bads = new ByteArrayDataSource(arrayInputStream, "application/pdf");
            messageBodyPart.setDataHandler(new DataHandler(bads));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);
        }
        msg.setContent(multipart);
        Transport.send(msg);
    }

    public static void postCampaignMail(String recipients[], String subject,
            String htmlMsg, String plainMsg, String fromAddress, String replyAddress[], String fromName, Map<String,Object> SMTPConfig) throws MessagingException, UnsupportedEncodingException {
        boolean debug = false;
        
        Session session = getSMTPSession(SMTPConfig);
        session.setDebug(debug);
        MimeMessage msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(fromAddress, fromName);
        msg.setFrom(addressFrom);
        
        //get Reply to mapping (from -> reply_to_email_id)
        if (!StringUtil.isNullOrEmpty((String) SMTPConfig.get("replytoemail"))) {
            try {
                JSONObject replyToEmailsMap = new JSONObject((String) SMTPConfig.get("replytoemail"));
                // set the Reply to address (single email id OR comma separated email ids)
                if (!StringUtil.isNullOrEmpty(replyToEmailsMap.optString(addressFrom.getAddress()))) {
                    msg.setReplyTo(InternetAddress.parse(replyToEmailsMap.getString(addressFrom.getAddress())));
                }
            } catch (Exception e) {
                System.out.println("SendMailHandler : Error while setting replytoemail :- " + e.getMessage());
            }
        }
        
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i].trim().replace(" ", "+"));
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        msg.setSubject(subject);
        InternetAddress[] replyTo = new InternetAddress[replyAddress.length];
        for (int i = 0; i < recipients.length; i++) {
            replyTo[i] = new InternetAddress(replyAddress[i].trim().replace(" ", "+"));
        }
        msg.setReplyTo(replyTo);
        Multipart multipart = new MimeMultipart("alternative");
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(plainMsg, "text/plain");
        multipart.addBodyPart(messageBodyPart);
        messageBodyPart = new PreencodedMimeBodyPart("base64");
        messageBodyPart.setHeader("charset", "utf-8");
        messageBodyPart.setContent(htmlMsg, "text/html");
        multipart.addBodyPart(messageBodyPart);
        msg.setContent(multipart);
        Transport.send(msg);
    }
    
    public static Session getSMTPSession(Map<String, Object> smtpConfig) {
        Session session = null;

        // Set the host smtp address
        Properties props = new Properties();
        if (smtpConfig.containsKey("SMTPFlow") && ((Integer)smtpConfig.get("SMTPFlow") == Constants.SMTP_FlOW_ON) && 
                smtpConfig.containsKey("SMTPPath") && !StringUtil.isNullOrEmpty((String)smtpConfig.get("SMTPPath")) && smtpConfig.containsKey("SMTPPort") && !StringUtil.isNullOrEmpty((String)smtpConfig.get("SMTPPort"))) {
            props.put("mail.smtp.host", ((String) smtpConfig.get("SMTPPath")));
            props.put("mail.smtp.port", ((String) smtpConfig.get("SMTPPort")));
        } else {
            props.put("mail.smtp.host", getSMTPPath());
            props.put("mail.smtp.port", getSMTPPort());
        }

        if (smtpConfig.containsKey("SMTPFlow") && ((Integer)smtpConfig.get("SMTPFlow") == Constants.SMTP_FlOW_ON) && 
                smtpConfig.containsKey("SMTPUsername") && !StringUtil.isNullOrEmpty("SMTPUsername") && smtpConfig.containsKey("SMTPPassword") && !StringUtil.isNullOrEmpty("SMTPPassword")) {
            props.put("mail.smtp.user", ((String) smtpConfig.get("SMTPUsername")));
            props.put("mail.smtp.password", ((String) smtpConfig.get("SMTPPassword")));
            props.put("mail.smtp.auth", "true");

            props.put("mail.smtp.socketFactory.fallback", "true");
            props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.put("mail.smtp.socketFactory.port", ((String) smtpConfig.get("SMTPPort")));

            final String username = (String) smtpConfig.get("SMTPUsername");
            final String password = (String) smtpConfig.get("SMTPPassword");
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.starttls.enable", "true");
            session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
        } else {
            props.put("mail.smtp.host", getSMTPPath());
            props.put("mail.smtp.port", getSMTPPort());
            session = Session.getDefaultInstance(props, null);
        }

        return session;
    }
}
