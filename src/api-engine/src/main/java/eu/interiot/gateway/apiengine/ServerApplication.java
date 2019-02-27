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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationPath("/api/*")
public class ServerApplication extends Application{
	
	private static Logger log = LogManager.getLogger(ServerApplication.class);
	
	private final static Set<Class<?>> routeClasses = new HashSet<>();
	
	public static synchronized void addRoute(Class<?> routeClass) {
		log.info(String.format("Registering API class: %s", routeClass.getCanonicalName()));
		routeClasses.add(routeClass);
	}
	
	public static synchronized void addRoutes(Collection<Class<?>> routeClasses) {
		for(Class<?> cls : routeClasses) {
			log.info(String.format("Registering API class: %s", cls.getCanonicalName()));
			try {
				log.info(cls.newInstance());
			} catch (Exception e) {
				log.error(e);
			}
		}
		routeClasses.addAll(routeClasses);
	}
	
	public static synchronized Set<Class<?>> getClassesStatic() {
		return routeClasses;
	}
	
	@Override
	public synchronized Set<Class<?>> getClasses() {
		return routeClasses;
	}
}