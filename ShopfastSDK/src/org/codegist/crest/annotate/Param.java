/*
 * Copyright 2010 CodeGist.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Optional method level annotation, sets a method default parameter to add for all requests.
 * <p>Can be set at interface level to default all interface method default parameter.
 * @see org.codegist.crest.config.MethodConfig#DEFAULT_PARAMS
 * @see org.codegist.crest.config.MethodConfig#getStaticParams()
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Param {
    /**
     * @return Parameter name
     */
    String name();

    /**
     * @return Parameter value
     */
    String value();

    /**
     * @return Parameter destination
     */
    org.codegist.crest.config.Destination dest();
}
