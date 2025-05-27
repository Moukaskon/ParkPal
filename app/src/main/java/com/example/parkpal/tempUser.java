package com.example.parkpal;

public class tempUser {
	private String username;
	private String password;
	private String email;

	public tempUser(String name, String pass, String email) {
		username = name;
		password = pass;
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}
}
