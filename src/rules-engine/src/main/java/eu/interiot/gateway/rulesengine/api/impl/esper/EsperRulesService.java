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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Supplier;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.osgi.framework.BundleContext;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.device.Measurement;
import eu.interiot.gateway.rulesengine.api.CompilationException;
import eu.interiot.gateway.rulesengine.api.ExecutionContext;
import eu.interiot.gateway.rulesengine.api.RuleManager;
import eu.interiot.gateway.rulesengine.api.RuleService;
import eu.interiot.gateway.rulesengine.api.model.Execution;
import eu.interiot.gateway.rulesengine.api.model.Rule;
import eu.interiot.gateway.rulesengine.api.model.Statement;

public class EsperRulesService implements RuleService{

	private EPServiceProvider epService;
	private final EsperRuleManager ruleManager;
	private final ScriptEngine engine;
	private final ExecutionContext context;
	
	public EsperRulesService(BundleContext bundleContext) throws ScriptException {
		this.ruleManager = new EsperRuleManager();
		this.engine = createEngine();
		this.context = createExecutionContext(bundleContext);
	}
	
	public ScriptEngine createEngine() throws ScriptException {
		ScriptEngineManager scriptEnginemanager = new ScriptEngineManager(null);
		ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		ScriptEngine engine = scriptEnginemanager.getEngineByName("nashorn");
		Thread.currentThread().setContextClassLoader(contextClassloader);
		return engine;
	}
	
	public ExecutionContext createExecutionContext(BundleContext bundleContext) throws ScriptException {
		InputStream is = EsperRulesService.class.getResourceAsStream("/context.init.js");
		return new EsperExecutionContext(bundleContext, engine, new InputStreamReader(is));
	}
	
	@Override
	public void sendMeasurement(DeviceDefinition device, Measurement measurement) {
		EPRuntime epRuntime = epService.getEPRuntime();
		measurement.getData().stream().map(data -> new MeasurementEvent(device, data)).forEach(epRuntime::sendEvent);
	}
	
	@Override
	public void run() {
		this.epService = executeInDifferentClassLoader(() -> {
			EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
			epService.getEPAdministrator().getConfiguration().addEventType("Measurement", MeasurementEvent.class);
			return epService;
		}, this.getClass().getClassLoader());
	}

	@Override
	public Statement compileStatement(String statement) throws CompilationException{
		try {
			return executeInDifferentClassLoader(() ->  {
				EPStatement epStatement = this.epService.getEPAdministrator().createEPL(statement);
				return new EsperStatement(statement, epStatement);
			}, this.getClass().getClassLoader());
		}catch(RuntimeException ex) {
			throw new CompilationException(ex);
		}
	}

	@Override
	public Execution compileExecution(String execution) throws CompilationException{
		try {
			CompiledScript cs = ((Compilable) this.engine).compile(execution);
			return new EsperExecution(execution, cs);
		} catch (ScriptException ex) {
			throw new CompilationException(ex);
		}
		
	}

	@Override
	public RuleManager getRuleManager() {
		return this.ruleManager;
	}

	@Override
	public Rule createRule(Statement statement, Execution execution) {
		return new EsperRule(statement, execution, context);
	}

	@Override
	public ExecutionContext getExecutionContext() {
		return this.context;
	}

	private static <T> T executeInDifferentClassLoader(Supplier<T> supplier, ClassLoader classLoader) throws RuntimeException{
		ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);
		T t = supplier.get();
		Thread.currentThread().setContextClassLoader(contextClassloader);
		return t;
	}
	
}
