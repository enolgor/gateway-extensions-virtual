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
package eu.interiot.gateway.mwmodule.orion.api.impl;

import java.util.Collection;

import org.osgi.framework.BundleContext;

import eu.interiot.gateway.commons.api.connector.RemoteConnector;
import eu.interiot.gateway.commons.api.device.Action;
import eu.interiot.gateway.commons.api.messages.ActionMessage;

public class Notifier {
	
	//private static HashMap<String, OrionEntityAttribute> state = new HashMap<>();
	private static RemoteConnector connector;
	
	
	public static void configure(BundleContext context) {
		Notifier.connector =  context.getService(context.getServiceReference(RemoteConnector.class));
	}
	
	/*public static synchronized void udpate(String deviceId, Collection<OrionEntityAttribute> attributes) {
		//for(OrionEntityAttribute attr : attributes) update(entityId, attr);
	}*/
	
	/*public static synchronized void update(String entityId, OrionEntityAttribute attribute) {
		//Notifier.state.put(entityId + attribute.getName(), attribute);
	}*/
	
	public static synchronized void notify(String entityId, Collection<OrionEntityAttribute> attributes) {
		System.out.println("Notifying: " + entityId);
		for(OrionEntityAttribute attr : attributes) System.out.println(String.format("%s: %s (%s)", attr.getName(), attr.getValue(), attr.getType()));
		String deviceId = OrionUtil.getDeviceId(entityId);
		if(deviceId != null) {
			Action action = new Action();
			for(OrionEntityAttribute attr : attributes) action.addData(attr.toActionData());
			ActionMessage actionMessage = new ActionMessage(deviceId, action);
			try {
				connector.send(actionMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
