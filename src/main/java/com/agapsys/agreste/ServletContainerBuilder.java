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

import com.agapsys.security.web.SessionCsrfSecurityManager;
import com.agapsys.security.web.WebSecurityFilter;
import com.agapsys.security.web.WebSecurityManager;
import com.agapsys.sevlet.container.ServletContextHandlerBuilder;

/**
 * Servlet container builder for AGRESTE applications
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ServletContainerBuilder extends com.agapsys.web.toolkit.ServletContainerBuilder {
	// STATIC SCOPE ============================================================
	private static final WebSecurityManager DEFAULT_SECURITY_MANAGER = new SessionCsrfSecurityManager();
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private SecurityListener securityListener;
	
	private ServletContextHandlerBuilder _addContext(AbstractWebApplication webApp, String contextPath, WebSecurityManager securityManager, String...securedClasses) {
		securityListener = new SecurityListener(securityManager, securedClasses);
		WebSecurity.skipFrozenClasses(true);
		securityListener.contextInitialized(null);
		
		ServletContextHandlerBuilder ctxHandlerBuilder = super.addContext(webApp, contextPath)
			.registerFilter(WebSecurityFilter.class, "/*")
			.registerFilter(AbuseCheckFilter.class, "/*")
			.registerFilter(ClientExceptionFilter.class, "/*")
			.registerFilter(JpaTransactionFilter.class, "/*");
		
		return ctxHandlerBuilder;
	}
	
	public ServletContextHandlerBuilder addContext(AbstractWebApplication webApp, String contextPath, WebSecurityManager securityManager, String...securedClasses) {
		return _addContext(webApp, contextPath, securityManager, securedClasses);
	}
	
	public final ServletContextHandlerBuilder addContext(AbstractWebApplication webApp, String contextPath, String...securedClasses) {
		return _addContext(webApp, contextPath, DEFAULT_SECURITY_MANAGER, securedClasses);
	}
	
	public final ServletContextHandlerBuilder addRootContext(AbstractWebApplication webApp, WebSecurityManager securityManager, String...securedClasses) {
		return _addContext(webApp, ROOT_PATH, securityManager, securedClasses);
	}
	
	public final ServletContextHandlerBuilder addRootContext(AbstractWebApplication webApp, String...securedClasses) {
		return _addContext(webApp, ROOT_PATH, DEFAULT_SECURITY_MANAGER, securedClasses);
	}
	// =========================================================================
}
