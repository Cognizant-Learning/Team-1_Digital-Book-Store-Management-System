package com.libraryManagement.project.dto;

public class AuthenticationResponseDTO {
	private String token;
	

	public AuthenticationResponseDTO(String token) {
		super();
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	
}
