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

import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.interiot.gateway.commons.api.command.CommandLine.Command;
import eu.interiot.gateway.commons.api.command.CommandLine.Option;
import eu.interiot.gateway.commons.api.command.ExecutableCommand;
import eu.interiot.gateway.rulesengine.api.model.Execution;
import eu.interiot.gateway.rulesengine.api.model.Rule;
import eu.interiot.gateway.rulesengine.api.model.Statement;
import eu.interiot.gateway.rulesengine.api.model.description.ExecutionDescription;
import eu.interiot.gateway.rulesengine.api.model.description.RuleDescription;
import eu.interiot.gateway.rulesengine.api.model.description.StatementDescription;

@Command(name="rules", description="Manage rules of Rules Engine")
public class RulesEngineCommands extends ExecutableCommand{
	
	private RuleService ruleService;
	private Gson gson;
	
	public RulesEngineCommands(RuleService ruleService) {
		this.ruleService = ruleService;
		this.gson = new GsonBuilder().create();
	}
	
	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
    private boolean helpRequested;
	
	@Option(names= {"-a", "--add"}, arity="1...*", description="Add rules from json files")
	private List<String> filesToAdd;

	@Override
	public void execute(PrintWriter out) throws Exception {
		if(filesToAdd != null) {
			for(String filePath : filesToAdd) addFile(filePath, out);
			filesToAdd = null;
		}
	}
	
	private void addFile(String filePath, PrintWriter out) throws Exception{
		FileReader reader = new FileReader(filePath);
		JsonObject jsonRuleObj = new JsonParser().parse(reader).getAsJsonObject();
		StatementDescription statementDescription = this.gson.fromJson(jsonRuleObj.get("statement"), StatementDescription.class);
		ExecutionDescription executionDescription = this.gson.fromJson(jsonRuleObj.get("execution"), ExecutionDescription.class);
		RuleDescription ruleDescription = new RuleDescription();
		ruleDescription.setName(statementDescription.getName() + "-" + executionDescription.getName());
		ruleDescription.setDescription("Statement\n" + statementDescription.getDescription() + "\n\nExecution\n" + executionDescription.getDescription());
		Statement statement = ruleService.compileStatement(statementDescription.getStatement());
		Execution execution = ruleService.compileExecution(executionDescription.getExecution());
		Rule rule = ruleService.createRule(statement, execution);
		RuleManager ruleManager = ruleService.getRuleManager();
		String statementId = ruleManager.addStatement(statementDescription.getEntity(statement));
		String executionId = ruleManager.addExecution(executionDescription.getEntity(execution));
		ruleDescription.setStatementId(statementId);
		ruleDescription.setStatementId(executionId);
		String ruleId = ruleManager.addRule(ruleDescription.getEntity(rule));
		out.println("File: " + filePath);
		out.println("  Statement ID: " + statementId);
		out.println("  Execution ID: " + executionId);
		out.println("  Rule ID: " + ruleId);
	}
	
}
