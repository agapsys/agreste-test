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

import com.agapsys.agreste.model.AbstractUser;
import javax.persistence.Entity;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
@Entity
public class MyUser extends AbstractUser {

	private String username;
	private String passwordHash;

	protected MyUser() {}
	
	public MyUser(String username, String password) {
		setUsername(username);
		setPassword(password);
	}
	
	public String getUsername() {
		return username;
	}
	public final void setUsername(String username) {
		if (username == null || username.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty username");
		
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPasswordHash(String passwordHash) {
		if (passwordHash == null || passwordHash.trim().isEmpty()) throw new IllegalArgumentException("Null/Empty password hash");
			this.passwordHash = passwordHash;
	}
	public final void setPassword(String password) {
		
		int logRounds = 4;
		setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(logRounds)));
	}
	public boolean isPasswordValid(String password) {
		return BCrypt.checkpw(password, passwordHash);
	}
	
	@Override
	public boolean isAdmin() {
		return false;
	}
}
