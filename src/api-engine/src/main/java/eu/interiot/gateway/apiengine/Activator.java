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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import eu.interiot.gateway.apiengine.api.ServerService;
import eu.interiot.gateway.apiengine.api.impl.CORSFilter;
import eu.interiot.gateway.apiengine.api.impl.GsonProvider;
import eu.interiot.gateway.apiengine.api.impl.ServerServiceImpl;
import eu.interiot.gateway.commons.api.services.CoreService;

public class Activator implements BundleActivator, ServiceListener{

	private static Logger log = LogManager.getLogger(Activator.class);
	
	private BundleContext context;
	
	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		this.context.addServiceListener(this, "(&(objectClass=java.lang.Class)(eu.gateway.api.endpoint=true))");
		for(ServiceReference<?> sr : this.context.getServiceReferences(Class.class, "(eu.gateway.api.endpoint=true)")) 
			ServerApplication.addRoute((Class<?>)this.context.getService(sr));
		ServerApplication.addRoute(SwaggerEndpoint.class);
		ServerApplication.addRoute(GsonProvider.class);
		ServerApplication.addRoute(CORSFilter.class);
		ServerApplication.addRoute(ApiVersionRouter.class);
		//ServerApplication.addRoute(MarshallingFeature.class);
		ServerService serverService = new ServerServiceImpl(context);
		this.context.registerService(CoreService.class, serverService, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void serviceChanged(ServiceEvent event) {
		switch(event.getType()){
		case ServiceEvent.REGISTERED: 
			ServiceReference<?> serviceReference = event.getServiceReference();
			try {
				ServerApplication.addRoute((Class<?>)this.context.getService(serviceReference));
			} catch (Exception e) {
				log.error(e);
			}
			break;
		}
	}
	
}
