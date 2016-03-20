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

import com.agapsys.agreste.AbstractWebApplication;
import com.agapsys.agreste.AbuseCheckFilter;
import com.agapsys.agreste.ClientExceptionFilter;
import com.agapsys.agreste.JpaTransactionFilter;
import com.agapsys.agreste.SecurityListener;
import com.agapsys.agreste.WebSecurity;
import com.agapsys.rcf.Controller;
import com.agapsys.rcf.WebController;
import com.agapsys.security.web.SessionCsrfSecurityManager;
import com.agapsys.security.web.WebSecurityFilter;
import com.agapsys.security.web.WebSecurityManager;
import com.agapsys.sevlet.container.ServletContainer;
import java.util.EventListener;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.server.handler.ErrorHandler;

/**
 * Servlet container builder for AGRESTE applications
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ServletContainerBuilder extends com.agapsys.web.toolkit.ServletContainerBuilder {
	// STATIC SCOPE ============================================================
	private static final WebSecurityManager DEFAULT_SECURITY_MANAGER = new SessionCsrfSecurityManager();
	
	public static ServletContainer getControllerContainer(Class<? extends Controller>...controllers) {
		return getControllerContainer(MockedWebApplication.class, controllers);
	}
	
	public static ServletContainer getControllerContainer(Class<? extends AbstractWebApplication> webApp, Class<? extends Controller>...controllers) {
		ServletContainerBuilder builder = new ServletContainerBuilder(webApp);
		for (Class<? extends Controller> controller : controllers) {
			builder.registerController(controller);
		}
		return builder.build();
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private SecurityListener securityListener;
	
	private void init(WebSecurityManager securityManager, String...securedClasses) {
		securityListener = new SecurityListener(securityManager, securedClasses);
		WebSecurity.skipFrozenClasses(true);
		securityListener.contextInitialized(null);
		
		super.registerFilter(WebSecurityFilter.class, "/*");
		super.registerFilter(AbuseCheckFilter.class, "/*");
		super.registerFilter(ClientExceptionFilter.class, "/*");
		super.registerFilter(JpaTransactionFilter.class, "/*");
	}
	
	public ServletContainerBuilder(Class<? extends AbstractWebApplication> webApp, WebSecurityManager securityManager, String...securedClasses) {
		super(webApp);
		init(securityManager, securedClasses);
	}
			
	public ServletContainerBuilder(Class<? extends AbstractWebApplication> webApp, String...securedClasses) {
		super(webApp);
		init(DEFAULT_SECURITY_MANAGER, securedClasses);
	}
	
	public ServletContainerBuilder registerController(Class<? extends Controller> controllerClass, String name) {
		return (ServletContainerBuilder) super.registerServlet(controllerClass, String.format("/%s/*", name));
	}
	
	public ServletContainerBuilder registerController(Class<? extends Controller> controllerClass) {
		WebController annotation = controllerClass.getAnnotation(WebController.class);

		if (annotation == null)
			throw new IllegalArgumentException("Controller class does not have a WebController annotation");

		String name = annotation.value();
		if (name.trim().isEmpty())
			name = controllerClass.getSimpleName();

		registerController(controllerClass, name);
	
		return this;
	}
	
	@Override
	public ServletContainerBuilder setLocalPort(int localPort) {
		return (ServletContainerBuilder) super.setLocalPort(localPort);
	}

	@Override
	public ServletContainerBuilder setErrorHandler(ErrorHandler errorHandler) {
		return (ServletContainerBuilder) super.setErrorHandler(errorHandler);
	}

	@Override
	public ServletContainerBuilder registerErrorPage(int code, String url) {
		return (ServletContainerBuilder) super.registerErrorPage(code, url);
	}

	@Override
	public ServletContainerBuilder registerServlet(Class<? extends HttpServlet> servletClass, String urlPattern) {
		return (ServletContainerBuilder) super.registerServlet(servletClass, urlPattern);
	}

	@Override
	public ServletContainerBuilder registerFilter(Class<? extends Filter> filterClass, String urlPattern, boolean append) {
		return (ServletContainerBuilder) super.registerFilter(filterClass, urlPattern, append);
	}

	@Override
	public ServletContainerBuilder registerEventListener(Class<? extends EventListener> eventListener, boolean append) {
		return (ServletContainerBuilder) super.registerEventListener(eventListener, append);
	}
	// =========================================================================
}
