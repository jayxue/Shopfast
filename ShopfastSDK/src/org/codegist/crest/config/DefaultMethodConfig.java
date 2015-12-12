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

package org.codegist.crest.config;

import org.codegist.common.lang.ToStringBuilder;
import org.codegist.crest.HttpMethod;
import org.codegist.crest.handler.ErrorHandler;
import org.codegist.crest.handler.ResponseHandler;
import org.codegist.crest.handler.RetryHandler;
import org.codegist.crest.interceptor.RequestInterceptor;

import java.lang.reflect.Method;

/**
 * Default immutable in-memory implementation of {@link org.codegist.crest.config.DefaultMethodConfig}
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
class DefaultMethodConfig implements MethodConfig {

    private final Method method;
    private final String path;
    private final StaticParam[] param;
    private final HttpMethod httpMethod;
    private final Long socketTimeout;
    private final Long connectionTimeout;
    private final RequestInterceptor requestInterceptor;
    private final ResponseHandler responseHandler;
    private final ErrorHandler errorHandler;
    private final RetryHandler retryHandler;

    private final ParamConfig[] paramConfigs;

    DefaultMethodConfig(Method method, String path, StaticParam[] param, HttpMethod httpMethod, Long socketTimeout, Long connectionTimeout, RequestInterceptor requestInterceptor, ResponseHandler responseHandler, ErrorHandler errorHandler, RetryHandler retryHandler, ParamConfig[] paramConfigs) {
        this.method = method;
        this.path = path;
        this.param = param != null ? param.clone() : null;
        this.httpMethod = httpMethod;
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.requestInterceptor = requestInterceptor;
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
        this.retryHandler = retryHandler;
        this.paramConfigs = paramConfigs != null ? paramConfigs.clone() : null;
    }

    public StaticParam[] getStaticParams() {
        return param != null ? param.clone() : null;
    }

    public String getPath() {
        return path;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public Method getMethod() {
        return method;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Long getSocketTimeout() {
        return socketTimeout;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public RequestInterceptor getRequestInterceptor() {
        return requestInterceptor;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public RetryHandler getRetryHandler() {
        return retryHandler;
    }

    public ParamConfig getParamConfig(int index) {
        return paramConfigs != null && index < paramConfigs.length ? paramConfigs[index] : null;
    }

    public Integer getParamCount() {
        return paramConfigs != null ? paramConfigs.length : null;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("path", path)
                .append("method", method)
                .append("httpMethod", httpMethod)
                .append("socketTimeout", socketTimeout)
                .append("connectionTimeout", connectionTimeout)
                .append("requestInterceptor", requestInterceptor)
                .append("responseHandler", responseHandler)
                .append("errorHandler", errorHandler)
                .append("retryHandler", retryHandler)
                .append("paramConfigs", paramConfigs)
                .toString();
    }
}
