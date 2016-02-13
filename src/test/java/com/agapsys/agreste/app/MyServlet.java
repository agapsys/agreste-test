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
package com.agapsys.agreste.app;

import com.agapsys.agreste.JpaTransaction;
import com.agapsys.agreste.WebSecurity;
import com.agapsys.agreste.exceptions.ForbiddenException;
import com.agapsys.agreste.servlets.BaseServlet;
import com.agapsys.jpa.FindBuilder;
import com.agapsys.security.Secured;
import com.agapsys.web.action.dispatcher.HttpExchange;
import com.agapsys.web.action.dispatcher.HttpMethod;
import com.agapsys.web.action.dispatcher.WebAction;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
@WebServlet("/*")
public class MyServlet extends BaseServlet {
	
	private MyService myService;

	@Override
	protected void onInit() {
		super.onInit();
		myService = getService(MyService.class);
	}
	
	@WebAction(httpMethods = HttpMethod.GET, mapping = "login")
	public void login(HttpExchange exchange) {
		final String PARAM_USERNAME = "username";
		final String PARAM_PASSWORD = "password";
		
		String username = getMandatoryParameter(exchange, PARAM_USERNAME);
		String password = getMandatoryParameter(exchange, PARAM_PASSWORD);
		
		JpaTransaction jpa = getJpaTransaction();
		MyUser user = (MyUser) new FindBuilder<>(MyUser.class).by("username", username).findFirst(jpa.getEntityManager());
		
		if (user == null || !user.isPasswordValid(password))
			throw new ForbiddenException("Invalid credentials");
		
		WebSecurity.setCurrentUser(user);
	}
	
	@WebAction(httpMethods = HttpMethod.GET, mapping = "publicGet")
	public void publicGet(HttpExchange exchange) throws IOException {
		HttpServletResponse resp = exchange.getResponse();
		
		resp.setStatus(200);
		resp.getWriter().print("OK");
	}
	
	@WebAction(httpMethods = HttpMethod.GET, mapping = "securedGet")
	@Secured
	public void securedGet(HttpExchange exchange) throws IOException {
		HttpServletResponse resp = exchange.getResponse();
		
		resp.setStatus(200);
		resp.getWriter().print("OK");
	}
	
	@WebAction(httpMethods = HttpMethod.GET, mapping = "implictSecuredGet")
	public void implictSecuredGet(HttpExchange exchange) {
		myService.protectedMethod();
	}
}
