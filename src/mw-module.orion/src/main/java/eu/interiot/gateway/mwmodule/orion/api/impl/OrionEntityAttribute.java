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

import eu.interiot.gateway.commons.api.device.Action.ActionData;
import eu.interiot.gateway.commons.api.device.Attribute;
import eu.interiot.gateway.commons.api.device.Attribute.Type;

public class OrionEntityAttribute {
	private String name;
	private String value;
	private String type;
	
	public OrionEntityAttribute() {
		
	}
	
	public OrionEntityAttribute(ActionData actionData){
		Attribute attribute = actionData.getAttribute();
		this.name = attribute.getName();
		this.type = attribute.getType().toString().toLowerCase();
		this.value = actionData.getValue();
	}
	
	public OrionEntityAttribute(Attribute attribute){
		this.name = attribute.getName();
		this.type = attribute.getType().toString().toLowerCase();
		switch(attribute.getType()){
		case BOOLEAN: this.value = "true"; break;
		case INTEGER: this.value = "0"; break;
		case FLOAT: this.value = "0.0"; break;
		case STRING: this.value = "\"\""; break; 
		}
	}
	
	public OrionEntityAttribute(Attribute attribute, String value){
		this.name = attribute.getName();
		this.type = attribute.getType().toString().toLowerCase();
		this.value = value;
	}
	
	public OrionEntityAttribute(String name, String type, String value){
		this.name = name;
		this.type = type;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public ActionData toActionData() {
		return new ActionData(this.name, Type.valueOf(this.type.toUpperCase()), this.value);
	}
	
}
