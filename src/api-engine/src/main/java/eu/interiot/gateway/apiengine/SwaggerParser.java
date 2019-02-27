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
package eu.interiot.gateway.apiengine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.interiot.gateway.commons.api.gateway.GatewayInfo;
import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;

public class SwaggerParser {
	
	private static Logger log = LogManager.getLogger(SwaggerParser.class);

	private static String swaggerJson = "";
	
	public static String getJson() {
		return swaggerJson;
	}
	
	public static void update(){
		Swagger swagger = getSwagger();
		Reader reader = new Reader(swagger);
		swagger = reader.read(ServerApplication.getClassesStatic());
		swagger.getInfo().setVersion(getBuildVersion());
		try {
			String swaggerJson = swaggerToJson(swagger);
			SwaggerParser.swaggerJson = swaggerJson;
		} catch (JsonProcessingException e) {
			log.error(e);
		}
	}
	
	private static Swagger getSwagger() {
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setBasePath("/api");
		beanConfig.setResourcePackage("");
		beanConfig.setScan(true);
		beanConfig.scanAndRead();
		return beanConfig.getSwagger();
	}

	private static String swaggerToJson(Swagger swagger) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		String json = objectMapper.writeValueAsString(swagger);
		return json;
	}
	
	private static String getBuildVersion() {
		GatewayInfo info = GatewayInfo.localInstance();
		String version = info.getVersion();
		if(!version.contains("SNAPSHOT")) return version;
		else return version + "-" + info.getBuild();
	}
}
