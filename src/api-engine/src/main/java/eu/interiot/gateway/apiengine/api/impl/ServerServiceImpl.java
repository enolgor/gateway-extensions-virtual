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
package eu.interiot.gateway.apiengine.api.impl;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.osgi.framework.BundleContext;

import eu.interiot.gateway.apiengine.ServerApplication;
import eu.interiot.gateway.apiengine.SwaggerParser;
import eu.interiot.gateway.apiengine.api.ServerService;
import eu.interiot.gateway.commons.api.configuration.ConfigurationService;

public class ServerServiceImpl implements ServerService {

	private static Logger log = LogManager.getLogger(ServerServiceImpl.class);

	private Server jettyServer;
	
	public ServerServiceImpl(BundleContext bndctx) {
		ConfigurationService config = bndctx.getService(bndctx.getServiceReference(ConfigurationService.class));
		boolean isSSLEnabled = false;
		int sslPort = 4433;
		int port = config.getInt("port", 8080);
		String host = config.get("host", "localhost");
		String basicAuthUser = "eneko";
		String basicAuthPassword = "3cerdit0s";
		boolean isBasicAuthEnabled = false;
		
		this.jettyServer = new Server();
		
		ServerConnector connector;
		if(isSSLEnabled){
			SslContextFactory sslContextFactory = new SslContextFactory();
			URL keystoreUrl = bndctx.getBundle().getResource("/keystore.jks");
			sslContextFactory.setKeyStorePath(keystoreUrl.toString());
			sslContextFactory.setKeyStorePassword("georbac");
			connector = new ServerConnector(jettyServer, sslContextFactory);
			connector.setPort(sslPort);
			connector.setHost(host);
		}else{
			connector = new ServerConnector(jettyServer);
			connector.setName("Api");
			connector.setPort(port);
			connector.setHost(host);
		}
		
		jettyServer.addConnector(connector);
		
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS); 
        context.addVirtualHosts(new String[] {"@Api"});
 		context.setContextPath( "/" );
 		context.setWelcomeFiles(new String[] {"index.html"});
 		
 		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/api/*");
 		jerseyServlet.setInitOrder(0);
		jerseyServlet.setInitParameter("javax.ws.rs.Application", ServerApplication.class.getCanonicalName());
 		
 		context.addServlet(new ServletHolder(new BundleResourceServlet(bndctx, "/www")), "/*");

 		if(isBasicAuthEnabled) context.setSecurityHandler(basicAuth(basicAuthUser, basicAuthPassword, "Restricted"));
 		
 		HandlerCollection collection = new HandlerCollection();
 		collection.addHandler(context);
 		
 		//configureWebSocket(jettyServer, collection);
 		
 		jettyServer.setHandler(collection);
 		
 		jettyServer.setStopTimeout(1000);
 		jettyServer.setStopAtShutdown(true);
	}
	/*
	private static final void configureWebSocket(Server jettyServer, HandlerCollection collection) {
		ServerConnector wsconnector = new ServerConnector(jettyServer);
		wsconnector.setName("WS");
		wsconnector.setPort(8831);
		jettyServer.addConnector(wsconnector);
		ServletContextHandler wscontext = new ServletContextHandler(); 
		wscontext.addVirtualHosts(new String[] {"@WS"});
		ServletHandler wshandler = new ServletHandler();
		wshandler.addServletWithMapping(DefaultWebSocketServlet.class, "/");
		wscontext.setHandler(wshandler);
		collection.addHandler(wscontext);
	}*/
			
	private static final SecurityHandler basicAuth(String username, String password, String realm) {
		HashLoginService loginService = new HashLoginService();
		loginService.putUser(username, Credential.getCredential(password), new String[] {"admin"});
		loginService.setName(realm);
        
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"admin"});
        constraint.setAuthenticate(true);
         
        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");
        
        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName(realm);
        csh.addConstraintMapping(cm);
        csh.setLoginService(loginService);
        
        return csh;
	}
	
	@Override
	public void run() {
		SwaggerParser.update();
		try {
			jettyServer.start();
			jettyServer.join();
		} catch (Exception ex) {
			log.error(ex);
		} finally {
			jettyServer.destroy();
		}

	}

}
