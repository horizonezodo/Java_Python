package com.example.newapp.response;

import lombok.Data;

@Data
public class ResponseError {
    String errorMessage;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
