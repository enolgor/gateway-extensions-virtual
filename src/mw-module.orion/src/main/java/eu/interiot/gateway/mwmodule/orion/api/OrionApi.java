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
package eu.interiot.gateway.mwmodule.orion.api;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.interiot.gateway.mwmodule.orion.api.impl.Notifier;
import eu.interiot.gateway.mwmodule.orion.api.impl.OrionEntityAttribute;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api("extensions")
@Path("/extensions/orion")
public class OrionApi{
	private static JsonParser parser = new JsonParser();
	@POST
    @Path("/notify")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Orion subscription endpoint", response = String.class)
    public Response test(String body) {
		JsonObject update = parser.parse(body).getAsJsonObject(); 
		if (update.has("data") && update.get("data").isJsonArray()) {
			JsonArray data = update.get("data").getAsJsonArray();
			for(JsonElement element : data) {
				if(element.isJsonObject()) {
					JsonObject entityUpdate = element.getAsJsonObject();
					if(entityUpdate.has("id") && entityUpdate.get("id").isJsonPrimitive()) {
						String entityId = entityUpdate.get("id").getAsString();
						Set<OrionEntityAttribute> attributes = new HashSet<>();
						for(Entry<String, JsonElement> entry : entityUpdate.entrySet()) {
							String attributeName = entry.getKey();
							if(attributeName.equals("id") || attributeName.equals("type")) continue;
							JsonElement entryValue = entry.getValue();
							if(entryValue.isJsonObject()) {
								JsonObject attribute = entryValue.getAsJsonObject();
								if(attribute.has("type") && attribute.has("value")) {
									JsonElement type = attribute.get("type");
									JsonElement value = attribute.get("value");
									if(type.isJsonPrimitive() && value.isJsonPrimitive()) {
										attributes.add(new OrionEntityAttribute(attributeName, type.getAsString(), value.getAsString()));
									}
								}
							}
						}
						Notifier.notify(entityId, attributes);
					}
				}
			}
		}
        return Response.ok().build();
    }
	
}
