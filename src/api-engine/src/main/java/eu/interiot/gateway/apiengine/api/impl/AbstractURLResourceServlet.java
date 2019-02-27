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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public abstract class AbstractURLResourceServlet extends StaticResourceServlet {

	private static Logger log = LogManager.getLogger(AbstractURLResourceServlet.class);
	
	public abstract URLConnection openURLConnection(String resourceName) throws Exception;

	@Override
	protected StaticResource getStaticResource(HttpServletRequest request) throws IllegalArgumentException {

		String pathInfo = request.getPathInfo();
		
		if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
			//throw new IllegalArgumentException();
			pathInfo = "/index.html";
		}

		String name;
		try {
			name = URLDecoder.decode(pathInfo.substring(1), StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			throw new IllegalArgumentException();
		}
		
		try {
			URLConnection connection = openURLConnection(name);
			return new StaticResource() {
				@Override
				public long getLastModified() {
					long lastModified = connection.getLastModified();
					return lastModified == 0 ? System.currentTimeMillis() : lastModified;
				}

				@Override
				public InputStream getInputStream() throws IOException {
					return connection.getInputStream();
				}

				@Override
				public String getFileName() {
					return name.substring(name.lastIndexOf("/") + 1, name.length());
				}

				@Override
				public long getContentLength() {
					return connection.getContentLengthLong();
				}
			};
		} catch (Exception e) {
			log.error(e);
			return null;
		}

	}

}
