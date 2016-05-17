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

import com.agapsys.agreste.AbuseCheckFilter;
import com.agapsys.agreste.ClientExceptionFilter;
import com.agapsys.agreste.JpaTransactionFilter;
import com.agapsys.rcf.Controller;
import com.agapsys.rcf.WebController;
import com.agapsys.sevlet.container.ServletContainer;
import com.agapsys.web.toolkit.AbstractWebApplication;

/**
 * Servlet container builder for AGRESTE applications
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ServletContainerBuilder<T extends ServletContainerBuilder> extends com.agapsys.web.toolkit.ServletContainerBuilder<T> {
	// STATIC SCOPE ============================================================
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
	private void init() {
		super.registerFilter(AbuseCheckFilter.class, "/*");
		super.registerFilter(ClientExceptionFilter.class, "/*");
		super.registerFilter(JpaTransactionFilter.class, "/*");
	}

	public ServletContainerBuilder(Class<? extends AbstractWebApplication> webApp) {
		super(webApp);
		init();
	}
	
	
	public ServletContainerBuilder registerController(Class<? extends Controller> controllerClass, String name) {
		return (ServletContainerBuilder) super.registerServlet(controllerClass, String.format("/%s/*", name));
	}
	
	public ServletContainerBuilder registerController(Class<? extends Controller> controllerClass) {
		WebController annotation = controllerClass.getAnnotation(WebController.class);

		if (annotation == null)
			throw new IllegalArgumentException(String.format("Missing annotation '%s' for '%s'", WebController.class.getName(), controllerClass.getName()));

		String name = annotation.value();
		if (name.trim().isEmpty())
			name = controllerClass.getSimpleName();

		registerController(controllerClass, name);
	
		return this;
	}
	// =========================================================================
}
