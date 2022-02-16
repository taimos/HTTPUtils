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

/**
 * @author thoeger
 */
@FunctionalInterface
public interface WaitStrategy {

    /**
     * @param retry First retry starts at 0
     * @return Milliseconds to sleep
     */
    int milliseconds(final int retry);

    /**
     * Exponential backoff slightly randomized
     *
     * @return Milliseconds to sleep
     */
    static WaitStrategy exponentialBackoff() {
        return retry -> (int) (Math.random() * (Math.pow(2, retry) * 500));
    }

    /**
     * @param ms Milliseconds to sleep
     * @return Milliseconds to sleep
     */
    static WaitStrategy constant(final int ms) {
        return retry -> ms;
    }

    /**
     * Always wait 1 second before retry
     *
     * @return Milliseconds to sleep
     */
    static WaitStrategy constant() {
        return WaitStrategy.constant(1000);
    }

    /**
     * @param ms Milliseconds to sleep
     * @return Milliseconds to sleep
     */
    static WaitStrategy linear(final int ms) {
        return retry -> (retry + 1) * ms;
    }

    /**
     * Wait 1 second for first retry, 2 seconds for 2nd retry, ...
     *
     * @return Milliseconds to sleep
     */
    static WaitStrategy linear() {
        return WaitStrategy.linear(1000);
    }
}
