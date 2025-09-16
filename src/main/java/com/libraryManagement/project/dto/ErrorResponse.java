package com.libraryManagement.project.dto;

import lombok.Data;

@Data
public class ErrorResponse {
	private int errorCode;
	private String msg;

}
