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
import com.agapsys.security.web.WebSecurityManager;
import com.agapsys.sevlet.container.ServletContextHandlerBuilder;
import com.agapsys.web.toolkit.AbstractWebApplication;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ServletContainerBuilder extends com.agapsys.web.toolkit.ServletContainerBuilder {
	// STATIC SCOPE ============================================================
	private static final WebSecurityManager DEFAULT_SECURITY_MANAGER = new SessionCsrfSecurityManager();
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final WebSecurityManager securityManager;
	
	public ServletContainerBuilder(AbstractWebApplication webApp) {
		this(webApp, DEFAULT_SECURITY_MANAGER);
	}
	
	public ServletContainerBuilder(AbstractWebApplication webApp, WebSecurityManager securityManager) {
		super(webApp);
		this.securityManager = securityManager;
	}

	@Override
	public ServletContextHandlerBuilder addContext(String contextPath) {
		ServletContextHandlerBuilder ctxHandlerBuilder = super.addContext(contextPath)
			.registerEventListener(new MockedSecurityListener(securityManager), false)
			.registerFilter(AbuseCheckFilter.class, "/*")
			.registerFilter(ClientExceptionFilter.class, "/*")
			.registerFilter(JpaTransactionFilter.class, "/*");
		
		return ctxHandlerBuilder;
	}
	// =========================================================================
}
