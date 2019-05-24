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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.interiot.gateway.commons.api.configuration.ConfigurationService;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.device.Measurement;
import eu.interiot.gateway.commons.virtual.api.remote.PhysicalRemoteGatewayService;
import eu.interiot.gateway.mwcontroller.api.MWMessage;
import eu.interiot.gateway.mwcontroller.api.MWModule;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

public class OrionMWModule implements MWModule{
	
	private Logger log = LogManager.getLogger(OrionMWModule.class);
	
	private HttpRequest baseRequest;
	private String subscriptionUrl;

	public OrionMWModule(ConfigurationService configurationService, PhysicalRemoteGatewayService physicalGateway) {
		String fiwareService = configurationService.get("fiware-service", "interiot");
		// String fiwareServicePath = "/" + physicalGateway.getRemoteInfo().getUUID().replaceAll("-", "");
		// String fiwareServicePath = configurationService.get("fiware-servicepath", "/gateway");
		String protocol = configurationService.get("protocol", "http");
		String host = configurationService.get("host", "localhost");
		int port = configurationService.getInt("port", 1026);
		this.subscriptionUrl = configurationService.get("notify.protocol", "http")
			+ "://" + configurationService.get("notify.ip", "localhost") + ":"
			+ configurationService.getInt("notify.port", 8080) + "/api/extensions/orion/notify";
			
		this.baseRequest = new HttpRequest()
				.protocol(protocol).host(host).port(port)
				.header("Content-Type", "application/json")
				.header("Fiware-Service", fiwareService);
	}
	
	@Override
	public void registerDevice(DeviceDefinition device) throws Exception {
		List<OrionEntityAttribute> attributeList = device.getDeviceIOs().stream().map(dio -> new OrionEntityAttribute(dio.getAttribute())).collect(Collectors.toList());
		MWMessage message = OrionMessage.CREATE_ENTITY.getMessageInstance();
		//String deviceUuid = device.getUuid();
		//String deviceId = device.getName() + "-" + deviceUuid.substring(deviceUuid.length() - 4, deviceUuid.length());
		message.set("entityId", OrionUtil.getEntityId(device.getId()));
		message.set("entityType", OrionUtil.getEntityType(device));
		message.set("attributes", attributeList);
		// HttpResponse response = baseRequest.method("POST").path("/v2/entities").body(message.process()).send();
		HttpResponse response = baseRequest.header("Fiware-ServicePath", OrionUtil.getFiwareServicePath()).method("POST").path("/v2/entities").body(message.process()).send();
		log.info(String.format("REQUEST CREATE ENTITY %s : %d", OrionUtil.getEntityId(device.getId()), response.statusCode()));
		this.subscribe(device);
	}

	@Override
	public void unregisterDevice(DeviceDefinition device) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void subscribe(DeviceDefinition device) throws Exception {
		MWMessage message = OrionMessage.SUBSCRIBE_ENTITY.getMessageInstance();
		message.set("entityId", OrionUtil.getEntityId(device.getId()));
		message.set("entityType", OrionUtil.getEntityType(device)); 
		message.set("subscriptionUrl", this.subscriptionUrl);
		//System.out.println(message.process());
		HttpResponse response = baseRequest.header("Fiware-ServicePath", OrionUtil.getFiwareServicePath()).method("POST").path("/v2/subscriptions").body(message.process()).send();
		log.info(String.format("REQUEST SUBSCRIBE ENTITY %s : %d", OrionUtil.getEntityId(device.getId()), response.statusCode()));
	}

	@Override
	public void pushMeasurement(String deviceId, Measurement measurement) throws Exception {
		List<OrionEntityAttribute> attributeList = measurement.getData().stream().map(data -> new OrionEntityAttribute(data.getAttribute(), data.getValue().toString())).collect(Collectors.toList());
		MWMessage message = OrionMessage.UPDATE_ENTITY.getMessageInstance();
		message.set("attributes", attributeList);
		HttpResponse response = baseRequest.header("Fiware-ServicePath", OrionUtil.getFiwareServicePath()).method("PATCH").path("/v2/entities/" + OrionUtil.getEntityId(deviceId) + "/attrs").body(message.process()).send();
		log.info("REQUEST UPDATE ENTITY: " + response.statusCode() + "\n" + response.bodyText());
	}
	
}
