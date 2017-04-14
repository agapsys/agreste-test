/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
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
 */

package com.agapsys.agreste.test;

import com.agapsys.http.HttpDelete;
import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpHead;
import com.agapsys.http.HttpOptions;
import com.agapsys.http.HttpRequest;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.http.HttpTrace;
import com.agapsys.http.StringEntityRequest;
import com.agapsys.http.StringEntityRequest.StringEntityPatch;
import com.agapsys.http.StringEntityRequest.StringEntityPost;
import com.agapsys.http.StringEntityRequest.StringEntityPut;
import com.agapsys.rcf.HttpMethod;
import com.agapsys.rcf.JsonRequest;
import com.agapsys.rcf.JsonResponse;
import com.agapsys.utils.console.printer.ConsoleColor;
import com.agapsys.utils.console.printer.ConsolePrinter;
import com.agapsys.web.toolkit.services.PersistenceService;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * Testing utilities for AGRESTE-based applications
 */
public class TestUtils extends com.agapsys.web.toolkit.TestUtils {

    protected TestUtils() {}

    static boolean __isEntityMethod(HttpMethod method) {
            switch(method) {
                case POST:
                case PUT:
                case PATCH:
                    return true;
            }

            return false;
        }

    public static class Endpoint {
        public final  HttpMethod method;
        public final  String uri;

        public Endpoint(HttpMethod method, String uri) {
            if (method == null)
                throw new IllegalArgumentException("Null HTTP method");

            if (uri == null || uri.trim().isEmpty())
                throw new IllegalArgumentException("Null/Empty URI");

            this.method = method;
            this.uri = uri;
        }

        public HttpRequest getRequest(String params, Object...paramArgs) {
            if (params == null)
                params = "";

            if (paramArgs.length > 0)
                params = String.format(params, paramArgs);

            params = params.trim();

            String finalUri = params.isEmpty() ? uri : String.format("%s?%s", getUri(), params);

            switch (method) {
                case DELETE:
                    return new HttpDelete(finalUri);

                case GET:
                    return new HttpGet(finalUri);

                case HEAD:
                    return new HttpHead(finalUri);

                case OPTIONS:
                    return new HttpOptions(finalUri);

                case TRACE:
                    return new HttpTrace(finalUri);

				case PATCH:
					return TestUtils.createJsonRequest(StringEntityPatch.class, null, finalUri);

				case POST:
					return TestUtils.createJsonRequest(StringEntityPost.class, null, finalUri);

				case PUT:
					return TestUtils.createJsonRequest(StringEntityPut.class, null, finalUri);

				default:
					throw new UnsupportedOperationException("Unsupported method: " + getMethod().name());
            }
        }

        public final HttpRequest getRequest() {
            return getRequest("");
        }

        public HttpMethod getMethod() {
            return method;
        }

        public String getUri() {
            return uri;
        }

        @Override
        public String toString() {
            return String.format("%s %s", method.name(), uri);
        }
    }

    public static class JsonEndpoint extends Endpoint {

        public JsonEndpoint(HttpMethod method, String uri) {
            super(method, uri);

            if (!__isEntityMethod(method))
                throw new UnsupportedOperationException("Unsupported method: " + method.name());
        }

        public HttpRequest getRequest(Object dto, String uriParams, Object...uriParamArgs) {
            if (uriParams == null)
                uriParams = "";

            if (uriParamArgs.length > 0)
                uriParams = String.format(uriParams, uriParamArgs);

            uriParams = uriParams.trim();

            String finalUri = uriParams.isEmpty() ? getUri() : String.format("%s?%s", getUri(), uriParams);

            Class<? extends StringEntityRequest> requestClass;
            switch (getMethod()) {
                case POST:
                    requestClass = StringEntityRequest.StringEntityPost.class;
                    break;
                case PUT:
                    requestClass = StringEntityRequest.StringEntityPut.class;
                    break;

                case PATCH:
                    requestClass = StringEntityRequest.StringEntityPatch.class;
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported method: " + getMethod().name());
            }

            return TestUtils.createJsonRequest(requestClass, dto, finalUri);
        }

        public HttpRequest getRequest(Object dto) {
            return getRequest(dto, "");
        }
    }


    public static <R extends StringEntityRequest> R createJsonRequest(Class<R> requestClass, Object obj, String uri, Object...uriParams) {
        try {
            Constructor c = requestClass.getConstructor(String.class, String.class, String.class, Object[].class);
            R r = (R) c.newInstance(JsonRequest.JSON_CONTENT_TYPE, JsonRequest.JSON_ENCODING, uri, uriParams);

            if (obj != null)
                r.setContentBody(JsonResponse.toJson(obj));

            return r;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> T readJsonObject(Class<T> objClass, StringResponse resp) {
        try {
            return JsonRequest.readObject(new InputStreamReader(resp.getContentInputStream()), objClass);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Reads a JSON list from given response.
     * @param <E> List element type
     * @param elementClass List element class.
     * @param resp server response
     * @return JSON list contained in given response.
     */
    public static <E> List<E> readJsonList(Class<E> elementClass, StringResponse resp) {
        try {
            return JsonRequest.readList(new InputStreamReader(resp.getContentInputStream()), elementClass);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void println(String msg, Object...msgArgs) {
        println(ConsoleColor.MAGENTA, msg, msgArgs);
    }

    public static void print(String msg, Object...msgArgs) {
        print(ConsoleColor.MAGENTA, msg, msgArgs);
    }

    /**
     * Prints a colored message to console.
     * @param fgColor foreground color
     * @param msg message to be print
     * @param msgArgs optional message arguments
     */
    public static void println(ConsoleColor fgColor, String msg, Object...msgArgs) {
        ConsolePrinter.println(fgColor, msg, msgArgs);
    }

    /**
     * Prints a colored message to console.
     * @param fgColor foreground color
     * @param msg message to be print
     * @param msgArgs optional message arguments
     */
    public static void print(ConsoleColor fgColor, String msg, Object...msgArgs) {
        ConsolePrinter.print(fgColor, msg, msgArgs);
    }

    /**
     * Returns an {@linkplain EntityManager} provided by {@linkplain PersistenceModule} registered with running application.
     * @return {@linkplain EntityManager} instance. Do not forget to close it after use in order to avoid resource leakage.
     */
    public static EntityManager getApplicationEntityManager() {
        return getApplicationService(PersistenceService.class).getEntityManager();
    }
}
