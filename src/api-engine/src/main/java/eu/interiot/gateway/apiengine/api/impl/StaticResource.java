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

public interface StaticResource {

    /**
     * Returns the file name of the resource. This must be unique across all static resources. If any, the file
     * extension will be used to determine the content type being set. If the container doesn't recognize the
     * extension, then you can always register it as <code>&lt;mime-type&gt;</code> in <code>web.xml</code>.
     * @return The file name of the resource.
     */
    public String getFileName();

    /**
     * Returns the last modified timestamp of the resource in milliseconds.
     * @return The last modified timestamp of the resource in milliseconds.
     */
    public long getLastModified();

    /**
     * Returns the content length of the resource. This returns <code>-1</code> if the content length is unknown.
     * In that case, the container will automatically switch to chunked encoding if the response is already
     * committed after streaming. The file download progress may be unknown.
     * @return The content length of the resource.
     */
    public long getContentLength();

    /**
     * Returns the input stream with the content of the resource. This method will be called only once by the
     * servlet, and only when the resource actually needs to be streamed, so lazy loading is not necessary.
     * @return The input stream with the content of the resource.
     * @throws IOException When something fails at I/O level.
     */
    public InputStream getInputStream() throws IOException;

}