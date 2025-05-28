package com.example.parkpal;

public class tempUser {
	private String username;
	private String password;
	private Boolean isAdmin;

	// Static instance to act like a session
	private static tempUser currentUser;

	public tempUser(String name, String pass) {
		this.username = name;
		this.password = pass;
		this.isAdmin = false;
	}

	// Call this to "log in" a user and create session
	public static void createSession(String name, String pass) {
		currentUser = new tempUser(name, pass);
	}

	public static tempUser getSession() {
		return currentUser;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
}
