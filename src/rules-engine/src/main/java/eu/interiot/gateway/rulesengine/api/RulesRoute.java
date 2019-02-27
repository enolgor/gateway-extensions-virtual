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
package eu.interiot.gateway.rulesengine.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.interiot.gateway.commons.virtual.api.ApiResponse;
import eu.interiot.gateway.rulesengine.RulesEngineSingleton;
import eu.interiot.gateway.rulesengine.api.model.Entity;
import eu.interiot.gateway.rulesengine.api.model.Execution;
import eu.interiot.gateway.rulesengine.api.model.Rule;
import eu.interiot.gateway.rulesengine.api.model.Statement;
import eu.interiot.gateway.rulesengine.api.model.description.ExecutionDescription;
import eu.interiot.gateway.rulesengine.api.model.description.RuleDescription;
import eu.interiot.gateway.rulesengine.api.model.description.StatementDescription;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api("extensions")
@Path("/extensions/rules")
public class RulesRoute{

	@GET
    @Path("/statements/list")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Statement List", response = Entity[].class)
    public Response listStatements() {
		RuleService ruleService = RulesEngineSingleton.getRuleService();
		return ApiResponse.jsonResponse(200, ruleService.getRuleManager().listStatements()).build();
    }
	
	@POST
    @Path("/statements/add")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add Statement", response = String.class)
    public Response addStatement(StatementDescription statementDescription) {
		try {
			RuleService ruleService = RulesEngineSingleton.getRuleService();
			Statement statement = ruleService.compileStatement(statementDescription.getStatement());
			Entity<Statement> statementEntity = statementDescription.getEntity(statement);
			return ApiResponse.jsonResponse(200, ruleService.getRuleManager().addStatement(statementEntity)).build();
		}catch(Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	
	@GET
    @Path("/statements/remove/{statementId}")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Remove Statement", response = String.class)
    public Response removeStatement(@PathParam("statementId") String statementId) {
		RuleService ruleService = RulesEngineSingleton.getRuleService();
		ruleService.getRuleManager().removeStatement(statementId); 
		return ApiResponse.jsonResponse(200, "").build();
    }
	
	@GET
    @Path("/executions/list")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Executions List", response = Entity[].class)
    public Response listExecutions() {
		RuleService ruleService = RulesEngineSingleton.getRuleService();
		return ApiResponse.jsonResponse(200, ruleService.getRuleManager().listExecutions()).build();
    }
	
	@POST
    @Path("/executions/add")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add Execution", response = String.class)
    public Response addExecution(ExecutionDescription executionDescription) {
		try {
			RuleService ruleService = RulesEngineSingleton.getRuleService();
			Execution execution = ruleService.compileExecution(executionDescription.getExecution());
			Entity<Execution> executionEntity = executionDescription.getEntity(execution);
			return ApiResponse.jsonResponse(200, ruleService.getRuleManager().addExecution(executionEntity)).build();
		}catch(Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	
	@GET
    @Path("/executions/remove/{executionId}")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Remove Execution", response = String.class)
    public Response removeExecution(@PathParam("executionId") String executionId) {
		RuleService RuleService = RulesEngineSingleton.getRuleService();
		RuleService.getRuleManager().removeExecution(executionId); 
		return ApiResponse.jsonResponse(200, "").build();
    }
	
	@GET
    @Path("/rules/list")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Rule List", response = Entity[].class)
    public Response listRules() {
		RuleService RuleService = RulesEngineSingleton.getRuleService();
		return ApiResponse.jsonResponse(200, RuleService.getRuleManager().listRules()).build();
    }
	
	@POST
    @Path("/rules/add")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add Rule", response = String.class)
    public Response addRule(RuleDescription ruleDescription) {
		try {
			RuleService ruleService = RulesEngineSingleton.getRuleService();
			RuleManager ruleManager = ruleService.getRuleManager();
			Entity<? extends Statement> statementEntity = ruleManager.getStatement(ruleDescription.getStatementId());
			if(statementEntity == null) throw new Exception("Statement not found");
			Entity<? extends Execution> executionEntity = ruleManager.getExecution(ruleDescription.getExecutionId());
			if(executionEntity == null) throw new Exception("Execution not found");
			Rule rule = ruleService.createRule(statementEntity.get(), executionEntity.get());
			Entity<Rule> ruleEntity = ruleDescription.getEntity(rule);
			return ApiResponse.jsonResponse(200, ruleService.getRuleManager().addRule(ruleEntity)).build();
		}catch(Exception e) {
			return ApiResponse.error(500, e).build();
		}
    }
	
	@GET
    @Path("/rules/remove/{ruleId}")
    @Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Remove Rule", response = String.class)
    public Response removeRule(@PathParam("ruleId") String ruleId) {
		RuleService RuleService = RulesEngineSingleton.getRuleService();
		RuleService.getRuleManager().removeRule(ruleId); 
		return ApiResponse.jsonResponse(200, "").build();
    }

}

