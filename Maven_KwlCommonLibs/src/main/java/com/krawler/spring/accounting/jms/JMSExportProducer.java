/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jms;

import com.krawler.common.util.Constants;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
//import org.apache.activemq.ScheduledMessage;

public class JMSExportProducer {

    private JmsTemplate jmsTemplate;

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    
    public void sendMessage(final JSONObject paramObj){
        Destination ExportQueue = new ActiveMQQueue(Constants.ExportQueue);
        jmsTemplate.send(ExportQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message message = session.createMessage();
//                message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 20000);
                message.setStringProperty("paramObj", paramObj.toString());
                return message;
            }
        });
    }
}