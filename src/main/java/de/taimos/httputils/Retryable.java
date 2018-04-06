package de.taimos.httputils;

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

import java.util.Optional;

/**
 * @author thoeger
 */
@FunctionalInterface
public interface Retryable {

    /**
     * Decide if a attempt should be retried
     *
     * @param exception  If an exception occurred, this parameter is filled
     * @param statusCode Otheriwse, the statusCode is provided
     * @return true if the attempt should be retried
     */
    boolean retry(final Optional<Exception> exception, final Optional<Integer> statusCode);

    static Retryable standard() {
        return (exception, statusCode) -> {
            if (exception.isPresent()) {
                return true;
            }
            return statusCode.map(code -> (code >= 500 && code < 599)).orElseThrow(IllegalStateException::new);
        };
    }
}
