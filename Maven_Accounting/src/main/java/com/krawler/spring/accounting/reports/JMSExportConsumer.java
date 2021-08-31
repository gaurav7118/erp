/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.springframework.jms.core.JmsTemplate;

public class JMSExportConsumer implements MessageListener {

    private JmsTemplate jmsTemplate;
    private Destination destination;
    private ExportGroupDetailReport exportGroupDetailReport;
    private CommonExportService commonExportService;

    public void setCommonExportService(CommonExportService commonExportService) {
        this.commonExportService = commonExportService;
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setExportGroupDetailReport(ExportGroupDetailReport exportGroupDetailReport) {
        this.exportGroupDetailReport = exportGroupDetailReport;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination exportQueue) {
        this.destination = exportQueue;
    }

    public void onMessage(Message message) {
        if (message instanceof Message) {
            JSONObject requestJSON = null;
            try {
                requestJSON = new JSONObject(message.getStringProperty("paramObj"));
                System.out.println("consumed");
                if (!StringUtil.isNullOrEmpty(message.getStringProperty("paramObj"))) {
                    String module = requestJSON.optString("module");
                    switch (module) {
                        case "GeneralLedger":
                            exportGroupDetailReport.exportGeneralLedger(requestJSON);
                            break;
                    }

                }
            } catch (IOException | JSONException | JMSException | ServiceException | SessionExpiredException  ex) {
                Logger.getLogger(JMSExportConsumer.class.getName()).log(Level.SEVERE, null, ex);
                Map params = new HashMap();
                params.put("exportid", requestJSON.optString("exportid"));
                try {
                    commonExportService.updateRequestStatus(5, params);
                } catch (Exception e) {
                    Logger.getLogger(JMSExportConsumer.class.getName()).log(Level.SEVERE, null, e);
                }

            }
        } else {
            throw new IllegalArgumentException("Message Error");
        }
    }

}
