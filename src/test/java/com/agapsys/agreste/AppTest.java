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

import com.agapsys.agreste.TestUtils.RestEndpoint;
import com.agapsys.agreste.app.MyApplication;
import com.agapsys.agreste.app.MyServlet;
import com.agapsys.agreste.exceptions.ForbiddenException;
import com.agapsys.http.HttpClient;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.sevlet.container.ServletContainer;
import com.agapsys.web.action.dispatcher.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class AppTest {
	private final TestUtils testUtils = TestUtils.getInstance();
	
	private ServletContainer sc;
	
	@Before
	public void before() {
		sc = new ServletContainerBuilder(new MyApplication())
			.addRootContext()
				.registerServlet(MyServlet.class)
			.endContext()
		.build();
		
		sc.startServer();
	}
	
	@After
	public void after() {
		sc.stopServer();
	}
	
	@Test
	public void testInvalidCredentials() {
		RestEndpoint endpoint = new RestEndpoint(HttpMethod.GET, "/login", "username", "password");
		testUtils.println(endpoint.toString());
		
		HttpClient client = new HttpClient();
		StringResponse resp;

		// Invalid credentials...
		resp = sc.doRequest(client, endpoint.getRequest("invalid_user", "invalid_password"));
		testUtils.assertErrorStatus(ForbiddenException.CODE, "Invalid credentials", resp);
		
		// Valid credentials...
		resp = sc.doRequest(client, endpoint.getRequest("user1", "password1"));
		testUtils.assertStatus(200, resp);
	}
}
