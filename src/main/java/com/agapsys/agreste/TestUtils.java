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

package com.agapsys.agreste;

import com.agapsys.agreste.controllers.BaseController;
import com.agapsys.agreste.utils.JsonSerializer;
import com.agapsys.http.HttpDelete;
import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpHead;
import com.agapsys.http.HttpOptions;
import com.agapsys.http.HttpRequest;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.http.HttpTrace;
import com.agapsys.http.StringEntityRequest;
import com.agapsys.rcf.HttpMethod;
import com.agapsys.utils.console.printer.ConsoleColor;
import com.agapsys.utils.console.printer.ConsolePrinter;
import com.agapsys.web.toolkit.modules.PersistenceModule;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.persistence.EntityManager;

/**
 * Testing utilities for AGRESTE-based applications
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class TestUtils extends com.agapsys.web.toolkit.TestUtils {
	// STATIC SCOPE =============================================================
	private static TestUtils singleton = null;
	
	public static TestUtils getInstance() {
		if (singleton == null)
			singleton = new TestUtils();
		
		return singleton;
	}
	
	public static class RestEndpoint {
		public final HttpMethod method;
		public final String uri;
		public final String[] uriParamNames;
		
		private boolean isEntityMethod(HttpMethod method) {
			switch(method) {
				case POST:
				case PUT:
				case PATCH:
					return true;
			}
			
			return false;
		}
		
		public RestEndpoint(HttpMethod method, String uri, String...uriParamNames) {			
			if (method == null)
				throw new IllegalArgumentException("Null HTTP method");
			
			if (isEntityMethod(method))
				throw new UnsupportedOperationException("Unsupported method: " + method.name());
			
			if (uri == null || uri.trim().isEmpty())
				throw new IllegalArgumentException("Null/Empty URI");
			
			this.method = method;
			this.uri = uri;
			this.uriParamNames = uriParamNames;
		}
		
		public HttpRequest getRequest(Object...uriParams) {
			StringBuilder uriFormat = new StringBuilder(uri);
			
			boolean firstParam = true;
			for (String paramName : uriParamNames) {
				if (firstParam) {
					uriFormat.append("?");
					firstParam = false;
				} else {
					uriFormat.append("&");
				}

				uriFormat.append(String.format("%s=%%s", paramName));
			}

			switch (method) {
				case DELETE:
					return new HttpDelete(uriFormat.toString(), uriParams);

				case GET:
					return new HttpGet(uriFormat.toString(), uriParams);

				case HEAD:
					return new HttpHead(uriFormat.toString(), uriParams);

				case OPTIONS:
					return new HttpOptions(uriFormat.toString(), uriParams);

				case TRACE:
					return new HttpTrace(uriFormat.toString(), uriParams);

				default:
					throw new UnsupportedOperationException("Unsupported method: " + method.name());
			}
		}
		
		@Override
		public String toString() {
			return String.format("%s %s", method.name(), uri);
		}
	}
	
	public static class EntityRestEndpoint {
		public final HttpMethod method;
		public final String uri;
		public final String[] uriParamNames;
		private JsonSerializer serializer;
		
		private final TestUtils testUtils = getInstance();
		
		private boolean isEntityMethod(HttpMethod method) {
			switch(method) {
				case POST:
				case PUT:
				case PATCH:
					return true;
			}
			
			return false;
		}

		public EntityRestEndpoint(HttpMethod method, JsonSerializer serializer, String uri, String...uriParamNames) {			
			if (method == null)
				throw new IllegalArgumentException("Null HTTP method");
			
			if (!isEntityMethod(method))
				throw new UnsupportedOperationException("Unsupported method: " + method.name());
			
			if (serializer == null)
				throw new IllegalArgumentException("Serializer cannot be null");
			
			if (uri == null || uri.trim().isEmpty())
				throw new IllegalArgumentException("Null/Empty URI");
			
			this.method = method;
			this.uri = uri;
			this.uriParamNames = uriParamNames;
			this.serializer = serializer;
		}
		
		public EntityRestEndpoint(HttpMethod method, String uri, String...uriParamNames) {
			this(method, (JsonSerializer) BaseController.DEFAULT_SERIALIZER, uri, uriParamNames);
		}
		
		public HttpRequest getRequest(Object dto, Object...uriParams) {
			StringBuilder uriFormat = new StringBuilder(uri);
			
			boolean firstParam = true;
			for (String paramName : uriParamNames) {
				if (firstParam) {
					uriFormat.append("?");
					firstParam = false;
				} else {
					uriFormat.append("&");
				}

				uriFormat.append(String.format("%s=%%s", paramName));
			}
				
			Class<? extends StringEntityRequest> requestClass;
			switch (method) {
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
					throw new UnsupportedOperationException("Unsupported method: " + method.name());
			}

			return testUtils.createJsonRequest(requestClass, serializer, uriParams[0], uri);
		}
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	protected TestUtils() {}
	
	/**
	 * Creates a {@linkplain StringEntityRequest}
	 * @param <T> Type of returned request
	 * @param requestClass {@linkplain StringEntityRequest} subclass
	 * @param jsonSerializer GSON serializer
	 * @param obj object to be serialized and added to request
	 * @param uri request URI
	 * @param uriParams optional request URI parameters
	 * @return request containing given object
	 */
	public <T extends StringEntityRequest> T createJsonRequest(Class<T> requestClass, JsonSerializer jsonSerializer, Object obj, String uri, Object...uriParams) {
		try {
			Constructor c = requestClass.getConstructor(String.class, String.class, String.class, Object[].class);
			T t = (T) c.newInstance("application/json", "utf-8", uri, uriParams);
			
			if (obj != null)
				t.setContentBody(jsonSerializer.toJson(obj));
			
			return t;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Read an object from a {@linkplain StringResponse} containing a JSON content.
	 * @param <T> type of object to be read
	 * @param objClass object class
	 * @param jsonSerializer JSON serializer used when reading the response
	 * @param resp server response
	 * @param encoding response content encoding (usually "utf-8")
	 * @return read object.
	 */
	public <T> T readJsonResponse(Class<T> objClass, JsonSerializer jsonSerializer, StringResponse resp, String encoding) {
		try {
			return (T) jsonSerializer.readObject(resp.getContentInputStream(), "utf-8", objClass);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	

	@Override
	public void println(String msg, Object...msgArgs) {
		println(ConsoleColor.MAGENTA, msg, msgArgs);
	}
	
	@Override
	public void print(String msg, Object...msgArgs) {
		print(ConsoleColor.MAGENTA, msg, msgArgs);
	}
	
	/** 
	 * Prints a colored message to console.
	 * @param fgColor foreground color
	 * @param msg message to be print
	 * @param msgArgs optional message arguments
	 */
	public void println(ConsoleColor fgColor, String msg, Object...msgArgs) {
		ConsolePrinter.println(fgColor, msg, msgArgs);
	}
	
	/** 
	 * Prints a colored message to console.
	 * @param fgColor foreground color
	 * @param msg message to be print
	 * @param msgArgs optional message arguments
	 */
	public void print(ConsoleColor fgColor, String msg, Object...msgArgs) {
		ConsolePrinter.print(fgColor, msg, msgArgs);
	}

	/**
	 * Returns an {@linkplain EntityManager} provided by {@linkplain PersistenceModule} registered with running application.
	 * @return {@linkplain EntityManager} instance. Do not forget to close it after use in order to avoid resource leakage.
	 */
	public EntityManager getApplicationEntityManager() {
		return getApplicationModule(PersistenceModule.class).getEntityManager();
	}
	// =========================================================================
}
