package de.taimos.httputils.callbacks;

/*
 * #%L
 * Taimos HTTPUtils
 * %%
 * Copyright (C) 2012 - 2015 Taimos GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import de.taimos.httputils.HTTPResponse;

public abstract class HTTPByteCallback extends HTTPStatusCheckCallback {

    @Override
    protected void checkedResponse(final HTTPResponse response) {
        this.byteResponse(response.getResponseAsBytes(), response);
    }

    protected abstract void byteResponse(byte[] body, HTTPResponse response);

}
