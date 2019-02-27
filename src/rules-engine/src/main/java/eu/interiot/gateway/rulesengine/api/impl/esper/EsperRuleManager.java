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
package eu.interiot.gateway.rulesengine.api.impl.esper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.interiot.gateway.rulesengine.api.RuleManager;
import eu.interiot.gateway.rulesengine.api.model.Entity;
import eu.interiot.gateway.rulesengine.api.model.Execution;
import eu.interiot.gateway.rulesengine.api.model.Rule;
import eu.interiot.gateway.rulesengine.api.model.Statement;

public class EsperRuleManager implements RuleManager{
	
	private Map<String, Entity<? extends Rule>> rules;
	private Map<String, Entity<? extends Statement>> statements;
	private Map<String, Entity<? extends Execution>> executions;
	
	public EsperRuleManager() {
		this.rules = new HashMap<>();
		this.statements = new HashMap<>();
		this.executions = new HashMap<>();
	}
	@Override
	public Collection<Entity<? extends Execution>> listExecutions() {
		return this.executions.values().stream().collect(Collectors.toSet());
	}

	@Override
	public String addExecution(Entity<? extends Execution> execution) {
		String uid = UUID.randomUUID().toString();
		if(execution.get() instanceof EsperExecution) {
			execution.setId(uid);
			this.executions.put(uid, execution);
			return uid;
		}
		return null;
	}

	@Override
	public void removeExecution(String executionId) {
		Entity<? extends Execution> executionEntity = this.executions.remove(executionId);
		Execution execution = executionEntity.get();
		if(executionEntity != null) {
			Collection<String> removeRules = this.rules.entrySet().stream()
			.filter(e -> e.getValue().get().getExecution() == execution)
			.map(e -> e.getKey())
			.collect(Collectors.toSet());
			removeRules.stream().forEach(this.rules::remove);
		}
	}

	@Override
	public Collection<Entity<? extends Statement>> listStatements() {
		return this.statements.values().stream().collect(Collectors.toSet());
	}

	@Override
	public String addStatement(Entity<? extends Statement> statement) {
		String uid = UUID.randomUUID().toString();
		if(statement.get() instanceof EsperStatement) {
			statement.setId(uid);
			this.statements.put(uid, statement);
			return uid;
		}
		return null;
	}

	@Override
	public void removeStatement(String statementId) {
		Entity<? extends Statement> statementEntity = this.statements.remove(statementId);
		Statement statement = statementEntity.get();
		if(statementEntity != null) {
			Collection<String> removeRules = this.rules.entrySet().stream()
			.filter(e -> e.getValue().get().getStatement() == statement)
			.map(e -> e.getKey())
			.collect(Collectors.toSet());
			removeRules.stream().forEach(this.rules::remove);
		}
		((EsperStatement) statement).getEPStatement().destroy();
	}

	@Override
	public Collection<Entity<? extends Rule>> listRules() {
		return this.rules.values().stream().collect(Collectors.toSet());
	}

	@Override
	public String addRule(Entity<? extends Rule> rule) {
		String uid = UUID.randomUUID().toString();
		if(rule.get() instanceof EsperRule) {
			rule.setId(uid);
			this.rules.put(uid, rule);
			return uid;
		}
		return null;
	}

	@Override
	public void removeRule(String ruleId) {
		Entity<? extends Rule> ruleEntity = this.rules.remove(ruleId);
		EsperRule esperRule = (EsperRule) ruleEntity.get();
		((EsperStatement) esperRule.getStatement()).getEPStatement().removeListener(esperRule);
	}
	
	@Override
	public Entity<? extends Execution> getExecution(String executionId) {
		return this.executions.get(executionId);
	}
	@Override
	public Entity<? extends Statement> getStatement(String statementId) {
		return this.statements.get(statementId);
	}
	@Override
	public Entity<? extends Rule> getRule(String ruleId) {
		return this.rules.get(ruleId);
	}
	
}
