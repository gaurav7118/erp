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

import javax.servlet.ServletContext;

import dojox.cometd.Bayeux;
import dojox.cometd.Channel;
import dojox.cometd.Client;

public class ServerEventManager {

    private static Bayeux mBayeux;
    private static String clientId;
    private static Integer msgID = new Integer(0);

    private static void initBayeux(ServletContext context) {
        mBayeux = (Bayeux) (context.getAttribute(Bayeux.DOJOX_COMETD_BAYEUX));
    }

    private static Client getClient() {
        Client client = null;
        if (clientId != null) {
            client = mBayeux.getClient(clientId);
        }
        if (client == null) {
            client = mBayeux.newClient(null, null);
            clientId = client.getId();
        }
        return client;
    }

    public static void publish(String channelName, Object data,
            ServletContext context) {
        if (mBayeux == null) {
            initBayeux(context);
        }
        if (mBayeux != null) {
            Channel channel = mBayeux.getChannel(channelName, true);
            channel.publish(getClient(), data, (msgID++).toString());
        }
    }
}