/*
 * Copyright 2016-2018 Universitat Politècnica de València
 * Copyright 2016-2018 Università della Calabria
 * Copyright 2016-2018 Prodevelop, SL
 * Copyright 2016-2018 Technische Universiteit Eindhoven
 * Copyright 2016-2018 Fundación de la Comunidad Valenciana para la
 * Investigación, Promoción y Estudios Comerciales de Valenciaport
 * Copyright 2016-2018 Rinicom Ltd
 * Copyright 2016-2018 Association pour le développement de la formation
 * professionnelle dans le transport
 * Copyright 2016-2018 Noatum Ports Valenciana, S.A.U.
 * Copyright 2016-2018 XLAB razvoj programske opreme in svetovanje d.o.o.
 * Copyright 2016-2018 Systems Research Institute Polish Academy of Sciences
 * Copyright 2016-2018 Azienda Sanitaria Locale TO5
 * Copyright 2016-2018 Alessandro Bassi Consulting SARL
 * Copyright 2016-2018 Neways Technologies B.V.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.interiot.gateway.apiengine.api.impl;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class WebSocket extends WebSocketAdapter {
	private static String keepalivestring = "{\"type\": \"keepalive\"}";
	private static int keepaliveinterval = 3*DefaultWebSocketServlet.idletimeout/4;
	private static Session session;
	private Timer keepalivetimer;
	
    @Override
    public void onWebSocketText(String message) {
    	System.out.println("RECEIVED: "+message);
    	try {
			sendData(message.toUpperCase());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
 
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
    	session = null;
    	keepalivetimer.cancel();
    }
 
    @Override
    public void onWebSocketConnect(final Session sess) {
        super.onWebSocketConnect(sess);
        try {
        	session = sess;
        	keepalivetimer = new Timer();
        	keepalivetimer.scheduleAtFixedRate(new TimerTask(){
				@Override
				public void run() {
					try {
						sess.getRemote().sendString(keepalivestring);
					} catch (IOException e) {}
				}
        	}, keepaliveinterval, keepaliveinterval);
        } catch (Exception e){
        	e.printStackTrace();
        }
    }
    
    public static void sendData(String data) throws IOException{
    	/*if(BUFFER_SIZE==null) BUFFER_SIZE = BootStrap.properties.intValue("buffer.size");
    	if(buffer.size()==BUFFER_SIZE) buffer.remove();
    	buffer.offer(data);
    	for(int i=0; i<sessions.length; i++) if(sessions[i]!=null){
    		sessions[i].getRemote().sendString(mapper.writeValueAsString(data));
    	}*/
    	session.getRemote().sendString(data);
    }
}
